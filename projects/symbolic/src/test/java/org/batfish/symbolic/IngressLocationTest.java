package org.batfish.symbolic;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.util.List;
import java.util.stream.Collectors;
import org.batfish.symbolic.IngressLocation.Type;
import org.junit.Test;

/** Test for {@link IngressLocation}. */
public final class IngressLocationTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            IngressLocation.interfaceLink("node", "iface"),
            IngressLocation.interfaceLink("node", "iface"))
        .addEqualityGroup(IngressLocation.interfaceLink("node2", "iface"))
        .addEqualityGroup(IngressLocation.interfaceLink("node2", "iface2"))
        .addEqualityGroup(
            IngressLocation.vrf(
                "node2", /* Not a vrf name, but tests just type difference */ "iface2"))
        .addEqualityGroup(IngressLocation.vrf("node2", "vrf"))
        .addEqualityGroup(IngressLocation.vrf("node3", "vrf"))
        .testEquals();
  }

  @Test
  public void testInterfaceLink_getters() {
    IngressLocation link = IngressLocation.interfaceLink("node", "iface");
    assertThat(link.getNode(), equalTo("node"));
    assertThat(link.getInterface(), equalTo("iface"));
    assertThat(link.getType(), equalTo(Type.INTERFACE_LINK));
    assertFalse(link.isIngressVrf());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInterfaceLink_crash() {
    IngressLocation.interfaceLink("node", "iface").getVrf();
  }

  @Test
  public void testVrf_getters() {
    IngressLocation vrf = IngressLocation.vrf("node", "vrf");
    assertThat(vrf.getNode(), equalTo("node"));
    assertThat(vrf.getVrf(), equalTo("vrf"));
    assertThat(vrf.getType(), equalTo(Type.VRF));
    assertTrue(vrf.isIngressVrf());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testVrf_crash() {
    IngressLocation.vrf("node", "vrf").getInterface();
  }

  @Test
  public void testSort() {
    List<IngressLocation> locs =
        ImmutableList.of(
            IngressLocation.interfaceLink("n", "i"),
            IngressLocation.interfaceLink("n", "i2"),
            IngressLocation.interfaceLink("n2", "i"),
            IngressLocation.vrf("n", "v"),
            IngressLocation.vrf("n", "v2"),
            IngressLocation.vrf("n2", "v"));
    assertThat(
        locs.stream().sorted().collect(Collectors.toList()),
        equalTo(Lists.reverse(locs).stream().sorted().collect(Collectors.toList())));
  }

  @Test
  public void testToString_interface() {
    assertEquals("node[iface]", IngressLocation.interfaceLink("node", "iface").toString());
  }

  @Test
  public void testToString_vrf() {
    assertEquals("node[@vrf(vrf)]", IngressLocation.vrf("node", "vrf").toString());
  }

  @Test
  public void testToString_escape() {
    assertEquals("\"<\"[\",\"]", IngressLocation.interfaceLink("<", ",").toString());
  }
}
