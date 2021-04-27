package org.batfish.main;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.Files.createDirectories;
import static org.batfish.main.CliUtils.readAllFiles;

import com.google.common.base.Throwables;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.main.preprocess.Preprocessor;

/** Utility to dump output of configuration pre-processing. */
public final class Preprocess {

  public static void main(String[] args) throws IOException {
    checkArgument(args.length == 2, "Expected arguments: <input_dir> <output_dir>");
    Path inputPath = Paths.get(args[0]);
    Path outputPath = Paths.get(args[1]);

    // Bazel: resolve relative to current working directory. No-op if paths are already absolute.
    String wd = System.getenv("BUILD_WORKING_DIRECTORY");
    if (wd != null) {
      inputPath = Paths.get(wd).resolve(inputPath);
      outputPath = Paths.get(wd).resolve(outputPath);
    }

    Settings settings = new Settings(new String[] {"-storagebase", "/"});
    BatfishLogger logger = new BatfishLogger(BatfishLogger.LEVELSTR_WARN, false, System.out);
    settings.setLogger(logger);
    preprocess(inputPath, outputPath, settings);
  }

  /**
   * Pre-process configs in snapshot stored at {@code inputPath}, and dump to {@code outputPath}.
   * Depending on vendor, pre-processing may be a no-op.
   */
  private static void preprocess(
      @Nonnull Path inputPath, @Nonnull Path outputPath, Settings settings) throws IOException {
    BatfishLogger logger = settings.getLogger();
    logger.info("\n*** READING INPUT FILES ***\n");
    Map<Path, String> configurationData =
        readAllFiles(inputPath.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR), logger);

    Path outputConfigDir = outputPath.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR);
    createDirectories(outputConfigDir);
    logger.info("\n*** COMPUTING OUTPUT FILES ***\n");
    logger.resetTimer();
    configurationData.entrySet().parallelStream()
        .forEach(
            e -> {
              Path inputFile = e.getKey();
              String fileText = e.getValue();
              Warnings warnings = Warnings.forLogger(logger);
              Path outputFile = outputConfigDir.resolve(inputFile.getFileName());
              try {
                String result = Preprocessor.preprocess(settings, fileText, inputFile, warnings);
                CommonUtil.writeFile(outputFile, result);
              } catch (IOException err) {
                CommonUtil.writeFile(outputFile, Throwables.getStackTraceAsString(err));
              }
            });
  }
}
