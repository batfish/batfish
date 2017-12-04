package org.batfish.datamodel;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DeviceTypeTest {

  NetworkFactory nf;
  Configuration.Builder cb;
  Vrf.Builder vb;

  @Before
  public void initializeBuilders() {
    nf = new NetworkFactory();
    cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    vb = nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
  }

  @Test
  public void emptyConfigurationIsSwitch() {
    Configuration c = cb.build();
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.SWITCH));
  }

  @Test
  public void configWithEmptyVrfIsSwitch() {
    Configuration c = cb.build();
    vb.setOwner(c).build();
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.SWITCH));
  }

  @Test
  public void hostConfigIsHost() {
    Configuration c = this.cb.setConfigurationFormat(ConfigurationFormat.HOST).build();
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.HOST));
  }

  @Test
  public void hostWithBgpIsHost() {
    Configuration c = this.cb.setConfigurationFormat(ConfigurationFormat.HOST).build();
    Vrf vrf = vb.setOwner(c).build();
    nf.bgpProcessBuilder().setVrf(vrf).build();
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.HOST));
  }

  @Test
  public void configWithFirstVrfEmptyIsRouter() {
    Configuration c = cb.build();
    vb.setOwner(c).build();
    Vrf vrf = vb.setOwner(c).build();
    nf.bgpProcessBuilder().setVrf(vrf).build();
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.ROUTER));
  }

  @Test
  public void configWithBgpIsRouter() {
    Configuration c = cb.build();
    Vrf vrf = vb.setOwner(c).build();
    nf.bgpProcessBuilder().setVrf(vrf).build();
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.ROUTER));
  }

  @Test
  public void configWithOspfIsRouter() {
    Configuration c = cb.build();
    Vrf vrf = vb.setOwner(c).build();
    nf.ospfProcessBuilder().setVrf(vrf).build();
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.ROUTER));
  }

  @Test
  public void configWithRipIsRouter() {
    Configuration c = cb.build();
    Vrf vrf = vb.setOwner(c).build();
    nf.ospfProcessBuilder().setVrf(vrf).build();
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.ROUTER));
  }

  // Exact copy of the code executed for each configuration in Batfish.postProcessConfigurations()
  private void postProcessConfiguration(Configuration c) {
    // Set device type to host iff the configuration format is HOST
    if (c.getConfigurationFormat() == ConfigurationFormat.HOST) {
      c.setDeviceType(DeviceType.HOST);
    }
    for (Vrf vrf : c.getVrfs().values()) {
      // If vrf has BGP, OSPF, or RIP process and device isn't a host, set device type to router
      if (c.getDeviceType() == null
          && (vrf.getBgpProcess() != null
              || vrf.getOspfProcess() != null
              || vrf.getRipProcess() != null)) {
        c.setDeviceType(DeviceType.ROUTER);
      }
      // Compute OSPF interface costs where they are missing
      OspfProcess proc = vrf.getOspfProcess();
      if (proc != null) {
        proc.initInterfaceCosts();
      }
    }
    // If device was not a host or router, call it a switch
    if (c.getDeviceType() == null) {
      c.setDeviceType(DeviceType.SWITCH);
    }
  }
}
