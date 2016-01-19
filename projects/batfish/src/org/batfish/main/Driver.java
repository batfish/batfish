package org.batfish.main;

import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CleanBatfishException;
import org.batfish.common.CoordConsts;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.Util;
import org.codehaus.jettison.json.JSONArray;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Driver {

   private static boolean _idle = true;

   private static Date _lastPollFromCoordinator = new Date();

   private static BatfishLogger _mainLogger = null;

   private static Settings _mainSettings = null;

   private static HashMap<String, Task> _taskLog;

   private static final String SERVICE_URL = "http://0.0.0.0";

   private static synchronized boolean claimIdle() {
      if (_idle) {
         _idle = false;
         return true;
      }

      return false;
   }

   public static boolean getIdle() {
      _lastPollFromCoordinator = new Date();
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

      try {
         _mainSettings = new Settings(args);
      }
      catch (Exception e) {
         System.err.println("batfish: Initialization failed. Reason: "
               + e.getMessage());
         System.exit(1);
      }
      _mainLogger = new BatfishLogger(_mainSettings.getLogLevel(),
            _mainSettings.getTimestamp(), _mainSettings.getLogFile(),
            _mainSettings.getLogTee(), true);
      System.setErr(_mainLogger.getPrintStream());
      System.setOut(_mainLogger.getPrintStream());
      _mainSettings.setLogger(_mainLogger);
      if (_mainSettings.runInServiceMode()) {
         URI baseUri = UriBuilder.fromUri(SERVICE_URL)
               .port(_mainSettings.getServicePort()).build();

         _mainLogger.output(String.format("Starting server at %s\n", baseUri));

         ResourceConfig rc = new ResourceConfig(Service.class)
               .register(new JettisonFeature());

         GrizzlyHttpServerFactory.createHttpServer(baseUri, rc);

         try {
            if (_mainSettings.getCoordinatorRegister()) {
               // this function does not return until registration succeeds
               registerWithCoordinatorPersistent();
            }

            // sleep indefinitely, in 1 minute chunks
            while (true) {
               Thread.sleep(1 * 60 * 1000); // 1 minute

               // every time we wake up, we check if the coordinator has polled
               // us recently
               // if not, re-register the service. the coordinator might have
               // died and come back.
               if (_mainSettings.getCoordinatorRegister()
                     && new Date().getTime()
                           - _lastPollFromCoordinator.getTime() > 30 * 1000) {
                  // this function does not return until registration succeeds
                  registerWithCoordinatorPersistent();
               }

            }
         }
         catch (Exception ex) {
            String stackTrace = ExceptionUtils.getFullStackTrace(ex);
            _mainLogger.error(stackTrace);
         }
      }
      else if (_mainSettings.canExecute()) {
         _mainSettings.setLogger(_mainLogger);
         Batfish.applyAutoBaseDir(_mainSettings);
         if (!RunBatfish(_mainSettings)) {
            System.exit(1);
         }
      }
   }

   private static void makeIdle() {
      _idle = true;
   }

   private static boolean registerWithCoordinator() {
      String coordinatorHost = _mainSettings.getCoordinatorHost();
      String workMgr = coordinatorHost + ":"
            + _mainSettings.getCoordinatorWorkPort();
      String poolMgr = coordinatorHost + ":"
            + _mainSettings.getCoordinatorPoolPort();
      try {
         Client client = Util.getClientBuilder(
               _mainSettings.getCoordinatorUseSsl(),
               _mainSettings.getTrustAllSslCerts()).build();
         String protocol = (_mainSettings.getCoordinatorUseSsl()) ? "https"
               : "http";
         WebTarget webTarget = client.target(
               String.format("%s://%s%s/%s", protocol, poolMgr,
                     CoordConsts.SVC_BASE_POOL_MGR,
                     CoordConsts.SVC_POOL_UPDATE_RSC)).queryParam(
               "add",
               _mainSettings.getServiceHost() + ":"
                     + _mainSettings.getServicePort());
         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .get();

         _mainLogger.output(response.getStatus() + " "
               + response.getStatusInfo() + " " + response + "\n");

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            _mainLogger.error("Did not get an OK response\n");
            return false;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         _mainLogger.outputf("response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            _mainLogger.errorf("got error while checking work status: %s %s\n",
                  array.get(0), array.get(1));
            return false;
         }

         return true;
      }
      catch (ProcessingException e) {
         _mainLogger.errorf("unable to connect to %s\n", workMgr);
         return false;
      }
      catch (Exception e) {
         _mainLogger.errorf("exception: " + ExceptionUtils.getStackTrace(e));
         return false;
      }
   }

   private static void registerWithCoordinatorPersistent()
         throws InterruptedException {
      boolean registrationSuccess;
      do {
         registrationSuccess = registerWithCoordinator();
         if (!registrationSuccess) {
            ;
            _mainLogger.error("Unable to register  with coordinator\n");
            Thread.sleep(10 * 1000); // 10 seconds
         }
      } while (!registrationSuccess);
   }

   @SuppressWarnings("deprecation")
   private static boolean RunBatfish(Settings settings) {

      final BatfishLogger logger = settings.getLogger();

      try {
         final Batfish batfish = new Batfish(settings);

         Thread thread = new Thread() {
            @Override
            public void run() {
               try {
                  batfish.run();
                  batfish.SetTerminatedWithException(false);
               }
               catch (CleanBatfishException e) {
                  batfish.SetTerminatedWithException(true);
                  logger.error("FATAL ERROR: " + e.getMessage());
               }
               catch (Exception e) {
                  String stackTrace = ExceptionUtils.getFullStackTrace(e);
                  logger.error(stackTrace);
                  batfish.SetTerminatedWithException(true);
               }
            }
         };

         thread.start();
         thread.join(settings.getMaxRuntimeMs());

         if (thread.isAlive()) {
            // this is deprecated but we should be safe since we don't have
            // locks and such
            thread.stop();
            logger.error("Batfish worker took too long. Terminated.");
            batfish.SetTerminatedWithException(true);
         }

         batfish.close();
         return !batfish.GetTerminatedWithException();
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         logger.error(stackTrace);
         return false;
      }
   }

   public static List<String> RunBatfishThroughService(String taskId,
         String[] args) {
      final Settings settings;
      try {
         settings = new Settings(args);
      }
      catch (Exception e) {
         return Arrays.asList("failure",
               "Initialization failed: " + e.getMessage());
      }

      try {
         Batfish.applyAutoBaseDir(settings);
      }
      catch (Exception e) {
         return Arrays.asList("failure",
               "Failed while applying auto basedir. (All arguments are supplied?): "
                     + e.getMessage());
      }

      if (settings.canExecute()) {
         if (claimIdle()) {

            // lets put a try-catch around all the code around claimIdle
            // so that we never the worker non-idle accidentally

            try {

               final BatfishLogger jobLogger = new BatfishLogger(
                     settings.getLogLevel(), settings.getTimestamp(),
                     settings.getLogFile(), settings.getLogTee(), false);
               settings.setLogger(jobLogger);

               settings.setMaxRuntimeMs(_mainSettings.getMaxRuntimeMs());

               final Task task = new Task(args);

               logTask(taskId, task);

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
            catch (Exception e) {
               _mainLogger.error("Exception while running task: "
                     + e.getMessage());
               makeIdle();
               return Arrays.asList(BfConsts.SVC_FAILURE_KEY, e.getMessage());
            }
         }
         else {
            return Arrays.asList(BfConsts.SVC_FAILURE_KEY, "Not idle");
         }
      }
      else {
         return Arrays.asList(BfConsts.SVC_FAILURE_KEY,
               "Non-executable command");
      }
   }

}
