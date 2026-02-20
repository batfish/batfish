package org.batfish.representation.fortios;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.SortedSet;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.junit.Test;

public class FortiosRouteConversionsTest {
  @Test
  public void testConvertStaticRoutes_enabledWithGateway() {
    StaticRoute route = new StaticRoute("1");
    route.setStatus(StaticRoute.Status.ENABLE);
    route.setDevice("port1");
    route.setDst(Prefix.parse("10.0.0.0/24"));
    route.setGateway(Ip.parse("192.168.1.1"));

    List<StaticRoute> routes = ImmutableList.of(route);
    SortedSet<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoutes(routes);

    assertThat(result.size(), equalTo(1));
    org.batfish.datamodel.StaticRoute converted = result.first();
    assertThat(converted.getNetwork(), equalTo(Prefix.parse("10.0.0.0/24")));
    assertThat(converted.getNextHopIp(), equalTo(Ip.parse("192.168.1.1")));
    assertThat(converted.getAdministrativeCost(), equalTo(10L)); // DEFAULT_DISTANCE
  }

  @Test
  public void testConvertStaticRoutes_enabledWithInterfaceOnly() {
    StaticRoute route = new StaticRoute("2");
    route.setStatus(StaticRoute.Status.ENABLE);
    route.setDevice("port2");
    route.setDst(Prefix.parse("10.0.1.0/24"));
    route.setGateway(null);

    List<StaticRoute> routes = ImmutableList.of(route);
    SortedSet<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoutes(routes);

    assertThat(result.size(), equalTo(1));
    org.batfish.datamodel.StaticRoute converted = result.first();
    assertThat(converted.getNetwork(), equalTo(Prefix.parse("10.0.1.0/24")));
    assertThat(converted.getNextHopInterface(), equalTo("port2"));
  }

  @Test
  public void testConvertStaticRoutes_disabledRoute() {
    StaticRoute route = new StaticRoute("3");
    route.setStatus(StaticRoute.Status.DISABLE);
    route.setDevice("port1");
    route.setDst(Prefix.parse("10.0.2.0/24"));

    List<StaticRoute> routes = ImmutableList.of(route);
    SortedSet<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoutes(routes);

    // Disabled route should not be converted
    assertThat(result.size(), equalTo(0));
  }

  @Test
  public void testConvertStaticRoutes_withCustomDistance() {
    StaticRoute route = new StaticRoute("4");
    route.setStatus(StaticRoute.Status.ENABLE);
    route.setDevice("port1");
    route.setDst(Prefix.parse("10.0.3.0/24"));
    route.setGateway(Ip.parse("192.168.1.1"));
    route.setDistance(50);

    List<StaticRoute> routes = ImmutableList.of(route);
    SortedSet<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoutes(routes);

    assertThat(result.size(), equalTo(1));
    org.batfish.datamodel.StaticRoute converted = result.first();
    assertThat(converted.getAdministrativeCost(), equalTo(50L));
  }

  @Test
  public void testConvertStaticRoutes_sdwanRoute() {
    StaticRoute route = new StaticRoute("5");
    route.setStatus(StaticRoute.Status.ENABLE);
    route.setDevice("port1");
    route.setDst(Prefix.parse("10.0.4.0/24"));
    route.setGateway(Ip.parse("192.168.1.1"));
    route.setSdwanEnabled(true);

    List<StaticRoute> routes = ImmutableList.of(route);
    SortedSet<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoutes(routes);

    assertThat(result.size(), equalTo(1));
    org.batfish.datamodel.StaticRoute converted = result.first();
    assertThat(converted.getAdministrativeCost(), equalTo(1L)); // DEFAULT_DISTANCE_SDWAN
  }

  @Test
  public void testConvertStaticRoutes_multipleRoutes() {
    StaticRoute route1 = new StaticRoute("1");
    route1.setStatus(StaticRoute.Status.ENABLE);
    route1.setDevice("port1");
    route1.setDst(Prefix.parse("10.0.0.0/24"));
    route1.setGateway(Ip.parse("192.168.1.1"));

    StaticRoute route2 = new StaticRoute("2");
    route2.setStatus(StaticRoute.Status.ENABLE);
    route2.setDevice("port2");
    route2.setDst(Prefix.parse("10.0.1.0/24"));
    route2.setGateway(Ip.parse("192.168.2.1"));

    List<StaticRoute> routes = ImmutableList.of(route1, route2);
    SortedSet<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoutes(routes);

    assertThat(result.size(), equalTo(2));
  }

  @Test
  public void testConvertStaticRoutes_filtersDisabled() {
    StaticRoute route1 = new StaticRoute("1");
    route1.setStatus(StaticRoute.Status.ENABLE);
    route1.setDevice("port1");
    route1.setDst(Prefix.parse("10.0.0.0/24"));
    route1.setGateway(Ip.parse("192.168.1.1"));

    StaticRoute route2 = new StaticRoute("2");
    route2.setStatus(StaticRoute.Status.DISABLE);
    route2.setDevice("port2");
    route2.setDst(Prefix.parse("10.0.1.0/24"));

    List<StaticRoute> routes = ImmutableList.of(route1, route2);
    SortedSet<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoutes(routes);

    // Only enabled route should be converted
    assertThat(result.size(), equalTo(1));
  }

  @Test
  public void testConvertStaticRoutes_empty() {
    SortedSet<org.batfish.datamodel.StaticRoute> result =
        FortiosRouteConversions.convertStaticRoutes(ImmutableList.of());

    assertThat(result.size(), equalTo(0));
  }
}
