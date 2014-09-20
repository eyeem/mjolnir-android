package com.eyeem.sdk.instagram;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.eyeem.mjolnir.DateParser;
import com.eyeem.mjolnir.RequestBuilder;
import com.eyeem.mjolnir.oauth.OAuth2Account;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Client side implict authentication
 *
 * Created by vishna on 28/10/13.
 */
public class Instagram extends RequestBuilder {

   public final static String SCOPE_BASIC = "basic";
   public final static String SCOPE_COMMENTS = "comments";
   public final static String SCOPE_RELATIONSHIPS = "relationships";
   public final static String SCOPE_LIKES = "likes";

   public final static List<String> ALL_THE_SCOPES = Arrays.asList(SCOPE_BASIC, SCOPE_COMMENTS, SCOPE_LIKES, SCOPE_RELATIONSHIPS);

   private final static String API_URL = "https://api.instagram.com";

   public static String ID = "";
   public static String SECRET = "";
   public static String CALLBACK_URI = "";

   public static void init(String id, String secret, String callback_uri) {
      Account.registerAccountType("instagram", Instagram.Account.class);
      Instagram.ID = id;
      Instagram.SECRET = secret;
      Instagram.CALLBACK_URI = callback_uri;
   }

   public Instagram() { /*kryo*/ }

   private Instagram(String path) {
      super(API_URL, path);
      param("client_id", ID);
      //header("X-Api-Version", "2.3.2");
      //header("X-hourOfDay", String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
      if (default_headers != null) headers.putAll(default_headers.get());
   }

   public static Instagram path(String path) {
      return new Instagram(path);
   }

//// API CALLS
   public static Instagram user(String id) {
      return (Instagram) new Instagram("/v1/users/" + id).jsonpath("data");
   }


   static {
      new DateParser("com.eyeem.sdk.instagram") {
         @Override public long toSeconds(String date) {
            try { return new SimpleDateFormat(
               "yyyy-MM-dd'T'HH:mm:ssZ",
               Locale.getDefault()).parse(date).getTime()/1000;
            } catch (ParseException e) { return 0; }
         }
      };
   }

   public static class Account extends OAuth2Account {

      public static List<String> SCOPES;

      public final static String TYPE = "instagram";

      public IGUser user;

      public Account() {
         type = TYPE;
      }

      public static Account fromJSON(Account account, JSONObject json) {
         OAuth2Account.fromJSON(account, json);
         account.user = IGUser.fromJSON(json.optJSONObject("user"));
         return account;
      }

      public static Account fromJSON(JSONObject json) {
         return fromJSON(new Account(), json);
      }

      @Override
      public String authorizeUrl() {
         return String.format(
            "https://api.instagram.com/oauth/authorize/?response_type=token&client_id=%s&redirect_uri=%s&scope=%s",
            ID,
            callbackUrl(),
            SCOPES == null ? "" : TextUtils.join("+", SCOPES));
      }

      @Override public String callbackUrl() { return CALLBACK_URI; }
      @Override public String secret() { return SECRET; }
      @Override public RequestBuilder oauthRequest() { return new Instagram("/oauth/access_token").post(); }
      @Override public String avatarUrl() { return user != null ? user.profile_picture : ""; }
      @Override public String displayName() {
         if (user == null)
            return "";
         else if (!TextUtils.isEmpty(user.full_name))
            return user.full_name;
         else if (!TextUtils.isEmpty(user.username))
            return user.username;
         return "";
      }

      @Override public RequestBuilder sign(RequestBuilder requestBuilder) {
         return requestBuilder.param("access_token", accessToken);
      }

      @Override
      public JSONObject toJSON(JSONObject json) throws JSONException {
         json.put("user", user.toJSON());
         return super.toJSON(json);
      }

      @Override public void postAuth(RequestQueue queue, final Context context, final WeakReference<OAuth2Account.UICallback> _callback) {
         Instagram.user("self")
            .with(Account.this)
            .objectOf(IGUser.class)
            .listener(new Response.Listener<Object>() {
               @Override
               public void onResponse(Object o) {
                  user = (IGUser) o;
                  id = user.id;
                  save(context);
                  UICallback callback = _callback.get();
                  if (callback != null) {
                     callback.onPostAuth(Instagram.Account.this);
                  }
               }
            })
            .enqueue(queue);
      }
   }

   public static DefaultHeaders default_headers;

   public interface DefaultHeaders {
      public HashMap<String, String> get();
   }
}
