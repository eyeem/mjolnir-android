package com.eyeem.mjolnir;

import android.text.TextUtils;

import com.android.volley.Request;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vishna on 29/10/13.
 */
public class RequestBuilder implements Serializable {
   public String host;
   public String path;
   public PathDeclutter declutter;
   public HashMap<String, String> params = new HashMap<String, String>();
   public HashMap<String, String> headers = new HashMap<String, String>();
   public HashMap<String, String> files = new HashMap<String, String>();
   public Account account;
   public int method = Request.Method.GET; // GET by default
   public String content;
   public String content_type;

   public RequestBuilder() { /*kryo*/ }

   public RequestBuilder(String host, String path) {
      this.host = host;
      this.path = path;
   }

   public RequestBuilder with(Account account) {
      this.account = account;
      return this;
   }

   public RequestBuilder get() {
      method = Request.Method.GET;
      return this;
   }

   public RequestBuilder put() {
      method = Request.Method.PUT;
      return this;
   }

   public RequestBuilder delete() {
      method = Request.Method.DELETE;
      return this;
   }

   public RequestBuilder post() {
      method = Request.Method.POST;
      return this;
   }

   public String method() {
      switch (method) {
         case Request.Method.POST : return "POST";
         case Request.Method.PUT : return "PUT";
         case Request.Method.DELETE : return "DELETE";
         case Request.Method.GET :
         default: return "GET";
      }
   }

   public RequestBuilder content(String content, String content_type) {
      this.content = content;
      this.content_type = content_type;
      return this;
   }

   public RequestBuilder header(String key, String value) {
      headers.put(key, value);
      return this;
   }

   public RequestBuilder filepath(String key, String filepath) {
      if (!TextUtils.isEmpty(filepath) && new File(filepath).exists())
      files.put(key, filepath);
      return this;
   }

   public RequestBuilder param(String key, String value) {
      if (TextUtils.isEmpty(value))
         return this;
      params.put(key, value);
      return this;
   }

   public RequestBuilder param(String key, int value) {
      params.put(key, String.valueOf(value));
      return this;
   }

   public RequestBuilder param(String key, long value) {
      params.put(key, String.valueOf(value));
      return this;
   }

   public RequestBuilder params(HashMap<String, String> params) {
      this.params.putAll(params);
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
      if (params.size() > 0 && method != Request.Method.PUT && method != Request.Method.POST) {
         sb.append('?').append(toQuery());
      }
      return sb.toString();
   }

   public SyncClient sync() {
      sign();
      return new SyncClient(this);
   }

   public VolleyListRequestExecutor listOf(Class clazz) {
      sign();
      return new VolleyListRequestExecutor(this, clazz);
   }

   public VolleyObjectRequestExecutor objectOf(Class clazz) {
      sign();
      return new VolleyObjectRequestExecutor(this, clazz);
   }

   public StorageRequestExecutor store(Class clazz) {
      sign();
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

   /**
    * Returns a copy of the object, or null if the object cannot
    * be serialized.
    */
   public RequestBuilder copy() {
      // TODO use kryo for deep copying
      RequestBuilder copy = null;
      try {
         // Write the object out to a byte array
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         ObjectOutputStream out = new ObjectOutputStream(bos);
         out.writeObject(this);
         out.flush();
         out.close();

         // Make an input stream from the byte array and read
         // a copy of the object back in.
         ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
         copy = (RequestBuilder) in.readObject();
      }
      catch(IOException e) {}
      catch(ClassNotFoundException cnfe) {}
      return copy;
   }
}
