package com.eyeem.mjolnir;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by vishna on 22/11/13.
 */
public class SyncClient {

   /** Copied from Volley/Request. */
   public interface Method {
      int DEPRECATED_GET_OR_POST = -1;
      int GET = 0;
      int POST = 1;
      int PUT = 2;
      int DELETE = 3;
      int HEAD = 4;
      int OPTIONS = 5;
      int TRACE = 6;
      int PATCH = 7;
   }

   RequestBuilder rb;

   public SyncClient(RequestBuilder rb) {
      this.rb = rb;
   }

   public JSONObject json() throws Exception {
      return new JSONObject(raw());
   }

   public JSONObject jsonFromPath() throws Exception {
      return rb.declutter == null ? json() : rb.declutter.jsonObject(json());
   }

   public <E extends Object> E objectOf(Class clazz) throws Exception {
      return (E) ObjectRequest.fromJSON(clazz, rb.declutter == null ? json() : rb.declutter.jsonObject(json()));
   }

   public <E extends List> E  listOf(Class clazz) throws Exception {
      return (E) ListRequest.fromArray(clazz, rb.declutter.jsonArray(json()));
   }

   public String raw() throws Exception {

      OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(Constants.CONNECTION_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
            .writeTimeout(Constants.CONNECTION_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
            .connectTimeout(Constants.CONNECTION_TIMEOUT_IN_SEC, TimeUnit.SECONDS)
            .build();

      Request.Builder okRB = new Request.Builder().url(rb.toUrl());

      // headers
      for (Map.Entry<String, String> header : rb.headers.entrySet() ) {
         okRB.header(header.getKey(), header.getValue());
      }

      // generate request body (if applicable)
      RequestBody body = null;
      if (rb.method == Method.POST || rb.method == Method.PUT || rb.method == Method.PATCH) {
         if (!TextUtils.isEmpty(rb.content)) { // string content, e.g. json
            MediaType mediaType = MediaType.parse(rb.content_type);
            body = RequestBody.create(mediaType, rb.content);
         } else if (rb.files.entrySet().size() == 0) { // url encoded
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8");
            body = RequestBody.create(mediaType, rb.toQuery());
         }
      }

      // set some request method
      okRB.method(rb.method(), body);

      Request request = okRB.build();

      int code = 0;
      String responseBody = null;

      try (Response response = client.newCall(request).execute()) {
         try {
            code = response.code();
            responseBody = response.body().string();
         } catch(Throwable t) {}
      }

      if (code >= 500 && code < 600) {
         throw new Mjolnir(rb, code);
      }

      if (code < 200 || code >= 300) {
         throw new Mjolnir(rb, code, responseBody);
      }

      if (Constants.DEBUG && code / 200 != 2) {
         Log.i(Constants.TAG, String.format("%d : %s", code, rb.toUrl()));
      }

      return responseBody;
   }

   public interface ProgressCallback {
      public void transferred(File file, long bytesUploaded, long totalBytes);
   }
}
