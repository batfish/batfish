package org.batfish.job;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.job.ParseVendorConfigurationJob.detectFormat;
import static org.batfish.job.ParseVendorConfigurationJob.getSonicFileMap;
import static org.batfish.job.ParseVendorConfigurationJob.getSonicFrrFilename;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
import org.batfish.job.ParseVendorConfigurationJob.SonicFileType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests of {@link ParseVendorConfigurationJob}. */
public class ParseVendorConfigurationJobTest {
  private static final String HOST_TESTCONFIGS_PREFIX = "org/batfish/job/host/";

  @Rule public ExpectedException _thrown = ExpectedException.none();

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
  public void testGetSonicFileMap() {
    assertEquals(
        ImmutableMap.of(
            SonicFileType.FRR_CONF, "frr.conf", SonicFileType.CONFIG_DB_JSON, "config_db.json"),
        getSonicFileMap(ImmutableMap.of("frr.conf", "hello", "config_db.json", "{}")));

    // cfg variant for frr
    assertEquals(
        ImmutableMap.of(
            SonicFileType.FRR_CONF, "frr.cfg", SonicFileType.CONFIG_DB_JSON, "config_db.json"),
        getSonicFileMap(ImmutableMap.of("frr.cfg", "hello", "config_db.json", "{}")));

    // non-empty prefixes
    assertEquals(
        ImmutableMap.of(
            SonicFileType.FRR_CONF,
            "device_frr.cfg",
            SonicFileType.CONFIG_DB_JSON,
            "device_config_db.json"),
        getSonicFileMap(ImmutableMap.of("device_frr.cfg", "hello", "device_config_db.json", "{}")));
  }

  @Test
  public void testGetSonicFileMap_Snmp() {
    assertEquals(
        ImmutableMap.of(
            SonicFileType.FRR_CONF,
            "frr.conf",
            SonicFileType.CONFIG_DB_JSON,
            "config_db.json",
            SonicFileType.SNMP_YML,
            "snmp.yml"),
        getSonicFileMap(
            ImmutableMap.of("frr.conf", "hello", "config_db.json", "{}", "snmp.yml", "blah")));

    // non-empty prefix
    assertEquals(
        ImmutableMap.of(
            SonicFileType.FRR_CONF,
            "frr.conf",
            SonicFileType.CONFIG_DB_JSON,
            "config_db.json",
            SonicFileType.SNMP_YML,
            "device_snmp.yml"),
        getSonicFileMap(
            ImmutableMap.of(
                "frr.conf", "hello", "config_db.json", "{}", "device_snmp.yml", "blah")));
  }

  @Test
  public void testGetSonicFileMap_Resolve() {
    assertEquals(
        ImmutableMap.of(
            SonicFileType.FRR_CONF,
            "frr.conf",
            SonicFileType.CONFIG_DB_JSON,
            "config_db.json",
            SonicFileType.RESOLVE_CONF,
            "resolve.conf"),
        getSonicFileMap(
            ImmutableMap.of("frr.conf", "hello", "config_db.json", "{}", "resolve.conf", "blah")));

    // non-empty prefix
    assertEquals(
        ImmutableMap.of(
            SonicFileType.FRR_CONF,
            "frr.conf",
            SonicFileType.CONFIG_DB_JSON,
            "config_db.json",
            SonicFileType.RESOLVE_CONF,
            "device_resolve.conf"),
        getSonicFileMap(
            ImmutableMap.of(
                "frr.conf", "hello", "config_db.json", "{}", "device_resolve.conf", "blah")));
  }

  @Test
  public void testGetSonicFileMap_missingConfigDb() {
    _thrown.expect(IllegalArgumentException.class);
    getSonicFileMap(ImmutableMap.of("frr.conf", "hello"));
  }

  @Test
  public void testGetSonicFileMap_missingFrr() {
    _thrown.expect(IllegalArgumentException.class);
    getSonicFileMap(ImmutableMap.of("config_db.json", "{}"));
  }

  @Test
  public void testGetSonicFileMap_duplicateType() {
    _thrown.expect(IllegalArgumentException.class);
    getSonicFileMap(ImmutableMap.of("config_db.json", "{}", "device_config_db.json", "{}"));
  }

  @Test
  public void testGetSonicFileMap_unknownType() {
    _thrown.expect(IllegalArgumentException.class);
    // frr is not a valid tail, and config_db's content is not json
    getSonicFileMap(ImmutableMap.of("frr", "hello", "config_db,json", "not json"));
  }

  @Test
  public void testGetSonicFileMap_deprecated() {
    // this will delegate to getSonicFrrFilename because frr filename is not legal
    assertEquals(
        ImmutableMap.of(
            SonicFileType.FRR_CONF, "frr", SonicFileType.CONFIG_DB_JSON, "config_db.json"),
        getSonicFileMap(ImmutableMap.of("frr", "hello", "config_db.json", "{}")));

    // this will delegate to getSonicFrrFilename because config_db filename is not legal
    assertEquals(
        ImmutableMap.of(
            SonicFileType.FRR_CONF, "frr.conf", SonicFileType.CONFIG_DB_JSON, "config_db"),
        getSonicFileMap(ImmutableMap.of("frr.conf", "hello", "config_db", "{}")));

    // fail -- don't delegate when there are more than 2 files
    _thrown.expect(IllegalArgumentException.class);
    getSonicFileMap(
        ImmutableMap.of("frr", "hello", "config_db.json", "{}", "resolve.conf", "blah"));
  }

  @Test
  public void testGetSonicFrrFilename() {
    assertEquals(
        "frr", getSonicFrrFilename(ImmutableMap.of("frr", "hello", "configdb.json", "{}")));

    // leading whitespace
    assertEquals(
        "frr", getSonicFrrFilename(ImmutableMap.of("frr", "hello", "configdb.json", " \n  {}")));
  }

  @Test
  public void testGetSonicFrrFilename_twoJsonFiles() {
    _thrown.expect(IllegalArgumentException.class);
    getSonicFrrFilename(ImmutableMap.of("f1", "{}", "f2", "{}"));
  }

  @Test
  public void testGetSonicFrrFilename_noJsonFiles() {
    _thrown.expect(IllegalArgumentException.class);
    getSonicFrrFilename(ImmutableMap.of("f1", "aa", "f2", "ab"));
  }

  @Test
  public void testGetSonicFrrFilename_oneFile() {
    _thrown.expect(IllegalArgumentException.class);
    getSonicFrrFilename(ImmutableMap.of("frr", "hello"));
  }

  @Test
  public void testGetSonicFrrFilename_manyFiles() {
    _thrown.expect(IllegalArgumentException.class);
    getSonicFrrFilename(ImmutableMap.of("frr", "hello", "a", "{}", "b", "{}"));
  }
}
