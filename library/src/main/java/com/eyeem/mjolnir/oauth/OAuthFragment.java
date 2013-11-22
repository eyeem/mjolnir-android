package com.eyeem.mjolnir.oauth;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;

import com.eyeem.mjolnir.R;

/**
 * Created by vishna on 15/11/13.
 */
public class OAuthFragment extends DialogFragment implements OAuth2Account.UICallback {

   public final static String TAG = "com.eyeem.mjolnir.oauth.OAuthFragment.TAG";

   public final static String KEY_ACCOUNT = "account";

   private WebView webViewOauth;
   private FrameLayout webViewContainer;
   private OAuth2Account account;
   private boolean loadLaunched;

   RequestQueue queue;

   public void setRequestQueue(RequestQueue queue) {
      this.queue = queue;
   }

   @Override
   public void onAuth(OAuth2Account account) {
      if (webViewOauth != null)
         Toast.makeText(webViewOauth.getContext(), "Connected successfully", Toast.LENGTH_SHORT).show();
   }

   @Override
   public void onPostAuth(OAuth2Account account) {

   }

   @Override
   public void fail(VolleyError error) {
      String txt = "Unknown error has occured";
      try {
         txt = error.getMessage();
         txt = new String(error.networkResponse.data, "UTF-8");
      } catch (Exception e) {
      }
      Toast.makeText(getView().getContext(), txt, Toast.LENGTH_LONG).show();
   }

   private class OAuth2ViewClient extends WebViewClient {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
         // check if the login was successful and the access token returned
         // this test depend of your API
         if (url.startsWith(account.callbackUrl())) {
            // save your token
            account.requestAccessToken(url, queue, webViewOauth.getContext().getApplicationContext(), OAuthFragment.this);
            dismiss();
            return true;
         }
         return false;
      }
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      // request a window without the title
      getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
      getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

      //Retrieve the webview
      View view = inflater.inflate(R.layout.oauth_fragment, container, false);
      webViewContainer = (FrameLayout) view.findViewById(R.id.web_oauth_container);

      if (webViewOauth == null) {
         webViewOauth = new WebView(webViewContainer.getContext());
         webViewOauth.setBackgroundColor(0x00000000); // transparent
      }
      webViewContainer.addView(webViewOauth,
         FrameLayout.LayoutParams.MATCH_PARENT,
         FrameLayout.LayoutParams.MATCH_PARENT);
      if (account == null)
         account = (OAuth2Account) getArguments().get(KEY_ACCOUNT);
      return view;
   }

   @Override
   public void onViewCreated(View view, Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      if (!loadLaunched) {
         loadLaunched = true;

         //set the web client
         webViewOauth.setWebViewClient(new OAuth2ViewClient());

         //activates JavaScript (just in case)
         WebSettings webSettings = webViewOauth.getSettings();
         webSettings.setJavaScriptEnabled(true);

         // clear credentials
         CookieSyncManager.createInstance(view.getContext());
         CookieManager cookieManager = CookieManager.getInstance();
         cookieManager.removeAllCookie();

         //load the url of the oAuth login page
         webViewOauth.loadUrl(account.authorizeUrl());
      }
   }

   public static void show(FragmentManager fm, RequestQueue queue, OAuth2Account account) {
      FragmentTransaction ft = fm.beginTransaction();
      ft.addToBackStack(null);

      // Create and show the dialog.
      OAuthFragment newFragment = new OAuthFragment();
      Bundle args = new Bundle();
      args.putSerializable(KEY_ACCOUNT, account);
      newFragment.setArguments(args);
      newFragment.setRequestQueue(queue);
      newFragment.show(ft, TAG);
   }

   @Override
   public void onDestroyView() { // don't dismiss dialog on rotation
      if (getDialog() != null && getRetainInstance())
         getDialog().setDismissMessage(null);
      super.onDestroyView();
   }

   @Override
   public void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      webViewContainer.removeAllViews();
   }
}
