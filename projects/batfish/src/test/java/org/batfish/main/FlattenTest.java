package org.batfish.main;

import static com.google.common.io.MoreFiles.createParentDirectories;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.CommonUtil.readFile;
import static org.batfish.common.util.CommonUtil.writeFile;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.main.Flatten.main;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import java.nio.file.Path;
import org.batfish.common.BfConsts;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public final class FlattenTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testMainBadArgs() throws IOException {
    _thrown.expect(IllegalArgumentException.class);
    main(new String[] {});
  }

  private static final String TESTCONFIGS_PATH = "org/batfish/main/testconfigs";

  public void assertInputOutputPair(String inputFilename, String outputFilename)
      throws IOException {
    Path root = _folder.getRoot().toPath();
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
  public void testMainValid() throws IOException {
    assertInputOutputPair("hierarchical", "flat");
  }

  @Test
  public void testReplaceOnly() throws IOException {
    assertInputOutputPair("replace_only", "replace_only_flattened");
  }

  @Test
  public void testReplaceFlatMix() throws IOException {
    assertInputOutputPair("replace_flat_mix", "replace_flat_mix_flattened");
  }

  @Test
  public void testReplaceFilter() throws IOException {
    assertInputOutputPair("replace_filter", "replace_filter_flattened");
  }

  @Test
  public void testJunosReplaceOrder() throws IOException {
    assertInputOutputPair(
        "junos-order-sensitive-replace-before", "junos-order-sensitive-replace-flattened");
  }

  @Test
  public void testActiveOnly() throws IOException {
    assertInputOutputPair("active_only", "active_only_flattened");
  }

  @Test
  public void testMultipleTags() throws IOException {
    assertInputOutputPair("multiple_tags", "multiple_tags_flattened");
  }
}
