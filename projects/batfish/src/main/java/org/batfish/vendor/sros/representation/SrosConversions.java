package org.batfish.vendor.sros.representation;

import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.PREFER_NETWORK;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.LOWEST_NEXT_HOP_IP;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

/**
 * Converts a {@link SrosConfiguration} (the typed P4 feature model) into the vendor-independent
 * {@link Configuration} model.
 *
 * <p>The SR-OS router instance maps to a VRF: the {@code Base} router is the main routing instance
 * and maps to the Batfish default VRF; any other router instance maps to a VRF of the same name.
 * Other non-obvious semantics (BGP group inheritance, eBGP default-reject) are documented on the
 * functions that implement them.
 */
@ParametersAreNonnullByDefault
public final class SrosConversions {

  /**
   * The default BGP route {@code preference} on SR-OS (the vendor's term for what Batfish models as
   * administrative distance). SR-OS uses preference 170 for both eBGP and iBGP by default — they
   * share one preference, unlike Cisco's 20/200 split.
   */
  static final int DEFAULT_BGP_PREFERENCE = 170;

  /** Converts every {@link PrefixList} into a {@link RouteFilterList} on {@code c}. */
  static void convertPrefixLists(SrosConfiguration vc, Configuration c, Warnings w) {
    for (PrefixList pl : vc.getPrefixLists().values()) {
      c.getRouteFilterLists().put(pl.getName(), toRouteFilterList(pl, w));
    }
  }

  /**
   * Converts an SR-OS {@code prefix-list} to a {@link RouteFilterList} (all lines permit; a
   * route-policy decides accept/reject). The match {@code type} maps to a prefix-length {@link
   * SubRange}: {@code exact} matches only the configured length; {@code longer} matches strictly
   * longer. {@code through}/{@code range}/{@code to}/{@code address-mask} carry extra bounds the P4
   * model does not yet capture, so they are over-approximated as "this length or longer" and a
   * warning is emitted (the resulting filter is broader than the device's — see the warning).
   */
  @VisibleForTesting
  static @Nonnull RouteFilterList toRouteFilterList(PrefixList pl, Warnings w) {
    List<RouteFilterLine> lines = new ArrayList<>();
    for (PrefixListEntry entry : pl.getEntries()) {
      Prefix prefix = entry.getPrefix();
      int len = prefix.getPrefixLength();
      SubRange lengthRange =
          switch (entry.getType()) {
            case EXACT -> SubRange.singleton(len);
            case LONGER -> new SubRange(len + 1, Prefix.MAX_PREFIX_LENGTH);
            case THROUGH, RANGE, TO, ADDRESS_MASK -> {
              // The upper/lower bound leaves for these types are not modeled in P4 yet; match this
              // length or longer as a conservative over-approximation, and warn that the filter is
              // broader than the device's until the bounds are modeled.
              w.redFlagf(
                  "SR-OS: prefix-list '%s' entry %s match type '%s' is not fully modeled; matching"
                      + " '%s' or longer (over-approximation)",
                  pl.getName(), prefix, entry.getType(), prefix);
              yield new SubRange(len, Prefix.MAX_PREFIX_LENGTH);
            }
          };
      lines.add(new RouteFilterLine(LineAction.PERMIT, prefix, lengthRange));
    }
    return new RouteFilterList(pl.getName(), lines);
  }

  /**
   * Converts every {@link PolicyStatement} into a chainable {@link RoutingPolicy} on {@code c}. The
   * generated policy is callable as a subroutine: it walks its numbered entries in order, and on an
   * entry match applies the entry action ({@code accept}/{@code reject}/{@code next-entry}/{@code
   * next-policy}); if no entry matches it applies the {@code default-action}. The terminal idiom
   * (return when called as a subroutine, exit otherwise) mirrors the Junos conversion so these
   * policies compose correctly inside a {@link FirstMatchChain}.
   */
  static void convertPolicyStatements(SrosConfiguration vc, Configuration c) {
    for (PolicyStatement ps : vc.getPolicyStatements().values()) {
      c.getRoutingPolicies().put(ps.getName(), toRoutingPolicy(ps, c));
    }
  }

  private static @Nonnull RoutingPolicy toRoutingPolicy(PolicyStatement ps, Configuration c) {
    List<Statement> statements = new ArrayList<>();
    for (PolicyStatementEntry entry : ps.getEntries().values()) {
      BooleanExpr guard = toGuard(entry, c);
      List<Statement> onMatch = actionStatements(entry.getAction());
      if (guard == BooleanExprs.TRUE) {
        // No from-criteria: the entry always matches. Emit its action unconditionally.
        statements.addAll(onMatch);
      } else {
        statements.add(new If(guard, onMatch, ImmutableList.of()));
      }
    }
    // No entry matched: apply the default-action. When default-action is absent SR-OS falls through
    // to the next policy in the chain (and ultimately the protocol default, e.g. eBGP reject), so
    // a null action emits nothing and the policy returns fall-through — see actionStatements.
    statements.addAll(actionStatements(ps.getDefaultAction()));
    return RoutingPolicy.builder()
        .setName(ps.getName())
        .setOwner(c)
        .setStatements(statements)
        .build();
  }

  /**
   * The match condition for one policy entry: a disjunction over its {@code from prefix-list}
   * references (a route matches if it is permitted by any referenced list). An entry with no
   * modeled from-criteria matches everything ({@link BooleanExprs#TRUE}).
   */
  private static @Nonnull BooleanExpr toGuard(PolicyStatementEntry entry, Configuration c) {
    List<BooleanExpr> disjuncts = new ArrayList<>();
    for (String plName : entry.getFromPrefixLists()) {
      if (!c.getRouteFilterLists().containsKey(plName)) {
        // Undefined prefix-list: skip this disjunct. The undefined reference is reported by the
        // structure manager (extraction records the reference; markConcreteStructure flags it), so
        // no warning is emitted here to avoid duplicating it.
        continue;
      }
      disjuncts.add(new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(plName)));
    }
    if (disjuncts.isEmpty()) {
      return BooleanExprs.TRUE;
    }
    if (disjuncts.size() == 1) {
      return disjuncts.get(0);
    }
    return new Disjunction(disjuncts);
  }

  /**
   * The statements that realize a policy {@link PolicyAction}, correct both when the policy is
   * called as a subroutine (return) and when it is the top-level policy (exit). A null action is an
   * implicit fall-through (SR-OS: continue to the next entry / the default-action), so it emits
   * nothing.
   */
  private static @Nonnull List<Statement> actionStatements(@Nullable PolicyAction action) {
    if (action == null) {
      return ImmutableList.of();
    }
    return switch (action) {
      case ACCEPT ->
          ImmutableList.of(
              new If(
                  BooleanExprs.CALL_EXPR_CONTEXT,
                  ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                  ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
      case REJECT ->
          ImmutableList.of(
              new If(
                  BooleanExprs.CALL_EXPR_CONTEXT,
                  ImmutableList.of(Statements.ReturnFalse.toStaticStatement()),
                  ImmutableList.of(Statements.ExitReject.toStaticStatement())));
      // next-entry: stop evaluating this entry, fall through to the next one (no statement).
      case NEXT_ENTRY -> ImmutableList.of();
      // next-policy: stop this policy and defer to the next policy in the import/export chain. In
      // FirstMatchChain terms that is a fall-through (Statements.FallThrough yields
      // fallThrough=true
      // so the chain advances to the next subroutine), NOT Return (which the chain reads as a
      // non-match terminal).
      case NEXT_POLICY -> ImmutableList.of(Statements.FallThrough.toStaticStatement());
    };
  }

  /**
   * Converts an SR-OS router instance's interfaces to VI {@link Interface}s, attaching each to
   * {@code vrf} on {@code c}.
   *
   * <p>SR-OS separates the physical <em>port</em> (e.g. {@code 1/1/c1/1}) from the L3
   * <em>router-interface</em> (e.g. {@code to-r2}) that binds it — the Junos physical/unit split.
   * We model it the same way Batfish models Junos so that a user-provided Layer-1 topology, which
   * names the physical port, drives the L3 adjacency of the router-interface:
   *
   * <ul>
   *   <li>A port-less interface ({@code system}, loopbacks) is a single {@link
   *       InterfaceType#LOOPBACK} holding the address.
   *   <li>A port-bound interface becomes two VI interfaces: an addressless {@link
   *       InterfaceType#PHYSICAL} interface named by the <em>port path</em> (the Layer-1 endpoint),
   *       and a {@link InterfaceType#LOGICAL} interface named by the router-interface, holding the
   *       address and carrying a {@link Interface.DependencyType#BIND} dependency on the port.
   *       {@code PointToPointComputer} follows that BIND dependency to map the L3 interface back to
   *       its physical port, so an L1 edge between ports yields the L3 edge between the router
   *       interfaces (and the logical interface shares the port's fate).
   * </ul>
   */
  static void convertInterfaces(SrosConfiguration vc, Router router, Configuration c, Vrf vrf) {
    for (RouterInterface ri : router.getInterfaces().values()) {
      String portPath = ri.getPort();
      Interface.Builder ib =
          Interface.builder()
              .setName(ri.getName())
              .setOwner(c)
              .setVrf(vrf)
              // SR-OS router interfaces have no shutdown leaf in the modeled subset; they are up
              // once configured (port admin-state is modeled on the PHYSICAL port below).
              .setAdminUp(true);
      if (portPath == null) {
        // system / loopback: no port binding, a single loopback interface holds the address.
        ib.setType(InterfaceType.LOOPBACK);
      } else {
        // Port-bound: the L3 router-interface is a LOGICAL interface bound to its physical port.
        ib.setType(InterfaceType.LOGICAL);
        ib.setDependencies(
            ImmutableList.of(new Interface.Dependency(portPath, Interface.DependencyType.BIND)));
        ensurePortInterface(vc, portPath, c, vrf);
      }
      ConcreteInterfaceAddress address = ri.getPrimaryConcreteAddress();
      if (address != null) {
        ib.setAddress(address);
        // SR-OS installs the connected (subnet) route for an interface address but NOT a
        // local /32 host route for the interface's own IP — its route-table shows only the
        // connected prefix. Batfish would otherwise synthesize a local /32; suppress it so the
        // main RIB matches the device. (Verified against the lab route-table, P5-V.)
        ib.setAddressMetadata(
            ImmutableMap.of(
                address, ConnectedRouteMetadata.builder().setGenerateLocalRoute(false).build()));
      }
      ib.build();
    }
  }

  /**
   * Converts a router instance's {@code static-routes} to VI {@link
   * org.batfish.datamodel.StaticRoute}s on {@code vrf}. Only routes whose next-hop/blackhole
   * context is {@code admin-state enable} are installed — SR-OS does not put a disabled (or
   * admin-state unset) static route into the RIB (confirmed on SR-SIM 26.3.R1). A next-hop route
   * maps to a {@link org.batfish.datamodel.route.nh.NextHopIp}; a blackhole maps to {@link
   * org.batfish.datamodel.route.nh.NextHopDiscard}. The SR-OS {@code preference} is the Batfish
   * admin distance and {@code metric} is the route metric (YANG defaults 5 and 1).
   */
  static void convertStaticRoutes(Router router, Vrf vrf, Warnings w) {
    for (StaticRoute sr : router.getStaticRoutes()) {
      if (!sr.getAdminStateEnable()) {
        // Configured but not admin-state enable -> not installed on the device; skip it.
        continue;
      }
      org.batfish.datamodel.StaticRoute.Builder b =
          org.batfish.datamodel.StaticRoute.builder()
              .setNetwork(sr.getPrefix())
              .setAdministrativeCost(sr.getPreference())
              .setMetric(sr.getMetric());
      if (sr.getBlackhole()) {
        b.setNextHop(org.batfish.datamodel.route.nh.NextHopDiscard.instance());
      } else {
        Ip nhIp = sr.getNextHopIp();
        if (nhIp == null) {
          w.redFlagf(
              "SR-OS: static route %s has neither a next-hop IP nor blackhole; skipping",
              sr.getPrefix());
          continue;
        }
        b.setNextHop(org.batfish.datamodel.route.nh.NextHopIp.of(nhIp));
      }
      vrf.getStaticRoutes().add(b.build());
    }
  }

  /**
   * Creates the addressless {@link InterfaceType#PHYSICAL} VI interface for an SR-OS port if it
   * does not already exist on {@code c}. This is the interface a user Layer-1 topology references;
   * the L3 router-interface binds to it. The port is admin-up unless its {@code admin-state} is
   * explicitly {@code disable} (an absent admin-state is treated as up to avoid spuriously
   * deactivating a configured interface; SR-OS lab configs set it explicitly).
   */
  private static void ensurePortInterface(
      SrosConfiguration vc, String portPath, Configuration c, Vrf vrf) {
    if (c.getAllInterfaces().containsKey(portPath)) {
      return;
    }
    Port port = vc.getPorts().get(portPath);
    boolean adminUp = port == null || !Boolean.FALSE.equals(port.getAdminStateEnable());
    Interface.builder()
        .setName(portPath)
        .setOwner(c)
        .setVrf(vrf)
        .setType(InterfaceType.PHYSICAL)
        .setAdminUp(adminUp)
        .build();
  }

  /**
   * Converts an SR-OS {@link BgpProcess} on {@code router} to a VI {@link
   * org.batfish.datamodel.BgpProcess} attached to {@code vrf}, with one {@link BgpActivePeerConfig}
   * per neighbor. Group→neighbor inheritance has already been resolved on the model (see {@link
   * BgpNeighbor#inheritFrom}); eBGP default-reject is realized by {@link #buildPeerPolicy}.
   */
  static void convertBgp(Router router, Configuration c, Vrf vrf, Warnings w) {
    BgpProcess proc = router.getBgpProcess();
    if (proc == null) {
      return;
    }
    Long localAs = router.getAutonomousSystem();
    Ip routerId = proc.getRouterId();
    if (routerId == null) {
      // SR-OS derives the router-id from the system interface address when unset. Use the system
      // interface's address if present; otherwise the process cannot be modeled.
      routerId = systemInterfaceIp(router);
    }
    if (routerId == null) {
      w.redFlagf(
          "SR-OS: router '%s' BGP has no router-id and no system interface address; skipping BGP",
          router.getName());
      return;
    }
    if (localAs == null) {
      w.redFlagf(
          "SR-OS: router '%s' has BGP configured but no autonomous-system; skipping BGP",
          router.getName());
      return;
    }

    org.batfish.datamodel.BgpProcess newProc =
        org.batfish.datamodel.BgpProcess.builder()
            .setRouterId(routerId)
            .setEbgpAdminCost(DEFAULT_BGP_PREFERENCE)
            .setIbgpAdminCost(DEFAULT_BGP_PREFERENCE)
            .setLocalAdminCost(DEFAULT_BGP_PREFERENCE)
            .setLocalOriginationTypeTieBreaker(PREFER_NETWORK)
            .setNetworkNextHopIpTieBreaker(LOWEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setVrf(vrf)
            .build();

    for (BgpNeighbor neighbor : proc.getNeighbors().values()) {
      convertNeighbor(router, neighbor, localAs, c, newProc, w);
    }
  }

  private static void convertNeighbor(
      Router router,
      BgpNeighbor neighbor,
      long localAs,
      Configuration c,
      org.batfish.datamodel.BgpProcess newProc,
      Warnings w) {
    Ip peerAddress = Ip.tryParse(neighbor.getIpAddress()).orElse(null);
    if (peerAddress == null) {
      w.redFlagf("SR-OS: BGP neighbor has unparseable address '%s'", neighbor.getIpAddress());
      return;
    }

    // The neighbor's attributes already include anything inherited from its group (resolved on the
    // model). peer-as is still required to model the session.
    Long peerAs = neighbor.getPeerAs();
    if (peerAs == null) {
      w.redFlagf(
          "SR-OS: BGP neighbor %s has no peer-as (none configured or inherited); skipping",
          neighbor.getIpAddress());
      return;
    }
    // eBGP vs iBGP: an explicit peer type wins (SR-OS, like Junos, lets a session be typed
    // directly);
    // otherwise infer from whether the peer-as differs from the local AS.
    boolean ebgp =
        neighbor.getType() != null ? neighbor.getType() == PeerType.EXTERNAL : peerAs != localAs;

    String importPolicyName =
        generatedBgpPeerImportPolicyName(router.getName(), neighbor.getIpAddress());
    String exportPolicyName =
        generatedBgpPeerExportPolicyName(router.getName(), neighbor.getIpAddress());
    buildPeerPolicy(importPolicyName, neighbor.getImportPolicies(), ebgp, false, c);
    buildPeerPolicy(exportPolicyName, neighbor.getExportPolicies(), ebgp, true, c);

    Ipv4UnicastAddressFamily af =
        Ipv4UnicastAddressFamily.builder()
            .setImportPolicy(importPolicyName)
            .setExportPolicy(exportPolicyName)
            .setAddressFamilyCapabilities(
                AddressFamilyCapabilities.builder()
                    .setSendCommunity(true)
                    .setSendExtendedCommunity(true)
                    .build())
            .build();

    // Leave local-IP unset: SR-OS auto-selects the source address per peer (the egress
    // interface address for a directly-connected eBGP peer, the system address for a
    // multi-hop/iBGP peer). The modeled subset has no explicit BGP local-address leaf, so we
    // let Batfish resolve the source IP from the topology — for single-hop eBGP it picks the
    // connected interface toward the peer, matching the device. (Forcing the system address
    // here would put the local IP off the peering subnet and the session would never
    // establish.) When local-address modeling is added, set it explicitly here.
    BgpActivePeerConfig.builder()
        .setPeerAddress(peerAddress)
        .setRemoteAsns(LongSpace.of(peerAs))
        .setLocalAs(localAs)
        .setIpv4UnicastAddressFamily(af)
        .setBgpProcess(newProc)
        .build();
  }

  /**
   * Builds a generated per-peer import or export {@link RoutingPolicy}: chain the named policies in
   * order ({@link FirstMatchChain}); if none matches, default to accept for iBGP or reject for eBGP
   * (SR-OS eBGP default-reject). Mirrors the Junos generated-peer-policy idiom.
   */
  private static void buildPeerPolicy(
      String name, List<String> policyNames, boolean ebgp, boolean isExport, Configuration c) {
    List<Statement> statements = new ArrayList<>();
    // SR-OS originates locally-sourced routes (the system/connected prefixes advertised via an
    // export policy) into BGP with origin IGP, like Junos. Set origin IGP for non-BGP routes at
    // the head of the export policy so advertised local routes carry origin igp, not the Batfish
    // redistribution default of incomplete. (Caught by lab validation against the eBGP peer, P5-V.)
    if (isExport) {
      statements.add(
          new If(
              new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP),
              ImmutableList.of(),
              ImmutableList.of(new SetOrigin(new LiteralOrigin(OriginType.IGP, null)))));
    }
    // The default policy backs the chain when no named policy makes a terminal decision.
    String defaultPolicyName = ebgp ? defaultRejectPolicy(c) : defaultAcceptPolicy(c);
    statements.add(new SetDefaultPolicy(defaultPolicyName));

    List<BooleanExpr> calls = new ArrayList<>();
    for (String policyName : policyNames) {
      if (c.getRoutingPolicies().containsKey(policyName)) {
        calls.add(new CallExpr(policyName));
      }
    }
    If conditional = new If();
    conditional.setGuard(new FirstMatchChain(calls));
    conditional.setTrueStatements(ImmutableList.of(Statements.ExitAccept.toStaticStatement()));
    conditional.setFalseStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()));
    statements.add(conditional);

    RoutingPolicy.builder().setName(name).setOwner(c).setStatements(statements).build();
  }

  /** Lazily creates and returns the name of a shared "accept all" default policy on {@code c}. */
  private static @Nonnull String defaultAcceptPolicy(Configuration c) {
    if (!c.getRoutingPolicies().containsKey(DEFAULT_BGP_ACCEPT_POLICY_NAME)) {
      RoutingPolicy.builder()
          .setName(DEFAULT_BGP_ACCEPT_POLICY_NAME)
          .setOwner(c)
          .setStatements(ImmutableList.of(Statements.ExitAccept.toStaticStatement()))
          .build();
    }
    return DEFAULT_BGP_ACCEPT_POLICY_NAME;
  }

  /** Lazily creates and returns the name of a shared "reject all" default policy on {@code c}. */
  private static @Nonnull String defaultRejectPolicy(Configuration c) {
    if (!c.getRoutingPolicies().containsKey(DEFAULT_BGP_REJECT_POLICY_NAME)) {
      RoutingPolicy.builder()
          .setName(DEFAULT_BGP_REJECT_POLICY_NAME)
          .setOwner(c)
          .setStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()))
          .build();
    }
    return DEFAULT_BGP_REJECT_POLICY_NAME;
  }

  /** The {@code system} interface's primary IP, used as router-id fallback and BGP local-ip. */
  private static @Nullable Ip systemInterfaceIp(Router router) {
    RouterInterface system = router.getInterfaces().get("system");
    return system == null ? null : system.getPrimaryAddress();
  }

  static @Nonnull String generatedBgpPeerImportPolicyName(String router, String peer) {
    return String.format("~BGP_PEER_IMPORT_POLICY:%s:%s~", router, peer);
  }

  static @Nonnull String generatedBgpPeerExportPolicyName(String router, String peer) {
    return String.format("~BGP_PEER_EXPORT_POLICY:%s:%s~", router, peer);
  }

  static final String DEFAULT_BGP_ACCEPT_POLICY_NAME = "~DEFAULT_BGP_ACCEPT_POLICY~";
  static final String DEFAULT_BGP_REJECT_POLICY_NAME = "~DEFAULT_BGP_REJECT_POLICY~";

  private SrosConversions() {}
}
