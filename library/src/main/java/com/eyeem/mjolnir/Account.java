package com.eyeem.mjolnir;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by vishna on 15/11/13.
 */
public abstract class Account implements Serializable {

   public final static String PREFS_NAME = "mjolnir.accounts";
   public final static String PREFS_KEY = "accoutns";

   public String type;
   public String id;

   public abstract RequestBuilder sign(RequestBuilder requestBuilder);

   public JSONObject toJSON(JSONObject json) throws JSONException {
      json.put("type", type);
      json.put("id", id);
      return json;
   }

   public static Account fromJSON(JSONObject json) {
      if (json == null)
         return null;
      String type = json.optString("type");
      if (type == null)
         return null;
      Class subclass = subclasses.get(type);
      if (subclass == null) {
         return UnsupportedAccount.fromJSON(json);
      } else {
         return fromJSON(json, subclass);
      }
   }

   public static void fromJSON(Account account, JSONObject json) {
      account.type = json.optString("type", "");
      account.id = json.optString("id", "");
   }

   public static Account fromJSON(JSONObject jsonAccount, Class clazz) {
      try {
         java.lang.reflect.Method fromJSON = clazz.getMethod("fromJSON", JSONObject.class);
         return (Account)fromJSON.invoke(null, jsonAccount);
      } catch (NoSuchMethodException e) {
         return null;
      } catch (InvocationTargetException e) {
         return null;
      } catch (IllegalAccessException e) {
         return null;
      }
   }

   @Override
   public boolean equals(Object o) {
      return o != null
         && (o instanceof Account)
         && !TextUtils.isEmpty(id)
         && !TextUtils.isEmpty(type)
         && !TextUtils.isEmpty(((Account)o).type)
         && !TextUtils.isEmpty(((Account)o).id)
         && ((Account)o).type.equals(type)
         && ((Account)o).id.equals(id);
   }

   private static HashMap<String, Class> subclasses = new HashMap<String, Class>();

   public static void registerAccountType(String type, Class clazz) {
      subclasses.put(type, clazz);
   }

   public static HashSet<Account> getByType(Context context, String type) {
      HashSet<Account> accounts = new HashSet<Account>();
      if (TextUtils.isEmpty(type))
         return accounts;
      for (Account account : getAccounts(context)) {
         if (!TextUtils.isEmpty(account.type) && account.type.equals(type))
            accounts.add(account);
      }
      return accounts;
   }

   public void save(Context context) {
      HashSet<Account> accounts = getAccounts(context);
      accounts.add(this);
      JSONArray accountsJSONArray = new JSONArray();
      for (Account account : accounts) {
         try {
            JSONObject accountJSON = account.toJSON(new JSONObject());
            if (accountJSON != null)
               accountsJSONArray.put(accountJSON);
         } catch (Exception e) {}
      }
      context
         .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
         .edit()
         .putString(PREFS_KEY, accountsJSONArray.toString())
         .commit();
   }

   public static HashSet<Account> getAccounts(Context context) {
      HashSet<Account> accounts = new HashSet<Account>();
      String jsonAccounts = context
         .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
         .getString(PREFS_KEY, "[]");
      try {
         JSONArray jsonArray = new JSONArray(jsonAccounts);
         for (int i = 0; i < jsonArray.length(); i++) {
            Account account = fromJSON(jsonArray.optJSONObject(i));
            if (account != null)
               accounts.add(account);
         }
      } catch (JSONException je) {}
      return accounts;
   }

   public static class UnsupportedAccount extends Account {

      String jsonString;

      public static UnsupportedAccount fromJSON(JSONObject json) {
         UnsupportedAccount unsupportedAccount = new UnsupportedAccount();
         unsupportedAccount.id = json.optString("id", "");
         unsupportedAccount.type = json.optString("type", "");
         unsupportedAccount.jsonString = json.toString();
         return unsupportedAccount;
      }

      @Override
      public RequestBuilder sign(RequestBuilder requestBuilder) {
         // no-op
         return requestBuilder;
      }

      @Override
      public JSONObject toJSON(JSONObject json) throws JSONException {
         return new JSONObject(jsonString);
      }
   }

   public static void registerListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
      SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      sp.registerOnSharedPreferenceChangeListener(listener);
   }

   public static void unregisterListener(Context context, SharedPreferences.OnSharedPreferenceChangeListener listener) {
      SharedPreferences sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
      sp.unregisterOnSharedPreferenceChangeListener(listener);
   }
}
