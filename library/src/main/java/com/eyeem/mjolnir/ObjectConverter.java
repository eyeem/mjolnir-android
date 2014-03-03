package com.eyeem.mjolnir;

import com.squareup.tape.FileObjectQueue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Created by vishna on 03/03/14.
 */
public class ObjectConverter<T> implements FileObjectQueue.Converter<T> {

   public ObjectConverter(Class<T> type) {}

   @Override
   public T from(byte[] bytes) throws IOException {
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
      try {
         return (T) ois.readObject();
      } catch (ClassNotFoundException e) {
         return null;
      }
   }

   @Override
   public void toStream(T t, OutputStream outputStream) throws IOException {
      ObjectOutputStream oos = new ObjectOutputStream(outputStream);
      oos.writeObject(t);
   }
}
