package com.eyeem.mjolnir;

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
   public PersistentRequest(RequestBuilder b, Response.Listener<Object> listener, Response.ErrorListener errorListener) {
      super(b, Object.class, listener, errorListener);
   }

   @Override
   protected Response<Object> parseNetworkResponse(NetworkResponse response) {
      try {
         String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
         JSONObject jsonObject = new JSONObject(jsonString);
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

      public PersistentRequest build() {
         return new PersistentRequest(rb, listener, errorListener);
      }
   }
}
