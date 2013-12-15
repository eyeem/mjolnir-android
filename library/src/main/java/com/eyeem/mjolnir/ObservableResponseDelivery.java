package com.eyeem.mjolnir;

import android.os.Handler;

import com.android.volley.ExecutorDelivery;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ResponseDelivery;
import com.android.volley.VolleyError;

import java.util.concurrent.Executor;

/**
 * Wrapper around ExecutorDelivery
 */
public class ObservableResponseDelivery implements ResponseDelivery {
   /**
    * Used for posting responses, typically to the main thread.
    */
   private final Executor mResponsePoster;
   private final ExecutorDelivery mExecutorDelivery;
   ObservableRequestQueue orq;

   public ObservableResponseDelivery(Executor executor) {
      mResponsePoster = executor;
      mExecutorDelivery = new ExecutorDelivery(executor);
   }

   /**
    * Creates a new response delivery interface.
    *
    * @param handler {@link android.os.Handler} to post responses on
    */
   public ObservableResponseDelivery(final Handler handler) {
      // Make an Executor that just wraps the handler.
      mResponsePoster = new Executor() {
         @Override
         public void execute(Runnable command) {
            handler.post(command);
         }
      };
      mExecutorDelivery = new ExecutorDelivery(handler);
   }

   @Override
   public void postResponse(Request<?> request, Response<?> response) {
      postResponse(request, response, null);
   }

   @Override
   public void postResponse(Request<?> request, Response<?> response, Runnable runnable) {
      //request.markDelivered();
      //request.addMarker("post-response");
      mResponsePoster.execute(new ResponseDeliveryRunnable(request, response));
      // inner handler
      mExecutorDelivery.postResponse(request, response, runnable);
   }

   @Override
   public void postError(Request<?> request, VolleyError error) {
      //request.addMarker("post-error");
      Response<?> response = Response.error(error);
      mResponsePoster.execute(new ResponseDeliveryRunnable(request, response));
      // inner handler
      mExecutorDelivery.postError(request, error);
   }

   /**
    * A Runnable used for delivering network responses to a listener on the
    * main thread.
    */
   @SuppressWarnings("rawtypes")
   private class ResponseDeliveryRunnable implements Runnable {
      private final Request mRequest;
      private final Response mResponse;

      public ResponseDeliveryRunnable(Request request, Response response) {
         mRequest = request;
         mResponse = response;
      }

      @SuppressWarnings("unchecked")
      @Override
      public void run() {
         // If this request has canceled, finish it and don't deliver.
         if (mRequest.isCanceled()) {
            orq.report(mRequest, ObservableRequestQueue.STATUS_CANCELLED, mResponse);
            return;
         }

         // Deliver a normal response or error, depending.
         if (mResponse.isSuccess()) {
            orq.report(mRequest, ObservableRequestQueue.STATUS_SUCCESS, mResponse.result);
         } else {
            orq.report(mRequest, ObservableRequestQueue.STATUS_FAILED, mResponse.error);
         }

//         // If this is an intermediate response, add a marker, otherwise we're done
//         // and the request can be finished.
//         if (mResponse.intermediate) {
//            mRequest.addMarker("intermediate-response");
//         } else {
//            mRequest.finish("done");
//         }
//
//         // If we have been provided a post-delivery runnable, run it.
//         if (mRunnable != null) {
//            mRunnable.run();
//         }
      }
   }
}
