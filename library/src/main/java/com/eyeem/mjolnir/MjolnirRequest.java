package com.eyeem.mjolnir;

import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by vishna on 26/03/14.
 */
public class MjolnirRequest<T> extends JsonRequest<T> {

   protected RequestBuilder b;
   protected Class clazz;

   public MjolnirRequest(RequestBuilder b, Class clazz, Response.Listener<T> listener,
                         Response.ErrorListener errorListener) {
      super(b.method, b.toUrl(), null, listener, errorListener);
      this.b = b;
      this.clazz = clazz;
   }

   @Override public Map<String, String> getHeaders() throws AuthFailureError {
      return b.headers;
   }

   public RequestBuilder getRequestBuilder() {return b;}

   @Override public String getBodyContentType() {
      if (b.method != Request.Method.PUT && b.method != Request.Method.POST)
         return super.getBodyContentType();

      if (!TextUtils.isEmpty(b.content)) { // string content, e.g. json
         return b.content_type;
      } else if (b.files.entrySet().size() == 0) {
         return "application/x-www-form-urlencoded;charset=UTF-8";
      }

      // TODO multipart upload??? volley not so good for this?
      return super.getBodyContentType();
   }

   @Override public byte[] getBody() {
      if (b.method != Request.Method.PUT && b.method != Request.Method.POST)
         return super.getBody();

      if (!TextUtils.isEmpty(b.content)) { // string content, e.g. json
         return b.content.getBytes();
      } else if (b.files.entrySet().size() == 0) {
         try {
            return b.toQuery().getBytes("UTF-8");
         } catch (UnsupportedEncodingException e) {
            return null;
         }
      }

      // TODO multipart upload??? volley not so good for this?
      // can we stream here?
      return super.getBody();
   }

   @Override public boolean equals(Object o) {
      if (!(o instanceof MjolnirRequest)) return false;
      return b.toUrl().equals(((MjolnirRequest)o).b.toUrl()) && b.method == ((MjolnirRequest)o).b.method;
   }

///// if you need to operate on raw data, just use the below to fit request to your needs
   public static MjolnirRequest<Object> raw(RequestBuilder b) {
      return new MjolnirRequest<Object>(b, null, dummy, null);
   }

   @Override protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
      try {
         String raw = new String(networkResponse.data, HttpHeaderParser.parseCharset(networkResponse.headers));
         return Response.success(
            (T)raw,
            HttpHeaderParser.parseCacheHeaders(networkResponse));
      } catch (UnsupportedEncodingException e) {
         return Response.error(new ParseError(e));
      }
   }

   static Response.Listener<Object> dummy = new Response.Listener<Object>() { @Override public void onResponse(Object o) {} };
}
