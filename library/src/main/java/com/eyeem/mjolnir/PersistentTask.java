package com.eyeem.mjolnir;

import com.squareup.tape.Task;

/**
 * Created by vishna on 03/03/14.
 */
public class PersistentTask implements Task<ObservableRequestQueue> {

   RequestBuilder rb;

   public PersistentTask() { /*kryo*/ }
   public PersistentTask(RequestBuilder rb) {
      this.rb = rb;
   }

   @Override
   public void execute(ObservableRequestQueue requestQueue) {
      requestQueue.add(rb.persistent().build());
   }
}
