package org.batfish.datamodel;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.SessionVrfScope.SpecificVrf;
import org.batfish.datamodel.dataplane.rib.RibGroup;
import org.batfish.datamodel.dataplane.rib.RibId;
import org.junit.Test;

/** Tests of {@link BgpPeerConfig}. */
public final class BgpPeerConfigTest {
  private static Object[] withToBuilder(BgpPeerConfig peer) {
    return new Object[] {peer, peer.toBuilder().build()};
  }

  @Test
  public void testActiveEquals() {
    BgpAuthenticationSettings authenticationSettings = new BgpAuthenticationSettings();
    authenticationSettings.setAuthenticationAlgorithm(BgpAuthenticationAlgorithm.TCP_ENHANCED_MD5);
    BgpActivePeerConfig peer =
        BgpActivePeerConfig.builder()
            .setAppliedRibGroup(
                new RibGroup(
                    "rib-group",
                    ImmutableList.of(new RibId("host", "vrf", "rib")),
                    "import-policy",
                    new RibId("host", "vrf", "main")))
            .setAuthenticationSettings(authenticationSettings)
            .setCheckLocalIpOnAccept(false)
            .setClusterId(1L)
            .setConfederation(2L)
            .setDefaultMetric(3)
            .setDescription("description")
            .setEbgpMultihop(true)
            .setEnforceFirstAs(true)
            .setGeneratedRoutes(
                ImmutableSet.of(
                    GeneratedRoute.builder().setNetwork(Prefix.ZERO).setDiscard(true).build()))
            .setGroup("group")
            .setLocalAs(4L)
            .setLocalIp(Ip.parse("192.0.2.1"))
            .setPeerAddress(Ip.parse("192.0.2.2"))
            .setRemoteAsns(LongSpace.of(5L))
            .setIpv4UnicastAddressFamily(
                Ipv4UnicastAddressFamily.builder()
                    .setAddressFamilyCapabilities(
                        AddressFamilyCapabilities.builder().setSendCommunity(true).build())
                    .setExportPolicy("export")
                    .setExportPolicySources(ImmutableSortedSet.of("export-source"))
                    .setImportPolicy("import")
                    .setImportPolicySources(ImmutableSortedSet.of("import-source"))
                    .setRouteReflectorClient(true)
                    .build())
            .setEvpnAddressFamily(EvpnAddressFamily.builder().setPropagateUnmatched(true).build())
            .setReplaceNonLocalAsesOnExport(true)
            .setSessionVrfScope(new SpecificVrf("session-vrf"))
            .build();

    new EqualsTester()
        .addEqualityGroup(withToBuilder(peer))
        .addEqualityGroup(
            withToBuilder(peer.toBuilder().setPeerAddress(Ip.parse("192.0.2.3")).build()))
        .testEquals();
  }

  @Test
  public void testPassiveEquals() {
    BgpPassivePeerConfig peer =
        BgpPassivePeerConfig.builder()
            .setLocalAs(1L)
            .setPeerPrefix(Prefix.parse("192.0.2.0/24"))
            .setRemoteAs(2L)
            .build();

    new EqualsTester()
        .addEqualityGroup(withToBuilder(peer))
        .addEqualityGroup(
            withToBuilder(peer.toBuilder().setPeerPrefix(Prefix.parse("198.51.100.0/24")).build()))
        .testEquals();
  }
}
