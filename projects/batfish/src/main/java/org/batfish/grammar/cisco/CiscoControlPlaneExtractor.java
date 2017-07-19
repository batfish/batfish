package org.batfish.grammar.cisco;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.BatfishException;
import org.batfish.common.RedFlagBatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DscpType;
import org.batfish.datamodel.IcmpCode;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Ip6Wildcard;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IsisInterfaceMode;
import org.batfish.datamodel.IsisLevel;
import org.batfish.datamodel.IsisMetricType;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.OspfMetricType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.datamodel.Prefix6Range;
import org.batfish.datamodel.Prefix6Space;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SnmpCommunity;
import org.batfish.datamodel.SnmpHost;
import org.batfish.datamodel.SnmpServer;
import org.batfish.datamodel.State;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.AsPathSetElem;
import org.batfish.datamodel.routing_policy.expr.AsPathSetExpr;
import org.batfish.datamodel.routing_policy.expr.AutoAs;
import org.batfish.datamodel.routing_policy.expr.CommunitySetElem;
import org.batfish.datamodel.routing_policy.expr.CommunitySetElemHalfExpr;
import org.batfish.datamodel.routing_policy.expr.DecrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.DecrementMetric;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.ExplicitAsPathSet;
import org.batfish.datamodel.routing_policy.expr.IgpCost;
import org.batfish.datamodel.routing_policy.expr.IncrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.IncrementMetric;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.IsisLevelExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySetElemHalf;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralIsisLevel;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.LiteralRouteType;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.OriginExpr;
import org.batfish.datamodel.routing_policy.expr.RangeCommunitySetElemHalf;
import org.batfish.datamodel.routing_policy.expr.RegexAsPathSetElem;
import org.batfish.datamodel.routing_policy.expr.RouteType;
import org.batfish.datamodel.routing_policy.expr.RouteTypeExpr;
import org.batfish.datamodel.routing_policy.expr.SubRangeExpr;
import org.batfish.datamodel.routing_policy.expr.VarAs;
import org.batfish.datamodel.routing_policy.expr.VarAsPathSet;
import org.batfish.datamodel.routing_policy.expr.VarCommunitySetElemHalf;
import org.batfish.datamodel.routing_policy.expr.VarInt;
import org.batfish.datamodel.routing_policy.expr.VarIsisLevel;
import org.batfish.datamodel.routing_policy.expr.VarOrigin;
import org.batfish.datamodel.routing_policy.expr.VarRouteType;
import org.batfish.datamodel.vendor_family.cisco.Aaa;
import org.batfish.datamodel.vendor_family.cisco.AaaAccounting;
import org.batfish.datamodel.vendor_family.cisco.AaaAccountingCommands;
import org.batfish.datamodel.vendor_family.cisco.AaaAccountingDefault;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthentication;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLogin;
import org.batfish.datamodel.vendor_family.cisco.AaaAuthenticationLoginList;
import org.batfish.datamodel.vendor_family.cisco.Buffered;
import org.batfish.datamodel.vendor_family.cisco.Line;
import org.batfish.datamodel.vendor_family.cisco.Logging;
import org.batfish.datamodel.vendor_family.cisco.LoggingHost;
import org.batfish.datamodel.vendor_family.cisco.LoggingType;
import org.batfish.datamodel.vendor_family.cisco.Ntp;
import org.batfish.datamodel.vendor_family.cisco.NtpServer;
import org.batfish.datamodel.vendor_family.cisco.Service;
import org.batfish.datamodel.vendor_family.cisco.Sntp;
import org.batfish.datamodel.vendor_family.cisco.SntpServer;
import org.batfish.datamodel.vendor_family.cisco.SshSettings;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.cisco.CiscoParser.*;
import org.batfish.representation.cisco.AsPathSet;
import org.batfish.representation.cisco.BgpAggregateIpv4Network;
import org.batfish.representation.cisco.BgpAggregateIpv6Network;
import org.batfish.representation.cisco.BgpNetwork;
import org.batfish.representation.cisco.BgpNetwork6;
import org.batfish.representation.cisco.BgpPeerGroup;
import org.batfish.representation.cisco.BgpProcess;
import org.batfish.representation.cisco.BgpRedistributionPolicy;
import org.batfish.representation.cisco.CiscoConfiguration;
import org.batfish.representation.cisco.CiscoStructureType;
import org.batfish.representation.cisco.CiscoStructureUsage;
import org.batfish.representation.cisco.DynamicIpBgpPeerGroup;
import org.batfish.representation.cisco.DynamicIpv6BgpPeerGroup;
import org.batfish.representation.cisco.ExpandedCommunityList;
import org.batfish.representation.cisco.ExpandedCommunityListLine;
import org.batfish.representation.cisco.ExtendedAccessList;
import org.batfish.representation.cisco.ExtendedAccessListLine;
import org.batfish.representation.cisco.ExtendedIpv6AccessList;
import org.batfish.representation.cisco.ExtendedIpv6AccessListLine;
import org.batfish.representation.cisco.Interface;
import org.batfish.representation.cisco.IpAsPathAccessList;
import org.batfish.representation.cisco.IpAsPathAccessListLine;
import org.batfish.representation.cisco.IpBgpPeerGroup;
import org.batfish.representation.cisco.Ipv6BgpPeerGroup;
import org.batfish.representation.cisco.IsisProcess;
import org.batfish.representation.cisco.IsisRedistributionPolicy;
import org.batfish.representation.cisco.MacAccessList;
import org.batfish.representation.cisco.MasterBgpPeerGroup;
import org.batfish.representation.cisco.NamedBgpPeerGroup;
import org.batfish.representation.cisco.NatPool;
import org.batfish.representation.cisco.OspfNetwork;
import org.batfish.representation.cisco.OspfProcess;
import org.batfish.representation.cisco.OspfRedistributionPolicy;
import org.batfish.representation.cisco.OspfWildcardNetwork;
import org.batfish.representation.cisco.Prefix6List;
import org.batfish.representation.cisco.Prefix6ListLine;
import org.batfish.representation.cisco.PrefixList;
import org.batfish.representation.cisco.PrefixListLine;
import org.batfish.representation.cisco.RouteMap;
import org.batfish.representation.cisco.RouteMapClause;
import org.batfish.representation.cisco.RouteMapContinue;
import org.batfish.representation.cisco.RouteMapMatchAsPathAccessListLine;
import org.batfish.representation.cisco.RouteMapMatchCommunityListLine;
import org.batfish.representation.cisco.RouteMapMatchIpAccessListLine;
import org.batfish.representation.cisco.RouteMapMatchIpPrefixListLine;
import org.batfish.representation.cisco.RouteMapMatchIpv6AccessListLine;
import org.batfish.representation.cisco.RouteMapMatchIpv6PrefixListLine;
import org.batfish.representation.cisco.RouteMapMatchTagLine;
import org.batfish.representation.cisco.RouteMapSetAdditiveCommunityLine;
import org.batfish.representation.cisco.RouteMapSetAdditiveCommunityListLine;
import org.batfish.representation.cisco.RouteMapSetAsPathPrependLine;
import org.batfish.representation.cisco.RouteMapSetCommunityLine;
import org.batfish.representation.cisco.RouteMapSetCommunityListLine;
import org.batfish.representation.cisco.RouteMapSetCommunityNoneLine;
import org.batfish.representation.cisco.RouteMapSetDeleteCommunityLine;
import org.batfish.representation.cisco.RouteMapSetLine;
import org.batfish.representation.cisco.RouteMapSetLocalPreferenceLine;
import org.batfish.representation.cisco.RouteMapSetMetricLine;
import org.batfish.representation.cisco.RouteMapSetNextHopLine;
import org.batfish.representation.cisco.RouteMapSetNextHopPeerAddress;
import org.batfish.representation.cisco.RouteMapSetOriginTypeLine;
import org.batfish.representation.cisco.RoutePolicy;
import org.batfish.representation.cisco.RoutePolicyApplyStatement;
import org.batfish.representation.cisco.RoutePolicyBoolean;
import org.batfish.representation.cisco.RoutePolicyBooleanAnd;
import org.batfish.representation.cisco.RoutePolicyBooleanApply;
import org.batfish.representation.cisco.RoutePolicyBooleanAsPathIn;
import org.batfish.representation.cisco.RoutePolicyBooleanAsPathNeighborIs;
import org.batfish.representation.cisco.RoutePolicyBooleanAsPathOriginatesFrom;
import org.batfish.representation.cisco.RoutePolicyBooleanAsPathPassesThrough;
import org.batfish.representation.cisco.RoutePolicyBooleanCommunityMatchesAny;
import org.batfish.representation.cisco.RoutePolicyBooleanCommunityMatchesEvery;
import org.batfish.representation.cisco.RoutePolicyBooleanDestination;
import org.batfish.representation.cisco.RoutePolicyBooleanLocalPreference;
import org.batfish.representation.cisco.RoutePolicyBooleanMed;
import org.batfish.representation.cisco.RoutePolicyBooleanNextHopIn;
import org.batfish.representation.cisco.RoutePolicyBooleanNot;
import org.batfish.representation.cisco.RoutePolicyBooleanOr;
import org.batfish.representation.cisco.RoutePolicyBooleanRibHasRoute;
import org.batfish.representation.cisco.RoutePolicyBooleanRouteTypeIs;
import org.batfish.representation.cisco.RoutePolicyBooleanTagIs;
import org.batfish.representation.cisco.RoutePolicyComment;
import org.batfish.representation.cisco.RoutePolicyCommunitySet;
import org.batfish.representation.cisco.RoutePolicyCommunitySetInline;
import org.batfish.representation.cisco.RoutePolicyCommunitySetName;
import org.batfish.representation.cisco.RoutePolicyDeleteAllStatement;
import org.batfish.representation.cisco.RoutePolicyDeleteCommunityStatement;
import org.batfish.representation.cisco.RoutePolicyDispositionStatement;
import org.batfish.representation.cisco.RoutePolicyDispositionType;
import org.batfish.representation.cisco.RoutePolicyElseBlock;
import org.batfish.representation.cisco.RoutePolicyElseIfBlock;
import org.batfish.representation.cisco.RoutePolicyIfStatement;
import org.batfish.representation.cisco.RoutePolicyInlinePrefix6Set;
import org.batfish.representation.cisco.RoutePolicyInlinePrefixSet;
import org.batfish.representation.cisco.RoutePolicyNextHop;
import org.batfish.representation.cisco.RoutePolicyNextHopIP6;
import org.batfish.representation.cisco.RoutePolicyNextHopIp;
import org.batfish.representation.cisco.RoutePolicyNextHopPeerAddress;
import org.batfish.representation.cisco.RoutePolicyNextHopSelf;
import org.batfish.representation.cisco.RoutePolicyPrefixSet;
import org.batfish.representation.cisco.RoutePolicyPrefixSetName;
import org.batfish.representation.cisco.RoutePolicyPrependAsPath;
import org.batfish.representation.cisco.RoutePolicySetCommunity;
import org.batfish.representation.cisco.RoutePolicySetIsisMetric;
import org.batfish.representation.cisco.RoutePolicySetIsisMetricType;
import org.batfish.representation.cisco.RoutePolicySetLevel;
import org.batfish.representation.cisco.RoutePolicySetLocalPref;
import org.batfish.representation.cisco.RoutePolicySetMed;
import org.batfish.representation.cisco.RoutePolicySetNextHop;
import org.batfish.representation.cisco.RoutePolicySetOrigin;
import org.batfish.representation.cisco.RoutePolicySetOspfMetricType;
import org.batfish.representation.cisco.RoutePolicySetTag;
import org.batfish.representation.cisco.RoutePolicySetVarMetricType;
import org.batfish.representation.cisco.RoutePolicySetWeight;
import org.batfish.representation.cisco.RoutePolicyStatement;
import org.batfish.representation.cisco.StandardAccessList;
import org.batfish.representation.cisco.StandardAccessListLine;
import org.batfish.representation.cisco.StandardCommunityList;
import org.batfish.representation.cisco.StandardCommunityListLine;
import org.batfish.representation.cisco.StandardIpv6AccessList;
import org.batfish.representation.cisco.StandardIpv6AccessListLine;
import org.batfish.representation.cisco.StaticRoute;
import org.batfish.representation.cisco.Vrf;
import org.batfish.vendor.VendorConfiguration;

public class CiscoControlPlaneExtractor extends CiscoParserBaseListener
      implements ControlPlaneExtractor {

   private static final Map<String, String> CISCO_INTERFACE_PREFIXES = getCiscoInterfacePrefixes();

   private static final int DEFAULT_STATIC_ROUTE_DISTANCE = 1;

   private static final String DUPLICATE = "DUPLICATE";

   private static final String F_ALLOWAS_IN_NUMBER =
         "bgp -  allowas-in with number - ignored and effectively infinite for now";

   private static final String F_BGP_AUTO_SUMMARY = "bgp - auto-summary";

   private static final String F_BGP_INHERIT_PEER_OTHER =
         "bgp - inherit peer - inheritance not implemented for this peer type";

   private static final String F_BGP_INHERIT_PEER_SESSION_OTHER =
         "bgp - inherit peer-session - inheritance not implemented for this peer type";

   private static final String F_BGP_MAXIMUM_PEERS = "bgp - maximum-peers";

   private static final String F_BGP_NEIGHBOR_DISTRIBUTE_LIST = "bgp - neighbor distribute-list";

   private static final String F_BGP_REDISTRIBUTE_AGGREGATE = "bgp - redistribute aggregate";

   private static final String F_FRAGMENTS = "acl fragments";

   private static final String F_INTERFACE_MULTIPOINT = "interface multipoint";

   private static final String F_IP_DEFAULT_GATEWAY = "ip default-gateway";

   private static final String F_IP_ROUTE_VRF = "ip route vrf / vrf - ip route";

   private static final String F_OSPF_AREA_NSSA = "ospf - not-so-stubby areas";

   private static final String F_OSPF_MAXIMUM_PATHS = "ospf - maximum-paths";

   private static final String F_OSPF_REDISTRIBUTE_RIP = "ospf - redistribute rip";

   private static final String F_RIP = "rip";

   private static final String F_ROUTE_MAP_SET_METRIC_TYPE = "route-map - set metric-type";

   private static final String F_SWITCHING_MODE = "switching-mode";

   private static final String F_TTL = "acl ttl eq number";

   private static final String NXOS_MANAGEMENT_INTERFACE_PREFIX = "mgmt";

   private static String getCanonicalInterfaceName(String ifaceName) {
      Matcher matcher = Pattern.compile("[A-Za-z][-A-Za-z0-9]*[A-Za-z]")
            .matcher(ifaceName);
      if (matcher.find()) {
         String ifacePrefix = matcher.group();
         String canonicalPrefix = getCanonicalInterfaceNamePrefix(ifacePrefix);
         String suffix = ifaceName.substring(ifacePrefix.length());
         return canonicalPrefix + suffix;
      }
      throw new BatfishException("Invalid interface name: '" + ifaceName + "'");
   }

   private static String getCanonicalInterfaceNamePrefix(String prefix) {
      for (Entry<String, String> e : CISCO_INTERFACE_PREFIXES.entrySet()) {
         String matchPrefix = e.getKey();
         String canonicalPrefix = e.getValue();
         if (matchPrefix.toLowerCase().startsWith(prefix.toLowerCase())) {
            return canonicalPrefix;
         }
      }
      throw new BatfishException(
            "Invalid interface name prefix: '" + prefix + "'");
   }

   private synchronized static Map<String, String> getCiscoInterfacePrefixes() {
      Map<String, String> prefixes = new LinkedHashMap<>();
      prefixes.put("Async", "Async");
      prefixes.put("ATM", "ATM");
      prefixes.put("BDI", "BDI");
      prefixes.put("BRI", "BRI");
      prefixes.put("Bundle-Ether", "Bundle-Ethernet");
      prefixes.put("BVI", "BVI");
      prefixes.put("Crypto-Engine", "Crypto-Engine");
      prefixes.put("cmp-mgmt", "cmp-mgmt");
      prefixes.put("Dialer", "Dialer");
      prefixes.put("Dot11Radio", "Dot11Radio");
      prefixes.put("Embedded-Service-Engine", "Embedded-Service-Engine");
      prefixes.put("Ethernet", "Ethernet");
      prefixes.put("FastEthernet", "FastEthernet");
      prefixes.put("fc", "fc");
      prefixes.put("fe", "FastEthernet");
      prefixes.put("fortyGigE", "FortyGigabitEthernet");
      prefixes.put("FortyGigabitEthernet", "FortyGigabitEthernet");
      prefixes.put("GigabitEthernet", "GigabitEthernet");
      prefixes.put("ge", "GigabitEthernet");
      prefixes.put("GMPLS", "GMPLS");
      prefixes.put("HundredGigE", "HundredGigabitEthernet");
      prefixes.put("ip", "ip");
      prefixes.put("Group-Async", "Group-Async");
      prefixes.put("LongReachEthernet", "LongReachEthernet");
      prefixes.put("Loopback", "Loopback");
      prefixes.put("Management", "Management");
      prefixes.put("ManagementEthernet", "ManagementEthernet");
      prefixes.put("mgmt", NXOS_MANAGEMENT_INTERFACE_PREFIX);
      prefixes.put("MgmtEth", "ManagementEthernet");
      prefixes.put("Null", "Null");
      prefixes.put("Port-channel", "Port-Channel");
      prefixes.put("POS", "POS");
      prefixes.put("PTP", "PTP");
      prefixes.put("Serial", "Serial");
      prefixes.put("TenGigabitEthernet", "TenGigabitEthernet");
      prefixes.put("TenGigE", "TenGigabitEthernet");
      prefixes.put("te", "TenGigabitEthernet");
      prefixes.put("trunk", "trunk");
      prefixes.put("Tunnel", "Tunnel");
      prefixes.put("tunnel-ip", "tunnel-ip");
      prefixes.put("tunnel-te", "tunnel-te");
      prefixes.put("ve", "VirtualEthernet");
      prefixes.put("Virtual-Template", "Virtual-Template");
      prefixes.put("Vlan", "Vlan");
      prefixes.put("Vxlan", "Vxlan");
      return prefixes;
   }

   private static Ip getIp(Access_list_ip_rangeContext ctx) {
      if (ctx.ip != null) {
         return toIp(ctx.ip);
      }
      else if (ctx.prefix != null) {
         return new Prefix(ctx.prefix.getText()).getAddress();
      }
      else {
         return Ip.ZERO;
      }
   }

   private static Ip6 getIp(Access_list_ip6_rangeContext ctx) {
      if (ctx.ip != null) {
         return toIp6(ctx.ip);
      }
      else if (ctx.ipv6_prefix != null) {
         return new Prefix6(ctx.ipv6_prefix.getText()).getAddress();
      }
      else {
         return Ip6.ZERO;
      }
   }

   private static int toInteger(TerminalNode t) {
      return Integer.parseInt(t.getText());
   }

   private static int toInteger(Token t) {
      return Integer.parseInt(t.getText());
   }

   private static String toInterfaceName(Interface_nameContext ctx) {
      String canonicalNamePrefix = getCanonicalInterfaceNamePrefix(
            ctx.name_prefix_alpha.getText());
      String name = canonicalNamePrefix;
      for (Token part : ctx.name_middle_parts) {
         name += part.getText();
      }
      if (ctx.range().range_list.size() != 1) {
         throw new RedFlagBatfishException(
               "got interface range where single interface was expected: '"
                     + ctx.getText() + "'");
      }
      name += ctx.range().getText();
      return name;
   }

   private static Ip toIp(TerminalNode t) {
      return new Ip(t.getText());
   }

   private static Ip toIp(Token t) {
      return new Ip(t.getText());
   }

   private static Ip6 toIp6(TerminalNode t) {
      return new Ip6(t.getText());
   }

   private static Ip6 toIp6(Token t) {
      return new Ip6(t.getText());
   }

   private static long toLong(Token t) {
      return Long.parseLong(t.getText());
   }

   private static List<SubRange> toRange(RangeContext ctx) {
      List<SubRange> range = new ArrayList<>();
      for (SubrangeContext sc : ctx.range_list) {
         SubRange sr = toSubRange(sc);
         range.add(sr);
      }
      return range;
   }

   private static SubRange toSubRange(SubrangeContext ctx) {
      int low = toInteger(ctx.low);
      if (ctx.DASH() != null) {
         int high = toInteger(ctx.high);
         return new SubRange(low, high);
      }
      else {
         return new SubRange(low, low);
      }
   }

   private CiscoConfiguration _configuration;

   @SuppressWarnings("unused")
   private List<AaaAccountingCommands> _currentAaaAccountingCommands;

   private AaaAuthenticationLoginList _currentAaaAuthenticationLoginList;

   private String _currentAddressFamily;

   private IpAsPathAccessList _currentAsPathAcl;

   private final Set<String> _currentBlockNeighborAddressFamilies;

   private DynamicIpBgpPeerGroup _currentDynamicIpPeerGroup;

   private DynamicIpv6BgpPeerGroup _currentDynamicIpv6PeerGroup;

   private ExpandedCommunityList _currentExpandedCommunityList;

   private ExtendedAccessList _currentExtendedAcl;

   private ExtendedIpv6AccessList _currentExtendedIpv6Acl;

   private List<Interface> _currentInterfaces;

   private IpBgpPeerGroup _currentIpPeerGroup;

   private Ipv6BgpPeerGroup _currentIpv6PeerGroup;

   private Interface _currentIsisInterface;

   private IsisProcess _currentIsisProcess;

   private List<String> _currentLineNames;

   @SuppressWarnings("unused")
   private MacAccessList _currentMacAccessList;

   private NamedBgpPeerGroup _currentNamedPeerGroup;

   private NatPool _currentNatPool;

   private Long _currentOspfArea;

   private String _currentOspfInterface;

   private OspfProcess _currentOspfProcess;

   private BgpPeerGroup _currentPeerGroup;

   private NamedBgpPeerGroup _currentPeerSession;

   private Prefix6List _currentPrefix6List;

   private PrefixList _currentPrefixList;

   private int _currentPrefixSetDefinitionLine;

   private String _currentPrefixSetName;

   private RouteMap _currentRouteMap;
   
   private RouteMapClause _currentRouteMapClause;
   
   private RoutePolicy _currentRoutePolicy;

   private SnmpCommunity _currentSnmpCommunity;

   @SuppressWarnings("unused")
   private SnmpHost _currentSnmpHost;

   private StandardAccessList _currentStandardAcl;

   private StandardCommunityList _currentStandardCommunityList;

   private StandardIpv6AccessList _currentStandardIpv6Acl;

   private String _currentVrf;

   private BgpPeerGroup _dummyPeerGroup;

   private ConfigurationFormat _format;

   private boolean _inBlockNeighbor;

   private boolean _inIpv6BgpPeer;

   private boolean _no;

   private final BatfishCombinedParser<?, ?> _parser;

   private List<BgpPeerGroup> _peerGroupStack;

   private final String _text;

   private final Set<String> _unimplementedFeatures;

   private final boolean _unrecognizedAsRedFlag;

   private final Warnings _w;

   public CiscoControlPlaneExtractor(
         String text,
         BatfishCombinedParser<?, ?> parser, ConfigurationFormat format,
         Warnings warnings, boolean unrecognizedAsRedFlag) {
      _text = text;
      _parser = parser;
      _format = format;
      _unimplementedFeatures = new TreeSet<>();
      _w = warnings;
      _peerGroupStack = new ArrayList<>();
      _unrecognizedAsRedFlag = unrecognizedAsRedFlag;
      _currentBlockNeighborAddressFamilies = new HashSet<>();
   }

   private Interface addInterface(
         String name, Interface_nameContext ctx,
         boolean explicit) {
      Interface newInterface = _configuration.getInterfaces().get(name);
      if (newInterface == null) {
         newInterface = new Interface(name, _configuration);
         initInterface(newInterface, _configuration.getVendor());
         _configuration.getInterfaces().put(name, newInterface);
         initInterface(newInterface, ctx);
      }
      else {
         _w.pedantic("Interface: '" + name + "' altered more than once");
      }
      if (explicit) {
         _currentInterfaces.add(newInterface);
      }
      return newInterface;
   }

   private BatfishException convError(Class<?> type, ParserRuleContext ctx) {
      String typeName = type.getSimpleName();
      String txt = getFullText(ctx);
      return new BatfishException(
            "Could not convert to " + typeName + ": " + txt);
   }

   private Vrf currentVrf() {
      return initVrf(_currentVrf);
   }

   @Override
   public void enterAaa_accounting(Aaa_accountingContext ctx) {
      if (_configuration.getCf().getAaa().getAccounting() == null) {
         _configuration.getCf().getAaa().setAccounting(new AaaAccounting());
      }
   }

   @Override
   public void enterAaa_accounting_commands(
         Aaa_accounting_commandsContext ctx) {
      Map<String, AaaAccountingCommands> commands = _configuration.getCf()
            .getAaa().getAccounting().getCommands();
      Set<String> levels = new TreeSet<>();
      if (ctx.levels != null) {
         List<SubRange> range = toRange(ctx.levels);
         for (SubRange subRange : range) {
            for (int i = subRange.getStart(); i <= subRange.getEnd(); i++) {
               String level = Integer.toString(i);
               levels.add(level);
            }
         }
      }
      else {
         levels.add(AaaAccounting.DEFAULT_COMMANDS);
      }
      List<AaaAccountingCommands> currentAaaAccountingCommands = new ArrayList<>();
      for (String level : levels) {
         AaaAccountingCommands c = commands.get(level);
         if (c == null) {
            c = new AaaAccountingCommands();
            commands.put(level, c);
         }
         currentAaaAccountingCommands.add(c);
      }
      _currentAaaAccountingCommands = currentAaaAccountingCommands;
   }

   @Override
   public void enterAaa_accounting_default(Aaa_accounting_defaultContext ctx) {
      AaaAccounting accounting = _configuration.getCf().getAaa()
            .getAccounting();
      if (accounting.getDefault() == null) {
         accounting.setDefault(new AaaAccountingDefault());
      }
   }

   @Override
   public void enterAaa_authentication(Aaa_authenticationContext ctx) {
      if (_configuration.getCf().getAaa().getAuthentication() == null) {
         _configuration.getCf().getAaa()
               .setAuthentication(new AaaAuthentication());
      }
   }

   @Override
   public void enterAaa_authentication_login(
         Aaa_authentication_loginContext ctx) {
      if (_configuration.getCf().getAaa().getAuthentication()
            .getLogin() == null) {
         _configuration.getCf().getAaa().getAuthentication()
               .setLogin(new AaaAuthenticationLogin());
      }
   }

   @Override
   public void enterAaa_authentication_login_list(
         Aaa_authentication_login_listContext ctx) {
      AaaAuthenticationLogin login = _configuration.getCf().getAaa()
            .getAuthentication().getLogin();
      String name;
      if (ctx.DEFAULT() != null) {
         name = AaaAuthenticationLogin.DEFAULT_LIST_NAME;
      }
      else if (ctx.variable() != null) {
         name = ctx.variable().getText();
      }
      else {
         throw new BatfishException("Unsupported mode");
      }
      _currentAaaAuthenticationLoginList = login.getLists().get(name);
      if (_currentAaaAuthenticationLoginList == null) {
         _currentAaaAuthenticationLoginList = new AaaAuthenticationLoginList();
         login.getLists().put(name, _currentAaaAuthenticationLoginList);
      }
   }

   @Override
   public void enterAddress_family_header(Address_family_headerContext ctx) {
      String addressFamilyStr = ctx.addressFamilyStr;
      if (_inBlockNeighbor) {
         if (_currentBlockNeighborAddressFamilies.contains(addressFamilyStr)) {
            popPeer();
            _inBlockNeighbor = false;
            _currentBlockNeighborAddressFamilies.clear();
         }
         else {
            _currentBlockNeighborAddressFamilies.add(addressFamilyStr);
         }
      }
      Bgp_address_familyContext af = ctx.af;
      if (af.VPNV4() != null || af.VPNV6() != null || af.MDT() != null
            || af.MULTICAST() != null) {
         pushPeer(_dummyPeerGroup);
      }
      else {
         pushPeer(_currentPeerGroup);
      }
      if (af.IPV6() != null) {
         _inIpv6BgpPeer = true;
      }
      _currentAddressFamily = addressFamilyStr;
   }

   @Override
   public void enterAf_group_rb_stanza(Af_group_rb_stanzaContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      String name = ctx.name.getText();
      int definitionLine = ctx.name.getStart().getLine();
      // String af = ctx.bgp_address_family().getText();
      NamedBgpPeerGroup afGroup = proc.getAfGroups().get(name);
      if (afGroup == null) {
         proc.addNamedPeerGroup(name, definitionLine);
         afGroup = proc.getNamedPeerGroups().get(name);
      }
      pushPeer(afGroup);
      _currentNamedPeerGroup = afGroup;
   }

   @Override
   public void enterCisco_configuration(Cisco_configurationContext ctx) {
      _configuration = new CiscoConfiguration(_unimplementedFeatures);
      _configuration.setVendor(_format);
      _currentVrf = Configuration.DEFAULT_VRF_NAME;
   }

   @Override
   public void enterExtended_access_list_stanza(
         Extended_access_list_stanzaContext ctx) {
      String name;
      int definitionLine;
      if (ctx.name != null) {
         name = ctx.name.getText();
         definitionLine = ctx.name.getStart().getLine();
      }
      else if (ctx.shortname != null) {
         name = ctx.shortname.getText();
         definitionLine = ctx.shortname.getStart().getLine();
      }
      else if (ctx.num != null) {
         name = ctx.num.getText();
         definitionLine = ctx.num.getLine();
      }
      else {
         throw new BatfishException("Could not determine acl name");
      }
      ExtendedAccessList list = _configuration.getExtendedAcls().get(name);
      if (list == null) {
         list = new ExtendedAccessList(name, definitionLine);
         _configuration.getExtendedAcls().put(name, list);
      }
      _currentExtendedAcl = list;
   }

   @Override
   public void enterExtended_ipv6_access_list_stanza(
         Extended_ipv6_access_list_stanzaContext ctx) {
      String name;
      int definitionLine;
      if (ctx.name != null) {
         name = ctx.name.getText();
         definitionLine = ctx.name.getStart().getLine();
      }
      else {
         throw new BatfishException("Could not determine acl name");
      }
      ExtendedIpv6AccessList list = _configuration.getExtendedIpv6Acls()
            .get(name);
      if (list == null) {
         list = new ExtendedIpv6AccessList(name, definitionLine);
         _configuration.getExtendedIpv6Acls().put(name, list);
      }
      _currentExtendedIpv6Acl = list;
   }

   @Override
   public void enterIf_description(If_descriptionContext ctx) {
      Token descriptionToken = ctx.description_line().text;
      String description = descriptionToken != null
            ? descriptionToken.getText().trim() : "";
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setDescription(description);
      }
   }

   @Override
   public void enterIf_spanning_tree(If_spanning_treeContext ctx) {
      _no = ctx.NO() != null;
   }

   @Override
   public void enterInterface_is_stanza(Interface_is_stanzaContext ctx) {
      String ifaceName = ctx.iname.getText();
      String canonicalIfaceName = getCanonicalInterfaceName(ifaceName);
      Interface iface = _configuration.getInterfaces().get(canonicalIfaceName);
      if (iface == null) {
         iface = addInterface(canonicalIfaceName, ctx.iname, false);
      }
      iface.setIsisInterfaceMode(IsisInterfaceMode.ACTIVE);
      _currentIsisInterface = iface;
   }

   @Override
   public void enterIp_as_path_access_list_stanza(
         Ip_as_path_access_list_stanzaContext ctx) {
      String name = ctx.name.getText();
      int definitionLine = ctx.name.getStart().getLine();
      _currentAsPathAcl = _configuration.getAsPathAccessLists().get(name);
      if (_currentAsPathAcl == null) {
         _currentAsPathAcl = new IpAsPathAccessList(name, definitionLine);
         _configuration.getAsPathAccessLists().put(name, _currentAsPathAcl);
      }
   }

   @Override
   public void enterIp_community_list_expanded_stanza(
         Ip_community_list_expanded_stanzaContext ctx) {
      String name;
      int definitionLine;
      if (ctx.num != null) {
         name = ctx.num.getText();
         definitionLine = ctx.num.getLine();
      }
      else if (ctx.name != null) {
         name = ctx.name.getText();
         definitionLine = ctx.name.getStart().getLine();
      }
      else {
         throw new BatfishException("Invalid community-list name");
      }
      _currentExpandedCommunityList = _configuration.getExpandedCommunityLists()
            .get(name);
      if (_currentExpandedCommunityList == null) {
         _currentExpandedCommunityList = new ExpandedCommunityList(
               name,
               definitionLine);
         _configuration.getExpandedCommunityLists().put(
               name,
               _currentExpandedCommunityList);
      }
   }

   @Override
   public void enterIp_community_list_standard_stanza(
         Ip_community_list_standard_stanzaContext ctx) {
      String name;
      int definitionLine;
      if (ctx.num != null) {
         name = ctx.num.getText();
         definitionLine = ctx.num.getLine();
      }
      else if (ctx.name != null) {
         name = ctx.name.getText();
         definitionLine = ctx.name.getStart().getLine();
      }
      else {
         throw new BatfishException("Invalid standard community-list name");
      }
      _currentStandardCommunityList = _configuration.getStandardCommunityLists()
            .get(name);
      if (_currentStandardCommunityList == null) {
         _currentStandardCommunityList = new StandardCommunityList(
               name,
               definitionLine);
         _configuration.getStandardCommunityLists().put(
               name,
               _currentStandardCommunityList);
      }
   }

   @Override
   public void enterIp_nat_pool(Ip_nat_poolContext ctx) {
      String name = ctx.name.getText();
      int definitionLine = ctx.name.getStart().getLine();
      NatPool natPool = new NatPool(name, definitionLine);
      _configuration.getNatPools().put(name, natPool);
      _currentNatPool = natPool;
   }

   @Override
   public void enterIp_prefix_list_stanza(Ip_prefix_list_stanzaContext ctx) {
      String name = ctx.name.getText();
      int definitionLine = ctx.name.getStart().getLine();
      PrefixList list = _configuration.getPrefixLists().get(name);
      if (list == null) {
         list = new PrefixList(name, definitionLine);
         _configuration.getPrefixLists().put(name, list);
      }
      _currentPrefixList = list;
   }

   @Override
   public void enterIp_route_stanza(Ip_route_stanzaContext ctx) {
      if (ctx.vrf != null) {
         _currentVrf = ctx.vrf.getText();
      }
      if (ctx.MANAGEMENT() != null) {
         _currentVrf = CiscoConfiguration.MANAGEMENT_VRF_NAME;
      }
   }

   @Override
   public void enterIpv6_prefix_list_stanza(
         Ipv6_prefix_list_stanzaContext ctx) {
      String name = ctx.name.getText();
      int definitionLine = ctx.name.getStart().getLine();
      Prefix6List list = _configuration.getPrefix6Lists().get(name);
      if (list == null) {
         list = new Prefix6List(name, definitionLine);
         _configuration.getPrefix6Lists().put(name, list);
      }
      _currentPrefix6List = list;
   }

   @Override
   public void enterIs_type_is_stanza(Is_type_is_stanzaContext ctx) {
      IsisProcess proc = currentVrf().getIsisProcess();
      if (ctx.LEVEL_1() != null) {
         proc.setLevel(IsisLevel.LEVEL_1);
      }
      else if (ctx.LEVEL_2_ONLY() != null || ctx.LEVEL_2() != null) {
         proc.setLevel(IsisLevel.LEVEL_2);
      }
      else {
         throw new BatfishException("Unsupported is-type");
      }
   }

   @Override
   public void enterLogging_address(Logging_addressContext ctx) {
      if (_no) {
         return;
      }
      Logging logging = _configuration.getCf().getLogging();
      String hostname = ctx.hostname.getText();
      LoggingHost host = new LoggingHost(hostname);
      logging.getHosts().put(hostname, host);
   }

   @Override
   public void enterNeighbor_block_rb_stanza(
         Neighbor_block_rb_stanzaContext ctx) {
      _currentBlockNeighborAddressFamilies.clear();
      _inBlockNeighbor = true;
      // do no further processing for unsupported address families / containers
      if (_currentPeerGroup == _dummyPeerGroup) {
         pushPeer(_dummyPeerGroup);
         return;
      }
      BgpProcess proc = currentVrf().getBgpProcess();
      if (ctx.ip_address != null) {
         Ip ip = toIp(ctx.ip_address);
         _currentIpPeerGroup = proc.getIpPeerGroups().get(ip);
         if (_currentIpPeerGroup == null) {
            proc.addIpPeerGroup(ip);
            _currentIpPeerGroup = proc.getIpPeerGroups().get(ip);
         }
         else {
            _w.redFlag("Duplicate IP peer group in neighbor config (line:"
                  + ctx.start.getLine() + ")", DUPLICATE);
         }
         pushPeer(_currentIpPeerGroup);
      }
      else if (ctx.ip_prefix != null) {
         Prefix prefix = new Prefix(ctx.ip_prefix.getText());
         _currentDynamicIpPeerGroup = proc.getDynamicIpPeerGroups().get(prefix);
         if (_currentDynamicIpPeerGroup == null) {
            _currentDynamicIpPeerGroup = proc.addDynamicIpPeerGroup(prefix);
         }
         else {
            _w.redFlag("Duplicate DynamicIP peer group neighbor config (line:"
                  + ctx.start.getLine() + ")", DUPLICATE);
         }
         pushPeer(_currentDynamicIpPeerGroup);
      }
      else if (ctx.ipv6_address != null) {
         Ip6 ip6 = toIp6(ctx.ipv6_address);
         Ipv6BgpPeerGroup pg = proc.getIpv6PeerGroups().get(ip6);
         if (pg == null) {
            proc.addIpv6PeerGroup(ip6);
            pg = proc.getIpv6PeerGroups().get(ip6);
         }
         else {
            _w.redFlag("Duplicate IPV6 peer group in neighbor config (line:"
                  + ctx.start.getLine() + ")", DUPLICATE);
         }
         pushPeer(pg);
         _currentIpv6PeerGroup = pg;
      }
      else if (ctx.ipv6_prefix != null) {
         Prefix6 prefix6 = new Prefix6(ctx.ipv6_prefix.getText());
         DynamicIpv6BgpPeerGroup pg = proc.getDynamicIpv6PeerGroups()
               .get(prefix6);
         if (pg == null) {
            pg = proc.addDynamicIpv6PeerGroup(prefix6);
         }
         else {
            _w.redFlag(
                  "Duplicate Dynamic Ipv6 peer group neighbor config (line:"
                        + ctx.start.getLine() + ")",
                  DUPLICATE);
         }
         pushPeer(pg);
         _currentDynamicIpv6PeerGroup = pg;
      }
      if (ctx.REMOTE_AS() != null) {
         int remoteAs = toInteger(ctx.asnum);
         _currentPeerGroup.setRemoteAs(remoteAs);
      }
      // TODO: verify if this is correct for nexus
      _currentPeerGroup.setActive(true);
      _currentPeerGroup.setShutdown(false);
   }

   @Override
   public void enterNeighbor_flat_rb_stanza(
         Neighbor_flat_rb_stanzaContext ctx) {
      // do no further processing for unsupported address families / containers
      if (_currentPeerGroup == _dummyPeerGroup) {
         pushPeer(_dummyPeerGroup);
         return;
      }
      BgpProcess proc = currentVrf().getBgpProcess();
      // we must create peer group if it does not exist and this is a remote_as
      // declaration
      boolean create = ctx.remote_as_bgp_tail() != null
            || ctx.inherit_peer_session_bgp_tail() != null;
      if (ctx.ip != null) {
         Ip ip = toIp(ctx.ip);
         _currentIpPeerGroup = proc.getIpPeerGroups().get(ip);
         if (_currentIpPeerGroup == null) {
            if (create) {
               proc.addIpPeerGroup(ip);
               _currentIpPeerGroup = proc.getIpPeerGroups().get(ip);
               pushPeer(_currentIpPeerGroup);
            }
            else {
               String message = "Ignoring reference to undeclared peer group: '"
                     + ip.toString() + "'";
               _w.redFlag(message);
               pushPeer(_dummyPeerGroup);
            }
         }
         else {
            pushPeer(_currentIpPeerGroup);
         }
      }
      else if (ctx.ip6 != null) {
         Ip6 ip6 = toIp6(ctx.ip6);
         Ipv6BgpPeerGroup pg6 = proc.getIpv6PeerGroups().get(ip6);
         if (pg6 == null) {
            if (create) {
               proc.addIpv6PeerGroup(ip6);
               pg6 = proc.getIpv6PeerGroups().get(ip6);
               pushPeer(pg6);
            }
            else {
               String message = "Ignoring reference to undeclared peer group: '"
                     + ip6.toString() + "'";
               _w.redFlag(message);
               pushPeer(_dummyPeerGroup);
            }
         }
         else {
            pushPeer(pg6);
         }
         _currentIpv6PeerGroup = pg6;
      }
      else if (ctx.peergroup != null) {
         String name = ctx.peergroup.getText();
         int definitionLine = ctx.peergroup.getLine();
         _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
         if (_currentNamedPeerGroup == null) {
            if (create
                  || _configuration.getVendor() == ConfigurationFormat.ARISTA) {
               proc.addNamedPeerGroup(name, definitionLine);
               _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
            }
            else {
               int line = ctx.peergroup.getLine();
               _configuration.getUndefinedPeerGroups().put(name, line);
               _w.redFlag("reference to undeclared peer group: '" + name + "'");
            }
         }
         pushPeer(_currentNamedPeerGroup);
      }
      else {
         throw new BatfishException("unknown neighbor type");
      }
   }

   @Override
   public void enterNeighbor_group_rb_stanza(
         Neighbor_group_rb_stanzaContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      String name = ctx.name.getText();
      int definitionLine = ctx.name.getStart().getLine();
      _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
      if (_currentNamedPeerGroup == null) {
         proc.addNamedPeerGroup(name, definitionLine);
         _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
      }
      pushPeer(_currentNamedPeerGroup);
   }

   @Override
   public void enterNet_is_stanza(Net_is_stanzaContext ctx) {
      IsisProcess proc = currentVrf().getIsisProcess();
      IsoAddress isoAddress = new IsoAddress(ctx.ISO_ADDRESS().getText());
      proc.setNetAddress(isoAddress);
   }

   @Override
   public void enterPrefix_set_stanza(Prefix_set_stanzaContext ctx) {
      _currentPrefixSetName = ctx.name.getText();
      _currentPrefixSetDefinitionLine = ctx.name.getStart().getLine();
   }

   @Override
   public void enterRo_area(Ro_areaContext ctx) {
      long area;
      if (ctx.area_int != null) {
         area = toInteger(ctx.area_int);
      }
      else if (ctx.area_ip != null) {
         area = toIp(ctx.area_ip).asLong();
      }
      else {
         throw new BatfishException("Missing area");
      }
      _currentOspfArea = area;
   }

   @Override
   public void enterRoa_interface(Roa_interfaceContext ctx) {
      String ifaceName = ctx.iname.getText();
      String canonicalIfaceName = getCanonicalInterfaceName(ifaceName);
      Interface iface = _configuration.getInterfaces().get(canonicalIfaceName);
      if (iface == null) {
         _w.redFlag("OSPF: Interface: '" + ifaceName
               + "' not declared before OSPF process");
         iface = addInterface(canonicalIfaceName, ctx.iname, false);
      }
      // might cause problems if interfaces are declared after ospf, but
      // whatever
      for (Prefix prefix : iface.getAllPrefixes()) {
         Prefix networkPrefix = prefix.getNetworkPrefix();
         OspfNetwork network = new OspfNetwork(networkPrefix, _currentOspfArea);
         _currentOspfProcess.getNetworks().add(network);
      }
      _currentOspfInterface = iface.getName();
   }

   @Override
   public void enterRoute_map_stanza(Route_map_stanzaContext ctx) {
      String name = ctx.name.getText();
      int definitionLine = ctx.name.getStart().getLine();
      RouteMap routeMap = _configuration.getRouteMaps().get(name);
      if (routeMap == null) {
         routeMap = new RouteMap(name, definitionLine);
         _configuration.getRouteMaps().put(name, routeMap);
      }
      _currentRouteMap = routeMap;
      int num = toInteger(ctx.num);
      LineAction action = toLineAction(ctx.rmt);
      RouteMapClause clause = _currentRouteMap.getClauses().get(num);
      if (clause == null) {
         clause = new RouteMapClause(action, name, num);
         routeMap.getClauses().put(num, clause);
      }
      else {
         _w.redFlag("Route map '" + _currentRouteMap.getName()
               + "' already contains clause numbered '" + num
               + "'. Duplicate clause will be merged with original clause.");
      }
      _currentRouteMapClause = clause;
   }

   @Override
   public void enterRoute_policy_stanza(Route_policy_stanzaContext ctx) {
      String name = ctx.name.getText();
      _currentRoutePolicy = _configuration.getRoutePolicies().get(name);
      if (_currentRoutePolicy == null) {
         _currentRoutePolicy = new RoutePolicy(name);
         _configuration.getRoutePolicies().put(name, _currentRoutePolicy);
      }

      List<RoutePolicyStatement> stmts = _currentRoutePolicy.getStatements();

      stmts.addAll(toRoutePolicyStatementList(ctx.route_policy_tail().stanzas));
   }

   @Override
   public void enterRouter_bgp_stanza(Router_bgp_stanzaContext ctx) {
      int procNum = (ctx.procnum == null) ? 0 : toInteger(ctx.procnum);
      BgpProcess proc = new BgpProcess(_format, procNum);
      _configuration.getVrfs().get(Configuration.DEFAULT_VRF_NAME)
            .setBgpProcess(proc);
      _dummyPeerGroup = new MasterBgpPeerGroup();
      pushPeer(proc.getMasterBgpPeerGroup());
   }

   @Override
   public void enterRouter_isis_stanza(Router_isis_stanzaContext ctx) {
      IsisProcess isisProcess = new IsisProcess();
      isisProcess.setLevel(IsisLevel.LEVEL_1_2);
      currentVrf().setIsisProcess(isisProcess);
      _currentIsisProcess = isisProcess;
   }

   @Override
   public void enterRouter_rip_stanza(Router_rip_stanzaContext ctx) {
      todo(ctx, F_RIP);
   }

   @Override
   public void enterRs_vrf(Rs_vrfContext ctx) {
      _currentVrf = ctx.name.getText();
   }

   @Override
   public void enterS_aaa(S_aaaContext ctx) {
      if (_configuration.getCf().getAaa() == null) {
         _configuration.getCf().setAaa(new Aaa());
      }
   }

   @Override
   public void enterS_interface(S_interfaceContext ctx) {
      String nameAlpha = ctx.iname.name_prefix_alpha.getText();
      String canonicalNamePrefix;
      try {
         canonicalNamePrefix = getCanonicalInterfaceNamePrefix(nameAlpha);
      }
      catch (BatfishException e) {
         throw new BatfishException("Error fetching interface name at: "
               + getLocation(ctx) + getFullText(ctx), e);
      }
      String namePrefix = canonicalNamePrefix;
      for (Token part : ctx.iname.name_middle_parts) {
         namePrefix += part.getText();
      }
      _currentInterfaces = new ArrayList<>();
      if (ctx.iname.range() != null) {
         List<SubRange> ranges = toRange(ctx.iname.range());
         for (SubRange range : ranges) {
            for (int i = range.getStart(); i <= range.getEnd(); i++) {
               String name = namePrefix + i;
               addInterface(name, ctx.iname, true);
            }
         }
      }
      else {
         String name = namePrefix;
         addInterface(name, ctx.iname, true);
      }
      if (ctx.MULTIPOINT() != null) {
         todo(ctx, F_INTERFACE_MULTIPOINT);
      }
   }

   @Override
   public void enterS_ip_domain(S_ip_domainContext ctx) {
      _no = ctx.NO() != null;
   }

   @Override
   public void enterS_ip_pim(S_ip_pimContext ctx) {
      _no = ctx.NO() != null;
   }

   @Override
   public void enterS_ip_ssh(S_ip_sshContext ctx) {
      if (_configuration.getCf().getSsh() == null) {
         _configuration.getCf().setSsh(new SshSettings());
      }
   }

   @Override
   public void enterS_line(S_lineContext ctx) {
      String lineType = ctx.line_type().getText();
      if (lineType.equals("")) {
         lineType = "<UNNAMED>";
      }
      String nameBase = lineType;
      Integer slot1 = null;
      Integer slot2 = null;
      Integer port1 = null;
      Integer port2 = null;
      List<String> names = new ArrayList<>();
      if (ctx.first != null) {
         if (ctx.slot1 != null) {
            slot1 = toInteger(ctx.slot1);
            slot2 = slot1;
            if (ctx.port1 != null) {
               port1 = toInteger(ctx.port1);
               port2 = port1;
            }
         }
         int first = toInteger(ctx.first);
         int last;
         if (ctx.last != null) {
            if (ctx.slot2 != null) {
               slot2 = toInteger(ctx.slot2);
               if (ctx.port2 != null) {
                  port2 = toInteger(ctx.port2);
               }
            }
            last = toInteger(ctx.last);
         }
         else {
            last = first;
         }
         if (last < first) {
            throw new BatfishException("Do not support decreasing line range: "
                  + first + " " + last);
         }
         if (slot1 != null && port1 != null) {
            for (int s = slot1; s <= slot2; s++) {
               for (int p = port1; p <= port2; p++) {
                  for (int i = first; i <= last; i++) {
                     String name = nameBase + s + "/" + p + "/" + i;
                     names.add(name);
                  }
               }
            }
         }
         else if (slot1 != null) {
            for (int s = slot1; s <= slot2; s++) {
               for (int i = first; i <= last; i++) {
                  String name = nameBase + s + "/" + i;
                  names.add(name);
               }
            }
         }
         else {
            for (int i = first; i <= last; i++) {
               String name = nameBase + i;
               names.add(name);
            }
         }
      }
      else {
         names.add(nameBase);
      }
      for (String name : names) {
         if (_configuration.getCf().getLines().get(name) == null) {
            Line line = new Line(name);
            line.setLoginAuthentication(
                  AaaAuthenticationLogin.DEFAULT_LIST_NAME);
            _configuration.getCf().getLines().put(name, line);
         }
      }
      _currentLineNames = names;
   }

   @Override
   public void enterS_logging(S_loggingContext ctx) {
      if (_configuration.getCf().getLogging() == null) {
         _configuration.getCf().setLogging(new Logging());
      }
      if (ctx.NO() != null) {
         _no = true;
      }
   }

   @Override
   public void enterS_mac_access_list(S_mac_access_listContext ctx) {
      String name = ctx.num.getText();
      int line = ctx.num.getLine();
      MacAccessList list = _configuration.getMacAccessLists().get(name);
      if (list == null) {
         list = new MacAccessList(name, line);
         _configuration.getMacAccessLists().put(name, list);
      }
      _currentMacAccessList = list;
   }

   @Override
   public void enterS_mac_access_list_extended(
         S_mac_access_list_extendedContext ctx) {
      String name;
      int line;
      if (ctx.num != null) {
         name = ctx.num.getText();
         line = ctx.num.getLine();

      }
      else if (ctx.name != null) {
         name = ctx.name.getText();
         line = ctx.name.getStart().getLine();
      }
      else {
         throw new BatfishException(
               "Could not determine name of extended mac access-list");
      }
      MacAccessList list = _configuration.getMacAccessLists().get(name);
      if (list == null) {
         list = new MacAccessList(name, line);
         _configuration.getMacAccessLists().put(name, list);
      }
      _currentMacAccessList = list;
   }

   @Override
   public void enterS_ntp(S_ntpContext ctx) {
      if (_configuration.getCf().getNtp() == null) {
         _configuration.getCf().setNtp(new Ntp());
      }
   }

   @Override
   public void enterS_router_ospf(S_router_ospfContext ctx) {
      String procName = ctx.name.getText();
      if (ctx.vrf != null) {
         _currentVrf = ctx.vrf.getText();
      }
      OspfProcess proc = new OspfProcess(procName);
      currentVrf().setOspfProcess(proc);
      _currentOspfProcess = proc;
   }

   @Override
   public void enterS_snmp_server(S_snmp_serverContext ctx) {
      if (_configuration.getSnmpServer() == null) {
         SnmpServer snmpServer = new SnmpServer();
         snmpServer.setVrf(Configuration.DEFAULT_VRF_NAME);
         _configuration.setSnmpServer(snmpServer);

      }
   }

   @Override
   public void enterS_sntp(S_sntpContext ctx) {
      if (_configuration.getCf().getSntp() == null) {
         _configuration.getCf().setSntp(new Sntp());
      }
   }

   @Override
   public void enterS_spanning_tree(S_spanning_treeContext ctx) {
      _no = ctx.NO() != null;
   }

   @Override
   public void enterS_tacacs_server(S_tacacs_serverContext ctx) {
      _no = ctx.NO() != null;
   }

   @Override
   public void enterS_vrf_context(S_vrf_contextContext ctx) {
      String vrf = ctx.name.getText();
      _currentVrf = vrf;
   }

   @Override
   public void enterSession_group_rb_stanza(
         Session_group_rb_stanzaContext ctx) {
      String name = ctx.name.getText();
      int definitionLine = ctx.name.getStart().getLine();
      BgpProcess proc = currentVrf().getBgpProcess();
      _currentPeerSession = proc.getPeerSessions().get(name);
      if (_currentPeerSession == null) {
         proc.addPeerSession(name, definitionLine);
         _currentPeerSession = proc.getPeerSessions().get(name);
      }
      pushPeer(_currentPeerSession);
   }

   @Override
   public void enterSs_community(Ss_communityContext ctx) {
      String name = ctx.name.getText();
      Map<String, SnmpCommunity> communities = _configuration.getSnmpServer()
            .getCommunities();
      SnmpCommunity community = communities.get(name);
      if (community == null) {
         community = new SnmpCommunity(name);
         communities.put(name, community);
      }
      _currentSnmpCommunity = community;
   }

   @Override
   public void enterSs_host(Ss_hostContext ctx) {
      String hostname;
      if (ctx.ip4 != null) {
         hostname = ctx.ip4.getText();
      }
      else if (ctx.ip6 != null) {
         hostname = ctx.ip6.getText();
      }
      else if (ctx.host != null) {
         hostname = ctx.host.getText();
      }
      else {
         throw new BatfishException("Invalid host");
      }
      Map<String, SnmpHost> hosts = _configuration.getSnmpServer().getHosts();
      SnmpHost host = hosts.get(hostname);
      if (host == null) {
         host = new SnmpHost(hostname);
         hosts.put(hostname, host);
      }
      _currentSnmpHost = host;
   }

   @Override
   public void enterStandard_access_list_stanza(
         Standard_access_list_stanzaContext ctx) {
      String name;
      int definitionLine;
      if (ctx.name != null) {
         name = ctx.name.getText();
         definitionLine = ctx.name.getStart().getLine();
      }
      else if (ctx.num != null) {
         name = ctx.num.getText();
         definitionLine = ctx.num.getLine();
      }
      else {
         throw new BatfishException("Invalid standard access-list name");
      }
      StandardAccessList list = _configuration.getStandardAcls().get(name);
      if (list == null) {
         list = new StandardAccessList(name, definitionLine);
         _configuration.getStandardAcls().put(name, list);
      }
      _currentStandardAcl = list;
   }

   @Override
   public void enterStandard_ipv6_access_list_stanza(
         Standard_ipv6_access_list_stanzaContext ctx) {
      String name;
      int definitionLine;
      if (ctx.name != null) {
         name = ctx.name.getText();
         definitionLine = ctx.name.getStart().getLine();
      }
      else {
         throw new BatfishException("Invalid standard access-list name");
      }
      StandardIpv6AccessList list = _configuration.getStandardIpv6Acls()
            .get(name);
      if (list == null) {
         list = new StandardIpv6AccessList(name, definitionLine);
         _configuration.getStandardIpv6Acls().put(name, list);
      }
      _currentStandardIpv6Acl = list;
   }

   @Override
   public void enterTemplate_peer_policy_rb_stanza(
         Template_peer_policy_rb_stanzaContext ctx) {
      String name = ctx.name.getText();
      int definitionLine = ctx.name.getLine();
      BgpProcess proc = currentVrf().getBgpProcess();
      _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
      if (_currentNamedPeerGroup == null) {
         proc.addNamedPeerGroup(name, definitionLine);
         _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
      }
      pushPeer(_currentNamedPeerGroup);
   }

   @Override
   public void enterTemplate_peer_rb_stanza(
         Template_peer_rb_stanzaContext ctx) {
      String name = ctx.name.getText();
      int definitionLine = ctx.name.getLine();
      BgpProcess proc = currentVrf().getBgpProcess();
      _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
      if (_currentNamedPeerGroup == null) {
         proc.addNamedPeerGroup(name, definitionLine);
         _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
      }
      pushPeer(_currentNamedPeerGroup);
   }

   @Override
   public void enterTemplate_peer_session_rb_stanza(
         Template_peer_session_rb_stanzaContext ctx) {
      String name = ctx.name.getText();
      int definitionLine = ctx.name.getLine();
      BgpProcess proc = currentVrf().getBgpProcess();
      _currentPeerSession = proc.getPeerSessions().get(name);
      if (_currentPeerSession == null) {
         proc.addPeerSession(name, definitionLine);
         _currentPeerSession = proc.getPeerSessions().get(name);
      }
      pushPeer(_currentPeerSession);
   }

   @Override
   public void enterTs_host(Ts_hostContext ctx) {
      String hostname = ctx.hostname.getText();
      if (!_no) {
         _configuration.getTacacsServers().add(hostname);
      }
   }

   @Override
   public void enterVrf_block_rb_stanza(Vrf_block_rb_stanzaContext ctx) {
      _currentVrf = ctx.name.getText();
      int procNum = _configuration.getVrfs().get(Configuration.DEFAULT_VRF_NAME)
            .getBgpProcess().getName();
      BgpProcess proc = new BgpProcess(_format, procNum);
      currentVrf().setBgpProcess(proc);
      pushPeer(proc.getMasterBgpPeerGroup());
      _currentBlockNeighborAddressFamilies.clear();
      _inBlockNeighbor = false;
   }

   @Override
   public void exitAaa_accounting_commands(Aaa_accounting_commandsContext ctx) {
      _currentAaaAccountingCommands = null;
   }

   @Override
   public void exitAaa_accounting_default_group(
         Aaa_accounting_default_groupContext ctx) {
      List<String> groups = ctx.groups.stream().map(g -> g.getText())
            .collect(Collectors.toList());
      _configuration.getCf().getAaa().getAccounting().getDefault()
            .setGroups(groups);
   }

   @Override
   public void exitAaa_accounting_default_local(
         Aaa_accounting_default_localContext ctx) {
      _configuration.getCf().getAaa().getAccounting().getDefault()
            .setLocal(true);
   }

   @Override
   public void exitAaa_authentication_login_list(
         Aaa_authentication_login_listContext ctx) {
      _currentAaaAuthenticationLoginList = null;
   }

   @Override
   public void exitAaa_authentication_login_privilege_mode(
         Aaa_authentication_login_privilege_modeContext ctx) {
      _configuration.getCf().getAaa().getAuthentication().getLogin()
            .setPrivilegeMode(true);
   }

   @Override
   public void exitAaa_new_model(Aaa_new_modelContext ctx) {
      _configuration.getCf().getAaa().setNewModel(true);
   }

   @Override
   public void exitActivate_bgp_tail(Activate_bgp_tailContext ctx) {
      if (_currentPeerGroup == null) {
         return;
      }
      BgpProcess proc = currentVrf().getBgpProcess();
      if (_currentPeerGroup != proc.getMasterBgpPeerGroup()) {
         _currentPeerGroup.setActive(true);
      }
      else {
         throw new BatfishException(
               "no peer or peer group to activate in this context");
      }
   }

   @Override
   public void exitAdditional_paths_rb_stanza(
         Additional_paths_rb_stanzaContext ctx) {
      if (ctx.SELECT() != null && ctx.ALL() != null) {
         _currentPeerGroup.setAdditionalPathsSelectAll(true);
      }
      else {
         if (ctx.RECEIVE() != null) {
            _currentPeerGroup.setAdditionalPathsReceive(true);
         }
         if (ctx.SEND() != null) {
            _currentPeerGroup.setAdditionalPathsSend(true);
         }
      }
   }

   @Override
   public void exitAddress_family_rb_stanza(
         Address_family_rb_stanzaContext ctx) {
      popPeer();
      _currentAddressFamily = null;
   }

   @Override
   public void exitAf_group_rb_stanza(Af_group_rb_stanzaContext ctx) {
      resetPeerGroups();
      popPeer();
   }

   @Override
   public void exitAggregate_address_rb_stanza(
         Aggregate_address_rb_stanzaContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      if (_currentPeerGroup == proc.getMasterBgpPeerGroup()) {
         boolean summaryOnly = ctx.summary_only != null;
         boolean asSet = ctx.as_set != null;
         if (ctx.network != null || ctx.prefix != null) {
            // ipv4
            Prefix prefix;
            if (ctx.network != null) {
               Ip network = toIp(ctx.network);
               Ip subnet = toIp(ctx.subnet);
               int prefixLength = subnet.numSubnetBits();
               prefix = new Prefix(network, prefixLength);
            }
            else {
               // ctx.prefix != null
               prefix = new Prefix(ctx.prefix.getText());
            }
            BgpAggregateIpv4Network net = new BgpAggregateIpv4Network(prefix);
            net.setAsSet(asSet);
            net.setSummaryOnly(summaryOnly);
            if (ctx.mapname != null) {
               String mapName = ctx.mapname.getText();
               int mapLine = ctx.mapname.getStart().getLine();
               net.setAttributeMap(mapName);
               net.setAttributeMapLine(mapLine);
            }
            proc.getAggregateNetworks().put(prefix, net);
         }
         else if (ctx.ipv6_prefix != null) {
            // ipv6
            Prefix6 prefix6 = new Prefix6(ctx.ipv6_prefix.getText());
            BgpAggregateIpv6Network net = new BgpAggregateIpv6Network(prefix6);
            net.setAsSet(asSet);
            net.setSummaryOnly(summaryOnly);
            if (ctx.mapname != null) {
               String mapName = ctx.mapname.getText();
               int mapLine = ctx.mapname.getStart().getLine();
               net.setAttributeMap(mapName);
               net.setAttributeMapLine(mapLine);
            }
            proc.getAggregateIpv6Networks().put(prefix6, net);
         }
      }
      else if (_currentIpPeerGroup != null || _currentIpv6PeerGroup != null
            || _currentDynamicIpPeerGroup != null
            || _currentDynamicIpv6PeerGroup != null
            || _currentNamedPeerGroup != null) {
         throw new BatfishException(
               "unexpected occurrence in peer group/neighbor context");

      }
      else if (ctx.mapname != null) {
         String map = ctx.mapname.getText();
         int line = ctx.mapname.getStart().getLine();
         _configuration.getBgpVrfAggregateAddressRouteMaps().add(map);
         _configuration.referenceStructure(CiscoStructureType.ROUTE_MAP, map,
               CiscoStructureUsage.BGP_VRF_AGGREGATE_ROUTE_MAP, line);
      }
   }

   @Override
   public void exitAllowas_in_bgp_tail(Allowas_in_bgp_tailContext ctx) {
      _currentPeerGroup.setAllowAsIn(true);
      if (ctx.num != null) {
         todo(ctx, F_ALLOWAS_IN_NUMBER);
      }
   }

   @Override
   public void exitAlways_compare_med_rb_stanza(
         Always_compare_med_rb_stanzaContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      proc.setAlwaysCompareMed(true);
   }

   @Override
   public void exitAs_path_set_stanza(As_path_set_stanzaContext ctx) {
      String name = ctx.name.getText();
      int definitionLine = ctx.name.getStart().getLine();
      AsPathSet asPathSet = _configuration.getAsPathSets().get(name);
      if (asPathSet != null) {
         _w.redFlag("Redeclaration of as-path-set: '" + name + "'");
      }
      asPathSet = new AsPathSet(name, definitionLine);
      _configuration.getAsPathSets().put(name, asPathSet);
      for (As_path_set_elemContext elemCtx : ctx.elems) {
         AsPathSetElem elem = toAsPathSetElem(elemCtx);
         asPathSet.getElements().add(elem);
      }
   }

   @Override
   public void exitAuto_summary_bgp_tail(Auto_summary_bgp_tailContext ctx) {
      todo(ctx, F_BGP_AUTO_SUMMARY);
   }

   @Override
   public void exitBanner_stanza(Banner_stanzaContext ctx) {
      String bannerType = ctx.banner_type().getText();
      String message = ctx.banner().getText();
      _configuration.getCf().getBanners().put(bannerType, message);
   }

   @Override
   public void exitBgp_advertise_inactive_rb_stanza(
         Bgp_advertise_inactive_rb_stanzaContext ctx) {
      _currentPeerGroup.setAdvertiseInactive(true);
   }

   @Override
   public void exitBgp_listen_range_rb_stanza(
         Bgp_listen_range_rb_stanzaContext ctx) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      BgpProcess proc = currentVrf().getBgpProcess();
      if (ctx.IP_PREFIX() != null) {
         Prefix prefix = new Prefix(ctx.IP_PREFIX().getText());
         DynamicIpBgpPeerGroup pg = proc.addDynamicIpPeerGroup(prefix);
         pg.setGroupName(name);
         pg.setGroupNameLine(line);
         if (ctx.as != null) {
            int remoteAs = toInteger(ctx.as);
            pg.setRemoteAs(remoteAs);
         }
      }
      else if (ctx.IPV6_PREFIX() != null) {
         Prefix6 prefix6 = new Prefix6(ctx.IPV6_PREFIX().getText());
         DynamicIpv6BgpPeerGroup pg = proc.addDynamicIpv6PeerGroup(prefix6);
         pg.setGroupName(name);
         pg.setGroupNameLine(line);
         if (ctx.as != null) {
            int remoteAs = toInteger(ctx.as);
            pg.setRemoteAs(remoteAs);
         }
      }
   }

   @Override
   public void exitCluster_id_bgp_tail(Cluster_id_bgp_tailContext ctx) {
      Ip clusterId = null;
      if (ctx.DEC() != null) {
         long ipAsLong = Long.parseLong(ctx.DEC().getText());
         clusterId = new Ip(ipAsLong);
      }
      else if (ctx.IP_ADDRESS() != null) {
         clusterId = toIp(ctx.IP_ADDRESS());
      }
      _currentPeerGroup.setClusterId(clusterId);
   }

   @Override
   public void exitCmm_access_group(Cmm_access_groupContext ctx) {
      String name;
      int line;
      if (ctx.name != null) {
         name = ctx.name.getText();
         line = ctx.name.getStart().getLine();
      }
      else {
         name = ctx.num.getText();
         line = ctx.num.getLine();
      }
      _configuration.referenceStructure(CiscoStructureType.IP_ACCESS_LIST, name,
            CiscoStructureUsage.CLASS_MAP_ACCESS_GROUP, line);
      _configuration.getClassMapAccessGroups().add(name);
   }

   @Override
   public void exitContinue_rm_stanza(Continue_rm_stanzaContext ctx) {
      int statementLine = ctx.getStart().getLine();
      Integer target = null;
      if (ctx.DEC() != null) {
         target = toInteger(ctx.DEC());
      }
      RouteMapContinue continueLine = new RouteMapContinue(
            target,
            statementLine);
      _currentRouteMapClause.setContinueLine(continueLine);
   }

   @Override
   public void exitCp_ip_access_group(Cp_ip_access_groupContext ctx) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.getControlPlaneAccessGroups().add(name);
      _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
            name, CiscoStructureUsage.CONTROL_PLANE_ACCESS_GROUP, line);
   }

   @Override
   public void exitCrypto_map_ii_match_address(
         Crypto_map_ii_match_addressContext ctx) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.getCryptoAcls().add(name);
      _configuration.referenceStructure(CiscoStructureType.IP_ACCESS_LIST, name,
            CiscoStructureUsage.CRYPTO_MAP_IPSEC_ISAKMP_ACL, line);
   }

   @Override
   public void exitDefault_information_originate_rb_stanza(
         Default_information_originate_rb_stanzaContext ctx) {
      _currentPeerGroup.setDefaultOriginate(true);
   }

   @Override
   public void exitDefault_metric_bgp_tail(Default_metric_bgp_tailContext ctx) {
      int metric = toInteger(ctx.metric);
      _currentPeerGroup.setDefaultMetric(metric);
   }

   @Override
   public void exitDefault_originate_bgp_tail(
         Default_originate_bgp_tailContext ctx) {
      _currentPeerGroup.setDefaultOriginate(true);
      if (ctx.map != null) {
         String mapName = ctx.map.getText();
         int line = ctx.map.getStart().getLine();
         _currentPeerGroup.setDefaultOriginateMap(mapName);
         _currentPeerGroup.setDefaultOriginateMapLine(line);
      }
   }

   @Override
   public void exitDefault_shutdown_bgp_tail(
         Default_shutdown_bgp_tailContext ctx) {
      _currentPeerGroup.setShutdown(true);
   }

   @Override
   public void exitDescription_bgp_tail(Description_bgp_tailContext ctx) {
      String description = ctx.description_line().text.getText().trim();
      _currentPeerGroup.setDescription(description);
   }

   @Override
   public void exitDisable_peer_as_check_bgp_tail(
         Disable_peer_as_check_bgp_tailContext ctx) {
      _currentPeerGroup.setDisablePeerAsCheck(true);
   }

   @Override
   public void exitDistribute_list_bgp_tail(
         Distribute_list_bgp_tailContext ctx) {
      todo(ctx, F_BGP_NEIGHBOR_DISTRIBUTE_LIST);
   }

   @Override
   public void exitDomain_lookup(Domain_lookupContext ctx) {
      String ifaceName = ctx.iname.getText();
      String canonicalIfaceName = getCanonicalInterfaceName(ifaceName);
      _configuration.setDnsSourceInterface(canonicalIfaceName);
   }

   @Override
   public void exitDomain_name(Domain_nameContext ctx) {
      String domainName = ctx.hostname.getText();
      _configuration.setDomainName(domainName);
   }

   @Override
   public void exitDomain_name_server(Domain_name_serverContext ctx) {
      Set<String> dnsServers = _configuration.getDnsServers();
      String hostname = ctx.hostname.getText();
      dnsServers.add(hostname);
   }

   @Override
   public void exitEbgp_multihop_bgp_tail(Ebgp_multihop_bgp_tailContext ctx) {
      _currentPeerGroup.setEbgpMultihop(true);
   }

   @Override
   public void exitEmpty_neighbor_block_address_family(
         Empty_neighbor_block_address_familyContext ctx) {
      popPeer();
   }

   @Override
   public void exitExtended_access_list_stanza(
         Extended_access_list_stanzaContext ctx) {
      _currentExtendedAcl = null;
   }

   @Override
   public void exitExtended_access_list_tail(
         Extended_access_list_tailContext ctx) {

      LineAction action = toLineAction(ctx.ala);
      IpProtocol protocol = toIpProtocol(ctx.prot);
      Ip srcIp = getIp(ctx.srcipr);
      Ip srcWildcard = getWildcard(ctx.srcipr);
      Ip dstIp = getIp(ctx.dstipr);
      Ip dstWildcard = getWildcard(ctx.dstipr);
      String srcAddressGroup = getAddressGroup(ctx.srcipr);
      String dstAddressGroup = getAddressGroup(ctx.dstipr);
      List<SubRange> srcPortRanges = ctx.alps_src != null
            ? toPortRanges(ctx.alps_src) : Collections.<SubRange>emptyList();
      List<SubRange> dstPortRanges = ctx.alps_dst != null
            ? toPortRanges(ctx.alps_dst) : Collections.<SubRange>emptyList();
      Integer icmpType = null;
      Integer icmpCode = null;
      List<TcpFlags> tcpFlags = new ArrayList<>();
      Set<Integer> dscps = new TreeSet<>();
      Set<Integer> ecns = new TreeSet<>();
      Set<State> states = EnumSet.noneOf(State.class);
      for (Extended_access_list_additional_featureContext feature : ctx.features) {
         if (feature.ACK() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUseAck(true);
            alt.setAck(true);
            tcpFlags.add(alt);
         }
         if (feature.DSCP() != null) {
            int dscpType = toDscpType(feature.dscp_type());
            dscps.add(dscpType);
         }
         if (feature.ECE() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUseEce(true);
            alt.setEce(true);
            tcpFlags.add(alt);
         }
         if (feature.ECHO_REPLY() != null) {
            icmpType = IcmpType.ECHO_REPLY;
            icmpCode = IcmpCode.ECHO_REPLY;
         }
         if (feature.ECHO() != null) {
            icmpType = IcmpType.ECHO_REQUEST;
            icmpCode = IcmpCode.ECHO_REQUEST;
         }
         if (feature.ECN() != null) {
            int ecn = toInteger(feature.ecn);
            ecns.add(ecn);
         }
         if (feature.ESTABLISHED() != null) {
            // must contain ACK or RST
            TcpFlags alt1 = new TcpFlags();
            TcpFlags alt2 = new TcpFlags();
            alt1.setUseAck(true);
            alt1.setAck(true);
            alt2.setUseRst(true);
            alt2.setRst(true);
            tcpFlags.add(alt1);
            tcpFlags.add(alt2);
         }
         if (feature.FIN() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUseFin(true);
            alt.setFin(true);
            tcpFlags.add(alt);
         }
         if (feature.FRAGMENTS() != null) {
            todo(ctx, F_FRAGMENTS);
         }
         if (feature.HOST_UNKNOWN() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
            icmpCode = IcmpCode.DESTINATION_HOST_UNKNOWN;
         }
         if (feature.HOST_UNREACHABLE() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
            icmpCode = IcmpCode.DESTINATION_HOST_UNREACHABLE;
         }
         if (feature.NETWORK_UNKNOWN() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
            icmpCode = IcmpCode.DESTINATION_NETWORK_UNKNOWN;
         }
         if (feature.NET_UNREACHABLE() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
            icmpCode = IcmpCode.DESTINATION_NETWORK_UNREACHABLE;
         }
         if (feature.PACKET_TOO_BIG() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
            icmpCode = IcmpCode.PACKET_TOO_BIG;
         }
         if (feature.PARAMETER_PROBLEM() != null) {
            icmpType = IcmpType.PARAMETER_PROBLEM;
         }
         if (feature.PORT_UNREACHABLE() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
            icmpCode = IcmpCode.DESTINATION_PORT_UNREACHABLE;
         }
         if (feature.PSH() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUsePsh(true);
            alt.setPsh(true);
            tcpFlags.add(alt);
         }
         if (feature.REDIRECT() != null) {
            icmpType = IcmpType.REDIRECT_MESSAGE;
         }
         if (feature.RST() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUseRst(true);
            alt.setRst(true);
            tcpFlags.add(alt);
         }
         if (feature.SOURCE_QUENCH() != null) {
            icmpType = IcmpType.SOURCE_QUENCH;
            icmpCode = IcmpCode.SOURCE_QUENCH;
         }
         if (feature.SYN() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUseSyn(true);
            alt.setSyn(true);
            tcpFlags.add(alt);
         }
         if (feature.TIME_EXCEEDED() != null) {
            icmpType = IcmpType.TIME_EXCEEDED;
         }
         if (feature.TTL() != null) {
            todo(ctx, F_TTL);
         }
         if (feature.TTL_EXCEEDED() != null) {
            icmpType = IcmpType.TIME_EXCEEDED;
            icmpCode = IcmpCode.TTL_EXCEEDED;
         }
         if (feature.TRACEROUTE() != null) {
            icmpType = IcmpType.TRACEROUTE;
            icmpCode = IcmpCode.TRACEROUTE;
         }
         if (feature.TRACKED() != null) {
            states.add(State.ESTABLISHED);
         }
         if (feature.UNREACHABLE() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
         }
         if (feature.URG() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUseUrg(true);
            alt.setUrg(true);
            tcpFlags.add(alt);
         }
      }
      String name = getFullText(ctx).trim();
      ExtendedAccessListLine line = new ExtendedAccessListLine(name, action,
            protocol, new IpWildcard(srcIp, srcWildcard), srcAddressGroup,
            new IpWildcard(dstIp, dstWildcard), dstAddressGroup, srcPortRanges,
            dstPortRanges, dscps, ecns, icmpType, icmpCode, states, tcpFlags);
      _currentExtendedAcl.addLine(line);
   }

   @Override
   public void exitExtended_ipv6_access_list_stanza(
         Extended_ipv6_access_list_stanzaContext ctx) {
      _currentExtendedIpv6Acl = null;
   }

   @Override
   public void exitExtended_ipv6_access_list_tail(
         Extended_ipv6_access_list_tailContext ctx) {
      LineAction action = toLineAction(ctx.ala);
      IpProtocol protocol = toIpProtocol(ctx.prot);
      Ip6 srcIp = getIp(ctx.srcipr);
      Ip6 srcWildcard = getWildcard(ctx.srcipr);
      Ip6 dstIp = getIp(ctx.dstipr);
      Ip6 dstWildcard = getWildcard(ctx.dstipr);
      String srcAddressGroup = getAddressGroup(ctx.srcipr);
      String dstAddressGroup = getAddressGroup(ctx.dstipr);
      List<SubRange> srcPortRanges = ctx.alps_src != null
            ? toPortRanges(ctx.alps_src) : Collections.<SubRange>emptyList();
      List<SubRange> dstPortRanges = ctx.alps_dst != null
            ? toPortRanges(ctx.alps_dst) : Collections.<SubRange>emptyList();
      Integer icmpType = null;
      Integer icmpCode = null;
      List<TcpFlags> tcpFlags = new ArrayList<>();
      Set<Integer> dscps = new TreeSet<>();
      Set<Integer> ecns = new TreeSet<>();
      Set<State> states = EnumSet.noneOf(State.class);
      for (Extended_access_list_additional_featureContext feature : ctx.features) {
         if (feature.ACK() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUseAck(true);
            alt.setAck(true);
            tcpFlags.add(alt);
         }
         if (feature.DSCP() != null) {
            int dscpType = toDscpType(feature.dscp_type());
            dscps.add(dscpType);
         }
         if (feature.ECE() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUseEce(true);
            alt.setEce(true);
            tcpFlags.add(alt);
         }
         if (feature.ECHO_REPLY() != null) {
            icmpType = IcmpType.ECHO_REPLY;
            icmpCode = IcmpCode.ECHO_REPLY;
         }
         if (feature.ECHO() != null) {
            icmpType = IcmpType.ECHO_REQUEST;
            icmpCode = IcmpCode.ECHO_REQUEST;
         }
         if (feature.ECN() != null) {
            int ecn = toInteger(feature.ecn);
            ecns.add(ecn);
         }
         if (feature.ESTABLISHED() != null) {
            // must contain ACK or RST
            TcpFlags alt1 = new TcpFlags();
            TcpFlags alt2 = new TcpFlags();
            alt1.setUseAck(true);
            alt1.setAck(true);
            alt2.setUseRst(true);
            alt2.setRst(true);
            tcpFlags.add(alt1);
            tcpFlags.add(alt2);
         }
         if (feature.FIN() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUseFin(true);
            alt.setFin(true);
            tcpFlags.add(alt);
         }
         if (feature.FRAGMENTS() != null) {
            todo(ctx, F_FRAGMENTS);
         }
         if (feature.HOST_UNKNOWN() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
            icmpCode = IcmpCode.DESTINATION_HOST_UNKNOWN;
         }
         if (feature.HOST_UNREACHABLE() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
            icmpCode = IcmpCode.DESTINATION_HOST_UNREACHABLE;
         }
         if (feature.NETWORK_UNKNOWN() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
            icmpCode = IcmpCode.DESTINATION_NETWORK_UNKNOWN;
         }
         if (feature.NET_UNREACHABLE() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
            icmpCode = IcmpCode.DESTINATION_NETWORK_UNREACHABLE;
         }
         if (feature.PACKET_TOO_BIG() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
            icmpCode = IcmpCode.PACKET_TOO_BIG;
         }
         if (feature.PARAMETER_PROBLEM() != null) {
            icmpType = IcmpType.PARAMETER_PROBLEM;
         }
         if (feature.PORT_UNREACHABLE() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
            icmpCode = IcmpCode.DESTINATION_PORT_UNREACHABLE;
         }
         if (feature.PSH() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUsePsh(true);
            alt.setPsh(true);
            tcpFlags.add(alt);
         }
         if (feature.REDIRECT() != null) {
            icmpType = IcmpType.REDIRECT_MESSAGE;
         }
         if (feature.RST() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUseRst(true);
            alt.setRst(true);
            tcpFlags.add(alt);
         }
         if (feature.SOURCE_QUENCH() != null) {
            icmpType = IcmpType.SOURCE_QUENCH;
            icmpCode = IcmpCode.SOURCE_QUENCH;
         }
         if (feature.SYN() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUseSyn(true);
            alt.setSyn(true);
            tcpFlags.add(alt);
         }
         if (feature.TIME_EXCEEDED() != null) {
            icmpType = IcmpType.TIME_EXCEEDED;
         }
         if (feature.TTL() != null) {
            todo(ctx, F_TTL);
         }
         if (feature.TTL_EXCEEDED() != null) {
            icmpType = IcmpType.TIME_EXCEEDED;
            icmpCode = IcmpCode.TTL_EXCEEDED;
         }
         if (feature.TRACEROUTE() != null) {
            icmpType = IcmpType.TRACEROUTE;
            icmpCode = IcmpCode.TRACEROUTE;
         }
         if (feature.TRACKED() != null) {
            states.add(State.ESTABLISHED);
         }
         if (feature.UNREACHABLE() != null) {
            icmpType = IcmpType.DESTINATION_UNREACHABLE;
         }
         if (feature.URG() != null) {
            TcpFlags alt = new TcpFlags();
            alt.setUseUrg(true);
            alt.setUrg(true);
            tcpFlags.add(alt);
         }
      }
      String name = getFullText(ctx).trim();
      ExtendedIpv6AccessListLine line = new ExtendedIpv6AccessListLine(name,
            action, protocol, new Ip6Wildcard(srcIp, srcWildcard),
            srcAddressGroup, new Ip6Wildcard(dstIp, dstWildcard),
            dstAddressGroup, srcPortRanges, dstPortRanges, dscps, ecns,
            icmpType, icmpCode, states, tcpFlags);
      _currentExtendedIpv6Acl.addLine(line);
   }

   @Override
   public void exitFailover_interface(Failover_interfaceContext ctx) {
      String name = ctx.name.getText();
      Ip primaryIp = toIp(ctx.pip);
      Ip primaryMask = toIp(ctx.pmask);
      Ip standbyIp = toIp(ctx.sip);
      Prefix primaryPrefix = new Prefix(primaryIp, primaryMask);
      Prefix standbyPrefix = new Prefix(standbyIp, primaryMask);
      _configuration.getFailoverPrimaryPrefixes().put(name, primaryPrefix);
      _configuration.getFailoverStandbyPrefixes().put(name, standbyPrefix);
   }

   @Override
   public void exitFailover_link(Failover_linkContext ctx) {
      String alias = ctx.name.getText();
      String ifaceName = ctx.iface.getText();
      _configuration.getFailoverInterfaces().put(alias, ifaceName);
      _configuration.setFailoverStatefulSignalingInterfaceAlias(alias);
      _configuration.setFailoverStatefulSignalingInterface(ifaceName);
   }

   @Override
   public void exitFlan_interface(Flan_interfaceContext ctx) {
      String alias = ctx.name.getText();
      String ifaceName = ctx.iface.getText();
      _configuration.getFailoverInterfaces().put(alias, ifaceName);
      _configuration.setFailoverCommunicationInterface(ifaceName);
      _configuration.setFailoverCommunicationInterfaceAlias(alias);
   }

   @Override
   public void exitFlan_unit(Flan_unitContext ctx) {
      if (ctx.PRIMARY() != null) {
         _configuration.setFailoverSecondary(false);
      }
      else if (ctx.SECONDARY() != null) {
         _configuration.setFailoverSecondary(true);
         _configuration.setHostname(
               _configuration.getHostname() + "-FAILOVER-SECONDARY");
      }
      _configuration.setFailover(true);
   }

   @Override
   public void exitIf_autostate(If_autostateContext ctx) {
      if (ctx.NO() != null) {
         for (Interface currentInterface : _currentInterfaces) {
            currentInterface.setAutoState(false);
         }
      }
   }

   @Override
   public void exitIf_ip_access_group(If_ip_access_groupContext ctx) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      if (ctx.IN() != null || ctx.INGRESS() != null) {
         for (Interface currentInterface : _currentInterfaces) {
            currentInterface.setIncomingFilter(name);
            currentInterface.setIncomingFilterLine(line);
         }
      }
      else if (ctx.OUT() != null || ctx.EGRESS() != null) {
         for (Interface currentInterface : _currentInterfaces) {
            currentInterface.setOutgoingFilter(name);
            currentInterface.setOutgoingFilterLine(line);
         }
      }
      else {
         throw new BatfishException("bad direction");
      }
   }

   @Override
   public void exitIf_ip_address(If_ip_addressContext ctx) {
      Prefix prefix;
      if (ctx.prefix != null) {
         prefix = new Prefix(ctx.prefix.getText());
      }
      else {
         Ip address = new Ip(ctx.ip.getText());
         Ip mask = new Ip(ctx.subnet.getText());
         prefix = new Prefix(address, mask);
      }
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setPrefix(prefix);
      }
      if (ctx.STANDBY() != null) {
         Ip standbyAddress = toIp(ctx.standby_address);
         Prefix standbyPrefix = new Prefix(
               standbyAddress,
               prefix.getPrefixLength());
         for (Interface currentInterface : _currentInterfaces) {
            currentInterface.setStandbyPrefix(standbyPrefix);
         }
      }
   }

   @Override
   public void exitIf_ip_address_secondary(If_ip_address_secondaryContext ctx) {
      Ip address;
      Ip mask;
      Prefix prefix;
      if (ctx.prefix != null) {
         prefix = new Prefix(ctx.prefix.getText());
      }
      else {
         address = new Ip(ctx.ip.getText());
         mask = new Ip(ctx.subnet.getText());
         prefix = new Prefix(address, mask.numSubnetBits());
      }
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.getSecondaryPrefixes().add(prefix);
      }
   }

   @Override
   public void exitIf_ip_helper_address(If_ip_helper_addressContext ctx) {
      for (Interface iface : _currentInterfaces) {
         Ip dhcpRelayAddress = toIp(ctx.address);
         iface.setDhcpRelayAddress(dhcpRelayAddress);
      }
   }

   @Override
   public void exitIf_ip_nat_destination(If_ip_nat_destinationContext ctx) {
      String acl = ctx.acl.getText();
      int line = ctx.acl.getStart().getLine();
      _configuration.getIpNatDestinationAccessLists().add(acl);
      _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
            acl, CiscoStructureUsage.IP_NAT_DESTINATION_ACCESS_LIST, line);
   }

   @Override
   public void exitIf_ip_nat_source(If_ip_nat_sourceContext ctx) {
      for (Interface iface : _currentInterfaces) {
         iface.setSourceNat(true);
         String acl = ctx.acl.getText();
         if (ctx.acl != null) {
            int aclLine = ctx.acl.getStart().getLine();
            iface.setSourceNatAcl(acl);
            iface.setSourceNatAclLine(aclLine);
         }
         if (ctx.pool != null) {
            String pool = ctx.pool.getText();
            int poolLine = ctx.pool.getStart().getLine();
            iface.setSourceNatPool(pool);
            iface.setSourceNatPoolLine(poolLine);
         }
      }
   }

   @Override
   public void exitIf_ip_ospf_area(If_ip_ospf_areaContext ctx) {
      long area = toInteger(ctx.area);
      for (Interface iface : _currentInterfaces) {
         iface.setOspfArea(area);
      }
   }

   @Override
   public void exitIf_ip_ospf_cost(If_ip_ospf_costContext ctx) {
      int cost = toInteger(ctx.cost);
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setOspfCost(cost);
      }
   }

   @Override
   public void exitIf_ip_ospf_dead_interval(
         If_ip_ospf_dead_intervalContext ctx) {
      int seconds = toInteger(ctx.seconds);
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setOspfDeadInterval(seconds);
         currentInterface.setOspfHelloMultiplier(0);
      }
   }

   @Override
   public void exitIf_ip_ospf_dead_interval_minimal(
         If_ip_ospf_dead_interval_minimalContext ctx) {
      int multiplier = toInteger(ctx.mult);
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setOspfDeadInterval(1);
         currentInterface.setOspfHelloMultiplier(multiplier);
      }
   }

   @Override
   public void exitIf_ip_ospf_passive_interface(
         If_ip_ospf_passive_interfaceContext ctx) {
      boolean active = ctx.NO() != null;
      if (active) {
         for (Interface iface : _currentInterfaces) {
            iface.setOspfActive(true);
         }
      }
      else {
         for (Interface iface : _currentInterfaces) {
            iface.setOspfPassive(true);
         }
      }
   }

   @Override
   public void exitIf_ip_pim_neighbor_filter(
         If_ip_pim_neighbor_filterContext ctx) {
      String acl = ctx.acl.getText();
      int line = ctx.acl.getStart().getLine();
      _configuration.getIpPimNeighborFilters().add(acl);
      _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
            acl, CiscoStructureUsage.INTERFACE_PIM_NEIGHBOR_FILTER, line);
   }

   @Override
   public void exitIf_ip_policy(If_ip_policyContext ctx) {
      String policyName = ctx.name.getText();
      int policyLine = ctx.name.getLine();
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setRoutingPolicy(policyName);
         currentInterface.setRoutingPolicyLine(policyLine);
      }
   }

   @Override
   public void exitIf_ip_proxy_arp(If_ip_proxy_arpContext ctx) {
      boolean enabled = ctx.NO() == null;
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setProxyArp(enabled);
      }
   }

   @Override
   public void exitIf_ip_router_isis(If_ip_router_isisContext ctx) {
      for (Interface iface : _currentInterfaces) {
         iface.setIsisInterfaceMode(IsisInterfaceMode.ACTIVE);
      }
   }

   @Override
   public void exitIf_ip_router_ospf_area(If_ip_router_ospf_areaContext ctx) {
      long area = new Ip(ctx.area.getText()).asLong();
      for (Interface iface : _currentInterfaces) {
         iface.setOspfArea(area);
      }
   }

   @Override
   public void exitIf_ip_verify(If_ip_verifyContext ctx) {
      if (ctx.acl != null) {
         String acl = ctx.acl.getText();
         int line = ctx.acl.getLine();
         _configuration.getVerifyAccessLists().add(acl);
         _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
               acl, CiscoStructureUsage.INTERFACE_IP_VERIFY_ACCESS_LIST, line);
      }
   }

   @Override
   public void exitIf_isis_metric(If_isis_metricContext ctx) {
      int metric = toInteger(ctx.metric);
      for (Interface iface : _currentInterfaces) {
         iface.setIsisCost(metric);
      }
   }

   @Override
   public void exitIf_mtu(If_mtuContext ctx) {
      int mtu = toInteger(ctx.DEC());
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setMtu(mtu);
      }
   }

   @Override
   public void exitIf_shutdown(If_shutdownContext ctx) {
      if (ctx.NO() == null) {
         for (Interface currentInterface : _currentInterfaces) {
            currentInterface.setActive(false);
         }
      }
   }

   @Override
   public void exitIf_spanning_tree(If_spanning_treeContext ctx) {
      _no = false;
   }

   @Override
   public void exitIf_st_portfast(If_st_portfastContext ctx) {
      if (!_no) {
         boolean spanningTreePortfast = ctx.disable == null;
         for (Interface iface : _currentInterfaces) {
            iface.setSpanningTreePortfast(spanningTreePortfast);
         }
      }
   }

   @Override
   public void exitIf_switchport(If_switchportContext ctx) {
      if (ctx.NO() != null) {
         for (Interface iface : _currentInterfaces) {
            iface.setSwitchportMode(SwitchportMode.NONE);
            iface.setSwitchport(false);
         }
      }
      else {
         for (Interface iface : _currentInterfaces) {
            iface.setSwitchportMode(SwitchportMode.ACCESS);
            iface.setSwitchport(true);
         }
      }
   }

   @Override
   public void exitIf_switchport_access(If_switchport_accessContext ctx) {
      if (ctx.vlan != null) {
         int vlan = toInteger(ctx.vlan);
         for (Interface currentInterface : _currentInterfaces) {
            currentInterface.setSwitchportMode(SwitchportMode.ACCESS);
            currentInterface.setAccessVlan(vlan);
         }
      }
      else {
         for (Interface currentInterface : _currentInterfaces) {
            currentInterface.setSwitchportMode(SwitchportMode.ACCESS);
            currentInterface.setSwitchportAccessDynamic(true);
         }
      }
   }

   @Override
   public void exitIf_switchport_mode(If_switchport_modeContext ctx) {
      SwitchportMode mode;
      if (ctx.ACCESS() != null) {
         mode = SwitchportMode.ACCESS;
      }
      else if (ctx.DOT1Q_TUNNEL() != null) {
         mode = SwitchportMode.DOT1Q_TUNNEL;
      }
      else if (ctx.DYNAMIC() != null && ctx.AUTO() != null) {
         mode = SwitchportMode.DYNAMIC_AUTO;
      }
      else if (ctx.DYNAMIC() != null && ctx.DESIRABLE() != null) {
         mode = SwitchportMode.DYNAMIC_DESIRABLE;
      }
      else if (ctx.FEX_FABRIC() != null) {
         mode = SwitchportMode.FEX_FABRIC;
      }
      else if (ctx.TAP() != null) {
         mode = SwitchportMode.TAP;
      }
      else if (ctx.TRUNK() != null) {
         mode = SwitchportMode.TRUNK;
      }
      else if (ctx.TOOL() != null) {
         mode = SwitchportMode.TOOL;
      }
      else {
         throw new BatfishException("Unhandled switchport mode");
      }
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setSwitchportMode(mode);
      }
   }

   @Override
   public void exitIf_switchport_trunk_allowed(
         If_switchport_trunk_allowedContext ctx) {
      List<SubRange> ranges = toRange(ctx.r);
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.addAllowedRanges(ranges);
      }
   }

   @Override
   public void exitIf_switchport_trunk_encapsulation(
         If_switchport_trunk_encapsulationContext ctx) {
      SwitchportEncapsulationType type = toEncapsulation(ctx.e);
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setSwitchportTrunkEncapsulation(type);
      }
   }

   @Override
   public void exitIf_switchport_trunk_native(
         If_switchport_trunk_nativeContext ctx) {
      int vlan = toInteger(ctx.vlan);
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setNativeVlan(vlan);
      }
   }

   @Override
   public void exitIf_vrf_forwarding(If_vrf_forwardingContext ctx) {
      String name = ctx.name.getText();
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setVrf(name);
         initVrf(name);
      }
   }

   @Override
   public void exitIf_vrf_member(If_vrf_memberContext ctx) {
      String name = ctx.name.getText();
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setVrf(name);
         initVrf(name);
      }
   }

   @Override
   public void exitIfdhcpr_address(Ifdhcpr_addressContext ctx) {
      for (Interface iface : _currentInterfaces) {
         Ip address = toIp(ctx.address);
         iface.setDhcpRelayAddress(address);
      }
   }

   @Override
   public void exitIfigmpsg_acl(Ifigmpsg_aclContext ctx) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.getIgmpAcls().add(name);
      _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
            name, CiscoStructureUsage.INTERFACE_IGMP_STATIC_GROUP_ACL, line);
   }

   @Override
   public void exitInherit_peer_policy_bgp_tail(
         Inherit_peer_policy_bgp_tailContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      String groupName = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      if (_currentIpPeerGroup != null) {
         _currentIpPeerGroup.setGroupName(groupName);
         _currentIpPeerGroup.setGroupNameLine(line);
      }
      else if (_currentNamedPeerGroup != null) {
         // May not hit this since parser for peer-policy does not have
         // recursion.
         _currentNamedPeerGroup.setGroupName(groupName);
         _currentNamedPeerGroup.setGroupNameLine(line);
      }
      else if (_currentPeerGroup == proc.getMasterBgpPeerGroup()) {
         throw new BatfishException("Invalid peer context for inheritance");
      }
      else {
         todo(ctx, F_BGP_INHERIT_PEER_SESSION_OTHER);
      }
   }

   @Override
   public void exitInherit_peer_session_bgp_tail(
         Inherit_peer_session_bgp_tailContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      String groupName = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      if (_currentIpPeerGroup != null) {
         _currentIpPeerGroup.setPeerSession(groupName);
         _currentIpPeerGroup.setPeerSessionLine(line);
      }
      else if (_currentNamedPeerGroup != null) {
         _currentNamedPeerGroup.setPeerSession(groupName);
         _currentNamedPeerGroup.setPeerSessionLine(line);
      }
      else if (_currentPeerGroup == proc.getMasterBgpPeerGroup()) {
         throw new BatfishException("Invalid peer context for inheritance");
      }
      else {
         todo(ctx, F_BGP_INHERIT_PEER_SESSION_OTHER);
      }
   }

   @Override
   public void exitInterface_is_stanza(Interface_is_stanzaContext ctx) {
      _currentIsisInterface = null;
   }

   @Override
   public void exitIp_as_path_access_list_stanza(
         Ip_as_path_access_list_stanzaContext ctx) {
      _currentAsPathAcl = null;
   }

   @Override
   public void exitIp_as_path_access_list_tail(
         Ip_as_path_access_list_tailContext ctx) {
      LineAction action = toLineAction(ctx.action);
      String regex = ctx.as_path_regex.getText().trim();
      IpAsPathAccessListLine line = new IpAsPathAccessListLine(action, regex);
      _currentAsPathAcl.addLine(line);
   }

   @Override
   public void exitIp_community_list_expanded_stanza(
         Ip_community_list_expanded_stanzaContext ctx) {
      _currentExpandedCommunityList = null;
   }

   @Override
   public void exitIp_community_list_expanded_tail(
         Ip_community_list_expanded_tailContext ctx) {
      LineAction action = toLineAction(ctx.ala);
      String regex = "";
      for (Token remainder : ctx.remainder) {
         regex += remainder.getText();
      }
      ExpandedCommunityListLine line = new ExpandedCommunityListLine(
            action,
            regex);
      _currentExpandedCommunityList.addLine(line);
   }

   @Override
   public void exitIp_community_list_standard_stanza(
         Ip_community_list_standard_stanzaContext ctx) {
      _currentStandardCommunityList = null;
   }

   @Override
   public void exitIp_community_list_standard_tail(
         Ip_community_list_standard_tailContext ctx) {
      LineAction action = toLineAction(ctx.ala);
      List<Long> communities = new ArrayList<>();
      for (CommunityContext communityCtx : ctx.communities) {
         long community = toLong(communityCtx);
         communities.add(community);
      }
      StandardCommunityListLine line = new StandardCommunityListLine(
            action,
            communities);
      _currentStandardCommunityList.getLines().add(line);
   }

   @Override
   public void exitIp_default_gateway_stanza(
         Ip_default_gateway_stanzaContext ctx) {
      todo(ctx, F_IP_DEFAULT_GATEWAY);
   }

   @Override
   public void exitIp_domain_lookup(Ip_domain_lookupContext ctx) {
      if (ctx.iname != null) {
         String ifaceName = ctx.iname.getText();
         String canonicalIfaceName = getCanonicalInterfaceName(ifaceName);
         _configuration.setDnsSourceInterface(canonicalIfaceName);
      }
   }

   @Override
   public void exitIp_domain_name(Ip_domain_nameContext ctx) {
      if (!_no) {
         String domainName = ctx.hostname.getText();
         _configuration.setDomainName(domainName);
      }
      else {
         _configuration.setDomainName(null);
      }

   }

   @Override
   public void exitIp_nat_pool(Ip_nat_poolContext ctx) {
      _currentNatPool = null;
   }

   @Override
   public void exitIp_nat_pool_range(Ip_nat_pool_rangeContext ctx) {
      Ip first = new Ip(ctx.first.getText());
      _currentNatPool.setFirst(first);
      Ip last = new Ip(ctx.last.getText());
      _currentNatPool.setLast(last);
   }

   @Override
   public void exitIp_prefix_list_stanza(Ip_prefix_list_stanzaContext ctx) {
      _currentPrefixList = null;
   }

   @Override
   public void exitIp_prefix_list_tail(Ip_prefix_list_tailContext ctx) {
      LineAction action = toLineAction(ctx.action);
      Prefix prefix = new Prefix(ctx.prefix.getText());
      int prefixLength = prefix.getPrefixLength();
      int minLen = prefixLength;
      int maxLen = prefixLength;
      if (ctx.minpl != null) {
         minLen = toInteger(ctx.minpl);
         maxLen = Prefix.MAX_PREFIX_LENGTH;
      }
      if (ctx.maxpl != null) {
         maxLen = toInteger(ctx.maxpl);
      }
      if (ctx.eqpl != null) {
         minLen = toInteger(ctx.eqpl);
         maxLen = toInteger(ctx.eqpl);
      }
      SubRange lengthRange = new SubRange(minLen, maxLen);
      PrefixListLine line = new PrefixListLine(action, prefix, lengthRange);
      _currentPrefixList.addLine(line);
   }

   @Override
   public void exitIp_route_stanza(Ip_route_stanzaContext ctx) {
      if (ctx.vrf != null || ctx.MANAGEMENT() != null) {
         _currentVrf = Configuration.DEFAULT_VRF_NAME;
      }
   }

   @Override
   public void exitIp_route_tail(Ip_route_tailContext ctx) {
      Prefix prefix;
      if (ctx.prefix != null) {
         prefix = new Prefix(ctx.prefix.getText());
      }
      else {
         Ip address = toIp(ctx.address);
         Ip mask = toIp(ctx.mask);
         int prefixLength = mask.numSubnetBits();
         prefix = new Prefix(address, prefixLength);
      }
      Ip nextHopIp = Route.UNSET_ROUTE_NEXT_HOP_IP;
      String nextHopInterface = null;
      int distance = DEFAULT_STATIC_ROUTE_DISTANCE;
      Integer tag = null;
      Integer track = null;
      boolean permanent = ctx.perm != null;
      if (ctx.nexthopip != null) {
         nextHopIp = toIp(ctx.nexthopip);
      }
      else if (ctx.nexthopprefix != null) {
         Prefix nextHopPrefix = new Prefix(ctx.nexthopprefix.getText());
         nextHopIp = nextHopPrefix.getAddress();
      }
      if (ctx.nexthopint != null) {
         nextHopInterface = getCanonicalInterfaceName(ctx.nexthopint.getText());
      }
      if (ctx.distance != null) {
         distance = toInteger(ctx.distance);
      }
      if (ctx.tag != null) {
         tag = toInteger(ctx.tag);
      }
      if (ctx.track != null) {
         track = toInteger(ctx.track);
      }
      StaticRoute route = new StaticRoute(prefix, nextHopIp, nextHopInterface,
            distance, tag, track, permanent);
      currentVrf().getStaticRoutes().add(route);
   }

   @Override
   public void exitIp_ssh_version(Ip_ssh_versionContext ctx) {
      int version = toInteger(ctx.version);
      if (version < 1 || version > 2) {
         throw new BatfishException("Invalid ssh version: " + version);
      }
      _configuration.getCf().getSsh().setVersion(version);
   }

   @Override
   public void exitIpv6_prefix_list_stanza(Ipv6_prefix_list_stanzaContext ctx) {
      _currentPrefix6List = null;
   }

   @Override
   public void exitIpv6_prefix_list_tail(Ipv6_prefix_list_tailContext ctx) {
      LineAction action = toLineAction(ctx.action);
      Prefix6 prefix6 = new Prefix6(ctx.prefix6.getText());
      int prefixLength = prefix6.getPrefixLength();
      int minLen = prefixLength;
      int maxLen = prefixLength;
      if (ctx.minpl != null) {
         minLen = toInteger(ctx.minpl);
         maxLen = Prefix6.MAX_PREFIX_LENGTH;
      }
      if (ctx.maxpl != null) {
         maxLen = toInteger(ctx.maxpl);
      }
      if (ctx.eqpl != null) {
         minLen = toInteger(ctx.eqpl);
         maxLen = toInteger(ctx.eqpl);
      }
      SubRange lengthRange = new SubRange(minLen, maxLen);
      Prefix6ListLine line = new Prefix6ListLine(action, prefix6, lengthRange);
      _currentPrefix6List.addLine(line);
   }

   @Override
   public void exitL_access_class(L_access_classContext ctx) {
      boolean ipv6 = (ctx.IPV6() != null);
      String name = ctx.name.getText();
      int nameLine = ctx.name.getStart().getLine();
      BiConsumer<Line, String> setter;
      CiscoStructureType structureType;
      CiscoStructureUsage structureUsage;
      if (ctx.OUT() != null || ctx.EGRESS() != null) {
         if (ipv6) {
            setter = Line::setOutputIpv6AccessList;
            structureType = CiscoStructureType.IPV6_ACCESS_LIST;
            structureUsage = CiscoStructureUsage.LINE_ACCESS_CLASS_LIST6;
            _configuration.getLineIpv6AccessClassLists().add(name);
         }
         else {
            setter = Line::setOutputAccessList;
            structureType = CiscoStructureType.IP_ACCESS_LIST;
            structureUsage = CiscoStructureUsage.LINE_ACCESS_CLASS_LIST;
            _configuration.getLineAccessClassLists().add(name);
         }

      }
      else {
         if (ipv6) {
            setter = Line::setInputIpv6AccessList;
            structureType = CiscoStructureType.IPV6_ACCESS_LIST;
            structureUsage = CiscoStructureUsage.LINE_ACCESS_CLASS_LIST6;
            _configuration.getLineIpv6AccessClassLists().add(name);
         }
         else {
            setter = Line::setInputAccessList;
            structureType = CiscoStructureType.IP_ACCESS_LIST;
            structureUsage = CiscoStructureUsage.LINE_ACCESS_CLASS_LIST;
            _configuration.getLineAccessClassLists().add(name);
         }
      }
      _configuration.referenceStructure(structureType, name, structureUsage,
            nameLine);
      for (String currentName : _currentLineNames) {
         Line line = _configuration.getCf().getLines().get(currentName);
         setter.accept(line, name);
      }
   }

   @Override
   public void exitL_exec_timeout(L_exec_timeoutContext ctx) {
      int minutes = toInteger(ctx.minutes);
      int seconds = ctx.seconds != null ? toInteger(ctx.seconds) : 0;
      for (String lineName : _currentLineNames) {
         Line line = _configuration.getCf().getLines().get(lineName);
         line.setExecTimeoutMinutes(minutes);
         line.setExecTimeoutSeconds(seconds);
      }
   }

   @Override
   public void exitL_login_authentication(L_login_authenticationContext ctx) {
      String list;
      if (ctx.DEFAULT() != null) {
         list = ctx.DEFAULT().getText();
      }
      else if (ctx.name != null) {
         list = ctx.name.getText();
      }
      else {
         throw new BatfishException("Invalid list name");
      }
      for (String line : _currentLineNames) {
         _configuration.getCf().getLines().get(line)
               .setLoginAuthentication(list);
      }
   }

   @Override
   public void exitL_transport(L_transportContext ctx) {
      SortedSet<String> protocols = new TreeSet<>(ctx.prot.stream()
            .map(c -> c.getText()).collect(Collectors.toSet()));
      BiConsumer<Line, SortedSet<String>> setter;
      if (ctx.INPUT() != null) {
         setter = Line::setTransportInput;
      }
      else if (ctx.OUTPUT() != null) {
         setter = Line::setTransportOutput;
      }
      else if (ctx.PREFERRED() != null) {
         setter = Line::setTransportPreferred;
      }
      else {
         throw new BatfishException(
               "Invalid or unsupported line transport type");
      }
      for (String currentName : _currentLineNames) {
         Line line = _configuration.getCf().getLines().get(currentName);
         setter.accept(line, protocols);
      }
   }

   @Override
   public void exitLogging_buffered(Logging_bufferedContext ctx) {
      if (_no) {
         return;
      }
      Integer size = null;
      Integer severityNum = null;
      String severity = null;
      if (ctx.size != null) {
         // something was parsed as buffer size but it could be logging severity
         // as well
         // it is buffer size if the value is greater than min buffer size
         // otherwise, it is logging severity
         int sizeRawNum = toInteger(ctx.size);
         if (sizeRawNum > Logging.MAX_LOGGING_SEVERITY) {
            size = sizeRawNum;
         }
         else {
            if (ctx.logging_severity() != null) {
               // if we have explicity severity as well; we've messed up
               throw new BatfishException(
                     "Ambiguous parsing of logging buffered");
            }
            severityNum = sizeRawNum;
            severity = toLoggingSeverity(severityNum);
         }
      }
      else if (ctx.logging_severity() != null) {
         severityNum = toLoggingSeverityNum(ctx.logging_severity());
         severity = toLoggingSeverity(ctx.logging_severity());
      }
      Logging logging = _configuration.getCf().getLogging();
      Buffered buffered = logging.getBuffered();
      if (buffered == null) {
         buffered = new Buffered();
         logging.setBuffered(buffered);
      }
      buffered.setSeverity(severity);
      buffered.setSeverityNum(severityNum);
      buffered.setSize(size);
   }

   @Override
   public void exitLogging_console(Logging_consoleContext ctx) {
      if (_no) {
         return;
      }
      Integer severityNum = null;
      String severity = null;
      if (ctx.logging_severity() != null) {
         severityNum = toLoggingSeverityNum(ctx.logging_severity());
         severity = toLoggingSeverity(ctx.logging_severity());
      }
      Logging logging = _configuration.getCf().getLogging();
      LoggingType console = logging.getConsole();
      if (console == null) {
         console = new LoggingType();
         logging.setConsole(console);
      }
      console.setSeverity(severity);
      console.setSeverityNum(severityNum);
   }

   @Override
   public void exitLogging_host(Logging_hostContext ctx) {
      if (_no) {
         return;
      }
      Logging logging = _configuration.getCf().getLogging();
      String hostname = ctx.hostname.getText();
      LoggingHost host = new LoggingHost(hostname);
      logging.getHosts().put(hostname, host);
   }

   @Override
   public void exitLogging_on(Logging_onContext ctx) {
      Logging logging = _configuration.getCf().getLogging();
      logging.setOn(!_no);
   }

   @Override
   public void exitLogging_server(Logging_serverContext ctx) {
      if (_no) {
         return;
      }
      Logging logging = _configuration.getCf().getLogging();
      String hostname = ctx.hostname.getText();
      LoggingHost host = new LoggingHost(hostname);
      logging.getHosts().put(hostname, host);
   }

   @Override
   public void exitLogging_source_interface(
         Logging_source_interfaceContext ctx) {
      if (_no) {
         return;
      }
      Logging logging = _configuration.getCf().getLogging();
      String sourceInterface = toInterfaceName(ctx.interface_name());
      logging.setSourceInterface(sourceInterface);
   }

   @Override
   public void exitLogging_trap(Logging_trapContext ctx) {
      if (_no) {
         return;
      }
      Integer severityNum = null;
      String severity = null;
      if (ctx.logging_severity() != null) {
         severityNum = toLoggingSeverityNum(ctx.logging_severity());
         severity = toLoggingSeverity(ctx.logging_severity());
      }
      Logging logging = _configuration.getCf().getLogging();
      LoggingType trap = logging.getTrap();
      if (trap == null) {
         trap = new LoggingType();
         logging.setTrap(trap);
      }
      trap.setSeverity(severity);
      trap.setSeverityNum(severityNum);
   }

   @Override
   public void exitMatch_as_path_access_list_rm_stanza(
         Match_as_path_access_list_rm_stanzaContext ctx) {
      int statementLine = ctx.getStart().getLine();
      Set<String> names = new TreeSet<>();
      for (VariableContext name : ctx.name_list) {
         names.add(name.getText());
      }
      RouteMapMatchAsPathAccessListLine line = new RouteMapMatchAsPathAccessListLine(
            names, statementLine);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMatch_community_list_rm_stanza(
         Match_community_list_rm_stanzaContext ctx) {
      int statementLine = ctx.getStart().getLine();
      Set<String> names = new TreeSet<>();
      for (VariableContext name : ctx.name_list) {
         names.add(name.getText());
      }
      RouteMapMatchCommunityListLine line = new RouteMapMatchCommunityListLine(
            names, statementLine);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMatch_ip_access_list_rm_stanza(
         Match_ip_access_list_rm_stanzaContext ctx) {
      int statementLine = ctx.getStart().getLine();
      Set<String> names = new TreeSet<>();
      for (Variable_access_listContext v : ctx.name_list) {
         names.add(v.getText());
      }
      RouteMapMatchIpAccessListLine line = new RouteMapMatchIpAccessListLine(
            names, statementLine);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMatch_ip_prefix_list_rm_stanza(
         Match_ip_prefix_list_rm_stanzaContext ctx) {
      int statementLine = ctx.getStart().getLine();
      Set<String> names = new TreeSet<>();
      for (VariableContext t : ctx.name_list) {
         names.add(t.getText());
      }
      RouteMapMatchIpPrefixListLine line = new RouteMapMatchIpPrefixListLine(
            names, statementLine);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMatch_ipv6_access_list_rm_stanza(
         Match_ipv6_access_list_rm_stanzaContext ctx) {
      int statementLine = ctx.getStart().getLine();
      Set<String> names = new TreeSet<>();
      for (Variable_access_listContext v : ctx.name_list) {
         names.add(v.getText());
      }
      RouteMapMatchIpv6AccessListLine line = new RouteMapMatchIpv6AccessListLine(
            names, statementLine);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMatch_ipv6_prefix_list_rm_stanza(
         Match_ipv6_prefix_list_rm_stanzaContext ctx) {
      int statementLine = ctx.getStart().getLine();
      Set<String> names = new TreeSet<>();
      for (VariableContext t : ctx.name_list) {
         names.add(t.getText());
      }
      RouteMapMatchIpv6PrefixListLine line = new RouteMapMatchIpv6PrefixListLine(
            names, statementLine);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMatch_tag_rm_stanza(Match_tag_rm_stanzaContext ctx) {
      Set<Integer> tags = new TreeSet<>();
      for (Token t : ctx.tag_list) {
         tags.add(toInteger(t));
      }
      RouteMapMatchTagLine line = new RouteMapMatchTagLine(tags);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMaximum_paths_bgp_tail(Maximum_paths_bgp_tailContext ctx) {
      int maximumPaths = toInteger(ctx.paths);
      BgpProcess proc = currentVrf().getBgpProcess();
      if (ctx.EBGP() != null) {
         proc.setMaximumPathsEbgp(maximumPaths);
      }
      else if (ctx.IBGP() != null) {
         proc.setMaximumPathsIbgp(maximumPaths);
      }
      else if (ctx.EIBGP() != null) {
         proc.setMaximumPathsEibgp(maximumPaths);
      }
      else {
         proc.setMaximumPaths(maximumPaths);
      }
   }

   @Override
   public void exitMaximum_peers_bgp_tail(Maximum_peers_bgp_tailContext ctx) {
      todo(ctx, F_BGP_MAXIMUM_PEERS);
   }

   @Override
   public void exitMgmt_ip_access_group(Mgmt_ip_access_groupContext ctx) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.getManagementAccessGroups().add(name);
      _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
            name, CiscoStructureUsage.MANAGEMENT_ACCESS_GROUP, line);
   }

   @Override
   public void exitNeighbor_block_address_family(
         Neighbor_block_address_familyContext ctx) {
      if (_inBlockNeighbor) {
         popPeer();
      }
      else {
         _currentBlockNeighborAddressFamilies.clear();
      }
   }

   @Override
   public void exitNeighbor_block_inherit(Neighbor_block_inheritContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      String groupName = ctx.name.getText();
      if (_currentIpPeerGroup != null) {
         _currentIpPeerGroup.setGroupName(groupName);
      }
      else if (_currentIpv6PeerGroup != null) {
         _currentIpv6PeerGroup.setGroupName(groupName);
      }
      else if (_currentDynamicIpPeerGroup != null) {
         _currentDynamicIpPeerGroup.setGroupName(groupName);
      }
      else if (_currentDynamicIpv6PeerGroup != null) {
         _currentDynamicIpv6PeerGroup.setGroupName(groupName);
      }
      else if (_currentPeerGroup == proc.getMasterBgpPeerGroup()) {
         throw new BatfishException("Invalid peer context for inheritance");
      }
      else {
         todo(ctx, F_BGP_INHERIT_PEER_OTHER);
      }
   }

   @Override
   public void exitNeighbor_block_rb_stanza(
         Neighbor_block_rb_stanzaContext ctx) {
      resetPeerGroups();
      if (_inBlockNeighbor) {
         popPeer();
      }
      else {
         _inBlockNeighbor = false;
      }
   }

   @Override
   public void exitNeighbor_flat_rb_stanza(Neighbor_flat_rb_stanzaContext ctx) {
      resetPeerGroups();
      popPeer();
   }

   @Override
   public void exitNeighbor_group_rb_stanza(
         Neighbor_group_rb_stanzaContext ctx) {
      resetPeerGroups();
      popPeer();
   }

   @Override
   public void exitNetwork_bgp_tail(Network_bgp_tailContext ctx) {
      Prefix prefix;
      if (ctx.prefix != null) {
         prefix = new Prefix(ctx.prefix.getText());
      }
      else {
         Ip address = toIp(ctx.ip);
         Ip mask = (ctx.mask != null) ? toIp(ctx.mask) : address.getClassMask();
         int prefixLength = mask.numSubnetBits();
         prefix = new Prefix(address, prefixLength);
      }
      String map = null;
      Integer mapLine = null;
      if (ctx.mapname != null) {
         map = ctx.mapname.getText();
         mapLine = ctx.mapname.getStart().getLine();
      }
      BgpNetwork bgpNetwork = new BgpNetwork(prefix, map, mapLine);
      BgpProcess proc = currentVrf().getBgpProcess();
      proc.getIpNetworks().put(prefix, bgpNetwork);
   }

   @Override
   public void exitNetwork6_bgp_tail(Network6_bgp_tailContext ctx) {
      Prefix6 prefix6 = new Prefix6(ctx.prefix.getText());
      String map = null;
      Integer mapLine = null;
      if (ctx.mapname != null) {
         map = ctx.mapname.getText();
         mapLine = ctx.mapname.getStart().getLine();
      }
      BgpProcess proc = currentVrf().getBgpProcess();
      BgpNetwork6 bgpNetwork6 = new BgpNetwork6(prefix6, map, mapLine);
      proc.getIpv6Networks().put(prefix6, bgpNetwork6);
   }

   @Override
   public void exitNext_hop_self_bgp_tail(Next_hop_self_bgp_tailContext ctx) {
      boolean val = ctx.NO() == null;
      _currentPeerGroup.setNextHopSelf(val);
   }

   @Override
   public void exitNo_ip_prefix_list_stanza(
         No_ip_prefix_list_stanzaContext ctx) {
      String prefixListName = ctx.name.getText();
      if (_configuration.getPrefixLists().containsKey(prefixListName)) {
         _configuration.getPrefixLists().remove(prefixListName);
      }
   }

   @Override
   public void exitNo_neighbor_activate_rb_stanza(
         No_neighbor_activate_rb_stanzaContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      if (ctx.ip != null) {
         Ip ip = toIp(ctx.ip);
         IpBgpPeerGroup pg = proc.getIpPeerGroups().get(ip);
         if (pg == null) {
            String message = "ignoring attempt to activate undefined ip peer group: "
                  + ip.toString();
            _w.redFlag(message);
         }
         else {
            pg.setActive(false);
         }
      }
      else if (ctx.ip6 != null) {
         Ip6 ip6 = toIp6(ctx.ip6);
         Ipv6BgpPeerGroup pg = proc.getIpv6PeerGroups().get(ip6);
         if (pg == null) {
            String message = "ignoring attempt to activate undefined ipv6 peer group: "
                  + ip6.toString();
            _w.redFlag(message);
         }
         else {
            pg.setActive(false);
         }
      }
      else if (ctx.peergroup != null) {
         String pgName = ctx.peergroup.getText();
         NamedBgpPeerGroup npg = proc.getNamedPeerGroups().get(pgName);
         npg.setActive(false);
         for (IpBgpPeerGroup ipg : proc.getIpPeerGroups().values()) {
            String currentGroupName = ipg.getGroupName();
            if (currentGroupName != null && currentGroupName.equals(pgName)) {
               ipg.setActive(false);
            }
         }
         for (Ipv6BgpPeerGroup ipg : proc.getIpv6PeerGroups().values()) {
            String currentGroupName = ipg.getGroupName();
            if (currentGroupName != null && currentGroupName.equals(pgName)) {
               ipg.setActive(false);
            }
         }
      }
   }

   @Override
   public void exitNo_neighbor_shutdown_rb_stanza(
         No_neighbor_shutdown_rb_stanzaContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      if (ctx.ip != null) {
         Ip ip = toIp(ctx.ip);
         IpBgpPeerGroup pg = proc.getIpPeerGroups().get(ip);
         // TODO: see if it is always ok to set active on 'no shutdown'
         if (pg == null) {
            String message = "ignoring attempt to shut down to undefined ip peer group: "
                  + ip.toString();
            _w.redFlag(message);
         }
         else {
            pg.setActive(true);
            pg.setShutdown(false);
         }
      }
      else if (ctx.ip6 != null) {
         Ip6 ip6 = toIp6(ctx.ip6);
         Ipv6BgpPeerGroup pg = proc.getIpv6PeerGroups().get(ip6);
         // TODO: see if it is always ok to set active on 'no shutdown'
         if (pg == null) {
            String message = "ignoring attempt to shut down undefined ipv6 peer group: "
                  + ip6.toString();
            _w.redFlag(message);
         }
         else {
            pg.setActive(true);
            pg.setShutdown(false);
         }
      }
      else if (ctx.peergroup != null) {
         _w.redFlag("'no shutdown' of  peer group unsupported");
      }
   }

   @Override
   public void exitNo_redistribute_connected_rb_stanza(
         No_redistribute_connected_rb_stanzaContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      if (_currentPeerGroup == proc.getMasterBgpPeerGroup()) {
         RoutingProtocol sourceProtocol = RoutingProtocol.CONNECTED;
         proc.getRedistributionPolicies().remove(sourceProtocol);
      }
      else if (_currentIpPeerGroup != null || _currentNamedPeerGroup != null) {
         throw new BatfishException(
               "do not currently handle per-neighbor redistribution policies");
      }
   }

   @Override
   public void exitNo_route_map_stanza(No_route_map_stanzaContext ctx) {
      String mapName = ctx.name.getText();
      if (_configuration.getRouteMaps().containsKey(mapName)) {
         _configuration.getRouteMaps().remove(mapName);
      }
   }

   @Override
   public void exitNo_shutdown_rb_stanza(No_shutdown_rb_stanzaContext ctx) {
      // TODO: see if it is always ok to set active on 'no shutdown'
      _currentPeerGroup.setShutdown(false);
      _currentPeerGroup.setActive(true);
   }

   @Override
   public void exitNtp_access_group(Ntp_access_groupContext ctx) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.getNtpAccessGroups().add(name);
      _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
            name, CiscoStructureUsage.NTP_ACCESS_GROUP, line);
   }

   @Override
   public void exitNtp_server(Ntp_serverContext ctx) {
      Ntp ntp = _configuration.getCf().getNtp();
      String hostname = ctx.hostname.getText();
      NtpServer server = ntp.getServers().get(hostname);
      if (server == null) {
         server = new NtpServer(hostname);
         ntp.getServers().put(hostname, server);
      }
      if (ctx.vrf != null) {
         String vrfName = ctx.vrf.getText();
         server.setVrf(vrfName);
         initVrf(vrfName);
      }
      if (ctx.PREFER() != null) {
         // TODO: implement
      }
   }

   @Override
   public void exitNtp_source_interface(Ntp_source_interfaceContext ctx) {
      String ifaceName = ctx.iname.getText();
      String canonicalIfaceName = getCanonicalInterfaceName(ifaceName);
      _configuration.setNtpSourceInterface(canonicalIfaceName);
   }

   @Override
   public void exitNull_as_path_regex(Null_as_path_regexContext ctx) {
      _w.redFlag("as-path regexes this complicated are not supported yet");
   }

   @Override
   public void exitPassive_iis_stanza(Passive_iis_stanzaContext ctx) {
      _currentIsisInterface.setIsisInterfaceMode(IsisInterfaceMode.PASSIVE);
   }

   @Override
   public void exitPassive_interface_default_is_stanza(
         Passive_interface_default_is_stanzaContext ctx) {
      for (Interface iface : _configuration.getInterfaces().values()) {
         iface.setIsisInterfaceMode(IsisInterfaceMode.PASSIVE);
      }
   }

   @Override
   public void exitPassive_interface_is_stanza(
         Passive_interface_is_stanzaContext ctx) {
      String ifaceName = ctx.name.getText();
      String canonicalIfaceName = getCanonicalInterfaceName(ifaceName);
      Interface iface = _configuration.getInterfaces().get(canonicalIfaceName);
      if (iface == null) {
         iface = addInterface(canonicalIfaceName, ctx.name, false);
      }
      if (ctx.NO() == null) {
         iface.setIsisInterfaceMode(IsisInterfaceMode.PASSIVE);
      }
      else {
         iface.setIsisInterfaceMode(IsisInterfaceMode.ACTIVE);
      }
   }

   @Override
   public void exitPeer_group_assignment_rb_stanza(
         Peer_group_assignment_rb_stanzaContext ctx) {
      String peerGroupName = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      BgpProcess proc = currentVrf().getBgpProcess();
      if (ctx.address != null) {
         Ip address = toIp(ctx.address);
         IpBgpPeerGroup ipPeerGroup = proc.getIpPeerGroups().get(address);
         if (ipPeerGroup == null) {
            proc.addIpPeerGroup(address);
            ipPeerGroup = proc.getIpPeerGroups().get(address);
         }
         ipPeerGroup.setGroupName(peerGroupName);
         ipPeerGroup.setGroupNameLine(line);
      }
      else if (ctx.address6 != null) {
         Ip6 address6 = toIp6(ctx.address6);
         Ipv6BgpPeerGroup ipv6PeerGroup = proc.getIpv6PeerGroups()
               .get(address6);
         if (ipv6PeerGroup == null) {
            proc.addIpv6PeerGroup(address6);
            ipv6PeerGroup = proc.getIpv6PeerGroups().get(address6);
         }
         ipv6PeerGroup.setGroupName(peerGroupName);
         ipv6PeerGroup.setGroupNameLine(line);
      }
   }

   @Override
   public void exitPeer_group_creation_rb_stanza(
         Peer_group_creation_rb_stanzaContext ctx) {
      String name = ctx.name.getText();
      int definitionLine = ctx.name.getLine();
      BgpProcess proc = currentVrf().getBgpProcess();
      if (proc.getNamedPeerGroups().get(name) == null) {
         proc.addNamedPeerGroup(name, definitionLine);
         if (ctx.PASSIVE() != null) {
            // dell: won't otherwise specify activation so just activate here
            NamedBgpPeerGroup npg = proc.getNamedPeerGroups().get(name);
            npg.setActive(true);
         }
      }
   }

   @Override
   public void exitPeer_sa_filter(Peer_sa_filterContext ctx) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.getMsdpPeerSaLists().add(name);
      _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
            name, CiscoStructureUsage.MSDP_PEER_SA_LIST, line);
   }

   @Override
   public void exitPim_accept_register(Pim_accept_registerContext ctx) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      if (ctx.LIST() != null) {
         _configuration.getPimAcls().add(name);
         _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
               name, CiscoStructureUsage.PIM_ACCEPT_REGISTER_ACL, line);
      }
      else if (ctx.ROUTE_MAP() != null) {
         _configuration.getPimRouteMaps().add(name);
         _configuration.referenceStructure(CiscoStructureType.ROUTE_MAP, name,
               CiscoStructureUsage.PIM_ACCEPT_REGISTER_ROUTE_MAP, line);
      }
   }

   @Override
   public void exitPim_accept_rp(Pim_accept_rpContext ctx) {
      if (ctx.name != null) {
         String name = ctx.name.getText();
         int line = ctx.name.getStart().getLine();
         _configuration.getPimAcls().add(name);
         _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
               name, CiscoStructureUsage.PIM_ACCEPT_RP_ACL, line);
      }
   }

   @Override
   public void exitPim_rp_address(Pim_rp_addressContext ctx) {
      if (!_no) {
         if (ctx.name != null) {
            String name = ctx.name.getText();
            int line = ctx.name.getStart().getLine();
            _configuration.getPimAcls().add(name);
            _configuration.referenceStructure(
                  CiscoStructureType.IPV4_ACCESS_LIST, name,
                  CiscoStructureUsage.PIM_RP_ADDRESS_ACL, line);
         }
      }
   }

   @Override
   public void exitPim_rp_announce_filter(Pim_rp_announce_filterContext ctx) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.getPimAcls().add(name);
      _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
            name, CiscoStructureUsage.PIM_RP_ANNOUNCE_FILTER, line);
   }

   @Override
   public void exitPim_rp_candidate(Pim_rp_candidateContext ctx) {
      if (ctx.name != null) {
         String name = ctx.name.getText();
         int line = ctx.name.getStart().getLine();
         _configuration.getPimAcls().add(name);
         _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
               name, CiscoStructureUsage.PIM_RP_CANDIDATE_ACL, line);
      }
   }

   @Override
   public void exitPim_send_rp_announce(Pim_send_rp_announceContext ctx) {
      if (ctx.name != null) {
         String name = ctx.name.getText();
         int line = ctx.name.getStart().getLine();
         _configuration.getPimAcls().add(name);
         _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
               name, CiscoStructureUsage.PIM_SEND_RP_ANNOUNCE_ACL, line);
      }
   }

   @Override
   public void exitPim_spt_threshold(Pim_spt_thresholdContext ctx) {
      if (ctx.name != null) {
         String name = ctx.name.getText();
         int line = ctx.name.getStart().getLine();
         _configuration.getPimAcls().add(name);
         _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
               name, CiscoStructureUsage.PIM_SPT_THRESHOLD_ACL, line);
      }
   }

   @Override
   public void exitPim_ssm(Pim_ssmContext ctx) {
      if (ctx.name != null) {
         String name = ctx.name.getText();
         int line = ctx.name.getStart().getLine();
         _configuration.getPimAcls().add(name);
         _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
               name, CiscoStructureUsage.PIM_SSM_ACL, line);
      }
   }

   @Override
   public void exitPrefix_list_bgp_tail(Prefix_list_bgp_tailContext ctx) {
      String listName = ctx.list_name.getText();
      int line = ctx.list_name.getLine();
      if (ctx.IN() != null) {
         _currentPeerGroup.setInboundPrefixList(listName);
         _currentPeerGroup.setInboundPrefixListLine(line);
      }
      else if (ctx.OUT() != null) {
         _currentPeerGroup.setOutboundPrefixList(listName);
         _currentPeerGroup.setOutboundPrefixListLine(line);
      }
      else {
         throw new BatfishException("bad direction");
      }
   }

   @Override
   public void exitPrefix_set_elem(Prefix_set_elemContext ctx) {
      String name = _currentPrefixSetName;
      if (name != null) {
         if (ctx.ipa != null || ctx.prefix != null) {
            PrefixList pl = _configuration.getPrefixLists().get(name);
            if (pl == null) {
               pl = new PrefixList(name, _currentPrefixSetDefinitionLine);
               _configuration.getPrefixLists().put(name, pl);
            }
            Prefix prefix;
            if (ctx.ipa != null) {
               prefix = new Prefix(toIp(ctx.ipa), Prefix.MAX_PREFIX_LENGTH);
            }
            else {
               prefix = new Prefix(ctx.prefix.getText());
            }
            int prefixLength = prefix.getPrefixLength();
            int minLen = prefixLength;
            int maxLen = prefixLength;
            if (ctx.minpl != null) {
               minLen = toInteger(ctx.minpl);
               maxLen = Prefix.MAX_PREFIX_LENGTH;
            }
            if (ctx.maxpl != null) {
               maxLen = toInteger(ctx.maxpl);
            }
            if (ctx.eqpl != null) {
               minLen = toInteger(ctx.eqpl);
               maxLen = toInteger(ctx.eqpl);
            }
            SubRange lengthRange = new SubRange(minLen, maxLen);
            PrefixListLine line = new PrefixListLine(LineAction.ACCEPT, prefix,
                  lengthRange);
            pl.addLine(line);
         }
         else {
            Prefix6List pl = _configuration.getPrefix6Lists().get(name);
            if (pl == null) {
               pl = new Prefix6List(name, _currentPrefixSetDefinitionLine);
               _configuration.getPrefix6Lists().put(name, pl);
            }
            Prefix6 prefix6;
            if (ctx.ipv6a != null) {
               prefix6 = new Prefix6(
                     toIp6(ctx.ipv6a),
                     Prefix6.MAX_PREFIX_LENGTH);
            }
            else {
               prefix6 = new Prefix6(ctx.ipv6_prefix.getText());
            }
            int prefixLength = prefix6.getPrefixLength();
            int minLen = prefixLength;
            int maxLen = prefixLength;
            if (ctx.minpl != null) {
               minLen = toInteger(ctx.minpl);
               maxLen = Prefix6.MAX_PREFIX_LENGTH;
            }
            if (ctx.maxpl != null) {
               maxLen = toInteger(ctx.maxpl);
            }
            if (ctx.eqpl != null) {
               minLen = toInteger(ctx.eqpl);
               maxLen = toInteger(ctx.eqpl);
            }
            SubRange lengthRange = new SubRange(minLen, maxLen);
            Prefix6ListLine line = new Prefix6ListLine(LineAction.ACCEPT,
                  prefix6, lengthRange);
            pl.addLine(line);
         }
      }
   }

   public void exitPrefix_set_stanza(Prefix_set_stanzaContext ctx) {
      _currentPrefixSetName = null;
   }

   @Override
   public void exitRedistribute_aggregate_bgp_tail(
         Redistribute_aggregate_bgp_tailContext ctx) {
      todo(ctx, F_BGP_REDISTRIBUTE_AGGREGATE);
   }

   @Override
   public void exitRedistribute_connected_bgp_tail(
         Redistribute_connected_bgp_tailContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      if (_currentPeerGroup == proc.getMasterBgpPeerGroup()) {
         RoutingProtocol sourceProtocol = RoutingProtocol.CONNECTED;
         BgpRedistributionPolicy r = new BgpRedistributionPolicy(
               sourceProtocol);
         proc.getRedistributionPolicies().put(sourceProtocol, r);
         if (ctx.metric != null) {
            int metric = toInteger(ctx.metric);
            r.setMetric(metric);
         }
         if (ctx.map != null) {
            String map = ctx.map.getText();
            int mapLine = ctx.map.getStart().getLine();
            r.setRouteMap(map);
            r.setRouteMapLine(mapLine);
         }
      }
      else if (_currentIpPeerGroup != null || _currentNamedPeerGroup != null) {
         throw new BatfishException(
               "do not currently handle per-neighbor redistribution policies");
      }
   }

   @Override
   public void exitRedistribute_connected_is_stanza(
         Redistribute_connected_is_stanzaContext ctx) {
      IsisProcess proc = currentVrf().getIsisProcess();
      RoutingProtocol sourceProtocol = RoutingProtocol.CONNECTED;
      IsisRedistributionPolicy r = new IsisRedistributionPolicy(sourceProtocol);
      proc.getRedistributionPolicies().put(sourceProtocol, r);
      if (ctx.metric != null) {
         int metric = toInteger(ctx.metric);
         r.setMetric(metric);
      }
      if (ctx.map != null) {
         String map = ctx.map.getText();
         r.setMap(map);
      }
      if (ctx.LEVEL_1() != null) {
         r.setLevel(IsisLevel.LEVEL_1);
      }
      else if (ctx.LEVEL_2() != null) {
         r.setLevel(IsisLevel.LEVEL_2);
      }
      else if (ctx.LEVEL_1_2() != null) {
         r.setLevel(IsisLevel.LEVEL_1_2);
      }
      else {
         r.setLevel(IsisRedistributionPolicy.DEFAULT_LEVEL);
      }
   }

   @Override
   public void exitRedistribute_ospf_bgp_tail(
         Redistribute_ospf_bgp_tailContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      if (_currentPeerGroup == proc.getMasterBgpPeerGroup()) {
         RoutingProtocol sourceProtocol = RoutingProtocol.OSPF;
         BgpRedistributionPolicy r = new BgpRedistributionPolicy(
               sourceProtocol);
         proc.getRedistributionPolicies().put(sourceProtocol, r);
         if (ctx.metric != null) {
            int metric = toInteger(ctx.metric);
            r.setMetric(metric);
         }
         if (ctx.map != null) {
            String map = ctx.map.getText();
            int mapLine = ctx.map.getStart().getLine();
            r.setRouteMap(map);
            r.setRouteMapLine(mapLine);
         }
         int procNum = toInteger(ctx.procnum);
         r.getSpecialAttributes()
               .put(BgpRedistributionPolicy.OSPF_PROCESS_NUMBER, procNum);
      }
      else if (_currentIpPeerGroup != null || _currentNamedPeerGroup != null) {
         throw new BatfishException(
               "do not currently handle per-neighbor redistribution policies");
      }
   }

   @Override
   public void exitRedistribute_static_bgp_tail(
         Redistribute_static_bgp_tailContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      if (_currentPeerGroup == proc.getMasterBgpPeerGroup()) {
         RoutingProtocol sourceProtocol = RoutingProtocol.STATIC;
         BgpRedistributionPolicy r = new BgpRedistributionPolicy(
               sourceProtocol);
         proc.getRedistributionPolicies().put(sourceProtocol, r);
         if (ctx.metric != null) {
            int metric = toInteger(ctx.metric);
            r.setMetric(metric);
         }
         if (ctx.map != null) {
            String map = ctx.map.getText();
            int mapLine = ctx.map.getStart().getLine();
            r.setRouteMap(map);
            r.setRouteMapLine(mapLine);
         }
      }
      else if (_currentIpPeerGroup != null || _currentNamedPeerGroup != null) {
         throw new BatfishException(
               "do not currently handle per-neighbor redistribution policies");
      }
   }

   @Override
   public void exitRedistribute_static_is_stanza(
         Redistribute_static_is_stanzaContext ctx) {
      IsisProcess proc = currentVrf().getIsisProcess();
      RoutingProtocol sourceProtocol = RoutingProtocol.STATIC;
      IsisRedistributionPolicy r = new IsisRedistributionPolicy(sourceProtocol);
      proc.getRedistributionPolicies().put(sourceProtocol, r);
      if (ctx.metric != null) {
         int metric = toInteger(ctx.metric);
         r.setMetric(metric);
      }
      if (ctx.map != null) {
         String map = ctx.map.getText();
         r.setMap(map);
      }
      if (ctx.LEVEL_1() != null) {
         r.setLevel(IsisLevel.LEVEL_1);
      }
      else if (ctx.LEVEL_2() != null) {
         r.setLevel(IsisLevel.LEVEL_2);
      }
      else if (ctx.LEVEL_1_2() != null) {
         r.setLevel(IsisLevel.LEVEL_1_2);
      }
      else {
         r.setLevel(IsisRedistributionPolicy.DEFAULT_LEVEL);
      }
   }

   @Override
   public void exitRemote_as_bgp_tail(Remote_as_bgp_tailContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      int as = toInteger(ctx.as);
      if (_currentPeerGroup != proc.getMasterBgpPeerGroup()) {
         _currentPeerGroup.setRemoteAs(as);
      }
      else {
         throw new BatfishException("no peer or peer group in context: "
               + getLocation(ctx) + getFullText(ctx));
      }
   }

   @Override
   public void exitRemove_private_as_bgp_tail(
         Remove_private_as_bgp_tailContext ctx) {
      _currentPeerGroup.setRemovePrivateAs(true);
   }

   @Override
   public void exitRo_area(Ro_areaContext ctx) {
      _currentOspfArea = null;
   }

   @Override
   public void exitRo_area_nssa(Ro_area_nssaContext ctx) {
      OspfProcess proc = _currentOspfProcess;
      int area = (ctx.area_int != null) ? toInteger(ctx.area_int)
            : (int) toIp(ctx.area_ip).asLong();
      boolean noSummary = ctx.NO_SUMMARY() != null;
      boolean defaultOriginate = ctx.DEFAULT_INFORMATION_ORIGINATE() != null;
      if (defaultOriginate) {
         todo(ctx, F_OSPF_AREA_NSSA);
      }
      proc.getNssas().put(area, noSummary);
   }

   @Override
   public void exitRo_default_information(Ro_default_informationContext ctx) {
      OspfProcess proc = _currentOspfProcess;
      proc.setDefaultInformationOriginate(true);
      boolean always = ctx.ALWAYS().size() > 0;
      proc.setDefaultInformationOriginateAlways(always);
      if (ctx.metric != null) {
         int metric = toInteger(ctx.metric);
         proc.setDefaultInformationMetric(metric);
      }
      if (ctx.metric_type != null) {
         int metricTypeInt = toInteger(ctx.metric_type);
         OspfMetricType metricType = OspfMetricType.fromInteger(metricTypeInt);
         proc.setDefaultInformationMetricType(metricType);
      }
      if (ctx.map != null) {
         int line = ctx.map.getLine();
         proc.setDefaultInformationOriginateMap(ctx.map.getText());
         proc.setDefaultInformationOriginateMapLine(line);
      }
   }

   @Override
   public void exitRo_maximum_paths(Ro_maximum_pathsContext ctx) {
      todo(ctx, F_OSPF_MAXIMUM_PATHS);
      /*
       * Note that this is very difficult to enforce, and may not help the
       * analysis without major changes
       */
   }

   @Override
   public void exitRo_network(Ro_networkContext ctx) {
      Ip address;
      Ip wildcard;
      if (ctx.prefix != null) {
         Prefix prefix = new Prefix(ctx.prefix.getText());
         address = prefix.getAddress();
         wildcard = prefix.getPrefixWildcard();
      }
      else {
         address = toIp(ctx.ip);
         wildcard = toIp(ctx.wildcard);
      }
      long area;
      if (ctx.area_int != null) {
         area = toLong(ctx.area_int);
      }
      else if (ctx.area_ip != null) {
         area = toIp(ctx.area_ip).asLong();
      }
      else {
         throw new BatfishException("bad area");
      }
      OspfWildcardNetwork network = new OspfWildcardNetwork(address, wildcard,
            area);
      _currentOspfProcess.getWildcardNetworks().add(network);
   }

   @Override
   public void exitRo_passive_interface(Ro_passive_interfaceContext ctx) {
      boolean passive = ctx.NO() == null;
      String iname = ctx.i.getText();
      OspfProcess proc = _currentOspfProcess;
      if (passive) {
         proc.getPassiveInterfaceList().add(iname);
      }
      else {
         proc.getActiveInterfaceList().add(iname);
      }
   }

   @Override
   public void exitRo_passive_interface_default(
         Ro_passive_interface_defaultContext ctx) {
      _currentOspfProcess.setPassiveInterfaceDefault(true);
   }

   @Override
   public void exitRo_redistribute_bgp(Ro_redistribute_bgpContext ctx) {
      OspfProcess proc = _currentOspfProcess;
      RoutingProtocol sourceProtocol = RoutingProtocol.BGP;
      OspfRedistributionPolicy r = new OspfRedistributionPolicy(sourceProtocol);
      proc.getRedistributionPolicies().put(sourceProtocol, r);
      int as = toInteger(ctx.as);
      r.getSpecialAttributes().put(OspfRedistributionPolicy.BGP_AS, as);
      if (ctx.metric != null) {
         int metric = toInteger(ctx.metric);
         r.setMetric(metric);
      }
      if (ctx.map != null) {
         String map = ctx.map.getText();
         int mapLine = ctx.map.getLine();
         r.setRouteMap(map);
         r.setRouteMapLine(mapLine);
      }
      if (ctx.type != null) {
         int typeInt = toInteger(ctx.type);
         OspfMetricType type = OspfMetricType.fromInteger(typeInt);
         r.setOspfMetricType(type);
      }
      else {
         r.setOspfMetricType(OspfRedistributionPolicy.DEFAULT_METRIC_TYPE);
      }
      if (ctx.tag != null) {
         long tag = toLong(ctx.tag);
         r.setTag(tag);
      }
      r.setSubnets(ctx.subnets != null);
   }

   @Override
   public void exitRo_redistribute_connected(
         Ro_redistribute_connectedContext ctx) {
      OspfProcess proc = _currentOspfProcess;
      RoutingProtocol sourceProtocol = RoutingProtocol.CONNECTED;
      OspfRedistributionPolicy r = new OspfRedistributionPolicy(sourceProtocol);
      proc.getRedistributionPolicies().put(sourceProtocol, r);
      if (ctx.metric != null) {
         int metric = toInteger(ctx.metric);
         r.setMetric(metric);
      }
      if (ctx.map != null) {
         String map = ctx.map.getText();
         int mapLine = ctx.map.getLine();
         r.setRouteMap(map);
         r.setRouteMapLine(mapLine);
      }
      if (ctx.type != null) {
         int typeInt = toInteger(ctx.type);
         OspfMetricType type = OspfMetricType.fromInteger(typeInt);
         r.setOspfMetricType(type);
      }
      else {
         r.setOspfMetricType(OspfRedistributionPolicy.DEFAULT_METRIC_TYPE);
      }
      if (ctx.tag != null) {
         long tag = toLong(ctx.tag);
         r.setTag(tag);
      }
      r.setSubnets(ctx.subnets != null);
   }

   @Override
   public void exitRo_redistribute_rip(Ro_redistribute_ripContext ctx) {
      todo(ctx, F_OSPF_REDISTRIBUTE_RIP);
   }

   @Override
   public void exitRo_redistribute_static(Ro_redistribute_staticContext ctx) {
      OspfProcess proc = _currentOspfProcess;
      RoutingProtocol sourceProtocol = RoutingProtocol.STATIC;
      OspfRedistributionPolicy r = new OspfRedistributionPolicy(sourceProtocol);
      proc.getRedistributionPolicies().put(sourceProtocol, r);
      if (ctx.metric != null) {
         int metric = toInteger(ctx.metric);
         r.setMetric(metric);
      }
      if (ctx.map != null) {
         String map = ctx.map.getText();
         int mapLine = ctx.map.getLine();
         r.setRouteMap(map);
         r.setRouteMapLine(mapLine);
      }
      if (ctx.type != null) {
         int typeInt = toInteger(ctx.type);
         OspfMetricType type = OspfMetricType.fromInteger(typeInt);
         r.setOspfMetricType(type);
      }
      else {
         r.setOspfMetricType(OspfRedistributionPolicy.DEFAULT_METRIC_TYPE);
      }
      if (ctx.tag != null) {
         long tag = toLong(ctx.tag);
         r.setTag(tag);
      }
      r.setSubnets(ctx.subnets != null);
   }

   @Override
   public void exitRo_router_id(Ro_router_idContext ctx) {
      Ip routerId = toIp(ctx.ip);
      _currentOspfProcess.setRouterId(routerId);
   }

   @Override
   public void exitRoa_interface(Roa_interfaceContext ctx) {
      _currentOspfInterface = null;
   }

   @Override
   public void exitRoa_range(Roa_rangeContext ctx) {
      OspfProcess proc = currentVrf().getOspfProcess();
      Prefix prefix = new Prefix(ctx.prefix.getText());
      boolean advertise = ctx.NOT_ADVERTISE() == null;
      Map<Prefix, Boolean> area = proc.getSummaries().get(_currentOspfArea);
      if (area == null) {
         area = new TreeMap<>();
         proc.getSummaries().put(_currentOspfArea, area);
      }
      area.put(prefix, advertise);
   }

   @Override
   public void exitRoi_cost(Roi_costContext ctx) {
      Interface iface = _configuration.getInterfaces()
            .get(_currentOspfInterface);
      int cost = toInteger(ctx.cost);
      iface.setOspfCost(cost);
   }

   @Override
   public void exitRoi_passive(Roi_passiveContext ctx) {
      if (ctx.ENABLE() != null) {
         _currentOspfProcess.getPassiveInterfaceList()
               .add(_currentOspfInterface);
      }
   }

   @Override
   public void exitRoute_map_bgp_tail(Route_map_bgp_tailContext ctx) {
      if (_currentPeerGroup == null) {
         return;
      }
      String mapName = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      boolean ipv6 = _inIpv6BgpPeer || _currentIpv6PeerGroup != null;
      if (ctx.IN() != null) {
         if (ipv6) {
            _currentPeerGroup.setInboundRoute6Map(mapName);
            _currentPeerGroup.setInboundRoute6MapLine(line);
         }
         else {
            _currentPeerGroup.setInboundRouteMap(mapName);
            _currentPeerGroup.setInboundRouteMapLine(line);
         }
      }
      else if (ctx.OUT() != null) {
         if (ipv6) {
            _currentPeerGroup.setOutboundRoute6Map(mapName);
            _currentPeerGroup.setOutboundRoute6MapLine(line);
         }
         else {
            _currentPeerGroup.setOutboundRouteMap(mapName);
            _currentPeerGroup.setOutboundRouteMapLine(line);
         }
      }
      else {
         throw new BatfishException("bad direction");
      }
   }

   @Override
   public void exitRoute_map_stanza(Route_map_stanzaContext ctx) {
      _currentRouteMap = null;
      _currentRouteMapClause = null;
   }

   @Override
   public void exitRoute_policy_stanza(Route_policy_stanzaContext ctx) {
      _currentRoutePolicy = null;
   }

   @Override
   public void exitRoute_reflector_client_bgp_tail(
         Route_reflector_client_bgp_tailContext ctx) {
      _currentPeerGroup.setRouteReflectorClient(true);
   }

   @Override
   public void exitRouter_bgp_stanza(Router_bgp_stanzaContext ctx) {
      popPeer();
   }

   @Override
   public void exitRouter_id_bgp_tail(Router_id_bgp_tailContext ctx) {
      Ip routerId = toIp(ctx.routerid);
      BgpProcess proc = currentVrf().getBgpProcess();
      proc.setRouterId(routerId);
   }

   @Override
   public void exitRouter_isis_stanza(Router_isis_stanzaContext ctx) {
      _currentIsisProcess = null;
   }

   @Override
   public void exitRs_route(Rs_routeContext ctx) {
      if (ctx.prefix != null) {
         Prefix prefix = new Prefix(ctx.prefix.getText());
         Ip nextHopIp = Route.UNSET_ROUTE_NEXT_HOP_IP;
         String nextHopInterface = null;
         if (ctx.nhip != null) {
            nextHopIp = new Ip(ctx.nhip.getText());
         }
         if (ctx.nhint != null) {
            nextHopInterface = getCanonicalInterfaceName(ctx.nhint.getText());
         }
         int distance = DEFAULT_STATIC_ROUTE_DISTANCE;
         if (ctx.distance != null) {
            distance = toInteger(ctx.distance);
         }
         Integer tag = null;
         if (ctx.tag != null) {
            tag = toInteger(ctx.tag);
         }

         boolean permanent = ctx.PERMANENT() != null;
         Integer track = null;
         if (ctx.track != null) {
            // TODO: handle named instead of numbered track
         }
         StaticRoute route = new StaticRoute(prefix, nextHopIp,
               nextHopInterface, distance, tag, track, permanent);
         currentVrf().getStaticRoutes().add(route);
      }
      else if (ctx.prefix6 != null) {
         // TODO: ipv6 static route
      }
   }

   @Override
   public void exitRs_vrf(Rs_vrfContext ctx) {
      _currentVrf = Configuration.DEFAULT_VRF_NAME;
   }

   @Override
   public void exitS_domain_name(S_domain_nameContext ctx) {
      String domainName = ctx.hostname.getText();
      _configuration.setDomainName(domainName);
   }

   @Override
   public void exitS_feature(S_featureContext ctx) {
      List<String> words = ctx.words.stream().map(w -> w.getText())
            .collect(Collectors.toList());
      boolean enabled = ctx.NO() == null;
      String name = String.join(".", words);
      _configuration.getCf().getFeatures().put(name, enabled);
   }

   @Override
   public void exitS_hostname(S_hostnameContext ctx) {
      StringBuilder sb = new StringBuilder();
      for (Token namePart : ctx.name_parts) {
         sb.append(namePart.getText());
      }
      String hostname = sb.toString();
      _configuration.setHostname(hostname);
      _configuration.getCf().setHostname(hostname);
   }

   @Override
   public void exitS_interface(S_interfaceContext ctx) {
      _currentInterfaces = null;
   }

   @Override
   public void exitS_ip_domain(S_ip_domainContext ctx) {
      _no = false;
   }

   @Override
   public void exitS_ip_domain_name(S_ip_domain_nameContext ctx) {
      String domainName = ctx.hostname.getText();
      _configuration.setDomainName(domainName);
   }

   @Override
   public void exitS_ip_name_server(S_ip_name_serverContext ctx) {
      Set<String> dnsServers = _configuration.getDnsServers();
      for (Ip_hostnameContext ipCtx : ctx.hostnames) {
         String domainName = ipCtx.getText();
         dnsServers.add(domainName);
      }
   }

   @Override
   public void exitS_ip_pim(S_ip_pimContext ctx) {
      _no = false;
   }

   @Override
   public void exitS_ip_source_route(S_ip_source_routeContext ctx) {
      boolean enabled = ctx.NO() == null;
      _configuration.getCf().setSourceRoute(enabled);
   }

   @Override
   public void exitS_ip_tacacs_source_interface(
         S_ip_tacacs_source_interfaceContext ctx) {
      String ifaceName = ctx.iname.getText();
      String canonicalIfaceName = getCanonicalInterfaceName(ifaceName);
      _configuration.setTacacsSourceInterface(canonicalIfaceName);
   }

   @Override
   public void exitS_line(S_lineContext ctx) {
      _currentLineNames = null;
   }

   @Override
   public void exitS_logging(S_loggingContext ctx) {
      _no = false;
   }

   @Override
   public void exitS_no_access_list_extended(
         S_no_access_list_extendedContext ctx) {
      String name = ctx.ACL_NUM_EXTENDED().getText();
      _configuration.getExtendedAcls().remove(name);
   }

   @Override
   public void exitS_no_access_list_standard(
         S_no_access_list_standardContext ctx) {
      String name = ctx.ACL_NUM_STANDARD().getText();
      _configuration.getStandardAcls().remove(name);
   }

   @Override
   public void exitS_router_ospf(S_router_ospfContext ctx) {
      _currentOspfProcess
            .computeNetworks(_configuration.getInterfaces().values());
      _currentOspfProcess = null;
      _currentVrf = Configuration.DEFAULT_VRF_NAME;
   }

   @Override
   public void exitS_service(S_serviceContext ctx) {
      List<String> words = ctx.words.stream().map(w -> w.getText())
            .collect(Collectors.toList());
      boolean enabled = ctx.NO() == null;
      Iterator<String> i = words.iterator();
      SortedMap<String, Service> currentServices = _configuration.getCf()
            .getServices();
      while (i.hasNext()) {
         String name = i.next();
         Service s = currentServices.get(name);
         if (s == null) {
            s = new Service();
            currentServices.put(name, s);
            if (enabled) {
               s.setEnabled(true);
            }
            else if (!enabled && !i.hasNext()) {
               s.disable();
            }
            currentServices = s.getSubservices();
         }
      }
   }

   @Override
   public void exitS_spanning_tree(S_spanning_treeContext ctx) {
      _no = false;
   }

   @Override
   public void exitS_switchport(S_switchportContext ctx) {
      if (ctx.ACCESS() != null) {
         _configuration.getCf().setDefaultSwitchportMode(SwitchportMode.ACCESS);
      }
      else if (ctx.ROUTED() != null) {
         _configuration.getCf().setDefaultSwitchportMode(SwitchportMode.NONE);
      }
      else {
         throw new BatfishException(
               "Unsupported top-level switchport statement: " + ctx.getText());
      }
   }

   @Override
   public void exitS_tacacs_server(S_tacacs_serverContext ctx) {
      _no = false;
   }

   @Override
   public void exitS_vrf_context(S_vrf_contextContext ctx) {
      _currentVrf = Configuration.DEFAULT_VRF_NAME;
   }

   @Override
   public void exitSd_switchport(Sd_switchportContext ctx) {
      if (ctx.SHUTDOWN() != null) {
         _configuration.getCf().setDefaultSwitchportMode(SwitchportMode.NONE);
      }
      else {
         // TODO: support more
         _configuration.getCf().setDefaultSwitchportMode(SwitchportMode.ACCESS);
      }
   }

   @Override
   public void exitSend_community_bgp_tail(Send_community_bgp_tailContext ctx) {
      boolean extended = false;
      boolean standard = false;
      if (ctx.SEND_COMMUNITY() != null) {
         if (ctx.BOTH() != null) {
            extended = true;
            standard = true;
         }
         else if (ctx.EXTENDED() != null) {
            extended = true;
         }
         else {
            standard = true;
         }
      }
      else if (ctx.SEND_COMMUNITY_EBGP() != null) {
         standard = true;
      }
      else if (ctx.SEND_EXTENDED_COMMUNITY_EBGP() != null) {
         extended = true;
      }
      if (standard) {
         _currentPeerGroup.setSendCommunity(true);
      }
      if (extended) {
         _currentPeerGroup.setSendExtendedCommunity(true);
      }
   }

   @Override
   public void exitSession_group_rb_stanza(Session_group_rb_stanzaContext ctx) {
      _currentIpPeerGroup = null;
      _currentIpv6PeerGroup = null;
      _currentNamedPeerGroup = null;
      _currentPeerSession = null;
      popPeer();
   }

   @Override
   public void exitSet_as_path_prepend_rm_stanza(
         Set_as_path_prepend_rm_stanzaContext ctx) {
      List<AsExpr> asList = new ArrayList<>();
      for (As_exprContext asx : ctx.as_list) {
         AsExpr as = toAsExpr(asx);
         asList.add(as);
      }
      RouteMapSetAsPathPrependLine line = new RouteMapSetAsPathPrependLine(
            asList);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_comm_list_delete_rm_stanza(
         Set_comm_list_delete_rm_stanzaContext ctx) {
      String name = ctx.name.getText();
      int statementLine = ctx.name.getLine();
      RouteMapSetDeleteCommunityLine line = new RouteMapSetDeleteCommunityLine(
            name, statementLine);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_community_additive_rm_stanza(
         Set_community_additive_rm_stanzaContext ctx) {
      List<Long> commList = new ArrayList<>();
      for (CommunityContext c : ctx.communities) {
         long community = toLong(c);
         commList.add(community);
      }
      RouteMapSetAdditiveCommunityLine line = new RouteMapSetAdditiveCommunityLine(
            commList);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_community_list_additive_rm_stanza(
         Set_community_list_additive_rm_stanzaContext ctx) {
      int statementLine = ctx.getStart().getLine();
      Set<String> communityLists = new LinkedHashSet<>();
      for (VariableContext comm_list : ctx.comm_lists) {
         String communityList = comm_list.getText();
         communityLists.add(communityList);
      }
      RouteMapSetAdditiveCommunityListLine line = new RouteMapSetAdditiveCommunityListLine(
            communityLists, statementLine);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_community_list_rm_stanza(
         Set_community_list_rm_stanzaContext ctx) {
      Set<String> communityLists = new LinkedHashSet<>();
      int statementLine = ctx.getStart().getLine();
      for (VariableContext comm_list : ctx.comm_lists) {
         String communityList = comm_list.getText();
         communityLists.add(communityList);
      }
      RouteMapSetCommunityListLine line = new RouteMapSetCommunityListLine(
            communityLists, statementLine);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_community_none_rm_stanza(
         Set_community_none_rm_stanzaContext ctx) {
      RouteMapSetCommunityNoneLine line = new RouteMapSetCommunityNoneLine();
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_community_rm_stanza(Set_community_rm_stanzaContext ctx) {
      List<Long> commList = new ArrayList<>();
      for (CommunityContext c : ctx.communities) {
         long community = toLong(c);
         commList.add(community);
      }
      RouteMapSetCommunityLine line = new RouteMapSetCommunityLine(commList);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_local_preference_rm_stanza(
         Set_local_preference_rm_stanzaContext ctx) {
      IntExpr localPreference = toLocalPreferenceIntExpr(ctx.pref);
      RouteMapSetLocalPreferenceLine line = new RouteMapSetLocalPreferenceLine(
            localPreference);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_metric_rm_stanza(Set_metric_rm_stanzaContext ctx) {
      IntExpr metric = toMetricIntExpr(ctx.metric);
      RouteMapSetMetricLine line = new RouteMapSetMetricLine(metric);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_metric_type_rm_stanza(
         Set_metric_type_rm_stanzaContext ctx) {
      todo(ctx, F_ROUTE_MAP_SET_METRIC_TYPE);
   }

   @Override
   public void exitSet_next_hop_peer_address_stanza(
         Set_next_hop_peer_address_stanzaContext ctx) {
      RouteMapSetNextHopPeerAddress line = new RouteMapSetNextHopPeerAddress();
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_next_hop_rm_stanza(Set_next_hop_rm_stanzaContext ctx) {
      List<Ip> nextHops = new ArrayList<>();
      for (Token t : ctx.nexthop_list) {
         Ip nextHop = toIp(t);
         nextHops.add(nextHop);
      }
      RouteMapSetNextHopLine line = new RouteMapSetNextHopLine(nextHops);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_origin_rm_stanza(Set_origin_rm_stanzaContext ctx) {
      OriginExpr originExpr = toOriginExpr(ctx.origin_expr_literal());
      RouteMapSetLine line = new RouteMapSetOriginTypeLine(originExpr);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitShutdown_bgp_tail(Shutdown_bgp_tailContext ctx) {
      if (_currentPeerGroup == null) {
         return;
      }
      _currentPeerGroup.setShutdown(true);
   }

   @Override
   public void exitSntp_server(Sntp_serverContext ctx) {
      Sntp sntp = _configuration.getCf().getSntp();
      String hostname = ctx.hostname.getText();
      SntpServer server = sntp.getServers().get(hostname);
      if (server == null) {
         server = new SntpServer(hostname);
         sntp.getServers().put(hostname, server);
      }
   }

   @Override
   public void exitSpanning_tree_portfast(Spanning_tree_portfastContext ctx) {
      if (ctx.defaultLiteral != null) {
         _configuration.setSpanningTreePortfastDefault(true);
      }
   }

   @Override
   public void exitSs_community(Ss_communityContext ctx) {
      _currentSnmpCommunity = null;
   }

   @Override
   public void exitSs_enable_traps(Ss_enable_trapsContext ctx) {
      if (ctx.snmp_trap_type != null) {
         String trapName = ctx.snmp_trap_type.getText();
         SortedSet<String> subfeatureNames = new TreeSet<>(ctx.subfeature
               .stream().map(s -> s.getText()).collect(Collectors.toList()));
         SortedMap<String, SortedSet<String>> traps = _configuration
               .getSnmpServer().getTraps();
         SortedSet<String> subfeatures = traps.get(trapName);
         if (subfeatures == null) {
            traps.put(trapName, subfeatureNames);
         }
         else {
            subfeatures.addAll(subfeatureNames);
         }
      }
   }

   @Override
   public void exitSs_file_transfer(Ss_file_transferContext ctx) {
      String acl = ctx.acl.getText();
      int line = ctx.acl.getStart().getLine();
      _configuration.getSnmpAccessLists().add(acl);
      _configuration.referenceStructure(CiscoStructureType.IP_ACCESS_LIST, acl,
            CiscoStructureUsage.SNMP_SERVER_FILE_TRANSFER_ACL, line);
   }

   @Override
   public void exitSs_host(Ss_hostContext ctx) {
      _currentSnmpHost = null;
   }

   @Override
   public void exitSs_source_interface(Ss_source_interfaceContext ctx) {
      String ifaceName = ctx.iname.getText();
      String canonicalIfaceName = getCanonicalInterfaceName(ifaceName);
      _configuration.setSnmpSourceInterface(canonicalIfaceName);
   }

   @Override
   public void exitSs_tftp_server_list(Ss_tftp_server_listContext ctx) {
      String acl = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.getSnmpAccessLists().add(acl);
      _configuration.referenceStructure(CiscoStructureType.IP_ACCESS_LIST, acl,
            CiscoStructureUsage.SNMP_SERVER_TFTP_SERVER_LIST, line);
   }

   @Override
   public void exitSs_trap_source(Ss_trap_sourceContext ctx) {
      String ifaceName = ctx.iname.getText();
      String canonicalIfaceName = getCanonicalInterfaceName(ifaceName);
      _configuration.setSnmpSourceInterface(canonicalIfaceName);
   }

   @Override
   public void exitSsc_access_control(Ssc_access_controlContext ctx) {
      int line;
      if (ctx.acl4 != null) {
         String name = ctx.acl4.getText();
         line = ctx.acl4.getStart().getLine();
         _configuration.getSnmpAccessLists().add(name);
         _currentSnmpCommunity.setAccessList(name);
         _configuration.referenceStructure(CiscoStructureType.IP_ACCESS_LIST,
               name, CiscoStructureUsage.SNMP_SERVER_COMMUNITY_ACL, line);
      }
      if (ctx.acl6 != null) {
         String name = ctx.acl6.getText();
         line = ctx.acl6.getStart().getLine();
         _configuration.getSnmpAccessLists().add(name);
         _currentSnmpCommunity.setAccessList6(name);
         _configuration.referenceStructure(CiscoStructureType.IPV6_ACCESS_LIST,
               name, CiscoStructureUsage.SNMP_SERVER_COMMUNITY_ACL6, line);
      }
      if (ctx.RO() != null) {
         _currentSnmpCommunity.setRo(true);
      }
      if (ctx.RW() != null) {
         _currentSnmpCommunity.setRw(true);
      }
   }

   @Override
   public void exitSsc_use_ipv4_acl(Ssc_use_ipv4_aclContext ctx) {
      String name = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      _configuration.getSnmpAccessLists().add(name);
      _currentSnmpCommunity.setAccessList(name);
      _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
            name, CiscoStructureUsage.SNMP_SERVER_COMMUNITY_ACL4, line);
   }

   @Override
   public void exitSsh_access_group(Ssh_access_groupContext ctx) {
      String acl = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      if (ctx.IPV6() != null) {
         _configuration.getSshIpv6Acls().add(acl);
         _configuration.referenceStructure(CiscoStructureType.IPV6_ACCESS_LIST,
               acl, CiscoStructureUsage.SSH_IPV6_ACL, line);
      }
      else {
         _configuration.getSshAcls().add(acl);
         _configuration.referenceStructure(CiscoStructureType.IP_ACCESS_LIST,
               acl, CiscoStructureUsage.SSH_ACL, line);
      }
   }

   @Override
   public void exitSsh_server(Ssh_serverContext ctx) {
      if (ctx.acl != null) {
         String acl = ctx.acl.getText();
         int line = ctx.acl.getStart().getLine();
         _configuration.getSshAcls().add(acl);
         _configuration.referenceStructure(CiscoStructureType.IPV4_ACCESS_LIST,
               acl, CiscoStructureUsage.SSH_IPV4_ACL, line);
      }
      if (ctx.acl6 != null) {
         String acl6 = ctx.acl6.getText();
         int line = ctx.acl6.getStart().getLine();
         _configuration.getSshIpv6Acls().add(acl6);
         _configuration.referenceStructure(CiscoStructureType.IPV6_ACCESS_LIST,
               acl6, CiscoStructureUsage.SSH_IPV6_ACL, line);
      }
   }

   @Override
   public void exitStandard_access_list_stanza(
         Standard_access_list_stanzaContext ctx) {
      _currentStandardAcl = null;
   }

   @Override
   public void exitStandard_access_list_tail(
         Standard_access_list_tailContext ctx) {
      LineAction action = toLineAction(ctx.ala);
      Ip srcIp = getIp(ctx.ipr);
      Ip srcWildcard = getWildcard(ctx.ipr);
      Set<Integer> dscps = new TreeSet<>();
      Set<Integer> ecns = new TreeSet<>();
      for (Standard_access_list_additional_featureContext feature : ctx.features) {
         if (feature.DSCP() != null) {
            int dscpType = toDscpType(feature.dscp_type());
            dscps.add(dscpType);
         }
         else if (feature.ECN() != null) {
            int ecn = toInteger(feature.ecn);
            ecns.add(ecn);
         }
      }
      String name;
      if (ctx.num != null) {
         name = ctx.num.getText();
      }
      else {
         name = getFullText(ctx).trim();
      }
      StandardAccessListLine line = new StandardAccessListLine(name, action,
            new IpWildcard(srcIp, srcWildcard), dscps, ecns);
      _currentStandardAcl.addLine(line);
   }

   @Override
   public void exitStandard_ipv6_access_list_stanza(
         Standard_ipv6_access_list_stanzaContext ctx) {
      _currentStandardIpv6Acl = null;
   }

   @Override
   public void exitStandard_ipv6_access_list_tail(
         Standard_ipv6_access_list_tailContext ctx) {
      LineAction action = toLineAction(ctx.ala);
      Ip6 srcIp = getIp(ctx.ipr);
      Ip6 srcWildcard = getWildcard(ctx.ipr);
      Set<Integer> dscps = new TreeSet<>();
      Set<Integer> ecns = new TreeSet<>();
      for (Standard_access_list_additional_featureContext feature : ctx.features) {
         if (feature.DSCP() != null) {
            int dscpType = toDscpType(feature.dscp_type());
            dscps.add(dscpType);
         }
         else if (feature.ECN() != null) {
            int ecn = toInteger(feature.ecn);
            ecns.add(ecn);
         }
      }
      String name;
      if (ctx.num != null) {
         name = ctx.num.getText();
      }
      else {
         name = getFullText(ctx).trim();
      }
      StandardIpv6AccessListLine line = new StandardIpv6AccessListLine(name,
            action, new Ip6Wildcard(srcIp, srcWildcard), dscps, ecns);
      _currentStandardIpv6Acl.addLine(line);
   }

   // @Override
   // public void exitSubnet_bgp_tail(Subnet_bgp_tailContext ctx) {
   // BgpProcess proc = currentVrf().getBgpProcess();
   // if (ctx.IP_PREFIX() != null) {
   // Prefix prefix = new Prefix(ctx.IP_PREFIX().getText());
   // NamedBgpPeerGroup namedGroup = _currentNamedPeerGroup;
   // namedGroup.addNeighborIpPrefix(prefix);
   // DynamicIpBgpPeerGroup pg = proc.addDynamicIpPeerGroup(prefix);
   // pg.setGroupName(namedGroup.getName());
   // }
   // else if (ctx.IPV6_PREFIX() != null) {
   // Prefix6 prefix6 = new Prefix6(ctx.IPV6_PREFIX().getText());
   // NamedBgpPeerGroup namedGroup = _currentNamedPeerGroup;
   // namedGroup.addNeighborIpv6Prefix(prefix6);
   // DynamicIpv6BgpPeerGroup pg = proc.addDynamicIpv6PeerGroup(prefix6);
   // pg.setGroupName(namedGroup.getName());
   // }
   // }
   //
   @Override
   public void exitSummary_address_is_stanza(
         Summary_address_is_stanzaContext ctx) {
      Ip ip = toIp(ctx.ip);
      Ip mask = toIp(ctx.mask);
      Prefix prefix = new Prefix(ip, mask);
      RoutingProtocol sourceProtocol = RoutingProtocol.ISIS_L1;
      IsisRedistributionPolicy r = new IsisRedistributionPolicy(sourceProtocol);
      r.setSummaryPrefix(prefix);
      _currentIsisProcess.getRedistributionPolicies().put(sourceProtocol, r);
      if (ctx.metric != null) {
         int metric = toInteger(ctx.metric);
         r.setMetric(metric);
      }
      if (!ctx.LEVEL_1().isEmpty()) {
         r.setLevel(IsisLevel.LEVEL_1);
      }
      else if (!ctx.LEVEL_2().isEmpty()) {
         r.setLevel(IsisLevel.LEVEL_2);
      }
      else if (!ctx.LEVEL_1_2().isEmpty()) {
         r.setLevel(IsisLevel.LEVEL_1_2);
      }
      else {
         r.setLevel(IsisRedistributionPolicy.DEFAULT_LEVEL);
      }
   }

   @Override
   public void exitSwitching_mode_stanza(Switching_mode_stanzaContext ctx) {
      todo(ctx, F_SWITCHING_MODE);
   }

   @Override
   public void exitT_server(T_serverContext ctx) {
      String hostname = ctx.hostname.getText();
      _configuration.getTacacsServers().add(hostname);
   }

   @Override
   public void exitT_source_interface(T_source_interfaceContext ctx) {
      String ifaceName = ctx.iname.getText();
      String canonicalIfaceName = getCanonicalInterfaceName(ifaceName);
      _configuration.setTacacsSourceInterface(canonicalIfaceName);
   }

   @Override
   public void exitTemplate_peer_address_family(
         Template_peer_address_familyContext ctx) {
      popPeer();
   }

   @Override
   public void exitTemplate_peer_policy_rb_stanza(
         Template_peer_policy_rb_stanzaContext ctx) {
      _currentIpPeerGroup = null;
      _currentIpv6PeerGroup = null;
      _currentNamedPeerGroup = null;
      popPeer();
   }

   @Override
   public void exitTemplate_peer_rb_stanza(Template_peer_rb_stanzaContext ctx) {
      _currentIpPeerGroup = null;
      _currentIpv6PeerGroup = null;
      _currentNamedPeerGroup = null;
      popPeer();
   }

   @Override
   public void exitTemplate_peer_session_rb_stanza(
         Template_peer_session_rb_stanzaContext ctx) {
      _currentIpPeerGroup = null;
      _currentIpv6PeerGroup = null;
      _currentNamedPeerGroup = null;
      _currentPeerSession = null;
      popPeer();
   }

   @Override
   public void exitUnrecognized_line(Unrecognized_lineContext ctx) {
      String line = _text.substring(
            ctx.start.getStartIndex(),
            ctx.stop.getStopIndex());
      String msg = String.format("Line %d unrecognized: %s",
            ctx.start.getLine(), line);
      if (_unrecognizedAsRedFlag) {
         msg += "\nLINES BELOW LINE " + line
               + " ARE UNLIKELY TO BE PROCESSED CORRECTLY";
         _w.redFlag(msg);
         _configuration.setUnrecognized(true);
      }
      else {
         _parser.getParserErrorListener().syntaxError(ctx, ctx.getStart(),
               ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
               msg);
      }
   }

   @Override
   public void exitUpdate_source_bgp_tail(Update_source_bgp_tailContext ctx) {
      if (_currentPeerGroup == null) {
         return;
      }
      else {
         String source = toInterfaceName(ctx.source);
         int line = ctx.source.getStart().getLine();
         _currentPeerGroup.setUpdateSource(source);
         _currentPeerGroup.setUpdateSourceLine(line);
      }
   }

   @Override
   public void exitUse_af_group_bgp_tail(Use_af_group_bgp_tailContext ctx) {
      String groupName = ctx.name.getText();
      if (_currentIpPeerGroup == null && _currentIpv6PeerGroup == null) {
         throw new BatfishException(
               "Unexpected context for use neighbor group");
      }
      _currentPeerGroup.getAfGroups().put(_currentAddressFamily, groupName);
   }

   @Override
   public void exitUse_neighbor_group_bgp_tail(
         Use_neighbor_group_bgp_tailContext ctx) {
      String groupName = ctx.name.getText();
      if (_currentIpPeerGroup != null) {
         _currentIpPeerGroup.setGroupName(groupName);
      }
      else if (_currentIpv6PeerGroup != null) {
         _currentIpv6PeerGroup.setGroupName(groupName);
      }
      else if (_currentNamedPeerGroup != null) {
         _currentNamedPeerGroup.setGroupName(groupName);
      }
      else {
         throw new BatfishException(
               "Unexpected context for use neighbor group");
      }
   }

   @Override
   public void exitUse_session_group_bgp_tail(
         Use_session_group_bgp_tailContext ctx) {
      BgpProcess proc = currentVrf().getBgpProcess();
      String groupName = ctx.name.getText();
      int line = ctx.name.getStart().getLine();
      if (_currentIpPeerGroup != null) {
         _currentIpPeerGroup.setPeerSession(groupName);
         _currentIpPeerGroup.setPeerSessionLine(line);
      }
      else if (_currentNamedPeerGroup != null) {
         _currentNamedPeerGroup.setPeerSession(groupName);
         _currentNamedPeerGroup.setPeerSessionLine(line);
      }
      else if (_currentPeerGroup == proc.getMasterBgpPeerGroup()) {
         throw new BatfishException("Invalid peer context for inheritance");
      }
      else {
         todo(ctx, F_BGP_INHERIT_PEER_SESSION_OTHER);
      }
   }

   @Override
   public void exitVrf_block_rb_stanza(Vrf_block_rb_stanzaContext ctx) {
      _currentVrf = Configuration.DEFAULT_VRF_NAME;
      popPeer();
   }

   @Override
   public void exitVrfc_ip_route(Vrfc_ip_routeContext ctx) {
      todo(ctx, F_IP_ROUTE_VRF);
   }

   @Override
   public void exitWccp_id(Wccp_idContext ctx) {
      if (ctx.group_list != null) {
         String name = ctx.group_list.getText();
         int line = ctx.group_list.getStart().getLine();
         _configuration.getWccpAcls().add(name);
         _configuration.referenceStructure(CiscoStructureType.IP_ACCESS_LIST,
               name, CiscoStructureUsage.WCCP_GROUP_LIST, line);
      }
      if (ctx.redirect_list != null) {
         String name = ctx.redirect_list.getText();
         int line = ctx.redirect_list.getStart().getLine();
         _configuration.getWccpAcls().add(name);
         _configuration.referenceStructure(CiscoStructureType.IP_ACCESS_LIST,
               name, CiscoStructureUsage.WCCP_REDIRECT_LIST, line);
      }
      if (ctx.service_list != null) {
         String name = ctx.service_list.getText();
         int line = ctx.service_list.getStart().getLine();
         _configuration.getWccpAcls().add(name);
         _configuration.referenceStructure(CiscoStructureType.IP_ACCESS_LIST,
               name, CiscoStructureUsage.WCCP_SERVICE_LIST, line);
      }
   }

   private String getAddressGroup(Access_list_ip_rangeContext ctx) {
      if (ctx.address_group != null) {
         return ctx.address_group.getText();
      }
      else {
         return null;
      }
   }

   private String getAddressGroup(Access_list_ip6_rangeContext ctx) {
      if (ctx.address_group != null) {
         return ctx.address_group.getText();
      }
      else {
         return null;
      }
   }

   public CiscoConfiguration getConfiguration() {
      return _configuration;
   }

   private String getFullText(ParserRuleContext ctx) {
      int start = ctx.getStart().getStartIndex();
      int end = ctx.getStop().getStopIndex();
      String text = _text.substring(start, end + 1);
      return text;
   }

   private String getLocation(ParserRuleContext ctx) {
      return ctx.getStart().getLine() + ":"
            + ctx.getStart().getCharPositionInLine() + ": ";
   }

   public int getPortNumber(PortContext ctx) {
      if (ctx.DEC() != null) {
         return toInteger(ctx.DEC());
      }
      else {
         NamedPort namedPort = toNamedPort(ctx);
         return namedPort.number();
      }
   }

   public String getText() {
      return _text;
   }

   @Override
   public Set<String> getUnimplementedFeatures() {
      return _unimplementedFeatures;
   }

   @Override
   public VendorConfiguration getVendorConfiguration() {
      return _configuration;
   }

   private Ip getWildcard(Access_list_ip_rangeContext ctx) {
      // TODO: fix for address-group, object, object-group, interface
      if (ctx.wildcard != null) {
         return toIp(ctx.wildcard);
      }
      else if (ctx.ANY() != null || ctx.ANY4() != null
            || ctx.address_group != null || ctx.obj != null || ctx.og != null
            || ctx.iface != null) {
         return Ip.MAX;
      }
      else if (ctx.HOST() != null) {
         return Ip.ZERO;
      }
      else if (ctx.prefix != null) {
         return new Prefix(ctx.prefix.getText()).getPrefixWildcard();
      }
      else if (ctx.ip != null) {
         // basically same as host
         return Ip.ZERO;
      }
      else {
         throw convError(Ip.class, ctx);
      }
   }

   private Ip6 getWildcard(Access_list_ip6_rangeContext ctx) {
      if (ctx.wildcard != null) {
         return toIp6(ctx.wildcard);
      }
      else if (ctx.ANY() != null || ctx.ANY6() != null
            || ctx.address_group != null) {
         return Ip6.MAX;
      }
      else if (ctx.HOST() != null) {
         return Ip6.ZERO;
      }
      else if (ctx.ipv6_prefix != null) {
         return new Prefix6(ctx.ipv6_prefix.getText()).getPrefixWildcard();
      }
      else if (ctx.ip != null) {
         // basically same as host
         return Ip6.ZERO;
      }
      else {
         throw convError(Ip.class, ctx);
      }
   }

   private void initInterface(Interface iface, ConfigurationFormat format) {
      switch (format) {
      case CISCO_IOS:
         iface.setProxyArp(true);
         break;

      case ARISTA:
         if (iface.getName().startsWith("Ethernet")) {
            iface.setSwitchportMode(SwitchportMode.ACCESS);
         }
         break;

      // $CASES-OMITTED$
      default:
         break;

      }
   }

   private void initInterface(Interface iface, Interface_nameContext ctx) {
      String nameAlpha = ctx.name_prefix_alpha.getText();
      String canonicalNamePrefix = getCanonicalInterfaceNamePrefix(nameAlpha);
      String vrf = canonicalNamePrefix.equals(NXOS_MANAGEMENT_INTERFACE_PREFIX)
            ? CiscoConfiguration.MANAGEMENT_VRF_NAME
            : Configuration.DEFAULT_VRF_NAME;
      double bandwidth = Interface.getDefaultBandwidth(canonicalNamePrefix);
      int mtu = Interface.getDefaultMtu();
      iface.setBandwidth(bandwidth);
      iface.setVrf(vrf);
      initVrf(vrf);
      iface.setMtu(mtu);
   }

   private Vrf initVrf(String vrfName) {
      Vrf currentVrf = _configuration.getVrfs().get(vrfName);
      if (currentVrf == null) {
         currentVrf = new Vrf(vrfName);
         _configuration.getVrfs().put(vrfName, currentVrf);
      }
      return currentVrf;
   }

   private void popPeer() {
      int index = _peerGroupStack.size() - 1;
      _currentPeerGroup = _peerGroupStack.get(index);
      _peerGroupStack.remove(index);
      _inIpv6BgpPeer = false;
   }

   @Override
   public void processParseTree(ParserRuleContext tree) {
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(this, tree);
   }

   private void pushPeer(BgpPeerGroup pg) {
      _peerGroupStack.add(_currentPeerGroup);
      _currentPeerGroup = pg;
   }

   private void resetPeerGroups() {
      _currentDynamicIpPeerGroup = null;
      _currentIpPeerGroup = null;
      _currentIpv6PeerGroup = null;
      _currentNamedPeerGroup = null;
   }

   private AsExpr toAsExpr(As_exprContext ctx) {
      if (ctx.DEC() != null) {
         int as = toInteger(ctx.DEC());
         return new ExplicitAs(as);
      }
      else if (ctx.AUTO() != null) {
         return new AutoAs();
      }
      else if (ctx.RP_VARIABLE() != null) {
         return new VarAs(ctx.RP_VARIABLE().getText());
      }
      else {
         throw convError(AsExpr.class, ctx);
      }
   }

   private AsPathSetElem toAsPathSetElem(As_path_set_elemContext ctx) {
      if (ctx.IOS_REGEX() != null) {
         String withQuotes = ctx.AS_PATH_SET_REGEX().getText();
         String iosRegex = withQuotes.substring(1, withQuotes.length() - 1);
         String regex = toJavaRegex(iosRegex);
         return new RegexAsPathSetElem(regex);
      }
      else {
         throw convError(AsPathSetElem.class, ctx);
      }
   }

   private AsPathSetExpr toAsPathSetExpr(As_path_set_exprContext ctx) {
      if (ctx.named != null) {
         return new NamedAsPathSet(ctx.named.getText());
      }
      else if (ctx.rpvar != null) {
         return new VarAsPathSet(ctx.rpvar.getText());
      }
      else if (ctx.inline != null) {
         return toAsPathSetExpr(ctx.inline);
      }
      else {
         throw convError(AsPathSetExpr.class, ctx);
      }
   }

   private AsPathSetExpr toAsPathSetExpr(As_path_set_inlineContext ctx) {
      List<AsPathSetElem> elems = ctx.elems.stream()
            .map(e -> toAsPathSetElem(e)).collect(Collectors.toList());
      return new ExplicitAsPathSet(elems);
   }

   private IntExpr toCommonIntExpr(Int_exprContext ctx) {
      if (ctx.DEC() != null && ctx.PLUS() == null && ctx.DASH() == null) {
         int val = toInteger(ctx.DEC());
         return new LiteralInt(val);
      }
      else if (ctx.RP_VARIABLE() != null) {
         return new VarInt(ctx.RP_VARIABLE().getText());
      }
      else {
         /*
          * Unsupported static integer expression - do not add cases unless you
          * know what you are doing
          */
         throw convError(IntExpr.class, ctx);
      }
   }

   private CommunitySetElem toCommunitySetElemExpr(
         Rp_community_set_elemContext ctx) {
      if (ctx.prefix != null) {
         CommunitySetElemHalfExpr prefix = toCommunitySetElemHalfExpr(
               ctx.prefix);
         CommunitySetElemHalfExpr suffix = toCommunitySetElemHalfExpr(
               ctx.suffix);
         return new CommunitySetElem(prefix, suffix);
      }
      else if (ctx.community() != null) {
         long value = toLong(ctx.community());
         return new CommunitySetElem(value);
      }
      else {
         throw convError(CommunitySetElem.class, ctx);
      }
   }

   private CommunitySetElemHalfExpr toCommunitySetElemHalfExpr(
         Rp_community_set_elem_halfContext ctx) {
      if (ctx.value != null) {
         int value = toInteger(ctx.value);
         return new LiteralCommunitySetElemHalf(value);
      }
      else if (ctx.var != null) {
         String var = ctx.var.getText();
         return new VarCommunitySetElemHalf(var);
      }
      else if (ctx.first != null) {
         int first = toInteger(ctx.first);
         int last = toInteger(ctx.last);
         SubRange range = new SubRange(first, last);
         return new RangeCommunitySetElemHalf(range);
      }
      else {
         throw convError(CommunitySetElem.class, ctx);
      }
   }

   private void todo(ParserRuleContext ctx, String feature) {
      _w.todo(ctx, feature, _parser, _text);
      _unimplementedFeatures.add("Cisco: " + feature);
   }

   private int toDscpType(Dscp_typeContext ctx) {
      int val;
      if (ctx.DEC() != null) {
         val = toInteger(ctx.DEC());
      }
      else if (ctx.AF11() != null) {
         val = DscpType.AF11.number();
      }
      else if (ctx.AF12() != null) {
         val = DscpType.AF12.number();
      }
      else if (ctx.AF13() != null) {
         val = DscpType.AF13.number();
      }
      else if (ctx.AF21() != null) {
         val = DscpType.AF21.number();
      }
      else if (ctx.AF22() != null) {
         val = DscpType.AF22.number();
      }
      else if (ctx.AF23() != null) {
         val = DscpType.AF23.number();
      }
      else if (ctx.AF31() != null) {
         val = DscpType.AF31.number();
      }
      else if (ctx.AF32() != null) {
         val = DscpType.AF32.number();
      }
      else if (ctx.AF33() != null) {
         val = DscpType.AF33.number();
      }
      else if (ctx.AF41() != null) {
         val = DscpType.AF41.number();
      }
      else if (ctx.AF42() != null) {
         val = DscpType.AF42.number();
      }
      else if (ctx.AF43() != null) {
         val = DscpType.AF43.number();
      }
      else if (ctx.CS1() != null) {
         val = DscpType.CS1.number();
      }
      else if (ctx.CS2() != null) {
         val = DscpType.CS2.number();
      }
      else if (ctx.CS3() != null) {
         val = DscpType.CS3.number();
      }
      else if (ctx.CS4() != null) {
         val = DscpType.CS4.number();
      }
      else if (ctx.CS5() != null) {
         val = DscpType.CS5.number();
      }
      else if (ctx.CS6() != null) {
         val = DscpType.CS6.number();
      }
      else if (ctx.CS7() != null) {
         val = DscpType.CS7.number();
      }
      else if (ctx.DEFAULT() != null) {
         val = DscpType.DEFAULT.number();
      }
      else if (ctx.EF() != null) {
         val = DscpType.EF.number();
      }
      else {
         throw convError(DscpType.class, ctx);
      }
      return val;
   }

   private SwitchportEncapsulationType toEncapsulation(
         Switchport_trunk_encapsulationContext ctx) {
      if (ctx.DOT1Q() != null) {
         return SwitchportEncapsulationType.DOT1Q;
      }
      else if (ctx.ISL() != null) {
         return SwitchportEncapsulationType.ISL;
      }
      else if (ctx.NEGOTIATE() != null) {
         return SwitchportEncapsulationType.NEGOTIATE;
      }
      else {
         throw convError(SwitchportEncapsulationType.class, ctx);
      }
   }

   private IntComparator toIntComparator(Int_compContext ctx) {
      if (ctx.EQ() != null || ctx.IS() != null) {
         return IntComparator.EQ;
      }
      else if (ctx.GE() != null) {
         return IntComparator.GE;
      }
      else if (ctx.LE() != null) {
         return IntComparator.LE;
      }
      else {
         throw convError(IntComparator.class, ctx);
      }
   }

   private IntExpr toIntExpr(Int_exprContext ctx) {
      if (ctx.DEC() != null && ctx.PLUS() == null && ctx.DASH() == null) {
         int val = toInteger(ctx.DEC());
         return new LiteralInt(val);
      }
      else if (ctx.RP_VARIABLE() != null) {
         return new VarInt(ctx.RP_VARIABLE().getText());
      }
      else {
         throw convError(IntExpr.class, ctx);
      }
   }

   private IpProtocol toIpProtocol(ProtocolContext ctx) {
      if (ctx.DEC() != null) {
         int num = toInteger(ctx.DEC());
         return IpProtocol.fromNumber(num);
      }
      else if (ctx.AHP() != null) {
         return IpProtocol.AHP;
      }
      else if (ctx.EIGRP() != null) {
         return IpProtocol.EIGRP;
      }
      else if (ctx.ESP() != null) {
         return IpProtocol.ESP;
      }
      else if (ctx.GRE() != null) {
         return IpProtocol.GRE;
      }
      else if (ctx.ICMP() != null) {
         return IpProtocol.ICMP;
      }
      else if (ctx.ICMP6() != null || ctx.ICMPV6() != null) {
         return IpProtocol.IPV6_ICMP;
      }
      else if (ctx.IGMP() != null) {
         return IpProtocol.IGMP;
      }
      else if (ctx.IP() != null) {
         return IpProtocol.IP;
      }
      else if (ctx.IPINIP() != null) {
         return IpProtocol.IPINIP;
      }
      else if (ctx.IPV4() != null) {
         return IpProtocol.IP;
      }
      else if (ctx.IPV6() != null) {
         return IpProtocol.IPV6;
      }
      else if (ctx.OSPF() != null) {
         return IpProtocol.OSPF;
      }
      else if (ctx.PIM() != null) {
         return IpProtocol.PIM;
      }
      else if (ctx.SCTP() != null) {
         return IpProtocol.SCTP;
      }
      else if (ctx.TCP() != null) {
         return IpProtocol.TCP;
      }
      else if (ctx.UDP() != null) {
         return IpProtocol.UDP;
      }
      else if (ctx.VRRP() != null) {
         return IpProtocol.VRRP;
      }
      else {
         throw convError(IpProtocol.class, ctx);
      }
   }

   private IsisLevel toIsisLevel(Isis_levelContext ctx) {
      if (ctx.LEVEL_1() != null) {
         return IsisLevel.LEVEL_1;
      }
      else if (ctx.LEVEL_1_2() != null) {
         return IsisLevel.LEVEL_1_2;
      }
      else if (ctx.LEVEL_2() != null) {
         return IsisLevel.LEVEL_2;
      }
      else {
         throw convError(IsisLevel.class, ctx);
      }
   }

   private IsisLevelExpr toIsisLevelExpr(Isis_level_exprContext ctx) {
      if (ctx.isis_level() != null) {
         IsisLevel level = toIsisLevel(ctx.isis_level());
         return new LiteralIsisLevel(level);
      }
      else if (ctx.RP_VARIABLE() != null) {
         return new VarIsisLevel(ctx.RP_VARIABLE().getText());
      }
      else {
         throw convError(IsisLevelExpr.class, ctx);
      }
   }

   private IsisMetricType toIsisMetricType(Rp_isis_metric_typeContext ctx) {
      if (ctx.EXTERNAL() != null) {
         return IsisMetricType.EXTERNAL;
      }
      else if (ctx.INTERNAL() != null) {
         return IsisMetricType.INTERNAL;
      }
      else if (ctx.RIB_METRIC_AS_EXTERNAL() != null) {
         return IsisMetricType.RIB_METRIC_AS_EXTERNAL;
      }
      else if (ctx.RIB_METRIC_AS_INTERNAL() != null) {
         return IsisMetricType.RIB_METRIC_AS_INTERNAL;
      }
      else {
         throw convError(IsisMetricType.class, ctx);
      }
   }

   private String toJavaRegex(String rawRegex) {
      // TODO: fix so it actually works
      return rawRegex;
   }

   private LineAction toLineAction(Access_list_actionContext ctx) {
      if (ctx.PERMIT() != null) {
         return LineAction.ACCEPT;
      }
      else if (ctx.DENY() != null) {
         return LineAction.REJECT;
      }
      else {
         throw convError(LineAction.class, ctx);
      }
   }

   private IntExpr toLocalPreferenceIntExpr(Int_exprContext ctx) {
      if (ctx.DEC() != null) {
         int val = toInteger(ctx.DEC());
         if (ctx.PLUS() != null) {
            return new IncrementLocalPreference(val);
         }
         else if (ctx.DASH() != null) {
            return new DecrementLocalPreference(val);
         }
         else {
            return new LiteralInt(val);
         }
      }
      else if (ctx.RP_VARIABLE() != null) {
         return new VarInt(ctx.RP_VARIABLE().getText());
      }
      else {
         /*
          * Unsupported local-preference integer expression - do not add cases
          * unless you know what you are doing
          */
         throw convError(IntExpr.class, ctx);
      }
   }

   private String toLoggingSeverity(int severityNum) {
      switch (severityNum) {
      case 0:
         return Logging.SEVERITY_EMERGENCIES;
      case 1:
         return Logging.SEVERITY_ALERTS;
      case 2:
         return Logging.SEVERITY_CRITICAL;
      case 3:
         return Logging.SEVERITY_ERRORS;
      case 4:
         return Logging.SEVERITY_WARNINGS;
      case 5:
         return Logging.SEVERITY_NOTIFICATIONS;
      case 6:
         return Logging.SEVERITY_INFORMATIONAL;
      case 7:
         return Logging.SEVERITY_DEBUGGING;
      default:
         throw new BatfishException("Invalid logging severity: " + severityNum);
      }
   }

   private String toLoggingSeverity(Logging_severityContext ctx) {
      if (ctx.DEC() != null) {
         int severityNum = toInteger(ctx.DEC());
         return toLoggingSeverity(severityNum);
      }
      else {
         return ctx.getText();
      }
   }

   private Integer toLoggingSeverityNum(Logging_severityContext ctx) {
      if (ctx.EMERGENCIES() != null) {
         return 0;
      }
      else if (ctx.ALERTS() != null) {
         return 1;
      }
      else if (ctx.CRITICAL() != null) {
         return 2;
      }
      else if (ctx.ERRORS() != null) {
         return 3;
      }
      else if (ctx.WARNINGS() != null) {
         return 4;
      }
      else if (ctx.NOTIFICATIONS() != null) {
         return 5;
      }
      else if (ctx.INFORMATIONAL() != null) {
         return 6;
      }
      else if (ctx.DEBUGGING() != null) {
         return 7;
      }
      else {
         throw new BatfishException(
               "Invalid logging severity: " + ctx.getText());
      }
   }

   public long toLong(CommunityContext ctx) {
      if (ctx.COMMUNITY_NUMBER() != null) {
         String numberText = ctx.com.getText();
         String[] parts = numberText.split(":");
         String leftStr = parts[0];
         String rightStr = parts[1];
         long left = Long.parseLong(leftStr);
         long right = Long.parseLong(rightStr);
         return (left << 16) | right;
      }
      else if (ctx.DEC() != null) {
         return toLong(ctx.com);
      }
      else if (ctx.INTERNET() != null) {
         return 0l;
      }
      else if (ctx.GSHUT() != null) {
         return 0xFFFFFF04l;
      }
      else if (ctx.LOCAL_AS() != null) {
         return 0xFFFFFF03l;
      }
      else if (ctx.NO_ADVERTISE() != null) {
         return 0xFFFFFF02l;
      }
      else if (ctx.NO_EXPORT() != null) {
         return 0xFFFFFF01l;
      }
      else {
         throw convError(Long.class, ctx);
      }
   }

   private IntExpr toMetricIntExpr(Int_exprContext ctx) {
      if (ctx.DEC() != null) {
         int val = toInteger(ctx.DEC());
         if (ctx.PLUS() != null) {
            return new IncrementMetric(val);
         }
         else if (ctx.DASH() != null) {
            return new DecrementMetric(val);
         }
         else {
            return new LiteralInt(val);
         }
      }
      else if (ctx.IGP_COST() != null) {
         return new IgpCost();
      }
      else if (ctx.RP_VARIABLE() != null) {
         return new VarInt(ctx.RP_VARIABLE().getText());
      }
      else {
         /*
          * Unsupported metric integer expression - do not add cases unless you
          * know what you are doing
          */
         throw convError(IntExpr.class, ctx);
      }
   }

   private NamedPort toNamedPort(PortContext ctx) {
      if (ctx.AOL() != null) {
         return NamedPort.AOL;
      }
      else if (ctx.BGP() != null) {
         return NamedPort.BGP;
      }
      else if (ctx.BIFF() != null) {
         return NamedPort.BIFFudp_OR_EXECtcp;
      }
      else if (ctx.BOOTPC() != null) {
         return NamedPort.BOOTPC;
      }
      else if (ctx.BOOTPS() != null) {
         return NamedPort.BOOTPS_OR_DHCP;
      }
      else if (ctx.CHARGEN() != null) {
         return NamedPort.CHARGEN;
      }
      else if (ctx.CITRIX_ICA() != null) {
         return NamedPort.CITRIX_ICA;
      }
      else if (ctx.CMD() != null) {
         return NamedPort.CMDtcp_OR_SYSLOGudp;
      }
      else if (ctx.CTIQBE() != null) {
         return NamedPort.CTIQBE;
      }
      else if (ctx.DAYTIME() != null) {
         return NamedPort.DAYTIME;
      }
      else if (ctx.DISCARD() != null) {
         return NamedPort.DISCARD;
      }
      else if (ctx.DNSIX() != null) {
         return NamedPort.DNSIX;
      }
      else if (ctx.DOMAIN() != null) {
         return NamedPort.DOMAIN;
      }
      else if (ctx.ECHO() != null) {
         return NamedPort.ECHO;
      }
      else if (ctx.EXEC() != null) {
         return NamedPort.BIFFudp_OR_EXECtcp;
      }
      else if (ctx.FINGER() != null) {
         return NamedPort.FINGER;
      }
      else if (ctx.FTP() != null) {
         return NamedPort.FTP;
      }
      else if (ctx.FTP_DATA() != null) {
         return NamedPort.FTP_DATA;
      }
      else if (ctx.GOPHER() != null) {
         return NamedPort.GOPHER;
      }
      else if (ctx.H323() != null) {
         return NamedPort.H323;
      }
      else if (ctx.HTTPS() != null) {
         return NamedPort.HTTPS;
      }
      else if (ctx.HOSTNAME() != null) {
         return NamedPort.HOSTNAME;
      }
      else if (ctx.IDENT() != null) {
         return NamedPort.IDENT;
      }
      else if (ctx.IMAP4() != null) {
         return NamedPort.IMAP;
      }
      else if (ctx.IRC() != null) {
         return NamedPort.IRC;
      }
      else if (ctx.ISAKMP() != null) {
         return NamedPort.ISAKMP;
      }
      else if (ctx.KERBEROS() != null) {
         return NamedPort.KERBEROS;
      }
      else if (ctx.KLOGIN() != null) {
         return NamedPort.KLOGIN;
      }
      else if (ctx.KSHELL() != null) {
         return NamedPort.KSHELL;
      }
      else if (ctx.LDAP() != null) {
         return NamedPort.LDAP;
      }
      else if (ctx.LDAPS() != null) {
         return NamedPort.LDAPS;
      }
      else if (ctx.LPD() != null) {
         return NamedPort.LPD;
      }
      else if (ctx.LOGIN() != null) {
         return NamedPort.LOGINtcp_OR_WHOudp;
      }
      else if (ctx.LOTUSNOTES() != null) {
         return NamedPort.LOTUSNOTES;
      }
      else if (ctx.MICROSOFT_DS() != null) {
         return NamedPort.MICROSOFT_DS;
      }
      else if (ctx.MLAG() != null) {
         return NamedPort.MLAG;
      }
      else if (ctx.MOBILE_IP() != null) {
         return NamedPort.MOBILE_IP_AGENT;
      }
      else if (ctx.MSRPC() != null) {
         return NamedPort.MSRPC;
      }
      else if (ctx.NAMESERVER() != null) {
         return NamedPort.NAMESERVER;
      }
      else if (ctx.NETBIOS_DGM() != null) {
         return NamedPort.NETBIOS_DGM;
      }
      else if (ctx.NETBIOS_NS() != null) {
         return NamedPort.NETBIOS_NS;
      }
      else if (ctx.NETBIOS_SS() != null) {
         return NamedPort.NETBIOS_SSN;
      }
      else if (ctx.NETBIOS_SSN() != null) {
         return NamedPort.NETBIOS_SSN;
      }
      else if (ctx.NNTP() != null) {
         return NamedPort.NNTP;
      }
      else if (ctx.NON500_ISAKMP() != null) {
         return NamedPort.NON500_ISAKMP;
      }
      else if (ctx.NTP() != null) {
         return NamedPort.NTP;
      }
      else if (ctx.PCANYWHERE_DATA() != null) {
         return NamedPort.PCANYWHERE_DATA;
      }
      else if (ctx.PCANYWHERE_STATUS() != null) {
         return NamedPort.PCANYWHERE_STATUS;
      }
      else if (ctx.PIM_AUTO_RP() != null) {
         return NamedPort.PIM_AUTO_RP;
      }
      else if (ctx.POP2() != null) {
         return NamedPort.POP2;
      }
      else if (ctx.POP3() != null) {
         return NamedPort.POP3;
      }
      else if (ctx.PPTP() != null) {
         return NamedPort.PPTP;
      }
      else if (ctx.RADIUS() != null) {
         return NamedPort.RADIUS_CISCO;
      }
      else if (ctx.RADIUS_ACCT() != null) {
         return NamedPort.RADIUS_ACCT_CISCO;
      }
      else if (ctx.RIP() != null) {
         return NamedPort.RIP;
      }
      else if (ctx.SECUREID_UDP() != null) {
         return NamedPort.SECUREID_UDP;
      }
      else if (ctx.SMTP() != null) {
         return NamedPort.SMTP;
      }
      else if (ctx.SNMP() != null) {
         return NamedPort.SNMP;
      }
      else if (ctx.SNMP_TRAP() != null) {
         return NamedPort.SNMPTRAP;
      }
      else if (ctx.SNMPTRAP() != null) {
         return NamedPort.SNMPTRAP;
      }
      else if (ctx.SQLNET() != null) {
         return NamedPort.SQLNET;
      }
      else if (ctx.SSH() != null) {
         return NamedPort.SSH;
      }
      else if (ctx.SUNRPC() != null) {
         return NamedPort.SUNRPC;
      }
      else if (ctx.SYSLOG() != null) {
         return NamedPort.CMDtcp_OR_SYSLOGudp;
      }
      else if (ctx.TACACS() != null) {
         return NamedPort.TACACS;
      }
      else if (ctx.TACACS_DS() != null) {
         return NamedPort.TACACS_DS;
      }
      else if (ctx.TALK() != null) {
         return NamedPort.TALK;
      }
      else if (ctx.TELNET() != null) {
         return NamedPort.TELNET;
      }
      else if (ctx.TFTP() != null) {
         return NamedPort.TFTP;
      }
      else if (ctx.TIME() != null) {
         return NamedPort.TIME;
      }
      else if (ctx.UUCP() != null) {
         return NamedPort.UUCP;
      }
      else if (ctx.WHO() != null) {
         return NamedPort.LOGINtcp_OR_WHOudp;
      }
      else if (ctx.WHOIS() != null) {
         return NamedPort.WHOIS;
      }
      else if (ctx.WWW() != null) {
         return NamedPort.HTTP;
      }
      else if (ctx.XDMCP() != null) {
         return NamedPort.XDMCP;
      }
      else {
         throw convError(NamedPort.class, ctx);
      }
   }

   private OriginExpr toOriginExpr(Origin_expr_literalContext ctx) {
      OriginType originType;
      Integer asNum = null;
      LiteralOrigin originExpr;
      if (ctx.IGP() != null) {
         originType = OriginType.IGP;
      }
      else if (ctx.INCOMPLETE() != null) {
         originType = OriginType.INCOMPLETE;
      }
      else if (ctx.as != null) {
         asNum = toInteger(ctx.as);
         originType = OriginType.IGP;
      }
      else {
         throw convError(OriginExpr.class, ctx);
      }
      originExpr = new LiteralOrigin(originType, asNum);
      return originExpr;
   }

   private OriginExpr toOriginExpr(Origin_exprContext ctx) {
      if (ctx.origin_expr_literal() != null) {
         return toOriginExpr(ctx.origin_expr_literal());
      }
      else if (ctx.RP_VARIABLE() != null) {
         return new VarOrigin(ctx.RP_VARIABLE().getText());
      }
      else {
         throw convError(OriginExpr.class, ctx);
      }
   }

   private OspfMetricType toOspfMetricType(Rp_ospf_metric_typeContext ctx) {
      if (ctx.TYPE_1() != null) {
         return OspfMetricType.E1;
      }
      else if (ctx.TYPE_2() != null) {
         return OspfMetricType.E2;
      }
      else {
         throw convError(OspfMetricType.class, ctx);
      }
   }

   private List<SubRange> toPortRanges(Port_specifierContext ps) {
      List<SubRange> ranges = new ArrayList<>();
      if (ps.EQ() != null) {
         for (PortContext pc : ps.args) {
            int port = getPortNumber(pc);
            ranges.add(new SubRange(port, port));
         }
      }
      else if (ps.GT() != null) {
         int port = getPortNumber(ps.arg);
         ranges.add(new SubRange(port + 1, 65535));
      }
      else if (ps.NEQ() != null) {
         int port = getPortNumber(ps.arg);
         SubRange beforeRange = new SubRange(0, port - 1);
         SubRange afterRange = new SubRange(port + 1, 65535);
         ranges.add(beforeRange);
         ranges.add(afterRange);
      }
      else if (ps.LT() != null) {
         int port = getPortNumber(ps.arg);
         ranges.add(new SubRange(0, port - 1));
      }
      else if (ps.RANGE() != null) {
         int lowPort = getPortNumber(ps.arg1);
         int highPort = getPortNumber(ps.arg2);
         ranges.add(new SubRange(lowPort, highPort));
      }
      else {
         throw convError(List.class, ps);
      }
      return ranges;
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_and_rp_stanzaContext ctx) {
      if (ctx.AND() == null) {
         return toRoutePolicyBoolean(ctx.boolean_not_rp_stanza());
      }
      else {
         return new RoutePolicyBooleanAnd(
               toRoutePolicyBoolean(ctx.boolean_and_rp_stanza()),
               toRoutePolicyBoolean(ctx.boolean_not_rp_stanza()));
      }
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_apply_rp_stanzaContext ctx) {
      return new RoutePolicyBooleanApply(ctx.name.getText());
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_as_path_in_rp_stanzaContext ctx) {
      AsPathSetExpr asPathSetExpr = toAsPathSetExpr(ctx.expr);
      int expressionLine = ctx.expr.getStart().getLine();
      return new RoutePolicyBooleanAsPathIn(asPathSetExpr, expressionLine);
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_as_path_neighbor_is_rp_stanzaContext ctx) {
      List<SubRangeExpr> range = ctx.as_range_expr().subranges.stream()
            .map(sr -> toSubRangeExpr(sr)).collect(Collectors.toList());
      boolean exact = ctx.as_range_expr().EXACT() != null;
      return new RoutePolicyBooleanAsPathNeighborIs(range, exact);
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_as_path_originates_from_rp_stanzaContext ctx) {
      List<SubRangeExpr> range = ctx.as_range_expr().subranges.stream()
            .map(sr -> toSubRangeExpr(sr)).collect(Collectors.toList());
      boolean exact = ctx.as_range_expr().EXACT() != null;
      return new RoutePolicyBooleanAsPathOriginatesFrom(range, exact);

   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_as_path_passes_through_rp_stanzaContext ctx) {
      List<SubRangeExpr> range = ctx.as_range_expr().subranges.stream()
            .map(sr -> toSubRangeExpr(sr)).collect(Collectors.toList());
      boolean exact = ctx.as_range_expr().EXACT() != null;
      return new RoutePolicyBooleanAsPathPassesThrough(range, exact);
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_community_matches_any_rp_stanzaContext ctx) {
      RoutePolicyCommunitySet communitySet = toRoutePolicyCommunitySet(
            ctx.rp_community_set());
      return new RoutePolicyBooleanCommunityMatchesAny(communitySet);
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_community_matches_every_rp_stanzaContext ctx) {
      RoutePolicyCommunitySet communitySet = toRoutePolicyCommunitySet(
            ctx.rp_community_set());
      return new RoutePolicyBooleanCommunityMatchesEvery(communitySet);
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_destination_rp_stanzaContext ctx) {
      RoutePolicyPrefixSet prefixSet = toRoutePolicyPrefixSet(
            ctx.rp_prefix_set());
      return new RoutePolicyBooleanDestination(prefixSet);
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_local_preference_rp_stanzaContext ctx) {
      IntComparator cmp = toIntComparator(ctx.int_comp());
      IntExpr rhs = toCommonIntExpr(ctx.rhs);
      return new RoutePolicyBooleanLocalPreference(cmp, rhs);
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_med_rp_stanzaContext ctx) {
      IntComparator cmp = toIntComparator(ctx.int_comp());
      IntExpr rhs = toCommonIntExpr(ctx.rhs);
      return new RoutePolicyBooleanMed(cmp, rhs);
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_next_hop_in_rp_stanzaContext ctx) {
      RoutePolicyPrefixSet prefixSet = toRoutePolicyPrefixSet(
            ctx.rp_prefix_set());
      return new RoutePolicyBooleanNextHopIn(prefixSet);
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_not_rp_stanzaContext ctx) {
      if (ctx.NOT() == null) {
         return toRoutePolicyBoolean(ctx.boolean_simple_rp_stanza());
      }
      else {
         return new RoutePolicyBooleanNot(
               toRoutePolicyBoolean(ctx.boolean_simple_rp_stanza()));
      }
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_rib_has_route_rp_stanzaContext ctx) {
      return new RoutePolicyBooleanRibHasRoute(
            toRoutePolicyPrefixSet(ctx.rp_prefix_set()));
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_route_type_is_rp_stanzaContext ctx) {
      RouteTypeExpr type = toRouteType(ctx.type);
      return new RoutePolicyBooleanRouteTypeIs(type);
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_rp_stanzaContext ctx) {
      if (ctx.OR() == null) {
         return toRoutePolicyBoolean(ctx.boolean_and_rp_stanza());
      }
      else {
         return new RoutePolicyBooleanOr(
               toRoutePolicyBoolean(ctx.boolean_rp_stanza()),
               toRoutePolicyBoolean(ctx.boolean_and_rp_stanza()));
      }
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_simple_rp_stanzaContext ctx) {
      Boolean_rp_stanzaContext bctx = ctx.boolean_rp_stanza();
      if (bctx != null) {
         return toRoutePolicyBoolean(bctx);
      }

      Boolean_apply_rp_stanzaContext actx = ctx.boolean_apply_rp_stanza();
      if (actx != null) {
         return toRoutePolicyBoolean(actx);
      }

      Boolean_as_path_in_rp_stanzaContext aictx = ctx
            .boolean_as_path_in_rp_stanza();
      if (aictx != null) {
         return toRoutePolicyBoolean(aictx);
      }

      Boolean_as_path_neighbor_is_rp_stanzaContext anctx = ctx
            .boolean_as_path_neighbor_is_rp_stanza();
      if (anctx != null) {
         return toRoutePolicyBoolean(anctx);
      }

      Boolean_as_path_originates_from_rp_stanzaContext aoctx = ctx
            .boolean_as_path_originates_from_rp_stanza();
      if (aoctx != null) {
         return toRoutePolicyBoolean(aoctx);
      }

      Boolean_as_path_passes_through_rp_stanzaContext apctx = ctx
            .boolean_as_path_passes_through_rp_stanza();
      if (apctx != null) {
         return toRoutePolicyBoolean(apctx);
      }

      Boolean_community_matches_any_rp_stanzaContext cmactx = ctx
            .boolean_community_matches_any_rp_stanza();
      if (cmactx != null) {
         return toRoutePolicyBoolean(cmactx);
      }

      Boolean_community_matches_every_rp_stanzaContext cmectx = ctx
            .boolean_community_matches_every_rp_stanza();
      if (cmectx != null) {
         return toRoutePolicyBoolean(cmectx);
      }

      Boolean_destination_rp_stanzaContext dctx = ctx
            .boolean_destination_rp_stanza();
      if (dctx != null) {
         return toRoutePolicyBoolean(dctx);
      }

      Boolean_local_preference_rp_stanzaContext lctx = ctx
            .boolean_local_preference_rp_stanza();
      if (lctx != null) {
         return toRoutePolicyBoolean(lctx);
      }

      Boolean_med_rp_stanzaContext mctx = ctx.boolean_med_rp_stanza();
      if (mctx != null) {
         return toRoutePolicyBoolean(mctx);
      }

      Boolean_next_hop_in_rp_stanzaContext nctx = ctx
            .boolean_next_hop_in_rp_stanza();
      if (nctx != null) {
         return toRoutePolicyBoolean(nctx);
      }

      Boolean_rib_has_route_rp_stanzaContext ribctx = ctx
            .boolean_rib_has_route_rp_stanza();
      if (ribctx != null) {
         return toRoutePolicyBoolean(ribctx);
      }

      Boolean_route_type_is_rp_stanzaContext routectx = ctx
            .boolean_route_type_is_rp_stanza();
      if (routectx != null) {
         return toRoutePolicyBoolean(routectx);
      }

      Boolean_tag_is_rp_stanzaContext tctx = ctx.boolean_tag_is_rp_stanza();
      if (tctx != null) {
         return toRoutePolicyBoolean(tctx);
      }

      throw convError(RoutePolicyBoolean.class, ctx);
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_tag_is_rp_stanzaContext ctx) {
      IntComparator cmp = toIntComparator(ctx.int_comp());
      IntExpr rhs = toTagIntExpr(ctx.int_expr());
      return new RoutePolicyBooleanTagIs(cmp, rhs);
   }

   private RoutePolicyCommunitySet toRoutePolicyCommunitySet(
         Rp_community_setContext ctx) {
      if (ctx.name != null) {
         // named
         return new RoutePolicyCommunitySetName(ctx.name.getText());
      }
      else {
         // inline
         return new RoutePolicyCommunitySetInline(
               ctx.elems.stream().map(elem -> toCommunitySetElemExpr(elem))
                     .collect(Collectors.toList()));
      }
   }

   private RoutePolicyElseBlock toRoutePolicyElseBlock(
         Else_rp_stanzaContext ctx) {
      List<RoutePolicyStatement> stmts = toRoutePolicyStatementList(
            ctx.rp_stanza());
      return new RoutePolicyElseBlock(stmts);

   }

   private RoutePolicyElseIfBlock toRoutePolicyElseIfBlock(
         Elseif_rp_stanzaContext ctx) {
      RoutePolicyBoolean b = toRoutePolicyBoolean(ctx.boolean_rp_stanza());
      List<RoutePolicyStatement> stmts = toRoutePolicyStatementList(
            ctx.rp_stanza());
      return new RoutePolicyElseIfBlock(b, stmts);

   }

   private RoutePolicyPrefixSet toRoutePolicyPrefixSet(
         Rp_prefix_setContext ctx) {
      if (ctx.name != null) {
         // named
         String name = ctx.name.getText();
         int expressionLine = ctx.name.getStart().getLine();
         return new RoutePolicyPrefixSetName(name, expressionLine);
      }
      else {
         // inline
         PrefixSpace prefixSpace = new PrefixSpace();
         Prefix6Space prefix6Space = new Prefix6Space();
         boolean ipv6 = false;
         for (Prefix_set_elemContext pctxt : ctx.elems) {
            int lower;
            int upper;
            Prefix prefix = null;
            Prefix6 prefix6 = null;
            if (pctxt.prefix != null) {
               prefix = new Prefix(pctxt.prefix.getText());
               lower = prefix.getPrefixLength();
               upper = Prefix.MAX_PREFIX_LENGTH;
            }
            else if (pctxt.ipa != null) {
               prefix = new Prefix(toIp(pctxt.ipa), Prefix.MAX_PREFIX_LENGTH);
               lower = prefix.getPrefixLength();
               upper = Prefix.MAX_PREFIX_LENGTH;
            }
            else if (pctxt.ipv6a != null) {
               prefix6 = new Prefix6(
                     toIp6(pctxt.ipv6a),
                     Prefix6.MAX_PREFIX_LENGTH);
               lower = prefix6.getPrefixLength();
               upper = Prefix6.MAX_PREFIX_LENGTH;
            }
            else if (pctxt.ipv6_prefix != null) {
               prefix6 = new Prefix6(pctxt.ipv6_prefix.getText());
               lower = prefix6.getPrefixLength();
               upper = Prefix6.MAX_PREFIX_LENGTH;
            }
            else {
               throw new BatfishException("Unhandled alternative");
            }
            if (pctxt.minpl != null) {
               lower = toInteger(pctxt.minpl);
            }
            if (pctxt.maxpl != null) {
               upper = toInteger(pctxt.maxpl);
            }
            if (pctxt.eqpl != null) {
               lower = toInteger(pctxt.eqpl);
               upper = lower;
            }
            if (prefix != null) {
               prefixSpace.addPrefixRange(
                     new PrefixRange(prefix, new SubRange(lower, upper)));
            }
            else {
               prefix6Space.addPrefix6Range(
                     new Prefix6Range(prefix6, new SubRange(lower, upper)));
               ipv6 = true;
            }
         }
         if (ipv6) {
            return new RoutePolicyInlinePrefix6Set(prefix6Space);
         }
         else {
            return new RoutePolicyInlinePrefixSet(prefixSpace);
         }

      }
   }

   private RoutePolicyApplyStatement toRoutePolicyStatement(
         Apply_rp_stanzaContext ctx) {
      return new RoutePolicyApplyStatement(ctx.name.getText());
   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Delete_rp_stanzaContext ctx) {
      if (ctx.ALL() != null) {
         return new RoutePolicyDeleteAllStatement();
      }
      else {
         boolean negated = (ctx.NOT() != null);
         return new RoutePolicyDeleteCommunityStatement(
               negated,
               toRoutePolicyCommunitySet(ctx.rp_community_set()));
      }
   }

   private RoutePolicyDispositionStatement toRoutePolicyStatement(
         Disposition_rp_stanzaContext ctx) {
      RoutePolicyDispositionType t = null;
      if (ctx.DONE() != null) {
         t = RoutePolicyDispositionType.DONE;
      }
      else if (ctx.DROP() != null) {
         t = RoutePolicyDispositionType.DROP;
      }
      else if (ctx.PASS() != null) {
         t = RoutePolicyDispositionType.PASS;
      }
      return new RoutePolicyDispositionStatement(t);
   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Hash_commentContext ctx) {
      String text = ctx.RAW_TEXT().getText();
      return new RoutePolicyComment(text);
   }

   private RoutePolicyIfStatement toRoutePolicyStatement(
         If_rp_stanzaContext ctx) {
      RoutePolicyBoolean b = toRoutePolicyBoolean(ctx.boolean_rp_stanza());
      List<RoutePolicyStatement> stmts = toRoutePolicyStatementList(
            ctx.rp_stanza());
      List<RoutePolicyElseIfBlock> elseIfs = new ArrayList<>();
      for (Elseif_rp_stanzaContext ectxt : ctx.elseif_rp_stanza()) {
         elseIfs.add(toRoutePolicyElseIfBlock(ectxt));
      }
      RoutePolicyElseBlock els = null;
      Else_rp_stanzaContext elctxt = ctx.else_rp_stanza();
      if (elctxt != null) {
         els = toRoutePolicyElseBlock(elctxt);
      }
      return new RoutePolicyIfStatement(b, stmts, elseIfs, els);

   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Prepend_as_path_rp_stanzaContext ctx) {
      AsExpr expr = toAsExpr(ctx.as);
      IntExpr number = null;
      if (ctx.number != null) {
         number = toIntExpr(ctx.number);
      }
      return new RoutePolicyPrependAsPath(expr, number);
   }

   private RoutePolicyStatement toRoutePolicyStatement(Rp_stanzaContext ctx) {
      Apply_rp_stanzaContext actx = ctx.apply_rp_stanza();
      if (actx != null) {
         return toRoutePolicyStatement(actx);
      }

      Delete_rp_stanzaContext dectx = ctx.delete_rp_stanza();
      if (dectx != null) {
         return toRoutePolicyStatement(dectx);
      }

      Disposition_rp_stanzaContext dictx = ctx.disposition_rp_stanza();
      if (dictx != null) {
         return toRoutePolicyStatement(dictx);
      }

      Hash_commentContext hctx = ctx.hash_comment();
      if (hctx != null) {
         return toRoutePolicyStatement(hctx);
      }

      If_rp_stanzaContext ictx = ctx.if_rp_stanza();
      if (ictx != null) {
         return toRoutePolicyStatement(ictx);
      }

      Set_rp_stanzaContext sctx = ctx.set_rp_stanza();
      if (sctx != null) {
         return toRoutePolicyStatement(sctx);
      }

      throw convError(RoutePolicyStatement.class, ctx);
   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Set_community_rp_stanzaContext ctx) {
      RoutePolicyCommunitySet cset = toRoutePolicyCommunitySet(
            ctx.rp_community_set());
      boolean additive = (ctx.ADDITIVE() != null);
      return new RoutePolicySetCommunity(cset, additive);
   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Set_isis_metric_rp_stanzaContext ctx) {
      IntExpr metric = toCommonIntExpr(ctx.int_expr());
      return new RoutePolicySetIsisMetric(metric);
   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Set_level_rp_stanzaContext ctx) {
      return new RoutePolicySetLevel(toIsisLevelExpr(ctx.isis_level_expr()));
   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Set_local_preference_rp_stanzaContext ctx) {
      return new RoutePolicySetLocalPref(toLocalPreferenceIntExpr(ctx.pref));
   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Set_med_rp_stanzaContext ctx) {
      return new RoutePolicySetMed(toMetricIntExpr(ctx.med));
   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Set_metric_type_rp_stanzaContext ctx) {
      Rp_metric_typeContext t = ctx.type;
      if (t.rp_ospf_metric_type() != null) {
         return new RoutePolicySetOspfMetricType(
               toOspfMetricType(t.rp_ospf_metric_type()));
      }
      else if (t.rp_isis_metric_type() != null) {
         return new RoutePolicySetIsisMetricType(
               toIsisMetricType(t.rp_isis_metric_type()));
      }
      else if (t.RP_VARIABLE() != null) {
         return new RoutePolicySetVarMetricType(t.RP_VARIABLE().getText());
      }
      else {
         throw convError(RoutePolicyStatement.class, ctx);
      }
   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Set_next_hop_rp_stanzaContext ctx) {
      RoutePolicyNextHop hop = null;
      if (ctx.IP_ADDRESS() != null) {
         hop = new RoutePolicyNextHopIp(toIp(ctx.IP_ADDRESS()));
      }
      else if (ctx.IPV6_ADDRESS() != null) {
         hop = new RoutePolicyNextHopIP6(toIp6(ctx.IPV6_ADDRESS()));
      }
      else if (ctx.PEER_ADDRESS() != null) {
         hop = new RoutePolicyNextHopPeerAddress();
      }
      else if (ctx.SELF() != null) {
         hop = new RoutePolicyNextHopSelf();
      }

      boolean dest_vrf = (ctx.DESTINATION_VRF() != null);
      return new RoutePolicySetNextHop(hop, dest_vrf);

   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Set_origin_rp_stanzaContext ctx) {
      OriginExpr origin = toOriginExpr(ctx.origin_expr());
      return new RoutePolicySetOrigin(origin);
   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Set_rp_stanzaContext ctx) {
      Prepend_as_path_rp_stanzaContext pasctx = ctx.prepend_as_path_rp_stanza();
      if (pasctx != null) {
         return toRoutePolicyStatement(pasctx);
      }

      Set_community_rp_stanzaContext cctx = ctx.set_community_rp_stanza();
      if (cctx != null) {
         return toRoutePolicyStatement(cctx);
      }

      Set_isis_metric_rp_stanzaContext ictx = ctx.set_isis_metric_rp_stanza();
      if (ictx != null) {
         return toRoutePolicyStatement(ictx);
      }

      Set_level_rp_stanzaContext lctx = ctx.set_level_rp_stanza();
      if (lctx != null) {
         return toRoutePolicyStatement(lctx);
      }

      Set_local_preference_rp_stanzaContext lpctx = ctx
            .set_local_preference_rp_stanza();
      if (lpctx != null) {
         return toRoutePolicyStatement(lpctx);
      }

      Set_med_rp_stanzaContext medctx = ctx.set_med_rp_stanza();
      if (medctx != null) {
         return toRoutePolicyStatement(medctx);
      }

      Set_metric_type_rp_stanzaContext mctx = ctx.set_metric_type_rp_stanza();
      if (mctx != null) {
         return toRoutePolicyStatement(mctx);
      }

      Set_next_hop_rp_stanzaContext nhctx = ctx.set_next_hop_rp_stanza();
      if (nhctx != null) {
         return toRoutePolicyStatement(nhctx);
      }

      Set_origin_rp_stanzaContext octx = ctx.set_origin_rp_stanza();
      if (octx != null) {
         return toRoutePolicyStatement(octx);
      }

      Set_tag_rp_stanzaContext tctx = ctx.set_tag_rp_stanza();
      if (tctx != null) {
         return toRoutePolicyStatement(tctx);
      }

      Set_weight_rp_stanzaContext wctx = ctx.set_weight_rp_stanza();
      if (wctx != null) {
         return toRoutePolicyStatement(wctx);
      }

      throw convError(RoutePolicyStatement.class, ctx);
   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Set_tag_rp_stanzaContext ctx) {
      IntExpr tag = toTagIntExpr(ctx.tag);
      return new RoutePolicySetTag(tag);
   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Set_weight_rp_stanzaContext wctx) {
      IntExpr weight = toCommonIntExpr(wctx.weight);
      return new RoutePolicySetWeight(weight);
   }

   private List<RoutePolicyStatement> toRoutePolicyStatementList(
         List<Rp_stanzaContext> ctxts) {
      List<RoutePolicyStatement> stmts = new ArrayList<>();
      for (Rp_stanzaContext ctxt : ctxts) {
         RoutePolicyStatement stmt = toRoutePolicyStatement(ctxt);
         if (stmt != null) {
            stmts.add(stmt);
         }
      }
      return stmts;
   }

   private RouteTypeExpr toRouteType(Rp_route_typeContext ctx) {
      if (ctx.INTERAREA() != null) {
         return new LiteralRouteType(RouteType.INTERAREA);
      }
      else if (ctx.INTERNAL() != null) {
         return new LiteralRouteType(RouteType.INTERNAL);
      }
      else if (ctx.LEVEL_1() != null) {
         return new LiteralRouteType(RouteType.LEVEL_1);
      }
      else if (ctx.LEVEL_1_2() != null) {
         return new LiteralRouteType(RouteType.INTERAREA); // not a typo
      }
      else if (ctx.LEVEL_2() != null) {
         return new LiteralRouteType(RouteType.LEVEL_2);
      }
      else if (ctx.LOCAL() != null) {
         return new LiteralRouteType(RouteType.LOCAL);
      }
      else if (ctx.OSPF_EXTERNAL_TYPE_1() != null) {
         return new LiteralRouteType(RouteType.OSPF_EXTERNAL_TYPE_1);
      }
      else if (ctx.OSPF_EXTERNAL_TYPE_2() != null) {
         return new LiteralRouteType(RouteType.OSPF_EXTERNAL_TYPE_2);
      }
      else if (ctx.OSPF_INTER_AREA() != null) {
         return new LiteralRouteType(RouteType.OSPF_INTER_AREA);
      }
      else if (ctx.OSPF_INTRA_AREA() != null) {
         return new LiteralRouteType(RouteType.OSPF_INTRA_AREA);
      }
      else if (ctx.OSPF_NSSA_TYPE_1() != null) {
         return new LiteralRouteType(RouteType.OSPF_NSSA_TYPE_1);
      }
      else if (ctx.OSPF_NSSA_TYPE_2() != null) {
         return new LiteralRouteType(RouteType.OSPF_NSSA_TYPE_2);
      }
      else if (ctx.RP_VARIABLE() != null) {
         return new VarRouteType(ctx.RP_VARIABLE().getText());
      }
      else if (ctx.TYPE_1() != null) {
         return new LiteralRouteType(RouteType.TYPE_1);
      }
      else if (ctx.TYPE_2() != null) {
         return new LiteralRouteType(RouteType.TYPE_2);
      }
      else {
         throw convError(RouteTypeExpr.class, ctx);
      }
   }

   private SubRangeExpr toSubRangeExpr(Rp_subrangeContext ctx) {
      IntExpr first = toCommonIntExpr(ctx.first);
      IntExpr last = first;
      if (ctx.last != null) {
         last = toCommonIntExpr(ctx.first);
      }
      return new SubRangeExpr(first, last);
   }

   private IntExpr toTagIntExpr(Int_exprContext ctx) {
      if (ctx.DEC() != null && ctx.DASH() == null && ctx.PLUS() == null) {
         int val = toInteger(ctx.DEC());
         return new LiteralInt(val);
      }
      else if (ctx.RP_VARIABLE() != null) {
         String var = ctx.RP_VARIABLE().getText();
         return new VarInt(var);
      }
      else {
         throw convError(IntExpr.class, ctx);
      }
   }

}
