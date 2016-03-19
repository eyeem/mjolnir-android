package com.eyeem.mjolnir;

import java.io.Serializable;

/**
 * Created by vishna on 11/08/15.
 */
public interface Pagination extends Serializable {
   public void fetchFront(RequestBuilder rb, Object info);
   public void fetchBack(RequestBuilder rb, Object info);

   public void onFrontFetched(RequestBuilder rb, Object response, Object info);
   public void onBackFetched(RequestBuilder rb, Object response, Object info);
}
