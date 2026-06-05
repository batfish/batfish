package org.batfish.vendor.sros.representation;

import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.PREFER_NETWORK;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.LOWEST_NEXT_HOP_IP;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
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
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

/**
 * Converts a {@link SrosConfiguration} (the typed P4 feature model) into the vendor-independent
 * {@link Configuration} model.
 *
 * <p>The non-obvious SR-OS semantics handled here:
 *
 * <ul>
 *   <li><b>Router instance ↔ VRF.</b> The SR-OS {@code Base} router is the main routing instance;
 *       it maps to the Batfish default VRF. Any other router instance maps to a VRF of the same
 *       name.
 *   <li><b>BGP {@code group} → {@code neighbor} inheritance.</b> A neighbor inherits {@code
 *       peer-as} and the {@code import}/{@code export} policy lists from its {@code group} when it
 *       does not configure them directly (per-neighbor config wins). This is resolved here, not in
 *       extraction (P4 only recorded the {@code group} leafref).
 *   <li><b>eBGP default-reject.</b> SR-OS drops eBGP routes in both directions unless an explicit
 *       policy accepts them ({@code ebgp-default-reject-policy} defaults true). Each peer therefore
 *       gets a generated import and export policy that chains its named policies and then defaults
 *       to <em>reject</em> for an eBGP session (and to <em>accept</em> for an iBGP session, which
 *       SR-OS does not default-reject).
 * </ul>
 */
@ParametersAreNonnullByDefault
public final class SrosConversions {

  /**
   * The default administrative distance Batfish assigns BGP routes. SR-OS uses route {@code
   * preference} 170 for both eBGP and iBGP by default (eBGP and iBGP share one preference in SR-OS,
   * unlike Cisco's 20/200 split).
   */
  static final int DEFAULT_BGP_ADMIN_DISTANCE = 170;

  /** Converts every {@link PrefixList} into a {@link RouteFilterList} on {@code c}. */
  static void convertPrefixLists(SrosConfiguration vc, Configuration c) {
    for (PrefixList pl : vc.getPrefixLists().values()) {
      c.getRouteFilterLists().put(pl.getName(), toRouteFilterList(pl));
    }
  }

  /**
   * Converts an SR-OS {@code prefix-list} to a {@link RouteFilterList} (all lines permit; a
   * route-policy decides accept/reject). The match {@code type} maps to a prefix-length {@link
   * SubRange}: {@code exact} matches only the configured length; {@code longer} matches strictly
   * longer; {@code through}/{@code range}/{@code to}/{@code address-mask} carry extra bounds the P4
   * model does not yet capture, so they are approximated as "this length or longer". Here we model
   * the two types the lab exercises precisely ({@code exact}, {@code longer}) and over-approximate
   * the rest.
   */
  @VisibleForTesting
  static @Nonnull RouteFilterList toRouteFilterList(PrefixList pl) {
    List<RouteFilterLine> lines = new ArrayList<>();
    for (PrefixListEntry entry : pl.getEntries()) {
      Prefix prefix = entry.getPrefix();
      int len = prefix.getPrefixLength();
      SubRange lengthRange =
          switch (entry.getType()) {
            case EXACT -> SubRange.singleton(len);
            case LONGER -> new SubRange(len + 1, Prefix.MAX_PREFIX_LENGTH);
            case THROUGH, RANGE, TO, ADDRESS_MASK ->
                // The upper/lower bound leaves for these types are not modeled in P4 yet; match
                // this length or longer as a conservative over-approximation.
                new SubRange(len, Prefix.MAX_PREFIX_LENGTH);
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
  static void convertPolicyStatements(SrosConfiguration vc, Configuration c, Warnings w) {
    for (PolicyStatement ps : vc.getPolicyStatements().values()) {
      c.getRoutingPolicies().put(ps.getName(), toRoutingPolicy(ps, c, w));
    }
  }

  private static @Nonnull RoutingPolicy toRoutingPolicy(
      PolicyStatement ps, Configuration c, Warnings w) {
    List<Statement> statements = new ArrayList<>();
    for (PolicyStatementEntry entry : ps.getEntries().values()) {
      BooleanExpr guard = toGuard(ps, entry, c, w);
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
  private static @Nonnull BooleanExpr toGuard(
      PolicyStatement ps, PolicyStatementEntry entry, Configuration c, Warnings w) {
    List<BooleanExpr> disjuncts = new ArrayList<>();
    for (String plName : entry.getFromPrefixLists()) {
      if (!c.getRouteFilterLists().containsKey(plName)) {
        w.redFlagf(
            "SR-OS: policy-statement '%s' entry %d references undefined prefix-list '%s'",
            ps.getName(), entry.getEntryId(), plName);
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
   * {@code vrf} on {@code c}. The {@code system} interface and any port-less interface is modeled
   * as {@link InterfaceType#LOOPBACK}; a port-bound interface is {@link InterfaceType#PHYSICAL}.
   */
  static void convertInterfaces(Router router, Configuration c, Vrf vrf) {
    for (RouterInterface ri : router.getInterfaces().values()) {
      InterfaceType type = ri.getPort() == null ? InterfaceType.LOOPBACK : InterfaceType.PHYSICAL;
      Interface.Builder ib =
          Interface.builder()
              .setName(ri.getName())
              .setOwner(c)
              .setVrf(vrf)
              .setType(type)
              // SR-OS router interfaces have no shutdown leaf in the modeled subset; they are up
              // once configured (admin-state is provisioned at the port, modeled separately).
              .setAdminUp(true);
      ConcreteInterfaceAddress address = ri.getPrimaryConcreteAddress();
      if (address != null) {
        ib.setAddress(address);
      }
      ib.build();
    }
  }

  /**
   * Converts an SR-OS {@link BgpProcess} on {@code router} to a VI {@link
   * org.batfish.datamodel.BgpProcess} attached to {@code vrf}, with one {@link BgpActivePeerConfig}
   * per neighbor. Resolves group→neighbor inheritance and eBGP default-reject (see the class
   * Javadoc).
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
            .setEbgpAdminCost(DEFAULT_BGP_ADMIN_DISTANCE)
            .setIbgpAdminCost(DEFAULT_BGP_ADMIN_DISTANCE)
            .setLocalAdminCost(DEFAULT_BGP_ADMIN_DISTANCE)
            .setLocalOriginationTypeTieBreaker(PREFER_NETWORK)
            .setNetworkNextHopIpTieBreaker(LOWEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setVrf(vrf)
            .build();

    for (BgpNeighbor neighbor : proc.getNeighbors().values()) {
      convertNeighbor(router, proc, neighbor, localAs, c, newProc, w);
    }
  }

  private static void convertNeighbor(
      Router router,
      BgpProcess proc,
      BgpNeighbor neighbor,
      long localAs,
      Configuration c,
      org.batfish.datamodel.BgpProcess newProc,
      Warnings w) {
    Ip peerAddress;
    try {
      peerAddress = Ip.parse(neighbor.getIpAddress());
    } catch (IllegalArgumentException e) {
      w.redFlagf("SR-OS: BGP neighbor has unparseable address '%s'", neighbor.getIpAddress());
      return;
    }

    BgpGroup group = neighbor.getGroup() == null ? null : proc.getGroups().get(neighbor.getGroup());
    if (neighbor.getGroup() != null && group == null) {
      w.redFlagf(
          "SR-OS: BGP neighbor %s references undefined group '%s'",
          neighbor.getIpAddress(), neighbor.getGroup());
    }

    // peer-as: per-neighbor wins, else inherited from the group.
    Long peerAs = neighbor.getPeerAs();
    if (peerAs == null && group != null) {
      peerAs = group.getPeerAs();
    }
    if (peerAs == null) {
      w.redFlagf(
          "SR-OS: BGP neighbor %s has no peer-as (none configured or inherited); skipping",
          neighbor.getIpAddress());
      return;
    }
    boolean ebgp = peerAs != localAs;

    // import/export policy lists: per-neighbor wins, else inherited from the group.
    List<String> importPolicies = inheritedPolicies(neighbor.getImportPolicies(), group, true);
    List<String> exportPolicies = inheritedPolicies(neighbor.getExportPolicies(), group, false);

    String importPolicyName =
        generatedBgpPeerImportPolicyName(router.getName(), neighbor.getIpAddress());
    String exportPolicyName =
        generatedBgpPeerExportPolicyName(router.getName(), neighbor.getIpAddress());
    buildPeerPolicy(importPolicyName, importPolicies, ebgp, c);
    buildPeerPolicy(exportPolicyName, exportPolicies, ebgp, c);

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

    BgpActivePeerConfig.builder()
        .setPeerAddress(peerAddress)
        .setRemoteAsns(LongSpace.of(peerAs))
        .setLocalAs(localAs)
        .setLocalIp(systemInterfaceIp(router))
        .setIpv4UnicastAddressFamily(af)
        .setBgpProcess(newProc)
        .build();
  }

  /**
   * Resolves a per-peer policy list with group inheritance: the neighbor's own list if non-empty,
   * otherwise the group's list.
   */
  private static @Nonnull List<String> inheritedPolicies(
      List<String> neighborPolicies, @Nullable BgpGroup group, boolean isImport) {
    if (!neighborPolicies.isEmpty()) {
      return neighborPolicies;
    }
    if (group == null) {
      return ImmutableList.of();
    }
    return isImport ? group.getImportPolicies() : group.getExportPolicies();
  }

  /**
   * Builds a generated per-peer import or export {@link RoutingPolicy}: chain the named policies in
   * order ({@link FirstMatchChain}); if none matches, default to accept for iBGP or reject for eBGP
   * (SR-OS eBGP default-reject). Mirrors the Junos generated-peer-policy idiom.
   */
  private static void buildPeerPolicy(
      String name, List<String> policyNames, boolean ebgp, Configuration c) {
    List<Statement> statements = new ArrayList<>();
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
