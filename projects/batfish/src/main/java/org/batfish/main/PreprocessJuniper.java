package org.batfish.main;

import static com.google.common.base.Preconditions.checkArgument;

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
import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.job.BatfishJobExecutor;
import org.batfish.job.PreprocessJuniperJob;

/** Utility to dump output of Juniper configuration pre-processing. */
public final class PreprocessJuniper {

  public static void main(String[] args) {
    checkArgument(args.length == 2, "Expected arguments: <input_dir> <output_dir>");
    Path inputPath = Paths.get(args[0]);
    Path outputPath = Paths.get(args[1]);
    Settings settings = new Settings(new String[] {"-storagebase", "/"});
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_WARN, false, System.out);
    settings.setLogger(logger);
    new PreprocessJuniper(new Batfish(settings, null, null, null, null, null, null))
        .preprocessJuniper(inputPath, outputPath);
  }

  private final @Nonnull BatfishLogger _logger;
  private final @Nonnull Settings _settings;

  private PreprocessJuniper(Batfish batfish) {
    _logger = batfish.getSettings().getLogger();
    _settings = batfish.getSettings();
  }

  /**
   * Pre-process Juniper configs in snapshot stored at {@code inputPath}, and dump to {@code
   * outputPath}. Non-Juniper configs are copied unprocessed.
   */
  private void preprocessJuniper(@Nonnull Path inputPath, @Nonnull Path outputPath) {
    _logger.info("\n*** READING INPUT FILES ***\n");
    Map<Path, String> configurationData =
        Batfish.readAllFiles(inputPath.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR), _logger);

    Map<Path, String> outputConfigurationData = new TreeMap<>();
    Path outputConfigDir = outputPath.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR);
    try {
      Files.createDirectories(outputConfigDir);
    } catch (IOException e) {
      throw new BatfishException(
          String.format("Failed to create output directory: '%s'", outputPath));
    }
    _logger.info("\n*** COMPUTING OUTPUT FILES ***\n");
    _logger.resetTimer();
    List<PreprocessJuniperJob> jobs = new ArrayList<>();
    for (Entry<Path, String> configFile : configurationData.entrySet()) {
      Path inputFile = configFile.getKey();
      String fileText = configFile.getValue();
      Warnings warnings = Batfish.buildWarnings(_settings);
      String name = inputFile.getFileName().toString();
      Path outputFile = outputConfigDir.resolve(name);
      PreprocessJuniperJob job =
          new PreprocessJuniperJob(_settings, fileText, inputFile, outputFile, warnings);
      jobs.add(job);
    }
    BatfishJobExecutor.runJobsInExecutor(
        _settings,
        _logger,
        jobs,
        outputConfigurationData,
        null,
        _settings.getFlatten() || _settings.getHaltOnParseError(),
        "Preprocesss Juniper configurations");
    _logger.printElapsedTime();
    for (Entry<Path, String> e : outputConfigurationData.entrySet()) {
      Path outputFile = e.getKey();
      String preprocessedConfigText = e.getValue();
      String outputFileAsString = outputFile.toString();
      _logger.debugf("Writing config to \"%s\"...", outputFileAsString);
      CommonUtil.writeFile(outputFile, preprocessedConfigText);
      _logger.debug("OK\n");
    }
  }
}
