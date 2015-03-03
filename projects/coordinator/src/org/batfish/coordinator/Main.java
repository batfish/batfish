package org.batfish.coordinator;

// Include the following imports to use queue APIs.
import java.net.URI;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.cli.ParseException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Main {

   private static Coordinator _coordinator;
   
   public static void main(String[] args) {

      Settings settings = null;
      try {
         settings = new Settings(args);
      }
      catch (ParseException e) {
         System.err.println("org.batfish.coordinator: Parsing command-line failed. Reason: "
               + e.getMessage());
         System.exit(1);
      }
      
      URI baseUri = UriBuilder.fromUri(settings.getServiceUrl())
            .port(settings.getServicePort()).build();

      System.out
            .println(String.format("Starting coordinator at %s\n", baseUri));

      ResourceConfig rc = new ResourceConfig(Service.class)
            .register(new JettisonFeature());

      HttpServer _server = GrizzlyHttpServerFactory.createHttpServer(baseUri,
            rc);

      _coordinator = new Coordinator(settings);
      _coordinator.run();
   }
   
   public static Coordinator getCoordinator() {
      return _coordinator;
   }
}