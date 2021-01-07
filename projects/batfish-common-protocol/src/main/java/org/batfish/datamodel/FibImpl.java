package org.batfish.datamodel;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.PrefixTrieMultiMap.FoldOperator;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVisitor;
import org.batfish.datamodel.route.nh.NextHopVrf;

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

  /** This trie is the source of truth for all resolved FIB routes */
  @Nonnull private final PrefixTrieMultiMap<FibEntry> _root;

  private transient Supplier<Set<FibEntry>> _entries;

  public FibImpl(@Nonnull GenericRib<? extends AbstractRouteDecorator> rib) {
    _root = new PrefixTrieMultiMap<>(Prefix.ZERO);
    rib.getTypedRoutes()
        .forEach(
            r -> {
              Set<FibEntry> s = resolveRoute(rib, r.getAbstractRoute());
              _root.putAll(r.getNetwork(), s);
            });
    initSuppliers();
  }

  private void initSuppliers() {
    _entries = Suppliers.memoize(this::computeEntries);
  }

  private Set<FibEntry> computeEntries() {
    return _root.getAllElements();
  }

  @Nonnull
  @Override
  public Set<FibEntry> allEntries() {
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
    AbstractRoute route = node.getRoute();
    if (node.getChildren().isEmpty() && node.getFinalNextHopIp() != null) {
      FibAction fibAction =
          new NextHopVisitor<FibAction>() {

            @Override
            public FibAction visitNextHopIp(NextHopIp nextHopIp) {
              throw new IllegalStateException(
                  String.format("FIB resolution failed to reach an interface route for %s", route));
            }

            @Override
            public FibAction visitNextHopInterface(NextHopInterface nextHopInterface) {
              return new FibForward(node.getFinalNextHopIp(), nextHopInterface.getInterfaceName());
            }

            @Override
            public FibAction visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
              return FibNullRoute.INSTANCE;
            }

            @Override
            public FibAction visitNextHopVrf(NextHopVrf nextHopVrf) {
              return new FibNextVrf(nextHopVrf.getVrfName());
            }
          }.visit(route.getNextHop());
      entriesBuilder.add(new FibEntry(fibAction, ImmutableList.copyOf(stack)));
      return;
    }
    stack.push(route);
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

    new NextHopVisitor<Void>() {

      @Override
      public Void visitNextHopIp(NextHopIp nextHopIp) {
        Set<? extends AbstractRouteDecorator> nextHopLongestPrefixMatchRoutes =
            rib.longestPrefixMatch(nextHopIp.getIp(), maxPrefixLength);

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
                nextHopIp.getIp(),
                newSeenNetworks,
                depth + 1,
                Prefix.MAX_PREFIX_LENGTH,
                route,
                ResolutionTreeNode.withParent(nextHopLongestPrefixMatchRoute, treeNode, null));
          }
        }
        return null;
      }

      @Override
      public Void visitNextHopInterface(NextHopInterface nextHopInterface) {
        Ip finalNextHopIp =
            nextHopInterface.getIp() == null ? mostRecentNextHopIp : route.getNextHopIp();
        ResolutionTreeNode.withParent(route, treeNode, finalNextHopIp);
        return null;
      }

      @Override
      public Void visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
        ResolutionTreeNode.withParent(route, treeNode, Route.UNSET_ROUTE_NEXT_HOP_IP);
        return null;
      }

      @Override
      public Void visitNextHopVrf(NextHopVrf nextHopVrf) {
        ResolutionTreeNode.withParent(route, treeNode, Route.UNSET_ROUTE_NEXT_HOP_IP);
        return null;
      }
    }.visit(route.getNextHop());
  }

  @Nonnull
  @Override
  public Set<FibEntry> get(Ip ip) {
    return _root.longestPrefixMatch(ip);
  }

  @Nonnull
  @Override
  public Map<Prefix, IpSpace> getMatchingIps() {
    ImmutableMap.Builder<Prefix, IpSpace> builder = ImmutableMap.builder();

    /* Do a fold over the trie. At each node, create the matching Ips for that prefix (adding it
     * to the builder) and return an IpSpace of IPs matched by any prefix in that subtrie. To create
     * the matching Ips of the prefix, whitelist the prefix and blacklist the IPs matched by
     * subtrie prefixes (i.e. longer prefixes).
     *
     * We build ImmutableSortedSets because IpWildcardSetIpSpace uses them internally, and this
     * avoids making an extra copy.
     */
    _root.fold(
        new FoldOperator<FibEntry, SortedSet<IpWildcard>>() {
          @Nonnull
          @Override
          public SortedSet<IpWildcard> fold(
              Prefix prefix,
              Set<FibEntry> elems,
              @Nullable SortedSet<IpWildcard> leftPrefixes,
              @Nullable SortedSet<IpWildcard> rightPrefixes) {
            SortedSet<IpWildcard> subTriePrefixes;
            boolean leftEmpty = leftPrefixes == null || leftPrefixes.isEmpty();
            boolean rightEmpty = rightPrefixes == null || rightPrefixes.isEmpty();
            if (leftEmpty && rightEmpty) {
              subTriePrefixes = ImmutableSortedSet.of();
            } else if (leftEmpty) {
              subTriePrefixes = rightPrefixes;
            } else if (rightEmpty) {
              subTriePrefixes = leftPrefixes;
            } else {
              subTriePrefixes =
                  ImmutableSortedSet.<IpWildcard>naturalOrder()
                      .addAll(leftPrefixes)
                      .addAll(rightPrefixes)
                      .build();
            }

            if (elems.isEmpty()) {
              return subTriePrefixes;
            }

            IpWildcard wc = IpWildcard.create(prefix);

            if (subTriePrefixes.isEmpty()) {
              builder.put(prefix, prefix.toIpSpace());
            } else {
              // Ips matching prefix are those in prefix and not in any subtrie prefixes.
              builder.put(
                  prefix, new IpWildcardSetIpSpace(subTriePrefixes, ImmutableSortedSet.of(wc)));
            }

            return ImmutableSortedSet.of(wc);
          }
        });

    return builder.build();
  }

  private void readObject(java.io.ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    initSuppliers();
  }
}
