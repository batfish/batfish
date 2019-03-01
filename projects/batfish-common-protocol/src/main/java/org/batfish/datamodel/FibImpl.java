package org.batfish.datamodel;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;

@ParametersAreNonnullByDefault
public final class FibImpl implements Fib {

  /** Helps perform recursive route resolution and maintain the route chain */
  private static final class ResolutionTreeNode {
    private final @Nonnull AbstractRoute _route;
    private final @Nullable Ip _finalNextHopIp;

    private final @Nonnull List<ResolutionTreeNode> _children;

    /** Use static factories for sanity */
    private ResolutionTreeNode(
        AbstractRoute route, @Nullable Ip finalNextHopIp, List<ResolutionTreeNode> children) {
      _route = route;
      _finalNextHopIp = finalNextHopIp;
      _children = children;
    }

    static ResolutionTreeNode withParent(
        AbstractRoute route, @Nullable ResolutionTreeNode parent, @Nullable Ip finalNextHopIp) {
      ResolutionTreeNode child = new ResolutionTreeNode(route, finalNextHopIp, new LinkedList<>());
      if (parent != null) {
        parent.addChild(child);
      }
      return child;
    }

    static ResolutionTreeNode root(@Nonnull AbstractRoute route) {
      return new ResolutionTreeNode(route, null, new LinkedList<>());
    }

    @Nonnull
    public AbstractRoute getRoute() {
      return _route;
    }

    @Nullable
    public Ip getFinalNextHopIp() {
      return _finalNextHopIp;
    }

    @Nonnull
    public List<ResolutionTreeNode> getChildren() {
      return _children;
    }

    private void addChild(ResolutionTreeNode child) {
      _children.add(child);
    }
  }

  private static final int MAX_DEPTH = 10;
  private static final long serialVersionUID = 1L;

  /** This trie is the source of truth for all resolved FIB routes */
  @Nonnull private final PrefixTrieMultiMap<FibEntry> _root;

  private transient Supplier<Map<AbstractRoute, Set<FibEntry>>> _entries;
  private transient Supplier<Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>>
      _nextHopInterfaces;

  public FibImpl(@Nonnull GenericRib<? extends AbstractRouteDecorator> rib) {
    _root = new PrefixTrieMultiMap<>(Prefix.ZERO);
    rib.getRoutes()
        .forEach(
            r -> {
              Set<FibEntry> s = resolveRoute(rib, r);
              _root.putAll(r.getNetwork(), s);
            });
    initSuppliers();
  }

  private void initSuppliers() {
    _entries = Suppliers.memoize(this::computeEntries);
    _nextHopInterfaces = Suppliers.memoize(this::computeNextHopInterfaces);
  }

  private Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>> computeNextHopInterfaces() {
    return _root.getAllElements().stream()
        .collect(
            Collectors.groupingBy(
                FibEntry::getTopLevelRoute,
                Collectors.groupingBy(
                    FibEntry::getInterfaceName,
                    Collectors.groupingBy(
                        FibEntry::getArpIP,
                        Collectors.mapping(FibEntry::getResolvedToRoute, Collectors.toSet())))));
  }

  private Map<AbstractRoute, Set<FibEntry>> computeEntries() {
    return _root.getAllElements().stream()
        .collect(Collectors.groupingBy(FibEntry::getTopLevelRoute, Collectors.toSet()));
  }

  @Nonnull
  public Map<AbstractRoute, Set<FibEntry>> getEntries() {
    return _entries.get();
  }

  /**
   * Attempt to resolve a RIB route down to an interface route.
   *
   * @param rib {@link GenericRib} for which to do the resolution.
   * @param route {@link AbstractRoute} with a next hop IP to be resolved.
   * @return A map (interface name -&gt; last hop IP -&gt; last taken route) for
   * @throws BatfishException if resolution depth is exceeded (high likelihood of a routing loop) OR
   *     an invalid route in the RIB has been encountered.
   */
  @VisibleForTesting
  Set<FibEntry> resolveRoute(
      GenericRib<? extends AbstractRouteDecorator> rib, AbstractRoute route) {
    ResolutionTreeNode resolutionRoot = ResolutionTreeNode.root(route);
    buildResolutionTree(
        rib,
        route,
        Route.UNSET_ROUTE_NEXT_HOP_IP,
        new HashSet<>(),
        0,
        Prefix.MAX_PREFIX_LENGTH,
        null,
        resolutionRoot);
    Builder<FibEntry> collector = ImmutableSet.builder();
    collectEntries(resolutionRoot, new Stack<>(), collector);
    return collector.build();
  }

  private void collectEntries(
      ResolutionTreeNode node,
      Stack<AbstractRoute> stack,
      ImmutableCollection.Builder<FibEntry> entriesBuilder) {
    if (node.getChildren().isEmpty() && node.getFinalNextHopIp() != null) {
      entriesBuilder.add(
          new FibEntry(
              node.getFinalNextHopIp(),
              node.getRoute().getNextHopInterface(),
              ImmutableList.copyOf(stack)));
      return;
    }
    stack.push(node.getRoute());
    for (ResolutionTreeNode child : node.getChildren()) {
      collectEntries(child, stack, entriesBuilder);
    }
    stack.pop();
  }

  /**
   * Tail-recursive method to build a route resolution tree. Each top-level route is mapped to a
   * number of leaf {@link ResolutionTreeNode}. Leaf nodes must contain non-null {@link
   * ResolutionTreeNode#_finalNextHopIp}
   */
  private void buildResolutionTree(
      GenericRib<? extends AbstractRouteDecorator> rib,
      AbstractRoute route,
      Ip mostRecentNextHopIp,
      Set<Prefix> seenNetworks,
      int depth,
      int maxPrefixLength,
      @Nullable AbstractRoute parentRoute,
      ResolutionTreeNode treeNode) {
    Prefix network = route.getNetwork();
    if (seenNetworks.contains(network)) {
      // Don't enter a resolution loop
      return;
    }
    Set<Prefix> newSeenNetworks = new HashSet<>(seenNetworks);
    newSeenNetworks.add(network);
    if (depth > MAX_DEPTH) {
      // TODO: Declare this a loop using some warning mechanism
      // https://github.com/batfish/batfish/issues/1469
      return;
    }

    // For non-forwarding routes, try to find a less specific route
    if (route.getNonForwarding()) {
      if (parentRoute == null) {
        return;
      } else {
        seenNetworks.remove(parentRoute.getNetwork());
        buildResolutionTree(
            rib,
            parentRoute,
            mostRecentNextHopIp,
            seenNetworks,
            depth + 1,
            maxPrefixLength - 1,
            null,
            treeNode);
        return;
      }
    }

    /* For BGP next-hop-discard routes, ignore next-hop-ip and exit early */
    if (route instanceof BgpRoute && ((BgpRoute) route).getDiscard()) {
      ResolutionTreeNode.withParent(route, treeNode, Route.UNSET_ROUTE_NEXT_HOP_IP);
      return;
    }

    if (!Route.UNSET_NEXT_HOP_INTERFACE.equals(route.getNextHopInterface())) {
      Ip finalNextHopIp =
          route.getNextHopIp().equals(Route.UNSET_ROUTE_NEXT_HOP_IP)
              ? mostRecentNextHopIp
              : route.getNextHopIp();
      ResolutionTreeNode.withParent(route, treeNode, finalNextHopIp);
    } else {
      Ip nextHopIp = route.getNextHopIp();
      if (nextHopIp.equals(Route.UNSET_ROUTE_NEXT_HOP_IP)) {
        // TODO: Declare this using some warning mechanism
        // https://github.com/batfish/batfish/issues/1469
        return;
      }
      Set<? extends AbstractRouteDecorator> nextHopLongestPrefixMatchRoutes =
          rib.longestPrefixMatch(nextHopIp, maxPrefixLength);

      /* Filter out any non-forwarding routes from the matches */
      Set<AbstractRoute> forwardingRoutes =
          nextHopLongestPrefixMatchRoutes.stream()
              .map(AbstractRouteDecorator::getAbstractRoute)
              .filter(r -> !r.getNonForwarding())
              .collect(ImmutableSet.toImmutableSet());

      if (forwardingRoutes.isEmpty()) {
        // Re-resolve *this route* with a less specific prefix match
        seenNetworks.remove(route.getNetwork());
        buildResolutionTree(
            rib,
            route,
            mostRecentNextHopIp,
            seenNetworks,
            depth + 1,
            maxPrefixLength - 1,
            parentRoute,
            treeNode);
      } else {
        // We have at least one valid longest-prefix match
        for (AbstractRoute nextHopLongestPrefixMatchRoute : forwardingRoutes) {
          buildResolutionTree(
              rib,
              nextHopLongestPrefixMatchRoute,
              nextHopIp,
              newSeenNetworks,
              depth + 1,
              Prefix.MAX_PREFIX_LENGTH,
              route,
              ResolutionTreeNode.withParent(nextHopLongestPrefixMatchRoute, treeNode, null));
        }
      }
    }
  }

  /** Mapping: route -&gt; nextHopInterface -&gt; resolved nextHopIp -&gt; interfaceRoutes */
  @Override
  public @Nonnull Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>
      getNextHopInterfaces() {
    return _nextHopInterfaces.get();
  }

  @Override
  @Deprecated
  public @Nonnull Set<String> getNextHopInterfaces(Ip ip) {
    return get(ip).stream().map(FibEntry::getInterfaceName).collect(ImmutableSet.toImmutableSet());
  }

  @Nonnull
  @Override
  public Set<FibEntry> get(Ip ip) {
    return _root.longestPrefixMatch(ip);
  }

  @Override
  @Deprecated
  public @Nonnull Map<AbstractRoute, Map<String, Map<Ip, Set<AbstractRoute>>>>
      getNextHopInterfacesByRoute(Ip dstIp) {
    return get(dstIp).stream()
        .collect(
            Collectors.groupingBy(
                FibEntry::getTopLevelRoute,
                Collectors.groupingBy(
                    FibEntry::getInterfaceName,
                    Collectors.groupingBy(
                        FibEntry::getArpIP,
                        Collectors.mapping(FibEntry::getResolvedToRoute, Collectors.toSet())))));
  }

  @Override
  public @Nonnull Map<String, Set<AbstractRoute>> getRoutesByNextHopInterface() {
    Map<String, ImmutableSet.Builder<AbstractRoute>> routesByNextHopInterface = new HashMap<>();
    getNextHopInterfaces()
        .forEach(
            (route, nextHopInterfaceMap) ->
                nextHopInterfaceMap
                    .keySet()
                    .forEach(
                        nextHopInterface ->
                            routesByNextHopInterface
                                .computeIfAbsent(nextHopInterface, n -> ImmutableSet.builder())
                                .add(route)));
    return routesByNextHopInterface.entrySet().stream()
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey /* interfaceName */,
                routesByNextHopInterfaceEntry -> routesByNextHopInterfaceEntry.getValue().build()));
  }

  private void readObject(java.io.ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    initSuppliers();
  }
}
