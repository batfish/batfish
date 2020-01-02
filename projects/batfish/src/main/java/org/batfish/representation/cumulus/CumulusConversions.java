package org.batfish.representation.cumulus;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static org.batfish.common.util.CollectionUtil.toImmutableSortedMap;
import static org.batfish.representation.cumulus.OspfInterface.DEFAULT_OSPF_DEAD_INTERVAL;
import static org.batfish.representation.cumulus.OspfInterface.DEFAULT_OSPF_HELLO_INTERVAL;
import static org.batfish.representation.cumulus.OspfProcess.DEFAULT_OSPF_PROCESS_NAME;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;

/** Utilities that convert Cumulus-specific representations to vendor-independent model. */
@ParametersAreNonnullByDefault
public final class CumulusConversions {
  private static final int AGGREGATE_ROUTE_ADMIN_COST = 200; // TODO verify this

  private static final Prefix LOOPBACK_PREFIX = Prefix.parse("127.0.0.0/8");

  public static String computeBgpGenerationPolicyName(boolean ipv4, String vrfName, String prefix) {
    return String.format("~AGGREGATE_ROUTE%s_GEN:%s:%s~", ipv4 ? "" : "6", vrfName, prefix);
  }

  public static String computeMatchSuppressedSummaryOnlyPolicyName(String vrfName) {
    return String.format("~MATCH_SUPPRESSED_SUMMARY_ONLY:%s~", vrfName);
  }

  private static WithEnvironmentExpr bgpRedistributeWithEnvironmentExpr(
      BooleanExpr expr, OriginType originType) {
    WithEnvironmentExpr we = new WithEnvironmentExpr();
    we.setExpr(expr);
    we.setPreStatements(
        ImmutableList.of(Statements.SetWriteIntermediateBgpAttributes.toStaticStatement()));
    we.setPostStatements(
        ImmutableList.of(Statements.UnsetWriteIntermediateBgpAttributes.toStaticStatement()));
    we.setPostTrueStatements(
        ImmutableList.of(
            Statements.SetReadIntermediateBgpAttributes.toStaticStatement(),
            new SetOrigin(new LiteralOrigin(originType, null))));
    return we;
  }

  static BooleanExpr generateExportAggregateConditions(
      Map<Prefix, BgpVrfAddressFamilyAggregateNetworkConfiguration> aggregateNetworks) {
    return new Disjunction(
        aggregateNetworks.entrySet().stream()
            .map(
                entry -> {
                  Prefix prefix = entry.getKey();

                  // Conditions to generate this route
                  List<BooleanExpr> exportAggregateConjuncts = new ArrayList<>();
                  exportAggregateConjuncts.add(
                      new MatchPrefixSet(
                          DestinationNetwork.instance(),
                          new ExplicitPrefixSet(new PrefixSpace(PrefixRange.fromPrefix(prefix)))));
                  exportAggregateConjuncts.add(new MatchProtocol(RoutingProtocol.AGGREGATE));

                  // TODO consider attribute map
                  BooleanExpr weInterior = BooleanExprs.TRUE;
                  exportAggregateConjuncts.add(
                      bgpRedistributeWithEnvironmentExpr(weInterior, OriginType.IGP));

                  // Do export a generated aggregate.
                  return new Conjunction(exportAggregateConjuncts);
                })
            .collect(ImmutableList.toImmutableList()));
  }

  /**
   * Creates generated routes and route generation policies for aggregate routes for the input vrf.
   */
  static void generateGeneratedRoutes(
      Configuration c,
      org.batfish.datamodel.Vrf vrf,
      Map<Prefix, BgpVrfAddressFamilyAggregateNetworkConfiguration> aggregateNetworks) {
    aggregateNetworks.forEach(
        (prefix, agg) -> {
          generateGenerationPolicy(c, vrf.getName(), prefix);

          // TODO generate attribute policy
          GeneratedRoute gr =
              GeneratedRoute.builder()
                  .setNetwork(prefix)
                  .setAdmin(AGGREGATE_ROUTE_ADMIN_COST)
                  .setGenerationPolicy(
                      computeBgpGenerationPolicyName(true, vrf.getName(), prefix.toString()))
                  .setDiscard(true)
                  .build();

          vrf.getGeneratedRoutes().add(gr);
        });
  }

  /**
   * Creates a generation policy for the aggregate network with the given {@link Prefix}. The
   * generation policy matches any route with a destination more specific than {@code prefix}.
   *
   * @param c {@link Configuration} in which to create the generation policy
   * @param vrfName Name of VRF in which the aggregate network exists
   * @param prefix The aggregate network prefix
   */
  static void generateGenerationPolicy(Configuration c, String vrfName, Prefix prefix) {
    RoutingPolicy.builder()
        .setOwner(c)
        .setName(computeBgpGenerationPolicyName(true, vrfName, prefix.toString()))
        .addStatement(
            new If(
                // Match routes with destination networks more specific than prefix.
                new MatchPrefixSet(
                    DestinationNetwork.instance(),
                    new ExplicitPrefixSet(new PrefixSpace(PrefixRange.moreSpecificThan(prefix)))),
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement())))
        .build();
  }

  /**
   * Generates and returns a {@link Statement} that suppresses routes that are summarized by the
   * given set of {@link Prefix prefixes} configured as {@code summary-only}.
   *
   * <p>Returns {@code null} if {@code prefixesToSuppress} has no entries.
   *
   * <p>If any Batfish-generated structures are generated, does the bookkeeping in the provided
   * {@link Configuration} to ensure they are available and tracked.
   */
  @Nullable
  static If suppressSummarizedPrefixes(
      Configuration c, String vrfName, Stream<Prefix> summaryOnlyPrefixes) {
    Iterator<Prefix> prefixesToSuppress = summaryOnlyPrefixes.iterator();
    if (!prefixesToSuppress.hasNext()) {
      return null;
    }
    // Create a RouteFilterList that matches any network longer than a prefix marked summary only.
    RouteFilterList matchLonger =
        new RouteFilterList(computeMatchSuppressedSummaryOnlyPolicyName(vrfName));
    prefixesToSuppress.forEachRemaining(
        p ->
            matchLonger.addLine(
                new RouteFilterLine(LineAction.PERMIT, PrefixRange.moreSpecificThan(p))));
    // Bookkeeping: record that we created this RouteFilterList to match longer networks.
    c.getRouteFilterLists().put(matchLonger.getName(), matchLonger);

    return new If(
        "Suppress more specific networks for summary-only aggregate-address networks",
        new MatchPrefixSet(
            DestinationNetwork.instance(), new NamedPrefixSet(matchLonger.getName())),
        ImmutableList.of(Statements.Suppress.toStaticStatement()),
        ImmutableList.of());
  }

  static void convertOspfProcess(
      Configuration c,
      @Nullable OspfProcess ospfProcess,
      Loopback loopback,
      Map<String, Interface> vsIfaces,
      Warnings w) {
    if (ospfProcess == null) {
      return;
    }

    convertOspfVrf(c, ospfProcess.getDefaultVrf(), c.getDefaultVrf(), loopback, vsIfaces, w);

    ospfProcess
        .getVrfs()
        .values()
        .forEach(
            ospfVrf -> {
              org.batfish.datamodel.Vrf vrf = c.getVrfs().get(ospfVrf.getVrfName());

              if (vrf == null) {
                w.redFlag(String.format("Vrf %s is not found.", ospfVrf.getVrfName()));
                return;
              }

              convertOspfVrf(c, ospfVrf, vrf, loopback, vsIfaces, w);
            });
  }

  private static void convertOspfVrf(
      Configuration c,
      OspfVrf ospfVrf,
      org.batfish.datamodel.Vrf vrf,
      Loopback loopback,
      Map<String, Interface> vsInterfaces,
      Warnings w) {
    org.batfish.datamodel.ospf.OspfProcess ospfProcess =
        toOspfProcess(ospfVrf, c.getAllInterfaces(vrf.getName()), loopback, vsInterfaces, w);
    vrf.addOspfProcess(ospfProcess);
  }

  @VisibleForTesting
  static org.batfish.datamodel.ospf.OspfProcess toOspfProcess(
      OspfVrf ospfVrf,
      Map<String, org.batfish.datamodel.Interface> vrfInterfaces,
      Loopback loopback,
      Map<String, Interface> vsInterfaces,
      Warnings w) {
    Ip routerId = ospfVrf.getRouterId();
    if (routerId == null) {
      routerId = inferRouterId(loopback, vsInterfaces);
    }

    org.batfish.datamodel.ospf.OspfProcess.Builder builder =
        org.batfish.datamodel.ospf.OspfProcess.builder();

    org.batfish.datamodel.ospf.OspfProcess proc =
        builder
            .setRouterId(routerId)
            .setProcessId(DEFAULT_OSPF_PROCESS_NAME)
            .setReferenceBandwidth(OspfProcess.DEFAULT_REFERENCE_BANDWIDTH)
            .build();

    addOspfInterfaces(vrfInterfaces, proc.getProcessId(), vsInterfaces, w);
    proc.setAreas(computeOspfAreas(vrfInterfaces.keySet(), vsInterfaces));
    return proc;
  }

  /**
   * Logic of inferring router ID for Zebra based system
   * (https://github.com/coreswitch/zebra/blob/master/docs/router-id.md):
   *
   * <p>If the loopback is configured with an IP address NOT in 127.0.0.0/8, the numerically largest
   * such IP is used. Otherwise, the numerically largest IP configured on any interface on the
   * device is used. Otherwise, 0.0.0.0 is used.
   */
  @VisibleForTesting
  static Ip inferRouterId(Loopback loopback, Map<String, Interface> vsInterfaces) {
    if (loopback.getConfigured()) {
      Optional<ConcreteInterfaceAddress> maxLoIp =
          loopback.getAddresses().stream()
              .filter(addr -> !LOOPBACK_PREFIX.containsIp(addr.getIp()))
              .max(ConcreteInterfaceAddress::compareTo);
      if (maxLoIp.isPresent()) {
        return maxLoIp.get().getIp();
      }
    }

    Optional<ConcreteInterfaceAddress> biggestInterfaceIp =
        vsInterfaces.values().stream()
            .flatMap(iface -> iface.getIpAddresses().stream())
            .max(InterfaceAddress::compareTo);

    return biggestInterfaceIp
        .map(ConcreteInterfaceAddress::getIp)
        .orElseGet(() -> Ip.parse("0.0.0.0"));
  }

  @VisibleForTesting
  static void addOspfInterfaces(
      Map<String, org.batfish.datamodel.Interface> viIfaces,
      String processId,
      Map<String, Interface> vsIfaces,
      Warnings w) {
    viIfaces.forEach(
        (ifaceName, iface) -> {
          Interface vsIface = vsIfaces.get(iface.getName());
          OspfInterface ospfInterface = vsIface.getOspf();
          if (ospfInterface == null || ospfInterface.getOspfArea() == null) {
            // no ospf running on this interface
            return;
          }

          iface.setOspfSettings(
              OspfInterfaceSettings.builder()
                  .setPassive(Optional.ofNullable(ospfInterface.getPassive()).orElse(false))
                  .setAreaName(ospfInterface.getOspfArea())
                  .setNetworkType(toOspfNetworkType(ospfInterface.getNetwork(), w))
                  .setDeadInterval(
                      Optional.ofNullable(ospfInterface.getDeadInterval())
                          .orElse(DEFAULT_OSPF_DEAD_INTERVAL))
                  .setHelloInterval(
                      Optional.ofNullable(ospfInterface.getHelloInterval())
                          .orElse(DEFAULT_OSPF_HELLO_INTERVAL))
                  .setProcess(processId)
                  .build());
        });
  }

  @VisibleForTesting
  static SortedMap<Long, OspfArea> computeOspfAreas(
      Collection<String> vrfIfaceNames, Map<String, Interface> vsIfaces) {
    Map<Long, List<String>> areaInterfaces =
        vrfIfaceNames.stream()
            .map(vsIfaces::get)
            .filter(vsIface -> vsIface.getOspf() != null && vsIface.getOspf().getOspfArea() != null)
            .collect(
                groupingBy(
                    vsIface -> vsIface.getOspf().getOspfArea(),
                    mapping(Interface::getName, Collectors.toList())));

    return toImmutableSortedMap(
        areaInterfaces,
        Entry::getKey,
        e -> OspfArea.builder().setNumber(e.getKey()).addInterfaces(e.getValue()).build());
  }

  @Nullable
  private static org.batfish.datamodel.ospf.OspfNetworkType toOspfNetworkType(
      @Nullable OspfNetworkType type, Warnings w) {
    if (type == null) {
      return null;
    }
    switch (type) {
      case BROADCAST:
        return org.batfish.datamodel.ospf.OspfNetworkType.BROADCAST;
      case POINT_TO_POINT:
        return org.batfish.datamodel.ospf.OspfNetworkType.POINT_TO_POINT;
      default:
        w.redFlag(
            String.format(
                "Conversion of Cumulus FRR OSPF network type '%s' is not handled.",
                type.toString()));
        return null;
    }
  }

  static void convertIpCommunityLists(
      Configuration c, Map<String, IpCommunityList> ipCommunityLists) {
    ipCommunityLists.forEach(
        (name, list) -> c.getCommunityLists().put(name, toCommunityList(list)));
  }

  @VisibleForTesting
  static CommunityList toCommunityList(IpCommunityList list) {
    return list.accept(
        ipCommunityList ->
            new CommunityList(
                ipCommunityList.getName(),
                ipCommunityList.getCommunities().stream()
                    .map(LiteralCommunity::new)
                    .map(k -> new CommunityListLine(ipCommunityList.getAction(), k))
                    .collect(ImmutableList.toImmutableList()),
                false));
  }

  static void convertIpAsPathAccessLists(
      Configuration c, Map<String, IpAsPathAccessList> ipAsPathAccessLists) {
    ipAsPathAccessLists.forEach(
        (name, asPathAccessList) ->
            c.getAsPathAccessLists().put(name, toAsPathAccessList(asPathAccessList)));
  }

  @VisibleForTesting
  static @Nonnull AsPathAccessList toAsPathAccessList(IpAsPathAccessList asPathAccessList) {
    String name = asPathAccessList.getName();
    List<AsPathAccessListLine> lines =
        asPathAccessList.getLines().stream()
            // TODO Check FRR AS path match semantics.
            // This regex assumes we should match any path containing the specified ASN anywhere.
            .map(
                line ->
                    new AsPathAccessListLine(
                        line.getAction(), String.format("(^| )%s($| )", line.getAsNum())))
            .collect(ImmutableList.toImmutableList());
    return new AsPathAccessList(name, lines);
  }

  static void convertIpPrefixLists(Configuration c, Map<String, IpPrefixList> ipPrefixLists) {
    ipPrefixLists.forEach(
        (name, ipPrefixList) -> c.getRouteFilterLists().put(name, toRouteFilterList(ipPrefixList)));
  }

  @VisibleForTesting
  static @Nonnull RouteFilterList toRouteFilterList(IpPrefixList ipPrefixList) {
    String name = ipPrefixList.getName();
    RouteFilterList rfl = new RouteFilterList(name);
    rfl.setLines(
        ipPrefixList.getLines().values().stream()
            .map(CumulusConversions::toRouteFilterLine)
            .collect(ImmutableList.toImmutableList()));
    return rfl;
  }

  @VisibleForTesting
  static @Nonnull RouteFilterLine toRouteFilterLine(IpPrefixListLine ipPrefixListLine) {
    return new RouteFilterLine(
        ipPrefixListLine.getAction(),
        ipPrefixListLine.getPrefix(),
        ipPrefixListLine.getLengthRange());
  }

  static void convertRouteMaps(
      Configuration c, CumulusNodeConfiguration vc, Map<String, RouteMap> routeMaps, Warnings w) {
    routeMaps.forEach((name, routeMap) -> new RouteMapConvertor(c, vc, routeMap, w).toRouteMap());
  }

  static void convertDnsServers(Configuration c, List<Ip> ipv4Nameservers) {
    c.setDnsServers(
        ipv4Nameservers.stream()
            .map(Object::toString)
            .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder())));
  }
}
