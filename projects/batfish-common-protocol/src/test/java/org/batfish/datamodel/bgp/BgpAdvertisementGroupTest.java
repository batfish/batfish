package org.batfish.datamodel.bgp;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.ExtendedCommunity;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public final class BgpAdvertisementGroupTest {

  @Test
  public void testJavaSerialization() throws IOException {
    BgpAdvertisementGroup bgpAdvertisementGroup =
        BgpAdvertisementGroup.builder()
            .setAsPath(AsPath.ofSingletonAsSets(2L))
            .setDescription("hello")
            .setExtendedCommunities(ImmutableSet.of(new ExtendedCommunity(12345678L)))
            .setLocalPreference(123L)
            .setMed(456L)
            .setOriginator(new Ip("1.1.1.1"))
            .setOriginType(OriginType.EGP)
            .setPrefixes(ImmutableSet.of(Prefix.ZERO))
            .setRxPeer(new Ip("2.2.2.2"))
            .setStandardCommunities(ImmutableSet.of(555L))
            .setTxAs(666L)
            .setTxPeer(new Ip("3.3.3.3"))
            .build();

    assertThat(SerializationUtils.clone(bgpAdvertisementGroup), equalTo(bgpAdvertisementGroup));
  }

  @Test
  public void testJsonSerialization() throws IOException {
    BgpAdvertisementGroup bgpAdvertisementGroup =
        BgpAdvertisementGroup.builder()
            .setAsPath(AsPath.ofSingletonAsSets(2L))
            .setDescription("hello")
            .setExtendedCommunities(ImmutableSet.of(new ExtendedCommunity(12345678L)))
            .setLocalPreference(123L)
            .setMed(456L)
            .setOriginator(new Ip("1.1.1.1"))
            .setOriginType(OriginType.EGP)
            .setPrefixes(ImmutableSet.of(Prefix.ZERO))
            .setRxPeer(new Ip("2.2.2.2"))
            .setStandardCommunities(ImmutableSet.of(555L))
            .setTxAs(666L)
            .setTxPeer(new Ip("3.3.3.3"))
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
            builder.setExtendedCommunities(ImmutableSet.of(new ExtendedCommunity(1L))).build())
        .addEqualityGroup(builder.setLocalPreference(1L).build())
        .addEqualityGroup(builder.setMed(1L).build())
        .addEqualityGroup(builder.setOriginator(new Ip("1.1.1.1")).build())
        .addEqualityGroup(builder.setOriginType(OriginType.EGP).build())
        .addEqualityGroup(builder.setPrefixes(ImmutableSet.of(Prefix.strict("1.0.0.0/8"))))
        .addEqualityGroup(builder.setRxPeer(new Ip("1.1.1.1")).build())
        .addEqualityGroup(builder.setStandardCommunities(ImmutableSet.of(4L)).build())
        .addEqualityGroup(builder.setTxAs(123L).build())
        .addEqualityGroup(builder.setTxPeer(new Ip("2.2.2.2")).build())
        .testEquals();
  }
}
