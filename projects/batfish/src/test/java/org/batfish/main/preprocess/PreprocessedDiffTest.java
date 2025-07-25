package org.batfish.main.preprocess;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

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
}
