package org.batfish.allinone;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.batfish.allinone.config.Settings;
import org.batfish.client.Client;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.BindPortFutures;
import org.batfish.coordinator.BatfishWorkerServiceWorkExecutor;
import org.batfish.coordinator.WorkExecutorCreator;
import org.batfish.main.Driver;

public class AllInOne {

  private static String[] getArgArrayFromString(String argString) {
    if (Strings.isNullOrEmpty(argString)) {
      return new String[0];
    }
    return argString.trim().split("\\s+");
  }

  private final String[] _args;

  private Client _client;

  private BatfishLogger _logger;

  private Settings _settings;

  public AllInOne(String[] args) {
    _args = args;
  }

  public void run() {
    try {
      _settings = new Settings(_args);
    } catch (Exception e) {
      System.err.println("org.batfish.allinone: Initialization failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }

    String argString =
        String.format(
            "%s -%s %s",
            _settings.getClientArgs(),
            org.batfish.client.config.Settings.ARG_LOG_LEVEL,
            _settings.getLogLevel());

    if (_settings.getLogFile() != null) {
      argString +=
          String.format(
              " -%s %s", org.batfish.client.config.Settings.ARG_LOG_FILE, _settings.getLogFile());
    }

    if (_settings.getCommandFile() != null) {
      argString +=
          String.format(
              " -%s %s",
              org.batfish.client.config.Settings.ARG_COMMAND_FILE, _settings.getCommandFile());
    }

    if (_settings.getSnapshotDir() != null) {
      argString +=
          String.format(
              " -%s %s",
              org.batfish.client.config.Settings.ARG_SNAPSHOT_DIR, _settings.getSnapshotDir());
    }

    // if we are not running the client, we were like not specified a cmdfile.
    // lets do a dummy cmdfile do client initialization does not barf
    if (!_settings.getRunClient() && _settings.getCommandFile() == null) {
      argString +=
          String.format(
              " -%s %s", org.batfish.client.config.Settings.ARG_COMMAND_FILE, "dummy_allinone");
    }

    String[] initialArgArray = getArgArrayFromString(argString);
    List<String> clientArgs = new ArrayList<>(Arrays.asList(initialArgArray));
    final String[] argArray = clientArgs.toArray(new String[] {});

    try {
      _client = new Client(argArray);
      _logger = _client.getLogger();
      _logger.debugf("Started client with args: %s\n", Arrays.toString(argArray));
    } catch (Exception e) {
      System.err.printf(
          "Client initialization failed with args: %s\nExceptionMessage: %s\n",
          argString, e.getMessage());
      System.exit(1);
    }

    BindPortFutures bindPortFutures = runCoordinator();

    try {
      runBatfish();
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("org.batfish.allinone: Worker initialization failed: " + e.getMessage());
      e.printStackTrace();
      System.exit(1);
    }

    if (_settings.getRunClient()) {
      try {
        _client.getSettings().setCoordinatorWorkV2Port(bindPortFutures.getWorkV2Port().get());
      } catch (ExecutionException | InterruptedException e) {
        System.err.println("org.batfish.allinone: Worker initialization failed: " + e.getMessage());
        e.printStackTrace();
        System.exit(1);
      }
      _client.run(new LinkedList<>());
      // The program does not terminate without it if the user misses the
      // quit command
      System.exit(0);
    } else {
      // sleep indefinitely, in chunks, since the client does not keep us
      // alive
      try {
        while (true) {
          Thread.sleep(10 * 60 * 1000); // 10 minutes
          _logger.info("allinone: still alive ....\n");
        }
      } catch (Exception ex) {
        String stackTrace = Throwables.getStackTraceAsString(ex);
        System.err.println(stackTrace);
      }
    }
  }

  private void runBatfish() throws ExecutionException, InterruptedException {
    String batfishArgs =
        String.format(
            "%s -%s %s",
            _settings.getBatfishArgs(),
            org.batfish.config.Settings.ARG_RUN_MODE,
            _settings.getBatfishRunMode());

    String[] initialArgArray = getArgArrayFromString(batfishArgs);
    List<String> args = new ArrayList<>(Arrays.asList(initialArgArray));
    final String[] argArray = args.toArray(new String[] {});
    _logger.debugf("Starting batfish worker with args: %s\n", Arrays.toString(argArray));
    Thread thread =
        new Thread("batfishThread") {
          @Override
          public void run() {
            try {
              org.batfish.main.Driver.main(argArray, _logger);
            } catch (Exception e) {
              _logger.errorf(
                  "Initialization of batfish failed with args: %s\nExceptionMessage: %s\n",
                  Arrays.toString(argArray), e.getMessage());
            }
          }
        };
    thread.start();
  }

  private BindPortFutures runCoordinator() {
    String coordinatorArgs = _settings.getCoordinatorArgs();
    // If we are using a command file, just pick ephemeral ports to listen on
    if (_settings.getCommandFile() != null) {
      coordinatorArgs +=
          String.format(
              " -%s %s", org.batfish.coordinator.config.Settings.ARG_SERVICE_WORK_V2_PORT, 0);
    }
    String[] initialArgArray = getArgArrayFromString(coordinatorArgs);
    List<String> args = new ArrayList<>(Arrays.asList(initialArgArray));
    final String[] argArray = args.toArray(new String[] {});
    _logger.debugf("Starting coordinator with args: %s\n", Arrays.toString(argArray));

    BindPortFutures bindPortFutures = new BindPortFutures();
    Thread thread =
        new Thread("coordinatorThread") {
          @Override
          public void run() {
            WorkExecutorCreator workExecutorCreator =
                (logger, settings) ->
                    new BatfishWorkerServiceWorkExecutor(
                        logger, settings.getContainersLocation(), Driver.getBatfishWorkerService());
            try {
              org.batfish.coordinator.Main.main(
                  argArray, _logger, bindPortFutures, workExecutorCreator);
            } catch (Exception e) {
              _logger.errorf(
                  "Initialization of coordinator failed with args: %s\nExceptionMessage: %s\n",
                  Arrays.toString(argArray), e.getMessage());
            }
          }
        };

    thread.start();
    return bindPortFutures;
  }
}
