package org.batfish.datamodel.bgp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.junit.Test;

public final class BgpAdvertisementGroupTest {

  @Test
  public void testJavaSerialization() {
    BgpAdvertisementGroup bgpAdvertisementGroup =
        BgpAdvertisementGroup.builder()
            .setAsPath(AsPath.ofSingletonAsSets(2L))
            .setDescription("hello")
            .setExtendedCommunities(ImmutableSet.of(ExtendedCommunity.of(1, 6555L, 123456L)))
            .setLocalPreference(123L)
            .setMed(456L)
            .setOriginator(Ip.parse("1.1.1.1"))
            .setOriginType(OriginType.EGP)
            .setPrefixes(ImmutableSet.of(Prefix.ZERO))
            .setRxPeer(Ip.parse("2.2.2.2"))
            .setStandardCommunities(ImmutableSet.of(555L))
            .setTxAs(666L)
            .setTxPeer(Ip.parse("3.3.3.3"))
            .build();

    assertThat(SerializationUtils.clone(bgpAdvertisementGroup), equalTo(bgpAdvertisementGroup));
  }

  @Test
  public void testJsonSerialization() {
    BgpAdvertisementGroup bgpAdvertisementGroup =
        BgpAdvertisementGroup.builder()
            .setAsPath(AsPath.ofSingletonAsSets(2L))
            .setDescription("hello")
            .setExtendedCommunities(ImmutableSet.of(ExtendedCommunity.of(0, 6555L, 123456L)))
            .setLocalPreference(123L)
            .setMed(456L)
            .setOriginator(Ip.parse("1.1.1.1"))
            .setOriginType(OriginType.EGP)
            .setPrefixes(ImmutableSet.of(Prefix.ZERO))
            .setRxPeer(Ip.parse("2.2.2.2"))
            .setStandardCommunities(ImmutableSet.of(555L))
            .setTxAs(666L)
            .setTxPeer(Ip.parse("3.3.3.3"))
            .build();

    assertThat(
        BatfishObjectMapper.clone(bgpAdvertisementGroup, BgpAdvertisementGroup.class),
        equalTo(bgpAdvertisementGroup));
  }

  @Test
  public void testEquals() {
    BgpAdvertisementGroup.Builder builder =
        BgpAdvertisementGroup.builder()
            .setAsPath(AsPath.ofSingletonAsSets())
            .setPrefixes(ImmutableSet.of(Prefix.ZERO))
            .setRxPeer(Ip.ZERO)
            .setTxPeer(Ip.ZERO);
    BgpAdvertisementGroup initial = builder.build();
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(initial, initial, builder.build())
        .addEqualityGroup(builder.setAsPath(AsPath.ofSingletonAsSets(5L)).build())
        .addEqualityGroup(builder.setDescription("hello").build())
        .addEqualityGroup(
            builder
                .setExtendedCommunities(ImmutableSet.of(ExtendedCommunity.of(0, 6555L, 1L)))
                .build())
        .addEqualityGroup(builder.setLocalPreference(1L).build())
        .addEqualityGroup(builder.setMed(1L).build())
        .addEqualityGroup(builder.setOriginator(Ip.parse("1.1.1.1")).build())
        .addEqualityGroup(builder.setOriginType(OriginType.EGP).build())
        .addEqualityGroup(builder.setPrefixes(ImmutableSet.of(Prefix.strict("1.0.0.0/8"))))
        .addEqualityGroup(builder.setRxPeer(Ip.parse("1.1.1.1")).build())
        .addEqualityGroup(builder.setStandardCommunities(ImmutableSet.of(4L)).build())
        .addEqualityGroup(builder.setTxAs(123L).build())
        .addEqualityGroup(builder.setTxPeer(Ip.parse("2.2.2.2")).build())
        .testEquals();
  }
}
