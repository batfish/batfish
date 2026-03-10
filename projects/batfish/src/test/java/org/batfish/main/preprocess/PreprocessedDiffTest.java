package org.batfish.main.preprocess;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

/** Tests for {@link PreprocessedDiff}. */
public class PreprocessedDiffTest {

  @Test
  public void testDiffStrings_identical() {
    String config1 = "hostname router1\nip route 0.0.0.0 0.0.0.0 192.168.1.1";
    String config2 = "hostname router1\nip route 0.0.0.0 0.0.0.0 192.168.1.1";

    DiffResult result =
        PreprocessedDiff.diffStrings(config1, config2, "file1", "file2", DiffOptions.defaults());

    assertThat("Diff should succeed", result.wasSuccessful(), equalTo(true));
    assertThat("Identical configs should have empty diff", result.isEmpty(), equalTo(true));
    assertThat("Unified diff should be empty", result.getUnifiedDiff(), equalTo(""));
  }

  @Test
  public void testDiffStrings_different() {
    String config1 = "hostname router1\nip route 0.0.0.0 0.0.0.0 192.168.1.1";
    String config2 = "hostname router2\nip route 0.0.0.0 0.0.0.0 192.168.1.1";

    DiffResult result =
        PreprocessedDiff.diffStrings(config1, config2, "file1", "file2", DiffOptions.defaults());

    assertThat("Diff should succeed", result.wasSuccessful(), equalTo(true));
    assertThat("Different configs should have non-empty diff", result.isEmpty(), equalTo(false));
    assertThat(
        "Unified diff should contain change", result.getUnifiedDiff(), containsString("router1"));
    assertThat(
        "Unified diff should contain change", result.getUnifiedDiff(), containsString("router2"));
    assertThat(
        "Unified diff should contain file names", result.getUnifiedDiff(), containsString("file1"));
    assertThat(
        "Unified diff should contain file names", result.getUnifiedDiff(), containsString("file2"));
  }

  @Test
  public void testDiffStrings_emptyFiles() {
    String config1 = "";
    String config2 = "";

    DiffResult result =
        PreprocessedDiff.diffStrings(config1, config2, "file1", "file2", DiffOptions.defaults());

    assertThat("Diff should succeed", result.wasSuccessful(), equalTo(true));
    assertThat("Empty configs should have empty diff", result.isEmpty(), equalTo(true));
  }

  @Test
  public void testDiffOptions_contextLines() {
    String config1 = "line1\nline2\nline3\nline4\nline5";
    String config2 = "line1\nline2\nCHANGED\nline4\nline5";

    DiffResult result =
        PreprocessedDiff.diffStrings(
            config1, config2, "file1", "file2", DiffOptions.withContextLines(1));

    assertThat("Diff should succeed", result.wasSuccessful(), equalTo(true));
    assertThat("Different configs should have non-empty diff", result.isEmpty(), equalTo(false));
    String diff = result.getUnifiedDiff();
    assertThat("Unified diff should contain changed line", diff, containsString("CHANGED"));
    assertThat("Unified diff should contain context", diff, containsString("line2"));
    assertThat("Unified diff should contain context", diff, containsString("line4"));
  }

  @Test
  public void testDiffStrings_configPreprocessing() {
    // Test that configs actually get preprocessed if they are in a supported format
    // For configs that don't get preprocessed, they should still diff correctly
    String config1 = "hostname router1\ninterface eth0\n ip address 1.1.1.1/24";
    String config2 = "hostname router2\ninterface eth0\n ip address 1.1.1.2/24";

    DiffResult result =
        PreprocessedDiff.diffStrings(
            config1, config2, "config1", "config2", DiffOptions.defaults());

    assertThat("Diff should succeed", result.wasSuccessful(), equalTo(true));
    assertThat("Different configs should have non-empty diff", result.isEmpty(), equalTo(false));
    String diff = result.getUnifiedDiff();
    assertThat("Diff should show hostname difference", diff, containsString("router1"));
    assertThat("Diff should show hostname difference", diff, containsString("router2"));
    assertThat("Diff should show IP difference", diff, containsString("1.1.1.1"));
    assertThat("Diff should show IP difference", diff, containsString("1.1.1.2"));
  }

  @Test
  public void testDiffStrings_nonPreprocessibleConfig() {
    // Test with config format that doesn't get preprocessed (like generic text)
    String config1 = "some random text\nthat won't be preprocessed";
    String config2 = "some different text\nthat won't be preprocessed";

    DiffResult result =
        PreprocessedDiff.diffStrings(
            config1, config2, "file1.txt", "file2.txt", DiffOptions.defaults());

    assertThat("Diff should succeed", result.wasSuccessful(), equalTo(true));
    assertThat("Different configs should have non-empty diff", result.isEmpty(), equalTo(false));
    String diff = result.getUnifiedDiff();
    assertThat("Diff should contain original content", diff, containsString("random"));
    assertThat("Diff should contain changed content", diff, containsString("different"));
  }

  @Test
  public void testDiffStrings_largeConfig() {
    // Test with larger config to ensure it handles size reasonably
    StringBuilder config1 = new StringBuilder();
    StringBuilder config2 = new StringBuilder();

    for (int i = 0; i < 100; i++) {
      config1.append("line ").append(i).append("\n");
      config2.append("line ").append(i == 50 ? "CHANGED" : i).append("\n");
    }

    DiffResult result =
        PreprocessedDiff.diffStrings(
            config1.toString(),
            config2.toString(),
            "large1.txt",
            "large2.txt",
            DiffOptions.defaults());

    assertThat("Diff should succeed", result.wasSuccessful(), equalTo(true));
    assertThat("Different configs should have non-empty diff", result.isEmpty(), equalTo(false));
    String diff = result.getUnifiedDiff();
    assertThat("Diff should contain change", diff, containsString("CHANGED"));
    assertThat("Diff should contain line numbers", diff, containsString("@@"));
  }

  @Test
  public void testDiffOptions_defaultsVsCustom() {
    String config1 = "line1\nline2\nline3\nline4\nline5\nline6\nline7";
    String config2 = "line1\nline2\nCHANGED\nline4\nline5\nline6\nline7";

    DiffResult defaultResult =
        PreprocessedDiff.diffStrings(config1, config2, "file1", "file2", DiffOptions.defaults());
    DiffResult customResult =
        PreprocessedDiff.diffStrings(
            config1, config2, "file1", "file2", DiffOptions.withContextLines(1));

    assertThat("Both diffs should succeed", defaultResult.wasSuccessful(), equalTo(true));
    assertThat("Both diffs should succeed", customResult.wasSuccessful(), equalTo(true));
    assertThat("Both should detect changes", defaultResult.isEmpty(), equalTo(false));
    assertThat("Both should detect changes", customResult.isEmpty(), equalTo(false));

    // Default should have more context than custom (3 vs 1)
    String defaultDiff = defaultResult.getUnifiedDiff();
    String customDiff = customResult.getUnifiedDiff();

    // Both should contain the changed line
    assertThat("Default diff should contain change", defaultDiff, containsString("CHANGED"));
    assertThat("Custom diff should contain change", customDiff, containsString("CHANGED"));
  }

  // ==== Error Handling Tests ====

  @Test
  public void testDiffFiles_fileNotFound() throws IOException {
    // Create a temporary file and then delete it to simulate file not found
    Path tempFile1 = Files.createTempFile("test1", ".conf");
    Path tempFile2 = Files.createTempFile("test2", ".conf");
    Files.delete(tempFile1); // Delete to simulate file not found

    DiffResult result = PreprocessedDiff.diffFiles(tempFile1, tempFile2, DiffOptions.defaults());

    assertThat("Should fail", result.wasSuccessful(), equalTo(false));
    assertThat("Should have error message", result.getErrorMessage().isPresent(), equalTo(true));
    assertThat(
        "Error should mention file reading",
        result.getErrorMessage().get(),
        containsString("Error reading files"));
    assertThat("Should return empty diff on error", result.getUnifiedDiff(), equalTo(""));

    // Clean up
    Files.deleteIfExists(tempFile2);
  }

  @Test
  public void testDiffFiles_successfulFileRead() throws IOException {
    // Create temporary files with content
    Path tempFile1 = Files.createTempFile("test1", ".conf");
    Path tempFile2 = Files.createTempFile("test2", ".conf");

    Files.writeString(tempFile1, "hostname router1");
    Files.writeString(tempFile2, "hostname router2");

    DiffResult result = PreprocessedDiff.diffFiles(tempFile1, tempFile2, DiffOptions.defaults());

    assertThat("Should succeed", result.wasSuccessful(), equalTo(true));
    assertThat("Should detect differences", result.isEmpty(), equalTo(false));
    assertThat("Should contain router1", result.getUnifiedDiff(), containsString("router1"));
    assertThat("Should contain router2", result.getUnifiedDiff(), containsString("router2"));

    // Clean up
    Files.delete(tempFile1);
    Files.delete(tempFile2);
  }

  @Test
  public void testDiffStrings_invalidFilenames() {
    // Test with very long filenames or special characters
    String config1 = "test content 1";
    String config2 = "test content 2";
    String longFileName = "a".repeat(1000) + ".conf";
    String specialFileName = "file with spaces & special chars (test).conf";

    DiffResult result =
        PreprocessedDiff.diffStrings(
            config1, config2, longFileName, specialFileName, DiffOptions.defaults());

    assertThat("Should succeed despite unusual filenames", result.wasSuccessful(), equalTo(true));
    assertThat("Should detect differences", result.isEmpty(), equalTo(false));
    assertThat(
        "Should contain filename in diff",
        result.getUnifiedDiff(),
        containsString(specialFileName));
  }

  @Test
  public void testDiffStrings_nullContent() {
    // Test behavior with null content (this should cause preprocessing to fail gracefully)
    DiffResult result =
        PreprocessedDiff.diffStrings(null, "content", "file1", "file2", DiffOptions.defaults());

    assertThat("Should fail gracefully", result.wasSuccessful(), equalTo(false));
    assertThat("Should have error message", result.getErrorMessage().isPresent(), equalTo(true));
    assertThat("Should return empty diff on error", result.getUnifiedDiff(), equalTo(""));
  }
}
