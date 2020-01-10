package org.batfish.representation.cumulus;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Interface.DEFAULT_MTU;
import static org.batfish.representation.cumulus.CumulusConversions.DEFAULT_LOOPBACK_BANDWIDTH;
import static org.batfish.representation.cumulus.CumulusNodeConfiguration.LOOPBACK_INTERFACE_NAME;
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
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.representation.cumulus.CumulusPortsConfiguration.PortSettings;
import org.junit.Test;

/** Test for {@link CumulusNcluConfiguration}. */
public class CumulusConcatenatedConfigurationTest {

  /** Test that loopback interface is unconditionally created */
  @Test
  public void testInitializeAllInterfaces_createLoopback() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    CumulusConcatenatedConfiguration.builder().build().initializeAllInterfaces(c);
    assertTrue(c.getAllInterfaces().containsKey(LOOPBACK_INTERFACE_NAME));
  }

  /** Test that bridge is not included as an interface */
  @Test
  public void testInitializeAllInterfaces_noBridge() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    InterfacesInterface bridge = new InterfacesInterface(BRIDGE_NAME);
    CumulusConcatenatedConfiguration.builder()
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
        .addInterfaces(ImmutableMap.of(iface1.getName(), iface1))
        .build()
        .initializeAllInterfaces(c);
    assertTrue(c.getAllInterfaces().containsKey(iface1.getName()));
    assertEquals(c.getAllInterfaces().get(iface1.getName()).getVrfName(), iface1.getVrf());
  }

  /** Interfaces in frrConfiguration are included */
  @Test
  public void testInitializeAllInterfaces_frrInterfaces() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    FrrInterface iface1 = new FrrInterface("frr", "vrf1");
    CumulusFrrConfiguration frrConfiguration = new CumulusFrrConfiguration();
    frrConfiguration.getInterfaces().put(iface1.getName(), iface1);
    CumulusConcatenatedConfiguration.builder()
        .setFrrConfiguration(frrConfiguration)
        .build()
        .initializeAllInterfaces(c);
    assertTrue(c.getAllInterfaces().containsKey(iface1.getName()));
    assertEquals(c.getAllInterfaces().get(iface1.getName()).getVrfName(), iface1.getVrfName());
  }

  /** Missing super interfaces are included */
  @Test
  public void testInitializeAllInterfaces_superInterfaces() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    InterfacesInterface iface1 = new InterfacesInterface("swp1.2");
    CumulusConcatenatedConfiguration.builder()
        .addInterfaces(ImmutableMap.of(iface1.getName(), iface1))
        .build()
        .initializeAllInterfaces(c);
    assertTrue(c.getAllInterfaces().containsKey("swp1"));
    // this interface is put in the default vrf
    assertEquals(c.getAllInterfaces().get("swp1").getVrfName(), DEFAULT_VRF_NAME);
  }

  @Test
  public void testToInterface_active() {
    InterfacesInterface vsIface = new InterfacesInterface("swp1");
    CumulusConcatenatedConfiguration vsConfig =
        CumulusConcatenatedConfiguration.builder()
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
  public void testPopulateCommonProperties_mtu() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    InterfacesInterface vsIface = new InterfacesInterface("iface");
    Interface viIface =
        org.batfish.datamodel.Interface.builder().setName("iface").setOwner(c).build();

    CumulusConcatenatedConfiguration vsConfig = new CumulusConcatenatedConfiguration();

    // unset means default
    vsConfig.populateCommonInterfaceProperties(vsIface, viIface);
    assertEquals(viIface.getMtu(), DEFAULT_MTU);

    // should get the set value
    vsIface.setMtu(42);
    vsConfig.populateCommonInterfaceProperties(vsIface, viIface);
    assertEquals(viIface.getMtu(), 42);
  }

  @Test
  public void testPopulateLoopbackProperties_baseCase() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    org.batfish.datamodel.Interface loopback =
        org.batfish.datamodel.Interface.builder().setName("lo").setOwner(c).build();
    CumulusConcatenatedConfiguration vsConfig = CumulusConcatenatedConfiguration.builder().build();
    vsConfig.populateLoopbackProperties(loopback);

    assertNull(loopback.getDescription());
    assertThat(loopback.getAllAddresses(), equalTo(ImmutableSet.of()));
    assertThat(loopback.getBandwidth(), equalTo(DEFAULT_LOOPBACK_BANDWIDTH));
  }

  @Test
  public void testPopulateLoopbackProperties_loopbackAddressOnly() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);

    // no address configured as a regular interface
    org.batfish.datamodel.Interface loopback =
        org.batfish.datamodel.Interface.builder().setName("lo").setOwner(c).build();

    // address configured as loopback address
    ConcreteInterfaceAddress loopbackAddress = ConcreteInterfaceAddress.parse("2.1.1.1/32");

    CumulusInterfacesConfiguration interfaces = new CumulusInterfacesConfiguration();
    interfaces.getLoopback().getAddresses().add(loopbackAddress);

    CumulusConcatenatedConfiguration.builder()
        .setInterfacesConfiguration(interfaces)
        .build()
        .populateLoopbackProperties(loopback);

    // loopback address is made primary
    assertThat(loopback.getAddress(), equalTo(loopbackAddress));
    assertThat(loopback.getAllAddresses(), equalTo(ImmutableSet.of(loopbackAddress)));
  }

  @Test
  public void testPopulateLoopbackProperties_bothAddresses() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);

    // address configured as a regular interface configuration
    ConcreteInterfaceAddress interfacesAddress = ConcreteInterfaceAddress.parse("1.1.1.1/32");

    org.batfish.datamodel.Interface loopback =
        org.batfish.datamodel.Interface.builder()
            .setName("lo")
            .setOwner(c)
            .setAddress(interfacesAddress)
            .build();

    // address configured as loopback address
    ConcreteInterfaceAddress loopbackAddress = ConcreteInterfaceAddress.parse("2.1.1.1/32");

    CumulusInterfacesConfiguration interfaces = new CumulusInterfacesConfiguration();
    interfaces.getLoopback().getAddresses().add(loopbackAddress);

    CumulusConcatenatedConfiguration.builder()
        .setInterfacesConfiguration(interfaces)
        .build()
        .populateLoopbackProperties(loopback);

    assertThat(loopback.getAddress(), equalTo(interfacesAddress));
    assertThat(
        loopback.getAllAddresses(), equalTo(ImmutableSet.of(interfacesAddress, loopbackAddress)));
  }

  @Test
  public void testPopulateLoopbackProperties_alias() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    org.batfish.datamodel.Interface loopback =
        org.batfish.datamodel.Interface.builder().setName("lo").setOwner(c).build();

    CumulusInterfacesConfiguration ifaceConfig = new CumulusInterfacesConfiguration();
    ifaceConfig.getLoopback().setAlias("lalala");

    CumulusConcatenatedConfiguration.builder()
        .setInterfacesConfiguration(ifaceConfig)
        .build()
        .populateLoopbackProperties(loopback);

    assertEquals(loopback.getDescription(), "lalala");
  }

  @Test
  public void testPopulateLoopbackProperties_clagVxlanAnycastIp() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    org.batfish.datamodel.Interface loopback =
        org.batfish.datamodel.Interface.builder().setName("lo").setOwner(c).build();

    Ip clagIp = Ip.parse("1.1.1.1");
    CumulusInterfacesConfiguration interfaces = new CumulusInterfacesConfiguration();
    interfaces.getLoopback().setClagVxlanAnycastIp(clagIp);

    CumulusConcatenatedConfiguration vsConfig =
        CumulusConcatenatedConfiguration.builder().setInterfacesConfiguration(interfaces).build();

    vsConfig.populateLoopbackProperties(loopback);

    assertNull(loopback.getAddress()); // clag ip is not made primary
    assertThat(
        loopback.getAllAddresses(),
        equalTo(
            ImmutableSet.of(ConcreteInterfaceAddress.create(clagIp, Prefix.MAX_PREFIX_LENGTH))));
  }

  @Test
  public void testPopulateLoopbackProperties_bandwidth() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    org.batfish.datamodel.Interface loopback =
        org.batfish.datamodel.Interface.builder().setName("lo").setOwner(c).build();

    CumulusInterfacesConfiguration interfaces = new CumulusInterfacesConfiguration();
    interfaces.getLoopback().setBandwidth(42.0);

    CumulusConcatenatedConfiguration vsConfig =
        CumulusConcatenatedConfiguration.builder().setInterfacesConfiguration(interfaces).build();

    vsConfig.populateLoopbackProperties(loopback);

    assertThat(loopback.getBandwidth(), equalTo(42.0));
  }

  @Test
  public void testInitPostUpRoutes() {
    StaticRoute route0 = new StaticRoute(Prefix.parse("1.1.1.0/24"), null, "eth0");
    StaticRoute route1 = new StaticRoute(Prefix.parse("2.1.1.0/24"), null, "eth0");

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
}
