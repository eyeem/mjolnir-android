package com.eyeem.mjolnir;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.squareup.tape.Task;

import java.io.Serializable;

/**
 * Created by vishna on 03/03/14.
 */
public class PersistentTask implements Task<ObservableRequestQueue>, Serializable {

   private RequestBuilder rb;

   public PersistentTask() { /*kryo*/ }

   public PersistentTask setRequestBuilder(RequestBuilder rb) {
      this.rb = rb;
      return this;
   }

   @Override
   public void execute(ObservableRequestQueue requestQueue) {
      requestQueue.add(rb.persistent().build(this));
   }

   public void onStart() {}
   public void onSuccess(PersistentRequest request, Object data) {}

   /**
    * @return milliseconds to wait for a retry, negative value means abort
    */
   public long onError(PersistentRequest request, Object data) {
      return -1;
   }

   public void start(Context context) {
      onStart();
      PersistentTaskService.addPersistentTask(context, this);
   }

   public void startDelayed(Context context, long delay) {
      Intent intent = PersistentTaskService.persistentIntent(context, this);
      PendingIntent pi = PendingIntent.getService(
         context,
         0,
         intent,
         PendingIntent.FLAG_UPDATE_CURRENT);

      AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
      am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, pi);
   }
}
