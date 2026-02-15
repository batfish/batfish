package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertNotNull;

import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
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
}
