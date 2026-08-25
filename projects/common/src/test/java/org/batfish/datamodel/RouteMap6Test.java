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

  @Test
  public void testMatchSourceProtocolAndSerialization() {

    RouteMap6 routeMap =
        new RouteMap6(
            ImmutableMap.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    null,
                    RoutingProtocol.OSPF3,
                    40L,
                    OspfMetricType.E1,
                    101L),
                20L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    null,
                    RoutingProtocol.STATIC,
                    50L,
                    null,
                    202L),
                30L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    null,
                    RoutingProtocol.CONNECTED,
                    60L,
                    null,
                    303L),
                40L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    null,
                    RoutingProtocol.BGP,
                    70L,
                    null,
                    404L)));

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
            .get(10L)
            .getMatchSourceProtocol(),
        equalTo(
            RoutingProtocol.OSPF3));

    assertThat(
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:710::/64"),
                25L,
                Route.UNSET_ROUTE_TAG,
                OspfMetricType.E2,
                RoutingProtocol.OSPF3)
            .orElseThrow()
            .getMetric(),
        equalTo(
            40L));

    assertThat(
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:711::/64"),
                25L,
                Route.UNSET_ROUTE_TAG,
                OspfMetricType.E2,
                RoutingProtocol.STATIC)
            .orElseThrow()
            .getTag(),
        equalTo(
            202L));

    assertThat(
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:712::/64"),
                25L,
                Route.UNSET_ROUTE_TAG,
                OspfMetricType.E2,
                RoutingProtocol.CONNECTED)
            .orElseThrow()
            .getMetric(),
        equalTo(
            60L));

    assertThat(
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:713::/64"),
                25L,
                Route.UNSET_ROUTE_TAG,
                OspfMetricType.E2,
                RoutingProtocol.BGP)
            .orElseThrow()
            .getMetric(),
        equalTo(
            70L));

    /*
     * Existing process overloads do not invent a source protocol.
     * Therefore a route-map containing only source-protocol clauses has
     * no matching sequence when the caller supplies no provenance.
     */
    assertThat(
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:714::/64"),
                25L,
                Route.UNSET_ROUTE_TAG,
                OspfMetricType.E2)
            .isEmpty(),
        equalTo(
            true));
  }

  @Test
  public void testMatchOspfExternalRouteTypeAndSerialization() {

    RouteMap6 routeMap =
        new RouteMap6(
            ImmutableMap.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    null,
                    null,
                    RoutingProtocol.OSPF3,
                    OspfMetricType.E1,
                    40L,
                    OspfMetricType.E1,
                    101L)));

    RouteMap6 clone =
        BatfishObjectMapper.clone(
            routeMap,
            RouteMap6.class);

    assertThat(
        clone,
        equalTo(
            routeMap));

    assertThat(
        clone
            .getEntries()
            .get(10L)
            .getMatchSourceProtocol(),
        equalTo(
            RoutingProtocol.OSPF3));

    assertThat(
        clone
            .getEntries()
            .get(10L)
            .getMatchOspfMetricType(),
        equalTo(
            OspfMetricType.E1));

    /*
     * Actual OSPF external type-1 source matches.
     */
    RouteMap6.Result e1 =
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:720::/64"),
                25L,
                Route.UNSET_ROUTE_TAG,
                OspfMetricType.E2,
                RoutingProtocol.OSPF3,
                OspfMetricType.E1)
            .orElseThrow();

    assertThat(
        e1.getMetric(),
        equalTo(
            40L));

    assertThat(
        e1.getOspfMetricType(),
        equalTo(
            OspfMetricType.E1));

    assertThat(
        e1.getTag(),
        equalTo(
            101L));

    /*
     * OSPF external type-2 does not match external type-1.
     */
    assertThat(
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:721::/64"),
                25L,
                Route.UNSET_ROUTE_TAG,
                OspfMetricType.E2,
                RoutingProtocol.OSPF3,
                OspfMetricType.E2)
            .isEmpty(),
        equalTo(
            true));

    /*
     * An OSPF intra/inter-area source has no external metric type.
     */
    assertThat(
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:722::/64"),
                25L,
                Route.UNSET_ROUTE_TAG,
                OspfMetricType.E2,
                RoutingProtocol.OSPF3,
                null)
            .isEmpty(),
        equalTo(
            true));

    /*
     * Critically: a STATIC route that would be originated as E1 does not
     * become an OSPF external type-1 SOURCE merely because the output
     * redistribution metric type is E1.
     */
    assertThat(
        clone
            .process(
                Prefix6.parse(
                    "2001:db8:723::/64"),
                25L,
                Route.UNSET_ROUTE_TAG,
                OspfMetricType.E1,
                RoutingProtocol.STATIC,
                null)
            .isEmpty(),
        equalTo(
            true));
  }

}
