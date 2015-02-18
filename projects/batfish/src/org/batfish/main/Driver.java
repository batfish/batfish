package org.batfish.main;

import org.apache.commons.cli.ParseException;

public class Driver {

   public static void main(String[] args) {
      Settings settings = null;
      try {
         settings = new Settings(args);
      }
      catch (ParseException e) {
         System.err.println("org.batfish: Parsing command-line failed. Reason: "
               + e.getMessage());
         System.exit(1);
      }
      if (settings.canExecute()) {
         boolean error = false;
         try (Batfish batfish = new Batfish(settings)) {
            batfish.run();
         }
         catch (Exception e) {
            e.printStackTrace();
            error = true;
         }
         finally {
            if (error) {
               System.exit(1);
            }
         }
      }
   }

}
