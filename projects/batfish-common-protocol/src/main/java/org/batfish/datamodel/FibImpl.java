package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.Names.generatedTenantVniInterfaceName;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import java.io.IOException;
import java.io.Serial;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.batfish.datamodel.route.nh.NextHopVtep;

@ParametersAreNonnullByDefault
public final class FibImpl implements Fib {

  /** Helps perform recursive route resolution and maintain the route chain */
  private static final class ResolutionTreeNode {
    private final @Nonnull AbstractRoute _route;
    private final @Nullable Ip _finalNextHopIp;

    private final @Nonnull List<ResolutionTreeNode> _children;
    private boolean _unresolvable;

    /** Use static factories for sanity */
    private ResolutionTreeNode(
        AbstractRoute route, @Nullable Ip finalNextHopIp, List<ResolutionTreeNode> children) {
      // TODO: remove once Route.UNSET_NEXT_HOP_IP and Ip.AUTO are killed
      assert !Ip.AUTO.equals(finalNextHopIp);
      _route = route;
      _children = children;
      _finalNextHopIp = finalNextHopIp;
    }

    static ResolutionTreeNode withParent(
        AbstractRoute route, ResolutionTreeNode parent, @Nullable Ip finalNextHopIp) {
      ResolutionTreeNode child = new ResolutionTreeNode(route, finalNextHopIp, new LinkedList<>());
      parent.addChild(child);
      return child;
    }

    static ResolutionTreeNode root(@Nonnull AbstractRoute route) {
      return new ResolutionTreeNode(route, null, new LinkedList<>());
    }

    public @Nonnull AbstractRoute getRoute() {
      return _route;
    }

    public @Nullable Ip getFinalNextHopIp() {
      return _finalNextHopIp;
    }

    public @Nonnull List<ResolutionTreeNode> getChildren() {
      return _children;
    }

    private void addChild(ResolutionTreeNode child) {
      _children.add(child);
    }

    public void markUnresolvable() {
      _unresolvable = true;
    }

    public boolean getUnresolvable() {
      return _unresolvable;
    }
  }

  private static final int MAX_DEPTH = 10;

  /** This trie is the source of truth for all resolved FIB routes */
  private final @Nonnull PrefixTrieMultiMap<FibEntry> _root;

  private transient Supplier<Set<FibEntry>> _entries;

  public <R extends AbstractRouteDecorator> FibImpl(
      GenericRib<R> rib, ResolutionRestriction<R> restriction) {
    _root = new PrefixTrieMultiMap<>();
    rib.getRoutes().stream()
        .map(AbstractRouteDecorator::getAbstractRoute)
        .filter(r -> !r.getNonForwarding())
        .forEach(r -> _root.putAll(r.getNetwork(), resolveRoute(rib, r, restriction)));
    initSuppliers();
  }

  private void initSuppliers() {
    _entries = Suppliers.memoize(this::computeEntries);
  }

  private Set<FibEntry> computeEntries() {
    return _root.getAllElements();
  }

  @Override
  public @Nonnull Set<FibEntry> allEntries() {
    return _entries.get();
  }

  /**
   * Attempt to resolve a RIB route down to an interface route.
   *
   * @param rib {@link GenericRib} for which to do the resolution.
   * @param route {@link AbstractRoute} with a next hop IP to be resolved.
   * @param restriction A restriction on which routes may be used to recursively resolve next-hop
   *     IPs
   * @return A map (interface name -&gt; last hop IP -&gt; last taken route) for
   * @throws BatfishException if resolution depth is exceeded (high likelihood of a routing loop) OR
   *     an invalid route in the RIB has been encountered.
   */
  @VisibleForTesting
  <R extends AbstractRouteDecorator> Set<FibEntry> resolveRoute(
      GenericRib<R> rib, AbstractRoute route, ResolutionRestriction<R> restriction) {
    ResolutionTreeNode resolutionRoot = ResolutionTreeNode.root(route);
    buildResolutionTree(rib, route, null, new HashSet<>(), 0, resolutionRoot, restriction);
    Builder<FibEntry> collector = ImmutableSet.builder();
    collectEntries(resolutionRoot, new Stack<>(), collector);
    return collector.build();
  }

  private void collectEntries(
      ResolutionTreeNode node,
      Stack<AbstractRoute> stack,
      ImmutableCollection.Builder<FibEntry> entriesBuilder) {
    AbstractRoute route = node.getRoute();
    assert !route.getNonForwarding();
    if (node.getChildren().isEmpty()) {
      FibAction fibAction =
          new NextHopVisitor<FibAction>() {

            @Override
            public FibAction visitNextHopIp(NextHopIp nextHopIp) {
              checkState(
                  node.getUnresolvable(),
                  "FIB resolution failed to reach a terminal route for NHIP route not marked"
                      + " unresolvable: %s",
                  route);
              return FibNullRoute.INSTANCE;
            }

            @Override
            public FibAction visitNextHopInterface(NextHopInterface nextHopInterface) {
              return FibForward.of(node.getFinalNextHopIp(), nextHopInterface.getInterfaceName());
            }

            @Override
            public FibAction visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
              return FibNullRoute.INSTANCE;
            }

            @Override
            public FibAction visitNextHopVrf(NextHopVrf nextHopVrf) {
              return FibNextVrf.of(nextHopVrf.getVrfName(), nextHopVrf.getIp());
            }

            @Override
            public FibAction visitNextHopVtep(NextHopVtep nextHopVtep) {
              // Forward out the VXLAN "interface", which will send to the correct remote node by
              // "ARPing" for the VTEP IP.
              String forwardingIface = generatedTenantVniInterfaceName(nextHopVtep.getVni());
              return FibForward.of(nextHopVtep.getVtepIp(), forwardingIface);
            }
          }.visit(route.getNextHop());
      entriesBuilder.add(new FibEntry(fibAction, stack));
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
   * number of leaf {@link ResolutionTreeNode}. Only leaf nodes of {@link NextHopInterface} routes
   * may contain non-null {@link ResolutionTreeNode#_finalNextHopIp}. Each {@link NextHopIp} route
   * node must have one or more children, or be an unresolvable leaf node.
   */
  private <R extends AbstractRouteDecorator> void buildResolutionTree(
      GenericRib<R> rib,
      AbstractRoute route,
      @Nullable Ip mostRecentNextHopIp,
      Set<Prefix> seenNetworks,
      int depth,
      ResolutionTreeNode treeNode,
      ResolutionRestriction<R> restriction) {
    assert !route.getNonForwarding();
    Prefix network = route.getNetwork();
    checkState(!seenNetworks.contains(network), "Unexpected resolution loop resolving %s", route);
    Set<Prefix> newSeenNetworks = new HashSet<>(seenNetworks);
    newSeenNetworks.add(network);
    if (depth > MAX_DEPTH) {
      // TODO: Declare this a loop using some warning mechanism
      // https://github.com/batfish/batfish/issues/1469
      return;
    }

    new NextHopVisitor<Void>() {

      @Override
      public Void visitNextHopIp(NextHopIp nextHopIp) {
        Set<R> lpmRoutes =
            rib.longestPrefixMatch(
                nextHopIp.getIp(),
                r -> {
                  if (route.getProtocol() == RoutingProtocol.STATIC) {
                    // TODO: factor out common code with
                    // StaticRouteHelper.shouldActivateNextHopIpRoute
                    if (r.getAbstractRoute().getProtocol() == RoutingProtocol.CONNECTED) {
                      // All static routes can be activated by a connected route.
                      return true;
                    }
                    if (!((StaticRoute) route).getRecursive()) {
                      // Non-recursive static routes cannot be activated by non-connected
                      // routes.
                      return false;
                    }
                  }
                  // Recursive routes must pass restriction if present.
                  return restriction.test(r);
                });

        if (lpmRoutes.isEmpty()
            || newSeenNetworks.contains(lpmRoutes.iterator().next().getNetwork())) {
          // The next hop IP does not resolve or resolves in a loop, so this route becomes a
          // discard entry. Note that such entries may only exist on some vendors (e.g. IOS), and
          // will only survive in the final FIB if they do not cause an oscillation during data
          // plane computation. On other vendors, the main RIB does not activate such routes, so we
          // will not encounter them here.
          ResolutionTreeNode.withParent(route, treeNode, null).markUnresolvable();
          return null;
        }
        // We have at least one longest-prefix match, and have not looped yet.
        for (R nextHopLongestPrefixMatchRoute : lpmRoutes) {
          AbstractRoute genericRoute = nextHopLongestPrefixMatchRoute.getAbstractRoute();
          buildResolutionTree(
              rib,
              genericRoute,
              nextHopIp.getIp(),
              newSeenNetworks,
              depth + 1,
              ResolutionTreeNode.withParent(genericRoute, treeNode, null),
              restriction);
        }
        return null;
      }

      @Override
      public Void visitNextHopInterface(NextHopInterface nextHopInterface) {
        Ip nhintIp = nextHopInterface.getIp();
        Ip finalNextHopIp = nhintIp != null ? nhintIp : mostRecentNextHopIp;
        ResolutionTreeNode.withParent(route, treeNode, finalNextHopIp);
        return null;
      }

      @Override
      public Void visitNextHopDiscard(NextHopDiscard nextHopDiscard) {
        ResolutionTreeNode.withParent(route, treeNode, null);
        return null;
      }

      @Override
      public Void visitNextHopVrf(NextHopVrf nextHopVrf) {
        ResolutionTreeNode.withParent(route, treeNode, null);
        return null;
      }

      @Override
      public Void visitNextHopVtep(NextHopVtep nextHopVtep) {
        ResolutionTreeNode.withParent(route, treeNode, null);
        return null;
      }
    }.visit(route.getNextHop());
  }

  @Override
  public @Nonnull Set<FibEntry> get(Ip ip) {
    return _root.longestPrefixMatch(ip);
  }

  @Override
  public @Nonnull Map<Prefix, IpSpace> getMatchingIps() {
    ImmutableMap.Builder<Prefix, IpSpace> builder = ImmutableMap.builder();

    /* Do a fold over the trie. At each node, create the matching Ips for that prefix (adding it
     * to the builder) and return an IpSpace of IPs matched by any prefix in that subtrie. To create
     * the matching Ips of the prefix, whitelist the prefix and blacklist the IPs matched by
     * subtrie prefixes (i.e. longer prefixes).
     */
    _root.fold(
        new FoldOperator<FibEntry, Set<IpWildcard>>() {
          @Override
          public @Nonnull Set<IpWildcard> fold(
              Prefix prefix,
              Set<FibEntry> elems,
              @Nullable Set<IpWildcard> leftPrefixes,
              @Nullable Set<IpWildcard> rightPrefixes) {
            Set<IpWildcard> subTriePrefixes;
            boolean leftEmpty = leftPrefixes == null || leftPrefixes.isEmpty();
            boolean rightEmpty = rightPrefixes == null || rightPrefixes.isEmpty();
            if (leftEmpty && rightEmpty) {
              subTriePrefixes = ImmutableSet.of();
            } else if (leftEmpty) {
              subTriePrefixes = rightPrefixes;
            } else if (rightEmpty) {
              subTriePrefixes = leftPrefixes;
            } else {
              subTriePrefixes =
                  ImmutableSet.<IpWildcard>builderWithExpectedSize(
                          leftPrefixes.size() + rightPrefixes.size())
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
                  prefix, IpWildcardSetIpSpace.create(subTriePrefixes, ImmutableSet.of(wc)));
            }

            return ImmutableSet.of(wc);
          }
        });

    return builder.build();
  }

  @Serial
  private void readObject(java.io.ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    initSuppliers();
  }
}
