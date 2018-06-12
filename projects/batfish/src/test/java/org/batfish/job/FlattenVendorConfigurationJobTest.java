package org.batfish.job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.nio.file.Paths;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class FlattenVendorConfigurationJobTest {
  private static final String PAN_TESTCONFIGS_PREFIX = "org/batfish/grammar/palo_alto/testconfigs/";

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testFlattenVendorConfigurationJobPaloAlto() {
    String nestedConfig = "nested-config";
    String flattenedConfig = "nested-config-flattened";
    FlattenVendorConfigurationJob job =
        new FlattenVendorConfigurationJob(
            new Settings(),
            CommonUtil.readResource(PAN_TESTCONFIGS_PREFIX + nestedConfig),
            Paths.get("input"),
            Paths.get("output"),
            new Warnings());
    FlattenVendorConfigurationResult result = job.call();
    assertThat(
        result.getFlattenedText(),
        equalTo(CommonUtil.readResource(PAN_TESTCONFIGS_PREFIX + flattenedConfig)));
  }
}
