package com.eyeem.sdk.fhpx;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.eyeem.mjolnir.DateParser;
import com.eyeem.mjolnir.RequestBuilder;
import com.eyeem.mjolnir.oauth.Auth1;
import com.eyeem.mjolnir.oauth.OAuth1Account;
import com.eyeem.storage.Storage;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

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

   // region api endpoints
   public static FHPX users() {
      return (FHPX) path("/v1/users").jsonpath("user");
   }

   public static FHPX users(String userId) {
      return (FHPX) path("/v1/users/" + userId).jsonpath("user");
   }

   public static FHPX usersFriends(String userId) {
      return (FHPX) path("/v1/users/" + userId + "/friends").jsonpath("user");
   }

   public static FHPX photos() {
      return (FHPX) path("/v1/photos").jsonpath("photos");
   }

   public static FHPX photosSearch() {
      return (FHPX) path("/v1/photos/search").jsonpath("photos");
   }

   public static FHPX photosComments(String photoId) {
      return (FHPX) path("/v1/photos/" + photoId + "/comments").jsonpath("comments");
   }

   public static FHPX photosComments(String photoId, String body) {
      return (FHPX) path("/v1/photos/" + photoId + "/comments").param("body", body).jsonpath("comment").post();
   }

   public static FHPX photosVote(String photoId) {
      return (FHPX) path("/v1/photos/" + photoId + "/vote").jsonpath("photo");
   }

   public static FHPX usersSearch(String term) { return (FHPX) path("/v1/users/search").param("term", term).jsonpath("users"); }

   public static FHPX galleries(String userId) {
      return (FHPX) path("/v1/users/" + userId + "/galleries")
         .param("cover_size", TextUtils.join(",", SIZES_ID))
         .param("include_cover", "1")
         .param("kinds", "0,1,2,4,5")
         .param("privacy", "public")
         .param("sort", "last_added_to_at")
         .param("sort_direction", "desc")
         .jsonpath("galleries");
   }

   public static FHPX galleryPhotos(String userId, String galleryId) {
      return (FHPX) FHPX.path("/v1/users/" + userId + "/galleries/" + galleryId + "/items").jsonpath("photos");
   }

   public static FHPX galleries(String userId, String galleryId) {
      return (FHPX) path("/v1/users/" + userId + "/galleries/" + galleryId + "/items");
   }
   // endregion

   // region params
   public FHPX page(int index) { return (FHPX)param("page", index); }

   /**
    * @param value 1 - like, 0 - dislike
    * @return
    */
   public FHPX vote(int value) { return (FHPX)param("vote", value); }

   public FHPX image_size(int value) { return (FHPX)param("image_size", value); }

   public FHPX bestQuality() { return (FHPX)param("image_size", TextUtils.join(",", SIZES_ID)); }

   public FHPX feature(String value) { return (FHPX)param("feature", value); }

   public FHPX includeStore() { return (FHPX)param("include_store", 1); }

   public FHPX includeStates() { return (FHPX)param("include_states", 1); }

   public FHPX tag(String tag) { return (FHPX)param("tag", tag); }

   public FHPX tags() { return (FHPX)param("tags", 1); }

   public FHPX geo(FHLocation location) {
      return (FHPX)param("geo",
         String.format(Locale.US, "%1$f,%2$f,%3$s", location.latitude, location.longitude, location.radius)
      );
   }

   public Feature feature() {
      return new Feature(this);
   }

   public static class Feature {
      FHPX fhpx;

      Feature(FHPX fhpx) {
         this.fhpx = fhpx;
      }

      /**
       * Return photos in Popular. Default sort: rating.
       * @return
       */
      public FHPX popular() {
         return fhpx.feature("popular");
      }

      /**
       * Return photos that have been in Popular. Default sort: highest_rating.
       * @return
       */
      public FHPX highestRated() {
         return fhpx.feature("highest_rated");
      }

      /**
       * Return photos in Upcoming. Default sort: time when Upcoming was reached.
       * @return
       */
      public FHPX upcoming() {
         return fhpx.feature("upcoming");
      }

      /**
       * Return photos in Editors' Choice. Default sort: time when selected by an editor.
       * @return
       */
      public FHPX editors() {
         return fhpx.feature("editors");
      }

      /**
       * Return photos in Fresh Today. Default sort: time when reached fresh.
       * @return
       */
      public FHPX fresh_today() {
         return fhpx.feature("fresh_today");
      }

      /**
       * Return photos in Fresh Yesterday. Default sort: same as 'fresh_today'
       * @return
       */
      public FHPX fresh_yesterday() {
         return fhpx.feature("fresh_yesterday");
      }

      /**
       * Return photos in Fresh This Week. Default sort: same as 'fresh_today'.
       * @return
       */
      public FHPX fresh_week() {
         return fhpx.feature("fresh_week");
      }

      /**
       * Return photos User follows.
       * @return
       */
      public FHPX user_friends(String user_id) {
         return (FHPX) fhpx.feature("user_friends").param("user_id", user_id);
      }

      /**
       * Return photos of User.
       * @return
       */
      public FHPX user(String user_id) {
         return (FHPX) fhpx.feature("user").param("user_id", user_id);
      }
   }

   public FHPX sort(String value) { return (FHPX)param("sort", value); }

   public Sort sortBy() {
      return new Sort(this);
   }

   /**
    * Sort in ascending order (lowest or least-recent first)
    * @return
    */
   public FHPX asc() { return (FHPX)param("sort_direction", "asc"); }

   /**
    * Sort in descending order (highest or most-recent first). This is the default.
    * @return
    */
   public FHPX desc() { return (FHPX)param("sort_direction", "desc"); }

   public static class Sort {
      FHPX fhpx;

      Sort(FHPX fhpx) {
         this.fhpx = fhpx;
      }

      /**
       * Sort by time of upload.
       * @return
       */
      public FHPX createdAt() {
         return fhpx.sort("created_at");
      }

      /**
       * Sort by rating
       * @return
       */
      public FHPX rating() {
         return fhpx.sort("rating");
      }

      /**
       * Sort by highest rating
       * @return
       */
      public FHPX highestRating() {
         return fhpx.sort("highest_rating");
      }

      /**
       * Sort by view count
       * @return
       */
      public FHPX timesViewed() {
         return fhpx.sort("times_viewed");
      }

      /**
       * Sort by votes count
       * @return
       */
      public FHPX votesCount() {
         return fhpx.sort("votes_count");
      }

      /**
       * Sort by comments count
       * @return
       */
      public FHPX commentsCount() {
         return fhpx.sort("comments_count");
      }


      /**
       * Sort by the original date of the image extracted from metadata (might not be available for all images)
       * @return
       */
      public FHPX takenAt() {
         return fhpx.sort("taken_at");
      }
   }

   public FHPX photoDefaults() {
      return bestQuality()
         .includeStore()
         .includeStates()
         .tags()
         .sortBy().createdAt().desc();
   }
   // endregion

   // region account
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
   // endregion

   //region timestamp parsing
   static {
      new DateParser("com.eyeem.sdk.fhpx") {
         @Override public long toSeconds(String date) {
            try {
               if (date.charAt(date.length() - 3) == ':' &&
                  (date.charAt(date.length() - 6) == '-' || date.charAt(date.length() - 6) == '+')) {
                  StringBuilder sb = new StringBuilder(date);
                  date = sb.deleteCharAt(date.length() - 3).toString();
               }
               return new SimpleDateFormat(
               "yyyy-MM-dd'T'HH:mm:ssZ",
               Locale.getDefault()).parse(date).getTime()/1000;
            } catch (ParseException e) { return 0; }
         }
      };
   }
   //endregion

   //region image size support
   public final static Integer[] SIZES_ID = new Integer[]{4, 1080, 5, 1600, 2048};
   public final static Integer[] SIZES_PX = new Integer[]{900, 1080, 1170, 1600, 2048};

   public static int idForSize(int px) {
      int i = 0;
      for (; i < SIZES_PX.length; i++) {
         if (px <= SIZES_PX[i]) break;
      }
      return SIZES_ID[i];
   }
   //endregion

   //region pagination
   public static final int DEFAULT_RESULTS_PER_PAGE = 20;

   @Override public RequestBuilder fetchFront(Object info) {
      if (pagination != null) {
         pagination.fetchFront(this, info);
      }
      return this;
   }

   @Override public RequestBuilder fetchBack(Object info) {
      if (pagination != null) {
         pagination.fetchBack(this, info);
         return this;
      }
      Storage.List list = (Storage.List) info;
      if (list.size() >= DEFAULT_RESULTS_PER_PAGE) {
         return page(list.size()/DEFAULT_RESULTS_PER_PAGE + 1);
      }
      return this;
   }
   //endregion
}
