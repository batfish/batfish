package org.batfish.representation.cisco;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Collections.singletonList;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.IkePhase1Policy.PREFIX_ISAKMP_KEY;
import static org.batfish.datamodel.IkePhase1Policy.PREFIX_RSA_PUB;
import static org.batfish.datamodel.Interface.INVALID_LOCAL_INTERFACE;
import static org.batfish.datamodel.Interface.UNSET_LOCAL_INTERFACE;
import static org.batfish.datamodel.Names.generatedBgpCommonExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerExportPolicyName;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.ospf.OspfNetworkType.BROADCAST;
import static org.batfish.datamodel.ospf.OspfNetworkType.POINT_TO_POINT;
import static org.batfish.datamodel.routing_policy.Common.DEFAULT_UNDERSCORE_REPLACEMENT;
import static org.batfish.datamodel.routing_policy.Common.generateSuppressionPolicy;
import static org.batfish.datamodel.routing_policy.communities.CommunitySetExprs.toMatchExpr;
import static org.batfish.datamodel.tracking.TrackMethods.alwaysFalse;
import static org.batfish.datamodel.tracking.TrackMethods.alwaysTrue;
import static org.batfish.datamodel.tracking.TrackMethods.interfaceActive;
import static org.batfish.datamodel.tracking.TrackMethods.negatedReference;
import static org.batfish.datamodel.tracking.TrackMethods.reachability;
import static org.batfish.datamodel.tracking.TrackMethods.reference;
import static org.batfish.representation.cisco.CiscoConfiguration.DEFAULT_EBGP_ADMIN;
import static org.batfish.representation.cisco.CiscoConfiguration.computeBgpDefaultRouteExportPolicyName;
import static org.batfish.representation.cisco.CiscoConfiguration.computeBgpPeerImportPolicyName;
import static org.batfish.representation.cisco.CiscoConfiguration.computeIcmpObjectGroupAclName;
import static org.batfish.representation.cisco.CiscoConfiguration.computeProtocolObjectGroupAclName;
import static org.batfish.representation.cisco.CiscoConfiguration.computeServiceObjectAclName;
import static org.batfish.representation.cisco.CiscoConfiguration.computeServiceObjectGroupAclName;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.BgpVrfLeakConfig;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IkeAuthenticationMethod;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpsecDynamicPeerConfig;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.VrfLeakConfig;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.bgp.BgpAggregate;
import org.batfish.datamodel.bgp.community.ExtendedCommunity;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.eigrp.EigrpMetricVersion;
import org.batfish.datamodel.isis.IsisLevelSettings;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.routing_policy.Common;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.ColonSeparatedRendering;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetAclLine;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunitySetUnion;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.InputCommunities;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.MatchCommunities;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.LiteralEigrpMetric;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProcessAsn;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetEigrpMetric;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.tracking.DecrementPriority;
import org.batfish.datamodel.tracking.TrackAction;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.visitors.HeaderSpaceConverter;
import org.batfish.representation.cisco.DistributeList.DistributeListFilterType;
import org.batfish.vendor.VendorStructureId;

/** Utilities that convert Cisco-specific representations to vendor-independent model. */
@ParametersAreNonnullByDefault
public class CiscoConversions {

  @VisibleForTesting public static int DEFAULT_HSRP_DECREMENT = 10;
  @VisibleForTesting public static int DEFAULT_VRRP_DECREMENT = 10;

  // Defaults from
  // https://www.cisco.com/c/en/us/support/docs/ip/open-shortest-path-first-ospf/13689-17.html
  static int DEFAULT_OSPF_HELLO_INTERVAL_P2P_AND_BROADCAST = 10;

  static int DEFAULT_OSPF_HELLO_INTERVAL = 30;

  // Default dead interval is hello interval times 4
  static int OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER = 4;

  static int DEFAULT_OSPF_DEAD_INTERVAL_P2P_AND_BROADCAST =
      OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * DEFAULT_OSPF_HELLO_INTERVAL_P2P_AND_BROADCAST;

  static int DEFAULT_OSPF_DEAD_INTERVAL =
      OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * DEFAULT_OSPF_HELLO_INTERVAL;

  static Ip getHighestIp(Map<String, Interface> allInterfaces) {
    Map<String, Interface> interfacesToCheck;
    Map<String, Interface> loopbackInterfaces = new HashMap<>();
    for (Entry<String, Interface> e : allInterfaces.entrySet()) {
      String ifaceName = e.getKey();
      Interface iface = e.getValue();
      if (ifaceName.toLowerCase().startsWith("loopback")
          && iface.getActive()
          && iface.getAddress() != null) {
        loopbackInterfaces.put(ifaceName, iface);
      }
    }
    if (loopbackInterfaces.isEmpty()) {
      interfacesToCheck = allInterfaces;
    } else {
      interfacesToCheck = loopbackInterfaces;
    }
    Ip highestIp = Ip.ZERO;
    for (Interface iface : interfacesToCheck.values()) {
      if (!iface.getActive()) {
        continue;
      }
      for (ConcreteInterfaceAddress address : iface.getAllAddresses()) {
        Ip ip = address.getIp();
        if (highestIp.asLong() < ip.asLong()) {
          highestIp = ip;
        }
      }
    }
    return highestIp;
  }

  static @Nonnull BgpAggregate toBgpAggregate(
      BgpAggregateIpv4Network vsAggregate, Configuration c) {
    // TODO: handle as-set
    // TODO: handle suppress-map
    // TODO: verify undefined route-map can be treated as omitted
    String attributeMap =
        Optional.ofNullable(vsAggregate.getAttributeMap())
            .filter(c.getRoutingPolicies()::containsKey)
            .orElse(null);
    return BgpAggregate.of(
        vsAggregate.getPrefix(),
        generateSuppressionPolicy(vsAggregate.getSummaryOnly(), c),
        // TODO: put advertise-map here
        null,
        attributeMap);
  }

  /**
   * Converts a {@link CryptoMapEntry} to an {@link IpsecPhase2Policy} and a list of {@link
   * IpsecPeerConfig}
   */
  private static void convertCryptoMapEntry(
      Configuration c,
      CryptoMapEntry cryptoMapEntry,
      String cryptoMapNameSeqNumber,
      String cryptoMapName,
      Warnings w) {
    // skipping incomplete static or dynamic crypto maps
    if (!cryptoMapEntry.getDynamic()) {
      if (cryptoMapEntry.getAccessList() == null || cryptoMapEntry.getPeer() == null) {
        return;
      }
    } else {
      if (cryptoMapEntry.getAccessList() == null) {
        return;
      }
    }

    IpsecPhase2Policy ipsecPhase2Policy = toIpsecPhase2Policy(cryptoMapEntry);
    String ipsecPhase2PolicyName =
        String.format("~IPSEC_PHASE2_POLICY:%s~", cryptoMapNameSeqNumber);

    // add IPSec phase 2 policies to existing ones
    ImmutableSortedMap.Builder<String, IpsecPhase2Policy> ipsecPhase2PolicyBuilder =
        ImmutableSortedMap.naturalOrder();
    c.setIpsecPhase2Policies(
        ipsecPhase2PolicyBuilder
            .putAll(c.getIpsecPhase2Policies())
            .put(ipsecPhase2PolicyName, ipsecPhase2Policy)
            .build());

    ImmutableSortedMap.Builder<String, IpsecPeerConfig> ipsecPeerConfigsBuilder =
        ImmutableSortedMap.naturalOrder();
    c.setIpsecPeerConfigs(
        ipsecPeerConfigsBuilder
            .putAll(c.getIpsecPeerConfigs())
            .putAll(
                toIpsecPeerConfigs(
                    c,
                    cryptoMapEntry,
                    cryptoMapNameSeqNumber,
                    cryptoMapName,
                    ipsecPhase2PolicyName,
                    w))
            .build());
  }

  /**
   * Converts each crypto map entry in all crypto map sets to {@link IpsecPhase2Policy} and {@link
   * IpsecPeerConfig}s
   */
  static void convertCryptoMapSet(
      Configuration c,
      CryptoMapSet ciscoCryptoMapSet,
      Map<String, CryptoMapSet> cryptoMapSets,
      Warnings w) {
    if (ciscoCryptoMapSet.getDynamic()) {
      return;
    }
    for (CryptoMapEntry cryptoMapEntry : ciscoCryptoMapSet.getCryptoMapEntries()) {
      String nameSeqNum =
          String.format("%s:%s", cryptoMapEntry.getName(), cryptoMapEntry.getSequenceNumber());
      if (cryptoMapEntry.getReferredDynamicMapSet() != null) {
        CryptoMapSet dynamicCryptoMapSet =
            cryptoMapSets.get(cryptoMapEntry.getReferredDynamicMapSet());
        if (dynamicCryptoMapSet != null && dynamicCryptoMapSet.getDynamic()) {
          // convert all entries of the referred dynamic crypto map
          dynamicCryptoMapSet
              .getCryptoMapEntries()
              .forEach(
                  cryptoMap ->
                      convertCryptoMapEntry(
                          c,
                          cryptoMap,
                          String.format("%s:%s", nameSeqNum, cryptoMap.getSequenceNumber()),
                          cryptoMapEntry.getName(),
                          w));
        }
      } else {
        convertCryptoMapEntry(c, cryptoMapEntry, nameSeqNum, cryptoMapEntry.getName(), w);
      }
    }
  }

  private static final Statement ROUTE_MAP_DENY_STATEMENT =
      new If(
          BooleanExprs.CALL_EXPR_CONTEXT,
          ImmutableList.of(Statements.ReturnFalse.toStaticStatement()),
          ImmutableList.of(Statements.ExitReject.toStaticStatement()));

  /**
   * Implements the IOS behavior for undefined route-maps when used in BGP import/export policies.
   *
   * <p>Always returns {@code null} when given a null {@code mapName}, and non-null otherwise.
   */
  private static @Nullable String routeMapOrRejectAll(@Nullable String mapName, Configuration c) {
    if (mapName == null || c.getRoutingPolicies().containsKey(mapName)) {
      return mapName;
    }
    String undefinedName = mapName + "~undefined";
    if (!c.getRoutingPolicies().containsKey(undefinedName)) {
      // For undefined route-map, generate a route-map that denies everything.
      RoutingPolicy.builder()
          .setName(undefinedName)
          .addStatement(ROUTE_MAP_DENY_STATEMENT)
          .setOwner(c)
          .build();
    }
    return undefinedName;
  }

  /**
   * Returns the name of a {@link RoutingPolicy} to be used as the BGP import policy for the given
   * {@link LeafBgpPeerGroup}, or {@code null} if no constraints are imposed on the peer's inbound
   * routes. When a nonnull policy name is returned, the corresponding policy is guaranteed to exist
   * in the given configuration's routing policies.
   */
  static @Nullable String generateBgpImportPolicy(
      LeafBgpPeerGroup lpg, String vrfName, Configuration c, Warnings w) {
    // TODO Support filter-list
    // https://www.cisco.com/c/en/us/support/docs/ip/border-gateway-protocol-bgp/5816-bgpfaq-5816.html

    String inboundRouteMapName = lpg.getInboundRouteMap();
    String inboundPrefixListName = lpg.getInboundPrefixList();
    String inboundIpAccessListName = lpg.getInboundIpAccessList();

    // TODO Support using multiple filters in BGP import policies
    if (Stream.of(inboundRouteMapName, inboundPrefixListName, inboundIpAccessListName)
            .filter(Objects::nonNull)
            .count()
        > 1) {
      w.redFlag(
          "Batfish does not support configuring more than one filter"
              + " (route-map/prefix-list/distribute-list) for incoming BGP routes. When this"
              + " occurs, only the route-map will be used, or the prefix-list if no route-map is"
              + " configured.");
    }

    // Warnings for references to undefined route-maps and prefix-lists will be surfaced elsewhere.
    if (inboundRouteMapName != null) {
      // Inbound route-map is defined. Use that as the BGP import policy.
      return routeMapOrRejectAll(inboundRouteMapName, c);
    }

    String exportRouteFilter = null;
    if (inboundPrefixListName != null
        && c.getRouteFilterLists().containsKey(inboundPrefixListName)) {
      exportRouteFilter = inboundPrefixListName;
    } else if (inboundIpAccessListName != null
        && c.getRouteFilterLists().containsKey(inboundIpAccessListName)) {
      exportRouteFilter = inboundIpAccessListName;
    }

    if (exportRouteFilter != null) {
      // Inbound prefix-list or distribute-list is defined. Build an import policy around it.
      String generatedImportPolicyName = computeBgpPeerImportPolicyName(vrfName, lpg.getName());
      RoutingPolicy.builder()
          .setOwner(c)
          .setName(generatedImportPolicyName)
          .addStatement(
              new If(
                  new MatchPrefixSet(
                      DestinationNetwork.instance(), new NamedPrefixSet(exportRouteFilter)),
                  ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                  ImmutableList.of(Statements.ExitReject.toStaticStatement())))
          .build();
      return generatedImportPolicyName;
    }
    // Return null to indicate no constraints were imposed on inbound BGP routes.
    return null;
  }

  /**
   * Creates a {@link RoutingPolicy} to be used as the BGP export policy for the given {@link
   * LeafBgpPeerGroup}. The generated policy is added to the given configuration's routing policies.
   */
  static void generateBgpExportPolicy(
      LeafBgpPeerGroup lpg, String vrfName, Configuration c, Warnings w) {
    List<Statement> exportPolicyStatements = new ArrayList<>();
    if (lpg.getNextHopSelf() != null && lpg.getNextHopSelf()) {
      exportPolicyStatements.add(new SetNextHop(SelfNextHop.getInstance()));
    }
    if (lpg.getRemovePrivateAs() != null && lpg.getRemovePrivateAs()) {
      exportPolicyStatements.add(Statements.RemovePrivateAs.toStaticStatement());
    }

    // If defaultOriginate is set, generate a default route export policy. Default route will match
    // this policy and get exported without going through the rest of the export policy.
    // TODO Verify that nextHopSelf and removePrivateAs settings apply to default-originate route.
    if (lpg.getDefaultOriginate()) {
      initBgpDefaultRouteExportPolicy(vrfName, lpg.getName(), c);
      exportPolicyStatements.add(
          new If(
              "Export default route from peer with default-originate configured",
              new CallExpr(computeBgpDefaultRouteExportPolicyName(vrfName, lpg.getName())),
              singletonList(Statements.ReturnTrue.toStaticStatement()),
              ImmutableList.of()));
    }

    // Conditions for exporting regular routes (not spawned by default-originate)
    List<BooleanExpr> peerExportConjuncts = new ArrayList<>();
    peerExportConjuncts.add(new CallExpr(generatedBgpCommonExportPolicyName(vrfName)));

    // Add constraints on export routes from configured outbound filter.
    // TODO support configuring multiple outbound filters
    String outboundPrefixListName = lpg.getOutboundPrefixList();
    String outboundRouteMapName = lpg.getOutboundRouteMap();
    String outboundIpAccessListName = lpg.getOutboundIpAccessList();
    if (Stream.of(outboundRouteMapName, outboundPrefixListName, outboundIpAccessListName)
            .filter(Objects::nonNull)
            .count()
        > 1) {
      w.redFlag(
          "Batfish does not support configuring more than one filter"
              + " (route-map/prefix-list/distribute-list) for outgoing BGP routes. When this"
              + " occurs, only the route-map will be used, or the prefix-list if no route-map is"
              + " configured.");
    }
    if (outboundRouteMapName != null) {
      peerExportConjuncts.add(new CallExpr(routeMapOrRejectAll(outboundRouteMapName, c)));
    } else if (outboundPrefixListName != null
        && c.getRouteFilterLists().containsKey(outboundPrefixListName)) {
      peerExportConjuncts.add(
          new MatchPrefixSet(
              DestinationNetwork.instance(), new NamedPrefixSet(outboundPrefixListName)));
    } else if (outboundIpAccessListName != null
        && c.getRouteFilterLists().containsKey(outboundIpAccessListName)) {
      peerExportConjuncts.add(
          new MatchPrefixSet(
              DestinationNetwork.instance(), new NamedPrefixSet(outboundIpAccessListName)));
    }
    exportPolicyStatements.add(
        new If(
            "peer-export policy main conditional: exitAccept if true / exitReject if false",
            new Conjunction(peerExportConjuncts),
            ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
            ImmutableList.of(Statements.ExitReject.toStaticStatement())));
    RoutingPolicy.builder()
        .setOwner(c)
        .setName(generatedBgpPeerExportPolicyName(vrfName, lpg.getName()))
        .setStatements(exportPolicyStatements)
        .build();
  }

  /**
   * Initializes export policy for IPv4 default routes if it doesn't already exist. This policy is
   * the same across BGP processes, so only one is created for each configuration.
   */
  static void initBgpDefaultRouteExportPolicy(String vrfName, String peerName, Configuration c) {
    SetOrigin setOrigin =
        new SetOrigin(
            new LiteralOrigin(
                c.getConfigurationFormat() == ConfigurationFormat.CISCO_IOS
                    ? OriginType.IGP
                    : OriginType.INCOMPLETE,
                null));
    List<Statement> defaultRouteExportStatements;
    defaultRouteExportStatements =
        ImmutableList.of(setOrigin, Statements.ReturnTrue.toStaticStatement());

    RoutingPolicy.builder()
        .setOwner(c)
        .setName(computeBgpDefaultRouteExportPolicyName(vrfName, peerName))
        .addStatement(
            new If(
                new Conjunction(
                    ImmutableList.of(
                        Common.matchDefaultRoute(), new MatchProtocol(RoutingProtocol.AGGREGATE))),
                defaultRouteExportStatements))
        .addStatement(Statements.ReturnFalse.toStaticStatement())
        .build();
  }

  /**
   * Computes a mapping of primary {@link Ip}s to the names of interfaces owning them. Filters out
   * the interfaces having no primary {@link ConcreteInterfaceAddress}
   */
  private static Map<Ip, String> computeIpToIfaceNameMap(Map<String, Interface> interfaces) {
    Map<Ip, String> ipToIfaceNameMap = new HashMap<>();
    for (Entry<String, Interface> interfaceNameToInterface : interfaces.entrySet()) {
      interfaceNameToInterface
          .getValue()
          .getAllAddresses()
          .forEach(
              interfaceAddress -> {
                ipToIfaceNameMap.put(interfaceAddress.getIp(), interfaceNameToInterface.getKey());
              });
    }
    return ipToIfaceNameMap;
  }

  /** Resolves the interface names of the addresses used as local addresses of {@link Keyring} */
  static void resolveKeyringIfaceNames(
      Map<String, Interface> interfaces, Map<String, Keyring> keyrings) {
    Map<Ip, String> iptoIfaceName = computeIpToIfaceNameMap(interfaces);

    // setting empty string as interface name if cannot find the IP
    keyrings.values().stream()
        .filter(keyring -> keyring.getLocalAddress() != null)
        .forEach(
            keyring ->
                keyring.setLocalInterfaceName(
                    firstNonNull(
                        iptoIfaceName.get(keyring.getLocalAddress()), INVALID_LOCAL_INTERFACE)));
  }

  /**
   * Resolves the interface names of the addresses used as local addresses of {@link IsakmpProfile}
   */
  static void resolveIsakmpProfileIfaceNames(
      Map<String, Interface> interfaces, Map<String, IsakmpProfile> isakpProfiles) {
    Map<Ip, String> iptoIfaceName = computeIpToIfaceNameMap(interfaces);

    isakpProfiles.values().stream()
        .filter(isakmpProfile -> isakmpProfile.getLocalAddress() != null)
        .forEach(
            isakmpProfile ->
                isakmpProfile.setLocalInterfaceName(
                    firstNonNull(
                        iptoIfaceName.get(isakmpProfile.getLocalAddress()),
                        INVALID_LOCAL_INTERFACE)));
  }

  /** Resolves the interface names of the addresses used as source addresses in {@link Tunnel}s */
  static void resolveTunnelIfaceNames(Map<String, Interface> interfaces) {
    Map<Ip, String> iptoIfaceName = computeIpToIfaceNameMap(interfaces);

    for (Interface iface : interfaces.values()) {
      Tunnel tunnel = iface.getTunnel();
      // resolve if tunnel's source interface name is not set
      if (tunnel != null
          && UNSET_LOCAL_INTERFACE.equals(tunnel.getSourceInterfaceName())
          && tunnel.getSourceAddress() != null) {
        tunnel.setSourceInterfaceName(
            firstNonNull(iptoIfaceName.get(tunnel.getSourceAddress()), INVALID_LOCAL_INTERFACE));
      }
    }
  }

  static AsPathAccessList toAsPathAccessList(IpAsPathAccessList pathList) {
    List<AsPathAccessListLine> lines =
        pathList.getLines().stream()
            .map(IpAsPathAccessListLine::toAsPathAccessListLine)
            .collect(ImmutableList.toImmutableList());
    return new AsPathAccessList(pathList.getName(), lines);
  }

  static @Nonnull CommunityMatchRegex toCommunityMatchRegex(String regex) {
    return new CommunityMatchRegex(ColonSeparatedRendering.instance(), toJavaRegex(regex));
  }

  static @Nonnull CommunitySetAclLine toCommunitySetAclLine(ExpandedCommunityListLine line) {
    return new CommunitySetAclLine(line.getAction(), toMatchExpr(toJavaRegex(line.getRegex())));
  }

  static String toJavaRegex(String ciscoRegex) {
    String withoutQuotes;
    if (ciscoRegex.charAt(0) == '"' && ciscoRegex.charAt(ciscoRegex.length() - 1) == '"') {
      withoutQuotes = ciscoRegex.substring(1, ciscoRegex.length() - 1);
    } else {
      withoutQuotes = ciscoRegex;
    }
    String output = withoutQuotes.replaceAll("_", DEFAULT_UNDERSCORE_REPLACEMENT);
    return output;
  }

  static org.batfish.datamodel.hsrp.HsrpGroup toHsrpGroup(
      HsrpGroup hsrpGroup,
      Set<Integer> trackMethodIds,
      @Nullable ConcreteInterfaceAddress sourceAddress,
      Configuration c) {
    Ip groupIp = hsrpGroup.getIp();
    // HSRP track uses negated value of referenced TrackMethod
    SortedMap<String, TrackAction> trackActions =
        hsrpGroup.getTrackActions().entrySet().stream()
            .filter(
                actionByTrackMethodId -> trackMethodIds.contains(actionByTrackMethodId.getKey()))
            .collect(
                ImmutableSortedMap.toImmutableSortedMap(
                    Comparator.naturalOrder(),
                    actionByTrackMethodId ->
                        createNegatedTrackMethodIfNeededAndReturnName(
                            actionByTrackMethodId.getKey(), c),
                    actionByTrackMethodId -> toTrackAction(actionByTrackMethodId.getValue())));

    return org.batfish.datamodel.hsrp.HsrpGroup.builder()
        .setAuthentication(hsrpGroup.getAuthentication())
        .setHelloTime(hsrpGroup.getHelloTime())
        .setHoldTime(hsrpGroup.getHoldTime())
        .setVirtualAddresses(groupIp == null ? ImmutableSet.of() : ImmutableSet.of(groupIp))
        .setSourceAddress(sourceAddress)
        .setPreempt(hsrpGroup.getPreempt())
        .setPriority(hsrpGroup.getPriority())
        .setTrackActions(trackActions)
        .build();
  }

  private static @Nonnull String createNegatedTrackMethodIfNeededAndReturnName(
      int trackNum, Configuration c) {
    String referencedName = Integer.toString(trackNum);
    String name = Names.generatedNegatedTrackMethodId(referencedName);
    c.getTrackingGroups().computeIfAbsent(name, n -> negatedReference(referencedName));
    return name;
  }

  private static @Nonnull TrackAction toTrackAction(HsrpTrackAction hsrpTrackAction) {
    // skip visitor overhead since there are only two types
    if (hsrpTrackAction instanceof HsrpDecrementPriority) {
      int decrement =
          firstNonNull(
              ((HsrpDecrementPriority) hsrpTrackAction).getDecrement(), DEFAULT_HSRP_DECREMENT);
      return new DecrementPriority(decrement);
    } else {
      assert hsrpTrackAction instanceof HsrpShutdown;
      // TODO: Support non-participation as an action.
      //       For now, just use min priority.
      return new DecrementPriority(255);
    }
  }

  static IkePhase1Key toIkePhase1Key(Keyring keyring) {
    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash(keyring.getKey());
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);
    ikePhase1Key.setLocalInterface(keyring.getLocalInterfaceName());
    if (keyring.getRemoteIdentity() != null) {
      ikePhase1Key.setRemoteIdentity(keyring.getRemoteIdentity().toIpSpace());
    }
    return ikePhase1Key;
  }

  static IkePhase1Key toIkePhase1Key(@Nonnull NamedRsaPubKey rsaPubKey) {
    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash(rsaPubKey.getKey());
    ikePhase1Key.setKeyType(IkeKeyType.RSA_PUB_KEY);
    if (rsaPubKey.getAddress() != null) {
      ikePhase1Key.setRemoteIdentity(rsaPubKey.getAddress().toIpSpace());
    }
    return ikePhase1Key;
  }

  static IkePhase1Key toIkePhase1Key(@Nonnull IsakmpKey isakmpKey) {
    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyHash(isakmpKey.getKey());
    ikePhase1Key.setKeyType(isakmpKey.getIkeKeyType());
    ikePhase1Key.setRemoteIdentity(isakmpKey.getAddress());
    return ikePhase1Key;
  }

  static IkePhase1Policy toIkePhase1Policy(
      @Nonnull NamedRsaPubKey rsaPubKey,
      @Nonnull CiscoConfiguration oldConfig,
      @Nonnull IkePhase1Key ikePhase1KeyFromRsaPubKey) {
    IkePhase1Policy ikePhase1Policy = new IkePhase1Policy(getRsaPubKeyGeneratedName(rsaPubKey));

    ikePhase1Policy.setIkePhase1Proposals(
        oldConfig.getIsakmpPolicies().values().stream()
            .filter(
                isakmpPolicy ->
                    isakmpPolicy.getAuthenticationMethod()
                        == IkeAuthenticationMethod.RSA_ENCRYPTED_NONCES)
            .map(isakmpPolicy -> isakmpPolicy.getName().toString())
            .collect(ImmutableList.toImmutableList()));
    if (rsaPubKey.getAddress() != null) {
      ikePhase1Policy.setRemoteIdentity(rsaPubKey.getAddress().toIpSpace());
    }
    ikePhase1Policy.setIkePhase1Key(ikePhase1KeyFromRsaPubKey);
    // RSA pub key is not per interface so unsetting local interface
    ikePhase1Policy.setLocalInterface(UNSET_LOCAL_INTERFACE);
    return ikePhase1Policy;
  }

  static IkePhase1Policy toIkePhase1Policy(
      @Nonnull IsakmpKey isakmpKey,
      @Nonnull CiscoConfiguration oldConfig,
      @Nonnull IkePhase1Key ikePhase1KeyFromIsakmpKey) {
    IkePhase1Policy ikePhase1Policy = new IkePhase1Policy(getIsakmpKeyGeneratedName(isakmpKey));

    ikePhase1Policy.setIkePhase1Proposals(
        oldConfig.getIsakmpPolicies().values().stream()
            .filter(
                isakmpPolicy ->
                    isakmpPolicy.getAuthenticationMethod()
                        == IkeAuthenticationMethod.PRE_SHARED_KEYS)
            .map(isakmpPolicy -> isakmpPolicy.getName().toString())
            .collect(ImmutableList.toImmutableList()));
    ikePhase1Policy.setRemoteIdentity(isakmpKey.getAddress());

    ikePhase1Policy.setIkePhase1Key(ikePhase1KeyFromIsakmpKey);
    // ISAKMP key is not per interface so local interface will not be set
    ikePhase1Policy.setLocalInterface(UNSET_LOCAL_INTERFACE);
    return ikePhase1Policy;
  }

  static String getRsaPubKeyGeneratedName(NamedRsaPubKey namedRsaPubKey) {
    return String.format("~%s_%s~", PREFIX_RSA_PUB, namedRsaPubKey.getName());
  }

  static String getIsakmpKeyGeneratedName(IsakmpKey isakmpKey) {
    return String.format("~%s_%s~", PREFIX_ISAKMP_KEY, isakmpKey.getAddress());
  }

  static IkePhase1Policy toIkePhase1Policy(
      IsakmpProfile isakmpProfile, CiscoConfiguration oldConfig, Configuration config, Warnings w) {
    IkePhase1Policy ikePhase1Policy = new IkePhase1Policy(isakmpProfile.getName());

    ImmutableList.Builder<String> ikePhase1ProposalBuilder = ImmutableList.builder();
    for (Entry<Integer, IsakmpPolicy> entry : oldConfig.getIsakmpPolicies().entrySet()) {
      ikePhase1ProposalBuilder.add(entry.getKey().toString());
    }

    ikePhase1Policy.setIkePhase1Proposals(ikePhase1ProposalBuilder.build());
    ikePhase1Policy.setSelfIdentity(isakmpProfile.getSelfIdentity());
    if (isakmpProfile.getMatchIdentity() != null) {
      ikePhase1Policy.setRemoteIdentity(isakmpProfile.getMatchIdentity().toIpSpace());
    }

    ikePhase1Policy.setLocalInterface(isakmpProfile.getLocalInterfaceName());
    ikePhase1Policy.setIkePhase1Key(getMatchingPsk(isakmpProfile, w, config.getIkePhase1Keys()));
    return ikePhase1Policy;
  }

  /**
   * Gets the {@link IkePhase1Key} that can be used for the given {@link IsakmpProfile} based on
   * {@code remoteIdentity} and {@code localInterfaceName} present in the {@link IkePhase1Key}
   */
  static IkePhase1Key getMatchingPsk(
      IsakmpProfile isakmpProfile, Warnings w, Map<String, IkePhase1Key> ikePhase1Keys) {
    IkePhase1Key ikePhase1Key = null;
    String isakmpProfileName = isakmpProfile.getName();
    if (isakmpProfile.getLocalInterfaceName().equals(INVALID_LOCAL_INTERFACE)) {
      w.redFlagf(
          "Invalid local address interface configured for ISAKMP profile %s", isakmpProfileName);
    } else if (isakmpProfile.getKeyring() == null) {
      w.redFlagf("Keyring not set for ISAKMP profile %s", isakmpProfileName);
    } else if (!ikePhase1Keys.containsKey(isakmpProfile.getKeyring())) {
      w.redFlagf(
          "Cannot find keyring %s for ISAKMP profile %s",
          isakmpProfile.getKeyring(), isakmpProfileName);
    } else {
      IkePhase1Key tempIkePhase1Key = ikePhase1Keys.get(isakmpProfile.getKeyring());
      if (tempIkePhase1Key.getLocalInterface().equals(INVALID_LOCAL_INTERFACE)) {
        w.redFlagf(
            "Invalid local address interface configured for keyring %s",
            isakmpProfile.getKeyring());
      } else if (tempIkePhase1Key.match(
          isakmpProfile.getLocalInterfaceName(), isakmpProfile.getMatchIdentity())) {
        // found a matching keyring
        ikePhase1Key = tempIkePhase1Key;
      }
    }
    return ikePhase1Key;
  }

  static IkePhase1Proposal toIkePhase1Proposal(IsakmpPolicy isakmpPolicy) {
    IkePhase1Proposal ikePhase1Proposal = new IkePhase1Proposal(isakmpPolicy.getName().toString());
    ikePhase1Proposal.setDiffieHellmanGroup(isakmpPolicy.getDiffieHellmanGroup());
    ikePhase1Proposal.setAuthenticationMethod(isakmpPolicy.getAuthenticationMethod());
    ikePhase1Proposal.setEncryptionAlgorithm(isakmpPolicy.getEncryptionAlgorithm());
    ikePhase1Proposal.setLifetimeSeconds(isakmpPolicy.getLifetimeSeconds());
    ikePhase1Proposal.setHashingAlgorithm(isakmpPolicy.getHashAlgorithm());
    return ikePhase1Proposal;
  }

  static IpAccessList toIpAccessList(
      ExtendedAccessList eaList, Map<String, ObjectGroup> objectGroups, String filename) {
    String aclName = eaList.getName();
    boolean isStandard = eaList.getParent() != null;
    CiscoStructureType lineType =
        isStandard
            ? CiscoStructureType.IPV4_ACCESS_LIST_STANDARD_LINE
            : CiscoStructureType.IPV4_ACCESS_LIST_EXTENDED_LINE;
    List<AclLine> lines =
        eaList.getLines().stream()
            .map(
                l ->
                    toIpAccessListLine(l, objectGroups)
                        .setVendorStructureId(
                            new VendorStructureId(
                                filename,
                                lineType.getDescription(),
                                aclLineStructureName(aclName, l.getName())))
                        .build())
            .collect(ImmutableList.toImmutableList());
    String sourceType =
        isStandard
            ? CiscoStructureType.IPV4_ACCESS_LIST_STANDARD.getDescription()
            : CiscoStructureType.IPV4_ACCESS_LIST_EXTENDED.getDescription();
    return IpAccessList.builder()
        .setName(aclName)
        .setLines(lines)
        .setSourceName(aclName)
        .setSourceType(sourceType)
        .build();
  }

  static IpAccessList toIpAccessList(IcmpTypeObjectGroup icmpTypeObjectGroup) {
    return IpAccessList.builder()
        .setLines(
            ImmutableList.of(
                ExprAclLine.accepting()
                    .setMatchCondition(icmpTypeObjectGroup.toAclLineMatchExpr())
                    .build()))
        .setName(computeIcmpObjectGroupAclName(icmpTypeObjectGroup.getName()))
        .setSourceName(icmpTypeObjectGroup.getName())
        .setSourceType(CiscoStructureType.ICMP_TYPE_OBJECT_GROUP.getDescription())
        .build();
  }

  static IpAccessList toIpAccessList(ProtocolObjectGroup protocolObjectGroup) {
    return IpAccessList.builder()
        .setLines(
            ImmutableList.of(
                ExprAclLine.accepting()
                    .setMatchCondition(protocolObjectGroup.toAclLineMatchExpr())
                    .build()))
        .setName(computeProtocolObjectGroupAclName(protocolObjectGroup.getName()))
        .setSourceName(protocolObjectGroup.getName())
        .setSourceType(CiscoStructureType.PROTOCOL_OBJECT_GROUP.getDescription())
        .build();
  }

  static IpAccessList toIpAccessList(
      ServiceObject serviceObject,
      Map<String, ServiceObject> serviceObjects,
      Map<String, ServiceObjectGroup> serviceObjectGroups) {
    return IpAccessList.builder()
        .setLines(
            ImmutableList.of(
                ExprAclLine.accepting()
                    .setMatchCondition(
                        serviceObject.toAclLineMatchExpr(serviceObjects, serviceObjectGroups))
                    .build()))
        .setName(computeServiceObjectAclName(serviceObject.getName()))
        .setSourceName(serviceObject.getName())
        .setSourceType(CiscoStructureType.SERVICE_OBJECT.getDescription())
        .build();
  }

  static IpAccessList toIpAccessList(
      ServiceObjectGroup serviceObjectGroup,
      Map<String, ServiceObject> serviceObjects,
      Map<String, ServiceObjectGroup> serviceObjectGroups) {
    return IpAccessList.builder()
        .setLines(
            ImmutableList.of(
                ExprAclLine.accepting()
                    .setMatchCondition(
                        serviceObjectGroup.toAclLineMatchExpr(serviceObjects, serviceObjectGroups))
                    .build()))
        .setName(computeServiceObjectGroupAclName(serviceObjectGroup.getName()))
        .setSourceName(serviceObjectGroup.getName())
        .setSourceType(CiscoStructureType.SERVICE_OBJECT_GROUP.getDescription())
        .build();
  }

  static IpSpace toIpSpace(NetworkObjectGroup networkObjectGroup) {
    return firstNonNull(AclIpSpace.union(networkObjectGroup.getLines()), EmptyIpSpace.INSTANCE);
  }

  /**
   * Converts a {@link Tunnel} to an {@link IpsecPeerConfig}, or empty optional if it can't be
   * converted
   */
  static Optional<IpsecPeerConfig> toIpsecPeerConfig(
      Tunnel tunnel,
      String tunnelIfaceName,
      CiscoConfiguration oldConfig,
      Configuration newConfig,
      Warnings w) {
    Ip localAddress = tunnel.getSourceAddress();
    if (localAddress == null || !localAddress.valid()) {
      w.redFlagf(
          "Cannot create IPsec peer on tunnel %s: cannot determine tunnel source address",
          tunnelIfaceName);
      return Optional.empty();
    }

    IpsecStaticPeerConfig.Builder ipsecStaticPeerConfigBuilder =
        IpsecStaticPeerConfig.builder()
            .setTunnelInterface(tunnelIfaceName)
            .setDestinationAddress(tunnel.getDestination())
            .setLocalAddress(localAddress)
            .setSourceInterface(tunnel.getSourceInterfaceName())
            .setIpsecPolicy(tunnel.getIpsecProfileName());

    IpsecProfile ipsecProfile = null;
    if (tunnel.getIpsecProfileName() != null) {
      ipsecProfile = oldConfig.getIpsecProfiles().get(tunnel.getIpsecProfileName());
    }

    if (ipsecProfile != null && ipsecProfile.getIsakmpProfile() != null) {
      ipsecStaticPeerConfigBuilder.setIkePhase1Policy(ipsecProfile.getIsakmpProfile());
    } else if (tunnel.getDestination() != null) {
      // Try to infer policy from destination IP and source interface name.
      ipsecStaticPeerConfigBuilder.setIkePhase1Policy(
          getIkePhase1Policy(
              newConfig.getIkePhase1Policies(),
              tunnel.getDestination(),
              tunnel.getSourceInterfaceName()));
    }

    return Optional.of(ipsecStaticPeerConfigBuilder.build());
  }

  /**
   * Converts a {@link CryptoMapEntry} to multiple {@link IpsecPeerConfig}(one per interface on
   * which crypto map is referred)
   */
  private static Map<String, IpsecPeerConfig> toIpsecPeerConfigs(
      Configuration c,
      CryptoMapEntry cryptoMapEntry,
      String cryptoMapNameSeqNumber,
      String cryptoMapName,
      String ipsecPhase2Policy,
      Warnings w) {

    List<org.batfish.datamodel.Interface> referencingInterfaces =
        c.getAllInterfaces().values().stream()
            .filter(iface -> Objects.equals(iface.getCryptoMap(), cryptoMapName))
            .collect(Collectors.toList());

    ImmutableSortedMap.Builder<String, IpsecPeerConfig> ipsecPeerConfigsBuilder =
        ImmutableSortedMap.naturalOrder();

    for (org.batfish.datamodel.Interface iface : referencingInterfaces) {
      // skipping interfaces with no ip-address
      if (iface.getConcreteAddress() == null) {
        w.redFlagf(
            "Interface %s with declared crypto-map %s has no ip-address",
            iface.getName(), cryptoMapName);
        continue;
      }
      // add one IPSec peer config per interface for the crypto map entry
      String peerName =
          String.format("~IPSEC_PEER_CONFIG:%s_%s~", cryptoMapNameSeqNumber, iface.getName());
      toIpsecPeerConfig(c, cryptoMapEntry, iface, ipsecPhase2Policy, w)
          .ifPresent(config -> ipsecPeerConfigsBuilder.put(peerName, config));
    }
    return ipsecPeerConfigsBuilder.build();
  }

  private static Optional<IpsecPeerConfig> toIpsecPeerConfig(
      Configuration c,
      CryptoMapEntry cryptoMapEntry,
      org.batfish.datamodel.Interface iface,
      String ipsecPhase2Policy,
      Warnings w) {
    Ip localAddress =
        Optional.ofNullable(iface.getConcreteAddress())
            .map(ConcreteInterfaceAddress::getIp)
            .orElse(null);
    if (localAddress == null || !localAddress.valid()) {
      w.redFlagf(
          "Cannot create IPsec peer on interface %s: no valid interface IP", iface.getName());
      return Optional.empty();
    }

    IpsecPeerConfig.Builder<?, ?> newIpsecPeerConfigBuilder;

    String ikePhase1Policy = cryptoMapEntry.getIsakmpProfile();

    // static crypto maps
    if (cryptoMapEntry.getPeer() != null) {
      if (ikePhase1Policy == null) {
        ikePhase1Policy =
            getIkePhase1Policy(c.getIkePhase1Policies(), cryptoMapEntry.getPeer(), iface.getName());
      }
      newIpsecPeerConfigBuilder =
          IpsecStaticPeerConfig.builder()
              .setDestinationAddress(cryptoMapEntry.getPeer())
              .setIkePhase1Policy(ikePhase1Policy);
    } else {
      // dynamic crypto maps
      List<String> ikePhase1Policies;
      if (ikePhase1Policy != null) {
        ikePhase1Policies = ImmutableList.of(ikePhase1Policy);
      } else {
        ikePhase1Policies = getMatchingIKePhase1Policies(c.getIkePhase1Policies(), iface.getName());
      }

      newIpsecPeerConfigBuilder =
          IpsecDynamicPeerConfig.builder().setIkePhase1Policies(ikePhase1Policies);
    }

    newIpsecPeerConfigBuilder
        .setSourceInterface(iface.getName())
        .setIpsecPolicy(ipsecPhase2Policy)
        .setLocalAddress(localAddress);

    setIpsecPeerConfigPolicyAccessList(c, cryptoMapEntry, newIpsecPeerConfigBuilder, w);

    return Optional.of(newIpsecPeerConfigBuilder.build());
  }

  private static void setIpsecPeerConfigPolicyAccessList(
      Configuration c,
      CryptoMapEntry cryptoMapEntry,
      IpsecPeerConfig.Builder<?, ?> ipsecPeerConfigBuilder,
      Warnings w) {
    if (cryptoMapEntry.getAccessList() != null) {
      IpAccessList cryptoAcl = c.getIpAccessLists().get(cryptoMapEntry.getAccessList());
      if (cryptoAcl != null) {
        IpAccessList symmetricCryptoAcl = createAclWithSymmetricalLines(cryptoAcl);
        if (symmetricCryptoAcl != null) {
          ipsecPeerConfigBuilder.setPolicyAccessList(symmetricCryptoAcl);
        } else {
          // log a warning if the ACL was not made symmetrical successfully
          w.redFlagf(
              "Cannot process the Access List for crypto map %s:%s",
              cryptoMapEntry.getName(), cryptoMapEntry.getSequenceNumber());
        }
      }
    }
  }

  /**
   * Returns a new symmetrical {@link IpAccessList} by adding mirror image {@link ExprAclLine}s to
   * the original {@link IpAccessList} or null if the conversion is not supported
   */
  @VisibleForTesting
  static @Nullable IpAccessList createAclWithSymmetricalLines(IpAccessList ipAccessList) {
    List<AclLine> aclLines = new ArrayList<>(ipAccessList.getLines());

    for (AclLine line : ipAccessList.getLines()) {
      // Does not support types of ACL line other than ExprAclLine
      if (!(line instanceof ExprAclLine)) {
        return null;
      }
      ExprAclLine exprAclLine = (ExprAclLine) line;
      HeaderSpace originalHeaderSpace =
          HeaderSpaceConverter.convert(exprAclLine.getMatchCondition());

      if (!originalHeaderSpace.equals(
          HeaderSpace.builder()
              .setSrcIps(originalHeaderSpace.getSrcIps())
              .setDstIps(originalHeaderSpace.getDstIps())
              .setSrcPorts(originalHeaderSpace.getSrcPorts())
              .setDstPorts(originalHeaderSpace.getDstPorts())
              .setIpProtocols(originalHeaderSpace.getIpProtocols())
              .setIcmpCodes(originalHeaderSpace.getIcmpCodes())
              .setTcpFlags(originalHeaderSpace.getTcpFlags())
              .build())) {
        //  not supported if the access list line contains any more fields
        return null;
      } else {
        HeaderSpace.Builder reversedHeaderSpaceBuilder = originalHeaderSpace.toBuilder();
        aclLines.add(
            ExprAclLine.builder()
                .setMatchCondition(
                    new MatchHeaderSpace(
                        reversedHeaderSpaceBuilder
                            .setSrcIps(originalHeaderSpace.getDstIps())
                            .setSrcPorts(originalHeaderSpace.getDstPorts())
                            .setDstIps(originalHeaderSpace.getSrcIps())
                            .setDstPorts(originalHeaderSpace.getSrcPorts())
                            .build()))
                .setAction(exprAclLine.getAction())
                .build());
      }
    }

    return IpAccessList.builder().setName(ipAccessList.getName()).setLines(aclLines).build();
  }

  /**
   * Returns the first {@link IkePhase1Policy} name matching {@code remoteAddress} and {@code
   * localInterface}, null is returned if no matching {@link IkePhase1Policy} could not be found
   */
  private static @Nullable String getIkePhase1Policy(
      Map<String, IkePhase1Policy> ikePhase1Policies, Ip remoteAddress, String localInterface) {
    for (Entry<String, IkePhase1Policy> e : ikePhase1Policies.entrySet()) {
      IkePhase1Policy ikePhase1Policy = e.getValue();
      String ikePhase1PolicyLocalInterface = ikePhase1Policy.getLocalInterface();
      IpSpace remoteAddressSpace =
          firstNonNull(ikePhase1Policy.getRemoteIdentity(), EmptyIpSpace.INSTANCE);
      if (remoteAddressSpace.containsIp(remoteAddress, ImmutableMap.of())
          && (UNSET_LOCAL_INTERFACE.equals(ikePhase1PolicyLocalInterface)
              || ikePhase1PolicyLocalInterface.equals(localInterface))) {
        return e.getKey();
      }
    }
    return null;
  }

  /** Returns all {@link IkePhase1Policy} names matching the {@code localInterface} */
  private static List<String> getMatchingIKePhase1Policies(
      Map<String, IkePhase1Policy> ikePhase1Policies, String localInterface) {
    List<String> filteredIkePhase1Policies = new ArrayList<>();
    for (Entry<String, IkePhase1Policy> e : ikePhase1Policies.entrySet()) {
      String ikePhase1PolicyLocalInterface = e.getValue().getLocalInterface();
      if ((UNSET_LOCAL_INTERFACE.equals(ikePhase1PolicyLocalInterface)
          || ikePhase1PolicyLocalInterface.equals(localInterface))) {
        filteredIkePhase1Policies.add(e.getKey());
      }
    }
    return filteredIkePhase1Policies;
  }

  static IpsecPhase2Proposal toIpsecPhase2Proposal(IpsecTransformSet ipsecTransformSet) {
    IpsecPhase2Proposal ipsecPhase2Proposal = new IpsecPhase2Proposal();
    ipsecPhase2Proposal.setAuthenticationAlgorithm(ipsecTransformSet.getAuthenticationAlgorithm());
    ipsecPhase2Proposal.setEncryptionAlgorithm(ipsecTransformSet.getEncryptionAlgorithm());
    ipsecPhase2Proposal.setProtocols(ipsecTransformSet.getProtocols());
    ipsecPhase2Proposal.setIpsecEncapsulationMode(ipsecTransformSet.getIpsecEncapsulationMode());

    return ipsecPhase2Proposal;
  }

  static IpsecPhase2Policy toIpsecPhase2Policy(IpsecProfile ipsecProfile) {
    IpsecPhase2Policy ipsecPhase2Policy = new IpsecPhase2Policy();
    ipsecPhase2Policy.setPfsKeyGroup(ipsecProfile.getPfsGroup());
    ipsecPhase2Policy.setProposals(ImmutableList.copyOf(ipsecProfile.getTransformSets()));

    return ipsecPhase2Policy;
  }

  static IpsecPhase2Policy toIpsecPhase2Policy(CryptoMapEntry cryptoMapEntry) {
    IpsecPhase2Policy ipsecPhase2Policy = new IpsecPhase2Policy();
    ipsecPhase2Policy.setProposals(ImmutableList.copyOf(cryptoMapEntry.getTransforms()));
    ipsecPhase2Policy.setPfsKeyGroup(cryptoMapEntry.getPfsKeyGroup());
    return ipsecPhase2Policy;
  }

  static @Nullable org.batfish.datamodel.eigrp.EigrpProcess toEigrpProcess(
      EigrpProcess proc, String vrfName, Configuration c, CiscoConfiguration oldConfig) {
    org.batfish.datamodel.eigrp.EigrpProcess.Builder newProcess =
        org.batfish.datamodel.eigrp.EigrpProcess.builder();

    if (proc.getAsn() == null) {
      oldConfig.getWarnings().redFlag("Invalid EIGRP process");
      return null;
    }

    if (firstNonNull(proc.getShutdown(), Boolean.FALSE)) {
      return null;
    }

    newProcess.setAsNumber(proc.getAsn());
    newProcess.setMode(proc.getMode());

    // TODO set stub process
    // newProcess.setStub(proc.isStub())

    // TODO create summary filters

    // TODO originate default route if configured

    Ip routerId = proc.getRouterId();
    if (routerId == null) {
      routerId = getHighestIp(oldConfig.getInterfaces());
      if (routerId == Ip.ZERO) {
        oldConfig
            .getWarnings()
            .redFlag("No candidates for EIGRP (AS " + proc.getAsn() + ") router-id");
        return null;
      }
    }
    newProcess.setRouterId(routerId).setMetricVersion(EigrpMetricVersion.V1);

    /*
     * Route redistribution modifies the configuration structure, so do this last to avoid having to
     * clean up configuration if another conversion step fails
     */
    String redistributionPolicyName = "~EIGRP_EXPORT_POLICY:" + vrfName + ":" + proc.getAsn() + "~";
    RoutingPolicy redistributionPolicy = new RoutingPolicy(redistributionPolicyName, c);
    c.getRoutingPolicies().put(redistributionPolicyName, redistributionPolicy);
    newProcess.setRedistributionPolicy(redistributionPolicyName);

    redistributionPolicy
        .getStatements()
        .addAll(
            eigrpRedistributionPoliciesToStatements(
                proc.getRedistributionPolicies().values(), proc, oldConfig));

    return newProcess.build();
  }

  /** Creates a {@link BooleanExpr} statement that matches EIGRP routes with a given ASN */
  static @Nonnull BooleanExpr matchOwnAsn(long localAsn) {
    return new Conjunction(
        ImmutableList.of(
            new MatchProtocol(RoutingProtocol.EIGRP, RoutingProtocol.EIGRP_EX),
            new MatchProcessAsn(localAsn)));
  }

  /**
   * Converts {@link EigrpRedistributionPolicy}s in an {@link EigrpProcess} to equivalent {@link If}
   * statements
   *
   * @param eigrpRedistributionPolicies {@link EigrpRedistributionPolicy}s of the EIGRP process
   * @param vsEigrpProc Vendor specific {@link EigrpProcess}
   * @param vsConfig Vendor specific {@link CiscoConfiguration configuration}
   * @return {@link List} of {@link If} statements
   */
  static List<If> eigrpRedistributionPoliciesToStatements(
      Collection<EigrpRedistributionPolicy> eigrpRedistributionPolicies,
      EigrpProcess vsEigrpProc,
      CiscoConfiguration vsConfig) {
    return eigrpRedistributionPolicies.stream()
        .map(policy -> convertEigrpRedistributionPolicy(policy, vsEigrpProc, vsConfig))
        .filter(Objects::nonNull)
        .collect(ImmutableList.toImmutableList());
  }

  private static @Nullable If convertEigrpRedistributionPolicy(
      EigrpRedistributionPolicy policy, EigrpProcess proc, CiscoConfiguration oldConfig) {
    RoutingProtocol protocol = policy.getInstance().getProtocol();
    // All redistribution must match the specified protocol.
    Conjunction eigrpExportConditions = new Conjunction();
    BooleanExpr matchExpr;
    if (protocol == RoutingProtocol.EIGRP) {
      matchExpr = new MatchProtocol(RoutingProtocol.EIGRP, RoutingProtocol.EIGRP_EX);

      Long otherAsn = Long.parseLong(policy.getInstance().getTag());
      if (otherAsn == null) {
        oldConfig
            .getWarnings()
            .redFlagf(
                "Unable to redistribute %s into EIGRP proc %s - policy has no ASN",
                protocol, proc.getAsn());
        return null;
      }
      eigrpExportConditions.getConjuncts().add(new MatchProcessAsn(otherAsn));
    } else if (protocol == RoutingProtocol.ISIS_ANY) {
      matchExpr =
          new MatchProtocol(
              RoutingProtocol.ISIS_EL1,
              RoutingProtocol.ISIS_EL2,
              RoutingProtocol.ISIS_L1,
              RoutingProtocol.ISIS_L2);
    } else {
      matchExpr = new MatchProtocol(protocol);
    }
    eigrpExportConditions.getConjuncts().add(matchExpr);

    // Default routes can be redistributed into EIGRP. Don't filter them.

    ImmutableList.Builder<Statement> eigrpExportStatements = ImmutableList.builder();

    // Set the metric
    // TODO prefer metric from route map
    // https://github.com/batfish/batfish/issues/2070
    EigrpMetricValues metric =
        policy.getMetric() != null
            ? policy.getMetric()
            : Optional.ofNullable(proc.getDefaultMetric()).map(EigrpMetric::getValues).orElse(null);
    if (metric != null) {
      eigrpExportStatements.add(new SetEigrpMetric(new LiteralEigrpMetric(metric)));
    } else if (protocol != RoutingProtocol.EIGRP) {
      /*
       * TODO no default metric (and not EIGRP into EIGRP)
       * 1) connected can use the interface metric
       * 2) static with next hop interface can use the interface metric
       * 3) If none of the above, bad configuration
       */
      oldConfig
          .getWarnings()
          .redFlagf(
              "Unable to redistribute %s into EIGRP proc %s - no metric", protocol, proc.getAsn());
      return null;
    }

    String exportRouteMapName = policy.getRouteMap();
    if (exportRouteMapName != null) {
      RouteMap exportRouteMap = oldConfig.getRouteMaps().get(exportRouteMapName);
      if (exportRouteMap != null) {
        eigrpExportConditions.getConjuncts().add(new CallExpr(exportRouteMapName));
      }
    }

    eigrpExportStatements.add(Statements.ExitAccept.toStaticStatement());

    // Construct a new policy and add it before returning.
    return new If(
        "EIGRP export routes for " + protocol.protocolName(),
        eigrpExportConditions,
        eigrpExportStatements.build(),
        ImmutableList.of());
  }

  static org.batfish.datamodel.isis.IsisProcess toIsisProcess(
      IsisProcess proc, Configuration c, CiscoConfiguration oldConfig) {
    org.batfish.datamodel.isis.IsisProcess.Builder newProcess =
        org.batfish.datamodel.isis.IsisProcess.builder();
    if (proc.getNetAddress() == null) {
      oldConfig.getWarnings().redFlag("Cannot create IS-IS process without specifying net-address");
      return null;
    }
    newProcess.setNetAddress(proc.getNetAddress());
    IsisLevelSettings settings = IsisLevelSettings.builder().build();
    switch (proc.getLevel()) {
      case LEVEL_1 -> newProcess.setLevel1(settings);
      case LEVEL_2 -> newProcess.setLevel2(settings);
      case LEVEL_1_2 -> {
        newProcess.setLevel1(settings);
        newProcess.setLevel2(settings);
      }
    }
    return newProcess.build();
  }

  static RouteFilterList toRouteFilterList(ExtendedAccessList eaList, String vendorConfigFilename) {
    List<RouteFilterLine> lines =
        eaList.getLines().stream()
            .map(CiscoConversions::toRouteFilterLine)
            .collect(ImmutableList.toImmutableList());
    return new RouteFilterList(
        eaList.getName(),
        lines,
        new VendorStructureId(
            vendorConfigFilename,
            CiscoStructureType.IPV4_ACCESS_LIST_EXTENDED.getDescription(),
            eaList.getName()));
  }

  static RouteFilterList toRouteFilterList(StandardAccessList saList, String vendorConfigFilename) {
    List<RouteFilterLine> lines =
        saList.getLines().stream()
            .map(CiscoConversions::toRouteFilterLine)
            .collect(ImmutableList.toImmutableList());
    return new RouteFilterList(
        saList.getName(),
        lines,
        new VendorStructureId(
            vendorConfigFilename,
            CiscoStructureType.IPV4_ACCESS_LIST_STANDARD.getDescription(),
            saList.getName()));
  }

  static RouteFilterList toRouteFilterList(PrefixList list, String vendorConfigFilename) {
    List<RouteFilterLine> newLines =
        list.getLines().values().stream()
            .map(
                l ->
                    new RouteFilterLine(
                        l.getAction(), IpWildcard.create(l.getPrefix()), l.getLengthRange()))
            .collect(ImmutableList.toImmutableList());
    return new RouteFilterList(
        list.getName(),
        newLines,
        new VendorStructureId(
            vendorConfigFilename, CiscoStructureType.PREFIX_LIST.getDescription(), list.getName()));
  }

  @VisibleForTesting
  static boolean sanityCheckDistributeList(
      @Nonnull DistributeList distributeList,
      @Nonnull Configuration c,
      @Nonnull CiscoConfiguration oldConfig,
      String vrfName,
      String ospfProcessId) {
    if (distributeList.getFilterType() != DistributeListFilterType.PREFIX_LIST) {
      // only prefix-lists are supported in distribute-list
      oldConfig
          .getWarnings()
          .redFlagf(
              "OSPF process %s:%s in %s uses distribute-list of type %s, only prefix-lists are"
                  + " supported in dist-lists by Batfish",
              vrfName, ospfProcessId, oldConfig.getHostname(), distributeList.getFilterType());
      return false;
    } else if (!c.getRouteFilterLists().containsKey(distributeList.getFilterName())) {
      // if referred prefix-list is not defined, all prefixes will be allowed
      oldConfig
          .getWarnings()
          .redFlagf(
              "dist-list in OSPF process %s:%s uses a prefix-list which is not defined, this"
                  + " dist-list will allow everything",
              vrfName, ospfProcessId);
      return false;
    }
    return true;
  }

  /**
   * Populates the {@link RoutingPolicy}s for inbound {@link DistributeList}s which use {@link
   * PrefixList} as the {@link DistributeList#getFilterType()}. {@link
   * DistributeListFilterType#ROUTE_MAP} and {@link DistributeListFilterType#ACCESS_LIST} are not
   * supported currently.
   *
   * @param ospfProcess {@link OspfProcess} for which {@link DistributeList}s are to be processed
   * @param c {@link Configuration} containing the Vendor Independent representation
   * @param vrf Id of the {@link Vrf} containing the {@link OspfProcess}
   * @param ospfProcessId {@link OspfProcess}'s Id
   */
  static void computeDistributeListPolicies(
      @Nonnull OspfProcess ospfProcess,
      @Nonnull org.batfish.datamodel.ospf.OspfProcess newOspfProcess,
      @Nonnull Configuration c,
      @Nonnull String vrf,
      @Nonnull String ospfProcessId,
      @Nonnull CiscoConfiguration oldConfig,
      @Nonnull Warnings w) {
    DistributeList globalDistributeList = ospfProcess.getInboundGlobalDistributeList();

    BooleanExpr globalCondition = null;
    if (globalDistributeList != null
        && sanityCheckDistributeList(globalDistributeList, c, oldConfig, vrf, ospfProcessId)) {
      globalCondition =
          new MatchPrefixSet(
              DestinationNetwork.instance(),
              new NamedPrefixSet(globalDistributeList.getFilterName()));
    }

    Map<String, DistributeList> interfaceDistributeLists =
        ospfProcess.getInboundInterfaceDistributeLists();

    for (String ifaceName :
        newOspfProcess.getAreas().values().stream()
            .flatMap(a -> a.getInterfaces().stream())
            .collect(Collectors.toList())) {
      org.batfish.datamodel.Interface iface = c.getAllInterfaces(vrf).get(ifaceName);
      DistributeList ifaceDistributeList = interfaceDistributeLists.get(ifaceName);
      BooleanExpr ifaceCondition = null;
      if (ifaceDistributeList != null
          && sanityCheckDistributeList(ifaceDistributeList, c, oldConfig, vrf, ospfProcessId)) {
        ifaceCondition =
            new MatchPrefixSet(
                DestinationNetwork.instance(),
                new NamedPrefixSet(ifaceDistributeList.getFilterName()));
      }

      if (globalCondition == null && ifaceCondition == null) {
        // doing nothing if both global and interface conditions are empty
        continue;
      }

      String policyName = String.format("~OSPF_DIST_LIST_%s_%s_%s~", vrf, ospfProcessId, ifaceName);
      RoutingPolicy routingPolicy = new RoutingPolicy(policyName, c);
      routingPolicy
          .getStatements()
          .add(
              new If(
                  new Conjunction(
                      Stream.of(globalCondition, ifaceCondition)
                          .filter(Objects::nonNull)
                          .collect(ImmutableList.toImmutableList())),
                  ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                  ImmutableList.of(Statements.ExitReject.toStaticStatement())));
      c.getRoutingPolicies().put(routingPolicy.getName(), routingPolicy);
      OspfInterfaceSettings ospfSettings = iface.getOspfSettings();
      if (ospfSettings == null) {
        w.redFlagf(
            "Cannot attach inbound distribute list policy '%s' to interface '%s' not"
                + " configured for OSPF.",
            ifaceName, iface.getName());
      } else {
        ospfSettings.setInboundDistributeListPolicy(policyName);
      }
    }
  }

  /**
   * Generate an EIGRP policy from the provided {@param distributeLists} and any additional {@param
   * extraConditions} that must be true for the policy to permit the route
   *
   * <p>Note that the list of distribute lists is allowed to have {@code null} elements (those will
   * be skipped). Invalid (e.g., non-existent) distribute lists will be skipped as well.
   */
  static RoutingPolicy generateEigrpPolicy(
      @Nonnull Configuration c,
      @Nonnull CiscoConfiguration vsConfig,
      @Nonnull List<DistributeList> distributeLists,
      @Nonnull List<BooleanExpr> extraConditions,
      @Nonnull String name) {
    ImmutableList.Builder<BooleanExpr> matchesBuilder = ImmutableList.builder();
    for (DistributeList distributeList : distributeLists) {
      if (distributeList == null || !sanityCheckEigrpDistributeList(c, distributeList, vsConfig)) {
        continue;
      }
      String filterName = distributeList.getFilterName();
      if (distributeList.getFilterType() == DistributeListFilterType.ROUTE_MAP) {
        matchesBuilder.add(new CallExpr(filterName));
      } else {
        // prefix-list or ACL
        matchesBuilder.add(
            new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(filterName)));
      }
    }
    matchesBuilder.addAll(extraConditions);

    return RoutingPolicy.builder()
        .setOwner(c)
        .setName(name)
        .setStatements(
            ImmutableList.of(
                new If(
                    new Conjunction(matchesBuilder.build()),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement()))))
        .build();
  }

  /**
   * Checks if the {@link DistributeList distributeList} can be converted to a routing policy.
   * Returns false if it refers to an extended access list, which is not supported and also returns
   * false if the access-list referred by it does not exist.
   *
   * <p>Adds appropriate {@link org.batfish.common.Warning} if the {@link DistributeList
   * distributeList} is not found to be valid for conversion to routing policy.
   *
   * @param c Vendor independent {@link Configuration configuration}
   * @param distributeList {@link DistributeList distributeList} to be validated
   * @param vsConfig Vendor specific {@link CiscoConfiguration configuration}
   * @return false if the {@link DistributeList distributeList} cannot be converted to a routing
   *     policy
   */
  static boolean sanityCheckEigrpDistributeList(
      @Nonnull Configuration c,
      @Nonnull DistributeList distributeList,
      @Nonnull CiscoConfiguration vsConfig) {
    if (distributeList.getFilterType() == DistributeListFilterType.ACCESS_LIST
        && vsConfig.getExtendedAcls().containsKey(distributeList.getFilterName())) {
      vsConfig
          .getWarnings()
          .redFlagf(
              "Extended access lists are not supported in EIGRP distribute-lists: %s",
              distributeList.getFilterName());
      return false;
    } else {
      if (distributeList.getFilterType() == DistributeListFilterType.ROUTE_MAP) {
        if (!c.getRoutingPolicies().containsKey(distributeList.getFilterName())) {
          // if referred route-map is not defined, all prefixes will be allowed
          vsConfig
              .getWarnings()
              .redFlagf(
                  "distribute-list refers an undefined route-map `%s`, it will not filter"
                      + " anything",
                  distributeList.getFilterName());
          return false;
        }
      } else {
        if (!c.getRouteFilterLists().containsKey(distributeList.getFilterName())) {
          // if referred access-list is not defined, all prefixes will be allowed
          vsConfig
              .getWarnings()
              .redFlagf(
                  "distribute-list refers an undefined access-list `%s`, it will not filter"
                      + " anything",
                  distributeList.getFilterName());
          return false;
        }
      }
    }
    return true;
  }

  static org.batfish.datamodel.StaticRoute toStaticRoute(
      StaticRoute staticRoute, Predicate<Integer> trackExists) {
    String nextHopInterface = staticRoute.getNextHopInterface();
    if (nextHopInterface != null && nextHopInterface.toLowerCase().startsWith("null")) {
      nextHopInterface = org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
    }
    String track =
        Optional.ofNullable(staticRoute.getTrack())
            .filter(trackExists)
            .map(Object::toString)
            .orElse(null);
    return org.batfish.datamodel.StaticRoute.builder()
        .setNetwork(staticRoute.getPrefix())
        .setNextHop(NextHop.legacyConverter(nextHopInterface, staticRoute.getNextHopIp()))
        .setAdministrativeCost(staticRoute.getDistance())
        .setTag(firstNonNull(staticRoute.getTag(), -1L))
        .setTrack(track)
        .build();
  }

  private static ExprAclLine.Builder toIpAccessListLine(
      ExtendedAccessListLine line, Map<String, ObjectGroup> objectGroups) {
    IpSpace srcIpSpace = line.getSourceAddressSpecifier().toIpSpace();
    IpSpace dstIpSpace = line.getDestinationAddressSpecifier().toIpSpace();
    AclLineMatchExpr matchService = line.getServiceSpecifier().toAclLineMatchExpr(objectGroups);
    AclLineMatchExpr match;
    if (matchService instanceof MatchHeaderSpace) {
      match =
          new MatchHeaderSpace(
              ((MatchHeaderSpace) matchService)
                  .getHeaderspace().toBuilder()
                      .setSrcIps(srcIpSpace)
                      .setDstIps(dstIpSpace)
                      .build());
    } else {
      match = and(matchService, matchSrc(srcIpSpace), matchDst(dstIpSpace));
    }

    return ExprAclLine.builder()
        .setAction(line.getAction())
        .setMatchCondition(match)
        .setName(line.getName());
  }

  private static RouteFilterLine toRouteFilterLine(ExtendedAccessListLine fromLine) {
    LineAction action = fromLine.getAction();
    IpWildcard srcIpWildcard =
        ((WildcardAddressSpecifier) fromLine.getSourceAddressSpecifier()).getIpWildcard();
    Ip ip = srcIpWildcard.getIp();
    IpWildcard dstIpWildcard =
        ((WildcardAddressSpecifier) fromLine.getDestinationAddressSpecifier()).getIpWildcard();
    long minSubnet = dstIpWildcard.getIp().asLong();
    long maxSubnet = minSubnet | dstIpWildcard.getWildcardMask();
    int minPrefixLength = dstIpWildcard.getIp().numSubnetBits();
    int maxPrefixLength = Ip.create(maxSubnet).numSubnetBits();
    int statedPrefixLength = srcIpWildcard.getWildcardMaskAsIp().inverted().numSubnetBits();
    int prefixLength = Math.min(statedPrefixLength, minPrefixLength);
    Prefix prefix = Prefix.create(ip, prefixLength);
    return new RouteFilterLine(
        action, IpWildcard.create(prefix), new SubRange(minPrefixLength, maxPrefixLength));
  }

  /** Convert a standard access list line to a route filter list line */
  private static RouteFilterLine toRouteFilterLine(StandardAccessListLine fromLine) {
    LineAction action = fromLine.getAction();
    /*
     * This cast is safe since the other address specifier (network object group specifier)
     * can be used only from extended ACLs.
     */
    IpWildcard srcIpWildcard =
        ((WildcardAddressSpecifier) fromLine.getSrcAddressSpecifier()).getIpWildcard();

    // A standard ACL is simply a wildcard on the network address, and does not filter on the
    // prefix length at all (beyond the prefix length implied by the unmasked bits in wildcard).
    return new RouteFilterLine(action, srcIpWildcard, new SubRange(0, Prefix.MAX_PREFIX_LENGTH));
  }

  /**
   * Helper to infer dead interval from configured OSPF settings on an interface. Check explicitly
   * set dead interval, infer from hello interval, or infer from OSPF network type, in that order.
   * See https://www.cisco.com/c/en/us/support/docs/ip/open-shortest-path-first-ospf/13689-17.html
   * for more details.
   */
  @VisibleForTesting
  static int toOspfDeadInterval(
      Interface iface, @Nullable org.batfish.datamodel.ospf.OspfNetworkType networkType) {
    Integer deadInterval = iface.getOspfDeadInterval();
    if (deadInterval != null) {
      return deadInterval;
    }
    Integer helloInterval = iface.getOspfHelloInterval();
    if (helloInterval != null) {
      return OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * helloInterval;
    }
    if (networkType == POINT_TO_POINT || networkType == BROADCAST) {
      return DEFAULT_OSPF_DEAD_INTERVAL_P2P_AND_BROADCAST;
    }
    return DEFAULT_OSPF_DEAD_INTERVAL;
  }

  /**
   * Helper to infer hello interval from configured OSPF settings on an interface. Check explicitly
   * set hello interval or infer from OSPF network type, in that order. See
   * https://www.cisco.com/c/en/us/support/docs/ip/open-shortest-path-first-ospf/13689-17.html for
   * more details.
   */
  @VisibleForTesting
  static int toOspfHelloInterval(
      Interface iface, @Nullable org.batfish.datamodel.ospf.OspfNetworkType networkType) {
    Integer helloInterval = iface.getOspfHelloInterval();
    if (helloInterval != null) {
      return helloInterval;
    }
    if (networkType == POINT_TO_POINT || networkType == BROADCAST) {
      return DEFAULT_OSPF_HELLO_INTERVAL_P2P_AND_BROADCAST;
    }
    return DEFAULT_OSPF_HELLO_INTERVAL;
  }

  /** Helper to convert Cisco VS OSPF network type to VI model type. */
  @VisibleForTesting
  static @Nullable org.batfish.datamodel.ospf.OspfNetworkType toOspfNetworkType(
      @Nullable OspfNetworkType type, Warnings warnings) {
    if (type == null) {
      // default is broadcast for all Ethernet interfaces
      // (https://learningnetwork.cisco.com/thread/66827)
      return org.batfish.datamodel.ospf.OspfNetworkType.BROADCAST;
    }
    switch (type) {
      case BROADCAST:
        return org.batfish.datamodel.ospf.OspfNetworkType.BROADCAST;
      case POINT_TO_POINT:
        return org.batfish.datamodel.ospf.OspfNetworkType.POINT_TO_POINT;
      case NON_BROADCAST:
        return org.batfish.datamodel.ospf.OspfNetworkType.NON_BROADCAST_MULTI_ACCESS;
      case POINT_TO_MULTIPOINT:
        return org.batfish.datamodel.ospf.OspfNetworkType.POINT_TO_MULTIPOINT;
      default:
        warnings.redFlagf(
            "Conversion of Cisco OSPF network type '%s' is not handled.", type.toString());
        return null;
    }
  }

  /**
   * Convert VRF leaking configs, if needed. Must be called after VRF address family inheritance is
   * completed, and routing policies have been converted.
   *
   * @param c VI {@link Configuration}
   */
  public static void convertVrfLeakingConfig(Collection<Vrf> vrfs, Configuration c) {
    List<Vrf> vrfsWithIpv4Af =
        vrfs.stream()
            .filter(v -> v.getIpv4UnicastAddressFamily() != null)
            .collect(Collectors.toList());
    List<Vrf> vrfsWithoutExportMap =
        vrfsWithIpv4Af.stream()
            .filter(v -> v.getIpv4UnicastAddressFamily().getExportMap() == null)
            .collect(ImmutableList.toImmutableList());
    List<Vrf> vrfsWithExportMap =
        vrfsWithIpv4Af.stream()
            .filter(v -> v.getIpv4UnicastAddressFamily().getExportMap() != null)
            .collect(ImmutableList.toImmutableList());
    Multimap<ExtendedCommunity, String> vrfsByExportRt = HashMultimap.create();
    // pre-compute RT to VRF name mapping
    for (Vrf vrf : vrfsWithoutExportMap) {
      assert vrf.getIpv4UnicastAddressFamily() != null;
      vrf.getIpv4UnicastAddressFamily()
          .getRouteTargetExport()
          .forEach(rt -> vrfsByExportRt.put(rt, vrf.getName()));
    }

    // Create VRF leaking configs for each importing VRF that has import RTs defined.
    for (Vrf importingVrf : vrfsWithIpv4Af) {
      VrfAddressFamily ipv4uaf = importingVrf.getIpv4UnicastAddressFamily();
      assert ipv4uaf != null;
      // TODO: should instead attach all export RTs in single config per compatible exporter
      for (ExtendedCommunity importRt : ipv4uaf.getRouteTargetImport()) {
        org.batfish.datamodel.Vrf viVrf = c.getVrfs().get(importingVrf.getName());
        assert viVrf != null;
        // Add leak config for every exporting vrf with no export map whose export route-target
        // matches this vrf's import route-target
        for (String exportingVrf : vrfsByExportRt.get(importRt)) {
          // Take care to prevent self-loops
          if (importingVrf.getName().equals(exportingVrf)) {
            continue;
          }
          getOrInitVrfLeakConfig(viVrf)
              .addBgpVrfLeakConfig(
                  BgpVrfLeakConfig.builder()
                      .setImportFromVrf(exportingVrf)
                      .setImportPolicy(routeMapOrRejectAll(ipv4uaf.getImportMap(), c))
                      // TODO: input and honor result of 'bgp distance' command argument 1 (eBGP
                      // admin)
                      .setAdmin(DEFAULT_EBGP_ADMIN)
                      // TODO: this should be export RTs, not single import RT
                      .setAttachRouteTargets(importRt)
                      .setWeight(BGP_VRF_LEAK_IGP_WEIGHT)
                      .build());
        }
        // Add leak config for every exporting vrf with an export map, since the map can potentially
        // alter the route-target to match the import route-target.
        for (Vrf mapExportingVrf : vrfsWithExportMap) {
          if (importingVrf == mapExportingVrf) {
            // Take care to prevent self-loops
            continue;
          }
          getOrInitVrfLeakConfig(viVrf)
              .addBgpVrfLeakConfig(
                  BgpVrfLeakConfig.builder()
                      .setImportFromVrf(mapExportingVrf.getName())
                      .setImportPolicy(
                          vrfExportImportPolicy(
                              mapExportingVrf.getName(),
                              routeMapOrRejectAll(
                                  mapExportingVrf.getIpv4UnicastAddressFamily().getExportMap(), c),
                              mapExportingVrf.getIpv4UnicastAddressFamily().getRouteTargetExport(),
                              importingVrf.getName(),
                              routeMapOrRejectAll(ipv4uaf.getImportMap(), c),
                              ipv4uaf.getRouteTargetImport(),
                              c))
                      // TODO: input and honor result of 'bgp distance' command argument 1 (eBGP
                      // admin)
                      .setAdmin(DEFAULT_EBGP_ADMIN)
                      // TODO: this should be export RTs, not single import RT
                      .setAttachRouteTargets(importRt)
                      .setWeight(BGP_VRF_LEAK_IGP_WEIGHT)
                      .build());
        }
      }
    }
  }

  private static @Nonnull VrfLeakConfig getOrInitVrfLeakConfig(org.batfish.datamodel.Vrf vrf) {
    if (vrf.getVrfLeakConfig() == null) {
      vrf.setVrfLeakConfig(new VrfLeakConfig(true));
    }
    return vrf.getVrfLeakConfig();
  }

  @VisibleForTesting public static final int BGP_VRF_LEAK_IGP_WEIGHT = 32768;

  /** Create a policy for exporting from one vrf to another in the presence of an export map. */
  private static @Nonnull String vrfExportImportPolicy(
      String exportingVrf,
      String exportMap,
      Set<ExtendedCommunity> routeTargetExport,
      String importingVrf,
      @Nullable String importMap,
      Set<ExtendedCommunity> routeTargetImport,
      Configuration c) {
    // Implementation overview:
    // 1. (Re)write the export route-target to intermediate BGP properties so that they can be read
    //    later.
    // 2. Apply the export-map if it exists. This may change properties of the route, but it may not
    //    reject the route. If the export-map rejects, then it should not modify the route.
    //    TODO: verify and enforce lack of side effects when export map rejects
    // 3. Drop the route if does not have a route-target matching the importing VRF's import
    //    route-target communities.
    // 4. Apply the import route-map if it exists. This route-map may permit with or without further
    //    modification, or may reject the route.
    String policyName = computeVrfExportImportPolicyName(exportingVrf, importingVrf);
    if (c.getRoutingPolicies().containsKey(policyName)) {
      return policyName;
    }
    Statement addExportRt =
        new SetCommunities(
            CommunitySetUnion.of(
                InputCommunities.instance(),
                new LiteralCommunitySet(CommunitySet.of(routeTargetExport))));
    Statement tryApplyExportMap =
        new If(new CallExpr(exportMap), ImmutableList.of(), ImmutableList.of());
    Statement filterImportRt =
        new If(
            new MatchCommunities(
                InputCommunities.instance(),
                CommunitySetMatchAny.matchAny(
                    routeTargetImport.stream()
                        .map(CommunityIs::new)
                        .map(HasCommunity::new)
                        .collect(ImmutableList.toImmutableList()))),
            ImmutableList.of(),
            ImmutableList.of(Statements.ExitReject.toStaticStatement()));
    Statement applyImportMap =
        importMap != null
            ? new If(
                new CallExpr(importMap),
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))
            : Statements.ReturnTrue.toStaticStatement();
    // TODO: prevent side-effects from a route-map continue that eventually rejects in export map
    RoutingPolicy.builder()
        .setName(policyName)
        .addStatement(Statements.SetWriteIntermediateBgpAttributes.toStaticStatement())
        .addStatement(addExportRt)
        .addStatement(tryApplyExportMap)
        .addStatement(Statements.SetReadIntermediateBgpAttributes.toStaticStatement())
        .addStatement(filterImportRt)
        .addStatement(applyImportMap)
        .setOwner(c)
        .build();
    return policyName;
  }

  @VisibleForTesting
  public static @Nonnull String computeVrfExportImportPolicyName(
      String exportingVrf, String importingVrf) {
    return String.format("~vrfExportImport~%s~%s", exportingVrf, importingVrf);
  }

  /**
   * The structure name of an ACL line for definition/reference tracking. All lines in a config are
   * in the same namespace, so we have to qualify them with the name of the ACL.
   */
  public static @Nonnull String aclLineStructureName(String aclName, String lineName) {
    return String.format("%s: %s", aclName, lineName);
  }

  static void convertIpSlas(Map<Integer, IpSla> slas, Configuration c) {
    IpSlaConverter converter = new IpSlaConverter(c);
    slas.forEach(
        (slaNum, sla) -> {
          String name = generatedIpSlaTrackMethodName(slaNum);
          TrackMethod method;
          if (!(sla.getLivesForever() && sla.getStartsEventually())) {
            // We assume "infinite" time has passed since the configuration was applied.
            // So either the sla expired, or it never started.
            method = alwaysFalse();
          } else {
            method = converter.visit(sla);
          }
          c.getTrackingGroups().put(name, method);
        });
  }

  private static class IpSlaConverter implements IpSlaVisitor<TrackMethod> {

    @Override
    public TrackMethod visitIcmpEchoSla(IcmpEchoSla icmpEchoSla) {
      Ip sourceIp = getSourceIp(icmpEchoSla.getSourceIp(), icmpEchoSla.getSourceInterface());
      if (sourceIp == null && icmpEchoSla.getSourceInterface() != null) {
        // the interface was invalid
        return alwaysFalse();
      }
      Ip destinationIp = icmpEchoSla.getDestinationIp();
      if (destinationIp == null) {
        // because they specified a hostname to be resolved through DNS
        return alwaysTrue();
      }
      String vrf = firstNonNull(icmpEchoSla.getVrf(), DEFAULT_VRF_NAME);
      return sourceIp != null
          ? reachability(destinationIp, vrf, sourceIp)
          : reachability(destinationIp, vrf);
    }

    private @Nullable Ip getSourceIp(@Nullable Ip sourceIp, @Nullable String sourceInterface) {
      if (sourceIp != null) {
        return sourceIp;
      }
      if (sourceInterface == null) {
        return null;
      }
      // interface addresses should have been converted already
      // TODO: does it matter if the interface is active?
      return Optional.ofNullable(_c.getAllInterfaces().get(sourceInterface))
          .map(org.batfish.datamodel.Interface::getConcreteAddress)
          .map(ConcreteInterfaceAddress::getIp)
          .orElse(null);
    }

    private final @Nonnull Configuration _c;

    private IpSlaConverter(Configuration c) {
      _c = c;
    }
  }

  /** Convert {@link Track}s to {@link TrackMethod}s. */
  static void convertTracks(
      Map<Integer, Track> tracks,
      Predicate<Integer> slaExists,
      Predicate<String> interfaceExists,
      Configuration c) {
    TrackConverter converter =
        new TrackConverter(tracks.keySet()::contains, slaExists, interfaceExists, c);
    tracks.forEach(
        (num, track) -> c.getTrackingGroups().put(Integer.toString(num), converter.visit(track)));
  }

  /** Converts a {@link Track} to a {@link TrackMethod}. */
  private static class TrackConverter implements TrackVisitor<TrackMethod> {

    private TrackConverter(
        Predicate<Integer> trackExists,
        Predicate<Integer> slaExists,
        Predicate<String> interfaceExists,
        Configuration c) {
      _trackExists = trackExists;
      _slaExists = slaExists;
      _interfaceExists = interfaceExists;
      _c = c;
    }

    private final @Nonnull Predicate<String> _interfaceExists;

    private final @Nonnull Predicate<Integer> _slaExists;

    @SuppressWarnings("unused")
    private final @Nonnull Predicate<Integer> _trackExists;

    private final @Nonnull Configuration _c;

    @Override
    public TrackMethod visitTrackInterface(TrackInterface trackInterface) {
      if (!_interfaceExists.test(trackInterface.getInterfaceName())) {
        return alwaysFalse();
      }
      if (trackInterface.getIpRouting()) {
        // TODO: at least support also checking for existence of an IP address
        return interfaceActive(trackInterface.getInterfaceName());
      } else {
        return interfaceActive(trackInterface.getInterfaceName());
      }
    }

    @Override
    public TrackMethod visitTrackIpSla(TrackIpSla trackIpSla) {
      if (!_slaExists.test(trackIpSla.getIpSla())) {
        return alwaysFalse();
      }
      if (!trackIpSla.getReachability()) {
        // Type is 'track ip sla state'
        // Unsupported, just assume it works like reachability.
        // TODO: something else?
        return reference(createTrackIpSlaStateIfNeededAndReturnName(trackIpSla.getIpSla(), _c));
      } else {
        return reference(
            createTrackIpSlaReachabilityIfNeededAndReturnName(trackIpSla.getIpSla(), _c));
      }
    }

    private @Nonnull String createTrackIpSlaStateIfNeededAndReturnName(int ipSla, Configuration c) {
      String name = generatedTrackIpSlaStateMethodName(ipSla);
      c.getTrackingGroups()
          .computeIfAbsent(name, n -> reference(generatedIpSlaTrackMethodName(ipSla)));
      return name;
    }

    private @Nonnull String createTrackIpSlaReachabilityIfNeededAndReturnName(
        int ipSla, Configuration c) {
      String name = generatedTrackIpSlaReachabilityMethodName(ipSla);
      c.getTrackingGroups()
          .computeIfAbsent(name, n -> reference(generatedIpSlaTrackMethodName(ipSla)));
      return name;
    }
  }

  private static @Nonnull String generatedIpSlaTrackMethodName(int sla) {
    return String.format("ip sla %d", sla);
  }

  private static @Nonnull String generatedTrackIpSlaReachabilityMethodName(int sla) {
    return String.format("ip sla %d reachability", sla);
  }

  private static @Nonnull String generatedTrackIpSlaStateMethodName(int sla) {
    return String.format("ip sla %d state", sla);
  }

  private CiscoConversions() {} // prevent instantiation of utility class
}
