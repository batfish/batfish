package org.batfish.main.preprocess;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

/** Tests for {@link DiffResult}. */
public class DiffResultTest {

  @Test
  public void testSuccess_withContent() {
    String diffOutput = "--- file1\n+++ file2\n@@ -1,1 +1,1 @@\n-old\n+new";
    DiffResult result = DiffResult.success(diffOutput);

    assertThat("Should be successful", result.wasSuccessful(), equalTo(true));
    assertThat("Should not be empty", result.isEmpty(), equalTo(false));
    assertThat("Should return diff output", result.getUnifiedDiff(), equalTo(diffOutput));
    assertThat(
        "Should not have error message", result.getErrorMessage().isPresent(), equalTo(false));
  }

  @Test
  public void testSuccess_empty() {
    DiffResult result = DiffResult.success("");

    assertThat("Should be successful", result.wasSuccessful(), equalTo(true));
    assertThat("Should be empty", result.isEmpty(), equalTo(true));
    assertThat("Should return empty diff", result.getUnifiedDiff(), equalTo(""));
    assertThat(
        "Should not have error message", result.getErrorMessage().isPresent(), equalTo(false));
  }

  @Test
  public void testFailure() {
    String errorMsg = "File not found";
    DiffResult result = DiffResult.failure(errorMsg);

    assertThat("Should not be successful", result.wasSuccessful(), equalTo(false));
    assertThat("Should not be empty (failure != empty)", result.isEmpty(), equalTo(false));
    assertThat("Should return empty diff on failure", result.getUnifiedDiff(), equalTo(""));
    assertThat("Should have error message", result.getErrorMessage().isPresent(), equalTo(true));
    assertThat(
        "Should return correct error message", result.getErrorMessage().get(), equalTo(errorMsg));
  }
}
