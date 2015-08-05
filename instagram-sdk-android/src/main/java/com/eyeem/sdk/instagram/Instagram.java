package com.eyeem.sdk.instagram;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.eyeem.mjolnir.DateParser;
import com.eyeem.mjolnir.RequestBuilder;
import com.eyeem.mjolnir.oauth.OAuth2Account;
import com.eyeem.storage.Storage;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
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

   public static Instagram userSelfFeed() {
      return (Instagram) new Instagram("/v1/users/self/feed").jsonpath("data");
   }

   public static Instagram userMediaRecent(String userId) {
      return (Instagram) new Instagram("/v1/users/" + userId + "/media/recent").jsonpath("data");
   }

   public static Instagram usersSearch(String query) {
      return (Instagram) new Instagram("/v1/users/search").param("q", query).jsonpath("data");
   }

   public static Instagram tagMediaRecent(String tagName) {
      return (Instagram) new Instagram("/v1/tags/" + tagName + "/media/recent").jsonpath("data");
   }

   public static Instagram locationMediaRecent(String locationId) {
      return (Instagram) new Instagram("/v1/locations/" + locationId + "/media/recent").jsonpath("data");
   }

   public static Instagram mediaComments(String mediaId) {
      return (Instagram) new Instagram("/v1/media/" + mediaId + "/comments").jsonpath("data");
   }

   public static Instagram postComment(String mediaId, String text) {
      return (Instagram) new Instagram("/v1/media/" + mediaId + "/comments").param("text", text).post();
   }

   static {
      new DateParser("com.eyeem.sdk.instagram") {
         @Override public long toSeconds(String date) {
            try { return Long.valueOf(date);
            } catch (Throwable e) { return 0; }
         }
      };
   }

   /**
    * Client side implict authentication
    */
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

      @Override public String userName() {
         return (user == null || TextUtils.isEmpty(user.username)) ? "" : user.username;
      }

      @Override public String fullName() {
         return (user == null || TextUtils.isEmpty(user.full_name)) ? "" : user.full_name;
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

//// Pagination support
   @Override
   public RequestBuilder fetchFront(Object info) {
      if (decapsulator != null) {
         return decapsulator.fetchFront(this, info);
      }
      return this;
   }

   @Override
   public RequestBuilder fetchBack(Object info) {
      if (decapsulator != null) {
         return decapsulator.fetchBack(this, info);
      }
      Storage.List list = (Storage.List) info;
      if (list.size() == 0) return this;
      return param("max_id", list.lastId());
   }

/// customizable pagination
   public interface PaginationDecapsulator {
      public RequestBuilder fetchBack(RequestBuilder rb, Object info);
      public RequestBuilder fetchFront(RequestBuilder rb, Object info);
   }

   public static PaginationDecapsulator decapsulator;
}
