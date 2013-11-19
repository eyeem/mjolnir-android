package com.eyeem.mjolnir;

import android.text.TextUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vishna on 29/10/13.
 */
public class RequestBuilder implements Serializable, Cloneable {
   final public String host;
   final public String path;
   public PathDeclutter declutter;
   public HashMap<String, String> params = new HashMap<String, String>();
   public HashMap<String, String> headers = new HashMap<String, String>();
   public Account account;

   public RequestBuilder(String host, String path) {
      this.host = host;
      this.path = path;
   }

   public RequestBuilder with(Account account) {
      this.account = account;
      return this;
   }

   public RequestBuilder header(String key, String value) {
      headers.put(key, value);
      return this;
   }

   public RequestBuilder param(String key, String value) {
      params.put(key, value);
      return this;
   }

   public RequestBuilder param(String key, int value) {
      params.put(key, String.valueOf(value));
      return this;
   }

   public RequestBuilder declutter(PathDeclutter declutter) {
      this.declutter = declutter;
      return this;
   }

   public RequestBuilder jsonpath(String path) {
      return declutter(new PathDeclutter(path));
   }

   public String toQuery() {
      ArrayList<String> pairs = new ArrayList<String>();
      try {
      for (Map.Entry<String, String> entry : params.entrySet()) {
         pairs.add(String.format("%s=%s",
            URLEncoder.encode(entry.getKey(), "UTF-8"),
            URLEncoder.encode(entry.getValue(), "UTF-8")
         ));
      }
      } catch (UnsupportedEncodingException uee) {}
      return TextUtils.join("&", pairs);
   }

   public String toUrl() {
      StringBuilder sb = new StringBuilder();
      sb.append(host).append(path);
      if (params.size() > 0) {
         sb.append('?').append(toQuery());
      }
      return sb.toString();
   }

   public VolleyListRequestExecutor listOf(Class clazz) {
      return new VolleyListRequestExecutor(this, clazz);
   }

   public VolleyObjectRequestExecutor objectOf(Class clazz) {
      return new VolleyObjectRequestExecutor(this, clazz);
   }

   public StorageRequestExecutor store(Class clazz) {
      return new StorageRequestExecutor(this, clazz);
   }

   public RequestBuilder fetchFront(Object info) {
      return this;
   }

   public RequestBuilder fetchBack(Object info) {
      return this;
   }

   public RequestBuilder sign() {
      if (account != null) {
         account.sign(this);
      }
      return this;
   }

   @Override public RequestBuilder clone() {
      try {
         return (RequestBuilder) super.clone();
      } catch (CloneNotSupportedException e) {
         e.printStackTrace();
         return null;
      }
   }
}
