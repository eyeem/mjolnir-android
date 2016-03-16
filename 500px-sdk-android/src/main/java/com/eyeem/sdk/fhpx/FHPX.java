package com.eyeem.sdk.fhpx;

import com.eyeem.mjolnir.RequestBuilder;
import com.eyeem.mjolnir.oauth.Auth1;
import com.eyeem.mjolnir.oauth.OAuth1Account;

/**
 * Created by vishna on 15/03/16.
 */
public class FHPX extends RequestBuilder {

   private final static String API_URL = "https://api.instagram.com";

   public static String CONSUMER_KEY = "";
   public static String CONSUMER_SECRET = "";
   public static String CALLBACK_URI = "";

   public static void init(String key, String secret, String callback_uri) {
      Account.registerAccountType("500px", FHPX.Account.class);
      FHPX.CONSUMER_KEY = key;
      FHPX.CONSUMER_SECRET = secret;
      FHPX.CALLBACK_URI = callback_uri;
   }

   public static class Account extends OAuth1Account {
      @Override public String consumerKey() { return CONSUMER_KEY; }
      @Override public String consumerSecret() { return CONSUMER_SECRET; }
      @Override public String oauthCallback() { return CALLBACK_URI; }

      @Override public RequestBuilder oauthRequestToken() {
         return new RequestBuilder("https://api.500px.com", "/v1/oauth/request_token").post();
      }

      @Override public RequestBuilder authorizeUrl(Auth1 auth) {
         return new RequestBuilder("https://api.500px.com", "/v1/oauth/authorize")
            .param("oauth_token", auth.oauth_token)
            .param("oauth_callback", oauthCallback()); // mandatory for 500px
      }

      @Override public RequestBuilder oauthAccessToken() {
         return new RequestBuilder("https://api.500px.com", "/v1/oauth/access_token").post();
      }
   }
}
