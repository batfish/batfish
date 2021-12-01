package org.batfish.job;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.job.ParseVendorConfigurationJob.detectFormat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.junit.Test;

/** Tests of {@link ParseVendorConfigurationJob}. */
public class ParseVendorConfigurationJobTest {
  private static final String HOST_TESTCONFIGS_PREFIX = "org/batfish/job/host/";

  private static ParseVendorConfigurationResult parseHost(String resourcePath) {
    return new ParseVendorConfigurationJob(
            new Settings(),
            new NetworkSnapshot(new NetworkId("net"), new SnapshotId("ss")),
            readResource(resourcePath, UTF_8),
            "filename",
            new Warnings(),
            ConfigurationFormat.HOST,
            ImmutableMultimap.of(),
            null)
        .call();
  }

  @Test
  public void testHost() {
    ParseVendorConfigurationResult result = parseHost(HOST_TESTCONFIGS_PREFIX + "host.json");
    // Confirm a good host file results in no failure cause
    assertThat(result.getFailureCause(), equalTo(null));
  }

  @Test
  public void testHostInvalid() {
    ParseVendorConfigurationResult result = parseHost(HOST_TESTCONFIGS_PREFIX + "hostInvalid.json");
    // Confirm a bad host file does not cause a crash but results in failure cause
    assertThat(result.getFailureCause(), not(equalTo(null)));
  }

  // Tests that empty files are detected as empty, even when another format is provided.
  @Test
  public void testDetectFormatEmpty() {
    String[] empties = {
      "", "\n", "\t", " ", "\r\n", "   \r\n\r\n\t\n\n   ",
    };
    Settings settings = new Settings();
    Settings ignored = new Settings();
    ignored.setIgnoreFilesWithStrings(ImmutableList.of("\n"));
    for (String empty : empties) {
      assertThat(
          detectFormat(empty, settings, ConfigurationFormat.UNKNOWN),
          equalTo(ConfigurationFormat.EMPTY));
    }
  }

  // Tests that empty files are detected as empty, even if content is ignored.
  @Test
  public void testDetectFormatEmptyBeatsIgnored() {
    Settings settings = new Settings();
    settings.setIgnoreFilesWithStrings(ImmutableList.of("\n"));
    assertThat(
        detectFormat("\n", settings, ConfigurationFormat.UNKNOWN),
        equalTo(ConfigurationFormat.EMPTY));
    assertThat(
        detectFormat("\n\n\n", settings, ConfigurationFormat.UNKNOWN),
        equalTo(ConfigurationFormat.EMPTY));
  }

  // Tests that empty files are detected as empty, even if format is given.
  @Test
  public void testDetectFormatEmptyBeatsFormat() {
    assertThat(
        detectFormat("", new Settings(), ConfigurationFormat.HOST),
        equalTo(ConfigurationFormat.EMPTY));
  }

  // Tests that files with ignored content are ignored even if another format is provided.
  @Test
  public void testDetectFormatIgnored() {
    String fileText = "!RANCID-CONTENT-TYPE: cisco-nx\n\n\nfoo\nbar\n";
    Settings settings = new Settings();
    // Nothing ignored, is Cisco NX-OS.
    assertThat(
        detectFormat(fileText, settings, ConfigurationFormat.UNKNOWN),
        equalTo(ConfigurationFormat.CISCO_NX));

    // "foo" ignored, file is ignored.
    settings.setIgnoreFilesWithStrings(ImmutableList.of("\n"));
    assertThat(
        detectFormat(fileText, settings, ConfigurationFormat.UNKNOWN),
        equalTo(ConfigurationFormat.IGNORED));
  }
}
