package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
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

  @Test
  public void testJson() {
    FirewallSessionInterfaceInfo info1 =
        new FirewallSessionInterfaceInfo(true, ImmutableList.of(), null, null);
    assertThat(
        info1, equalTo(BatfishObjectMapper.clone(info1, FirewallSessionInterfaceInfo.class)));

    FirewallSessionInterfaceInfo info2 =
        new FirewallSessionInterfaceInfo(false, ImmutableList.of("a"), "b", "c");
    assertThat(
        info2, equalTo(BatfishObjectMapper.clone(info2, FirewallSessionInterfaceInfo.class)));
  }
}
