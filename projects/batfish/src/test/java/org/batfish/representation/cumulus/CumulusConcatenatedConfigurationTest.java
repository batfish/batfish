package org.batfish.representation.cumulus;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Interface.DEFAULT_MTU;
import static org.batfish.representation.cumulus.CumulusConcatenatedConfiguration.LINK_LOCAL_ADDRESS;
import static org.batfish.representation.cumulus.CumulusConcatenatedConfiguration.LOOPBACK_INTERFACE_NAME;
import static org.batfish.representation.cumulus.CumulusConcatenatedConfiguration.isValidVIInterface;
import static org.batfish.representation.cumulus.CumulusConcatenatedConfiguration.populateCommonInterfaceProperties;
import static org.batfish.representation.cumulus.CumulusConcatenatedConfiguration.populateLoopbackProperties;
import static org.batfish.representation.cumulus.CumulusConversions.DEFAULT_LOOPBACK_BANDWIDTH;
import static org.batfish.representation.cumulus.CumulusConversions.DEFAULT_PORT_BANDWIDTH;
import static org.batfish.representation.cumulus.InterfaceConverter.BRIDGE_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.common.runtime.InterfaceRuntimeData;
import org.batfish.common.runtime.RuntimeData;
import org.batfish.common.runtime.SnapshotRuntimeData;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.representation.cumulus.BgpNeighbor.RemoteAs;
import org.batfish.representation.cumulus.CumulusPortsConfiguration.PortSettings;
import org.junit.Test;

/** Test for {@link CumulusConcatenatedConfiguration}. */
public class CumulusConcatenatedConfigurationTest {

  /** Test that loopback interface is unconditionally created */
  @Test
  public void testInitializeAllInterfaces_createLoopback() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    CumulusConcatenatedConfiguration.builder().setHostname("c").build().initializeAllInterfaces(c);
    assertTrue(c.getAllInterfaces().containsKey(LOOPBACK_INTERFACE_NAME));
    assertThat(
        c.getAllInterfaces().get(LOOPBACK_INTERFACE_NAME).getBandwidth(),
        equalTo(DEFAULT_LOOPBACK_BANDWIDTH));
  }

  /** Test bandwidths incorporate interface runtime data. */
  @Test
  public void testInterfaceRuntimeData() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    InterfaceRuntimeData ifaceData = InterfaceRuntimeData.builder().setBandwidth(123456.0).build();
    RuntimeData hostData =
        RuntimeData.builder()
            .setInterfaces(ImmutableMap.of(LOOPBACK_INTERFACE_NAME, ifaceData))
            .build();
    SnapshotRuntimeData data =
        SnapshotRuntimeData.builder()
            .setRuntimeData(ImmutableMap.of(c.getHostname(), hostData))
            .build();
    CumulusConcatenatedConfiguration.builder()
        .setHostname("c")
        .setSnapshotRuntimeData(data)
        .build()
        .initializeAllInterfaces(c);
    assertTrue(c.getAllInterfaces().containsKey(LOOPBACK_INTERFACE_NAME));
    assertThat(c.getAllInterfaces().get(LOOPBACK_INTERFACE_NAME).getBandwidth(), equalTo(123456.0));
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
    CumulusFrrConfiguration frrConfiguration = new CumulusFrrConfiguration();
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
    assertTrue(
        vsConfig
            .toVendorIndependentConfiguration()
            .getAllInterfaces()
            .get(vsIface.getName())
            .getActive());
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
    CumulusFrrConfiguration frrConfiguration = new CumulusFrrConfiguration();

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

    assertFalse(c.getAllInterfaces().get(vsIface.getName()).getActive());

    // Flip the shutdown status around and test again.
    frrInterface.setShutdown(false);
    frrConfiguration.getInterfaces().put("swp1", frrInterface);
    c = vsConfig.toVendorIndependentConfiguration();

    assertTrue(c.getAllInterfaces().get(vsIface.getName()).getActive());
  }

  @Test
  public void testToVIConfigIntfNoShut() {
    InterfacesInterface vsIface = new InterfacesInterface("swp1");
    CumulusFrrConfiguration frrConfiguration = new CumulusFrrConfiguration();

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

    CumulusFrrConfiguration frrConfiguration = new CumulusFrrConfiguration();
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
    InterfacesInterface vsIface = new InterfacesInterface("iface");
    Interface viIface =
        org.batfish.datamodel.Interface.builder().setName("iface").setOwner(c).build();

    // unset means default
    populateCommonInterfaceProperties(vsIface, viIface);
    assertEquals(viIface.getMtu(), DEFAULT_MTU);

    // should get the set value
    vsIface.setMtu(42);
    populateCommonInterfaceProperties(vsIface, viIface);
    assertEquals(viIface.getMtu(), 42);
  }

  @Test
  public void testPopulateLoopbackProperties_clagVxlanAnycastIp() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    org.batfish.datamodel.Interface viLoopback =
        org.batfish.datamodel.Interface.builder()
            .setName(LOOPBACK_INTERFACE_NAME)
            .setOwner(c)
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
                        org.batfish.datamodel.Interface.builder()
                            .setName(iface.getName())
                            .setOwner(c)
                            .build()));
    c.getAllInterfaces().get(iface3.getName()).setActive(false);

    vsConfig.initVrfStaticRoutes(c);

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
