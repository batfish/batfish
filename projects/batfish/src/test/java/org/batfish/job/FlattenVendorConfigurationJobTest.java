package org.batfish.job;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.nio.file.Paths;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.junit.Test;

public class FlattenVendorConfigurationJobTest {
  private static final String JUNIPER_TESTCONFIGS_PREFIX =
      "org/batfish/grammar/juniper/testconfigs/";
  private static final String PAN_TESTCONFIGS_PREFIX = "org/batfish/grammar/palo_alto/testconfigs/";
  private static final String VYOS_TESTCONFIGS_PREFIX = "org/batfish/grammar/vyos/testconfigs/";

  private static String getFlattenedText(String resourcePath) {
    FlattenVendorConfigurationJob job =
        new FlattenVendorConfigurationJob(
            new Settings(),
            CommonUtil.readResource(resourcePath),
            Paths.get("input"),
            Paths.get("output"),
            new Warnings());
    FlattenVendorConfigurationResult result = job.call();
    return result.getFlattenedText();
  }

  @Test
  public void testFlattenVendorConfigurationJobJuniper() {
    String nestedConfig = "nested-config";
    String flattenedConfig = "nested-config-flattened";
    assertThat(
        getFlattenedText(JUNIPER_TESTCONFIGS_PREFIX + nestedConfig),
        equalTo(CommonUtil.readResource(JUNIPER_TESTCONFIGS_PREFIX + flattenedConfig)));
  }

  @Test
  public void testFlattenVendorConfigurationJobPaloAlto() {
    String nestedConfig = "nested-config";
    String flattenedConfig = "nested-config-flattened";
    assertThat(
        getFlattenedText(PAN_TESTCONFIGS_PREFIX + nestedConfig),
        equalTo(CommonUtil.readResource(PAN_TESTCONFIGS_PREFIX + flattenedConfig)));
  }

  @Test
  public void testFlattenVendorConfigurationJobVyos() {
    String nestedConfig = "nested-config";
    String flattenedConfig = "nested-config-flattened";
    assertThat(
        getFlattenedText(VYOS_TESTCONFIGS_PREFIX + nestedConfig),
        equalTo(CommonUtil.readResource(VYOS_TESTCONFIGS_PREFIX + flattenedConfig)));
  }
}
