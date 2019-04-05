package org.batfish.job;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import org.batfish.common.Warnings;
import org.batfish.common.util.CommonUtil;
import org.batfish.config.Settings;
import org.junit.Test;

/** Tests of {@link FlattenVendorConfigurationJob}. */
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
    // Confirm Juniper nested config is flattened properly
    assertThat(
        getFlattenedText(JUNIPER_TESTCONFIGS_PREFIX + nestedConfig),
        equalTo(CommonUtil.readResource(JUNIPER_TESTCONFIGS_PREFIX + flattenedConfig)));
  }

  @Test
  public void testFlattenVendorConfigurationJobJuniperBrackets() {
    String nestedConfig = "nested-config-brackets";
    String flattenedConfig = "nested-config-brackets-flattened";

    String flatText = getFlattenedText(JUNIPER_TESTCONFIGS_PREFIX + nestedConfig);
    // Confirm Juniper nested config with bracketed list is flattened properly
    assertThat(
        flatText, equalTo(CommonUtil.readResource(JUNIPER_TESTCONFIGS_PREFIX + flattenedConfig)));
  }

  @Test
  public void testFlattenVendorConfigurationJobPaloAlto() {
    String nestedConfig = "nested-config";
    String flattenedConfig = "nested-config-flattened";
    // Confirm Palo Alto nested config is flattened properly
    assertThat(
        getFlattenedText(PAN_TESTCONFIGS_PREFIX + nestedConfig),
        equalTo(CommonUtil.readResource(PAN_TESTCONFIGS_PREFIX + flattenedConfig)));
  }

  @Test
  public void testFlattenVendorConfigurationJobPaloAltoBrackets() {
    String nestedConfig = "nested-config-brackets";
    String flattenedConfig = "nested-config-brackets-flattened";

    String flatText = getFlattenedText(PAN_TESTCONFIGS_PREFIX + nestedConfig);
    // Confirm Palo Alto nested config with bracketed list is flattened properly
    assertThat(
        flatText, equalTo(CommonUtil.readResource(PAN_TESTCONFIGS_PREFIX + flattenedConfig)));
  }

  @Test
  public void testFlattenVendorConfigurationJobUnknown() {
    String unknownFileText = "unknown config format {\ntest;\n}\n";
    FlattenVendorConfigurationJob job =
        new FlattenVendorConfigurationJob(
            new Settings(),
            unknownFileText,
            Paths.get("input"),
            Paths.get("output"),
            new Warnings());
    // Confirm text from unknown config type is unchanged
    assertThat(job.call().getFlattenedText(), equalTo(unknownFileText));
  }

  @Test
  public void testFlattenVendorConfigurationJobVyos() {
    String nestedConfig = "nested-config";
    String flattenedConfig = "nested-config-flattened";
    // Confirm Vyos nested config is flattened properly
    assertThat(
        getFlattenedText(VYOS_TESTCONFIGS_PREFIX + nestedConfig),
        equalTo(CommonUtil.readResource(VYOS_TESTCONFIGS_PREFIX + flattenedConfig)));
  }
}
