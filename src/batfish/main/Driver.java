package batfish.main;

public class Driver {

   public static void main(String[] args) {
      Settings settings = new Settings(args);
      if (settings.canExecute()) {
         Batfish batfish = new Batfish(settings);
         batfish.run();
      }
   }

}
