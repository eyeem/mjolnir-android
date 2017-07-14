package com.eyeem.mjolnir;

import android.os.Handler;
import android.os.Looper;

import android.support.annotation.NonNull;
import android.util.Log;


import com.android.volley.DefaultRetryPolicy;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

/**
 * Created by vishna on 03/03/14.
 */
public class PersistentTask extends Job {

   protected static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());
   public static Handler BG_HANDLER;

   public static final String TAG = PersistentTask.class.getSimpleName();

   protected boolean immediate;

   public Object data;
   public Result result;
   public Throwable reason;

   public PersistentTask() { /*kryo*/ }

   @NonNull @Override protected Result onRunJob(Params params) {
      if (!immediate) {
         try {
            fromPersistableBundle(params.getExtras());
         } catch (Exception e) {
            Log.e(TAG, "Failed to deserialize Job's extras", e);
            return Result.FAILURE;
         }
      }

      MAIN_THREAD.post(new Runnable() {
         @Override public void run() {
            try {
               onStart();
            } catch (Throwable t) {
            }
         }
      });

      try {
         data = execute();
         MAIN_THREAD.post(new Runnable() {
            @Override public void run() {
               onSuccess();
            }
         });
         return result = Result.SUCCESS;
      } catch (Throwable t) {
         reason = t;
         result = onError(t);
         MAIN_THREAD.post(new Runnable() {
            @Override public void run() {
               onFailure();
            }
         });
         return result;
      }
   }

   /**
    * This should deliver some kind of result
    * @return
    * @throws Exception
    */
   public Object execute() throws Exception {
      throw new IllegalStateException("Method not implemented");
   }

   /**
    * Triggered when the job is run, happens on UI thread and before the execute()
    * which is supposed to be an IO operation
    */
   public void onStart() {}

   /**
    * Executed on UI thread in case task has completed sucessfully
    */
   public void onSuccess() {}

   /**
    * Executed on UI thread in case task has failed
    */
   public void onFailure() {}

   protected Result onError(Throwable t) {
      return Result.FAILURE;
   }

   protected JobRequest buildMe(long delay) {
      PersistableBundleCompat extras = new PersistableBundleCompat();

      try {
         toPersistableBundle(extras);
      } catch (Exception e) {
         Log.e(TAG, "Failed to deserialize Job's extras", e);
         return null;
      }

      return new JobRequest.Builder(getClass().getCanonicalName())
         .setExecutionWindow(delay + 1L, delay + 60_000L)
         .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
         .setBackoffCriteria(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, JobRequest.BackoffPolicy.EXPONENTIAL)
         .setPersisted(true)
         .setExtras(extras)
         .build();
   }

   public void start() {
      if (BG_HANDLER != null) {
         immediate = true;
         BG_HANDLER.post(new Runnable() {
            @Override public void run() {
               Job.Result result = null;
               try {
                  result = onRunJob(null);
               } catch (Throwable t) {
                  // NO-OP
               } finally {
                  if (result == null || result == Result.RESCHEDULE) {
                     buildMe(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS).schedule();
                  }
               }
            }
         });
      } else try {
         buildMe(0).schedule();
      } catch (NullPointerException npe) {
      }
   }

   public void fromPersistableBundle(PersistableBundleCompat bundle) throws Exception {
   }

   public void toPersistableBundle(PersistableBundleCompat bundle) throws Exception {
   }
}
