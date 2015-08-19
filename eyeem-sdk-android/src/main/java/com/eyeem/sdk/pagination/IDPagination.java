package com.eyeem.sdk.pagination;

import android.text.TextUtils;

import com.eyeem.mjolnir.Pagination;
import com.eyeem.mjolnir.RequestBuilder;

import java.util.Collections;
import java.util.List;

/**
 * Created by vishna on 19/08/15.
 */
public class IDPagination implements Pagination {

   List<String> ids;
   int limitPerPage;

   public IDPagination() {}

   public IDPagination(List<String> ids, int limitPerPage) {
      this.ids = ids;
      this.limitPerPage = limitPerPage;
   }

   @Override public void fetchFront(RequestBuilder rb, Object info) {
      List<String> subIDs = subList(0, Math.min(limitPerPage, idsSize()));
      if (subIDs.size() == 0) return;
      rb.paramEncoded("ids", TextUtils.join(",", subIDs));
   }

   @Override public void fetchBack(RequestBuilder rb, Object info) {
      int currentCount = ((List) info).size();
      List<String> subIDs = subList(currentCount, Math.min(currentCount + limitPerPage, idsSize()));
      if (subIDs.size() == 0) return;
      rb.paramEncoded("ids", TextUtils.join(",", subIDs));
   }

   private int idsSize() {
      return ids == null ? 0 : ids.size();
   }

   private List<String> subList(int start, int end) {
      try {
         return ids.subList(start, end);
      } catch (Exception e) {
         return Collections.emptyList();
      }
   }
}
