package org.batfish.main.preprocess;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;

/** Library for computing diffs between preprocessed configuration files. */
@ParametersAreNonnullByDefault
public final class PreprocessedDiff {

  /**
   * Compute a diff between two configuration files after preprocessing.
   *
   * @param file1 Path to first configuration file
   * @param file2 Path to second configuration file
   * @param options Diff options (context lines, etc.)
   * @return DiffResult containing unified diff output or error information
   */
  public static @Nonnull DiffResult diffFiles(Path file1, Path file2, DiffOptions options) {
    try {
      // Read both files
      String content1 = Files.readString(file1);
      String content2 = Files.readString(file2);

      return diffStrings(content1, content2, file1.toString(), file2.toString(), options);
    } catch (IOException e) {
      return DiffResult.failure("Error reading files: " + e.getMessage());
    }
  }

  /**
   * Compute a diff between two configuration strings after preprocessing.
   *
   * @param content1 Content of first configuration
   * @param content2 Content of second configuration
   * @param filename1 Name/path of first file (for diff headers)
   * @param filename2 Name/path of second file (for diff headers)
   * @param options Diff options (context lines, etc.)
   * @return DiffResult containing unified diff output or error information
   */
  public static @Nonnull DiffResult diffStrings(
      String content1, String content2, String filename1, String filename2, DiffOptions options) {

    try {
      // Create settings for preprocessing
      Settings settings = new Settings(new String[] {"-storagebase", "/"});

      // Preprocess both configurations
      Warnings warnings1 = new Warnings();
      Warnings warnings2 = new Warnings();

      String preprocessed1 =
          Preprocessor.preprocess(settings, content1, Path.of(filename1), warnings1);
      String preprocessed2 =
          Preprocessor.preprocess(settings, content2, Path.of(filename2), warnings2);

      // Compute unified diff
      String unifiedDiff =
          computeUnifiedDiff(preprocessed1, preprocessed2, filename1, filename2, options);

      return DiffResult.success(unifiedDiff);

    } catch (IOException e) {
      return DiffResult.failure("Error preprocessing configurations: " + e.getMessage());
    } catch (Exception e) {
      return DiffResult.failure("Unexpected error: " + e.getMessage());
    }
  }

  /**
   * Computes a unified diff of the input strings, returning the empty string if the strings are
   * equal. Based on org.batfish.client.Client.getPatch().
   */
  private static @Nonnull String computeUnifiedDiff(
      String expected,
      String actual,
      String expectedFileName,
      String actualFileName,
      DiffOptions options) {
    List<String> referenceLines = Arrays.asList(expected.split("\n"));
    List<String> testLines = Arrays.asList(actual.split("\n"));
    Patch<String> patch = DiffUtils.diff(referenceLines, testLines);
    if (patch.getDeltas().isEmpty()) {
      return "";
    } else {
      List<String> patchLines =
          UnifiedDiffUtils.generateUnifiedDiff(
              expectedFileName, actualFileName, referenceLines, patch, options.getContextLines());
      return String.join("\n", patchLines);
    }
  }

  private PreprocessedDiff() {} // prevent instantiation
}
