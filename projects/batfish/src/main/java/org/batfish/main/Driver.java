package org.batfish.main;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BatfishWorkerService;
import org.batfish.common.BfConsts.TaskStatus;
import org.batfish.common.CleanBatfishException;
import org.batfish.common.LaunchResult;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.QuestionException;
import org.batfish.common.Task;
import org.batfish.config.Settings;
import org.batfish.datamodel.answers.Answer;
import org.batfish.datamodel.answers.AnswerStatus;
import org.glassfish.grizzly.http.server.HttpServer;

@SuppressWarnings("restriction")
public class Driver {

  private static volatile boolean _initialized;

  public enum RunMode {
    WORKER,
    WORKSERVICE,
  }

  private static boolean _idle = true;

  private static BatfishLogger _mainLogger = null;

  private static Settings _mainSettings = null;

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

  @Deprecated
  @SuppressWarnings("unused")
  public static void main(String[] args, BatfishLogger logger, boolean unused) {
    main(args, logger);
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
      case WORKER -> mainRunWorker();
      case WORKSERVICE -> {}
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

  private static synchronized void makeIdle() {
    _idle = true;
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
