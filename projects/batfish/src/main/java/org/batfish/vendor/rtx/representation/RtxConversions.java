package org.batfish.vendor.rtx.representation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcPort;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchTcpFlags;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.batfish.datamodel.flow.TransformationStep.TransformationType.SOURCE_NAT;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TcpFlagsMatchConditions;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.transformation.ApplyAny;
import org.batfish.datamodel.transformation.AssignIpAddressFromPool;
import org.batfish.datamodel.transformation.AssignPortFromPool;
import org.batfish.datamodel.transformation.IpField;
import org.batfish.datamodel.transformation.PortField;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;

/** Converts an {@link RtxConfiguration} to the vendor-independent {@link Configuration} model. */
public final class RtxConversions {

  /** Administrative cost Batfish assigns to a static route, matching other vendors' default. */
  private static final int STATIC_ROUTE_ADMIN_COST = 1;

  private static final int EBGP_ADMIN_COST = 20;
  private static final int IBGP_ADMIN_COST = 200;
  private static final int LOCAL_ADMIN_COST = 0;

  /** Name of the generated {@link IpAccessList} filtering packets received on {@code iface}. */
  public static String computeInboundFilterName(String iface) {
    return String.format("~SECURE_FILTER_IN~%s~", iface);
  }

  /** Name of the generated {@link IpAccessList} filtering packets sent out {@code iface}. */
  public static String computeOutboundFilterName(String iface) {
    return String.format("~SECURE_FILTER_OUT~%s~", iface);
  }

  /** Name of the policy applied to routes received from {@code peerAddress}. */
  public static String computeBgpImportPolicyName(Ip peerAddress) {
    return String.format("~RTX_BGP_IMPORT_POLICY~%s~", peerAddress);
  }

  /** Name of the policy applied while advertising routes to {@code peerAddress}. */
  public static String computeBgpExportPolicyName(Ip peerAddress) {
    return String.format("~RTX_BGP_EXPORT_POLICY~%s~", peerAddress);
  }

  /**
   * Builds one {@link IpAccessList} per interface and direction that has {@code ip <if> secure
   * filter} applied.
   *
   * <p>RTX keeps filter definitions ({@code ip filter}) separate from their application, and the
   * same filter number may appear in several lists. Batfish needs a single ordered list per
   * application point, so the referenced definitions are inlined here.
   */
  static void convertFilters(RtxConfiguration vc, Configuration c) {
    for (RtxInterface iface : vc.getInterfaces().values()) {
      if (!iface.getInboundFilters().isEmpty()) {
        String name = computeInboundFilterName(iface.getName());
        c.getIpAccessLists().put(name, toIpAccessList(name, iface.getInboundFilters(), vc));
      }
      if (!iface.getOutboundFilters().isEmpty()) {
        String name = computeOutboundFilterName(iface.getName());
        c.getIpAccessLists().put(name, toIpAccessList(name, iface.getOutboundFilters(), vc));
      }
    }
  }

  private static IpAccessList toIpAccessList(
      String name, List<Long> filterNumbers, RtxConfiguration vc) {
    ImmutableList.Builder<AclLine> lines = ImmutableList.builder();
    for (long number : filterNumbers) {
      RtxFilter filter = vc.getFilters().get(number);
      if (filter == null) {
        // A reference to a filter that was never defined, or whose definition could not be
        // interpreted. The device ignores such a filter, so the list simply does not contain it.
        // The dangling reference is reported through structure tracking.
        continue;
      }
      lines.add(toAclLine(filter));
    }
    // "Packets matching none of the filters are discarded" -- this trailing deny is part of the
    // semantics of `ip <if> secure filter`, not something the operator wrote.
    lines.add(
        ExprAclLine.rejecting()
            .setMatchCondition(TRUE)
            .setName("implicit deny at end of secure filter list")
            .build());
    return IpAccessList.builder().setName(name).setLines(lines.build()).build();
  }

  private static ExprAclLine toAclLine(RtxFilter filter) {
    ExprAclLine.Builder line =
        filter.getAction() == org.batfish.datamodel.LineAction.PERMIT
            ? ExprAclLine.accepting()
            : ExprAclLine.rejecting();
    return line.setMatchCondition(toMatchExpr(filter))
        .setName(String.format("ip filter %d", filter.getNumber()))
        .build();
  }

  private static AclLineMatchExpr toMatchExpr(RtxFilter filter) {
    List<AclLineMatchExpr> conjuncts = new ArrayList<>();
    if (filter.getSrcIps() != null) {
      conjuncts.add(matchSrc(filter.getSrcIps()));
    }
    if (filter.getDstIps() != null) {
      conjuncts.add(matchDst(filter.getDstIps()));
    }
    if (!filter.getProtocols().isEmpty()) {
      conjuncts.add(
          or(
              filter.getProtocols().stream()
                  .map(protocol -> matchIpProtocol(protocol))
                  .collect(ImmutableList.toImmutableList())));
    }
    if (filter.getEstablished()) {
      conjuncts.add(matchTcpFlags(TcpFlagsMatchConditions.ACK_TCP_FLAG));
    }
    if (filter.getSrcPorts() != null) {
      conjuncts.add(matchSrcPort(filter.getSrcPorts()));
    }
    if (filter.getDstPorts() != null) {
      conjuncts.add(matchDstPort(filter.getDstPorts()));
    }
    if (filter.getIcmpTypes() != null) {
      conjuncts.add(
          org.batfish.datamodel.acl.AclLineMatchExprs.match(
              HeaderSpace.builder().setIcmpTypes(filter.getIcmpTypes().getSubRanges()).build()));
    }
    if (filter.getIcmpCodes() != null) {
      conjuncts.add(
          org.batfish.datamodel.acl.AclLineMatchExprs.match(
              HeaderSpace.builder().setIcmpCodes(filter.getIcmpCodes().getSubRanges()).build()));
    }
    return conjuncts.isEmpty() ? TRUE : and(conjuncts);
  }

  /** Creates a vendor-independent interface for each RTX interface. */
  static void convertInterfaces(RtxConfiguration vc, Configuration c) {
    for (RtxInterface iface : vc.getInterfaces().values()) {
      convertInterface(iface, c);
    }
  }

  private static void convertInterface(RtxInterface iface, Configuration c) {
    Interface.Builder viIface =
        Interface.builder()
            .setOwner(c)
            .setName(iface.getName())
            .setVrf(c.getDefaultVrf())
            .setType(toInterfaceType(iface.getType()))
            // RTX has no per-interface administrative shutdown in the configurations we model.
            .setAdminUp(true);

    List<ConcreteInterfaceAddress> addresses = iface.getAddressesSnapshot();
    if (!addresses.isEmpty()) {
      viIface.setAddresses(
          addresses.get(0),
          ImmutableList.<InterfaceAddress>copyOf(addresses.subList(1, addresses.size())));
    }

    if (iface.getVlanId() != null) {
      viIface.setEncapsulationVlan(iface.getVlanId());
    }
    if (iface.getParentInterface() != null) {
      viIface.setDependencies(
          ImmutableSet.of(
              new Interface.Dependency(iface.getParentInterface(), Interface.DependencyType.BIND)));
    }

    viIface.setIncomingFilter(c.getIpAccessLists().get(computeInboundFilterName(iface.getName())));
    viIface.setOutgoingFilter(c.getIpAccessLists().get(computeOutboundFilterName(iface.getName())));
    viIface.setVrrpGroups(toVrrpGroups(iface));

    viIface.build();
  }

  private static InterfaceType toInterfaceType(RtxInterface.Type type) {
    return switch (type) {
      case LAN -> InterfaceType.PHYSICAL;
      case LAN_SUBINTERFACE -> InterfaceType.LOGICAL;
      case TUNNEL -> InterfaceType.TUNNEL;
    };
  }

  /**
   * Converts the VRRP groups on {@code iface}.
   *
   * <p>The J57 routers point every Juniper switch's default route at a VRRP virtual address, so
   * without this the virtual addresses would be owned by nobody and those routes would not resolve.
   */
  private static SortedMap<Integer, VrrpGroup> toVrrpGroups(RtxInterface iface) {
    if (iface.getVrrpGroups().isEmpty()) {
      return ImmutableSortedMap.of();
    }
    // The source address is the interface's own address, which is what the router sends
    // advertisements from.
    ConcreteInterfaceAddress sourceAddress =
        iface.getAddresses().isEmpty() ? null : iface.getAddresses().get(0);
    SortedMap<Integer, VrrpGroup> groups = new TreeMap<>();
    iface
        .getVrrpGroups()
        .forEach(
            (vrid, vsGroup) ->
                groups.put(
                    vrid,
                    VrrpGroup.builder()
                        .setPriority(vsGroup.getPriority())
                        .setPreempt(vsGroup.getPreempt())
                        .setSourceAddress(sourceAddress)
                        .addVirtualAddress(iface.getName(), vsGroup.getVirtualAddress())
                        .build()));
    return ImmutableSortedMap.copyOf(groups);
  }

  /** Adds the static routes from {@code ip route} to the default VRF. */
  static void convertStaticRoutes(RtxConfiguration vc, Configuration c) {
    c.getDefaultVrf()
        .setStaticRoutes(
            vc.getStaticRoutes().stream()
                .map(RtxConversions::toStaticRoute)
                .collect(
                    com.google.common.collect.ImmutableSortedSet.toImmutableSortedSet(
                        Comparator.naturalOrder())));
  }

  private static org.batfish.datamodel.StaticRoute toStaticRoute(RtxStaticRoute route) {
    org.batfish.datamodel.StaticRoute.Builder builder =
        org.batfish.datamodel.StaticRoute.builder()
            .setNetwork(route.getNetwork())
            .setAdministrativeCost(STATIC_ROUTE_ADMIN_COST)
            .setMetric(0L);
    if (route.getNextHopIp() != null) {
      builder.setNextHop(NextHopIp.of(route.getNextHopIp()));
    } else {
      // `gateway tunnel <n>`. The J57 tunnels terminate on IPv6 endpoints, which Batfish does not
      // model, so this route will not resolve to a next hop.
      builder.setNextHop(NextHopInterface.of(route.getNextHopInterface()));
    }
    return builder.build();
  }

  /**
   * IP protocols that masquerade can translate. Masquerade needs a port (or an ICMP identifier) to
   * demultiplex return traffic, so it does not apply to other protocols.
   */
  private static final List<IpProtocol> MASQUERADE_PROTOCOLS =
      ImmutableList.of(IpProtocol.TCP, IpProtocol.UDP, IpProtocol.ICMP);

  /**
   * Applies the NAT descriptors that {@code ip <if> nat descriptor} put on each interface.
   *
   * <p>A descriptor is applied to the interface facing its outer addresses, so it translates the
   * source of traffic leaving that interface. Return traffic is handled by a session rather than by
   * a second transformation: Batfish reverses the transformation for flows matching a session,
   * which is how a real NAT table behaves.
   */
  static void convertNat(RtxConfiguration vc, Configuration c) {
    for (RtxInterface iface : vc.getInterfaces().values()) {
      if (iface.getNatDescriptors().isEmpty()) {
        continue;
      }
      Interface viIface = c.getAllInterfaces().get(iface.getName());
      if (viIface == null) {
        continue;
      }
      Transformation transformation = toOutgoingTransformation(iface, vc);
      if (transformation == null) {
        continue;
      }
      viIface.setOutgoingTransformation(transformation);
      viIface.setFirewallSessionInterfaceInfo(
          new FirewallSessionInterfaceInfo(
              FirewallSessionInterfaceInfo.Action.FORWARD_OUT_IFACE,
              ImmutableList.of(iface.getName()),
              nameIfPresent(c, computeInboundFilterName(iface.getName())),
              nameIfPresent(c, computeOutboundFilterName(iface.getName()))));
    }
  }

  private static @Nullable String nameIfPresent(Configuration c, String aclName) {
    return c.getIpAccessLists().containsKey(aclName) ? aclName : null;
  }

  /**
   * Chains the interface's descriptors so that the first one whose inner addresses match wins,
   * which is how overlapping NAT rules behave. Returns {@code null} if none of them translates
   * anything.
   */
  private static @Nullable Transformation toOutgoingTransformation(
      RtxInterface iface, RtxConfiguration vc) {
    Transformation chain = null;
    // Build from the end of the list backwards so that earlier descriptors are tried first.
    for (int i = iface.getNatDescriptors().size() - 1; i >= 0; i--) {
      RtxNatDescriptor descriptor = vc.getNatDescriptors().get(iface.getNatDescriptors().get(i));
      if (descriptor == null) {
        // A reference to a descriptor that was never defined; reported through structure tracking.
        continue;
      }
      List<TransformationStep> steps = toTransformationSteps(descriptor, iface);
      if (steps.isEmpty()) {
        continue;
      }
      chain = Transformation.when(toNatGuard(descriptor)).apply(steps).setOrElse(chain).build();
    }
    return chain;
  }

  /** The condition under which {@code descriptor} translates a flow leaving the interface. */
  private static @Nonnull AclLineMatchExpr toNatGuard(RtxNatDescriptor descriptor) {
    List<AclLineMatchExpr> conjuncts = new ArrayList<>();
    if (descriptor.getInnerAddresses() != null) {
      // `nat descriptor address inner` restricts which sources are translated; `auto` (the
      // default) leaves it unrestricted.
      conjuncts.add(matchSrc(descriptor.getInnerAddresses()));
    }
    if (translatesPorts(descriptor)) {
      conjuncts.add(
          org.batfish.datamodel.acl.AclLineMatchExprs.match(
              HeaderSpace.builder().setIpProtocols(MASQUERADE_PROTOCOLS).build()));
    }
    return conjuncts.isEmpty() ? TRUE : and(conjuncts);
  }

  private static boolean translatesPorts(RtxNatDescriptor descriptor) {
    return descriptor.getType() == RtxNatDescriptor.Type.MASQUERADE
        || descriptor.getType() == RtxNatDescriptor.Type.NAT_MASQUERADE;
  }

  /** The steps {@code descriptor} applies, or an empty list if it translates nothing. */
  private static @Nonnull List<TransformationStep> toTransformationSteps(
      RtxNatDescriptor descriptor, RtxInterface iface) {
    RangeSet<Ip> pool = toOuterAddressPool(descriptor, iface);
    if (pool == null || pool.isEmpty()) {
      return ImmutableList.of();
    }
    ImmutableList.Builder<TransformationStep> steps = ImmutableList.builder();
    switch (descriptor.getType()) {
      case NONE:
        return ImmutableList.of();
      case MASQUERADE:
        // "For IP masquerade, the first of the outer IP addresses is used." The pool computed
        // above is already narrowed to that address for this type.
        steps.add(new AssignIpAddressFromPool(SOURCE_NAT, IpField.SOURCE, pool));
        steps.add(toPortStep(descriptor));
        break;
      case NAT:
        steps.add(new AssignIpAddressFromPool(SOURCE_NAT, IpField.SOURCE, pool));
        break;
      case NAT_MASQUERADE:
        // Flows are assigned an address from the pool one-to-one until it runs out, then
        // masqueraded. Modeled as the union of the two behaviors.
        steps.add(new AssignIpAddressFromPool(SOURCE_NAT, IpField.SOURCE, pool));
        steps.add(toPortStep(descriptor));
        break;
    }
    return steps.build();
  }

  /**
   * The outer addresses {@code descriptor} may assign, or {@code null} if they cannot be determined
   * from the configuration.
   */
  private static @Nullable RangeSet<Ip> toOuterAddressPool(
      RtxNatDescriptor descriptor, RtxInterface iface) {
    RtxNatDescriptor.OuterAddressMnemonic mnemonic = descriptor.getOuterMnemonic();
    if (mnemonic != null) {
      // `primary` and `secondary` name addresses the configuration does contain; the others are
      // learned at runtime.
      List<ConcreteInterfaceAddress> addresses = iface.getAddresses();
      Integer index =
          switch (mnemonic) {
            case PRIMARY -> 0;
            case SECONDARY -> 1;
            case IPCP, MAP_E, HB46PP -> null;
          };
      if (index == null || addresses.size() <= index) {
        return null;
      }
      return TreeRangeSet.create(ImmutableList.of(Range.singleton(addresses.get(index).getIp())));
    }
    if (descriptor.getType() == RtxNatDescriptor.Type.MASQUERADE) {
      Ip first = descriptor.getFirstOuterAddress();
      return first == null ? null : TreeRangeSet.create(ImmutableList.of(Range.singleton(first)));
    }
    return descriptor.getOuterAddresses().isEmpty() ? null : descriptor.getOuterAddresses();
  }

  private static @Nonnull TransformationStep toPortStep(RtxNatDescriptor descriptor) {
    List<SubRange> ranges = ImmutableList.copyOf(descriptor.getMasqueradePorts().getSubRanges());
    if (ranges.size() == 1) {
      return toPortStep(ranges.get(0));
    }
    // Ports are taken from the first configured range until it is exhausted, then the next, so any
    // of them may end up on a given flow.
    return new ApplyAny(
        ranges.stream().map(RtxConversions::toPortStep).collect(ImmutableList.toImmutableList()));
  }

  private static @Nonnull AssignPortFromPool toPortStep(SubRange range) {
    return new AssignPortFromPool(SOURCE_NAT, PortField.SOURCE, range.getStart(), range.getEnd());
  }

  /** Converts the {@code bgp} commands and route filters to a {@link BgpProcess}. */
  static void convertBgp(RtxConfiguration vc, Configuration c) {
    RtxBgpProcess vsProc = vc.getBgpProcess();
    if (vsProc == null || !vsProc.getEnabled() || vsProc.getLocalAs() == null) {
      return;
    }
    long localAs = vsProc.getLocalAs();
    Ip routerId = inferRouterId(c);
    BgpProcess viProc =
        BgpProcess.builder()
            .setRouterId(routerId)
            .setEbgpAdminCost(EBGP_ADMIN_COST)
            .setIbgpAdminCost(IBGP_ADMIN_COST)
            .setLocalAdminCost(LOCAL_ADMIN_COST)
            // RTX has no equivalent knobs; these only matter when the same prefix is originated
            // locally in more than one way, which the converted model never does.
            .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
            .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
            .build();
    c.getDefaultVrf().setBgpProcess(viProc);

    for (RtxBgpNeighbor neighbor : vsProc.getNeighbors().values()) {
      Ip peerAddress = neighbor.getRemoteAddress();
      String importPolicy = computeBgpImportPolicyName(peerAddress);
      String exportPolicy = computeBgpExportPolicyName(peerAddress);
      buildBgpImportPolicy(vsProc, c, neighbor.getRemoteAs(), importPolicy);
      buildBgpExportPolicy(vsProc, c, localAs, neighbor.getRemoteAs(), exportPolicy);
      viProc
          .getActiveNeighbors()
          .put(
              peerAddress,
              BgpActivePeerConfig.builder()
                  .setPeerAddress(peerAddress)
                  .setRemoteAs(neighbor.getRemoteAs())
                  .setLocalAs(localAs)
                  .setLocalIp(inferLocalIp(c, peerAddress))
                  .setIpv4UnicastAddressFamily(
                      Ipv4UnicastAddressFamily.builder()
                          .setImportPolicy(importPolicy)
                          .setExportPolicy(exportPolicy)
                          .build())
                  .build());
    }
  }

  /**
   * RTX's {@code bgp export} commands filter received routes before they enter the local routing
   * table, so they become Batfish import policies.
   */
  private static void buildBgpImportPolicy(
      RtxBgpProcess proc, Configuration c, long remoteAs, String policyName) {
    RoutingPolicy.Builder policy = RoutingPolicy.builder().setOwner(c).setName(policyName);
    proc.getExportRules().stream()
        .filter(rule -> rule.getRemoteAs() == remoteAs)
        .findFirst()
        .ifPresent(
            rule ->
                toBgpFilterStatements(rule.getFilters(), proc.getExportFilters())
                    .forEach(policy::addStatement));
    policy.addStatement(Statements.ExitReject.toStaticStatement()).build();
  }

  /**
   * RTX's {@code bgp import} commands select routes from the local table for advertisement, so they
   * become Batfish export policies.
   */
  private static void buildBgpExportPolicy(
      RtxBgpProcess proc, Configuration c, long localAs, long remoteAs, String policyName) {
    RoutingPolicy.Builder policy = RoutingPolicy.builder().setOwner(c).setName(policyName);
    proc.getImportRules().stream()
        .filter(rule -> rule.getRemoteAs() == remoteAs)
        .forEach(
            rule ->
                policy.addStatement(
                    new If(
                        toBgpImportProtocolGuard(rule, localAs),
                        toBgpFilterStatements(rule.getFilters(), proc.getImportFilters()))));
    policy.addStatement(Statements.ExitReject.toStaticStatement()).build();
  }

  private static @Nonnull BooleanExpr toBgpImportProtocolGuard(
      RtxBgpImportRule rule, long localAs) {
    if (rule.getProtocol() == RtxBgpImportRule.Protocol.STATIC) {
      return new MatchProtocol(RoutingProtocol.STATIC);
    }
    assert rule.getFromAs() != null;
    long fromAs = rule.getFromAs();
    if (fromAs == localAs) {
      return new MatchProtocol(RoutingProtocol.IBGP);
    }
    return new Conjunction(
        ImmutableList.of(
            new MatchProtocol(RoutingProtocol.BGP),
            MatchAsPath.of(
                InputAsPath.instance(), AsPathMatchRegex.of(String.format("^%d( |$)", fromAs)))));
  }

  /**
   * Returns an ordered first-match filter chain. RTX rejects a route if none of the referenced
   * filters matches.
   */
  private static @Nonnull List<Statement> toBgpFilterStatements(
      List<Integer> filterNumbers, java.util.Map<Integer, RtxBgpRouteFilter> filters) {
    ImmutableList.Builder<Statement> statements = ImmutableList.builder();
    for (int number : filterNumbers) {
      RtxBgpRouteFilter filter = filters.get(number);
      if (filter == null) {
        continue;
      }
      PrefixSpace prefixSpace = new PrefixSpace();
      for (Prefix prefix : filter.getPrefixes()) {
        if (filter.getMatchType() == RtxBgpRouteFilter.MatchType.REFINES
            && prefix.getPrefixLength() == Prefix.MAX_PREFIX_LENGTH) {
          // A host prefix has no more-specific IPv4 routes, so this item matches nothing.
          continue;
        }
        PrefixRange range =
            switch (filter.getMatchType()) {
              case INCLUDE -> PrefixRange.sameAsOrMoreSpecificThan(prefix);
              case REFINES -> PrefixRange.moreSpecificThan(prefix);
              case EQUAL -> PrefixRange.fromPrefix(prefix);
            };
        prefixSpace.addPrefixRange(range);
      }
      statements.add(
          new If(
              new MatchPrefixSet(DestinationNetwork.instance(), new ExplicitPrefixSet(prefixSpace)),
              ImmutableList.of(
                  filter.getAction() == org.batfish.datamodel.LineAction.PERMIT
                      ? Statements.ExitAccept.toStaticStatement()
                      : Statements.ExitReject.toStaticStatement())));
    }
    statements.add(Statements.ExitReject.toStaticStatement());
    return statements.build();
  }

  /**
   * RTX has no {@code router-id} command, so use the highest interface address, which is the
   * convention Batfish applies elsewhere when a device leaves the router ID implicit.
   */
  private static Ip inferRouterId(Configuration c) {
    return c.getAllInterfaces().values().stream()
        .flatMap(iface -> iface.getAllConcreteAddresses().stream())
        .map(ConcreteInterfaceAddress::getIp)
        .max(Comparator.naturalOrder())
        .orElse(Ip.ZERO);
  }

  /** The address of the interface on the same subnet as {@code peerAddress}, if there is one. */
  private static @Nullable Ip inferLocalIp(Configuration c, Ip peerAddress) {
    Optional<ConcreteInterfaceAddress> match =
        c.getAllInterfaces().values().stream()
            .flatMap(iface -> iface.getAllConcreteAddresses().stream())
            .filter(address -> address.getPrefix().containsIp(peerAddress))
            .findFirst();
    return match.map(ConcreteInterfaceAddress::getIp).orElse(null);
  }

  /** The prefix of an {@code ip route default} command. */
  public static @Nonnull Prefix defaultNetwork() {
    return Prefix.ZERO;
  }

  private RtxConversions() {}
}
