package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
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
        BgpUnnumberedPeerConfig.builder().setPeerInterface("eth1");
    BgpUnnumberedPeerConfig c = builder.build();
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(c, c, builder.build())
        .addEqualityGroup(builder.setAdditionalPathsReceive(true).build())
        .addEqualityGroup(builder.setAdditionalPathsSelectAll(true).build())
        .addEqualityGroup(builder.setAdditionalPathsSend(true).build())
        .addEqualityGroup(builder.setAdvertiseExternal(true).build())
        .addEqualityGroup(builder.setAdvertiseInactive(true).build())
        .addEqualityGroup(builder.setAllowLocalAsIn(true).build())
        .addEqualityGroup(builder.setAllowRemoteAsOut(true).build())
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
        .addEqualityGroup(builder.setExportPolicy("bar").build())
        .addEqualityGroup(builder.setExportPolicySources(ImmutableSortedSet.of("baz")).build())
        .addEqualityGroup(
            builder
                .setGeneratedRoutes(
                    ImmutableSet.of(
                        GeneratedRoute.builder().setNetwork(Prefix.ZERO).setDiscard(true).build()))
                .build())
        .addEqualityGroup(builder.setGroup("g1").build())
        .addEqualityGroup(builder.setImportPolicy("boo").build())
        .addEqualityGroup(builder.setImportPolicySources(ImmutableSortedSet.of("booze")).build())
        .addEqualityGroup(builder.setLocalAs(10L).build())
        .addEqualityGroup(builder.setLocalIp(Ip.FIRST_CLASS_A_PRIVATE_IP).build())
        .addEqualityGroup(builder.setPeerInterface("eth0").build())
        .addEqualityGroup(builder.setRemoteAsns(LongSpace.of(11L)).build())
        .addEqualityGroup(builder.setRouteReflectorClient(true).build())
        .addEqualityGroup(builder.setSendCommunity(true).build())
        .testEquals();
  }

  @Test
  public void testJacksonSerialization() throws IOException {
    BgpUnnumberedPeerConfig bgpUnnumberedPeerConfig =
        BgpUnnumberedPeerConfig.builder()
            .setAdditionalPathsReceive(true)
            .setAdditionalPathsSelectAll(true)
            .setAdditionalPathsSend(true)
            .setAdvertiseExternal(true)
            .setAdvertiseInactive(true)
            .setAllowLocalAsIn(true)
            .setAllowRemoteAsOut(true)
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
            .setExportPolicy("bar")
            .setExportPolicySources(ImmutableSortedSet.of("baz"))
            .setGeneratedRoutes(
                ImmutableSet.of(
                    GeneratedRoute.builder().setNetwork(Prefix.ZERO).setDiscard(true).build()))
            .setGroup("g1")
            .setImportPolicy("boo")
            .setImportPolicySources(ImmutableSortedSet.of("booze"))
            .setLocalAs(10L)
            .setLocalIp(Ip.FIRST_CLASS_A_PRIVATE_IP)
            .setPeerInterface("eth0")
            .setRemoteAsns(LongSpace.of(11L))
            .setRouteReflectorClient(true)
            .setSendCommunity(true)
            .build();

    assertThat(
        BatfishObjectMapper.clone(bgpUnnumberedPeerConfig, BgpUnnumberedPeerConfig.class),
        equalTo(bgpUnnumberedPeerConfig));
  }

  @Test
  public void testJavaSerialization() {
    BgpUnnumberedPeerConfig bgpUnnumberedPeerConfig =
        BgpUnnumberedPeerConfig.builder()
            .setAdditionalPathsReceive(true)
            .setAdditionalPathsSelectAll(true)
            .setAdditionalPathsSend(true)
            .setAdvertiseExternal(true)
            .setAdvertiseInactive(true)
            .setAllowLocalAsIn(true)
            .setAllowRemoteAsOut(true)
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
            .setExportPolicy("bar")
            .setExportPolicySources(ImmutableSortedSet.of("baz"))
            .setGeneratedRoutes(
                ImmutableSet.of(
                    GeneratedRoute.builder().setNetwork(Prefix.ZERO).setDiscard(true).build()))
            .setGroup("g1")
            .setImportPolicy("boo")
            .setImportPolicySources(ImmutableSortedSet.of("booze"))
            .setLocalAs(10L)
            .setLocalIp(Ip.FIRST_CLASS_A_PRIVATE_IP)
            .setPeerInterface("eth0")
            .setRemoteAsns(LongSpace.of(11L))
            .setRouteReflectorClient(true)
            .setSendCommunity(true)
            .build();

    assertThat(SerializationUtils.clone(bgpUnnumberedPeerConfig), equalTo(bgpUnnumberedPeerConfig));
  }
}
