package org.batfish.client;

import java.util.LinkedList;

import org.apache.commons.lang.exception.ExceptionUtils;

public class Main {

   public static void main(String[] args) {

      // Uncomment these lines when you want things to be captured by fiddler
      // System.setProperty("http.proxyHost", "127.0.0.1");
      // System.setProperty("https.proxyHost", "127.0.0.1");
      // System.setProperty("http.proxyPort", "8888");
      // System.setProperty("https.proxyPort", "8888");

      Settings _settings = null;
      try {
         _settings = new Settings(args);
      }
      catch (Exception e) {
         System.err
               .println(Main.class.getName() + ": Initialization failed:\n");
         System.err.print(ExceptionUtils.getFullStackTrace(e));
         System.exit(1);
      }

      Client client = new Client(_settings);
      client.run(new LinkedList<String>());
   }
}
