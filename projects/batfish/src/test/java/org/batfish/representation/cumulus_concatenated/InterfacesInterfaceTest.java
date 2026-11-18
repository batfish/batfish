package org.batfish.representation.cumulus_concatenated;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

/** Test for {@link InterfacesInterface}. */
final class InterfacesInterfaceTest {

  @Test
  void testGetType_bond() {
    InterfacesInterface iface = new InterfacesInterface("iface");
    iface.setBondSlaves(ImmutableSet.of("a"));
    assertThat(iface.getType(), equalTo(CumulusStructureType.BOND));
  }

  @Test
  void testGetType_vlan() {
    assertThat(new InterfacesInterface("vlan123").getType(), equalTo(CumulusStructureType.VLAN));
  }

  @Test
  void testGetType_vxlan() {
    InterfacesInterface iface = new InterfacesInterface("foo");
    iface.setVxlanId(1);
    assertThat(iface.getType(), equalTo(CumulusStructureType.VXLAN));
  }

  @Test
  void testGetType_vrf() {
    InterfacesInterface iface = new InterfacesInterface("foo");
    iface.setVrfTable("auto");
    assertThat(iface.getType(), equalTo(CumulusStructureType.VRF));
  }

  @Test
  void testGetType_interface() {
    assertThat(new InterfacesInterface("iface").getType(), equalTo(CumulusStructureType.INTERFACE));
  }

  @Test
  void testGetType_ambiguous_vlan_vrf() {
    InterfacesInterface iface = new InterfacesInterface("vlan123");
    iface.setVrfTable("auto");

    assertThrows(IllegalStateException.class, () -> iface.getType());
  }

  @Test
  void testGetType_ambiguous_vlan_vxlan() {
    InterfacesInterface iface = new InterfacesInterface("vlan123");
    iface.setVxlanId(123);

    assertThrows(IllegalStateException.class, () -> iface.getType());
  }

  @Test
  void testGetType_ambiguous_vxlan_vrf() {
    InterfacesInterface iface = new InterfacesInterface("foo");
    iface.setVxlanId(123);
    iface.setVrfTable("auto");

    assertThrows(IllegalStateException.class, () -> iface.getType());
  }
}
