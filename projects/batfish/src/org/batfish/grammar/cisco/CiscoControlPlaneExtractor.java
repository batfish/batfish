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
import java.util.stream.Collectors;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.cisco.CiscoParser.*;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.DscpType;
import org.batfish.datamodel.IcmpCode;
import org.batfish.datamodel.IcmpType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
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
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.State;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.AsPathSetExpr;
import org.batfish.datamodel.routing_policy.expr.AutoAs;
import org.batfish.datamodel.routing_policy.expr.CommunitySetElem;
import org.batfish.datamodel.routing_policy.expr.CommunitySetElemHalfExpr;
import org.batfish.datamodel.routing_policy.expr.DecrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.DecrementMetric;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.IgpCost;
import org.batfish.datamodel.routing_policy.expr.IncrementLocalPreference;
import org.batfish.datamodel.routing_policy.expr.IncrementMetric;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunitySetElemHalf;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.NamedAsPathSet;
import org.batfish.datamodel.routing_policy.expr.OriginExpr;
import org.batfish.datamodel.routing_policy.expr.RangeCommunitySetElemHalf;
import org.batfish.datamodel.routing_policy.expr.VarAs;
import org.batfish.datamodel.routing_policy.expr.VarAsPathSet;
import org.batfish.datamodel.routing_policy.expr.VarCommunitySetElemHalf;
import org.batfish.datamodel.routing_policy.expr.VarInt;
import org.batfish.datamodel.routing_policy.expr.VarOrigin;
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
import org.batfish.datamodel.vendor_family.cisco.SnmpCommunity;
import org.batfish.datamodel.vendor_family.cisco.SnmpHost;
import org.batfish.datamodel.vendor_family.cisco.SnmpServer;
import org.batfish.datamodel.vendor_family.cisco.SshSettings;
import org.batfish.main.RedFlagBatfishException;
import org.batfish.main.Warnings;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.cisco.BgpAggregateNetwork;
import org.batfish.representation.cisco.BgpPeerGroup;
import org.batfish.representation.cisco.BgpProcess;
import org.batfish.representation.cisco.BgpRedistributionPolicy;
import org.batfish.representation.cisco.CiscoConfiguration;
import org.batfish.representation.cisco.CiscoVendorConfiguration;
import org.batfish.representation.cisco.DynamicBgpPeerGroup;
import org.batfish.representation.cisco.ExpandedCommunityList;
import org.batfish.representation.cisco.ExpandedCommunityListLine;
import org.batfish.representation.cisco.ExtendedAccessList;
import org.batfish.representation.cisco.ExtendedAccessListLine;
import org.batfish.representation.cisco.Interface;
import org.batfish.representation.cisco.IpAsPathAccessList;
import org.batfish.representation.cisco.IpAsPathAccessListLine;
import org.batfish.representation.cisco.IpBgpPeerGroup;
import org.batfish.representation.cisco.Ipv6BgpPeerGroup;
import org.batfish.representation.cisco.IsisProcess;
import org.batfish.representation.cisco.IsisRedistributionPolicy;
import org.batfish.representation.cisco.MasterBgpPeerGroup;
import org.batfish.representation.cisco.NamedBgpPeerGroup;
import org.batfish.representation.cisco.OspfNetwork;
import org.batfish.representation.cisco.OspfProcess;
import org.batfish.representation.cisco.OspfRedistributionPolicy;
import org.batfish.representation.cisco.OspfWildcardNetwork;
import org.batfish.representation.cisco.PrefixList;
import org.batfish.representation.cisco.PrefixListLine;
import org.batfish.representation.cisco.RouteMap;
import org.batfish.representation.cisco.RouteMapClause;
import org.batfish.representation.cisco.RouteMapMatchAsPathAccessListLine;
import org.batfish.representation.cisco.RouteMapMatchCommunityListLine;
import org.batfish.representation.cisco.RouteMapMatchIpAccessListLine;
import org.batfish.representation.cisco.RouteMapMatchIpPrefixListLine;
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
import org.batfish.representation.cisco.RoutePolicyBooleanAsPathIn;
import org.batfish.representation.cisco.RoutePolicyBooleanAsPathOriginatesFrom;
import org.batfish.representation.cisco.RoutePolicyBooleanAsPathPassesThrough;
import org.batfish.representation.cisco.RoutePolicyBooleanAnd;
import org.batfish.representation.cisco.RoutePolicyBooleanCommunityMatchesAny;
import org.batfish.representation.cisco.RoutePolicyBooleanCommunityMatchesEvery;
import org.batfish.representation.cisco.RoutePolicyBooleanDestination;
import org.batfish.representation.cisco.RoutePolicyBooleanNot;
import org.batfish.representation.cisco.RoutePolicyBooleanOr;
import org.batfish.representation.cisco.RoutePolicyBooleanRibHasRoute;
import org.batfish.representation.cisco.RoutePolicyBooleanTagIs;
import org.batfish.representation.cisco.RoutePolicyCommunitySet;
import org.batfish.representation.cisco.RoutePolicyCommunitySetName;
import org.batfish.representation.cisco.RoutePolicyCommunitySetInline;
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
import org.batfish.representation.cisco.RoutePolicyNextHopIp;
import org.batfish.representation.cisco.RoutePolicyNextHopIP6;
import org.batfish.representation.cisco.RoutePolicyNextHopPeerAddress;
import org.batfish.representation.cisco.RoutePolicyNextHopSelf;
import org.batfish.representation.cisco.RoutePolicyPrefixSet;
import org.batfish.representation.cisco.RoutePolicyPrefixSetName;
import org.batfish.representation.cisco.RoutePolicyPrependAsPath;
import org.batfish.representation.cisco.RoutePolicySetCommunity;
import org.batfish.representation.cisco.RoutePolicySetIsisMetricType;
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
import org.batfish.representation.cisco.StaticRoute;

public class CiscoControlPlaneExtractor extends CiscoParserBaseListener
      implements ControlPlaneExtractor {

   private static final Map<String, String> CISCO_INTERFACE_PREFIXES = getCiscoInterfacePrefixes();

   private static final int DEFAULT_STATIC_ROUTE_DISTANCE = 1;

   private static final Interface DUMMY_INTERFACE = new Interface("dummy");

   private static final String DUPLICATE = "DUPLICATE";

   private static final String F_ALLOWAS_IN_NUMBER = "bgp -  allowas-in with number - ignored and effectively infinite for now";

   private static final String F_BGP_AUTO_SUMMARY = "bgp - auto-summary";

   private static final String F_BGP_INHERIT_PEER_OTHER = "bgp - inherit peer - inheritance not implemented for this peer type";

   private static final String F_BGP_INHERIT_PEER_SESSION_OTHER = "bgp - inherit peer-session - inheritance not implemented for this peer type";

   private static final String F_BGP_MAXIMUM_PEERS = "bgp - maximum-peers";

   private static final String F_BGP_NEIGHBOR_DISTRIBUTE_LIST = "bgp - neighbor distribute-list";

   private static final String F_BGP_NETWORK_ROUTE_MAP = "bgp - network with route-map";

   private static final String F_BGP_NEXT_HOP_SELF = "bgp - (no) next-hop-self";

   private static final String F_BGP_REDISTRIBUTE_AGGREGATE = "bgp - redistribute aggregate";

   private static final String F_FRAGMENTS = "acl fragments";

   private static final String F_INTERFACE_MULTIPOINT = "interface multipoint";

   private static final String F_IP_DEFAULT_GATEWAY = "ip default-gateway";

   private static final String F_IP_ROUTE_VRF = "ip route vrf / vrf - ip route";

   private static final String F_IPV6 = "ipv6 - other";

   private static final String F_OSPF_AREA_NSSA = "ospf - not-so-stubby areas";

   private static final String F_OSPF_MAXIMUM_PATHS = "ospf - maximum-paths";

   private static final String F_OSPF_REDISTRIBUTE_RIP = "ospf - redistribute rip";

   private static final String F_OSPF_VRF = "router ospf vrf";

   private static final String F_RIP = "rip";

   private static final String F_ROUTE_MAP_SET_METRIC_TYPE = "route-map - set metric-type";

   private static final String F_SWITCHING_MODE = "switching-mode";

   private static final String F_TTL = "acl ttl eq number";

   private static final String NXOS_MANAGEMENT_INTERFACE_PREFIX = "mgmt";

   public static LineAction getAccessListAction(Access_list_actionContext ctx) {
      if (ctx.PERMIT() != null) {
         return LineAction.ACCEPT;
      }
      else if (ctx.DENY() != null) {
         return LineAction.REJECT;
      }
      else {
         throw new BatfishException("bad LineAction");
      }
   }

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
      prefixes.put("Bundle-Ether", "Bundle-Ethernet");
      prefixes.put("BVI", "BVI");
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
      prefixes.put("Port-channel", "Port-channel");
      prefixes.put("POS", "POS");
      prefixes.put("Serial", "Serial");
      prefixes.put("TenGigabitEthernet", "TenGigabitEthernet");
      prefixes.put("TenGigE", "TenGigabitEthernet");
      prefixes.put("te", "TenGigabitEthernet");
      prefixes.put("trunk", "trunk");
      prefixes.put("Tunnel", "Tunnel");
      prefixes.put("tunnel-te", "tunnel-te");
      prefixes.put("ve", "VirtualEthernet");
      prefixes.put("Virtual-Template", "Virtual-Template");
      prefixes.put("Vlan", "Vlan");
      return prefixes;
   }

   public static Ip getIp(Access_list_ip_rangeContext ctx) {
      if (ctx.ip != null) {
         return toIp(ctx.ip);
      }
      else if (ctx.prefix != null) {
         return getPrefixIp(ctx.prefix);
      }
      else {
         return new Ip(0l);
      }
   }

   public static NamedPort getNamedPort(PortContext ctx) {
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
         throw new BatfishException(
               "missing port-number mapping for port: '" + ctx.getText() + "'");
      }
   }

   public static int getPortNumber(PortContext ctx) {
      if (ctx.DEC() != null) {
         return toInteger(ctx.DEC());
      }
      else {
         NamedPort namedPort = getNamedPort(ctx);
         return namedPort.number();
      }
   }

   private static List<SubRange> getPortRanges(Port_specifierContext ps) {
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
         throw new BatfishException("bad port range");
      }
      return ranges;
   }

   public static Ip getPrefixIp(Token ipPrefixToken) {
      if (ipPrefixToken.getType() != CiscoLexer.IP_PREFIX) {
         throw new BatfishException(
               "attempted to get prefix length from non-IP_PREFIX token: "
                     + ipPrefixToken.getType() + " with text: '"
                     + ipPrefixToken.getText() + "'");
      }
      String text = ipPrefixToken.getText();
      String[] parts = text.split("/");
      String prefixIpStr = parts[0];
      Ip prefixIp = new Ip(prefixIpStr);
      return prefixIp;
   }

   public static int getPrefixLength(Token ipPrefixToken) {
      if (ipPrefixToken.getType() != CiscoLexer.IP_PREFIX) {
         throw new BatfishException(
               "attempted to get prefix length from non-IP_PREFIX token: "
                     + ipPrefixToken.getType());
      }
      String text = ipPrefixToken.getText();
      String[] parts = text.split("/");
      String prefixLengthStr = parts[1];
      int prefixLength = Integer.parseInt(prefixLengthStr);
      return prefixLength;
   }

   public static Ip getWildcard(Access_list_ip_rangeContext ctx) {
      if (ctx.wildcard != null) {
         return toIp(ctx.wildcard);
      }
      else if (ctx.ANY() != null || ctx.address_group != null) {
         return new Ip(0xFFFFFFFFl);
      }
      else if (ctx.HOST() != null) {
         return new Ip(0l);
      }
      else if (ctx.prefix != null) {
         int pfxLength = getPrefixLength(ctx.prefix);
         long ipAsLong = 0xFFFFFFFFl >>> pfxLength;
         return new Ip(ipAsLong);
      }
      else if (ctx.ip != null) {
         // basically same as host
         return new Ip(0l);
      }
      else {
         throw new BatfishException("bad extended ip access list ip range");
      }
   }

   private static AsExpr toAsExpr(As_exprContext ctx) {
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
         throw new BatfishException("Cannot convert '" + ctx.getText() + "' to "
               + AsExpr.class.getSimpleName());
      }
   }

   private static int toDscpType(Dscp_typeContext ctx) {
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
         throw new BatfishException(
               "Unhandled dscp type: '" + ctx.getText() + "'");
      }
      return val;
   }

   private static SwitchportEncapsulationType toEncapsulation(
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
         throw new BatfishException("bad encapsulation");
      }
   }

   public static int toInteger(TerminalNode t) {
      return Integer.parseInt(t.getText());
   }

   public static int toInteger(Token t) {
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

   public static Ip toIp(TerminalNode t) {
      return new Ip(t.getText());
   }

   public static Ip toIp(Token t) {
      return new Ip(t.getText());
   }

   public static Ip6 toIp6(TerminalNode t) {
      return new Ip6(t.getText());
   }

   public static Ip6 toIp6(Token t) {
      return new Ip6(t.getText());
   }

   public static IpProtocol toIpProtocol(ProtocolContext ctx) {
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
         throw new BatfishException("missing token-protocol mapping");
      }
   }

   public static long toLong(TerminalNode t) {
      return Long.parseLong(t.getText());
   }

   public static long toLong(Token t) {
      return Long.parseLong(t.getText());
   }

   public static List<SubRange> toRange(RangeContext ctx) {
      List<SubRange> range = new ArrayList<>();
      for (SubrangeContext sc : ctx.range_list) {
         SubRange sr = toSubRange(sc);
         range.add(sr);
      }
      return range;
   }

   private static SubRange toSubrange(As_path_regex_rangeContext ctx) {
      if (ctx.DEC() != null) {
         int as = toInteger(ctx.DEC());
         return new SubRange(as, as);
      }
      else if (ctx.PERIOD() != null) {
         return new SubRange(0, 65535);
      }
      else {
         throw new BatfishException("Invalid as path regex range");
      }
   }

   public static SubRange toSubRange(SubrangeContext ctx) {
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

   private AaaAccountingCommands _currentAaaAccountingCommands;

   private AaaAuthenticationLoginList _currentAaaAuthenticationLoginList;

   private IpAsPathAccessList _currentAsPathAcl;

   private DynamicBgpPeerGroup _currentDynamicPeerGroup;

   private ExpandedCommunityList _currentExpandedCommunityList;

   private ExtendedAccessList _currentExtendedAcl;

   private List<Interface> _currentInterfaces;

   private IpBgpPeerGroup _currentIpPeerGroup;

   private Ipv6BgpPeerGroup _currentIpv6PeerGroup;

   private Interface _currentIsisInterface;

   private IsisProcess _currentIsisProcess;

   private List<String> _currentLineNames;

   private NamedBgpPeerGroup _currentNamedPeerGroup;

   private final Set<String> _currentNexusNeighborAddressFamilies;

   private Long _currentOspfArea;

   private String _currentOspfInterface;

   private OspfProcess _currentOspfProcess;

   private BgpPeerGroup _currentPeerGroup;

   private NamedBgpPeerGroup _currentPeerSession;

   private PrefixList _currentPrefixList;

   private RouteMap _currentRouteMap;

   private RouteMapClause _currentRouteMapClause;

   private RoutePolicy _currentRoutePolicy;

   private SnmpCommunity _currentSnmpCommunity;

   @SuppressWarnings("unused")
   private SnmpHost _currentSnmpHost;

   private StandardAccessList _currentStandardAcl;

   private StandardCommunityList _currentStandardCommunityList;

   private String _currentVrf;

   private BgpPeerGroup _dummyPeerGroup;

   private boolean _inNexusNeighbor;

   private final BatfishCombinedParser<?, ?> _parser;

   private List<BgpPeerGroup> _peerGroupStack;

   private final String _text;

   private final Set<String> _unimplementedFeatures;

   private final boolean _unrecognizedAsRedFlag;

   private CiscoVendorConfiguration _vendorConfiguration;

   private final Warnings _w;

   public CiscoControlPlaneExtractor(String text,
         BatfishCombinedParser<?, ?> parser, Warnings warnings,
         boolean unrecognizedAsRedFlag) {
      _text = text;
      _parser = parser;
      _unimplementedFeatures = new TreeSet<>();
      _w = warnings;
      _peerGroupStack = new ArrayList<>();
      _unrecognizedAsRedFlag = unrecognizedAsRedFlag;
      _currentNexusNeighborAddressFamilies = new HashSet<>();
   }

   private void addInterface(String vrf, double bandwidth, int mtu,
         String name) {
      Interface newInterface = _configuration.getInterfaces().get(name);
      if (newInterface == null) {
         newInterface = new Interface(name);
         _configuration.getInterfaces().put(name, newInterface);
      }
      else {
         _w.pedantic("Interface: '" + name + "' altered more than once");
      }
      _currentInterfaces.add(newInterface);
      newInterface.setBandwidth(bandwidth);
      newInterface.setVrf(vrf);
      newInterface.setMtu(mtu);
   }

   private BatfishException convError(Class<?> type, ParserRuleContext ctx) {
      String typeName = type.getSimpleName();
      String txt = getFullText(ctx);
      return new BatfishException(
            "Could not convert to " + typeName + ": " + txt);
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
      String level;
      if (ctx.level != null) {
         level = ctx.level.getText();
      }
      else {
         level = AaaAccounting.DEFAULT_COMMANDS;
      }
      AaaAccountingCommands c = commands.get(level);
      if (c == null) {
         c = new AaaAccountingCommands();
         commands.put(level, _currentAaaAccountingCommands);
      }
      _currentAaaAccountingCommands = c;
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
      if (_inNexusNeighbor) {
         if (_currentNexusNeighborAddressFamilies.contains(addressFamilyStr)) {
            popPeer();
            _inNexusNeighbor = false;
            _currentNexusNeighborAddressFamilies.clear();
         }
         else {
            _currentNexusNeighborAddressFamilies.add(addressFamilyStr);
         }
      }
      Bgp_address_familyContext af = ctx.af;
      if (af.VPNV4() != null || af.VPNV6() != null || af.IPV6() != null
            || af.MDT() != null || af.MULTICAST() != null || af.VRF() != null) {
         pushPeer(_dummyPeerGroup);
      }
      else {
         pushPeer(_currentPeerGroup);
      }
   }

   @Override
   public void enterArea_xr_ro_stanza(Area_xr_ro_stanzaContext ctx) {
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
   public void enterCisco_configuration(Cisco_configurationContext ctx) {
      _vendorConfiguration = new CiscoVendorConfiguration(
            _unimplementedFeatures);
      _configuration = _vendorConfiguration;
      _currentVrf = CiscoConfiguration.MASTER_VRF_NAME;
   }

   @Override
   public void enterDescription_if_stanza(Description_if_stanzaContext ctx) {
      Token descriptionToken = ctx.description_line().text;
      String description = descriptionToken != null
            ? descriptionToken.getText().trim() : "";
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setDescription(description);
      }
   }

   @Override
   public void enterExtended_access_list_stanza(
         Extended_access_list_stanzaContext ctx) {
      boolean ipv6 = (ctx.IPV6() != null);

      if (ipv6) {
         todo(ctx, F_IPV6);
      }

      String name;
      if (ctx.name != null) {
         name = ctx.name.getText();
      }
      else if (ctx.num != null) {
         name = ctx.num.getText();
      }
      else {
         throw new BatfishException("Could not determine acl name");
      }
      _currentExtendedAcl = _configuration.getExtendedAcls().get(name);
      if (_currentExtendedAcl == null) {
         _currentExtendedAcl = new ExtendedAccessList(name);
         _currentExtendedAcl.setIpv6(ipv6);
         if (!ipv6) {
            _configuration.getExtendedAcls().put(name, _currentExtendedAcl);
         }
      }
   }

   @Override
   public void enterInterface_is_stanza(Interface_is_stanzaContext ctx) {
      String ifaceName = ctx.iname.getText();
      _currentIsisInterface = _configuration.getInterfaces().get(ifaceName);
      if (_currentIsisInterface == null) {
         _w.redFlag("IS-IS process references nonexistent interface: '"
               + ifaceName + "'");
         _currentIsisInterface = DUMMY_INTERFACE;
      }
      _currentIsisInterface.setIsisInterfaceMode(IsisInterfaceMode.ACTIVE);
   }

   @Override
   public void enterInterface_stanza(Interface_stanzaContext ctx) {
      String nameAlpha = ctx.iname.name_prefix_alpha.getText();
      String canonicalNamePrefix = getCanonicalInterfaceNamePrefix(nameAlpha);
      String vrf = canonicalNamePrefix.equals(NXOS_MANAGEMENT_INTERFACE_PREFIX)
            ? CiscoConfiguration.MANAGEMENT_VRF_NAME
            : CiscoConfiguration.MASTER_VRF_NAME;
      double bandwidth = Interface.getDefaultBandwidth(canonicalNamePrefix);
      int mtu = Interface.getDefaultMtu();

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
               addInterface(vrf, bandwidth, mtu, name);
            }
         }
      }
      else {
         String name = namePrefix;
         addInterface(vrf, bandwidth, mtu, name);
      }
      if (ctx.MULTIPOINT() != null) {
         todo(ctx, F_INTERFACE_MULTIPOINT);
      }
   }

   @Override
   public void enterInterface_xr_ro_stanza(Interface_xr_ro_stanzaContext ctx) {
      String ifaceName = getCanonicalInterfaceName(
            ctx.interface_name().getText());
      // may have to change if interfaces are ever declared after ospf
      Interface iface = _configuration.getInterfaces().get(ifaceName);
      if (iface == null) {
         throw new BatfishException(
               "fixme: reference to undefined interface in ospf area");
      }
      for (Prefix prefix : iface.getAllPrefixes()) {
         OspfNetwork network = new OspfNetwork(prefix, _currentOspfArea);
         _currentOspfProcess.getNetworks().add(network);
      }
      _currentOspfInterface = ifaceName;
   }

   @Override
   public void enterIp_as_path_access_list_stanza(
         Ip_as_path_access_list_stanzaContext ctx) {
      String name = ctx.name.getText();
      _currentAsPathAcl = _configuration.getAsPathAccessLists().get(name);
      if (_currentAsPathAcl == null) {
         _currentAsPathAcl = new IpAsPathAccessList(name);
         _configuration.getAsPathAccessLists().put(name, _currentAsPathAcl);
      }
   }

   @Override
   public void enterIp_community_list_expanded_stanza(
         Ip_community_list_expanded_stanzaContext ctx) {
      String name;
      if (ctx.num != null) {
         name = ctx.num.getText();
      }
      else if (ctx.name != null) {
         name = ctx.name.getText();
      }
      else {
         throw new BatfishException("Invalid community-list name");
      }
      _currentExpandedCommunityList = _configuration.getExpandedCommunityLists()
            .get(name);
      if (_currentExpandedCommunityList == null) {
         _currentExpandedCommunityList = new ExpandedCommunityList(name);
         _configuration.getExpandedCommunityLists().put(name,
               _currentExpandedCommunityList);
      }
   }

   @Override
   public void enterIp_community_list_standard_stanza(
         Ip_community_list_standard_stanzaContext ctx) {
      String name;
      if (ctx.num != null) {
         name = ctx.num.getText();
      }
      else if (ctx.name != null) {
         name = ctx.name.getText();
      }
      else {
         throw new BatfishException("Invalid standard community-list name");
      }
      _currentStandardCommunityList = _configuration.getStandardCommunityLists()
            .get(name);
      if (_currentStandardCommunityList == null) {
         _currentStandardCommunityList = new StandardCommunityList(name);
         _configuration.getStandardCommunityLists().put(name,
               _currentStandardCommunityList);
      }
   }

   @Override
   public void enterIp_prefix_list_stanza(Ip_prefix_list_stanzaContext ctx) {
      String name = ctx.name.getText();
      boolean isIpv6 = (ctx.IPV6() != null);
      if (isIpv6) {
         _currentPrefixList = null;
         todo(ctx, F_IPV6);
         return;
      }
      else {
         _currentPrefixList = _configuration.getPrefixLists().get(name);
         if (_currentPrefixList == null) {
            _currentPrefixList = new PrefixList(name);
            _configuration.getPrefixLists().put(name, _currentPrefixList);
         }
      }
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
   public void enterIp_ssh(Ip_sshContext ctx) {
      if (_configuration.getCf().getSsh() == null) {
         _configuration.getCf().setSsh(new SshSettings());
      }
   }

   @Override
   public void enterIs_type_is_stanza(Is_type_is_stanzaContext ctx) {
      IsisProcess proc = _configuration.getIsisProcess();
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
   public void enterNeighbor_group_rb_stanza(
         Neighbor_group_rb_stanzaContext ctx) {
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      String name = ctx.name.getText();
      _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
      if (_currentNamedPeerGroup == null) {
         proc.addNamedPeerGroup(name);
         _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
      }
      pushPeer(_currentNamedPeerGroup);
   }

   @Override
   public void enterNeighbor_rb_stanza(Neighbor_rb_stanzaContext ctx) {
      // do no further processing for unsupported address families / containers
      if (_currentPeerGroup == _dummyPeerGroup) {
         pushPeer(_dummyPeerGroup);
         return;
      }
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
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
               String message = "reference to undeclared peer group: '"
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
         todo(ctx, F_IPV6);
         _currentIpv6PeerGroup = Ipv6BgpPeerGroup.INSTANCE;
         pushPeer(_currentIpv6PeerGroup);
      }
      else if (ctx.peergroup != null) {
         String name = ctx.peergroup.getText();
         _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
         if (_currentNamedPeerGroup == null) {
            if (create) {
               proc.addNamedPeerGroup(name);
               _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
            }
            else {
               throw new BatfishException(
                     "reference to undeclared peer group: '" + name + "'");
            }
         }
         pushPeer(_currentNamedPeerGroup);
      }
      else {
         throw new BatfishException("unknown neighbor type");
      }
   }

   @Override
   public void enterNet_is_stanza(Net_is_stanzaContext ctx) {
      IsisProcess proc = _configuration.getIsisProcess();
      IsoAddress isoAddress = new IsoAddress(ctx.ISO_ADDRESS().getText());
      proc.setNetAddress(isoAddress);
   }

   @Override
   public void enterNexus_neighbor_rb_stanza(
         Nexus_neighbor_rb_stanzaContext ctx) {
      _currentNexusNeighborAddressFamilies.clear();
      _inNexusNeighbor = true;
      // do no further processing for unsupported address families / containers
      if (_currentPeerGroup == _dummyPeerGroup) {
         pushPeer(_dummyPeerGroup);
         return;
      }
      if (ctx.ipv6_address != null || ctx.ipv6_prefix != null) {
         todo(ctx, F_IPV6);
         _currentIpv6PeerGroup = Ipv6BgpPeerGroup.INSTANCE;
         pushPeer(_currentIpv6PeerGroup);
         return;
      }
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
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
         Ip ip = getPrefixIp(ctx.ip_prefix);
         int prefixLength = getPrefixLength(ctx.ip_prefix);
         Prefix prefix = new Prefix(ip, prefixLength);
         _currentDynamicPeerGroup = proc.getDynamicPeerGroups().get(prefix);
         if (_currentDynamicPeerGroup == null) {
            _currentDynamicPeerGroup = proc.addDynamicPeerGroup(prefix);
         }
         else {
            _w.redFlag("Duplicate DynamicIP peer group neighbor config (line:"
                  + ctx.start.getLine() + ")", DUPLICATE);
         }
         pushPeer(_currentDynamicPeerGroup);
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
   public void enterNexus_vrf_rb_stanza(Nexus_vrf_rb_stanzaContext ctx) {
      _currentVrf = ctx.name.getText();
      int procNum = _configuration.getBgpProcesses()
            .get(CiscoConfiguration.MASTER_VRF_NAME).getName();
      BgpProcess proc = new BgpProcess(procNum);
      _configuration.getBgpProcesses().put(_currentVrf, proc);
      pushPeer(proc.getMasterBgpPeerGroup());
      _currentNexusNeighborAddressFamilies.clear();
      _inNexusNeighbor = false;
   }

   @Override
   public void enterRoute_map_stanza(Route_map_stanzaContext ctx) {
      String name = ctx.name.getText();
      _currentRouteMap = _configuration.getRouteMaps().get(name);
      if (_currentRouteMap == null) {
         _currentRouteMap = new RouteMap(name);
         _configuration.getRouteMaps().put(name, _currentRouteMap);
      }
      int num = toInteger(ctx.num);
      LineAction action = getAccessListAction(ctx.rmt);
      _currentRouteMapClause = _currentRouteMap.getClauses().get(num);
      if (_currentRouteMapClause == null) {
         _currentRouteMapClause = new RouteMapClause(action,
               _currentRouteMap.getName(), num);
         _currentRouteMap.getClauses().put(num, _currentRouteMapClause);
      }
      else {
         _w.redFlag("Route map '" + _currentRouteMap.getName()
               + "' already contains clause numbered '" + num
               + "'. Duplicate clause will be merged with original clause.");
      }
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
      BgpProcess proc = new BgpProcess(procNum);
      _configuration.getBgpProcesses().put(_currentVrf, proc);
      _dummyPeerGroup = new MasterBgpPeerGroup();
      pushPeer(proc.getMasterBgpPeerGroup());
   }

   @Override
   public void enterRouter_isis_stanza(Router_isis_stanzaContext ctx) {
      _currentIsisProcess = new IsisProcess();
      _currentIsisProcess.setLevel(IsisLevel.LEVEL_1_2);
      _configuration.setIsisProcess(_currentIsisProcess);
   }

   @Override
   public void enterRouter_ospf_stanza(Router_ospf_stanzaContext ctx) {
      int procNum = toInteger(ctx.procnum);
      _currentOspfProcess = new OspfProcess(procNum);
      if (ctx.vrf != null) {
         todo(ctx, F_OSPF_VRF);
      }
      else {
         _configuration.setOspfProcess(_currentOspfProcess);
      }
   }

   @Override
   public void enterRouter_rip_stanza(Router_rip_stanzaContext ctx) {
      todo(ctx, F_RIP);
   }

   @Override
   public void enterS_aaa(S_aaaContext ctx) {
      if (_configuration.getCf().getAaa() == null) {
         _configuration.getCf().setAaa(new Aaa());
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
            nameBase += slot1 + "/";
            if (ctx.port1 != null) {
               port1 = toInteger(ctx.port1);
               nameBase += port1 + "/";
            }
         }
         int first = toInteger(ctx.first);
         int last;
         if (ctx.last != null) {
            if (ctx.slot2 != null) {
               slot2 = toInteger(ctx.slot2);
               if (!slot2.equals(slot1)) {
                  throw new BatfishException(
                        "Do not support changing slot number in line declaration");
               }
               if (ctx.port2 != null) {
                  port2 = toInteger(ctx.port2);
                  if (!port2.equals(port1)) {
                     throw new BatfishException(
                           "Do not support changing port number in line declaration");
                  }
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
         for (int i = first; i <= last; i++) {
            String name = nameBase + i;
            names.add(name);
         }
      }
      else {
         names.add(nameBase);
      }
      for (String name : names) {
         if (_configuration.getCf().getLines().get(name) == null) {
            Line line = new Line(name);
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
   }

   @Override
   public void enterS_ntp(S_ntpContext ctx) {
      if (_configuration.getCf().getNtp() == null) {
         _configuration.getCf().setNtp(new Ntp());
      }
   }

   @Override
   public void enterS_snmp_server(S_snmp_serverContext ctx) {
      if (_configuration.getCf().getSnmpServer() == null) {
         _configuration.getCf().setSnmpServer(new SnmpServer());
      }
   }

   @Override
   public void enterSs_community(Ss_communityContext ctx) {
      String name = ctx.name.getText();
      Map<String, SnmpCommunity> communities = _configuration.getCf()
            .getSnmpServer().getCommunities();
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
      Map<String, SnmpHost> hosts = _configuration.getCf().getSnmpServer()
            .getHosts();
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
      boolean ipv6 = (ctx.IPV6() != null);
      if (ctx.name != null) {
         name = ctx.name.getText();
      }
      else if (ctx.num != null) {
         name = ctx.num.getText();
      }
      else {
         throw new BatfishException("Invalid standard access-list name");
      }
      _currentStandardAcl = _configuration.getStandardAcls().get(name);
      if (_currentStandardAcl == null) {
         _currentStandardAcl = new StandardAccessList(name);
         _currentStandardAcl.setIpv6(ipv6);
         if (!ipv6) {
            _configuration.getStandardAcls().put(name, _currentStandardAcl);
         }
      }
   }

   @Override
   public void enterTemplate_peer_policy_rb_stanza(
         Template_peer_policy_rb_stanzaContext ctx) {
      String name = ctx.name.getText();
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
      if (_currentNamedPeerGroup == null) {
         proc.addNamedPeerGroup(name);
         _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
      }
      pushPeer(_currentNamedPeerGroup);
   }

   @Override
   public void enterTemplate_peer_rb_stanza(
         Template_peer_rb_stanzaContext ctx) {
      String name = ctx.name.getText();
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
      if (_currentNamedPeerGroup == null) {
         proc.addNamedPeerGroup(name);
         _currentNamedPeerGroup = proc.getNamedPeerGroups().get(name);
      }
      pushPeer(_currentNamedPeerGroup);
   }

   @Override
   public void enterTemplate_peer_session_rb_stanza(
         Template_peer_session_rb_stanzaContext ctx) {
      String name = ctx.name.getText();
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      _currentPeerSession = proc.getPeerSessions().get(name);
      if (_currentPeerSession == null) {
         proc.addPeerSession(name);
         _currentPeerSession = proc.getPeerSessions().get(name);
      }
      pushPeer(_currentPeerSession);
   }

   @Override
   public void enterVrf_context_stanza(Vrf_context_stanzaContext ctx) {
      _currentVrf = ctx.name.getText();
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
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      if (_currentPeerGroup != proc.getMasterBgpPeerGroup()) {
         _currentPeerGroup.setActive(true);
      }
      else {
         throw new BatfishException(
               "no peer or peer group to activate in this context");
      }
   }

   @Override
   public void exitAddress_family_rb_stanza(
         Address_family_rb_stanzaContext ctx) {
      popPeer();
   }

   @Override
   public void exitAggregate_address_rb_stanza(
         Aggregate_address_rb_stanzaContext ctx) {
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
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
            BgpAggregateNetwork net = new BgpAggregateNetwork(prefix);
            net.setAsSet(asSet);
            net.setSummaryOnly(summaryOnly);
            if (ctx.mapname != null) {
               String mapName = ctx.mapname.getText();
               net.setAttributeMap(mapName);
            }
            proc.getAggregateNetworks().put(prefix, net);
         }
         else if (ctx.ipv6_prefix != null) {
            todo(ctx, F_IPV6);
         }
      }
      else if (_currentIpPeerGroup != null || _currentNamedPeerGroup != null) {
         throw new BatfishException(
               "unexpected occurrence in peer group/neighbor context");

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
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      proc.setAlwaysCompareMed(true);
   }

   @Override
   public void exitArea_nssa_ro_stanza(Area_nssa_ro_stanzaContext ctx) {
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
   public void exitArea_xr_ro_stanza(Area_xr_ro_stanzaContext ctx) {
      _currentOspfArea = null;
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
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      if (ctx.IP_PREFIX() != null) {
         Ip ip = getPrefixIp(ctx.IP_PREFIX().getSymbol());
         int prefixLength = getPrefixLength(ctx.IP_PREFIX().getSymbol());
         Prefix prefix = new Prefix(ip, prefixLength);
         DynamicBgpPeerGroup pg = proc.addDynamicPeerGroup(prefix);
         NamedBgpPeerGroup namedGroup = proc.getNamedPeerGroups().get(name);
         if (namedGroup == null) {
            proc.addNamedPeerGroup(name);
            namedGroup = proc.getNamedPeerGroups().get(name);
         }
         namedGroup.addNeighborPrefix(prefix);
         if (ctx.as != null) {
            int remoteAs = toInteger(ctx.as);
            pg.setRemoteAs(remoteAs);
         }
      }
      else if (ctx.IPV6_PREFIX() != null) {
         todo(ctx, F_IPV6);
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
      if (ctx.name != null) {
         name = ctx.name.getText();
      }
      else {
         name = ctx.num.getText();
      }
      _configuration.getClassMapAccessGroups().add(name);
   }

   @Override
   public void exitCp_ip_access_group(Cp_ip_access_groupContext ctx) {
      String name = ctx.name.getText();
      _configuration.getControlPlaneAccessGroups().add(name);
   }

   @Override
   public void exitDefault_information_originate_rb_stanza(
         Default_information_originate_rb_stanzaContext ctx) {
      _currentPeerGroup.setDefaultOriginate(true);
   }

   @Override
   public void exitDefault_information_ro_stanza(
         Default_information_ro_stanzaContext ctx) {
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
         proc.setDefaultInformationOriginateMap(ctx.map.getText());
      }
   }

   @Override
   public void exitDefault_metric_bgp_tail(Default_metric_bgp_tailContext ctx) {
      int metric = toInteger(ctx.metric);
      _currentPeerGroup.setDefaultMetric(metric);
   }

   @Override
   public void exitDefault_originate_bgp_tail(
         Default_originate_bgp_tailContext ctx) {
      String mapName = ctx.map != null ? ctx.map.getText() : null;
      if (_currentIpv6PeerGroup != null) {
         todo(ctx, F_IPV6);
         return;
      }
      else {
         _currentPeerGroup.setDefaultOriginate(true);
         _currentPeerGroup.setDefaultOriginateMap(mapName);
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
   public void exitEbgp_multihop_bgp_tail(Ebgp_multihop_bgp_tailContext ctx) {
      _currentPeerGroup.setEbgpMultihop(true);
   }

   @Override
   public void exitEmpty_nexus_neighbor_address_family(
         Empty_nexus_neighbor_address_familyContext ctx) {
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

      if (_currentExtendedAcl.getIpv6()) {
         return;
      }
      LineAction action = getAccessListAction(ctx.ala);
      IpProtocol protocol = toIpProtocol(ctx.prot);
      switch (protocol) {
      case IPV6:
      case IPV6_FRAG:
      case IPV6_ICMP:
      case IPV6_NO_NXT:
      case IPV6_OPTS:
      case IPV6_ROUTE:
         _currentExtendedAcl.setIpv6(true);
         // $CASES-OMITTED$
      default:
         break;
      }
      Ip srcIp = getIp(ctx.srcipr);
      Ip srcWildcard = getWildcard(ctx.srcipr);
      Ip dstIp = getIp(ctx.dstipr);
      Ip dstWildcard = getWildcard(ctx.dstipr);
      String srcAddressGroup = getAddressGroup(ctx.srcipr);
      String dstAddressGroup = getAddressGroup(ctx.dstipr);
      List<SubRange> srcPortRanges = ctx.alps_src != null
            ? getPortRanges(ctx.alps_src) : Collections.<SubRange> emptyList();
      List<SubRange> dstPortRanges = ctx.alps_dst != null
            ? getPortRanges(ctx.alps_dst) : Collections.<SubRange> emptyList();
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
   public void exitHostname_stanza(Hostname_stanzaContext ctx) {
      StringBuilder sb = new StringBuilder();
      for (Token namePart : ctx.name_parts) {
         sb.append(namePart.getText());
      }
      String hostname = sb.toString();
      _configuration.setHostname(hostname);
      _configuration.getCf().setHostname(hostname);
   }

   @Override
   public void exitIf_ip_proxy_arp(If_ip_proxy_arpContext ctx) {
      boolean enabled = ctx.NO() == null;
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setProxyArp(enabled);
      }
   }

   @Override
   public void exitInherit_peer_policy_bgp_tail(
         Inherit_peer_policy_bgp_tailContext ctx) {
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      String groupName = ctx.name.getText();
      if (_currentIpPeerGroup != null) {
         _currentIpPeerGroup.setGroupName(groupName);
      }
      else if (_currentNamedPeerGroup != null) {
         // May not hit this since parser for peer-policy does not have
         // recursion.
         _currentNamedPeerGroup.setGroupName(groupName);
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
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      String groupName = ctx.name.getText();
      if (_currentIpPeerGroup != null) {
         _currentIpPeerGroup.setPeerSession(groupName);
      }
      else if (_currentNamedPeerGroup != null) {
         _currentNamedPeerGroup.setPeerSession(groupName);
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
   public void exitInterface_stanza(Interface_stanzaContext ctx) {
      _currentInterfaces = null;
   }

   @Override
   public void exitInterface_xr_ro_stanza(Interface_xr_ro_stanzaContext ctx) {
      _currentOspfInterface = null;
   }

   @Override
   public void exitInterface_xr_ro_tail(Interface_xr_ro_tailContext ctx) {
      if (ctx.PASSIVE() != null && ctx.ENABLE() != null) {
         _currentOspfProcess.getInterfaceBlacklist().add(_currentOspfInterface);
      }
   }

   @Override
   public void exitIp_access_group_if_stanza(
         Ip_access_group_if_stanzaContext ctx) {
      String name = ctx.name.getText();
      if (ctx.IN() != null || ctx.INGRESS() != null) {
         for (Interface currentInterface : _currentInterfaces) {
            currentInterface.setIncomingFilter(name);
         }
      }
      else if (ctx.OUT() != null || ctx.EGRESS() != null) {
         for (Interface currentInterface : _currentInterfaces) {
            currentInterface.setOutgoingFilter(name);
         }
      }
      else {
         throw new BatfishException("bad direction");
      }
   }

   @Override
   public void exitIp_address_if_stanza(Ip_address_if_stanzaContext ctx) {
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
         Prefix standbyPrefix = new Prefix(standbyAddress,
               prefix.getPrefixLength());
         for (Interface currentInterface : _currentInterfaces) {
            currentInterface.setStandbyPrefix(standbyPrefix);
         }
      }
   }

   @Override
   public void exitIp_address_secondary_if_stanza(
         Ip_address_secondary_if_stanzaContext ctx) {
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
   public void exitIp_as_path_access_list_stanza(
         Ip_as_path_access_list_stanzaContext ctx) {
      _currentAsPathAcl = null;
   }

   @Override
   public void exitIp_as_path_access_list_tail(
         Ip_as_path_access_list_tailContext ctx) {
      LineAction action = getAccessListAction(ctx.action);
      As_path_regexContext asPath = ctx.as_path_regex();
      if (asPath == null) {
         // not an as-path we can use right now
         return;
      }
      IpAsPathAccessListLine line = new IpAsPathAccessListLine(action);
      boolean atBeginning = asPath.CARAT() != null;
      boolean matchEmpty = asPath.ranges.size() == asPath.ASTERISK().size();
      line.setAtBeginning(atBeginning);
      line.setMatchEmpty(matchEmpty);
      switch (asPath.ranges.size()) {
      case 0:
         break;

      case 2:
         As_path_regex_rangeContext range2ctx = asPath.ranges.get(1);
         SubRange asRange2 = toSubrange(range2ctx);
         line.setAs2Range(asRange2);
      case 1:
         As_path_regex_rangeContext range1ctx = asPath.ranges.get(0);
         SubRange asRange1 = toSubrange(range1ctx);
         line.setAs1Range(asRange1);
         break;

      default:
         _w.redFlag(
               "Do not currently support more than two AS'es in Cisco as-path regexes");
      }
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
      LineAction action = getAccessListAction(ctx.ala);
      String regex = "";
      for (Token remainder : ctx.remainder) {
         regex += remainder.getText();
      }
      ExpandedCommunityListLine line = new ExpandedCommunityListLine(action,
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
      LineAction action = getAccessListAction(ctx.ala);
      List<Long> communities = new ArrayList<>();
      for (CommunityContext communityCtx : ctx.communities) {
         long community = toLong(communityCtx);
         communities.add(community);
      }
      StandardCommunityListLine line = new StandardCommunityListLine(action,
            communities);
      _currentStandardCommunityList.getLines().add(line);
   }

   @Override
   public void exitIp_default_gateway_stanza(
         Ip_default_gateway_stanzaContext ctx) {
      todo(ctx, F_IP_DEFAULT_GATEWAY);
   }

   @Override
   public void exitIp_domain_name(Ip_domain_nameContext ctx) {
      String name = ctx.name.getText();
      _configuration.getCf().setDomainName(name);
   }

   @Override
   public void exitIp_ospf_cost_if_stanza(Ip_ospf_cost_if_stanzaContext ctx) {
      int cost = toInteger(ctx.cost);
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setOspfCost(cost);
      }
   }

   @Override
   public void exitIp_ospf_dead_interval_if_stanza(
         Ip_ospf_dead_interval_if_stanzaContext ctx) {
      int seconds = toInteger(ctx.seconds);
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setOspfDeadInterval(seconds);
         currentInterface.setOspfHelloMultiplier(0);
      }
   }

   @Override
   public void exitIp_ospf_dead_interval_minimal_if_stanza(
         Ip_ospf_dead_interval_minimal_if_stanzaContext ctx) {
      int multiplier = toInteger(ctx.mult);
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setOspfDeadInterval(1);
         currentInterface.setOspfHelloMultiplier(multiplier);
      }
   }

   @Override
   public void exitIp_policy_if_stanza(Ip_policy_if_stanzaContext ctx) {
      String policyName = ctx.name.getText();
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setRoutingPolicy(policyName);
      }
   }

   @Override
   public void exitIp_prefix_list_stanza(Ip_prefix_list_stanzaContext ctx) {
      _currentPrefixList = null;
   }

   @Override
   public void exitIp_prefix_list_tail(Ip_prefix_list_tailContext ctx) {
      boolean ipv6 = ctx.ipv6_prefix != null;
      LineAction action = getAccessListAction(ctx.action);
      Prefix prefix = null;
      Prefix6 prefix6 = null;
      int prefixLength;
      if (ipv6) {
         prefix6 = new Prefix6(ctx.ipv6_prefix.getText());
         prefixLength = prefix6.getPrefixLength();
      }
      else {
         prefix = new Prefix(ctx.prefix.getText());
         prefixLength = prefix.getPrefixLength();
      }
      int minLen = prefixLength;
      int maxLen = prefixLength;
      if (ctx.minpl != null) {
         minLen = toInteger(ctx.minpl);
         maxLen = ipv6 ? 128 : 32;
      }
      if (ctx.maxpl != null) {
         maxLen = toInteger(ctx.maxpl);
      }
      if (ctx.eqpl != null) {
         minLen = toInteger(ctx.eqpl);
         maxLen = toInteger(ctx.eqpl);
      }
      SubRange lengthRange = new SubRange(minLen, maxLen);
      if (ipv6) {
         todo(ctx, F_IPV6);
      }
      else {
         PrefixListLine line = new PrefixListLine(action, prefix, lengthRange);
         _currentPrefixList.addLine(line);
      }
   }

   @Override
   public void exitIp_route_stanza(Ip_route_stanzaContext ctx) {
      if (ctx.vrf != null || ctx.MANAGEMENT() != null) {
         _currentVrf = CiscoConfiguration.MASTER_VRF_NAME;
      }
   }

   @Override
   public void exitIp_route_tail(Ip_route_tailContext ctx) {
      if (!_currentVrf.equals(CiscoConfiguration.MASTER_VRF_NAME)) {
         todo(ctx, F_IP_ROUTE_VRF);
         return;
      }
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
      Ip nextHopIp = null;
      String nextHopInterface = null;
      int distance = DEFAULT_STATIC_ROUTE_DISTANCE;
      Integer tag = null;
      Integer track = null;
      boolean permanent = ctx.perm != null;
      if (ctx.nexthopip != null) {
         nextHopIp = toIp(ctx.nexthopip);
      }
      else if (ctx.nexthopprefix != null) {
         nextHopIp = getPrefixIp(ctx.nexthopprefix);
      }
      if (ctx.nexthopint != null) {
         nextHopInterface = ctx.nexthopint.getText();
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
      _configuration.getStaticRoutes().add(route);
   }

   @Override
   public void exitIp_route_vrfc_stanza(Ip_route_vrfc_stanzaContext ctx) {
      todo(ctx, F_IP_ROUTE_VRF);
   }

   @Override
   public void exitIp_router_isis_if_stanza(
         Ip_router_isis_if_stanzaContext ctx) {
      for (Interface iface : _currentInterfaces) {
         iface.setIsisInterfaceMode(IsisInterfaceMode.ACTIVE);
      }
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
   public void exitIsis_metric_if_stanza(Isis_metric_if_stanzaContext ctx) {
      int metric = toInteger(ctx.metric);
      for (Interface iface : _currentInterfaces) {
         iface.setIsisCost(metric);
      }
   }

   @Override
   public void exitL_access_class(L_access_classContext ctx) {
      String name = ctx.name.getText();
      BiConsumer<Line, String> setter;
      if (ctx.OUT() != null || ctx.EGRESS() != null) {
         setter = Line::setOutputAccessList;
      }
      else {
         setter = Line::setInputAccessList;
      }
      for (String currentName : _currentLineNames) {
         Line line = _configuration.getCf().getLines().get(currentName);
         setter.accept(line, name);
      }
      _configuration.getLineAccessClassLists().add(name);
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
   public void exitL_login(L_loginContext ctx) {
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
      String protocol = ctx.prot.getText();
      BiConsumer<Line, String> setter;
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
         setter.accept(line, protocol);
      }
   }

   @Override
   public void exitLogging_buffered(Logging_bufferedContext ctx) {
      Integer size = null;
      Integer severityNum = null;
      String severity = null;
      if (ctx.size != null) {
         // something was parsed as buffer size but it could be logging severity
         // as well
         // it is buffer size if the value is greater than min buffer size
         // otherwise, it is logging severity
         int sizeRawNum = toInteger(ctx.size);
         if (sizeRawNum >= Logging.MIN_LOGGING_BUFFER_SIZE) {
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
      Logging logging = _configuration.getCf().getLogging();
      String hostname = ctx.hostname.getText();
      LoggingHost host = new LoggingHost(hostname);
      logging.getHosts().put(hostname, host);
   }

   @Override
   public void exitLogging_on(Logging_onContext ctx) {
      Logging logging = _configuration.getCf().getLogging();
      logging.setOn(true);
   }

   @Override
   public void exitLogging_source_interface(
         Logging_source_interfaceContext ctx) {
      Logging logging = _configuration.getCf().getLogging();
      String sourceInterface = toInterfaceName(ctx.interface_name());
      logging.setSourceInterface(sourceInterface);
   }

   @Override
   public void exitLogging_trap(Logging_trapContext ctx) {
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
      Set<String> names = new TreeSet<>();
      for (VariableContext name : ctx.name_list) {
         names.add(name.getText());
      }
      RouteMapMatchAsPathAccessListLine line = new RouteMapMatchAsPathAccessListLine(
            names);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMatch_community_list_rm_stanza(
         Match_community_list_rm_stanzaContext ctx) {
      Set<String> names = new TreeSet<>();
      for (VariableContext name : ctx.name_list) {
         names.add(name.getText());
      }
      RouteMapMatchCommunityListLine line = new RouteMapMatchCommunityListLine(
            names);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMatch_ip_access_list_rm_stanza(
         Match_ip_access_list_rm_stanzaContext ctx) {
      Set<String> names = new TreeSet<>();
      for (Token t : ctx.name_list) {
         names.add(t.getText());
      }
      RouteMapMatchIpAccessListLine line = new RouteMapMatchIpAccessListLine(
            names);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMatch_ip_prefix_list_rm_stanza(
         Match_ip_prefix_list_rm_stanzaContext ctx) {
      Set<String> names = new TreeSet<>();
      for (VariableContext t : ctx.name_list) {
         names.add(t.getText());
      }
      RouteMapMatchIpPrefixListLine line = new RouteMapMatchIpPrefixListLine(
            names);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMatch_ipv6_rm_stanza(Match_ipv6_rm_stanzaContext ctx) {
      _currentRouteMap.setIpv6(true);
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
   public void exitMaximum_paths_ro_stanza(Maximum_paths_ro_stanzaContext ctx) {
      todo(ctx, F_OSPF_MAXIMUM_PATHS);
      /*
       * Note that this is very difficult to enforce, and may not help the
       * analysis without major changes
       */
   }

   @Override
   public void exitMaximum_peers_bgp_tail(Maximum_peers_bgp_tailContext ctx) {
      todo(ctx, F_BGP_MAXIMUM_PEERS);
   }

   @Override
   public void exitMgmt_ip_access_group(Mgmt_ip_access_groupContext ctx) {
      String name = ctx.name.getText();
      _configuration.getManagementAccessGroups().add(name);
   }

   @Override
   public void exitMtu_if_stanza(Mtu_if_stanzaContext ctx) {
      int mtu = toInteger(ctx.DEC());
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setMtu(mtu);
      }
   }

   @Override
   public void exitNeighbor_group_rb_stanza(
         Neighbor_group_rb_stanzaContext ctx) {
      _currentIpPeerGroup = null;
      _currentIpv6PeerGroup = null;
      _currentNamedPeerGroup = null;
      popPeer();
   }

   @Override
   public void exitNeighbor_rb_stanza(Neighbor_rb_stanzaContext ctx) {
      _currentDynamicPeerGroup = null;
      _currentIpPeerGroup = null;
      _currentIpv6PeerGroup = null;
      _currentNamedPeerGroup = null;
      popPeer();
   }

   @Override
   public void exitNetwork_bgp_tail(Network_bgp_tailContext ctx) {
      if (ctx.mapname != null) {
         todo(ctx, F_BGP_NETWORK_ROUTE_MAP);
      }
      else {
         Prefix prefix;

         if (ctx.prefix != null) {
            prefix = new Prefix(ctx.prefix.getText());
         }
         else {
            Ip address = toIp(ctx.ip);
            Ip mask = (ctx.mask != null) ? toIp(ctx.mask)
                  : address.getClassMask();
            int prefixLength = mask.numSubnetBits();
            prefix = new Prefix(address, prefixLength);
         }
         _configuration.getBgpProcesses().get(_currentVrf).getNetworks()
               .add(prefix);
      }
   }

   @Override
   public void exitNetwork_ro_stanza(Network_ro_stanzaContext ctx) {
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
   public void exitNext_hop_self_bgp_tail(Next_hop_self_bgp_tailContext ctx) {
      todo(ctx, F_BGP_NEXT_HOP_SELF);
      // note that this rule matches "no next-hop-self"
   }

   @Override
   public void exitNexus_neighbor_address_family(
         Nexus_neighbor_address_familyContext ctx) {
      if (_inNexusNeighbor) {
         popPeer();
      }
      else {
         _currentNexusNeighborAddressFamilies.clear();
      }
   }

   @Override
   public void exitNexus_neighbor_inherit(Nexus_neighbor_inheritContext ctx) {
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      String groupName = ctx.name.getText();
      if (_currentIpPeerGroup != null) {
         _currentIpPeerGroup.setGroupName(groupName);
      }
      else if (_currentDynamicPeerGroup != null) {
         _currentDynamicPeerGroup.setGroupName(groupName);
      }
      else if (_currentPeerGroup == proc.getMasterBgpPeerGroup()) {
         throw new BatfishException("Invalid peer context for inheritance");
      }
      else if (_currentPeerGroup == Ipv6BgpPeerGroup.INSTANCE) {
         _configuration.getIpv6PeerGroups().add(groupName);
      }
      else {
         todo(ctx, F_BGP_INHERIT_PEER_OTHER);
      }
   }

   @Override
   public void exitNexus_neighbor_rb_stanza(
         Nexus_neighbor_rb_stanzaContext ctx) {
      _currentDynamicPeerGroup = null;
      _currentIpPeerGroup = null;
      _currentIpv6PeerGroup = null;
      _currentNamedPeerGroup = null;
      if (_inNexusNeighbor) {
         popPeer();
      }
      else {
         _inNexusNeighbor = false;
      }
   }

   @Override
   public void exitNexus_vrf_rb_stanza(Nexus_vrf_rb_stanzaContext ctx) {
      _currentVrf = CiscoConfiguration.MASTER_VRF_NAME;
      popPeer();
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
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      if (ctx.ip != null) {
         Ip ip = toIp(ctx.ip);
         IpBgpPeerGroup pg = proc.getIpPeerGroups().get(ip);
         if (pg == null) {
            String message = "reference to undefined ip peer group: "
                  + ip.toString();
            _w.redFlag(message);
         }
         else {
            pg.setActive(false);
         }
      }
      else if (ctx.ip6 != null) {
         todo(ctx, F_IPV6);
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
      }
   }

   @Override
   public void exitNo_neighbor_shutdown_rb_stanza(
         No_neighbor_shutdown_rb_stanzaContext ctx) {
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      if (ctx.ip != null) {
         Ip ip = toIp(ctx.ip);
         IpBgpPeerGroup pg = proc.getIpPeerGroups().get(ip);
         // TODO: see if it is always ok to set active on 'no shutdown'
         if (pg == null) {
            String message = "reference to undefined ip peer group: "
                  + ip.toString();
            _w.redFlag(message);
         }
         else {
            pg.setActive(true);
            pg.setShutdown(false);
         }
      }
      else if (ctx.ip6 != null) {
         todo(ctx, F_IPV6);
      }
      else if (ctx.peergroup != null) {
         _w.redFlag("'no shutdown' of  peer group unsupported");
      }
   }

   @Override
   public void exitNo_redistribute_connected_rb_stanza(
         No_redistribute_connected_rb_stanzaContext ctx) {
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
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
      _configuration.getNtpAccessGroups().add(name);
   }

   @Override
   public void exitNtp_server(Ntp_serverContext ctx) {
      Ntp ntp = _configuration.getCf().getNtp();
      String hostname = ctx.hostname.getText();
      NtpServer server = ntp.getServers().get(hostname);
      if (server == null) {
         server = new NtpServer();
         ntp.getServers().put(hostname, server);
      }
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
   public void exitPassive_interface_default_ro_stanza(
         Passive_interface_default_ro_stanzaContext ctx) {
      _currentOspfProcess.setPassiveInterfaceDefault(true);
   }

   @Override
   public void exitPassive_interface_is_stanza(
         Passive_interface_is_stanzaContext ctx) {
      String ifaceName = ctx.name.getText();

      if (ifaceName.equals("default")) {
         for (Interface iface : _configuration.getInterfaces().values()) {
            iface.setIsisInterfaceMode(IsisInterfaceMode.PASSIVE);
         }
      }
      else {
         String canonicalIfaceName = getCanonicalInterfaceName(ifaceName);
         Interface iface = _configuration.getInterfaces()
               .get(canonicalIfaceName);
         if (iface != null) {
            if (ctx.NO() == null) {
               iface.setIsisInterfaceMode(IsisInterfaceMode.PASSIVE);
            }
            else {
               iface.setIsisInterfaceMode(IsisInterfaceMode.ACTIVE);
            }
         }
         else {
            throw new BatfishException(
                  "FIXME: Reference to undefined interface: '"
                        + canonicalIfaceName + "'");
         }
      }
   }

   @Override
   public void exitPassive_interface_ro_stanza(
         Passive_interface_ro_stanzaContext ctx) {
      boolean passive = ctx.NO() == null;
      String iname = ctx.i.getText();
      OspfProcess proc = _currentOspfProcess;
      if (passive) {
         proc.getInterfaceBlacklist().add(iname);
      }
      else {
         proc.getInterfaceWhitelist().add(iname);
      }
   }

   @Override
   public void exitPeer_group_assignment_rb_stanza(
         Peer_group_assignment_rb_stanzaContext ctx) {
      String peerGroupName = ctx.name.getText();
      if (ctx.address != null) {
         Ip address = toIp(ctx.address);
         _configuration.getBgpProcesses().get(_currentVrf)
               .addPeerGroupMember(address, peerGroupName);
      }
      else if (ctx.address6 != null) {
         todo(ctx, F_IPV6);
         _configuration.getIpv6PeerGroups().add(peerGroupName);
      }
   }

   @Override
   public void exitPeer_group_creation_rb_stanza(
         Peer_group_creation_rb_stanzaContext ctx) {
      String name = ctx.name.getText();
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      if (proc.getNamedPeerGroups().get(name) == null) {
         proc.addNamedPeerGroup(name);
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
      _configuration.getMsdpPeerSaLists().add(name);
   }

   @Override
   public void exitPim_accept_register(Pim_accept_registerContext ctx) {
      String name = ctx.name.getText();
      if (ctx.LIST() != null) {
         _configuration.getPimAcls().add(name);
      }
      else if (ctx.ROUTE_MAP() != null) {
         _configuration.getPimRouteMaps().add(name);
      }
   }

   @Override
   public void exitPim_accept_rp(Pim_accept_rpContext ctx) {
      if (ctx.name != null) {
         String name = ctx.name.getText();
         _configuration.getPimAcls().add(name);
      }
   }

   @Override
   public void exitPim_rp_address(Pim_rp_addressContext ctx) {
      if (ctx.name != null) {
         String name = ctx.name.getText();
         _configuration.getPimAcls().add(name);
      }
   }

   @Override
   public void exitPim_rp_announce_filter(Pim_rp_announce_filterContext ctx) {
      String name = ctx.name.getText();
      _configuration.getPimAcls().add(name);
   }

   @Override
   public void exitPim_rp_candidate(Pim_rp_candidateContext ctx) {
      if (ctx.name != null) {
         String name = ctx.name.getText();
         _configuration.getPimAcls().add(name);
      }
   }

   @Override
   public void exitPim_send_rp_announce(Pim_send_rp_announceContext ctx) {
      if (ctx.name != null) {
         String name = ctx.name.getText();
         _configuration.getPimAcls().add(name);
      }
   }

   @Override
   public void exitPim_spt_threshold(Pim_spt_thresholdContext ctx) {
      if (ctx.name != null) {
         String name = ctx.name.getText();
         _configuration.getPimAcls().add(name);
      }
   }

   @Override
   public void exitPim_ssm(Pim_ssmContext ctx) {
      if (ctx.name != null) {
         String name = ctx.name.getText();
         _configuration.getPimAcls().add(name);
      }
   }

   @Override
   public void exitPrefix_list_bgp_tail(Prefix_list_bgp_tailContext ctx) {
      if (_currentIpv6PeerGroup != null) {
         todo(ctx, F_IPV6);
      }
      else {
         String listName = ctx.list_name.getText();
         if (ctx.IN() != null) {
            _currentPeerGroup.setInboundPrefixList(listName);
         }
         else if (ctx.OUT() != null) {
            _currentPeerGroup.setOutboundPrefixList(listName);
         }
         else {
            throw new BatfishException("bad direction");
         }
      }
   }

   @Override
   public void exitRedistribute_aggregate_bgp_tail(
         Redistribute_aggregate_bgp_tailContext ctx) {
      todo(ctx, F_BGP_REDISTRIBUTE_AGGREGATE);
   }

   @Override
   public void exitRedistribute_bgp_ro_stanza(
         Redistribute_bgp_ro_stanzaContext ctx) {
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
         r.setMap(map);
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
   public void exitRedistribute_connected_bgp_tail(
         Redistribute_connected_bgp_tailContext ctx) {
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
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
            r.setMap(map);
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
      IsisProcess proc = _configuration.getIsisProcess();
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
   public void exitRedistribute_connected_ro_stanza(
         Redistribute_connected_ro_stanzaContext ctx) {
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
         r.setMap(map);
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
   public void exitRedistribute_ospf_bgp_tail(
         Redistribute_ospf_bgp_tailContext ctx) {
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
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
            r.setMap(map);
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
   public void exitRedistribute_rip_ro_stanza(
         Redistribute_rip_ro_stanzaContext ctx) {
      todo(ctx, F_OSPF_REDISTRIBUTE_RIP);
   }

   @Override
   public void exitRedistribute_static_bgp_tail(
         Redistribute_static_bgp_tailContext ctx) {
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
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
            r.setMap(map);
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
      IsisProcess proc = _configuration.getIsisProcess();
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
   public void exitRedistribute_static_ro_stanza(
         Redistribute_static_ro_stanzaContext ctx) {
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
         r.setMap(map);
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
   public void exitRemote_as_bgp_tail(Remote_as_bgp_tailContext ctx) {
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      int as = toInteger(ctx.as);
      if (_currentPeerGroup != proc.getMasterBgpPeerGroup()) {
         _currentPeerGroup.setRemoteAs(as);
      }
      else {
         throw new BatfishException("no peer or peer group in context");
      }
   }

   @Override
   public void exitRemove_private_as_bgp_tail(
         Remove_private_as_bgp_tailContext ctx) {
      _currentPeerGroup.setRemovePrivateAs(true);
   }

   @Override
   public void exitRoute_map_bgp_tail(Route_map_bgp_tailContext ctx) {
      if (_currentPeerGroup == null) {
         return;
      }
      String mapName = ctx.name.getText();
      _configuration.getReferencedRouteMaps().add(mapName);
      if (ctx.IN() != null) {
         _currentPeerGroup.setInboundRouteMap(mapName);
      }
      else if (ctx.OUT() != null) {
         _currentPeerGroup.setOutboundRouteMap(mapName);
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
      _configuration.getBgpProcesses().get(_currentVrf).setRouterId(routerId);
   }

   @Override
   public void exitRouter_id_ro_stanza(Router_id_ro_stanzaContext ctx) {
      Ip routerId = toIp(ctx.ip);
      _currentOspfProcess.setRouterId(routerId);
   }

   @Override
   public void exitRouter_isis_stanza(Router_isis_stanzaContext ctx) {
      _currentIsisProcess = null;
   }

   @Override
   public void exitRouter_ospf_stanza(Router_ospf_stanzaContext ctx) {
      _currentOspfProcess
            .computeNetworks(_configuration.getInterfaces().values());
      _currentOspfProcess = null;
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
   public void exitS_ip_source_route(S_ip_source_routeContext ctx) {
      boolean enabled = ctx.NO() == null;
      _configuration.getCf().setSourceRoute(enabled);
   }

   @Override
   public void exitS_line(S_lineContext ctx) {
      _currentLineNames = null;
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
   public void exitSend_community_bgp_tail(Send_community_bgp_tailContext ctx) {
      _currentPeerGroup.setSendCommunity(true);
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
      RouteMapSetDeleteCommunityLine line = new RouteMapSetDeleteCommunityLine(
            name);
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
      Set<String> communityLists = new LinkedHashSet<>();
      for (VariableContext comm_list : ctx.comm_lists) {
         String communityList = comm_list.getText();
         communityLists.add(communityList);
      }
      RouteMapSetAdditiveCommunityListLine line = new RouteMapSetAdditiveCommunityListLine(
            communityLists);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_community_list_rm_stanza(
         Set_community_list_rm_stanzaContext ctx) {
      Set<String> communityLists = new LinkedHashSet<>();
      for (VariableContext comm_list : ctx.comm_lists) {
         String communityList = comm_list.getText();
         communityLists.add(communityList);
      }
      RouteMapSetCommunityListLine line = new RouteMapSetCommunityListLine(
            communityLists);
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
   public void exitSet_ipv6_rm_stanza(Set_ipv6_rm_stanzaContext ctx) {
      _currentRouteMap.setIpv6(true);
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
   public void exitShutdown_if_stanza(Shutdown_if_stanzaContext ctx) {
      if (ctx.NO() == null) {
         for (Interface currentInterface : _currentInterfaces) {
            currentInterface.setActive(false);
         }
      }
   }

   @Override
   public void exitSs_community(Ss_communityContext ctx) {
      _currentSnmpCommunity = null;
   }

   @Override
   public void exitSs_enable_traps(Ss_enable_trapsContext ctx) {
      String trapName = ctx.snmp_trap_type.getText();
      SortedSet<String> subfeatureNames = new TreeSet<>(ctx.subfeature.stream()
            .map(s -> s.getText()).collect(Collectors.toList()));
      SortedMap<String, SortedSet<String>> traps = _configuration.getCf()
            .getSnmpServer().getTraps();
      SortedSet<String> subfeatures = traps.get(trapName);
      if (subfeatures == null) {
         traps.put(trapName, subfeatureNames);
      }
      else {
         subfeatures.addAll(subfeatureNames);
      }
   }

   @Override
   public void exitSs_host(Ss_hostContext ctx) {
      _currentSnmpHost = null;
   }

   @Override
   public void exitSsc_access_control(Ssc_access_controlContext ctx) {
      if (ctx.name != null) {
         String name = ctx.name.getText();
         _configuration.getSnmpAccessLists().add(name);
         _currentSnmpCommunity.setAccessList(name);
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
      _configuration.getSnmpAccessLists().add(name);
      _currentSnmpCommunity.setAccessList(name);
   }

   @Override
   public void exitStandard_access_list_stanza(
         Standard_access_list_stanzaContext ctx) {
      _currentStandardAcl = null;
   }

   @Override
   public void exitStandard_access_list_tail(
         Standard_access_list_tailContext ctx) {

      if (_currentStandardAcl.isIpV6()) {
         return;
      }

      LineAction action = getAccessListAction(ctx.ala);
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
   public void exitSubnet_bgp_tail(Subnet_bgp_tailContext ctx) {
      BgpProcess proc = _configuration.getBgpProcesses().get(_currentVrf);
      if (ctx.IP_PREFIX() != null) {
         Ip ip = getPrefixIp(ctx.IP_PREFIX().getSymbol());
         int prefixLength = getPrefixLength(ctx.IP_PREFIX().getSymbol());
         Prefix prefix = new Prefix(ip, prefixLength);
         NamedBgpPeerGroup namedGroup = _currentNamedPeerGroup;
         namedGroup.addNeighborPrefix(prefix);
         DynamicBgpPeerGroup pg = proc.addDynamicPeerGroup(prefix);
         pg.setGroupName(namedGroup.getName());
      }
      else if (ctx.IPV6_PREFIX() != null) {
         todo(ctx, F_IPV6);
      }
   }

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
   public void exitSwitchport_access_if_stanza(
         Switchport_access_if_stanzaContext ctx) {
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
   public void exitSwitchport_mode_stanza(Switchport_mode_stanzaContext ctx) {
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
   public void exitSwitchport_trunk_allowed_if_stanza(
         Switchport_trunk_allowed_if_stanzaContext ctx) {
      List<SubRange> ranges = toRange(ctx.r);
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.addAllowedRanges(ranges);
      }
   }

   @Override
   public void exitSwitchport_trunk_encapsulation_if_stanza(
         Switchport_trunk_encapsulation_if_stanzaContext ctx) {
      SwitchportEncapsulationType type = toEncapsulation(ctx.e);
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setSwitchportTrunkEncapsulation(type);
      }
   }

   @Override
   public void exitSwitchport_trunk_native_if_stanza(
         Switchport_trunk_native_if_stanzaContext ctx) {
      int vlan = toInteger(ctx.vlan);
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setNativeVlan(vlan);
      }
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
      String line = _text.substring(ctx.start.getStartIndex(),
            ctx.stop.getStopIndex());
      String msg = String.format("Line %d unrecognized: %s",
            ctx.start.getLine(), line);
      if (_unrecognizedAsRedFlag) {
         _w.redFlag(msg);
      }
      else {
         _parser.getParserErrorListener().syntaxError(ctx, ctx.getStart(),
               ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine(),
               msg);
         // throw new BatfishException(msg);
      }
   }

   @Override
   public void exitUpdate_source_bgp_tail(Update_source_bgp_tailContext ctx) {
      if (_currentPeerGroup == null) {
         return;
      }
      else if (_currentIpv6PeerGroup != null) {
         todo(ctx, F_IPV6);
      }
      else {
         String source = toInterfaceName(ctx.source);
         _currentPeerGroup.setUpdateSource(source);
      }
   }

   @Override
   public void exitUse_neighbor_group_bgp_tail(
         Use_neighbor_group_bgp_tailContext ctx) {
      String groupName = ctx.name.getText();
      if (_currentIpPeerGroup != null) {
         _currentIpPeerGroup.setGroupName(groupName);
      }
      else if (_currentIpv6PeerGroup != null) {
         todo(ctx, F_IPV6);
      }
      else {
         throw new BatfishException(
               "Unexpected context for use neighbor group");
      }
   }

   @Override
   public void exitVrf_context_stanza(Vrf_context_stanzaContext ctx) {
      _currentVrf = CiscoConfiguration.MASTER_VRF_NAME;
   }

   @Override
   public void exitVrf_forwarding_if_stanza(
         Vrf_forwarding_if_stanzaContext ctx) {
      String name = ctx.name.getText();
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setVrf(name);
         currentInterface.setPrefix(null);
      }
   }

   @Override
   public void exitVrf_member_if_stanza(Vrf_member_if_stanzaContext ctx) {
      String name = ctx.name.getText();
      for (Interface currentInterface : _currentInterfaces) {
         currentInterface.setVrf(name);
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

   public CiscoConfiguration getConfiguration() {
      return _configuration;
   }

   private String getFullText(ParserRuleContext ctx) {
      int start = ctx.getStart().getStartIndex();
      int end = ctx.getStop().getStopIndex();
      String text = _text.substring(start, end + 1);
      return text;
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
      return _vendorConfiguration;
   }

   private void popPeer() {
      int index = _peerGroupStack.size() - 1;
      _currentPeerGroup = _peerGroupStack.get(index);
      _peerGroupStack.remove(index);
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

   private List<AsExpr> toAsExprList(List<As_exprContext> list) {
      return list.stream().map(ctx -> toAsExpr(ctx))
            .collect(Collectors.toList());
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
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
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
         originType = OriginType.EGP;
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

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_and_rp_stanzaContext ctxt) {
      if (ctxt.AND() == null) {
         return toRoutePolicyBoolean(ctxt.boolean_not_rp_stanza());
      }
      else {
         return new RoutePolicyBooleanAnd(
               toRoutePolicyBoolean(ctxt.boolean_and_rp_stanza()),
               toRoutePolicyBoolean(ctxt.boolean_not_rp_stanza()));
      }
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_not_rp_stanzaContext ctxt) {
      if (ctxt.NOT() == null) {
         return toRoutePolicyBoolean(ctxt.boolean_simple_rp_stanza());
      }
      else {
         return new RoutePolicyBooleanNot(
               toRoutePolicyBoolean(ctxt.boolean_simple_rp_stanza()));
      }
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_rp_stanzaContext ctxt) {
      if (ctxt.OR() == null) {
         return toRoutePolicyBoolean(ctxt.boolean_and_rp_stanza());
      }
      else {
         return new RoutePolicyBooleanOr(
               toRoutePolicyBoolean(ctxt.boolean_rp_stanza()),
               toRoutePolicyBoolean(ctxt.boolean_and_rp_stanza()));
      }
   }

   private RoutePolicyBoolean toRoutePolicyBoolean(
         Boolean_simple_rp_stanzaContext ctx) {
      Boolean_rp_stanzaContext bctxt = ctx.boolean_rp_stanza();
      if (bctxt != null) {
         return toRoutePolicyBoolean(bctxt);
      }

      Boolean_community_matches_any_rp_stanzaContext mactxt = ctx
            .boolean_community_matches_any_rp_stanza();
      if (mactxt != null) {
         return new RoutePolicyBooleanCommunityMatchesAny(
               toRoutePolicyCommunitySet(mactxt.rp_community_set()));
      }

      Boolean_community_matches_every_rp_stanzaContext mectxt = ctx
            .boolean_community_matches_every_rp_stanza();
      if (mectxt != null) {
         return new RoutePolicyBooleanCommunityMatchesEvery(
               toRoutePolicyCommunitySet(mectxt.rp_community_set()));
      }

      Boolean_destination_rp_stanzaContext dctxt = ctx
            .boolean_destination_rp_stanza();
      if (dctxt != null) {
         return new RoutePolicyBooleanDestination(
               toRoutePolicyPrefixSet(dctxt.rp_prefix_set()));
      }

      Boolean_rib_has_route_rp_stanzaContext rctxt = ctx
            .boolean_rib_has_route_rp_stanza();
      if (rctxt != null) {
         return new RoutePolicyBooleanRibHasRoute(
               toRoutePolicyPrefixSet(rctxt.rp_prefix_set()));
      }

      Boolean_as_path_in_rp_stanzaContext actxt = ctx
            .boolean_as_path_in_rp_stanza();
      if (actxt != null) {
         return new RoutePolicyBooleanAsPathIn(toAsPathSetExpr(actxt.expr));
      }

      Boolean_tag_is_rp_stanzaContext tagctxt = ctx.boolean_tag_is_rp_stanza();
      if (tagctxt != null) {
         return new RoutePolicyBooleanTagIs(toIntComparator(tagctxt.int_comp()),
               toTagIntExpr(tagctxt.int_expr()));
      }

      Boolean_as_path_originates_from_rp_stanzaContext aotxt = ctx
            .boolean_as_path_originates_from_rp_stanza();
      if (aotxt != null) {
         return new RoutePolicyBooleanAsPathOriginatesFrom(
               toAsExprList(aotxt.as_list), aotxt.EXACT() != null);
      }

      Boolean_as_path_passes_through_rp_stanzaContext aptxt = ctx
            .boolean_as_path_passes_through_rp_stanza();
      if (aptxt != null) {
         return new RoutePolicyBooleanAsPathPassesThrough(
               toAsExprList(aptxt.as_list), aptxt.EXACT() != null);
      }
      throw convError(RoutePolicyBoolean.class, ctx);
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
                     .collect(Collectors.toSet()));
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
         return new RoutePolicyPrefixSetName(ctx.name.getText());
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
               prefix6 = new Prefix6(toIp6(pctxt.ipv6a),
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
         return new RoutePolicyDeleteCommunityStatement(negated,
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
      Apply_rp_stanzaContext actxt = ctx.apply_rp_stanza();
      if (actxt != null) {
         return toRoutePolicyStatement(actxt);
      }

      Delete_rp_stanzaContext dctxt = ctx.delete_rp_stanza();
      if (dctxt != null) {
         return toRoutePolicyStatement(dctxt);
      }

      Disposition_rp_stanzaContext pctxt = ctx.disposition_rp_stanza();
      if (pctxt != null) {
         return toRoutePolicyStatement(pctxt);
      }

      If_rp_stanzaContext ictxt = ctx.if_rp_stanza();
      if (ictxt != null) {
         return toRoutePolicyStatement(ictxt);
      }

      Set_rp_stanzaContext sctxt = ctx.set_rp_stanza();
      if (sctxt != null) {
         return toRoutePolicyStatement(sctxt);
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
      return new RoutePolicySetTag(toTagIntExpr(ctx.tag));
   }

   private RoutePolicyStatement toRoutePolicyStatement(
         Set_weight_rp_stanzaContext wctx) {
      IntExpr weight = toWeightIntExpr(wctx.weight);
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

   private IntExpr toWeightIntExpr(Int_exprContext ctx) {
      if (ctx.DEC() != null && ctx.PLUS() == null && ctx.DASH() == null) {
         int val = toInteger(ctx.DEC());
         return new LiteralInt(val);
      }
      else if (ctx.RP_VARIABLE() != null) {
         return new VarInt(ctx.RP_VARIABLE().getText());
      }
      else {
         /*
          * Unsupported weight integer expression - do not add cases unless you
          * know what you are doing
          */
         throw convError(IntExpr.class, ctx);
      }
   }

}
