package com.eyeem.sdk.pagination;

import com.eyeem.mjolnir.Pagination;
import com.eyeem.mjolnir.RequestBuilder;

import java.util.List;

/**
 * Created by vishna on 19/08/15.
 */
public class DefaultPagination implements Pagination {

   @Override public void fetchFront(RequestBuilder rb, Object info) {
      rb.param("offset", 0);
   }

   @Override public void fetchBack(RequestBuilder rb, Object info) {
      rb.param("offset",  ((List) info).size());
   }

   @Override public void onFrontFetched(RequestBuilder rb, Object response, Object info) {}
   @Override public void onBackFetched(RequestBuilder rb, Object response, Object info) {}
}
