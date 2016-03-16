package com.eyeem.sdk.fhpx;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.eyeem.mjolnir.RequestBuilder;
import com.eyeem.mjolnir.oauth.Auth1;
import com.eyeem.mjolnir.oauth.OAuth1Account;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by vishna on 15/03/16.
 */
public class FHPX extends RequestBuilder {

   private final static String API_URL = "https://api.500px.com";

   public static String CONSUMER_KEY = "";
   public static String CONSUMER_SECRET = "";
   public static String CALLBACK_URI = "";

   public static void init(String key, String secret, String callback_uri) {
      Account.registerAccountType(Account.TYPE, FHPX.Account.class);
      FHPX.CONSUMER_KEY = key;
      FHPX.CONSUMER_SECRET = secret;
      FHPX.CALLBACK_URI = callback_uri;
   }

   public FHPX() { /* kryo */ }
   protected FHPX(String path) {
      super(API_URL, path);
   }

   public static FHPX path(String path) {
      return new FHPX(path);
   }

   public static FHPX users() {
      return (FHPX) path("/v1/users").jsonpath("user");
   }

   public static class Account extends OAuth1Account {

      public final static String TYPE = "500px";

      public FHUser user;

      public Account() {
         type = TYPE;
      }

      public static Account fromJSON(Account account, JSONObject json) {
         OAuth1Account.fromJSON(account, json);
         account.user = FHUser.fromJSON(json.optJSONObject("user"));
         return account;
      }

      public static Account fromJSON(JSONObject json) {
         return fromJSON(new Account(), json);
      }

      @Override public String consumerKey() { return CONSUMER_KEY; }
      @Override public String consumerSecret() { return CONSUMER_SECRET; }
      @Override public String oauthCallback() { return CALLBACK_URI; }

      @Override public RequestBuilder oauthRequestToken() {
         return path("/v1/oauth/request_token").post();
      }

      @Override public RequestBuilder authorizeUrl(Auth1 auth) {
         return path("/v1/oauth/authorize")
            .param("oauth_token", auth.oauth_token)
            .param("oauth_callback", oauthCallback()); // mandatory for 500px
      }

      @Override public RequestBuilder oauthAccessToken() {
         return path("/v1/oauth/access_token").post();
      }

      @Override public JSONObject toJSON(JSONObject json) throws JSONException {
         json.put("user", user.toJSON());
         return super.toJSON(json);
      }

      @Override
      public void postAuth(RequestQueue queue, final Context context, final WeakReference<UICallback> _callback) {
         FHPX.users()
            .with(this)
            .objectOf(FHUser.class)
            .listener(new Response.Listener<Object>() {
               @Override
               public void onResponse(Object o) {
                  user = (FHUser) o;
                  id = user.id;
                  save(context);
                  UICallback callback = _callback.get();
                  if (callback != null) {
                     callback.onPostAuth(FHPX.Account.this);
                  }
               }
            })
            .enqueue(queue);
      }

      @Override public String avatarUrl() {
         try {
            return user.avatars.large.https;
         } catch (NullPointerException npe) {
            return "";
         }
      }

      @Override public String userName() {
         return (user == null || TextUtils.isEmpty(user.username)) ? "" : user.username;
      }

      @Override public String fullName() {
         return (user == null || TextUtils.isEmpty(user.fullname)) ? "" : user.fullname;
      }
   }
}
