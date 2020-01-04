package org.batfish.representation.cumulus;

import static org.batfish.representation.cumulus.CumulusConversions.DEFAULT_LOOPBACK_BANDWIDTH;
import static org.batfish.representation.cumulus.CumulusNodeConfiguration.LOOPBACK_INTERFACE_NAME;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.representation.cumulus.CumulusPortsConfiguration.PortSettings;
import org.junit.Test;

/** Test for {@link CumulusNcluConfiguration}. */
public class CumulusConcatenatedConfigurationTest {

  /** Test that loopback interface is unconditionally created */
  @Test
  public void testCreateLoopback() {
    CumulusConcatenatedConfiguration vsConfig = CumulusConcatenatedConfiguration.builder().build();
    Configuration viConfig = vsConfig.toVendorIndependentConfiguration();
    assertTrue(viConfig.getAllInterfaces().containsKey(LOOPBACK_INTERFACE_NAME));
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
  public void testPopulateLoopbackProperties_baseCase() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    org.batfish.datamodel.Interface loopback =
        org.batfish.datamodel.Interface.builder().setName("lo").setOwner(c).build();
    CumulusConcatenatedConfiguration vsConfig = CumulusConcatenatedConfiguration.builder().build();
    vsConfig.populateLoopbackProperties(loopback);

    assertThat(loopback.getAllAddresses(), equalTo(ImmutableSet.of()));
    assertThat(loopback.getBandwidth(), equalTo(DEFAULT_LOOPBACK_BANDWIDTH));
  }

  @Test
  public void testPopulateLoopbackProperties_primaryAddress() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    org.batfish.datamodel.Interface loopback =
        org.batfish.datamodel.Interface.builder().setName("lo").setOwner(c).build();

    ConcreteInterfaceAddress primary = ConcreteInterfaceAddress.parse("1.1.1.1/32");
    CumulusInterfacesConfiguration interfaces = new CumulusInterfacesConfiguration();
    interfaces.getLoopback().getAddresses().add(primary);

    CumulusConcatenatedConfiguration vsConfig =
        CumulusConcatenatedConfiguration.builder().setInterfacesConfiguration(interfaces).build();

    vsConfig.populateLoopbackProperties(loopback);

    assertThat(loopback.getAddress(), equalTo(primary));
    assertThat(loopback.getAllAddresses(), equalTo(ImmutableSet.of(primary)));
  }

  @Test
  public void testPopulateLoopbackProperties_moreAddresses() {
    Configuration c = new Configuration("c", ConfigurationFormat.CUMULUS_CONCATENATED);
    org.batfish.datamodel.Interface loopback =
        org.batfish.datamodel.Interface.builder().setName("lo").setOwner(c).build();

    ConcreteInterfaceAddress primary = ConcreteInterfaceAddress.parse("1.1.1.1/32");
    CumulusInterfacesConfiguration interfaces = new CumulusInterfacesConfiguration();
    interfaces.getLoopback().getAddresses().add(primary);

    ConcreteInterfaceAddress secondary = ConcreteInterfaceAddress.parse("2.2.2.2/32");
    InterfacesInterface vsIface = new InterfacesInterface(LOOPBACK_INTERFACE_NAME);
    vsIface.addAddress(secondary);
    interfaces.getInterfaces().put(LOOPBACK_INTERFACE_NAME, vsIface);

    CumulusConcatenatedConfiguration vsConfig =
        CumulusConcatenatedConfiguration.builder().setInterfacesConfiguration(interfaces).build();

    vsConfig.populateLoopbackProperties(loopback);

    assertThat(loopback.getAddress(), equalTo(primary));
    assertThat(loopback.getAllAddresses(), equalTo(ImmutableSet.of(primary, secondary)));
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

    assertNull(loopback.getAddress());
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
}
