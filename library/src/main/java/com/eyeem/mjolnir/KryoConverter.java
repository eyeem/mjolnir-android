package com.eyeem.mjolnir;

import com.squareup.tape.FileObjectQueue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Created by vishna on 03/03/14.
 */
public class KryoConverter<T> implements FileObjectQueue.Converter<T> {

   private final Class<T> type;

   public KryoConverter(Class<T> type) {
      this.type = type;
   }

   @Override
   public T from(byte[] bytes) throws IOException {
      Kryo kryo = new Kryo();
      Input input = new Input(new ByteArrayInputStream(bytes));
      return kryo.readObject(input, type);
   }

   @Override
   public void toStream(T t, OutputStream outputStream) throws IOException {
      Kryo kryo = new Kryo();
      Output output = new Output(outputStream);
      kryo.writeObject(output, t);
   }
}
