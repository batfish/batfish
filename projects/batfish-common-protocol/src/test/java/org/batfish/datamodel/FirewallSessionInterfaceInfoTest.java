package org.batfish.datamodel;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.FirewallSessionInterfaceInfo.Action;
import org.junit.Test;

/** Tests for {@link FirewallSessionInterfaceInfo}. */
public final class FirewallSessionInterfaceInfoTest {

  @Test
  public void testEquals() {
    ImmutableSet<String> ifaces = ImmutableSet.of("A");
    new EqualsTester()
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(Action.NO_FIB_LOOKUP, ifaces, "IN_ACL", "OUT_ACL"),
            new FirewallSessionInterfaceInfo(Action.NO_FIB_LOOKUP, ifaces, "IN_ACL", "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(Action.NO_FIB_LOOKUP, ifaces, "IN_ACL", null))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(Action.NO_FIB_LOOKUP, ifaces, null, "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.NO_FIB_LOOKUP, ImmutableSet.of("B"), "IN_ACL", "OUT_ACL"))
        .testEquals();
  }
}
