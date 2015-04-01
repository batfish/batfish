package org.batfish.main;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.UriBuilder;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BfConsts;
import org.batfish.common.BfConsts.TaskStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Driver {

   private static boolean _idle = true;

   private static BatfishLogger _mainLogger = null;

   private static HashMap<String, Task> _taskLog;

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

   public static BatfishLogger getMainLogger() {
      return _mainLogger;
   }

   synchronized static Task getTaskkFromLog(String taskId) {
      if (_taskLog.containsKey(taskId)) {
         return _taskLog.get(taskId);
      }
      else {
         return null;
      }
   }

   private synchronized static void logTask(String taskId, Task task)
         throws Exception {
      if (_taskLog.containsKey(taskId)) {
         throw new Exception("duplicate UUID for task");
      }
      else {
         _taskLog.put(taskId, task);
      }
   }

   public static void main(String[] args) {
      _taskLog = new HashMap<String, Task>();

      Settings settings = null;
      try {
         settings = new Settings(args);
      }
      catch (ParseException e) {
         System.err.println("batfish: Parsing command-line failed. Reason: "
               + e.getMessage());
         System.exit(1);
      }
      _mainLogger = new BatfishLogger(settings);
      System.setErr(_mainLogger.getPrintStream());
      System.setOut(_mainLogger.getPrintStream());
      settings.setLogger(_mainLogger);
      if (settings.runInServiceMode()) {
         URI baseUri = UriBuilder.fromUri(settings.getServiceUrl())
               .port(settings.getServicePort()).build();

         _mainLogger.output(String.format("Starting server at %s\n", baseUri));

         ResourceConfig rc = new ResourceConfig(Service.class)
               .register(new JettisonFeature());

         GrizzlyHttpServerFactory.createHttpServer(baseUri, rc);

         // sleep indefinitely, in 10 minute chunks
         try {
            while (true) {
               Thread.sleep(10 * 60 * 1000); // 10 minutes
            }
         }
         catch (Exception ex) {
            String stackTrace = ExceptionUtils.getFullStackTrace(ex);
            _mainLogger.error(stackTrace);
         }
      }
      else if (settings.canExecute()) {
         settings.setLogger(_mainLogger);
         if (!RunBatfish(settings)) {
            System.exit(1);
         }
      }
   }

   private static void makeIdle() {
      _idle = true;
   }

   private static boolean RunBatfish(Settings settings) {
      BatfishLogger logger = settings.getLogger();
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

   public static List<String> RunBatfishThroughService(String taskId,
         String[] args) {
      final Settings settings;
      try {
         settings = new Settings(args);
      }
      catch (ParseException e) {
         return Arrays.asList("failure",
               "Parsing command-line failed: " + e.getMessage());
      }

      String baseDir = settings.getAutoBaseDir();
      if (baseDir != null) {
         settings.setSerializeIndependentPath(Paths.get(baseDir, BfConsts.RELPATH_VENDOR_INDEPENDENT_CONFIG_DIR).toString());
         settings.setSerializeVendorPath(Paths.get(baseDir, BfConsts.RELPATH_VENDOR_SPECIFIC_CONFIG_DIR).toString());
         settings.setTestRigPath(Paths.get(baseDir, BfConsts.RELPATH_TEST_RIG_DIR).toString());
         settings.setDumpFactsDir(Paths.get(baseDir, BfConsts.RELPATH_FACT_DUMP_DIR).toString());
      }

      if (settings.canExecute()) {
         if (claimIdle()) {
            final BatfishLogger jobLogger = new BatfishLogger(settings);
            settings.setLogger(jobLogger);

            final Task task = new Task(args);

            try {
               logTask(taskId, task);
            }
            catch (Exception e) {
               return Arrays.asList(BfConsts.SVC_FAILURE_KEY, e.getMessage());
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
                  jobLogger.close();
                  makeIdle();
               }
            };

            thread.start();

            return Arrays.asList(BfConsts.SVC_SUCCESS_KEY, "running now");
         }
         else {
            return Arrays.asList(BfConsts.SVC_FAILURE_KEY, "Not idle");
         }
      }
      else {
         return Arrays.asList(BfConsts.SVC_FAILURE_KEY, "Non-executable command");
      }
   }
}
