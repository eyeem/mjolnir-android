package com.eyeem.sdk.pagination;

import com.eyeem.mjolnir.Pagination;
import com.eyeem.mjolnir.RequestBuilder;

import java.util.List;

/**
 * Created by vishna on 19/08/15.
 */
public class NewsPagination implements Pagination {
   @Override public void fetchFront(RequestBuilder rb, Object info) {}
   @Override public void fetchBack(RequestBuilder rb, Object info) {
      List list = (List) info;
      if (list.size() > 0) {
         com.eyeem.sdk.News news = (com.eyeem.sdk.News) list.get(list.size() - 1);
         rb.param("oldestId", news.id);
      }
   }

   @Override public void onFrontFetched(RequestBuilder rb, Object response, Object info) {}
   @Override public void onBackFetched(RequestBuilder rb, Object response, Object info) {}
}
