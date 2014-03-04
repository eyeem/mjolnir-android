package com.eyeem.mjolnir;

import android.app.Application;
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
import java.util.Vector;

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
            if (data instanceof NoConnectionError) {
               waitForNetworkConnected(this);
               return;
            }
            break;
         case ObservableRequestQueue.STATUS_CANCELLED:
         case ObservableRequestQueue.STATUS_SUCCESS:
         case ObservableRequestQueue.STATUS_ALREADY_ADDED:
            persistenceHandler.sendEmptyMessage(PersistenceHandler.REMOVE);
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
      if (intent.hasExtra(KEY_TASK)) {
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

   public static void addPersistentTask(Context context, PersistentTask task) {
      Intent intent = new Intent(context, PersistentTaskService.class);
      intent.putExtra(KEY_TASK, task);
      context.startService(intent);
   }

   public static void startSerivceIfNecessary(final Context context) {
      Thread t = new Thread(new Runnable() {
         @Override
         public void run() {
            PersistentTaskQueue persistentQueue = PersistentTaskQueue.create(context);
            if (persistentQueue.peek() != null) {
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
         switch (msg.what) {
            case NEXT: next(); break;
            case REMOVE: persistentQueue().remove(); break;
            case ADD: persistentQueue().add((PersistentTask) msg.obj); break;
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
               task.execute(requestQueue);
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
