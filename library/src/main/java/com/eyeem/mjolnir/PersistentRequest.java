package com.eyeem.mjolnir;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by vishna on 03/03/14.
 */
public class PersistentRequest extends ObjectRequest {

   public NetworkResponse response;
   public JSONObject jsonObject;
   public PersistentTask task;

   public PersistentRequest(RequestBuilder b, PersistentTask task, Response.Listener<Object> listener, Response.ErrorListener errorListener) {
      super(b, Object.class, listener, errorListener);
      this.task = task;
      setRetryPolicy(new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                0, // we disable retry policy as this is handled by PersistentService
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
   }

   @Override
   protected Response<Object> parseNetworkResponse(NetworkResponse response) {
      try {
         this.response = response;
         String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
         jsonObject = new JSONObject(jsonString);
         jsonObject = b.declutter == null ? jsonObject : b.declutter.jsonObject(jsonObject);
         return Response.success((Object)jsonObject,
            HttpHeaderParser.parseCacheHeaders(response));
      } catch (UnsupportedEncodingException e) {
         return Response.error(new ParseError(e));
      } catch (JSONException je) {
         return Response.error(new ParseError(je));
      }
   }

   public static class Builder {
      RequestBuilder rb;
      Response.Listener<Object> listener;
      Response.ErrorListener errorListener;

      Builder(RequestBuilder rb) {
         this.rb = rb;
      }

      public Builder listener(Response.Listener<Object> listener) {
         this.listener = listener;
         return this;
      }

      public Builder errorListener(Response.ErrorListener errorListener) {
         this.errorListener = errorListener;
         return this;
      }

      public PersistentRequest build(PersistentTask task) {
         if (listener == null) {
            // dummy listener to avoid crashes
            listener = new Response.Listener<Object> () {
               @Override
               public void onResponse(Object o) {}
            };
         }
         return new PersistentRequest(rb.sign(), task, listener, errorListener);
      }
   }
}
