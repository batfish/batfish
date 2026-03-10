package org.batfish.representation.cumulus_concatenated;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Interface.DEFAULT_MTU;
import static org.batfish.datamodel.matchers.ConfigurationMatchers.hasInterface;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasBandwidth;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInterfaceType;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.representation.cumulus_concatenated.CumulusConcatenatedConfiguration.convertVxlans;
import static org.batfish.representation.cumulus_concatenated.CumulusConcatenatedConfiguration.isValidVIInterface;
import static org.batfish.representation.cumulus_concatenated.CumulusConcatenatedConfiguration.populateLoopbackProperties;
import static org.batfish.representation.cumulus_concatenated.InterfaceConverter.BRIDGE_NAME;
import static org.batfish.representation.frr.FrrConfiguration.LINK_LOCAL_ADDRESS;
import static org.batfish.representation.frr.FrrConfiguration.LOOPBACK_INTERFACE_NAME;
import static org.batfish.representation.frr.FrrConversions.DEFAULT_LOOPBACK_BANDWIDTH;
import static org.batfish.representation.frr.FrrConversions.DEFAULT_PORT_BANDWIDTH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.common.Warnings;
import org.batfish.common.runtime.InterfaceRuntimeData;
import org.batfish.common.runtime.RuntimeData;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.representation.cumulus_concatenated.CumulusPortsConfiguration.PortSettings;
import org.batfish.representation.frr.BgpInterfaceNeighbor;
import org.batfish.representation.frr.BgpNeighbor;
import org.batfish.representation.frr.BgpNeighbor.RemoteAs;
import org.batfish.representation.frr.BgpProcess;
import org.batfish.representation.frr.BgpVrf;
import org.batfish.representation.frr.FrrConfiguration;
import org.batfish.representation.frr.FrrInterface;
import org.batfish.representation.frr.StaticRoute;
import org.junit.Test;

/** Test for {@link CumulusConcatenatedConfiguration}. */
public class CumulusConcatenatedConfigurationTest {

  @Test
  public void testConvertVxlan_localIpPrecedence() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    Vrf vrf = new Vrf("vrf");
    c.setVrfs(ImmutableMap.of(vrf.getName(), vrf));

    Ip vxlanLocalTunnelIp = Ip.parse("1.1.1.1");
    Ip loopbackTunnelIp = Ip.parse("2.2.2.2");
    Ip loopbackAnycastIp = Ip.parse("3.3.3.3");

    InterfacesInterface vxlan = new InterfacesInterface("vxlan1001");
    vxlan.setVxlanId(1001);
    vxlan.setVxlanLocalTunnelIp(vxlanLocalTunnelIp);
    vxlan.createOrGetBridgeSettings().setAccess(101);

    CumulusInterfacesConfiguration ifaces = new CumulusInterfacesConfiguration();
    ifaces.getInterfaces().put(vxlan.getName(), vxlan);

    CumulusConcatenatedConfiguration vc =
        CumulusConcatenatedConfiguration.builder()
            .setHostname("c")
            .setInterfacesConfiguration(ifaces)
            .build();

    // vxlan's local tunnel ip should win when anycast is null
    convertVxlans(
        c, vc, ImmutableMap.of(1001, vrf.getName()), null, loopbackTunnelIp, new Warnings());
    assertThat(vrf.getLayer3Vnis().get(1001).getSourceAddress(), equalTo(vxlanLocalTunnelIp));

    // anycast should win if non-null
    vrf.setLayer3Vnis(ImmutableList.of()); // wipe out prior state
    convertVxlans(
        c,
        vc,
        ImmutableMap.of(1001, vrf.getName()),
        loopbackAnycastIp,
        loopbackTunnelIp,
        new Warnings());
    assertThat(vrf.getLayer3Vnis().get(1001).getSourceAddress(), equalTo(loopbackAnycastIp));

    // loopback tunnel ip should win when nothing else is present
    vrf.setLayer3Vnis(ImmutableList.of()); // wipe out prior state
    vxlan.setVxlanLocalTunnelIp(null);
    convertVxlans(
        c, vc, ImmutableMap.of(1001, vrf.getName()), null, loopbackTunnelIp, new Warnings());
    assertThat(vrf.getLayer3Vnis().get(1001).getSourceAddress(), equalTo(loopbackTunnelIp));
  }

  /** Test that loopback interface is unconditionally created */
  @Test
  public void testInitializeAllInterfaces_createLoopback() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    CumulusConcatenatedConfiguration.builder().setHostname("c").build().initializeAllInterfaces(c);
    assertTrue(c.getAllInterfaces().containsKey(LOOPBACK_INTERFACE_NAME));
    assertThat(
        c.getAllInterfaces().get(LOOPBACK_INTERFACE_NAME),
        hasBandwidth(DEFAULT_LOOPBACK_BANDWIDTH));
  }

  private static SnapshotRuntimeData makeRuntimeData(String hostname, double loopbackBandwidth) {
    InterfaceRuntimeData ifaceData =
        InterfaceRuntimeData.builder().setBandwidth(loopbackBandwidth).build();
    RuntimeData hostData =
        RuntimeData.builder()
            .setInterfaces(ImmutableMap.of(LOOPBACK_INTERFACE_NAME, ifaceData))
            .build();
    return SnapshotRuntimeData.builder()
        .setRuntimeData(ImmutableMap.of(hostname, hostData))
        .build();
  }

  /** Test bandwidths incorporate interface runtime data. */
  @Test
  public void testInterfaceRuntimeData() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    CumulusConcatenatedConfiguration.builder()
        .setHostname("c")
        .setSnapshotRuntimeData(makeRuntimeData(c.getHostname(), 123456.0))
        .build()
        .initializeAllInterfaces(c);
    assertThat(c, hasInterface(LOOPBACK_INTERFACE_NAME, hasBandwidth(123456.0)));
  }

  /** Test bandwidths ignore invalid interface runtime data. */
  @Test
  public void testInterfaceRuntimeDataInvalidTooLow() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    CumulusConcatenatedConfiguration vs =
        CumulusConcatenatedConfiguration.builder()
            .setHostname("c")
            .setSnapshotRuntimeData(makeRuntimeData(c.getHostname(), 0))
            .build();
    Warnings w = new Warnings(true, true, true);
    vs.setWarnings(w);
    vs.initializeAllInterfaces(c);
    assertThat(c, hasInterface(LOOPBACK_INTERFACE_NAME, hasBandwidth(DEFAULT_LOOPBACK_BANDWIDTH)));
    assertThat(w.getRedFlagWarnings(), hasSize(1));
  }

  /** Test bandwidths ignore invalid interface runtime data. */
  @Test
  public void testInterfaceRuntimeDataInvalidTooHigh() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    CumulusConcatenatedConfiguration vs =
        CumulusConcatenatedConfiguration.builder()
            .setHostname("c")
            .setSnapshotRuntimeData(makeRuntimeData(c.getHostname(), 1e20))
            .build();
    Warnings w = new Warnings(true, true, true);
    vs.setWarnings(w);
    vs.initializeAllInterfaces(c);
    assertThat(c, hasInterface(LOOPBACK_INTERFACE_NAME, hasBandwidth(DEFAULT_LOOPBACK_BANDWIDTH)));
    assertThat(w.getRedFlagWarnings(), hasSize(1));
  }

  /** Test that bridge is not included as an interface */
  @Test
  public void testInitializeAllInterfaces_noBridge() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    InterfacesInterface bridge = new InterfacesInterface(BRIDGE_NAME);
    CumulusConcatenatedConfiguration.builder()
        .setHostname("c")
        .addInterfaces(ImmutableMap.of(bridge.getName(), bridge))
        .build()
        .initializeAllInterfaces(c);
    assertFalse(c.getAllInterfaces().containsKey(BRIDGE_NAME));
  }

  /** Interfaces in interfaceConfiguration are included */
  @Test
  public void testInitializeAllInterfaces_interfacesInterfaces() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    InterfacesInterface iface1 = new InterfacesInterface("interfaces");
    iface1.setVrf("vrf1");
    CumulusConcatenatedConfiguration.builder()
        .setHostname("c")
        .addInterfaces(ImmutableMap.of(iface1.getName(), iface1))
        .build()
        .initializeAllInterfaces(c);
    assertTrue(c.getAllInterfaces().containsKey(iface1.getName()));
    assertEquals(c.getAllInterfaces().get(iface1.getName()).getVrfName(), iface1.getVrf());
    assertThat(
        c.getAllInterfaces().get(iface1.getName()).getBandwidth(), equalTo(DEFAULT_PORT_BANDWIDTH));
  }

  /** Interfaces in frrConfiguration are included */
  @Test
  public void testInitializeAllInterfaces_frrInterfaces() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    FrrInterface iface1 = new FrrInterface("frr", "vrf1");
    FrrConfiguration frrConfiguration = new FrrConfiguration();
    frrConfiguration.getInterfaces().put(iface1.getName(), iface1);
    CumulusConcatenatedConfiguration.builder()
        .setHostname("c")
        .setFrrConfiguration(frrConfiguration)
        .build()
        .initializeAllInterfaces(c);
    assertTrue(c.getAllInterfaces().containsKey(iface1.getName()));
    assertEquals(c.getAllInterfaces().get(iface1.getName()).getVrfName(), iface1.getVrfName());
    assertThat(
        c.getAllInterfaces().get(iface1.getName()).getBandwidth(), equalTo(DEFAULT_PORT_BANDWIDTH));
  }

  /** Missing super interfaces are included */
  @Test
  public void testInitializeAllInterfaces_superInterfaces() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    InterfacesInterface iface1 = new InterfacesInterface("swp1.2");
    CumulusConcatenatedConfiguration.builder()
        .setHostname("c")
        .addInterfaces(ImmutableMap.of(iface1.getName(), iface1))
        .build()
        .initializeAllInterfaces(c);
    assertTrue(c.getAllInterfaces().containsKey("swp1"));
    // this interface is put in the default vrf
    assertEquals(c.getAllInterfaces().get("swp1").getVrfName(), DEFAULT_VRF_NAME);
  }

  @Test
  public void testInitializeAllInterfaces_vxlanInterfaces() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    InterfacesInterface iface1 = new InterfacesInterface("vni4001");
    iface1.setVxlanId(10001);
    CumulusConcatenatedConfiguration.builder()
        .setHostname("c")
        .addInterfaces(ImmutableMap.of(iface1.getName(), iface1))
        .build()
        .initializeAllInterfaces(c);
    assertFalse(c.getAllInterfaces().containsKey("vni4001"));
  }

  @Test
  public void testToInterface_active() {
    InterfacesInterface vsIface = new InterfacesInterface("swp1");
    CumulusConcatenatedConfiguration vsConfig =
        CumulusConcatenatedConfiguration.builder()
            .setHostname("c")
            .addInterfaces(ImmutableMap.of(vsIface.getName(), vsIface))
            .build();
    assertTrue(
        vsConfig
            .toVendorIndependentConfiguration()
            .getAllInterfaces()
            .get(vsIface.getName())
            .getActive());
  }

  @Test
  public void testToInterface_inactive() {
    InterfacesInterface vsIface = new InterfacesInterface("swp1");
    CumulusConcatenatedConfiguration vsConfig =
        CumulusConcatenatedConfiguration.builder()
            .setHostname("c")
            .addInterfaces(ImmutableMap.of(vsIface.getName(), vsIface))
            .setPorts(
                ImmutableMap.of(
                    vsIface.getName(), PortSettings.builder().setDisabled(true).build()))
            .build();
    assertTrue(
        vsConfig
            .toVendorIndependentConfiguration()
            .getAllInterfaces()
            .get(vsIface.getName())
            .getActive());
  }

  @Test
  public void testToInterface_sub_active() {
    InterfacesInterface vsIface = new InterfacesInterface("swp1s1");
    CumulusConcatenatedConfiguration vsConfig =
        CumulusConcatenatedConfiguration.builder()
            .setHostname("c")
            .addInterfaces(ImmutableMap.of(vsIface.getName(), vsIface))
            .build();
    assertThat(
        vsConfig.toVendorIndependentConfiguration(), hasInterface(vsIface.getName(), isActive()));
  }

  @Test
  public void testToInterface_sub_inactive() {
    InterfacesInterface vsIface = new InterfacesInterface("swp1s1");
    CumulusConcatenatedConfiguration vsConfig =
        CumulusConcatenatedConfiguration.builder()
            .setHostname("c")
            .addInterfaces(ImmutableMap.of(vsIface.getName(), vsIface))
            .setPorts(
                ImmutableMap.of(
                    vsIface.getName(), PortSettings.builder().setDisabled(true).build()))
            .build();
    assertTrue(
        vsConfig
            .toVendorIndependentConfiguration()
            .getAllInterfaces()
            .get(vsIface.getName())
            .getActive());
  }

  @Test
  public void testToVIConfigIntfShut() {
    InterfacesInterface vsIface = new InterfacesInterface("swp1");
    FrrConfiguration frrConfiguration = new FrrConfiguration();

    CumulusConcatenatedConfiguration vsConfig =
        CumulusConcatenatedConfiguration.builder()
            .setHostname("c")
            .addInterfaces(ImmutableMap.of(vsIface.getName(), vsIface))
            .setFrrConfiguration(frrConfiguration)
            .build();

    // Setup the FRR Interface
    FrrInterface frrInterface = new FrrInterface("swp1");
    frrInterface.setShutdown(true);
    frrConfiguration.getInterfaces().put("swp1", frrInterface);

    // Convert - method under test
    Configuration c = vsConfig.toVendorIndependentConfiguration();

    assertThat(
        c,
        hasInterface(
            vsIface.getName(), allOf(isActive(false), hasInterfaceType(InterfaceType.PHYSICAL))));

    // Flip the shutdown status around and test again.
    frrInterface.setShutdown(false);
    frrConfiguration.getInterfaces().put("swp1", frrInterface);
    c = vsConfig.toVendorIndependentConfiguration();

    assertThat(
        c,
        hasInterface(
            vsIface.getName(), allOf(isActive(), hasInterfaceType(InterfaceType.PHYSICAL))));
  }

  @Test
  public void testToVIConfigIntfNoShut() {
    InterfacesInterface vsIface = new InterfacesInterface("swp1");
    FrrConfiguration frrConfiguration = new FrrConfiguration();

    CumulusConcatenatedConfiguration vsConfig =
        CumulusConcatenatedConfiguration.builder()
            .setHostname("c")
            .addInterfaces(ImmutableMap.of(vsIface.getName(), vsIface))
            .setFrrConfiguration(frrConfiguration)
            .build();

    // Setup the FRR Interface
    FrrInterface frrInterface = new FrrInterface("swp1");
    frrConfiguration.getInterfaces().put("swp1", frrInterface);

    // Convert - method under test
    Configuration c = vsConfig.toVendorIndependentConfiguration();

    assertTrue(c.getAllInterfaces().get(vsIface.getName()).getActive());
  }

  /** Tests that interfaces are assigned LLAs iff needed */
  @Test
  public void testInterface_assignLla() {
    /*
     - swp1 -- no address in interfaces or frr, not used for unnumbered
     - swp2 -- no address in interfaces or frr, used for unnumbered
     - swp3 -- no address in interfaces, address in frr
     - swp4 -- address in interfaces, no address in frr

     LLA should assigned only to swp2
    */
    CumulusInterfacesConfiguration interfacesConfiguration = new CumulusInterfacesConfiguration();
    interfacesConfiguration.createOrGetInterface("swp1");
    interfacesConfiguration.createOrGetInterface("swp2");
    interfacesConfiguration.createOrGetInterface("swp3");
    interfacesConfiguration
        .createOrGetInterface("swp4")
        .addAddress(ConcreteInterfaceAddress.parse("4.4.4.4/31"));

    FrrConfiguration frrConfiguration = new FrrConfiguration();
    frrConfiguration.getInterfaces().put("swp1", new FrrInterface("swp1"));
    frrConfiguration.getInterfaces().put("swp2", new FrrInterface("swp2"));
    frrConfiguration.getInterfaces().put("swp3", new FrrInterface("swp3"));
    frrConfiguration.getInterfaces().put("swp4", new FrrInterface("swp4"));
    frrConfiguration
        .getInterfaces()
        .get("swp3")
        .getIpAddresses()
        .add(ConcreteInterfaceAddress.parse("3.3.3.3/31"));

    BgpProcess bgpProc = new BgpProcess();
    BgpVrf bgpVrf = new BgpVrf(DEFAULT_VRF_NAME);
    bgpVrf.setAutonomousSystem(65000L);
    BgpNeighbor neighbpr = new BgpInterfaceNeighbor("swp2");
    neighbpr.setRemoteAs(RemoteAs.external());
    frrConfiguration.setBgpProcess(bgpProc);
    bgpProc.getVrfs().put(DEFAULT_VRF_NAME, bgpVrf);
    bgpVrf.getNeighbors().put(neighbpr.getName(), neighbpr);

    Configuration c =
        CumulusConcatenatedConfiguration.builder()
            .setHostname("test")
            .setInterfacesConfiguration(interfacesConfiguration)
            .setFrrConfiguration(frrConfiguration)
            .build()
            .toVendorIndependentConfiguration();

    assertEquals(c.getAllInterfaces().get("swp1").getAllAddresses(), ImmutableSet.of());
    assertEquals(
        c.getAllInterfaces().get("swp2").getAllAddresses(), ImmutableSet.of(LINK_LOCAL_ADDRESS));
    assertEquals(
        c.getAllInterfaces().get("swp3").getAllAddresses(),
        ImmutableSet.of(ConcreteInterfaceAddress.parse("3.3.3.3/31")));
    assertEquals(
        c.getAllInterfaces().get("swp4").getAllAddresses(),
        ImmutableSet.of(ConcreteInterfaceAddress.parse("4.4.4.4/31")));
  }

  @Test
  public void testPopulateCommonProperties_mtu() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    CumulusConcatenatedConfiguration vc = new CumulusConcatenatedConfiguration();
    InterfacesInterface vsIface = new InterfacesInterface("iface");
    Interface viIface =
        org.batfish.datamodel.TestInterface.builder()
            .setName("iface")
            .setOwner(c)
            .setType(InterfaceType.UNKNOWN)
            .build();

    // unset means default
    vc.populateCommonInterfaceProperties(vsIface, viIface);
    assertEquals(viIface.getMtu(), DEFAULT_MTU);

    // should get the set value
    vsIface.setMtu(42);
    vc.populateCommonInterfaceProperties(vsIface, viIface);
    assertEquals(viIface.getMtu(), 42);
  }

  @Test
  public void testPopulateLoopbackProperties_clagVxlanAnycastIp() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    org.batfish.datamodel.Interface viLoopback =
        org.batfish.datamodel.TestInterface.builder()
            .setName(LOOPBACK_INTERFACE_NAME)
            .setOwner(c)
            .setType(InterfaceType.UNKNOWN)
            .build();

    Ip clagIp = Ip.parse("1.1.1.1");
    InterfacesInterface vsLoopback = new InterfacesInterface("lo");
    vsLoopback.setClagVxlanAnycastIp(clagIp);

    populateLoopbackProperties(vsLoopback, viLoopback);

    assertNull(viLoopback.getAddress()); // clag ip is not made primary
    assertThat(
        viLoopback.getAllAddresses(),
        equalTo(
            ImmutableSet.of(ConcreteInterfaceAddress.create(clagIp, Prefix.MAX_PREFIX_LENGTH))));
    assertThat(viLoopback.getInterfaceType(), is(InterfaceType.LOOPBACK));
  }

  @Test
  public void testInitPostUpRoutes() {
    StaticRoute route0 = new StaticRoute(Prefix.parse("1.1.1.0/24"), null, "eth0", null);
    StaticRoute route1 = new StaticRoute(Prefix.parse("2.1.1.0/24"), null, "eth0", null);

    // enabled interface in the target vrf
    InterfacesInterface iface1 = new InterfacesInterface("eth0");
    iface1.addPostUpIpRoute(route0);
    iface1.setVrf("vrf0");

    // enabled interface in a different vrf
    InterfacesInterface iface2 = new InterfacesInterface("eth1");
    iface2.addPostUpIpRoute(route1);
    iface2.setVrf("vrf1");

    // disabled interface in the target vrf (disabling is happening below)
    InterfacesInterface iface3 = new InterfacesInterface("eth2");
    iface3.addPostUpIpRoute(route1);
    iface3.setVrf("vrf0");

    CumulusConcatenatedConfiguration vsConfig =
        CumulusConcatenatedConfiguration.builder()
            .setHostname("c")
            .addInterfaces(
                ImmutableMap.of(
                    iface1.getName(), iface1, iface2.getName(), iface2, iface3.getName(), iface3))
            .build();

    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    c.getVrfs().put("vrf0", new Vrf("vrf0"));
    c.getVrfs().put("vrf1", new Vrf("vrf1"));
    ImmutableSet.of(iface1, iface2, iface3)
        .forEach(
            iface ->
                c.getAllInterfaces()
                    .put(
                        iface.getName(),
                        org.batfish.datamodel.TestInterface.builder()
                            .setName(iface.getName())
                            .setOwner(c)
                            .setType(InterfaceType.PHYSICAL)
                            .build()));
    c.getAllInterfaces().get(iface3.getName()).adminDown();

    vsConfig.initPostUpRoutes(c);

    // should only have routes from iface0
    assertThat(
        c.getVrfs().get("vrf0").getStaticRoutes(),
        equalTo(ImmutableSortedSet.of(route0.convert())));
  }

  @Test
  public void testLowerCaseHostname() {
    CumulusConcatenatedConfiguration vsConfig =
        CumulusConcatenatedConfiguration.builder().setHostname("Node").build();
    assertThat(vsConfig.getHostname(), equalTo("node"));
  }

  @Test
  public void testIsValidVIInterface() {
    assertFalse(isValidVIInterface(new InterfacesInterface(BRIDGE_NAME)));
    InterfacesInterface vxlan = new InterfacesInterface("vni4001");
    vxlan.setVxlanId(10001);
    assertFalse(isValidVIInterface(vxlan));
    assertTrue(isValidVIInterface(new InterfacesInterface("iface")));
  }
}
