package com.eyeem.mjolnir;

import java.util.HashMap;

/**
 * Created by vishna on 13/11/13.
 */
public abstract class DateParser {
   private static HashMap<String, DateParser> parsers = new HashMap<String, DateParser>();

   public DateParser(String packageName) {
      parsers.put(packageName, this);
   }

   public abstract long toSeconds(String date);

   public static long toMilliseconds(String packageName, String date) {
      return parsers.get(packageName).toSeconds(date);
   }
}
