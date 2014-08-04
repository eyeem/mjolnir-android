package com.eyeem.mjolnir;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;

import java.lang.ref.WeakReference;
import java.util.NoSuchElementException;
import java.util.Vector;

import java.io.InterruptedIOException;

/**
 * Created by vishna on 03/03/14.
 */
public class PersistentTaskService extends Service implements ObservableRequestQueue.Listener {

   private static final String TAG = "Mjolnir:PersistentTaskService";
   private static final String KEY_TASK = "PersistenTask.key";

   private static ObservableRequestQueue requestQueue;
   private PersistenceHandler persistenceHandler;

   public static void setRequestQueue(ObservableRequestQueue requestQueue) {
      PersistentTaskService.requestQueue = requestQueue;
   }

   @Override
   public IBinder onBind(Intent intent) {
      return null;
   }

   @Override
   public void onStatusUpdate(Request request, int status, Object data) {
      if (!(request instanceof PersistentRequest)) return;

      PersistentRequest pr = (PersistentRequest) request;
      Log.i(TAG, "[" + status + "] onStatusUpdate: " + pr.getRequestBuilder().toUrl());

      switch (status) {
         case ObservableRequestQueue.STATUS_FAILED:
            boolean shouldAbort = false;
            if (data instanceof NoConnectionError) {
               NoConnectionError nce = (NoConnectionError) data;
               if (nce.getCause() instanceof InterruptedIOException) {
                  // i/o issues, abort to avoid duplicates
                  shouldAbort = true;
               } else {
                  // network unavailable
                  waitForNetworkConnected(this);
                  return;
               }
            }

            long retryTime = shouldAbort ? -1 : pr.task.onError(pr, data);

            if (retryTime == 0) {
               // don't remove the task from the queue, we will retry immediatelly
               break;
            }

            if (retryTime > 0) {
               // this will stop service and restart it the future
               Intent intent = new Intent(this, PersistentTaskService.class);
               PendingIntent pi = PendingIntent.getService(this,
                  0,
                  intent,
                  PendingIntent.FLAG_UPDATE_CURRENT);

               AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
               am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + retryTime, pi);
               stopSelf();
               return;
            }
            // intentional fallthru, will remove the task from the queue
         case ObservableRequestQueue.STATUS_CANCELLED:
         case ObservableRequestQueue.STATUS_SUCCESS:
            // if there was enqueued PersistenceHandler.NEXT in the meantime
            // make sure we don't pick what's on the peek of the persistent queue,
            // as we will duplicate ourselves
            Message m = new Message();
            m.what = PersistenceHandler.REMOVE;
            persistenceHandler.sendMessageAtFrontOfQueue(m);
            if (status == ObservableRequestQueue.STATUS_SUCCESS) {
               pr.task.onSuccess(pr, data);
            }
         case ObservableRequestQueue.STATUS_ALREADY_ADDED:
            break;
         default:
            // NO-OP;
      }
      persistenceHandler.sendEmptyMessage(PersistenceHandler.NEXT);
   }

   @Override
   public void onCreate() {
      super.onCreate();
      Log.i(TAG, "onCreate");
      requestQueue.registerListener(this);
      HandlerThread thread = new HandlerThread(TAG, Thread.MIN_PRIORITY);
      thread.start();
      persistenceHandler = new PersistenceHandler(thread.getLooper(), getApplication());
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      requestQueue.unregisterListener(this);
      persistenceHandler.getLooper().quit();
      persistenceHandler = null;
      Log.i(TAG, "onDestroy");
   }

   @Override public int onStartCommand(Intent intent, int flags, int startId) {
      if (intent != null && intent.hasExtra(KEY_TASK)) {
         PersistentTask task = (PersistentTask) intent.getSerializableExtra(KEY_TASK);
         add(task);
      }
      persistenceHandler.sendEmptyMessage(PersistenceHandler.NEXT);
      return START_STICKY;
   }

   private static boolean isNetworkConnected(Context context) {
      try {
         return null != ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
      } catch (Exception e) {
         return false;
      }
   }

   public static class NetworkStateReceiver extends BroadcastReceiver {
      @Override
      public void onReceive(Context context, Intent intent) {
         if (isNetworkConnected(context)) {
            startSerivceIfNecessary(context);
            setNetworkStateReceiverEnabled(context, false);
         }
      }
   }

   public static Intent persistentIntent(Context context, PersistentTask task) {
      Intent intent = new Intent(context, PersistentTaskService.class);
      intent.putExtra(KEY_TASK, task);
      return intent;
   }

   public static void addPersistentTask(Context context, PersistentTask task) {
      context.startService(persistentIntent(context, task));
   }

   public static void startSerivceIfNecessary(final Context context) {
      Thread t = new Thread(new Runnable() {
         @Override
         public void run() {
            PersistentTaskQueue persistentQueue = PersistentTaskQueue.create(context);
            if (persistentQueue != null && persistentQueue.peek() != null) {
               context.startService(new Intent(context, PersistentTaskService.class));
            }
         }
      });
      t.setPriority(Thread.MIN_PRIORITY);
      t.start();
   }

   private static void setNetworkStateReceiverEnabled(Context context, boolean value) {
      ComponentName receiver = new ComponentName(context, NetworkStateReceiver.class);
      try {
         context.getPackageManager().setComponentEnabledSetting(
            receiver,
            value ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP);
      } catch (NullPointerException npe) {
      }
   }

   private static void waitForNetworkConnected(Context context) {
      context.stopService(new Intent(context, PersistentTaskService.class));
      setNetworkStateReceiverEnabled(context, true);
   }

   private final static class PersistenceHandler extends Handler {

      PersistentTaskQueue pq;

      private PersistentTaskQueue persistentQueue () {
         if (pq == null) pq = PersistentTaskQueue.create(app);
         return pq;
      }

      private static final int NEXT = 2;
      private static final int ADD = 3;
      private static final int REMOVE = 4;

      private Application app;

      public PersistenceHandler(Looper looper, Application app) {
         super(looper);
         this.app = app;
      }

      @Override
      public void handleMessage(Message msg) {
         try {
            switch (msg.what) {
               case NEXT: next(); break;
               case REMOVE: persistentQueue().remove(); break;
               case ADD: persistentQueue().add((PersistentTask) msg.obj); break;
            }
         } catch (Throwable t) {
            // log tape failures
            android.util.Log.w(TAG, "handleMessage() failed", t);
         }
      }

      private void next() {
         Vector<Request> ongoingRequests = requestQueue.ongoing();
         boolean running = false;
         for (Object object : ongoingRequests) {
            running = running || object instanceof PersistentRequest;
         }

         if (running) return;

         PersistentTask task = persistentQueue().peek();
         if (task != null) {
            if (isNetworkConnected(app)) {
               try {
                  task.execute(requestQueue);
               } catch (IllegalStateException e) {
                  persistentQueue().remove();
                  next();
               }
            } else {
               waitForNetworkConnected(app);
            }
         } else {
            app.stopService(new Intent(app, PersistentTaskService.class));
         }
      }
   }

   private void add(PersistentTask task) {
      if (task == null) return;
      Message m = new Message();
      m.what = PersistenceHandler.ADD;
      m.obj = task;
      persistenceHandler.sendMessage(m);
   }
}
