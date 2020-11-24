package org.batfish.datamodel;

import static org.batfish.datamodel.BgpAdvertisement.nullSafeCompareTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import org.batfish.datamodel.BgpAdvertisement.BgpAdvertisementType;
import org.junit.Test;

public class BgpAdvertisementTest {

  @Test
  public void testConstructorCanonicalization() {
    BgpAdvertisement advert =
        new BgpAdvertisement(
            BgpAdvertisementType.EBGP_SENT,
            Prefix.parse("1.1.1.1/24"),
            Ip.parse("1.1.1.1"),
            "srcNode",
            "srcVrf",
            Ip.parse("2.2.2.2"),
            "dstNode",
            "dstVrf",
            Ip.parse("3.3.3.3"),
            RoutingProtocol.BGP,
            OriginType.EGP,
            20,
            20,
            Ip.parse("0.0.0.0"),
            AsPath.of(Lists.newArrayList()),
            ImmutableSortedSet.of(),
            ImmutableSortedSet.of(),
            10);
    assertThat(advert.getSrcNode(), equalTo("srcnode"));
    assertThat(advert.getDstNode(), equalTo("dstnode"));

    // don't canonicalize VRFs
    assertThat(advert.getSrcVrf(), equalTo("srcVrf"));
    assertThat(advert.getDstVrf(), equalTo("dstVrf"));
  }

  @Test
  public void testNullSafeCompareTo() {
    assertThat(nullSafeCompareTo(null, null), equalTo(0));
    assertThat(nullSafeCompareTo(null, "a"), equalTo(-1));
    assertThat(nullSafeCompareTo("a", null), equalTo(1));
    assertThat(nullSafeCompareTo("a", "b"), equalTo("a".compareTo("b")));
  }
}
