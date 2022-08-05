package org.batfish.main;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.UriBuilder;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishWorkerService;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.CleanBatfishException;
import org.batfish.common.CoordConsts;
import org.batfish.common.LaunchResult;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.QuestionException;
import org.batfish.common.Task;
import org.batfish.config.Settings;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.version.BatfishVersion;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@SuppressWarnings("restriction")
public class Driver {

  private static volatile boolean _initialized;

  public enum RunMode {
    WORKER,
    WORKSERVICE,
  }

  private static boolean _idle = true;

  private static Date _lastPollFromCoordinator = new Date();

  private static BatfishLogger _mainLogger = null;

  private static Settings _mainSettings = null;

  private static final int COORDINATOR_CHECK_INTERVAL_MS = 1 * 60 * 1000; // 1 min

  private static final int COORDINATOR_POLL_TIMEOUT_MS = 30 * 1000; // 30 secs

  private static final int COORDINATOR_REGISTRATION_RETRY_INTERVAL_MS = 1 * 1000; // 1 sec

  static Logger httpServerLogger = Logger.getLogger(HttpServer.class.getName());

  static Logger networkListenerLogger =
      Logger.getLogger("org.glassfish.grizzly.http.server.NetworkListener");

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

  public static void main(String[] args, BatfishLogger logger, boolean initLegacyWorkerService) {
    mainInit(args);
    _mainLogger = logger;
    mainRun(initLegacyWorkerService);
  }

  private static void mainInit(String[] args) {
    try {
      _mainSettings = new Settings(args);
      networkListenerLogger.setLevel(Level.WARNING);
      httpServerLogger.setLevel(Level.WARNING);
    } catch (Exception e) {
      System.err.println(
          "batfish: Initialization failed. Reason: " + Throwables.getStackTraceAsString(e));
      System.exit(1);
    }
  }

  private static void mainRun(boolean initLegacyWorkerService) {
    System.setErr(_mainLogger.getPrintStream());
    System.setOut(_mainLogger.getPrintStream());
    _mainSettings.setLogger(_mainLogger);
    switch (_mainSettings.getRunMode()) {
      case WORKER:
        mainRunWorker();
        break;
      case WORKSERVICE:
        if (initLegacyWorkerService) {
          mainRunWorkService();
        }
        break;
      default:
        System.err.println(
            "batfish: Initialization failed. Unknown runmode: " + _mainSettings.getRunMode());
        System.exit(1);
    }
    _initialized = true;
  }

  private static void mainRunWorker() {
    if (_mainSettings.canExecute()) {
      _mainSettings.setLogger(_mainLogger);
      if (runBatfish(_mainSettings) != null) {
        System.exit(1);
      }
    }
  }

  public static @Nonnull BatfishWorkerService getBatfishWorkerService() {
    return BATFISH_WORKER_SERVICE;
  }

  private static final BatfishWorkerService BATFISH_WORKER_SERVICE =
      new BatfishWorkerService() {

        @Override
        public Task getTaskStatus(String taskId) {
          assert !Strings.isNullOrEmpty(taskId);
          Task task = BatchManager.get().getTaskFromLog(taskId);
          if (task == null) {
            return new Task(TaskStatus.Unknown);
          }
          return task;
        }

        @Override
        public LaunchResult runTask(String taskId, String[] args) {
          return runBatfishThroughService(taskId, args);
        }
      };

  private static void mainRunWorkService() {
    String baseUrl = String.format("http://%s", _mainSettings.getServiceBindHost());
    URI baseUri = UriBuilder.fromUri(baseUrl).port(_mainSettings.getServicePort()).build();
    _mainLogger.debug(String.format("Starting server at %s\n", baseUri));
    ResourceConfig rc = new ResourceConfig(Service.class).register(new JettisonFeature());
    try {
      HttpServer server;
      server = GrizzlyHttpServerFactory.createHttpServer(baseUri, rc);
      int selectedListenPort = server.getListeners().iterator().next().getPort();
      if (_mainSettings.getCoordinatorRegister()) {
        // this function does not return until registration succeeds
        registerWithCoordinatorPersistent(selectedListenPort);
      }

      // sleep indefinitely, check for coordinator each time
      while (true) {
        Thread.sleep(COORDINATOR_CHECK_INTERVAL_MS);
        /*
         * every time we wake up, we check if the coordinator has polled us recently
         * if not, re-register the service. the coordinator might have died and come back.
         */
        if (_mainSettings.getCoordinatorRegister()
            && new Date().getTime() - _lastPollFromCoordinator.getTime()
                > COORDINATOR_POLL_TIMEOUT_MS) {
          // this function does not return until registration succeeds
          registerWithCoordinatorPersistent(selectedListenPort);
        }
      }
    } catch (ProcessingException e) {
      String msg = "FATAL ERROR: " + e.getMessage() + "\n";
      _mainLogger.error(msg);
      System.exit(1);
    } catch (Exception ex) {
      String stackTrace = Throwables.getStackTraceAsString(ex);
      _mainLogger.error(stackTrace);
      System.exit(1);
    }
  }

  private static synchronized void makeIdle() {
    _idle = true;
  }

  private static boolean registerWithCoordinator(String poolRegUrl, int listenPort) {
    Map<String, String> params = new HashMap<>();
    params.put(CoordConsts.SVC_KEY_ADD_WORKER, _mainSettings.getServiceHost() + ":" + listenPort);
    params.put(CoordConsts.SVC_KEY_VERSION, BatfishVersion.getVersionStatic());

    Object response = CoordinatorClient.talkToCoordinator(poolRegUrl, params, _mainLogger);
    return response != null;
  }

  private static void registerWithCoordinatorPersistent(int listenPort)
      throws InterruptedException {
    boolean registrationSuccess;

    String poolRegUrl =
        String.format(
            "http://%s:%s%s/%s",
            _mainSettings.getCoordinatorHost(),
            +_mainSettings.getCoordinatorPoolPort(),
            CoordConsts.SVC_CFG_POOL_MGR,
            CoordConsts.SVC_RSC_POOL_UPDATE);

    do {
      registrationSuccess = registerWithCoordinator(poolRegUrl, listenPort);
      if (!registrationSuccess) {
        Thread.sleep(COORDINATOR_REGISTRATION_RETRY_INTERVAL_MS);
      }
    } while (!registrationSuccess);
  }

  @SuppressWarnings("deprecation")
  private static String runBatfish(Settings settings) {

    BatfishLogger logger = settings.getLogger();

    try {
      Batfish batfish =
          new Batfish(
              settings,
              BfCache.CACHED_TESTRIGS,
              BfCache.CACHED_DATA_PLANES,
              BfCache.CACHED_ENVIRONMENT_BGP_TABLES,
              BfCache.CACHED_VENDOR_CONFIGURATIONS,
              null,
              null);

      Thread thread =
          new Thread(
              () -> {
                Answer answer = null;
                NetworkSnapshot snapshot =
                    new NetworkSnapshot(settings.getContainer(), settings.getTestrig());
                try {
                  answer = batfish.run(snapshot);
                  if (answer.getStatus() == null) {
                    answer.setStatus(AnswerStatus.SUCCESS);
                  }
                } catch (CleanBatfishException e) {
                  String msg = "FATAL ERROR: " + e.getMessage();
                  logger.errorf(
                      "Exception in container:%s, testrig:%s; exception:%s",
                      snapshot.getNetwork(), snapshot.getSnapshot(), msg);
                  batfish.setTerminatingExceptionMessage(
                      e.getClass().getName() + ": " + e.getMessage());
                  answer = Answer.failureAnswer(msg, null);
                } catch (QuestionException e) {
                  String stackTrace = Throwables.getStackTraceAsString(e);
                  logger.errorf(
                      "Exception in container:%s, testrig:%s; exception:%s",
                      snapshot.getNetwork(), snapshot.getSnapshot(), stackTrace);
                  batfish.setTerminatingExceptionMessage(
                      e.getClass().getName() + ": " + e.getMessage());
                  answer = e.getAnswer();
                  answer.setStatus(AnswerStatus.FAILURE);
                } catch (BatfishException e) {
                  String stackTrace = Throwables.getStackTraceAsString(e);
                  logger.errorf(
                      "Exception in container:%s, testrig:%s; exception:%s",
                      snapshot.getNetwork(), snapshot.getSnapshot(), stackTrace);
                  batfish.setTerminatingExceptionMessage(
                      e.getClass().getName() + ": " + e.getMessage());
                  answer = new Answer();
                  answer.setStatus(AnswerStatus.FAILURE);
                  answer.addAnswerElement(e.getBatfishStackTrace());
                } catch (Throwable e) {
                  String stackTrace = Throwables.getStackTraceAsString(e);
                  logger.errorf(
                      "Exception in container:%s, testrig:%s; exception:%s",
                      snapshot.getNetwork(), snapshot.getSnapshot(), stackTrace);
                  batfish.setTerminatingExceptionMessage(
                      e.getClass().getName() + ": " + e.getMessage());
                  answer = new Answer();
                  answer.setStatus(AnswerStatus.FAILURE);
                  answer.addAnswerElement(
                      new BatfishException("Batfish job failed", e).getBatfishStackTrace());
                } finally {
                  try {
                    if (settings.getTaskId() != null) {
                      batfish.outputAnswerWithLog(answer);
                      batfish.outputAnswerMetadata(answer);
                    }
                  } catch (IOException e) {
                    String stackTrace = Throwables.getStackTraceAsString(e);
                    logger.errorf(
                        "Exception in network:%s, snapshot:%s; exception:%s",
                        snapshot.getNetwork(), snapshot.getSnapshot(), stackTrace);
                    batfish.setTerminatingExceptionMessage(
                        e.getClass().getName() + ": " + e.getMessage());
                  }
                }
              });

      thread.start();
      thread.join(settings.getMaxRuntimeMs());

      if (thread.isAlive()) {
        // this is deprecated but we should be safe since we don't have
        // locks and such
        // AF: This doesn't do what you think it does, esp. not in Java 8.
        // It needs to be replaced. TODO
        thread.stop();
        logger.error("Batfish worker took too long. Terminated.");
        batfish.setTerminatingExceptionMessage("Batfish worker took too long. Terminated.");
      }

      return batfish.getTerminatingExceptionMessage();
    } catch (Exception e) {
      String stackTrace = Throwables.getStackTraceAsString(e);
      logger.error(stackTrace);
      return stackTrace;
    }
  }

  public static @Nonnull LaunchResult runBatfishThroughService(String taskId, String[] args) {
    Settings settings;
    try {
      if (!_initialized) {
        return LaunchResult.busy();
      }
      settings = new Settings(_mainSettings);
      settings.setRunMode(RunMode.WORKER);
      settings.parseCommandLine(args);
      // assign taskId for status updates, termination requests
      settings.setTaskId(taskId);
    } catch (Exception e) {
      return LaunchResult.error("Initialization failed: " + Throwables.getStackTraceAsString(e));
    }

    if (!settings.canExecute()) {
      return LaunchResult.error("Non-executable command");
    }

    if (!claimIdle()) {
      return LaunchResult.busy();
    }

    // try/catch so that the worker becomes idle again in case of problem submitting thread.
    try {

      BatfishLogger jobLogger =
          new BatfishLogger(settings.getLogLevel(), settings.getTimestamp(), settings.getLogFile());
      settings.setLogger(jobLogger);

      Task task = new Task(args);

      BatchManager.get().logTask(taskId, task);

      // run batfish on a new thread and set idle to true when done
      Thread thread =
          new Thread(
              () -> {
                task.setStatus(TaskStatus.InProgress);
                String errMsg = runBatfish(settings);
                if (errMsg == null) {
                  task.setStatus(TaskStatus.TerminatedNormally);
                } else {
                  task.setStatus(TaskStatus.TerminatedAbnormally);
                  task.setErrMessage(errMsg);
                }
                task.setTerminated(new Date());
                jobLogger.close();
                makeIdle();
              });

      thread.start();

      return LaunchResult.launched();
    } catch (Exception e) {
      _mainLogger.error("Exception while launching task: " + e.getMessage());
      makeIdle();
      return LaunchResult.error(e.getMessage());
    }
  }
}
