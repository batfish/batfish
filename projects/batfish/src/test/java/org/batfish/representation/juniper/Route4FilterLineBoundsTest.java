package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterList;
import org.junit.Test;

public final class Route4FilterLineBoundsTest {

  @Test
  public void testHostLonger() {
    assertThat(
        new Route4FilterLineLonger(Prefix.parse("192.0.2.1/32")).toRouteFilterLines(), empty());
  }

  @Test
  public void testRangeBelowBase() {
    RouteFilterList filter =
        new RouteFilterList(
            "test",
            new Route4FilterLineLengthRange(Prefix.parse("192.0.2.0/26"), 23, 24)
                .toRouteFilterLines());
    assertFalse(filter.permits(Prefix.parse("192.0.2.0/23")));
    assertFalse(filter.permits(Prefix.parse("192.0.2.0/24")));
    assertFalse(filter.permits(Prefix.parse("192.0.2.0/26")));
  }

  @Test
  public void testRangeStraddlesBase() {
    RouteFilterList filter =
        new RouteFilterList(
            "test",
            new Route4FilterLineLengthRange(Prefix.parse("192.0.2.0/26"), 24, 28)
                .toRouteFilterLines());
    assertFalse(filter.permits(Prefix.parse("192.0.2.0/24")));
    assertFalse(filter.permits(Prefix.parse("192.0.2.0/25")));
    assertTrue(filter.permits(Prefix.parse("192.0.2.0/26")));
    assertTrue(filter.permits(Prefix.parse("192.0.2.0/28")));
    assertFalse(filter.permits(Prefix.parse("192.0.2.0/29")));
    assertFalse(filter.permits(Prefix.parse("192.0.2.64/26")));
  }

  @Test
  public void testEmptyFilterPreventsFallback() {
    RouteFilter filter = new RouteFilter();
    filter.insertLine(
        new Route4FilterLineOrLonger(Prefix.parse("192.0.2.0/24")), Route4FilterLine.class);
    filter.insertLine(
        new Route4FilterLineUpTo(Prefix.parse("192.0.2.64/26"), 24), Route4FilterLine.class);
    filter.insertLine(
        new Route4FilterLineExact(Prefix.parse("192.0.2.64/28")), Route4FilterLine.class);
    RouteFilterList converted = new RouteFilterList("test", filter.toRouteFilterLines());
    assertTrue(converted.permits(Prefix.parse("192.0.2.0/24")));
    assertTrue(converted.permits(Prefix.parse("192.0.2.128/26")));
    assertFalse(converted.permits(Prefix.parse("192.0.2.64/26")));
    assertFalse(converted.permits(Prefix.parse("192.0.2.64/27")));
    assertTrue(converted.permits(Prefix.parse("192.0.2.64/28")));
    assertFalse(converted.permits(Prefix.parse("192.0.2.64/29")));
  }

  @Test
  public void testEmptyFilterPreventsAddressMaskFallback() {
    RouteFilter filter = new RouteFilter();
    filter.insertLine(
        new Route4FilterLineAddressMask(Prefix.parse("192.0.2.0/26"), Ip.MAX),
        Route4FilterLine.class);
    filter.insertLine(
        new Route4FilterLineUpTo(Prefix.parse("192.0.2.64/26"), 24), Route4FilterLine.class);
    RouteFilterList converted = new RouteFilterList("test", filter.toRouteFilterLines());
    assertFalse(converted.permits(Prefix.parse("192.0.2.64/26")));
    assertTrue(converted.permits(Prefix.parse("192.0.2.128/26")));
  }
}
