package org.batfish.client;

import java.util.LinkedList;

public class Main {

   public static void main(String[] args) {

      //Uncomment these lines when you want things to be captured by fiddler
//      System.setProperty("http.proxyHost", "127.0.0.1");
//      System.setProperty("https.proxyHost", "127.0.0.1");
//      System.setProperty("http.proxyPort", "8888");
//      System.setProperty("https.proxyPort", "8888");
      
      Settings _settings = null;
      try {
         _settings = new Settings(args);
      }
      catch (Exception e) {
         System.err.println(
               "org.batfish.client: Initialization failed: " + e.getMessage());
         System.exit(1);
      }

      Client client = new Client(_settings);
      client.run(new LinkedList<String>());
   }
}
