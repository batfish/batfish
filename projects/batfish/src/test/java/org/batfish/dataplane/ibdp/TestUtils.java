package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasMetric;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasNextHopIp;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteDecoratorMatchers.hasProtocol;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.matchers.IsisRouteMatchers;
import org.hamcrest.Matchers;

public class TestUtils {
  public static <T extends AbstractRouteDecorator> void assertNoRoute(
      SortedMap<String, SortedMap<String, Set<T>>> routesByNode,
      String hostname,
      ConcreteInterfaceAddress address) {
    assertNoRoute(routesByNode, hostname, address.getPrefix());
  }

  public static <T extends AbstractRouteDecorator> void assertNoRoute(
      SortedMap<String, SortedMap<String, Set<T>>> routesByNode, String hostname, Prefix prefix) {
    assertThat(routesByNode, hasKey(hostname));
    SortedMap<String, Set<T>> routesByVrf = routesByNode.get(hostname);
    assertThat(routesByVrf, hasKey(Configuration.DEFAULT_VRF_NAME));
    Set<T> routes = routesByVrf.get(Configuration.DEFAULT_VRF_NAME);
    assertThat(routes, not(hasItem(hasPrefix(prefix))));
  }

  public static void assertRoute(
      SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routesByNode,
      RoutingProtocol protocol,
      String hostname,
      ConcreteInterfaceAddress address,
      long expectedCost) {
    assertRoute(routesByNode, protocol, hostname, address.getPrefix(), expectedCost);
  }

  public static void assertIsisRoute(
      SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routesByNode,
      RoutingProtocol protocol,
      String hostname,
      Prefix prefix,
      long expectedCost,
      Ip nextHopIp,
      boolean overload) {
    List<AbstractRoute> routesForPrefix = getRoutesForPrefix(routesByNode, hostname, prefix);
    assertThat(
        routesForPrefix,
        hasItem(
            IsisRouteMatchers.isisRouteWith(prefix, nextHopIp, expectedCost, overload, protocol)));
  }

  public static void assertRoute(
      SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routesByNode,
      RoutingProtocol protocol,
      String hostname,
      Prefix prefix,
      long expectedCost) {
    assertRoute(routesByNode, protocol, hostname, prefix, expectedCost, null);
  }

  public static void assertRoute(
      SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routesByNode,
      RoutingProtocol protocol,
      String hostname,
      Prefix prefix,
      long expectedCost,
      @Nullable Ip nextHopIp) {
    List<AbstractRoute> routesForPrefix = getRoutesForPrefix(routesByNode, hostname, prefix);
    assertThat(
        routesForPrefix,
        hasItem(
            allOf(
                hasMetric(expectedCost),
                hasProtocol(protocol),
                hasNextHopIp(nextHopIp == null ? Matchers.any(Ip.class) : equalTo(nextHopIp)))));
  }

  private static List<AbstractRoute> getRoutesForPrefix(
      SortedMap<String, SortedMap<String, Set<AbstractRoute>>> routesByNode,
      String hostname,
      Prefix prefix) {
    assertThat(routesByNode, hasKey(hostname));
    SortedMap<String, Set<AbstractRoute>> routesByVrf = routesByNode.get(hostname);
    assertThat(routesByVrf, hasKey(Configuration.DEFAULT_VRF_NAME));
    Set<AbstractRoute> routes = routesByVrf.get(Configuration.DEFAULT_VRF_NAME);
    assertThat(routes, hasItem(hasPrefix(prefix)));
    return routes.stream().filter(r -> r.getNetwork().equals(prefix)).collect(Collectors.toList());
  }

  public static Node makeF5Router(String hostname) {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setHostname(hostname)
            .setConfigurationFormat(ConfigurationFormat.F5_BIGIP_STRUCTURED)
            .build();
    nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(c).build();
    return new Node(c);
  }

  public static Node makeA10Router(String hostname) {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setHostname(hostname)
            .setConfigurationFormat(ConfigurationFormat.A10_ACOS)
            .build();
    nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(c).build();
    return new Node(c);
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

  /** Annotates route with {@link Configuration#DEFAULT_VRF_NAME} */
  public static <T extends AbstractRoute> AnnotatedRoute<T> annotateRoute(T r) {
    return new AnnotatedRoute<>(r, Configuration.DEFAULT_VRF_NAME);
  }
}
