package org.batfish.datamodel;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.datamodel.BgpAdvertisement.UNSET_LOCAL_PREFERENCE;
import static org.batfish.datamodel.BgpAdvertisement.UNSET_ORIGINATOR_IP;
import static org.batfish.datamodel.BgpAdvertisement.UNSET_WEIGHT;
import static org.batfish.datamodel.BgpAdvertisement.nullSafeCompareTo;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpAdvertisement.BgpAdvertisementType;
import org.batfish.datamodel.bgp.community.StandardCommunity;
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

  /** Test that optional fields are handled properly in the json creator */
  @Test
  public void testCreateMissingValues() throws JsonProcessingException {
    String input =
        readResource("org/batfish/datamodel/bgp-advertisement-missing-fields.json", UTF_8);
    BgpAdvertisement advertisement =
        BatfishObjectMapper.mapper().readValue(input, BgpAdvertisement.class);

    assertThat(
        advertisement,
        equalTo(
            new BgpAdvertisement(
                BgpAdvertisementType.EBGP_SENT,
                Prefix.parse("4.0.0.0/8"),
                Ip.parse("10.14.22.4"),
                null,
                DEFAULT_VRF_NAME,
                Ip.parse("10.14.22.4"),
                "as1border2",
                "default",
                Ip.parse("10.14.22.1"),
                RoutingProtocol.AGGREGATE,
                OriginType.EGP,
                UNSET_LOCAL_PREFERENCE,
                0L,
                UNSET_ORIGINATOR_IP,
                AsPath.of(AsSet.of(1239)),
                ImmutableSortedSet.of(StandardCommunity.of(262145)),
                ImmutableSortedSet.of(),
                UNSET_WEIGHT)));
  }

  @Test
  public void testCompareTo() {
    BgpAdvertisement left =
        new BgpAdvertisement(
            BgpAdvertisementType.EBGP_SENT,
            Prefix.parse("4.0.0.0/8"),
            Ip.parse("10.14.22.4"),
            null,
            DEFAULT_VRF_NAME,
            Ip.parse("10.14.22.4"),
            "as1border2",
            "default",
            Ip.parse("10.14.22.1"),
            RoutingProtocol.AGGREGATE,
            OriginType.EGP,
            UNSET_LOCAL_PREFERENCE,
            0L,
            UNSET_ORIGINATOR_IP,
            AsPath.of(AsSet.of(1239)),
            ImmutableSortedSet.of(StandardCommunity.of(262145)),
            ImmutableSortedSet.of(),
            UNSET_WEIGHT);
    BgpAdvertisement right =
        new BgpAdvertisement(
            BgpAdvertisementType.EBGP_SENT,
            Prefix.parse("4.0.0.0/8"),
            Ip.parse("10.14.22.4"),
            null,
            DEFAULT_VRF_NAME,
            Ip.parse("10.14.22.4"),
            "as1border2",
            "default",
            Ip.parse("10.14.22.1"),
            RoutingProtocol.AGGREGATE,
            OriginType.EGP,
            UNSET_LOCAL_PREFERENCE,
            0L,
            UNSET_ORIGINATOR_IP,
            AsPath.of(AsSet.of(1239)),
            ImmutableSortedSet.of(StandardCommunity.of(262145)),
            ImmutableSortedSet.of(),
            UNSET_WEIGHT);
    assertThat(left.compareTo(right), equalTo(0));
  }
}
