package com.eyeem.mjolnir;

import com.google.gson.Gson;
import com.squareup.tape.FileObjectQueue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Created by vishna on 03/03/14.
 */
public class ObjectConverter<T> implements FileObjectQueue.Converter<T> {

   private final Gson gson;
   private final Class<T> type;

   public ObjectConverter(Gson gson, Class<T> type) {
      this.gson = gson;
      this.type = type;
   }

   @Override public T from(byte[] bytes) {
      Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes));
      return gson.fromJson(reader, type);
   }

   @Override public void toStream(T object, OutputStream bytes) throws IOException {
      Writer writer = new OutputStreamWriter(bytes);
      gson.toJson(object, writer);
      writer.close();
   }
}
