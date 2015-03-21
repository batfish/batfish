package org.batfish.main;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.BatfishConstants.TaskStatus;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Driver {

   public static final String MAIN_LOGGER = "MainLogger";

   private static boolean _idle = true;
   private static HashMap<String,Task> _taskLog;

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

      _taskLog = new HashMap<String,Task>();
            
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

         //sleep indefinitely, in 10 minute chunks
         try {
            while (true) {
               Thread.sleep(10 * 60 * 1000);  //10 minutes
               logger.info("Still alive ....");
            }
         }
         catch (Exception ex) {
            String stackTrace = ExceptionUtils.getFullStackTrace(ex);
            logger.error(stackTrace);
         }
      }
      else if (settings.canExecute()) {
         if (!RunBatfish(settings)) {
            System.exit(1);            
         }
      }
   }

   private static void makeIdle() {
      _idle = true;
   }

   private static boolean RunBatfish(Settings settings) {
      Logger logger = LogManager.getLogger(MAIN_LOGGER);
      boolean noError = true;
      try (Batfish batfish = new Batfish(settings)) {
         batfish.run();
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         logger.error(stackTrace);
         noError = false;
      }

      return noError;
   }

   public static List<String> RunBatfishThroughService(String taskId, String[] args) {
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

            final Task task = new Task(args);
            
            try {
               logTask(taskId, task);
            }
            catch (Exception e) {
               return Arrays.asList("failure", e.getMessage());
            }
            
            // run batfish on a new thread and set idle to true when done
            Thread thread = new Thread() {
               @Override
               public void run() {
                  task.setStatus(TaskStatus.InProgress);
                  if (RunBatfish(settings)) {
                     task.setStatus(TaskStatus.TerminatedNormally);
                  }
                  else {
                     task.setStatus(TaskStatus.TerminatedAbnormally);
                  }
                  task.setTerminated();
                  makeIdle();
               }
            };

            thread.start();

            return Arrays.asList("", "running now");
         }
         else {
            return Arrays.asList("failure", "Not idle");
         }
      }
      else {
         return Arrays.asList("failure", "Non-executable command");
      }
   }
   
   private synchronized static void logTask(String taskId, Task task) throws Exception {
      if (_taskLog.containsKey(taskId)) {
         throw new Exception("duplicate UUID for task");
      }
      else {
         _taskLog.put(taskId, task);
      }
   }
   
   synchronized static Task getTaskkFromLog(String taskId) {
      if (_taskLog.containsKey(taskId)) {
         return _taskLog.get(taskId);
      }
      else {
         return null;
      }
   }
}
