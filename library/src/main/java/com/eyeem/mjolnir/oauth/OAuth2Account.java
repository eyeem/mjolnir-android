package com.eyeem.mjolnir.oauth;

import android.content.Context;
import android.net.Uri;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.eyeem.mjolnir.Account;
import com.eyeem.mjolnir.RequestBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

/**
 * Created by vishna on 15/11/13.
 */
public abstract class OAuth2Account extends Account {

   protected String accessToken;

   public abstract String authorizeUrl();
   public abstract String callbackUrl();
   public abstract String secret();
   public abstract RequestBuilder oauthRequest();

   @Override public JSONObject toJSON(JSONObject json) throws JSONException {
      super.toJSON(json);
      json.put("accessToken", accessToken);
      return json;
   }

   public void requestAccessToken(
      final String callbackUrl,
      final RequestQueue queue,
      final Context c,
      UICallback callback) {

      final Context context = c.getApplicationContext();

      final WeakReference<UICallback> _callback = new WeakReference<UICallback>(callback);

      oauthRequest()
         .param("grant_type", "authorization_code")
         .param("client_secret", secret())
         .param("redirect_uri", callbackUrl)
         .param("code", Uri.parse(callbackUrl).getQueryParameter("code"))
         .objectOf(Auth.class)
         .listener(new Response.Listener<Object>() {
            @Override
            public void onResponse(Object o) {
               Auth auth = (Auth) o;
               setAuth(auth);

               // e.g. fetch about user info
               UICallback callback = _callback.get();
               if (callback != null)
                  callback.onAuth(OAuth2Account.this);
               postAuth(queue, context, _callback);
            }
         })
         .errorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
               UICallback callback = _callback.get();
               if (callback != null)
                  callback.fail(volleyError);
            }
         })
         .enqueue(queue);
   }

   public abstract void postAuth(RequestQueue queue, Context context, WeakReference<UICallback> _callback);

   public void setAuth(Auth auth) {
      accessToken = auth.access_token;
   }

   public static void fromJSON(OAuth2Account account, JSONObject json) {
      Account.fromJSON(account, json);
      account.accessToken = json.optString("accessToken", "");
   }

   public interface UICallback {
      void onAuth(OAuth2Account account);
      void onPostAuth(OAuth2Account account);
      void fail(VolleyError error);
   }
}
