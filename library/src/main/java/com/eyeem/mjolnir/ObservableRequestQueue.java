package com.eyeem.mjolnir;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpStack;
import com.eyeem.storage.WeakEqualReference;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Created by vishna on 15/12/13.
 */
public class ObservableRequestQueue extends RequestQueue {

   /**
    * Default on-disk cache directory.
    */
   private static final String DEFAULT_CACHE_DIR = "volley";

   /** Number of network request dispatcher threads to start. */
   private static final int DEFAULT_NETWORK_THREAD_POOL_SIZE = 4;

   public ObservableRequestQueue(Cache cache, Network network, int threadPoolSize, ObservableResponseDelivery delivery) {
      super(cache, network, threadPoolSize, delivery);
   }

   public Handler handler = new Handler(Looper.getMainLooper());

   public static ObservableRequestQueue newInstance(Context context, HttpStack stack) {
      File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);

      if (stack == null) {
         stack = OkHttpStack.newInstance();
      }

      Network network = new BasicNetwork(stack);

      ObservableResponseDelivery ord = new ObservableResponseDelivery(new Handler(Looper.getMainLooper()));
      ObservableRequestQueue queue = new ObservableRequestQueue(new DiskBasedCache(cacheDir), network, DEFAULT_NETWORK_THREAD_POOL_SIZE, ord);
      ord.orq = queue;
      queue.start();

      return queue;
   }

   public static ObservableRequestQueue newInstance(Context context) {
      return newInstance(context, null);
   }

   @Override
   public Request add(Request request) {
      if (ongoing.contains(request)) {
         report(request, STATUS_ADDED, null);
         return request;
      }
      ongoing.add(request);
      report(request, STATUS_ADDED, null);
      return super.add(request);
   }

   public final static int STATUS_ALREADY_ADDED = -1;
   public final static int STATUS_ADDED = 0;
   public final static int STATUS_FAILED = 1;
   public final static int STATUS_SUCCESS = 2;
   public final static int STATUS_CANCELLED = 3;

   private Vector<Request> ongoing = new Vector<Request>();
   private Vector<WeakEqualReference<Listener>> listeners = new Vector<WeakEqualReference<Listener>>();

   public void report(final Request request, final int status, final Object data) {
      if (status > STATUS_ADDED) ongoing.remove(request);
      handler.post(new Runnable() {
         @Override
         public void run() {
            Vector<WeakEqualReference<Listener>> threadSafeCopy = (Vector<WeakEqualReference<Listener>>)listeners.clone();
            for (WeakEqualReference<Listener> _listener : threadSafeCopy) {
               Listener listener = _listener.get();
               if (listener != null) listener.onStatusUpdate(request, status, data);
            }
         }
      });
   }

   public void registerListener(Listener listener) {
      WeakEqualReference<Listener> listener_ = new WeakEqualReference<Listener>(listener);
      if (!listeners.contains(listener_)) {
         listeners.add(listener_);
      }
   }

   public void unregisterListener(Listener listener) {
      Vector<WeakEqualReference<Listener>> toBeRemoved = new Vector<WeakEqualReference<Listener>>();
      for (WeakEqualReference<Listener> _listener : listeners) {
         Listener aListener = _listener.get();
         if (listener == null || aListener == listener) toBeRemoved.add(_listener);
      }
      listeners.removeAll(toBeRemoved);
   }

   public interface Listener {
      public void onStatusUpdate(Request request, int status, Object data);
   }

   public Vector<Request> ongoing() { return (Vector<Request>) ongoing.clone(); }
}
