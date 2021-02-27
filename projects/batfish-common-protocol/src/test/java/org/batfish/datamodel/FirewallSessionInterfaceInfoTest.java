package org.batfish.datamodel;

import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.SOURCE_ORIGINATING_FROM_DEVICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.FirewallSessionInterfaceInfo.Action;
import org.junit.Test;

/** Tests for {@link FirewallSessionInterfaceInfo}. */
public final class FirewallSessionInterfaceInfoTest {

  @Test
  public void testEquals() {
    ImmutableSet<String> ifaces = ImmutableSet.of("A");
    ImmutableSet<String> srcIfaces = ImmutableSet.of("B");
    new EqualsTester()
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ifaces, srcIfaces, false, "IN_ACL", "OUT_ACL"),
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ifaces, srcIfaces, false, "IN_ACL", "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.POST_NAT_FIB_LOOKUP, ifaces, srcIfaces, false, "IN_ACL", "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE,
                ImmutableSet.of("C"),
                srcIfaces,
                false,
                "IN_ACL",
                "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ifaces, ImmutableSet.of("D"), false, "IN_ACL", "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ifaces, null, false, "IN_ACL", "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ifaces, srcIfaces, true, "IN_ACL", "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ifaces, srcIfaces, false, null, "OUT_ACL"))
        .addEqualityGroup(
            new FirewallSessionInterfaceInfo(
                Action.FORWARD_OUT_IFACE, ifaces, srcIfaces, false, "IN_ACL", null))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    FirewallSessionInterfaceInfo info =
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE,
            ImmutableSet.of("A"),
            ImmutableSet.of("B"),
            false,
            "IN_ACL",
            "OUT_ACL");
    FirewallSessionInterfaceInfo clone =
        BatfishObjectMapper.clone(info, FirewallSessionInterfaceInfo.class);
    assertEquals(info, clone);
  }

  @Test
  public void testCanSetUpSessionForFlowFrom() {
    // Short constructor: Defaults are srcInterfaces = null and matchOriginatingFromDevice = true
    FirewallSessionInterfaceInfo setUpForAnything =
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE, ImmutableSet.of("A"), null, null);
    assertTrue(setUpForAnything.canSetUpSessionForFlowFrom("someInterface"));
    assertTrue(setUpForAnything.canSetUpSessionForFlowFrom(null));
    assertTrue(setUpForAnything.canSetUpSessionForFlowFrom(SOURCE_ORIGINATING_FROM_DEVICE));

    // srcInterfaces = null, but matchOriginatingFromDevice = false
    FirewallSessionInterfaceInfo noMatchOriginatingFromDevice =
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE, ImmutableSet.of("A"), null, false, null, null);
    assertTrue(noMatchOriginatingFromDevice.canSetUpSessionForFlowFrom("someInterface"));
    assertFalse(noMatchOriginatingFromDevice.canSetUpSessionForFlowFrom(null));
    assertFalse(
        noMatchOriginatingFromDevice.canSetUpSessionForFlowFrom(SOURCE_ORIGINATING_FROM_DEVICE));

    // srcInterfaces are limited to eth1, matchOriginatingFromDevice = true
    FirewallSessionInterfaceInfo matchEth1OrFromDevice =
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE,
            ImmutableSet.of("A"),
            ImmutableSet.of("eth1"),
            true,
            null,
            null);
    assertFalse(matchEth1OrFromDevice.canSetUpSessionForFlowFrom("someInterface"));
    assertTrue(matchEth1OrFromDevice.canSetUpSessionForFlowFrom("eth1"));
    assertTrue(matchEth1OrFromDevice.canSetUpSessionForFlowFrom(null));
    assertTrue(matchEth1OrFromDevice.canSetUpSessionForFlowFrom(SOURCE_ORIGINATING_FROM_DEVICE));

    // srcInterfaces are limited to eth1, matchOriginatingFromDevice = false
    FirewallSessionInterfaceInfo matchEth1Only =
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE,
            ImmutableSet.of("A"),
            ImmutableSet.of("eth1"),
            false,
            null,
            null);
    assertFalse(matchEth1Only.canSetUpSessionForFlowFrom("someInterface"));
    assertTrue(matchEth1Only.canSetUpSessionForFlowFrom("eth1"));
    assertFalse(matchEth1Only.canSetUpSessionForFlowFrom(null));
    assertFalse(matchEth1Only.canSetUpSessionForFlowFrom(SOURCE_ORIGINATING_FROM_DEVICE));
  }
}
