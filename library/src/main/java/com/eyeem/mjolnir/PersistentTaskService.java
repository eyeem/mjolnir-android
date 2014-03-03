package com.eyeem.mjolnir;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.android.volley.Request;

import java.util.Vector;

/**
 * Created by vishna on 03/03/14.
 */
public class PersistentTaskService extends Service implements ObservableRequestQueue.Listener {

   private static final String TAG = "Mjolnir:PersistentTaskService";

   private PersistentTaskQueue persistentQueue;
   private static ObservableRequestQueue requestQueue;

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
         case ObservableRequestQueue.STATUS_CANCELLED:
         case ObservableRequestQueue.STATUS_SUCCESS:
         case ObservableRequestQueue.STATUS_ALREADY_ADDED:
            persistentQueue.remove();
            break;
         default:
            // NO-OP;
      }
      executeNext();
   }

   @Override
   public void onCreate() {
      super.onCreate();
      persistentQueue = PersistentTaskQueue.create(this);
      requestQueue.registerListener(this);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      requestQueue.unregisterListener(this);
   }

   @Override public int onStartCommand(Intent intent, int flags, int startId) {
      executeNext();
      return START_STICKY;
   }

   private void executeNext() {
      Vector<Request> ongoingRequests = requestQueue.ongoing();
      boolean running = false;
      for (Object object : ongoingRequests) {
         running = running || object instanceof PersistentRequest;
      }

      if (running) return;

      PersistentTask task = persistentQueue.peek();
      if (task != null) {
         task.execute(requestQueue);
      } else {
         Log.i(TAG, "Service stopping!");
         stopSelf(); // No more tasks are present. Stop.
      }
   }
}
