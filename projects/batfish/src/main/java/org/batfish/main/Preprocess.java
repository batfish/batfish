package org.batfish.main;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.file.Files.createDirectories;
import static org.batfish.main.CliUtils.readAllFiles;

import com.google.common.base.Throwables;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishLogger;
import org.batfish.common.BfConsts;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.batfish.main.preprocess.DiffOptions;
import org.batfish.main.preprocess.DiffResult;
import org.batfish.main.preprocess.PreprocessedDiff;
import org.batfish.main.preprocess.Preprocessor;

/** Utility to dump output of configuration pre-processing. */
public final class Preprocess {

  public static void main(String[] args) throws IOException {
    // Check for diff mode
    if (args.length >= 3 && "--diff".equals(args[0])) {
      runDiffMode(args);
      return;
    }

    // Original preprocess mode
    checkArgument(
        args.length == 2,
        "Expected arguments: <input_path> <output_path> OR --diff <file1> <file2> [--output"
            + " <outputfile>]");
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

  /** Handle --diff mode: compare two preprocessed configuration files. */
  private static void runDiffMode(String[] args) throws IOException {
    // Parse arguments: --diff <file1> <file2> [--output <outputfile>]
    checkArgument(
        args.length >= 3, "Expected arguments: --diff <file1> <file2> [--output <outputfile>]");

    Path file1 = Paths.get(args[1]);
    Path file2 = Paths.get(args[2]);
    Path outputFile = null;

    // Check for --output option
    if (args.length >= 5 && "--output".equals(args[3])) {
      outputFile = Paths.get(args[4]);
    }

    // Bazel: resolve relative to current working directory. No-op if paths are already absolute.
    String wd = System.getenv("BUILD_WORKING_DIRECTORY");
    if (wd != null) {
      file1 = Paths.get(wd).resolve(file1);
      file2 = Paths.get(wd).resolve(file2);
      if (outputFile != null) {
        outputFile = Paths.get(wd).resolve(outputFile);
      }
    }

    // Compute diff
    DiffResult result = PreprocessedDiff.diffFiles(file1, file2, DiffOptions.defaults());

    if (!result.wasSuccessful()) {
      System.err.println(
          "Error computing diff: " + result.getErrorMessage().orElse("Unknown error"));
      System.exit(1);
    }

    String diffOutput = result.getUnifiedDiff();
    if (diffOutput.isEmpty()) {
      System.out.println("Files are identical after preprocessing.");
    } else {
      // Output diff to file or stdout
      if (outputFile != null) {
        CommonUtil.writeFile(outputFile, diffOutput);
        System.out.println("Diff written to: " + outputFile);
      } else {
        System.out.println(diffOutput);
      }
    }
  }

  /**
   * Pre-process configs in snapshot stored at {@code inputPath}, and dump to {@code outputPath}.
   * Depending on vendor, pre-processing may be a no-op.
   *
   * <p>Handles both file and directory inputs:
   *
   * <ul>
   *   <li>If {@code inputPath} is a directory, processes all files in the configs subdirectory
   *   <li>If {@code inputPath} is a file, processes that file directly
   * </ul>
   */
  private static void preprocess(
      @Nonnull Path inputPath, @Nonnull Path outputPath, Settings settings) throws IOException {
    BatfishLogger logger = settings.getLogger();

    // Check if input is a file or directory
    if (Files.isRegularFile(inputPath)) {
      // Handle single file input
      logger.info("\n*** READING INPUT FILE ***\n");
      logger.debugf("Reading: \"%s\"\n", inputPath);

      String fileText = Files.readString(inputPath);

      logger.info("\n*** COMPUTING OUTPUT FILE ***\n");
      logger.resetTimer();

      processFile(settings, inputPath, fileText, outputPath, logger);
    } else {
      // Handle directory input (existing behavior)
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
                Path outputFile = outputConfigDir.resolve(inputFile.getFileName());
                processFile(settings, inputFile, fileText, outputFile, logger);
              });
    }
  }

  /**
   * Process a single file by preprocessing its content and writing the result to the output path.
   */
  private static void processFile(
      Settings settings, Path inputFile, String fileText, Path outputFile, BatfishLogger logger) {
    Warnings warnings = Warnings.forLogger(logger);
    try {
      String result = Preprocessor.preprocess(settings, fileText, inputFile, warnings);
      CommonUtil.writeFile(outputFile, result);
    } catch (IOException err) {
      CommonUtil.writeFile(outputFile, Throwables.getStackTraceAsString(err));
    }
  }
}
