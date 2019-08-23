package org.batfish.representation.cumulus_interfaces;

import static org.batfish.representation.cumulus.CumulusInterfaceType.BOND_SUBINTERFACE;
import static org.batfish.representation.cumulus.CumulusInterfaceType.PHYSICAL;
import static org.batfish.representation.cumulus.CumulusInterfaceType.PHYSICAL_SUBINTERFACE;
import static org.batfish.representation.cumulus_interfaces.Converter.BRIDGE_NAME;
import static org.batfish.representation.cumulus_interfaces.Converter.convertBond;
import static org.batfish.representation.cumulus_interfaces.Converter.convertVlan;
import static org.batfish.representation.cumulus_interfaces.Converter.convertVrf;
import static org.batfish.representation.cumulus_interfaces.Converter.convertVxlan;
import static org.batfish.representation.cumulus_interfaces.Converter.getEncapsulationVlan;
import static org.batfish.representation.cumulus_interfaces.Converter.getSuperInterfaceName;
import static org.batfish.representation.cumulus_interfaces.Converter.isInterface;
import static org.batfish.representation.cumulus_interfaces.Converter.isVlan;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;
import org.batfish.representation.cumulus.Bond;
import org.batfish.representation.cumulus.Bridge;
import org.batfish.representation.cumulus.InterfaceBridgeSettings;
import org.batfish.representation.cumulus.Vlan;
import org.batfish.representation.cumulus.Vrf;
import org.batfish.representation.cumulus.Vxlan;
import org.junit.Before;
import org.junit.Test;

/** Test for Cumulus {@link Interfaces} {@link Converter}. */
public class ConverterTest {
  private static final ConcreteInterfaceAddress ADDR1 =
      ConcreteInterfaceAddress.parse("1.2.3.4/24");
  private static final ConcreteInterfaceAddress ADDR2 =
      ConcreteInterfaceAddress.parse("2.3.4.5/25");
  private static final MacAddress MAC = MacAddress.parse("00:00:00:00:00:01");

  private static final Interface BOND_IFACE = new Interface("bond");
  private static final Interface BRIDGE_IFACE = new Interface("bridge");
  private static final Interface PHYSICAL_IFACE = new Interface("swp1");
  private static final Interface PHYSICAL_SUBIFACE = new Interface("swp1.1");
  private static final Interface VLAN_IFACE = new Interface("vlan123");
  private static final Interface VRF_IFACE = new Interface("vrf1");
  private static final Interface VXLAN_IFACE = new Interface("vni1");

  private static final Map<String, Interface> INTERFACE_MAP =
      ImmutableMap.<String, Interface>builder()
          .put(BOND_IFACE.getName(), BOND_IFACE)
          .put(PHYSICAL_IFACE.getName(), PHYSICAL_IFACE)
          .put(PHYSICAL_SUBIFACE.getName(), PHYSICAL_SUBIFACE)
          .put(VLAN_IFACE.getName(), VLAN_IFACE)
          .put(VRF_IFACE.getName(), VRF_IFACE)
          .put(VXLAN_IFACE.getName(), VXLAN_IFACE)
          .build();

  static {
    BOND_IFACE.setBondSlaves(ImmutableSet.of("swp1"));

    BRIDGE_IFACE.setBridgePorts(ImmutableSet.of("i1", "i2"));
    InterfaceBridgeSettings bridgeSettings = BRIDGE_IFACE.createOrGetBridgeSettings();
    bridgeSettings.setPvid(5);
    bridgeSettings.setVids(IntegerSpace.of(123));

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

  private Warnings _w;

  @Before
  public void setup() {
    _w = new Warnings();
  }

  @Test
  public void testIsVlan() {
    assertTrue(isVlan(new Interface("vlan123")));
    assertFalse(isVlan(new Interface("vni123")));
    assertFalse(isVlan(new Interface(BRIDGE_NAME)));
  }

  @Test
  public void testIsInterface() {
    assertTrue(isInterface(new Interface("swp1")));
    assertFalse(isInterface(new Interface("vlan123")));

    // vrf
    {
      Interface swp1 = new Interface("swp1");
      swp1.setVrfTable("auto");
      assertFalse(isInterface(swp1));
    }

    // vxlan
    {
      Interface vxlan1 = new Interface("vni1");
      vxlan1.setVxlanId(1);
      assertFalse(isInterface(vxlan1));
    }
  }

  @Test
  public void testGetEncapsulationVlan() {
    assertThat(getEncapsulationVlan(new Interface("swp1")), nullValue());
    assertThat(getEncapsulationVlan(new Interface("swp1.1")), equalTo(1));
    assertThat(getEncapsulationVlan(new Interface("peerlink.4094")), equalTo(4094));
  }

  @Test
  public void testGetInterfaceType() {
    Interfaces interfaces = new Interfaces();
    interfaces.getInterfaces().putAll(INTERFACE_MAP);
    Converter converter = new Converter(interfaces, _w);

    assertThat(converter.getInterfaceType(PHYSICAL_IFACE), equalTo(PHYSICAL));
    assertThat(
        converter.getInterfaceType(new Interface(BOND_IFACE.getName() + ".1")),
        equalTo(BOND_SUBINTERFACE));
    assertThat(converter.getInterfaceType(PHYSICAL_SUBIFACE), equalTo(PHYSICAL_SUBINTERFACE));
  }

  @Test
  public void testGetSuperInterfaceName_physicalSubInterface() {
    assertThat(getSuperInterfaceName(new Interface("swp1.0")), equalTo("swp1"));
    assertThat(getSuperInterfaceName(new Interface("swp1s5.0")), equalTo("swp1s5"));
    assertThat(getSuperInterfaceName(new Interface("eth0.0")), equalTo("eth0"));
  }

  @Test
  public void testGetSuperInterface_none() {
    // could be a bond slave, but isn't in bondSlaveParents map
    assertThat(getSuperInterfaceName(new Interface("swp1")), nullValue());
  }

  @Test
  public void testConvertBond() {
    {
      Interface bondIface = new Interface("bond");
      bondIface.setClagId(1);
      bondIface.setBondSlaves(ImmutableSet.of("slave"));
      bondIface.setVrf("vrf");
      bondIface.createOrGetBridgeSettings().setVids(IntegerSpace.of(10));

      Bond bond = convertBond(bondIface);

      assertThat(bond.getClagId(), equalTo(1));
      assertThat(bond.getSlaves(), contains("slave"));
      assertThat(bond.getVrf(), equalTo("vrf"));
      assertThat(bond.getBridge().getVids().enumerate(), contains(10));
    }
    {
      Interface bondIface = new Interface("bond");
      bondIface.setBondSlaves(ImmutableSet.of("slave"));
      bondIface.createOrGetBridgeSettings().setPvid(5);

      Bond bond = convertBond(bondIface);

      assertThat(bond.getBridge().getPvid(), equalTo(5));
    }
    {
      Interface bondIface = new Interface("bond");
      bondIface.setBondSlaves(ImmutableSet.of("slave"));
      bondIface.createOrGetBridgeSettings().setAccess(5);

      Bond bond = convertBond(bondIface);

      assertThat(bond.getBridge().getAccess(), equalTo(5));
    }
  }

  @Test
  public void testConvertBridge() {
    Interfaces interfaces = new Interfaces();
    interfaces.getInterfaces().put(BRIDGE_IFACE.getName(), BRIDGE_IFACE);

    Converter converter = new Converter(interfaces, _w);
    Bridge bridge = converter.convertBridge();
    assertThat(bridge.getPorts(), equalTo(BRIDGE_IFACE.getBridgePorts()));
    assertThat(bridge.getPvid(), equalTo(BRIDGE_IFACE.getBridgeSettings().getPvid()));
    assertThat(bridge.getVids(), equalTo(BRIDGE_IFACE.getBridgeSettings().getVids()));
  }

  @Test
  public void testConvertInterface() {
    Converter converter = new Converter(new Interfaces(), _w);

    org.batfish.representation.cumulus.Interface vsIface =
        converter.convertInterface(PHYSICAL_IFACE);

    assertThat(vsIface.getName(), equalTo(PHYSICAL_IFACE.getName()));
    assertThat(vsIface.getType(), equalTo(PHYSICAL));
    assertNull(vsIface.getSuperInterfaceName());
    assertThat(vsIface.getEncapsulationVlan(), equalTo(null));
    assertThat(vsIface.getAlias(), equalTo(PHYSICAL_IFACE.getDescription()));

    // bridge settings all null/empty
    assertNull(vsIface.getBridge().getAccess());
    assertTrue(vsIface.getBridge().getVids().isEmpty());
    assertNull(vsIface.getBridge().getPvid());

    assertThat(vsIface.getClag(), equalTo(PHYSICAL_IFACE.getClagSettings()));
    assertTrue(vsIface.getIpAddresses().isEmpty());
    assertThat(vsIface.getSpeed(), equalTo(PHYSICAL_IFACE.getLinkSpeed()));
    assertThat(vsIface.getVrf(), equalTo(PHYSICAL_IFACE.getVrf()));
  }

  @Test
  public void testConvertSubInterface() {
    Interfaces interfaces = new Interfaces();
    interfaces.getInterfaces().put(PHYSICAL_IFACE.getName(), PHYSICAL_IFACE);

    Converter converter = new Converter(interfaces, _w);

    org.batfish.representation.cumulus.Interface vsIface =
        converter.convertInterface(PHYSICAL_SUBIFACE);

    assertThat(vsIface.getName(), equalTo(PHYSICAL_SUBIFACE.getName()));
    assertThat(vsIface.getType(), equalTo(PHYSICAL_SUBINTERFACE));
    assertThat(vsIface.getSuperInterfaceName(), equalTo(PHYSICAL_IFACE.getName()));
    assertThat(vsIface.getEncapsulationVlan(), equalTo(1 /* parsed from name */));
    assertThat(vsIface.getAlias(), equalTo(PHYSICAL_SUBIFACE.getDescription()));
    assertThat(vsIface.getBridge(), equalTo(PHYSICAL_SUBIFACE.getBridgeSettings()));
    assertThat(vsIface.getClag(), equalTo(PHYSICAL_SUBIFACE.getClagSettings()));
    assertThat(vsIface.getIpAddresses(), equalTo(PHYSICAL_SUBIFACE.getAddresses()));
    assertThat(vsIface.getSpeed(), equalTo(PHYSICAL_SUBIFACE.getLinkSpeed()));
    assertThat(vsIface.getVrf(), equalTo(PHYSICAL_SUBIFACE.getVrf()));
  }

  @Test
  public void testConvertInterfaces() {
    Interfaces ifaces = new Interfaces();
    ifaces.getInterfaces().putAll(INTERFACE_MAP);
    Map<String, org.batfish.representation.cumulus.Interface> vsIfaces =
        new Converter(ifaces, _w).convertInterfaces();

    // non-interfaces filtered out
    assertThat(
        vsIfaces.keySet(),
        containsInAnyOrder(PHYSICAL_IFACE.getName(), PHYSICAL_SUBIFACE.getName()));
  }

  @Test
  public void testConvertVlan() {
    Vlan vlan = convertVlan(VLAN_IFACE);
    assertThat(vlan.getName(), equalTo(VLAN_IFACE.getName()));
    assertThat(vlan.getAlias(), equalTo(VLAN_IFACE.getDescription()));
    assertThat(vlan.getAddresses(), equalTo(VLAN_IFACE.getAddresses()));
    assertThat(vlan.getAddressVirtuals(), equalTo(VLAN_IFACE.getAddressVirtuals()));
    assertThat(vlan.getVlanId(), equalTo(VLAN_IFACE.getVlanId()));
    assertThat(vlan.getVrf(), equalTo(VLAN_IFACE.getVrf()));
  }

  @Test
  public void testConvertVlans() {
    Interfaces ifaces = new Interfaces();
    ifaces.getInterfaces().putAll(INTERFACE_MAP);
    Map<String, Vlan> vlans = new Converter(ifaces, _w).convertVlans();

    // non-vlans filtered out
    assertThat(vlans.keySet(), containsInAnyOrder(VLAN_IFACE.getName()));
  }

  @Test
  public void testConvertVrf() {
    Vrf vrf = convertVrf(VRF_IFACE);
    assertThat(vrf.getName(), equalTo(VRF_IFACE.getName()));
    assertThat(vrf.getAddresses(), contains(ADDR1));
  }

  @Test
  public void testConvertVrfs() {
    Interfaces ifaces = new Interfaces();
    ifaces.getInterfaces().putAll(INTERFACE_MAP);
    Map<String, Vrf> vrfs = new Converter(ifaces, _w).convertVrfs();

    // non-vrfs filtered out
    assertThat(vrfs.keySet(), containsInAnyOrder(VRF_IFACE.getName()));
  }

  @Test
  public void testConvertVxlan() {
    Vxlan vxlan = convertVxlan(VXLAN_IFACE);
    assertThat(vxlan.getName(), equalTo(VXLAN_IFACE.getName()));
    assertThat(vxlan.getId(), equalTo(VXLAN_IFACE.getVxlanId()));
    assertThat(vxlan.getBridgeAccessVlan(), equalTo(VXLAN_IFACE.getBridgeSettings().getAccess()));
    assertThat(vxlan.getLocalTunnelip(), equalTo(VXLAN_IFACE.getVxlanLocalTunnelIp()));
  }

  @Test
  public void testConvertVxlans() {
    Interfaces ifaces = new Interfaces();
    ifaces.getInterfaces().putAll(INTERFACE_MAP);
    Map<String, Vxlan> vxlans = new Converter(ifaces, _w).convertVxlans();

    // non-vxlans filtered out
    assertThat(vxlans.keySet(), containsInAnyOrder(VXLAN_IFACE.getName()));
  }
}
