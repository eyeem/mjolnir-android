package com.eyeem.mjolnir;

/**
 * Created by vishna on 18/02/14.
 */
public class Mjolnir extends Exception {

   public RequestBuilder rb;
   public String serverMessage;
   public int errorCode;

   public Mjolnir(RequestBuilder rb, int errorCode) {
      this.rb = rb;
      this.errorCode = errorCode;
   }

   public Mjolnir(RequestBuilder rb, int errorCode, String serverMessage) {
      this.rb = rb;
      this.serverMessage = serverMessage;
      this.errorCode = errorCode;
   }

   @Override public String getMessage() {
      return "[" + errorCode + "] " + serverMessage;
   }
}
