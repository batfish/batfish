package org.batfish.datamodel.ospf;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableSortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.TestInterface;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link org.batfish.datamodel.ospf.OspfProcess} */
@RunWith(JUnit4.class)
public class OspfProcessTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testComputeInterfaceCost() {
    Interface.Builder ib =
        TestInterface.builder()
            .setName("eth0")
            .setBandwidth(1e3)
            .setOwner(new Configuration("r1", ConfigurationFormat.CISCO_IOS));
    Interface i = ib.build();

    // Round up to 1
    int cost = OspfProcess.computeInterfaceCost(1.0, i);
    assertThat(cost, equalTo(1));

    // Defaults for NXOS VLAN iface
    assertThat(
        OspfProcess.computeInterfaceCost(
            4e10,
            TestInterface.builder()
                .setName("Vlan1")
                .setBandwidth(1e9)
                .setOwner(new Configuration("r1", ConfigurationFormat.CISCO_NX))
                .build()),
        equalTo(40));

    cost = OspfProcess.computeInterfaceCost(1e6, i);
    assertThat(cost, equalTo(1000));

    _thrown.expectMessage("Interface eth0 on r1 is missing bandwidth");
    _thrown.expect(IllegalStateException.class);
    OspfProcess.computeInterfaceCost(1e6, ib.setBandwidth(null).build());
  }

  @Test
  public void testInitInterfaceCosts() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 =
        nf.configurationBuilder()
            .setHostname("r1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();

    OspfProcess.Builder opb = nf.ospfProcessBuilder();
    OspfArea.Builder oab = nf.ospfAreaBuilder().setNonStub().setNumber(0L);
    OspfArea area0 = oab.build();
    OspfArea area1 = oab.setNumber(1L).build();

    Interface.Builder ib = nf.interfaceBuilder().setOwner(c1);
    Interface i0 =
        ib.setName("eth0")
            .setBandwidth(1e3)
            .setOspfSettings(
                OspfInterfaceSettings.defaultSettingsBuilder()
                    .setAreaName(area0.getAreaNumber())
                    .build())
            .build();
    area0.addInterface("eth0");
    Interface i1 =
        ib.setName("eth1")
            .setBandwidth(1e4)
            .setOspfSettings(
                OspfInterfaceSettings.defaultSettingsBuilder()
                    .setAreaName(area1.getAreaNumber())
                    .build())
            .build();
    area1.addInterface("eth1");
    Interface i2 =
        ib.setName("eth2")
            .setBandwidth(1e5)
            .setOspfSettings(
                OspfInterfaceSettings.defaultSettingsBuilder()
                    .setAreaName(area1.getAreaNumber())
                    .build())
            .setAdminUp(false)
            .build();
    area1.addInterface("eth2");
    Interface i3 =
        ib.setName("eth3")
            .setBandwidth(1e6)
            .setOspfSettings(
                OspfInterfaceSettings.defaultSettingsBuilder()
                    .setAreaName(area1.getAreaNumber())
                    .setPassive(true)
                    .build())
            .setAdminUp(true)
            .build();
    area1.addInterface("eth3");

    OspfProcess proc =
        opb.setReferenceBandwidth(1e8)
            .setRouterId(Ip.ZERO)
            .setAreas(ImmutableSortedMap.of(0L, area0, 1L, area1))
            .build();

    // Test
    proc.initInterfaceCosts(c1);

    assertThat(i0.getOspfCost(), equalTo(100000));
    assertThat(i1.getOspfCost(), equalTo(10000));
    // No value for shutdown interfaces
    assertThat(i2.getOspfCost(), nullValue());
    // Compute values for passive interfaces
    assertThat(i3.getOspfCost(), equalTo(100));
  }

  @Test
  public void setAreasMismatchedNumbers() {
    OspfProcess proc =
        OspfProcess.builder()
            .setProcessId("1")
            .setRouterId(Ip.ZERO)
            .setReferenceBandwidth(10e8)
            .build();
    _thrown.expect(IllegalArgumentException.class);
    proc.setAreas(ImmutableSortedMap.of(1L, OspfArea.builder().setNumber(2L).build()));
  }

  @Test
  public void setAreasMismatchedNumbersOnBuild() {
    _thrown.expect(IllegalArgumentException.class);
    OspfProcess.builder()
        .setProcessId("1")
        .setReferenceBandwidth(10e8)
        .setAreas(ImmutableSortedMap.of(1L, OspfArea.builder().setNumber(2L).build()))
        .build();
  }
}
