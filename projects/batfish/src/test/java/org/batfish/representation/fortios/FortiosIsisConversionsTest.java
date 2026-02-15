package org.batfish.representation.fortios;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.isis.IsisInterfaceSettings;
import org.batfish.datamodel.isis.IsisLevel;
import org.junit.Test;

public class FortiosIsisConversionsTest {

  @Test
  public void testConvertIsis_nullProcess() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIsisConversions.convertIsis(null, c, warnings);

    // Should complete without error
    assertNull(c.getDefaultVrf().getIsisProcess());
    assertTrue(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertIsis_noNetAddress() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    IsisProcess isisProcess = new IsisProcess();
    // No net address set

    Warnings warnings = new Warnings(true, true, true);

    FortiosIsisConversions.convertIsis(isisProcess, c, warnings);

    // Should not create IS-IS process and should warn
    assertNull(c.getDefaultVrf().getIsisProcess());
    assertFalse(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertIsis_invalidNetAddress() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    IsisProcess isisProcess = new IsisProcess();
    isisProcess.setNetAddress("invalid-net-address");

    Warnings warnings = new Warnings(true, true, true);

    FortiosIsisConversions.convertIsis(isisProcess, c, warnings);

    // Should not create IS-IS process and should warn
    assertNull(c.getDefaultVrf().getIsisProcess());
    assertFalse(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertIsis_level1Only() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    IsisProcess isisProcess = new IsisProcess();
    isisProcess.setNetAddress("49.0001.0000.0000.0001.00");
    isisProcess.setIsType(IsisProcess.Level.LEVEL_1);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIsisConversions.convertIsis(isisProcess, c, warnings);

    org.batfish.datamodel.isis.IsisProcess viProc = c.getDefaultVrf().getIsisProcess();
    assertNotNull(viProc);
    assertNotNull(viProc.getLevel1());
    assertNull(viProc.getLevel2());
    assertTrue(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertIsis_level2Only() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    IsisProcess isisProcess = new IsisProcess();
    isisProcess.setNetAddress("49.0001.0000.0000.0001.00");
    isisProcess.setIsType(IsisProcess.Level.LEVEL_2);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIsisConversions.convertIsis(isisProcess, c, warnings);

    org.batfish.datamodel.isis.IsisProcess viProc = c.getDefaultVrf().getIsisProcess();
    assertNotNull(viProc);
    assertNull(viProc.getLevel1());
    assertNotNull(viProc.getLevel2());
    assertTrue(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertIsis_level1_2() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    IsisProcess isisProcess = new IsisProcess();
    isisProcess.setNetAddress("49.0001.0000.0000.0001.00");
    isisProcess.setIsType(IsisProcess.Level.LEVEL_1_2);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIsisConversions.convertIsis(isisProcess, c, warnings);

    org.batfish.datamodel.isis.IsisProcess viProc = c.getDefaultVrf().getIsisProcess();
    assertNotNull(viProc);
    assertNotNull(viProc.getLevel1());
    assertNotNull(viProc.getLevel2());
    assertTrue(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertIsis_defaultIsType() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    IsisProcess isisProcess = new IsisProcess();
    isisProcess.setNetAddress("49.0001.0000.0000.0001.00");
    // No is-type set, should default to level-1-2

    Warnings warnings = new Warnings(true, true, true);

    FortiosIsisConversions.convertIsis(isisProcess, c, warnings);

    org.batfish.datamodel.isis.IsisProcess viProc = c.getDefaultVrf().getIsisProcess();
    assertNotNull(viProc);
    assertNotNull(viProc.getLevel1());
    assertNotNull(viProc.getLevel2());
  }

  @Test
  public void testConvertIsisInterface_disabled() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    Interface iface =
        Interface.builder()
            .setName("port1")
            .setType(InterfaceType.PHYSICAL)
            .setAdminUp(true)
            .build();
    iface.setVrfName("default");
    c.getAllInterfaces().put("port1", iface);

    IsisProcess isisProcess = new IsisProcess();
    isisProcess.setNetAddress("49.0001.0000.0000.0001.00");

    IsisInterface isisIface = new IsisInterface("port1");
    isisIface.setStatus(false); // Disabled
    isisProcess.getInterfaces().put("port1", isisIface);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIsisConversions.convertIsis(isisProcess, c, warnings);

    // Interface should not have IS-IS settings since it's disabled
    assertNull(c.getAllInterfaces().get("port1").getIsis());
  }

  @Test
  public void testConvertIsisInterface_missingInterface() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    IsisProcess isisProcess = new IsisProcess();
    isisProcess.setNetAddress("49.0001.0000.0000.0001.00");

    IsisInterface isisIface = new IsisInterface("nonexistent");
    isisIface.setStatus(true);
    isisProcess.getInterfaces().put("nonexistent", isisIface);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIsisConversions.convertIsis(isisProcess, c, warnings);

    // Should warn about missing interface
    assertFalse(warnings.getRedFlagWarnings().isEmpty());
  }

  @Test
  public void testConvertIsisInterface_metricOverride() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    Interface iface =
        Interface.builder()
            .setName("port1")
            .setType(InterfaceType.PHYSICAL)
            .setAdminUp(true)
            .build();
    iface.setVrfName("default");
    c.getAllInterfaces().put("port1", iface);

    IsisProcess isisProcess = new IsisProcess();
    isisProcess.setNetAddress("49.0001.0000.0000.0001.00");

    IsisInterface isisIface = new IsisInterface("port1");
    isisIface.setStatus(true);
    isisIface.setMetric(50);
    isisIface.setMetricLevel1(100);
    isisIface.setMetricLevel2(200);
    isisProcess.getInterfaces().put("port1", isisIface);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIsisConversions.convertIsis(isisProcess, c, warnings);

    IsisInterfaceSettings isisSettings = c.getAllInterfaces().get("port1").getIsis();
    assertNotNull(isisSettings);
    // Level-specific metrics should be used
    assertThat(isisSettings.getLevel1().getCost(), equalTo(100L));
    assertThat(isisSettings.getLevel2().getCost(), equalTo(200L));
  }

  @Test
  public void testConvertIsisInterface_circuitTypeFilter() {
    Configuration c = new Configuration("test", ConfigurationFormat.FORTIOS);
    Vrf vrf = new Vrf("default");
    c.getVrfs().put("default", vrf);

    Interface iface =
        Interface.builder()
            .setName("port1")
            .setType(InterfaceType.PHYSICAL)
            .setAdminUp(true)
            .build();
    iface.setVrfName("default");
    c.getAllInterfaces().put("port1", iface);

    IsisProcess isisProcess = new IsisProcess();
    isisProcess.setNetAddress("49.0001.0000.0000.0001.00");
    isisProcess.setIsType(IsisProcess.Level.LEVEL_1_2);

    IsisInterface isisIface = new IsisInterface("port1");
    isisIface.setStatus(true);
    isisIface.setCircuitType(IsisProcess.Level.LEVEL_1); // Only level-1
    isisProcess.getInterfaces().put("port1", isisIface);

    Warnings warnings = new Warnings(true, true, true);

    FortiosIsisConversions.convertIsis(isisProcess, c, warnings);

    IsisInterfaceSettings isisSettings = c.getAllInterfaces().get("port1").getIsis();
    assertNotNull(isisSettings);
    // Only level-1 should be enabled
    assertThat(isisSettings.getEnabledLevels(), equalTo(IsisLevel.LEVEL_1));
  }

  @Test
  public void testIsisInterfaceDefaults() {
    IsisInterface iface = new IsisInterface("test");

    assertThat(iface.getCircuitTypeEffective(), equalTo(IsisInterface.DEFAULT_CIRCUIT_TYPE));
    assertThat(iface.getMetricEffective(), equalTo(IsisInterface.DEFAULT_METRIC));
    assertThat(iface.getBfdEffective(), equalTo(IsisInterface.DEFAULT_BFD));
    assertThat(iface.getStatusEffective(), equalTo(IsisInterface.DEFAULT_STATUS));
  }

  @Test
  public void testIsisLevelParsing() {
    IsisProcess process = new IsisProcess();

    process.setIsType(IsisProcess.Level.LEVEL_1);
    assertThat(process.getIsTypeEffective(), equalTo(IsisProcess.Level.LEVEL_1));

    process.setIsType(IsisProcess.Level.LEVEL_2);
    assertThat(process.getIsTypeEffective(), equalTo(IsisProcess.Level.LEVEL_2));

    process.setIsType(IsisProcess.Level.LEVEL_1_2);
    assertThat(process.getIsTypeEffective(), equalTo(IsisProcess.Level.LEVEL_1_2));

    // Test default
    process.setIsType(null);
    assertThat(process.getIsTypeEffective(), equalTo(IsisProcess.DEFAULT_IS_TYPE));
  }
}
