package org.batfish.symbolic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.testing.EqualsTester;
import java.util.List;
import java.util.stream.Collectors;
import org.batfish.symbolic.IngressLocation.Type;
import org.junit.jupiter.api.Test;

/** Test for {@link IngressLocation}. */
final class IngressLocationTest {

  @Test
  void testEquals() {
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
  void testInterfaceLink_getters() {
    IngressLocation link = IngressLocation.interfaceLink("node", "iface");
    assertThat(link.getNode(), equalTo("node"));
    assertThat(link.getInterface(), equalTo("iface"));
    assertThat(link.getType(), equalTo(Type.INTERFACE_LINK));
    assertFalse(link.isIngressVrf());
  }

  @Test
  void testInterfaceLink_crash() {
    assertThrows(
        IllegalArgumentException.class,
        () -> IngressLocation.interfaceLink("node", "iface").getVrf());
  }

  @Test
  void testVrf_getters() {
    IngressLocation vrf = IngressLocation.vrf("node", "vrf");
    assertThat(vrf.getNode(), equalTo("node"));
    assertThat(vrf.getVrf(), equalTo("vrf"));
    assertThat(vrf.getType(), equalTo(Type.VRF));
    assertTrue(vrf.isIngressVrf());
  }

  @Test
  void testVrf_crash() {
    assertThrows(
        IllegalArgumentException.class, () -> IngressLocation.vrf("node", "vrf").getInterface());
  }

  @Test
  void testSort() {
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
  void testToString_interface() {
    assertEquals("node[iface]", IngressLocation.interfaceLink("node", "iface").toString());
  }

  @Test
  void testToString_vrf() {
    assertEquals("node[@vrf(vrf)]", IngressLocation.vrf("node", "vrf").toString());
  }

  @Test
  void testToString_escape() {
    assertEquals("\"<\"[\",\"]", IngressLocation.interfaceLink("<", ",").toString());
  }
}
