package org.batfish.representation.cisco_asa;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.common.util.CollectionUtil.toImmutableSortedMap;
import static org.batfish.datamodel.Interface.UNSET_LOCAL_INTERFACE;
import static org.batfish.datamodel.Interface.computeInterfaceType;
import static org.batfish.datamodel.Interface.isRealInterfaceName;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.EXACT_PATH;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.PATH_LENGTH;
import static org.batfish.datamodel.Names.generatedBgpRedistributionPolicyName;
import static org.batfish.datamodel.Names.generatedNegatedTrackMethodId;
import static org.batfish.datamodel.Names.generatedOspfDefaultRouteGenerationPolicyName;
import static org.batfish.datamodel.Names.generatedOspfExportPolicyName;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.datamodel.bgp.AllowRemoteAsOutMode.ALWAYS;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.batfish.datamodel.routing_policy.Common.DEFAULT_UNDERSCORE_REPLACEMENT;
import static org.batfish.datamodel.routing_policy.Common.suppressSummarizedPrefixes;
import static org.batfish.datamodel.tracking.TrackMethods.negatedReference;
import static org.batfish.representation.cisco_asa.AsaConversions.computeDistributeListPolicies;
import static org.batfish.representation.cisco_asa.AsaConversions.convertCryptoMapSet;
import static org.batfish.representation.cisco_asa.AsaConversions.convertVrfLeakingConfig;
import static org.batfish.representation.cisco_asa.AsaConversions.generateBgpExportPolicy;
import static org.batfish.representation.cisco_asa.AsaConversions.generateBgpImportPolicy;
import static org.batfish.representation.cisco_asa.AsaConversions.generateEigrpPolicy;
import static org.batfish.representation.cisco_asa.AsaConversions.getIsakmpKeyGeneratedName;
import static org.batfish.representation.cisco_asa.AsaConversions.getRsaPubKeyGeneratedName;
import static org.batfish.representation.cisco_asa.AsaConversions.matchOwnAsn;
import static org.batfish.representation.cisco_asa.AsaConversions.resolveIsakmpProfileIfaceNames;
import static org.batfish.representation.cisco_asa.AsaConversions.resolveKeyringIfaceNames;
import static org.batfish.representation.cisco_asa.AsaConversions.resolveTunnelIfaceNames;
import static org.batfish.representation.cisco_asa.AsaConversions.toBgpAggregate;
import static org.batfish.representation.cisco_asa.AsaConversions.toCommunitySetMatchExpr;
import static org.batfish.representation.cisco_asa.AsaConversions.toIkePhase1Key;
import static org.batfish.representation.cisco_asa.AsaConversions.toIkePhase1Policy;
import static org.batfish.representation.cisco_asa.AsaConversions.toIkePhase1Proposal;
import static org.batfish.representation.cisco_asa.AsaConversions.toIpAccessList;
import static org.batfish.representation.cisco_asa.AsaConversions.toIpSpace;
import static org.batfish.representation.cisco_asa.AsaConversions.toIpsecPeerConfig;
import static org.batfish.representation.cisco_asa.AsaConversions.toIpsecPhase2Policy;
import static org.batfish.representation.cisco_asa.AsaConversions.toIpsecPhase2Proposal;
import static org.batfish.representation.cisco_asa.AsaConversions.toOspfDeadInterval;
import static org.batfish.representation.cisco_asa.AsaConversions.toOspfHelloInterval;
import static org.batfish.representation.cisco_asa.AsaConversions.toOspfNetworkType;
import static org.batfish.representation.cisco_asa.OspfProcess.DEFAULT_LOOPBACK_OSPF_COST;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Streams;
import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.VendorConversionException;
import org.batfish.common.util.CollectionUtil;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.FirewallSessionInterfaceInfo.Action;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.Line;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SnmpServer;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.TunnelConfiguration;
import org.batfish.datamodel.Zone;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.BgpConfederation;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.eigrp.ClassicMetric;
import org.batfish.datamodel.eigrp.EigrpInterfaceSettings;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.eigrp.EigrpProcessMode;
import org.batfish.datamodel.eigrp.WideMetric;
import org.batfish.datamodel.isis.IsisInterfaceLevelSettings;
import org.batfish.datamodel.isis.IsisInterfaceMode;
import org.batfish.datamodel.isis.IsisInterfaceSettings;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.batfish.datamodel.ospf.OspfDefaultOriginateType;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.StubType;
import org.batfish.datamodel.routing_policy.Common;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.RouteIsClassful;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.vendor_family.cisco.Aaa;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthentication;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLogin;
import org.batfish.datamodel.vendor_family.cisco.CiscoFamily;
import org.batfish.representation.cisco_asa.AsaNat.Section;
import org.batfish.representation.cisco_asa.Tunnel.TunnelMode;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.VendorStructureId;

public final class AsaConfiguration extends VendorConfiguration {
  public static final int DEFAULT_EBGP_ADMIN = 20;
  public static final int DEFAULT_IBGP_ADMIN = 200;
  public static final int DEFAULT_LOCAL_ADMIN = 200;

  public static final int DEFAULT_STATIC_ROUTE_DISTANCE = 1;

  @VisibleForTesting
  public static final TraceElement PERMIT_TRAFFIC_FROM_DEVICE =
      TraceElement.of("Matched traffic originating from this device");

  @VisibleForTesting
  public static final TraceElement PERMIT_SAME_SECURITY_TRAFFIC_INTRA_TRACE_ELEMENT =
      TraceElement.of("Matched same-security-traffic permit intra-interface");

  @VisibleForTesting
  public static final TraceElement DENY_SAME_SECURITY_TRAFFIC_INTRA_TRACE_ELEMENT =
      TraceElement.of("same-security-traffic permit intra-interface is not set");

  @VisibleForTesting
  public static final TraceElement PERMIT_SAME_SECURITY_TRAFFIC_INTER_TRACE_ELEMENT =
      TraceElement.of("Matched same-security-traffic permit inter-interface");

  @VisibleForTesting
  public static final TraceElement DENY_SAME_SECURITY_TRAFFIC_INTER_TRACE_ELEMENT =
      TraceElement.of("same-security-traffic permit inter-interface is not set");

  @VisibleForTesting
  public static TraceElement asaDeniedByOutputFilterTraceElement(
      String filename, IpAccessList filter) {
    return TraceElement.builder()
        .add("Denied by output filter ")
        .add(
            filter.getName(),
            new VendorStructureId(filename, filter.getSourceType(), filter.getSourceName()))
        .build();
  }

  @VisibleForTesting
  public static TraceElement asaPermittedByOutputFilterTraceElement(
      String filename, IpAccessList filter) {
    return TraceElement.builder()
        .add("Permitted by output filter ")
        .add(
            filter.getName(),
            new VendorStructureId(filename, filter.getSourceType(), filter.getSourceName()))
        .build();
  }

  /** Matches anything but the IPv4 default route. */
  static final Not NOT_DEFAULT_ROUTE = new Not(Common.matchDefaultRoute());

  /*
   * This map is used to convert interface names to their canonical forms.
   * The entries are visited in insertion order until a key is found of which the name to convert is
   * case-insensitively a prefix. The value corresponding to that key is chosen as the canonical
   * form for that name.
   *
   * NOTE: Entries are sorted by priority. Do not reorder unless you have a good reason.
   * For instance, we don't want 'e' to be canonically considered 'Embedded-Service-Engine' instead
   * of 'Ethernet'.
   */
  private static final Map<String, String> CISCO_INTERFACE_PREFIXES =
      ImmutableMap.<String, String>builder()
          .put("ap", "ap")
          .put("Async", "Async")
          .put("ATM", "ATM")
          .put("BDI", "BDI")
          .put("BRI", "BRI")
          .put("Bundle-Ether", "Bundle-Ethernet")
          .put("Bundle-Ethernet", "Bundle-Ethernet")
          .put("BVI", "BVI")
          .put("Cable", "Cable")
          .put("cable-downstream", "cable-downstream")
          .put("cable-mac", "cable-mac")
          .put("cable-upstream", "cable-upstream")
          .put("Cellular", "Cellular")
          .put("Crypto-Engine", "Crypto-Engine")
          .put("cmp-mgmt", "cmp-mgmt")
          .put("Dialer", "Dialer")
          .put("Dot11Radio", "Dot11Radio")
          .put("Ethernet", "Ethernet")
          .put("Embedded-Service-Engine", "Embedded-Service-Engine")
          .put("FastEthernet", "FastEthernet")
          .put("fc", "fc")
          .put("fe", "FastEthernet")
          .put("fortyGigE", "FortyGigabitEthernet")
          .put("FortyGigabitEthernet", "FortyGigabitEthernet")
          .put("GigabitEthernet", "GigabitEthernet")
          .put("ge", "GigabitEthernet")
          .put("GMPLS", "GMPLS")
          .put("HundredGigE", "HundredGigabitEthernet")
          .put("HundredGigabitEthernet", "HundredGigabitEthernet")
          .put("ip", "ip")
          .put("Group-Async", "Group-Async")
          .put("lo", "Loopback")
          .put("LongReachEthernet", "LongReachEthernet")
          .put("Loopback", "Loopback")
          .put("ma", "Management")
          .put("Management", "Management")
          .put("ManagementEthernet", "ManagementEthernet")
          .put("mfr", "mfr")
          .put("mgmt", "mgmt")
          .put("MgmtEth", "ManagementEthernet")
          .put("Modular-Cable", "Modular-Cable")
          .put("Multilink", "Multilink")
          .put("Null", "Null")
          .put("nve", "nve")
          .put("Port-channel", "Port-channel")
          .put("POS", "POS")
          .put("PTP", "PTP")
          .put("Redundant", "Redundant")
          .put("Serial", "Serial")
          .put("Service-Engine", "Service-Engine")
          .put("TenGigabitEthernet", "TenGigabitEthernet")
          .put("TenGigE", "TenGigabitEthernet")
          .put("te", "TenGigabitEthernet")
          .put("trunk", "trunk")
          .put("Tunnel", "Tunnel")
          .put("tunnel-ip", "tunnel-ip")
          .put("tunnel-te", "tunnel-te")
          .put("tw", "TwoGigabitEthernet")
          .put("twe", "TwentyFiveGigE")
          .put("TwentyFiveGigE", "TwentyFiveGigE")
          .put("TwentyFiveGigabitEthernet", "TwentyFiveGigE")
          .put("TwoGigabitEthernet", "TwoGigabitEthernet")
          .put("vasileft", "vasileft")
          .put("vasiright", "vasiright")
          .put("ve", "VirtualEthernet")
          .put("VirtualEthernet", "VirtualEthernet")
          .put("Virtual-Template", "Virtual-Template")
          .put("Vlan", "Vlan")
          .put("Vxlan", "Vxlan")
          .put("Wideband-Cable", "Wideband-Cable")
          .put("Wlan-ap", "Wlan-ap")
          .put("Wlan-GigabitEthernet", "Wlan-GigabitEthernet")
          .build();

  static final boolean DEFAULT_VRRP_PREEMPT = true;

  static final int DEFAULT_VRRP_PRIORITY = 100;

  public static final String MANAGEMENT_VRF_NAME = "management";

  // https://www.cisco.com/c/en/us/td/docs/security/asa/asa910/configuration/general/asa-910-general-config/route-static.html
  // The distance argument is the administrative distance for the route, between 1 and 254.
  // 2023-09-22 dhalperi@: seems like we should allow 255, which is likely in some cases.
  // TODO: disallow 255 for some CLI commands that don't allow it.
  static final int MAX_ADMINISTRATIVE_COST = 255;

  public static final String MANAGEMENT_INTERFACE_PREFIX = "mgmt";

  private static final int VLAN_NORMAL_MAX_CISCO = 1005;

  private static final int VLAN_NORMAL_MIN_CISCO = 2;

  public static String computeBgpDefaultRouteExportPolicyName(String vrf, String peer) {
    return String.format("~BGP_DEFAULT_ROUTE_PEER_EXPORT_POLICY:IPv4:%s:%s~", vrf, peer);
  }

  public static String computeBgpPeerImportPolicyName(String vrf, String peer) {
    return String.format("~BGP_PEER_IMPORT_POLICY:%s:%s~", vrf, peer);
  }

  public static String computeIcmpObjectGroupAclName(String name) {
    return String.format("~ICMP_OBJECT_GROUP~%s~", name);
  }

  /**
   * Computes a mapping of interface names to the primary {@link Ip} owned by each of the interface.
   * Filters out the interfaces having no primary {@link ConcreteInterfaceAddress}
   */
  private static Map<String, Ip> computeInterfaceOwnedPrimaryIp(Map<String, Interface> interfaces) {
    return interfaces.entrySet().stream()
        .filter(e -> Objects.nonNull(e.getValue().getAddress()))
        .collect(
            ImmutableMap.toImmutableMap(Entry::getKey, e -> e.getValue().getAddress().getIp()));
  }

  public static String computeProtocolObjectGroupAclName(String name) {
    return String.format("~PROTOCOL_OBJECT_GROUP~%s~", name);
  }

  public static String computeServiceObjectAclName(String name) {
    return String.format("~SERVICE_OBJECT~%s~", name);
  }

  public static String computeServiceObjectGroupAclName(String name) {
    return String.format("~SERVICE_OBJECT_GROUP~%s~", name);
  }

  @Override
  public String canonicalizeInterfaceName(String ifaceName) {
    Matcher matcher = Pattern.compile("[A-Za-z][-A-Za-z0-9]*[A-Za-z]").matcher(ifaceName);
    if (matcher.find()) {
      String ifacePrefix = matcher.group();
      String canonicalPrefix = getCanonicalInterfaceNamePrefix(ifacePrefix);
      String suffix = ifaceName.substring(ifacePrefix.length());
      return canonicalPrefix + suffix;
    }
    throw new BatfishException("Invalid interface name: '" + ifaceName + "'");
  }

  public static String getCanonicalInterfaceNamePrefix(String prefix) {
    for (Entry<String, String> e : CISCO_INTERFACE_PREFIXES.entrySet()) {
      String matchPrefix = e.getKey();
      String canonicalPrefix = e.getValue();
      if (matchPrefix.toLowerCase().startsWith(prefix.toLowerCase())) {
        return canonicalPrefix;
      }
    }
    throw new BatfishException("Invalid interface name prefix: '" + prefix + "'");
  }

  private static String getRouteMapClausePolicyName(RouteMap map, int continueTarget) {
    String mapName = map.getName();
    String clausePolicyName = "~RMCLAUSE~" + mapName + "~" + continueTarget + "~";
    return clausePolicyName;
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

  private final Map<String, IpAsPathAccessList> _asPathAccessLists;

  private final CiscoFamily _cf;

  private final Map<String, CryptoMapSet> _cryptoMapSets;

  private final Map<String, NamedRsaPubKey> _cryptoNamedRsaPubKeys;

  private final List<Ip> _dhcpRelayServers;

  private NavigableSet<String> _dnsServers;

  private String _dnsSourceInterface;

  private String _domainName;

  private final Map<String, ExpandedCommunityList> _expandedCommunityLists;

  private final Map<String, ExtendedAccessList> _extendedAccessLists;

  private final Map<String, ExtendedIpv6AccessList> _extendedIpv6AccessLists;

  private boolean _failover;

  private String _failoverCommunicationInterface;

  private String _failoverCommunicationInterfaceAlias;

  private final Map<String, String> _failoverInterfaces;

  private final Map<String, ConcreteInterfaceAddress> _failoverPrimaryAddresses;

  private boolean _failoverSecondary;

  private final Map<String, ConcreteInterfaceAddress> _failoverStandbyAddresses;

  private String _failoverStatefulSignalingInterface;

  private String _failoverStatefulSignalingInterfaceAlias;

  private String _hostname;
  private String _rawHostname;

  private final Map<String, InspectClassMap> _inspectClassMaps;

  private final Map<String, InspectPolicyMap> _inspectPolicyMaps;

  private final Map<String, Interface> _interfaces;

  private final Map<String, IpsecProfile> _ipsecProfiles;

  private final Map<String, IpsecTransformSet> _ipsecTransformSets;

  private final List<IsakmpKey> _isakmpKeys;

  private final Map<Integer, IsakmpPolicy> _isakmpPolicies;

  private final Map<String, IsakmpProfile> _isakmpProfiles;

  private final Map<String, Keyring> _keyrings;

  private final Map<String, MacAccessList> _macAccessLists;

  private final Map<String, IcmpTypeObjectGroup> _icmpTypeObjectGroups;

  private final Map<String, IntegerSpace> _namedVlans;

  private final List<AsaNat> _nats;

  private final Map<String, NetworkObjectGroup> _networkObjectGroups;

  private final Map<String, NetworkObjectInfo> _networkObjectInfos;

  private final Map<String, NetworkObject> _networkObjects;

  private String _ntpSourceInterface;

  private final Map<String, ObjectGroup> _objectGroups;

  private final Map<String, Prefix6List> _prefix6Lists;

  private final Map<String, PrefixList> _prefixLists;

  private final Map<String, ProtocolObjectGroup> _protocolObjectGroups;

  private final Map<String, RouteMap> _routeMaps;

  /**
   * Maps zone names to integers. Only includes zones that were created for security levels. In
   * effect, the reverse of computeSecurityLevelZoneName.
   */
  private final Map<String, Integer> _securityLevels;

  private boolean _sameSecurityTrafficInter;

  private boolean _sameSecurityTrafficIntra;

  private final Map<String, ServiceObject> _serviceObjects;

  private SnmpServer _snmpServer;

  private String _snmpSourceInterface;

  private boolean _spanningTreePortfastDefault;

  private final Map<String, StandardAccessList> _standardAccessLists;

  private final Map<String, StandardCommunityList> _standardCommunityLists;

  private final Map<String, StandardIpv6AccessList> _standardIpv6AccessLists;

  private NavigableSet<String> _tacacsServers;

  private String _tacacsSourceInterface;

  private final Map<String, Vrf> _vrfs;

  private final SortedMap<String, VrrpInterface> _vrrpGroups;

  private final Map<String, ServiceObjectGroup> _serviceObjectGroups;

  private final Map<String, Map<String, SecurityZonePair>> _securityZonePairs;

  private final Map<String, SecurityZone> _securityZones;

  private final Map<String, TrackMethod> _trackingGroups;

  // initialized when needed
  private Multimap<Integer, Interface> _interfacesBySecurityLevel;

  public AsaConfiguration() {
    _asPathAccessLists = new TreeMap<>();
    _cf = new CiscoFamily();
    _cryptoNamedRsaPubKeys = new TreeMap<>();
    _cryptoMapSets = new HashMap<>();
    _dhcpRelayServers = new ArrayList<>();
    _dnsServers = new TreeSet<>();
    _expandedCommunityLists = new TreeMap<>();
    _extendedAccessLists = new TreeMap<>();
    _extendedIpv6AccessLists = new TreeMap<>();
    _failoverInterfaces = new TreeMap<>();
    _failoverPrimaryAddresses = new TreeMap<>();
    _failoverStandbyAddresses = new TreeMap<>();
    _isakmpKeys = new ArrayList<>();
    _isakmpPolicies = new TreeMap<>();
    _isakmpProfiles = new TreeMap<>();
    _inspectClassMaps = new TreeMap<>();
    _inspectPolicyMaps = new TreeMap<>();
    _interfaces = new TreeMap<>();
    _ipsecTransformSets = new TreeMap<>();
    _ipsecProfiles = new TreeMap<>();
    _keyrings = new TreeMap<>();
    _macAccessLists = new TreeMap<>();
    _icmpTypeObjectGroups = new TreeMap<>();
    _namedVlans = new HashMap<>();
    _nats = new ArrayList<>();
    _networkObjectGroups = new TreeMap<>();
    _networkObjectInfos = new TreeMap<>();
    _networkObjects = new TreeMap<>();
    _objectGroups = new TreeMap<>();
    _prefixLists = new TreeMap<>();
    _prefix6Lists = new TreeMap<>();
    _protocolObjectGroups = new TreeMap<>();
    _routeMaps = new TreeMap<>();
    _securityLevels = new TreeMap<>();
    _securityZonePairs = new TreeMap<>();
    _securityZones = new TreeMap<>();
    _serviceObjectGroups = new TreeMap<>();
    _serviceObjects = new TreeMap<>();
    _standardAccessLists = new TreeMap<>();
    _standardIpv6AccessLists = new TreeMap<>();
    _standardCommunityLists = new TreeMap<>();
    _tacacsServers = new TreeSet<>();
    _trackingGroups = new TreeMap<>();
    _vrfs = new TreeMap<>();
    _vrfs.put(Configuration.DEFAULT_VRF_NAME, new Vrf(Configuration.DEFAULT_VRF_NAME));
    _vrrpGroups = new TreeMap<>();
  }

  private void applyVrrp(Configuration c) {
    _vrrpGroups.forEach(
        (ifaceName, vrrpInterface) -> {
          org.batfish.datamodel.Interface iface = c.getAllInterfaces().get(ifaceName);
          if (iface != null) {
            vrrpInterface
                .getVrrpGroups()
                .forEach(
                    (vrid, vrrpGroup) -> {
                      org.batfish.datamodel.VrrpGroup.Builder newGroup =
                          org.batfish.datamodel.VrrpGroup.builder();
                      newGroup.setPreempt(vrrpGroup.getPreempt());
                      newGroup.setPriority(vrrpGroup.getPriority());
                      ConcreteInterfaceAddress ifaceAddress = iface.getConcreteAddress();
                      if (ifaceAddress != null) {
                        newGroup.setSourceAddress(ifaceAddress);
                        Ip virtualAddress = vrrpGroup.getVirtualAddress();
                        if (virtualAddress != null) {
                          newGroup.setVirtualAddresses(ifaceName, virtualAddress);
                        } else {
                          _w.redFlag(
                              "No virtual address set for VRRP on interface: '" + ifaceName + "'");
                        }
                      } else {
                        _w.redFlag(
                            "Could not determine prefix length of VRRP address on interface '"
                                + ifaceName
                                + "' due to missing prefix");
                      }
                      iface.addVrrpGroup(vrid, newGroup.build());
                    });
          }
        });
  }

  private boolean containsIpAccessList(String eaListName, String mapName) {
    if (mapName == null || !_routeMaps.containsKey(mapName)) {
      return false;
    }
    return _routeMaps.get(mapName).getClauses().values().stream()
        .flatMap(clause -> clause.getMatchList().stream())
        .filter(line -> line instanceof RouteMapMatchIpAccessListLine)
        .anyMatch(
            line -> ((RouteMapMatchIpAccessListLine) line).getListNames().contains(eaListName));
  }

  private boolean containsIpv6AccessList(String eaListName, String mapName) {
    if (mapName == null || !_routeMaps.containsKey(mapName)) {
      return false;
    }
    return _routeMaps.get(mapName).getClauses().values().stream()
        .flatMap(clause -> clause.getMatchList().stream())
        .filter(line -> line instanceof RouteMapMatchIpv6AccessListLine)
        .anyMatch(
            line -> ((RouteMapMatchIpv6AccessListLine) line).getListNames().contains(eaListName));
  }

  public Map<String, IpAsPathAccessList> getAsPathAccessLists() {
    return _asPathAccessLists;
  }

  private Ip getBgpRouterId(Configuration c, String vrfName, BgpProcess proc) {
    Ip processRouterId = proc.getRouterId();
    if (processRouterId == null) {
      processRouterId = _vrfs.get(Configuration.DEFAULT_VRF_NAME).getBgpProcess().getRouterId();
    }
    if (processRouterId == null) {
      processRouterId = Ip.ZERO;
      for (Entry<String, org.batfish.datamodel.Interface> e :
          c.getAllInterfaces(vrfName).entrySet()) {
        String iname = e.getKey();
        org.batfish.datamodel.Interface iface = e.getValue();
        if (iname.startsWith("Loopback")) {
          ConcreteInterfaceAddress address = iface.getConcreteAddress();
          if (address != null) {
            Ip currentIp = address.getIp();
            if (currentIp.asLong() > processRouterId.asLong()) {
              processRouterId = currentIp;
            }
          }
        }
      }
      if (processRouterId.equals(Ip.ZERO)) {
        for (org.batfish.datamodel.Interface currentInterface :
            c.getAllInterfaces(vrfName).values()) {
          ConcreteInterfaceAddress address = currentInterface.getConcreteAddress();
          if (address != null) {
            Ip currentIp = address.getIp();
            if (currentIp.asLong() > processRouterId.asLong()) {
              processRouterId = currentIp;
            }
          }
        }
      }
    }
    return processRouterId;
  }

  public CiscoFamily getCf() {
    return _cf;
  }

  public Map<String, CryptoMapSet> getCryptoMapSets() {
    return _cryptoMapSets;
  }

  public Map<String, NamedRsaPubKey> getCryptoNamedRsaPubKeys() {
    return _cryptoNamedRsaPubKeys;
  }

  public Vrf getDefaultVrf() {
    return _vrfs.get(Configuration.DEFAULT_VRF_NAME);
  }

  public List<Ip> getDhcpRelayServers() {
    return _dhcpRelayServers;
  }

  public NavigableSet<String> getDnsServers() {
    return _dnsServers;
  }

  public String getDnsSourceInterface() {
    return _dnsSourceInterface;
  }

  public Map<String, ExpandedCommunityList> getExpandedCommunityLists() {
    return _expandedCommunityLists;
  }

  public Map<String, ExtendedAccessList> getExtendedAcls() {
    return _extendedAccessLists;
  }

  public Map<String, ExtendedIpv6AccessList> getExtendedIpv6Acls() {
    return _extendedIpv6AccessLists;
  }

  public boolean getFailover() {
    return _failover;
  }

  public String getFailoverCommunicationInterface() {
    return _failoverCommunicationInterface;
  }

  public String getFailoverCommunicationInterfaceAlias() {
    return _failoverCommunicationInterfaceAlias;
  }

  public Map<String, String> getFailoverInterfaces() {
    return _failoverInterfaces;
  }

  public Map<String, ConcreteInterfaceAddress> getFailoverPrimaryAddresses() {
    return _failoverPrimaryAddresses;
  }

  public boolean getFailoverSecondary() {
    return _failoverSecondary;
  }

  public Map<String, ConcreteInterfaceAddress> getFailoverStandbyAddresses() {
    return _failoverStandbyAddresses;
  }

  public String getFailoverStatefulSignalingInterface() {
    return _failoverStatefulSignalingInterface;
  }

  public String getFailoverStatefulSignalingInterfaceAlias() {
    return _failoverStatefulSignalingInterfaceAlias;
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  public Map<String, IpsecProfile> getIpsecProfiles() {
    return _ipsecProfiles;
  }

  public Map<String, IpsecTransformSet> getIpsecTransformSets() {
    return _ipsecTransformSets;
  }

  public List<IsakmpKey> getIsakmpKeys() {
    return _isakmpKeys;
  }

  public Map<Integer, IsakmpPolicy> getIsakmpPolicies() {
    return _isakmpPolicies;
  }

  public Map<String, IsakmpProfile> getIsakmpProfiles() {
    return _isakmpProfiles;
  }

  public Map<String, Keyring> getKeyrings() {
    return _keyrings;
  }

  public Map<String, MacAccessList> getMacAccessLists() {
    return _macAccessLists;
  }

  public Map<String, IntegerSpace> getNamedVlans() {
    return _namedVlans;
  }

  public List<AsaNat> getCiscoAsaNats() {
    return _nats;
  }

  private String getNewInterfaceName(Interface iface) {
    return firstNonNull(iface.getAlias(), iface.getName());
  }

  private String getNewInterfaceName(@Nonnull String ifaceName) {
    Interface iface = _interfaces.get(ifaceName);
    return iface != null ? getNewInterfaceName(iface) : ifaceName;
  }

  public String getNtpSourceInterface() {
    return _ntpSourceInterface;
  }

  public Map<String, Prefix6List> getPrefix6Lists() {
    return _prefix6Lists;
  }

  public Map<String, PrefixList> getPrefixLists() {
    return _prefixLists;
  }

  public Map<String, RouteMap> getRouteMaps() {
    return _routeMaps;
  }

  private @Nullable String getASASecurityLevelZoneName(Interface iface) {
    Integer level = iface.getSecurityLevel();
    if (level == null) {
      return null;
    }
    return computeASASecurityLevelZoneName(level);
  }

  public SnmpServer getSnmpServer() {
    return _snmpServer;
  }

  public String getSnmpSourceInterface() {
    return _snmpSourceInterface;
  }

  public boolean getSpanningTreePortfastDefault() {
    return _spanningTreePortfastDefault;
  }

  public Map<String, StandardAccessList> getStandardAcls() {
    return _standardAccessLists;
  }

  public Map<String, StandardCommunityList> getStandardCommunityLists() {
    return _standardCommunityLists;
  }

  public Map<String, StandardIpv6AccessList> getStandardIpv6Acls() {
    return _standardIpv6AccessLists;
  }

  public NavigableSet<String> getTacacsServers() {
    return _tacacsServers;
  }

  public String getTacacsSourceInterface() {
    return _tacacsSourceInterface;
  }

  private Ip getUpdateSource(
      Configuration c, String vrfName, LeafBgpPeerGroup lpg, String updateSourceInterface) {
    Ip updateSource = null;
    if (updateSourceInterface != null) {
      org.batfish.datamodel.Interface sourceInterface =
          c.getAllInterfaces(vrfName).get(updateSourceInterface);
      if (sourceInterface != null) {
        ConcreteInterfaceAddress address = sourceInterface.getConcreteAddress();
        if (address != null) {
          Ip sourceIp = address.getIp();
          updateSource = sourceIp;
        } else {
          _w.redFlag(
              "bgp update source interface: '"
                  + updateSourceInterface
                  + "' not assigned an ip address");
        }
      }
    } else {
      if (lpg instanceof DynamicIpBgpPeerGroup) {
        updateSource = null;
      } else {
        Ip neighborAddress = lpg.getNeighborPrefix().getStartIp();
        for (org.batfish.datamodel.Interface iface : c.getAllInterfaces(vrfName).values()) {
          for (ConcreteInterfaceAddress interfaceAddress : iface.getAllConcreteAddresses()) {
            if (interfaceAddress.getPrefix().containsIp(neighborAddress)) {
              Ip ifaceAddress = interfaceAddress.getIp();
              updateSource = ifaceAddress;
            }
          }
        }
      }
    }
    return updateSource;
  }

  public Map<String, Vrf> getVrfs() {
    return _vrfs;
  }

  public SortedMap<String, VrrpInterface> getVrrpGroups() {
    return _vrrpGroups;
  }

  private void markAcls(AsaStructureUsage... usages) {
    for (AsaStructureUsage usage : usages) {
      markAbstractStructure(
          AsaStructureType.IP_ACCESS_LIST,
          usage,
          ImmutableList.of(
              AsaStructureType.IPV4_ACCESS_LIST_STANDARD,
              AsaStructureType.IPV4_ACCESS_LIST_EXTENDED,
              AsaStructureType.IPV6_ACCESS_LIST_STANDARD,
              AsaStructureType.IPV6_ACCESS_LIST_EXTENDED));
    }
  }

  private void markIpOrMacAcls(AsaStructureUsage... usages) {
    for (AsaStructureUsage usage : usages) {
      markAbstractStructure(
          AsaStructureType.ACCESS_LIST,
          usage,
          Arrays.asList(
              AsaStructureType.IPV4_ACCESS_LIST_EXTENDED,
              AsaStructureType.IPV4_ACCESS_LIST_STANDARD,
              AsaStructureType.IPV6_ACCESS_LIST_EXTENDED,
              AsaStructureType.IPV6_ACCESS_LIST_STANDARD,
              AsaStructureType.MAC_ACCESS_LIST));
    }
  }

  private void markIpv4Acls(AsaStructureUsage... usages) {
    for (AsaStructureUsage usage : usages) {
      markAbstractStructure(
          AsaStructureType.IPV4_ACCESS_LIST,
          usage,
          ImmutableList.of(
              AsaStructureType.IPV4_ACCESS_LIST_STANDARD,
              AsaStructureType.IPV4_ACCESS_LIST_EXTENDED));
    }
  }

  private void markIpv6Acls(AsaStructureUsage... usages) {
    for (AsaStructureUsage usage : usages) {
      markAbstractStructure(
          AsaStructureType.IPV6_ACCESS_LIST,
          usage,
          ImmutableList.of(
              AsaStructureType.IPV6_ACCESS_LIST_STANDARD,
              AsaStructureType.IPV6_ACCESS_LIST_EXTENDED));
    }
  }

  private void processFailoverSettings() {
    if (!_failover) {
      return;
    }

    if (_failoverCommunicationInterface == null
        || _failoverCommunicationInterfaceAlias == null
        || _failoverStatefulSignalingInterface == null
        || _failoverStatefulSignalingInterfaceAlias == null) {
      _w.redFlag(
          "Unable to process failover configuration: one of failover communication or stateful"
              + " signaling interfaces is unset");
      return;
    }

    Interface commIface = _interfaces.get(_failoverCommunicationInterface);
    Interface sigIface = _interfaces.get(_failoverStatefulSignalingInterface);
    if (commIface == null) {
      _w.redFlag(
          String.format(
              "Unable to process failover configuration: communication interface %s is not present",
              _failoverCommunicationInterface));
      return;
    }
    if (sigIface == null) {
      _w.redFlag(
          String.format(
              "Unable to process failover configuration: stateful signaling interface %s is not"
                  + " present",
              _failoverStatefulSignalingInterface));
      return;
    }

    ConcreteInterfaceAddress commAddress;
    ConcreteInterfaceAddress sigAddress;

    if (_failoverSecondary) {
      commAddress = _failoverStandbyAddresses.get(_failoverCommunicationInterfaceAlias);
      sigAddress = _failoverStandbyAddresses.get(_failoverStatefulSignalingInterfaceAlias);
      for (Interface iface : _interfaces.values()) {
        iface.setAddress(iface.getStandbyAddress());
      }
    } else {
      commAddress = _failoverPrimaryAddresses.get(_failoverCommunicationInterfaceAlias);
      sigAddress = _failoverPrimaryAddresses.get(_failoverStatefulSignalingInterfaceAlias);
    }
    commIface.setAddress(commAddress);
    commIface.setActive(true);
    sigIface.setAddress(sigAddress);
    sigIface.setActive(true);
  }

  public void setDnsSourceInterface(String dnsSourceInterface) {
    _dnsSourceInterface = dnsSourceInterface;
  }

  public void setDomainName(String domainName) {
    _domainName = domainName;
  }

  public void setFailover(boolean failover) {
    _failover = failover;
  }

  public void setFailoverCommunicationInterface(String failoverCommunicationInterface) {
    _failoverCommunicationInterface = failoverCommunicationInterface;
  }

  public void setFailoverCommunicationInterfaceAlias(String failoverCommunicationInterfaceAlias) {
    _failoverCommunicationInterfaceAlias = failoverCommunicationInterfaceAlias;
  }

  public void setFailoverSecondary(boolean failoverSecondary) {
    _failoverSecondary = failoverSecondary;
  }

  public void setFailoverStatefulSignalingInterface(String failoverStatefulSignalingInterface) {
    _failoverStatefulSignalingInterface = failoverStatefulSignalingInterface;
  }

  public void setFailoverStatefulSignalingInterfaceAlias(
      String failoverStatefulSignalingInterfaceAlias) {
    _failoverStatefulSignalingInterfaceAlias = failoverStatefulSignalingInterfaceAlias;
  }

  @Override
  public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
    _rawHostname = hostname;
  }

  public void setNtpSourceInterface(String ntpSourceInterface) {
    _ntpSourceInterface = ntpSourceInterface;
  }

  public void setSameSecurityTrafficInter(boolean permit) {
    _sameSecurityTrafficInter = permit;
  }

  public void setSameSecurityTrafficIntra(boolean permit) {
    _sameSecurityTrafficIntra = permit;
  }

  public void setSnmpServer(SnmpServer snmpServer) {
    _snmpServer = snmpServer;
  }

  public void setSnmpSourceInterface(String snmpSourceInterface) {
    _snmpSourceInterface = snmpSourceInterface;
  }

  public void setSpanningTreePortfastDefault(boolean spanningTreePortfastDefault) {
    _spanningTreePortfastDefault = spanningTreePortfastDefault;
  }

  public void setTacacsSourceInterface(String tacacsSourceInterface) {
    _tacacsSourceInterface = tacacsSourceInterface;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    checkArgument(format == ConfigurationFormat.CISCO_ASA);
  }

  private org.batfish.datamodel.BgpProcess toBgpProcess(
      Configuration c, BgpProcess proc, String vrfName) {
    Ip bgpRouterId = getBgpRouterId(c, vrfName, proc);
    // TODO: customizable admin costs
    org.batfish.datamodel.BgpProcess newBgpProcess =
        bgpProcessBuilder().setRouterId(bgpRouterId).build();
    newBgpProcess.setClusterListAsIbgpCost(true);
    BgpTieBreaker tieBreaker = proc.getTieBreaker();
    if (tieBreaker != null) {
      newBgpProcess.setTieBreaker(tieBreaker);
    }
    MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode =
        proc.getAsPathMultipathRelax() ? PATH_LENGTH : EXACT_PATH;
    newBgpProcess.setMultipathEquivalentAsPathMatchMode(multipathEquivalentAsPathMatchMode);
    boolean multipathEbgp = false;
    boolean multipathIbgp = false;
    if (firstNonNull(proc.getMaximumPaths(), 0) > 1) {
      multipathEbgp = true;
      multipathIbgp = true;
    }
    if (firstNonNull(proc.getMaximumPathsEbgp(), 0) > 1) {
      multipathEbgp = true;
    }
    if (firstNonNull(proc.getMaximumPathsIbgp(), 0) > 1) {
      multipathIbgp = true;
    }
    newBgpProcess.setMultipathEbgp(multipathEbgp);
    newBgpProcess.setMultipathIbgp(multipathIbgp);

    // Global confederation config
    Long confederation = proc.getConfederation();
    if (confederation != null && !proc.getConfederationMembers().isEmpty()) {
      newBgpProcess.setConfederation(
          new BgpConfederation(confederation, proc.getConfederationMembers()));
    }

    // Populate process-level BGP aggregates
    proc.getAggregateNetworks().values().stream()
        .map(ipv4Aggregate -> toBgpAggregate(ipv4Aggregate, c, _w))
        .forEach(newBgpProcess::addAggregate);

    /*
     * Create common BGP export policy. This policy's only function is to prevent export of
     * suppressed routes (contributors to summary-only aggregates).
     */
    RoutingPolicy.Builder bgpCommonExportPolicy =
        RoutingPolicy.builder()
            .setOwner(c)
            .setName(Names.generatedBgpCommonExportPolicyName(vrfName));

    // Never export routes suppressed because they are more specific than summary-only aggregate
    Stream<Prefix> summaryOnlyNetworks =
        proc.getAggregateNetworks().entrySet().stream()
            .filter(e -> e.getValue().getSummaryOnly())
            .map(Entry::getKey);
    If suppressSummaryOnly = suppressSummarizedPrefixes(c, vrfName, summaryOnlyNetworks);
    if (suppressSummaryOnly != null) {
      bgpCommonExportPolicy.addStatement(suppressSummaryOnly);
    }

    // Finalize common export policy
    bgpCommonExportPolicy.addStatement(Statements.ReturnTrue.toStaticStatement()).build();

    /*
     * Create BGP redistribution policy. This should capture network statements, redistribute
     * commands, and aggregates (at this time)
     */
    String redistPolicyName = generatedBgpRedistributionPolicyName(vrfName);
    RoutingPolicy.Builder redistributionPolicy =
        RoutingPolicy.builder().setOwner(c).setName(redistPolicyName);

    // Export RIP routes that should be redistributed.
    BgpRedistributionPolicy redistributeRipPolicy =
        proc.getRedistributionPolicies().get(RoutingProtocol.RIP);
    if (redistributeRipPolicy != null) {
      Conjunction exportRipConditions = new Conjunction();
      exportRipConditions.setComment("Redistribute RIP routes into BGP");
      exportRipConditions.getConjuncts().add(new MatchProtocol(RoutingProtocol.RIP));
      String mapName = redistributeRipPolicy.getRouteMap();
      if (mapName != null) {
        if (_routeMaps.containsKey(mapName)) {
          exportRipConditions.getConjuncts().add(new CallExpr(mapName));
        }
      }
      redistributionPolicy.addStatement(
          new If(exportRipConditions, ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
    }

    // Export static routes that should be redistributed.
    BgpRedistributionPolicy redistributeStaticPolicy =
        proc.getRedistributionPolicies().get(RoutingProtocol.STATIC);
    if (redistributeStaticPolicy != null) {
      Conjunction exportStaticConditions = new Conjunction();
      exportStaticConditions.setComment("Redistribute static routes into BGP");
      exportStaticConditions.getConjuncts().add(new MatchProtocol(RoutingProtocol.STATIC));
      String mapName = redistributeStaticPolicy.getRouteMap();
      if (mapName != null) {
        if (_routeMaps.containsKey(mapName)) {
          exportStaticConditions.getConjuncts().add(new CallExpr(mapName));
        }
      }
      redistributionPolicy.addStatement(
          new If(
              exportStaticConditions, ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
    }

    // Export connected routes that should be redistributed.
    BgpRedistributionPolicy redistributeConnectedPolicy =
        proc.getRedistributionPolicies().get(RoutingProtocol.CONNECTED);
    if (redistributeConnectedPolicy != null) {
      Conjunction exportConnectedConditions = new Conjunction();
      exportConnectedConditions.setComment("Redistribute connected routes into BGP");
      exportConnectedConditions.getConjuncts().add(new MatchProtocol(RoutingProtocol.CONNECTED));
      String mapName = redistributeConnectedPolicy.getRouteMap();
      if (mapName != null) {
        if (_routeMaps.containsKey(mapName)) {
          exportConnectedConditions.getConjuncts().add(new CallExpr(mapName));
        }
      }
      redistributionPolicy.addStatement(
          new If(
              exportConnectedConditions,
              ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
    }

    // Export OSPF routes that should be redistributed.
    BgpRedistributionPolicy redistributeOspfPolicy =
        proc.getRedistributionPolicies().get(RoutingProtocol.OSPF);
    if (redistributeOspfPolicy != null) {
      Conjunction exportOspfConditions = new Conjunction();
      exportOspfConditions.setComment("Redistribute OSPF routes into BGP");
      exportOspfConditions
          .getConjuncts()
          .add(
              firstNonNull(
                  (MatchProtocol)
                      redistributeOspfPolicy
                          .getSpecialAttributes()
                          .get(BgpRedistributionPolicy.OSPF_ROUTE_TYPES),
                  // No match type means internal routes only, at least on IOS.
                  // https://www.cisco.com/c/en/us/support/docs/ip/border-gateway-protocol-bgp/5242-bgp-ospf-redis.html#redistributionofonlyospfinternalroutesintobgp
                  new MatchProtocol(RoutingProtocol.OSPF, RoutingProtocol.OSPF_IA)));
      String mapName = redistributeOspfPolicy.getRouteMap();
      if (mapName != null) {
        if (_routeMaps.containsKey(mapName)) {
          exportOspfConditions.getConjuncts().add(new CallExpr(mapName));
        }
      }
      redistributionPolicy.addStatement(
          new If(
              exportOspfConditions, ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
    }

    // Export EIGRP routes that should be redistributed.
    BgpRedistributionPolicy redistributeEigrpPolicy =
        // key EIGRP indicates redist external too; EIGRP_EX is never used as a key
        proc.getRedistributionPolicies().get(RoutingProtocol.EIGRP);
    if (redistributeEigrpPolicy != null) {
      ImmutableList.Builder<BooleanExpr> exportEigrpConditions = ImmutableList.builder();
      exportEigrpConditions.add(new MatchProtocol(RoutingProtocol.EIGRP, RoutingProtocol.EIGRP_EX));
      String mapName = redistributeEigrpPolicy.getRouteMap();
      if (mapName != null) {
        if (_routeMaps.containsKey(mapName)) {
          exportEigrpConditions.add(new CallExpr(mapName));
        }
      }
      Conjunction eigrp = new Conjunction(exportEigrpConditions.build());
      eigrp.setComment("Redistribute EIGRP routes into BGP");
      redistributionPolicy.addStatement(
          new If(eigrp, ImmutableList.of(Statements.ExitAccept.toStaticStatement())));
    }

    // cause ip peer groups to inherit unset fields from owning named peer
    // group if it exists, and then always from process master peer group
    Set<LeafBgpPeerGroup> leafGroups = new LinkedHashSet<>();
    leafGroups.addAll(proc.getIpPeerGroups().values());
    leafGroups.addAll(proc.getIpv6PeerGroups().values());
    leafGroups.addAll(proc.getDynamicIpPeerGroups().values());
    leafGroups.addAll(proc.getDynamicIpv6PeerGroups().values());
    for (LeafBgpPeerGroup lpg : leafGroups) {
      lpg.inheritUnsetFields(proc, this);
    }

    // create origination prefilter from listed advertised networks
    proc.getIpNetworks()
        .forEach(
            (prefix, bgpNetwork) -> {
              Conjunction exportNetworkConditions = new Conjunction();
              PrefixSpace space = new PrefixSpace();
              space.addPrefix(prefix);
              newBgpProcess.addToOriginationSpace(space);
              exportNetworkConditions
                  .getConjuncts()
                  .add(
                      new MatchPrefixSet(
                          DestinationNetwork.instance(), new ExplicitPrefixSet(space)));
              exportNetworkConditions
                  .getConjuncts()
                  .add(
                      new Not(
                          new MatchProtocol(
                              RoutingProtocol.BGP,
                              RoutingProtocol.IBGP,
                              RoutingProtocol.AGGREGATE)));
              String mapName = bgpNetwork.getRouteMapName();
              if (mapName != null && _routeMaps.containsKey(mapName)) {
                exportNetworkConditions.getConjuncts().add(new CallExpr(mapName));
              }
              redistributionPolicy.addStatement(
                  new If(
                      exportNetworkConditions,
                      ImmutableList.of(
                          new SetOrigin(new LiteralOrigin(OriginType.IGP, null)),
                          Statements.ExitAccept.toStaticStatement())));
            });

    // Finalize redistribution policy and attach to process
    redistributionPolicy.addStatement(Statements.ExitReject.toStaticStatement()).build();
    newBgpProcess.setRedistributionPolicy(redistPolicyName);

    for (LeafBgpPeerGroup lpg : leafGroups) {
      if (!lpg.getActive() || lpg.getShutdown()) {
        continue;
      }
      if (lpg.getRemoteAs() == null) {
        _w.redFlag("No remote-as set for peer: " + lpg.getName());
        continue;
      }
      if (lpg instanceof Ipv6BgpPeerGroup
          || lpg instanceof DynamicIpv6BgpPeerGroup
          || lpg.getNeighborPrefix6() != null) {
        // TODO: implement ipv6 bgp neighbors
        continue;
      }
      // update source
      String updateSourceInterface = lpg.getUpdateSource();
      assert lpg.getNeighborPrefix() != null;
      Ip updateSource = getUpdateSource(c, vrfName, lpg, updateSourceInterface);

      // Get default-originate generation policy
      String defaultOriginateGenerationMap = null;
      if (lpg.getDefaultOriginate()) {
        defaultOriginateGenerationMap = lpg.getDefaultOriginateMap();
      }

      // Generate import and export policies
      String peerImportPolicyName = generateBgpImportPolicy(lpg, vrfName, c, _w);
      generateBgpExportPolicy(lpg, vrfName, c, _w);

      // If defaultOriginate is set, create default route for this peer group
      GeneratedRoute.Builder defaultRoute = null;
      if (lpg.getDefaultOriginate()) {
        defaultRoute = GeneratedRoute.builder();
        defaultRoute.setNetwork(Prefix.ZERO);
        defaultRoute.setAdmin(MAX_ADMINISTRATIVE_COST);

        if (defaultOriginateGenerationMap != null
            && c.getRoutingPolicies().containsKey(defaultOriginateGenerationMap)) {
          // originate contingent on generation policy
          defaultRoute.setGenerationPolicy(defaultOriginateGenerationMap);
        }
      }

      Ip clusterId = lpg.getClusterId();
      if (clusterId == null) {
        clusterId = bgpRouterId;
      }
      String description = lpg.getDescription();
      Long pgLocalAs = lpg.getLocalAs();
      long localAs = pgLocalAs != null ? pgLocalAs : proc.getProcnum();

      BgpPeerConfig.Builder<?, ?> newNeighborBuilder;
      if (lpg instanceof IpBgpPeerGroup) {
        IpBgpPeerGroup ipg = (IpBgpPeerGroup) lpg;
        newNeighborBuilder =
            BgpActivePeerConfig.builder()
                .setPeerAddress(ipg.getIp())
                .setRemoteAsns(
                    Optional.ofNullable(lpg.getRemoteAs())
                        .map(LongSpace::of)
                        .orElse(LongSpace.EMPTY));
      } else if (lpg instanceof DynamicIpBgpPeerGroup) {
        DynamicIpBgpPeerGroup dpg = (DynamicIpBgpPeerGroup) lpg;
        LongSpace.Builder asns = LongSpace.builder().including(dpg.getRemoteAs());
        Optional.ofNullable(dpg.getAlternateAs()).ifPresent(asns::includingAll);
        newNeighborBuilder =
            BgpPassivePeerConfig.builder()
                .setPeerPrefix(dpg.getPrefix())
                .setRemoteAsns(asns.build());
      } else {
        throw new VendorConversionException("Invalid BGP leaf neighbor type");
      }
      newNeighborBuilder.setBgpProcess(newBgpProcess);
      newNeighborBuilder.setConfederation(proc.getConfederation());
      newNeighborBuilder.setEnforceFirstAs(firstNonNull(proc.getEnforceFirstAs(), Boolean.TRUE));

      AddressFamilyCapabilities ipv4AfSettings =
          AddressFamilyCapabilities.builder()
              .setAdditionalPathsReceive(lpg.getAdditionalPathsReceive())
              .setAdditionalPathsSelectAll(lpg.getAdditionalPathsSelectAll())
              .setAdditionalPathsSend(lpg.getAdditionalPathsSend())
              .setAllowLocalAsIn(lpg.getAllowAsIn())
              .setAllowRemoteAsOut(ALWAYS) /* no outgoing remote-as check on IOS */
              /*
               * On Cisco IOS, advertise-inactive is true by default. This can be modified by
               * "bgp suppress-inactive" command,
               * which we currently do not parse/extract. So we choose the default value here.
               *
               * For other Cisco OS variations (e.g., IOS-XR) we did not find a similar command and for now,
               * we assume behavior to be identical to IOS family.
               */
              .setAdvertiseInactive(true)
              .setSendCommunity(lpg.getSendCommunity())
              .setSendExtendedCommunity(lpg.getSendExtendedCommunity())
              .build();
      newNeighborBuilder.setIpv4UnicastAddressFamily(
          Ipv4UnicastAddressFamily.builder()
              .setAddressFamilyCapabilities(ipv4AfSettings)
              .setImportPolicy(peerImportPolicyName)
              .setExportPolicy(Names.generatedBgpPeerExportPolicyName(vrfName, lpg.getName()))
              .setRouteReflectorClient(lpg.getRouteReflectorClient())
              .build());
      newNeighborBuilder.setClusterId(clusterId.asLong());
      newNeighborBuilder.setDefaultMetric(proc.getDefaultMetric());
      newNeighborBuilder.setDescription(description);
      newNeighborBuilder.setEbgpMultihop(lpg.getEbgpMultihop());
      if (defaultRoute != null) {
        newNeighborBuilder.setGeneratedRoutes(ImmutableSet.of(defaultRoute.build()));
      }
      newNeighborBuilder.setGroup(lpg.getGroupName());
      newNeighborBuilder.setLocalAs(localAs);
      newNeighborBuilder.setLocalIp(updateSource);
      newNeighborBuilder.build();
    }
    return newBgpProcess;
  }

  private static final Pattern INTERFACE_WITH_SUBINTERFACE = Pattern.compile("^(.*)\\.(\\d+)$");

  /**
   * Get the {@link OspfNetwork} in the specified {@link OspfProcess} containing the specified
   * {@link Interface}'s address
   *
   * <p>Returns {@code null} if the interface address is {@code null} or the interface address does
   * not overlap with any {@link OspfNetwork} in the specified {@link OspfProcess}
   */
  private static @Nullable OspfNetwork getOspfNetworkForInterface(
      Interface iface, OspfProcess process) {
    ConcreteInterfaceAddress interfaceAddress = iface.getAddress();
    if (interfaceAddress == null) {
      // Iface has no IP address / isn't associated with a network in this OSPF process
      return null;
    }

    // Sort networks with longer prefixes first, then lower start IPs and areas
    SortedSet<OspfNetwork> networks =
        ImmutableSortedSet.copyOf(
            Comparator.<OspfNetwork>comparingInt(n -> n.getPrefix().getPrefixLength())
                .reversed()
                .thenComparing(n -> n.getPrefix().getStartIp())
                .thenComparingLong(OspfNetwork::getArea),
            process.getNetworks());
    for (OspfNetwork network : networks) {
      Prefix networkPrefix = network.getPrefix();
      Ip networkAddress = networkPrefix.getStartIp();
      Ip maskedInterfaceAddress =
          interfaceAddress.getIp().getNetworkAddress(networkPrefix.getPrefixLength());
      if (maskedInterfaceAddress.equals(networkAddress)) {
        // Found a longest prefix match, so found the network in this OSPF process for the iface
        return network;
      }
    }
    return null;
  }

  /**
   * Get the {@link OspfProcess} corresponding to the specified {@link Interface}
   *
   * <p>Returns {@code null} if the {@link Interface} does not have an {@link OspfProcess}
   * explicitly associated with it and does not overlap with an {@link OspfNetwork} in any {@link
   * OspfProcess} in the specified {@link Vrf}
   */
  private static @Nullable OspfProcess getOspfProcessForInterface(Vrf vrf, Interface iface) {
    if (iface.getOspfProcess() != null) {
      return vrf.getOspfProcesses().get(iface.getOspfProcess());
    }
    return vrf.getOspfProcesses().values().stream()
        .filter(p -> getOspfNetworkForInterface(iface, p) != null)
        .findFirst()
        .orElse(null);
  }

  private org.batfish.datamodel.Interface toInterface(
      String ifaceName, Interface iface, Configuration c) {
    org.batfish.datamodel.Interface newIface =
        org.batfish.datamodel.Interface.builder()
            .setName(ifaceName)
            .setOwner(c)
            .setType(computeInterfaceType(iface.getName(), c.getConfigurationFormat()))
            .build();
    if (newIface.getInterfaceType() == InterfaceType.VLAN) {
      Integer vlan = Ints.tryParse(ifaceName.substring("vlan".length()));
      newIface.setVlan(vlan);
      if (vlan == null) {
        _w.redFlag("Unable assign vlan for interface " + ifaceName);
      }
      newIface.setAutoState(iface.getAutoState());
    }
    String vrfName = iface.getVrf();
    Vrf vrf = _vrfs.computeIfAbsent(vrfName, Vrf::new);
    newIface.setDescription(iface.getDescription());
    if (!newIface.getActive()) {
      newIface.adminDown();
    }
    if (iface.getChannelGroup() != null) {
      // to handle nameif in setting channel group, get the alias from the interface if possible
      newIface.setChannelGroup(getNewInterfaceName(iface.getChannelGroup()));
    }
    newIface.setCryptoMap(iface.getCryptoMap());
    newIface.setHsrpVersion(iface.getHsrpVersion());
    newIface.setVrf(c.getVrfs().get(vrfName));
    newIface.setSpeed(
        firstNonNull(
            iface.getSpeed(),
            Interface.getDefaultSpeed(iface.getName(), c.getConfigurationFormat())));
    newIface.setBandwidth(
        firstNonNull(
            iface.getBandwidth(),
            newIface.getSpeed(),
            Interface.getDefaultBandwidth(iface.getName(), c.getConfigurationFormat())));
    if (iface.getDhcpRelayClient()) {
      newIface.setDhcpRelayAddresses(_dhcpRelayServers);
    } else {
      newIface.setDhcpRelayAddresses(ImmutableList.copyOf(iface.getDhcpRelayAddresses()));
    }
    newIface.setMlagId(iface.getMlagId());
    newIface.setMtu(iface.getMtu());
    newIface.setProxyArp(iface.getProxyArp());
    newIface.setSpanningTreePortfast(iface.getSpanningTreePortfast());
    newIface.setSwitchport(iface.getSwitchport());
    newIface.setDeclaredNames(ImmutableSortedSet.copyOf(iface.getDeclaredNames()));

    // All prefixes is the combination of the interface prefix + any secondary prefixes.
    ImmutableSet.Builder<InterfaceAddress> allPrefixes = ImmutableSet.builder();
    if (iface.getAddress() != null) {
      newIface.setAddress(iface.getAddress());
      allPrefixes.add(iface.getAddress());
    }
    allPrefixes.addAll(iface.getSecondaryAddresses());
    newIface.setAllAddresses(allPrefixes.build());
    newIface.setHsrpGroups(
        CollectionUtil.toImmutableMap(
            iface.getHsrpGroups(),
            Entry::getKey,
            e ->
                AsaConversions.toHsrpGroup(
                    e.getValue(), _trackingGroups.keySet(), newIface.getConcreteAddress())));

    EigrpProcess eigrpProcess = null;
    if (iface.getAddress() != null) {
      for (EigrpProcess process : vrf.getEigrpProcesses().values()) {
        if (process.getNetworks().contains(iface.getAddress().getPrefix())) {
          // Found a process on interface
          if (eigrpProcess != null) {
            // Cisco does not recommend running multiple EIGRP autonomous systems on the same
            // interface
            _w.redFlag("Interface: '" + iface.getName() + "' matches multiple EIGRP processes");
            break;
          }
          eigrpProcess = process;
        }
      }
    }
    // Let toEigrpProcess handle null asn failure
    if (eigrpProcess != null && eigrpProcess.getAsn() != null) {
      boolean passive =
          eigrpProcess
              .getInterfacePassiveStatus()
              .getOrDefault(getNewInterfaceName(iface), eigrpProcess.getPassiveInterfaceDefault());

      // Export distribute lists
      String exportPolicyName =
          eigrpNeighborExportPolicyName(ifaceName, vrfName, eigrpProcess.getAsn());
      RoutingPolicy exportPolicy =
          generateEigrpPolicy(
              c,
              this,
              Arrays.asList(
                  eigrpProcess.getOutboundGlobalDistributeList(),
                  eigrpProcess.getOutboundInterfaceDistributeLists().get(ifaceName)),
              ImmutableList.of(matchOwnAsn(eigrpProcess.getAsn())),
              exportPolicyName);
      c.getRoutingPolicies().put(exportPolicyName, exportPolicy);

      // Import distribute lists
      String importPolicyName =
          eigrpNeighborImportPolicyName(ifaceName, vrfName, eigrpProcess.getAsn());
      RoutingPolicy importPolicy =
          generateEigrpPolicy(
              c,
              this,
              Arrays.asList(
                  eigrpProcess.getInboundGlobalDistributeList(),
                  eigrpProcess.getInboundInterfaceDistributeLists().get(ifaceName)),
              ImmutableList.of(),
              importPolicyName);
      c.getRoutingPolicies().put(importPolicyName, importPolicy);

      newIface.setEigrp(
          EigrpInterfaceSettings.builder()
              .setAsn(eigrpProcess.getAsn())
              .setEnabled(true)
              .setExportPolicy(exportPolicyName)
              .setImportPolicy(importPolicyName)
              .setMetric(computeEigrpMetricForInterface(iface, eigrpProcess.getMode()))
              .setPassive(passive)
              .build());
      if (newIface.getEigrp() == null) {
        _w.redFlag("Interface: '" + iface.getName() + "' failed to set EIGRP settings");
      }
    }

    boolean level1 = false;
    boolean level2 = false;
    IsisProcess isisProcess = vrf.getIsisProcess();
    if (isisProcess != null && iface.getIsisInterfaceMode() != IsisInterfaceMode.UNSET) {
      switch (isisProcess.getLevel()) {
        case LEVEL_1:
          level1 = true;
          break;
        case LEVEL_1_2:
          level1 = true;
          level2 = true;
          break;
        case LEVEL_2:
          level2 = true;
          break;
        default:
          throw new VendorConversionException("Invalid IS-IS level");
      }
      IsisInterfaceSettings.Builder isisInterfaceSettingsBuilder = IsisInterfaceSettings.builder();
      IsisInterfaceLevelSettings levelSettings =
          IsisInterfaceLevelSettings.builder()
              .setCost(iface.getIsisCost())
              .setMode(iface.getIsisInterfaceMode())
              .build();
      if (level1) {
        isisInterfaceSettingsBuilder.setLevel1(levelSettings);
      }
      if (level2) {
        isisInterfaceSettingsBuilder.setLevel2(levelSettings);
      }
      newIface.setIsis(isisInterfaceSettingsBuilder.build());
    }

    // subinterface settings
    newIface.setEncapsulationVlan(iface.getEncapsulationVlan());

    // switch settings
    newIface.setAccessVlan(iface.getAccessVlan());

    if (iface.getSwitchportMode() == SwitchportMode.TRUNK) {
      newIface.setNativeVlan(firstNonNull(iface.getNativeVlan(), 1));
    }

    newIface.setSwitchportMode(iface.getSwitchportMode());
    SwitchportEncapsulationType encapsulation = iface.getSwitchportTrunkEncapsulation();
    if (encapsulation == null) { // no encapsulation set, so use default..
      // TODO: check if this is OK
      encapsulation = SwitchportEncapsulationType.DOT1Q;
    }
    newIface.setSwitchportTrunkEncapsulation(encapsulation);
    if (iface.getSwitchportMode() == SwitchportMode.TRUNK) {
      /*
       * Compute allowed VLANs:
       * - If allowed VLANs are set, honor them;
       */
      if (iface.getAllowedVlans() != null) {
        newIface.setAllowedVlans(iface.getAllowedVlans());
      } else {
        newIface.setAllowedVlans(Interface.ALL_VLANS);
      }
    }

    String incomingFilterName = iface.getIncomingFilter();
    if (incomingFilterName != null) {
      newIface.setIncomingFilter(c.getIpAccessLists().get(incomingFilterName));
    }
    String outgoingFilterName = iface.getOutgoingFilter();
    if (outgoingFilterName != null) {
      newIface.setOutgoingFilter(c.getIpAccessLists().get(outgoingFilterName));
    }
    // Apply zone outgoing filter if necessary
    applyZoneFilter(iface, newIface, c);

    List<AsaNat> nats = firstNonNull(_nats, ImmutableList.of());
    if (!nats.isEmpty()) {
      generateCiscoAsaNatTransformations(ifaceName, newIface, nats);
    }

    String routingPolicyName = iface.getRoutingPolicy();
    if (routingPolicyName != null) {
      newIface.setPacketPolicy(routingPolicyName);
    }

    newIface.setPostTransformationIncomingFilter(newIface.getIncomingFilter());
    newIface.setPreTransformationOutgoingFilter(newIface.getOutgoingFilter());
    newIface.setIncomingFilter(null);
    newIface.setOutgoingFilter(null);

    // Assume each interface has its own session info (sessions are not shared by interfaces).
    // That is, return flows can only enter the interface the forward flow exited in order to
    // match the session setup by the forward flow.
    newIface.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(
            Action.POST_NAT_FIB_LOOKUP, ImmutableSet.of(newIface.getName()), null, null));
    return newIface;
  }

  public static String eigrpNeighborImportPolicyName(String ifaceName, String vrfName, Long asn) {
    return String.format("~EIGRP_IMPORT_POLICY_%s_%s_%s~", vrfName, asn, ifaceName);
  }

  public static String eigrpNeighborExportPolicyName(String ifaceName, String vrfName, Long asn) {
    return String.format("~EIGRP_EXPORT_POLICY_%s_%s_%s~", vrfName, asn, ifaceName);
  }

  private @Nonnull EigrpMetric computeEigrpMetricForInterface(
      Interface iface, EigrpProcessMode mode) {
    Long bw =
        Stream.of(
                iface.getBandwidth(),
                Interface.getDefaultBandwidth(iface.getName(), ConfigurationFormat.CISCO_ASA))
            .filter(Objects::nonNull)
            .findFirst()
            .map(bandwidth -> bandwidth.longValue() / 1000) // convert to kbps
            .orElse(null);
    // Bandwidth can be null for port-channels (will be calculated later).
    if (bw == null) {
      InterfaceType ifaceType =
          computeInterfaceType(iface.getName(), ConfigurationFormat.CISCO_ASA);
      assert ifaceType == InterfaceType.AGGREGATED || ifaceType == InterfaceType.AGGREGATE_CHILD;
    }
    EigrpMetricValues values =
        EigrpMetricValues.builder()
            .setDelay(
                firstNonNull(
                    iface.getDelay(),
                    Interface.getDefaultDelay(iface.getName(), ConfigurationFormat.CISCO_ASA, bw)))
            .setBandwidth(bw)
            .build();
    if (mode == EigrpProcessMode.CLASSIC) {
      return ClassicMetric.builder().setValues(values).build();
    } else if (mode == EigrpProcessMode.NAMED) {
      return WideMetric.builder().setValues(values).build();
    } else {
      throw new IllegalArgumentException("Invalid EIGRP process mode: " + mode);
    }
  }

  private void generateCiscoAsaNatTransformations(
      String ifaceName, org.batfish.datamodel.Interface newIface, List<AsaNat> nats) {

    if (!nats.stream().map(AsaNat::getSection).allMatch(Section.OBJECT::equals)) {
      _w.unimplemented("No support for Twice NAT");
    }

    // ASA places incoming and outgoing object NATs as transformations on the outside interface.
    // Each NAT rule specifies an outside interface or ANY_INTERFACE
    SortedSet<AsaNat> objectNats =
        nats.stream()
            .filter(nat -> nat.getSection().equals(Section.OBJECT))
            .filter(
                nat ->
                    nat.getOutsideInterface().equals(AsaNat.ANY_INTERFACE)
                        || nat.getOutsideInterface().equals(ifaceName))
            .collect(Collectors.toCollection(TreeSet::new));

    newIface.setIncomingTransformation(
        objectNats.stream()
            .map(nat -> nat.toIncomingTransformation(_networkObjects, _w))
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toList(), AsaNatUtil::toTransformationChain)));

    newIface.setOutgoingTransformation(
        objectNats.stream()
            .map(nat -> nat.toOutgoingTransformation(_networkObjects, _w))
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toList(), AsaNatUtil::toTransformationChain)));
  }

  private void applyZoneFilter(
      Interface iface, org.batfish.datamodel.Interface newIface, Configuration c) {
    if (getASASecurityLevelZoneName(iface) != null) {
      applyASASecurityLevelFilter(iface, newIface, c);
    }
  }

  private void applyASASecurityLevelFilter(
      Interface iface, org.batfish.datamodel.Interface newIface, Configuration c) {
    String zoneName =
        checkNotNull(
            getASASecurityLevelZoneName(iface),
            "interface %s is not in a security level",
            iface.getName());
    String interSecurityLevelAclName = computeZoneOutgoingAclName(zoneName);
    IpAccessList interSecurityLevelAcl = c.getIpAccessLists().get(interSecurityLevelAclName);
    if (interSecurityLevelAcl == null) {
      return;
    }
    IpAccessList oldOutgoingFilter = newIface.getOutgoingFilter();
    String oldOutgoingFilterName = oldOutgoingFilter == null ? null : oldOutgoingFilter.getName();

    // Construct a new ACL that:
    // 1) rejects if the (inter- or intra-) security-level policy rejects
    // 2) rejects if both of the following are true:
    //    a) the (inter- or intra-) security-level policy permits
    //    b) the interface outbound filter REJECTS
    // 3) permits if both of the following are true:
    //    a) the (inter- or intra-) security-level policy permits
    //    b) the interface outbound filter permits
    //
    // NOTE: step 3 is mutually exclusive with each of steps 1 and 2. We do steps
    // 1 and 2 to produce better traces than we would with step 3 alone (letting the flows
    // that would be matched by steps 1 and 2 be denied by the default no-match semantics).

    ImmutableList.Builder<AclLine> lineBuilder = ImmutableList.builder();

    // Step 1.
    // 1a. intra-security-level
    lineBuilder.add(ExprAclLine.rejecting(getAsaIntraSecurityLevelDenyExpr(iface, newIface)));
    // 1b. inter-security-level
    lineBuilder.addAll(getAsaInterSecurityLevelDenyAclLines(iface.getSecurityLevel()));

    AclLineMatchExpr securityLevelPolicies =
        or(
            new PermittedByAcl(interSecurityLevelAclName),
            getAsaIntraSecurityLevelPermitExpr(iface, newIface));

    // Step 2.
    if (oldOutgoingFilterName != null) {
      lineBuilder.add(
          ExprAclLine.rejecting()
              .setMatchCondition(
                  and(
                      securityLevelPolicies,
                      new DeniedByAcl(
                          oldOutgoingFilterName,
                          asaDeniedByOutputFilterTraceElement(
                              _filename, c.getIpAccessLists().get(oldOutgoingFilterName)))))
              .build());
    }

    // Step 3.

    if (oldOutgoingFilterName != null) {
      lineBuilder.add(
          ExprAclLine.accepting()
              .setMatchCondition(
                  and(
                      securityLevelPolicies,
                      new PermittedByAcl(
                          oldOutgoingFilterName,
                          asaPermittedByOutputFilterTraceElement(
                              _filename, c.getIpAccessLists().get(oldOutgoingFilterName)))))
              .build());
    } else {
      lineBuilder.add(ExprAclLine.accepting().setMatchCondition(securityLevelPolicies).build());
    }
    newIface.setOutgoingFilter(
        IpAccessList.builder()
            .setOwner(c)
            .setName(computeCombinedOutgoingAclName(newIface.getName()))
            .setLines(lineBuilder.build())
            .build());
  }

  /**
   * Drop outbound traffic from interfaces with lower security levels if that interface does not
   * have an inbound ACL. Note: this could be shared among out-interfaces in the same security
   * level, but for now we're recomputing it for each one.
   */
  private @Nonnull List<ExprAclLine> getAsaInterSecurityLevelDenyAclLines(int level) {
    return _interfacesBySecurityLevel.keySet().stream()
        .filter(l -> l < level)
        .map(
            l -> {
              List<String> denySrcInterfaces =
                  _interfacesBySecurityLevel.get(l).stream()
                      .filter(inIface -> inIface.getIncomingFilter() == null)
                      .map(this::getNewInterfaceName)
                      .collect(Collectors.toList());
              if (denySrcInterfaces.isEmpty()) {
                return null;
              }
              return ExprAclLine.rejecting()
                  .setName("Traffic from security level " + l + " without inbound filter")
                  .setTraceElement(asaRejectLowerSecurityLevelTraceElement(l))
                  .setMatchCondition(new MatchSrcInterface(denySrcInterfaces))
                  .build();
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Construct an {@link AclLineMatchExpr} that matches traffic NOT allowed to exit the specified
   * {@code iface} after entering the same interface or another interface in the same security
   * level. Only for Cisco ASA devices.
   */
  private AclLineMatchExpr getAsaIntraSecurityLevelDenyExpr(
      Interface iface, org.batfish.datamodel.Interface newIface) {
    checkNotNull(iface.getSecurityLevel(), "interface %s not in a security level", iface.getName());
    ImmutableList.Builder<AclLineMatchExpr> disjuncts = ImmutableList.builder();

    if (!_sameSecurityTrafficIntra) {
      // reject traffic received on the outgoing interface (hairpinning)
      disjuncts.add(
          new MatchSrcInterface(
              ImmutableList.of(newIface.getName()),
              DENY_SAME_SECURITY_TRAFFIC_INTRA_TRACE_ELEMENT));
    }

    if (!_sameSecurityTrafficInter) {
      // reject traffic received on another interface in the same security level
      disjuncts.add(
          new MatchSrcInterface(
              _interfacesBySecurityLevel.get(iface.getSecurityLevel()).stream()
                  .filter(other -> !other.equals(iface))
                  .map(this::getNewInterfaceName)
                  .collect(ImmutableList.toImmutableList()),
              DENY_SAME_SECURITY_TRAFFIC_INTER_TRACE_ELEMENT));
    }
    return or(disjuncts.build()); // no trace element for the or
  }

  /**
   * Construct an {@link AclLineMatchExpr} that matches traffic allowed to exit the specified {@code
   * iface} after entering the same interface or another interface in the same security level. Only
   * for Cisco ASA devices.
   */
  private AclLineMatchExpr getAsaIntraSecurityLevelPermitExpr(
      Interface iface, org.batfish.datamodel.Interface newIface) {
    checkNotNull(iface.getSecurityLevel(), "interface %s not in a security level", iface.getName());
    ImmutableList.Builder<AclLineMatchExpr> exprs = ImmutableList.builder();

    // handle traffic received on the outgoing interface (hairpinning)
    if (_sameSecurityTrafficIntra) {
      exprs.add(
          new MatchSrcInterface(
              ImmutableList.of(newIface.getName()),
              PERMIT_SAME_SECURITY_TRAFFIC_INTRA_TRACE_ELEMENT));
    }

    // handle traffic received on another interface in the same security level
    if (_sameSecurityTrafficInter) {
      exprs.add(
          new MatchSrcInterface(
              _interfacesBySecurityLevel.get(iface.getSecurityLevel()).stream()
                  .filter(other -> !other.equals(iface))
                  .map(this::getNewInterfaceName)
                  .collect(ImmutableList.toImmutableList()),
              PERMIT_SAME_SECURITY_TRAFFIC_INTER_TRACE_ELEMENT));
    }

    return or(exprs.build()); // no trace element for the or
  }

  public static String computeCombinedOutgoingAclName(String interfaceName) {
    return String.format("~COMBINED_OUTGOING_ACL~%s~", interfaceName);
  }

  // For testing.
  If convertOspfRedistributionPolicy(OspfRedistributionPolicy policy, OspfProcess proc) {
    RoutingProtocol protocol = policy.getSourceProtocol();
    // All redistribution must match the specified protocol.
    Conjunction ospfExportConditions = new Conjunction();
    if (protocol == RoutingProtocol.EIGRP) {
      ospfExportConditions
          .getConjuncts()
          .add(new MatchProtocol(RoutingProtocol.EIGRP, RoutingProtocol.EIGRP_EX));
    } else if (protocol == RoutingProtocol.ISIS_ANY) {
      ospfExportConditions
          .getConjuncts()
          .add(
              new MatchProtocol(
                  RoutingProtocol.ISIS_EL1,
                  RoutingProtocol.ISIS_EL2,
                  RoutingProtocol.ISIS_L1,
                  RoutingProtocol.ISIS_L2));
    } else {
      ospfExportConditions.getConjuncts().add(new MatchProtocol(protocol));
    }

    // Do not redistribute the default route.
    ospfExportConditions.getConjuncts().add(NOT_DEFAULT_ROUTE);

    ImmutableList.Builder<Statement> ospfExportStatements = ImmutableList.builder();

    // Set the metric type and value.
    ospfExportStatements.add(new SetOspfMetricType(policy.getMetricType()));
    long metric = policy.getMetric() != null ? policy.getMetric() : proc.getDefaultMetric(protocol);
    ospfExportStatements.add(new SetMetric(new LiteralLong(metric)));

    // If only classful routes should be redistributed, filter to classful routes.
    if (policy.getOnlyClassfulRoutes()) {
      ospfExportConditions.getConjuncts().add(RouteIsClassful.instance());
    }

    // If a route-map filter is present, honor it.
    String exportRouteMapName = policy.getRouteMap();
    if (exportRouteMapName != null) {
      RouteMap exportRouteMap = _routeMaps.get(exportRouteMapName);
      if (exportRouteMap != null) {
        ospfExportConditions.getConjuncts().add(new CallExpr(exportRouteMapName));
      }
    }

    ospfExportStatements.add(Statements.ExitAccept.toStaticStatement());

    // Construct the policy and add it before returning.
    return new If(
        "OSPF export routes for " + protocol.protocolName(),
        ospfExportConditions,
        ospfExportStatements.build(),
        ImmutableList.of());
  }

  private org.batfish.datamodel.ospf.OspfProcess toOspfProcess(
      OspfProcess proc, String vrfName, Configuration c, AsaConfiguration oldConfig) {
    Ip routerId = proc.getRouterId();
    if (routerId == null) {
      routerId = AsaConversions.getHighestIp(oldConfig.getInterfaces());
      if (routerId == Ip.ZERO) {
        _w.redFlag("No candidates for OSPF router-id");
        return null;
      }
    }
    org.batfish.datamodel.ospf.OspfProcess newProcess =
        org.batfish.datamodel.ospf.OspfProcess.builder()
            .setProcessId(proc.getName())
            .setReferenceBandwidth(proc.getReferenceBandwidth())
            .setAdminCosts(
                org.batfish.datamodel.ospf.OspfProcess.computeDefaultAdminCosts(
                    c.getConfigurationFormat()))
            .setSummaryAdminCost(
                RoutingProtocol.OSPF_IA.getSummaryAdministrativeCost(c.getConfigurationFormat()))
            .setRouterId(routerId)
            .build();

    if (proc.getMaxMetricRouterLsa()) {
      newProcess.setMaxMetricTransitLinks(OspfProcess.MAX_METRIC_ROUTER_LSA);
      if (proc.getMaxMetricIncludeStub()) {
        newProcess.setMaxMetricStubNetworks(OspfProcess.MAX_METRIC_ROUTER_LSA);
      }
      newProcess.setMaxMetricExternalNetworks(proc.getMaxMetricExternalLsa());
      newProcess.setMaxMetricSummaryNetworks(proc.getMaxMetricSummaryLsa());
    }

    // establish areas and associated interfaces
    Map<Long, OspfArea.Builder> areas = new HashMap<>();
    Map<Long, ImmutableSortedSet.Builder<String>> areaInterfacesBuilders = new HashMap<>();

    // Set RFC 1583 compatibility
    newProcess.setRfc1583Compatible(proc.getRfc1583Compatible());

    for (Entry<String, org.batfish.datamodel.Interface> e :
        c.getAllInterfaces(vrfName).entrySet()) {
      org.batfish.datamodel.Interface iface = e.getValue();
      /*
       * Filter out interfaces that do not belong to this process, however if the process name is missing,
       * proceed down to inference based on network addresses.
       */
      Interface vsIface = _interfaces.get(iface.getName());
      if (vsIface == null) {
        // Need to look at aliases because in ASA the VI model iface will be named using the alias
        vsIface =
            _interfaces.values().stream()
                .filter(i -> iface.getName().equals(i.getAlias()))
                .findFirst()
                .get();
      }
      if (vsIface.getOspfProcess() != null && !vsIface.getOspfProcess().equals(proc.getName())) {
        continue;
      }
      OspfNetwork network = getOspfNetworkForInterface(vsIface, proc);
      if (vsIface.getOspfProcess() == null && network == null) {
        // Interface is not in an OspfNetwork on this process
        continue;
      }

      String ifaceName = e.getKey();
      Long areaNum = vsIface.getOspfArea();
      // OSPF area number was not configured on the interface itself, so get from OspfNetwork
      if (areaNum == null) {
        if (network == null) {
          continue;
        }
        areaNum = network.getArea();
      }
      areas.computeIfAbsent(areaNum, areaNumber -> OspfArea.builder().setNumber(areaNumber));
      ImmutableSortedSet.Builder<String> newAreaInterfacesBuilder =
          areaInterfacesBuilders.computeIfAbsent(areaNum, n -> ImmutableSortedSet.naturalOrder());
      newAreaInterfacesBuilder.add(ifaceName);
      finalizeInterfaceOspfSettings(iface, vsIface, proc, areaNum);
    }
    areaInterfacesBuilders.forEach(
        (areaNum, interfacesBuilder) ->
            areas.get(areaNum).addInterfaces(interfacesBuilder.build()));
    proc.getNssas()
        .forEach(
            (areaId, nssaSettings) -> {
              if (!areas.containsKey(areaId)) {
                return;
              }
              areas.get(areaId).setStubType(StubType.NSSA);
              areas.get(areaId).setNssaSettings(toNssaSettings(nssaSettings));
            });

    proc.getStubs()
        .forEach(
            (areaId, stubSettings) -> {
              if (!areas.containsKey(areaId)) {
                return;
              }
              areas.get(areaId).setStubType(StubType.STUB);
              areas.get(areaId).setStubSettings(toStubSettings(stubSettings));
            });

    // create summarization filters for inter-area routes
    for (Entry<Long, Map<Prefix, OspfAreaSummary>> e1 : proc.getSummaries().entrySet()) {
      long areaLong = e1.getKey();
      Map<Prefix, OspfAreaSummary> summaries = e1.getValue();
      OspfArea.Builder area = areas.get(areaLong);
      String summaryFilterName = "~OSPF_SUMMARY_FILTER:" + vrfName + ":" + areaLong + "~";
      RouteFilterList summaryFilter = new RouteFilterList(summaryFilterName);
      c.getRouteFilterLists().put(summaryFilterName, summaryFilter);
      if (area == null) {
        area = OspfArea.builder().setNumber(areaLong);
        areas.put(areaLong, area);
      }
      area.setSummaryFilter(summaryFilterName);
      for (Entry<Prefix, OspfAreaSummary> e2 : summaries.entrySet()) {
        Prefix prefix = e2.getKey();
        OspfAreaSummary summary = e2.getValue();
        int prefixLength = prefix.getPrefixLength();
        int filterMinPrefixLength =
            summary.isAdvertised()
                ? Math.min(Prefix.MAX_PREFIX_LENGTH, prefixLength + 1)
                : prefixLength;
        summaryFilter.addLine(
            new RouteFilterLine(
                LineAction.DENY,
                IpWildcard.create(prefix),
                new SubRange(filterMinPrefixLength, Prefix.MAX_PREFIX_LENGTH)));
      }
      area.addSummaries(ImmutableSortedMap.copyOf(summaries));
      summaryFilter.addLine(
          new RouteFilterLine(
              LineAction.PERMIT,
              IpWildcard.create(Prefix.ZERO),
              new SubRange(0, Prefix.MAX_PREFIX_LENGTH)));
    }
    newProcess.setAreas(toImmutableSortedMap(areas, Entry::getKey, e -> e.getValue().build()));

    String ospfExportPolicyName = generatedOspfExportPolicyName(vrfName, proc.getName());
    RoutingPolicy ospfExportPolicy = new RoutingPolicy(ospfExportPolicyName, c);
    c.getRoutingPolicies().put(ospfExportPolicyName, ospfExportPolicy);
    List<Statement> ospfExportStatements = ospfExportPolicy.getStatements();
    newProcess.setExportPolicy(ospfExportPolicyName);

    // policy map for default information
    if (proc.getDefaultInformationOriginate()) {
      If ospfExportDefault = new If();
      ospfExportStatements.add(ospfExportDefault);
      ospfExportDefault.setComment("OSPF export default route");
      List<Statement> ospfExportDefaultStatements = ospfExportDefault.getTrueStatements();
      long metric = proc.getDefaultInformationMetric();
      ospfExportDefaultStatements.add(new SetMetric(new LiteralLong(metric)));
      OspfMetricType metricType = proc.getDefaultInformationMetricType();
      ospfExportDefaultStatements.add(new SetOspfMetricType(metricType));
      // add default export map with metric
      String defaultOriginateMapName = proc.getDefaultInformationOriginateMap();
      GeneratedRoute.Builder route =
          GeneratedRoute.builder()
              .setNetwork(Prefix.ZERO)
              .setNonRouting(true)
              .setAdmin(MAX_ADMINISTRATIVE_COST);
      if (defaultOriginateMapName != null) {
        RoutingPolicy ospfDefaultGenerationPolicy =
            c.getRoutingPolicies().get(defaultOriginateMapName);
        if (ospfDefaultGenerationPolicy != null) {
          // TODO This should depend on a default route existing, unless `always` is configured
          // If `always` is configured, maybe the route-map should be ignored. Needs GNS3 check.
          route.setGenerationPolicy(defaultOriginateMapName);
          newProcess.addGeneratedRoute(route.build());
        }
      } else if (proc.getDefaultInformationOriginateAlways()) {
        // add generated aggregate with no precondition
        newProcess.addGeneratedRoute(route.build());
      } else {
        // Use a generated route that will only be generated if a default route exists in RIB
        String defaultRouteGenerationPolicyName =
            generatedOspfDefaultRouteGenerationPolicyName(vrfName, proc.getName());
        RoutingPolicy.builder()
            .setOwner(c)
            .setName(defaultRouteGenerationPolicyName)
            .addStatement(
                new If(
                    Common.matchDefaultRoute(),
                    ImmutableList.of(Statements.ReturnTrue.toStaticStatement())))
            .build();
        route.setGenerationPolicy(defaultRouteGenerationPolicyName);
        newProcess.addGeneratedRoute(route.build());
      }
      ospfExportDefaultStatements.add(Statements.ExitAccept.toStaticStatement());
      ospfExportDefault.setGuard(
          new Conjunction(
              ImmutableList.of(
                  Common.matchDefaultRoute(), new MatchProtocol(RoutingProtocol.AGGREGATE))));
    }

    computeDistributeListPolicies(proc, newProcess, c, vrfName, proc.getName(), oldConfig, _w);

    // policies for redistributing routes
    ospfExportStatements.addAll(
        proc.getRedistributionPolicies().values().stream()
            .map(policy -> convertOspfRedistributionPolicy(policy, proc))
            .collect(Collectors.toList()));

    return newProcess;
  }

  /** Setup OSPF settings on specified VI interface. */
  private void finalizeInterfaceOspfSettings(
      org.batfish.datamodel.Interface iface,
      Interface vsIface,
      @Nullable OspfProcess proc,
      @Nullable Long areaNum) {
    String ifaceName = vsIface.getName();
    OspfInterfaceSettings.Builder ospfSettings = OspfInterfaceSettings.builder().setPassive(false);
    if (proc != null) {
      ospfSettings.setProcess(proc.getName());
      if (firstNonNull(
          vsIface.getOspfPassive(),
          proc.getPassiveInterfaces().contains(ifaceName)
              || (proc.getPassiveInterfaceDefault()
                  ^ proc.getNonDefaultInterfaces().contains(ifaceName)))) {
        proc.getPassiveInterfaces().add(ifaceName);
        ospfSettings.setPassive(true);
      }
    }
    ospfSettings.setHelloMultiplier(vsIface.getOspfHelloMultiplier());

    ospfSettings.setAreaName(areaNum);
    ospfSettings.setEnabled(proc != null && areaNum != null && !vsIface.getOspfShutdown());
    org.batfish.datamodel.ospf.OspfNetworkType networkType =
        toOspfNetworkType(vsIface.getOspfNetworkType(), _w);
    ospfSettings.setNetworkType(networkType);
    if (vsIface.getOspfCost() == null
        && iface.isLoopback()
        && networkType != OspfNetworkType.POINT_TO_POINT) {
      ospfSettings.setCost(DEFAULT_LOOPBACK_OSPF_COST);
    } else {
      ospfSettings.setCost(vsIface.getOspfCost());
    }
    ospfSettings.setHelloInterval(toOspfHelloInterval(vsIface, networkType));
    ospfSettings.setDeadInterval(toOspfDeadInterval(vsIface, networkType));

    iface.setOspfSettings(ospfSettings.build());
  }

  private org.batfish.datamodel.ospf.StubSettings toStubSettings(StubSettings stubSettings) {
    return org.batfish.datamodel.ospf.StubSettings.builder()
        .setSuppressType3(stubSettings.getNoSummary())
        .build();
  }

  private org.batfish.datamodel.ospf.NssaSettings toNssaSettings(NssaSettings nssaSettings) {
    return org.batfish.datamodel.ospf.NssaSettings.builder()
        .setDefaultOriginateType(
            nssaSettings.getDefaultInformationOriginate()
                ? OspfDefaultOriginateType.INTER_AREA
                : OspfDefaultOriginateType.NONE)
        .setSuppressType3(nssaSettings.getNoSummary())
        .setSuppressType7(nssaSettings.getNoRedistribution())
        .build();
  }

  private org.batfish.datamodel.RipProcess toRipProcess(
      RipProcess proc, String vrfName, Configuration c) {
    org.batfish.datamodel.RipProcess newProcess = new org.batfish.datamodel.RipProcess();

    // establish areas and associated interfaces
    SortedSet<Prefix> networks = proc.getNetworks();
    for (Entry<String, org.batfish.datamodel.Interface> e :
        c.getAllInterfaces(vrfName).entrySet()) {
      String ifaceName = e.getKey();
      org.batfish.datamodel.Interface i = e.getValue();
      ConcreteInterfaceAddress interfaceAddress = i.getConcreteAddress();
      if (interfaceAddress == null) {
        continue;
      }
      Prefix interfaceNetwork = interfaceAddress.getPrefix();
      if (networks.contains(interfaceNetwork)) {
        newProcess.getInterfaces().add(ifaceName);
        i.setRipEnabled(true);
        boolean passive =
            proc.getPassiveInterfaceList().contains(i.getName())
                || (proc.getPassiveInterfaceDefault()
                    && !proc.getActiveInterfaceList().contains(ifaceName));
        i.setRipPassive(passive);
      }
    }

    String ripExportPolicyName = "~RIP_EXPORT_POLICY:" + vrfName + "~";
    RoutingPolicy ripExportPolicy = new RoutingPolicy(ripExportPolicyName, c);
    c.getRoutingPolicies().put(ripExportPolicyName, ripExportPolicy);
    List<Statement> ripExportStatements = ripExportPolicy.getStatements();
    newProcess.setExportPolicy(ripExportPolicyName);

    // policy map for default information
    if (proc.getDefaultInformationOriginate()) {
      If ripExportDefault = new If();
      ripExportStatements.add(ripExportDefault);
      ripExportDefault.setComment("RIP export default route");
      Conjunction ripExportDefaultConditions = new Conjunction();
      List<Statement> ripExportDefaultStatements = ripExportDefault.getTrueStatements();
      ripExportDefaultConditions.getConjuncts().add(Common.matchDefaultRoute());
      long metric = proc.getDefaultInformationMetric();
      ripExportDefaultStatements.add(new SetMetric(new LiteralLong(metric)));
      // add default export map with metric
      String defaultOriginateMapName = proc.getDefaultInformationOriginateMap();
      if (defaultOriginateMapName != null) {
        RoutingPolicy ripDefaultGenerationPolicy =
            c.getRoutingPolicies().get(defaultOriginateMapName);
        if (ripDefaultGenerationPolicy != null) {
          GeneratedRoute.Builder route = GeneratedRoute.builder();
          route.setNetwork(Prefix.ZERO);
          route.setAdmin(MAX_ADMINISTRATIVE_COST);
          route.setGenerationPolicy(defaultOriginateMapName);
          newProcess.getGeneratedRoutes().add(route.build());
        }
      } else {
        // add generated aggregate with no precondition
        GeneratedRoute.Builder route = GeneratedRoute.builder();
        route.setNetwork(Prefix.ZERO);
        route.setAdmin(MAX_ADMINISTRATIVE_COST);
        newProcess.getGeneratedRoutes().add(route.build());
      }
      ripExportDefaultConditions.getConjuncts().add(new MatchProtocol(RoutingProtocol.AGGREGATE));
      ripExportDefaultStatements.add(Statements.ExitAccept.toStaticStatement());
      ripExportDefault.setGuard(ripExportDefaultConditions);
    }

    // policy for redistributing connected routes
    RipRedistributionPolicy rcp = proc.getRedistributionPolicies().get(RoutingProtocol.CONNECTED);
    if (rcp != null) {
      If ripExportConnected = new If();
      ripExportConnected.setComment("RIP export connected routes");
      Conjunction ripExportConnectedConditions = new Conjunction();
      ripExportConnectedConditions.getConjuncts().add(new MatchProtocol(RoutingProtocol.CONNECTED));
      List<Statement> ripExportConnectedStatements = ripExportConnected.getTrueStatements();

      Long metric = rcp.getMetric();
      boolean explicitMetric = metric != null;
      if (!explicitMetric) {
        metric = RipRedistributionPolicy.DEFAULT_REDISTRIBUTE_CONNECTED_METRIC;
      }
      ripExportStatements.add(new SetMetric(new LiteralLong(metric)));
      ripExportStatements.add(ripExportConnected);
      // add default export map with metric
      String exportConnectedRouteMapName = rcp.getRouteMap();
      if (exportConnectedRouteMapName != null) {
        RouteMap exportConnectedRouteMap = _routeMaps.get(exportConnectedRouteMapName);
        if (exportConnectedRouteMap != null) {
          ripExportConnectedConditions
              .getConjuncts()
              .add(new CallExpr(exportConnectedRouteMapName));
        }
      }
      ripExportConnectedStatements.add(Statements.ExitAccept.toStaticStatement());
      ripExportConnected.setGuard(ripExportConnectedConditions);
    }

    // policy map for redistributing static routes
    RipRedistributionPolicy rsp = proc.getRedistributionPolicies().get(RoutingProtocol.STATIC);
    if (rsp != null) {
      If ripExportStatic = new If();
      ripExportStatic.setComment("RIP export static routes");
      Conjunction ripExportStaticConditions = new Conjunction();
      ripExportStaticConditions.getConjuncts().add(new MatchProtocol(RoutingProtocol.STATIC));
      List<Statement> ripExportStaticStatements = ripExportStatic.getTrueStatements();
      ripExportStaticConditions.getConjuncts().add(NOT_DEFAULT_ROUTE);

      Long metric = rsp.getMetric();
      boolean explicitMetric = metric != null;
      if (!explicitMetric) {
        metric = RipRedistributionPolicy.DEFAULT_REDISTRIBUTE_STATIC_METRIC;
      }
      ripExportStatements.add(new SetMetric(new LiteralLong(metric)));
      ripExportStatements.add(ripExportStatic);
      // add export map with metric
      String exportStaticRouteMapName = rsp.getRouteMap();
      if (exportStaticRouteMapName != null) {
        RouteMap exportStaticRouteMap = _routeMaps.get(exportStaticRouteMapName);
        if (exportStaticRouteMap != null) {
          ripExportStaticConditions.getConjuncts().add(new CallExpr(exportStaticRouteMapName));
        }
      }
      ripExportStaticStatements.add(Statements.ExitAccept.toStaticStatement());
      ripExportStatic.setGuard(ripExportStaticConditions);
    }

    // policy map for redistributing bgp routes
    RipRedistributionPolicy rbp = proc.getRedistributionPolicies().get(RoutingProtocol.BGP);
    if (rbp != null) {
      If ripExportBgp = new If();
      ripExportBgp.setComment("RIP export bgp routes");
      Conjunction ripExportBgpConditions = new Conjunction();
      ripExportBgpConditions.getConjuncts().add(new MatchProtocol(RoutingProtocol.BGP));
      List<Statement> ripExportBgpStatements = ripExportBgp.getTrueStatements();
      ripExportBgpConditions.getConjuncts().add(NOT_DEFAULT_ROUTE);

      Long metric = rbp.getMetric();
      boolean explicitMetric = metric != null;
      if (!explicitMetric) {
        metric = RipRedistributionPolicy.DEFAULT_REDISTRIBUTE_BGP_METRIC;
      }
      ripExportStatements.add(new SetMetric(new LiteralLong(metric)));
      ripExportStatements.add(ripExportBgp);
      // add export map with metric
      String exportBgpRouteMapName = rbp.getRouteMap();
      if (exportBgpRouteMapName != null) {
        RouteMap exportBgpRouteMap = _routeMaps.get(exportBgpRouteMapName);
        if (exportBgpRouteMap != null) {
          ripExportBgpConditions.getConjuncts().add(new CallExpr(exportBgpRouteMapName));
        }
      }
      ripExportBgpStatements.add(Statements.ExitAccept.toStaticStatement());
      ripExportBgp.setGuard(ripExportBgpConditions);
    }
    return newProcess;
  }

  private RoutingPolicy toRoutingPolicy(Configuration c, RouteMap map) {
    boolean hasContinue =
        map.getClauses().values().stream().anyMatch(clause -> clause.getContinueLine() != null);
    if (hasContinue) {
      return toRoutingPolicies(c, map);
    }
    RoutingPolicy output = new RoutingPolicy(map.getName(), c);
    List<Statement> statements = output.getStatements();
    Map<Integer, If> clauses = new HashMap<>();
    // descend map so continue targets are available
    If followingClause = null;
    for (Entry<Integer, RouteMapClause> e : map.getClauses().descendingMap().entrySet()) {
      int clauseNumber = e.getKey();
      RouteMapClause rmClause = e.getValue();
      String clausePolicyName = getRouteMapClausePolicyName(map, clauseNumber);
      Conjunction conj = new Conjunction();
      // match ipv4s must be disjoined with match ipv6
      Disjunction matchIpOrPrefix = new Disjunction();
      for (RouteMapMatchLine rmMatch : rmClause.getMatchList()) {
        BooleanExpr matchExpr = rmMatch.toBooleanExpr(c, this, _w);
        if (rmMatch instanceof RouteMapMatchIpAccessListLine
            || rmMatch instanceof RouteMapMatchIpPrefixListLine
            || rmMatch instanceof RouteMapMatchIpv6AccessListLine
            || rmMatch instanceof RouteMapMatchIpv6PrefixListLine) {
          matchIpOrPrefix.getDisjuncts().add(matchExpr);
        } else {
          conj.getConjuncts().add(matchExpr);
        }
      }
      if (!matchIpOrPrefix.getDisjuncts().isEmpty()) {
        conj.getConjuncts().add(matchIpOrPrefix);
      }
      If ifExpr = new If();
      clauses.put(clauseNumber, ifExpr);
      ifExpr.setComment(clausePolicyName);
      ifExpr.setGuard(conj);
      List<Statement> matchStatements = ifExpr.getTrueStatements();
      for (RouteMapSetLine rmSet : rmClause.getSetList()) {
        rmSet.applyTo(matchStatements, this, c, _w);
      }
      switch (rmClause.getAction()) {
        case PERMIT:
          matchStatements.add(Statements.ReturnTrue.toStaticStatement());
          break;

        case DENY:
          matchStatements.add(Statements.ReturnFalse.toStaticStatement());
          break;

        default:
          throw new BatfishException("Invalid action");
      }
      if (followingClause != null) {
        ifExpr.getFalseStatements().add(followingClause);
      } else {
        ifExpr.getFalseStatements().add(Statements.ReturnLocalDefaultAction.toStaticStatement());
      }
      followingClause = ifExpr;
    }
    statements.add(followingClause);
    return output;
  }

  private RoutingPolicy toRoutingPolicies(Configuration c, RouteMap map) {
    RoutingPolicy output = new RoutingPolicy(map.getName(), c);
    List<Statement> statements = output.getStatements();
    Map<Integer, RoutingPolicy> clauses = new HashMap<>();
    // descend map so continue targets are available
    RoutingPolicy followingClause = null;
    Integer followingClauseNumber = null;
    for (Entry<Integer, RouteMapClause> e : map.getClauses().descendingMap().entrySet()) {
      int clauseNumber = e.getKey();
      RouteMapClause rmClause = e.getValue();
      String clausePolicyName = getRouteMapClausePolicyName(map, clauseNumber);
      Conjunction conj = new Conjunction();
      // match ipv4s must be disjoined with match ipv6
      Disjunction matchIpOrPrefix = new Disjunction();
      for (RouteMapMatchLine rmMatch : rmClause.getMatchList()) {
        BooleanExpr matchExpr = rmMatch.toBooleanExpr(c, this, _w);
        if (rmMatch instanceof RouteMapMatchIpAccessListLine
            || rmMatch instanceof RouteMapMatchIpPrefixListLine
            || rmMatch instanceof RouteMapMatchIpv6AccessListLine
            || rmMatch instanceof RouteMapMatchIpv6PrefixListLine) {
          matchIpOrPrefix.getDisjuncts().add(matchExpr);
        } else {
          conj.getConjuncts().add(matchExpr);
        }
      }
      if (!matchIpOrPrefix.getDisjuncts().isEmpty()) {
        conj.getConjuncts().add(matchIpOrPrefix);
      }
      RoutingPolicy clausePolicy = new RoutingPolicy(clausePolicyName, c);
      c.getRoutingPolicies().put(clausePolicyName, clausePolicy);
      If ifStatement = new If();
      clausePolicy.getStatements().add(ifStatement);
      clauses.put(clauseNumber, clausePolicy);
      ifStatement.setComment(clausePolicyName);
      ifStatement.setGuard(conj);
      List<Statement> onMatchStatements = ifStatement.getTrueStatements();
      for (RouteMapSetLine rmSet : rmClause.getSetList()) {
        rmSet.applyTo(onMatchStatements, this, c, _w);
      }
      RouteMapContinue continueStatement = rmClause.getContinueLine();
      Integer continueTarget = null;
      RoutingPolicy continueTargetPolicy = null;
      if (continueStatement != null) {
        continueTarget = continueStatement.getTarget();
        if (continueTarget == null) {
          continueTarget = followingClauseNumber;
        }
        if (continueTarget != null) {
          if (continueTarget <= clauseNumber) {
            throw new BatfishException("Can only continue to later clause");
          }
          continueTargetPolicy = clauses.get(continueTarget);
          if (continueTargetPolicy == null) {
            String name = "clause: '" + continueTarget + "' in route-map: '" + map.getName() + "'";
            undefined(
                AsaStructureType.ROUTE_MAP_CLAUSE,
                name,
                AsaStructureUsage.ROUTE_MAP_CONTINUE,
                continueStatement.getStatementLine());
            continueStatement = null;
          }
        } else {
          continueStatement = null;
        }
      }
      switch (rmClause.getAction()) {
        case PERMIT:
          if (continueStatement == null) {
            onMatchStatements.add(Statements.ExitAccept.toStaticStatement());
          } else {
            onMatchStatements.add(Statements.SetDefaultActionAccept.toStaticStatement());
            onMatchStatements.add(new CallStatement(continueTargetPolicy.getName()));
          }
          break;

        case DENY:
          onMatchStatements.add(Statements.ExitReject.toStaticStatement());
          break;

        default:
          throw new BatfishException("Invalid action");
      }
      if (followingClause != null) {
        ifStatement.getFalseStatements().add(new CallStatement(followingClause.getName()));
      } else {
        ifStatement
            .getFalseStatements()
            .add(Statements.ReturnLocalDefaultAction.toStaticStatement());
      }
      followingClause = clausePolicy;
      followingClauseNumber = clauseNumber;
    }
    statements.add(new CallStatement(followingClause.getName()));
    return output;
  }

  @Override
  public String toString() {
    if (_hostname != null) {
      return getClass().getSimpleName() + "<" + _hostname + ">";
    } else {
      return super.toString();
    }
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() {
    Configuration c = new Configuration(_hostname, ConfigurationFormat.CISCO_ASA);
    c.setHumanName(_rawHostname);
    c.setDeviceModel(DeviceModel.CISCO_UNSPECIFIED);
    c.getVendorFamily().setCisco(_cf);
    c.setDefaultInboundAction(LineAction.PERMIT);
    c.setDefaultCrossZoneAction(LineAction.PERMIT);
    c.setDnsServers(_dnsServers);
    c.setDnsSourceInterface(_dnsSourceInterface);
    c.setDomainName(_domainName);
    c.setExportBgpFromBgpRib(true);
    c.setNormalVlanRange(
        IntegerSpace.of(new SubRange(VLAN_NORMAL_MIN_CISCO, VLAN_NORMAL_MAX_CISCO)));
    c.setTacacsServers(_tacacsServers);
    c.setTacacsSourceInterface(_tacacsSourceInterface);
    c.setNtpSourceInterface(_ntpSourceInterface);
    if (_cf.getNtp() != null) {
      c.setNtpServers(new TreeSet<>(_cf.getNtp().getServers().keySet()));
    }
    if (_cf.getLogging() != null) {
      c.setLoggingSourceInterface(_cf.getLogging().getSourceInterface());
      c.setLoggingServers(new TreeSet<>(_cf.getLogging().getHosts().keySet()));
    }
    c.setSnmpSourceInterface(_snmpSourceInterface);

    processFailoverSettings();

    // remove line login authentication lists if they don't exist
    for (Line line : _cf.getLines().values()) {
      String list = line.getLoginAuthentication();
      if (list == null) {
        continue;
      }
      boolean found = false;
      Aaa aaa = _cf.getAaa();
      if (aaa != null) {
        AaaAuthentication authentication = aaa.getAuthentication();
        if (authentication != null) {
          AaaAuthenticationLogin login = authentication.getLogin();
          if (login != null && login.getLists().containsKey(list)) {
            found = true;
          }
        }
      }
      if (!found) {
        line.setLoginAuthentication(null);
      }
    }

    // initialize vrfs
    for (String vrfName : _vrfs.keySet()) {
      c.getVrfs().put(vrfName, new org.batfish.datamodel.Vrf(vrfName));
      // inherit address family props
      Vrf vrf = _vrfs.get(vrfName);
      VrfAddressFamily ip4uaf = vrf.getIpv4UnicastAddressFamily();
      if (ip4uaf == null) {
        continue;
      }
      ip4uaf.inherit(vrf.getGenericAddressFamilyConfig());
    }

    // snmp server
    if (_snmpServer != null) {
      String snmpServerVrf = _snmpServer.getVrf();
      c.getVrfs().get(snmpServerVrf).setSnmpServer(_snmpServer);
    }

    // convert as path access lists to vendor independent format
    for (IpAsPathAccessList pathList : _asPathAccessLists.values()) {
      AsPathAccessList apList = AsaConversions.toAsPathAccessList(pathList);
      c.getAsPathAccessLists().put(apList.getName(), apList);
    }

    // convert standard/expanded community lists
    convertCommunityLists(c);

    // convert prefix lists to route filter lists
    for (PrefixList prefixList : _prefixLists.values()) {
      RouteFilterList newRouteFilterList = AsaConversions.toRouteFilterList(prefixList, _filename);
      c.getRouteFilterLists().put(newRouteFilterList.getName(), newRouteFilterList);
    }

    // convert standard/extended access lists to access lists or route filter
    // lists
    for (StandardAccessList saList : _standardAccessLists.values()) {
      if (isAclUsedForRouting(saList.getName())) {
        RouteFilterList rfList = AsaConversions.toRouteFilterList(saList, _filename);
        c.getRouteFilterLists().put(rfList.getName(), rfList);
      }
      c.getIpAccessLists()
          .put(saList.getName(), toIpAccessList(saList.toExtendedAccessList(), _objectGroups));
    }
    for (ExtendedAccessList eaList : _extendedAccessLists.values()) {
      if (isAclUsedForRouting(eaList.getName())) {
        RouteFilterList rfList = AsaConversions.toRouteFilterList(eaList, _filename);
        c.getRouteFilterLists().put(rfList.getName(), rfList);
      }
      IpAccessList ipaList = toIpAccessList(eaList, _objectGroups);
      c.getIpAccessLists().put(ipaList.getName(), ipaList);
    }

    /*
     * Consolidate info about networkObjects
     * - Associate networkObjects with their Info
     * - Associate ASA Object NATs with their object (needed for sorting)
     * - Removes ASA Object NATs that were created without a valid network object
     */
    _networkObjectInfos.forEach(
        (name, info) -> {
          if (_networkObjects.containsKey(name)) {
            _networkObjects.get(name).setInfo(info);
          }
        });
    _nats.removeIf(
        nat -> {
          if (nat.getSection() != Section.OBJECT) {
            return false;
          }
          String objectName = ((NetworkObjectAddressSpecifier) nat.getRealSource()).getName();
          NetworkObject object = _networkObjects.get(objectName);
          if (object == null) {
            // Network object has a NAT but no addresses
            _w.redFlag("Invalid reference for object NAT " + objectName + ".");
            return true;
          }
          if (object.getStart() == null) {
            // Unsupported network object type, already warned
            return true;
          }
          nat.setRealSourceObject(object);
          return false;
        });

    // convert each NetworkObject and NetworkObjectGroup to IpSpace
    _networkObjectGroups.forEach(
        (name, networkObjectGroup) -> c.getIpSpaces().put(name, toIpSpace(networkObjectGroup)));
    _networkObjectGroups
        .keySet()
        .forEach(
            name ->
                c.getIpSpaceMetadata()
                    .put(
                        name,
                        new IpSpaceMetadata(
                            name, AsaStructureType.NETWORK_OBJECT_GROUP.getDescription(), null)));
    _networkObjects.forEach(
        (name, networkObject) -> c.getIpSpaces().put(name, networkObject.toIpSpace()));
    _networkObjects
        .keySet()
        .forEach(
            name ->
                c.getIpSpaceMetadata()
                    .put(
                        name,
                        new IpSpaceMetadata(
                            name, AsaStructureType.NETWORK_OBJECT.getDescription(), null)));

    // convert each IcmpTypeGroup to IpAccessList
    _icmpTypeObjectGroups.forEach(
        (name, icmpTypeObjectGroups) ->
            c.getIpAccessLists()
                .put(computeIcmpObjectGroupAclName(name), toIpAccessList(icmpTypeObjectGroups)));

    // convert each ProtocolObjectGroup to IpAccessList
    _protocolObjectGroups.forEach(
        (name, protocolObjectGroup) ->
            c.getIpAccessLists()
                .put(computeProtocolObjectGroupAclName(name), toIpAccessList(protocolObjectGroup)));

    // convert each ServiceObject and ServiceObjectGroup to IpAccessList
    _serviceObjectGroups.forEach(
        (name, serviceObjectGroup) ->
            c.getIpAccessLists()
                .put(
                    computeServiceObjectGroupAclName(name),
                    toIpAccessList(serviceObjectGroup, _serviceObjects, _serviceObjectGroups)));
    _serviceObjects.forEach(
        (name, serviceObject) ->
            c.getIpAccessLists()
                .put(
                    computeServiceObjectAclName(name),
                    toIpAccessList(serviceObject, _serviceObjects, _serviceObjectGroups)));

    // TODO: convert route maps that are used for PBR to PacketPolicies

    for (RouteMap map : _routeMaps.values()) {
      // convert route maps to RoutingPolicy objects
      RoutingPolicy newPolicy = toRoutingPolicy(c, map);
      c.getRoutingPolicies().put(newPolicy.getName(), newPolicy);
    }

    createInspectClassMapAcls(c);

    // create inspect policy-map ACLs
    createInspectPolicyMapAcls(c);

    // create zones based on IOS security zones
    _securityZones.forEach((name, securityZone) -> c.getZones().put(name, new Zone(name)));

    // populate zone interfaces based on IOS security zones
    _interfaces.forEach(
        (ifaceName, iface) -> {
          String zoneName = iface.getSecurityZone();
          if (zoneName == null) {
            return;
          }
          Zone zone = c.getZones().get(zoneName);
          if (zone == null) {
            return;
          }
          zone.setInterfaces(
              ImmutableSet.<String>builder().addAll(zone.getInterfaces()).add(ifaceName).build());
        });

    _interfacesBySecurityLevel =
        _interfaces.values().stream()
            .filter(iface -> iface.getSecurityLevel() != null)
            .filter(iface -> iface.getAddress() != null)
            .collect(
                Multimaps.toMultimap(
                    Interface::getSecurityLevel,
                    Functions.identity(),
                    MultimapBuilder.hashKeys().arrayListValues()::build));

    // create and populate zones based on ASA security levels
    _interfacesBySecurityLevel.forEach(
        (level, iface) -> {
          String zoneName = computeASASecurityLevelZoneName(level);
          Zone zone = c.getZones().computeIfAbsent(zoneName, Zone::new);
          zone.setInterfaces(
              ImmutableSet.<String>builder()
                  .addAll(zone.getInterfaces())
                  .add(getNewInterfaceName(iface))
                  .build());
          _securityLevels.putIfAbsent(zoneName, level);
        });

    // create zone policies
    createZoneAcls(c);

    // convert interfaces
    _interfaces.forEach(
        (ifaceName, iface) -> {
          // Handle renaming interfaces for ASA devices
          String newIfaceName = getNewInterfaceName(iface);
          org.batfish.datamodel.Interface newInterface = toInterface(newIfaceName, iface, c);
          String vrfName = iface.getVrf();
          if (vrfName == null) {
            throw new BatfishException("Missing vrf name for iface: '" + iface.getName() + "'");
          }
          c.getAllInterfaces().put(newIfaceName, newInterface);
        });

    /*
     * Second pass over the interfaces to set dependency pointers correctly for:
     * - portchannels
     * - subinterfaces
     * - redundant interfaces
     * - tunnel interfaces
     * TODO: VLAN interfaces
     */
    _interfaces.forEach(
        (ifaceName, iface) -> {
          // Portchannels
          if (iface.getChannelGroup() != null) {
            String chGroup = getNewInterfaceName(iface.getChannelGroup());
            if (chGroup != null) {
              org.batfish.datamodel.Interface viIface = c.getAllInterfaces().get(chGroup);
              if (viIface != null) {
                viIface.addDependency(new Dependency(ifaceName, DependencyType.AGGREGATE));
              }
            }
          }
          // subinterfaces
          Matcher m = INTERFACE_WITH_SUBINTERFACE.matcher(iface.getName());
          if (m.matches()) {
            String parentInterfaceName = m.group(1);
            Interface parentInterface = _interfaces.get(parentInterfaceName);
            if (parentInterface != null) {
              String newParentInterfaceName = getNewInterfaceName(parentInterfaceName);
              org.batfish.datamodel.Interface viIface =
                  c.getAllInterfaces().get(getNewInterfaceName(iface));
              if (viIface != null) {
                viIface.addDependency(new Dependency(newParentInterfaceName, DependencyType.BIND));
              }
            }
          }
          // Redundant Interfaces
          {
            org.batfish.datamodel.Interface viIface = c.getAllInterfaces().get(ifaceName);
            if (viIface != null && viIface.getInterfaceType() == InterfaceType.REDUNDANT) {
              iface.getMemberInterfaces().stream()
                  .map(c.getAllInterfaces()::get)
                  .filter(Objects::nonNull)
                  .forEach(
                      memberViIface ->
                          viIface.addDependency(
                              new Dependency(memberViIface.getName(), DependencyType.AGGREGATE)));
            }
          }
          // Tunnels
          Tunnel tunnel = iface.getTunnel();
          if (tunnel != null) {
            org.batfish.datamodel.Interface viIface = c.getAllInterfaces().get(ifaceName);
            if (viIface != null) {
              // Add dependency
              if (isRealInterfaceName(tunnel.getSourceInterfaceName())) {
                String parentIfaceName = canonicalizeInterfaceName(tunnel.getSourceInterfaceName());
                viIface.addDependency(new Dependency(parentIfaceName, DependencyType.BIND));
                // Also set tunnel config while we're at at it.
                // Step one: determine IP address of parent interface
                @Nullable
                Ip parentIp =
                    Optional.ofNullable(c.getActiveInterfaces().get(parentIfaceName))
                        .map(org.batfish.datamodel.Interface::getConcreteAddress)
                        .map(ConcreteInterfaceAddress::getIp)
                        .orElse(null);
                // Step 2: create tunnel configs for non-IPsec tunnels. IPsec handled separately.
                if (tunnel.getMode() != TunnelMode.IPSEC_IPV4) {
                  // Ensure we have both src and dst IPs, otherwise don't convert
                  if (tunnel.getDestination() != null
                      && (tunnel.getSourceAddress() != null || parentIp != null)) {
                    viIface.setTunnelConfig(
                        TunnelConfiguration.builder()
                            .setSourceAddress(firstNonNull(tunnel.getSourceAddress(), parentIp))
                            .setDestinationAddress(tunnel.getDestination())
                            .build());
                  } else {
                    _w.redFlag(
                        String.format(
                            "Could not determine src/dst IPs for tunnel %s", iface.getName()));
                  }
                }
              }
            }
          }
        });

    // copy tracking groups
    c.getTrackingGroups().putAll(_trackingGroups);
    // create negated tracking groups
    _interfaces.values().stream()
        .flatMap(i -> i.getHsrpGroups().values().stream())
        .flatMap(group -> group.getTrackActions().keySet().stream())
        .distinct()
        .filter(_trackingGroups::containsKey)
        .forEach(
            id ->
                c.getTrackingGroups().put(generatedNegatedTrackMethodId(id), negatedReference(id)));

    // apply vrrp settings to interfaces
    applyVrrp(c);

    // ISAKMP policies to IKE Phase 1 proposals
    for (Entry<Integer, IsakmpPolicy> e : _isakmpPolicies.entrySet()) {
      IkePhase1Proposal ikePhase1Proposal = toIkePhase1Proposal(e.getValue());
      c.getIkePhase1Proposals().put(ikePhase1Proposal.getName(), ikePhase1Proposal);
    }
    resolveKeyringIsakmpProfileAddresses();
    resolveTunnelSourceInterfaces();

    resolveKeyringIfaceNames(_interfaces, _keyrings);
    resolveIsakmpProfileIfaceNames(_interfaces, _isakmpProfiles);
    resolveTunnelIfaceNames(_interfaces);

    // keyrings to IKE phase 1 keys
    ImmutableSortedMap.Builder<String, IkePhase1Key> ikePhase1KeysBuilder =
        ImmutableSortedMap.naturalOrder();
    _keyrings
        .values()
        .forEach(keyring -> ikePhase1KeysBuilder.put(keyring.getName(), toIkePhase1Key(keyring)));
    // RSA pub named keys to IKE phase 1 key and IKE phase 1 policy
    _cryptoNamedRsaPubKeys
        .values()
        .forEach(
            namedRsaPubKey -> {
              IkePhase1Key ikePhase1Key = toIkePhase1Key(namedRsaPubKey);
              ikePhase1KeysBuilder.put(getRsaPubKeyGeneratedName(namedRsaPubKey), ikePhase1Key);

              IkePhase1Policy ikePhase1Policy =
                  toIkePhase1Policy(namedRsaPubKey, this, ikePhase1Key);
              c.getIkePhase1Policies().put(ikePhase1Policy.getName(), ikePhase1Policy);
            });

    // standalone ISAKMP keys to IKE phase 1 key and IKE phase 1 policy
    _isakmpKeys.forEach(
        isakmpKey -> {
          IkePhase1Key ikePhase1Key = toIkePhase1Key(isakmpKey);
          ikePhase1KeysBuilder.put(getIsakmpKeyGeneratedName(isakmpKey), ikePhase1Key);

          IkePhase1Policy ikePhase1Policy = toIkePhase1Policy(isakmpKey, this, ikePhase1Key);
          c.getIkePhase1Policies().put(ikePhase1Policy.getName(), ikePhase1Policy);
        });

    c.setIkePhase1Keys(ikePhase1KeysBuilder.build());

    // ISAKMP profiles to IKE phase 1 policies
    _isakmpProfiles
        .values()
        .forEach(
            isakmpProfile ->
                c.getIkePhase1Policies()
                    .put(isakmpProfile.getName(), toIkePhase1Policy(isakmpProfile, this, c, _w)));

    // convert ipsec transform sets
    ImmutableSortedMap.Builder<String, IpsecPhase2Proposal> ipsecPhase2ProposalsBuilder =
        ImmutableSortedMap.naturalOrder();
    for (Entry<String, IpsecTransformSet> e : _ipsecTransformSets.entrySet()) {
      ipsecPhase2ProposalsBuilder.put(e.getKey(), toIpsecPhase2Proposal(e.getValue()));
    }
    c.setIpsecPhase2Proposals(ipsecPhase2ProposalsBuilder.build());

    // ipsec policies
    ImmutableSortedMap.Builder<String, IpsecPhase2Policy> ipsecPhase2PoliciesBuilder =
        ImmutableSortedMap.naturalOrder();
    for (IpsecProfile ipsecProfile : _ipsecProfiles.values()) {
      ipsecPhase2PoliciesBuilder.put(ipsecProfile.getName(), toIpsecPhase2Policy(ipsecProfile));
    }
    c.setIpsecPhase2Policies(ipsecPhase2PoliciesBuilder.build());

    // crypto-map sets to IPsec Peer Configs
    for (CryptoMapSet cryptoMapSet : _cryptoMapSets.values()) {
      convertCryptoMapSet(c, cryptoMapSet, _cryptoMapSets, _w);
    }

    // IPsec tunnels to IPsec Peer Configs
    ImmutableSortedMap.Builder<String, IpsecPeerConfig> ipsecPeerConfigBuilder =
        ImmutableSortedMap.naturalOrder();
    ipsecPeerConfigBuilder.putAll(c.getIpsecPeerConfigs());
    for (Entry<String, Interface> e : _interfaces.entrySet()) {
      String name = e.getKey();
      Interface iface = e.getValue();
      Tunnel tunnel = iface.getTunnel();
      if (iface.getActive() && tunnel != null && tunnel.getMode() == TunnelMode.IPSEC_IPV4) {
        if (tunnel.getIpsecProfileName() == null) {
          _w.redFlagf("No IPSec Profile set for IPSec tunnel %s", name);
          continue;
        }
        // convert to IpsecPeerConfig
        toIpsecPeerConfig(tunnel, name, this, c, _w)
            .ifPresent(config -> ipsecPeerConfigBuilder.put(name, config));
      }
    }
    c.setIpsecPeerConfigs(ipsecPeerConfigBuilder.build());

    // convert routing processes
    _vrfs.forEach(
        (vrfName, vrf) -> {
          org.batfish.datamodel.Vrf newVrf = c.getVrfs().get(vrfName);

          // description
          newVrf.setDescription(vrf.getDescription());

          // add snmp trap servers to main list
          if (newVrf.getSnmpServer() != null) {
            c.getSnmpTrapServers().addAll(newVrf.getSnmpServer().getHosts().keySet());
          }

          // convert static routes
          for (StaticRoute staticRoute : vrf.getStaticRoutes()) {
            newVrf.getStaticRoutes().add(AsaConversions.toStaticRoute(staticRoute));
          }

          // convert rip process
          RipProcess ripProcess = vrf.getRipProcess();
          if (ripProcess != null) {
            org.batfish.datamodel.RipProcess newRipProcess = toRipProcess(ripProcess, vrfName, c);
            newVrf.setRipProcess(newRipProcess);
          }

          // Convert OSPF processes.
          newVrf.setOspfProcesses(
              vrf.getOspfProcesses().values().stream()
                  .map(proc -> toOspfProcess(proc, vrfName, c, this))
                  .filter(Objects::nonNull));

          // convert eigrp processes
          vrf.getEigrpProcesses().values().stream()
              .map(proc -> AsaConversions.toEigrpProcess(proc, vrfName, c, this))
              .filter(Objects::nonNull)
              .forEach(newVrf::addEigrpProcess);

          // convert isis process
          IsisProcess isisProcess = vrf.getIsisProcess();
          if (isisProcess != null) {
            org.batfish.datamodel.isis.IsisProcess newIsisProcess =
                AsaConversions.toIsisProcess(isisProcess, c, this);
            newVrf.setIsisProcess(newIsisProcess);
          }

          // convert bgp process
          BgpProcess bgpProcess = vrf.getBgpProcess();
          if (bgpProcess != null) {
            org.batfish.datamodel.BgpProcess newBgpProcess = toBgpProcess(c, bgpProcess, vrfName);
            newVrf.setBgpProcess(newBgpProcess);
          } else if (vrf.getIpv4UnicastAddressFamily() != null
              && !vrf.getIpv4UnicastAddressFamily().getRouteTargetImport().isEmpty()) {
            /*
             * Despite no BGP config this vrf is leaked into. Make a dummy BGP process.
             */
            assert newVrf.getBgpProcess() == null;
            newVrf.setBgpProcess(bgpProcessBuilder().setRouterId(Ip.ZERO).build());
          }
        });
    /*
     * Another pass over interfaces to push final settings to VI interfaces.
     * (e.g. has OSPF settings but no associated OSPF process, common in show run all)
     */
    _interfaces.forEach(
        (key, vsIface) -> {
          // Check alias first to handle ASA using alias as VI interface name
          String ifaceName = firstNonNull(vsIface.getAlias(), key);
          org.batfish.datamodel.Interface iface = c.getAllInterfaces().get(ifaceName);
          if (iface == null) {
            // Should never get here
            return;
          } else if (iface.getOspfAreaName() != null) {
            // Already configured
            return;
          }
          // Not part of an OSPF area, but has settings
          if (vsIface.getOspfArea() != null
              || vsIface.getOspfCost() != null
              || vsIface.getOspfPassive() != null
              || vsIface.getOspfNetworkType() != null
              || vsIface.getOspfDeadInterval() != null
              || vsIface.getOspfHelloInterval() != null) {
            finalizeInterfaceOspfSettings(iface, vsIface, null, null);
          }
        });

    convertVrfLeakingConfig(_vrfs.values(), c);

    // Define the Null0 interface if it has been referenced. Otherwise, these show as undefined
    // references.
    Optional<Integer> firstRefToNull0 =
        _structureManager
            .getStructureReferences(AsaStructureType.INTERFACE)
            .getOrDefault("Null0", ImmutableSortedMap.of())
            .values()
            .stream()
            .flatMap(Collection::stream)
            .min(Integer::compare);
    if (firstRefToNull0.isPresent()) {
      defineSingleLineStructure(AsaStructureType.INTERFACE, "Null0", firstRefToNull0.get());
    }

    markConcreteStructure(AsaStructureType.BFD_TEMPLATE, AsaStructureUsage.INTERFACE_BFD_TEMPLATE);

    markConcreteStructure(
        AsaStructureType.SECURITY_ZONE_PAIR, AsaStructureUsage.SECURITY_ZONE_PAIR_SELF_REF);

    markConcreteStructure(
        AsaStructureType.INTERFACE,
        AsaStructureUsage.BGP_UPDATE_SOURCE_INTERFACE,
        AsaStructureUsage.DOMAIN_LOOKUP_SOURCE_INTERFACE,
        AsaStructureUsage.EIGRP_AF_INTERFACE,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_ACCESS_LIST_IN,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_ACCESS_LIST_OUT,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_GATEWAY_IN,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_GATEWAY_OUT,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_PREFIX_LIST_IN,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_PREFIX_LIST_OUT,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_ROUTE_MAP_IN,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_ROUTE_MAP_OUT,
        AsaStructureUsage.EIGRP_PASSIVE_INTERFACE,
        AsaStructureUsage.FAILOVER_LAN_INTERFACE,
        AsaStructureUsage.FAILOVER_LINK_INTERFACE,
        AsaStructureUsage.INTERFACE_SELF_REF,
        AsaStructureUsage.IP_DOMAIN_LOOKUP_INTERFACE,
        AsaStructureUsage.IP_TACACS_SOURCE_INTERFACE,
        AsaStructureUsage.NTP_SOURCE_INTERFACE,
        AsaStructureUsage.OBJECT_NAT_MAPPED_INTERFACE,
        AsaStructureUsage.OBJECT_NAT_REAL_INTERFACE,
        AsaStructureUsage.OSPF_AREA_INTERFACE,
        AsaStructureUsage.OSPF_DISTRIBUTE_LIST_ACCESS_LIST_IN,
        AsaStructureUsage.OSPF_DISTRIBUTE_LIST_ACCESS_LIST_OUT,
        AsaStructureUsage.OSPF_DISTRIBUTE_LIST_PREFIX_LIST_IN,
        AsaStructureUsage.OSPF_DISTRIBUTE_LIST_PREFIX_LIST_OUT,
        AsaStructureUsage.OSPF_DISTRIBUTE_LIST_ROUTE_MAP_IN,
        AsaStructureUsage.OSPF_DISTRIBUTE_LIST_ROUTE_MAP_OUT,
        AsaStructureUsage.OSPF6_DISTRIBUTE_LIST_PREFIX_LIST_IN,
        AsaStructureUsage.OSPF6_DISTRIBUTE_LIST_PREFIX_LIST_OUT,
        AsaStructureUsage.ROUTE_MAP_MATCH_INTERFACE,
        AsaStructureUsage.SERVICE_POLICY_INTERFACE,
        AsaStructureUsage.SNMP_SERVER_SOURCE_INTERFACE,
        AsaStructureUsage.SNMP_SERVER_TRAP_SOURCE,
        AsaStructureUsage.TACACS_SOURCE_INTERFACE,
        AsaStructureUsage.TRACK_INTERFACE,
        AsaStructureUsage.TWICE_NAT_MAPPED_INTERFACE,
        AsaStructureUsage.TWICE_NAT_REAL_INTERFACE);

    // mark references to ACLs that may not appear in data model
    markIpOrMacAcls(
        AsaStructureUsage.CLASS_MAP_ACCESS_GROUP, AsaStructureUsage.CLASS_MAP_ACCESS_LIST);
    markIpv4Acls(
        AsaStructureUsage.BGP_NEIGHBOR_DISTRIBUTE_LIST_ACCESS_LIST_IN,
        AsaStructureUsage.BGP_NEIGHBOR_DISTRIBUTE_LIST_ACCESS_LIST_OUT,
        AsaStructureUsage.CONTROL_PLANE_ACCESS_GROUP,
        AsaStructureUsage.INTERFACE_IGMP_STATIC_GROUP_ACL,
        AsaStructureUsage.INTERFACE_INCOMING_FILTER,
        AsaStructureUsage.INTERFACE_IP_VERIFY_ACCESS_LIST,
        AsaStructureUsage.INTERFACE_OUTGOING_FILTER,
        AsaStructureUsage.INTERFACE_PIM_NEIGHBOR_FILTER,
        AsaStructureUsage.LINE_ACCESS_CLASS_LIST,
        AsaStructureUsage.MANAGEMENT_SSH_ACCESS_GROUP,
        AsaStructureUsage.MANAGEMENT_TELNET_ACCESS_GROUP,
        AsaStructureUsage.MSDP_PEER_SA_LIST,
        AsaStructureUsage.NTP_ACCESS_GROUP,
        AsaStructureUsage.PIM_ACCEPT_REGISTER_ACL,
        AsaStructureUsage.PIM_ACCEPT_RP_ACL,
        AsaStructureUsage.PIM_RP_ADDRESS_ACL,
        AsaStructureUsage.PIM_RP_ANNOUNCE_FILTER,
        AsaStructureUsage.PIM_RP_CANDIDATE_ACL,
        AsaStructureUsage.PIM_SEND_RP_ANNOUNCE_ACL,
        AsaStructureUsage.PIM_SPT_THRESHOLD_ACL,
        AsaStructureUsage.ROUTE_MAP_MATCH_IPV4_ACCESS_LIST,
        AsaStructureUsage.SNMP_SERVER_COMMUNITY_ACL4,
        AsaStructureUsage.SNMP_SERVER_GROUP_V3_ACCESS,
        AsaStructureUsage.SSH_IPV4_ACL);
    markIpv6Acls(
        AsaStructureUsage.BGP_NEIGHBOR_DISTRIBUTE_LIST_ACCESS6_LIST_IN,
        AsaStructureUsage.BGP_NEIGHBOR_DISTRIBUTE_LIST_ACCESS6_LIST_OUT,
        AsaStructureUsage.LINE_ACCESS_CLASS_LIST6,
        AsaStructureUsage.NTP_ACCESS_GROUP,
        AsaStructureUsage.ROUTE_MAP_MATCH_IPV6_ACCESS_LIST,
        AsaStructureUsage.SNMP_SERVER_COMMUNITY_ACL6,
        AsaStructureUsage.SNMP_SERVER_GROUP_V3_ACCESS_IPV6,
        AsaStructureUsage.SSH_IPV6_ACL,
        AsaStructureUsage.INTERFACE_IPV6_TRAFFIC_FILTER_IN,
        AsaStructureUsage.INTERFACE_IPV6_TRAFFIC_FILTER_OUT);
    markAcls(
        AsaStructureUsage.ACCESS_GROUP_GLOBAL_FILTER,
        AsaStructureUsage.COPS_LISTENER_ACCESS_LIST,
        AsaStructureUsage.CRYPTO_MAP_IPSEC_ISAKMP_ACL,
        AsaStructureUsage.CRYPTO_MAP_MATCH_ADDRESS,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_ACCESS_LIST_IN,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_ACCESS_LIST_OUT,
        AsaStructureUsage.INSPECT_CLASS_MAP_MATCH_ACCESS_GROUP,
        AsaStructureUsage.INTERFACE_IGMP_ACCESS_GROUP_ACL,
        AsaStructureUsage.INTERFACE_IGMP_HOST_PROXY_ACCESS_LIST,
        AsaStructureUsage.INTERFACE_INCOMING_FILTER,
        AsaStructureUsage.INTERFACE_IP_INBAND_ACCESS_GROUP,
        AsaStructureUsage.INTERFACE_OUTGOING_FILTER,
        AsaStructureUsage.OSPF_DISTRIBUTE_LIST_ACCESS_LIST_IN,
        AsaStructureUsage.OSPF_DISTRIBUTE_LIST_ACCESS_LIST_OUT,
        AsaStructureUsage.RIP_DISTRIBUTE_LIST,
        AsaStructureUsage.ROUTER_ISIS_DISTRIBUTE_LIST_ACL,
        AsaStructureUsage.SNMP_SERVER_FILE_TRANSFER_ACL,
        AsaStructureUsage.SNMP_SERVER_TFTP_SERVER_LIST,
        AsaStructureUsage.SNMP_SERVER_COMMUNITY_ACL,
        AsaStructureUsage.SSH_ACL,
        AsaStructureUsage.WCCP_GROUP_LIST,
        AsaStructureUsage.WCCP_REDIRECT_LIST,
        AsaStructureUsage.WCCP_SERVICE_LIST);

    markCommunityLists(
        AsaStructureUsage.ROUTE_MAP_ADD_COMMUNITY,
        AsaStructureUsage.ROUTE_MAP_DELETE_COMMUNITY,
        AsaStructureUsage.ROUTE_MAP_MATCH_COMMUNITY_LIST,
        AsaStructureUsage.ROUTE_MAP_SET_COMMUNITY);

    markExtcommunityLists(AsaStructureUsage.ROUTE_MAP_MATCH_EXTCOMMUNITY);

    markConcreteStructure(
        AsaStructureType.PREFIX_LIST,
        AsaStructureUsage.BGP_INBOUND_PREFIX_LIST,
        AsaStructureUsage.BGP_OUTBOUND_PREFIX_LIST,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_GATEWAY_IN,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_GATEWAY_OUT,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_PREFIX_LIST_IN,
        AsaStructureUsage.EIGRP_DISTRIBUTE_LIST_PREFIX_LIST_OUT,
        AsaStructureUsage.OSPF_DISTRIBUTE_LIST_PREFIX_LIST_IN,
        AsaStructureUsage.OSPF_DISTRIBUTE_LIST_PREFIX_LIST_OUT,
        AsaStructureUsage.ROUTE_MAP_MATCH_IPV4_PREFIX_LIST);
    markConcreteStructure(
        AsaStructureType.PREFIX6_LIST,
        AsaStructureUsage.BGP_INBOUND_PREFIX6_LIST,
        AsaStructureUsage.BGP_OUTBOUND_PREFIX6_LIST,
        AsaStructureUsage.OSPF6_DISTRIBUTE_LIST_PREFIX_LIST_IN,
        AsaStructureUsage.OSPF6_DISTRIBUTE_LIST_PREFIX_LIST_OUT,
        AsaStructureUsage.ROUTE_MAP_MATCH_IPV6_PREFIX_LIST);

    // mark references to route-maps
    markConcreteStructure(AsaStructureType.ROUTE_MAP);

    // Cable
    markConcreteStructure(AsaStructureType.DEPI_CLASS);
    markConcreteStructure(AsaStructureType.DEPI_TUNNEL);
    markConcreteStructure(AsaStructureType.DOCSIS_POLICY);
    markConcreteStructure(AsaStructureType.DOCSIS_POLICY_RULE);
    markConcreteStructure(
        AsaStructureType.SERVICE_CLASS, AsaStructureUsage.QOS_ENFORCE_RULE_SERVICE_CLASS);

    // L2tp
    markConcreteStructure(AsaStructureType.L2TP_CLASS);

    // Crypto, Isakmp, and IPSec
    markConcreteStructure(AsaStructureType.CRYPTO_DYNAMIC_MAP_SET);
    markConcreteStructure(AsaStructureType.ISAKMP_PROFILE);
    markConcreteStructure(AsaStructureType.ISAKMP_POLICY);
    markConcreteStructure(AsaStructureType.IPSEC_PROFILE);
    markConcreteStructure(AsaStructureType.IPSEC_TRANSFORM_SET);
    markConcreteStructure(AsaStructureType.KEYRING, AsaStructureUsage.ISAKMP_PROFILE_KEYRING);
    markConcreteStructure(
        AsaStructureType.NAMED_RSA_PUB_KEY, AsaStructureUsage.NAMED_RSA_PUB_KEY_SELF_REF);

    // class-map
    markConcreteStructure(
        AsaStructureType.INSPECT_CLASS_MAP, AsaStructureUsage.INSPECT_POLICY_MAP_INSPECT_CLASS);
    markConcreteStructure(
        AsaStructureType.CLASS_MAP,
        AsaStructureUsage.POLICY_MAP_CLASS,
        AsaStructureUsage.POLICY_MAP_EVENT_CLASS);

    // policy-map
    markConcreteStructure(
        AsaStructureType.INSPECT_POLICY_MAP, AsaStructureUsage.ZONE_PAIR_INSPECT_SERVICE_POLICY);
    markConcreteStructure(AsaStructureType.POLICY_MAP);

    // object-group
    markConcreteStructure(
        AsaStructureType.ICMP_TYPE_OBJECT_GROUP,
        AsaStructureUsage.EXTENDED_ACCESS_LIST_ICMP_TYPE_OBJECT_GROUP,
        AsaStructureUsage.ICMP_TYPE_OBJECT_GROUP_GROUP_OBJECT);
    markConcreteStructure(
        AsaStructureType.NETWORK_OBJECT_GROUP,
        AsaStructureUsage.EXTENDED_ACCESS_LIST_NETWORK_OBJECT_GROUP,
        AsaStructureUsage.NETWORK_OBJECT_GROUP_GROUP_OBJECT,
        AsaStructureUsage.OBJECT_NAT_MAPPED_SOURCE_NETWORK_OBJECT_GROUP,
        AsaStructureUsage.TWICE_NAT_MAPPED_DESTINATION_NETWORK_OBJECT_GROUP,
        AsaStructureUsage.TWICE_NAT_MAPPED_SOURCE_NETWORK_OBJECT_GROUP,
        AsaStructureUsage.TWICE_NAT_REAL_DESTINATION_NETWORK_OBJECT_GROUP,
        AsaStructureUsage.TWICE_NAT_REAL_SOURCE_NETWORK_OBJECT_GROUP);
    markConcreteStructure(
        AsaStructureType.PROTOCOL_OBJECT_GROUP,
        AsaStructureUsage.EXTENDED_ACCESS_LIST_PROTOCOL_OBJECT_GROUP,
        AsaStructureUsage.PROTOCOL_OBJECT_GROUP_GROUP_OBJECT);
    markConcreteStructure(
        AsaStructureType.SERVICE_OBJECT_GROUP,
        AsaStructureUsage.EXTENDED_ACCESS_LIST_SERVICE_OBJECT_GROUP,
        AsaStructureUsage.SERVICE_OBJECT_GROUP_GROUP_OBJECT);
    markAbstractStructure(
        AsaStructureType.PROTOCOL_OR_SERVICE_OBJECT_GROUP,
        AsaStructureUsage.EXTENDED_ACCESS_LIST_PROTOCOL_OR_SERVICE_OBJECT_GROUP,
        ImmutableList.of(
            AsaStructureType.PROTOCOL_OBJECT_GROUP, AsaStructureType.SERVICE_OBJECT_GROUP));

    // objects
    markConcreteStructure(
        AsaStructureType.ICMP_TYPE_OBJECT, AsaStructureUsage.ICMP_TYPE_OBJECT_GROUP_ICMP_OBJECT);
    markConcreteStructure(
        AsaStructureType.NETWORK_OBJECT,
        AsaStructureUsage.EXTENDED_ACCESS_LIST_NETWORK_OBJECT,
        AsaStructureUsage.NETWORK_OBJECT_GROUP_NETWORK_OBJECT,
        AsaStructureUsage.OBJECT_NAT_MAPPED_SOURCE_NETWORK_OBJECT,
        AsaStructureUsage.OBJECT_NAT_REAL_SOURCE_NETWORK_OBJECT,
        AsaStructureUsage.TWICE_NAT_MAPPED_DESTINATION_NETWORK_OBJECT,
        AsaStructureUsage.TWICE_NAT_MAPPED_SOURCE_NETWORK_OBJECT,
        AsaStructureUsage.TWICE_NAT_REAL_DESTINATION_NETWORK_OBJECT,
        AsaStructureUsage.TWICE_NAT_REAL_SOURCE_NETWORK_OBJECT);
    markConcreteStructure(
        AsaStructureType.SERVICE_OBJECT,
        AsaStructureUsage.EXTENDED_ACCESS_LIST_SERVICE_OBJECT,
        AsaStructureUsage.SERVICE_OBJECT_GROUP_SERVICE_OBJECT);
    markConcreteStructure(
        AsaStructureType.PROTOCOL_OBJECT, AsaStructureUsage.PROTOCOL_OBJECT_GROUP_PROTOCOL_OBJECT);

    // service template
    markConcreteStructure(
        AsaStructureType.SERVICE_TEMPLATE,
        AsaStructureUsage.CLASS_MAP_SERVICE_TEMPLATE,
        AsaStructureUsage.CLASS_MAP_ACTIVATED_SERVICE_TEMPLATE,
        AsaStructureUsage.POLICY_MAP_EVENT_CLASS_ACTIVATE);

    // track
    markConcreteStructure(AsaStructureType.TRACK);

    // zone
    markConcreteStructure(
        AsaStructureType.SECURITY_ZONE,
        AsaStructureUsage.INTERFACE_ZONE_MEMBER,
        AsaStructureUsage.ZONE_PAIR_DESTINATION_ZONE,
        AsaStructureUsage.ZONE_PAIR_SOURCE_ZONE);

    markConcreteStructure(AsaStructureType.NAT_POOL);
    markConcreteStructure(
        AsaStructureType.AS_PATH_ACCESS_LIST,
        AsaStructureUsage.BGP_NEIGHBOR_FILTER_AS_PATH_ACCESS_LIST,
        AsaStructureUsage.ROUTE_MAP_MATCH_AS_PATH_ACCESS_LIST);

    // BGP inheritance. This is complicated, as there are many similar-but-overlapping concepts
    markConcreteStructure(AsaStructureType.BGP_AF_GROUP, AsaStructureUsage.BGP_USE_AF_GROUP);
    markConcreteStructure(
        AsaStructureType.BGP_NEIGHBOR_GROUP, AsaStructureUsage.BGP_USE_NEIGHBOR_GROUP);
    markConcreteStructure(
        AsaStructureType.BGP_PEER_GROUP,
        AsaStructureUsage.BGP_LISTEN_RANGE_PEER_GROUP,
        AsaStructureUsage.BGP_NEIGHBOR_PEER_GROUP,
        AsaStructureUsage.BGP_NEIGHBOR_STATEMENT);
    markConcreteStructure(
        AsaStructureType.BGP_SESSION_GROUP, AsaStructureUsage.BGP_USE_SESSION_GROUP);
    markConcreteStructure(
        AsaStructureType.BGP_TEMPLATE_PEER_POLICY, AsaStructureUsage.BGP_INHERITED_PEER_POLICY);
    markConcreteStructure(
        AsaStructureType.BGP_TEMPLATE_PEER_SESSION, AsaStructureUsage.BGP_INHERITED_SESSION);
    markConcreteStructure(
        AsaStructureType.BGP_UNDECLARED_PEER, AsaStructureUsage.BGP_NEIGHBOR_WITHOUT_REMOTE_AS);
    markConcreteStructure(
        AsaStructureType.BGP_UNDECLARED_PEER_GROUP,
        AsaStructureUsage.BGP_PEER_GROUP_REFERENCED_BEFORE_DEFINED);

    return ImmutableList.of(c);
  }

  private @Nonnull org.batfish.datamodel.BgpProcess.Builder bgpProcessBuilder() {
    return org.batfish.datamodel.BgpProcess.builder()
        .setEbgpAdminCost(DEFAULT_EBGP_ADMIN)
        .setIbgpAdminCost(DEFAULT_IBGP_ADMIN)
        .setLocalAdminCost(DEFAULT_LOCAL_ADMIN)
        // TODO: verify following values
        .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP);
  }

  private void createInspectClassMapAcls(Configuration c) {
    _inspectClassMaps.forEach(
        (inspectClassMapName, inspectClassMap) -> {
          String inspectClassMapAclName = computeInspectClassMapAclName(inspectClassMapName);
          MatchSemantics matchSemantics = inspectClassMap.getMatchSemantics();
          List<AclLineMatchExpr> matchConditions =
              inspectClassMap.getMatches().stream()
                  .map(
                      inspectClassMapMatch ->
                          inspectClassMapMatch.toAclLineMatchExpr(this, c, matchSemantics, _w))
                  .collect(ImmutableList.toImmutableList());
          AclLineMatchExpr matchClassMap;
          switch (matchSemantics) {
            case MATCH_ALL:
              matchClassMap = and(matchConditions);
              break;
            case MATCH_ANY:
              matchClassMap = or(matchConditions);
              break;
            default:
              throw new BatfishException(
                  String.format(
                      "Unsupported %s: %s", MatchSemantics.class.getSimpleName(), matchSemantics));
          }
          IpAccessList.builder()
              .setOwner(c)
              .setName(inspectClassMapAclName)
              .setLines(
                  ImmutableList.of(
                      ExprAclLine.accepting().setMatchCondition(matchClassMap).build()))
              .setSourceName(inspectClassMapName)
              .setSourceType(AsaStructureType.INSPECT_CLASS_MAP.getDescription())
              .build();
        });
  }

  private void createInspectPolicyMapAcls(Configuration c) {
    _inspectPolicyMaps.forEach(
        (inspectPolicyMapName, inspectPolicyMap) -> {
          String inspectPolicyMapAclName = computeInspectPolicyMapAclName(inspectPolicyMapName);
          ImmutableList.Builder<AclLine> policyMapAclLines = ImmutableList.builder();
          inspectPolicyMap
              .getInspectClasses()
              .forEach(
                  (inspectClassName, inspectPolicyMapInspectClass) -> {
                    PolicyMapClassAction action = inspectPolicyMapInspectClass.getAction();
                    if (action == null) {
                      return;
                    }
                    String inspectClassMapAclName = computeInspectClassMapAclName(inspectClassName);
                    if (!c.getIpAccessLists().containsKey(inspectClassMapAclName)) {
                      return;
                    }
                    AclLineMatchExpr matchCondition = new PermittedByAcl(inspectClassMapAclName);
                    switch (action) {
                      case DROP:
                        policyMapAclLines.add(
                            ExprAclLine.rejecting()
                                .setMatchCondition(matchCondition)
                                .setName(
                                    String.format(
                                        "Drop if matched by class-map: '%s'", inspectClassName))
                                .build());
                        break;

                      case INSPECT:
                        policyMapAclLines.add(
                            ExprAclLine.accepting()
                                .setMatchCondition(matchCondition)
                                .setName(
                                    String.format(
                                        "Inspect if matched by class-map: '%s'", inspectClassName))
                                .build());
                        break;

                      case PASS:
                        policyMapAclLines.add(
                            ExprAclLine.accepting()
                                .setMatchCondition(matchCondition)
                                .setName(
                                    String.format(
                                        "Pass if matched by class-map: '%s'", inspectClassName))
                                .build());
                        break;

                      default:
                        _w.unimplemented("Unimplemented policy-map class action: " + action);
                        return;
                    }
                  });
          policyMapAclLines.add(
              ExprAclLine.builder()
                  .setAction(inspectPolicyMap.getClassDefaultAction())
                  .setMatchCondition(TrueExpr.INSTANCE)
                  .setName(
                      String.format(
                          "class-default action: %s", inspectPolicyMap.getClassDefaultAction()))
                  .build());
          IpAccessList.builder()
              .setOwner(c)
              .setName(inspectPolicyMapAclName)
              .setLines(policyMapAclLines.build())
              .setSourceName(inspectPolicyMapName)
              .setSourceType(AsaStructureType.INSPECT_POLICY_MAP.getDescription())
              .build();
        });
  }

  private void createZoneAcls(Configuration c) {
    // Mapping: zoneName -> (MatchSrcInterface for interfaces in zone)
    Map<String, MatchSrcInterface> matchSrcInterfaceBySrcZone =
        toImmutableMap(
            c.getZones(),
            Entry::getKey,
            zoneByNameEntry -> new MatchSrcInterface(zoneByNameEntry.getValue().getInterfaces()));

    c.getZones().values().stream()
        .map(
            zone -> {
              // Don't bother if zone is empty
              SortedSet<String> interfaces = zone.getInterfaces();
              if (interfaces.isEmpty()) {
                return null;
              }
              String zoneName = zone.getName();
              if (_securityLevels.containsKey(zoneName)) {
                // ASA security level
                return createAsaSecurityLevelZoneAcl(zone);
              } else if (_securityZones.containsKey(zoneName)) {
                // IOS security zone
                return createIosSecurityZoneAcl(zone, matchSrcInterfaceBySrcZone, c);
              }
              // shouldn't reach here
              return null;
            })
        .filter(Objects::nonNull)
        .forEach(acl -> c.getIpAccessLists().put(acl.getName(), acl));
  }

  IpAccessList createAsaSecurityLevelZoneAcl(Zone zone) {

    ImmutableList.Builder<AclLine> zonePolicies = ImmutableList.builder();

    // Allow traffic originating from device (no source interface)
    zonePolicies.add(
        ExprAclLine.accepting()
            .setMatchCondition(OriginatingFromDevice.INSTANCE)
            .setName("Allow traffic originating from this device")
            .setTraceElement(PERMIT_TRAFFIC_FROM_DEVICE)
            .build());

    String zoneName = zone.getName();

    // Security level policies
    zonePolicies.addAll(createSecurityLevelAcl(zoneName));

    return IpAccessList.builder()
        .setName(computeZoneOutgoingAclName(zoneName))
        .setLines(zonePolicies.build())
        .build();
  }

  IpAccessList createIosSecurityZoneAcl(
      Zone zone, Map<String, MatchSrcInterface> matchSrcInterfaceBySrcZone, Configuration c) {

    ImmutableList.Builder<AclLine> zonePolicies = ImmutableList.builder();

    // Allow traffic originating from device (no source interface)
    zonePolicies.add(
        ExprAclLine.accepting()
            .setMatchCondition(OriginatingFromDevice.INSTANCE)
            .setName("Allow traffic originating from this device")
            .build());

    // Allow traffic staying within this zone (always true for IOS)
    String zoneName = zone.getName();
    zonePolicies.add(
        ExprAclLine.accepting()
            .setMatchCondition(matchSrcInterfaceBySrcZone.get(zoneName))
            .setName(
                String.format("Allow traffic received on interface in same zone: '%s'", zoneName))
            .build());

    /*
     * Add zone-pair policies
     */
    // zoneName refers to dstZone
    Map<String, SecurityZonePair> zonePairsBySrcZoneName = _securityZonePairs.get(zoneName);
    if (zonePairsBySrcZoneName != null) {
      zonePairsBySrcZoneName.forEach(
          (srcZoneName, zonePair) ->
              createZonePairAcl(
                      c,
                      matchSrcInterfaceBySrcZone.get(srcZoneName),
                      zoneName,
                      srcZoneName,
                      zonePair)
                  .ifPresent(zonePolicies::add));
    }

    // Security level policies
    zonePolicies.addAll(createSecurityLevelAcl(zoneName));

    return IpAccessList.builder()
        .setName(computeZoneOutgoingAclName(zoneName))
        .setLines(zonePolicies.build())
        .build();
  }

  public Optional<ExprAclLine> createZonePairAcl(
      Configuration c,
      MatchSrcInterface matchSrcZoneInterface,
      String dstZoneName,
      String srcZoneName,
      SecurityZonePair zonePair) {
    String inspectPolicyMapName = zonePair.getInspectPolicyMap();
    if (!_securityZones.containsKey(srcZoneName)) {
      return Optional.empty();
    }
    if (inspectPolicyMapName == null) {
      return Optional.empty();
    }
    String inspectPolicyMapAclName = computeInspectPolicyMapAclName(inspectPolicyMapName);
    if (!c.getIpAccessLists().containsKey(inspectPolicyMapAclName)) {
      return Optional.empty();
    }
    PermittedByAcl permittedByPolicyMap = new PermittedByAcl(inspectPolicyMapAclName);
    String zonePairAclName = computeZonePairAclName(srcZoneName, dstZoneName);
    IpAccessList.builder()
        .setName(zonePairAclName)
        .setOwner(c)
        .setLines(
            ImmutableList.of(
                ExprAclLine.accepting()
                    .setMatchCondition(and(matchSrcZoneInterface, permittedByPolicyMap))
                    .setName(
                        String.format(
                            "Allow traffic received on interface in zone '%s' permitted by"
                                + " policy-map: '%s'",
                            srcZoneName, inspectPolicyMapName))
                    .build()))
        .setSourceName(zonePair.getName())
        .setSourceType(AsaStructureType.SECURITY_ZONE_PAIR.getDescription())
        .build();
    return Optional.of(
        ExprAclLine.accepting()
            .setMatchCondition(new PermittedByAcl(zonePairAclName))
            .setName(
                String.format(
                    "Allow traffic from zone '%s' to '%s' permitted by service-policy: %s",
                    srcZoneName, dstZoneName, inspectPolicyMapName))
            .build());
  }

  @VisibleForTesting
  public static TraceElement asaPermitHigherSecurityLevelTrafficTraceElement(int level) {
    return TraceElement.of(String.format("Matched traffic from a higher security level %d", level));
  }

  @VisibleForTesting
  public static TraceElement asaRejectLowerSecurityLevelTraceElement(int level) {
    return TraceElement.of(
        String.format("Matched unfiltered traffic from a lower security level %d", level));
  }

  @VisibleForTesting
  public static TraceElement asaPermitLowerSecurityLevelTraceElement(int level) {
    return TraceElement.of(
        String.format("Matched filtered traffic from a lower security level %d", level));
  }

  private List<ExprAclLine> createSecurityLevelAcl(String zoneName) {
    Integer level = _securityLevels.get(zoneName);
    if (level == null) {
      return ImmutableList.of();
    }

    // Allow outbound traffic from interfaces with higher security levels unconditionally
    List<ExprAclLine> lines =
        _interfacesBySecurityLevel.keySet().stream()
            .filter(l -> l > level)
            .map(
                l ->
                    ExprAclLine.accepting()
                        .setName("Traffic from security level " + l)
                        .setTraceElement(asaPermitHigherSecurityLevelTrafficTraceElement(l))
                        .setMatchCondition(
                            new MatchSrcInterface(
                                _interfacesBySecurityLevel.get(l).stream()
                                    .map(this::getNewInterfaceName)
                                    .collect(Collectors.toList())))
                        .build())
            .collect(Collectors.toList());

    // Allow outbound traffic from interfaces with lower security levels if that interface has an
    // inbound ACL
    lines.addAll(
        _interfacesBySecurityLevel.keySet().stream()
            .filter(l -> l < level)
            .map(
                l ->
                    ExprAclLine.accepting()
                        .setName("Traffic from security level " + l + " with inbound filter")
                        .setTraceElement(asaPermitLowerSecurityLevelTraceElement(l))
                        .setMatchCondition(
                            new MatchSrcInterface(
                                _interfacesBySecurityLevel.get(l).stream()
                                    .filter(iface -> iface.getIncomingFilter() != null)
                                    .map(this::getNewInterfaceName)
                                    .collect(Collectors.toList())))
                        .build())
            .filter(
                line ->
                    !((MatchSrcInterface) line.getMatchCondition()).getSrcInterfaces().isEmpty())
            .collect(Collectors.toList()));

    return lines;
  }

  public static String computeZoneOutgoingAclName(@Nonnull String zoneName) {
    return String.format("~ZONE_OUTGOING_ACL~%s~", zoneName);
  }

  public static String computeZonePairAclName(
      @Nonnull String srcZoneName, @Nonnull String dstZoneName) {
    return String.format("~ZONE_PAIR_ACL~SRC~%s~DST~%s", srcZoneName, dstZoneName);
  }

  public static String computeInspectPolicyMapAclName(@Nonnull String inspectPolicyMapName) {
    return String.format("~INSPECT_POLICY_MAP_ACL~%s~", inspectPolicyMapName);
  }

  public static String computeInspectClassMapAclName(@Nonnull String inspectClassMapName) {
    return String.format("~INSPECT_CLASS_MAP_ACL~%s~", inspectClassMapName);
  }

  public static String computeASASecurityLevelZoneName(int securityLevel) {
    return String.format("SECURITY_LEVEL_%s", securityLevel);
  }

  private boolean isAclUsedForRouting(@Nonnull String aclName) {
    String currentMapName;
    for (Vrf vrf : _vrfs.values()) {
      // check ospf policies
      for (OspfProcess ospfProcess : vrf.getOspfProcesses().values()) {
        for (OspfRedistributionPolicy rp : ospfProcess.getRedistributionPolicies().values()) {
          currentMapName = rp.getRouteMap();
          if (containsIpAccessList(aclName, currentMapName)) {
            return true;
          }
        }
        currentMapName = ospfProcess.getDefaultInformationOriginateMap();
        if (containsIpAccessList(aclName, currentMapName)) {
          return true;
        }
      }
      RipProcess ripProcess = vrf.getRipProcess();
      if (ripProcess != null) {
        // check rip distribute lists
        if (ripProcess.getDistributeListInAcl()
            && ripProcess.getDistributeListIn().equals(aclName)) {
          return true;
        }
        if (ripProcess.getDistributeListOutAcl()
            && ripProcess.getDistributeListOut().equals(aclName)) {
          return true;
        }
        // check rip redistribution policies
        for (RipRedistributionPolicy rp : ripProcess.getRedistributionPolicies().values()) {
          currentMapName = rp.getRouteMap();
          if (containsIpAccessList(aclName, currentMapName)) {
            return true;
          }
        }
      }
      // check bgp policies
      BgpProcess bgpProcess = vrf.getBgpProcess();
      if (bgpProcess != null) {
        for (BgpRedistributionPolicy rp : bgpProcess.getRedistributionPolicies().values()) {
          currentMapName = rp.getRouteMap();
          if (containsIpAccessList(aclName, currentMapName)) {
            return true;
          }
        }
        for (BgpPeerGroup pg : bgpProcess.getAllPeerGroups()) {
          currentMapName = pg.getInboundRouteMap();
          if (containsIpAccessList(aclName, currentMapName)) {
            return true;
          }
          currentMapName = pg.getOutboundRouteMap();
          if (containsIpAccessList(aclName, currentMapName)) {
            return true;
          }
          currentMapName = pg.getDefaultOriginateMap();
          if (containsIpAccessList(aclName, currentMapName)) {
            return true;
          }
          if (aclName.equals(pg.getInboundIpAccessList())
              || aclName.equals(pg.getOutboundIpAccessList())) {
            return true;
          }
        }
      }
      // check EIGRP policies
      // distribute lists
      if (vrf.getEigrpProcesses().values().stream()
          .flatMap(
              eigrpProcess ->
                  Streams.concat(
                      Stream.of(
                              eigrpProcess.getInboundGlobalDistributeList(),
                              eigrpProcess.getOutboundGlobalDistributeList())
                          .filter(Objects::nonNull),
                      eigrpProcess.getInboundInterfaceDistributeLists().values().stream(),
                      eigrpProcess.getOutboundInterfaceDistributeLists().values().stream()))
          .anyMatch(distributeList -> distributeList.getFilterName().equals(aclName))) {
        return true;
      }
      // EIGRP redistribution policy
      if (vrf.getEigrpProcesses().values().stream()
          .map(EigrpProcess::getRedistributionPolicies)
          .flatMap(redisrPolicies -> redisrPolicies.values().stream())
          .anyMatch(rm -> containsIpAccessList(aclName, rm.getRouteMap()))) {
        return true;
      }
    }
    return false;
  }

  private boolean isAclUsedForRoutingv6(String aclName) {
    String currentMapName;
    for (Vrf vrf : _vrfs.values()) {
      // check ospf policies
      for (OspfProcess ospfProcess : vrf.getOspfProcesses().values()) {
        for (OspfRedistributionPolicy rp : ospfProcess.getRedistributionPolicies().values()) {
          currentMapName = rp.getRouteMap();
          if (containsIpv6AccessList(aclName, currentMapName)) {
            return true;
          }
        }
        currentMapName = ospfProcess.getDefaultInformationOriginateMap();
        if (containsIpAccessList(aclName, currentMapName)) {
          return true;
        }
      }
      // check bgp policies
      BgpProcess bgpProcess = vrf.getBgpProcess();
      if (bgpProcess != null) {
        for (BgpRedistributionPolicy rp : bgpProcess.getRedistributionPolicies().values()) {
          currentMapName = rp.getRouteMap();
          if (containsIpv6AccessList(aclName, currentMapName)) {
            return true;
          }
        }
        for (BgpPeerGroup pg : bgpProcess.getAllPeerGroups()) {
          currentMapName = pg.getInboundRouteMap();
          if (containsIpv6AccessList(aclName, currentMapName)) {
            return true;
          }
          currentMapName = pg.getInboundRoute6Map();
          if (containsIpv6AccessList(aclName, currentMapName)) {
            return true;
          }
          currentMapName = pg.getOutboundRouteMap();
          if (containsIpv6AccessList(aclName, currentMapName)) {
            return true;
          }
          currentMapName = pg.getOutboundRoute6Map();
          if (containsIpv6AccessList(aclName, currentMapName)) {
            return true;
          }
          currentMapName = pg.getDefaultOriginateMap();
          if (containsIpv6AccessList(aclName, currentMapName)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private void markCommunityLists(AsaStructureUsage... usages) {
    for (AsaStructureUsage usage : usages) {
      markAbstractStructure(
          AsaStructureType.COMMUNITY_LIST,
          usage,
          ImmutableList.of(
              AsaStructureType.COMMUNITY_LIST_EXPANDED, AsaStructureType.COMMUNITY_LIST_STANDARD));
    }
  }

  private void markExtcommunityLists(AsaStructureUsage... usages) {
    for (AsaStructureUsage usage : usages) {
      markAbstractStructure(
          AsaStructureType.EXTCOMMUNITY_LIST,
          usage,
          ImmutableList.of(
              AsaStructureType.EXTCOMMUNITY_LIST_EXPANDED,
              AsaStructureType.EXTCOMMUNITY_LIST_STANDARD));
    }
  }

  /**
   * Resolves the addresses of the interfaces used in localInterfaceName of IsaKmpProfiles and
   * Keyrings
   */
  private void resolveKeyringIsakmpProfileAddresses() {
    Map<String, Ip> ifaceNameToPrimaryIp = computeInterfaceOwnedPrimaryIp(_interfaces);

    _keyrings.values().stream()
        .filter(keyring -> !keyring.getLocalInterfaceName().equals(UNSET_LOCAL_INTERFACE))
        .forEach(
            keyring ->
                keyring.setLocalAddress(
                    firstNonNull(
                        ifaceNameToPrimaryIp.get(keyring.getLocalInterfaceName()), Ip.AUTO)));

    _isakmpProfiles.values().stream()
        .filter(
            isakmpProfile -> !isakmpProfile.getLocalInterfaceName().equals(UNSET_LOCAL_INTERFACE))
        .forEach(
            isakmpProfile ->
                isakmpProfile.setLocalAddress(
                    firstNonNull(
                        ifaceNameToPrimaryIp.get(isakmpProfile.getLocalInterfaceName()), Ip.AUTO)));
  }

  /** Resolves the addresses of the interfaces used in sourceInterfaceName of Tunnel interfaces */
  private void resolveTunnelSourceInterfaces() {
    Map<String, Ip> ifaceNameToPrimaryIp = computeInterfaceOwnedPrimaryIp(_interfaces);

    for (Interface iface : _interfaces.values()) {
      Tunnel tunnel = iface.getTunnel();
      if (tunnel != null && !tunnel.getSourceInterfaceName().equals(UNSET_LOCAL_INTERFACE)) {
        tunnel.setSourceAddress(ifaceNameToPrimaryIp.get(tunnel.getSourceInterfaceName()));
      }
    }
  }

  public Map<String, IcmpTypeObjectGroup> getIcmpTypeObjectGroups() {
    return _icmpTypeObjectGroups;
  }

  public Map<String, NetworkObjectGroup> getNetworkObjectGroups() {
    return _networkObjectGroups;
  }

  public Map<String, NetworkObjectInfo> getNetworkObjectInfos() {
    return _networkObjectInfos;
  }

  public Map<String, NetworkObject> getNetworkObjects() {
    return _networkObjects;
  }

  public Map<String, ObjectGroup> getObjectGroups() {
    return _objectGroups;
  }

  public Map<String, ProtocolObjectGroup> getProtocolObjectGroups() {
    return _protocolObjectGroups;
  }

  public Map<String, ServiceObjectGroup> getServiceObjectGroups() {
    return _serviceObjectGroups;
  }

  public Map<String, ServiceObject> getServiceObjects() {
    return _serviceObjects;
  }

  public Map<String, InspectClassMap> getInspectClassMaps() {
    return _inspectClassMaps;
  }

  public Map<String, InspectPolicyMap> getInspectPolicyMaps() {
    return _inspectPolicyMaps;
  }

  public Map<String, Map<String, SecurityZonePair>> getSecurityZonePairs() {
    return _securityZonePairs;
  }

  public Map<String, SecurityZone> getSecurityZones() {
    return _securityZones;
  }

  public Map<String, TrackMethod> getTrackingGroups() {
    return _trackingGroups;
  }

  private void convertCommunityLists(Configuration c) {
    // create CommunitySetMatchExpr for route-map match community
    _standardCommunityLists.forEach(
        (name, ipCommunityListStandard) ->
            c.getCommunitySetMatchExprs()
                .put(name, toCommunitySetMatchExpr(ipCommunityListStandard)));
    _expandedCommunityLists.forEach(
        (name, ipCommunityListExpanded) ->
            c.getCommunitySetMatchExprs()
                .put(name, toCommunitySetMatchExpr(ipCommunityListExpanded)));
  }
}
