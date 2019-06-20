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
            new FirewallSessionInterfaceInfo(false, ifaces, "IN_ACL", "OUT_ACL"),
            new FirewallSessionInterfaceInfo(false, ifaces, "IN_ACL", "OUT_ACL"))
        .addEqualityGroup(new FirewallSessionInterfaceInfo(false, ifaces, "IN_ACL", null))
        .addEqualityGroup(new FirewallSessionInterfaceInfo(false, ifaces, null, "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(false, ImmutableSet.of("B"), "IN_ACL", "OUT_ACL"))
        .testEquals();
  }
}
