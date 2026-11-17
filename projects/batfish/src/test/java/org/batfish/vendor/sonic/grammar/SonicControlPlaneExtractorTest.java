package org.batfish.vendor.sonic.grammar;

import static org.batfish.vendor.sonic.grammar.SonicControlPlaneExtractor.getSonicFileMap;
import static org.batfish.vendor.sonic.grammar.SonicControlPlaneExtractor.getSonicFrrFilename;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import org.batfish.vendor.sonic.grammar.SonicControlPlaneExtractor.SonicFileType;
import org.junit.jupiter.api.Test;

class SonicControlPlaneExtractorTest {

  @Test
  void testGetSonicFileMap() {
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
  void testGetSonicFileMap_Snmp() {
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
  void testGetSonicFileMap_Resolv() {
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
  void testGetSonicFileMap_missingConfigDb() {
    assertThrows(
        IllegalArgumentException.class,
        () -> getSonicFileMap(ImmutableMap.of("frr.conf", "hello")));
  }

  @Test
  void testGetSonicFileMap_missingFrr() {
    assertThrows(
        IllegalArgumentException.class,
        () -> getSonicFileMap(ImmutableMap.of("config_db.json", "{}")));
  }

  @Test
  void testGetSonicFileMap_duplicateType() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            getSonicFileMap(
                ImmutableMap.of("config_db.json", "{}", "device_config_db.json", "{}")));
  }

  @Test
  void testGetSonicFileMap_unknownType() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            // frr is not a valid tail, and config_db's content is not json
            getSonicFileMap(ImmutableMap.of("frr", "hello", "config_db,json", "not json")));
  }

  @Test
  void testGetSonicFileMap_deprecated() {
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
    assertThrows(
        IllegalArgumentException.class,
        () ->
            getSonicFileMap(
                ImmutableMap.of("frr", "hello", "config_db.json", "{}", "resolv.conf", "blah")));
  }

  @Test
  void testGetSonicFrrFilename() {
    assertEquals(
        "frr", getSonicFrrFilename(ImmutableMap.of("frr", "hello", "configdb.json", "{}")));

    // leading whitespace
    assertEquals(
        "frr", getSonicFrrFilename(ImmutableMap.of("frr", "hello", "configdb.json", " \n  {}")));
  }

  @Test
  void testGetSonicFrrFilename_twoJsonFiles() {
    assertThrows(
        IllegalArgumentException.class,
        () -> getSonicFrrFilename(ImmutableMap.of("f1", "{}", "f2", "{}")));
  }

  @Test
  void testGetSonicFrrFilename_noJsonFiles() {
    assertThrows(
        IllegalArgumentException.class,
        () -> getSonicFrrFilename(ImmutableMap.of("f1", "aa", "f2", "ab")));
  }

  @Test
  void testGetSonicFrrFilename_oneFile() {
    assertThrows(
        IllegalArgumentException.class, () -> getSonicFrrFilename(ImmutableMap.of("frr", "hello")));
  }

  @Test
  void testGetSonicFrrFilename_manyFiles() {
    assertThrows(
        IllegalArgumentException.class,
        () -> getSonicFrrFilename(ImmutableMap.of("frr", "hello", "a", "{}", "b", "{}")));
  }
}
