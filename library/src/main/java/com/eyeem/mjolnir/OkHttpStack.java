package com.eyeem.mjolnir;

import com.android.volley.toolbox.HurlStack;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

/**
 * An {@link com.android.volley.toolbox.HttpStack HttpStack} implementation which
 * uses OkHttp as its transport.
 */
public class OkHttpStack extends HurlStack {
   private final OkUrlFactory client;

   public OkHttpStack() {
      this(new OkHttpClient());
   }

   public OkHttpStack(OkHttpClient client) {
      if (client == null) {
         throw new NullPointerException("Client must not be null.");
      }
      this.client = new OkUrlFactory(client);
   }

   public static OkHttpStack newInstance() {
      OkHttpClient client = new OkHttpClient();
      return new OkHttpStack(client);
   }

   @Override protected HttpURLConnection createConnection(URL url) throws IOException {
      return client.open(url);
   }
}
