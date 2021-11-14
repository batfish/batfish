package org.batfish.representation.frr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.rules.ExpectedException.none;

import com.google.common.collect.ImmutableSet;
import org.batfish.representation.cumulus_concatenated.InterfacesInterface;
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
    assertThat(iface.getType(), equalTo(FrrStructureType.BOND));
  }

  @Test
  public void testGetType_vlan() {
    assertThat(new InterfacesInterface("vlan123").getType(), equalTo(FrrStructureType.VLAN));
  }

  @Test
  public void testGetType_vxlan() {
    InterfacesInterface iface = new InterfacesInterface("foo");
    iface.setVxlanId(1);
    assertThat(iface.getType(), equalTo(FrrStructureType.VXLAN));
  }

  @Test
  public void testGetType_vrf() {
    InterfacesInterface iface = new InterfacesInterface("foo");
    iface.setVrfTable("auto");
    assertThat(iface.getType(), equalTo(FrrStructureType.VRF));
  }

  @Test
  public void testGetType_interface() {
    assertThat(new InterfacesInterface("iface").getType(), equalTo(FrrStructureType.INTERFACE));
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
