package com.eyeem.mjolnir.oauth;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.eyeem.mjolnir.Account;
import com.eyeem.mjolnir.MjolnirRequest;
import com.eyeem.mjolnir.RequestBuilder;
import com.eyeem.mjolnir.oauth.utils.PercentEscaper;

import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by vishna on 16/03/16.
 */
public abstract class OAuth1Account extends Account {

   protected String token;
   protected String tokenSecret;

   public abstract String consumerKey();
   public abstract String consumerSecret();
   public abstract String oauthCallback();
   public abstract RequestBuilder authorizeUrl(Auth1 auth);

   public abstract RequestBuilder oauthRequestToken();
   public abstract RequestBuilder oauthAccessToken();

   private void setAuth(Auth1 auth) {
      this.token = auth.oauth_token;
      this.tokenSecret = auth.oauth_token_secret;
   }

   @Override public RequestBuilder sign(RequestBuilder requestBuilder) {
      // TODO implement
      return null;
   }

   public void requestRequestToken(
      final RequestQueue queue,
      final Context c,
      UICallback callback) {

      final Context context = c.getApplicationContext();

      final WeakReference<UICallback> _callback = new WeakReference<UICallback>(callback);
      final String timestamp = Long.toString(System.currentTimeMillis() / 1000L);

      RequestBuilder rb = oauthRequestToken()
         .param("oauth_callback", oauthCallback())
         .param("oauth_consumer_key", consumerKey())
         .param("oauth_nonce", nonce())
         .param("oauth_signature_method", "HMAC-SHA1")
         .param("oauth_timestamp", timestamp)
         .param("oauth_version", "1.0");

      String oauth_signature_string = rb.method() + "&" + percentEncode(rb.justUrl()) + "&" + percentEncode(rb.toQuery());
      String oauth_signature = HmacSHA1Signature(oauth_signature_string, consumerSecret() + "&");
      rb.param("oauth_signature", oauth_signature);

      MjolnirRequest<Object> mr = new MjolnirRequest<>(
         rb, null,
         new Response.Listener<Object>() {
            @Override public void onResponse(Object response) {
               UICallback callback = _callback.get();
               try {
                  HashMap<String, String> v = paramsFromEncodedQuery(response.toString());
                  Auth1 auth = new Auth1();
                  auth.oauth_token = v.get("oauth_token");
                  auth.oauth_token_secret = v.get("oauth_token_secret");
                  auth.oauth_callback_confirmed = "true".equals(v.get("oauth_callback_confirmed"));

                  if (auth.oauth_callback_confirmed) {
                     callback.onRequestTokenGranted(auth);
                  } else {
                     throw new RuntimeException("Failed to authenticate user");
                  }
               } catch (Throwable e) {
                  if (callback != null) {
                     callback.fail(e);
                  }
               }
            }
         },
         new Response.ErrorListener() {
            @Override public void onErrorResponse(VolleyError error) {
               UICallback callback = _callback.get();
               if (callback != null) {
                  callback.fail(error);
               }
            }
         }
      );

      queue.add(mr);
   }

   public void requestAccessToken(
      final Auth1 auth,
      final RequestQueue queue,
      final Context c,
      UICallback callback) throws Exception {

      if (auth == null || TextUtils.isEmpty(auth.oauth_token) || TextUtils.isEmpty(auth.oauth_verifier)) {
         throw new IllegalStateException("Auth incomplete");
      }

      final Context context = c.getApplicationContext();

      final WeakReference<UICallback> _callback = new WeakReference<UICallback>(callback);
      final String timestamp = Long.toString(System.currentTimeMillis() / 1000L);

      RequestBuilder rb = oauthAccessToken()
         .param("oauth_consumer_key", consumerKey())
         .param("oauth_nonce", nonce())
         .param("oauth_timestamp", timestamp)
         .param("oauth_token", auth.oauth_token)
         .param("oauth_signature_method", "HMAC-SHA1")
         .param("oauth_verifier", auth.oauth_verifier)
         .param("oauth_version", "1.0");

      String oauth_signature_string = rb.method() + "&" + percentEncode(rb.justUrl()) + "&" + percentEncode(rb.toQuery());
      String oauth_signature = HmacSHA1Signature(oauth_signature_string, consumerSecret() + "&" + auth.oauth_token_secret);
      rb.param("oauth_signature", oauth_signature);

      MjolnirRequest<Object> mr = new MjolnirRequest<>(
         rb, null,
         new Response.Listener<Object>() {
            @Override public void onResponse(Object response) {
               UICallback callback = _callback.get();
               try {
                  HashMap<String, String> v = paramsFromEncodedQuery(response.toString());
                  Auth1 auth = new Auth1();
                  auth.oauth_token = v.get("oauth_token");
                  auth.oauth_token_secret = v.get("oauth_token_secret");

                  if (!TextUtils.isEmpty(auth.oauth_token) && !TextUtils.isEmpty(auth.oauth_token)) {
                     setAuth(auth);
                     callback.onAccessTokenGranted(OAuth1Account.this);
                  } else {
                     throw new IllegalStateException("Failed to login user");
                  }
               } catch (Throwable e) {
                  if (callback != null) {
                     callback.fail(e);
                  }
               }
            }
         },
         new Response.ErrorListener() {
            @Override public void onErrorResponse(VolleyError error) {
               UICallback callback = _callback.get();
               if (callback != null) {
                  callback.fail(error);
               }
            }
         }
      );

      queue.add(mr);
   }

   private String nonce(){
      return Integer.toString(new Random().nextInt());
   }

   public static String HmacSHA1Signature(String data, String key) {
      try {
         byte[] hmacData;

         SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");
         Mac mac = Mac.getInstance("HmacSHA1");
         mac.init(secretKey);
         hmacData = mac.doFinal(data.getBytes());

         return base64(hmacData);
      } catch (Exception e) {
         return "";
      }
   }

   public static String base64(byte[] data) {
      return Base64.encodeToString(data, Base64.DEFAULT);
   }

   public static HashMap<String, String> paramsFromEncodedQuery(String s){
      HashMap<String, String> map = new HashMap<>();
      for(String t : s.split("\\&")){
         final int equals = t.indexOf('=');
         String name;
         String value;
         if (equals < 0) {
            name = percentDecode(t);
            value = null;
         } else {
            name = percentDecode(t.substring(0, equals));
            value = percentDecode(t.substring(equals + 1));
         }
         map.put(name, value);
      }
      return map;
   }

   private static final PercentEscaper percentEncoder = new PercentEscaper("-._~", false);

   public static String percentEncode(String s) {
      if (s == null) return "";
      return percentEncoder.escape(s);
   }

   public static String percentDecode(String s) {
      try { return URLDecoder.decode(s, "UTF-8"); } catch (Throwable t) { return ""; }
   }

   public interface UICallback {
      void onRequestTokenGranted(Auth1 auth);
      void onAccessTokenGranted(OAuth1Account account);
      void fail(Throwable error);
   }
}
