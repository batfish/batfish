package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasMetric;
import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasProtocol;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;

public class TestUtils {
  public static void assertNoRoute(
      SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routesByNode,
      String hostname,
      InterfaceAddress address) {
    assertNoRoute(routesByNode, hostname, address.getPrefix());
  }

  public static void assertNoRoute(
      SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routesByNode,
      String hostname,
      Prefix prefix) {
    assertThat(routesByNode, hasKey(hostname));
    SortedMap<String, SortedSet<AbstractRoute>> routesByVrf = routesByNode.get(hostname);
    assertThat(routesByVrf, hasKey(Configuration.DEFAULT_VRF_NAME));
    SortedSet<AbstractRoute> routes = routesByVrf.get(Configuration.DEFAULT_VRF_NAME);
    assertThat(routes, not(hasItem(hasPrefix(prefix))));
  }

  public static void assertRoute(
      SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routesByNode,
      RoutingProtocol protocol,
      String hostname,
      InterfaceAddress address,
      long expectedCost) {
    assertRoute(routesByNode, protocol, hostname, address.getPrefix(), expectedCost);
  }

  public static void assertRoute(
      SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routesByNode,
      RoutingProtocol protocol,
      String hostname,
      Prefix prefix,
      long expectedCost) {
    assertThat(routesByNode, hasKey(hostname));
    SortedMap<String, SortedSet<AbstractRoute>> routesByVrf = routesByNode.get(hostname);
    assertThat(routesByVrf, hasKey(Configuration.DEFAULT_VRF_NAME));
    SortedSet<AbstractRoute> routes = routesByVrf.get(Configuration.DEFAULT_VRF_NAME);
    assertThat(routes, hasItem(hasPrefix(prefix)));
    AbstractRoute route =
        routes.stream().filter(r -> r.getNetwork().equals(prefix)).findAny().get();
    assertThat(route, hasMetric(expectedCost));
    assertThat(route, hasProtocol(protocol));
  }

  public static Node makeIosRouter(String hostname) {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setHostname(hostname)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(c).build();
    return new Node(c);
  }
}
