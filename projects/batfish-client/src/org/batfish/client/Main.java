package org.batfish.client;

public class Main {

   public static void main(String[] args) {
      Settings _settings = null;
      try {
         _settings = new Settings(args);
         System.err.println("apikey " + _settings.getApiKey());
      }
      catch (Exception e) {
         System.err
               .println("org.batfish.client: Initialization failed: "
                     + e.getMessage());
         System.exit(1);
      }

      new Client(_settings);
   }
}