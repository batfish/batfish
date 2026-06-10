package org.batfish.vendor.sros.representation;

import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.PREFER_NETWORK;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.LOWEST_NEXT_HOP_IP;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import org.batfish.datamodel.IsoAddress;
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
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
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
   * route-policy decides accept/reject). The match {@code type} determines the lines: {@code exact}
   * matches only the configured length; {@code longer} matches strictly longer; {@code through}
   * matches this length through {@code through-length}; {@code range} matches {@code
   * start-length}..{@code end-length}. {@code to} matches, for each {@code to-prefix} (nested in
   * the base), every ancestor prefix on the path from the base length down to the to-prefix length
   * — emitted as one exact line per length (a line over the base prefix with a length range would
   * wrongly match siblings off that path; confirmed on SR-SIM 26.3.R1). {@code address-mask} with a
   * contiguous mask equal to the base length is an exact match; a non-contiguous mask cannot be a
   * length {@link SubRange} and is over-approximated with a warning.
   */
  @VisibleForTesting
  static @Nonnull RouteFilterList toRouteFilterList(PrefixList pl, Warnings w) {
    List<RouteFilterLine> lines = new ArrayList<>();
    for (PrefixListEntry entry : pl.getEntries()) {
      Prefix prefix = entry.getPrefix();
      int len = prefix.getPrefixLength();
      switch (entry.getType()) {
        case EXACT ->
            lines.add(new RouteFilterLine(LineAction.PERMIT, prefix, SubRange.singleton(len)));
        case LONGER ->
            lines.add(
                new RouteFilterLine(
                    LineAction.PERMIT, prefix, new SubRange(len + 1, Prefix.MAX_PREFIX_LENGTH)));
        case THROUGH -> {
          // through-length: match this prefix's length through through-length (inclusive). If the
          // bound is missing (older capture), fall back to "this length or longer" + warn.
          Integer through = entry.getThroughLength();
          lines.add(
              new RouteFilterLine(
                  LineAction.PERMIT,
                  prefix,
                  through == null ? overApproximate(pl, entry, w) : new SubRange(len, through)));
        }
        case RANGE -> {
          // range: match start-length through end-length (inclusive).
          Integer start = entry.getStartLength();
          Integer end = entry.getEndLength();
          lines.add(
              new RouteFilterLine(
                  LineAction.PERMIT,
                  prefix,
                  start == null || end == null
                      ? overApproximate(pl, entry, w)
                      : new SubRange(start, end)));
        }
        case TO -> {
          // `to`: match the ancestors of each to-prefix at lengths [base-length .. to-length].
          // Each is a distinct prefix (the to-prefix truncated to that length), so emit one exact
          // line per length rather than a single base-prefix line (which would match off-path
          // siblings). An empty to-prefix list (older capture) falls back to over-approximation.
          if (entry.getToPrefixes().isEmpty()) {
            lines.add(
                new RouteFilterLine(LineAction.PERMIT, prefix, overApproximate(pl, entry, w)));
            break;
          }
          for (Prefix toPrefix : entry.getToPrefixes()) {
            for (int l = len; l <= toPrefix.getPrefixLength(); l++) {
              Prefix ancestor = Prefix.create(toPrefix.getStartIp(), l);
              lines.add(new RouteFilterLine(LineAction.PERMIT, ancestor, SubRange.singleton(l)));
            }
          }
        }
        case ADDRESS_MASK -> {
          // address-mask: a route matches if (address & mask) equals the entry's masked prefix and
          // the mask length matches. A contiguous mask is a prefix length; model it as an exact
          // match at that length on the masked prefix. A non-contiguous mask is not a length range
          // -> over-approximate + warn.
          List<Ip> masks = entry.getMaskPatterns();
          if (masks.isEmpty()) {
            lines.add(
                new RouteFilterLine(LineAction.PERMIT, prefix, overApproximate(pl, entry, w)));
            break;
          }
          for (Ip mask : masks) {
            int maskLen = mask.numSubnetBits();
            if (!mask.equals(Ip.numSubnetBitsToSubnetMask(maskLen))) {
              // Non-contiguous mask: cannot express as a prefix-length range.
              w.redFlagf(
                  "prefix-list '%s' entry %s address-mask '%s' is non-contiguous; not fully"
                      + " modeled, matching '%s' or longer (over-approximation)",
                  pl.getName(), prefix, mask, prefix);
              lines.add(
                  new RouteFilterLine(
                      LineAction.PERMIT,
                      prefix,
                      new SubRange(prefix.getPrefixLength(), Prefix.MAX_PREFIX_LENGTH)));
              continue;
            }
            Prefix masked = Prefix.create(prefix.getStartIp(), maskLen);
            lines.add(new RouteFilterLine(LineAction.PERMIT, masked, SubRange.singleton(maskLen)));
          }
        }
      }
    }
    return new RouteFilterList(pl.getName(), lines);
  }

  /** The "this length or longer" over-approximation + warning, for unmodeled match-type bounds. */
  private static @Nonnull SubRange overApproximate(
      PrefixList pl, PrefixListEntry entry, Warnings w) {
    Prefix prefix = entry.getPrefix();
    w.redFlagf(
        "prefix-list '%s' entry %s match type '%s' is not fully modeled; matching '%s' or"
            + " longer (over-approximation)",
        pl.getName(), prefix, entry.getType(), prefix);
    return new SubRange(prefix.getPrefixLength(), Prefix.MAX_PREFIX_LENGTH);
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
      c.getRoutingPolicies().put(ps.getName(), toRoutingPolicy(vc, ps, c));
    }
  }

  private static @Nonnull RoutingPolicy toRoutingPolicy(
      SrosConfiguration vc, PolicyStatement ps, Configuration c) {
    List<Statement> statements = new ArrayList<>();
    for (PolicyStatementEntry entry : ps.getEntries().values()) {
      BooleanExpr guard = toGuard(vc, entry, c);
      // On a match, apply the entry's set-clauses (metric/MED, as-path-prepend, community) before
      // its terminal action, so the modifications take effect on the matched route.
      List<Statement> onMatch = new ArrayList<>(setStatements(vc, entry));
      onMatch.addAll(actionStatements(entry.getAction()));
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
   * The set-clause statements for one policy entry, applied (in SR-OS order) when the entry
   * matches: {@code metric set} → {@link SetMetric}; {@code as-path-prepend} → {@link
   * PrependAsPath} (the AS repeated {@code repeat} times); {@code community add} → {@link
   * SetCommunities} unioning the named community members onto the route's existing communities.
   */
  private static @Nonnull List<Statement> setStatements(
      SrosConfiguration vc, PolicyStatementEntry entry) {
    List<Statement> statements = new ArrayList<>();
    if (entry.getSetMetric() != null) {
      statements.add(new SetMetric(new LiteralLong(entry.getSetMetric())));
    }
    if (entry.getMetricAdd() != null) {
      statements.add(
          new SetMetric(
              new org.batfish.datamodel.routing_policy.expr.IncrementMetric(entry.getMetricAdd())));
    }
    if (entry.getSetLocalPreference() != null) {
      statements.add(
          new org.batfish.datamodel.routing_policy.statement.SetLocalPreference(
              new LiteralLong(entry.getSetLocalPreference())));
    }
    if (entry.getSetOrigin() != null) {
      statements.add(new SetOrigin(new LiteralOrigin(entry.getSetOrigin(), null)));
    }
    if (entry.getAsPathPrependAsn() != null) {
      List<AsExpr> ases = new ArrayList<>();
      for (int i = 0; i < entry.getAsPathPrependRepeat(); i++) {
        ases.add(new ExplicitAs(entry.getAsPathPrependAsn()));
      }
      statements.add(new PrependAsPath(new LiteralAsList(ases)));
    }
    List<org.batfish.datamodel.bgp.community.Community> communitiesToAdd = new ArrayList<>();
    for (String commName : entry.getCommunityAdds()) {
      Community community = vc.getCommunities().get(commName);
      if (community == null) {
        // Undefined community reference: reported by the structure manager; skip silently here.
        continue;
      }
      for (String member : community.getMembers()) {
        StandardCommunity.tryParse(member).ifPresent(communitiesToAdd::add);
      }
    }
    if (!communitiesToAdd.isEmpty()) {
      statements.add(
          new SetCommunities(
              CommunitySetUnion.of(
                  InputCommunities.instance(),
                  new LiteralCommunitySet(CommunitySet.of(communitiesToAdd)))));
    }
    return statements;
  }

  /**
   * The match condition for one policy entry. SR-OS ANDs the distinct {@code from} criteria, so the
   * guard is a conjunction of: the {@code from prefix-list} match (a disjunction over the
   * referenced lists — a route matches if permitted by any), and the {@code from protocol} match (a
   * disjunction over the named protocols). An entry with no modeled from-criteria matches
   * everything ({@link BooleanExprs#TRUE}).
   */
  private static @Nonnull BooleanExpr toGuard(
      SrosConfiguration vc, PolicyStatementEntry entry, Configuration c) {
    List<BooleanExpr> conjuncts = new ArrayList<>();

    // from prefix-list [...] — OR over the (defined) referenced lists.
    List<BooleanExpr> pfxDisjuncts = new ArrayList<>();
    for (String plName : entry.getFromPrefixLists()) {
      if (!c.getRouteFilterLists().containsKey(plName)) {
        // Undefined prefix-list: skip this disjunct. The undefined reference is reported by the
        // structure manager (extraction records the reference; markConcreteStructure flags it), so
        // no warning is emitted here to avoid duplicating it.
        continue;
      }
      pfxDisjuncts.add(
          new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(plName)));
    }
    if (!pfxDisjuncts.isEmpty()) {
      conjuncts.add(pfxDisjuncts.size() == 1 ? pfxDisjuncts.get(0) : new Disjunction(pfxDisjuncts));
    }

    // from protocol name [...] — OR over the named protocols the route was learned from. The
    // protocols are already a validated enum (unknown values were warned + dropped at extraction).
    List<RoutingProtocol> protocols = new ArrayList<>();
    for (FromProtocol proto : entry.getFromProtocols()) {
      protocols.addAll(toRoutingProtocols(proto));
    }
    if (!protocols.isEmpty()) {
      conjuncts.add(new MatchProtocol(protocols));
    }

    // from community name <name> — the route must carry any member of the named community list.
    for (String commName : entry.getFromCommunities()) {
      Community comm = vc.getCommunities().get(commName);
      if (comm == null) {
        continue;
      }
      List<org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr> members =
          new ArrayList<>();
      for (String member : comm.getMembers()) {
        members.add(
            new org.batfish.datamodel.routing_policy.communities.CommunityIs(
                StandardCommunity.parse(member)));
      }
      if (!members.isEmpty()) {
        conjuncts.add(
            new org.batfish.datamodel.routing_policy.communities.MatchCommunities(
                InputCommunities.instance(),
                new org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny(
                    members.stream()
                        .map(
                            m ->
                                (org.batfish.datamodel.routing_policy.communities
                                        .CommunitySetMatchExpr)
                                    new org.batfish.datamodel.routing_policy.communities
                                        .HasCommunity(m))
                        .collect(ImmutableList.toImmutableList()))));
      }
    }

    // from as-path name <name> — the route's AS path must match the named list's regex.
    for (String apName : entry.getFromAsPaths()) {
      AsPathList ap = vc.getAsPathLists().get(apName);
      if (ap == null || ap.getExpression() == null) {
        continue;
      }
      conjuncts.add(
          org.batfish.datamodel.routing_policy.as_path.MatchAsPath.of(
              org.batfish.datamodel.routing_policy.as_path.InputAsPath.instance(),
              org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex.of(
                  ap.getExpression())));
    }

    if (conjuncts.isEmpty()) {
      return BooleanExprs.TRUE;
    }
    if (conjuncts.size() == 1) {
      return conjuncts.get(0);
    }
    return new Conjunction(conjuncts);
  }

  /**
   * Maps a {@link FromProtocol} to the VI {@link RoutingProtocol}(s) it matches. {@code direct} is
   * Batfish's CONNECTED; {@code bgp} matches both eBGP and iBGP; OSPF/ISIS expand to their
   * sub-protocols.
   */
  private static @Nonnull List<RoutingProtocol> toRoutingProtocols(FromProtocol proto) {
    return switch (proto) {
      case STATIC -> ImmutableList.of(RoutingProtocol.STATIC);
      case DIRECT -> ImmutableList.of(RoutingProtocol.CONNECTED);
      case BGP -> ImmutableList.of(RoutingProtocol.BGP, RoutingProtocol.IBGP);
      case OSPF ->
          ImmutableList.of(
              RoutingProtocol.OSPF,
              RoutingProtocol.OSPF_IA,
              RoutingProtocol.OSPF_E1,
              RoutingProtocol.OSPF_E2);
      case ISIS -> ImmutableList.of(RoutingProtocol.ISIS_L1, RoutingProtocol.ISIS_L2);
    };
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
   * Converts a router instance's {@code ospf} process to a VI {@link
   * org.batfish.datamodel.ospf.OspfProcess} on {@code vrf}, attaching {@link
   * org.batfish.datamodel.ospf.OspfInterfaceSettings} to each OSPF-enabled interface. Batfish
   * derives the OSPF adjacencies and neighbor configs from the interface settings (area +
   * addresses) in post-processing, so only the interface settings and area membership are set here.
   *
   * <p>Interface cost: an explicit {@code metric} wins; otherwise the cost is derived from the
   * reference bandwidth (SR-OS and Batfish both default to a 100 Gbps reference, so a 100 Gbps port
   * derives cost 1). A loopback gets cost 0 (it has no bandwidth). The process is skipped when
   * {@code admin-state} is not enable.
   */
  static void convertOspf(Router router, Configuration c, Vrf vrf, Warnings w) {
    org.batfish.vendor.sros.representation.OspfProcess proc = router.getOspfProcess();
    if (proc == null || !proc.getAdminStateEnable()) {
      return;
    }
    Ip routerId = proc.getRouterId();
    if (routerId == null) {
      routerId = systemInterfaceIp(router);
    }
    if (routerId == null) {
      w.redFlagf(
          "router '%s' OSPF has no router-id and no system interface address; skipping OSPF",
          router.getName());
      return;
    }
    String procName = Integer.toString(proc.getInstance());
    org.batfish.datamodel.ospf.OspfProcess newProc =
        org.batfish.datamodel.ospf.OspfProcess.builder()
            .setProcessId(procName)
            .setRouterId(routerId)
            .setReferenceBandwidth(OSPF_REFERENCE_BANDWIDTH)
            // SR-OS OSPF route preference (admin distance): internal (intra/inter-area, summary)
            // default 10, external default 150 — not the Cisco 110/110. An explicit `preference`
            // overrides the internal default (the lab raises it to 20 so IS-IS, pref 18, wins).
            .setAdminCosts(ospfAdminCosts(proc.getPreference()))
            .setAreas(ImmutableMap.of())
            .setVrf(vrf)
            .build();

    Map<Long, org.batfish.datamodel.ospf.OspfArea.Builder> areaBuilders = new LinkedHashMap<>();
    for (OspfArea area : proc.getAreas().values()) {
      long areaNum = Ip.parse(area.getAreaId()).asLong();
      org.batfish.datamodel.ospf.OspfArea.Builder ab =
          areaBuilders.computeIfAbsent(
              areaNum,
              n -> org.batfish.datamodel.ospf.OspfArea.builder().setNumber(n).setNonStub());
      for (OspfAreaInterface ospfIf : area.getInterfaces().values()) {
        Interface viIface = c.getAllInterfaces().get(ospfIf.getName());
        if (viIface == null) {
          w.redFlagf(
              "router '%s' OSPF area %s references undefined interface '%s'; skipping",
              router.getName(), area.getAreaId(), ospfIf.getName());
          continue;
        }
        int cost = ospfInterfaceCost(ospfIf, viIface, newProc.getReferenceBandwidth());
        viIface.setOspfSettings(
            org.batfish.datamodel.ospf.OspfInterfaceSettings.builder()
                .setProcess(procName)
                .setAreaName(areaNum)
                .setEnabled(true)
                .setPassive(false)
                .setNetworkType(toOspfNetworkType(ospfIf.getInterfaceType()))
                .setCost(cost)
                .setOspfAddresses(
                    org.batfish.datamodel.ospf.OspfAddresses.of(viIface.getAllConcreteAddresses()))
                .build());
        ab.addInterface(ospfIf.getName());
      }
    }
    Map<Long, org.batfish.datamodel.ospf.OspfArea> areas = new TreeMap<>();
    for (Map.Entry<Long, org.batfish.datamodel.ospf.OspfArea.Builder> e : areaBuilders.entrySet()) {
      areas.put(e.getKey(), e.getValue().setOspfProcess(newProc).build());
    }
    newProc.setAreas(ImmutableMap.copyOf(areas));
  }

  /**
   * Converts a router instance's {@code isis} process to a VI {@link
   * org.batfish.datamodel.isis.IsisProcess} on {@code vrf}, attaching {@link
   * org.batfish.datamodel.isis.IsisInterfaceSettings} to each IS-IS-enabled interface. The NET is
   * built from the first {@code area-address} + {@code system-id} + an {@code 00} N-selector. A
   * {@code passive} interface advertises its subnet but forms no adjacency (VI {@link
   * org.batfish.datamodel.isis.IsisInterfaceMode#PASSIVE}). Admin distances come from the
   * NOKIA_SROS {@link org.batfish.datamodel.RoutingProtocol} defaults (L1 15 / L2 18 internal).
   */
  static void convertIsis(Router router, Configuration c, Vrf vrf, Warnings w) {
    org.batfish.vendor.sros.representation.IsisProcess proc = router.getIsisProcess();
    if (proc == null || !proc.getAdminStateEnable()) {
      return;
    }
    String systemId = proc.getSystemId();
    if (systemId == null) {
      // SR OS derives the IS-IS system-id from the system interface IPv4 address when it is not
      // explicitly configured: each of the four octets is zero-padded to three decimal digits and
      // the resulting twelve digits are regrouped as XXXX.XXXX.XXXX (e.g. 10.10.10.10 ->
      // 010.010.010.010 -> 0100.1001.0010). Confirmed live on SR-SIM 26.3.R1 (sros_services lab).
      systemId = deriveIsisSystemId(systemInterfaceIp(router));
    }
    if (systemId == null || proc.getAreaAddresses().isEmpty()) {
      w.redFlagf(
          "router '%s' IS-IS has no system-id (and none derivable from the system interface) or no"
              + " area-address; skipping IS-IS",
          router.getName());
      return;
    }
    // NET = <area-address>.<system-id>.00 (e.g. 49.0001.0100.1000.0001.00). Build the IsoAddress
    // from the dotted form, stripping the dots the IsoAddress parser does not expect.
    String net = proc.getAreaAddresses().get(0) + "." + systemId + ".00";
    IsoAddress netAddress;
    try {
      netAddress = new IsoAddress(net);
    } catch (IllegalArgumentException e) {
      w.redFlagf("router '%s' IS-IS NET '%s' is invalid; skipping IS-IS", router.getName(), net);
      return;
    }

    boolean level1 =
        proc.getLevelCapability()
            != org.batfish.vendor.sros.representation.IsisProcess.LevelCapability.LEVEL_2;
    boolean level2 =
        proc.getLevelCapability()
            != org.batfish.vendor.sros.representation.IsisProcess.LevelCapability.LEVEL_1;
    org.batfish.datamodel.isis.IsisProcess.Builder newProc =
        org.batfish.datamodel.isis.IsisProcess.builder().setNetAddress(netAddress).setVrf(vrf);
    if (level1) {
      newProc.setLevel1(org.batfish.datamodel.isis.IsisLevelSettings.builder().build());
    }
    if (level2) {
      newProc.setLevel2(org.batfish.datamodel.isis.IsisLevelSettings.builder().build());
    }
    newProc.build();

    for (org.batfish.vendor.sros.representation.IsisProcessInterface isisIf :
        proc.getInterfaces().values()) {
      Interface viIface = c.getAllInterfaces().get(isisIf.getName());
      if (viIface == null) {
        w.redFlagf(
            "router '%s' IS-IS references undefined interface '%s'; skipping",
            router.getName(), isisIf.getName());
        continue;
      }
      org.batfish.datamodel.isis.IsisInterfaceMode mode =
          isisIf.getPassive()
              ? org.batfish.datamodel.isis.IsisInterfaceMode.PASSIVE
              : org.batfish.datamodel.isis.IsisInterfaceMode.ACTIVE;
      // SR-OS default IS-IS interface metric is 10 (wide metrics); an explicit `metric` overrides.
      long isisCost = isisIf.getMetric() == null ? ISIS_DEFAULT_METRIC : isisIf.getMetric();
      org.batfish.datamodel.isis.IsisInterfaceLevelSettings levelSettings =
          org.batfish.datamodel.isis.IsisInterfaceLevelSettings.builder()
              .setMode(mode)
              .setCost(isisCost)
              .build();
      org.batfish.datamodel.isis.IsisInterfaceSettings.Builder ifSettings =
          org.batfish.datamodel.isis.IsisInterfaceSettings.builder()
              .setIsoAddress(netAddress)
              .setPointToPoint(
                  isisIf.getInterfaceType()
                      == org.batfish.vendor.sros.representation.IsisProcessInterface.InterfaceType
                          .POINT_TO_POINT);
      if (level1) {
        ifSettings.setLevel1(levelSettings);
      }
      if (level2) {
        ifSettings.setLevel2(levelSettings);
      }
      viIface.setIsis(ifSettings.build());
    }
  }

  /**
   * Converts a VPRN's {@code bgp-ipvpn mpls} settings to the VI model. Only the {@code
   * route-distinguisher} is applied (onto the VRF) — the VI model stores an RD per VRF. The {@code
   * vrf-target} route-targets and inter-PE VPN-IPv4 (L3VPN) route import are NOT modeled: the VI
   * datamodel has no VPNv4 address family and its cross-VRF leaking is intra-node only, so PE-to-PE
   * MP-BGP L3VPN route exchange is not reproducible. See {@link BgpIpvpn} and the tracked
   * follow-up.
   */
  static void convertBgpIpvpn(Router router, Vrf vrf, Warnings w) {
    BgpIpvpn ipvpn = router.getBgpIpvpn();
    if (ipvpn == null || ipvpn.getRouteDistinguisher() == null) {
      return;
    }
    try {
      vrf.setRouteDistinguisher(
          org.batfish.datamodel.bgp.RouteDistinguisher.parse(ipvpn.getRouteDistinguisher()));
    } catch (IllegalArgumentException e) {
      w.redFlagf(
          "VPRN '%s' route-distinguisher '%s' is invalid; not set",
          router.getName(), ipvpn.getRouteDistinguisher());
    }
  }

  /** OSPF default {@code reference-bandwidth}: 100,000,000 kilobps (100 Gbps), expressed in bps. */
  private static final double OSPF_REFERENCE_BANDWIDTH = 100E9D;

  /** SR-OS default IS-IS interface metric (wide metrics). */
  private static final long ISIS_DEFAULT_METRIC = 10L;

  /** SR-OS default OSPF internal route preference (intra-area, inter-area, internal summary). */
  private static final int OSPF_DEFAULT_INTERNAL_PREFERENCE = 10;

  /** SR-OS default OSPF external route preference (E1/E2). */
  private static final int OSPF_DEFAULT_EXTERNAL_PREFERENCE = 150;

  /**
   * SR-OS OSPF route preferences (Batfish admin distances) for a process: internal routes
   * (intra-area, inter-area, internal summary) use the configured {@code preference} (default 10);
   * external routes (E1/E2) use the {@code external-preference} default 150. Not the Cisco 110/110.
   */
  private static java.util.Map<RoutingProtocol, Integer> ospfAdminCosts(
      @Nullable Integer internalPreference) {
    int internal =
        internalPreference == null ? OSPF_DEFAULT_INTERNAL_PREFERENCE : internalPreference;
    return ImmutableMap.of(
        RoutingProtocol.OSPF, internal,
        RoutingProtocol.OSPF_IA, internal,
        RoutingProtocol.OSPF_IS, internal,
        RoutingProtocol.OSPF_E1, OSPF_DEFAULT_EXTERNAL_PREFERENCE,
        RoutingProtocol.OSPF_E2, OSPF_DEFAULT_EXTERNAL_PREFERENCE);
  }

  /**
   * The OSPF cost for an interface: an explicit {@code metric} wins; a loopback is 0; otherwise the
   * cost is derived as {@code max(1, referenceBandwidth / interfaceBandwidth)}.
   */
  private static int ospfInterfaceCost(
      OspfAreaInterface ospfIf, Interface viIface, double referenceBandwidth) {
    if (ospfIf.getMetric() != null) {
      return ospfIf.getMetric();
    }
    if (viIface.getInterfaceType() == InterfaceType.LOOPBACK) {
      return 0;
    }
    Double bw = viIface.getBandwidth();
    if (bw == null || bw <= 0) {
      return 1;
    }
    return Math.max(1, (int) (referenceBandwidth / bw));
  }

  private static org.batfish.datamodel.ospf.OspfNetworkType toOspfNetworkType(
      @Nullable OspfAreaInterface.InterfaceType type) {
    if (type == OspfAreaInterface.InterfaceType.POINT_TO_POINT) {
      return org.batfish.datamodel.ospf.OspfNetworkType.POINT_TO_POINT;
    }
    // SR-OS default and BROADCAST both map to the broadcast network type.
    return org.batfish.datamodel.ospf.OspfNetworkType.BROADCAST;
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
              "static route %s has neither a next-hop IP nor blackhole; skipping", sr.getPrefix());
          continue;
        }
        b.setNextHop(org.batfish.datamodel.route.nh.NextHopIp.of(nhIp));
      }
      vrf.getStaticRoutes().add(b.build());
    }
  }

  /**
   * Converts a router instance's {@code aggregates} to VI {@link
   * org.batfish.datamodel.GeneratedRoute}s on {@code vrf}. SR-OS installs an aggregate as a discard
   * summary route (preference 130) only when a contributing more-specific exists; Batfish's
   * generated-route mechanism has the same activation semantics (a generated route is installed
   * only when a contributing route is present), so each aggregate maps to a discard generated route
   * at admin distance 130. {@code summary-only}/{@code community} affect advertisement, not RIB
   * installation, and are not modeled here.
   */
  static void convertAggregates(Router router, Vrf vrf) {
    for (Aggregate agg : router.getAggregates()) {
      vrf.getGeneratedRoutes()
          .add(
              org.batfish.datamodel.GeneratedRoute.builder()
                  .setNetwork(agg.getPrefix())
                  .setAdmin(Aggregate.PREFERENCE)
                  .setDiscard(true)
                  .build());
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
    // A LAG (lag-N): model it as an AGGREGATED interface whose member ports are AGGREGATE
    // dependencies, so post-processing sums their bandwidth into the bundle. Each member port gets
    // its own PHYSICAL interface and is marked as part of this channel-group; the LAG records its
    // members as channel-group members. The channel-group linkage is what lets the logical-Layer-1
    // computation collapse the members' physical edges into a single lag<->lag edge — without it,
    // the two member edges look like two L1 neighbors and the dependency logic deactivates the LAG
    // as an LACP failure. With it, the LAG gets a BIND dependency on its single logical neighbor
    // and comes up when its members are up.
    Lag lag = vc.getLags().get(portPath);
    if (lag != null) {
      for (String member : lag.getMemberPorts()) {
        ensurePortInterface(vc, member, c, vrf);
        Interface memberIface = c.getAllInterfaces().get(member);
        if (memberIface != null) {
          memberIface.setChannelGroup(portPath);
        }
      }
      Interface lagIface =
          Interface.builder()
              .setName(portPath)
              .setOwner(c)
              .setVrf(vrf)
              .setType(InterfaceType.AGGREGATED)
              .setAdminUp(lag.getAdminStateEnable())
              .setChannelGroupMembers(lag.getMemberPorts())
              .build();
      lagIface.setDependencies(
          lag.getMemberPorts().stream()
              .map(m -> new Interface.Dependency(m, Interface.DependencyType.AGGREGATE))
              .collect(ImmutableList.toImmutableList()));
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
          "router '%s' BGP has no router-id and no system interface address; skipping BGP",
          router.getName());
      return;
    }
    if (localAs == null) {
      w.redFlagf(
          "router '%s' has BGP configured but no autonomous-system; skipping BGP",
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
      w.redFlagf("BGP neighbor has unparseable address '%s'", neighbor.getIpAddress());
      return;
    }

    // The neighbor's attributes already include anything inherited from its group (resolved on the
    // model). peer-as is still required to model the session.
    Long peerAs = neighbor.getPeerAs();
    if (peerAs == null) {
      w.redFlagf(
          "BGP neighbor %s has no peer-as (none configured or inherited); skipping",
          neighbor.getIpAddress());
      return;
    }
    // eBGP vs iBGP: an explicit peer type wins (SR-OS, like Junos, lets a session be typed
    // directly);
    // otherwise infer from whether the peer-as differs from the local AS.
    boolean ebgp =
        neighbor.getType() != null ? neighbor.getType() == PeerType.EXTERNAL : peerAs != localAs;

    boolean nextHopSelf = Boolean.TRUE.equals(neighbor.getNextHopSelf());
    String importPolicyName =
        generatedBgpPeerImportPolicyName(router.getName(), neighbor.getIpAddress());
    String exportPolicyName =
        generatedBgpPeerExportPolicyName(router.getName(), neighbor.getIpAddress());
    buildPeerPolicy(importPolicyName, neighbor.getImportPolicies(), ebgp, false, false, c);
    buildPeerPolicy(exportPolicyName, neighbor.getExportPolicies(), ebgp, true, nextHopSelf, c);

    // Route-reflection: a neighbor with a cluster-id is a route-reflector client. Mark the address
    // family as an RR client and set the cluster-id, so Batfish reflects routes between clients.
    Ip clusterId = neighbor.getClusterId();
    Ipv4UnicastAddressFamily af =
        Ipv4UnicastAddressFamily.builder()
            .setImportPolicy(importPolicyName)
            .setExportPolicy(exportPolicyName)
            .setRouteReflectorClient(clusterId != null)
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
        .setClusterId(clusterId == null ? null : clusterId.asLong())
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
      String name,
      List<String> policyNames,
      boolean ebgp,
      boolean isExport,
      boolean nextHopSelf,
      Configuration c) {
    List<Statement> statements = new ArrayList<>();
    // next-hop-self: on export, rewrite the BGP next-hop to this router's address. Needed for an
    // iBGP route reflector with no IGP underlay so its clients can resolve reflected routes (L4).
    if (isExport && nextHopSelf) {
      statements.add(
          new org.batfish.datamodel.routing_policy.statement.SetNextHop(
              org.batfish.datamodel.routing_policy.expr.SelfNextHop.getInstance()));
    }
    // SR-OS BGP origin on locally-sourced routes: a connected/direct route (e.g. the system or an
    // interface prefix) advertised by an export policy carries origin IGP; a redistributed route
    // (static/OSPF/etc.) carries origin INCOMPLETE. Set IGP for connected/local routes at the head
    // of the export policy; leave everything else at Batfish's redistribution default (incomplete),
    // which matches the device. (System-prefix IGP caught at P5-V; static=incomplete at L6.)
    if (isExport) {
      statements.add(
          new If(
              new MatchProtocol(RoutingProtocol.CONNECTED, RoutingProtocol.LOCAL),
              ImmutableList.of(new SetOrigin(new LiteralOrigin(OriginType.IGP, null))),
              ImmutableList.of()));
      // SR-OS does not carry a non-BGP route's IGP/static metric into the advertised MED: a
      // locally-sourced or redistributed route is advertised with MED 0 unless a policy explicitly
      // sets the metric. Reset MED to 0 for non-BGP routes at the policy head; an entry's explicit
      // `metric set` (modeled as SetMetric in the entry's set-clauses) runs later and overrides it.
      // (L6: redistributed static appears on the peer with MED 0, not the route metric 1.)
      statements.add(
          new If(
              new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP),
              ImmutableList.of(),
              ImmutableList.of(new SetMetric(new LiteralLong(0L)))));
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

  /**
   * Lazily creates and returns the name of the shared iBGP default-accept policy on {@code c}.
   *
   * <p>SR-OS iBGP "default-accept" (no import/export policy on the group) accepts <em>BGP</em>
   * routes — it propagates routes already in BGP, but does <em>not</em> pull connected/static/IGP
   * routes into BGP. (A connected route like the system prefix is advertised only when an explicit
   * export policy matches it; the default never does.) So the default-accept policy accepts iff the
   * route's protocol is BGP/iBGP, and otherwise rejects. This matters on export: with {@code
   * setExportBgpFromBgpRib(false)} Batfish runs the export policy over the whole main RIB, and a
   * blanket accept-all would leak the connected /31s the device never advertises (confirmed on the
   * L3 iBGP lab, where r1 advertises only 1.1.1.1/32 (explicit policy) and the eBGP-learned
   * 2.2.2.2/32 to its iBGP peer, not its connected interface prefixes). On import the route being
   * evaluated is always a received BGP route, so the protocol guard accepts everything — preserving
   * iBGP default-accept on import.
   */
  private static @Nonnull String defaultAcceptPolicy(Configuration c) {
    if (!c.getRoutingPolicies().containsKey(DEFAULT_BGP_ACCEPT_POLICY_NAME)) {
      RoutingPolicy.builder()
          .setName(DEFAULT_BGP_ACCEPT_POLICY_NAME)
          .setOwner(c)
          .setStatements(
              ImmutableList.of(
                  new If(
                      new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP),
                      ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                      ImmutableList.of(Statements.ExitReject.toStaticStatement()))))
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

  /**
   * Derives the IS-IS system-id from an IPv4 address the way SR OS does when {@code system-id} is
   * not explicitly configured: zero-pad each octet to three decimal digits, concatenate the twelve
   * digits, and regroup as {@code XXXX.XXXX.XXXX} (e.g. {@code 10.10.10.10} -> {@code
   * 0100.1001.0010}). Returns {@code null} if {@code ip} is null.
   */
  @VisibleForTesting
  static @Nullable String deriveIsisSystemId(@Nullable Ip ip) {
    if (ip == null) {
      return null;
    }
    long bits = ip.asLong();
    String digits =
        String.format(
            "%03d%03d%03d%03d",
            (bits >> 24) & 0xFF, (bits >> 16) & 0xFF, (bits >> 8) & 0xFF, bits & 0xFF);
    return digits.substring(0, 4) + "." + digits.substring(4, 8) + "." + digits.substring(8, 12);
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
