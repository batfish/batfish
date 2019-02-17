package org.batfish.dataplane.ibdp;

import static org.batfish.datamodel.matchers.HasAbstractRouteMatchers.hasMetric;
import static org.batfish.datamodel.matchers.HasAbstractRouteMatchers.hasNextHopIp;
import static org.batfish.datamodel.matchers.HasAbstractRouteMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.HasAbstractRouteMatchers.hasProtocol;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.HasAbstractRoute;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.matchers.IsisRouteMatchers;
import org.hamcrest.Matchers;

public class TestUtils {
  public static <T extends HasAbstractRoute> void assertNoRoute(
      SortedMap<String, SortedMap<String, SortedSet<T>>> routesByNode,
      String hostname,
      InterfaceAddress address) {
    assertNoRoute(routesByNode, hostname, address.getPrefix());
  }

  public static <T extends HasAbstractRoute> void assertNoRoute(
      SortedMap<String, SortedMap<String, SortedSet<T>>> routesByNode,
      String hostname,
      Prefix prefix) {
    assertThat(routesByNode, hasKey(hostname));
    SortedMap<String, SortedSet<T>> routesByVrf = routesByNode.get(hostname);
    assertThat(routesByVrf, hasKey(Configuration.DEFAULT_VRF_NAME));
    SortedSet<T> routes = routesByVrf.get(Configuration.DEFAULT_VRF_NAME);
    assertThat(routes, not(hasItem(hasPrefix(prefix))));
  }

  public static <T extends HasAbstractRoute> void assertRoute(
      SortedMap<String, SortedMap<String, SortedSet<T>>> routesByNode,
      RoutingProtocol protocol,
      String hostname,
      InterfaceAddress address,
      long expectedCost) {
    assertRoute(routesByNode, protocol, hostname, address.getPrefix(), expectedCost);
  }

  public static <T extends HasAbstractRoute> void assertIsisRoute(
      SortedMap<String, SortedMap<String, SortedSet<T>>> routesByNode,
      RoutingProtocol protocol,
      String hostname,
      Prefix prefix,
      long expectedCost,
      Ip nextHopIp,
      boolean overload) {
    Set<AbstractRoute> routesForPrefix =
        unannotateRoutes(getRoutesForPrefix(routesByNode, hostname, prefix));
    assertThat(
        routesForPrefix,
        hasItem(
            IsisRouteMatchers.isisRouteWith(prefix, nextHopIp, expectedCost, overload, protocol)));
  }

  public static <T extends HasAbstractRoute> void assertRoute(
      SortedMap<String, SortedMap<String, SortedSet<T>>> routesByNode,
      RoutingProtocol protocol,
      String hostname,
      Prefix prefix,
      long expectedCost) {
    assertRoute(routesByNode, protocol, hostname, prefix, expectedCost, null);
  }

  public static <T extends HasAbstractRoute> void assertRoute(
      SortedMap<String, SortedMap<String, SortedSet<T>>> routesByNode,
      RoutingProtocol protocol,
      String hostname,
      Prefix prefix,
      long expectedCost,
      @Nullable Ip nextHopIp) {
    List<T> routesForPrefix = getRoutesForPrefix(routesByNode, hostname, prefix);
    assertThat(
        routesForPrefix,
        hasItem(
            allOf(
                hasMetric(expectedCost),
                hasProtocol(protocol),
                hasNextHopIp(nextHopIp == null ? Matchers.any(Ip.class) : equalTo(nextHopIp)))));
  }

  private static <T extends HasAbstractRoute> List<T> getRoutesForPrefix(
      SortedMap<String, SortedMap<String, SortedSet<T>>> routesByNode,
      String hostname,
      Prefix prefix) {
    assertThat(routesByNode, hasKey(hostname));
    SortedMap<String, SortedSet<T>> routesByVrf = routesByNode.get(hostname);
    assertThat(routesByVrf, hasKey(Configuration.DEFAULT_VRF_NAME));
    SortedSet<T> routes = routesByVrf.get(Configuration.DEFAULT_VRF_NAME);
    assertThat(routes, hasItem(hasPrefix(prefix)));
    return routes.stream().filter(r -> r.getNetwork().equals(prefix)).collect(Collectors.toList());
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

  public static Set<AbstractRoute> unannotateRoutes(
      Collection<? extends HasAbstractRoute> annotated) {
    return annotated.stream().map(HasAbstractRoute::getAbstractRoute).collect(Collectors.toSet());
  }
}
