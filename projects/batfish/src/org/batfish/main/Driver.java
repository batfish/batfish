package org.batfish.main;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.CleanBatfishException;
import org.batfish.common.CompositeBatfishException;
import org.batfish.common.CoordConsts;
import org.batfish.common.QuestionException;
import org.batfish.common.Task;
import org.batfish.common.Task.Batch;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.util.CommonUtil;
import org.batfish.common.Version;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.main.Settings.EnvironmentSettings;
import org.batfish.main.Settings.TestrigSettings;
import org.codehaus.jettison.json.JSONArray;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class Driver {

   private static boolean _idle = true;

   private static Date _lastPollFromCoordinator = new Date();

   private static BatfishLogger _mainLogger = null;

   private static Settings _mainSettings = null;

   private static ConcurrentMap<String, Task> _taskLog;

   private static final Map<TestrigSettings, DataPlane> CACHED_DATA_PLANES = buildDataPlaneCache();

   private static final Map<EnvironmentSettings, SortedMap<String, BgpAdvertisementsByVrf>> CACHED_ENVIRONMENT_BGP_TABLES = buildEnvironmentBgpTablesCache();

   private static final Map<EnvironmentSettings, SortedMap<String, RoutesByVrf>> CACHED_ENVIRONMENT_ROUTING_TABLES = buildEnvironmentRoutingTablesCache();

   private static final Map<TestrigSettings, SortedMap<String, Configuration>> CACHED_TESTRIGS = buildTestrigCache();

   private static final int COORDINATOR_POLL_CHECK_INTERVAL_MS = 1 * 60 * 1000;

   private static final int COORDINATOR_POLL_TIMEOUT_MS = 30 * 1000;

   private static final int COORDINATOR_REGISTRATION_RETRY_INTERVAL_MS = 1
         * 1000; // 1
                 // second

   static Logger httpServerLogger = Logger.getLogger(
         org.glassfish.grizzly.http.server.HttpServer.class.getName());

   private static final int MAX_CACHED_DATA_PLANES = 2;

   private static final int MAX_CACHED_ENVIRONMENT_BGP_TABLES = 4;

   private static final int MAX_CACHED_ENVIRONMENT_ROUTING_TABLES = 4;

   private static final int MAX_CACHED_TESTRIGS = 5;

   static Logger networkListenerLogger = Logger
         .getLogger("org.glassfish.grizzly.http.server.NetworkListener");

   private static final String SERVICE_URL = "http://0.0.0.0";

   private static synchronized Map<TestrigSettings, DataPlane> buildDataPlaneCache() {
      return Collections.synchronizedMap(
            new LRUMap<TestrigSettings, DataPlane>(MAX_CACHED_DATA_PLANES));

   }

   private static Map<EnvironmentSettings, SortedMap<String, BgpAdvertisementsByVrf>> buildEnvironmentBgpTablesCache() {
      return Collections.synchronizedMap(
            new LRUMap<EnvironmentSettings, SortedMap<String, BgpAdvertisementsByVrf>>(
                  MAX_CACHED_ENVIRONMENT_BGP_TABLES));
   }

   private static Map<EnvironmentSettings, SortedMap<String, RoutesByVrf>> buildEnvironmentRoutingTablesCache() {
      return Collections.synchronizedMap(
            new LRUMap<EnvironmentSettings, SortedMap<String, RoutesByVrf>>(
                  MAX_CACHED_ENVIRONMENT_ROUTING_TABLES));
   }

   private static synchronized Map<TestrigSettings, SortedMap<String, Configuration>> buildTestrigCache() {
      return Collections.synchronizedMap(
            new LRUMap<TestrigSettings, SortedMap<String, Configuration>>(
                  MAX_CACHED_TESTRIGS));

   }

   private static synchronized boolean claimIdle() {
      if (_idle) {
         _idle = false;
         return true;
      }

      return false;
   }

   public static synchronized boolean getIdle() {
      _lastPollFromCoordinator = new Date();
      return _idle;
   }

   public static BatfishLogger getMainLogger() {
      return _mainLogger;
   }

   private synchronized static Task getTask(Settings settings) {
      String taskId = settings.getTaskId();
      if (taskId == null) {
         return null;
      }
      else if (!_taskLog.containsKey(taskId)) {
         return null;
      }
      else {
         return _taskLog.get(taskId);
      }
   }

   public synchronized static Task getTaskFromLog(String taskId) {
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
      mainInit(args);
      _mainLogger = new BatfishLogger(_mainSettings.getLogLevel(),
            _mainSettings.getTimestamp(), _mainSettings.getLogFile(),
            _mainSettings.getLogTee(), true);
      mainRun();
   }

   public static void main(String[] args, BatfishLogger logger) {
      mainInit(args);
      _mainLogger = logger;
      mainRun();
   }

   private static void mainInit(String[] args) {
      _taskLog = new ConcurrentHashMap<>();

      try {
         _mainSettings = new Settings(args);
         networkListenerLogger.setLevel(Level.WARNING);
         httpServerLogger.setLevel(Level.WARNING);
      }
      catch (Exception e) {
         System.err.println("batfish: Initialization failed. Reason: "
               + ExceptionUtils.getFullStackTrace(e));
         System.exit(1);
      }
   }

   private static void mainRun() {
      System.setErr(_mainLogger.getPrintStream());
      System.setOut(_mainLogger.getPrintStream());
      _mainSettings.setLogger(_mainLogger);
      if (_mainSettings.runInServiceMode()) {
         URI baseUri = UriBuilder.fromUri(SERVICE_URL)
               .port(_mainSettings.getServicePort()).build();

         _mainLogger.debug(String.format("Starting server at %s\n", baseUri));

         ResourceConfig rc = new ResourceConfig(Service.class)
               .register(new JettisonFeature());

         try {
            GrizzlyHttpServerFactory.createHttpServer(baseUri, rc);

            if (_mainSettings.getCoordinatorRegister()) {
               // this function does not return until registration succeeds
               registerWithCoordinatorPersistent();
            }

            // sleep indefinitely, in 1 minute chunks
            while (true) {
               Thread.sleep(COORDINATOR_POLL_CHECK_INTERVAL_MS);

               // every time we wake up, we check if the coordinator has polled
               // us recently
               // if not, re-register the service. the coordinator might have
               // died and come back.
               if (_mainSettings.getCoordinatorRegister()
                     && new Date().getTime() - _lastPollFromCoordinator
                           .getTime() > COORDINATOR_POLL_TIMEOUT_MS) {
                  // this function does not return until registration succeeds
                  registerWithCoordinatorPersistent();
               }

            }
         }
         catch (ProcessingException e) {
            String msg = "FATAL ERROR: " + e.getMessage() + "\n";
            _mainLogger.error(msg);
            System.exit(1);
         }
         catch (Exception ex) {
            String stackTrace = ExceptionUtils.getFullStackTrace(ex);
            _mainLogger.error(stackTrace);
            System.exit(1);
         }
      }
      else if (_mainSettings.canExecute()) {
         _mainSettings.setLogger(_mainLogger);
         Batfish.initTestrigSettings(_mainSettings);
         if (!RunBatfish(_mainSettings)) {
            System.exit(1);
         }
      }
   }

   private static synchronized void makeIdle() {
      _idle = true;
   }

   public static synchronized AtomicInteger newBatch(Settings settings,
         String description, int jobs) {
      Batch batch = null;
      Task task = getTask(settings);
      if (task != null) {
         batch = task.newBatch(description);
         batch.setSize(jobs);
         return batch.getCompleted();
      }
      else {
         return new AtomicInteger();
      }
   }

   private static boolean registerWithCoordinator(String poolRegUrl) {
      try {
         Client client = CommonUtil
               .getClientBuilder(_mainSettings.getCoordinatorUseSsl(),
                     _mainSettings.getTrustAllSslCerts())
               .build();
         WebTarget webTarget = client.target(poolRegUrl)
               .queryParam(CoordConsts.SVC_ADD_WORKER_KEY,
                     _mainSettings.getServiceHost() + ":"
                           + _mainSettings.getServicePort())
               .queryParam(CoordConsts.SVC_VERSION_KEY, Version.getVersion());
         Response response = webTarget.request(MediaType.APPLICATION_JSON)
               .get();

         _mainLogger.debug("BF: " + response.getStatus() + " "
               + response.getStatusInfo() + " " + response + "\n");

         if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            _mainLogger.error("Did not get an OK response\n");
            return false;
         }

         String sobj = response.readEntity(String.class);
         JSONArray array = new JSONArray(sobj);
         _mainLogger.debugf("BF: response: %s [%s] [%s]\n", array.toString(),
               array.get(0), array.get(1));

         if (!array.get(0).equals(CoordConsts.SVC_SUCCESS_KEY)) {
            _mainLogger.errorf(
                  "BF: got error while checking work status: %s %s\n",
                  array.get(0), array.get(1));
            return false;
         }

         return true;
      }
      catch (ProcessingException e) {
         _mainLogger.errorf(
               "BF: unable to connect to coordinator pool mgr at %s\n",
               poolRegUrl);
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

      String protocol = (_mainSettings.getCoordinatorUseSsl()) ? "https"
            : "http";
      String poolRegUrl = String.format("%s://%s:%s%s/%s", protocol,
            _mainSettings.getCoordinatorHost(),
            +_mainSettings.getCoordinatorPoolPort(),
            CoordConsts.SVC_BASE_POOL_MGR, CoordConsts.SVC_POOL_UPDATE_RSC);

      do {
         registrationSuccess = registerWithCoordinator(poolRegUrl);
         if (!registrationSuccess) {
            Thread.sleep(COORDINATOR_REGISTRATION_RETRY_INTERVAL_MS);
         }
      } while (!registrationSuccess);
   }

   @SuppressWarnings("deprecation")
   private static boolean RunBatfish(final Settings settings) {

      final BatfishLogger logger = settings.getLogger();

      try {
         final Batfish batfish = new Batfish(settings, CACHED_TESTRIGS,
               CACHED_DATA_PLANES, CACHED_ENVIRONMENT_BGP_TABLES,
               CACHED_ENVIRONMENT_ROUTING_TABLES);

         Thread thread = new Thread() {
            @Override
            public void run() {
               Answer answer = null;
               try {
                  answer = batfish.run();
                  batfish.setTerminatedWithException(false);
                  if (answer.getStatus() == null) {
                     answer.setStatus(AnswerStatus.SUCCESS);
                  }
               }
               catch (CleanBatfishException e) {
                  batfish.setTerminatedWithException(true);
                  String msg = "FATAL ERROR: " + e.getMessage();
                  logger.error(msg);
                  answer = Answer.failureAnswer(msg, null);
               }
               catch (QuestionException e) {
                  String stackTrace = ExceptionUtils.getFullStackTrace(e);
                  logger.error(stackTrace);
                  answer = e.getAnswer();
                  answer.setStatus(AnswerStatus.FAILURE);
                  batfish.setTerminatedWithException(true);
               }
               catch (CompositeBatfishException e) {
                  String stackTrace = ExceptionUtils.getFullStackTrace(e);
                  logger.error(stackTrace);
                  answer = new Answer();
                  answer.setStatus(AnswerStatus.FAILURE);
                  answer.addAnswerElement(e.getAnswerElement());
                  batfish.setTerminatedWithException(true);
               }
               catch (Exception | StackOverflowError e) {
                  String stackTrace = ExceptionUtils.getFullStackTrace(e);
                  logger.error(stackTrace);
                  answer = new Answer();
                  answer.setStatus(AnswerStatus.FAILURE);
                  answer.addAnswerElement(
                        new BatfishException("Batfish job failed", e)
                              .getBatfishStackTrace());
                  batfish.setTerminatedWithException(true);
               }
               finally {
                  if (settings.getAnswerJsonPath() != null) {
                     batfish.outputAnswerWithLog(answer);
                  }
               }
            }
         };

         thread.start();
         thread.join(settings.getMaxRuntimeMs());

         if (thread.isAlive()) {
            // this is deprecated but we should be safe since we don't have
            // locks and such
            // AF: This doesn't do what you think it does, esp. not in Java 8.
            // It needs to be replaced.
            thread.stop();
            logger.error("Batfish worker took too long. Terminated.");
            batfish.setTerminatedWithException(true);
         }

         batfish.close();
         return !batfish.getTerminatedWithException();
      }
      catch (Exception e) {
         String stackTrace = ExceptionUtils.getFullStackTrace(e);
         logger.error(stackTrace);
         return false;
      }
   }

   public static List<String> RunBatfishThroughService(final String taskId,
         String[] args) {
      final Settings settings;
      try {
         settings = new Settings(args);
         // inherit pluginDir passed to service on startup
         settings.setPluginDirs(_mainSettings.getPluginDirs());
         // assign taskId for status updates, termination requests
         settings.setTaskId(taskId);
      }
      catch (Exception e) {
         return Arrays.asList("failure",
               "Initialization failed: " + ExceptionUtils.getFullStackTrace(e));
      }

      try {
         Batfish.initTestrigSettings(settings);
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
               _mainLogger
                     .error("Exception while running task: " + e.getMessage());
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
