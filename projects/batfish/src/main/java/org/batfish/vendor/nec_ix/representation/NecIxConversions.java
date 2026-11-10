package org.batfish.vendor.nec_ix.representation;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.Names.generatedBgpIndependentNetworkPolicyName;
import static org.batfish.datamodel.Names.generatedBgpRedistributionPolicyName;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocols;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.DEST_NAT;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;
import static org.batfish.datamodel.routing_policy.Common.initDenyAllBgpRedistributionPolicy;
import static org.batfish.datamodel.routing_policy.Common.matchDefaultRoute;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.PrependAsPath;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.AssignPortFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.ShiftIpAddressIntoSubnet;
import org.batfish.datamodel.transformation.Transformation;

/** Converts a {@link NecIxConfiguration} to the vendor-independent {@link Configuration} model. */
public final class NecIxConversions {

  private static final int DEFAULT_MTU = 1500;

  /** {@code distance} defaults: "EXT: 20, INT: 200". */
  private static final int EBGP_ADMIN_COST = 20;

  private static final int IBGP_ADMIN_COST = 200;

  /** Batfish gives locally originated BGP routes the iBGP distance, as Cisco-like vendors do. */
  private static final int LOCAL_ADMIN_COST = 200;

  /** "Default: 49152-65535" for {@code ip napt translation port-range}. */
  public static final SubRange DEFAULT_NAPT_PORT_RANGE = new SubRange(49152, 65535);

  /**
   * Protocols that NAPT translates. Port translation needs a port (or an ICMP identifier) to tell
   * return traffic apart, so other protocols are not translated.
   */
  private static final List<IpProtocol> NAPT_PROTOCOLS =
      ImmutableList.of(IpProtocol.TCP, IpProtocol.UDP, IpProtocol.ICMP);

  /** Name of the generated {@link IpAccessList} filtering packets received on {@code iface}. */
  public static @Nonnull String computeInboundFilterName(String iface) {
    return String.format("~IP_FILTER_IN~%s~", iface);
  }

  /** Name of the generated {@link IpAccessList} filtering packets sent out {@code iface}. */
  public static @Nonnull String computeOutboundFilterName(String iface) {
    return String.format("~IP_FILTER_OUT~%s~", iface);
  }

  /** Name of the policy applied to routes received from {@code peer} in {@code vrf}. */
  public static @Nonnull String computeBgpImportPolicyName(String vrf, Ip peer) {
    return String.format("~BGP_PEER_IMPORT_POLICY:%s:%s~", vrf, peer);
  }

  /** Name of the policy applied to routes advertised to {@code peer} in {@code vrf}. */
  public static @Nonnull String computeBgpExportPolicyName(String vrf, Ip peer) {
    return String.format("~BGP_PEER_EXPORT_POLICY:%s:%s~", vrf, peer);
  }

  private static final String BGP_DEFAULT_ROUTE_EXPORT_POLICY =
      "~BGP_ORIGINATE_DEFAULT_ALWAYS_EXPORT_POLICY~";

  private static final String REJECT_ALL_POLICY = "~REJECT_ALL~";

  private final @Nonnull NecIxConfiguration _vc;
  private final @Nonnull Warnings _w;
  private Configuration _c;

  NecIxConversions(NecIxConfiguration vc) {
    _vc = vc;
    _w = vc.getWarnings();
  }

  @Nonnull
  Configuration convert() {
    _c = new Configuration(_vc.getHostname(), ConfigurationFormat.NEC_IX);
    _c.setHumanName(_vc.getRawHostname());
    _c.setDeviceModel(DeviceModel.NEC_IX_UNSPECIFIED);
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);
    _c.setDefaultInboundAction(LineAction.PERMIT);
    // IX advertises what is in the BGP table (received routes, `network`, `redistribute`), not
    // whatever is in the routing table.
    _c.setExportBgpFromBgpRib(true);

    convertVrfs();
    convertAccessLists();
    convertPrefixLists();
    convertRouteMaps();
    convertInterfaces();
    convertNat();
    convertStaticRoutes();
    convertBgp();
    return _c;
  }

  // ------------------------------------------------------------------------------------- VRFs

  private void convertVrfs() {
    _c.getVrfs().put(DEFAULT_VRF_NAME, new Vrf(DEFAULT_VRF_NAME));
    Stream.of(
            _vc.getInterfaces().values().stream().map(NecIxInterface::getVrf),
            _vc.getStaticRoutes().stream().map(NecIxStaticRoute::getVrf),
            Optional.ofNullable(_vc.getBgpProcess()).stream()
                .flatMap(p -> p.getVrfs().keySet().stream()))
        .flatMap(s -> s)
        .filter(Objects::nonNull)
        .forEach(vrf -> _c.getVrfs().computeIfAbsent(vrf, Vrf::new));
  }

  private @Nonnull Vrf getVrf(@Nullable String name) {
    return _c.getVrfs().get(name == null ? DEFAULT_VRF_NAME : name);
  }

  // ------------------------------------------------------------------------------ access-lists

  /**
   * Converts each {@code ip access-list} to an {@link IpAccessList} of the same name. The list has
   * no trailing deny: interface filters reference it with {@link AclAclLine}, which falls through
   * to the next list when no entry matches.
   */
  private void convertAccessLists() {
    for (NecIxAccessList acl : _vc.getAccessLists().values()) {
      List<AclLine> lines =
          acl.getOrderedLines().stream()
              .map(
                  line ->
                      (AclLine)
                          (line.getAction() == LineAction.PERMIT
                                  ? ExprAclLine.accepting()
                                  : ExprAclLine.rejecting())
                              .setMatchCondition(match(line.getHeaderSpace()))
                              .setName(line.getText())
                              .build())
              .collect(ImmutableList.toImmutableList());
      IpAccessList.builder().setOwner(_c).setName(acl.getName()).setLines(lines).build();
    }
  }

  /**
   * The filter for one direction of an interface: the applied access-lists in ascending {@code ip
   * filter} sequence number, then a deny for packets that matched none of them.
   */
  private @Nonnull IpAccessList toInterfaceFilter(String name, SortedMap<Integer, String> filters) {
    ImmutableList.Builder<AclLine> lines = ImmutableList.builder();
    filters.forEach(
        (seq, aclName) -> {
          if (!_c.getIpAccessLists().containsKey(aclName)) {
            // An undefined access-list filters nothing. Reported through structure tracking.
            return;
          }
          lines.add(new AclAclLine(String.format("ip filter %s %d", aclName, seq), aclName));
        });
    lines.add(
        ExprAclLine.rejecting()
            .setMatchCondition(TRUE)
            .setName("implicit deny: no access-list matched")
            .build());
    return IpAccessList.builder().setOwner(_c).setName(name).setLines(lines.build()).build();
  }

  // ------------------------------------------------------------------------------ prefix-lists

  private void convertPrefixLists() {
    for (NecIxPrefixList pl : _vc.getPrefixLists().values()) {
      List<RouteFilterLine> lines =
          pl.getLines().values().stream()
              .map(l -> new RouteFilterLine(l.getAction(), l.getPrefix(), l.getLengthRange()))
              .collect(ImmutableList.toImmutableList());
      _c.getRouteFilterLists().put(pl.getName(), new RouteFilterList(pl.getName(), lines));
    }
  }

  // -------------------------------------------------------------------------------- route-maps

  /**
   * Converts each {@code route-map} to a {@link RoutingPolicy} of the same name. Entries are tried
   * in sequence order; the first whose match conditions hold applies its {@code set} commands and
   * accepts ({@code permit}) or rejects ({@code deny}) the route. A route matching no entry is
   * rejected.
   */
  private void convertRouteMaps() {
    for (NecIxRouteMap map : _vc.getRouteMaps().values()) {
      RoutingPolicy.Builder policy = RoutingPolicy.builder().setOwner(_c).setName(map.getName());
      for (NecIxRouteMap.Entry entry : map.getEntries().values()) {
        ImmutableList.Builder<Statement> body = ImmutableList.builder();
        if (entry.getAction() == LineAction.PERMIT) {
          body.addAll(toSetStatements(entry));
          body.add(Statements.ExitAccept.toStaticStatement());
        } else {
          body.add(Statements.ExitReject.toStaticStatement());
        }
        If statement = new If(toMatchCondition(entry), body.build());
        statement.setComment(
            String.format(
                "route-map %s %s %d",
                map.getName(),
                entry.getAction() == LineAction.PERMIT ? "permit" : "deny",
                entry.getSequence()));
        policy.addStatement(statement);
      }
      policy.addStatement(Statements.ExitReject.toStaticStatement());
      policy.build();
    }
  }

  private @Nonnull BooleanExpr toMatchCondition(NecIxRouteMap.Entry entry) {
    if (entry.getMatchPrefixLists().isEmpty()) {
      // An entry without match commands matches every route.
      return BooleanExprs.TRUE;
    }
    List<BooleanExpr> disjuncts =
        entry.getMatchPrefixLists().stream()
            .map(
                name ->
                    _c.getRouteFilterLists().containsKey(name)
                        ? new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(name))
                        // The manual does not say how an undefined prefix-list behaves. Treat it
                        // as matching nothing; the reference is reported as undefined.
                        : BooleanExprs.FALSE)
            .collect(ImmutableList.toImmutableList());
    return disjuncts.size() == 1 ? disjuncts.get(0) : new Disjunction(disjuncts);
  }

  private static @Nonnull List<Statement> toSetStatements(NecIxRouteMap.Entry entry) {
    ImmutableList.Builder<Statement> statements = ImmutableList.builder();
    if (entry.getSetLocalPreference() != null) {
      statements.add(new SetLocalPreference(new LiteralLong(entry.getSetLocalPreference())));
    }
    if (entry.getSetMetric() != null) {
      statements.add(new SetMetric(new LiteralLong(entry.getSetMetric())));
    }
    List<StandardCommunity> communities = entry.getSetCommunities();
    if (communities != null) {
      CommunitySetExpr literal = new LiteralCommunitySet(CommunitySet.of(communities));
      statements.add(
          new SetCommunities(
              entry.getSetCommunitiesAdditive()
                  ? CommunitySetUnion.of(InputCommunities.instance(), literal)
                  : literal));
    }
    if (!entry.getAsPathPrepend().isEmpty()) {
      statements.add(
          new PrependAsPath(
              new LiteralAsList(
                  entry.getAsPathPrepend().stream()
                      .map(ExplicitAs::new)
                      .collect(ImmutableList.toImmutableList()))));
    }
    return statements.build();
  }

  // -------------------------------------------------------------------------------- interfaces

  private void convertInterfaces() {
    // Ethernet ports are never configured directly but carry the sub-interfaces, so create them
    // first; sub-interfaces BIND to them.
    _vc.getInterfaces().values().stream()
        .map(iface -> NecIxInterface.computeParentPort(iface.getName()))
        .filter(Objects::nonNull)
        .distinct()
        .forEach(
            port ->
                Interface.builder()
                    .setOwner(_c)
                    .setName(port)
                    .setVrf(_c.getDefaultVrf())
                    .setType(InterfaceType.PHYSICAL)
                    .setAdminUp(!isDeviceShutdown(port))
                    .build());
    for (NecIxInterface iface : _vc.getInterfaces().values()) {
      convertInterface(iface);
    }
  }

  private boolean isDeviceShutdown(String interfaceName) {
    return _vc.getShutdownDevices().contains(NecIxInterface.computeDevice(interfaceName));
  }

  private void convertInterface(NecIxInterface iface) {
    String name = iface.getName();
    Interface.Builder viIface =
        Interface.builder()
            .setOwner(_c)
            .setName(name)
            .setVrf(getVrf(iface.getVrf()))
            .setType(toInterfaceType(iface.getType()))
            .setAdminUp(isAdminUp(iface))
            .setMtu(iface.getMtu() != null ? iface.getMtu() : DEFAULT_MTU)
            .setDescription(iface.getDescription());
    if (iface.getAddress() != null) {
      viIface.setAddresses(
          iface.getAddress(), ImmutableList.<InterfaceAddress>copyOf(iface.getSecondaryAddresses()));
    }
    if (iface.getVlan() != null) {
      viIface.setEncapsulationVlan(iface.getVlan());
    }
    String parent = NecIxInterface.computeParentPort(name);
    if (parent != null) {
      viIface.setDependencies(
          ImmutableSet.of(new Interface.Dependency(parent, Interface.DependencyType.BIND)));
    }
    if (!iface.getInboundFilters().isEmpty()) {
      viIface.setIncomingFilter(
          toInterfaceFilter(computeInboundFilterName(name), iface.getInboundFilters()));
    }
    if (!iface.getOutboundFilters().isEmpty()) {
      viIface.setOutgoingFilter(
          toInterfaceFilter(computeOutboundFilterName(name), iface.getOutboundFilters()));
    }
    viIface.build();
  }

  private boolean isAdminUp(NecIxInterface iface) {
    return switch (iface.getType()) {
      // `shutdown` "cannot be used on Loopback and Null interfaces".
      case LOOPBACK, NULL -> true;
      // "Default: the interface is stopped", so only an explicit `no shutdown` brings it up.
      default -> Boolean.FALSE.equals(iface.getShutdown()) && !isDeviceShutdown(iface.getName());
    };
  }

  private static @Nonnull InterfaceType toInterfaceType(NecIxInterface.Type type) {
    return switch (type) {
      case ETHERNET_PORT, OTHER -> InterfaceType.PHYSICAL;
      case ETHERNET, BVI -> InterfaceType.LOGICAL;
      case LOOPBACK -> InterfaceType.LOOPBACK;
      case NULL -> InterfaceType.NULL;
      case TUNNEL -> InterfaceType.TUNNEL;
    };
  }

  // ------------------------------------------------------------------------------------- NAT

  /**
   * Applies NAPT ({@code ip napt}) and static NAT ({@code ip nat static}). Both translate the
   * source of traffic leaving the interface they are configured on, which faces the outside.
   * Static NAT also translates the destination of traffic arriving from the outside. Return traffic
   * of NAPT is handled by a session, which is how a real NAPT table behaves.
   */
  private void convertNat() {
    for (NecIxInterface iface : _vc.getInterfaces().values()) {
      Interface viIface = _c.getAllInterfaces().get(iface.getName());
      List<Transformation.Builder> outgoing = new ArrayList<>();
      if (iface.getNatEnabled()) {
        for (NecIxInterface.StaticNat nat : iface.getStaticNats()) {
          outgoing.add(
              Transformation.when(matchSrc(nat.getInside().toIpSpace()))
                  .apply(
                      new ShiftIpAddressIntoSubnet(SOURCE_NAT, IpField.SOURCE, nat.getOutside())));
        }
        viIface.setIncomingTransformation(
            chain(
                iface.getStaticNats().stream()
                    .map(
                        nat ->
                            Transformation.when(matchDst(nat.getOutside().toIpSpace()))
                                .apply(
                                    new ShiftIpAddressIntoSubnet(
                                        DEST_NAT, IpField.DESTINATION, nat.getInside())))
                    .collect(ImmutableList.toImmutableList())));
      }
      if (iface.getNaptEnabled()) {
        List<Transformation.Builder> napt = toNaptTransformations(iface);
        outgoing.addAll(napt);
        if (!napt.isEmpty()) {
          viIface.setFirewallSessionInterfaceInfo(
              new FirewallSessionInterfaceInfo(
                  FirewallSessionInterfaceInfo.Action.FORWARD_OUT_IFACE,
                  ImmutableList.of(iface.getName()),
                  filterNameIfPresent(viIface.getIncomingFilter()),
                  filterNameIfPresent(viIface.getOutgoingFilter())));
        }
      }
      viIface.setOutgoingTransformation(chain(outgoing));
    }
  }

  private static @Nullable String filterNameIfPresent(@Nullable IpAccessList acl) {
    return acl == null ? null : acl.getName();
  }

  /** Chains {@code transformations} so that the first one whose guard matches applies. */
  private static @Nullable Transformation chain(List<Transformation.Builder> transformations) {
    Transformation chain = null;
    for (Transformation.Builder t : transformations.reversed()) {
      chain = t.setOrElse(chain).build();
    }
    return chain;
  }

  private @Nonnull List<Transformation.Builder> toNaptTransformations(NecIxInterface iface) {
    // Batfish translates a flow with the first matching transformation, so only the first port
    // range is ever used.
    SubRange ports =
        iface.getNaptPortRanges() != null
            ? iface.getNaptPortRanges().get(0)
            : DEFAULT_NAPT_PORT_RANGE;
    Ip defaultAddress = toNaptAddress(iface);
    if (iface.getNaptInsides().isEmpty()) {
      // "Default: every address is on the inside."
      return toNaptTransformation(iface, TRUE, defaultAddress, ports).stream().toList();
    }
    ImmutableList.Builder<Transformation.Builder> transformations = ImmutableList.builder();
    for (NecIxInterface.NaptInside inside : iface.getNaptInsides()) {
      if (!_c.getIpAccessLists().containsKey(inside.getAccessList())) {
        // An undefined access-list; reported through structure tracking.
        continue;
      }
      toNaptTransformation(
              iface,
              permittedByAcl(inside.getAccessList()),
              inside.getOutsideAddress() != null ? inside.getOutsideAddress() : defaultAddress,
              ports)
          .ifPresent(transformations::add);
    }
    return transformations.build();
  }

  private @Nonnull Optional<Transformation.Builder> toNaptTransformation(
      NecIxInterface iface, AclLineMatchExpr insideMatch, @Nullable Ip address, SubRange ports) {
    if (address == null) {
      _w.redFlagf(
          "Interface %s: cannot determine the NAPT address; NAPT is not applied", iface.getName());
      return Optional.empty();
    }
    return Optional.of(
        Transformation.when(and(insideMatch, matchIpProtocols(NAPT_PROTOCOLS)))
            .apply(
                new AssignIpAddressFromPool(
                    SOURCE_NAT, IpField.SOURCE, ImmutableRangeSet.of(Range.singleton(address))),
                new AssignPortFromPool(
                    SOURCE_NAT, PortField.SOURCE, ports.getStart(), ports.getEnd())));
  }

  /** {@code ip napt address}, defaulting to the interface's own address. */
  private @Nullable Ip toNaptAddress(NecIxInterface iface) {
    if (iface.getNaptAddress() != null) {
      return iface.getNaptAddress();
    }
    NecIxInterface source =
        iface.getNaptAddressInterface() != null
            ? _vc.getInterfaces().get(iface.getNaptAddressInterface())
            : iface;
    return source == null || source.getAddress() == null ? null : source.getAddress().getIp();
  }

  // ---------------------------------------------------------------------------- static routes

  private void convertStaticRoutes() {
    for (NecIxStaticRoute route : _vc.getStaticRoutes()) {
      getVrf(route.getVrf())
          .getStaticRoutes()
          .add(
              StaticRoute.builder()
                  .setNetwork(route.getNetwork())
                  .setNextHop(toNextHop(route))
                  .setAdministrativeCost(route.getDistance())
                  .setMetric(route.getMetric())
                  .setTag(route.getTag())
                  .build());
    }
  }

  private static @Nonnull NextHop toNextHop(NecIxStaticRoute route) {
    String iface = route.getNextHopInterface();
    Ip ip = route.getNextHopIp();
    if (iface != null && NecIxInterface.computeType(iface) == NecIxInterface.Type.NULL) {
      return NextHopDiscard.instance();
    } else if (iface != null && ip != null) {
      return NextHopInterface.of(iface, ip);
    } else if (iface != null) {
      return NextHopInterface.of(iface);
    }
    assert ip != null;
    return NextHopIp.of(ip);
  }

  // -------------------------------------------------------------------------------------- BGP

  private void convertBgp() {
    NecIxBgpProcess proc = _vc.getBgpProcess();
    if (proc == null) {
      return;
    }
    proc.getVrfs().forEach((vrfName, bgpVrf) -> convertBgpVrf(proc, vrfName, bgpVrf));
  }

  private void convertBgpVrf(NecIxBgpProcess proc, String vrfName, NecIxBgpVrf bgpVrf) {
    Ip routerId = proc.getRouterId() != null ? proc.getRouterId() : inferRouterId(vrfName);
    BgpProcess viProc =
        BgpProcess.builder()
            .setRouterId(routerId)
            .setEbgpAdminCost(EBGP_ADMIN_COST)
            .setIbgpAdminCost(IBGP_ADMIN_COST)
            .setLocalAdminCost(LOCAL_ADMIN_COST)
            .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .build();
    getVrf(vrfName).setBgpProcess(viProc);

    convertBgpNetworks(proc, vrfName, bgpVrf, viProc);
    viProc.setRedistributionPolicy(toBgpRedistributionPolicy(proc, vrfName, bgpVrf));

    for (NecIxBgpNeighbor neighbor : bgpVrf.getNeighbors().values()) {
      if (neighbor.getRemoteAs() == null) {
        _w.redFlagf(
            "BGP neighbor %s in VRF %s has no remote-as; ignoring it",
            neighbor.getAddress(), vrfName);
        continue;
      }
      if (neighbor.getShutdown()) {
        continue;
      }
      convertBgpNeighbor(proc, vrfName, bgpVrf, neighbor, viProc);
    }
  }

  /**
   * {@code network} statements, and {@code originate-default} without {@code always}: the prefix
   * is advertised while the routing table has a non-BGP route for exactly that prefix.
   */
  private void convertBgpNetworks(
      NecIxBgpProcess proc, String vrfName, NecIxBgpVrf bgpVrf, BgpProcess viProc) {
    PrefixSpace networks = new PrefixSpace();
    bgpVrf.getNetworks().forEach(networks::addPrefix);
    if (bgpVrf.getOriginateDefault() == NecIxBgpVrf.OriginateDefault.CONDITIONAL) {
      networks.addPrefix(Prefix.ZERO);
    }
    if (networks.isEmpty()) {
      return;
    }
    String policyName = generatedBgpIndependentNetworkPolicyName(vrfName);
    RoutingPolicy.builder()
        .setOwner(_c)
        .setName(policyName)
        .addStatement(
            new If(
                new Conjunction(
                    ImmutableList.of(
                        new MatchPrefixSet(
                            DestinationNetwork.instance(), new ExplicitPrefixSet(networks)),
                        new Not(
                            new MatchProtocol(
                                RoutingProtocol.BGP,
                                RoutingProtocol.IBGP,
                                RoutingProtocol.AGGREGATE)))),
                ImmutableList.of(
                    new SetOrigin(new LiteralOrigin(OriginType.IGP, null)),
                    new SetLocalPreference(new LiteralLong(proc.getDefaultLocalPreference())),
                    Statements.ExitAccept.toStaticStatement())))
        .addStatement(Statements.ExitReject.toStaticStatement())
        .build();
    viProc.addToOriginationSpace(networks);
    viProc.setIndependentNetworkPolicy(policyName);
  }

  private @Nonnull String toBgpRedistributionPolicy(
      NecIxBgpProcess proc, String vrfName, NecIxBgpVrf bgpVrf) {
    if (bgpVrf.getRedistributions().isEmpty()) {
      return initDenyAllBgpRedistributionPolicy(_c);
    }
    String policyName = generatedBgpRedistributionPolicyName(vrfName);
    RoutingPolicy.Builder policy = RoutingPolicy.builder().setOwner(_c).setName(policyName);
    for (NecIxBgpVrf.Redistribution redistribution : bgpVrf.getRedistributions()) {
      ImmutableList.Builder<Statement> body = ImmutableList.builder();
      body.add(new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE, null)));
      body.add(new SetLocalPreference(new LiteralLong(proc.getDefaultLocalPreference())));
      if (redistribution.getMetric() != null) {
        body.add(new SetMetric(new LiteralLong(redistribution.getMetric())));
      }
      BooleanExpr guard = new MatchProtocol(redistribution.getProtocol());
      if (redistribution.getRouteMap() != null) {
        guard =
            new Conjunction(
                ImmutableList.of(guard, new CallExpr(routeMapOrRejectAll(redistribution.getRouteMap()))));
      }
      body.add(Statements.ExitAccept.toStaticStatement());
      policy.addStatement(new If(guard, body.build()));
    }
    policy.addStatement(Statements.ExitReject.toStaticStatement());
    policy.build();
    return policyName;
  }

  private void convertBgpNeighbor(
      NecIxBgpProcess proc,
      String vrfName,
      NecIxBgpVrf bgpVrf,
      NecIxBgpNeighbor neighbor,
      BgpProcess viProc) {
    Ip peer = neighbor.getAddress();
    long remoteAs = neighbor.getRemoteAs();
    boolean ebgp = remoteAs != proc.getAsn();
    String importPolicy = computeBgpImportPolicyName(vrfName, peer);
    String exportPolicy = computeBgpExportPolicyName(vrfName, peer);

    // Import: a route received without LOCAL_PREF (i.e. from an eBGP peer) gets
    // default-local-preference, then the inbound route-map decides.
    RoutingPolicy.Builder importBuilder =
        RoutingPolicy.builder().setOwner(_c).setName(importPolicy);
    if (ebgp) {
      importBuilder.addStatement(
          new SetLocalPreference(new LiteralLong(proc.getDefaultLocalPreference())));
    }
    importBuilder.addStatement(toRouteMapStatement(neighbor.getRouteMapIn())).build();

    // Export.
    BgpActivePeerConfig.Builder viNeighbor = BgpActivePeerConfig.builder();
    RoutingPolicy.Builder exportBuilder =
        RoutingPolicy.builder().setOwner(_c).setName(exportPolicy);
    if (!neighbor.getSendDefault()) {
      // `no neighbor <address> send-default` stops advertising the default route.
      exportBuilder.addStatement(
          new If(
              matchDefaultRoute(),
              ImmutableList.of(Statements.ExitReject.toStaticStatement())));
    } else if (bgpVrf.getOriginateDefault() == NecIxBgpVrf.OriginateDefault.ALWAYS) {
      // `originate-default always` advertises a default route even without one in the routing
      // table.
      initBgpDefaultRouteExportPolicy();
      exportBuilder.addStatement(
          new If(
              new CallExpr(BGP_DEFAULT_ROUTE_EXPORT_POLICY),
              ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
      viNeighbor.setGeneratedRoutes(
          ImmutableSet.of(GeneratedRoute.builder().setNetwork(Prefix.ZERO).build()));
    }
    if (neighbor.getNextHopSelf()) {
      exportBuilder.addStatement(new SetNextHop(SelfNextHop.getInstance()));
    }
    exportBuilder.addStatement(toRouteMapStatement(neighbor.getRouteMapOut())).build();

    viNeighbor
        .setBgpProcess(viProc)
        .setPeerAddress(peer)
        .setRemoteAs(remoteAs)
        .setLocalAs(proc.getAsn())
        .setLocalIp(inferLocalIp(vrfName, neighbor))
        .setEbgpMultihop(neighbor.getEbgpMultihop() != null && neighbor.getEbgpMultihop() > 1)
        .setIpv4UnicastAddressFamily(
            Ipv4UnicastAddressFamily.builder()
                .setAddressFamilyCapabilities(
                    AddressFamilyCapabilities.builder()
                        .setSendCommunity(true)
                        .setSendExtendedCommunity(true)
                        .build())
                .setImportPolicy(importPolicy)
                .setExportPolicy(exportPolicy)
                .build())
        .build();
  }

  /** Accepts the route if {@code routeMap} permits it; without a route-map, accepts every route. */
  private @Nonnull Statement toRouteMapStatement(@Nullable String routeMap) {
    if (routeMap == null) {
      return Statements.ExitAccept.toStaticStatement();
    }
    return new If(
        new CallExpr(routeMapOrRejectAll(routeMap)),
        ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
        ImmutableList.of(Statements.ExitReject.toStaticStatement()));
  }

  /**
   * The name of the policy for {@code routeMap}. A reference to an undefined route-map rejects
   * every route (as on Cisco IOS; the IX manual does not say), and is reported as undefined.
   */
  private @Nonnull String routeMapOrRejectAll(String routeMap) {
    if (_c.getRoutingPolicies().containsKey(routeMap)) {
      return routeMap;
    }
    if (!_c.getRoutingPolicies().containsKey(REJECT_ALL_POLICY)) {
      RoutingPolicy.builder()
          .setOwner(_c)
          .setName(REJECT_ALL_POLICY)
          .addStatement(Statements.ExitReject.toStaticStatement())
          .build();
    }
    return REJECT_ALL_POLICY;
  }

  private void initBgpDefaultRouteExportPolicy() {
    if (_c.getRoutingPolicies().containsKey(BGP_DEFAULT_ROUTE_EXPORT_POLICY)) {
      return;
    }
    RoutingPolicy.builder()
        .setOwner(_c)
        .setName(BGP_DEFAULT_ROUTE_EXPORT_POLICY)
        .addStatement(
            new If(
                new Conjunction(
                    ImmutableList.of(
                        matchDefaultRoute(), new MatchProtocol(RoutingProtocol.AGGREGATE))),
                ImmutableList.of(
                    new SetOrigin(new LiteralOrigin(OriginType.IGP, null)),
                    Statements.ReturnTrue.toStaticStatement())))
        .addStatement(Statements.ReturnFalse.toStaticStatement())
        .build();
  }

  /**
   * "Default: the largest IPv4 address on a loopback interface, or if no loopback interface has
   * an address, the largest address on any other interface." In a VRF, the router ID is chosen
   * from the VRF's interfaces.
   */
  private @Nonnull Ip inferRouterId(String vrfName) {
    Map<String, Interface> interfaces = _c.getAllInterfaces(vrfName);
    Comparator<Ip> order = Comparator.naturalOrder();
    Optional<Ip> loopback =
        interfaces.values().stream()
            .filter(i -> i.getInterfaceType() == InterfaceType.LOOPBACK)
            .flatMap(i -> i.getAllConcreteAddresses().stream())
            .map(ConcreteInterfaceAddress::getIp)
            .max(order);
    return loopback.orElseGet(
        () ->
            interfaces.values().stream()
                .flatMap(i -> i.getAllConcreteAddresses().stream())
                .map(ConcreteInterfaceAddress::getIp)
                .max(order)
                .orElse(Ip.ZERO));
  }

  /**
   * The address of {@code update-source}, or else of the interface in the VRF on the same subnet
   * as the peer ("the address of the interface the TCP packets are sent from").
   */
  private @Nullable Ip inferLocalIp(String vrfName, NecIxBgpNeighbor neighbor) {
    if (neighbor.getUpdateSource() != null) {
      Interface source = _c.getAllInterfaces().get(neighbor.getUpdateSource());
      if (source == null || source.getConcreteAddress() == null) {
        _w.redFlagf(
            "BGP neighbor %s: update-source %s has no address",
            neighbor.getAddress(), neighbor.getUpdateSource());
        return null;
      }
      return source.getConcreteAddress().getIp();
    }
    return _c.getAllInterfaces(vrfName).values().stream()
        .flatMap(i -> i.getAllConcreteAddresses().stream())
        .filter(address -> address.getPrefix().containsIp(neighbor.getAddress()))
        .map(ConcreteInterfaceAddress::getIp)
        .findFirst()
        .orElse(null);
  }
}
