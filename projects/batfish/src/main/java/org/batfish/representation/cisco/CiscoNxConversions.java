package org.batfish.representation.cisco;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Collections.singletonList;
import static org.batfish.datamodel.routing_policy.statement.Statements.RemovePrivateAs;
import static org.batfish.representation.cisco.CiscoConfiguration.MATCH_DEFAULT_ROUTE;
import static org.batfish.representation.cisco.CiscoConfiguration.MAX_ADMINISTRATIVE_COST;
import static org.batfish.representation.cisco.CiscoConfiguration.computeBgpCommonExportPolicyName;
import static org.batfish.representation.cisco.CiscoConfiguration.computeBgpPeerExportPolicyName;
import static org.batfish.representation.cisco.CiscoConfiguration.computeNxosBgpDefaultRouteExportPolicyName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.representation.cisco.nx.CiscoNxBgpGlobalConfiguration;
import org.batfish.representation.cisco.nx.CiscoNxBgpVrfAddressFamilyConfiguration;
import org.batfish.representation.cisco.nx.CiscoNxBgpVrfConfiguration;
import org.batfish.representation.cisco.nx.CiscoNxBgpVrfNeighborAddressFamilyConfiguration;
import org.batfish.representation.cisco.nx.CiscoNxBgpVrfNeighborConfiguration;

/**
 * A utility class for converting between Cisco NX-OS configurations and the Batfish
 * vendor-independent {@link org.batfish.datamodel}.
 */
@ParametersAreNonnullByDefault
final class CiscoNxConversions {
  /** Computes the router ID on Cisco NX-OS. */
  // See CiscoNxosTest#testRouterId for a test that is verifiable using GNS3.
  @Nonnull
  static Ip getNxBgpRouterId(CiscoNxBgpVrfConfiguration vrfConfig, Vrf vrf, Warnings w) {
    // If Router ID is configured in the VRF-Specific BGP config, it always wins.
    if (vrfConfig.getRouterId() != null) {
      return vrfConfig.getRouterId();
    }

    String messageBase =
        String.format(
            "Router-id is not manually configured for BGP process in VRF %s", vrf.getName());

    // Otherwise, Router ID is defined based on the interfaces in the VRF that have IP addresses.
    // NX-OS does use shutdown interfaces to configure router-id.
    Map<String, Interface> interfaceMap =
        vrf.getInterfaces().entrySet().stream()
            .filter(e -> e.getValue().getConcreteAddress() != null)
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    if (interfaceMap.isEmpty()) {
      w.redFlag(
          String.format(
              "%s. Unable to infer default router-id as no interfaces have IP addresses",
              messageBase));
      // With no interfaces in the VRF that have IP addresses, show ip bgp vrf all reports 0.0.0.0
      // as the router ID. Of course, this is not really relevant as no routes will be exchanged.
      return Ip.ZERO;
    }

    // Next, NX-OS prefers the IP of Loopback0 if one exists.
    Interface loopback0 = interfaceMap.get("Loopback0");
    if (loopback0 != null) {
      w.redFlag(String.format("%s. Using the IP address of Loopback0", messageBase));
      return loopback0.getConcreteAddress().getIp();
    }

    // Next, NX-OS prefers "first" loopback interface. NX-OS is non-deterministic, but we will
    // enforce determinism by always choosing the smallest loopback IP.
    Collection<Interface> interfaces = interfaceMap.values();
    Optional<Ip> lowestLoopback =
        interfaces.stream()
            .filter(i -> i.getName().startsWith("Loopback"))
            .map(Interface::getConcreteAddress)
            .map(ConcreteInterfaceAddress::getIp)
            .min(Comparator.naturalOrder());
    if (lowestLoopback.isPresent()) {
      w.redFlag(
          String.format(
              "%s. Making a non-deterministic choice from associated loopbacks", messageBase));
      return lowestLoopback.get();
    }

    // Finally, NX uses the first non-loopback interface defined in the vrf, assuming no loopback
    // addresses with IP address are present in the vrf. NX-OS is non-deterministic, by we will
    // enforce determinism by always choosing the smallest interface IP.
    Optional<Ip> lowestIp =
        interfaces.stream()
            .map(Interface::getConcreteAddress)
            .map(ConcreteInterfaceAddress::getIp)
            .min(Comparator.naturalOrder());
    w.redFlag(
        String.format(
            "%s. Making a non-deterministic choice from associated interfaces", messageBase));
    assert lowestIp.isPresent(); // This cannot happen if interfaces is non-empty.
    return lowestIp.get();
  }

  private static boolean isActive(
      String name, CiscoNxBgpVrfNeighborConfiguration neighbor, Warnings w) {
    // Shutdown
    if (firstNonNull(neighbor.getShutdown(), Boolean.FALSE)) {
      return false;
    }

    // No active address family that we support.
    if (neighbor.getIpv4UnicastAddressFamily() == null
        && neighbor.getIpv6UnicastAddressFamily() == null) {
      w.redFlag("No supported address-family configured for " + name);
      return false;
    }

    // No remote AS set.
    if (neighbor.getRemoteAs() == null) {
      w.redFlag("No remote-as configured for " + name);
    }

    return true;
  }

  @Nonnull
  static Map<Prefix, BgpActivePeerConfig> getNeighbors(
      Configuration c,
      Vrf vrf,
      BgpProcess proc,
      CiscoNxBgpGlobalConfiguration bgpConfig,
      CiscoNxBgpVrfConfiguration nxBgpVrf,
      Warnings warnings) {
    return nxBgpVrf.getNeighbors().entrySet().stream()
        .peek(e -> e.getValue().doInherit(bgpConfig, warnings))
        .filter(e -> isActive(getTextDesc(e.getKey(), vrf), e.getValue(), warnings))
        .collect(
            ImmutableMap.toImmutableMap(
                e -> Prefix.create(e.getKey(), Prefix.MAX_PREFIX_LENGTH),
                e ->
                    (BgpActivePeerConfig)
                        CiscoNxConversions.toBgpNeighbor(
                            c,
                            vrf,
                            proc,
                            Prefix.create(e.getKey(), Prefix.MAX_PREFIX_LENGTH),
                            bgpConfig,
                            nxBgpVrf,
                            e.getValue(),
                            false,
                            warnings)));
  }

  @Nonnull
  static Map<Prefix, BgpPassivePeerConfig> getPassiveNeighbors(
      Configuration c,
      Vrf vrf,
      BgpProcess proc,
      CiscoNxBgpGlobalConfiguration bgpConfig,
      CiscoNxBgpVrfConfiguration nxBgpVrf,
      Warnings warnings) {
    return nxBgpVrf.getPassiveNeighbors().entrySet().stream()
        .peek(e -> e.getValue().doInherit(bgpConfig, warnings))
        .filter(e -> isActive(getTextDesc(e.getKey(), vrf), e.getValue(), warnings))
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey,
                e ->
                    (BgpPassivePeerConfig)
                        CiscoNxConversions.toBgpNeighbor(
                            c,
                            vrf,
                            proc,
                            e.getKey(),
                            bgpConfig,
                            nxBgpVrf,
                            e.getValue(),
                            true,
                            warnings)));
  }

  @Nullable
  private static Ip computeUpdateSource(
      Vrf vrf,
      Prefix prefix,
      CiscoNxBgpVrfNeighborConfiguration neighbor,
      boolean dynamic,
      Warnings warnings) {
    String updateSourceInterface = neighbor.getUpdateSource();
    if (updateSourceInterface != null) {
      Interface iface = vrf.getInterfaces().get(updateSourceInterface);
      if (iface == null) {
        warnings.redFlag(
            String.format(
                "BGP neighbor %s in vrf %s: configured update-source %s does not exist or "
                    + "is not associated with this vrf",
                dynamic ? prefix : prefix.getStartIp(), vrf.getName(), updateSourceInterface));
        return null;
      }
      ConcreteInterfaceAddress address = iface.getConcreteAddress();
      if (address == null) {
        warnings.redFlag(
            String.format(
                "BGP neighbor %s in vrf %s: configured update-source %s has no IP address",
                dynamic ? prefix : prefix.getStartIp(), vrf.getName(), updateSourceInterface));
        return null;
      }
      return address.getIp();
    } else if (dynamic) {
      return Ip.AUTO;
    }
    Optional<Ip> firstMatchingInterfaceAddress =
        vrf.getInterfaces().values().stream()
            .flatMap(i -> i.getAllConcreteAddresses().stream())
            .filter(ia -> ia != null && ia.getPrefix().containsIp(prefix.getStartIp()))
            .map(ConcreteInterfaceAddress::getIp)
            .findFirst();
    if (firstMatchingInterfaceAddress.isPresent()) {
      /* TODO: Warn here? Seems like this may be standard practice, e.g., for a /31. */
      return firstMatchingInterfaceAddress.get();
    }

    warnings.redFlag(
        String.format(
            "BGP neighbor %s in vrf %s: could not determine update source",
            prefix.getStartIp(), vrf.getName()));
    return null;
  }

  @Nonnull
  private static BgpPeerConfig toBgpNeighbor(
      Configuration c,
      Vrf vrf,
      BgpProcess proc,
      Prefix prefix,
      CiscoNxBgpGlobalConfiguration bgpConfig,
      CiscoNxBgpVrfConfiguration vrfConfig,
      CiscoNxBgpVrfNeighborConfiguration neighbor,
      boolean dynamic,
      Warnings warnings) {

    BgpPeerConfig.Builder<?, ?> newNeighborBuilder;
    if (dynamic) {
      newNeighborBuilder =
          BgpPassivePeerConfig.builder()
              .setRemoteAsns(
                  Optional.ofNullable(neighbor.getRemoteAs())
                      .map(LongSpace::of)
                      .orElse(LongSpace.EMPTY))
              .setPeerPrefix(prefix);
    } else {
      newNeighborBuilder =
          BgpActivePeerConfig.builder()
              .setRemoteAs(neighbor.getRemoteAs())
              .setPeerAddress(prefix.getStartIp());
    }

    newNeighborBuilder.setClusterId(
        firstNonNull(vrfConfig.getClusterId(), proc.getRouterId()).asLong());

    newNeighborBuilder.setDescription(neighbor.getDescription());

    newNeighborBuilder.setEbgpMultihop(firstNonNull(neighbor.getEbgpMultihopTtl(), 0) > 1);

    newNeighborBuilder.setEnforceFirstAs(bgpConfig.getEnforceFirstAs());

    if (neighbor.getInheritPeer() != null) {
      newNeighborBuilder.setGroup(neighbor.getInheritPeer());
    }

    if (neighbor.getLocalAs() != null) {
      newNeighborBuilder.setLocalAs(neighbor.getLocalAs());
    } else if (vrfConfig.getLocalAs() != null) {
      newNeighborBuilder.setLocalAs(vrfConfig.getLocalAs());
    } else {
      newNeighborBuilder.setLocalAs(bgpConfig.getLocalAs());
    }

    newNeighborBuilder.setLocalIp(computeUpdateSource(vrf, prefix, neighbor, dynamic, warnings));

    @Nullable
    CiscoNxBgpVrfNeighborAddressFamilyConfiguration naf4 = neighbor.getIpv4UnicastAddressFamily();
    @Nullable CiscoNxBgpVrfAddressFamilyConfiguration af4 = vrfConfig.getIpv4UnicastAddressFamily();
    Ipv4UnicastAddressFamily.Builder ipv4FamilyBuilder = Ipv4UnicastAddressFamily.builder();

    if (naf4 != null) {
      ipv4FamilyBuilder.setAddressFamilyCapabilities(
          AddressFamilyCapabilities.builder()
              .setAdvertiseInactive(
                  !firstNonNull(
                      naf4.getSuppressInactive(),
                      af4 != null ? af4.getSuppressInactive() : Boolean.FALSE))
              .setAllowLocalAsIn(firstNonNull(naf4.getAllowAsIn(), Boolean.FALSE))
              .setAllowRemoteAsOut(firstNonNull(naf4.getDisablePeerAsCheck(), Boolean.FALSE))
              .setSendCommunity(firstNonNull(naf4.getSendCommunityStandard(), Boolean.FALSE))
              .build());
      newNeighborBuilder.setRouteReflectorClient(
          firstNonNull(naf4.getRouteReflectorClient(), Boolean.FALSE));
      String inboundMap = naf4.getInboundRouteMap();

      ipv4FamilyBuilder.setImportPolicy(
          inboundMap != null && c.getRoutingPolicies().containsKey(inboundMap) ? inboundMap : null);
    }

    // Export policy
    List<Statement> exportStatements = new LinkedList<>();
    if (naf4 != null && firstNonNull(naf4.getNextHopSelf(), Boolean.FALSE)) {
      exportStatements.add(new SetNextHop(SelfNextHop.getInstance(), false));
    }
    if (neighbor.getRemovePrivateAs() != null) {
      // TODO(handle different types of RemovePrivateAs)
      exportStatements.add(RemovePrivateAs.toStaticStatement());
    }

    // If defaultOriginate is set, generate route and default route export policy. Default route
    // will match this policy and get exported without going through the rest of the export policy.
    // TODO Verify that nextHopSelf and removePrivateAs settings apply to default-originate route.
    if (naf4 != null && firstNonNull(naf4.getDefaultOriginate(), Boolean.FALSE)) {
      initBgpDefaultRouteExportPolicy(c);
      exportStatements.add(
          new If(
              "Export default route from peer with default-originate configured",
              new CallExpr(computeNxosBgpDefaultRouteExportPolicyName(true)),
              singletonList(Statements.ReturnTrue.toStaticStatement()),
              ImmutableList.of()));

      GeneratedRoute defaultRoute =
          GeneratedRoute.builder()
              .setNetwork(Prefix.ZERO)
              .setAdmin(MAX_ADMINISTRATIVE_COST)
              .setGenerationPolicy(naf4.getDefaultOriginateMap())
              .build();
      newNeighborBuilder.setGeneratedRoutes(ImmutableSet.of(defaultRoute));
    }

    // Peer-specific export policy, after matching default-originate route.
    Conjunction peerExportGuard = new Conjunction();
    List<BooleanExpr> peerExportConditions = peerExportGuard.getConjuncts();
    exportStatements.add(
        new If(
            "peer-export policy main conditional: exitAccept if true / exitReject if false",
            peerExportGuard,
            ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
            ImmutableList.of(Statements.ExitReject.toStaticStatement())));
    peerExportConditions.add(new CallExpr(computeBgpCommonExportPolicyName(vrf.getName())));

    if (naf4 != null) {
      String outboundMap = naf4.getOutboundRouteMap();
      if (outboundMap != null && c.getRoutingPolicies().containsKey(outboundMap)) {
        peerExportConditions.add(new CallExpr(outboundMap));
      }
    }

    RoutingPolicy exportPolicy =
        new RoutingPolicy(
            computeBgpPeerExportPolicyName(
                vrf.getName(), dynamic ? prefix.toString() : prefix.getStartIp().toString()),
            c);
    exportPolicy.setStatements(exportStatements);
    c.getRoutingPolicies().put(exportPolicy.getName(), exportPolicy);
    ipv4FamilyBuilder.setExportPolicy(exportPolicy.getName());
    newNeighborBuilder.setIpv4UnicastAddressFamily(ipv4FamilyBuilder.build());

    return newNeighborBuilder.build();
  }

  /**
   * Initializes export policy for default routes if it doesn't already exist. This policy is the
   * same across BGP processes, so only one is created for each configuration.
   */
  static void initBgpDefaultRouteExportPolicy(Configuration c) {
    String defaultRouteExportPolicyName = computeNxosBgpDefaultRouteExportPolicyName(true);
    if (!c.getRoutingPolicies().containsKey(defaultRouteExportPolicyName)) {
      RoutingPolicy.builder()
          .setOwner(c)
          .setName(defaultRouteExportPolicyName)
          .addStatement(
              new If(
                  new Conjunction(
                      ImmutableList.of(
                          MATCH_DEFAULT_ROUTE, new MatchProtocol(RoutingProtocol.AGGREGATE))),
                  ImmutableList.of(
                      new SetOrigin(new LiteralOrigin(OriginType.IGP, null)),
                      Statements.ReturnTrue.toStaticStatement())))
          .addStatement(Statements.ReturnFalse.toStaticStatement())
          .build();
    }
  }

  private static String getTextDesc(Ip ip, Vrf v) {
    return String.format("BGP neighbor %s in vrf %s", ip.toString(), v.getName());
  }

  private static String getTextDesc(Prefix prefix, Vrf v) {
    return String.format("BGP neighbor %s in vrf %s", prefix.toString(), v.getName());
  }

  private CiscoNxConversions() {} // prevent instantiation of utility class.
}
