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
public class ObjectRequest extends MjolnirRequest<Object> {
   public ObjectRequest(RequestBuilder b, Class clazz, Response.Listener<Object> listener,
                      Response.ErrorListener errorListener) {
      super(b, clazz, listener, errorListener);
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
}
