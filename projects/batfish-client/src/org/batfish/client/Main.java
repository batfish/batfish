package org.batfish.client;

public class Main {

   public static void main(String[] args) {      
      Settings _settings = null;
      try {
         _settings = new Settings(args);         
      }
      catch (Exception e) {
         System.err.println("org.batfish.client: Initialization failed: "
               + e.getMessage());
         System.exit(1);
      }

      Client client = new Client(_settings);
      client.run();
   }
}