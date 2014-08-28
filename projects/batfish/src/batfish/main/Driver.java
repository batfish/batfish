package batfish.main;

import org.apache.commons.cli.ParseException;

public class Driver {

   public static void main(String[] args) {
      Settings settings = null;
      try {
         settings = new Settings(args);
      }
      catch (ParseException e) {
         System.err.println("batfish: Parsing command-line failed. Reason: " + e.getMessage());
         System.exit(1);
      }
      if (settings.canExecute()) {
         Batfish batfish = new Batfish(settings);
         batfish.run();
      }
   }

}
