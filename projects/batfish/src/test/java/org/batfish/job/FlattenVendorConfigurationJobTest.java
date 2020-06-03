package org.batfish.job;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.junit.Test;

/** Tests of {@link FlattenVendorConfigurationJob}. */
public class FlattenVendorConfigurationJobTest {
  private static final String JUNIPER_TESTCONFIGS_PREFIX = "org/batfish/job/juniper/";
  private static final String PAN_TESTCONFIGS_PREFIX = "org/batfish/job/palo_alto/";
  private static final String VYOS_TESTCONFIGS_PREFIX = "org/batfish/job/vyos/";

  private static String getFlattenedText(String resourcePath) {
    FlattenVendorConfigurationJob job =
        new FlattenVendorConfigurationJob(
            new Settings(),
            readResource(resourcePath, UTF_8),
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
        equalTo(readResource(JUNIPER_TESTCONFIGS_PREFIX + flattenedConfig, UTF_8)));
  }

  @Test
  public void testFlattenVendorConfigurationJobJuniperBrackets() {
    String nestedConfig = "nested-config-brackets";
    String flattenedConfig = "nested-config-brackets-flattened";

    String flatText = getFlattenedText(JUNIPER_TESTCONFIGS_PREFIX + nestedConfig);
    // Confirm Juniper nested config with bracketed list is flattened properly
    assertThat(
        flatText, equalTo(readResource(JUNIPER_TESTCONFIGS_PREFIX + flattenedConfig, UTF_8)));
  }

  @Test
  public void testFlattenVendorConfigurationJobPaloAlto() {
    String nestedConfig = "nested-config";
    String flattenedConfig = "nested-config-flattened";
    // Confirm Palo Alto nested config is flattened properly
    assertThat(
        getFlattenedText(PAN_TESTCONFIGS_PREFIX + nestedConfig),
        equalTo(readResource(PAN_TESTCONFIGS_PREFIX + flattenedConfig, UTF_8)));
  }

  @Test
  public void testFlattenVendorConfigurationJobPaloAltoBrackets() {
    String nestedConfig = "nested-config-brackets";
    String flattenedConfig = "nested-config-brackets-flattened";

    String flatText = getFlattenedText(PAN_TESTCONFIGS_PREFIX + nestedConfig);
    // Confirm Palo Alto nested config with bracketed list is flattened properly
    assertThat(flatText, equalTo(readResource(PAN_TESTCONFIGS_PREFIX + flattenedConfig, UTF_8)));
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
        equalTo(readResource(VYOS_TESTCONFIGS_PREFIX + flattenedConfig, UTF_8)));
  }
}
