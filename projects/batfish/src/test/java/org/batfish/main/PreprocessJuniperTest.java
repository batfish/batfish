package org.batfish.main;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Test of {@link PreprocessJuniper}. */
@ParametersAreNonnullByDefault
public final class PreprocessJuniperTest {

  private static final String JUNIPER_TESTCONFIGS_PREFIX =
      "org/batfish/grammar/juniper/testconfigs/";
  private static final String PALO_ALTO_TESTCONFIGS_PREFIX =
      "org/batfish/grammar/palo_alto/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  /**
   * Equivalent to calling {@link PreprocessJuniperTest#assertValidPair(String,String,String)} with
   * {@code prefix} set to {@code JUNIPER_TESTCONFIGS_PREFIX}.
   */
  private void assertValidPair(String before, String after) {
    assertValidPair(JUNIPER_TESTCONFIGS_PREFIX, before, after);
  }

  /**
   * Assert that the result of preprocessing the text of the resource {@code before} under {@code
   * prefix} is equal to the text of the resource {@code after} under {@code prefix}.
   */
  private void assertValidPair(String prefix, String before, String after) {
    Path root = _folder.getRoot().toPath();
    Path inputDir = root.resolve("input");
    Path outputDir = root.resolve("output");
    Path inputFile = inputDir.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR).resolve("conf");
    Path outputFile = outputDir.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR).resolve("conf");
    inputFile.getParent().toFile().mkdirs();
    CommonUtil.writeFile(inputFile, CommonUtil.readResource(String.format("%s%s", prefix, before)));
    PreprocessJuniper.main(new String[] {inputDir.toString(), outputDir.toString()});

    assertThat(
        CommonUtil.readFile(outputFile).trim(),
        equalTo(CommonUtil.readResource(String.format("%s%s", prefix, after)).trim()));
  }

  @Test
  public void testFlat() {
    assertValidPair("preprocess-flat-before", "preprocess-flat-after");
  }

  @Test
  public void testHierarchical() {
    assertValidPair("preprocess-hierarchical-before", "preprocess-hierarchical-after");
  }

  @Test
  public void testMainBadArgs() {
    _thrown.expect(IllegalArgumentException.class);
    PreprocessJuniper.main(new String[] {});
  }

  @Test
  public void testNonJuniper() {
    assertValidPair(PALO_ALTO_TESTCONFIGS_PREFIX, "basic-parsing", "basic-parsing");
  }
}
