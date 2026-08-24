package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.junit.Test;

/** Tests for {@link RouteMap6}. */
public final class RouteMap6Test {

  @Test
  public void testPrefixListAndRouteMapFirstMatch() {
    PrefixList6 prefixList =
        new PrefixList6(
            List.of(
                new PrefixList6.Line(
                    LineAction.DENY,
                    Prefix6.parse(
                        "2001:db8:100:1::/64"),
                    new SubRange(
                        64, 64)),
                new PrefixList6.Line(
                    LineAction.PERMIT,
                    Prefix6.parse(
                        "2001:db8:100::/48"),
                    new SubRange(
                        64, 64))));

    RouteMap6 routeMap =
        new RouteMap6(
            ImmutableMap.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    prefixList,
                    31L,
                    101L)));

    assertThat(
        routeMap
            .process(
                Prefix6.parse(
                    "2001:db8:100:1::/64"),
                25L,
                Route.UNSET_ROUTE_TAG)
            .isEmpty(),
        equalTo(true));

    RouteMap6.Result result =
        routeMap
            .process(
                Prefix6.parse(
                    "2001:db8:100:2::/64"),
                25L,
                Route.UNSET_ROUTE_TAG)
            .orElseThrow();

    assertThat(
        result.getMetric(),
        equalTo(31L));

    assertThat(
        result.getTag(),
        equalTo(101L));

    /*
     * No prefix-list line matches, so the route-map sequence does
     * not match. No later route-map sequence exists: implicit deny.
     */
    assertThat(
        routeMap
            .process(
                Prefix6.parse(
                    "2001:db8:200::/64"),
                25L,
                Route.UNSET_ROUTE_TAG)
            .isEmpty(),
        equalTo(true));
  }

  @Test
  public void testRouteMapDenyThenPermitAndSerialization() {
    PrefixList6 deniedPrefixes =
        new PrefixList6(
            List.of(
                new PrefixList6.Line(
                    LineAction.PERMIT,
                    Prefix6.parse(
                        "2001:db8:400::/48"),
                    new SubRange(
                        48, 128))));

    PrefixList6 allPrefixes =
        new PrefixList6(
            List.of(
                new PrefixList6.Line(
                    LineAction.PERMIT,
                    Prefix6.ZERO,
                    new SubRange(
                        0, 128))));

    RouteMap6 routeMap =
        new RouteMap6(
            ImmutableMap.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.DENY,
                    deniedPrefixes,
                    null,
                    null),
                20L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    allPrefixes,
                    41L,
                    202L)));

    RouteMap6 clone =
        BatfishObjectMapper.clone(
            routeMap,
            RouteMap6.class);

    assertThat(
        clone,
        equalTo(routeMap));

    assertThat(
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:400::/64"),
                25L,
                999L)
            .isEmpty(),
        equalTo(true));

    RouteMap6.Result permitted =
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:300::/64"),
                25L,
                999L)
            .orElseThrow();

    assertThat(
        permitted.getMetric(),
        equalTo(41L));

    assertThat(
        permitted.getTag(),
        equalTo(202L));
  }

  @Test
  public void testNoMatchAndUndefinedPrefixListSemantics() {
    RouteMap6 matchAll =
        new RouteMap6(
            ImmutableMap.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    null,
                    null)));

    assertThat(
        matchAll
            .process(
                Prefix6.parse(
                    "2001:db8::/32"),
                25L,
                7L)
            .isPresent(),
        equalTo(true));

    RouteMap6 undefinedPrefixList =
        new RouteMap6(
            ImmutableMap.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    PrefixList6.denyAll(),
                    null,
                    null)));

    assertThat(
        undefinedPrefixList
            .process(
                Prefix6.parse(
                    "2001:db8::/32"),
                25L,
                7L)
            .isEmpty(),
        equalTo(true));
  }
  @Test
  public void testOspfMetricTypeTransformation() {
    RouteMap6 routeMap =
        new RouteMap6(
            ImmutableMap.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    null,
                    OspfMetricType.E1,
                    null)));

    RouteMap6.Result result =
        routeMap
            .process(
                Prefix6.parse(
                    "2001:db8:500::/64"),
                25L,
                7L)
            .orElseThrow();

    assertThat(
        result.getMetric(),
        equalTo(25L));

    assertThat(
        result.getTag(),
        equalTo(7L));

    assertThat(
        result.getOspfMetricType(),
        equalTo(OspfMetricType.E1));

    RouteMap6 clone =
        BatfishObjectMapper.clone(
            routeMap,
            RouteMap6.class);

    assertThat(
        clone,
        equalTo(routeMap));

    assertThat(
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:500::/64"),
                25L,
                7L)
            .orElseThrow()
            .getOspfMetricType(),
        equalTo(OspfMetricType.E1));
  }

  @Test
  public void testMatchTagAndSerialization() {

    RouteMap6 routeMap =
        new RouteMap6(
            ImmutableMap.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.DENY,
                    null,
                    100L,
                    null,
                    null,
                    null),
                20L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    200L,
                    44L,
                    OspfMetricType.E1,
                    999L),
                30L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    null,
                    null,
                    null,
                    null)));

    RouteMap6 clone =
        BatfishObjectMapper.clone(
            routeMap,
            RouteMap6.class);

    assertThat(
        clone,
        equalTo(routeMap));

    assertThat(
        clone
            .getEntries()
            .get(20L)
            .getMatchTag(),
        equalTo(200L));

    /*
     * Sequence 10 matches tag 100 and denies.
     */
    assertThat(
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:100::/64"),
                25L,
                100L)
            .isEmpty(),
        equalTo(true));

    /*
     * Sequence 10 does not match tag 200.
     * Sequence 20 matches and transforms the route.
     */
    RouteMap6.Result matched =
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:200::/64"),
                25L,
                200L)
            .orElseThrow();

    assertThat(
        matched.getMetric(),
        equalTo(44L));

    assertThat(
        matched.getOspfMetricType(),
        equalTo(OspfMetricType.E1));

    assertThat(
        matched.getTag(),
        equalTo(999L));

    /*
     * Neither tagged clause matches tag 300.
     * Sequence 30 has no match conditions and therefore permits while
     * preserving the original route attributes.
     */
    RouteMap6.Result fallback =
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:300::/64"),
                25L,
                300L)
            .orElseThrow();

    assertThat(
        fallback.getMetric(),
        equalTo(25L));

    assertThat(
        fallback.getOspfMetricType(),
        equalTo(OspfMetricType.E2));

    assertThat(
        fallback.getTag(),
        equalTo(300L));

    /*
     * An untagged route also skips the tagged clauses and reaches the
     * unconditional fallback.
     */
    RouteMap6.Result untagged =
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:400::/64"),
                25L,
                Route.UNSET_ROUTE_TAG)
            .orElseThrow();

    assertThat(
        untagged.getTag(),
        equalTo(
            Route.UNSET_ROUTE_TAG));
  }

}
