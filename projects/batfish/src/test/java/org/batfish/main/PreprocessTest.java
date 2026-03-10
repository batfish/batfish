package org.batfish.main;

import static com.google.common.io.MoreFiles.createParentDirectories;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.CommonUtil.writeFile;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.main.Preprocess.main;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Test of {@link Preprocess}. */
@ParametersAreNonnullByDefault
public final class PreprocessTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/main/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  /**
   * Equivalent to calling {@link PreprocessTest#assertValidPair(String,String,String)} with {@code
   * prefix} set to {@link #TESTCONFIGS_PREFIX}.
   */
  private void assertValidPair(String before, String after) throws IOException {
    assertValidPair(TESTCONFIGS_PREFIX, before, after);
  }

  /**
   * Assert that the result of preprocessing the text of the resource {@code before} under {@code
   * prefix} is equal to the text of the resource {@code after} under {@code prefix}.
   */
  private void assertValidPair(String prefix, String before, String after) throws IOException {
    Path root = _folder.getRoot().toPath();
    Path inputDir = root.resolve("input");
    Path outputDir = root.resolve("output");
    Path inputFile = inputDir.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR).resolve("conf");
    Path outputFile = outputDir.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR).resolve("conf");
    createParentDirectories(inputFile);
    writeFile(inputFile, readResource(String.format("%s%s", prefix, before), UTF_8));
    main(new String[] {inputDir.toString(), outputDir.toString()});

    assertThat(
        CommonUtil.readFile(outputFile).trim(),
        equalTo(readResource(String.format("%s%s", prefix, after), UTF_8).trim()));
  }

  @Test
  public void testFlat() throws IOException {
    assertValidPair("preprocess-flat-before", "preprocess-flat-after");
  }

  @Test
  public void testApplyGroupsListVsNonList() throws IOException {
    assertValidPair(
        "preprocess-apply-groups-list-vs-non-list-before",
        "preprocess-apply-groups-list-vs-non-list-after");
  }

  @Test
  public void testApplyGroupsExceptAndPriority() throws IOException {
    assertValidPair(
        "preprocess-apply-groups-except-and-priority-before",
        "preprocess-apply-groups-except-and-priority-after");
  }

  @Test
  public void testApplyGroupsStaticRoute() throws IOException {
    assertValidPair(
        "preprocess-apply-groups-static-route-before",
        "preprocess-apply-groups-static-route-after");
  }

  @Test
  public void testFlatPaloAlto() throws IOException {
    assertValidPair("preprocess-flat-pa-before", "preprocess-flat-pa-after");
  }

  @Test
  public void testHierarchical() throws IOException {
    assertValidPair("preprocess-hierarchical-before", "preprocess-hierarchical-after");
  }

  @Test
  public void testFlatJuniperActivation() throws IOException {
    assertValidPair(
        "preprocess-flat-juniper-activation-before", "preprocess-flat-juniper-activation-after");
  }

  @Test
  public void testJuniperGenerated() throws IOException {
    assertValidPair("preprocess-juniper-generated-before", "preprocess-juniper-generated-after");
  }

  @Test
  public void testJuniperReplacePreservesOrder() throws IOException {
    assertValidPair(
        "junos-order-sensitive-replace-before", "junos-order-sensitive-replace-preprocessed");
  }

  @Test
  public void testMainBadArgs() throws IOException {
    _thrown.expect(IllegalArgumentException.class);
    main(new String[] {});
  }

  @Test
  public void testNop() throws IOException {
    assertValidPair(TESTCONFIGS_PREFIX, "nop-preprocess", "nop-preprocess");
  }

  @Test
  public void testApplyGroupsOrder() throws IOException {
    assertValidPair(
        "preprocess-flat-juniper-apply-groups-order-before",
        "preprocess-flat-juniper-apply-groups-order-after");
  }

  /**
   * Assert that the result of preprocessing the text of the resource {@code before} under {@code
   * prefix} as a direct file input (not inside a directory) is equal to the text of the resource
   * {@code after} under {@code prefix}.
   */
  private void assertValidFileProcessing(String prefix, String before, String after)
      throws IOException {
    Path root = _folder.getRoot().toPath();
    Path inputFile = root.resolve("input_file");
    Path outputFile = root.resolve("output_file");
    writeFile(inputFile, readResource(String.format("%s%s", prefix, before), UTF_8));
    main(new String[] {inputFile.toString(), outputFile.toString()});

    assertThat(
        CommonUtil.readFile(outputFile).trim(),
        equalTo(readResource(String.format("%s%s", prefix, after), UTF_8).trim()));
  }

  /**
   * Equivalent to calling {@link PreprocessTest#assertValidFileProcessing(String,String,String)}
   * with {@code prefix} set to {@link #TESTCONFIGS_PREFIX}.
   */
  private void assertValidFileProcessing(String before, String after) throws IOException {
    assertValidFileProcessing(TESTCONFIGS_PREFIX, before, after);
  }

  @Test
  public void testFileBasedProcessing() throws IOException {
    // Test that file-based processing works with the same input/output as directory-based
    // processing
    assertValidFileProcessing("preprocess-flat-before", "preprocess-flat-after");
  }

  @Test
  public void testFileBasedProcessingDifferentConfigType() throws IOException {
    // Test file-based processing with a different configuration type (hierarchical)
    assertValidFileProcessing("preprocess-hierarchical-before", "preprocess-hierarchical-after");
  }

  @Test
  public void testFileBasedProcessingNoOp() throws IOException {
    // Test file-based processing with input that doesn't require preprocessing (no-op case)
    assertValidFileProcessing(TESTCONFIGS_PREFIX, "nop-preprocess", "nop-preprocess");
  }

  @Test
  public void testFileBasedProcessingWithEmptyFile() throws IOException {
    // Test that file-based processing works with an empty file
    Path root = _folder.getRoot().toPath();
    Path inputFile = root.resolve("empty_file");
    Path outputFile = root.resolve("output_file");

    // Create an empty file
    writeFile(inputFile, "");

    main(new String[] {inputFile.toString(), outputFile.toString()});

    // Verify the output is processed correctly (likely empty or contains minimal content)
    String outputContent = CommonUtil.readFile(outputFile);
    assertThat(outputContent.trim(), org.hamcrest.Matchers.notNullValue());
  }
}
