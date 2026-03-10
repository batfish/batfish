package org.batfish.main.preprocess;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import org.junit.Test;

/** Tests for {@link DiffOptions}. */
public class DiffOptionsTest {

  @Test
  public void testDefaults() {
    DiffOptions options = DiffOptions.defaults();
    assertThat("Default context lines should be 3", options.getContextLines(), equalTo(3));
  }

  @Test
  public void testWithContextLines() {
    DiffOptions options0 = DiffOptions.withContextLines(0);
    assertThat("Context lines should be 0", options0.getContextLines(), equalTo(0));

    DiffOptions options5 = DiffOptions.withContextLines(5);
    assertThat("Context lines should be 5", options5.getContextLines(), equalTo(5));

    DiffOptions options10 = DiffOptions.withContextLines(10);
    assertThat("Context lines should be 10", options10.getContextLines(), equalTo(10));
  }

  @Test
  public void testZeroContextLines_integration() {
    String config1 = "line1\nline2\nline3\nline4\nline5";
    String config2 = "line1\nCHANGED\nline3\nline4\nline5";

    DiffResult result =
        PreprocessedDiff.diffStrings(
            config1, config2, "file1", "file2", DiffOptions.withContextLines(0));

    assertThat("Diff should succeed", result.wasSuccessful(), equalTo(true));
    assertThat("Should detect changes", result.isEmpty(), equalTo(false));
    String diff = result.getUnifiedDiff();
    assertThat("Should contain changed line", diff, containsString("CHANGED"));
    // With 0 context lines, should not contain unchanged lines around the change
    assertThat("Should not contain extra context", diff, not(containsString("line1")));
  }

  @Test
  public void testExtremeContextLines_integration() {
    String config1 = "line1\nline2\nline3";
    String config2 = "line1\nCHANGED\nline3";

    // Test with very large context lines (larger than file)
    DiffResult result =
        PreprocessedDiff.diffStrings(
            config1, config2, "file1", "file2", DiffOptions.withContextLines(1000));

    assertThat("Should succeed with large context", result.wasSuccessful(), equalTo(true));
    assertThat("Should detect changes", result.isEmpty(), equalTo(false));
    String diff = result.getUnifiedDiff();
    assertThat("Should contain changed line", diff, containsString("CHANGED"));
    assertThat("Should contain all context available", diff, containsString("line1"));
    assertThat("Should contain all context available", diff, containsString("line3"));
  }
}
