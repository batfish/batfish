package org.batfish.main;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.Warnings.forLogger;
import static org.batfish.main.CliUtils.readAllFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.datamodel.answers.FlattenVendorConfigurationAnswerElement;
import org.batfish.job.BatfishJobExecutor;
import org.batfish.job.FlattenVendorConfigurationJob;

public final class Flatten {

  public static void main(String[] args) throws IOException {
    checkArgument(args.length == 2, "Expected arguments: <input_path> <output_path>");
    Path inputPath = Paths.get(args[0]);
    Path outputPath = Paths.get(args[1]);

    // Bazel: resolve relative to current working directory. No-op if paths are already absolute.
    String wd = System.getenv("BUILD_WORKING_DIRECTORY");
    if (wd != null) {
      inputPath = Paths.get(wd).resolve(inputPath);
      outputPath = Paths.get(wd).resolve(outputPath);
    }

    Settings settings = new Settings(new String[] {"-storagebase", "/"});
    settings.setLogger(new BatfishLogger(BatfishLogger.LEVELSTR_WARN, false, System.out));

    flatten(inputPath, outputPath, settings);
  }

  /**
   * Flatten configs in snapshot stored at {@code inputPath}, and dump to {@code outputPath}.
   *
   * <p>Handles both file and directory inputs:
   *
   * <ul>
   *   <li>If {@code inputPath} is a regular file, flattens that file directly and writes to {@code
   *       outputPath} as a file.
   *   <li>Otherwise, treats {@code inputPath} as a snapshot directory and flattens all files in its
   *       configs subdirectory into {@code outputPath}'s configs subdirectory.
   * </ul>
   */
  private static void flatten(@Nonnull Path inputPath, @Nonnull Path outputPath, Settings settings)
      throws IOException {
    BatfishLogger logger = settings.getLogger();

    Map<Path, String> inputConfigurationData;
    Path outputConfigDir;
    if (Files.isRegularFile(inputPath)) {
      logger.info("\n*** READING INPUT FILE ***\n");
      logger.debugf("Reading: \"%s\"\n", inputPath);
      inputConfigurationData = Map.of(inputPath, Files.readString(inputPath));
      if (outputPath.getParent() != null) {
        Files.createDirectories(outputPath.getParent());
      }
      outputConfigDir = null;
    } else {
      logger.info("\n*** READING FILES TO FLATTEN ***\n");
      inputConfigurationData =
          readAllFiles(inputPath.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR), logger);
      outputConfigDir = outputPath.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR);
      Files.createDirectories(outputConfigDir);
    }

    Map<Path, String> outputConfigurationData = new TreeMap<>();
    logger.info("\n*** FLATTENING TEST RIG ***\n");
    logger.resetTimer();
    List<FlattenVendorConfigurationJob> jobs = new ArrayList<>();
    for (Entry<Path, String> configFile : inputConfigurationData.entrySet()) {
      Path inputFile = configFile.getKey();
      String fileText = configFile.getValue();
      Warnings warnings = forLogger(logger);
      Path outputFile =
          outputConfigDir == null
              ? outputPath
              : outputConfigDir.resolve(inputFile.getFileName().toString());
      FlattenVendorConfigurationJob job =
          new FlattenVendorConfigurationJob(settings, fileText, inputFile, outputFile, warnings);
      jobs.add(job);
    }
    BatfishJobExecutor.runJobsInExecutor(
        settings,
        logger,
        jobs,
        outputConfigurationData,
        new FlattenVendorConfigurationAnswerElement(),
        settings.getFlatten() || settings.getHaltOnParseError(),
        "Flatten configurations");
    logger.printElapsedTime();
    for (Entry<Path, String> e : outputConfigurationData.entrySet()) {
      Path outputFile = e.getKey();
      String flatConfigText = e.getValue();
      String outputFileAsString = outputFile.toString();
      logger.debugf("Writing config to \"%s\"...", outputFileAsString);
      CommonUtil.writeFile(outputFile, flatConfigText);
      logger.debug("OK\n");
    }
  }
}
