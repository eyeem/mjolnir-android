package com.eyeem.mjolnir;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.eyeem.storage.Storage;

import java.util.HashMap;

/**
 * Created by vishna on 28/02/14.
 */
public class ObjectStorageRequestExecutor {
   public Storage storage;
   public RequestBuilder requestBuilder;
   public Class objectClass;
   public boolean exhausted;

   public ObjectStorageRequestExecutor(RequestBuilder requestBuilder, Class objectClass) {
      this.requestBuilder = requestBuilder;
      this.objectClass = objectClass;
   }

   public ObjectStorageRequestExecutor in(Storage storage) {
      this.storage = storage;
      return this;
   }

   public VolleyObjectRequestExecutor fetch(final HashMap<String, String> metaParams) {
      RequestBuilder fetchRequest = requestBuilder.copy();
      if (metaParams != null) {
         fetchRequest.meta.putAll(metaParams);
      }
      return new VolleyObjectRequestExecutor(fetchRequest, objectClass)
         .listener(new Response.Listener<Object>() {
            @Override
            public void onResponse(Object object) {
               storage.retain(object);
            }
         })
         .errorListener(new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               //Toast.makeText(App.the, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
         });
   }
}
