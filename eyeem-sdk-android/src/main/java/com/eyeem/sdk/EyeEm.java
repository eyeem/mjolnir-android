package com.eyeem.sdk;

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
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by vishna on 28/10/13.
 */
public class EyeEm extends RequestBuilder {

   private static final String API_URL = "https://api.eyeem.com";

   public static String ID = "";
   public static String SECRET = "";
   public static String CALLBACK_URI = "";

   public static void init(String id, String secret, String callback_uri) {
      Account.registerAccountType("eyeem", EyeEm.Account.class);
      EyeEm.ID = id;
      EyeEm.SECRET = secret;
      EyeEm.CALLBACK_URI = callback_uri;
   }

   private EyeEm(String path) {
      super(API_URL, path);
      param("client_id", ID);
      header("X-Api-Version", "2.3.0");
      header("X-hourOfDay", String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
      headers.putAll(default_headers);
   }

   public static EyeEm path(String path) {
      return new EyeEm(path);
   }

   public static EyeEm discover() {
      return (EyeEm) new EyeEm("/v2/users/me/discover").jsonpath("discover");
   }

   public static EyeEm user(String id) {
      return (EyeEm) new EyeEm("/v2/users/" + id).jsonpath("user");
   }

   public static EyeEm userSearch(String query) {
      return (EyeEm) new EyeEm("/v2/users").param("q", query).jsonpath("users.items");
   }

   public static EyeEm userPhotos(String id) {
      return (EyeEm) new EyeEm("/v2/users/" + id + "/photos").jsonpath("photos.items");
   }

   public static EyeEm albumPhotos(String id) {
      return (EyeEm) new EyeEm("/v2/albums/" + id + "/photos").jsonpath("photos.items");
   }

   public static EyeEm albumSearch(String query) {
      return (EyeEm) new EyeEm("/v2/albums").param("q", query).jsonpath("albums.items");
   }

   public EyeEm defaults() {
      return detailed()
         .includeAlbums()
         .includeLikers()
         .includePeople()
         .includePhotos()
         .limit(30)
         .numComments(3)
         .numLikers(2);
   }

   public EyeEm detailed() {
      return (EyeEm)param("detailed", "1");
   }

   public EyeEm followers() {
      return (EyeEm)param("followers", "1");
   }

   public EyeEm limit(int count) {
      return (EyeEm)param("limit", count);
   }

   public EyeEm includeAlbums() {
      return (EyeEm)param("includeAlbums", "1");
   }

   public EyeEm includeLikers() {
      return (EyeEm)param("includeLikers", "1");
   }

   public EyeEm includePeople() {
      return (EyeEm)param("includePeople", "1");
   }

   public EyeEm includePhotos() {
      return (EyeEm)param("includePhotos", "1");
   }

   public EyeEm numComments(int count) {
      return (EyeEm)param("numComments", count);
   }

   public EyeEm numLikers(int count) {
      return (EyeEm)param("numLikers", count);
   }

   public EyeEm latlng(String lat, String lng) {
      if (TextUtils.isEmpty(lat) || TextUtils.isEmpty(lng))
         return this;
      return (EyeEm) param("lat", lat).param("lng", lng);
   }

   public EyeEm city(String city) {
      return (EyeEm) param("city", city);
   }

   public EyeEm cc(String cc) {
      return (EyeEm) param("cc", cc);
   }

   @Override
   public RequestBuilder fetchFront(Object info) {
      return param("offset", 0);
   }

   @Override
   public RequestBuilder fetchBack(Object info) {
      return param("offset", ((List) info).size());
   }

   public final static Comparator photoSort = new Comparator() {
      @Override
      public int compare(Object lhs, Object rhs) {
         return (int)((Photo)rhs).updated - (int)((Photo)lhs).updated;
      }
   };

   static {
      new DateParser("com.eyeem.sdk") {
         @Override public long toSeconds(String date) {
            try { return new SimpleDateFormat(
               "yyyy-MM-dd'T'HH:mm:ssZ",
               Locale.getDefault()).parse(date).getTime()/1000;
            } catch (ParseException e) { return 0; }
         }
      };
   }

   public static class Account extends OAuth2Account {

      public final static String TYPE = "eyeem";

      public User user;

      public Account() {
         type = TYPE;
      }

      public static Account fromJSON(JSONObject json) {
         Account account = new Account();
         OAuth2Account.fromJSON(account, json);
         account.user = User.fromJSON(json.optJSONObject("user"));
         return account;
      }

      @Override
      public String authorizeUrl() {
         return String.format(
            "https://www.eyeem.com/oauth/authorize?response_type=code&client_id=%s&redirect_uri=%s",
            ID,
            callbackUrl());
      }

      @Override public String callbackUrl() { return CALLBACK_URI; }
      @Override public String secret() { return SECRET; }
      @Override public RequestBuilder oauthRequest() { return new EyeEm("/v2/oauth/token"); }
      @Override public String avatarUrl() { return user != null ? user.thumbUrl : ""; }
      @Override public String displayName() {
         if (user == null)
            return "";
         else if (!TextUtils.isEmpty(user.fullname))
            return user.fullname;
         else if (!TextUtils.isEmpty(user.nickname))
            return user.nickname;
         return "";
      }

      @Override
      public RequestBuilder sign(RequestBuilder requestBuilder) {
         return requestBuilder.header("Authorization", String.format("Bearer %s", accessToken));
      }

      @Override
      public JSONObject toJSON(JSONObject json) throws JSONException {
         json.put("user", user.toJSON());
         return super.toJSON(json);
      }

      @Override public void postAuth(RequestQueue queue, final Context context, final WeakReference<OAuth2Account.UICallback> _callback) {
         EyeEm.user("me")
            .with(Account.this)
            .objectOf(User.class)
            .listener(new Response.Listener<Object>() {
               @Override
               public void onResponse(Object o) {
                  user = (User) o;
                  id = user.id;
                  save(context);
                  UICallback callback = _callback.get();
                  if (callback != null) {
                     callback.onPostAuth(EyeEm.Account.this);
                  }
               }
            })
            .enqueue(queue);
      }
   }

   public static HashMap<String, String> default_headers = new HashMap<String, String>();
}
