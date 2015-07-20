package com.eyeem.mjolnir;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.eyeem.storage.Storage;

import java.util.HashMap;
import java.util.List;

/**
 * Created by vishna on 02/11/13.
 */
public class ListStorageRequestExecutor {

   public Storage storage;
   public Storage.List list;
   public RequestBuilder requestBuilder;
   public Class objectClass;

   public ListStorageRequestExecutor(RequestBuilder requestBuilder, Class objectClass) {
      this.requestBuilder = requestBuilder;
      this.objectClass = objectClass;
   }

   public ListStorageRequestExecutor in(Storage storage) {
      this.storage = storage;
      list = storage.obtainList(String.valueOf(requestBuilder.toUrl().hashCode()));
      list.enableDedupe(true);
      return this;
   }

   public VolleyListRequestExecutor fetchFront(final HashMap<String, String> metaParams) {
      RequestBuilder frontRequest = requestBuilder.copy().fetchFront(list);
      if (metaParams != null) {
         frontRequest.meta.putAll(metaParams);
      }
      return new VolleyListRequestExecutor(frontRequest, objectClass)
         .listener(new FetchFrontListener(list, metaParams))
         .errorListener(new DummmyErrorListener());
   }

   public VolleyListRequestExecutor fetchBack(HashMap<String, String> metaParams) {
      RequestBuilder backRequest = requestBuilder.copy().fetchBack(list);
      if (metaParams != null) {
         backRequest.meta.putAll(metaParams);
      }
      return new VolleyListRequestExecutor(backRequest, objectClass)
         .listener(new FetchBackListener(list, metaParams))
         .errorListener(new DummmyErrorListener());
   }

   public final static String FORCE_FETCH_FRONT = "forceFetchFront";
   public final static String EXHAUSTED = "exhausted";

   public static HashMap<String, String> forceFrontFetch() {
      HashMap<String, String> params = new HashMap<String, String>();
      params.put(FORCE_FETCH_FRONT, "true");
      return params;
   }

   static class DummmyErrorListener implements Response.ErrorListener {
      @Override public void onErrorResponse(VolleyError error) {}
   }

   static class FetchFrontListener implements Response.Listener<List> {

      Storage.List list;
      HashMap<String, String> metaParams;

      FetchFrontListener(Storage.List list, HashMap<String, String> metaParams) {
         this.list = list;
         this.metaParams = metaParams;
      }

      @Override public void onResponse(List response) {
         if (response == null) return;
         if (metaParams != null && metaParams.containsKey(FORCE_FETCH_FRONT)) {
            Storage.List transaction = list.transaction();
            transaction.clear();
            transaction.addAll(response);
            transaction.setMeta(EXHAUSTED, (transaction.size() == 0));
            transaction.commit(new Storage.Subscription.Action(Storage.Subscription.ADD_UPFRONT));
         } else {
            list.addUpFront(response, null);
         }
      }
   }

   static class FetchBackListener implements Response.Listener<List> {

      Storage.List list;
      HashMap<String, String> metaParams;

      FetchBackListener(Storage.List list, HashMap<String, String> metaParams) {
         this.list = list;
         this.metaParams = metaParams;
      }

      @Override public void onResponse(List response) {
         if (response == null) return;
         Storage.List transaction = list.transaction();
         int before = transaction.size();
         transaction.addAll(response);
         transaction.setMeta(EXHAUSTED, before == transaction.size());
         transaction.commit(new Storage.Subscription.Action(Storage.Subscription.ADD_ALL));
      }
   }
}
