package org.batfish.datamodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.FirewallSessionInterfaceInfo.Action;
import org.junit.jupiter.api.Test;

/** Tests for {@link FirewallSessionInterfaceInfo}. */
final class FirewallSessionInterfaceInfoTest {

  @Test
  void testEquals() {
    ImmutableSet<String> ifaces = ImmutableSet.of("A");
    ImmutableSet<String> srcIfaces = ImmutableSet.of("B");
    new EqualsTester()
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ifaces, srcIfaces, "IN_ACL", "OUT_ACL"),
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ifaces, srcIfaces, "IN_ACL", "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.POST_NAT_FIB_LOOKUP, ifaces, srcIfaces, "IN_ACL", "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ImmutableSet.of("C"), srcIfaces, "IN_ACL", "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ifaces, ImmutableSet.of("D"), "IN_ACL", "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ifaces, null, "IN_ACL", "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ifaces, srcIfaces, null, "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ifaces, srcIfaces, "IN_ACL", null))
        .testEquals();
  }

  @Test
  void testJsonSerialization() {
    FirewallSessionInterfaceInfo info =
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE,
            ImmutableSet.of("A"),
            ImmutableSet.of("B"),
            "IN_ACL",
            "OUT_ACL");
    FirewallSessionInterfaceInfo clone =
        BatfishObjectMapper.clone(info, FirewallSessionInterfaceInfo.class);
    assertEquals(info, clone);
  }
}
