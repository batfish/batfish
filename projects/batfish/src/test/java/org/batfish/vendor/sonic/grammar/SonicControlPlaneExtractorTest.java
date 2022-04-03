package org.batfish.vendor.sonic.grammar;

import static org.batfish.vendor.sonic.grammar.SonicControlPlaneExtractor.getSonicFileMap;
import static org.batfish.vendor.sonic.grammar.SonicControlPlaneExtractor.getSonicFrrFilename;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.batfish.vendor.sonic.grammar.SonicControlPlaneExtractor.SonicFileType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class SonicControlPlaneExtractorTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

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
  public void testGetSonicFileMap_Resolv() {
    assertEquals(
        ImmutableMap.of(
            SonicFileType.FRR_CONF,
            "frr.conf",
            SonicFileType.CONFIG_DB_JSON,
            "config_db.json",
            SonicFileType.RESOLV_CONF,
            "resolv.conf"),
        getSonicFileMap(
            ImmutableMap.of("frr.conf", "hello", "config_db.json", "{}", "resolv.conf", "blah")));

    // non-empty prefix
    assertEquals(
        ImmutableMap.of(
            SonicFileType.FRR_CONF,
            "frr.conf",
            SonicFileType.CONFIG_DB_JSON,
            "config_db.json",
            SonicFileType.RESOLV_CONF,
            "device_resolv.conf"),
        getSonicFileMap(
            ImmutableMap.of(
                "frr.conf", "hello", "config_db.json", "{}", "device_resolv.conf", "blah")));
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
    getSonicFileMap(ImmutableMap.of("frr", "hello", "config_db.json", "{}", "resolv.conf", "blah"));
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
