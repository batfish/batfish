package org.batfish.job;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.job.ParseVendorConfigurationJob.detectFormat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import java.util.Optional;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.answers.ParseStatus;
import org.batfish.identifiers.NetworkId;
import org.batfish.identifiers.SnapshotId;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciFabricLink;
import org.junit.Test;

/** Tests of {@link ParseVendorConfigurationJob}. */
public class ParseVendorConfigurationJobTest {
  private static final String HOST_TESTCONFIGS_PREFIX = "org/batfish/job/host/";

  private static ParseVendorConfigurationResult parseHost(String resourcePath) {
    return new ParseVendorConfigurationJob(
            new Settings(),
            new NetworkSnapshot(new NetworkId("net"), new SnapshotId("ss")),
            ImmutableMap.of("filename", readResource(resourcePath, UTF_8)),
            new Warnings.Settings(false, false, false),
            ConfigurationFormat.HOST,
            ImmutableMultimap.of())
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

  @Test
  public void testFormatUnsupported() {
    ParseResult result =
        new ParseVendorConfigurationJob(
                new Settings(),
                new NetworkSnapshot(new NetworkId("net"), new SnapshotId("ss")),
                ImmutableMap.of("config", "some nonempty content"),
                new Warnings.Settings(false, false, false),
                ConfigurationFormat.UNSUPPORTED,
                ImmutableMultimap.of())
            .parse();
    assertThat(result.getFormat(), equalTo(ConfigurationFormat.UNSUPPORTED));
    assertThat(result.getFailureCause(), nullValue());
    assertThat(result.getConfig(), nullValue());
    assertThat(result.getParseStatus("config"), equalTo(Optional.of(ParseStatus.UNSUPPORTED)));
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
          detectFormat(ImmutableMap.of("file", empty), settings, ConfigurationFormat.UNKNOWN),
          equalTo(ConfigurationFormat.EMPTY));
    }
  }

  // Tests that empty files are detected as empty, even if content is ignored.
  @Test
  public void testDetectFormatEmptyBeatsIgnored() {
    Settings settings = new Settings();
    settings.setIgnoreFilesWithStrings(ImmutableList.of("\n"));
    assertThat(
        detectFormat(ImmutableMap.of("file", "\n"), settings, ConfigurationFormat.UNKNOWN),
        equalTo(ConfigurationFormat.EMPTY));
    assertThat(
        detectFormat(ImmutableMap.of("file", "\n\n\n"), settings, ConfigurationFormat.UNKNOWN),
        equalTo(ConfigurationFormat.EMPTY));
  }

  // Tests that empty files are detected as empty, even if format is given.
  @Test
  public void testDetectFormatEmptyBeatsFormat() {
    assertThat(
        detectFormat(ImmutableMap.of("file", ""), new Settings(), ConfigurationFormat.HOST),
        equalTo(ConfigurationFormat.EMPTY));
  }

  // Tests that files with ignored content are ignored even if another format is provided.
  @Test
  public void testDetectFormatIgnored() {
    String fileText = "!RANCID-CONTENT-TYPE: cisco-nx\n\n\nfoo\nbar\n";
    Settings settings = new Settings();
    // Nothing ignored, is Cisco NX-OS.
    assertThat(
        detectFormat(ImmutableMap.of("file", fileText), settings, ConfigurationFormat.UNKNOWN),
        equalTo(ConfigurationFormat.CISCO_NX));

    // "foo" ignored, file is ignored.
    settings.setIgnoreFilesWithStrings(ImmutableList.of("\n"));
    assertThat(
        detectFormat(ImmutableMap.of("file", fileText), settings, ConfigurationFormat.UNKNOWN),
        equalTo(ConfigurationFormat.IGNORED));
  }

  @Test
  public void testParseCiscoAciWithSupplementalFabricLinks() {
    String apicJson = "{\"polUni\":{\"children\":[]}}";
    String fabricLinksJson =
        "{\"totalCount\":\"1\",\"imdata\":[{\"fabricLink\":{\"attributes\":{"
            + "\"n1\":\"201\",\"n2\":\"101\",\"s1\":\"1\",\"s2\":\"1\","
            + "\"p1\":\"50\",\"p2\":\"24\",\"linkState\":\"ok\""
            + "}}}]}";

    ParseResult result =
        new ParseVendorConfigurationJob(
                new Settings(),
                new NetworkSnapshot(new NetworkId("net"), new SnapshotId("ss")),
                ImmutableMap.of("apic.json", apicJson, "outputtopology.json", fabricLinksJson),
                new Warnings.Settings(false, false, false),
                ConfigurationFormat.CISCO_ACI,
                ImmutableMultimap.of())
            .parse();

    assertThat(result.getFailureCause(), nullValue());
    assertThat(result.getConfig(), not(nullValue()));
    AciConfiguration config = (AciConfiguration) result.getConfig();
    assertThat(config.getHostname(), equalTo("aci-apic"));
    assertThat(config.getFabricLinks().size(), equalTo(1));
    AciFabricLink link = config.getFabricLinks().get(0);
    assertThat(link.getNode1Id(), equalTo("201"));
    assertThat(link.getNode1Interface(), equalTo("Ethernet1/50"));
    assertThat(link.getNode2Id(), equalTo("101"));
    assertThat(link.getNode2Interface(), equalTo("Ethernet1/24"));
  }

  @Test
  public void testParseCiscoAciWithSupplementalFabricLinksSkipsInvalidEntries() {
    String apicJson = "{\"polUni\":{\"children\":[]}}";
    String mixedFabricLinksJson =
        "{\"totalCount\":\"3\",\"imdata\":["
            + "{\"fabricLink\":{\"attributes\":{\"n1\":\"202\",\"n2\":\"102\",\"s1\":\"1\","
            + "\"s2\":\"1\",\"p1\":\"50\",\"p2\":\"4\",\"linkState\":\"ok\"}}},"
            + "{\"fabricLink\":{\"attributes\":{\"n1\":\"202\",\"n2\":\"102\",\"s1\":\"1\","
            + "\"s2\":\"1\",\"p1\":\"51\",\"linkState\":\"ok\"}}},"
            + "{\"fabricLink\":{\"attributes\":{\"n1\":\"202\",\"s1\":\"1\",\"s2\":\"1\","
            + "\"p1\":\"52\",\"p2\":\"5\",\"linkState\":\"ok\"}}}]}";

    ParseResult result =
        new ParseVendorConfigurationJob(
                new Settings(),
                new NetworkSnapshot(new NetworkId("net"), new SnapshotId("ss")),
                ImmutableMap.of(
                    "apic.json", apicJson, "outputtopology_mixed.json", mixedFabricLinksJson),
                new Warnings.Settings(false, false, false),
                ConfigurationFormat.CISCO_ACI,
                ImmutableMultimap.of())
            .parse();

    assertThat(result.getFailureCause(), nullValue());
    assertThat(result.getConfig(), not(nullValue()));
    AciConfiguration config = (AciConfiguration) result.getConfig();
    assertThat(config.getFabricLinks().size(), equalTo(1));
  }
}
