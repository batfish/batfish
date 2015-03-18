package org.batfish.main;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Driver {

   public static final String MAIN_LOGGER = "MainLogger";

   private static boolean _idle = true;

   private static synchronized boolean claimIdle() {
      if (_idle) {
         _idle = false;
         return true;
      }

      return false;
   }

   public static boolean getIdle() {
      return _idle;
   }

   public static void main(String[] args) {
      Logger logger = LogManager.getLogger(MAIN_LOGGER);

      Settings settings = null;
      try {
         settings = new Settings(args);
      }
      catch (ParseException e) {
         System.err.println("batfish: Parsing command-line failed. Reason: "
               + e.getMessage());
         System.exit(1);
      }
      if (settings.runInServiceMode()) {

         URI baseUri = UriBuilder.fromUri(settings.getServiceUrl())
               .port(settings.getServicePort()).build();

         System.out.println(String.format("Starting server at %s\n", baseUri));

         ResourceConfig rc = new ResourceConfig(Service.class)
               .register(new JettisonFeature());

         HttpServer _server = GrizzlyHttpServerFactory.createHttpServer(
               baseUri, rc);

         // this is temporary; we should probably sleep indefinitely
         try {
            System.in.read();
         }
         catch (IOException ex) {
            String stackTrace = ExceptionUtils.getFullStackTrace(ex);
            logger.error(stackTrace);
         }

         _server.shutdownNow();
      }
      else if (settings.canExecute()) {
         RunBatfish(settings);
      }
   }

   private static void makeIdle() {
      _idle = true;
   }

   private static void RunBatfish(Settings settings) {
      Logger logger = LogManager.getLogger(MAIN_LOGGER);
      boolean error = false;
      try (Batfish batfish = new Batfish(settings)) {
         batfish.run();
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         logger.error(stackTrace);
         error = true;
      }
      finally {
         if (error && !settings.runInServiceMode()) {
            System.exit(1);
         }
      }
   }

   public static List<String> RunBatfish(String[] args) {
      final Settings settings;
      try {
         settings = new Settings(args);
      }
      catch (ParseException e) {
         return Arrays.asList("failure",
               "Parsing command-line failed: " + e.getMessage());
      }

      if (settings.canExecute()) {
         if (claimIdle()) {

            // run batfish on a new thread and set idle to true when done
            Thread thread = new Thread() {
               @Override
               public void run() {
                  RunBatfish(settings);
                  makeIdle();
               }
            };

            thread.start();

            return Arrays.asList("success", "running now");
         }
         else {
            return Arrays.asList("failure", "Not idle");
         }
      }
      else {
         return Arrays.asList("failure", "Non-executable command");
      }
   }
}
