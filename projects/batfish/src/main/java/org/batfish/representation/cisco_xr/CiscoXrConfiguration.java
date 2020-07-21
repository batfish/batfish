package org.batfish.representation.cisco_xr;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.batfish.common.util.CollectionUtil.toImmutableSortedMap;
import static org.batfish.datamodel.Interface.UNSET_LOCAL_INTERFACE;
import static org.batfish.datamodel.Interface.computeInterfaceType;
import static org.batfish.datamodel.Interface.isRealInterfaceName;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.EXACT_PATH;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.PATH_LENGTH;
import static org.batfish.datamodel.routing_policy.Common.generateGenerationPolicy;
import static org.batfish.datamodel.routing_policy.Common.suppressSummarizedPrefixes;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.clearFalseStatementsAndAddMatchOwnAsn;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.computeDistributeListPolicies;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.convertCryptoMapSet;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.convertMatchesAnyToCommunitySetMatchExpr;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.convertMatchesEveryToCommunitySetMatchExpr;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.eigrpRedistributionPoliciesToStatements;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.generateBgpExportPolicy;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.generateBgpImportPolicy;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.getIsakmpKeyGeneratedName;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.getRsaPubKeyGeneratedName;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.insertDistributeListFilterAndGetPolicy;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.resolveIsakmpProfileIfaceNames;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.resolveKeyringIfaceNames;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.resolveTunnelIfaceNames;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toCommunityMatchExpr;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toCommunitySetExpr;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toIkePhase1Key;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toIkePhase1Policy;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toIkePhase1Proposal;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toIpAccessList;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toIpSpace;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toIpsecPeerConfig;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toIpsecPhase2Policy;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toIpsecPhase2Proposal;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toOspfDeadInterval;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toOspfHelloInterval;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toOspfNetworkType;
import static org.batfish.representation.cisco_xr.OspfProcess.DEFAULT_LOOPBACK_OSPF_COST;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.GeneratedRoute6;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6AccessList;
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
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.Route6FilterLine;
import org.batfish.datamodel.Route6FilterList;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SnmpServer;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.TunnelConfiguration;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
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
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.WithEnvironmentExpr;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.vendor_family.cisco_xr.Aaa;
import org.batfish.datamodel.vendor_family.cisco_xr.AaaAuthentication;
import org.batfish.datamodel.vendor_family.cisco_xr.AaaAuthenticationLogin;
import org.batfish.datamodel.vendor_family.cisco_xr.CiscoXrFamily;
import org.batfish.representation.cisco_xr.Tunnel.TunnelMode;
import org.batfish.vendor.VendorConfiguration;

public final class CiscoXrConfiguration extends VendorConfiguration {

  /** Matches anything but the IPv4 default route. */
  static final Not NOT_DEFAULT_ROUTE = new Not(Common.matchDefaultRoute());

  private static final int CISCO_AGGREGATE_ROUTE_ADMIN_COST = 200;

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
          .put("Bundle-Ether", "Bundle-Ether")
          .put("BE", "Bundle-Ether")
          .put("Bundle-POS", "Bundle-POS")
          .put("BP", "Bundle-POS")
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
          .put("Fa", "FastEthernet")
          .put("fc", "fc")
          .put("fe", "FastEthernet")
          .put("FiftyGigE", "FiftyGigE")
          .put("Fi", "FiftyGigE")
          .put("FortyGigE", "FortyGigE")
          .put("Fo", "FortyGigE")
          .put("FortyGigabitEthernet", "FortyGigabitEthernet")
          .put("FourHundredGigE", "FourHundredGigE")
          .put("F", "FourHundredGigE")
          .put("GCC0", "GCC0")
          .put("G0", "GCC0")
          .put("GCC1", "GCC1")
          .put("G1", "GCC1")
          .put("GigabitEthernet", "GigabitEthernet")
          .put("ge", "GigabitEthernet")
          .put("GMPLS", "GMPLS")
          .put("HundredGigE", "HundredGigE")
          .put("Hu", "HundredGigE")
          .put("ip", "ip")
          .put("Group-Async", "Group-Async")
          .put("LongReachEthernet", "LongReachEthernet")
          .put("Loopback", "Loopback")
          .put("ma", "Management")
          .put("Management", "Management")
          .put("ManagementEthernet", "ManagementEthernet")
          .put("mfr", "mfr")
          .put("mgmt", "mgmt")
          .put("MgmtEth", "MgmtEth")
          .put("Mg", "MgmtEth")
          .put("Modular-Cable", "Modular-Cable")
          .put("Multilink", "Multilink")
          .put("Null", "Null")
          .put("nve", "nve")
          .put("Port-channel", "Port-Channel")
          .put("POS", "POS")
          .put("PTP", "PTP")
          .put("Serial", "Serial")
          .put("Se", "Serial")
          .put("Service-Engine", "Service-Engine")
          .put("SRP", "SRP")
          .put("TenGigabitEthernet", "TenGigabitEthernet")
          .put("TenGigE", "TenGigE")
          .put("Te", "TenGigE")
          .put("trunk", "trunk")
          .put("Tunnel", "Tunnel")
          .put("tunnel-ip", "tunnel-ip")
          .put("tunnel-ipsec", "tunnel-ipsec")
          .put("tunnel-mte", "tunnel-mte")
          .put("tunnel-te", "tunnel-te")
          .put("tunnel-tp", "tunnel-tp")
          .put("TwentyFiveGigE", "TwentyFiveGigE")
          .put("TF", "TwentyFiveGigE")
          .put("TwoHundredGigE", "TwoHundredGigE")
          .put("TH", "TwoHundredGigE")
          .put("ve", "VirtualEthernet")
          .put("Virtual-Template", "Virtual-Template")
          .put("Vlan", "Vlan")
          .put("Wideband-Cable", "Wideband-Cable")
          .put("Wlan-ap", "Wlan-ap")
          .put("Wlan-GigabitEthernet", "Wlan-GigabitEthernet")
          .build();

  static final boolean DEFAULT_VRRP_PREEMPT = true;

  static final int DEFAULT_VRRP_PRIORITY = 100;

  public static final String MANAGEMENT_VRF_NAME = "management";

  static final int MAX_ADMINISTRATIVE_COST = 32767;

  public static final String MANAGEMENT_INTERFACE_PREFIX = "mgmt";

  private static final int VLAN_NORMAL_MAX_CISCO = 1005;

  private static final int VLAN_NORMAL_MIN_CISCO = 2;

  public static String computeBgpDefaultRouteExportPolicyName(
      boolean ipv4, String vrf, String peer) {
    return String.format(
        "~BGP_DEFAULT_ROUTE_PEER_EXPORT_POLICY:IPv%s:%s:%s~", ipv4 ? "4" : "6", vrf, peer);
  }

  public static String computeBgpPeerImportPolicyName(String vrf, String peer) {
    return String.format("~BGP_PEER_IMPORT_POLICY:%s:%s~", vrf, peer);
  }

  public static @Nonnull String computeCommunitySetMatchAnyName(String name) {
    return String.format("~MATCH_ANY~%s~", name);
  }

  public static @Nonnull String computeCommunitySetMatchEveryName(String name) {
    return String.format("~MATCH_EVERY~%s~", name);
  }

  public static @Nonnull String computeExtcommunitySetRtName(String name) {
    return String.format("RT-%s", name);
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

  public static String computeOspfDefaultRouteGenerationPolicyName(String vrf, String proc) {
    return String.format("~OSPF_DEFAULT_ROUTE_GENERATION_POLICY:%s:%s~", vrf, proc);
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

  static String toJavaRegex(String ciscoXrRegex) {
    String withoutQuotes;
    if (ciscoXrRegex.charAt(0) == '"' && ciscoXrRegex.charAt(ciscoXrRegex.length() - 1) == '"') {
      withoutQuotes = ciscoXrRegex.substring(1, ciscoXrRegex.length() - 1);
    } else {
      withoutQuotes = ciscoXrRegex;
    }
    String underscoreReplacement = "(,|\\\\{|\\\\}|^|\\$| )";
    String output = withoutQuotes.replaceAll("_", underscoreReplacement);
    return output;
  }

  private final Map<String, AsPathSet> _asPathSets;

  private final CiscoXrFamily _cf;

  private final Map<String, CryptoMapSet> _cryptoMapSets;

  private final Map<String, NamedRsaPubKey> _cryptoNamedRsaPubKeys;

  private final List<Ip> _dhcpRelayServers;

  private NavigableSet<String> _dnsServers;

  private String _dnsSourceInterface;

  private String _domainName;

  private final @Nonnull Map<String, ExtcommunitySetRt> _extcommunitySetRts;

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

  private final Map<String, InspectClassMap> _inspectClassMaps;

  private final Map<String, InspectPolicyMap> _inspectPolicyMaps;

  private final Map<String, Interface> _interfaces;

  private final Map<String, IpsecProfile> _ipsecProfiles;

  private final Map<String, IpsecTransformSet> _ipsecTransformSets;

  private final Map<String, Ipv4AccessList> _ipv4Acls;

  private final Map<String, Ipv6AccessList> _ipv6Acls;

  private final List<IsakmpKey> _isakmpKeys;

  private final Map<Integer, IsakmpPolicy> _isakmpPolicies;

  private final Map<String, IsakmpProfile> _isakmpProfiles;

  private final Map<String, Keyring> _keyrings;

  private final Map<String, MacAccessList> _macAccessLists;

  private final Map<String, IcmpTypeObjectGroup> _icmpTypeObjectGroups;

  private final Map<String, IntegerSpace> _namedVlans;

  private final Map<String, NetworkObjectGroup> _networkObjectGroups;

  private final Map<String, NetworkObjectInfo> _networkObjectInfos;

  private final Map<String, NetworkObject> _networkObjects;

  private String _ntpSourceInterface;

  private final Map<String, ObjectGroup> _objectGroups;

  private final Map<String, Prefix6List> _prefix6Lists;

  private final Map<String, PrefixList> _prefixLists;

  private final Map<String, ProtocolObjectGroup> _protocolObjectGroups;

  private final Map<String, RoutePolicy> _routePolicies;

  private final Map<String, ServiceObject> _serviceObjects;

  private SnmpServer _snmpServer;

  private String _snmpSourceInterface;

  private boolean _spanningTreePortfastDefault;

  private NavigableSet<String> _tacacsServers;

  private String _tacacsSourceInterface;

  private ConfigurationFormat _vendor;

  private final Map<String, Vrf> _vrfs;

  private final SortedMap<String, VrrpInterface> _vrrpGroups;

  private final Map<String, ServiceObjectGroup> _serviceObjectGroups;

  private final Map<String, TrackMethod> _trackingGroups;

  private Map<String, XrCommunitySet> _communitySets;

  public CiscoXrConfiguration() {
    _asPathSets = new TreeMap<>();
    _cf = new CiscoXrFamily();
    _communitySets = new TreeMap<>();
    _cryptoNamedRsaPubKeys = new TreeMap<>();
    _cryptoMapSets = new HashMap<>();
    _dhcpRelayServers = new ArrayList<>();
    _dnsServers = new TreeSet<>();
    _ipv4Acls = new TreeMap<>();
    _ipv6Acls = new TreeMap<>();
    _extcommunitySetRts = new HashMap<>();
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
    _networkObjectGroups = new TreeMap<>();
    _networkObjectInfos = new TreeMap<>();
    _networkObjects = new TreeMap<>();
    _objectGroups = new TreeMap<>();
    _prefixLists = new TreeMap<>();
    _prefix6Lists = new TreeMap<>();
    _protocolObjectGroups = new TreeMap<>();
    _routePolicies = new TreeMap<>();
    _serviceObjectGroups = new TreeMap<>();
    _serviceObjects = new TreeMap<>();
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
                    (groupNum, vrrpGroup) -> {
                      org.batfish.datamodel.VrrpGroup newGroup =
                          new org.batfish.datamodel.VrrpGroup(groupNum);
                      newGroup.setPreempt(vrrpGroup.getPreempt());
                      newGroup.setPriority(vrrpGroup.getPriority());
                      ConcreteInterfaceAddress ifaceAddress = iface.getConcreteAddress();
                      if (ifaceAddress != null) {
                        int prefixLength = ifaceAddress.getNetworkBits();
                        Ip address = vrrpGroup.getVirtualAddress();
                        if (address != null) {
                          ConcreteInterfaceAddress virtualAddress =
                              ConcreteInterfaceAddress.create(address, prefixLength);
                          newGroup.setVirtualAddress(virtualAddress);
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
                      iface.addVrrpGroup(groupNum, newGroup);
                    });
          }
        });
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

  public Map<String, AsPathSet> getAsPathSets() {
    return _asPathSets;
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

  public CiscoXrFamily getCf() {
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

  public @Nonnull Map<String, ExtcommunitySetRt> getExtcommunitySetRts() {
    return _extcommunitySetRts;
  }

  public Map<String, Ipv4AccessList> getIpv4Acls() {
    return _ipv4Acls;
  }

  public Map<String, Ipv6AccessList> getIpv6Acls() {
    return _ipv6Acls;
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

  public String getNtpSourceInterface() {
    return _ntpSourceInterface;
  }

  public Map<String, Prefix6List> getPrefix6Lists() {
    return _prefix6Lists;
  }

  public Map<String, PrefixList> getPrefixLists() {
    return _prefixLists;
  }

  public Map<String, RoutePolicy> getRoutePolicies() {
    return _routePolicies;
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

  public NavigableSet<String> getTacacsServers() {
    return _tacacsServers;
  }

  public String getTacacsSourceInterface() {
    return _tacacsSourceInterface;
  }

  private Ip getUpdateSource(
      Configuration c,
      String vrfName,
      LeafBgpPeerGroup lpg,
      String updateSourceInterface,
      boolean ipv4) {
    Ip updateSource = null;
    if (ipv4) {
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
          updateSource = Ip.AUTO;
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
      if (updateSource == null && lpg.getNeighborPrefix().getStartIp().valid()) {
        _w.redFlag("Could not determine update source for BGP neighbor: '" + lpg.getName() + "'");
      }
    }
    return updateSource;
  }

  public ConfigurationFormat getVendor() {
    return _vendor;
  }

  public Map<String, Vrf> getVrfs() {
    return _vrfs;
  }

  public SortedMap<String, VrrpInterface> getVrrpGroups() {
    return _vrrpGroups;
  }

  private void markAcls(CiscoXrStructureUsage... usages) {
    for (CiscoXrStructureUsage usage : usages) {
      markAbstractStructure(
          CiscoXrStructureType.IP_ACCESS_LIST,
          usage,
          ImmutableList.of(
              CiscoXrStructureType.IPV4_ACCESS_LIST, CiscoXrStructureType.IPV6_ACCESS_LIST));
    }
  }

  private void markIpOrMacAcls(CiscoXrStructureUsage... usages) {
    for (CiscoXrStructureUsage usage : usages) {
      markAbstractStructure(
          CiscoXrStructureType.ACCESS_LIST,
          usage,
          Arrays.asList(
              CiscoXrStructureType.IPV4_ACCESS_LIST,
              CiscoXrStructureType.IPV6_ACCESS_LIST,
              CiscoXrStructureType.MAC_ACCESS_LIST));
    }
  }

  private void processFailoverSettings() {
    if (_failover) {
      Interface commIface;
      ConcreteInterfaceAddress commAddress;
      Interface sigIface;
      ConcreteInterfaceAddress sigAddress;
      if (_failoverSecondary) {
        commIface = _interfaces.get(_failoverCommunicationInterface);
        commAddress = _failoverStandbyAddresses.get(_failoverCommunicationInterfaceAlias);
        sigIface = _interfaces.get(_failoverStatefulSignalingInterface);
        sigAddress = _failoverStandbyAddresses.get(_failoverStatefulSignalingInterfaceAlias);
        for (Interface iface : _interfaces.values()) {
          iface.setAddress(iface.getStandbyAddress());
        }
      } else {
        commIface = _interfaces.get(_failoverCommunicationInterface);
        commAddress = _failoverPrimaryAddresses.get(_failoverCommunicationInterfaceAlias);
        sigIface = _interfaces.get(_failoverStatefulSignalingInterface);
        sigAddress = _failoverPrimaryAddresses.get(_failoverStatefulSignalingInterfaceAlias);
      }
      commIface.setAddress(commAddress);
      commIface.setActive(true);
      sigIface.setAddress(sigAddress);
      sigIface.setActive(true);
    }
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
  }

  public void setNtpSourceInterface(String ntpSourceInterface) {
    _ntpSourceInterface = ntpSourceInterface;
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
    _vendor = format;
  }

  private org.batfish.datamodel.BgpProcess toBgpProcess(
      Configuration c, BgpProcess proc, String vrfName) {
    org.batfish.datamodel.Vrf v = c.getVrfs().get(vrfName);
    Ip bgpRouterId = getBgpRouterId(c, vrfName, proc);
    int ebgpAdmin = RoutingProtocol.BGP.getDefaultAdministrativeCost(c.getConfigurationFormat());
    int ibgpAdmin = RoutingProtocol.IBGP.getDefaultAdministrativeCost(c.getConfigurationFormat());
    org.batfish.datamodel.BgpProcess newBgpProcess =
        new org.batfish.datamodel.BgpProcess(bgpRouterId, ebgpAdmin, ibgpAdmin);
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

    int defaultMetric = proc.getDefaultMetric();

    /*
     * Create common bgp export policy. This policy encompasses network
     * statements, aggregate-address with/without summary-only, redistribution
     * from other protocols, and default-origination
     */
    RoutingPolicy bgpCommonExportPolicy =
        new RoutingPolicy(Names.generatedBgpCommonExportPolicyName(vrfName), c);
    c.getRoutingPolicies().put(bgpCommonExportPolicy.getName(), bgpCommonExportPolicy);
    List<Statement> bgpCommonExportStatements = bgpCommonExportPolicy.getStatements();

    // Never export routes suppressed because they are more specific than summary-only aggregate
    Stream<Prefix> summaryOnlyNetworks =
        proc.getAggregateNetworks().entrySet().stream()
            .filter(e -> e.getValue().getSummaryOnly())
            .map(Entry::getKey);
    If suppressSummaryOnly = suppressSummarizedPrefixes(c, vrfName, summaryOnlyNetworks);
    if (suppressSummaryOnly != null) {
      bgpCommonExportStatements.add(suppressSummaryOnly);
    }

    // The body of the export policy is a huge disjunction over many reasons routes may be exported.
    Disjunction routesShouldBeExported = new Disjunction();
    bgpCommonExportStatements.add(
        new If(
            routesShouldBeExported,
            ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
            ImmutableList.of()));
    // This list of reasons to export a route will be built up over the remainder of this function.
    List<BooleanExpr> exportConditions = routesShouldBeExported.getDisjuncts();

    // Finally, the export policy ends with returning false: do not export unmatched routes.
    bgpCommonExportStatements.add(Statements.ReturnFalse.toStaticStatement());

    // Export the generated routes for aggregate ipv4 addresses
    for (Entry<Prefix, BgpAggregateIpv4Network> e : proc.getAggregateNetworks().entrySet()) {
      Prefix prefix = e.getKey();
      BgpAggregateIpv4Network aggNet = e.getValue();

      // Generate a policy that matches routes to be aggregated.
      RoutingPolicy genPolicy = generateGenerationPolicy(c, vrfName, prefix);

      GeneratedRoute.Builder gr =
          GeneratedRoute.builder()
              .setNetwork(prefix)
              .setAdmin(CISCO_AGGREGATE_ROUTE_ADMIN_COST)
              .setGenerationPolicy(genPolicy.getName())
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
      String attributeMapName = aggNet.getAttributeMap();
      if (attributeMapName != null) {
        // TODO update to route-policy if valid, or delete grammar and VS
      }
      exportAggregateConditions.add(bgpRedistributeWithEnvironmentExpr(weInterior, OriginType.IGP));

      v.getGeneratedRoutes().add(gr.build());
      // Do export a generated aggregate.
      exportConditions.add(new Conjunction(exportAggregateConditions));
    }

    // add generated routes for aggregate ipv6 addresses
    // TODO: merge with above to make cleaner
    for (Entry<Prefix6, BgpAggregateIpv6Network> e : proc.getAggregateIpv6Networks().entrySet()) {
      Prefix6 prefix6 = e.getKey();
      BgpAggregateIpv6Network aggNet = e.getValue();

      // create generation policy for aggregate network
      RoutingPolicy genPolicy = generateGenerationPolicy(c, vrfName, prefix6);
      GeneratedRoute6 gr = new GeneratedRoute6(prefix6, CISCO_AGGREGATE_ROUTE_ADMIN_COST);
      gr.setGenerationPolicy(genPolicy.getName());
      gr.setDiscard(true);
      v.getGeneratedIpv6Routes().add(gr);

      // set attribute map for aggregate network
      String attributeMapName = aggNet.getAttributeMap();
      if (attributeMapName != null) {
        // TODO update to route-policy if valid, or delete grammar and VS
      }
    }

    // Export RIP routes that should be redistributed.
    BgpRedistributionPolicy redistributeRipPolicy =
        proc.getRedistributionPolicies().get(RoutingProtocol.RIP);
    if (redistributeRipPolicy != null) {
      BooleanExpr weInterior = BooleanExprs.TRUE;
      Conjunction exportRipConditions = new Conjunction();
      exportRipConditions.setComment("Redistribute RIP routes into BGP");
      exportRipConditions.getConjuncts().add(new MatchProtocol(RoutingProtocol.RIP));
      String mapName = redistributeRipPolicy.getRouteMap();
      if (mapName != null) {
        // TODO update to route-policy if valid, or delete grammar and VS
      }
      BooleanExpr we = bgpRedistributeWithEnvironmentExpr(weInterior, OriginType.INCOMPLETE);
      exportRipConditions.getConjuncts().add(we);
      exportConditions.add(exportRipConditions);
    }

    // Export static routes that should be redistributed.
    BgpRedistributionPolicy redistributeStaticPolicy =
        proc.getRedistributionPolicies().get(RoutingProtocol.STATIC);
    if (redistributeStaticPolicy != null) {
      BooleanExpr weInterior = BooleanExprs.TRUE;
      Conjunction exportStaticConditions = new Conjunction();
      exportStaticConditions.setComment("Redistribute static routes into BGP");
      exportStaticConditions.getConjuncts().add(new MatchProtocol(RoutingProtocol.STATIC));
      String mapName = redistributeStaticPolicy.getRouteMap();
      if (mapName != null) {
        // TODO update to route-policy if valid, or delete grammar and VS
      }
      BooleanExpr we = bgpRedistributeWithEnvironmentExpr(weInterior, OriginType.INCOMPLETE);
      exportStaticConditions.getConjuncts().add(we);
      exportConditions.add(exportStaticConditions);
    }

    // Export connected routes that should be redistributed.
    BgpRedistributionPolicy redistributeConnectedPolicy =
        proc.getRedistributionPolicies().get(RoutingProtocol.CONNECTED);
    if (redistributeConnectedPolicy != null) {
      BooleanExpr weInterior = BooleanExprs.TRUE;
      Conjunction exportConnectedConditions = new Conjunction();
      exportConnectedConditions.setComment("Redistribute connected routes into BGP");
      exportConnectedConditions.getConjuncts().add(new MatchProtocol(RoutingProtocol.CONNECTED));
      String mapName = redistributeConnectedPolicy.getRouteMap();
      if (mapName != null) {
        // TODO update to route-policy if valid, or delete grammar and VS
      }
      BooleanExpr we = bgpRedistributeWithEnvironmentExpr(weInterior, OriginType.INCOMPLETE);
      exportConnectedConditions.getConjuncts().add(we);
      exportConditions.add(exportConnectedConditions);
    }

    // Export OSPF routes that should be redistributed.
    BgpRedistributionPolicy redistributeOspfPolicy =
        proc.getRedistributionPolicies().get(RoutingProtocol.OSPF);
    if (redistributeOspfPolicy != null) {
      BooleanExpr weInterior = BooleanExprs.TRUE;
      Conjunction exportOspfConditions = new Conjunction();
      exportOspfConditions.setComment("Redistribute OSPF routes into BGP");
      exportOspfConditions.getConjuncts().add(new MatchProtocol(RoutingProtocol.OSPF));
      String mapName = redistributeOspfPolicy.getRouteMap();
      if (mapName != null) {
        // TODO update to route-policy if valid, or delete grammar and VS
      }
      BooleanExpr we = bgpRedistributeWithEnvironmentExpr(weInterior, OriginType.INCOMPLETE);
      exportOspfConditions.getConjuncts().add(we);
      exportConditions.add(exportOspfConditions);
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
              String mapName = bgpNetwork.getRouteMapName();
              BooleanExpr weExpr = BooleanExprs.TRUE;
              if (mapName != null) {
                // TODO update to route-policy if valid, or delete grammar and VS
              }
              BooleanExpr we = bgpRedistributeWithEnvironmentExpr(weExpr, OriginType.IGP);
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
              exportNetworkConditions.getConjuncts().add(we);
              exportConditions.add(exportNetworkConditions);
            });
    if (!proc.getIpv6Networks().isEmpty()) {
      String localFilter6Name = "~BGP_NETWORK6_NETWORKS_FILTER:" + vrfName + "~";
      Route6FilterList localFilter6 = new Route6FilterList(localFilter6Name);
      proc.getIpv6Networks()
          .forEach(
              (prefix6, bgpNetwork6) -> {
                int prefixLen = prefix6.getPrefixLength();
                Route6FilterLine line =
                    new Route6FilterLine(LineAction.PERMIT, prefix6, SubRange.singleton(prefixLen));
                localFilter6.addLine(line);
                String mapName = bgpNetwork6.getRouteMapName();
                if (mapName != null) {
                  // TODO update to route-policy if valid, or delete grammar and VS
                }
              });
      c.getRoute6FilterLists().put(localFilter6Name, localFilter6);
    }

    // Export BGP and IBGP routes.
    exportConditions.add(new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP));

    for (LeafBgpPeerGroup lpg : leafGroups) {
      if (!lpg.getActive() || lpg.getShutdown()) {
        continue;
      }
      if (lpg.getRemoteAs() == null) {
        _w.redFlag("No remote-as set for peer: " + lpg.getName());
        continue;
      }
      if (lpg instanceof Ipv6BgpPeerGroup || lpg instanceof DynamicIpv6BgpPeerGroup) {
        // TODO: implement ipv6 bgp neighbors
        continue;
      }
      // update source
      String updateSourceInterface = lpg.getUpdateSource();
      boolean ipv4 = lpg.getNeighborPrefix() != null;
      Ip updateSource = getUpdateSource(c, vrfName, lpg, updateSourceInterface, ipv4);

      // Get default-originate generation or export policy
      String defaultOriginateGenerationMap = null;
      if (lpg.getDefaultOriginate()) {
        defaultOriginateGenerationMap = lpg.getDefaultOriginateMap();
      }

      // Generate import and export policies
      String peerImportPolicyName = generateBgpImportPolicy(lpg, vrfName, c, _w);
      generateBgpExportPolicy(lpg, vrfName, ipv4, c, _w);

      // If defaultOriginate is set, create default route for this peer group
      GeneratedRoute.Builder defaultRoute = null;
      GeneratedRoute6.Builder defaultRoute6;
      if (lpg.getDefaultOriginate()) {
        defaultRoute = GeneratedRoute.builder();
        defaultRoute.setNetwork(Prefix.ZERO);
        defaultRoute.setAdmin(MAX_ADMINISTRATIVE_COST);
        defaultRoute6 = new GeneratedRoute6.Builder();
        defaultRoute6.setNetwork(Prefix6.ZERO);
        defaultRoute6.setAdmin(MAX_ADMINISTRATIVE_COST);

        if (defaultOriginateGenerationMap != null
            && c.getRoutingPolicies().containsKey(defaultOriginateGenerationMap)) {
          // originate contingent on generation policy
          if (ipv4) {
            defaultRoute.setGenerationPolicy(defaultOriginateGenerationMap);
          } else {
            defaultRoute6.setGenerationPolicy(defaultOriginateGenerationMap);
          }
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
                .setRemoteAs(lpg.getRemoteAs());
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

      AddressFamilyCapabilities ipv4AfSettings =
          AddressFamilyCapabilities.builder()
              .setAdditionalPathsReceive(lpg.getAdditionalPathsReceive())
              .setAdditionalPathsSelectAll(lpg.getAdditionalPathsSelectAll())
              .setAdditionalPathsSend(lpg.getAdditionalPathsSend())
              .setAllowLocalAsIn(lpg.getAllowAsIn())
              .setAllowRemoteAsOut(firstNonNull(lpg.getDisablePeerAsCheck(), Boolean.TRUE))
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
      newNeighborBuilder.setDefaultMetric(defaultMetric);
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
      String ifaceName, Interface iface, Map<String, IpAccessList> ipAccessLists, Configuration c) {
    org.batfish.datamodel.Interface newIface =
        org.batfish.datamodel.Interface.builder()
            .setName(ifaceName)
            .setOwner(c)
            .setType(computeInterfaceType(iface.getName(), c.getConfigurationFormat()))
            .build();
    if (newIface.getInterfaceType() == InterfaceType.VLAN) {
      newIface.setVlan(CommonUtil.getInterfaceVlanNumber(ifaceName));
    }
    String vrfName = iface.getVrf();
    Vrf vrf = _vrfs.computeIfAbsent(vrfName, Vrf::new);
    newIface.setDescription(iface.getDescription());
    newIface.setActive(iface.getActive());
    if (iface.getBundleId() != null) {
      newIface.setChannelGroup(String.format("Bundle-Ether%d", iface.getBundleId()));
    }
    newIface.setCryptoMap(iface.getCryptoMap());
    newIface.setHsrpGroups(
        CollectionUtil.toImmutableMap(
            iface.getHsrpGroups(),
            Entry::getKey,
            e -> CiscoXrConversions.toHsrpGroup(e.getValue())));
    newIface.setHsrpVersion(iface.getHsrpVersion());
    newIface.setAutoState(iface.getAutoState());
    newIface.setVrf(c.getVrfs().get(vrfName));
    newIface.setSpeed(firstNonNull(iface.getSpeed(), Interface.getDefaultSpeed(iface.getName())));
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

    EigrpProcess eigrpProcess = null;
    if (iface.getAddress() != null) {
      for (EigrpProcess process : vrf.getEigrpProcesses().values()) {
        if (process.getNetworks().contains(iface.getAddress().getPrefix())) {
          // Found a process on interface
          if (eigrpProcess != null) {
            // CiscoXr does not recommend running multiple EIGRP autonomous systems on the same
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
              .getOrDefault(iface.getName(), eigrpProcess.getPassiveInterfaceDefault());

      List<If> redistributePolicyStatements =
          eigrpRedistributionPoliciesToStatements(
              eigrpProcess.getRedistributionPolicies().values(), eigrpProcess, this);

      List<If> redistributeAndAllowEigrpFromSelfAsn =
          clearFalseStatementsAndAddMatchOwnAsn(
              redistributePolicyStatements, eigrpProcess.getAsn());

      String policyName =
          String.format("~EIGRP_EXPORT_POLICY_%s_%s_%s", vrfName, eigrpProcess.getAsn(), ifaceName);
      RoutingPolicy routingPolicy =
          insertDistributeListFilterAndGetPolicy(
              c,
              this,
              eigrpProcess.getOutboundInterfaceDistributeLists().get(newIface.getName()),
              redistributeAndAllowEigrpFromSelfAsn,
              policyName);

      c.getRoutingPolicies().put(policyName, routingPolicy);

      newIface.setEigrp(
          EigrpInterfaceSettings.builder()
              .setAsn(eigrpProcess.getAsn())
              .setEnabled(true)
              .setExportPolicy(policyName)
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
      // If allowed VLANs are set, honor them;
      if (iface.getAllowedVlans() != null) {
        newIface.setAllowedVlans(iface.getAllowedVlans());
      } else {
        newIface.setAllowedVlans(Interface.ALL_VLANS);
      }
    }

    String incomingFilterName = iface.getIncomingFilter();
    if (incomingFilterName != null) {
      newIface.setIncomingFilter(ipAccessLists.get(incomingFilterName));
    }
    String outgoingFilterName = iface.getOutgoingFilter();
    if (outgoingFilterName != null) {
      newIface.setOutgoingFilter(ipAccessLists.get(outgoingFilterName));
    }

    String routingPolicyName = iface.getRoutingPolicy();
    if (routingPolicyName != null) {
      newIface.setRoutingPolicy(routingPolicyName);
    }

    return newIface;
  }

  @Nonnull
  private EigrpMetric computeEigrpMetricForInterface(Interface iface, EigrpProcessMode mode) {
    Optional<Double> bw =
        Stream.of(iface.getBandwidth(), Interface.getDefaultBandwidth(iface.getName(), _vendor))
            .filter(Objects::nonNull)
            .findFirst();
    if (!bw.isPresent()) {
      _w.redFlag(
          String.format("Missing bandwidth for %s, EIGRP metric will be wrong", iface.getName()));
    }
    EigrpMetricValues values =
        EigrpMetricValues.builder()
            .setDelay(
                firstNonNull(iface.getDelay(), Interface.getDefaultDelay(iface.getName(), _vendor)))
            .setBandwidth(
                // Scale to kbps
                // TODO: this value is wrong for port-channels but will prevent crashing
                bw.orElse(1e12) / 1000)
            .build();
    if (mode == EigrpProcessMode.CLASSIC) {
      return ClassicMetric.builder().setValues(values).build();
    } else if (mode == EigrpProcessMode.NAMED) {
      return WideMetric.builder().setValues(values).build();
    } else {
      throw new IllegalArgumentException("Invalid EIGRP process mode: " + mode);
    }
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

    // Do not redistribute the default route on Cisco.
    ospfExportConditions.getConjuncts().add(NOT_DEFAULT_ROUTE);

    ImmutableList.Builder<Statement> ospfExportStatements = ImmutableList.builder();

    // Set the metric type and value.
    ospfExportStatements.add(new SetOspfMetricType(policy.getMetricType()));
    long metric = policy.getMetric() != null ? policy.getMetric() : proc.getDefaultMetric(protocol);
    ospfExportStatements.add(new SetMetric(new LiteralLong(metric)));

    // If a route-map filter is present, honor it.
    String exportRouteMapName = policy.getRouteMap();
    if (exportRouteMapName != null) {
      // TODO update to route-policy if valid, or delete grammar and VS
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
      OspfProcess proc, String vrfName, Configuration c, CiscoXrConfiguration oldConfig) {
    Ip routerId = proc.getRouterId();
    if (routerId == null) {
      routerId = CiscoXrConversions.getHighestIp(oldConfig.getInterfaces());
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
            summary.getAdvertised()
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

    String ospfExportPolicyName = "~OSPF_EXPORT_POLICY:" + vrfName + "~";
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
            computeOspfDefaultRouteGenerationPolicyName(vrfName, proc.getName());
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
        // TODO update to route-policy if valid, or delete grammar and VS
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
        // TODO update to route-policy if valid, or delete grammar and VS
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
        // TODO update to route-policy if valid, or delete grammar and VS
      }
      ripExportBgpStatements.add(Statements.ExitAccept.toStaticStatement());
      ripExportBgp.setGuard(ripExportBgpConditions);
    }
    return newProcess;
  }

  private RoutingPolicy toRoutingPolicy(Configuration c, RoutePolicy routePolicy) {
    String name = routePolicy.getName();
    RoutingPolicy rp = new RoutingPolicy(name, c);
    List<Statement> statements = rp.getStatements();
    for (RoutePolicyStatement routePolicyStatement : routePolicy.getStatements()) {
      routePolicyStatement.applyTo(statements, this, c, _w);
    }
    // At the end of a routing policy, we terminate based on the context.
    // 1. we're in call expr context, so we return the local default action of this policy.
    // 2. we're in call statement context, so we just return
    // 3. otherwise, we reach the end of the policy and return the policy's default action.
    If endPolicyBasedOnContext =
        new If(
            BooleanExprs.CALL_EXPR_CONTEXT,
            Collections.singletonList(Statements.ReturnLocalDefaultAction.toStaticStatement()),
            Collections.singletonList(
                new If(
                    BooleanExprs.CALL_STATEMENT_CONTEXT,
                    Collections.singletonList(Statements.Return.toStaticStatement()),
                    Collections.singletonList(Statements.DefaultAction.toStaticStatement()))));
    statements.add(endPolicyBasedOnContext);
    return rp;
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
    Configuration c = new Configuration(_hostname, _vendor);
    c.getVendorFamily().setCiscoXr(_cf);
    c.setDefaultInboundAction(LineAction.PERMIT);
    c.setDefaultCrossZoneAction(LineAction.PERMIT);
    c.setDnsServers(_dnsServers);
    c.setDnsSourceInterface(_dnsSourceInterface);
    c.setDomainName(_domainName);
    c.setNormalVlanRange(new SubRange(VLAN_NORMAL_MIN_CISCO, VLAN_NORMAL_MAX_CISCO));
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
    }

    // snmp server
    if (_snmpServer != null) {
      String snmpServerVrf = _snmpServer.getVrf();
      c.getVrfs().get(snmpServerVrf).setSnmpServer(_snmpServer);
    }

    // convert as-path-sets to vendor independent format
    for (AsPathSet asPathSet : _asPathSets.values()) {
      AsPathAccessList apList = CiscoXrConversions.toAsPathAccessList(asPathSet);
      c.getAsPathAccessLists().put(apList.getName(), apList);
    }

    convertCommunitySets(c);
    convertExtcommunitySetRts(c);

    // convert prefix lists to route filter lists
    for (PrefixList prefixList : _prefixLists.values()) {
      RouteFilterList newRouteFilterList = CiscoXrConversions.toRouteFilterList(prefixList);
      c.getRouteFilterLists().put(newRouteFilterList.getName(), newRouteFilterList);
    }

    // convert ipv6 prefix lists to route6 filter lists
    for (Prefix6List prefixList : _prefix6Lists.values()) {
      Route6FilterList newRouteFilterList = CiscoXrConversions.toRoute6FilterList(prefixList);
      c.getRoute6FilterLists().put(newRouteFilterList.getName(), newRouteFilterList);
    }

    // convert access lists to access lists or route filter
    for (Ipv4AccessList eaList : _ipv4Acls.values()) {
      if (isAclUsedForRouting(eaList.getName())) {
        RouteFilterList rfList = CiscoXrConversions.toRouteFilterList(eaList);
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
                            name, CiscoXrStructureType.NETWORK_OBJECT_GROUP.getDescription())));
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
                            name, CiscoXrStructureType.NETWORK_OBJECT.getDescription())));

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
                .put(computeServiceObjectGroupAclName(name), toIpAccessList(serviceObjectGroup)));
    _serviceObjects.forEach(
        (name, serviceObject) ->
            c.getIpAccessLists()
                .put(computeServiceObjectAclName(name), toIpAccessList(serviceObject)));

    // convert IPv6 access lists to ipv6 access lists route6 filter lists
    for (Ipv6AccessList eaList : _ipv6Acls.values()) {
      Ip6AccessList ipaList = CiscoXrConversions.toIp6AccessList(eaList);
      c.getIp6AccessLists().put(ipaList.getName(), ipaList);
    }

    // convert RoutePolicy to RoutingPolicy
    for (RoutePolicy routePolicy : _routePolicies.values()) {
      RoutingPolicy routingPolicy = toRoutingPolicy(c, routePolicy);
      c.getRoutingPolicies().put(routingPolicy.getName(), routingPolicy);
    }

    createInspectClassMapAcls(c);

    // create inspect policy-map ACLs
    createInspectPolicyMapAcls(c);

    // convert interfaces
    _interfaces.forEach(
        (ifaceName, iface) -> {
          org.batfish.datamodel.Interface newInterface =
              toInterface(ifaceName, iface, c.getIpAccessLists(), c);
          String vrfName = iface.getVrf();
          if (vrfName == null) {
            throw new BatfishException("Missing vrf name for iface: '" + ifaceName + "'");
          }
          c.getAllInterfaces().put(ifaceName, newInterface);
        });
    /*
     * Second pass over the interfaces to set dependency pointers correctly for portchannels
     * and tunnel interfaces
     * TODO: VLAN interfaces
     */
    _interfaces.forEach(
        (ifaceName, iface) -> {
          // Bundle ID
          Integer bundleId = iface.getBundleId();
          if (bundleId != null) {
            String bundleName = String.format("Bundle-Ether%d", bundleId);
            org.batfish.datamodel.Interface viIface = c.getAllInterfaces().get(bundleName);
            if (viIface != null) {
              viIface.addDependency(new Dependency(ifaceName, DependencyType.AGGREGATE));
            }
          }
          // Portchannel subinterfaces
          Matcher m = INTERFACE_WITH_SUBINTERFACE.matcher(iface.getName());
          if (m.matches()) {
            String parentInterfaceName = m.group(1);
            Interface parentInterface = _interfaces.get(parentInterfaceName);
            if (parentInterface != null) {
              org.batfish.datamodel.Interface viIface = c.getAllInterfaces().get(ifaceName);
              if (viIface != null) {
                viIface.addDependency(new Dependency(parentInterfaceName, DependencyType.BIND));
              }
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
          _w.redFlag(String.format("No IPSec Profile set for IPSec tunnel %s", name));
          continue;
        }
        // convert to IpsecPeerConfig
        ipsecPeerConfigBuilder.put(name, toIpsecPeerConfig(tunnel, name, this, c));
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
            newVrf.getStaticRoutes().add(CiscoXrConversions.toStaticRoute(staticRoute));
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
              .map(proc -> CiscoXrConversions.toEigrpProcess(proc, vrfName, c, this))
              .filter(Objects::nonNull)
              .forEach(newVrf::addEigrpProcess);

          // convert isis process
          IsisProcess isisProcess = vrf.getIsisProcess();
          if (isisProcess != null) {
            org.batfish.datamodel.isis.IsisProcess newIsisProcess =
                CiscoXrConversions.toIsisProcess(isisProcess, this);
            newVrf.setIsisProcess(newIsisProcess);
          }

          ///////////////////////////////////////////////
          // BEGIN Convert BGP process for various vendors
          // Hybrid
          BgpProcess bgpProcess = vrf.getBgpProcess();
          if (bgpProcess != null) {
            org.batfish.datamodel.BgpProcess newBgpProcess = toBgpProcess(c, bgpProcess, vrfName);
            newVrf.setBgpProcess(newBgpProcess);
          }
          // END Convert BGP process for various vendors
          ///////////////////////////////////////////////
        });

    /*
     * Another pass over interfaces to push final settings to VI interfaces and issue final warnings
     * (e.g. has OSPF settings but no associated OSPF process)
     */
    _interfaces.forEach(
        (ifaceName, vsIface) -> {
          org.batfish.datamodel.Interface iface = c.getAllInterfaces().get(ifaceName);
          assert iface != null;
          // Conversion of interface OSPF settings usually occurs per area
          // If the iface does not have an area, then need warn and convert settings here instead
          if (iface.getOspfAreaName() == null) {
            // Not part of an OSPF area
            if (vsIface.getOspfArea() != null
                || vsIface.getOspfCost() != null
                || vsIface.getOspfPassive() != null
                || vsIface.getOspfNetworkType() != null
                || vsIface.getOspfDeadInterval() != null
                || vsIface.getOspfHelloInterval() != null) {
              _w.redFlag(
                  "Interface: '"
                      + ifaceName
                      + "' contains OSPF settings, but there is no corresponding OSPF area (or process)");
              finalizeInterfaceOspfSettings(iface, vsIface, null, null);
            }
          }
        });

    // Define the Null0 interface if it has been referenced. Otherwise, these show as undefined
    // references.
    Optional<Integer> firstRefToNull0 =
        _structureReferences.getOrDefault(CiscoXrStructureType.INTERFACE, ImmutableSortedMap.of())
            .getOrDefault("Null0", ImmutableSortedMap.of()).entrySet().stream()
            .flatMap(e -> e.getValue().stream())
            .min(Integer::compare);
    if (firstRefToNull0.isPresent()) {
      defineSingleLineStructure(CiscoXrStructureType.INTERFACE, "Null0", firstRefToNull0.get());
    }

    markConcreteStructure(CiscoXrStructureType.BFD_TEMPLATE);
    markConcreteStructure(CiscoXrStructureType.COMMUNITY_SET);
    markConcreteStructure(CiscoXrStructureType.EXTCOMMUNITY_SET_RT);
    markConcreteStructure(CiscoXrStructureType.INTERFACE);

    markConcreteStructure(CiscoXrStructureType.IPV4_ACCESS_LIST);
    markConcreteStructure(CiscoXrStructureType.IPV6_ACCESS_LIST);

    // mark references to ACLs that may not appear in data model
    markIpOrMacAcls(
        CiscoXrStructureUsage.CLASS_MAP_ACCESS_GROUP, CiscoXrStructureUsage.CLASS_MAP_ACCESS_LIST);
    markAcls(
        CiscoXrStructureUsage.ACCESS_GROUP_GLOBAL_FILTER,
        CiscoXrStructureUsage.COPS_LISTENER_ACCESS_LIST,
        CiscoXrStructureUsage.CRYPTO_MAP_IPSEC_ISAKMP_ACL,
        CiscoXrStructureUsage.INSPECT_CLASS_MAP_MATCH_ACCESS_GROUP,
        CiscoXrStructureUsage.INTERFACE_IGMP_ACCESS_GROUP_ACL,
        CiscoXrStructureUsage.INTERFACE_IGMP_HOST_PROXY_ACCESS_LIST,
        CiscoXrStructureUsage.INTERFACE_INCOMING_FILTER,
        CiscoXrStructureUsage.INTERFACE_IP_INBAND_ACCESS_GROUP,
        CiscoXrStructureUsage.INTERFACE_OUTGOING_FILTER,
        CiscoXrStructureUsage.OSPF_DISTRIBUTE_LIST_ACCESS_LIST_IN,
        CiscoXrStructureUsage.OSPF_DISTRIBUTE_LIST_ACCESS_LIST_OUT,
        CiscoXrStructureUsage.RIP_DISTRIBUTE_LIST,
        CiscoXrStructureUsage.ROUTER_ISIS_DISTRIBUTE_LIST_ACL,
        CiscoXrStructureUsage.SNMP_SERVER_FILE_TRANSFER_ACL,
        CiscoXrStructureUsage.SNMP_SERVER_TFTP_SERVER_LIST,
        CiscoXrStructureUsage.SNMP_SERVER_COMMUNITY_ACL,
        CiscoXrStructureUsage.SSH_ACL,
        CiscoXrStructureUsage.WCCP_GROUP_LIST,
        CiscoXrStructureUsage.WCCP_REDIRECT_LIST,
        CiscoXrStructureUsage.WCCP_SERVICE_LIST);

    markConcreteStructure(CiscoXrStructureType.PREFIX_LIST);
    markConcreteStructure(CiscoXrStructureType.PREFIX6_LIST);

    markConcreteStructure(CiscoXrStructureType.PREFIX_SET);

    markConcreteStructure(CiscoXrStructureType.ROUTE_POLICY);

    // L2tp
    markConcreteStructure(CiscoXrStructureType.L2TP_CLASS);

    // Crypto, Isakmp, and IPSec
    markConcreteStructure(CiscoXrStructureType.CRYPTO_DYNAMIC_MAP_SET);
    markConcreteStructure(CiscoXrStructureType.ISAKMP_PROFILE);
    markConcreteStructure(CiscoXrStructureType.ISAKMP_POLICY);
    markConcreteStructure(CiscoXrStructureType.IPSEC_PROFILE);
    markConcreteStructure(CiscoXrStructureType.IPSEC_TRANSFORM_SET);
    markConcreteStructure(CiscoXrStructureType.KEYRING);
    markConcreteStructure(CiscoXrStructureType.NAMED_RSA_PUB_KEY);

    // class-map
    markConcreteStructure(CiscoXrStructureType.INSPECT_CLASS_MAP);
    markConcreteStructure(CiscoXrStructureType.CLASS_MAP);

    // policy-map
    markConcreteStructure(CiscoXrStructureType.INSPECT_POLICY_MAP);
    markConcreteStructure(CiscoXrStructureType.POLICY_MAP);

    // object-group
    markConcreteStructure(CiscoXrStructureType.ICMP_TYPE_OBJECT_GROUP);
    markConcreteStructure(CiscoXrStructureType.NETWORK_OBJECT_GROUP);
    markConcreteStructure(CiscoXrStructureType.PROTOCOL_OBJECT_GROUP);
    markConcreteStructure(CiscoXrStructureType.SERVICE_OBJECT_GROUP);
    markAbstractStructure(
        CiscoXrStructureType.PROTOCOL_OR_SERVICE_OBJECT_GROUP,
        CiscoXrStructureUsage.EXTENDED_ACCESS_LIST_PROTOCOL_OR_SERVICE_OBJECT_GROUP,
        ImmutableList.of(
            CiscoXrStructureType.PROTOCOL_OBJECT_GROUP, CiscoXrStructureType.SERVICE_OBJECT_GROUP));

    // objects
    markConcreteStructure(CiscoXrStructureType.ICMP_TYPE_OBJECT);
    markConcreteStructure(CiscoXrStructureType.NETWORK_OBJECT);
    markConcreteStructure(CiscoXrStructureType.SERVICE_OBJECT);
    markConcreteStructure(CiscoXrStructureType.PROTOCOL_OBJECT);

    // service template
    markConcreteStructure(CiscoXrStructureType.SERVICE_TEMPLATE);

    // track
    markConcreteStructure(CiscoXrStructureType.TRACK);

    markConcreteStructure(CiscoXrStructureType.AS_PATH_SET);

    // BGP inheritance. This is complicated, as there are many similar-but-overlapping concepts
    markConcreteStructure(CiscoXrStructureType.BGP_AF_GROUP);
    markConcreteStructure(CiscoXrStructureType.BGP_NEIGHBOR_GROUP);
    markConcreteStructure(CiscoXrStructureType.BGP_PEER_GROUP);
    markConcreteStructure(CiscoXrStructureType.BGP_SESSION_GROUP);
    markConcreteStructure(CiscoXrStructureType.BGP_TEMPLATE_PEER_POLICY);
    markConcreteStructure(CiscoXrStructureType.BGP_TEMPLATE_PEER_SESSION);

    return ImmutableList.of(c);
  }

  private void convertCommunitySets(Configuration c) {
    _communitySets.forEach(
        (name, communitySet) -> {
          c.getCommunitySetMatchExprs()
              .put(
                  computeCommunitySetMatchAnyName(name),
                  convertMatchesAnyToCommunitySetMatchExpr(communitySet, c));
          c.getCommunitySetMatchExprs()
              .put(
                  computeCommunitySetMatchEveryName(name),
                  convertMatchesEveryToCommunitySetMatchExpr(communitySet, c));
          c.getCommunityMatchExprs().put(name, toCommunityMatchExpr(communitySet, c));
          c.getCommunitySetExprs().put(name, toCommunitySetExpr(communitySet, c));
        });
  }

  private void convertExtcommunitySetRts(Configuration c) {
    _extcommunitySetRts.forEach(
        (name, extcommunitySetRt) -> {
          String qualifiedName = computeExtcommunitySetRtName(name);
          c.getCommunitySetMatchExprs()
              .put(
                  computeCommunitySetMatchAnyName(qualifiedName),
                  convertMatchesAnyToCommunitySetMatchExpr(extcommunitySetRt, c));
          c.getCommunitySetMatchExprs()
              .put(
                  computeCommunitySetMatchEveryName(qualifiedName),
                  convertMatchesEveryToCommunitySetMatchExpr(extcommunitySetRt, c));
          c.getCommunityMatchExprs().put(qualifiedName, toCommunityMatchExpr(extcommunitySetRt, c));
          c.getCommunitySetExprs().put(qualifiedName, toCommunitySetExpr(extcommunitySetRt, c));
        });
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
              matchClassMap = new AndMatchExpr(matchConditions);
              break;
            case MATCH_ANY:
              matchClassMap = new OrMatchExpr(matchConditions);
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
              .setSourceType(CiscoXrStructureType.INSPECT_CLASS_MAP.getDescription())
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
              .setSourceType(CiscoXrStructureType.INSPECT_POLICY_MAP.getDescription())
              .build();
        });
  }

  public static String computeInspectPolicyMapAclName(@Nonnull String inspectPolicyMapName) {
    return String.format("~INSPECT_POLICY_MAP_ACL~%s~", inspectPolicyMapName);
  }

  public static String computeInspectClassMapAclName(@Nonnull String inspectClassMapName) {
    return String.format("~INSPECT_CLASS_MAP_ACL~%s~", inspectClassMapName);
  }

  private boolean isAclUsedForRouting(@Nonnull String aclName) {
    for (Vrf vrf : _vrfs.values()) {
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
      }
      // check bgp policies
      BgpProcess bgpProcess = vrf.getBgpProcess();
      if (bgpProcess != null) {
        for (BgpPeerGroup pg : bgpProcess.getAllPeerGroups()) {
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
              eigrpProcess -> eigrpProcess.getOutboundInterfaceDistributeLists().values().stream())
          .anyMatch(distributeList -> distributeList.getFilterName().equals(aclName))) {
        return true;
      }
    }
    return false;
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

  public Map<String, TrackMethod> getTrackingGroups() {
    return _trackingGroups;
  }

  public Map<String, XrCommunitySet> getCommunitySets() {
    return _communitySets;
  }
}
