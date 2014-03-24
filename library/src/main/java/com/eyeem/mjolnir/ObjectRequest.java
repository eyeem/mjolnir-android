package com.eyeem.mjolnir;

import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * Created by vishna on 16/11/13.
 */
public class ObjectRequest extends JsonRequest<Object> {
   protected RequestBuilder b;
   Class clazz;

   public ObjectRequest(RequestBuilder b, Class clazz, Response.Listener<Object> listener,
                      Response.ErrorListener errorListener) {
      super(b.method, b.toUrl(), null, listener, errorListener);
      this.b = b;
      this.clazz = clazz;
   }

   protected Object fromJSON(JSONObject jsonObject) {
      return fromJSON(clazz, jsonObject);
   }

   @Override
   protected Response<Object> parseNetworkResponse(NetworkResponse response) {
      try {
         String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
         JSONObject jsonObject = new JSONObject(jsonString);
         return Response.success(fromJSON(b.declutter == null ? jsonObject : b.declutter.jsonObject(jsonObject)),
            HttpHeaderParser.parseCacheHeaders(response));
      } catch (UnsupportedEncodingException e) {
         return Response.error(new ParseError(e));
      } catch (JSONException je) {
         return Response.error(new ParseError(je));
      }
   }

   @Override
   public Map<String, String> getHeaders() throws AuthFailureError {
      return b.headers;
   }

   protected static Object fromJSON(Class clazz, JSONObject jsonObject) {
      try {
         java.lang.reflect.Method fromJSON = clazz.getMethod("fromJSON", JSONObject.class);
         return fromJSON.invoke(null, jsonObject);
      } catch (NoSuchMethodException e) {
         return null;
      } catch (InvocationTargetException e) {
         return null;
      } catch (IllegalAccessException e) {
         return null;
      }
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
}
