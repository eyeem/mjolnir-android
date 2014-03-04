package com.eyeem.mjolnir;

import android.content.Context;

import com.squareup.tape.Task;

import java.io.Serializable;

/**
 * Created by vishna on 03/03/14.
 */
public class PersistentTask implements Task<ObservableRequestQueue>, Serializable {

   private RequestBuilder rb;

   public PersistentTask() { /*kryo*/ }

   public void setRequestBuilder(RequestBuilder rb) {
      this.rb = rb;
   }

   @Override
   public void execute(ObservableRequestQueue requestQueue) {
      requestQueue.add(rb.persistent().build(this));
   }

   public void onStart() {}
   public void onSuccess(PersistentRequest request, Object data) {}

   /**
    * @return true if task should be resumed
    */
   public boolean onError(PersistentRequest request, Object data) {
      return false;
   }

   public void start(Context context) {
      onStart();
      PersistentTaskService.addPersistentTask(context, this);
   }
}
