package org.batfish.vendor.sros.grammar;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.FormatMethod;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warnings;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.batfish.vendor.StructureType;
import org.batfish.vendor.StructureUsage;
import org.batfish.vendor.sros.representation.Aggregate;
import org.batfish.vendor.sros.representation.AsPathList;
import org.batfish.vendor.sros.representation.BgpGroup;
import org.batfish.vendor.sros.representation.BgpIpvpn;
import org.batfish.vendor.sros.representation.BgpNeighbor;
import org.batfish.vendor.sros.representation.BgpProcess;
import org.batfish.vendor.sros.representation.Card;
import org.batfish.vendor.sros.representation.Community;
import org.batfish.vendor.sros.representation.FromProtocol;
import org.batfish.vendor.sros.representation.IsisProcess;
import org.batfish.vendor.sros.representation.IsisProcessInterface;
import org.batfish.vendor.sros.representation.Lag;
import org.batfish.vendor.sros.representation.Mda;
import org.batfish.vendor.sros.representation.OspfArea;
import org.batfish.vendor.sros.representation.OspfAreaInterface;
import org.batfish.vendor.sros.representation.OspfProcess;
import org.batfish.vendor.sros.representation.PeerType;
import org.batfish.vendor.sros.representation.PolicyAction;
import org.batfish.vendor.sros.representation.PolicyStatement;
import org.batfish.vendor.sros.representation.PolicyStatementEntry;
import org.batfish.vendor.sros.representation.Port;
import org.batfish.vendor.sros.representation.PrefixList;
import org.batfish.vendor.sros.representation.PrefixListEntry;
import org.batfish.vendor.sros.representation.Router;
import org.batfish.vendor.sros.representation.RouterInterface;
import org.batfish.vendor.sros.representation.SrosConfiguration;
import org.batfish.vendor.sros.representation.SrosStructureType;
import org.batfish.vendor.sros.representation.SrosStructureUsage;
import org.batfish.vendor.sros.representation.StaticRoute;

/**
 * Populates a {@link SrosConfiguration}'s typed feature model from the canonical, preprocessed
 * {@link SrosStatementTree}.
 *
 * <p>Each leaf's value is the single child key under its leaf node (e.g. {@code card 1 card-type
 * iom-1} is {@code card -> 1 -> card-type -> iom-1}); leaf-lists ({@code policy [a b]}) are the
 * ordered children of the leaf node. The extractor reads only the characterized paths (hardware
 * provisioning, router interfaces, BGP peering, routing-policy). Other configured subtrees present
 * in real configs — {@code system security}/{@code ssh}/{@code user-params}, {@code
 * persistent-indices} — are control-plane-irrelevant and intentionally left unread; they are not
 * warnings (the device accepts them and so do we). The one system leaf that matters, {@code system
 * name}, becomes the Batfish hostname.
 *
 * <p>Because each tree node carries the parse-tree context of the statement(s) that created it (see
 * {@link SrosStatementTree#getDefContexts}), the extractor emits line-stamped {@link
 * Warnings.ParseWarning}s for malformed or out-of-range values (via {@link #warn}) and records
 * structure definitions and references (via {@link #defineStructure}/{@link #referenceStructure}),
 * driving the {@code definedStructures}/{@code undefinedReferences}/unused-structure questions and
 * the {@code annotate} tool.
 */
@ParametersAreNonnullByDefault
public final class SrosFeatureExtractor {

  /** AS number: YANG {@code autonomous-system} is {@code uint32 range "1..max"}. */
  private static final LongSpace AUTONOMOUS_SYSTEM_SPACE =
      LongSpace.of(Range.closed(1L, 4294967295L));

  /** IPv4 prefix length: YANG {@code prefix-length} under a router interface is {@code 0..32}. */
  private static final IntegerSpace IPV4_PREFIX_LENGTH_SPACE = IntegerSpace.of(new SubRange(0, 32));

  /** Line-card and MDA slot numbers (YANG {@code uint32}; constrained to a sane positive range). */
  private static final IntegerSpace SLOT_SPACE = IntegerSpace.of(new SubRange(1, 255));

  /** policy-statement {@code entry-id}: YANG {@code uint32 range "1..65535"}. */
  private static final LongSpace ENTRY_ID_SPACE = LongSpace.of(Range.closed(1L, 65535L));

  /** static-route next-hop/blackhole {@code metric}: YANG {@code uint32 range "0..65535"}. */
  private static final IntegerSpace STATIC_METRIC_SPACE = IntegerSpace.of(new SubRange(0, 65535));

  /**
   * Route {@code preference} (admin distance): YANG {@code uint32 range "1..255"} across protocols.
   */
  private static final IntegerSpace ROUTE_PREFERENCE_SPACE = IntegerSpace.of(new SubRange(1, 255));

  /** policy action {@code metric set}: BGP MED, YANG {@code int64 range "0..4294967295"}. */
  private static final LongSpace POLICY_METRIC_SPACE = LongSpace.of(Range.closed(0L, 4294967295L));

  /** policy action {@code local-preference}: BGP local-pref, YANG {@code uint32}. */
  private static final LongSpace LOCAL_PREFERENCE_SPACE =
      LongSpace.of(Range.closed(0L, 4294967295L));

  /** policy action {@code origin} enumeration. */
  private static final Map<String, org.batfish.datamodel.OriginType> ORIGIN_TYPE =
      ImmutableMap.of(
          "igp", org.batfish.datamodel.OriginType.IGP,
          "egp", org.batfish.datamodel.OriginType.EGP,
          "incomplete", org.batfish.datamodel.OriginType.INCOMPLETE);

  /** as-path-prepend {@code repeat}: YANG {@code uint32 range "1..6"}. */
  private static final IntegerSpace AS_PATH_PREPEND_REPEAT_SPACE =
      IntegerSpace.of(new SubRange(1, 6));

  /** Port {@code admin-state} enumeration: enable -> up, disable -> down. */
  private static final Map<String, Boolean> ADMIN_STATE =
      ImmutableMap.of("enable", Boolean.TRUE, "disable", Boolean.FALSE);

  /** prefix-list entry {@code type} enumeration. */
  private static final Map<String, PrefixListEntry.Type> PREFIX_LIST_TYPE =
      ImmutableMap.<String, PrefixListEntry.Type>builder()
          .put("exact", PrefixListEntry.Type.EXACT)
          .put("longer", PrefixListEntry.Type.LONGER)
          .put("through", PrefixListEntry.Type.THROUGH)
          .put("range", PrefixListEntry.Type.RANGE)
          .put("to", PrefixListEntry.Type.TO)
          .put("address-mask", PrefixListEntry.Type.ADDRESS_MASK)
          .build();

  /** BGP {@code type} enumeration (nokia-types-bgp:peer-type). */
  private static final Map<String, PeerType> PEER_TYPE =
      ImmutableMap.of("internal", PeerType.INTERNAL, "external", PeerType.EXTERNAL);

  /** policy {@code from protocol name} enumeration (the modeled subset). */
  private static final Map<String, FromProtocol> FROM_PROTOCOL =
      ImmutableMap.<String, FromProtocol>builder()
          .put("static", FromProtocol.STATIC)
          .put("direct", FromProtocol.DIRECT)
          .put("bgp", FromProtocol.BGP)
          .put("ospf", FromProtocol.OSPF)
          .put("isis", FromProtocol.ISIS)
          .build();

  /** policy-statement entry/default {@code action-type} enumeration. */
  private static final Map<String, PolicyAction> POLICY_ACTION =
      ImmutableMap.<String, PolicyAction>builder()
          .put("accept", PolicyAction.ACCEPT)
          .put("reject", PolicyAction.REJECT)
          .put("next-entry", PolicyAction.NEXT_ENTRY)
          .put("next-policy", PolicyAction.NEXT_POLICY)
          .build();

  public static void extract(
      SrosStatementTree root,
      SrosConfiguration c,
      Warnings w,
      SrosCombinedParser parser,
      String text) {
    new SrosFeatureExtractor(c, w, parser, text).extractFrom(root);
  }

  private SrosFeatureExtractor(
      SrosConfiguration c, Warnings w, SrosCombinedParser parser, String text) {
    _c = c;
    _w = w;
    _parser = parser;
    _text = text;
  }

  private void extractFrom(SrosStatementTree root) {
    SrosStatementTree configure = root.getChild("configure");
    if (configure == null) {
      return;
    }
    extractSystem(configure.getChild("system"));
    extractCards(configure.getChild("card"));
    extractPorts(configure.getChild("port"));
    extractLags(configure.getChild("lag"));
    extractPolicyOptions(configure.getChild("policy-options"));
    extractServices(configure.getChild("service"));
    extractRouters(configure.getChild("router"));
  }

  // --- system -----------------------------------------------------------------------------------

  private void extractSystem(@Nullable SrosStatementTree system) {
    if (system == null) {
      return;
    }
    // Only the system name is control-plane-relevant; it maps to the Batfish hostname. The rest of
    // the system subtree (security, ssh, user-params, ...) is intentionally not modeled.
    String name = singleValue(system, "name");
    if (name != null) {
      _c.setHostname(unquote(name));
    }
  }

  // --- hardware ---------------------------------------------------------------------------------

  private void extractCards(@Nullable SrosStatementTree cardList) {
    if (cardList == null) {
      return;
    }
    for (Map.Entry<String, SrosStatementTree> e : cardList.getChildren().entrySet()) {
      SrosStatementTree cardNode = e.getValue();
      Optional<Integer> slot = toIntegerInSpace(e.getKey(), cardNode, SLOT_SPACE, "card slot");
      if (slot.isEmpty()) {
        continue;
      }
      Card card = new Card(slot.get());
      card.setCardType(singleValue(cardNode, "card-type"));
      SrosStatementTree mdaList = cardNode.getChild("mda");
      if (mdaList != null) {
        for (Map.Entry<String, SrosStatementTree> me : mdaList.getChildren().entrySet()) {
          SrosStatementTree mdaNode = me.getValue();
          Optional<Integer> mdaSlot =
              toIntegerInSpace(me.getKey(), mdaNode, SLOT_SPACE, "mda slot");
          if (mdaSlot.isEmpty()) {
            continue;
          }
          Mda mda = new Mda(mdaSlot.get());
          mda.setMdaType(singleValue(mdaNode, "mda-type"));
          card.getMdas().put(mdaSlot.get(), mda);
        }
      }
      _c.getCards().put(slot.get(), card);
    }
  }

  private void extractPorts(@Nullable SrosStatementTree portList) {
    if (portList == null) {
      return;
    }
    for (Map.Entry<String, SrosStatementTree> e : portList.getChildren().entrySet()) {
      String name = e.getKey();
      SrosStatementTree portNode = e.getValue();
      Port port = new Port(name);
      port.setAdminStateEnable(
          toEnum(portNode.getChild("admin-state"), ADMIN_STATE, "admin-state"));
      SrosStatementTree connector = portNode.getChild("connector");
      if (connector != null) {
        port.setBreakout(singleValue(connector, "breakout"));
      }
      _c.getPorts().put(name, port);
    }
  }

  /**
   * Extract {@code lag "<name>"} link-aggregation groups: the name, admin-state, and member port
   * paths (the {@code port} list keyed by port-id). A router interface may bind a LAG by name; the
   * LAG interface's bandwidth is the sum of its members' (see {@link Lag}).
   */
  private void extractLags(@Nullable SrosStatementTree lagList) {
    if (lagList == null) {
      return;
    }
    for (Map.Entry<String, SrosStatementTree> e : lagList.getChildren().entrySet()) {
      String name = unquote(e.getKey());
      SrosStatementTree lagNode = e.getValue();
      Lag lag = new Lag(name);
      Boolean adminUp = toEnum(lagNode.getChild("admin-state"), ADMIN_STATE, "admin-state");
      lag.setAdminStateEnable(!Boolean.FALSE.equals(adminUp));
      SrosStatementTree portList = lagNode.getChild("port");
      if (portList != null) {
        for (String portKey : portList.getChildren().keySet()) {
          lag.getMemberPorts().add(unquote(portKey));
        }
      }
      _c.getLags().put(name, lag);
    }
  }

  // --- routers, interfaces, bgp -----------------------------------------------------------------

  private void extractRouters(@Nullable SrosStatementTree routerList) {
    if (routerList == null) {
      return;
    }
    for (Map.Entry<String, SrosStatementTree> e : routerList.getChildren().entrySet()) {
      String name = unquote(e.getKey());
      SrosStatementTree routerNode = e.getValue();
      Router router = new Router(name);
      toLongInSpace(
              singleValue(routerNode, "autonomous-system"),
              singleValueNode(routerNode, "autonomous-system"),
              AUTONOMOUS_SYSTEM_SPACE,
              "autonomous-system")
          .ifPresent(router::setAutonomousSystem);
      extractInterfaces(router, routerNode.getChild("interface"));
      extractStaticRoutes(router, routerNode.getChild("static-routes"));
      extractAggregates(router, routerNode.getChild("aggregates"));
      extractOspf(router, routerNode.getChild("ospf"));
      extractIsis(router, routerNode.getChild("isis"));
      extractBgp(router, routerNode.getChild("bgp"));
      _c.getRouters().put(name, router);
    }
  }

  /**
   * Extract {@code service vprn "<name>"} instances. A VPRN is a routing instance in its own VRF,
   * so it is modeled as a {@link Router} keyed by the VPRN service-name (which conversion maps to a
   * same-named VRF, just like a non-Base {@code router "<name>"}). It carries the same
   * interface/static-route/OSPF/BGP feature set as the Base router. Only VPRNs are read from the
   * service tree; other service types (epipe, vpls, ies, ...) are not control-plane routers and are
   * left unread.
   */
  private void extractServices(@Nullable SrosStatementTree service) {
    if (service == null) {
      return;
    }
    SrosStatementTree vprnList = service.getChild("vprn");
    if (vprnList == null) {
      return;
    }
    for (Map.Entry<String, SrosStatementTree> e : vprnList.getChildren().entrySet()) {
      String name = unquote(e.getKey());
      SrosStatementTree vprnNode = e.getValue();
      Router router = new Router(name);
      toLongInSpace(
              singleValue(vprnNode, "autonomous-system"),
              singleValueNode(vprnNode, "autonomous-system"),
              AUTONOMOUS_SYSTEM_SPACE,
              "autonomous-system")
          .ifPresent(router::setAutonomousSystem);
      extractInterfaces(router, vprnNode.getChild("interface"));
      extractStaticRoutes(router, vprnNode.getChild("static-routes"));
      extractOspf(router, vprnNode.getChild("ospf"));
      extractIsis(router, vprnNode.getChild("isis"));
      extractBgp(router, vprnNode.getChild("bgp"));
      extractBgpIpvpn(router, vprnNode.getChild("bgp-ipvpn"));
      _c.getRouters().put(name, router);
    }
  }

  /**
   * Extract {@code service vprn "<name>" bgp-ipvpn mpls}: the {@code route-distinguisher} and the
   * {@code vrf-target} route-targets (the single {@code community} form populates both import and
   * export; the {@code import-community}/{@code export-community} form sets them separately). These
   * drive MPLS L3VPN import/export on the device; only the route-distinguisher converts to the VI
   * model (inter-PE VPN-IPv4 import is unmodeled — see {@link
   * org.batfish.vendor.sros.representation.BgpIpvpn}).
   */
  private void extractBgpIpvpn(Router router, @Nullable SrosStatementTree bgpIpvpn) {
    if (bgpIpvpn == null) {
      return;
    }
    SrosStatementTree mpls = bgpIpvpn.getChild("mpls");
    if (mpls == null) {
      return;
    }
    BgpIpvpn ipvpn = new BgpIpvpn();
    String rd = singleValue(mpls, "route-distinguisher");
    ipvpn.setRouteDistinguisher(rd == null ? null : unquote(rd));
    SrosStatementTree vrfTarget = mpls.getChild("vrf-target");
    if (vrfTarget != null) {
      String both = singleValue(vrfTarget, "community");
      if (both != null) {
        ipvpn.getImportRouteTargets().add(unquote(both));
        ipvpn.getExportRouteTargets().add(unquote(both));
      }
      String imp = singleValue(vrfTarget, "import-community");
      if (imp != null) {
        ipvpn.getImportRouteTargets().add(unquote(imp));
      }
      String exp = singleValue(vrfTarget, "export-community");
      if (exp != null) {
        ipvpn.getExportRouteTargets().add(unquote(exp));
      }
    }
    router.setBgpIpvpn(ipvpn);
  }

  private void extractInterfaces(Router router, @Nullable SrosStatementTree ifaceList) {
    if (ifaceList == null) {
      return;
    }
    for (Map.Entry<String, SrosStatementTree> e : ifaceList.getChildren().entrySet()) {
      String name = unquote(e.getKey());
      SrosStatementTree ifaceNode = e.getValue();
      RouterInterface iface = new RouterInterface(name);
      iface.setPort(singleValue(ifaceNode, "port"));
      SrosStatementTree primary = navigate(ifaceNode, "ipv4", "primary");
      if (primary != null) {
        iface.setPrimaryAddress(
            toIp(
                singleValue(primary, "address"), singleValueNode(primary, "address"), "interface"));
        toIntegerInSpace(
                singleValue(primary, "prefix-length"),
                singleValueNode(primary, "prefix-length"),
                IPV4_PREFIX_LENGTH_SPACE,
                "prefix-length")
            .ifPresent(iface::setPrimaryPrefixLength);
      }
      router.getInterfaces().put(name, iface);
    }
  }

  /**
   * Extract {@code static-routes route <prefix> route-type <unicast|multicast>} entries. Each route
   * is reached via one or more {@code next-hop <ip>} entries (multiple = ECMP, one VI {@link
   * StaticRoute} per next-hop) or a {@code blackhole}; each sub-context carries an {@code
   * admin-state} (a route is only RIB-installed when it is {@code enable} — see {@link
   * StaticRoute}), and optional {@code metric}/{@code preference}. Only the unicast IPv4 case is
   * modeled here; a non-IPv4 prefix is warned and skipped by {@link #toPrefix}.
   */
  private void extractStaticRoutes(Router router, @Nullable SrosStatementTree staticRoutes) {
    if (staticRoutes == null) {
      return;
    }
    SrosStatementTree routeList = staticRoutes.getChild("route");
    if (routeList == null) {
      return;
    }
    for (Map.Entry<String, SrosStatementTree> e : routeList.getChildren().entrySet()) {
      SrosStatementTree prefixNode = e.getValue();
      Prefix prefix = toPrefix(e.getKey(), prefixNode);
      if (prefix == null) {
        continue;
      }
      // The route-type ("unicast"/"multicast") is the next path word and keys the route with the
      // prefix in YANG; the next-hop/blackhole contexts hang under that route-type node.
      SrosStatementTree routeType = prefixNode.getChild("route-type");
      SrosStatementTree typeBody = routeType == null ? null : firstChild(routeType);
      if (typeBody == null) {
        continue;
      }
      SrosStatementTree nextHop = typeBody.getChild("next-hop");
      SrosStatementTree blackhole = typeBody.getChild("blackhole");
      if (nextHop != null) {
        // next-hop is a YANG list keyed by the next-hop IP; more than one entry is an ECMP route.
        // Each entry carries its own admin-state/metric/preference, so emit one StaticRoute per
        // next-hop — VI ECMP is the set of equal-preference legs to the same prefix, and Batfish's
        // own best-preference selection installs the equal-best legs and drops worse-preference
        // ones (confirmed on SR-SIM 26.3.R1: two equal-preference next-hops both install; an
        // unequal pair installs only the lower-preference leg). See
        // https://github.com/batfish/batfish/issues/9989.
        for (Map.Entry<String, SrosStatementTree> nhe : nextHop.getChildren().entrySet()) {
          router
              .getStaticRoutes()
              .add(toStaticRoute(prefix, nhe.getValue(), unquote(nhe.getKey())));
        }
      } else if (blackhole != null) {
        router.getStaticRoutes().add(toStaticRoute(prefix, blackhole, null));
      } else {
        // Neither next-hop nor blackhole: an unmodeled route shape (e.g. indirect, cpe-check).
        // Warn rather than silently skip.
        warnf(
            typeBody,
            "static route %s has no modeled next-hop or blackhole; not converted",
            prefix);
      }
    }
  }

  /**
   * Build a {@link StaticRoute} for one leg: a {@code next-hop} entry (whose list key {@code
   * nextHopKey} is the next-hop IP) or, when {@code nextHopKey} is {@code null}, a {@code
   * blackhole}. Both shapes carry the same {@code admin-state}/{@code metric}/{@code preference}
   * leaves on their context {@code leg}.
   */
  private @Nonnull StaticRoute toStaticRoute(
      Prefix prefix, SrosStatementTree leg, @Nullable String nextHopKey) {
    StaticRoute route = new StaticRoute(prefix);
    if (nextHopKey == null) {
      route.setBlackhole(true);
    } else {
      route.setNextHopIp(toIp(nextHopKey, leg, "static-route next-hop"));
    }
    Boolean adminUp = toEnum(leg.getChild("admin-state"), ADMIN_STATE, "admin-state");
    // admin-state defaults to disable for a static route: it is installed only when explicitly
    // enable.
    route.setAdminStateEnable(firstNonNull(adminUp, Boolean.FALSE));
    applyMetricPreference(route, leg);
    return route;
  }

  /**
   * Apply the {@code metric}/{@code preference} leaves from a static-route next-hop or blackhole
   * context onto {@code route}, defaulting per YANG when absent.
   */
  private void applyMetricPreference(StaticRoute route, SrosStatementTree ctx) {
    toIntegerInSpace(
            singleValue(ctx, "metric"),
            singleValueNode(ctx, "metric"),
            STATIC_METRIC_SPACE,
            "static-route metric")
        .ifPresent(route::setMetric);
    toIntegerInSpace(
            singleValue(ctx, "preference"),
            singleValueNode(ctx, "preference"),
            ROUTE_PREFERENCE_SPACE,
            "static-route preference")
        .ifPresent(route::setPreference);
  }

  /**
   * Extract {@code router "Base" aggregates aggregate <prefix>} entries: the prefix, {@code
   * summary-only}, and any {@code community} values. An aggregate installs as a discard summary
   * route (preference 130) only when a contributing more-specific exists (see {@link Aggregate}).
   */
  private void extractAggregates(Router router, @Nullable SrosStatementTree aggregates) {
    if (aggregates == null) {
      return;
    }
    SrosStatementTree aggList = aggregates.getChild("aggregate");
    if (aggList == null) {
      return;
    }
    for (Map.Entry<String, SrosStatementTree> e : aggList.getChildren().entrySet()) {
      SrosStatementTree aggNode = e.getValue();
      Prefix prefix = toPrefix(e.getKey(), aggNode);
      if (prefix == null) {
        continue;
      }
      Aggregate agg = new Aggregate(prefix);
      Boolean summaryOnly = toBoolean(aggNode.getChild("summary-only"), "aggregate summary-only");
      agg.setSummaryOnly(firstNonNull(summaryOnly, Boolean.FALSE));
      for (String c : policyNames(aggNode.getChild("community"))) {
        agg.getCommunities().add(c);
      }
      router.getAggregates().add(agg);
    }
  }

  /** The {@code type} leaf's value word (e.g. {@code through}/{@code range}), or {@code null}. */
  private static @Nullable String typeWord(SrosStatementTree entryNode) {
    return singleValue(entryNode, "type");
  }

  /**
   * Warn on illegal prefix-list length-bound combinations rather than silently building a bogus or
   * over-approximated filter: a {@code through} entry without a {@code through-length}; a {@code
   * range} entry missing {@code start-length} or {@code end-length}; an inverted range ({@code
   * start-length > end-length}); or a bound shorter than the prefix's own length. The entry is
   * still kept (conversion over-approximates a missing bound), but the warning makes it visible.
   */
  private void warnIllegalPrefixListBounds(PrefixListEntry ple, SrosStatementTree ctxNode) {
    int len = ple.getPrefix().getPrefixLength();
    switch (ple.getType()) {
      case THROUGH -> {
        Integer through = ple.getThroughLength();
        if (through == null) {
          warn(ctxNode, "prefix-list 'through' entry is missing through-length");
        } else if (through < len) {
          warnf(
              ctxNode,
              "prefix-list through-length %d is shorter than the prefix length %d",
              through,
              len);
        }
      }
      case RANGE -> {
        Integer start = ple.getStartLength();
        Integer end = ple.getEndLength();
        if (start == null || end == null) {
          warn(ctxNode, "prefix-list 'range' entry is missing start-length or end-length");
        } else if (start > end) {
          warnf(
              ctxNode,
              "prefix-list range start-length %d is greater than end-length %d",
              start,
              end);
        } else if (start < len) {
          warnf(
              ctxNode,
              "prefix-list range start-length %d is shorter than the prefix length %d",
              start,
              len);
        }
      }
      default -> {
        // exact/longer/to/address-mask carry no through/range length bounds to validate here.
      }
    }
  }

  // --- ospf -------------------------------------------------------------------------------------

  /** OSPF interface {@code metric} (cost): YANG {@code uint32 range "1..65535"}. */
  private static final IntegerSpace OSPF_METRIC_SPACE = IntegerSpace.of(new SubRange(1, 65535));

  /** IS-IS interface {@code metric}: YANG wide-metric {@code uint32 range "1..16777215"}. */
  private static final IntegerSpace ISIS_METRIC_SPACE = IntegerSpace.of(new SubRange(1, 16777215));

  /** OSPF {@code interface-type} enumeration (the modeled subset). */
  private static final Map<String, OspfAreaInterface.InterfaceType> OSPF_INTERFACE_TYPE =
      ImmutableMap.of(
          "broadcast", OspfAreaInterface.InterfaceType.BROADCAST,
          "point-to-point", OspfAreaInterface.InterfaceType.POINT_TO_POINT);

  /** IS-IS interface {@code interface-type} enumeration (nokia-types-isis:interface-type). */
  private static final Map<String, IsisProcessInterface.InterfaceType> ISIS_INTERFACE_TYPE =
      ImmutableMap.of(
          "broadcast", IsisProcessInterface.InterfaceType.BROADCAST,
          "point-to-point", IsisProcessInterface.InterfaceType.POINT_TO_POINT);

  /** IS-IS {@code level-capability} enumeration (nokia-types-isis:level). */
  private static final Map<String, IsisProcess.LevelCapability> ISIS_LEVEL_CAPABILITY =
      ImmutableMap.of(
          "1", IsisProcess.LevelCapability.LEVEL_1,
          "2", IsisProcess.LevelCapability.LEVEL_2,
          "1/2", IsisProcess.LevelCapability.LEVEL_1_2);

  /**
   * Extract {@code router "<name>" ospf <instance>}: router-id, admin-state, and the per-area
   * interfaces (with interface-type and metric/cost). Only the first/0 OSPF instance is modeled.
   */
  private void extractOspf(Router router, @Nullable SrosStatementTree ospfList) {
    if (ospfList == null || ospfList.getChildren().isEmpty()) {
      return;
    }
    // ospf is a list keyed by instance; model the first (typically instance 0).
    Map.Entry<String, SrosStatementTree> e = ospfList.getChildren().entrySet().iterator().next();
    SrosStatementTree ospfNode = e.getValue();
    Integer instance = toInstanceId(e.getKey(), ospfNode, "ospf");
    if (instance == null) {
      return;
    }
    OspfProcess proc = new OspfProcess(instance);
    proc.setRouterId(
        toIp(
            singleValue(ospfNode, "router-id"),
            singleValueNode(ospfNode, "router-id"),
            "ospf router-id"));
    Boolean adminUp = toEnum(ospfNode.getChild("admin-state"), ADMIN_STATE, "admin-state");
    // admin-state defaults to enable for an OSPF/IS-IS process: enabled unless explicitly disable.
    proc.setAdminStateEnable(firstNonNull(adminUp, Boolean.TRUE));
    // OSPF route preference (admin distance) for internal routes; SR-OS default 10.
    toIntegerInSpace(
            singleValue(ospfNode, "preference"),
            singleValueNode(ospfNode, "preference"),
            ROUTE_PREFERENCE_SPACE,
            "ospf preference")
        .ifPresent(proc::setPreference);

    SrosStatementTree areaList = ospfNode.getChild("area");
    if (areaList != null) {
      for (Map.Entry<String, SrosStatementTree> ae : areaList.getChildren().entrySet()) {
        String areaId = unquote(ae.getKey());
        SrosStatementTree areaNode = ae.getValue();
        OspfArea area = new OspfArea(areaId);
        SrosStatementTree ifaceList = areaNode.getChild("interface");
        if (ifaceList != null) {
          for (Map.Entry<String, SrosStatementTree> ie : ifaceList.getChildren().entrySet()) {
            String ifName = unquote(ie.getKey());
            SrosStatementTree ifNode = ie.getValue();
            OspfAreaInterface ospfIf = new OspfAreaInterface(ifName);
            ospfIf.setInterfaceType(
                toEnum(
                    ifNode.getChild("interface-type"), OSPF_INTERFACE_TYPE, "ospf interface-type"));
            toIntegerInSpace(
                    singleValue(ifNode, "metric"),
                    singleValueNode(ifNode, "metric"),
                    OSPF_METRIC_SPACE,
                    "ospf interface metric")
                .ifPresent(ospfIf::setMetric);
            area.getInterfaces().put(ifName, ospfIf);
          }
        }
        proc.getAreas().put(areaId, area);
      }
    }
    router.setOspfProcess(proc);
  }

  /**
   * Extract {@code router "<name>" isis <instance>}: admin-state, system-id, area-address(es),
   * level-capability, and the per-interface {@code interface-type}/{@code passive}. Only the
   * first/0 IS-IS instance is modeled.
   */
  private void extractIsis(Router router, @Nullable SrosStatementTree isisList) {
    if (isisList == null || isisList.getChildren().isEmpty()) {
      return;
    }
    // isis is a list keyed by instance; model the first (typically instance 0).
    Map.Entry<String, SrosStatementTree> e = isisList.getChildren().entrySet().iterator().next();
    SrosStatementTree isisNode = e.getValue();
    Integer instance = toInstanceId(e.getKey(), isisNode, "isis");
    if (instance == null) {
      return;
    }
    IsisProcess proc = new IsisProcess(instance);
    Boolean adminUp = toEnum(isisNode.getChild("admin-state"), ADMIN_STATE, "admin-state");
    // admin-state defaults to enable for an OSPF/IS-IS process: enabled unless explicitly disable.
    proc.setAdminStateEnable(firstNonNull(adminUp, Boolean.TRUE));
    proc.setSystemId(singleValue(isisNode, "system-id"));
    proc.getAreaAddresses().addAll(policyNames(isisNode.getChild("area-address")));
    IsisProcess.LevelCapability level =
        toEnum(
            isisNode.getChild("level-capability"), ISIS_LEVEL_CAPABILITY, "isis level-capability");
    if (level != null) {
      proc.setLevelCapability(level);
    }

    SrosStatementTree ifaceList = isisNode.getChild("interface");
    if (ifaceList != null) {
      for (Map.Entry<String, SrosStatementTree> ie : ifaceList.getChildren().entrySet()) {
        String ifName = unquote(ie.getKey());
        SrosStatementTree ifNode = ie.getValue();
        IsisProcessInterface isisIf = new IsisProcessInterface(ifName);
        isisIf.setInterfaceType(
            toEnum(ifNode.getChild("interface-type"), ISIS_INTERFACE_TYPE, "isis interface-type"));
        Boolean passive = toBoolean(ifNode.getChild("passive"), "isis interface passive");
        isisIf.setPassive(firstNonNull(passive, Boolean.FALSE));
        // Metric is configured per level (`interface "<name>" level <N> metric <M>`); SR-OS has no
        // interface-wide metric leaf. Extract each level's metric independently; the default 10
        // applies per level in conversion when unset.
        SrosStatementTree ifLevelList = ifNode.getChild("level");
        if (ifLevelList != null) {
          for (int lvl : new int[] {1, 2}) {
            SrosStatementTree levelNode = ifLevelList.getChild(Integer.toString(lvl));
            if (levelNode == null) {
              continue;
            }
            int metricLevel = lvl;
            toIntegerInSpace(
                    singleValue(levelNode, "metric"),
                    singleValueNode(levelNode, "metric"),
                    ISIS_METRIC_SPACE,
                    "isis interface metric")
                .ifPresent(m -> isisIf.setMetric(metricLevel, m));
          }
        }
        proc.getInterfaces().put(ifName, isisIf);
      }
    }
    router.setIsisProcess(proc);
  }

  /**
   * Parse an IGP process instance-id (the list key, e.g. the {@code 0} in {@code ospf 0}). The YANG
   * key is an integer; a non-numeric key is malformed input, so warn and return {@code null} (skip
   * the process) rather than silently defaulting to instance 0.
   */
  private @Nullable Integer toInstanceId(String key, SrosStatementTree ctx, String protocol) {
    try {
      return Integer.parseInt(key);
    } catch (NumberFormatException ex) {
      warnf(ctx, "%s instance '%s' is not an integer; skipping", protocol, key);
      return null;
    }
  }

  /** The first (insertion-order) child node of {@code node}, or {@code null} if it has none. */
  private static @Nullable SrosStatementTree firstChild(SrosStatementTree node) {
    return node.getChildren().isEmpty() ? null : node.getChildren().values().iterator().next();
  }

  private void extractBgp(Router router, @Nullable SrosStatementTree bgpNode) {
    if (bgpNode == null) {
      return;
    }
    BgpProcess proc = new BgpProcess();
    proc.setRouterId(
        toIp(
            singleValue(bgpNode, "router-id"), singleValueNode(bgpNode, "router-id"), "router-id"));

    SrosStatementTree groupList = bgpNode.getChild("group");
    if (groupList != null) {
      for (Map.Entry<String, SrosStatementTree> e : groupList.getChildren().entrySet()) {
        String name = unquote(e.getKey());
        SrosStatementTree groupNode = e.getValue();
        defineStructure(SrosStructureType.BGP_GROUP, name, groupNode);
        BgpGroup group = new BgpGroup(name);
        group.setType(toEnum(groupNode.getChild("type"), PEER_TYPE, "bgp peer type"));
        toLongInSpace(
                singleValue(groupNode, "peer-as"),
                singleValueNode(groupNode, "peer-as"),
                AUTONOMOUS_SYSTEM_SPACE,
                "peer-as")
            .ifPresent(group::setPeerAs);
        SrosStatementTree groupImport = navigate(groupNode, "import", "policy");
        SrosStatementTree groupExport = navigate(groupNode, "export", "policy");
        group.getImportPolicies().addAll(policyNames(groupImport));
        group.getExportPolicies().addAll(policyNames(groupExport));
        referencePolicies(groupImport, SrosStructureUsage.BGP_GROUP_IMPORT_POLICY);
        referencePolicies(groupExport, SrosStructureUsage.BGP_GROUP_EXPORT_POLICY);
        SrosStatementTree groupCluster = groupNode.getChild("cluster");
        if (groupCluster != null) {
          group.setClusterId(
              toIp(
                  singleValue(groupCluster, "cluster-id"),
                  singleValueNode(groupCluster, "cluster-id"),
                  "bgp cluster-id"));
        }
        group.setNextHopSelf(toBoolean(groupNode.getChild("next-hop-self"), "next-hop-self"));
        proc.getGroups().put(name, group);
      }
    }

    SrosStatementTree neighborList = bgpNode.getChild("neighbor");
    if (neighborList != null) {
      for (Map.Entry<String, SrosStatementTree> e : neighborList.getChildren().entrySet()) {
        String ip = unquote(e.getKey());
        SrosStatementTree nbrNode = e.getValue();
        BgpNeighbor neighbor = new BgpNeighbor(ip);
        SrosStatementTree groupValue = singleValueNode(nbrNode, "group");
        String group = singleValue(nbrNode, "group");
        if (group != null) {
          neighbor.setGroup(unquote(group));
          referenceStructure(
              SrosStructureType.BGP_GROUP,
              unquote(group),
              SrosStructureUsage.BGP_NEIGHBOR_GROUP,
              groupValue);
        }
        neighbor.setType(toEnum(nbrNode.getChild("type"), PEER_TYPE, "bgp peer type"));
        toLongInSpace(
                singleValue(nbrNode, "peer-as"),
                singleValueNode(nbrNode, "peer-as"),
                AUTONOMOUS_SYSTEM_SPACE,
                "peer-as")
            .ifPresent(neighbor::setPeerAs);
        SrosStatementTree nbrImport = navigate(nbrNode, "import", "policy");
        SrosStatementTree nbrExport = navigate(nbrNode, "export", "policy");
        neighbor.getImportPolicies().addAll(policyNames(nbrImport));
        neighbor.getExportPolicies().addAll(policyNames(nbrExport));
        referencePolicies(nbrImport, SrosStructureUsage.BGP_NEIGHBOR_IMPORT_POLICY);
        referencePolicies(nbrExport, SrosStructureUsage.BGP_NEIGHBOR_EXPORT_POLICY);
        SrosStatementTree nbrCluster = nbrNode.getChild("cluster");
        if (nbrCluster != null) {
          neighbor.setClusterId(
              toIp(
                  singleValue(nbrCluster, "cluster-id"),
                  singleValueNode(nbrCluster, "cluster-id"),
                  "bgp cluster-id"));
        }
        neighbor.setNextHopSelf(toBoolean(nbrNode.getChild("next-hop-self"), "next-hop-self"));
        proc.getNeighbors().put(ip, neighbor);
      }
    }
    // Resolve group -> neighbor inheritance now, in the representation, so conversion reads
    // fully-populated neighbors (NX-OS-style doInherit, not inline in conversion).
    for (BgpNeighbor neighbor : proc.getNeighbors().values()) {
      neighbor.inheritFrom(
          neighbor.getGroup() == null ? null : proc.getGroups().get(neighbor.getGroup()));
    }
    router.setBgpProcess(proc);
  }

  // --- policy-options ---------------------------------------------------------------------------

  private void extractPolicyOptions(@Nullable SrosStatementTree po) {
    if (po == null) {
      return;
    }
    SrosStatementTree communities = po.getChild("community");
    if (communities != null) {
      for (Map.Entry<String, SrosStatementTree> e : communities.getChildren().entrySet()) {
        String name = unquote(e.getKey());
        SrosStatementTree commNode = e.getValue();
        defineStructure(SrosStructureType.COMMUNITY, name, commNode);
        Community community = new Community(name);
        SrosStatementTree memberNode = commNode.getChild("member");
        if (memberNode != null) {
          for (String member : memberNode.getChildren().keySet()) {
            community.getMembers().add(unquote(member));
          }
        }
        _c.getCommunities().put(name, community);
      }
    }

    SrosStatementTree asPaths = po.getChild("as-path");
    if (asPaths != null) {
      for (Map.Entry<String, SrosStatementTree> e : asPaths.getChildren().entrySet()) {
        String name = unquote(e.getKey());
        SrosStatementTree apNode = e.getValue();
        AsPathList ap = new AsPathList(name);
        String expr = singleValue(apNode, "expression");
        ap.setExpression(expr == null ? null : unquote(expr));
        _c.getAsPathLists().put(name, ap);
      }
    }

    SrosStatementTree prefixLists = po.getChild("prefix-list");
    if (prefixLists != null) {
      for (Map.Entry<String, SrosStatementTree> e : prefixLists.getChildren().entrySet()) {
        String name = unquote(e.getKey());
        SrosStatementTree plNode = e.getValue();
        defineStructure(SrosStructureType.PREFIX_LIST, name, plNode);
        PrefixList pl = new PrefixList(name);
        SrosStatementTree prefixNode = plNode.getChild("prefix");
        if (prefixNode != null) {
          for (Map.Entry<String, SrosStatementTree> pe : prefixNode.getChildren().entrySet()) {
            Prefix prefix = toPrefix(pe.getKey(), pe.getValue());
            if (prefix == null) {
              continue;
            }
            SrosStatementTree entryNode = pe.getValue();
            PrefixListEntry.Type type =
                toEnum(entryNode.getChild("type"), PREFIX_LIST_TYPE, "prefix-list match type");
            if (type == null) {
              continue;
            }
            PrefixListEntry ple = new PrefixListEntry(prefix, type);
            // through/range carry length bounds in the block that hangs off the type value word,
            // i.e. under `type <through|range> { ... }` -> entryNode.type.<value>.<bound>.
            SrosStatementTree typeBody = navigate(entryNode, "type", typeWord(entryNode));
            if (typeBody != null) {
              toIntegerInSpace(
                      singleValue(typeBody, "through-length"),
                      singleValueNode(typeBody, "through-length"),
                      IPV4_PREFIX_LENGTH_SPACE,
                      "prefix-list through-length")
                  .ifPresent(ple::setThroughLength);
              toIntegerInSpace(
                      singleValue(typeBody, "start-length"),
                      singleValueNode(typeBody, "start-length"),
                      IPV4_PREFIX_LENGTH_SPACE,
                      "prefix-list start-length")
                  .ifPresent(ple::setStartLength);
              toIntegerInSpace(
                      singleValue(typeBody, "end-length"),
                      singleValueNode(typeBody, "end-length"),
                      IPV4_PREFIX_LENGTH_SPACE,
                      "prefix-list end-length")
                  .ifPresent(ple::setEndLength);
              // `to` carries a to-prefix list (each nested in the base prefix); `address-mask`
              // carries a mask-pattern list. Both key their list entries by the value word.
              SrosStatementTree toPrefixes = typeBody.getChild("to-prefix");
              if (toPrefixes != null) {
                for (Map.Entry<String, SrosStatementTree> tpe :
                    toPrefixes.getChildren().entrySet()) {
                  Prefix tp = toPrefix(tpe.getKey(), tpe.getValue());
                  if (tp != null) {
                    ple.getToPrefixes().add(tp);
                  }
                }
              }
              SrosStatementTree maskPatterns = typeBody.getChild("mask-pattern");
              if (maskPatterns != null) {
                for (Map.Entry<String, SrosStatementTree> mpe :
                    maskPatterns.getChildren().entrySet()) {
                  Ip mask = toIp(unquote(mpe.getKey()), mpe.getValue(), "prefix-list mask-pattern");
                  if (mask != null) {
                    ple.getMaskPatterns().add(mask);
                  }
                }
              }
            }
            // Warn against the type value node (e.g. the `through`/`range` word), which carries a
            // source context even when the entry's bound block is empty.
            warnIllegalPrefixListBounds(ple, typeBody != null ? typeBody : entryNode);
            pl.getEntries().add(ple);
          }
        }
        _c.getPrefixLists().put(name, pl);
      }
    }

    SrosStatementTree policyStatements = po.getChild("policy-statement");
    if (policyStatements != null) {
      for (Map.Entry<String, SrosStatementTree> e : policyStatements.getChildren().entrySet()) {
        String name = unquote(e.getKey());
        SrosStatementTree psNode = e.getValue();
        defineStructure(SrosStructureType.POLICY_STATEMENT, name, psNode);
        PolicyStatement ps = new PolicyStatement(name);
        SrosStatementTree entryList = psNode.getChild("entry");
        if (entryList != null) {
          for (Map.Entry<String, SrosStatementTree> ee : entryList.getChildren().entrySet()) {
            SrosStatementTree entryNode = ee.getValue();
            Optional<Long> entryId =
                toLongInSpace(ee.getKey(), entryNode, ENTRY_ID_SPACE, "policy entry-id");
            if (entryId.isEmpty()) {
              continue;
            }
            PolicyStatementEntry entry = new PolicyStatementEntry(entryId.get());
            SrosStatementTree fromPfx = navigate(entryNode, "from", "prefix-list");
            entry.getFromPrefixLists().addAll(policyNames(fromPfx));
            referenceStructures(
                SrosStructureType.PREFIX_LIST,
                fromPfx,
                SrosStructureUsage.POLICY_STATEMENT_FROM_PREFIX_LIST);
            // from protocol name [static direct bgp ...] — the protocol(s) the route was learned
            // from. Each value is resolved against the FromProtocol enum; an unrecognized protocol
            // is warned and dropped (not silently kept as an unmatchable string).
            SrosStatementTree fromProto = navigate(entryNode, "from", "protocol", "name");
            if (fromProto != null) {
              for (Map.Entry<String, SrosStatementTree> pe : fromProto.getChildren().entrySet()) {
                FromProtocol fp = FROM_PROTOCOL.get(unquote(pe.getKey()));
                if (fp == null) {
                  warnf(pe.getValue(), "unrecognized from-protocol '%s'", unquote(pe.getKey()));
                  continue;
                }
                entry.getFromProtocols().add(fp);
              }
            }
            // from community name <name> — the route's communities must match the named list.
            SrosStatementTree fromCommNode = navigate(entryNode, "from", "community");
            String fromComm = fromCommNode == null ? null : singleValue(fromCommNode, "name");
            if (fromComm != null) {
              entry.getFromCommunities().add(unquote(fromComm));
            }
            // from as-path name <name> — the route's AS path must match the named as-path list.
            SrosStatementTree fromApNode = navigate(entryNode, "from", "as-path");
            String fromAsPath = fromApNode == null ? null : singleValue(fromApNode, "name");
            if (fromAsPath != null) {
              entry.getFromAsPaths().add(unquote(fromAsPath));
            }
            SrosStatementTree action = entryNode.getChild("action");
            entry.setAction(
                toEnum(
                    action == null ? null : action.getChild("action-type"),
                    POLICY_ACTION,
                    "action-type"));
            extractEntrySetClauses(entry, action);
            ps.getEntries().put(entryId.get(), entry);
          }
        }
        ps.setDefaultAction(
            toEnum(
                navigate(psNode, "default-action", "action-type"), POLICY_ACTION, "action-type"));
        _c.getPolicyStatements().put(name, ps);
      }
    }
  }

  /**
   * Extract the modeled {@code action} set-clauses of a policy entry: {@code metric set <n>} (BGP
   * MED), {@code as-path-prepend as-path <asn> [repeat <n>]}, and {@code community add [...]}.
   */
  private void extractEntrySetClauses(
      PolicyStatementEntry entry, @Nullable SrosStatementTree action) {
    if (action == null) {
      return;
    }
    // metric set <n>
    SrosStatementTree metricSet = navigate(action, "metric", "set");
    toLongInSpace(
            singleKey(metricSet),
            metricSet == null ? null : firstChild(metricSet),
            POLICY_METRIC_SPACE,
            "policy action metric")
        .ifPresent(entry::setSetMetric);

    // metric add <n>
    SrosStatementTree metricAdd = navigate(action, "metric", "add");
    toLongInSpace(
            singleKey(metricAdd),
            metricAdd == null ? null : firstChild(metricAdd),
            POLICY_METRIC_SPACE,
            "policy action metric add")
        .ifPresent(entry::setMetricAdd);

    // local-preference <n>
    toLongInSpace(
            singleValue(action, "local-preference"),
            singleValueNode(action, "local-preference"),
            LOCAL_PREFERENCE_SPACE,
            "policy action local-preference")
        .ifPresent(entry::setSetLocalPreference);

    // origin <igp|egp|incomplete>
    entry.setSetOrigin(toEnum(action.getChild("origin"), ORIGIN_TYPE, "policy action origin"));

    // as-path-prepend as-path <asn> [repeat <n>]
    SrosStatementTree prepend = action.getChild("as-path-prepend");
    if (prepend != null) {
      SrosStatementTree asPath = prepend.getChild("as-path");
      toLongInSpace(
              singleKey(asPath),
              asPath == null ? null : firstChild(asPath),
              AUTONOMOUS_SYSTEM_SPACE,
              "as-path-prepend as-path")
          .ifPresent(entry::setAsPathPrependAsn);
      SrosStatementTree repeat = prepend.getChild("repeat");
      toIntegerInSpace(
              singleKey(repeat),
              repeat == null ? null : firstChild(repeat),
              AS_PATH_PREPEND_REPEAT_SPACE,
              "as-path-prepend repeat")
          .ifPresent(entry::setAsPathPrependRepeat);
    }

    // community add [...]
    SrosStatementTree communityAdd = navigate(action, "community", "add");
    if (communityAdd != null) {
      for (String c : communityAdd.getChildren().keySet()) {
        entry.getCommunityAdds().add(unquote(c));
      }
      referenceStructures(
          SrosStructureType.COMMUNITY,
          communityAdd,
          SrosStructureUsage.POLICY_STATEMENT_ACTION_COMMUNITY);
    }
  }

  // --- value helpers ----------------------------------------------------------------------------

  /** The single child key of {@code node}'s {@code leaf} child (a leaf value), or {@code null}. */
  private static @Nullable String singleValue(SrosStatementTree node, String leaf) {
    SrosStatementTree leafNode = node.getChild(leaf);
    return singleKey(leafNode);
  }

  /**
   * The value node of a single-valued leaf — i.e. the (single) child of {@code node}'s {@code leaf}
   * child. For {@code autonomous-system 5000000000} under a router node, this is the {@code
   * 5000000000} node, which is the deepest node of that statement and so carries its source context
   * (the leaf-name {@code autonomous-system} node, being an interior path word, does not). Returns
   * {@code null} if the leaf is absent or not single-valued, in which case warnings degrade to a
   * context-free red-flag.
   */
  private static @Nullable SrosStatementTree singleValueNode(SrosStatementTree node, String leaf) {
    SrosStatementTree leafNode = node.getChild(leaf);
    if (leafNode == null || leafNode.getChildren().size() != 1) {
      return null;
    }
    return leafNode.getChildren().values().iterator().next();
  }

  private static @Nullable String singleKey(@Nullable SrosStatementTree node) {
    if (node == null || node.getChildren().size() != 1) {
      return null;
    }
    return node.getChildren().keySet().iterator().next();
  }

  /** The ordered, unquoted children of a leaf-list node (policy names), or empty. */
  private static @Nonnull List<String> policyNames(@Nullable SrosStatementTree leafListNode) {
    if (leafListNode == null) {
      return List.of();
    }
    return leafListNode.getChildren().keySet().stream()
        .map(SrosFeatureExtractor::unquote)
        .collect(java.util.stream.Collectors.toList());
  }

  private static @Nullable SrosStatementTree navigate(SrosStatementTree start, String... path) {
    SrosStatementTree node = start;
    for (String word : path) {
      node = node.getChild(word);
      if (node == null) {
        return null;
      }
    }
    return node;
  }

  /**
   * Convert an integer leaf value to an {@link Integer} if it parses as a 32-bit decimal and is
   * contained in {@code space}, else {@link Optional#empty} (with a {@link Warnings.ParseWarning}).
   *
   * <p>Unlike the equivalent helpers in grammar-driven extractors (e.g. flatjuniper's {@code
   * toIntegerInSpace}), the value is NOT grammar-guaranteed numeric — every SR-OS leaf value is a
   * generic word — so a malformed value is warned here rather than assumed away. {@code ctxNode} is
   * the tree node whose source context locates the value for the warning; {@code name} names it. A
   * {@code null} input is {@link Optional#empty} with no warning (absent leaf == YANG default).
   */
  private @Nonnull Optional<Integer> toIntegerInSpace(
      @Nullable String text, @Nullable SrosStatementTree ctxNode, IntegerSpace space, String name) {
    if (text == null) {
      return Optional.empty();
    }
    SrosStatementTree node = requireNonNull(ctxNode);
    int num;
    try {
      num = Integer.parseInt(text);
    } catch (NumberFormatException e) {
      warnf(node, "Expected %s in range %s, but got '%s'", name, space, text);
      return Optional.empty();
    }
    if (!space.contains(num)) {
      warnf(node, "Expected %s in range %s, but got '%d'", name, space, num);
      return Optional.empty();
    }
    return Optional.of(num);
  }

  /**
   * Convert an integer leaf value to a {@link Long} if it parses as a 64-bit decimal and is
   * contained in {@code space}, else {@link Optional#empty} (with a {@link Warnings.ParseWarning}).
   * See {@link #toIntegerInSpace} for why malformed input is warned rather than assumed away.
   */
  private @Nonnull Optional<Long> toLongInSpace(
      @Nullable String text, @Nullable SrosStatementTree ctxNode, LongSpace space, String name) {
    if (text == null) {
      return Optional.empty();
    }
    SrosStatementTree node = requireNonNull(ctxNode);
    long num;
    try {
      num = Long.parseLong(text);
    } catch (NumberFormatException e) {
      warnf(node, "Expected %s in range %s, but got '%s'", name, space, text);
      return Optional.empty();
    }
    if (!space.contains(num)) {
      warnf(node, "Expected %s in range %s, but got '%d'", name, space, num);
      return Optional.empty();
    }
    return Optional.of(num);
  }

  private @Nullable Ip toIp(
      @Nullable String text, @Nullable SrosStatementTree ctxNode, String what) {
    if (text == null) {
      return null;
    }
    Optional<Ip> ip = Ip.tryParse(text);
    if (ip.isEmpty()) {
      warnf(requireNonNull(ctxNode), "expected an IPv4 address %s but got '%s'", what, text);
    }
    return ip.orElse(null);
  }

  private @Nullable Prefix toPrefix(String text, SrosStatementTree ctxNode) {
    Optional<Prefix> prefix = Prefix.tryParse(text);
    if (prefix.isEmpty()) {
      warnf(ctxNode, "expected an IPv4 prefix but got '%s'", text);
    }
    return prefix.orElse(null);
  }

  /**
   * Resolve the single value of {@code leafNode} (an enumerated leaf, e.g. {@code admin-state} or
   * {@code action-type}) against {@code values}. Returns {@code null} with no warning if the leaf
   * is absent or not single-valued (absent leaf == YANG default), and {@code null} with a
   * line-stamped {@link Warnings.ParseWarning} ({@code what} names the leaf) if the value is
   * outside the enumeration. The value node carries the source context, so the warning points at
   * the value.
   */
  private <T> @Nullable T toEnum(
      @Nullable SrosStatementTree leafNode, Map<String, T> values, String what) {
    String value = singleKey(leafNode);
    if (value == null) {
      return null;
    }
    T result = values.get(value);
    if (result == null) {
      // leafNode is single-valued (singleKey returned non-null), so its one child is the value
      // node.
      SrosStatementTree valueNode = leafNode.getChildren().values().iterator().next();
      warnf(valueNode, "unrecognized %s '%s'", what, value);
    }
    return result;
  }

  /**
   * Interpret a boolean leaf ({@code true}/{@code false}): returns the boxed value, or {@code null}
   * if the leaf is absent or not single-valued (so an unset flag stays {@code null} for
   * inheritance). A value that is neither {@code true} nor {@code false} is warned (no silent
   * fallback) and returns {@code null}.
   */
  private @Nullable Boolean toBoolean(@Nullable SrosStatementTree leafNode, String what) {
    String value = singleKey(leafNode);
    if (value == null) {
      return null;
    }
    if (value.equals("true")) {
      return Boolean.TRUE;
    }
    if (value.equals("false")) {
      return Boolean.FALSE;
    }
    // leafNode is single-valued, so its one child is the value node carrying the source context.
    warnf(
        leafNode.getChildren().values().iterator().next(),
        "expected %s to be true or false but got '%s'",
        what,
        value);
    return null;
  }

  private static @Nonnull String unquote(String text) {
    if (text.length() >= 2 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
      return text.substring(1, text.length() - 1);
    }
    return text;
  }

  // --- warning + structure helpers --------------------------------------------------------------

  /**
   * Emit a line-stamped {@link Warnings.ParseWarning} for the statement that created {@code
   * valueNode} (the same channel as the parser's unrecognized-line warnings, so it is
   * annotate-visible and carries a source line).
   *
   * <p>{@code valueNode} is a value node returned by {@link #singleValueNode} or a leaf-list child,
   * which always carries a source context: {@link SrosConfigurationBuilder} records the statement
   * context on its deepest (value) node, and the value-parse helpers only warn once they have a
   * non-null value (so the value node exists). The context is therefore required, not optional.
   */
  private void warn(SrosStatementTree valueNode, String message) {
    ParserRuleContext ctx = valueNode.firstDefContext();
    checkState(ctx != null, "value node for warning '%s' has no source context", message);
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    String fullText = _text.substring(start, end + 1);
    _w.addWarning(ctx, fullText, _parser, message);
  }

  /** {@link #warn} with a format string and args. */
  @FormatMethod
  private void warnf(SrosStatementTree valueNode, String format, Object... args) {
    warn(valueNode, String.format(format, args));
  }

  /**
   * Record a structure definition on every source context of {@code defNode} (so a structure
   * configured by more than one statement — e.g. mixed brace + flat — accumulates all its lines).
   */
  private void defineStructure(StructureType type, String name, SrosStatementTree defNode) {
    for (ParserRuleContext ctx : defNode.getDefContexts()) {
      _c.defineStructure(type, name, ctx);
    }
  }

  /** Record one reference to {@code name} on the line of {@code refNode}'s source context. */
  private void referenceStructure(
      StructureType type, String name, StructureUsage usage, @Nullable SrosStatementTree refNode) {
    ParserRuleContext ctx = refNode == null ? null : refNode.firstDefContext();
    if (ctx == null) {
      return;
    }
    _c.referenceStructure(type, name, usage, _parser.getLine(ctx.getStart()));
  }

  /**
   * Record one reference per (unquoted) child of a leaf-list node {@code refNode}, all at that
   * node's line. Used for the {@code policy [a b]} import/export lists and {@code from
   * prefix-list}.
   */
  private void referenceStructures(
      StructureType type, @Nullable SrosStatementTree refNode, StructureUsage usage) {
    if (refNode == null) {
      return;
    }
    for (String child : refNode.getChildren().keySet()) {
      referenceStructure(type, unquote(child), usage, refNode);
    }
  }

  /** Reference each policy name in an import/export {@code policy [..]} leaf-list. */
  private void referencePolicies(@Nullable SrosStatementTree policyLeafList, StructureUsage usage) {
    referenceStructures(SrosStructureType.POLICY_STATEMENT, policyLeafList, usage);
  }

  private final @Nonnull SrosConfiguration _c;
  private final @Nonnull Warnings _w;
  private final @Nonnull SrosCombinedParser _parser;
  private final @Nonnull String _text;
}
