package org.batfish.representation.arista;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.datamodel.Names.generatedBgpCommonExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerEvpnExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerImportPolicyName;
import static org.batfish.datamodel.bgp.AllowRemoteAsOutMode.ALWAYS;
import static org.batfish.datamodel.routing_policy.statement.Statements.RemovePrivateAs;
import static org.batfish.representation.arista.AristaConfiguration.MAX_ADMINISTRATIVE_COST;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.Layer2VniConfig;
import org.batfish.datamodel.bgp.Layer3VniConfig;
import org.batfish.datamodel.routing_policy.Common;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.expr.UnchangedNextHop;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
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
  @Nonnull
  static Ip getBgpRouterId(
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

    // No remote AS set.
    if (neighbor.getRemoteAs() == null) {
      w.redFlag("No remote-as configured for " + name);
      return false;
    }

    return true;
  }

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

    // No remote AS set.
    if (neighbor.getRemoteAs() == null && neighbor.getPeerFilter() == null) {
      w.redFlag("No remote-as configured for " + name);
      return false;
    }

    return true;
  }

  @Nonnull
  static Map<Prefix, BgpActivePeerConfig> getNeighbors(
      Configuration c,
      Vrf vrf,
      BgpProcess proc,
      AristaBgpProcess bgpConfig,
      AristaBgpVrf bgpVrf,
      @Nullable AristaEosVxlan vxlan,
      Warnings warnings) {

    return bgpVrf.getV4neighbors().entrySet().stream()
        .peek(e -> e.getValue().inherit(bgpConfig, bgpVrf, warnings))
        .filter(e -> isActive(getTextDesc(e.getKey(), vrf), bgpVrf, e.getValue(), warnings))
        .collect(
            ImmutableMap.toImmutableMap(
                e -> e.getKey().toPrefix(),
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
                            ImmutableMap.of(), // peer filters not needed for non-dynamic peers
                            warnings)));
  }

  @Nonnull
  static Map<Prefix, BgpPassivePeerConfig> getPassiveNeighbors(
      Configuration c,
      Vrf vrf,
      BgpProcess proc,
      AristaBgpProcess bgpConfig,
      AristaBgpVrf bgpVrf,
      @Nullable AristaEosVxlan vxlan,
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
                            peerFilters,
                            warnings)));
  }

  @Nullable
  private static Ip computeUpdateSource(
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
        warnings.redFlag(
            String.format(
                "BGP neighbor %s in vrf %s: configured update-source %s does not exist or "
                    + "is not associated with this vrf",
                dynamic ? prefix : prefix.getStartIp(), vrfName, updateSourceInterface));
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
      return Ip.AUTO;
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

    warnings.redFlag(
        String.format(
            "BGP neighbor %s in vrf %s: could not determine update source",
            prefix.getStartIp(), vrfName));
    return null;
  }

  /** Compute the remote AS space for a dynamic BGP neighbor */
  @Nonnull
  @VisibleForTesting
  static LongSpace getAsnSpace(
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

  @Nonnull
  private static BgpPeerConfig toBgpNeighbor(
      Configuration c,
      Vrf vrf,
      BgpProcess proc,
      Prefix prefix,
      AristaBgpProcess bgpConfig,
      AristaBgpVrf vrfConfig,
      AristaBgpNeighbor neighbor,
      boolean dynamic,
      @Nullable AristaEosVxlan vxlan,
      Map<String, AristaBgpPeerFilter> peerFilters,
      Warnings warnings) {
    // We should be converting only concrete (active or dynamic) neighbors
    assert neighbor instanceof AristaBgpHasPeerGroup;

    BgpPeerConfig.Builder<?, ?> newNeighborBuilder;
    if (dynamic) {
      assert neighbor instanceof AristaBgpV4DynamicNeighbor;
      newNeighborBuilder =
          BgpPassivePeerConfig.builder()
              .setRemoteAsns(getAsnSpace((AristaBgpV4DynamicNeighbor) neighbor, peerFilters))
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
    if (v4Enabled
        && neighbor.getDefaultOriginate() != null
        && neighbor.getDefaultOriginate().getEnabled()) {
      // TODO: fix the export pipeline in VI so that setting the attribute policy is sufficient.
      //   Similarly, "new MatchProtocol(RoutingProtocol.AGGREGATE)" below should go away
      //   https://github.com/batfish/batfish/issues/5375

      // 1. Unconditionally generate a default route that is sent directly to this neighbor, without
      // going through the export policy.
      String defaultOriginateRouteMapName = neighbor.getDefaultOriginate().getRouteMap();
      GeneratedRoute defaultRoute =
          GeneratedRoute.builder()
              .setNetwork(Prefix.ZERO)
              .setAdmin(MAX_ADMINISTRATIVE_COST)
              .setAttributePolicy(defaultOriginateRouteMapName)
              .build();
      newNeighborBuilder.setGeneratedRoutes(ImmutableSet.of(defaultRoute));

      // 2. Do not export any other default route to this neighbor, since the generated route should
      // dominate.
      Builder<Statement> trueStatementsForGeneratedDefaultRoute = ImmutableList.builder();
      if (defaultOriginateRouteMapName != null
          && c.getRoutingPolicies().containsKey(defaultOriginateRouteMapName)) {
        trueStatementsForGeneratedDefaultRoute.add(new CallStatement(defaultOriginateRouteMapName));
      }
      trueStatementsForGeneratedDefaultRoute.add(Statements.ReturnTrue.toStaticStatement());
      exportStatements.add(
          new If(
              Common.matchDefaultRoute(), // we are exporting some default route
              ImmutableList.of(
                  new If(
                      new MatchProtocol(RoutingProtocol.AGGREGATE),
                      // default-originate (we generated it): call the routemap, etc.
                      trueStatementsForGeneratedDefaultRoute.build(),
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
                af4.getNextHopUnchanged(),
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
          Vrf vrfForVlan = getVrfForVlan(c, vlan).orElse(null);
          if (vrfForVlan == null) {
            continue;
          }
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
                  .setVrf(vrfForVlan.getName())
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
      newNeighborBuilder.setEvpnAddressFamily(evpnFamilyBuilder.build());
    }

    return newNeighborBuilder.build();
  }

  @Nonnull
  static Optional<Vrf> getVrfForVlan(Configuration c, int vlan) {
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

  private AristaConversions() {} // prevent instantiation of utility class.
}
