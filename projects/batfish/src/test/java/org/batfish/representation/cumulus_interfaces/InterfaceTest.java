package org.batfish.representation.cumulus_interfaces;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.rules.ExpectedException.none;

import org.batfish.representation.cumulus.CumulusStructureType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Test for {@link Interface}. */
public final class InterfaceTest {
  @Rule public ExpectedException _exception = none();

  @Test
  public void testGetType_vlan() {
    assertThat(new Interface("vlan123").getType(), equalTo(CumulusStructureType.VLAN));
  }

  @Test
  public void testGetType_vxlan() {
    Interface iface = new Interface("foo");
    iface.setVxlanId(1);
    assertThat(iface.getType(), equalTo(CumulusStructureType.VXLAN));
  }

  @Test
  public void testGetType_vrf() {
    Interface iface = new Interface("foo");
    iface.setVrfTable("auto");
    assertThat(iface.getType(), equalTo(CumulusStructureType.VRF));
  }

  @Test
  public void testGetType_interface() {
    assertThat(new Interface("iface").getType(), equalTo(CumulusStructureType.INTERFACE));
  }

  @Test
  public void testGetType_ambiguous_vlan_vrf() {
    Interface iface = new Interface("vlan123");
    iface.setVrfTable("auto");

    _exception.expect(IllegalStateException.class);
    iface.getType();
  }

  @Test
  public void testGetType_ambiguous_vlan_vxlan() {
    Interface iface = new Interface("vlan123");
    iface.setVxlanId(123);

    _exception.expect(IllegalStateException.class);
    iface.getType();
  }

  @Test
  public void testGetType_ambiguous_vxlan_vrf() {
    Interface iface = new Interface("foo");
    iface.setVxlanId(123);
    iface.setVrfTable("auto");

    _exception.expect(IllegalStateException.class);
    iface.getType();
  }
}
