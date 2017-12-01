package org.batfish.datamodel;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DeviceTypeTest {

  @Test
  public void emptyConfigurationIsSwitch() {
    Configuration c = new Configuration("hostname");
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.SWITCH));
  }

  @Test
  public void hostConfigIsHost() {
    Configuration c = new Configuration("hostname");
    c.setConfigurationFormat(ConfigurationFormat.HOST);
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.HOST));
  }

  @Test
  public void hostWithBgpIsHost() {
    Configuration c = new Configuration("hostname");
    c.setConfigurationFormat(ConfigurationFormat.HOST);
    Vrf vrf = new Vrf("vrfName");
    vrf.setBgpProcess(new BgpProcess());
    c.getVrfs().put("sampleVrf", vrf);
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.HOST));
  }

  @Test
  public void configWithFirstVrfEmptyIsRouter() {
    Configuration c = new Configuration("hostname");
    Vrf emptyVrf = new Vrf("empty");
    Vrf vrf = new Vrf("vrfName");
    vrf.setBgpProcess(new BgpProcess());
    c.getVrfs().put("emptyVrf", emptyVrf);
    c.getVrfs().put("sampleVrf", vrf);
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.ROUTER));
  }

  @Test
  public void configWithBgpIsRouter() {
    Configuration c = new Configuration("hostname");
    Vrf vrf = new Vrf("vrfName");
    vrf.setBgpProcess(new BgpProcess());
    c.getVrfs().put("sampleVrf", vrf);
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.ROUTER));
  }

  @Test
  public void configWithOspfIsRouter() {
    Configuration c = new Configuration("hostname");
    Vrf vrf = new Vrf("vrfName");
    vrf.setOspfProcess(new OspfProcess());
    c.getVrfs().put("sampleVrf", vrf);
    postProcessConfiguration(c);
    assertThat(c.getDeviceType(), is(DeviceType.ROUTER));
  }

  @Test
  public void configWithRipIsRouter() {
    Configuration c = new Configuration("hostname");
    Vrf vrf = new Vrf("vrfName");
    vrf.setRipProcess(new RipProcess());
    c.getVrfs().put("sampleVrf", vrf);
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
