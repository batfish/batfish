package org.batfish.main.annotate;

import static com.google.common.io.MoreFiles.createParentDirectories;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.CommonUtil.writeFile;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.main.annotate.Annotate.main;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Test of {@link Annotate}. */
@ParametersAreNonnullByDefault
public final class AnnotateTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/main/annotate/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  /**
   * Equivalent to calling {@link AnnotateTest#assertValidPair(String,String,String)} with {@code
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
  public void testHierarchical() throws IOException {
    assertValidPair("annotate-hierarchical-before", "annotate-hierarchical-after");
  }
}
