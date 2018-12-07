package org.batfish.bgp;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.ExtendedCommunity;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public final class BgpAdvertisementGroupTest {

  @Test
  public void testSerialization() throws IOException {
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
}
