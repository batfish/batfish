package org.batfish.representation.fortios;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.ExprAclLine.accepting;
import static org.batfish.datamodel.ExprAclLine.rejecting;
import static org.batfish.datamodel.acl.AclLineMatchExprs.ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.deniedByAcl;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.datamodel.acl.AclLineMatchExprs.permittedByAcl;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.matchAddressTraceElement;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.matchAddrgrpTraceElement;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.matchPolicyTraceElement;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.matchServiceGroupTraceElement;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.matchServiceTraceElement;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.zoneToZoneDefaultTraceElement;
import static org.batfish.representation.fortios.InterfaceOrZoneUtils.getDefaultIntrazoneAction;
import static org.batfish.representation.fortios.InterfaceOrZoneUtils.getIncludedInterfaces;
import static org.batfish.representation.fortios.InterfaceOrZoneUtils.policyMatchesFrom;
import static org.batfish.representation.fortios.InterfaceOrZoneUtils.policyMatchesTo;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.representation.fortios.Addrgrp.Type;

/** Helper functions for generating VI ACLs for {@link FortiosConfiguration}. */
public final class FortiosPolicyConversions {
  /**
   * Generates an {@link IpAccessList} for each zone or interface in {@code
   * zonesAndUnzonedInterfaces} to be applied to traffic leaving that zone or interface.
   */
  static void generateOutgoingFilters(
      List<InterfaceOrZone> zonesAndUnzonedInterfaces, Configuration c) {
    for (InterfaceOrZone interfaceOrZone : zonesAndUnzonedInterfaces) {
      IpAccessList outgoingFilter =
          generateOutgoingFilter(interfaceOrZone, zonesAndUnzonedInterfaces);
      c.getIpAccessLists().put(outgoingFilter.getName(), outgoingFilter);
    }
  }

  /**
   * Generates {@link IpAccessList} to be used as outgoing filter by interfaces in {@code to}. This
   * filter will incorporate the cross-zone policies from each of the provided {@code
   * zonesAndUnzonedInterfaces}.
   */
  private static @Nonnull IpAccessList generateOutgoingFilter(
      InterfaceOrZone to, List<InterfaceOrZone> zonesAndUnzonedInterfaces) {
    ImmutableList.Builder<AclLine> lines = ImmutableList.builder();

    // TODO Should originated traffic always be rejected? Is it subject to intrazone policies or
    //  default intrazone action?
    lines.add(ExprAclLine.rejecting().setMatchCondition(ORIGINATING_FROM_DEVICE).build());

    // Add lines for each possible source:
    // 1. If from that source and matching the associated cross-zone policy, permit
    // 2. If from that source, deny
    zonesAndUnzonedInterfaces.stream()
        .flatMap(from -> generateCrossZoneCalls(from, to))
        .forEach(lines::add);

    return IpAccessList.builder()
        .setName(computeOutgoingFilterName(to))
        .setLines(lines.build())
        .build();
  }

  /**
   * Generates outgoing filter lines that will match traffic that entered {@code from} and was
   * routed out {@code to}, and permit or deny it according to the corresponding cross-zone filter.
   */
  private static @Nonnull Stream<ExprAclLine> generateCrossZoneCalls(
      InterfaceOrZone from, InterfaceOrZone to) {
    MatchSrcInterface matchSrcInterfaces = matchSrcInterface(getIncludedInterfaces(from));
    String crossZoneFilterName = Names.zoneToZoneFilter(from.getName(), to.getName());
    return Stream.of(
        accepting(and(matchSrcInterfaces, permittedByAcl(crossZoneFilterName))),
        // For traffic from the source interfaces, the deniedByAcl expr is guaranteed to match if it
        // reaches this line, since we know it wasn't permitted by the ACL in the last line.
        // In other words, rejecting(matchSrcInterfaces) would be logically identical here.
        // However, having an explicit deniedByAcl expr results in better ACL traces.
        rejecting(and(matchSrcInterfaces, deniedByAcl(crossZoneFilterName))));
  }

  /**
   * Generates filters ({@link IpAccessList}) for traffic from each member of {@code
   * zonesAndUnzonedInterfaces} to every other member of {@code zonesAndUnzonedInterfaces},
   * including both cross-zone and intrazone filters. All filters are named using {@link
   * Names#zoneToZoneFilter}.
   *
   * @param convertedPolicies Map of policy number to {@link AclLine} representing that policy
   */
  static void generateCrossZoneFilters(
      List<InterfaceOrZone> zonesAndUnzonedInterfaces,
      Map<String, AclLine> convertedPolicies,
      FortiosConfiguration vc,
      Configuration c) {
    // Generate cross-zone filters for each interface-interface, interface-zone, zone-interface, and
    // zone-zone pair
    for (InterfaceOrZone from : zonesAndUnzonedInterfaces) {
      for (InterfaceOrZone to : zonesAndUnzonedInterfaces) {
        IpAccessList acl =
            generateCrossZoneFilter(
                from, to, vc.getPolicies(), convertedPolicies, vc.getFilename());
        c.getIpAccessLists().put(acl.getName(), acl);
      }
    }
  }

  /**
   * Generates {@link IpAccessList} to apply to traffic from {@code fromZoneOrIface} to {@code
   * toZoneOrIface}.
   *
   * @param policies Map of policy number to {@link Policy} from {@link FortiosConfiguration}
   * @param convertedPolicies Map of policy number to {@link AclLine} representing that policy
   */
  private static IpAccessList generateCrossZoneFilter(
      InterfaceOrZone fromZoneOrIface,
      InterfaceOrZone toZoneOrIface,
      Map<String, Policy> policies,
      Map<String, AclLine> convertedPolicies,
      String filename) {
    checkArgument(
        fromZoneOrIface instanceof Interface || fromZoneOrIface instanceof Zone,
        String.format(
            "Cannot generate cross-zone filter for source type %s",
            fromZoneOrIface.getClass().getTypeName()));
    checkArgument(
        toZoneOrIface instanceof Interface || toZoneOrIface instanceof Zone,
        String.format(
            "Cannot generate cross-zone filter for destination type %s",
            toZoneOrIface.getClass().getTypeName()));

    String crossZoneFilterName =
        Names.zoneToZoneFilter(fromZoneOrIface.getName(), toZoneOrIface.getName());

    ImmutableList.Builder<AclLine> lines = ImmutableList.builder();
    policies.values().stream()
        .filter(p -> policyMatchesFrom(p, fromZoneOrIface) && policyMatchesTo(p, toZoneOrIface))
        .map(Policy::getNumber)
        .map(convertedPolicies::get)
        .filter(Objects::nonNull)
        .forEach(lines::add);

    // Add a line to apply the default action. All cross-zone policies default-deny; intrazone
    // policies can allow by default if configured to do so.
    ExprAclLine.Builder defaultLine =
        fromZoneOrIface == toZoneOrIface
                && getDefaultIntrazoneAction(fromZoneOrIface) == Zone.IntrazoneAction.ALLOW
            ? ExprAclLine.accepting()
            : ExprAclLine.rejecting();
    defaultLine
        .setMatchCondition(TRUE)
        .setTraceElement(zoneToZoneDefaultTraceElement(fromZoneOrIface, toZoneOrIface, filename));
    lines.add(defaultLine.build());

    return IpAccessList.builder().setName(crossZoneFilterName).setLines(lines.build()).build();
  }

  /**
   * Gets the structures that need unique cross-zone policies: all zones and all interfaces not
   * claimed by any zone.
   */
  static List<InterfaceOrZone> getZonesAndUnzonedInterfaces(
      Collection<Zone> zones, Collection<Interface> interfaces) {
    Set<String> ifacesInZones =
        zones.stream()
            .flatMap(z -> z.getInterface().stream())
            .collect(ImmutableSet.toImmutableSet());
    List<Interface> unzonedIfaces =
        interfaces.stream()
            .filter(iface -> !ifacesInZones.contains(iface.getName()))
            .collect(ImmutableList.toImmutableList());
    return Streams.concat(zones.stream(), unzonedIfaces.stream())
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Computes name for {@link IpAccessList} to act as the outgoing filter for all interfaces
   * included in the given {@code zoneOrIface}.
   */
  static String computeOutgoingFilterName(InterfaceOrZone zoneOrIface) {
    return computeOutgoingFilterName(
        zoneOrIface instanceof Interface ? "interface" : "zone", zoneOrIface.getName());
  }

  @VisibleForTesting
  public static String computeOutgoingFilterName(String type, String name) {
    return String.format("%s~%s~OUTGOING_FILTER", type, name);
  }

  /**
   * Converts each {@link Policy} in {@code policies} to an {@link AclLine} if the policy can and
   * should be converted.
   *
   * @param convertedServices Map of {@link Service} names to {@link AclLineMatchExpr} representing
   *     traffic that matches the service.
   * @param namedIpSpaces VI names of successfully converted {@link Address} objects
   */
  static Map<String, AclLine> convertPolicies(
      Map<String, Policy> policies,
      Map<String, AclLineMatchExpr> convertedServices,
      Map<String, AddrgrpMember> addrgrpMembers,
      Set<String> namedIpSpaces,
      String filename,
      Warnings w) {
    return policies.values().stream()
        .map(
            p ->
                convertPolicy(p, convertedServices, addrgrpMembers, namedIpSpaces, filename, w)
                    .map(line -> Maps.immutableEntry(p.getNumber(), line)))
        .filter(Optional::isPresent)
        .collect(ImmutableMap.toImmutableMap(e -> e.get().getKey(), e -> e.get().getValue()));
  }

  /**
   * Converts the given {@code policy} to an {@link AclLine}, or an empty optional if the policy
   * can't or shouldn't be converted.
   *
   * @param namedIpSpaces VI names of successfully converted {@link Address} objects
   * @param convertedServices Map of {@link Service} names to {@link AclLineMatchExpr} representing
   *     traffic that matches the service.
   */
  public static Optional<AclLine> convertPolicy(
      Policy policy,
      Map<String, AclLineMatchExpr> convertedServices,
      Map<String, AddrgrpMember> addrgrpMembers,
      Set<String> namedIpSpaces,
      String filename,
      Warnings w) {
    if (policy.getStatusEffective() != Policy.Status.ENABLE) {
      return Optional.empty();
    }
    String numAndName = getPolicyName(policy);

    ExprAclLine.Builder line;
    switch (policy.getActionEffective()) {
      case ACCEPT:
        line = ExprAclLine.accepting();
        break;
      case DENY:
        line = ExprAclLine.rejecting();
        break;
      default: // TODO: Support policies with action IPSEC
        w.redFlag(
            String.format(
                "Ignoring policy %s: Action %s is not supported",
                numAndName, policy.getActionEffective()));
        return Optional.empty();
    }

    // TODO Incorporate policy.getComments()
    Set<String> srcAddrs = policy.getSrcAddr();
    Set<String> dstAddrs = policy.getDstAddr();
    Set<String> services = policy.getService();

    // Make sure references were finalized
    assert srcAddrs != null
        && dstAddrs != null
        && services != null
        && policy.getSrcIntfZones() != null
        && policy.getDstIntfZones() != null;

    // Note that src/dst interface filtering will be done in generated export policies.
    ImmutableList.Builder<AclLineMatchExpr> matchConjuncts = ImmutableList.builder();

    // Match src addresses, dst addresses, and services
    //    List<AclLineMatchExpr> srcAddrExprs =
    //        Sets.intersection(srcAddrs, namedIpSpaces).stream()
    //            .map(
    //                addr -> {
    //                  HeaderSpace hs =
    //                      HeaderSpace.builder().setSrcIps(new IpSpaceReference(addr)).build();
    //                  return new MatchHeaderSpace(hs, matchSourceAddressTraceElement(addr,
    // filename));
    //                })
    //            .collect(ImmutableList.toImmutableList());
    List<AclLineMatchExpr> srcAddrExprs =
        Sets.intersection(srcAddrs, namedIpSpaces).stream()
            .map(addr -> toMatchExpr(addrgrpMembers.get(addr), addrgrpMembers, filename, true))
            .collect(ImmutableList.toImmutableList());

    //    List<AclLineMatchExpr> dstAddrExprs =
    //        Sets.intersection(dstAddrs, namedIpSpaces).stream()
    //            .map(
    //                addr -> {
    //                  HeaderSpace hs =
    //                      HeaderSpace.builder().setDstIps(new IpSpaceReference(addr)).build();
    //                  return new MatchHeaderSpace(
    //                      hs, matchDestinationAddressTraceElement(addr, filename));
    //                })
    //            .collect(ImmutableList.toImmutableList());
    List<AclLineMatchExpr> dstAddrExprs =
        Sets.intersection(dstAddrs, namedIpSpaces).stream()
            .map(addr -> toMatchExpr(addrgrpMembers.get(addr), addrgrpMembers, filename, false))
            .collect(ImmutableList.toImmutableList());

    List<AclLineMatchExpr> svcExprs =
        Sets.intersection(services, convertedServices.keySet()).stream()
            .map(convertedServices::get)
            .collect(ImmutableList.toImmutableList());

    SetView<String> convertedSrcAddrs = Sets.intersection(srcAddrs, namedIpSpaces);
    SetView<String> convertedDstAddrs = Sets.intersection(dstAddrs, namedIpSpaces);
    if (convertedSrcAddrs.isEmpty() || convertedDstAddrs.isEmpty() || services.isEmpty()) {
      String emptyField =
          convertedSrcAddrs.isEmpty()
              ? "source addresses"
              : convertedDstAddrs.isEmpty() ? "destination addresses" : "services";
      w.redFlag(
          String.format(
              "Policy %s will not match any packets because none of its %s were successfully"
                  + " converted",
              numAndName, emptyField));
    }
    matchConjuncts.add(or(srcAddrExprs));
    matchConjuncts.add(or(dstAddrExprs));
    matchConjuncts.add(or(svcExprs)); // TODO confirm services should be disjoined

    line.setMatchCondition(and(matchConjuncts.build()));
    line.setTraceElement(matchPolicyTraceElement(policy, filename));
    line.setName(numAndName);
    return Optional.of(line.build());
  }

  /**
   * Convert specified {@link AddrgrpMember} into its corresponding {@link AclLineMatchExpr}.
   * Converts to an expression matching source addresses if {@code sourceAddr} is true, otherwise an
   * expression matching destination addresses.
   */
  @VisibleForTesting
  @Nonnull
  public static AclLineMatchExpr toMatchExpr(
      AddrgrpMember member,
      Map<String, AddrgrpMember> addrgrpMembers,
      String filename,
      boolean soureAddr) {
    if (member instanceof Address) {
      return toMatchExpr((Address) member, filename, soureAddr);
    } else {
      assert member instanceof Addrgrp;
      return toMatchExpr((Addrgrp) member, addrgrpMembers, filename, soureAddr);
    }
  }

  @Nonnull
  private static AclLineMatchExpr toMatchExpr(
      Address address, String filename, boolean sourceAddr) {
    // TODO figure out src vs dst
    HeaderSpace.Builder hs = HeaderSpace.builder();
    IpSpaceReference ref = new IpSpaceReference(address.getName());

    if (sourceAddr) {
      hs.setSrcIps(ref);
    } else {
      hs.setDstIps(ref);
    }
    return new MatchHeaderSpace(
        hs.build(), matchAddressTraceElement(address.getName(), filename, sourceAddr));
  }

  @Nonnull
  private static AclLineMatchExpr toMatchExpr(
      Addrgrp addrgrp,
      Map<String, AddrgrpMember> addrgrpMembers,
      String filename,
      boolean sourceAddr) {
    // Guaranteed once extraction is complete
    assert addrgrp.getMember() != null;

    // Folder types are not supported yet, warning is handled elsewhere
    if (addrgrp.getTypeEffective() == Type.FOLDER) {
      return FalseExpr.INSTANCE;
    }

    ImmutableList<AclLineMatchExpr> exprs =
        addrgrp.getMember().stream()
            .map(m -> toMatchExpr(addrgrpMembers.get(m), addrgrpMembers, filename, sourceAddr))
            .collect(ImmutableList.toImmutableList());
    // Any valid group should match *some* members
    assert !exprs.isEmpty();

    return new OrMatchExpr(
        exprs, matchAddrgrpTraceElement(addrgrp.getName(), filename, sourceAddr));
  }

  /**
   * Convert specified {@link ServiceGroupMember} into its corresponding {@link AclLineMatchExpr}.
   */
  @VisibleForTesting
  @Nonnull
  public static AclLineMatchExpr toMatchExpr(
      ServiceGroupMember serviceGroupMember,
      Map<String, ServiceGroupMember> serviceGroupMembers,
      String filename) {
    if (serviceGroupMember instanceof Service) {
      return toMatchExpr((Service) serviceGroupMember, filename);
    } else {
      assert serviceGroupMember instanceof ServiceGroup;
      return toMatchExpr((ServiceGroup) serviceGroupMember, serviceGroupMembers, filename);
    }
  }

  @Nonnull
  private static AclLineMatchExpr toMatchExpr(
      ServiceGroup serviceGroup,
      Map<String, ServiceGroupMember> serviceGroupMembers,
      String filename) {
    // Guaranteed once extraction is complete
    assert serviceGroup.getMember() != null;

    ImmutableList<AclLineMatchExpr> exprs =
        serviceGroup.getMember().stream()
            .map(m -> toMatchExpr(serviceGroupMembers.get(m), serviceGroupMembers, filename))
            .collect(ImmutableList.toImmutableList());
    // Any valid service group should match *some* service group members
    assert !exprs.isEmpty();

    return new OrMatchExpr(exprs, matchServiceGroupTraceElement(serviceGroup, filename));
  }

  @Nonnull
  private static AclLineMatchExpr toMatchExpr(Service service, String filename) {
    ImmutableList<MatchHeaderSpace> exprs =
        toHeaderSpaces(service).map(MatchHeaderSpace::new).collect(ImmutableList.toImmutableList());
    // Any valid service should match *some* packets
    assert !exprs.isEmpty();

    return new OrMatchExpr(exprs, matchServiceTraceElement(service, filename));
  }

  @Nonnull
  private static Stream<HeaderSpace> toHeaderSpaces(Service service) {
    switch (service.getProtocolEffective()) {
      case TCP_UDP_SCTP:
        return Stream.of(
                buildHeaderSpaceWithPorts(
                    IpProtocol.TCP,
                    service.getTcpPortRangeSrcEffective(),
                    service.getTcpPortRangeDst()),
                buildHeaderSpaceWithPorts(
                    IpProtocol.UDP,
                    service.getUdpPortRangeSrcEffective(),
                    service.getUdpPortRangeDst()),
                buildHeaderSpaceWithPorts(
                    IpProtocol.SCTP,
                    service.getSctpPortRangeSrcEffective(),
                    service.getSctpPortRangeDst()))
            .filter(Objects::nonNull);
      case ICMP:
        return Stream.of(
            buildIcmpHeaderSpace(IpProtocol.ICMP, service.getIcmpCode(), service.getIcmpType()));
      case ICMP6:
        return Stream.of(
            buildIcmpHeaderSpace(
                IpProtocol.IPV6_ICMP, service.getIcmpCode(), service.getIcmpType()));
      case IP:
        // Note that tcp/udp/sctp/icmp fields can't be configured for protocol IP, even if the
        // protocol number specifies one of those protocols
        int protocolNumber = service.getProtocolNumberEffective();
        HeaderSpace.Builder hs = HeaderSpace.builder();
        // Protocol number 0 indicates all protocols.
        // TODO Figure out how one would define a service to specify protocol 0 (HOPOPT)
        return Stream.of(
            protocolNumber == 0
                ? hs.build()
                : hs.setIpProtocols(IpProtocol.fromNumber(protocolNumber)).build());
      default:
        throw new UnsupportedOperationException(
            String.format("Unrecognized service protocol %s", service.getProtocolEffective()));
    }
  }

  /** Returns a {@link HeaderSpace} with the given ports, or null if {@code dstPorts} are null */
  private static @Nullable HeaderSpace buildHeaderSpaceWithPorts(
      @Nonnull IpProtocol protocol,
      @Nullable IntegerSpace srcPorts,
      @Nullable IntegerSpace dstPorts) {
    if (dstPorts == null) {
      return null;
    }
    HeaderSpace.Builder headerSpace =
        HeaderSpace.builder().setIpProtocols(protocol).setDstPorts(dstPorts.getSubRanges());
    Optional.ofNullable(srcPorts).ifPresent(src -> headerSpace.setSrcPorts(src.getSubRanges()));
    return headerSpace.build();
  }

  private static HeaderSpace buildIcmpHeaderSpace(
      IpProtocol icmpProtocol, @Nullable Integer icmpCode, @Nullable Integer icmpType) {
    HeaderSpace.Builder headerSpace = HeaderSpace.builder().setIpProtocols(icmpProtocol);
    Optional.ofNullable(icmpCode).ifPresent(headerSpace::setIcmpCodes);
    Optional.ofNullable(icmpType).ifPresent(headerSpace::setIcmpTypes);
    return headerSpace.build();
  }

  /** Get human-readable name for the specified policy. */
  private static String getPolicyName(Policy policy) {
    return getPolicyName(policy.getNumber(), policy.getName());
  }

  /** Get human-readable name for the specified policy number and name. */
  @VisibleForTesting
  public static String getPolicyName(String number, @Nullable String name) {
    return name == null ? number : String.format("%s named %s", number, name);
  }
}
