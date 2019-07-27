package org.batfish.representation.cisco_nxos;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.EXACT_PATH;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.PATH_LENGTH;
import static org.batfish.datamodel.Route.UNSET_NEXT_HOP_INTERFACE;
import static org.batfish.datamodel.Route.UNSET_ROUTE_NEXT_HOP_IP;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.representation.cisco.CiscoConfiguration.computeBgpCommonExportPolicyName;
import static org.batfish.representation.cisco.CiscoConfiguration.computeBgpGenerationPolicyName;
import static org.batfish.representation.cisco.CiscoConversions.generateGenerationPolicy;
import static org.batfish.representation.cisco.CiscoConversions.suppressSummarizedPrefixes;
import static org.batfish.representation.cisco_nxos.CiscoNxosInterfaceType.PORT_CHANNEL;
import static org.batfish.representation.cisco_nxos.Interface.BANDWIDTH_CONVERSION_FACTOR;
import static org.batfish.representation.cisco_nxos.Interface.getDefaultBandwidth;
import static org.batfish.representation.cisco_nxos.Interface.getDefaultSpeed;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.BatfishException;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6Range;
import org.batfish.datamodel.Prefix6Space;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.Route6FilterLine;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork6;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefix6Set;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.tracking.DecrementPriority;
import org.batfish.datamodel.vendor_family.cisco_nxos.CiscoNxosFamily;
import org.batfish.representation.cisco_nxos.BgpVrfIpv6AddressFamilyConfiguration.Network;
import org.batfish.vendor.VendorConfiguration;

/** Vendor-specific representation of a Cisco NX-OS network configuration */
public final class CiscoNxosConfiguration extends VendorConfiguration {

  /*
   * This map is used to convert interface names to their canonical forms.
   * The entries are visited in insertion order until a key is found of which the name to convert is
   * case-insensitively a prefix. The value corresponding to that key is chosen as the canonical
   * form for that name.
   *
   * NOTE: Entries are sorted by priority. Do not reorder unless you have a good reason.
   */
  private static final Map<String, String> CISCO_NXOS_INTERFACE_PREFIXES;
  private static final Pattern CISCO_NXOS_INTERFACE_PREFIXES_REGEX;

  static {
    CISCO_NXOS_INTERFACE_PREFIXES =
        ImmutableMap.<String, String>builder()
            .put("Ethernet", "Ethernet")
            .put("loopback", "loopback")
            .put("mgmt", "mgmt")
            .put("Null", "Null")
            .put("nve", "nve")
            .put("port-channel", "port-channel")
            .put("Vlan", "Vlan")
            .build();
    CISCO_NXOS_INTERFACE_PREFIXES_REGEX =
        Pattern.compile(
            CISCO_NXOS_INTERFACE_PREFIXES.values().stream()
                .map(String::toLowerCase)
                .collect(Collectors.joining("|")));
  }

  /** Returns canonical prefix of interface name if valid, else {@code null}. */
  public static @Nullable String getCanonicalInterfaceNamePrefix(String prefix) {
    for (Entry<String, String> e : CISCO_NXOS_INTERFACE_PREFIXES.entrySet()) {
      String matchPrefix = e.getKey();
      String canonicalPrefix = e.getValue();
      if (matchPrefix.toLowerCase().startsWith(prefix.toLowerCase())) {
        return canonicalPrefix;
      }
    }
    return null;
  }

  private static final IntegerSpace DEFAULT_RESERVED_VLAN_RANGE =
      IntegerSpace.of(Range.closed(3968, 4094));

  private static final int MAX_FRAGMENT_OFFSET = (1 << 13) - 1;
  private static final AclLineMatchExpr MATCH_INITIAL_FRAGMENT_OFFSET =
      match(
          HeaderSpace.builder()
              .setFragmentOffsets(ImmutableList.of(SubRange.singleton(0)))
              .build());
  private static final AclLineMatchExpr MATCH_NON_INITIAL_FRAGMENT_OFFSET =
      match(
          HeaderSpace.builder()
              .setFragmentOffsets(ImmutableList.of(new SubRange(1, MAX_FRAGMENT_OFFSET)))
              .build());

  public static final String NULL_VRF_NAME = "~NULL_VRF~";

  /** Routing-related constants. */
  private static final int AGGREGATE_ROUTE_ADMIN_COST = 200;

  private static final double SPEED_CONVERSION_FACTOR = 1E6D;

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

  private static @Nonnull RouteFilterLine toRouteFilterLine(IpPrefixListLine ipPrefixListLine) {
    return new RouteFilterLine(
        ipPrefixListLine.getAction(),
        ipPrefixListLine.getPrefix(),
        ipPrefixListLine.getLengthRange());
  }

  private static @Nonnull RouteFilterList toRouteFilterList(IpPrefixList ipPrefixList) {
    String name = ipPrefixList.getName();
    RouteFilterList rfl = new RouteFilterList(name);
    rfl.setLines(
        ipPrefixList.getLines().values().stream()
            .map(CiscoNxosConfiguration::toRouteFilterLine)
            .collect(ImmutableList.toImmutableList()));
    return rfl;
  }

  private transient Configuration _c;

  private @Nullable String _bannerExec;
  private @Nullable String _bannerMotd;
  private final @Nonnull BgpGlobalConfiguration _bgpGlobalConfiguration;
  private final @Nonnull Vrf _defaultVrf;
  private @Nullable Evpn _evpn;
  private @Nullable String _hostname;
  private final @Nonnull Map<String, Interface> _interfaces;
  private final @Nonnull Map<String, IpAccessList> _ipAccessLists;
  private final @Nonnull Map<String, IpAsPathAccessList> _ipAsPathAccessLists;
  private final @Nonnull Map<String, IpCommunityList> _ipCommunityLists;
  private final @Nonnull Map<String, IpPrefixList> _ipPrefixLists;
  private final @Nonnull Map<Integer, Nve> _nves;
  private final @Nonnull Map<String, DefaultVrfOspfProcess> _ospfProcesses;
  private transient Multimap<String, String> _portChannelMembers;
  private @Nonnull IntegerSpace _reservedVlanRange;
  private final @Nonnull Map<String, RouteMap> _routeMaps;
  private @Nullable String _version;
  private final @Nonnull Map<Integer, Vlan> _vlans;
  private final @Nonnull Map<String, Vrf> _vrfs;

  public CiscoNxosConfiguration() {
    _bgpGlobalConfiguration = new BgpGlobalConfiguration();
    _defaultVrf = new Vrf(DEFAULT_VRF_NAME);
    _interfaces = new HashMap<>();
    _ipAccessLists = new HashMap<>();
    _ipAsPathAccessLists = new HashMap<>();
    _ipCommunityLists = new HashMap<>();
    _ipPrefixLists = new HashMap<>();
    _nves = new HashMap<>();
    _ospfProcesses = new HashMap<>();
    _reservedVlanRange = DEFAULT_RESERVED_VLAN_RANGE;
    _routeMaps = new HashMap<>();
    _vlans = new HashMap<>();
    _vrfs = new HashMap<>();
  }

  public void defineStructure(CiscoNxosStructureType type, String name, ParserRuleContext ctx) {
    for (int i = ctx.getStart().getLine(); i <= ctx.getStop().getLine(); ++i) {
      defineStructure(type, name, i);
    }
  }

  @Override
  public String canonicalizeInterfaceName(String ifaceName) {
    Matcher matcher = CISCO_NXOS_INTERFACE_PREFIXES_REGEX.matcher(ifaceName.toLowerCase());
    if (!matcher.find()) {
      throw new BatfishException("Invalid interface name: '" + ifaceName + "'");
    }
    String ifacePrefix = matcher.group();
    String canonicalPrefix = getCanonicalInterfaceNamePrefix(ifacePrefix);
    if (canonicalPrefix == null) {
      throw new BatfishException("Invalid interface name: '" + ifaceName + "'");
    }
    String suffix = ifaceName.substring(ifacePrefix.length());
    return canonicalPrefix + suffix;
  }

  private void convertBgp() {
    // Before we process any configuration, execute BGP inheritance.
    _bgpGlobalConfiguration.doInherit(_w);

    _bgpGlobalConfiguration
        .getVrfs()
        .forEach(
            (vrfName, bgpVrfConfig) -> {
              convertBgpVrf(_c, _bgpGlobalConfiguration, bgpVrfConfig, vrfName);
            });
  }

  private void convertBgpVrf(
      Configuration c,
      BgpGlobalConfiguration nxBgpGlobal,
      BgpVrfConfiguration nxBgpVrf,
      String vrfName) {
    org.batfish.datamodel.Vrf v = c.getVrfs().get(vrfName);
    if (v == null) {
      // Do nothing, but rely on the VRF having an undefined reference warning.
      return;
    }
    int ebgpAdmin = RoutingProtocol.BGP.getDefaultAdministrativeCost(c.getConfigurationFormat());
    int ibgpAdmin = RoutingProtocol.IBGP.getDefaultAdministrativeCost(c.getConfigurationFormat());
    org.batfish.datamodel.BgpProcess newBgpProcess =
        new org.batfish.datamodel.BgpProcess(
            Conversions.getBgpRouterId(nxBgpVrf, v, _w), ebgpAdmin, ibgpAdmin);
    if (nxBgpVrf.getBestpathCompareRouterId()) {
      newBgpProcess.setTieBreaker(BgpTieBreaker.ROUTER_ID);
    }

    // From NX-OS docs for `bestpath as-path multipath-relax`
    //  Allows load sharing across providers with different (but equal-length) autonomous system
    //  paths. Without this option, the AS paths must be identical for load sharing.
    newBgpProcess.setMultipathEquivalentAsPathMatchMode(
        nxBgpVrf.getBestpathAsPathMultipathRelax() ? PATH_LENGTH : EXACT_PATH);

    // Process vrf-level address family configuration, such as export policy.
    BgpVrfIpv4AddressFamilyConfiguration ipv4af = nxBgpVrf.getIpv4UnicastAddressFamily();
    if (ipv4af != null) {
      // Batfish seems to only track the IPv4 properties for multipath ebgp/ibgp.
      newBgpProcess.setMultipathEbgp(ipv4af.getMaximumPathsEbgp() > 1);
      newBgpProcess.setMultipathIbgp(ipv4af.getMaximumPathsIbgp() > 1);
    }

    // Next we build up the BGP common export policy.
    RoutingPolicy bgpCommonExportPolicy =
        new RoutingPolicy(computeBgpCommonExportPolicyName(vrfName), c);
    c.getRoutingPolicies().put(bgpCommonExportPolicy.getName(), bgpCommonExportPolicy);

    // 1. If there are any ipv4 summary only networks, do not export the more specific routes.
    if (ipv4af != null) {
      Stream<Prefix> summaryOnlyNetworks =
          ipv4af.getAggregateNetworks().entrySet().stream()
              .filter(e -> e.getValue().getSummaryOnly())
              .map(Entry::getKey);
      If suppressLonger = suppressSummarizedPrefixes(c, vrfName, summaryOnlyNetworks);
      if (suppressLonger != null) {
        bgpCommonExportPolicy.getStatements().add(suppressLonger);
      }
    }

    // The body of the export policy is a huge disjunction over many reasons routes may be exported.
    Disjunction routesShouldBeExported = new Disjunction();
    bgpCommonExportPolicy
        .getStatements()
        .add(
            new If(
                routesShouldBeExported,
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of()));
    // This list of reasons to export a route will be built up over the remainder of this function.
    List<BooleanExpr> exportConditions = routesShouldBeExported.getDisjuncts();

    // Generate and distribute aggregate routes.
    if (ipv4af != null) {
      for (Entry<Prefix, BgpVrfAddressFamilyAggregateNetworkConfiguration> e :
          ipv4af.getAggregateNetworks().entrySet()) {
        Prefix prefix = e.getKey();
        BgpVrfAddressFamilyAggregateNetworkConfiguration agg = e.getValue();
        generateGenerationPolicy(c, vrfName, prefix);

        GeneratedRoute.Builder gr =
            GeneratedRoute.builder()
                .setNetwork(prefix)
                .setAdmin(AGGREGATE_ROUTE_ADMIN_COST)
                .setGenerationPolicy(
                    computeBgpGenerationPolicyName(true, vrfName, prefix.toString()))
                .setDiscard(true);

        // Conditions to generate this route
        List<BooleanExpr> exportAggregateConditions = new ArrayList<>();
        exportAggregateConditions.add(
            new MatchPrefixSet(
                DestinationNetwork.instance(),
                new ExplicitPrefixSet(new PrefixSpace(PrefixRange.fromPrefix(prefix)))));
        exportAggregateConditions.add(new MatchProtocol(RoutingProtocol.AGGREGATE));

        // If defined, set attribute map for aggregate network
        BooleanExpr weInterior = BooleanExprs.TRUE;
        String attributeMapName = agg.getAttributeMap();
        if (attributeMapName != null) {
          RouteMap attributeMap = _routeMaps.get(attributeMapName);
          if (attributeMap != null) {
            // need to apply attribute changes if this specific route is matched
            weInterior = new CallExpr(attributeMapName);
            gr.setAttributePolicy(attributeMapName);
          }
        }
        exportAggregateConditions.add(
            bgpRedistributeWithEnvironmentExpr(weInterior, OriginType.IGP));

        v.getGeneratedRoutes().add(gr.build());
        // Do export a generated aggregate.
        exportConditions.add(new Conjunction(exportAggregateConditions));
      }
    }

    // Only redistribute default route if `default-information originate` is set.
    BooleanExpr redistributeDefaultRoute =
        ipv4af == null || !ipv4af.getDefaultInformationOriginate()
            ? Conversions.NOT_DEFAULT_ROUTE
            : BooleanExprs.TRUE;

    // Export RIP routes that should be redistributed.
    BgpRedistributionPolicy ripPolicy =
        ipv4af == null ? null : ipv4af.getRedistributionPolicy(RoutingProtocol.RIP);
    if (ripPolicy != null) {
      String routeMap = ripPolicy.getRouteMap();
      org.batfish.representation.cisco_nxos.RouteMap map = _routeMaps.get(routeMap);
      /* TODO: how do we match on source tag (aka RIP process id)? */
      List<BooleanExpr> conditions =
          ImmutableList.of(
              new MatchProtocol(RoutingProtocol.RIP),
              redistributeDefaultRoute,
              bgpRedistributeWithEnvironmentExpr(
                  map == null ? BooleanExprs.TRUE : new CallExpr(routeMap), OriginType.INCOMPLETE));
      Conjunction rip = new Conjunction(conditions);
      rip.setComment("Redistribute RIP routes into BGP");
      exportConditions.add(rip);
    }

    // Export static routes that should be redistributed.
    BgpRedistributionPolicy staticPolicy =
        ipv4af == null ? null : ipv4af.getRedistributionPolicy(RoutingProtocol.STATIC);
    if (staticPolicy != null) {
      String routeMap = staticPolicy.getRouteMap();
      RouteMap map = _routeMaps.get(routeMap);
      List<BooleanExpr> conditions =
          ImmutableList.of(
              new MatchProtocol(RoutingProtocol.STATIC),
              redistributeDefaultRoute,
              bgpRedistributeWithEnvironmentExpr(
                  map == null ? BooleanExprs.TRUE : new CallExpr(routeMap), OriginType.INCOMPLETE));
      Conjunction staticRedist = new Conjunction(conditions);
      staticRedist.setComment("Redistribute static routes into BGP");
      exportConditions.add(staticRedist);
    }

    // Export connected routes that should be redistributed.
    BgpRedistributionPolicy connectedPolicy =
        ipv4af == null ? null : ipv4af.getRedistributionPolicy(RoutingProtocol.CONNECTED);
    if (connectedPolicy != null) {
      String routeMap = connectedPolicy.getRouteMap();
      RouteMap map = _routeMaps.get(routeMap);
      List<BooleanExpr> conditions =
          ImmutableList.of(
              new MatchProtocol(RoutingProtocol.CONNECTED),
              redistributeDefaultRoute,
              bgpRedistributeWithEnvironmentExpr(
                  map == null ? BooleanExprs.TRUE : new CallExpr(routeMap), OriginType.INCOMPLETE));
      Conjunction connected = new Conjunction(conditions);
      connected.setComment("Redistribute connected routes into BGP");
      exportConditions.add(connected);
    }

    // Export OSPF routes that should be redistributed.
    BgpRedistributionPolicy ospfPolicy =
        ipv4af == null ? null : ipv4af.getRedistributionPolicy(RoutingProtocol.OSPF);
    if (ospfPolicy != null) {
      String routeMap = ospfPolicy.getRouteMap();
      RouteMap map = _routeMaps.get(routeMap);
      /* TODO: how do we match on source tag (aka OSPF process)? */
      List<BooleanExpr> conditions =
          ImmutableList.of(
              new MatchProtocol(RoutingProtocol.OSPF),
              redistributeDefaultRoute,
              bgpRedistributeWithEnvironmentExpr(
                  map == null ? BooleanExprs.TRUE : new CallExpr(routeMap), OriginType.INCOMPLETE));
      Conjunction ospf = new Conjunction(conditions);
      ospf.setComment("Redistribute OSPF routes into BGP");
      exportConditions.add(ospf);
    }

    // Now we add all the per-network export policies.
    if (ipv4af != null) {
      ipv4af
          .getNetworks()
          .forEach(
              network -> {
                PrefixSpace exportSpace =
                    new PrefixSpace(PrefixRange.fromPrefix(network.getNetwork()));
                @Nullable String routeMap = network.getRouteMap();
                List<BooleanExpr> exportNetworkConditions =
                    ImmutableList.of(
                        new MatchPrefixSet(
                            DestinationNetwork.instance(), new ExplicitPrefixSet(exportSpace)),
                        new Not(
                            new MatchProtocol(
                                RoutingProtocol.BGP,
                                RoutingProtocol.IBGP,
                                RoutingProtocol.AGGREGATE)),
                        bgpRedistributeWithEnvironmentExpr(
                            routeMap != null && _routeMaps.containsKey(routeMap)
                                ? new CallExpr(routeMap)
                                : BooleanExprs.TRUE,
                            OriginType.IGP));
                newBgpProcess.addToOriginationSpace(exportSpace);
                exportConditions.add(new Conjunction(exportNetworkConditions));
              });
    }

    BgpVrfIpv6AddressFamilyConfiguration ipv6af = nxBgpVrf.getIpv6UnicastAddressFamily();
    if (ipv6af != null) {
      ipv6af
          .getNetworks()
          .forEach(
              network -> {
                @Nullable String routeMap = network.getRouteMap();
                List<BooleanExpr> exportNetworkConditions =
                    ImmutableList.of(
                        new MatchPrefix6Set(
                            new DestinationNetwork6(),
                            new ExplicitPrefix6Set(
                                new Prefix6Space(Prefix6Range.fromPrefix6(network.getNetwork())))),
                        new Not(
                            new MatchProtocol(
                                RoutingProtocol.BGP,
                                RoutingProtocol.IBGP,
                                RoutingProtocol.AGGREGATE)),
                        bgpRedistributeWithEnvironmentExpr(
                            routeMap != null && _routeMaps.containsKey(routeMap)
                                ? new CallExpr(routeMap)
                                : BooleanExprs.TRUE,
                            OriginType.IGP));
                exportConditions.add(new Conjunction(exportNetworkConditions));
              });
    }

    // Always export BGP or IBGP routes.
    exportConditions.add(new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP));

    // Finally, the export policy ends with returning false: do not export unmatched routes.
    bgpCommonExportPolicy.getStatements().add(Statements.ReturnFalse.toStaticStatement());

    // Generate BGP_NETWORK6_NETWORKS filter.
    if (ipv6af != null) {
      List<Route6FilterLine> lines =
          ipv6af.getNetworks().stream()
              .map(Network::getNetwork)
              .map(p6 -> new Route6FilterLine(LineAction.PERMIT, Prefix6Range.fromPrefix6(p6)))
              .collect(ImmutableList.toImmutableList());
      Route6FilterList localFilter6 =
          new Route6FilterList("~BGP_NETWORK6_NETWORKS_FILTER:" + vrfName + "~", lines);
      c.getRoute6FilterLists().put(localFilter6.getName(), localFilter6);
    }

    // Process active neighbors first.
    Map<Prefix, BgpActivePeerConfig> activeNeighbors =
        Conversions.getNeighbors(c, v, newBgpProcess, nxBgpGlobal, nxBgpVrf, _w);
    newBgpProcess.setNeighbors(ImmutableSortedMap.copyOf(activeNeighbors));

    // Process passive neighbors next
    Map<Prefix, BgpPassivePeerConfig> passiveNeighbors =
        Conversions.getPassiveNeighbors(c, v, newBgpProcess, nxBgpGlobal, nxBgpVrf, _w);
    newBgpProcess.setPassiveNeighbors(ImmutableSortedMap.copyOf(passiveNeighbors));

    v.setBgpProcess(newBgpProcess);
  }

  private static void convertHsrp(
      InterfaceHsrp hsrp, org.batfish.datamodel.Interface.Builder newIfaceBuilder) {
    Optional.ofNullable(hsrp.getVersion())
        .map(Object::toString)
        .ifPresent(newIfaceBuilder::setHsrpVersion);
    newIfaceBuilder.setHsrpGroups(
        hsrp.getGroups().entrySet().stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Entry::getKey, hsrpGroupEntry -> toHsrpGroup(hsrpGroupEntry.getValue()))));
  }

  private void convertInterface(Interface iface) {
    String ifaceName = iface.getName();
    org.batfish.datamodel.Interface newIface = toInterface(iface);
    _c.getAllInterfaces().put(ifaceName, newIface);
    org.batfish.datamodel.Vrf vrf = newIface.getVrf();
    vrf.getInterfaces().put(ifaceName, newIface);
  }

  private void convertInterfaces() {
    _portChannelMembers = HashMultimap.create();
    // non-port channels
    _interfaces.values().stream()
        .filter(iface -> iface.getType() != CiscoNxosInterfaceType.PORT_CHANNEL)
        .forEach(this::convertInterface);
    // port channels
    _interfaces.values().stream()
        .filter(iface -> iface.getType() == CiscoNxosInterfaceType.PORT_CHANNEL)
        .forEach(this::convertInterface);
  }

  private void convertIpAccessLists() {
    _ipAccessLists.forEach(
        (name, ipAccessList) -> _c.getIpAccessLists().put(name, toIpAccessList(ipAccessList)));
  }

  private void convertIpPrefixLists() {
    _ipPrefixLists.forEach(
        (name, ipPrefixList) ->
            _c.getRouteFilterLists().put(name, toRouteFilterList(ipPrefixList)));
  }

  private void convertStaticRoutes() {
    Stream.concat(Stream.of(_defaultVrf), _vrfs.values().stream())
        .forEach(this::convertStaticRoutes);
  }

  private void convertStaticRoutes(Vrf vrf) {
    _c.getVrfs()
        .get(vrf.getName())
        .setStaticRoutes(
            vrf.getStaticRoutes().values().stream()
                .map(this::toStaticRoute)
                .filter(Objects::nonNull)
                .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder())));
  }

  private void convertVrfs() {
    _c.getVrfs().put(DEFAULT_VRF_NAME, new org.batfish.datamodel.Vrf(DEFAULT_VRF_NAME));
    _c.getVrfs().put(NULL_VRF_NAME, new org.batfish.datamodel.Vrf(NULL_VRF_NAME));
    _vrfs.forEach((name, vrf) -> _c.getVrfs().put(name, toVrf(vrf)));
  }

  private @Nonnull CiscoNxosFamily createCiscoNxosFamily() {
    return CiscoNxosFamily.builder().build();
  }

  /** Disable Vlan interfaces without corresponding top-level vlan declaration. */
  private void disableUnregisteredVlanInterfaces() {
    _c.getAllInterfaces().values().stream()
        .filter(
            iface ->
                iface.getInterfaceType() == InterfaceType.VLAN
                    && !_vlans.keySet().contains(iface.getVlan()))
        .forEach(iface -> iface.setActive(false));
  }

  public @Nullable String getBannerExec() {
    return _bannerExec;
  }

  public void setBannerExec(@Nullable String bannerExec) {
    _bannerExec = bannerExec;
  }

  public @Nullable String getBannerMotd() {
    return _bannerMotd;
  }

  public void setBannerMotd(@Nullable String bannerMotd) {
    _bannerMotd = bannerMotd;
  }

  public @Nonnull BgpGlobalConfiguration getBgpGlobalConfiguration() {
    return _bgpGlobalConfiguration;
  }

  public @Nonnull Vrf getDefaultVrf() {
    return _defaultVrf;
  }

  public @Nullable Evpn getEvpn() {
    return _evpn;
  }

  public void setEvpn(@Nullable Evpn evpn) {
    _evpn = evpn;
  }

  @Override
  public @Nullable String getHostname() {
    return _hostname;
  }

  public @Nonnull Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  public @Nonnull Map<String, IpAccessList> getIpAccessLists() {
    return _ipAccessLists;
  }

  public @Nonnull Map<String, IpAsPathAccessList> getIpAsPathAccessLists() {
    return _ipAsPathAccessLists;
  }

  public @Nonnull Map<String, IpCommunityList> getIpCommunityLists() {
    return _ipCommunityLists;
  }

  public @Nonnull Map<String, IpPrefixList> getIpPrefixLists() {
    return _ipPrefixLists;
  }

  public @Nonnull Map<Integer, Nve> getNves() {
    return _nves;
  }

  public @Nonnull Map<String, DefaultVrfOspfProcess> getOspfProcesses() {
    return _ospfProcesses;
  }

  /** Range of VLAN IDs reserved by the system and therefore unassignable. */
  public @Nonnull IntegerSpace getReservedVlanRange() {
    return _reservedVlanRange;
  }

  public @Nonnull Map<String, RouteMap> getRouteMaps() {
    return _routeMaps;
  }

  public @Nullable String getVersion() {
    return _version;
  }

  public void setVersion(@Nullable String version) {
    _version = version;
  }

  public @Nonnull Map<Integer, Vlan> getVlans() {
    return _vlans;
  }

  public @Nonnull Map<String, Vrf> getVrfs() {
    return _vrfs;
  }

  private void markStructures() {
    markConcreteStructure(
        CiscoNxosStructureType.INTERFACE,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_UPDATE_SOURCE,
        CiscoNxosStructureUsage.INTERFACE_SELF_REFERENCE,
        CiscoNxosStructureUsage.IP_ROUTE_NEXT_HOP_INTERFACE,
        CiscoNxosStructureUsage.NVE_SOURCE_INTERFACE);
    markConcreteStructure(
        CiscoNxosStructureType.IP_AS_PATH_ACCESS_LIST,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_FILTER_LIST_IN,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_FILTER_LIST_OUT,
        CiscoNxosStructureUsage.BGP_NEIGHBOR6_FILTER_LIST_IN,
        CiscoNxosStructureUsage.BGP_NEIGHBOR6_FILTER_LIST_OUT);
    markConcreteStructure(
        CiscoNxosStructureType.IP_PREFIX_LIST,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_PREFIX_LIST_IN,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_PREFIX_LIST_OUT,
        CiscoNxosStructureUsage.ROUTE_MAP_MATCH_IP_ADDRESS_PREFIX_LIST);
    markConcreteStructure(
        CiscoNxosStructureType.IPV6_PREFIX_LIST,
        CiscoNxosStructureUsage.BGP_NEIGHBOR6_PREFIX_LIST_IN,
        CiscoNxosStructureUsage.BGP_NEIGHBOR6_PREFIX_LIST_OUT);
    markConcreteStructure(CiscoNxosStructureType.NVE, CiscoNxosStructureUsage.NVE_SELF_REFERENCE);
    markConcreteStructure(
        CiscoNxosStructureType.PORT_CHANNEL, CiscoNxosStructureUsage.INTERFACE_CHANNEL_GROUP);
    markConcreteStructure(
        CiscoNxosStructureType.ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_ADDITIONAL_PATHS_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_ADVERTISE_MAP,
        CiscoNxosStructureUsage.BGP_ATTRIBUTE_MAP,
        CiscoNxosStructureUsage.BGP_DAMPENING_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_DEFAULT_ORIGINATE_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_EXIST_MAP,
        CiscoNxosStructureUsage.BGP_INJECT_MAP,
        CiscoNxosStructureUsage.BGP_L2VPN_EVPN_RETAIN_ROUTE_TARGET_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_ADVERTISE_MAP,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_EXIST_MAP,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_NON_EXIST_MAP,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_REMOTE_AS_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_ROUTE_MAP_IN,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_ROUTE_MAP_OUT,
        CiscoNxosStructureUsage.BGP_NETWORK_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_NETWORK6_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_NEXTHOP_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_REDISTRIBUTE_DIRECT_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_REDISTRIBUTE_EIGRP_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_REDISTRIBUTE_ISIS_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_REDISTRIBUTE_LISP_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_REDISTRIBUTE_OSPF_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_REDISTRIBUTE_OSPFV3_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_REDISTRIBUTE_RIP_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_REDISTRIBUTE_STATIC_ROUTE_MAP,
        CiscoNxosStructureUsage.BGP_SUPPRESS_MAP,
        CiscoNxosStructureUsage.BGP_TABLE_MAP,
        CiscoNxosStructureUsage.BGP_UNSUPPRESS_MAP,
        CiscoNxosStructureUsage.OSPF_AREA_FILTER_LIST_IN,
        CiscoNxosStructureUsage.OSPF_AREA_FILTER_LIST_OUT);
    markConcreteStructure(
        CiscoNxosStructureType.ROUTER_OSPF,
        CiscoNxosStructureUsage.BGP_REDISTRIBUTE_OSPF_SOURCE_TAG);
    markConcreteStructure(
        CiscoNxosStructureType.BGP_TEMPLATE_PEER,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_INHERIT_PEER);
    markConcreteStructure(
        CiscoNxosStructureType.BGP_TEMPLATE_PEER_POLICY,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_INHERIT_PEER_POLICY);
    markConcreteStructure(
        CiscoNxosStructureType.BGP_TEMPLATE_PEER_SESSION,
        CiscoNxosStructureUsage.BGP_NEIGHBOR_INHERIT_PEER_SESSION);
    markConcreteStructure(CiscoNxosStructureType.VLAN, CiscoNxosStructureUsage.INTERFACE_VLAN);
    markConcreteStructure(
        CiscoNxosStructureType.VRF,
        CiscoNxosStructureUsage.INTERFACE_VRF_MEMBER,
        CiscoNxosStructureUsage.IP_ROUTE_NEXT_HOP_VRF);
  }

  @Override
  public void setHostname(String hostname) {
    checkNotNull(hostname, "hostname cannot be null");
    _hostname = hostname.toLowerCase();
  }

  @Override
  public void setVendor(ConfigurationFormat format) {}

  private static @Nonnull org.batfish.datamodel.hsrp.HsrpGroup toHsrpGroup(HsrpGroup group) {
    org.batfish.datamodel.hsrp.HsrpGroup.Builder builder =
        org.batfish.datamodel.hsrp.HsrpGroup.builder()
            .setGroupNumber(group.getGroup())
            .setIp(group.getIp())
            .setPreempt(
                group.getPreemptDelayMinimumSeconds() != null); // true iff any preempt delay is set
    if (group.getHelloIntervalMs() != null) {
      builder.setHelloTime(group.getHelloIntervalMs());
    }
    if (group.getHoldTimeMs() != null) {
      builder.setHoldTime(group.getHoldTimeMs());
    }
    if (group.getPriority() != null) {
      builder.setPriority(group.getPriority());
    }
    builder.setTrackActions(
        group.getTracks().entrySet().stream()
            .collect(
                ImmutableSortedMap.toImmutableSortedMap(
                    Comparator.naturalOrder(),
                    trackEntry -> trackEntry.getKey().toString(),
                    trackEntry -> new DecrementPriority(trackEntry.getValue().getDecrement()))));
    return builder.build();
  }

  private @Nonnull org.batfish.datamodel.Interface toInterface(Interface iface) {
    String ifaceName = iface.getName();
    org.batfish.datamodel.Interface.Builder newIfaceBuilder =
        org.batfish.datamodel.Interface.builder().setName(ifaceName);

    String parent = iface.getParentInterface();
    if (parent != null) {
      newIfaceBuilder.setDependencies(ImmutableSet.of(new Dependency(parent, DependencyType.BIND)));
    }

    newIfaceBuilder.setActive(!iface.getShutdown());

    if (iface.getAddress() != null) {
      newIfaceBuilder.setAddress(iface.getAddress().getAddress());
    }
    newIfaceBuilder.setSecondaryAddresses(
        iface.getSecondaryAddresses().stream()
            .map(InterfaceAddressWithAttributes::getAddress)
            .collect(ImmutableSet.toImmutableSet()));

    newIfaceBuilder.setDescription(iface.getDescription());

    newIfaceBuilder.setMtu(iface.getMtu());

    // switchport+vlan settings
    SwitchportMode switchportMode = iface.getSwitchportMode();
    newIfaceBuilder.setSwitchportMode(switchportMode);
    switch (iface.getSwitchportMode()) {
      case ACCESS:
        newIfaceBuilder.setSwitchport(true);
        newIfaceBuilder.setAccessVlan(iface.getAccessVlan());
        break;

      case NONE:
        newIfaceBuilder.setEncapsulationVlan(iface.getEncapsulationVlan());
        break;

      case TRUNK:
        newIfaceBuilder.setSwitchport(true);
        newIfaceBuilder.setAllowedVlans(iface.getAllowedVlans());
        newIfaceBuilder.setNativeVlan(iface.getNativeVlan());
        break;

      case DOT1Q_TUNNEL:
      case DYNAMIC_AUTO:
      case DYNAMIC_DESIRABLE:
      case FEX_FABRIC:
      case TAP:
      case TOOL:
      default:
        // unsupported
        break;
    }
    newIfaceBuilder.setVlan(iface.getVlan());
    newIfaceBuilder.setAutoState(iface.getAutostate());

    CiscoNxosInterfaceType type = iface.getType();
    newIfaceBuilder.setType(toInterfaceType(type, parent != null));

    Double speed;
    @Nullable Integer speedMbps = iface.getSpeedMbps();
    if (speedMbps != null) {
      speed = speedMbps * SPEED_CONVERSION_FACTOR;
    } else {
      speed = getDefaultSpeed(type);
    }
    newIfaceBuilder.setSpeed(speed);
    Integer nxosBandwidth = iface.getBandwidth();
    Double finalBandwidth;
    if (nxosBandwidth != null) {
      finalBandwidth = nxosBandwidth * BANDWIDTH_CONVERSION_FACTOR;
    } else if (speed != null) {
      finalBandwidth = speed;
    } else {
      finalBandwidth = getDefaultBandwidth(type);
    }
    newIfaceBuilder.setBandwidth(finalBandwidth);

    // port-channel members
    String portChannel = iface.getChannelGroup();
    if (portChannel != null) {
      newIfaceBuilder.setChannelGroup(portChannel);
      _portChannelMembers.put(portChannel, ifaceName);
    }

    // port-channels
    if (type == PORT_CHANNEL) {
      Collection<String> members = _portChannelMembers.get(ifaceName);
      newIfaceBuilder.setChannelGroupMembers(members);
      newIfaceBuilder.setDependencies(
          members.stream()
              .map(member -> new Dependency(member, DependencyType.AGGREGATE))
              .collect(ImmutableSet.toImmutableSet()));
    }

    if (iface.getHsrp() != null) {
      convertHsrp(iface.getHsrp(), newIfaceBuilder);
    }

    org.batfish.datamodel.Interface newIface = newIfaceBuilder.build();

    String vrfName = iface.getVrfMember();

    if (vrfName != null) {
      org.batfish.datamodel.Vrf vrf = _c.getVrfs().get(vrfName);
      if (vrf == null) {
        // Non-existent VRF set; disable and put in null VRF
        newIface.setActive(false);
        vrf = _c.getVrfs().get(NULL_VRF_NAME);
      } else if (_vrfs.get(vrfName).getShutdown()) {
        // VRF is shutdown; disable
        newIface.setActive(false);
      }
      newIface.setVrf(vrf);
    } else {
      // No VRF set; put in default VRF
      newIface.setVrf(_c.getDefaultVrf());
    }

    newIface.setOwner(_c);
    return newIface;
  }

  private @Nonnull InterfaceType toInterfaceType(
      CiscoNxosInterfaceType type, boolean subinterface) {
    switch (type) {
      case ETHERNET:
        return subinterface ? InterfaceType.LOGICAL : InterfaceType.PHYSICAL;
      case LOOPBACK:
        return InterfaceType.LOOPBACK;
      case MGMT:
        return InterfaceType.PHYSICAL;
      case PORT_CHANNEL:
        return subinterface ? InterfaceType.AGGREGATE_CHILD : InterfaceType.AGGREGATED;
      case VLAN:
        return InterfaceType.VLAN;
      default:
        return InterfaceType.UNKNOWN;
    }
  }

  private @Nonnull org.batfish.datamodel.IpAccessList toIpAccessList(IpAccessList list) {
    // TODO: handle and test top-level fragments behavior
    return org.batfish.datamodel.IpAccessList.builder()
        .setName(list.getName())
        .setSourceName(list.getName())
        .setSourceType(CiscoNxosStructureType.IP_ACCESS_LIST.getDescription())
        .setLines(
            list.getLines().values().stream()
                .flatMap(this::toIpAccessListLine)
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  /**
   * Converts the supplied {@code line} to zero or more vendor-independent {@link
   * org.batfish.datamodel.IpAccessListLine}s depending on semantics.
   */
  private @Nonnull Stream<org.batfish.datamodel.IpAccessListLine> toIpAccessListLine(
      IpAccessListLine line) {
    return line.accept(
        new IpAccessListLineVisitor<Stream<org.batfish.datamodel.IpAccessListLine>>() {
          @Override
          public Stream<org.batfish.datamodel.IpAccessListLine> visitActionIpAccessListLine(
              ActionIpAccessListLine actionIpAccessListLine) {
            LineAction action = actionIpAccessListLine.getAction();
            return Stream.of(
                org.batfish.datamodel.IpAccessListLine.builder()
                    .setAction(action)
                    .setMatchCondition(toAclLineMatchExpr(actionIpAccessListLine, action))
                    .setName(Long.toString(actionIpAccessListLine.getLine()))
                    .build());
          }

          @Override
          public Stream<org.batfish.datamodel.IpAccessListLine> visitRemarkIpAccessListLine(
              RemarkIpAccessListLine remarkIpAccessListLine) {
            return Stream.empty();
          }
        });
  }

  private @Nonnull AclLineMatchExpr toAclLineMatchExpr(
      ActionIpAccessListLine line, LineAction action) {
    /*
     * All rules:
     * - if 'fragments' present in rule
     *   - match only non-initial fragment
     * Also, for L3+L4 rules:
     * - permit rules
     *   - if L3-match and is non-initial-fragment
     *     - permit
     *   - if L3+L4 match and is initial fragment (or fragments not applicable to protocol)
     *     - permit
     * - deny rules
     *   - if L3+L4 match and is initial fragment (or fragments not applicable to protocol)
     *     - deny
     */
    // L3 match condition
    AclLineMatchExpr l3 = matchL3(line);

    // If L3 only, no special handling needed
    if (line.getL4Options() == null) {
      return l3;
    }

    // L4 handling
    AclLineMatchExpr l4 = matchL4(line);
    if (action == LineAction.PERMIT) {
      // permit if either non-initial fragment or l4 conditions match
      return and(l3, or(MATCH_NON_INITIAL_FRAGMENT_OFFSET, l4));
    }

    assert action == LineAction.DENY;
    // deny if initial fragment and l4 conditions match. else do nothing (no match).
    return and(MATCH_INITIAL_FRAGMENT_OFFSET, l4);
  }

  private @Nonnull AclLineMatchExpr matchL3(ActionIpAccessListLine actionIpAccessListLine) {
    HeaderSpace.Builder hs = HeaderSpace.builder();
    if (actionIpAccessListLine.getProtocol() != null) {
      hs.setIpProtocols(ImmutableList.of(actionIpAccessListLine.getProtocol()));
    }
    hs.setSrcIps(toIpSpace(actionIpAccessListLine.getSrcAddressSpec()));
    hs.setDstIps(toIpSpace(actionIpAccessListLine.getDstAddressSpec()));
    Layer3Options l3Options = actionIpAccessListLine.getL3Options();
    if (l3Options.getDscp() != null) {
      hs.setDscps(ImmutableList.of(l3Options.getDscp()));
    }
    if (l3Options.getPacketLength() != null) {
      hs.setPacketLengths(l3Options.getPacketLength().getSubRanges());
    }
    if (l3Options.getPrecedence() != null) {
      // TODO: support precedence matching
      return AclLineMatchExprs.FALSE;
    }
    if (l3Options.getTtl() != null) {
      // TODO: support ttl matching
      return AclLineMatchExprs.FALSE;
    }
    AclLineMatchExpr matchL3ExceptFragmentOffset = match(hs.build());
    return actionIpAccessListLine.getFragments()
        ? and(MATCH_NON_INITIAL_FRAGMENT_OFFSET, matchL3ExceptFragmentOffset)
        : matchL3ExceptFragmentOffset;
  }

  private @Nonnull IpSpace toIpSpace(IpAddressSpec ipAddressSpec) {
    return ipAddressSpec.accept(
        new IpAddressSpecVisitor<IpSpace>() {

          @Override
          public IpSpace visitAddrGroupIpAddressSpec(
              AddrGroupIpAddressSpec addrGroupIpAddressSpec) {
            // TODO: support addr-group
            return EmptyIpSpace.INSTANCE;
          }

          @Override
          public IpSpace visitLiteralIpAddressSpec(LiteralIpAddressSpec literalIpAddressSpec) {
            return literalIpAddressSpec.getIpSpace();
          }
        });
  }

  private @Nonnull AclLineMatchExpr matchL4(ActionIpAccessListLine actionIpAccessListLine) {
    return actionIpAccessListLine
        .getL4Options()
        .accept(
            new Layer4OptionsVisitor<AclLineMatchExpr>() {
              @Override
              public AclLineMatchExpr visitIcmpOptions(IcmpOptions icmpOptions) {
                HeaderSpace.Builder hs =
                    HeaderSpace.builder()
                        .setIcmpTypes(ImmutableList.of(SubRange.singleton(icmpOptions.getType())));
                @Nullable Integer code = icmpOptions.getCode();
                if (code != null) {
                  hs.setIcmpCodes(ImmutableList.of(SubRange.singleton(code)));
                }
                return match(hs.build());
              }

              @Override
              public AclLineMatchExpr visitIgmpOptions(IgmpOptions igmpOptions) {
                // TODO: IGMP header field handling
                return AclLineMatchExprs.FALSE;
              }

              @Override
              public AclLineMatchExpr visitTcpOptions(TcpOptions tcpOptions) {
                ImmutableList.Builder<AclLineMatchExpr> conjuncts = ImmutableList.builder();
                HeaderSpace.Builder hs = HeaderSpace.builder();
                if (tcpOptions.getEstablished()) {
                  hs.setTcpFlags(
                      ImmutableList.of(
                          TcpFlagsMatchConditions.builder()
                              .setUseAck(true)
                              .setTcpFlags(TcpFlags.builder().setAck(true).build())
                              .build(),
                          TcpFlagsMatchConditions.builder()
                              .setUseRst(true)
                              .setTcpFlags(TcpFlags.builder().setRst(true).build())
                              .build()));
                }
                if (tcpOptions.getDstPortSpec() != null) {
                  conjuncts.add(
                      toPorts(tcpOptions.getDstPortSpec(), HeaderSpace.Builder::setDstPorts));
                }
                if (tcpOptions.getHttpMethod() != null) {
                  // TODO: support HTTP METHOD matching
                  return AclLineMatchExprs.FALSE;
                }
                if (tcpOptions.getSrcPortSpec() != null) {
                  conjuncts.add(
                      toPorts(tcpOptions.getSrcPortSpec(), HeaderSpace.Builder::setSrcPorts));
                }
                if (tcpOptions.getTcpFlags() != null) {
                  // TODO: validate logic
                  int tcpFlagsMask = firstNonNull(tcpOptions.getTcpFlagsMask(), 0);
                  hs.setTcpFlags(
                      ImmutableList.of(
                          toTcpFlagsMatchConditions(tcpOptions.getTcpFlags(), tcpFlagsMask)));
                }
                if (tcpOptions.getTcpOptionLength() != null) {
                  // TODO: support TCP option length matching
                  return AclLineMatchExprs.FALSE;
                }
                return and(conjuncts.add(match(hs.build())).build());
              }

              @Override
              public AclLineMatchExpr visitUdpOptions(UdpOptions udpOptions) {
                ImmutableList.Builder<AclLineMatchExpr> conjuncts = ImmutableList.builder();
                if (udpOptions.getDstPortSpec() != null) {
                  conjuncts.add(
                      toPorts(udpOptions.getDstPortSpec(), HeaderSpace.Builder::setDstPorts));
                }
                if (udpOptions.getSrcPortSpec() != null) {
                  conjuncts.add(
                      toPorts(udpOptions.getSrcPortSpec(), HeaderSpace.Builder::setSrcPorts));
                }
                return and(conjuncts.build());
              }
            });
  }

  private static @Nonnull TcpFlagsMatchConditions toTcpFlagsMatchConditions(
      TcpFlags tcpFlags, int tcpFlagsMask) {
    // NX-OS only supports lower 6 control bits
    // 0 in mask means use
    int chooseOnes = ~tcpFlagsMask & 0b111111;
    return TcpFlagsMatchConditions.builder()
        .setTcpFlags(tcpFlags)
        .setUseFin((chooseOnes & 0x000001) != 0)
        .setUseSyn((chooseOnes & 0x000010) != 0)
        .setUseRst((chooseOnes & 0x000100) != 0)
        .setUsePsh((chooseOnes & 0x001000) != 0)
        .setUseAck((chooseOnes & 0x010000) != 0)
        .setUseUrg((chooseOnes & 0x100000) != 0)
        .build();
  }

  private @Nonnull AclLineMatchExpr toPorts(
      PortSpec portSpec,
      BiFunction<HeaderSpace.Builder, Iterable<SubRange>, HeaderSpace.Builder> setter) {
    // TODO: clean up when ports in headerspace are changed to IntegerSpace from SortedSet<SubRange>
    return portSpec.accept(
        new PortSpecVisitor<AclLineMatchExpr>() {
          @Override
          public AclLineMatchExpr visitLiteralPortSpec(LiteralPortSpec literalPortSpec) {
            return AclLineMatchExprs.match(
                setter
                    .apply(HeaderSpace.builder(), literalPortSpec.getPorts().getSubRanges())
                    .build());
          }

          @Override
          public AclLineMatchExpr visitPortGroupPortSpec(PortGroupPortSpec portGroupPortSpec) {
            // TODO: support port groups
            return AclLineMatchExprs.FALSE;
          }
        });
  }

  /**
   * Converts the supplied {@code staticRoute} to a a vendor-independent {@link
   * org.batfish.datamodel.StaticRoute} if all options are supported and static route contains no
   * undefined references. Otherwise, returns {@code null}.
   */
  private @Nullable org.batfish.datamodel.StaticRoute toStaticRoute(StaticRoute staticRoute) {
    // TODO: VI and VS support for lookup of next-hop-ip in a different VRF
    if (staticRoute.getNextHopVrf() != null) {
      return null;
    }
    // TODO: support track object number
    String nextHopInterface = staticRoute.getNextHopInterface();
    String newNextHopInterface;
    if (nextHopInterface != null) {
      if (!_interfaces.containsKey(nextHopInterface)) {
        // undefined reference
        return null;
      }
      newNextHopInterface = nextHopInterface;
    } else if (staticRoute.getDiscard()) {
      newNextHopInterface = NULL_INTERFACE_NAME;
    } else {
      newNextHopInterface = UNSET_NEXT_HOP_INTERFACE;
    }
    return org.batfish.datamodel.StaticRoute.builder()
        .setAdministrativeCost((int) staticRoute.getPreference())
        .setMetric(0L)
        .setNetwork(staticRoute.getPrefix())
        .setNextHopInterface(newNextHopInterface)
        .setNextHopIp(firstNonNull(staticRoute.getNextHopIp(), UNSET_ROUTE_NEXT_HOP_IP))
        .setTag(staticRoute.getTag())
        .build();
  }

  private @Nonnull Configuration toVendorIndependentConfiguration() {
    _c = new Configuration(_hostname, ConfigurationFormat.CISCO_NX);
    _c.getVendorFamily().setCiscoNxos(createCiscoNxosFamily());
    _c.setDefaultInboundAction(LineAction.PERMIT);
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);

    convertVrfs();
    convertInterfaces();
    disableUnregisteredVlanInterfaces();
    convertStaticRoutes();
    convertIpAccessLists();
    convertIpPrefixLists();
    convertBgp();

    markStructures();
    return _c;
  }

  @Override
  public @Nonnull List<Configuration> toVendorIndependentConfigurations()
      throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }

  private @Nonnull org.batfish.datamodel.Vrf toVrf(Vrf vrf) {
    org.batfish.datamodel.Vrf.Builder newVrfBuilder =
        org.batfish.datamodel.Vrf.builder().setName(vrf.getName());
    return newVrfBuilder.build();
  }
}
