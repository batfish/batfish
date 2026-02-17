package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotNull;

import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciConversion;
import org.junit.Test;

/** Tests for {@link AciConfiguration} L3Out configuration. */
public class AciL3OutOspfTest {

  /** Test L3Out object can be created programmatically. */
  @Test
  public void testL3OutCreation() {
    AciConfiguration config = new AciConfiguration();
    config.setHostname("test-fabric");

    // Create L3Out programmatically
    AciConfiguration.L3Out l3out = config.getOrCreateL3Out("tenant1:l3out1");
    l3out.setTenant("tenant1");
    l3out.setVrf("tenant1:vrf1");
    l3out.setDescription("Test L3Out");

    assertNotNull(l3out);
    assertThat(l3out.getName(), equalTo("tenant1:l3out1"));
    assertThat(l3out.getTenant(), equalTo("tenant1"));
    assertThat(l3out.getVrf(), equalTo("tenant1:vrf1"));
    assertThat(l3out.getDescription(), equalTo("Test L3Out"));
  }

  /** Test L3Out with BGP process configuration. */
  @Test
  public void testL3OutWithBgp() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.L3Out l3out = config.getOrCreateL3Out("tenant1:l3out1");
    l3out.setTenant("tenant1");

    assertNotNull(l3out);
    assertThat(l3out.getName(), equalTo("tenant1:l3out1"));
  }

  /** Test L3Out with BGP peer configuration. */
  @Test
  public void testL3OutBgpPeers() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.L3Out l3out = config.getOrCreateL3Out("tenant1:l3out1");
    l3out.setTenant("tenant1");

    assertNotNull(l3out.getBgpPeers());
    assertThat(l3out.getBgpPeers().size(), equalTo(0));
  }

  /** Test L3Out with static route configuration. */
  @Test
  public void testL3OutStaticRoutes() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.L3Out l3out = config.getOrCreateL3Out("tenant1:l3out1");
    l3out.setTenant("tenant1");

    assertNotNull(l3out.getStaticRoutes());
    assertThat(l3out.getStaticRoutes().size(), equalTo(0));
  }

  /** Test L3Out with external EPG configuration. */
  @Test
  public void testL3OutExternalEpgs() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.L3Out l3out = config.getOrCreateL3Out("tenant1:l3out1");
    l3out.setTenant("tenant1");

    assertNotNull(l3out.getExternalEpgs());
    assertThat(l3out.getExternalEpgs().size(), equalTo(0));
  }

  /** Test L3Out with OSPF configuration. */
  @Test
  public void testL3OutOspf() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.L3Out l3out = config.getOrCreateL3Out("tenant1:l3out1");
    l3out.setTenant("tenant1");

    // OSPF config is null by default
    assertThat(l3out.getOspfConfig(), equalTo(null));

    // Test setting OSPF configuration
    AciConfiguration.OspfConfig ospfConfig = new AciConfiguration.OspfConfig();
    ospfConfig.setProcessId("100");
    l3out.setOspfConfig(ospfConfig);

    assertThat(l3out.getOspfConfig(), equalTo(ospfConfig));
    assertThat(l3out.getOspfConfig().getProcessId(), equalTo("100"));
  }

  /** Test multiple L3Outs. */
  @Test
  public void testMultipleL3Outs() {
    AciConfiguration config = new AciConfiguration();

    config.getOrCreateL3Out("tenant1:l3out1");
    config.getOrCreateL3Out("tenant1:l3out2");
    config.getOrCreateL3Out("tenant2:l3out1");

    assertThat(config.getL3Outs().size(), equalTo(3));
    assertThat(config.getL3Outs(), hasKey("tenant1:l3out1"));
    assertThat(config.getL3Outs(), hasKey("tenant1:l3out2"));
    assertThat(config.getL3Outs(), hasKey("tenant2:l3out1"));
  }

  /** Test L3Out description field. */
  @Test
  public void testL3OutDescription() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.L3Out l3out = config.getOrCreateL3Out("tenant1:l3out1");
    l3out.setDescription("Test description");

    assertThat(l3out.getDescription(), equalTo("Test description"));
  }

  /** Test L3Out VRF field. */
  @Test
  public void testL3OutVrf() {
    AciConfiguration config = new AciConfiguration();

    AciConfiguration.L3Out l3out = config.getOrCreateL3Out("tenant1:l3out1");
    l3out.setVrf("tenant1:vrf1");

    assertThat(l3out.getVrf(), equalTo("tenant1:vrf1"));
  }

  /** Test OSPF area configuration. */
  @Test
  public void testOspfAreaConfiguration() {
    AciConfiguration.L3Out l3out = new AciConfiguration.L3Out("tenant1:l3out1");

    AciConfiguration.OspfConfig ospfConfig = new AciConfiguration.OspfConfig();
    ospfConfig.setProcessId("1");

    AciConfiguration.OspfArea area0 = new AciConfiguration.OspfArea();
    area0.setAreaId("0.0.0.0");
    area0.setAreaType("regular");

    AciConfiguration.OspfArea area1 = new AciConfiguration.OspfArea();
    area1.setAreaId("0.0.0.1");
    area1.setAreaType("stub");

    ospfConfig.getAreas().put("0.0.0.0", area0);
    ospfConfig.getAreas().put("0.0.0.1", area1);

    l3out.setOspfConfig(ospfConfig);

    assertThat(l3out.getOspfConfig(), equalTo(ospfConfig));
    assertThat(l3out.getOspfConfig().getAreas().size(), equalTo(2));
    assertThat(l3out.getOspfConfig().getAreas(), hasKey("0.0.0.0"));
    assertThat(l3out.getOspfConfig().getAreas(), hasKey("0.0.0.1"));
  }

  /** Test OSPF area with networks. */
  @Test
  public void testOspfAreaWithNetworks() {
    AciConfiguration.OspfArea area = new AciConfiguration.OspfArea();
    area.setAreaId("0.0.0.0");

    area.getNetworks().add("10.1.1.0/24");
    area.getNetworks().add("10.2.1.0/24");

    assertThat(area.getNetworks().size(), equalTo(2));
    assertThat(area.getNetworks().get(0), equalTo("10.1.1.0/24"));
    assertThat(area.getNetworks().get(1), equalTo("10.2.1.0/24"));
  }

  /** Test OSPF interface configuration. */
  @Test
  public void testOspfInterfaceConfiguration() {
    AciConfiguration.OspfInterface ospfInterface = new AciConfiguration.OspfInterface();
    ospfInterface.setName("Ethernet1/1");
    ospfInterface.setCost(100);
    ospfInterface.setHelloInterval(10);
    ospfInterface.setDeadInterval(40);
    ospfInterface.setNetworkType("point-to-point");
    ospfInterface.setPassive(true);

    assertThat(ospfInterface.getName(), equalTo("Ethernet1/1"));
    assertThat(ospfInterface.getCost(), equalTo(100));
    assertThat(ospfInterface.getHelloInterval(), equalTo(10));
    assertThat(ospfInterface.getDeadInterval(), equalTo(40));
    assertThat(ospfInterface.getNetworkType(), equalTo("point-to-point"));
    assertThat(ospfInterface.getPassive(), equalTo(true));
  }

  /** Test OSPF interface with broadcast network type. */
  @Test
  public void testOspfInterfaceBroadcast() {
    AciConfiguration.OspfInterface ospfInterface = new AciConfiguration.OspfInterface();
    ospfInterface.setNetworkType("broadcast");

    assertThat(ospfInterface.getNetworkType(), equalTo("broadcast"));
  }

  /** Test OSPF area type conversion. */
  @Test
  public void testOspfAreaTypes() {
    // Test regular area
    AciConfiguration.OspfArea regularArea = new AciConfiguration.OspfArea();
    regularArea.setAreaId("0");
    regularArea.setAreaType("regular");
    assertThat(regularArea.getAreaType(), equalTo("regular"));

    // Test stub area
    AciConfiguration.OspfArea stubArea = new AciConfiguration.OspfArea();
    stubArea.setAreaId("1");
    stubArea.setAreaType("stub");
    assertThat(stubArea.getAreaType(), equalTo("stub"));

    // Test NSSA area
    AciConfiguration.OspfArea nssaArea = new AciConfiguration.OspfArea();
    nssaArea.setAreaId("2");
    nssaArea.setAreaType("nssa");
    assertThat(nssaArea.getAreaType(), equalTo("nssa"));
  }

  // ============================================
  // AciConversion OSPF conversion tests
  // ============================================

  /** Test parseAreaId with numeric area ID. */
  @Test
  public void testParseAreaIdNumeric() {
    assertThat(AciConversion.parseAreaId("0"), equalTo(0L));
    assertThat(AciConversion.parseAreaId("1"), equalTo(1L));
    assertThat(AciConversion.parseAreaId("12345"), equalTo(12345L));
  }

  /** Test parseAreaId with IP address format area ID. */
  @Test
  public void testParseAreaIdIpAddress() {
    // 0.0.0.0 = 0
    assertThat(AciConversion.parseAreaId("0.0.0.0"), equalTo(0L));
    // 0.0.0.1 = 1
    assertThat(AciConversion.parseAreaId("0.0.0.1"), equalTo(1L));
    // 0.0.1.0 = 256
    assertThat(AciConversion.parseAreaId("0.0.1.0"), equalTo(256L));
    // 1.0.0.0 = 16777216
    assertThat(AciConversion.parseAreaId("1.0.0.0"), equalTo(16777216L));
  }

  /** Test parseAreaId with invalid inputs. */
  @Test
  public void testParseAreaIdInvalid() {
    assertThat(AciConversion.parseAreaId(null), nullValue());
    assertThat(AciConversion.parseAreaId(""), nullValue());
    assertThat(AciConversion.parseAreaId("invalid"), nullValue());
    assertThat(AciConversion.parseAreaId("256.0.0.0"), nullValue()); // Invalid octet
  }

  /** Test convertOspfNetworkType with various inputs. */
  @Test
  public void testConvertOspfNetworkType() {
    assertThat(
        AciConversion.convertOspfNetworkType("point-to-point"),
        equalTo(OspfNetworkType.POINT_TO_POINT));
    assertThat(
        AciConversion.convertOspfNetworkType("p2p"), equalTo(OspfNetworkType.POINT_TO_POINT));
    assertThat(
        AciConversion.convertOspfNetworkType("broadcast"), equalTo(OspfNetworkType.BROADCAST));
    assertThat(AciConversion.convertOspfNetworkType("bcast"), equalTo(OspfNetworkType.BROADCAST));
    assertThat(
        AciConversion.convertOspfNetworkType("non-broadcast"),
        equalTo(OspfNetworkType.NON_BROADCAST_MULTI_ACCESS));
    assertThat(
        AciConversion.convertOspfNetworkType("nbma"),
        equalTo(OspfNetworkType.NON_BROADCAST_MULTI_ACCESS));
    assertThat(
        AciConversion.convertOspfNetworkType("point-to-multipoint"),
        equalTo(OspfNetworkType.POINT_TO_MULTIPOINT));
    assertThat(
        AciConversion.convertOspfNetworkType("p2mp"), equalTo(OspfNetworkType.POINT_TO_MULTIPOINT));
  }

  /** Test convertOspfNetworkType with null and invalid inputs. */
  @Test
  public void testConvertOspfNetworkTypeInvalid() {
    assertThat(AciConversion.convertOspfNetworkType(null), nullValue());
    assertThat(AciConversion.convertOspfNetworkType("invalid"), nullValue());
    assertThat(AciConversion.convertOspfNetworkType("unknown"), nullValue());
  }

  /** Test convertOspfNetworkType is case-insensitive. */
  @Test
  public void testConvertOspfNetworkTypeCaseInsensitive() {
    assertThat(
        AciConversion.convertOspfNetworkType("POINT-TO-POINT"),
        equalTo(OspfNetworkType.POINT_TO_POINT));
    assertThat(
        AciConversion.convertOspfNetworkType("BROADCAST"), equalTo(OspfNetworkType.BROADCAST));
    assertThat(
        AciConversion.convertOspfNetworkType("P2P"), equalTo(OspfNetworkType.POINT_TO_POINT));
    assertThat(
        AciConversion.convertOspfNetworkType("NBMA"),
        equalTo(OspfNetworkType.NON_BROADCAST_MULTI_ACCESS));
  }
}
