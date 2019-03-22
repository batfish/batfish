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

@ParametersAreNonnullByDefault
public final class PreprocessJuniperTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/juniper/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _thrown = ExpectedException.none();

  private void assertValidPair(String before, String after) {
    Path root = _folder.getRoot().toPath();
    Path inputDir = root.resolve("input");
    Path outputDir = root.resolve("output");
    Path inputFile = inputDir.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR).resolve("conf");
    Path outputFile = outputDir.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR).resolve("conf");
    inputFile.getParent().toFile().mkdirs();
    CommonUtil.writeFile(
        inputFile, CommonUtil.readResource(String.format("%s%s", TESTCONFIGS_PREFIX, before)));
    PreprocessJuniper.main(new String[] {inputDir.toString(), outputDir.toString()});

    assertThat(
        CommonUtil.readFile(outputFile),
        equalTo(CommonUtil.readResource(String.format("%s%s", TESTCONFIGS_PREFIX, after))));
  }

  @Test
  public void testMainBadArgs() {
    _thrown.expect(IllegalArgumentException.class);
    PreprocessJuniper.main(new String[] {});
  }

  @Test
  public void testMainValid() {
    assertValidPair("preprocess-flat-before", "preprocess-flat-after");
    assertValidPair("preprocess-hierarchical-before", "preprocess-hierarchical-after");
  }
}
