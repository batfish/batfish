package org.batfish.main;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.nio.file.Path;
import org.batfish.common.BfConsts;
import org.batfish.common.util.CommonUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public final class FlattenTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testMainBadArgs() {
    _thrown.expect(IllegalArgumentException.class);
    Flatten.main(new String[] {});
  }

  @Test
  public void testMainValid() {
    Path root = _folder.getRoot().toPath();
    Path inputDir = root.resolve("input");
    Path outputDir = root.resolve("output");
    Path inputFile = inputDir.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR).resolve("conf");
    Path outputFile = outputDir.resolve(BfConsts.RELPATH_CONFIGURATIONS_DIR).resolve("conf");
    inputFile.getParent().toFile().mkdirs();
    CommonUtil.writeFile(
        inputFile, CommonUtil.readResource("org/batfish/grammar/juniper/testconfigs/hierarchical"));
    Flatten.main(new String[] {inputDir.toString(), outputDir.toString()});

    assertThat(
        CommonUtil.readFile(outputFile),
        equalTo(CommonUtil.readResource("org/batfish/grammar/juniper/testconfigs/flat")));
  }
}
