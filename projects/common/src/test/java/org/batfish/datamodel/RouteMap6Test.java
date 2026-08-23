package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests for {@link RouteMap6}. */
public final class RouteMap6Test {

  @Test
  public void testPrefixListAndRouteMapFirstMatch() {
    RouteMap6 routeMap =
        new RouteMap6(
            ImmutableMap.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    List.of(
                        new RouteMap6.PrefixListLine(
                            LineAction.DENY,
                            Prefix6.parse(
                                "2001:db8:100:1::/64"),
                            new SubRange(
                                64, 64)),
                        new RouteMap6.PrefixListLine(
                            LineAction.PERMIT,
                            Prefix6.parse(
                                "2001:db8:100::/48"),
                            new SubRange(
                                64, 64))),
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

    // No route-map sequence matched: implicit deny.
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
    RouteMap6 routeMap =
        new RouteMap6(
            ImmutableMap.of(
                10L,
                new RouteMap6.Entry(
                    LineAction.DENY,
                    List.of(
                        new RouteMap6.PrefixListLine(
                            LineAction.PERMIT,
                            Prefix6.parse(
                                "2001:db8:400::/48"),
                            new SubRange(
                                48, 128))),
                    null,
                    null),
                20L,
                new RouteMap6.Entry(
                    LineAction.PERMIT,
                    List.of(
                        new RouteMap6.PrefixListLine(
                            LineAction.PERMIT,
                            Prefix6.ZERO,
                            new SubRange(
                                0, 128))),
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
}
