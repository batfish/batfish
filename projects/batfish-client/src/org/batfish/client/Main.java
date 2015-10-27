package org.batfish.client;

import org.apache.commons.cli.ParseException;

public class Main {

   public static void main(String[] args) {
      Settings _settings = null;
      try {
         _settings = new Settings(args);
      }
      catch (ParseException e) {
         System.err
               .println("org.batfish.client: Parsing command-line failed. Reason: "
                     + e.getMessage());
         System.exit(1);
      }

      new Client(_settings);
   }
}