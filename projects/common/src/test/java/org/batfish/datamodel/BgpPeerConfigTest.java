package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.SessionVrfScope.SpecificVrf;
import org.batfish.datamodel.dataplane.rib.RibGroup;
import org.batfish.datamodel.dataplane.rib.RibId;
import org.junit.Test;

/** Tests of {@link BgpPeerConfig}. */
public final class BgpPeerConfigTest {

  @Test
  public void testActiveToBuilder() {
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

    assertThat(peer.toBuilder().build(), equalTo(peer));
  }

  @Test
  public void testPassiveToBuilder() {
    BgpPassivePeerConfig peer =
        BgpPassivePeerConfig.builder()
            .setLocalAs(1L)
            .setPeerPrefix(Prefix.parse("192.0.2.0/24"))
            .setRemoteAs(2L)
            .build();

    assertThat(peer.toBuilder().build(), equalTo(peer));
  }

  @Test
  public void testUnnumberedToBuilder() {
    BgpUnnumberedPeerConfig peer =
        BgpUnnumberedPeerConfig.builder()
            .setLocalAs(1L)
            .setPeerInterface("Ethernet1")
            .setRemoteAs(2L)
            .build();

    assertThat(peer.toBuilder().build(), equalTo(peer));
  }
}
