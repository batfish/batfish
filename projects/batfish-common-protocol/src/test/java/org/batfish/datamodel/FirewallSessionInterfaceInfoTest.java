package org.batfish.datamodel;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Tests for {@link FirewallSessionInterfaceInfo}. */
public final class FirewallSessionInterfaceInfoTest {

  @Test
  public void testEquals() {
    ImmutableSet<String> ifaces = ImmutableSet.of("A");
    new EqualsTester()
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(ifaces, "IN_ACL", "OUT_ACL"),
            new FirewallSessionInterfaceInfo(ifaces, "IN_ACL", "OUT_ACL"))
        .addEqualityGroup(new FirewallSessionInterfaceInfo(ifaces, "IN_ACL", null))
        .addEqualityGroup(new FirewallSessionInterfaceInfo(ifaces, null, "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(ImmutableSet.of("B"), "IN_ACL", "OUT_ACL"))
        .testEquals();
  }
}
