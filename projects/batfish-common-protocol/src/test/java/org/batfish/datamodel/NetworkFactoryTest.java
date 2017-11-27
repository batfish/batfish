package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class NetworkFactoryTest {

  private static final ConfigurationFormat CONFIG_FORMAT = ConfigurationFormat.CISCO_IOS;

  @Test
  public void testNetworkFactory() {}

  @Test
  public void testConfigurationBuilder() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb = nf.configurationBuilder().setConfigurationFormat(CONFIG_FORMAT);
    Configuration c1 = cb.build();
    Configuration c2 = cb.build();

    assertThat(c1.getName(), is(notNullValue()));
    assertThat(c2.getName(), is(notNullValue()));
    assertThat(c1.getName(), not(equalTo(c2.getName())));
  }

  @Test
  public void testVrfBuilder() {
    NetworkFactory nf = new NetworkFactory();
    Vrf.Builder vb = nf.vrfBuilder();
    Vrf v1 = vb.build();
    Vrf v2 = vb.build();
    Configuration c = nf.configurationBuilder().setConfigurationFormat(CONFIG_FORMAT).build();
    vb.setOwner(c);
    Vrf v3 = vb.build();

    assertThat(v1.getName(), is(notNullValue()));
    assertThat(v2.getName(), is(notNullValue()));
    assertThat(v1.getName(), not(equalTo(v2.getName())));
    assertThat(v1.getName(), not(isIn(c.getVrfs().keySet())));
    assertThat(v2.getName(), not(isIn(c.getVrfs().keySet())));
    assertThat(v3.getName(), isIn(c.getVrfs().keySet()));
  }

  @Test
  public void testInterfaceBuilder() {
    NetworkFactory nf = new NetworkFactory();
    Interface.Builder ib = nf.interfaceBuilder();
    Interface i1 = ib.build();
    Interface i2 = ib.build();
    Configuration c = nf.configurationBuilder().setConfigurationFormat(CONFIG_FORMAT).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    ib.setOwner(c);
    Interface i3 = ib.build();
    ib.setVrf(vrf);
    Interface i4 = ib.build();

    assertThat(i1.getName(), is(notNullValue()));
    assertThat(i1.getName(), not(equalTo(i2.getName())));
    assertThat(i1.getName(), not(isIn(c.getInterfaces().keySet())));
    assertThat(i2.getName(), is(notNullValue()));
    assertThat(i2.getName(), not(isIn(c.getInterfaces().keySet())));
    assertThat(i3.getName(), isIn(c.getInterfaces().keySet()));
    assertThat(i3.getOwner(), sameInstance(c));
    assertThat(i3.getName(), not(isIn(vrf.getInterfaces().keySet())));
    assertThat(i4.getName(), isIn(c.getInterfaces().keySet()));
    assertThat(i4.getName(), isIn(vrf.getInterfaces().keySet()));
    assertThat(i4.getVrf(), sameInstance(vrf));
  }

  @Test
  public void testOspfProcessBuilder() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().setConfigurationFormat(CONFIG_FORMAT).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    OspfProcess.Builder ob = nf.ospfProcessBuilder();
    OspfProcess o1 = ob.build();
    OspfProcess o2 = ob.setVrf(vrf).build();
    assertThat(o1, not(sameInstance(o2)));
    assertThat(vrf.getOspfProcess(), sameInstance(o2));
  }

  @Test
  public void testOspfAreaBuilder() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().setConfigurationFormat(CONFIG_FORMAT).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    OspfProcess.Builder ob = nf.ospfProcessBuilder();
    OspfProcess ospfProcess = ob.setVrf(vrf).build();
    OspfArea.Builder oab = nf.ospfAreaBuilder();
    OspfArea oa1 = oab.build();
    OspfArea oa2 = oab.setOspfProcess(ospfProcess).build();
    Interface iface = nf.interfaceBuilder().setOwner(c).setVrf(vrf).setOspfArea(oa2).build();

    assertThat(oa1.getName(), not(equalTo(oa2.getName())));
    assertThat(oa1, not(sameInstance(oa2)));
    assertThat(ospfProcess.getAreas().get(oa2.getName()), sameInstance(oa2));
    assertThat(oa2.getInterfaces().get(iface.getName()), sameInstance(iface));
    assertThat(iface.getOspfAreaName(), equalTo(oa2.getName()));
  }
}
