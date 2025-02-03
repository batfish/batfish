package org.batfish.representation.cisco_xr;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.batfish.datamodel.Interface.UNSET_LOCAL_INTERFACE;
import static org.batfish.datamodel.Interface.computeInterfaceType;
import static org.batfish.datamodel.Interface.isRealInterfaceName;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.EXACT_PATH;
import static org.batfish.datamodel.MultipathEquivalentAsPathMatchMode.PATH_LENGTH;
import static org.batfish.datamodel.Names.generatedBgpRedistributionPolicyName;
import static org.batfish.datamodel.Names.generatedOspfDefaultRouteGenerationPolicyName;
import static org.batfish.datamodel.Names.generatedOspfExportPolicyName;
import static org.batfish.datamodel.bgp.AllowRemoteAsOutMode.ALWAYS;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.LOWEST_NEXT_HOP_IP;
import static org.batfish.datamodel.routing_policy.Common.DEFAULT_UNDERSCORE_REPLACEMENT;
import static org.batfish.datamodel.routing_policy.Common.matchDefaultRoute;
import static org.batfish.datamodel.routing_policy.Common.suppressSummarizedPrefixes;
import static org.batfish.datamodel.routing_policy.statement.Statements.ExitAccept;
import static org.batfish.datamodel.tracking.TrackMethods.interfaceInactive;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.clearFalseStatementsAndAddMatchOwnAsn;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.computeDedupedAsPathMatchExprName;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.computeOriginalAsPathMatchExprName;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.convertCryptoMapSet;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.convertMatchesAnyToCommunitySetMatchExpr;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.convertMatchesEveryToCommunitySetMatchExpr;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.convertVrfLeakingConfig;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.eigrpRedistributionPoliciesToStatements;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.generateBgpExportPolicy;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.generateBgpImportPolicy;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.generatedVrrpOrHsrpTrackInterfaceDownName;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.getIsakmpKeyGeneratedName;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.getOspfInboundDistributeListPolicy;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.getRsaPubKeyGeneratedName;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.resolveIsakmpProfileIfaceNames;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.resolveKeyringIfaceNames;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.resolveTunnelIfaceNames;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toAsPathMatchExpr;
import static org.batfish.representation.cisco_xr.CiscoXrConversions.toBgpAggregate;
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.primitives.Ints;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpTieBreaker;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
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
import org.batfish.datamodel.TunnelConfiguration;
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
import org.batfish.datamodel.ospf.StubType;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.tracking.DecrementPriority;
import org.batfish.datamodel.tracking.TrackAction;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.vendor_family.cisco_xr.Aaa;
import org.batfish.datamodel.vendor_family.cisco_xr.AaaAuthentication;
import org.batfish.datamodel.vendor_family.cisco_xr.AaaAuthenticationLogin;
import org.batfish.datamodel.vendor_family.cisco_xr.CiscoXrFamily;
import org.batfish.representation.cisco_xr.DistributeList.DistributeListFilterType;
import org.batfish.representation.cisco_xr.HsrpAddressFamily.Type;
import org.batfish.representation.cisco_xr.Tunnel.TunnelMode;
import org.batfish.vendor.VendorConfiguration;

public final class CiscoXrConfiguration extends VendorConfiguration {

  /** Matches anything but the IPv4 default route. */
  static final Not NOT_DEFAULT_ROUTE = new Not(matchDefaultRoute());

  public static final int DEFAULT_EBGP_ADMIN = 20;
  public static final int DEFAULT_IBGP_ADMIN = 200;
  public static final int DEFAULT_LOCAL_ADMIN = 200;
  // Locally generated BGP routes start with this weight.
  public static final int DEFAULT_LOCAL_BGP_WEIGHT = 32768;

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
  static final int DEFAULT_HSRP_PRIORITY = 100;
  @VisibleForTesting public static final int DEFAULT_HSRP_PRIORITY_DECREMENT = 10;
  static final boolean DEFAULT_HSRP_PREEMPT = false;

  public static final String MANAGEMENT_VRF_NAME = "management";

  // https://www.cisco.com/c/en/us/td/docs/routers/xr12000/software/xr12k_r4-0/routing/configuration/guide/rc40xr12k_chapter6.html
  // An administrative distance is an integer from 0 to 255. In general, the higher the value, the
  // lower the trust rating. An administrative distance of 255 means the routing information source
  // cannot be trusted at all and should be ignored.
  public static final int MAX_ADMINISTRATIVE_COST = 255;

  public static final String MANAGEMENT_INTERFACE_PREFIX = "mgmt";

  /** Name of the generated static route resolution policy, implementing XR resolution filtering */
  public static final String RESOLUTION_POLICY_NAME = "~RESOLUTION_POLICY~";

  private static final int VLAN_NORMAL_MAX_CISCO = 1005;

  private static final int VLAN_NORMAL_MIN_CISCO = 2;

  public static @Nonnull String computeCommunitySetMatchAnyName(String name) {
    return String.format("~MATCH_ANY~%s~", name);
  }

  public static @Nonnull String computeCommunitySetMatchEveryName(String name) {
    return String.format("~MATCH_EVERY~%s~", name);
  }

  public static @Nonnull String computeExtcommunitySetRtName(String name) {
    return String.format("RT-%s", name);
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

  public static String computeServiceObjectAclName(String name) {
    return String.format("~SERVICE_OBJECT~%s~", name);
  }

  public static String computeAbfIpv4PolicyName(String name) {
    return String.format("~ABF_POLICY_IPV4~%s~", name);
  }

  public static final Pattern INTERFACE_PREFIX_PATTERN = Pattern.compile("^[-A-Za-z]+");

  @Override
  public String canonicalizeInterfaceName(String ifaceName) {
    Matcher matcher = INTERFACE_PREFIX_PATTERN.matcher(ifaceName);
    if (matcher.find()) {
      String ifacePrefix = matcher.group();
      String canonicalPrefix = getCanonicalInterfaceNamePrefix(ifacePrefix);
      // remove optional space between prefix and number(s)
      String suffix = ifaceName.substring(ifacePrefix.length()).trim();
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
    String output = withoutQuotes.replaceAll("_", DEFAULT_UNDERSCORE_REPLACEMENT);
    return output;
  }

  private final Map<String, AsPathSet> _asPathSets;

  private final Map<String, BridgeGroup> _bridgeGroups;

  private final CiscoXrFamily _cf;

  private final Map<String, CryptoMapSet> _cryptoMapSets;

  private final Map<String, NamedRsaPubKey> _cryptoNamedRsaPubKeys;

  private final List<Ip> _dhcpRelayServers;

  private final NavigableSet<String> _dnsServers;

  private String _dnsSourceInterface;

  private String _domainName;

  private final @Nonnull Map<String, ExtcommunitySetRt> _extcommunitySetRts;

  private String _hostname;
  private String _rawHostname;

  private @Nullable Hsrp _hsrp;

  private final Map<String, Interface> _interfaces;

  private final Map<String, IpsecProfile> _ipsecProfiles;

  private final Map<String, IpsecTransformSet> _ipsecTransformSets;

  private final Map<String, Ipv4AccessList> _ipv4Acls;

  private final Map<String, Ipv6AccessList> _ipv6Acls;

  private final List<IsakmpKey> _isakmpKeys;

  private final Map<Integer, IsakmpPolicy> _isakmpPolicies;

  private final Map<String, IsakmpProfile> _isakmpProfiles;

  private final Map<String, Keyring> _keyrings;

  private final Map<String, NetworkObjectGroup> _networkObjectGroups;

  private String _ntpSourceInterface;

  private final Map<String, ObjectGroup> _objectGroups;

  private final Map<String, Prefix6List> _prefix6Lists;

  private final Map<String, PrefixList> _prefixLists;

  private final Map<String, RoutePolicy> _routePolicies;

  private SnmpServer _snmpServer;

  private String _snmpSourceInterface;

  private boolean _spanningTreePortfastDefault;

  private final NavigableSet<String> _tacacsServers;

  private String _tacacsSourceInterface;

  private ConfigurationFormat _vendor;

  private final Map<String, Vrf> _vrfs;

  private final SortedMap<String, VrrpInterface> _vrrpGroups;

  private final Map<String, TrackMethod> _trackingGroups;

  private final Map<String, XrCommunitySet> _communitySets;

  private final @Nonnull Map<String, RdSet> _rdSets;

  public CiscoXrConfiguration() {
    _asPathSets = new TreeMap<>();
    _bridgeGroups = new TreeMap<>();
    _cf = new CiscoXrFamily();
    _communitySets = new TreeMap<>();
    _cryptoNamedRsaPubKeys = new TreeMap<>();
    _cryptoMapSets = new HashMap<>();
    _dhcpRelayServers = new ArrayList<>();
    _dnsServers = new TreeSet<>();
    _ipv4Acls = new TreeMap<>();
    _ipv6Acls = new TreeMap<>();
    _extcommunitySetRts = new HashMap<>();
    _isakmpKeys = new ArrayList<>();
    _isakmpPolicies = new TreeMap<>();
    _isakmpProfiles = new TreeMap<>();
    _interfaces = new TreeMap<>();
    _ipsecTransformSets = new TreeMap<>();
    _ipsecProfiles = new TreeMap<>();
    _keyrings = new TreeMap<>();
    _networkObjectGroups = new TreeMap<>();
    _objectGroups = new TreeMap<>();
    _prefixLists = new TreeMap<>();
    _prefix6Lists = new TreeMap<>();
    _rdSets = new HashMap<>();
    _routePolicies = new TreeMap<>();
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

  public Map<String, AsPathSet> getAsPathSets() {
    return _asPathSets;
  }

  public Map<String, BridgeGroup> getBridgeGroups() {
    return _bridgeGroups;
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

  @Override
  public String getHostname() {
    return _hostname;
  }

  public @Nullable Hsrp getHsrp() {
    return _hsrp;
  }

  public @Nonnull Hsrp getOrCreateHsrp() {
    if (_hsrp == null) {
      _hsrp = new Hsrp();
    }
    return _hsrp;
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

  public ConfigurationFormat getVendor() {
    return _vendor;
  }

  public Map<String, Vrf> getVrfs() {
    return _vrfs;
  }

  public SortedMap<String, VrrpInterface> getVrrpGroups() {
    return _vrrpGroups;
  }

  public void setDnsSourceInterface(String dnsSourceInterface) {
    _dnsSourceInterface = dnsSourceInterface;
  }

  public void setDomainName(String domainName) {
    _domainName = domainName;
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

  private static @Nonnull org.batfish.datamodel.BgpProcess.Builder bgpProcessBuilder() {
    return org.batfish.datamodel.BgpProcess.builder()
        .setEbgpAdminCost(DEFAULT_EBGP_ADMIN)
        .setIbgpAdminCost(DEFAULT_IBGP_ADMIN)
        .setLocalAdminCost(DEFAULT_LOCAL_ADMIN)
        .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(LOWEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(LOWEST_NEXT_HOP_IP);
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

    int defaultMetric = proc.getDefaultMetric();

    // Populate process-level BGP aggregates
    proc.getAggregateNetworks().values().stream()
        .map(ipv4Aggregate -> toBgpAggregate(ipv4Aggregate, c, _w))
        .forEach(newBgpProcess::addAggregate);

    /*
     * Create common bgp export policy. This policy's only function is to prevent export of
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

    // Create BGP redistribution policy
    String redistPolicyName = generatedBgpRedistributionPolicyName(vrfName);
    RoutingPolicy.Builder redistributionPolicy =
        RoutingPolicy.builder().setOwner(c).setName(redistPolicyName);

    // For IOS-XR, local routes have a default weight of 32768.
    redistributionPolicy.addStatement(new SetWeight(new LiteralInt(DEFAULT_LOCAL_BGP_WEIGHT)));

    Set<String> convertedRoutePolicyNames = c.getRoutingPolicies().keySet();

    // Export RIP routes that should be redistributed.
    BgpRedistributionPolicy redistributeRipPolicy =
        proc.getRedistributionPolicies().get(RoutingProtocol.RIP);
    if (redistributeRipPolicy != null) {
      Conjunction exportRipConditions = new Conjunction();
      exportRipConditions.setComment("Redistribute RIP routes into BGP");
      exportRipConditions.getConjuncts().add(new MatchProtocol(RoutingProtocol.RIP));
      String mapName = redistributeRipPolicy.getRouteMap();
      if (mapName != null) {
        // TODO update to route-policy if valid, or delete grammar and VS
      }
      redistributionPolicy.addStatement(
          new If(exportRipConditions, ImmutableList.of(ExitAccept.toStaticStatement())));
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
        if (convertedRoutePolicyNames.contains(mapName)) {
          exportStaticConditions.getConjuncts().add(new CallExpr(mapName));
        } else {
          _w.redFlag(
              String.format(
                  "Ignoring undefined route-policy %s in static -> BGP redistribution", mapName));
        }
      }
      redistributionPolicy.addStatement(
          new If(exportStaticConditions, ImmutableList.of(ExitAccept.toStaticStatement())));
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
        if (convertedRoutePolicyNames.contains(mapName)) {
          exportConnectedConditions.getConjuncts().add(new CallExpr(mapName));
        } else {
          _w.redFlag(
              String.format(
                  "Ignoring undefined route-policy %s in connected -> BGP redistribution",
                  mapName));
        }
      }
      redistributionPolicy.addStatement(
          new If(exportConnectedConditions, ImmutableList.of(ExitAccept.toStaticStatement())));
    }

    // Export OSPF routes that should be redistributed.
    BgpRedistributionPolicy redistributeOspfPolicy =
        proc.getRedistributionPolicies().get(RoutingProtocol.OSPF);
    if (redistributeOspfPolicy != null) {
      Conjunction exportOspfConditions = new Conjunction();
      exportOspfConditions.setComment("Redistribute OSPF routes into BGP");
      exportOspfConditions.getConjuncts().add(new MatchProtocol(RoutingProtocol.OSPF));
      String mapName = redistributeOspfPolicy.getRouteMap();
      if (mapName != null) {
        // TODO update to route-policy if valid, or delete grammar and VS
      }
      redistributionPolicy.addStatement(
          new If(exportOspfConditions, ImmutableList.of(ExitAccept.toStaticStatement())));
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
              if (mapName != null) {
                // TODO update to route-policy if valid, or delete grammar and VS
              }
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
              redistributionPolicy.addStatement(
                  new If(
                      "Add network statement routes to BGP",
                      exportNetworkConditions,
                      ImmutableList.of(
                          new SetOrigin(new LiteralOrigin(OriginType.IGP, null)),
                          ExitAccept.toStaticStatement())));
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

      // Get default-originate generation or export policy
      String defaultOriginateGenerationMap = null;
      if (lpg.getDefaultOriginate()) {
        defaultOriginateGenerationMap = lpg.getDefaultOriginateMap();
      }

      // Generate import and export policies
      long localAs = firstNonNull(lpg.getLocalAs(), proc.getProcnum());
      String peerImportPolicyName = generateBgpImportPolicy(lpg, localAs, vrfName, c);
      generateBgpExportPolicy(lpg, localAs, vrfName, c);

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

      AddressFamilyCapabilities ipv4AfSettings =
          AddressFamilyCapabilities.builder()
              .setAdditionalPathsReceive(lpg.getAdditionalPathsReceive())
              .setAdditionalPathsSelectAll(lpg.getAdditionalPathsSelectAll())
              .setAdditionalPathsSend(lpg.getAdditionalPathsSend())
              .setAllowLocalAsIn(lpg.getAllowAsIn())
              .setAllowRemoteAsOut(ALWAYS) // TODO: support 'as-path-loopcheck out disable'
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

  private org.batfish.datamodel.hsrp.HsrpGroup toHsrpGroup(
      HsrpGroup group, @Nullable ConcreteInterfaceAddress sourceAddress, Configuration c) {
    org.batfish.datamodel.hsrp.HsrpGroup.Builder ret =
        org.batfish.datamodel.hsrp.HsrpGroup.builder();
    ret.setPreempt(firstNonNull(group.getPreempt(), DEFAULT_HSRP_PREEMPT));
    ret.setPriority(firstNonNull(group.getPriority(), DEFAULT_HSRP_PRIORITY));
    ret.setSourceAddress(sourceAddress);
    if (group.getAddress() != null) {
      ret.setVirtualAddresses(ImmutableSet.of(group.getAddress()));
    }
    ImmutableSortedMap.Builder<String, TrackAction> trackActionsBuilder =
        ImmutableSortedMap.naturalOrder();
    group
        .getInterfaceTracks()
        .forEach(
            (ifaceName, ifaceTrack) -> {
              if (!_interfaces.containsKey(ifaceName)) {
                return;
              }
              int decrement =
                  firstNonNull(ifaceTrack.getDecrementPriority(), DEFAULT_HSRP_PRIORITY_DECREMENT);
              String trackMethodName = generateVrrpOrHsrpTrackInterfaceDownIfNeeded(ifaceName, c);
              trackActionsBuilder.put(trackMethodName, new DecrementPriority(decrement));
            });
    ret.setTrackActions(trackActionsBuilder.build());
    // TODO: auth, timers
    return ret.build();
  }

  private @Nonnull String generateVrrpOrHsrpTrackInterfaceDownIfNeeded(
      String ifaceName, Configuration c) {
    String name = generatedVrrpOrHsrpTrackInterfaceDownName(ifaceName);
    c.getTrackingGroups().computeIfAbsent(name, n -> interfaceInactive(ifaceName));
    return name;
  }

  private Map<Integer, org.batfish.datamodel.hsrp.HsrpGroup> toHsrpGroups(
      String ifaceName, @Nullable ConcreteInterfaceAddress sourceAddress, Configuration c) {
    if (_hsrp == null) {
      return ImmutableMap.of();
    }
    HsrpInterface hsrpIface = _hsrp.getInterface(ifaceName);
    if (hsrpIface == null) {
      return ImmutableMap.of();
    }

    HsrpAddressFamily v4 = hsrpIface.getAddressFamily(Type.IPV4);
    if (v4 == null) {
      return ImmutableMap.of();
    }

    return v4.getGroups().values().stream()
        .map(
            group ->
                new SimpleImmutableEntry<>(group.getNumber(), toHsrpGroup(group, sourceAddress, c)))
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
  }

  private org.batfish.datamodel.Interface toInterface(
      String ifaceName, Interface iface, Map<String, IpAccessList> ipAccessLists, Configuration c) {
    org.batfish.datamodel.Interface newIface =
        org.batfish.datamodel.Interface.builder()
            .setName(ifaceName)
            .setOwner(c)
            .setType(computeInterfaceType(iface.getName(), c.getConfigurationFormat()))
            .build();
    String vrfName = iface.getVrf();
    Vrf vrf = _vrfs.computeIfAbsent(vrfName, Vrf::new);
    newIface.setDescription(iface.getDescription());
    if (!iface.getActive()) {
      newIface.adminDown();
    }
    if (iface.getBundleId() != null) {
      newIface.setChannelGroup(String.format("Bundle-Ether%d", iface.getBundleId()));
    }
    newIface.setCryptoMap(iface.getCryptoMap());
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
    newIface.setDeclaredNames(ImmutableSortedSet.copyOf(iface.getDeclaredNames()));
    newIface.setSwitchport(iface.getSwitchport());

    if (iface.getSwitchport()) {
      newIface.setSwitchportMode(iface.getSwitchportMode());

      // switch settings
      if (iface.getSwitchportMode() == SwitchportMode.ACCESS) {
        newIface.setAccessVlan(iface.getAccessVlan());
      }

      if (iface.getSwitchportMode() == SwitchportMode.TRUNK) {
        SwitchportEncapsulationType encapsulation =
            firstNonNull(
                // TODO: check if this is OK
                iface.getSwitchportTrunkEncapsulation(), SwitchportEncapsulationType.DOT1Q);
        newIface.setSwitchportTrunkEncapsulation(encapsulation);

        // If allowed VLANs are set, honor them;
        if (iface.getAllowedVlans() != null) {
          newIface.setAllowedVlans(iface.getAllowedVlans());
        } else {
          newIface.setAllowedVlans(Interface.ALL_VLANS);
        }
        newIface.setNativeVlan(firstNonNull(iface.getNativeVlan(), 1));
      }

      newIface.setSpanningTreePortfast(iface.getSpanningTreePortfast());
    } else {
      newIface.setSwitchportMode(SwitchportMode.NONE);
      if (newIface.getInterfaceType() == InterfaceType.VLAN) {
        Integer vlan = Ints.tryParse(ifaceName.substring("vlan".length()));
        newIface.setVlan(vlan);
        if (vlan == null) {
          _w.redFlag("Unable assign vlan for interface " + ifaceName);
        }
        newIface.setAutoState(iface.getAutoState());
      }

      // All prefixes is the combination of the interface prefix + any secondary prefixes.
      ImmutableSet.Builder<InterfaceAddress> allPrefixes = ImmutableSet.builder();
      if (iface.getAddress() != null) {
        newIface.setAddress(iface.getAddress());
        allPrefixes.add(iface.getAddress());
      }
      allPrefixes.addAll(iface.getSecondaryAddresses());
      newIface.setAllAddresses(allPrefixes.build());

      // subinterface settings
      newIface.setEncapsulationVlan(iface.getEncapsulationVlan());

      // HSRP source address is primary address.
      newIface.setHsrpGroups(toHsrpGroups(ifaceName, iface.getAddress(), c));
      // todo: HSRP version
    }

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

      List<Statement> redistributeAndAllowEigrpFromSelfAsn =
          clearFalseStatementsAndAddMatchOwnAsn(
              redistributePolicyStatements, eigrpProcess.getAsn());

      RoutingPolicy routingPolicy =
          RoutingPolicy.builder()
              .setOwner(c)
              .setName(
                  String.format(
                      "~EIGRP_EXPORT_POLICY_%s_%s_%s", vrfName, eigrpProcess.getAsn(), ifaceName))
              .setStatements(redistributeAndAllowEigrpFromSelfAsn)
              .build();

      newIface.setEigrp(
          EigrpInterfaceSettings.builder()
              .setAsn(eigrpProcess.getAsn())
              .setEnabled(true)
              .setExportPolicy(routingPolicy.getName())
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
        case LEVEL_1 -> level1 = true;
        case LEVEL_2 -> level2 = true;
        case LEVEL_1_2 -> {
          level1 = true;
          level2 = true;
        }
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

    String incomingFilterName = iface.getIncomingFilter();
    if (incomingFilterName != null) {
      Ipv4AccessList incomingFilter = _ipv4Acls.get(incomingFilterName);
      if (incomingFilter != null) {
        if (isIpv4AclUsedForAbf(incomingFilter)) {
          newIface.setPacketPolicy(computeAbfIpv4PolicyName(incomingFilterName));
        } else {
          newIface.setIncomingFilter(ipAccessLists.get(incomingFilterName));
        }
      }
    }
    String outgoingFilterName = iface.getOutgoingFilter();
    if (outgoingFilterName != null) {
      Ipv4AccessList outgoingFilter = _ipv4Acls.get(outgoingFilterName);
      if (outgoingFilter != null && isIpv4AclUsedForAbf(outgoingFilter)) {
        _w.redFlag(
            String.format(
                "ACL based forwarding rule %s cannot be applied to an egress interface.",
                outgoingFilterName));
      } else {
        newIface.setOutgoingFilter(ipAccessLists.get(outgoingFilterName));
      }
    }

    return newIface;
  }

  private @Nonnull EigrpMetric computeEigrpMetricForInterface(
      Interface iface, EigrpProcessMode mode) {
    Optional<Double> bw =
        Stream.of(iface.getBandwidth(), Interface.getDefaultBandwidth(iface.getName(), _vendor))
            .filter(Objects::nonNull)
            .findFirst();
    if (!bw.isPresent()) {
      _w.redFlagf("Missing bandwidth for %s, EIGRP metric will be wrong", iface.getName());
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

  private If convertOspfRedistributionPolicy(
      OspfRedistributionPolicy policy, OspfProcess proc, Set<String> convertedRoutePolicyNames) {
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

    // If a route-policy is present, honor it.
    String exportRouteMapName = policy.getRouteMap();
    if (exportRouteMapName != null) {
      if (convertedRoutePolicyNames.contains(exportRouteMapName)) {
        ospfExportConditions.getConjuncts().add(new CallExpr(policy.getRouteMap()));
      } else {
        // Undefined route-policy. This is only possible in a manually edited config; CLI rejects
        // references to undefined route-policies and removal of route-policies that are in use.
        _w.redFlag(
            String.format(
                "Ignoring undefined route-policy %s in OSPF redistribution", exportRouteMapName));
      }
    }

    ospfExportStatements.add(ExitAccept.toStaticStatement());

    // Construct the policy and add it before returning.
    return new If(
        "OSPF export routes for " + protocol.protocolName(),
        ospfExportConditions,
        ospfExportStatements.build(),
        ImmutableList.of());
  }

  /**
   * Copies configured {@link OspfSettings} from each {@link OspfProcess} into its member {@link
   * org.batfish.representation.cisco_xr.OspfArea areas}, and from each area into its member {@link
   * OspfInterfaceSettings interfaces}.
   *
   * @see OspfSettings#inheritFrom(OspfSettings)
   */
  private static void inheritOspfSettings(OspfProcess proc) {
    OspfSettings procSettings = proc.getOspfSettings();
    for (org.batfish.representation.cisco_xr.OspfArea area : proc.getAreas().values()) {
      OspfSettings areaSettings = area.getOspfSettings();
      areaSettings.inheritFrom(procSettings);
      for (OspfInterfaceSettings iface : area.getInterfaceSettings().values()) {
        iface.getOspfSettings().inheritFrom(areaSettings);
      }
    }
  }

  private org.batfish.datamodel.ospf.OspfProcess toOspfProcess(
      OspfProcess proc, String vrfName, Configuration c, CiscoXrConfiguration oldConfig) {
    Ip routerId = proc.getRouterId();
    if (routerId == null) {
      routerId = CiscoXrConversions.getOspfRouterId(proc, vrfName, oldConfig.getInterfaces());
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

    // establish areas
    ImmutableMap.Builder<Long, OspfArea> areas = ImmutableMap.builder();
    for (org.batfish.representation.cisco_xr.OspfArea area : proc.getAreas().values()) {
      long areaNum = area.getAreaNum();
      OspfArea.Builder viAreaBuilder = OspfArea.builder().setNumber(areaNum);

      // Fill in OSPF settings for interfaces in this area
      ImmutableSortedSet.Builder<String> areaInterfacesBuilder = ImmutableSortedSet.naturalOrder();
      for (Entry<String, OspfInterfaceSettings> e : area.getInterfaceSettings().entrySet()) {
        String ifaceName = e.getKey();
        org.batfish.datamodel.Interface iface = c.getAllInterfaces().get(ifaceName);
        if (iface == null) {
          // No need to file warning, there will be an undefined reference to the interface
          continue;
        }
        areaInterfacesBuilder.add(ifaceName);
        finalizeInterfaceOspfSettings(iface, proc, areaNum, e.getValue(), vrfName, c);
      }
      viAreaBuilder.setInterfaces(areaInterfacesBuilder.build());

      // Process stub type settings
      if (area.getNssaSettings() != null) {
        viAreaBuilder.setStubType(StubType.NSSA);
        viAreaBuilder.setNssaSettings(toNssaSettings(area.getNssaSettings()));
      } else if (area.getStubSettings() != null) {
        viAreaBuilder.setStubType(StubType.STUB);
        viAreaBuilder.setStubSettings(toStubSettings(area.getStubSettings()));
      }

      // Populate VI area summaries and create summary filter
      if (!area.getSummaries().isEmpty()) {
        String summaryFilterName = "~OSPF_SUMMARY_FILTER:" + vrfName + ":" + areaNum + "~";
        RouteFilterList summaryFilter = new RouteFilterList(summaryFilterName);
        c.getRouteFilterLists().put(summaryFilterName, summaryFilter);
        viAreaBuilder.setSummaryFilter(summaryFilterName);
        for (Entry<Prefix, OspfAreaSummary> e : area.getSummaries().entrySet()) {
          Prefix prefix = e.getKey();
          OspfAreaSummary summary = e.getValue();
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
        viAreaBuilder.addSummaries(ImmutableSortedMap.copyOf(area.getSummaries()));
        summaryFilter.addLine(
            new RouteFilterLine(
                LineAction.PERMIT,
                IpWildcard.create(Prefix.ZERO),
                new SubRange(0, Prefix.MAX_PREFIX_LENGTH)));
      }

      areas.put(areaNum, viAreaBuilder.build());
    }
    newProcess.setAreas(areas.build());

    String ospfExportPolicyName = generatedOspfExportPolicyName(vrfName, proc.getName());
    RoutingPolicy ospfExportPolicy = new RoutingPolicy(ospfExportPolicyName, c);
    c.getRoutingPolicies().put(ospfExportPolicyName, ospfExportPolicy);
    List<Statement> ospfExportStatements = ospfExportPolicy.getStatements();
    newProcess.setExportPolicy(ospfExportPolicyName);

    // policy map for default information
    if (proc.getDefaultInformationOriginate() != null) {
      OspfDefaultInformationOriginate defaultInformationOriginate =
          proc.getDefaultInformationOriginate();
      // Add an export statement to export the default route with correct metric and metric type
      SetMetric setMetric = new SetMetric(new LiteralLong(defaultInformationOriginate.getMetric()));
      SetOspfMetricType setMetricType =
          new SetOspfMetricType(defaultInformationOriginate.getMetricType());
      If ospfExportDefault =
          new If(
              new Conjunction(
                  ImmutableList.of(
                      matchDefaultRoute(), new MatchProtocol(RoutingProtocol.AGGREGATE))),
              ImmutableList.of(setMetric, setMetricType, ExitAccept.toStaticStatement()));
      ospfExportDefault.setComment("OSPF export default route");
      ospfExportStatements.add(ospfExportDefault);
      // Give the process a generated default route to export
      GeneratedRoute.Builder route =
          GeneratedRoute.builder()
              .setNetwork(Prefix.ZERO)
              .setNonRouting(true)
              .setAdmin(MAX_ADMINISTRATIVE_COST);
      if (!defaultInformationOriginate.getAlways()) {
        // Route should only be generated if a default route exists in RIB
        String defaultRouteGenerationPolicyName =
            generatedOspfDefaultRouteGenerationPolicyName(vrfName, proc.getName());
        RoutingPolicy.builder()
            .setOwner(c)
            .setName(defaultRouteGenerationPolicyName)
            .addStatement(
                new If(
                    matchDefaultRoute(),
                    ImmutableList.of(Statements.ReturnTrue.toStaticStatement())))
            .build();
        route.setGenerationPolicy(defaultRouteGenerationPolicyName);
      }
      newProcess.addGeneratedRoute(route.build());
    }

    // TODO: distribute lists

    // policies for redistributing routes
    ospfExportStatements.addAll(
        proc.getRedistributionPolicies().values().stream()
            .map(
                policy ->
                    convertOspfRedistributionPolicy(policy, proc, c.getRoutingPolicies().keySet()))
            .collect(Collectors.toList()));

    return newProcess;
  }

  /** Setup OSPF settings on specified VI interface. */
  private void finalizeInterfaceOspfSettings(
      org.batfish.datamodel.Interface iface,
      OspfProcess proc,
      long areaNum,
      OspfInterfaceSettings vsIfaceSettings,
      String vrfName,
      Configuration c) {
    org.batfish.datamodel.ospf.OspfInterfaceSettings.Builder ospfSettings =
        org.batfish.datamodel.ospf.OspfInterfaceSettings.builder().setPassive(false);
    ospfSettings.setProcess(proc.getName());
    if (firstNonNull(vsIfaceSettings.getOspfSettings().getPassive(), false)) {
      ospfSettings.setPassive(true);
    }

    // Interface settings already inherited from area and process settings, so we don't need to
    // check beyond interface settings for distribute lists, network type, etc.
    DistributeList distListIn = vsIfaceSettings.getOspfSettings().getDistributeListIn();
    Optional.ofNullable(
            getOspfInboundDistributeListPolicy(
                distListIn, vrfName, proc.getName(), areaNum, iface.getName(), c, _w))
        .ifPresent(ospfSettings::setInboundDistributeListPolicy);

    ospfSettings.setAreaName(areaNum);

    org.batfish.representation.cisco_xr.OspfNetworkType vsNetworkType =
        vsIfaceSettings.getOspfSettings().getNetworkType();
    org.batfish.datamodel.ospf.OspfNetworkType networkType = toOspfNetworkType(vsNetworkType, _w);

    ospfSettings.setNetworkType(networkType);
    if (vsIfaceSettings.getOspfSettings().getCost() == null && iface.isLoopback()) {
      ospfSettings.setCost(DEFAULT_LOOPBACK_OSPF_COST);
    } else {
      ospfSettings.setCost(vsIfaceSettings.getOspfSettings().getCost());
    }
    ospfSettings.setHelloInterval(
        toOspfHelloInterval(vsIfaceSettings.getOspfSettings(), networkType));
    ospfSettings.setDeadInterval(
        toOspfDeadInterval(vsIfaceSettings.getOspfSettings(), networkType));

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
      ripExportDefaultConditions.getConjuncts().add(matchDefaultRoute());
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
      ripExportDefaultStatements.add(ExitAccept.toStaticStatement());
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
      ripExportConnectedStatements.add(ExitAccept.toStaticStatement());
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
      ripExportStaticStatements.add(ExitAccept.toStaticStatement());
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
      ripExportBgpStatements.add(ExitAccept.toStaticStatement());
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
    c.setHumanName(_rawHostname);
    c.getVendorFamily().setCiscoXr(_cf);
    c.setDeviceModel(DeviceModel.CISCO_UNSPECIFIED);
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

    // Build static route resolution policy used by VRFs; prevents resolution w/ default-routes
    RoutingPolicy.builder()
        .setOwner(c)
        .setName(RESOLUTION_POLICY_NAME)
        .setStatements(
            ImmutableList.of(
                new If(
                    matchDefaultRoute(),
                    ImmutableList.of(Statements.ReturnFalse.toStaticStatement()),
                    ImmutableList.of(Statements.ReturnTrue.toStaticStatement()))))
        .build();

    // initialize vrfs
    for (String vrfName : _vrfs.keySet()) {
      c.getVrfs()
          .put(
              vrfName,
              org.batfish.datamodel.Vrf.builder()
                  .setName(vrfName)
                  .setResolutionPolicy(RESOLUTION_POLICY_NAME)
                  .build());
    }

    // snmp server
    if (_snmpServer != null) {
      String snmpServerVrf = _snmpServer.getVrf();
      c.getVrfs().get(snmpServerVrf).setSnmpServer(_snmpServer);
    }

    // convert as-path-sets to vendor independent format
    _asPathSets.forEach(
        (name, asPathSet) -> {
          c.getAsPathMatchExprs()
              .put(computeDedupedAsPathMatchExprName(name), toAsPathMatchExpr(asPathSet, true));
          c.getAsPathMatchExprs()
              .put(computeOriginalAsPathMatchExprName(name), toAsPathMatchExpr(asPathSet, false));
        });

    convertCommunitySets(c);
    convertExtcommunitySetRts(c);

    // convert prefix lists to route filter lists
    for (PrefixList prefixList : _prefixLists.values()) {
      RouteFilterList newRouteFilterList =
          CiscoXrConversions.toRouteFilterList(prefixList, _filename);
      c.getRouteFilterLists().put(newRouteFilterList.getName(), newRouteFilterList);
    }

    // inherit OSPF settings before calling getAclsUsedForRouting
    _vrfs.values().stream()
        .flatMap(vrf -> vrf.getOspfProcesses().values().stream())
        .forEach(CiscoXrConfiguration::inheritOspfSettings);

    // convert VS access lists to VI access lists, route filter, or packet policy
    Set<String> aclsUsedForRouting = getAclsUsedForRouting();
    for (Ipv4AccessList eaList : _ipv4Acls.values()) {
      if (isIpv4AclUsedForAbf(eaList)) {
        PacketPolicy packetPolicy = CiscoXrConversions.toPacketPolicy(eaList, _objectGroups, _w);
        c.getPacketPolicies().put(packetPolicy.getName(), packetPolicy);
      }

      if (aclsUsedForRouting.contains(eaList.getName())) {
        RouteFilterList rfList = CiscoXrConversions.toRouteFilterList(eaList, _filename);
        c.getRouteFilterLists().put(rfList.getName(), rfList);
      }
      IpAccessList ipaList = toIpAccessList(eaList, _objectGroups, _filename);
      c.getIpAccessLists().put(ipaList.getName(), ipaList);
    }

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
                            name,
                            CiscoXrStructureType.NETWORK_OBJECT_GROUP.getDescription(),
                            null)));

    // convert RoutePolicy to RoutingPolicy
    for (RoutePolicy routePolicy : _routePolicies.values()) {
      RoutingPolicy routingPolicy = toRoutingPolicy(c, routePolicy);
      c.getRoutingPolicies().put(routingPolicy.getName(), routingPolicy);
    }

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
     * Second pass over the interfaces to set dependency pointers correctly for Bundle-Ether
     * and tunnel interfaces
     * TODO: VLAN interfaces
     */
    _interfaces.forEach(
        (ifaceName, iface) -> {
          // Physical interface -> Bundle-Ether for its Bundle ID
          Integer bundleId = iface.getBundleId();
          if (bundleId != null) {
            String bundleName = String.format("Bundle-Ether%d", bundleId);
            org.batfish.datamodel.Interface bundleIface = c.getAllInterfaces().get(bundleName);
            if (bundleIface != null) {
              bundleIface.addDependency(new Dependency(ifaceName, DependencyType.AGGREGATE));
            }
          }
          // All subinterfaces
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

    convertVrfLeakingConfig(_vrfs.values(), c);

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

    // Define the Null0 interface if it has been referenced. Otherwise, these show as undefined
    // references.
    Optional<Integer> firstRefToNull0 =
        _structureManager
            .getStructureReferences(CiscoXrStructureType.INTERFACE)
            .getOrDefault("Null0", ImmutableSortedMap.of())
            .values()
            .stream()
            .flatMap(Collection::stream)
            .min(Integer::compare);
    if (firstRefToNull0.isPresent()) {
      defineSingleLineStructure(CiscoXrStructureType.INTERFACE, "Null0", firstRefToNull0.get());
    }

    CiscoXrStructureType.CONCRETE_STRUCTURES.forEach(this::markConcreteStructure);
    CiscoXrStructureType.ABSTRACT_STRUCTURES.asMap().forEach(this::markAbstractStructureAllUsages);

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

  /**
   * Returns names of all ACLs that must be converted to {@link RouteFilterList} because they are
   * used for routing. Currently this is limited to ACLs used as OSPF distribute-lists.
   *
   * <p>NOTE: OSPF settings should be {@link #inheritOspfSettings(OspfProcess) inherited} before
   * calling this function.
   */
  private Set<String> getAclsUsedForRouting() {
    // TODO Include OSPF global outbound distribute lists when conversion supports them
    return _vrfs.values().stream()
        .flatMap(vrf -> vrf.getOspfProcesses().values().stream())
        .flatMap(proc -> proc.getAreas().values().stream())
        .flatMap(area -> area.getInterfaceSettings().values().stream())
        .map(OspfInterfaceSettings::getOspfSettings)
        .map(OspfSettings::getDistributeListIn)
        .filter(
            distList ->
                distList != null
                    && distList.getFilterType() == DistributeListFilterType.ACCESS_LIST)
        .map(DistributeList::getFilterName)
        .collect(ImmutableSet.toImmutableSet());
  }

  /** Indicates if any line in the specified ipv4 ACL is used for ACL based forwarding. */
  @VisibleForTesting
  static boolean isIpv4AclUsedForAbf(@Nonnull Ipv4AccessList acl) {
    // ABF lines will always have nexthop1 set
    return acl.getLines().values().stream().anyMatch(l -> l.getNexthop1() != null);
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

  public Map<String, NetworkObjectGroup> getNetworkObjectGroups() {
    return _networkObjectGroups;
  }

  public Map<String, ObjectGroup> getObjectGroups() {
    return _objectGroups;
  }

  public Map<String, TrackMethod> getTrackingGroups() {
    return _trackingGroups;
  }

  public Map<String, XrCommunitySet> getCommunitySets() {
    return _communitySets;
  }

  public @Nonnull Map<String, RdSet> getRdSets() {
    return _rdSets;
  }
}
