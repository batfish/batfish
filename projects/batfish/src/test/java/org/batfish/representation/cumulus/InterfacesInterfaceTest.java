package org.batfish.representation.cumulus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.rules.ExpectedException.none;

import com.google.common.collect.ImmutableSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test for {@link InterfacesInterface}. */
public final class InterfacesInterfaceTest {
  @Rule public ExpectedException _exception = none();

  @Test
  public void testGetType_bond() {
    InterfacesInterface iface = new InterfacesInterface("iface");
    iface.setBondSlaves(ImmutableSet.of("a"));
    assertThat(iface.getType(), equalTo(CumulusStructureType.BOND));
  }

  @Test
  public void testGetType_vlan() {
    assertThat(new InterfacesInterface("vlan123").getType(), equalTo(CumulusStructureType.VLAN));
  }

  @Test
  public void testGetType_vxlan() {
    InterfacesInterface iface = new InterfacesInterface("foo");
    iface.setVxlanId(1);
    assertThat(iface.getType(), equalTo(CumulusStructureType.VXLAN));
  }

  @Test
  public void testGetType_vrf() {
    InterfacesInterface iface = new InterfacesInterface("foo");
    iface.setVrfTable("auto");
    assertThat(iface.getType(), equalTo(CumulusStructureType.VRF));
  }

  @Test
  public void testGetType_interface() {
    assertThat(new InterfacesInterface("iface").getType(), equalTo(CumulusStructureType.INTERFACE));
  }

  @Test
  public void testGetType_ambiguous_vlan_vrf() {
    InterfacesInterface iface = new InterfacesInterface("vlan123");
    iface.setVrfTable("auto");

    _exception.expect(IllegalStateException.class);
    iface.getType();
  }

  @Test
  public void testGetType_ambiguous_vlan_vxlan() {
    InterfacesInterface iface = new InterfacesInterface("vlan123");
    iface.setVxlanId(123);

    _exception.expect(IllegalStateException.class);
    iface.getType();
  }

  @Test
  public void testGetType_ambiguous_vxlan_vrf() {
    InterfacesInterface iface = new InterfacesInterface("foo");
    iface.setVxlanId(123);
    iface.setVrfTable("auto");

    _exception.expect(IllegalStateException.class);
    iface.getType();
  }
}
