package com.eyeem.mjolnir;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;

import java.util.List;

/**
 * Created by vishna on 29/10/13.
 */
public class VolleyListRequestExecutor {

   RequestBuilder requestBuilder;
   Response.Listener<List> listener;
   Response.ErrorListener errorListener;
   MjolnirRequest.OnParsedListener parsedListener;
   Class objectClass;

   public VolleyListRequestExecutor(com.eyeem.mjolnir.RequestBuilder requestBuilder, Class objectClass) {
      this.requestBuilder = requestBuilder.sign();
      this.objectClass = objectClass;
   }

   public VolleyListRequestExecutor listener(Response.Listener<List> listener) {
      this.listener = listener;
      return this;
   }

   public VolleyListRequestExecutor errorListener(Response.ErrorListener errorListener) {
      this.errorListener = errorListener;
      return this;
   }

   public VolleyListRequestExecutor parsedListener(MjolnirRequest.OnParsedListener parsedListener) {
      this.parsedListener = parsedListener;
      return this;
   }

   public MjolnirRequest build() {
      if (listener == null) { listener = dummy; }
      return new ListRequest(requestBuilder, objectClass, listener, errorListener).onParsedListener(parsedListener);
   }

   public void enqueue(RequestQueue queue) {
      queue.add(build());
   }

   static Response.Listener<List> dummy = new Response.Listener<List>() { @Override public void onResponse(List o) {} };
}
