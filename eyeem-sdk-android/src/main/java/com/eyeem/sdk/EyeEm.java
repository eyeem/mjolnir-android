package com.eyeem.sdk;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.eyeem.mjolnir.DateParser;
import com.eyeem.mjolnir.Pagination;
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

   public final static String PRODUCTION_API_URL = "https://api.eyeem.com";
   public static String API_VERSION = "2.3.4";
   private static String API_URL = PRODUCTION_API_URL;

   public static String ID = "";
   public static String SECRET = "";
   public static String CALLBACK_URI = "";

   public static void init(String id, String secret, String callback_uri) {
      Account.registerAccountType("eyeem", EyeEm.Account.class);
      EyeEm.ID = id;
      EyeEm.SECRET = secret;
      EyeEm.CALLBACK_URI = callback_uri;
   }

   public EyeEm() { /*kryo*/ }

   protected EyeEm(String path) {
      super(API_URL, path);
      param("client_id", ID);
      header("X-Api-Version", API_VERSION);
      header("X-hourOfDay", String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
      if (default_headers != null) headers.putAll(default_headers.get());
   }

   public static EyeEm path(String path) {
      return new EyeEm(path);
   }

//// API CALLS
   public static EyeEm discover() {
      return (EyeEm) new EyeEm("/v2/users/me/discover").jsonpath("discover");
   }

   public static EyeEm user(String id) {
      return (EyeEm) new EyeEm("/v2/users/" + id).jsonpath("user");
   }

   public static EyeEm userSearch(String query) {
      return (EyeEm) new EyeEm("/v2/search").param("q", query).param("includeUsers", "1").jsonpath("users.items");
   }

   public static EyeEm userPhotos(String id) {
      return (EyeEm) new EyeEm("/v2/users/" + id + "/photos").jsonpath("photos.items");
   }

   public static EyeEm userLikedPhotos(String id) {
      return (EyeEm) new EyeEm("/v2/users/" + id + "/likedPhotos").jsonpath("likedPhotos.items");
   }

   public static EyeEm album(String id) {
      return (EyeEm) new EyeEm("/v2/albums/" + id).jsonpath("album");
   }

   public static EyeEm albumPhotos(String id) {
      return (EyeEm) new EyeEm("/v2/albums/" + id + "/photos").jsonpath("photos.items");
   }

   public static EyeEm popularPhotos() {
      return (EyeEm) new EyeEm("/v2/photos/popular").jsonpath("photos.items");
   }

   public static EyeEm radiusPhotos(String lat, String lng) {
      return albumPhotos("radius:" + lat + ":" + lng);
   }

   public static EyeEm nearbyPhotos(String lat, String lng) {
      return (EyeEm) new EyeEm("/v2/collection").latlng(lat, lng).param("type", "nearbyLive").jsonpath("photos.items");
   }

   public static EyeEm userFriendsPhotos(String id) {
      return (EyeEm) new EyeEm("/v2/users/" + id + "/friendsPhotos").jsonpath("friendsPhotos.items");
   }

   public static EyeEm userRecommendedPhotos() {
      return (EyeEm) new EyeEm("/v2/users/recommended/photos").jsonpath("photos.items");
   }

   public static EyeEm albumSearch(String query) {
      return (EyeEm) new EyeEm("/v2/search").includeAlbums().param("q", query).jsonpath("albums.items");
   }

   public static EyeEm userFollowers(String id) {
      return (EyeEm) new EyeEm("/v2/users/" + id + "/followers").jsonpath("followers.items");
   }

   public static EyeEm userFriends(String id) {
      return (EyeEm) new EyeEm("/v2/users/" + id + "/friends").jsonpath("friends.items");
   }

   public static EyeEm userFavoritedAlbums(String id) {
      return (EyeEm) new EyeEm("/v2/users/" + id + "/favoritedAlbums").jsonpath("likedAlbums.items");
   }

   public static EyeEm missions() {
      return (EyeEm) new EyeEm("/v2/missions/lightweight").jsonpath("missions.items");
   }

   public static EyeEm mission(String id) {
      return (EyeEm) new EyeEm("/v2/missions/lightweight/" + id).jsonpath("mission");
   }

   public static EyeEm topics() {
      return (EyeEm) new EyeEm("/v2/topics").jsonpath("topics.items");
   }

   public static EyeEm favoriteAlbum(String id) {
      return new EyeEm("/v2/albums/" + id + "/favoriters/me");
   }

   public static EyeEm muteAlbum(String id) {
      return (EyeEm) new EyeEm("/v2/albums/" + id + "/mute").post();
   }

   public static EyeEm unmuteAlbum(String id) {
      return (EyeEm) new EyeEm("/v2/albums/" + id + "/mute").delete();
   }

   public static EyeEm follow(String id) {
      return new EyeEm("/v2/users/me/friends/" + id);
   }

   public static EyeEm likePhoto(String id) {
      return new EyeEm("/v2/photos/" + id + "/likers/me");
   }

   public static EyeEm albumContributors(String id) {
      return (EyeEm) new EyeEm("/v2/albums/" + id + "/contributors").jsonpath("contributors.items");
   }

   public static EyeEm photo(String id) {
      return (EyeEm) new EyeEm("/v2/photos/" + id).jsonpath("photo");
   }

   public static EyeEm photoLikers(String id) {
      return (EyeEm) new EyeEm("/v2/photos/" + id + "/likers").jsonpath("likers.items");
   }

   public static EyeEm photoComments(String id) {
      return (EyeEm) new EyeEm("/v2/photos/" + id + "/comments").jsonpath("comments.items");
   }

   public static EyeEm postComment(String id, String message) {
      return (EyeEm) new EyeEm("/v2/photos/" + id + "/comments").jsonpath("comment").param("message", message).post();
   }

   public static EyeEm deleteComment(String photoId, String commentId) {
      return (EyeEm) new EyeEm("/v2/photos/" + photoId + "/comments/" + commentId).delete();
   }

   public static EyeEm news() {
      return (EyeEm) new EyeEm("/v2/news").jsonpath("news.items").param("aggregated", "1").pagination(new NewsPagination());
   }

///// PARAMS
   public EyeEm defaults() {
      return detailed()
         .includeAlbums()
         .includeLikers()
         .includePeople()
         .includePhotos()
         .includeComments()
         .limit(30)
         .numComments(3)
         .numLikers(2)
         .numPeople(10);
   }

   public EyeEm param(String key, boolean value) {
      return (EyeEm)param(key, value ? "1" : "0");
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

   public EyeEm includeComments() {
      return (EyeEm)param("includeComments", "1");
   }

   public EyeEm numComments(int count) {
      return (EyeEm)param("numComments", count);
   }

   public EyeEm numLikers(int count) {
      return (EyeEm)param("numLikers", count);
   }

   public EyeEm numPeople(int count) {
      return (EyeEm)param("numPeople", count);
   }

   public EyeEm numPhotos(int count) {
      return (EyeEm)param("numPhotos", count);
   }

   public EyeEm offset(int count) {
      return (EyeEm)param("offset", count);
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
      return pagination == null ? param("offset", 0) : super.fetchFront(info);
   }

   @Override
   public RequestBuilder fetchBack(Object info) {
      return pagination == null ? param("offset", ((List) info).size()) : super.fetchBack(info);
   }

   public static void setStagingEnv(String api_url) {
      API_URL = TextUtils.isEmpty(api_url) ? PRODUCTION_API_URL : api_url;
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

      public static Account fromJSON(Account account, JSONObject json) {
         OAuth2Account.fromJSON(account, json);
         account.user = User.fromJSON(json.optJSONObject("user"));
         return account;
      }

      public static Account fromJSON(JSONObject json) {
         return fromJSON(new Account(), json);
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

   public static class NewsPagination implements Pagination {
      @Override public void fetchFront(RequestBuilder rb, Object info) {}
      @Override public void fetchBack(RequestBuilder rb, Object info) {
         List list = (List) info;
         if (list.size() > 0) {
            com.eyeem.sdk.News news = (com.eyeem.sdk.News) list.get(list.size() - 1);
            rb.param("oldestId", news.id);
         }
      }
   }

   public static DefaultHeaders default_headers;

   public interface DefaultHeaders {
      public HashMap<String, String> get();
   }
}
