package com.eyeem.mjolnir;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
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
 * Created by vishna on 30/10/13.
 */
public class ListRequest extends JsonRequest<List> {

   RequestBuilder b;
   Class clazz;

   public ListRequest(RequestBuilder b, Class clazz, Response.Listener<List> listener,
                      Response.ErrorListener errorListener) {
      super(Method.GET, b.toUrl(), null, listener, errorListener);
      this.b = b;
      this.clazz = clazz;
   }

   protected List fromArray(JSONArray jsonArray) {
      try {
         java.lang.reflect.Method fromJSONArray = clazz.getMethod("fromJSONArray", JSONArray.class);
         return (List)fromJSONArray.invoke(null, jsonArray);
      } catch (NoSuchMethodException e) {
         return null;
      } catch (InvocationTargetException e) {
         return null;
      } catch (IllegalAccessException e) {
         return null;
      }
   }

   @Override
   protected Response<List> parseNetworkResponse(NetworkResponse response) {
      try {
         String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
         JSONObject jsonObject = new JSONObject(jsonString);
         return Response.success(fromArray(b.declutter.jsonArray(jsonObject)), HttpHeaderParser.parseCacheHeaders(response));
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
}
