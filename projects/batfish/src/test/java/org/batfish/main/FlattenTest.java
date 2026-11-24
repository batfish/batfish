package org.batfish.main;

import static com.google.common.io.MoreFiles.createParentDirectories;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.CommonUtil.readFile;
import static org.batfish.common.util.CommonUtil.writeFile;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.main.Flatten.main;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.batfish.common.BfConsts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

final class FlattenTest {

  @TempDir public File _folder;

  @Test
  void testMainBadArgs() {
    assertThrows(IllegalArgumentException.class, () -> main(new String[] {}));
  }

  private static final String TESTCONFIGS_PATH = "org/batfish/main/testconfigs";

  public void assertInputOutputPair(String inputFilename, String outputFilename)
      throws IOException {
    Path root = _folder.toPath();
    Path inputDir = root.resolve("input");
    Path outputDir = root.resolve("output");
    Path inputFile = inputDir.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR).resolve("conf");
    Path outputFile = outputDir.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR).resolve("conf");
    createParentDirectories(inputFile);
    writeFile(
        inputFile, readResource(String.format("%s/%s", TESTCONFIGS_PATH, inputFilename), UTF_8));
    main(new String[] {inputDir.toString(), outputDir.toString()});

    assertThat(
        readFile(outputFile),
        equalTo(readResource(String.format("%s/%s", TESTCONFIGS_PATH, outputFilename), UTF_8)));
  }

  @Test
  void testMainValid() throws IOException {
    assertInputOutputPair("hierarchical", "flat");
  }

  @Test
  void testReplaceOnly() throws IOException {
    assertInputOutputPair("replace_only", "replace_only_flattened");
  }

  @Test
  void testReplaceFlatMix() throws IOException {
    assertInputOutputPair("replace_flat_mix", "replace_flat_mix_flattened");
  }

  @Test
  void testReplaceFilter() throws IOException {
    assertInputOutputPair("replace_filter", "replace_filter_flattened");
  }

  @Test
  void testJunosReplaceOrder() throws IOException {
    assertInputOutputPair(
        "junos-order-sensitive-replace-before", "junos-order-sensitive-replace-flattened");
  }

  @Test
  void testActiveOnly() throws IOException {
    assertInputOutputPair("active_only", "active_only_flattened");
  }

  @Test
  void testMultipleTags() throws IOException {
    assertInputOutputPair("multiple_tags", "multiple_tags_flattened");
  }

  /**
   * Assert that running flatten on {@code inputFilename} as a direct file input (not a snapshot
   * directory) produces the content of {@code outputFilename}.
   */
  private void assertValidFileProcessing(String inputFilename, String outputFilename)
      throws IOException {
    Path root = _folder.toPath();
    Path inputFile = root.resolve("input_file");
    Path outputFile = root.resolve("output_file");
    writeFile(
        inputFile, readResource(String.format("%s/%s", TESTCONFIGS_PATH, inputFilename), UTF_8));
    main(new String[] {inputFile.toString(), outputFile.toString()});

    assertThat(
        readFile(outputFile),
        equalTo(readResource(String.format("%s/%s", TESTCONFIGS_PATH, outputFilename), UTF_8)));
  }

  @Test
  void testFileBasedProcessing() throws IOException {
    assertValidFileProcessing("hierarchical", "flat");
  }
}
