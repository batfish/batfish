package org.batfish.main;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
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
   
   private static boolean _idle = true;
   
   private static URI getBaseURI() {
       return UriBuilder.fromUri(BASE_URI).port(SERVICE_PORT).build();
   }

   public static ResourceConfig createApp() {
      return new ResourceConfig(Service.class).register(new JettisonFeature());
  }

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
         
         //this is temporary; we should probably sleep indefinitely
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
   
   public static List<String> RunBatfish(String[] args) {
      final Settings settings;
      try {
         settings = new Settings(args);
      }
      catch (ParseException e) {
         return Arrays.asList("failure", "Parsing command-line failed: " + e.getMessage());
      }

      if (settings.canExecute()) {
         if (claimIdle()) {

            //run batfish on a new thread and set idle to true when done
            Thread thread = new Thread(){
               public void run(){
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
         if (error && ! settings.runInServiceMode()) {
            System.exit(1);
         }
      }
   }

   private static synchronized boolean claimIdle() {
      if (_idle) {
         _idle = false;
         return true;
      }
      
      return false;
   }
   
   private static void makeIdle() {
      _idle = true;
   }

   public static boolean getIdle(){
      return _idle;
   }
}
