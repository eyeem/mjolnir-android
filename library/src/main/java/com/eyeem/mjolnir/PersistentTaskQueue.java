package com.eyeem.mjolnir;

import android.content.Context;
import android.content.Intent;

import com.squareup.tape.FileException;
import com.squareup.tape.FileObjectQueue;
import com.squareup.tape.ObjectQueue;
import com.squareup.tape.TaskQueue;

import java.io.File;
import java.io.IOException;

/**
 * Created by vishna on 03/03/14.
 */
public class PersistentTaskQueue extends TaskQueue<PersistentTask> {
   private static final String FILENAME = "persistent_task_queue";

   private final Context context;

   public PersistentTaskQueue(ObjectQueue<PersistentTask> delegate, Context context) {
      super(delegate);
      this.context = context;

      if (size() > 0) {
         startService();
      }
   }

   private void startService() {
      context.startService(new Intent(context, PersistentTaskService.class));
   }

   @Override public void add(PersistentTask entry) {
      super.add(entry);
      startService();
   }

   static PersistentTaskQueue create(Context context) {
      FileObjectQueue.Converter<PersistentTask> converter = new ObjectConverter<PersistentTask>(PersistentTask.class);
      File queueFile = new File(context.getFilesDir(), FILENAME);
      FileObjectQueue<PersistentTask> delegate;
      try {
         delegate = new FileObjectQueue<PersistentTask>(queueFile, converter);
      } catch (IOException e) {
         throw new RuntimeException("Unable to create file queue.", e);
      }
      return new PersistentTaskQueue(delegate, context);
   }

   @Override
   public PersistentTask peek() {
      try {
         return super.peek();
      } catch (FileException fe) {
         remove();
         return peek();
      }
   }
}
