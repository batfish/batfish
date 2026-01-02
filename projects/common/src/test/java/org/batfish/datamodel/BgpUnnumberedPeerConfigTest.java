package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.testing.EqualsTester;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.dataplane.rib.RibGroup;
import org.batfish.datamodel.dataplane.rib.RibId;
import org.junit.Test;

/** Test of {@link BgpUnnumberedPeerConfig}. */
@ParametersAreNonnullByDefault
public final class BgpUnnumberedPeerConfigTest {

  private static final BgpAuthenticationSettings BGP_AUTHENTICATION_SETTINGS =
      initBgpAuthenticationSettings();

  private static @Nonnull BgpAuthenticationSettings initBgpAuthenticationSettings() {
    BgpAuthenticationSettings bgpAuthenticationSettings = new BgpAuthenticationSettings();
    bgpAuthenticationSettings.setAuthenticationAlgorithm(
        BgpAuthenticationAlgorithm.TCP_ENHANCED_MD5);
    return bgpAuthenticationSettings;
  }

  @Test
  public void testEquals() {
    BgpUnnumberedPeerConfig.Builder builder =
        BgpUnnumberedPeerConfig.builder()
            .setPeerInterface("eth1")
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build());
    BgpUnnumberedPeerConfig c = builder.build();
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(c, c, builder.build())
        .addEqualityGroup(
            builder
                .setIpv4UnicastAddressFamily(
                    Ipv4UnicastAddressFamily.builder()
                        .setAddressFamilyCapabilities(
                            AddressFamilyCapabilities.builder().setSendCommunity(true).build())
                        .build())
                .build())
        .addEqualityGroup(
            builder
                .setAppliedRibGroup(
                    new RibGroup(
                        "blah",
                        ImmutableList.of(new RibId("d", "e", "f")),
                        "blah1",
                        new RibId("a", "b", "c")))
                .build())
        .addEqualityGroup(builder.setAuthenticationSettings(BGP_AUTHENTICATION_SETTINGS).build())
        .addEqualityGroup(builder.setClusterId(5L).build())
        .addEqualityGroup(builder.setDefaultMetric(5).build())
        .addEqualityGroup(builder.setDescription("foo").build())
        .addEqualityGroup(builder.setEbgpMultihop(true).build())
        .addEqualityGroup(builder.setEnforceFirstAs(true).build())
        .addEqualityGroup(
            builder
                .setIpv4UnicastAddressFamily(
                    Ipv4UnicastAddressFamily.builder().setExportPolicy("bar").build())
                .build())
        .addEqualityGroup(
            builder
                .setGeneratedRoutes(
                    ImmutableSet.of(
                        GeneratedRoute.builder().setNetwork(Prefix.ZERO).setDiscard(true).build()))
                .build())
        .addEqualityGroup(builder.setGroup("g1").build())
        .addEqualityGroup(
            builder
                .setIpv4UnicastAddressFamily(
                    Ipv4UnicastAddressFamily.builder().setExportPolicy("boo").build())
                .build())
        .addEqualityGroup(builder.setLocalAs(10L).build())
        .addEqualityGroup(builder.setLocalIp(Ip.FIRST_CLASS_A_PRIVATE_IP).build())
        .addEqualityGroup(builder.setPeerInterface("eth0").build())
        .addEqualityGroup(builder.setRemoteAsns(LongSpace.of(11L)).build())
        .addEqualityGroup(
            builder.setEvpnAddressFamily(
                EvpnAddressFamily.builder().setPropagateUnmatched(true).build()))
        .addEqualityGroup(builder.setReplaceNonLocalAsesOnExport(true).build())
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() {
    BgpUnnumberedPeerConfig bgpUnnumberedPeerConfig =
        BgpUnnumberedPeerConfig.builder()
            .setAppliedRibGroup(
                new RibGroup(
                    "blah",
                    ImmutableList.of(new RibId("d", "e", "f")),
                    "blah1",
                    new RibId("a", "b", "c")))
            .setAuthenticationSettings(BGP_AUTHENTICATION_SETTINGS)
            .setClusterId(5L)
            .setDefaultMetric(5)
            .setDescription("foo")
            .setEbgpMultihop(true)
            .setEnforceFirstAs(true)
            .setIpv4UnicastAddressFamily(
                Ipv4UnicastAddressFamily.builder()
                    .setExportPolicy("bar")
                    .setExportPolicySources(ImmutableSortedSet.of("baz"))
                    .setImportPolicy("boo")
                    .setImportPolicySources(ImmutableSortedSet.of("booze"))
                    .build())
            .setGeneratedRoutes(
                ImmutableSet.of(
                    GeneratedRoute.builder().setNetwork(Prefix.ZERO).setDiscard(true).build()))
            .setGroup("g1")
            .setLocalAs(10L)
            .setLocalIp(Ip.FIRST_CLASS_A_PRIVATE_IP)
            .setPeerInterface("eth0")
            .setRemoteAsns(LongSpace.of(11L))
            .build();

    assertThat(
        BatfishObjectMapper.clone(bgpUnnumberedPeerConfig, BgpUnnumberedPeerConfig.class),
        equalTo(bgpUnnumberedPeerConfig));
  }

  @Test
  public void testJavaSerialization() {
    BgpUnnumberedPeerConfig bgpUnnumberedPeerConfig =
        BgpUnnumberedPeerConfig.builder()
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
            .setAppliedRibGroup(
                new RibGroup(
                    "blah",
                    ImmutableList.of(new RibId("d", "e", "f")),
                    "blah1",
                    new RibId("a", "b", "c")))
            .setAuthenticationSettings(BGP_AUTHENTICATION_SETTINGS)
            .setClusterId(5L)
            .setDefaultMetric(5)
            .setDescription("foo")
            .setEbgpMultihop(true)
            .setEnforceFirstAs(true)
            .setIpv4UnicastAddressFamily(
                Ipv4UnicastAddressFamily.builder()
                    .setExportPolicy("bar")
                    .setExportPolicySources(ImmutableSortedSet.of("baz"))
                    .setImportPolicy("boo")
                    .setImportPolicySources(ImmutableSortedSet.of("booze"))
                    .build())
            .setGeneratedRoutes(
                ImmutableSet.of(
                    GeneratedRoute.builder().setNetwork(Prefix.ZERO).setDiscard(true).build()))
            .setGroup("g1")
            .setLocalAs(10L)
            .setLocalIp(Ip.FIRST_CLASS_A_PRIVATE_IP)
            .setPeerInterface("eth0")
            .setRemoteAsns(LongSpace.of(11L))
            .build();

    assertThat(SerializationUtils.clone(bgpUnnumberedPeerConfig), equalTo(bgpUnnumberedPeerConfig));
  }
}
