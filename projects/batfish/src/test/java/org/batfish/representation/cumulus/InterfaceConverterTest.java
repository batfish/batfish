package org.batfish.representation.cumulus;

import static org.batfish.representation.cumulus.InterfaceConverter.BRIDGE_NAME;
import static org.batfish.representation.cumulus.InterfaceConverter.convertVxlan;
import static org.batfish.representation.cumulus.InterfaceConverter.getEncapsulationVlan;
import static org.batfish.representation.cumulus.InterfaceConverter.getSuperInterfaceName;
import static org.batfish.representation.cumulus.InterfaceConverter.isInterface;
import static org.batfish.representation.cumulus.InterfaceConverter.isVlan;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

/** Test for Cumulus {@link CumulusInterfacesConfiguration} {@link InterfaceConverter}. */
public class InterfaceConverterTest {
  private static final ConcreteInterfaceAddress ADDR1 =
      ConcreteInterfaceAddress.parse("1.2.3.4/24");
  private static final ConcreteInterfaceAddress ADDR2 =
      ConcreteInterfaceAddress.parse("2.3.4.5/25");
  private static final MacAddress MAC = MacAddress.parse("00:00:00:00:00:01");

  private static final InterfacesInterface BOND_IFACE = new InterfacesInterface("bond");
  private static final InterfacesInterface BRIDGE_IFACE = new InterfacesInterface("bridge");
  private static final InterfacesInterface PHYSICAL_IFACE = new InterfacesInterface("swp1");
  private static final InterfacesInterface PHYSICAL_SUBIFACE = new InterfacesInterface("swp1.1");
  private static final InterfacesInterface VLAN_IFACE = new InterfacesInterface("vlan123");
  private static final InterfacesInterface VRF_IFACE = new InterfacesInterface("vrf1");
  private static final InterfacesInterface VXLAN_IFACE = new InterfacesInterface("vni1");

  static {
    BOND_IFACE.setBondSlaves(ImmutableSet.of("swp1"));

    BRIDGE_IFACE.setBridgePorts(ImmutableSet.of("i1", "i2"));
    InterfaceBridgeSettings bridgeSettings = BRIDGE_IFACE.createOrGetBridgeSettings();
    bridgeSettings.setPvid(5);
    bridgeSettings.setVids(IntegerSpace.of(123));

    PHYSICAL_IFACE.addPostUpIpRoute(new StaticRoute(Prefix.ZERO, null, "eth0", null));

    PHYSICAL_SUBIFACE.addAddress(ADDR1);
    PHYSICAL_SUBIFACE.createOrGetBridgeSettings(); // create bridge settings object
    PHYSICAL_SUBIFACE.createOrGetClagSettings(); // create clag settings object
    PHYSICAL_SUBIFACE.setDescription("description");
    PHYSICAL_SUBIFACE.setLinkSpeed(1234);
    PHYSICAL_SUBIFACE.setVrf("vrf1");

    VLAN_IFACE.setDescription("description");
    VLAN_IFACE.addAddress(ADDR1);
    VLAN_IFACE.setAddressVirtual(MAC, ADDR2);
    VLAN_IFACE.setVlanId(123);
    VLAN_IFACE.setVrf("vrfName");

    VRF_IFACE.setVrfTable("auto");
    VRF_IFACE.addAddress(ADDR1);

    VXLAN_IFACE.setVxlanId(1);
    VXLAN_IFACE.setVxlanLocalTunnelIp(Ip.parse("1.2.3.4"));
    VXLAN_IFACE.createOrGetBridgeSettings().setAccess(2);
  }

  @Test
  public void testIsVlan() {
    assertTrue(isVlan(new InterfacesInterface("vlan123")));
    assertFalse(isVlan(new InterfacesInterface("vni123")));
    assertFalse(isVlan(new InterfacesInterface(BRIDGE_NAME)));
  }

  @Test
  public void testIsInterface() {
    assertTrue(isInterface(new InterfacesInterface("swp1")));
    assertFalse(isInterface(new InterfacesInterface("vlan123")));

    // vrf
    {
      InterfacesInterface swp1 = new InterfacesInterface("swp1");
      swp1.setVrfTable("auto");
      assertFalse(isInterface(swp1));
    }

    // vxlan
    {
      InterfacesInterface vxlan1 = new InterfacesInterface("vni1");
      vxlan1.setVxlanId(1);
      assertFalse(isInterface(vxlan1));
    }
  }

  @Test
  public void testGetEncapsulationVlan() {
    assertThat(getEncapsulationVlan(new InterfacesInterface("swp1")), nullValue());
    assertThat(getEncapsulationVlan(new InterfacesInterface("swp1.1")), equalTo(1));
    assertThat(getEncapsulationVlan(new InterfacesInterface("peerlink.4094")), equalTo(4094));
  }

  @Test
  public void testGetSuperInterfaceName_physicalSubInterface() {
    assertThat(getSuperInterfaceName("swp1.0"), equalTo("swp1"));
    assertThat(getSuperInterfaceName("swp1s5.0"), equalTo("swp1s5"));
    assertThat(getSuperInterfaceName("eth0.0"), equalTo("eth0"));
  }

  @Test
  public void testGetSuperInterface_none() {
    // could be a bond slave, but isn't in bondSlaveParents map
    assertThat(getSuperInterfaceName("swp1"), nullValue());
  }

  @Test
  public void testConvertVxlan() {
    Vxlan vxlan = convertVxlan(VXLAN_IFACE);
    assertThat(vxlan.getName(), equalTo(VXLAN_IFACE.getName()));
    assertThat(vxlan.getId(), equalTo(VXLAN_IFACE.getVxlanId()));
    assertThat(vxlan.getBridgeAccessVlan(), equalTo(VXLAN_IFACE.getBridgeSettings().getAccess()));
    assertThat(vxlan.getLocalTunnelip(), equalTo(VXLAN_IFACE.getVxlanLocalTunnelIp()));
  }
}
