package batfish.grammar.cisco.controlplane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import batfish.grammar.ControlPlaneExtractor;
import batfish.grammar.cisco.CiscoGrammar.*;
import batfish.grammar.cisco.*;
import batfish.grammar.cisco.CiscoGrammar.CommunityContext;
import batfish.grammar.cisco.CiscoGrammar.Interface_stanzaContext;
import batfish.grammar.cisco.CiscoGrammar.Port_specifierContext;
import batfish.representation.Ip;
import batfish.representation.LineAction;
import batfish.representation.OriginType;
import batfish.representation.OspfMetricType;
import batfish.representation.Protocol;
import batfish.representation.SwitchportEncapsulationType;
import batfish.representation.SwitchportMode;
import batfish.representation.VendorConfiguration;
import batfish.representation.cisco.BgpNetwork;
import batfish.representation.cisco.BgpPeerGroup;
import batfish.representation.cisco.BgpProcess;
import batfish.representation.cisco.BgpRedistributionPolicy;
import batfish.representation.cisco.CiscoConfiguration;
import batfish.representation.cisco.CiscoVendorConfiguration;
import batfish.representation.cisco.ExpandedCommunityList;
import batfish.representation.cisco.ExpandedCommunityListLine;
import batfish.representation.cisco.ExtendedAccessList;
import batfish.representation.cisco.ExtendedAccessListLine;
import batfish.representation.cisco.Interface;
import batfish.representation.cisco.IpAsPathAccessList;
import batfish.representation.cisco.IpAsPathAccessListLine;
import batfish.representation.cisco.NamedBgpPeerGroup;
import batfish.representation.cisco.OspfProcess;
import batfish.representation.cisco.OspfRedistributionPolicy;
import batfish.representation.cisco.OspfWildcardNetwork;
import batfish.representation.cisco.PrefixList;
import batfish.representation.cisco.PrefixListLine;
import batfish.representation.cisco.RouteMap;
import batfish.representation.cisco.RouteMapClause;
import batfish.representation.cisco.RouteMapMatchAsPathAccessListLine;
import batfish.representation.cisco.RouteMapMatchCommunityListLine;
import batfish.representation.cisco.RouteMapMatchIpAccessListLine;
import batfish.representation.cisco.RouteMapMatchIpPrefixListLine;
import batfish.representation.cisco.RouteMapMatchTagLine;
import batfish.representation.cisco.RouteMapSetAdditiveCommunityLine;
import batfish.representation.cisco.RouteMapSetAsPathPrependLine;
import batfish.representation.cisco.RouteMapSetCommunityLine;
import batfish.representation.cisco.RouteMapSetCommunityNoneLine;
import batfish.representation.cisco.RouteMapSetDeleteCommunityLine;
import batfish.representation.cisco.RouteMapSetLine;
import batfish.representation.cisco.RouteMapSetLocalPreferenceLine;
import batfish.representation.cisco.RouteMapSetMetricLine;
import batfish.representation.cisco.RouteMapSetNextHopLine;
import batfish.representation.cisco.RouteMapSetOriginTypeLine;
import batfish.representation.cisco.StandardAccessList;
import batfish.representation.cisco.StandardCommunityList;
import batfish.representation.cisco.StaticRoute;
import batfish.util.SubRange;
import batfish.util.Util;

public class CiscoControlPlaneExtractor extends CiscoGrammarBaseListener
      implements ControlPlaneExtractor {

   private static final int DEFAULT_STATIC_ROUTE_DISTANCE = 1;

   public static LineAction getAccessListAction(Access_list_actionContext ctx) {
      if (ctx.PERMIT() != null) {
         return LineAction.ACCEPT;
      }
      else if (ctx.DENY() != null) {
         return LineAction.REJECT;
      }
      else {
         throw new Error("bad LineAction");
      }
   }

   public static Ip getIp(Access_list_ip_rangeContext ctx) {
      if (ctx.ip != null) {
         return toIp(ctx.ip);
      }
      else {
         return new Ip(0l);
      }
   }

   public static int getPortNumber(PortContext ctx) {
      if (ctx.DEC() != null) {
         return toInteger(ctx.DEC());
      }
      else if (ctx.BOOTPC() != null) {
         return 68;
      }
      else if (ctx.BOOTPS() != null) {
         return 67;
      }
      else if (ctx.BGP() != null) {
         return 179;
      }
      else if (ctx.CMD() != null) {
         return 514;
      }
      else if (ctx.DOMAIN() != null) {
         return 53;
      }
      else if (ctx.EXEC() != null) {
         return 512;
      }
      else if (ctx.FTP() != null) {
         return 21;
      }
      else if (ctx.FTP_DATA() != null) {
         return 20;
      }
      else if (ctx.IDENT() != null) {
         return 113;
      }
      else if (ctx.ISAKMP() != null) {
         return 500;
      }
      else if (ctx.LPD() != null) {
         return 515;
      }
      else if (ctx.NETBIOS_DGM() != null) {
         return 138;
      }
      else if (ctx.NETBIOS_NS() != null) {
         return 137;
      }
      else if (ctx.NETBIOS_SS() != null) {
         return 139;
      }
      else if (ctx.NNTP() != null) {
         return 119;
      }
      else if (ctx.NON500_ISAKMP() != null) {
         return 4500;
      }
      else if (ctx.NTP() != null) {
         return 123;
      }
      else if (ctx.PIM_AUTO_RP() != null) {
         return 496;
      }
      else if (ctx.POP3() != null) {
         return 110;
      }
      else if (ctx.SMTP() != null) {
         return 25;
      }
      else if (ctx.SNMP() != null) {
         return 161;
      }
      else if (ctx.SNMPTRAP() != null) {
         return 162;
      }
      else if (ctx.SUNRPC() != null) {
         return 111;
      }
      else if (ctx.SYSLOG() != null) {
         return 514;
      }
      else if (ctx.TACACS() != null) {
         return 49;
      }
      else if (ctx.TELNET() != null) {
         return 23;
      }
      else if (ctx.TFTP() != null) {
         return 69;
      }
      else if (ctx.WWW() != null) {
         return 80;
      }
      else {
         throw new Error("bad port");
      }
   }

   private static List<SubRange> getPortRanges(Port_specifierContext ps) {
      List<SubRange> ranges = new ArrayList<SubRange>();
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
         throw new Error("bad port range");
      }
      return ranges;
   }

   public static Ip getPrefixIp(Token ipPrefixToken) {
      if (ipPrefixToken.getType() != CiscoGrammarCommonLexer.IP_PREFIX) {
         throw new Error(
               "attempted to get prefix length from non-IP_PREFIX token: " + ipPrefixToken.getType());
      }
      String text = ipPrefixToken.getText();
      String[] parts = text.split("/");
      String prefixIpStr = parts[0];
      Ip prefixIp = new Ip(prefixIpStr);
      return prefixIp;
   }

   public static int getPrefixLength(Token ipPrefixToken) {
      if (ipPrefixToken.getType() != CiscoGrammarCommonLexer.IP_PREFIX) {
         throw new Error(
               "attempted to get prefix length from non-IP_PREFIX token: " + ipPrefixToken.getType());
      }
      String text = ipPrefixToken.getText();
      String[] parts = text.split("/");
      String prefixLengthStr = parts[1];
      int prefixLength = Integer.parseInt(prefixLengthStr);
      return prefixLength;
   }

   public static int getProtocolNumber(ProtocolContext ctx) {
      if (ctx.DEC() != null) {
         return toInteger(ctx.DEC());
      }
      else if (ctx.AHP() != null) {
         return 51;
      }
      else if (ctx.ESP() != null) {
         return 50;
      }
      else if (ctx.GRE() != null) {
         return 47;
      }
      else if (ctx.ICMP() != null) {
         return 1;
      }
      else if (ctx.IGMP() != null) {
         return 2;
      }
      else if (ctx.IP() != null) {
         return 0;
      }
      else if (ctx.OSPF() != null) {
         return 0;
      }
      else if (ctx.PIM() != null) {
         return 103;
      }
      else if (ctx.SCTP() != null) {
         return 132;
      }
      else if (ctx.TCP() != null) {
         return 6;
      }
      else if (ctx.UDP() != null) {
         return 17;
      }
      else {
         throw new Error("bad protocol");
      }
   }

   public static Ip getWildcard(Access_list_ip_rangeContext ctx) {
      if (ctx.wildcard != null) {
         return toIp(ctx.wildcard);
      }
      else if (ctx.ANY() != null) {
         return new Ip(0xFFFFFFFFl);
      }
      else if (ctx.HOST() != null) {
         return new Ip(0l);
      }
      else {
         throw new Error("bad extended ip access list ip range");
      }
   }

   public static int toInteger(TerminalNode t) {
      return Integer.parseInt(t.getText());
   }

   public static int toInteger(Token t) {
      return Integer.parseInt(t.getText());
   }

   public static Ip toIp(Token t) {
      return new Ip(t.getText());
   }

   public static long toLong(CommunityContext ctx) {
      switch (ctx.com.getType()) {
      case CiscoGrammarCommonLexer.COMMUNITY_NUMBER:
         String numberText = ctx.com.getText();
         String[] parts = numberText.split(":");
         String leftStr = parts[0];
         String rightStr = parts[1];
         long left = Long.parseLong(leftStr);
         long right = Long.parseLong(rightStr);
         return (left << 16) | right;

      case CiscoGrammarCommonLexer.DEC:
         return toLong(ctx.com);

      case CiscoGrammarCommonLexer.INTERNET:
         return 0l;

      case CiscoGrammarCommonLexer.LOCAL_AS:
         return 0xFFFFFF03l;

      case CiscoGrammarCommonLexer.NO_ADVERTISE:
         return 0xFFFFFF02l;

      case CiscoGrammarCommonLexer.NO_EXPORT:
         return 0xFFFFFF01l;

      default:
         throw new Error("bad community");
      }
   }

   public static long toLong(TerminalNode t) {
      return Long.parseLong(t.getText());
   }

   public static long toLong(Token t) {
      return Long.parseLong(t.getText());
   }

   public static List<SubRange> toRange(RangeContext ctx) {
      List<SubRange> range = new ArrayList<SubRange>();
      for (SubrangeContext sc : ctx.range_list) {
         SubRange sr = toSubRange(sc);
         range.add(sr);
      }
      return range;
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

   private CiscoVendorConfiguration _configuration;

   private IpAsPathAccessList _currentAsPathAcl;

   private ExpandedCommunityList _currentExpandedCommunityList;

   private ExtendedAccessList _currentExtendedAcl;

   private Interface _currentInterface;

   private PrefixList _currentPrefixList;

   private RouteMap _currentRouteMap;

   private RouteMapClause _currentRouteMapClause;

   private StandardAccessList _currentStandardAcl;

   private StandardCommunityList _currentStandardCommunityList;

   private String _text;

   private List<String> _warnings;

   public CiscoControlPlaneExtractor(String text) {
      _text = text;
      _warnings = new ArrayList<String>();
   }

   @Override
   public void enterCisco_configuration(Cisco_configurationContext ctx) {
      _configuration = new CiscoVendorConfiguration();
      _configuration.setContext(ctx);
   }

   @Override
   public void enterExtended_access_list_stanza(
         Extended_access_list_stanzaContext ctx) {
      String name;
      if (ctx.named != null) {
         name = ctx.named.name.getText();
      }
      else {
         name = ctx.numbered.name.getText();
      }
      _currentExtendedAcl = new ExtendedAccessList(name);
      _currentExtendedAcl.setContext(ctx);
      _configuration.getExtendedAcls().put(name, _currentExtendedAcl);
   }

   @Override
   public void enterInterface_stanza(Interface_stanzaContext ctx) {
      String name = ctx.iname.getText();
      double bandwidth = Interface.getDefaultBandwidth(name);
      Interface newInterface = new Interface(name);
      newInterface.setContext(ctx);
      newInterface.setBandwidth(bandwidth);
      _configuration.getInterfaces().put(name, newInterface);
      _currentInterface = newInterface;
   }

   @Override
   public void enterIp_as_path_access_list_stanza(
         Ip_as_path_access_list_stanzaContext ctx) {
      String name = ctx.numbered.name.getText();
      _currentAsPathAcl = new IpAsPathAccessList(name);
      _currentAsPathAcl.setContext(ctx);
      _configuration.getAsPathAccessLists().put(name, _currentAsPathAcl);
   }

   @Override
   public void enterIp_community_list_expanded_stanza(
         Ip_community_list_expanded_stanzaContext ctx) {
      String name;
      if (ctx.numbered != null) {
         name = ctx.numbered.name.getText();
      }
      else {
         name = ctx.named.name.getText();
      }
      _currentExpandedCommunityList = new ExpandedCommunityList(name);
      _currentExpandedCommunityList.setContext(ctx);
      _configuration.getExpandedCommunityLists().put(name,
            _currentExpandedCommunityList);
   }

   @Override
   public void enterIp_community_list_standard_stanza(
         Ip_community_list_standard_stanzaContext ctx) {
      String name;
      if (ctx.numbered != null) {
         name = ctx.numbered.name.getText();
      }
      else {
         name = ctx.named.name.getText();
      }
      _currentStandardCommunityList = new StandardCommunityList(name);
      _currentStandardCommunityList.setContext(ctx);
      _configuration.getStandardCommunityLists().put(name,
            _currentStandardCommunityList);
   }

   @Override
   public void enterIp_prefix_list_stanza(Ip_prefix_list_stanzaContext ctx) {
      String name = ctx.named.name.getText();
      _currentPrefixList = new PrefixList(name);
      _currentPrefixList.setContext(ctx);
      _configuration.getPrefixLists().put(name, _currentPrefixList);
   }

   @Override
   public void enterRoute_map_stanza(Route_map_stanzaContext ctx) {
      String name = ctx.named.name.getText();
      _currentRouteMap = new RouteMap(name);
      _currentRouteMap.setContext(ctx);
      _configuration.getRouteMaps().put(name, _currentRouteMap);
   }

   @Override
   public void enterRoute_map_tail(Route_map_tailContext ctx) {
      int num = toInteger(ctx.num);
      LineAction action = getAccessListAction(ctx.rmt);
      _currentRouteMapClause = new RouteMapClause(action,
            _currentRouteMap.getMapName(), num);
      _currentRouteMapClause.setContext(ctx);
      Map<Integer, RouteMapClause> clauses = _currentRouteMap.getClauses();
      if (clauses.containsKey(num)) {
         throw new Error("Route map '" + _currentRouteMap.getMapName()
               + "' already contains clause numbered '" + num + "'");
      }
      else {
         clauses.put(num, _currentRouteMapClause);
      }
   }

   @Override
   public void enterRouter_bgp_stanza(Router_bgp_stanzaContext ctx) {
      int procNum = toInteger(ctx.procnum);
      BgpProcess proc = new BgpProcess(procNum);
      _configuration.setBgpProcess(proc, ctx);
   }

   @Override
   public void enterRouter_ospf_stanza(Router_ospf_stanzaContext ctx) {
      int procNum = toInteger(ctx.procnum);
      OspfProcess proc = new OspfProcess(procNum);
      _configuration.setOspfProcess(proc, ctx);
   }

   @Override
   public void enterStandard_access_list_stanza(
         Standard_access_list_stanzaContext ctx) {
      String name;
      if (ctx.named != null) {
         name = ctx.named.name.getText();
      }
      else {
         name = ctx.numbered.name.getText();
      }
      _currentStandardAcl = new StandardAccessList(name);
      _currentStandardAcl.setContext(ctx);
      _configuration.getStandardAcls().put(name, _currentStandardAcl);
   }

   @Override
   public void exitAggregate_address_tail_bgp(
         Aggregate_address_tail_bgpContext ctx) {
      if (ctx.network != null) {
         BgpProcess proc = _configuration.getBgpProcess();
         Ip network = toIp(ctx.network);
         Ip subnet = toIp(ctx.subnet);
         BgpNetwork net = new BgpNetwork(network, subnet);
         boolean summaryOnly = ctx.SUMMARY_ONLY() != null;
         proc.getAggregateNetworks().put(net, summaryOnly);
      }
      else if (ctx.ipv6_prefix != null) {
         todo(ctx);
      }
   }

   @Override
   public void exitArea_nssa_ro_stanza(Area_nssa_ro_stanzaContext ctx) {
      OspfProcess proc = _configuration.getOspfProcess();
      int area = (ctx.area_int != null) ? toInteger(ctx.area_int) : (int) toIp(
            ctx.area_ip).asLong();
      boolean noSummary = ctx.NO_SUMMARY() != null;
      boolean defaultOriginate = ctx.DEFAULT_INFORMATION_ORIGINATE() != null;
      if (defaultOriginate) {
         // TODO: implement
         todo(ctx);
      }
      proc.getNssas().put(area, noSummary);
   }

   @Override
   public void exitAuto_summary_af_stanza(Auto_summary_af_stanzaContext ctx) {
      todo(ctx);
   }

   @Override
   public void exitCluster_id_bgp_rb_stanza(Cluster_id_bgp_rb_stanzaContext ctx) {
      BgpProcess proc = _configuration.getBgpProcess();
      Ip clusterId = toIp(ctx.id);
      proc.setClusterId(clusterId);
   }

   @Override
   public void exitDefault_information_ro_stanza(
         Default_information_ro_stanzaContext ctx) {
      OspfProcess proc = _configuration.getOspfProcess();
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
   public void exitDefault_metric_tail_bgp(Default_metric_tail_bgpContext ctx) {
      int metric = toInteger(ctx.metric);
      _configuration.getBgpProcess().setDefaultMetric(metric);
   }

   @Override
   public void exitExtended_access_list_stanza(
         Extended_access_list_stanzaContext ctx) {
      _currentExtendedAcl = null;
   }

   @Override
   public void exitExtended_access_list_tail(
         Extended_access_list_tailContext ctx) {
      LineAction action = getAccessListAction(ctx.ala);
      int protocol = getProtocolNumber(ctx.prot);
      Ip srcIp = getIp(ctx.srcipr);
      Ip srcWildcard = getWildcard(ctx.srcipr);
      Ip dstIp = getIp(ctx.dstipr);
      Ip dstWildcard = getWildcard(ctx.dstipr);
      List<SubRange> srcPortRanges = ctx.alps_src != null ? getPortRanges(ctx.alps_src)
            : null;
      List<SubRange> dstPortRanges = ctx.alps_dst != null ? getPortRanges(ctx.alps_dst)
            : null;
      ExtendedAccessListLine line = new ExtendedAccessListLine(action,
            protocol, srcIp, srcWildcard, dstIp, dstWildcard, srcPortRanges,
            dstPortRanges);
      _currentExtendedAcl.addLine(line);
   }

   @Override
   public void exitHostname_stanza(Hostname_stanzaContext ctx) {
      _configuration.setHostname(ctx.name.getText());
   }

   @Override
   public void exitInterface_stanza(Interface_stanzaContext ctx) {
      _currentInterface = null;
   }

   @Override
   public void exitIp_access_group_if_stanza(
         Ip_access_group_if_stanzaContext ctx) {
      String name = ctx.name.getText();
      if (ctx.IN() != null) {
         _currentInterface.setIncomingFilter(name);
      }
      else if (ctx.OUT() != null) {
         _currentInterface.setOutgoingFilter(name);
      }
      else {
         throw new Error("bad direction");
      }
   }

   @Override
   public void exitIp_address_if_stanza(Ip_address_if_stanzaContext ctx) {
      Ip address;
      Ip mask;
      if (ctx.prefix != null) {
         address = getPrefixIp(ctx.prefix);
         int prefixLength = getPrefixLength(ctx.prefix);
         long maskLong = Util.numSubnetBitsToSubnetLong(prefixLength);
         mask = new Ip(maskLong);
      }
      else {
         address = new Ip(ctx.ip.getText());
         mask = new Ip(ctx.subnet.getText());
      }
      
      _currentInterface.setIp(address);
      _currentInterface.setSubnetMask(mask);
      _currentInterface.setIpAddressStanzaContext(ctx);
   }

   @Override
   public void exitIp_address_secondary_if_stanza(
         Ip_address_secondary_if_stanzaContext ctx) {
      todo(ctx);
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
      String regex = "";
      for (Token remainder : ctx.remainder) {
         regex += remainder.getText();
      }
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
   public void exitIp_default_gateway_stanza(
         Ip_default_gateway_stanzaContext ctx) {
      todo(ctx);
   }

   @Override
   public void exitIp_ospf_cost_if_stanza(Ip_ospf_cost_if_stanzaContext ctx) {
      int cost = toInteger(ctx.cost);
      _currentInterface.setOspfCost(cost);
   }

   @Override
   public void exitIp_ospf_dead_interval_if_stanza(
         Ip_ospf_dead_interval_if_stanzaContext ctx) {
      int seconds = toInteger(ctx.seconds);
      _currentInterface.setOSPFDeadInterval(seconds);
      _currentInterface.setOSPFHelloMultiplier(0);
   }

   @Override
   public void exitIp_ospf_dead_interval_minimal_if_stanza(
         Ip_ospf_dead_interval_minimal_if_stanzaContext ctx) {
      int multiplier = toInteger(ctx.mult);
      _currentInterface.setOSPFDeadInterval(1);
      _currentInterface.setOSPFHelloMultiplier(multiplier);
   }

   @Override
   public void exitIp_policy_if_stanza(Ip_policy_if_stanzaContext ctx) {
      String policyName = ctx.name.getText();
      _currentInterface.setRoutingPolicy(policyName);
   }

   @Override
   public void exitIp_prefix_list_stanza(Ip_prefix_list_stanzaContext ctx) {
      _currentPrefixList = null;
   }

   @Override
   public void exitIp_prefix_list_tail(Ip_prefix_list_tailContext ctx) {
      /*
       * if (ctx.seqnum != null) { int seqnum = toInteger(ctx.seqnum); }
       */
      LineAction action = getAccessListAction(ctx.action);
      Ip prefix = getPrefixIp(ctx.prefix);
      int prefixLength = getPrefixLength(ctx.prefix);
      int minLen = prefixLength;
      int maxLen = prefixLength;
      if (ctx.minpl != null) {
         minLen = toInteger(ctx.minpl);
         maxLen = 32;
      }
      if (ctx.maxpl != null) {
         maxLen = toInteger(ctx.maxpl);
      }
      SubRange lengthRange = new SubRange(minLen, maxLen);
      PrefixListLine line = new PrefixListLine(action, prefix, prefixLength,
            lengthRange);
      _currentPrefixList.addLine(line);
   }

   @Override
   public void exitIp_route_stanza(Ip_route_stanzaContext ctx) {
      Ip prefix;
      Ip mask;
      if (ctx.prefix != null) {
         prefix = getPrefixIp(ctx.prefix);
         int prefixLength = getPrefixLength(ctx.prefix);
         long maskLong = Util.numSubnetBitsToSubnetLong(prefixLength);
         mask = new Ip(maskLong);
      }
      else {
         prefix = toIp(ctx.address);
         mask = toIp(ctx.mask);
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
      StaticRoute route = new StaticRoute(prefix, mask, nextHopIp,
            nextHopInterface, distance, tag, track, permanent);
      _configuration.getStaticRoutes().put(prefix.networkString(mask), route);
   }

   @Override
   public void exitMatch_as_path_access_list_rm_stanza(
         Match_as_path_access_list_rm_stanzaContext ctx) {
      Set<String> names = new TreeSet<String>();
      for (Token t : ctx.name_list) {
         names.add(t.getText());
      }
      RouteMapMatchAsPathAccessListLine line = new RouteMapMatchAsPathAccessListLine(
            names);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMatch_community_list_rm_stanza(
         Match_community_list_rm_stanzaContext ctx) {
      Set<String> names = new TreeSet<String>();
      for (Token t : ctx.name_list) {
         names.add(t.getText());
      }
      RouteMapMatchCommunityListLine line = new RouteMapMatchCommunityListLine(
            names);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMatch_ip_access_list_rm_stanza(
         Match_ip_access_list_rm_stanzaContext ctx) {
      Set<String> names = new TreeSet<String>();
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
      Set<String> names = new TreeSet<String>();
      for (Token t : ctx.name_list) {
         names.add(t.getText());
      }
      RouteMapMatchIpPrefixListLine line = new RouteMapMatchIpPrefixListLine(
            names);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMatch_ipv6_rm_stanza(Match_ipv6_rm_stanzaContext ctx) {
      _currentRouteMap.setIgnore(true);
   }

   @Override
   public void exitMatch_tag_rm_stanza(Match_tag_rm_stanzaContext ctx) {
      Set<Integer> tags = new TreeSet<Integer>();
      for (Token t : ctx.tag_list) {
         tags.add(toInteger(t));
      }
      RouteMapMatchTagLine line = new RouteMapMatchTagLine(tags);
      _currentRouteMapClause.addMatchLine(line);
   }

   @Override
   public void exitMaximum_paths_ro_stanza(Maximum_paths_ro_stanzaContext ctx) {
      todo(ctx);
      /*
       * Note that this is very difficult to enforce, and may not help the
       * analysis without major changes
       */
   }

   @Override
   public void exitNeighbor_activate_af_stanza(
         Neighbor_activate_af_stanzaContext ctx) {
      BgpProcess proc = _configuration.getBgpProcess();
      if (ctx.neighbor != null) {
         Ip neighbor = toIp(ctx.neighbor);
         proc.addActivatedNeighbor(neighbor);
      }
      else if (ctx.neighbor6 != null) {
         todo(ctx);
         return;
      }
      else if (ctx.pg != null) {
         String peerGroupName = ctx.pg.getText();
         NamedBgpPeerGroup pg = proc.getNamedPeerGroups().get(peerGroupName);
         for (Ip neighborAddress : pg.getNeighborAddresses()) {
            proc.addActivatedNeighbor(neighborAddress);
         }
      }
   }

   @Override
   public void exitNeighbor_default_originate_af_stanza(
         Neighbor_default_originate_af_stanzaContext ctx) {
      String mapName = ctx.map != null ? ctx.map.getText() : null;
      String name = ctx.name.getText();
      _configuration.getBgpProcess().addDefaultOriginateNeighbor(name, mapName);
   }

   @Override
   public void exitNeighbor_ebgp_multihop_af_stanza(
         Neighbor_ebgp_multihop_af_stanzaContext ctx) {
      todo(ctx);
   }

   @Override
   public void exitNeighbor_ebgp_multihop_rb_stanza(
         Neighbor_ebgp_multihop_rb_stanzaContext ctx) {
      todo(ctx);
   }

   @Override
   public void exitNeighbor_next_hop_self_rb_stanza(
         Neighbor_next_hop_self_rb_stanzaContext ctx) {
      todo(ctx);
   }

   @Override
   public void exitNeighbor_peer_group_assignment_tail_bgp(
         Neighbor_peer_group_assignment_tail_bgpContext ctx) {
      if (ctx.address != null) {
         Ip address = toIp(ctx.address);
         String peerGroupName = ctx.name.getText();
         _configuration.getBgpProcess().addPeerGroupMember(address,
               peerGroupName);
      }
      else if (ctx.address6 != null) {
         todo(ctx);
      }
   }

   @Override
   public void exitNeighbor_peer_group_creation_rb_stanza(
         Neighbor_peer_group_creation_rb_stanzaContext ctx) {
      String name = ctx.name.getText();
      BgpProcess proc = _configuration.getBgpProcess();
      proc.addNamedPeerGroup(name);
      NamedBgpPeerGroup newGroup = proc.getNamedPeerGroups().get(name);
      newGroup.setClusterId(proc.getClusterId());
   }

   @Override
   public void exitNeighbor_prefix_list_tail_bgp(
         Neighbor_prefix_list_tail_bgpContext ctx) {
      switch(ctx.neighbor.getType()) {
      case CiscoGrammarCommonLexer.IPV6_ADDRESS:
         todo(ctx);
         break;
         
      case CiscoGrammarCommonLexer.IP_ADDRESS:
      default:
         String neighbor = ctx.neighbor.getText();
         String listName = ctx.list_name.getText();
         BgpProcess proc = _configuration.getBgpProcess();
         if (ctx.IN() != null) {
            proc.addPeerGroupInboundPrefixList(neighbor, listName);
         }
         else if (ctx.OUT() != null) {
            proc.addPeerGroupOutboundPrefixList(neighbor, listName);
         }
         else {
            throw new Error("bad direction");
         }
         break;
      }
   }

   @Override
   public void exitNeighbor_remote_as_tail_bgp(
         Neighbor_remote_as_tail_bgpContext ctx) {
      BgpProcess proc = _configuration.getBgpProcess();
      int as = toInteger(ctx.as);
      Ip pgIp = null;
      String peerGroupName;
      int pgType = ctx.pg.getType();
      boolean ip = pgType == CiscoGrammarCommonLexer.IP_ADDRESS;
      boolean ip6 = pgType == CiscoGrammarCommonLexer.IPV6_ADDRESS;
      if (ip) {
         pgIp = toIp(ctx.pg);
         peerGroupName = pgIp.toString();
      }
      else if (ip6) {
         todo(ctx);
         return;
      }
      else {
         peerGroupName = ctx.pg.getText();
      }
      BgpPeerGroup pg = proc.getPeerGroup(peerGroupName);
      if (pg == null) {
         if (ip) {
            proc.addIpPeerGroup(pgIp);
         }
         else if (ip6) {
            // TODO: implement
         }
         else {
            proc.addNamedPeerGroup(peerGroupName);
         }
         pg = proc.getPeerGroup(peerGroupName);
      }
      pg.setRemoteAS(as);
   }

   @Override
   public void exitNeighbor_remove_private_as_af_stanza(
         Neighbor_remove_private_as_af_stanzaContext ctx) {
      todo(ctx);
   }

   @Override
   public void exitNeighbor_route_map_tail_bgp(
         Neighbor_route_map_tail_bgpContext ctx) {
      switch(ctx.neighbor.getType()) {
      case CiscoGrammarCommonLexer.IPV6_ADDRESS:
         todo(ctx);
         break;
         
      case CiscoGrammarCommonLexer.IP_ADDRESS:
      default:
         String peerGroup = ctx.neighbor.getText();
         String mapName = ctx.name.getText();
         BgpProcess proc = _configuration.getBgpProcess();
         if (ctx.IN() != null) {
            proc.addPeerGroupInboundRouteMap(peerGroup, mapName);
         }
         else if (ctx.OUT() != null) {
            proc.addPeerGroupOutboundRouteMap(peerGroup, mapName);
         }
         else {
            throw new Error("bad direction");
         }
      }
   }

   @Override
   public void exitNeighbor_route_reflector_client_af_stanza(
         Neighbor_route_reflector_client_af_stanzaContext ctx) {
      String pgName = ctx.pg.getText();
      BgpProcess proc = _configuration.getBgpProcess();
      proc.addPeerGroupRouteReflectorClient(pgName);
   }

   @Override
   public void exitNeighbor_send_community_tail_bgp(
         Neighbor_send_community_tail_bgpContext ctx) {
      String pgName = ctx.neighbor.getText();
      _configuration.getBgpProcess().addSendCommunityPeerGroup(pgName);
   }

   @Override
   public void exitNeighbor_shutdown_tail_bgp(
         Neighbor_shutdown_tail_bgpContext ctx) {
      String pgName = ctx.neighbor.getText();
      _configuration.getBgpProcess().addShutDownNeighbor(pgName);
   }

   @Override
   public void exitNeighbor_update_source_rb_stanza(
         Neighbor_update_source_rb_stanzaContext ctx) {
      switch (ctx.neighbor.getType()) {
      case CiscoGrammarCommonLexer.IPV6_ADDRESS:
         todo(ctx);
         break;
         
      case CiscoGrammarCommonLexer.IP_ADDRESS:
      default:
         String pgName = ctx.neighbor.getText();
         String source = ctx.source.getText();
         _configuration.getBgpProcess().setPeerGroupUpdateSource(pgName, source);
         break;
      }
   }

   @Override
   public void exitNetwork_ro_stanza(Network_ro_stanzaContext ctx) {
      Ip prefix = toIp(ctx.ip);
      Ip wildcard = toIp(ctx.wildcard);
      long area;
      if (ctx.area_int != null) {
         area = toLong(ctx.area_int);
      }
      else if (ctx.area_ip != null) {
         area = toIp(ctx.area_ip).asLong();
      }
      else {
         throw new Error("bad area");
      }
      OspfWildcardNetwork network = new OspfWildcardNetwork(prefix, wildcard,
            area);
      _configuration.getOspfProcess().getWildcardNetworks().add(network);
   }

   @Override
   public void exitNetwork_tail_bgp(Network_tail_bgpContext ctx) {
      
      if (ctx.mapname != null) {
         todo(ctx);
      }
      else {
         Ip prefix = toIp(ctx.ip);
         Ip mask = (ctx.mask != null) ? toIp(ctx.mask) : prefix.getClassMask();
         BgpNetwork network = new BgpNetwork(prefix, mask);
         _configuration.getBgpProcess().getNetworks().add(network);
      }
   }

   @Override
   public void exitNo_neighbor_activate_af_stanza(
         No_neighbor_activate_af_stanzaContext ctx) {
      BgpProcess proc = _configuration.getBgpProcess();
      switch (ctx.pg.getType()) {
      case CiscoGrammarCommonLexer.IP_ADDRESS:
         Ip ip = toIp(ctx.pg);
         proc.getActivatedNeighbors().remove(ip);
         break;

      case CiscoGrammarCommonLexer.IPV6_ADDRESS:
      default:
         todo(ctx);
         break;
      }
   }

   @Override
   public void exitPassive_interface_default_ro_stanza(
         Passive_interface_default_ro_stanzaContext ctx) {
      _configuration.getOspfProcess().setPassiveInterfaceDefault(true);
   }

   @Override
   public void exitPassive_interface_ro_stanza(
         Passive_interface_ro_stanzaContext ctx) {
      boolean passive = ctx.NO() == null;
      String iname = ctx.i.toString();
      OspfProcess proc = _configuration.getOspfProcess();
      if (passive) {
         proc.getInterfaceBlacklist().add(iname);
      }
      else {
         proc.getInterfaceWhitelist().add(iname);
      }
   }

   @Override
   public void exitRedistribute_bgp_ro_stanza(
         Redistribute_bgp_ro_stanzaContext ctx) {
      OspfProcess proc = _configuration.getOspfProcess();
      Protocol sourceProtocol = Protocol.BGP;
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
   public void exitRedistribute_connected_ro_stanza(
         Redistribute_connected_ro_stanzaContext ctx) {
      OspfProcess proc = _configuration.getOspfProcess();
      Protocol sourceProtocol = Protocol.CONNECTED;
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
   public void exitRedistribute_connected_tail_bgp(
         Redistribute_connected_tail_bgpContext ctx) {
      BgpProcess proc = _configuration.getBgpProcess();
      Protocol sourceProtocol = Protocol.CONNECTED;
      BgpRedistributionPolicy r = new BgpRedistributionPolicy(sourceProtocol);
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

   @Override
   public void exitRedistribute_ospf_tail_bgp(
         Redistribute_ospf_tail_bgpContext ctx) {
      BgpProcess proc = _configuration.getBgpProcess();
      Protocol sourceProtocol = Protocol.OSPF;
      BgpRedistributionPolicy r = new BgpRedistributionPolicy(sourceProtocol);
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
      r.getSpecialAttributes().put(BgpRedistributionPolicy.OSPF_PROCESS_NUMBER,
            procNum);
   }

   @Override
   public void exitRedistribute_static_ro_stanza(
         Redistribute_static_ro_stanzaContext ctx) {
      OspfProcess proc = _configuration.getOspfProcess();
      Protocol sourceProtocol = Protocol.STATIC;
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
   public void exitRedistribute_static_tail_bgp(
         Redistribute_static_tail_bgpContext ctx) {
      BgpProcess proc = _configuration.getBgpProcess();
      Protocol sourceProtocol = Protocol.STATIC;
      BgpRedistributionPolicy r = new BgpRedistributionPolicy(sourceProtocol);
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

   @Override
   public void exitRoute_map_stanza(Route_map_stanzaContext ctx) {
      _currentRouteMap = null;
   }

   @Override
   public void exitRoute_map_tail(Route_map_tailContext ctx) {
      _currentRouteMapClause = null;
   }

   @Override
   public void exitRouter_id_bgp_rb_stanza(Router_id_bgp_rb_stanzaContext ctx) {
      Ip routerId = toIp(ctx.routerid);
      _configuration.getBgpProcess().setRouterId(routerId);
   }

   @Override
   public void exitRouter_id_ro_stanza(Router_id_ro_stanzaContext ctx) {
      Ip routerId = toIp(ctx.ip);
      _configuration.getOspfProcess().setRouterId(routerId);
   }

   @Override
   public void exitRouter_ospf_stanza(Router_ospf_stanzaContext ctx) {
      _configuration.getOspfProcess().computeNetworks(
            _configuration.getInterfaces().values());
   }

   @Override
   public void exitSet_as_path_prepend_rm_stanza(
         Set_as_path_prepend_rm_stanzaContext ctx) {
      List<Integer> asList = new ArrayList<Integer>();
      for (Token t : ctx.as_list) {
         int as = toInteger(t);
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
      List<Long> commList = new ArrayList<Long>();
      for (CommunityContext c : ctx.comm_list) {
         long community = toLong(c);
         commList.add(community);
      }
      RouteMapSetAdditiveCommunityLine line = new RouteMapSetAdditiveCommunityLine(
            commList);
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
      List<Long> commList = new ArrayList<Long>();
      for (CommunityContext c : ctx.comm_list) {
         long community = toLong(c);
         commList.add(community);
      }
      RouteMapSetCommunityLine line = new RouteMapSetCommunityLine(commList);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_ipv6_rm_stanza(Set_ipv6_rm_stanzaContext ctx) {
      _currentRouteMap.setIgnore(true);
   }

   @Override
   public void exitSet_local_preference_rm_stanza(
         Set_local_preference_rm_stanzaContext ctx) {
      int localPreference = toInteger(ctx.pref);
      RouteMapSetLocalPreferenceLine line = new RouteMapSetLocalPreferenceLine(
            localPreference);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_metric_rm_stanza(Set_metric_rm_stanzaContext ctx) {
      int metric = toInteger(ctx.metric);
      RouteMapSetMetricLine line = new RouteMapSetMetricLine(metric);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_metric_type_rm_stanza(
         Set_metric_type_rm_stanzaContext ctx) {
      todo(ctx);
   }

   @Override
   public void exitSet_next_hop_rm_stanza(Set_next_hop_rm_stanzaContext ctx) {
      Set<Ip> nextHops = new TreeSet<Ip>();
      for (Token t : ctx.nexthop_list) {
         Ip nextHop = toIp(t);
         nextHops.add(nextHop);
      }
      RouteMapSetNextHopLine line = new RouteMapSetNextHopLine(nextHops);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitSet_origin_rm_stanza(Set_origin_rm_stanzaContext ctx) {
      OriginType originType;
      Integer asNum = null;
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
         throw new Error("bad origin type");
      }
      RouteMapSetLine line = new RouteMapSetOriginTypeLine(originType, asNum);
      _currentRouteMapClause.addSetLine(line);
   }

   @Override
   public void exitShutdown_if_stanza(Shutdown_if_stanzaContext ctx) {
      _currentInterface.setActive(false);
   }

   @Override
   public void exitStandard_access_list_stanza(
         Standard_access_list_stanzaContext ctx) {
      _currentStandardAcl = null;
   }

   @Override
   public void exitSwitchport_access_if_stanza(
         Switchport_access_if_stanzaContext ctx) {
      int vlan = toInteger(ctx.vlan);
      _currentInterface.setSwitchportMode(SwitchportMode.ACCESS);
      _currentInterface.setAccessVlan(vlan);
   }

   @Override
   public void exitSwitchport_mode_access_stanza(
         Switchport_mode_access_stanzaContext ctx) {
      _currentInterface.setSwitchportMode(SwitchportMode.ACCESS);
   }

   @Override
   public void exitSwitchport_mode_dynamic_auto_stanza(
         Switchport_mode_dynamic_auto_stanzaContext ctx) {
      _currentInterface.setSwitchportMode(SwitchportMode.DYNAMIC_AUTO);
   }

   @Override
   public void exitSwitchport_mode_dynamic_desirable_stanza(
         Switchport_mode_dynamic_desirable_stanzaContext ctx) {
      _currentInterface.setSwitchportMode(SwitchportMode.DYNAMIC_DESIRABLE);
   }

   @Override
   public void exitSwitchport_mode_trunk_stanza(
         Switchport_mode_trunk_stanzaContext ctx) {
      _currentInterface.setSwitchportMode(SwitchportMode.TRUNK);
   }

   @Override
   public void exitSwitchport_trunk_allowed_if_stanza(
         Switchport_trunk_allowed_if_stanzaContext ctx) {
      List<SubRange> ranges = toRange(ctx.r);
      _currentInterface.addAllowedRanges(ranges);
   }

   @Override
   public void exitSwitchport_trunk_encapsulation_if_stanza(
         Switchport_trunk_encapsulation_if_stanzaContext ctx) {
      SwitchportEncapsulationType type = toEncapsulation(ctx.e);
      _currentInterface.setSwitchportTrunkEncapsulation(type);
   }

   @Override
   public void exitSwitchport_trunk_native_if_stanza(
         Switchport_trunk_native_if_stanzaContext ctx) {
      int vlan = toInteger(ctx.vlan);
      _currentInterface.setNativeVlan(vlan);
   }

   @Override
   public void exitVrf_stanza(Vrf_stanzaContext ctx) {
      todo(ctx);
   }

   public CiscoConfiguration getConfiguration() {
      return _configuration;
   }

   public String getText() {
      return _text;
   }

   @Override
   public VendorConfiguration getVendorConfiguration() {
      return _configuration;
   }

   public List<String> getWarnings() {
      return _warnings;
   }

   private void todo(ParserRuleContext ctx) {
      String prefix = "WARNING " + (_warnings.size() + 1) + ": ";
      StringBuilder sb = new StringBuilder();
      List<String> ruleNames = Arrays.asList(CiscoGrammar.ruleNames);
      String ruleStack = ctx.toString(ruleNames);
      sb.append(prefix
            + "Missing implementation for top (leftmost) parser rule in stack: '"
            + ruleStack + "'.\n");
      sb.append(prefix + "Rule context follows:\n");
      int start = ctx.start.getStartIndex();
      int startLine = ctx.start.getLine();
      int end = ctx.stop.getStopIndex();
      String ruleText = _text.substring(start, end + 1);
      String[] ruleTextLines = ruleText.split("\\n");
      for (int line = startLine, i = 0; i < ruleTextLines.length; line++, i++) {
         String contextPrefix = prefix + " line " + line + ": ";
         sb.append(contextPrefix + ruleTextLines[i] + "\n");
      }
      _warnings.add(sb.toString());
   }

   public SwitchportEncapsulationType toEncapsulation(
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
         throw new Error("bad encapsulation");
      }
   }

}
