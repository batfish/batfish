package org.batfish.datamodel;

import static org.batfish.datamodel.BgpProcess.testBgpProcess;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeviceTypeTest {

  NetworkFactory _nf;
  Configuration.Builder _cb;
  Vrf.Builder _vb;

  @BeforeEach
  void initializeBuilders() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _vb = _nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
  }

  @Test
  void emptyConfigurationIsSwitch() {
    Configuration c = _cb.build();
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.SWITCH));
  }

  @Test
  void configWithEmptyVrfIsSwitch() {
    Configuration c = _cb.build();
    _vb.setOwner(c).build();
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.SWITCH));
  }

  @Test
  void hostConfigIsHost() {
    Configuration c = _cb.setConfigurationFormat(ConfigurationFormat.HOST).build();
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.HOST));
  }

  @Test
  void hostWithBgpIsHost() {
    Configuration c = _cb.setConfigurationFormat(ConfigurationFormat.HOST).build();
    Vrf vrf = _vb.setOwner(c).build();
    vrf.setBgpProcess(testBgpProcess(Ip.ZERO));
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.HOST));
  }

  @Test
  void configWithFirstVrfEmptyIsRouter() {
    Configuration c = _cb.build();
    _vb.setOwner(c).build();
    Vrf vrf = _vb.setOwner(c).build();
    vrf.setBgpProcess(testBgpProcess(Ip.ZERO));
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.ROUTER));
  }

  @Test
  void configWithBgpIsRouter() {
    Configuration c = _cb.build();
    Vrf vrf = _vb.setOwner(c).build();
    vrf.setBgpProcess(testBgpProcess(Ip.ZERO));
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.ROUTER));
  }

  @Test
  void configWithOspfIsRouter() {
    Configuration c = _cb.build();
    Vrf vrf = _vb.setOwner(c).build();
    _nf.ospfProcessBuilder().setRouterId(Ip.ZERO).setVrf(vrf).build();
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.ROUTER));
  }

  @Test
  void configWithRipIsRouter() {
    Configuration c = _cb.build();
    Vrf vrf = _vb.setOwner(c).build();
    _nf.ospfProcessBuilder().setRouterId(Ip.ZERO).setVrf(vrf).build();
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
              || !vrf.getOspfProcesses().isEmpty()
              || vrf.getRipProcess() != null)) {
        c.setDeviceType(DeviceType.ROUTER);
      }
      // Compute OSPF interface costs where they are missing
      vrf.getOspfProcesses().values().forEach(proc -> proc.initInterfaceCosts(c));
    }
    // If device was not a host or router, call it a switch
    if (c.getDeviceType() == null) {
      c.setDeviceType(DeviceType.SWITCH);
    }
  }
}
