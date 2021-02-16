package org.batfish.representation.cumulus_nclu;

import static org.batfish.representation.cumulus_nclu.CumulusConversions.DEFAULT_LOOPBACK_BANDWIDTH;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.junit.Test;

/** Test for {@link CumulusNcluConfiguration}. */
public class CumulusNcluConfigurationTest {

  @Test
  public void testToInterface_active() {
    Interface vsIface = new Interface("swp1", CumulusInterfaceType.PHYSICAL, null, 1);
    org.batfish.datamodel.Interface viIface = new CumulusNcluConfiguration().toInterface(vsIface);
    assertTrue(viIface.getActive());
  }

  @Test
  public void testToInterface_inactive() {
    Interface vsIface = new Interface("swp1", CumulusInterfaceType.PHYSICAL, null, 1);
    vsIface.setDisabled(true);
    org.batfish.datamodel.Interface viIface = new CumulusNcluConfiguration().toInterface(vsIface);
    assertFalse(viIface.getActive());
  }

  @Test
  public void testToInterface_sub_active() {
    Interface vsIface = new Interface("swp1s1", CumulusInterfaceType.PHYSICAL, null, 1);
    org.batfish.datamodel.Interface viIface =
        new CumulusNcluConfiguration().toInterface(vsIface, "swp1");
    assertTrue(viIface.getActive());
  }

  @Test
  public void testToInterface_sub_inactive() {
    Interface vsIface = new Interface("swp1s1", CumulusInterfaceType.PHYSICAL, null, 1);
    vsIface.setDisabled(true);
    org.batfish.datamodel.Interface viIface =
        new CumulusNcluConfiguration().toInterface(vsIface, "swp1");
    assertFalse(viIface.getActive());
  }

  @Test
  public void testPopulateLoopback() {
    Interface iface = new Interface("lo", CumulusInterfaceType.LOOPBACK, null, null);
    iface.getIpAddresses().add(ConcreteInterfaceAddress.parse("1.1.1.1/30"));
    Loopback loopback = new Loopback();

    CumulusNcluConfiguration.populateLoInInterfacesToLoopback(iface, loopback);
    assertThat(
        loopback.getAddresses(),
        equalTo(ImmutableList.of(ConcreteInterfaceAddress.parse("1.1.1.1/30"))));
  }

  @Test
  public void testCreateVIInterfaceForLo_Bandwidth() {
    CumulusNcluConfiguration vendorConfiguration = new CumulusNcluConfiguration();
    org.batfish.datamodel.Interface iface = vendorConfiguration.createVIInterfaceForLo();
    assertThat(iface.getBandwidth(), equalTo(DEFAULT_LOOPBACK_BANDWIDTH));
  }

  @Test
  public void testInitVrfStaticRoutes_postUpRoutes() {
    CumulusNcluConfiguration vendorConfiguration = new CumulusNcluConfiguration();

    StaticRoute route0 = new StaticRoute(Prefix.parse("1.1.1.0/24"), null, "eth0", null);
    StaticRoute route1 = new StaticRoute(Prefix.parse("2.1.1.0/24"), null, "eth0", null);

    // enabled interface in the target vrf
    Interface iface1 = new Interface("eth0", CumulusInterfaceType.PHYSICAL, null, null);
    iface1.addPostUpIpRoute(route0);
    iface1.setVrf("vrf0");

    // enabled interface in a different vrf
    Interface iface2 = new Interface("eth1", CumulusInterfaceType.PHYSICAL, null, null);
    iface2.addPostUpIpRoute(route1);
    iface2.setVrf("vrf1");

    // disabled interface in the target vrf
    Interface iface3 = new Interface("eth2", CumulusInterfaceType.PHYSICAL, null, null);
    iface3.addPostUpIpRoute(route1);
    iface3.setVrf("vrf0");
    iface3.setDisabled(true);

    org.batfish.representation.cumulus_nclu.Vrf oldVrf0 =
        new org.batfish.representation.cumulus_nclu.Vrf("vrf0");
    Vrf newVrf0 = new Vrf("vrf0");

    vendorConfiguration.setInterfaces(
        ImmutableMap.of(
            iface1.getName(), iface1, iface2.getName(), iface2, iface3.getName(), iface3));

    vendorConfiguration.initVrfStaticRoutes(oldVrf0, newVrf0);

    // should only have routes from iface0
    assertThat(newVrf0.getStaticRoutes(), equalTo(ImmutableSortedSet.of(route0.convert())));
  }
}
