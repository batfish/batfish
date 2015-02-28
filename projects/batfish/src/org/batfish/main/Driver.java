package org.batfish.main;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.cli.ParseException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Driver {

   private static final String BASE_URI = "http://localhost/batfish";
   private static final int SERVICE_PORT = 9999;
   
   private static URI getBaseURI() {
       return UriBuilder.fromUri(BASE_URI).port(SERVICE_PORT).build();
   }

//   public static ResourceConfig createApp() {
//       return new ResourceConfig().
//               register(new JettisonFeature()).
//               packages("org.batfish.main");
//   }

   public static ResourceConfig createApp() {
      return new ResourceConfig(Service.class);
  }

   @SuppressWarnings({"ResultOfMethodCallIgnored"})
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
      if (settings.runInServiceMode()) {

         System.out.println(String.format("Starting server at %s\n", getBaseURI()));
         
         HttpServer _server = GrizzlyHttpServerFactory.createHttpServer(getBaseURI(), createApp());
         
         try {
             System.in.read();
          }
          catch (IOException ex) {
             Logger.getLogger(Driver.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         _server.shutdownNow();
      }
      else if (settings.canExecute()) {
         RunBatfish(settings);
      }
   }
   
   private static void RunBatfish(Settings settings) {
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
