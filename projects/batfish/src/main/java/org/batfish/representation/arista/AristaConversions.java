package org.batfish.representation.arista;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Names.generatedBgpCommonExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerEvpnExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerImportPolicyName;
import static org.batfish.datamodel.bgp.AllowRemoteAsOutMode.ALWAYS;
import static org.batfish.datamodel.routing_policy.Common.DEFAULT_UNDERSCORE_REPLACEMENT;
import static org.batfish.datamodel.routing_policy.Common.generateSuppressionPolicy;
import static org.batfish.datamodel.routing_policy.communities.CommunitySetExprs.toMatchExpr;
import static org.batfish.datamodel.routing_policy.statement.Statements.RemovePrivateAs;
import static org.batfish.representation.arista.AristaConfiguration.DEFAULT_VRF_NAME;
import static org.batfish.representation.arista.AristaConfiguration.MAX_ADMINISTRATIVE_COST;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
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
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.BgpAggregate;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.Layer2VniConfig;
import org.batfish.datamodel.bgp.Layer3VniConfig;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.routing_policy.Common;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.ColonSeparatedRendering;
import org.batfish.datamodel.routing_policy.communities.CommunityAcl;
import org.batfish.datamodel.routing_policy.communities.CommunityAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunityIn;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAcl;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchBgpSessionType;
import org.batfish.datamodel.routing_policy.expr.MatchBgpSessionType.Type;
import org.batfish.datamodel.routing_policy.expr.MatchLocalPreference;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.MatchSourceProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.expr.UnchangedNextHop;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.representation.arista.eos.AristaBgpAggregateNetwork;
import org.batfish.representation.arista.eos.AristaBgpDefaultOriginate;
import org.batfish.representation.arista.eos.AristaBgpHasPeerGroup;
import org.batfish.representation.arista.eos.AristaBgpNeighbor;
import org.batfish.representation.arista.eos.AristaBgpNeighbor.RemovePrivateAsMode;
import org.batfish.representation.arista.eos.AristaBgpNeighborAddressFamily;
import org.batfish.representation.arista.eos.AristaBgpPeerFilter;
import org.batfish.representation.arista.eos.AristaBgpProcess;
import org.batfish.representation.arista.eos.AristaBgpV4DynamicNeighbor;
import org.batfish.representation.arista.eos.AristaBgpV4Neighbor;
import org.batfish.representation.arista.eos.AristaBgpVlan;
import org.batfish.representation.arista.eos.AristaBgpVlanAwareBundle;
import org.batfish.representation.arista.eos.AristaBgpVrf;
import org.batfish.representation.arista.eos.AristaBgpVrfEvpnAddressFamily;
import org.batfish.representation.arista.eos.AristaBgpVrfIpv4UnicastAddressFamily;
import org.batfish.representation.arista.eos.AristaEosVxlan;

/**
 * A utility class for converting between Arista EOS configurations and the Batfish
 * vendor-independent {@link org.batfish.datamodel}.
 */
@ParametersAreNonnullByDefault
final class AristaConversions {
  /** Computes the router ID. */
  static @Nonnull Ip getBgpRouterId(
      AristaBgpVrf vrfConfig, String vrfName, Map<String, Interface> vrfInterfaces, Warnings w) {
    // If Router ID is configured in the VRF-Specific BGP config, it always wins.
    if (vrfConfig.getRouterId() != null) {
      return vrfConfig.getRouterId();
    }

    String messageBase =
        String.format("Router-id is not manually configured for BGP process in VRF %s", vrfName);

    // Otherwise, Router ID is defined based on the interfaces in the VRF that have IP addresses.
    // EOS does NOT use shutdown interfaces to configure router-id.
    Map<String, Interface> interfaceMap =
        vrfInterfaces.entrySet().stream()
            .filter(e -> e.getValue().getActive())
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

    // Next, EOS prefers highest loopback IP.
    Collection<Interface> interfaces = interfaceMap.values();
    Optional<Ip> highestLoopback =
        interfaces.stream()
            .filter(i -> i.getName().startsWith("Loopback"))
            .map(Interface::getConcreteAddress)
            .map(ConcreteInterfaceAddress::getIp)
            .max(Comparator.naturalOrder());
    if (highestLoopback.isPresent()) {
      return highestLoopback.get();
    }

    // Finally, EOS uses the highest non-loopback interface IP defined in the vrf.
    Optional<Ip> highestIp =
        interfaces.stream()
            .map(Interface::getConcreteAddress)
            .map(ConcreteInterfaceAddress::getIp)
            .max(Comparator.naturalOrder());
    assert highestIp.isPresent(); // This cannot happen if interfaces is non-empty.
    return highestIp.get();
  }

  /**
   * Checks that the neighbor is not shutdown and at least one of the address families is activated
   */
  private static boolean isActive(
      String name, AristaBgpVrf vrf, AristaBgpV4Neighbor neighbor, Warnings w) {
    if (firstNonNull(neighbor.getShutdown(), Boolean.FALSE)) {
      return false;
    }

    // No active address family that we support.
    boolean v4 =
        Optional.ofNullable(vrf.getV4UnicastAf())
            .map(af -> af.getNeighbor(neighbor.getIp()))
            .map(AristaBgpNeighborAddressFamily::getActivate)
            .orElse(Boolean.FALSE);
    boolean evpn =
        Optional.ofNullable(vrf.getEvpnAf())
            .map(af -> af.getNeighbor(neighbor.getIp()))
            .map(AristaBgpNeighborAddressFamily::getActivate)
            .orElse(Boolean.FALSE);
    if (!v4 && !evpn) {
      w.redFlag("No supported address-family configured for " + name);
      return false;
    }
    return true;
  }

  /**
   * Checks that the neighbor is not shutdown and at least one of the address families is activated
   */
  private static boolean isActive(
      String name, AristaBgpVrf vrf, AristaBgpV4DynamicNeighbor neighbor, Warnings w) {
    if (firstNonNull(neighbor.getShutdown(), Boolean.FALSE)) {
      return false;
    }

    // No active address family that we support.
    boolean v4 =
        Optional.ofNullable(vrf.getV4UnicastAf())
            .map(af -> af.getNeighbor(neighbor.getRange()))
            .map(AristaBgpNeighborAddressFamily::getActivate)
            .orElse(Boolean.FALSE);
    boolean evpn =
        Optional.ofNullable(vrf.getEvpnAf())
            .map(af -> af.getNeighbor(neighbor.getRange()))
            .map(AristaBgpNeighborAddressFamily::getActivate)
            .orElse(Boolean.FALSE);
    if (!v4 && !evpn) {
      w.redFlag("No supported address-family configured for " + name);
      return false;
    }
    return true;
  }

  static @Nonnull BgpAggregate toBgpAggregate(
      Prefix prefix, AristaBgpAggregateNetwork vsAggregate, Configuration c, Warnings w) {
    // TODO: handle advertise-only
    // TODO: handle as-set
    // TODO: handle match-map
    // TODO: verify undefined attribute-map can be treated as omitted
    String attributeMap = vsAggregate.getAttributeMap();
    if (attributeMap != null && !c.getRoutingPolicies().containsKey(attributeMap)) {
      w.redFlagf("Ignoring undefined aggregate-address attribute-map %s", attributeMap);
      attributeMap = null;
    }
    return BgpAggregate.of(
        prefix,
        generateSuppressionPolicy(vsAggregate.getSummaryOnlyEffective(), c),
        // TODO: put match-map here
        null,
        attributeMap);
  }

  static @Nonnull Map<Ip, BgpActivePeerConfig> getNeighbors(
      Configuration c,
      Vrf vrf,
      BgpProcess proc,
      AristaBgpProcess bgpConfig,
      AristaBgpVrf bgpVrf,
      @Nullable AristaEosVxlan vxlan,
      @Nullable Ip vxlanSourceInterfaceIp,
      Warnings warnings) {

    return bgpVrf.getV4neighbors().entrySet().stream()
        .peek(e -> e.getValue().inherit(bgpConfig, bgpVrf, warnings))
        .filter(e -> isActive(getTextDesc(e.getKey(), vrf), bgpVrf, e.getValue(), warnings))
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey,
                e ->
                    (BgpActivePeerConfig)
                        AristaConversions.toBgpNeighbor(
                            c,
                            vrf,
                            proc,
                            e.getKey().toPrefix(),
                            bgpConfig,
                            bgpVrf,
                            e.getValue(),
                            false,
                            vxlan,
                            vxlanSourceInterfaceIp,
                            ImmutableMap.of(), // peer filters not needed for non-dynamic peers
                            warnings)));
  }

  static @Nonnull Map<Prefix, BgpPassivePeerConfig> getPassiveNeighbors(
      Configuration c,
      Vrf vrf,
      BgpProcess proc,
      AristaBgpProcess bgpConfig,
      AristaBgpVrf bgpVrf,
      @Nullable AristaEosVxlan vxlan,
      @Nullable Ip vxlanSourceInterfaceIp,
      Map<String, AristaBgpPeerFilter> peerFilters,
      Warnings warnings) {
    return bgpVrf.getV4DynamicNeighbors().entrySet().stream()
        .peek(e -> e.getValue().inherit(bgpConfig, bgpVrf, warnings))
        .filter(e -> isActive(getTextDesc(e.getKey(), vrf), bgpVrf, e.getValue(), warnings))
        .collect(
            ImmutableMap.toImmutableMap(
                Entry::getKey,
                e ->
                    (BgpPassivePeerConfig)
                        AristaConversions.toBgpNeighbor(
                            c,
                            vrf,
                            proc,
                            e.getKey(),
                            bgpConfig,
                            bgpVrf,
                            e.getValue(),
                            true,
                            vxlan,
                            vxlanSourceInterfaceIp,
                            peerFilters,
                            warnings)));
  }

  private static @Nullable Ip computeUpdateSource(
      String vrfName,
      Map<String, Interface> vrfInterfaces,
      Prefix prefix,
      AristaBgpNeighbor neighbor,
      boolean dynamic,
      Warnings warnings) {
    String updateSourceInterface = neighbor.getUpdateSource();
    if (updateSourceInterface != null) {
      Interface iface = vrfInterfaces.get(updateSourceInterface);
      if (iface == null) {
        return null;
      }
      ConcreteInterfaceAddress address = iface.getConcreteAddress();
      if (address == null) {
        warnings.redFlag(
            String.format(
                "BGP neighbor %s in vrf %s: configured update-source %s has no IP address",
                dynamic ? prefix : prefix.getStartIp(), vrfName, updateSourceInterface));
        return null;
      }
      return address.getIp();
    } else if (dynamic) {
      return null;
    }
    Optional<Ip> firstMatchingInterfaceAddress =
        vrfInterfaces.values().stream()
            .flatMap(i -> i.getAllConcreteAddresses().stream())
            .filter(ia -> ia != null && ia.getPrefix().containsIp(prefix.getStartIp()))
            .map(ConcreteInterfaceAddress::getIp)
            .findFirst();
    if (firstMatchingInterfaceAddress.isPresent()) {
      /* TODO: Warn here? Seems like this may be standard practice, e.g., for a /31. */
      return firstMatchingInterfaceAddress.get();
    }
    return null;
  }

  /** Compute the remote AS space for a dynamic BGP neighbor */
  @VisibleForTesting
  static @Nonnull LongSpace getAsnSpace(
      AristaBgpV4DynamicNeighbor neighbor, Map<String, AristaBgpPeerFilter> peerFilters) {
    if (neighbor.getRemoteAs() != null) {
      return LongSpace.of(neighbor.getRemoteAs());
    } else if (neighbor.getPeerFilter() != null) {
      AristaBgpPeerFilter peerFilter = peerFilters.get(neighbor.getPeerFilter());
      if (peerFilter == null) {
        // If the filter does not exist, accept any ASN:
        // http://www.arista.com/en/um-eos/eos-section-33-2-configuring-bgp#ww1319501
        return BgpPeerConfig.ALL_AS_NUMBERS;
      }
      return peerFilter.toLongSpace();
    } else {
      return LongSpace.EMPTY;
    }
  }

  private static @Nonnull BgpPeerConfig toBgpNeighbor(
      Configuration c,
      Vrf vrf,
      BgpProcess proc,
      Prefix prefix,
      AristaBgpProcess bgpConfig,
      AristaBgpVrf vrfConfig,
      AristaBgpNeighbor neighbor,
      boolean dynamic,
      @Nullable AristaEosVxlan vxlan,
      @Nullable Ip vxlanSourceInterfaceIp,
      Map<String, AristaBgpPeerFilter> peerFilters,
      Warnings warnings) {
    // We should be converting only concrete (active or dynamic) neighbors
    assert neighbor instanceof AristaBgpHasPeerGroup;

    BgpPeerConfig.Builder<?, ?> newNeighborBuilder;
    if (dynamic) {
      assert neighbor instanceof AristaBgpV4DynamicNeighbor;
      LongSpace remoteAsns = getAsnSpace((AristaBgpV4DynamicNeighbor) neighbor, peerFilters);
      if (remoteAsns.isEmpty()) {
        warnings.redFlag(
            String.format(
                "No acceptable remote-as for %s",
                getTextDesc(((AristaBgpV4DynamicNeighbor) neighbor).getRange(), vrf)));
      }
      newNeighborBuilder =
          BgpPassivePeerConfig.builder().setRemoteAsns(remoteAsns).setPeerPrefix(prefix);
    } else {
      assert neighbor instanceof AristaBgpV4Neighbor;
      LongSpace remoteAsns =
          Optional.ofNullable(neighbor.getRemoteAs()).map(LongSpace::of).orElse(LongSpace.EMPTY);
      if (remoteAsns.isEmpty()) {
        warnings.redFlag(
            String.format(
                "No remote-as configured for %s",
                getTextDesc(((AristaBgpV4Neighbor) neighbor).getIp(), vrf)));
      }
      newNeighborBuilder =
          BgpActivePeerConfig.builder()
              .setRemoteAsns(remoteAsns)
              .setPeerAddress(prefix.getStartIp());
    }

    newNeighborBuilder.setClusterId(
        firstNonNull(vrfConfig.getClusterId(), proc.getRouterId()).asLong());

    newNeighborBuilder.setDescription(neighbor.getDescription());

    newNeighborBuilder.setEbgpMultihop(firstNonNull(neighbor.getEbgpMultihop(), 0) > 1);

    newNeighborBuilder.setEnforceFirstAs(
        firstNonNull(
            neighbor.getEnforceFirstAs(),
            firstNonNull(vrfConfig.getEnforceFirstAs(), Boolean.TRUE)));

    if (((AristaBgpHasPeerGroup) neighbor).getPeerGroup() != null) {
      newNeighborBuilder.setGroup(((AristaBgpHasPeerGroup) neighbor).getPeerGroup());
    }

    if (neighbor.getLocalAs() != null) {
      newNeighborBuilder.setLocalAs(neighbor.getLocalAs());
    } else if (vrfConfig.getLocalAs() != null) {
      newNeighborBuilder.setLocalAs(vrfConfig.getLocalAs());
    } else {
      newNeighborBuilder.setLocalAs(bgpConfig.getAsn());
    }

    newNeighborBuilder.setLocalIp(
        computeUpdateSource(
            vrf.getName(), c.getAllInterfaces(vrf.getName()), prefix, neighbor, dynamic, warnings));

    @Nullable AristaBgpVrfIpv4UnicastAddressFamily af4 = vrfConfig.getV4UnicastAf();
    @Nullable AristaBgpNeighborAddressFamily naf4;
    if (neighbor instanceof AristaBgpV4Neighbor) {
      naf4 = af4 == null ? null : af4.getNeighbor(((AristaBgpV4Neighbor) neighbor).getIp());
    } else if (neighbor instanceof AristaBgpV4DynamicNeighbor) {
      naf4 =
          af4 == null ? null : af4.getNeighbor(((AristaBgpV4DynamicNeighbor) neighbor).getRange());
    } else {
      throw new IllegalStateException("Unsupported type of BGP neighbor");
    }
    Ipv4UnicastAddressFamily.Builder ipv4FamilyBuilder = Ipv4UnicastAddressFamily.builder();
    boolean v4Enabled = naf4 != null && firstNonNull(naf4.getActivate(), Boolean.FALSE);

    String peerStrRepr = dynamic ? prefix.toString() : prefix.getStartIp().toString();
    if (v4Enabled) {
      ipv4FamilyBuilder.setAddressFamilyCapabilities(
          AddressFamilyCapabilities.builder()
              .setAdvertiseInactive(firstNonNull(vrfConfig.getAdvertiseInactive(), Boolean.FALSE))
              .setAllowLocalAsIn(
                  firstNonNull(neighbor.getAllowAsIn(), firstNonNull(vrfConfig.getAllowAsIn(), 0))
                      > 0)
              .setAllowRemoteAsOut(ALWAYS) // no outgoing remote-as check on Arista
              .setSendCommunity(firstNonNull(neighbor.getSendCommunity(), Boolean.FALSE))
              .setSendExtendedCommunity(
                  firstNonNull(neighbor.getSendExtendedCommunity(), Boolean.FALSE))
              .build());

      String inboundMap = naf4.getRouteMapIn();
      String inboundPrefixList = naf4.getPrefixListIn();
      String policy = null;
      if (inboundMap != null
          && inboundPrefixList != null
          && c.getRoutingPolicies().containsKey(inboundMap)
          && c.getRouteFilterLists().containsKey(inboundPrefixList)) {
        warnings.redFlag(
            String.format(
                "Inbound prefix list %s + route map %s not supported for neighbor %s. Preferring"
                    + " route map.",
                inboundPrefixList, inboundMap, peerStrRepr));
        policy = inboundMap;
      } else if (inboundMap != null && c.getRoutingPolicies().containsKey(inboundMap)) {
        policy = inboundMap;
      } else if (inboundPrefixList != null
          && c.getRouteFilterLists().containsKey(inboundPrefixList)) {
        policy = generatedBgpPeerImportPolicyName(vrf.getName(), peerStrRepr);
        RoutingPolicy.builder()
            .setOwner(c)
            .setName(policy)
            .setStatements(
                ImmutableList.of(
                    new If(
                        new MatchPrefixSet(
                            DestinationNetwork.instance(), new NamedPrefixSet(inboundPrefixList)),
                        ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                        ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))))
            .build();
      }
      ipv4FamilyBuilder
          .setImportPolicy(policy)
          .setRouteReflectorClient(firstNonNull(neighbor.getRouteReflectorClient(), Boolean.FALSE));
    }

    // Export policy
    List<Statement> exportStatements = new LinkedList<>();

    // First, change unset local preference for local (non-learned) routes to (neighbor-specific)
    // default value. Only applies to IBGP peers.
    If overrideUnsetLocalPref =
        new If(
            new Conjunction(
                ImmutableList.of(
                    // This is an IBGP session.
                    new MatchBgpSessionType(Type.IBGP),
                    // The route being exported is not learned.
                    new Not(new MatchSourceProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP)),
                    // The route being exported has the unset default local preference of 0 (since
                    // we can't yet model unset explicitly)
                    new MatchLocalPreference(IntComparator.EQ, new LiteralLong(0)))),
            ImmutableList.of(
                new SetLocalPreference(
                    new LiteralLong(
                        firstNonNull(
                            neighbor.getExportLocalPref(),
                            AristaBgpNeighbor.SYSTEM_DEFAULT_LOCALPREF)))));
    exportStatements.add(overrideUnsetLocalPref);

    @Nullable
    AristaBgpDefaultOriginate defaultOriginate =
        // bgp default-originate: address-family, per-neighbor binding wins first.
        Optional.ofNullable(naf4)
            .map(AristaBgpNeighborAddressFamily::getDefaultOriginate)
            // but if not present, then per-neighbor binding wins
            .orElse(neighbor.getDefaultOriginate());
    if (v4Enabled && defaultOriginate != null && defaultOriginate.getEnabled()) {
      // TODO: fix the export pipeline in VI so that setting the attribute policy is sufficient.
      //   Similarly, "new MatchProtocol(RoutingProtocol.AGGREGATE)" below should go away
      //   https://github.com/batfish/batfish/issues/5375

      // 1. Unconditionally generate a default route that is sent directly to this neighbor, without
      // going through the export policy.
      GeneratedRoute defaultRoute =
          GeneratedRoute.builder()
              .setNetwork(Prefix.ZERO)
              .setAdmin(MAX_ADMINISTRATIVE_COST)
              .setAttributePolicy(defaultOriginate.getRouteMap())
              .build();
      newNeighborBuilder.setGeneratedRoutes(ImmutableSet.of(defaultRoute));

      // 2. Do not export any other default route to this neighbor, since the generated route should
      // dominate.
      exportStatements.add(
          new If(
              Common.matchDefaultRoute(), // we are exporting some default route
              ImmutableList.of(
                  new If(
                      new MatchProtocol(RoutingProtocol.AGGREGATE),
                      // default-originate (we generated it): let it through.
                      ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                      // not default-originate: deny.
                      ImmutableList.of(Statements.ReturnFalse.toStaticStatement())))));
    }
    if (firstNonNull(neighbor.getNextHopSelf(), Boolean.FALSE)) {
      exportStatements.add(new SetNextHop(SelfNextHop.getInstance()));
    }
    if (firstNonNull(neighbor.getRemovePrivateAsMode(), RemovePrivateAsMode.NONE)
        != RemovePrivateAsMode.NONE) {
      // TODO(handle different types of RemovePrivateAs)
      exportStatements.add(RemovePrivateAs.toStaticStatement());
    }
    if (firstNonNull(
        v4Enabled ? naf4.getNextHopUnchanged() : null,
        firstNonNull(
            neighbor.getNextHopUnchanged(),
            firstNonNull(
                af4 == null ? null : af4.getNextHopUnchanged(),
                firstNonNull(vrfConfig.getNextHopUnchanged(), Boolean.FALSE))))) {
      exportStatements.add(new SetNextHop(UnchangedNextHop.getInstance()));
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
    peerExportConditions.add(new CallExpr(generatedBgpCommonExportPolicyName(vrf.getName())));

    if (v4Enabled) {
      String outboundMap = naf4.getRouteMapOut();
      String outboundPrefixList = naf4.getPrefixListOut();
      if (outboundMap != null
          && outboundPrefixList != null
          && c.getRoutingPolicies().containsKey(outboundMap)
          && c.getRouteFilterLists().containsKey(outboundPrefixList)) {
        warnings.redFlag(
            String.format(
                "Outbound prefix list %s + route map %s not supported for neighbor %s. Preferring"
                    + " route map.",
                outboundPrefixList, outboundMap, peerStrRepr));
        peerExportConditions.add(new CallExpr(outboundMap));
      } else if (outboundMap != null && c.getRoutingPolicies().containsKey(outboundMap)) {
        peerExportConditions.add(new CallExpr(outboundMap));
      } else if (outboundPrefixList != null
          && c.getRouteFilterLists().containsKey(outboundPrefixList)) {
        peerExportConditions.add(
            new MatchPrefixSet(
                DestinationNetwork.instance(), new NamedPrefixSet(outboundPrefixList)));
      }
    }

    RoutingPolicy exportPolicy =
        new RoutingPolicy(generatedBgpPeerExportPolicyName(vrf.getName(), peerStrRepr), c);
    exportPolicy.setStatements(exportStatements);
    c.getRoutingPolicies().put(exportPolicy.getName(), exportPolicy);
    if (v4Enabled) {
      ipv4FamilyBuilder.setExportPolicy(exportPolicy.getName());
      newNeighborBuilder.setIpv4UnicastAddressFamily(ipv4FamilyBuilder.build());
    }

    @Nullable AristaBgpVrfEvpnAddressFamily evpnAf = vrfConfig.getEvpnAf();
    @Nullable AristaBgpNeighborAddressFamily nEvpn;
    if (neighbor instanceof AristaBgpV4Neighbor) {
      nEvpn = evpnAf == null ? null : evpnAf.getNeighbor(((AristaBgpV4Neighbor) neighbor).getIp());
    } else {
      nEvpn =
          evpnAf == null
              ? null
              : evpnAf.getNeighbor(((AristaBgpV4DynamicNeighbor) neighbor).getRange());
    }
    boolean evpnEnabled = nEvpn != null && firstNonNull(nEvpn.getActivate(), Boolean.FALSE);
    if (evpnEnabled) {
      EvpnAddressFamily.Builder evpnFamilyBuilder = EvpnAddressFamily.builder();

      evpnFamilyBuilder
          .setNveIp(vxlanSourceInterfaceIp)
          .setPropagateUnmatched(true)
          .setAddressFamilyCapabilities(
              AddressFamilyCapabilities.builder()
                  .setAdvertiseInactive(
                      firstNonNull(vrfConfig.getAdvertiseInactive(), Boolean.FALSE))
                  .setAllowLocalAsIn(
                      firstNonNull(
                              neighbor.getAllowAsIn(), firstNonNull(vrfConfig.getAllowAsIn(), 0))
                          > 0)
                  .setAllowRemoteAsOut(ALWAYS) // no outgoing remote-as check on Arista
                  .setSendCommunity(firstNonNull(neighbor.getSendCommunity(), Boolean.FALSE))
                  .setSendExtendedCommunity(
                      firstNonNull(neighbor.getSendCommunity(), Boolean.FALSE))
                  .build());

      ImmutableSet.Builder<Layer2VniConfig> l2vnis = ImmutableSet.builder();
      SortedMap<Integer, Integer> vlanToVni =
          vxlan == null ? ImmutableSortedMap.of() : vxlan.getVlanVnis();
      for (AristaBgpVlan vlanConfig : bgpConfig.getVlans().values()) {
        if (vlanConfig.getRd() == null
            || vlanConfig.getRtImport() == null
            || vlanConfig.getRtExport() == null) {
          continue;
        }
        Vrf vrfForVlan = getVrfForVlan(c, vlanConfig.getVlan()).orElse(null);
        if (vrfForVlan == null) {
          continue;
        }
        Integer vni = vlanToVni.get(vlanConfig.getVlan());
        if (vni == null) {
          continue;
        }
        l2vnis.add(
            Layer2VniConfig.builder()
                .setVni(vni)
                .setImportRouteTarget(vlanConfig.getRtImport().matchString())
                .setRouteTarget(vlanConfig.getRtExport())
                .setRouteDistinguisher(vlanConfig.getRd())
                .setVrf(vrfForVlan.getName())
                .build());
      }
      for (AristaBgpVlanAwareBundle bundle : bgpConfig.getVlanAwareBundles().values()) {
        if (bundle.getVlans() == null
            || bundle.getRd() == null
            || bundle.getRtExport() == null
            || bundle.getRtImport() == null) {
          continue;
        }
        for (Integer vlan : bundle.getVlans().enumerate()) {
          Integer vni = vlanToVni.get(vlan);
          if (vni == null) {
            continue;
          }
          l2vnis.add(
              Layer2VniConfig.builder()
                  .setVni(vni)
                  .setImportRouteTarget(bundle.getRtImport().matchString())
                  .setRouteTarget(bundle.getRtExport())
                  .setRouteDistinguisher(bundle.getRd())
                  // TODO: remove vrf from Layer2Vni
                  .setVrf(DEFAULT_VRF_NAME)
                  .build());
        }
      }
      evpnFamilyBuilder.setL2Vnis(l2vnis.build());

      ImmutableSet.Builder<Layer3VniConfig> l3vnis = ImmutableSet.builder();
      Map<String, Integer> vrfToVni = vxlan == null ? ImmutableMap.of() : vxlan.getVrfToVni();
      for (Entry<String, Integer> entry : vrfToVni.entrySet()) {
        String vrfName = entry.getKey();
        if (!c.getVrfs().containsKey(vrfName) || !bgpConfig.getVrfs().containsKey(vrfName)) {
          continue;
        }
        AristaBgpVrf bgpVrf = bgpConfig.getVrfs().get(vrfName);
        if (bgpVrf.getRouteDistinguisher() == null
            || bgpVrf.getImportRouteTarget() == null
            || bgpVrf.getExportRouteTarget() == null) {
          continue;
        }
        l3vnis.add(
            Layer3VniConfig.builder()
                .setAdvertiseV4Unicast(true)
                .setVni(entry.getValue())
                .setImportRouteTarget(bgpVrf.getImportRouteTarget().matchString())
                .setRouteTarget(bgpVrf.getExportRouteTarget())
                .setRouteDistinguisher(bgpVrf.getRouteDistinguisher())
                .setVrf(vrfName)
                .build());
      }
      evpnFamilyBuilder.setL3Vnis(l3vnis.build());
      // Peer-specific export policy for EVPN
      String neighborKey;
      if (neighbor instanceof AristaBgpV4Neighbor) {
        neighborKey = ((AristaBgpV4Neighbor) neighbor).getIp().toString();
      } else if (neighbor instanceof AristaBgpV4DynamicNeighbor) {
        neighborKey = ((AristaBgpV4DynamicNeighbor) neighbor).getRange().toString();
      } else {
        throw new IllegalStateException("Unsupported type of BGP neighbor");
      }
      String policyName = generatedBgpPeerEvpnExportPolicyName(vrfConfig.getName(), neighborKey);

      // TODO: handle other modifiers (next-hop-self, etc.) and export route map
      Builder<Statement> exportStatementsBuilder = ImmutableList.builder();
      if (firstNonNull(
          nEvpn.getNextHopUnchanged(),
          firstNonNull(
              neighbor.getNextHopUnchanged(),
              firstNonNull(
                  evpnAf.getNextHopUnchanged(),
                  firstNonNull(vrfConfig.getNextHopUnchanged(), Boolean.FALSE))))) {
        exportStatementsBuilder.add(new SetNextHop(UnchangedNextHop.getInstance()));
      }
      exportStatementsBuilder.add(Statements.ExitAccept.toStaticStatement());
      RoutingPolicy.builder()
          .addStatement(
              new If(
                  "peer-export policy main conditional: exitAccept if true / exitReject if false",
                  new Conjunction(
                      Collections.singletonList(
                          new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP))),
                  exportStatementsBuilder.build(),
                  ImmutableList.of(Statements.ExitReject.toStaticStatement())))
          .setName(policyName)
          .setOwner(c)
          .build();
      evpnFamilyBuilder.setExportPolicy(policyName);
      String importPolicyName = nEvpn.getRouteMapIn();
      if (importPolicyName != null && c.getRoutingPolicies().get(importPolicyName) != null) {
        evpnFamilyBuilder.setImportPolicy(importPolicyName);
      }
      evpnFamilyBuilder.setRouteReflectorClient(
          firstNonNull(neighbor.getRouteReflectorClient(), Boolean.FALSE));
      newNeighborBuilder.setEvpnAddressFamily(evpnFamilyBuilder.build());
    }

    return newNeighborBuilder.build();
  }

  static @Nonnull Optional<Ip> getSourceInterfaceIp(
      @Nullable AristaEosVxlan vxlan,
      Map<String, org.batfish.representation.arista.Interface> interfaces) {
    return Optional.ofNullable(vxlan)
        .map(AristaEosVxlan::getSourceInterface)
        .map(interfaces::get)
        .map(org.batfish.representation.arista.Interface::getAddress)
        .map(ConcreteInterfaceAddress::getIp);
  }

  static @Nonnull Optional<Vrf> getVrfForVlan(Configuration c, int vlan) {
    return c.getVrfs().values().stream()
        .filter(vrf -> c.getAllInterfaces(vrf.getName()).containsKey(String.format("Vlan%d", vlan)))
        .findFirst();
  }

  private static String getTextDesc(Ip ip, Vrf v) {
    return String.format("BGP neighbor %s in vrf %s", ip.toString(), v.getName());
  }

  private static String getTextDesc(Prefix prefix, Vrf v) {
    return String.format("BGP neighbor %s in vrf %s", prefix.toString(), v.getName());
  }

  static @Nonnull CommunitySetMatchExpr toCommunitySetMatchExpr(
      ExpandedCommunityList ipCommunityListExpanded) {
    return CommunitySetAcl.acl(
        ipCommunityListExpanded.getLines().stream()
            .map(AristaConversions::toCommunitySetAclLine)
            .collect(ImmutableList.toImmutableList()));
  }

  static @Nonnull CommunitySetMatchExpr toCommunitySetMatchExpr(
      StandardCommunityList ipCommunityListStandard) {
    return CommunitySetAcl.acl(
        ipCommunityListStandard.getLines().stream()
            .map(AristaConversions::toCommunitySetAclLine)
            .collect(ImmutableList.toImmutableList()));
  }

  private static @Nonnull CommunitySetAclLine toCommunitySetAclLine(
      StandardCommunityListLine line) {
    return new CommunitySetAclLine(
        line.getAction(),
        CommunitySetMatchAll.matchAll(
            line.getCommunities().stream()
                .map(community -> new HasCommunity(new CommunityIs(community)))
                .collect(ImmutableSet.toImmutableSet())));
  }

  static @Nonnull CommunityMatchExpr toCommunityMatchExpr(
      ExpandedCommunityList ipCommunityListExpanded) {
    return CommunityAcl.acl(
        ipCommunityListExpanded.getLines().stream()
            .map(AristaConversions::toCommunityAclLine)
            .collect(ImmutableList.toImmutableList()));
  }

  private static @Nonnull CommunityAclLine toCommunityAclLine(ExpandedCommunityListLine line) {
    return new CommunityAclLine(
        line.getAction(), AristaConversions.toCommunityMatchRegex(line.getRegex()));
  }

  static @Nonnull CommunityMatchExpr toCommunityMatchExpr(
      StandardCommunityList ipCommunityListStandard) {
    Set<Community> whitelist = new HashSet<>();
    Set<Community> blacklist = new HashSet<>();
    for (StandardCommunityListLine line : ipCommunityListStandard.getLines()) {
      if (line.getCommunities().size() != 1) {
        continue;
      }
      Community community = Iterables.getOnlyElement(line.getCommunities());
      if (line.getAction() == LineAction.PERMIT) {
        if (!blacklist.contains(community)) {
          whitelist.add(community);
        }
      } else {
        // DENY
        if (!whitelist.contains(community)) {
          blacklist.add(community);
        }
      }
    }
    return new CommunityIn(new LiteralCommunitySet(CommunitySet.of(whitelist)));
  }

  static @Nonnull CommunityMatchRegex toCommunityMatchRegex(String regex) {
    return new CommunityMatchRegex(ColonSeparatedRendering.instance(), toJavaRegex(regex));
  }

  static @Nonnull CommunitySetAclLine toCommunitySetAclLine(ExpandedCommunityListLine line) {
    return new CommunitySetAclLine(line.getAction(), toMatchExpr(toJavaRegex(line.getRegex())));
  }

  static @Nonnull String toJavaRegex(String ciscoRegex) {
    String withoutQuotes;
    if (ciscoRegex.charAt(0) == '"' && ciscoRegex.charAt(ciscoRegex.length() - 1) == '"') {
      withoutQuotes = ciscoRegex.substring(1, ciscoRegex.length() - 1);
    } else {
      withoutQuotes = ciscoRegex;
    }
    String output = withoutQuotes.replaceAll("_", DEFAULT_UNDERSCORE_REPLACEMENT);
    return output;
  }

  static @Nonnull CommunitySet toCommunitySet(StandardCommunityList list) {
    return CommunitySet.of(
        list.getLines().stream()
            .filter(
                line -> line.getAction() == LineAction.PERMIT && line.getCommunities().size() == 1)
            .flatMap(line -> line.getCommunities().stream())
            .collect(ImmutableList.toImmutableList()));
  }

  static @Nonnull CommunitySet toCommunitySet(ExpandedCommunityList list) {
    // Cannot use expanded list for setting communities
    return CommunitySet.empty();
  }

  static @Nonnull Collection<RoutingProtocol> toOspfRedistributionProtocols(
      RedistributionSourceProtocol protocol) {
    return switch (protocol) {
      case BGP_ANY ->
          ImmutableList.of(RoutingProtocol.AGGREGATE, RoutingProtocol.BGP, RoutingProtocol.IBGP);
      case CONNECTED -> ImmutableList.of(RoutingProtocol.CONNECTED);
      case ISIS_ANY ->
          ImmutableList.of(
              RoutingProtocol.ISIS_EL1,
              RoutingProtocol.ISIS_EL2,
              RoutingProtocol.ISIS_L1,
              RoutingProtocol.ISIS_L2);
      case STATIC -> ImmutableList.of(RoutingProtocol.STATIC);
      case ISIS_L1 ->
          throw new IllegalArgumentException(
              "Unknown/invalid redistribution source protocol for OSPF" + protocol);
    };
  }

  private AristaConversions() {} // prevent instantiation of utility class.
}
