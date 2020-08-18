package org.batfish.main;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.Configuration.SenderConfiguration;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import io.opentracing.util.GlobalTracer;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.collections4.map.LRUMap;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.CleanBatfishException;
import org.batfish.common.CoordConsts;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.QuestionException;
import org.batfish.common.Task;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerStatus;
import org.batfish.datamodel.collections.BgpAdvertisementsByVrf;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.version.BatfishVersion;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jettison.JettisonFeature;
import org.glassfish.jersey.server.ResourceConfig;

@SuppressWarnings("restriction")
public class Driver {

  public enum RunMode {
    WORKER,
    WORKSERVICE,
  }

  private static boolean _idle = true;

  private static Date _lastPollFromCoordinator = new Date();

  private static BatfishLogger _mainLogger = null;

  private static Settings _mainSettings = null;

  private static final Cache<NetworkSnapshot, DataPlane> CACHED_DATA_PLANES = buildDataPlaneCache();

  private static final Map<NetworkSnapshot, SortedMap<String, BgpAdvertisementsByVrf>>
      CACHED_ENVIRONMENT_BGP_TABLES = buildEnvironmentBgpTablesCache();

  private static final Cache<NetworkSnapshot, SortedMap<String, Configuration>> CACHED_TESTRIGS =
      buildTestrigCache();

  private static final Cache<NetworkSnapshot, Map<String, VendorConfiguration>>
      CACHED_VENDOR_CONFIGURATIONS = buildVendorConfigurationCache();

  private static final int COORDINATOR_CHECK_INTERVAL_MS = 1 * 60 * 1000; // 1 min

  private static final int COORDINATOR_POLL_TIMEOUT_MS = 30 * 1000; // 30 secs

  private static final int COORDINATOR_REGISTRATION_RETRY_INTERVAL_MS = 1 * 1000; // 1 sec

  static Logger httpServerLogger = Logger.getLogger(HttpServer.class.getName());

  private static final int MAX_CACHED_DATA_PLANES = 2;

  private static final int MAX_CACHED_ENVIRONMENT_BGP_TABLES = 4;

  private static final int MAX_CACHED_TESTRIGS = 5;

  private static final int MAX_CACHED_VENDOR_CONFIGURATIONS = 2;

  static Logger networkListenerLogger =
      Logger.getLogger("org.glassfish.grizzly.http.server.NetworkListener");

  private static Cache<NetworkSnapshot, DataPlane> buildDataPlaneCache() {
    return CacheBuilder.newBuilder().softValues().maximumSize(MAX_CACHED_DATA_PLANES).build();
  }

  private static Map<NetworkSnapshot, SortedMap<String, BgpAdvertisementsByVrf>>
      buildEnvironmentBgpTablesCache() {
    return Collections.synchronizedMap(new LRUMap<>(MAX_CACHED_ENVIRONMENT_BGP_TABLES));
  }

  private static Cache<NetworkSnapshot, SortedMap<String, Configuration>> buildTestrigCache() {
    return CacheBuilder.newBuilder().softValues().maximumSize(MAX_CACHED_TESTRIGS).build();
  }

  private static Cache<NetworkSnapshot, Map<String, VendorConfiguration>>
      buildVendorConfigurationCache() {
    return CacheBuilder.newBuilder()
        .softValues()
        .maximumSize(MAX_CACHED_VENDOR_CONFIGURATIONS)
        .build();
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

  private static void initTracer() {
    io.jaegertracing.Configuration config =
        new io.jaegertracing.Configuration(_mainSettings.getServiceName())
            .withSampler(new SamplerConfiguration().withType("const").withParam(1))
            .withReporter(
                new ReporterConfiguration()
                    .withSender(
                        SenderConfiguration.fromEnv()
                            .withAgentHost(_mainSettings.getTracingAgentHost())
                            .withAgentPort(_mainSettings.getTracingAgentPort()))
                    .withLogSpans(false));
    GlobalTracer.registerIfAbsent(config.getTracer());
  }

  public static void main(String[] args, BatfishLogger logger) {
    mainInit(args);
    _mainLogger = logger;
    mainRun();
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

  private static void mainRun() {
    System.setErr(_mainLogger.getPrintStream());
    System.setOut(_mainLogger.getPrintStream());
    _mainSettings.setLogger(_mainLogger);
    switch (_mainSettings.getRunMode()) {
      case WORKER:
        mainRunWorker();
        break;
      case WORKSERVICE:
        mainRunWorkService();
        break;
      default:
        System.err.println(
            "batfish: Initialization failed. Unknown runmode: " + _mainSettings.getRunMode());
        System.exit(1);
    }
  }

  private static void mainRunWorker() {
    if (_mainSettings.canExecute()) {
      _mainSettings.setLogger(_mainLogger);
      if (runBatfish(_mainSettings) != null) {
        System.exit(1);
      }
    }
  }

  private static void mainRunWorkService() {
    if (_mainSettings.getTracingEnable() && !GlobalTracer.isRegistered()) {
      initTracer();
    }

    String baseUrl = String.format("http://%s", _mainSettings.getServiceBindHost());
    URI baseUri = UriBuilder.fromUri(baseUrl).port(_mainSettings.getServicePort()).build();
    _mainLogger.debug(String.format("Starting server at %s\n", baseUri));
    ResourceConfig rc = new ResourceConfig(Service.class).register(new JettisonFeature());
    if (_mainSettings.getTracingEnable()) {
      rc.register(ServerTracingDynamicFeature.class);
    }
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

    Object response =
        CoordinatorClient.talkToCoordinator(poolRegUrl, params, _mainSettings, _mainLogger);
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
              CACHED_TESTRIGS,
              CACHED_DATA_PLANES,
              CACHED_ENVIRONMENT_BGP_TABLES,
              CACHED_VENDOR_CONFIGURATIONS,
              null,
              null);

      @Nullable
      SpanContext runBatfishSpanContext =
          GlobalTracer.get().scopeManager().activeSpan() == null
              ? null
              : GlobalTracer.get().scopeManager().activeSpan().context();

      Thread thread =
          new Thread(
              () -> {
                Span runBatfishSpan =
                    GlobalTracer.get()
                        .buildSpan("Run Batfish job in a new thread and get the answer")
                        .addReference(References.FOLLOWS_FROM, runBatfishSpanContext)
                        .start();
                try (Scope scope = GlobalTracer.get().scopeManager().activate(runBatfishSpan)) {
                  assert scope != null; // avoid unused warning
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
                    Span outputAnswerSpan =
                        GlobalTracer.get().buildSpan("Outputting answer").start();
                    try (Scope answerScope =
                        GlobalTracer.get().scopeManager().activate(outputAnswerSpan)) {
                      assert answerScope != null; // avoid unused warning
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
                    } finally {
                      outputAnswerSpan.finish();
                    }
                  }
                } finally {
                  runBatfishSpan.finish();
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

  public static List<String> runBatfishThroughService(String taskId, String[] args) {
    Settings settings;
    try {
      settings = new Settings(_mainSettings);
      settings.setRunMode(RunMode.WORKER);
      settings.parseCommandLine(args);
      // assign taskId for status updates, termination requests
      settings.setTaskId(taskId);
    } catch (Exception e) {
      return Arrays.asList(
          "failure", "Initialization failed: " + Throwables.getStackTraceAsString(e));
    }

    if (!settings.canExecute()) {
      return Arrays.asList(BfConsts.SVC_FAILURE_KEY, "Non-executable command");
    }

    if (!claimIdle()) {
      return Arrays.asList(BfConsts.SVC_FAILURE_KEY, "Not idle");
    }

    // try/catch so that the worker becomes idle again in case of problem submitting thread.
    try {

      BatfishLogger jobLogger =
          new BatfishLogger(settings.getLogLevel(), settings.getTimestamp(), settings.getLogFile());
      settings.setLogger(jobLogger);

      Task task = new Task(args);

      BatchManager.get().logTask(taskId, task);

      @Nullable
      SpanContext runTaskSpanContext =
          GlobalTracer.get().activeSpan() == null
              ? null
              : GlobalTracer.get().activeSpan().context();

      // run batfish on a new thread and set idle to true when done
      Thread thread =
          new Thread(
              () -> {
                Span runBatfishSpan =
                    GlobalTracer.get()
                        .buildSpan("Initialize Batfish in a new thread")
                        .addReference(References.FOLLOWS_FROM, runTaskSpanContext)
                        .start();
                try (Scope scope = GlobalTracer.get().scopeManager().activate(runBatfishSpan)) {
                  assert scope != null; // avoid unused warning
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
                } finally {
                  runBatfishSpan.finish();
                }
              });

      thread.start();

      return Arrays.asList(BfConsts.SVC_SUCCESS_KEY, "running now");
    } catch (Exception e) {
      _mainLogger.error("Exception while running task: " + e.getMessage());
      makeIdle();
      return Arrays.asList(BfConsts.SVC_FAILURE_KEY, e.getMessage());
    }
  }
}
