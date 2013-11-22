package com.eyeem.mjolnir;

import com.android.volley.toolbox.HurlStack;
import com.squareup.okhttp.OkHttpClient;
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
   private final OkHttpClient client;

   public OkHttpStack() {
      this(new OkHttpClient());
   }

   public OkHttpStack(OkHttpClient client) {
      if (client == null) {
         throw new NullPointerException("Client must not be null.");
      }
      this.client = client;
   }

   public static OkHttpStack withSslWorkaround() {
      OkHttpClient client = new OkHttpClient();
      SSLContext sslContext; // workaround for okhttp issue #184, should be fixed in 2.0
      try {
         sslContext = SSLContext.getInstance("TLS");
         sslContext.init(null, null, null);
      } catch (GeneralSecurityException e) {
         throw new AssertionError(); // The system has no TLS. Just give up.
      }
      client.setSslSocketFactory(sslContext.getSocketFactory());
      return new OkHttpStack(client);
   }

   @Override protected HttpURLConnection createConnection(URL url) throws IOException {
      return client.open(url);
   }
}
