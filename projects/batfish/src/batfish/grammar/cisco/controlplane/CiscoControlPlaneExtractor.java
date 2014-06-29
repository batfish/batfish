package batfish.grammar.cisco.controlplane;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import batfish.grammar.cisco.CiscoGrammar.*;
import batfish.grammar.cisco.*;
import batfish.grammar.cisco.CiscoGrammar.Interface_stanzaContext;
import batfish.grammar.cisco.CiscoGrammar.Port_specifierContext;
import batfish.representation.Ip;
import batfish.representation.LineAction;
import batfish.representation.OspfMetricType;
import batfish.representation.cisco.BgpNetwork;
import batfish.representation.cisco.BgpProcess;
import batfish.representation.cisco.CiscoConfiguration;
import batfish.representation.cisco.ExpandedCommunityList;
import batfish.representation.cisco.ExpandedCommunityListLine;
import batfish.representation.cisco.ExtendedAccessList;
import batfish.representation.cisco.ExtendedAccessListLine;
import batfish.representation.cisco.Interface;
import batfish.representation.cisco.IpAsPathAccessList;
import batfish.representation.cisco.IpAsPathAccessListLine;
import batfish.representation.cisco.OspfProcess;
import batfish.representation.cisco.PrefixList;
import batfish.representation.cisco.PrefixListLine;
import batfish.representation.cisco.StandardAccessList;
import batfish.representation.cisco.StandardCommunityList;
import batfish.representation.cisco.StaticRoute;
import batfish.util.SubRange;

public class CiscoControlPlaneExtractor extends CiscoGrammarBaseListener {

   private static final double LOOPBACK_BANDWIDTH = 1E12; // dirty hack: just
                                                          // chose a very large
                                                          // number
   private static final double TEN_GIGABIT_ETHERNET_BANDWIDTH = 10E9;
   private static final double GIGABIT_ETHERNET_BANDWIDTH = 1E9;
   private static final double FAST_ETHERNET_BANDWIDTH = 100E6;
   private static final int DEFAULT_STATIC_ROUTE_DISTANCE = 1;
   private CiscoConfiguration _configuration;
   private Interface _currentInterface;
   private ExtendedAccessList _currentExtendedAcl;
   private StandardAccessList _currentStandardAcl;
   private String _text;
   private IpAsPathAccessList _currentAsPathAcl;
   private ExpandedCommunityList _currentExpandedCommunityList;
   private StandardCommunityList _currentStandardCommunityList;
   private PrefixList _currentPrefixList;

   private static double getDefaultBandwidth(String name) {
      Double bandwidth = null;
      if (name.startsWith("FastEthernet")) {
         bandwidth = FAST_ETHERNET_BANDWIDTH;
      }
      else if (name.startsWith("GigabitEthernet")) {
         bandwidth = GIGABIT_ETHERNET_BANDWIDTH;
      }
      else if (name.startsWith("TenGigabitEthernet")) {
         bandwidth = TEN_GIGABIT_ETHERNET_BANDWIDTH;
      }
      else if (name.startsWith("Vlan")) {
         bandwidth = null;
      }
      else if (name.startsWith("Loopback")) {
         bandwidth = LOOPBACK_BANDWIDTH;
      }
      if (bandwidth == null) {
         bandwidth = 1.0;
      }
      return bandwidth;
   }

   public String getText() {
      return _text;
   }

   public CiscoControlPlaneExtractor(String text) {
      _text = text;
   }

   public CiscoConfiguration getConfiguration() {
      return _configuration;
   }

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

   public static int toInteger(Token t) {
      return Integer.parseInt(t.getText());
   }

   public static int toInteger(TerminalNode t) {
      return Integer.parseInt(t.getText());
   }

   public static int toInteger(IntegerContext ctx) {
      return Integer.parseInt(ctx.getText());
   }

   public static Ip toIp(Token t) {
      return new Ip(t.getText());
   }

   public static Ip getIp(Access_list_ip_rangeContext ctx) {
      if (ctx.ip != null) {
         return toIp(ctx.ip);
      }
      else {
         return new Ip(0l);
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
      else if (ctx.FTP() != null) {
         return 21;
      }
      else if (ctx.FTP_DATA() != null) {
         return 20;
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

   public static int getProtocolNumber(ProtocolContext ctx) {
      if (ctx.DEC() != null) {
         return toInteger(ctx.DEC());
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

   @Override
   public void exitAddress_family_rb_stanza(Address_family_rb_stanzaContext ctx) {
      /*
       * BgpProcess bgpProcess = _configuration.getBgpProcess();
       * 
       * p.setDefaultMetric(_addressFamily.getDefaultMetric());
       * p.addNetworks(_addressFamily.getNetworks());
       * p.addActivatedNeighbors(_addressFamily.getNeighbors());
       * p.addPeerGroupRouteReflectorClients(_addressFamily.getRRCPeerGroups());
       * p.addSendCommunityPeerGroups(_addressFamily.getSCPeerGroups());
       * p.addPeerGroupInboundRouteMaps(_addressFamily.getInboundRouteMaps());
       * p.addPeerGroupOutboundRouteMaps(_addressFamily.getOutboundRouteMaps());
       * p.addDefaultOriginateNeighbors(_addressFamily
       * .getDefaultOriginateNeighbors());
       * p.getAggregateNetworks().putAll(_addressFamily.getAggregateNetworks());
       * p.setRedistributeStatic(_addressFamily.getRedistributeStatic());
       * p.setRedistributeStaticMap(_addressFamily.getRedistributeStaticMap());
       * p
       * .addPeerGroupInboundPrefixLists(_addressFamily.getInboundPrefixLists())
       * ; p.addPeerGroupMembership(_addressFamily.getPeerGroupMembership());
       */
   }

   @Override
   public void exitAggregate_address_af_stanza(
         Aggregate_address_af_stanzaContext ctx) {
      BgpProcess proc = _configuration.getBgpProcess();
      Ip network = toIp(ctx.network);
      Ip subnet = toIp(ctx.subnet);
      BgpNetwork net = new BgpNetwork(network, subnet);
      boolean summaryOnly = ctx.SUMMARY_ONLY() != null;
      proc.getAggregateNetworks().put(net, summaryOnly);
   }

   @Override
   public void exitAggregate_address_rb_stanza(
         Aggregate_address_rb_stanzaContext ctx) {
      BgpProcess proc = _configuration.getBgpProcess();
      Ip network = toIp(ctx.network);
      Ip subnet = toIp(ctx.subnet);
      BgpNetwork net = new BgpNetwork(network, subnet);
      boolean summaryOnly = ctx.SUMMARY_ONLY() != null;
      proc.getAggregateNetworks().put(net, summaryOnly);
   }

   @Override
   public void exitArea_nssa_ro_stanza(Area_nssa_ro_stanzaContext ctx) {
      OspfProcess proc = _configuration.getOspfProcess();
      int area = toInteger(ctx.num);
      boolean noSummary = ctx.NO_SUMMARY() != null;
      proc.getNssas().put(area, noSummary);
   }

   @Override
   public void exitAuto_summary_af_stanza(Auto_summary_af_stanzaContext ctx) {
      // TODO implement
   }

   @Override
   public void enterCisco_configuration(Cisco_configurationContext ctx) {
      _configuration = new CiscoConfiguration();
      _configuration.setContext(ctx);
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
   public void exitDefault_metric_af_stanza(Default_metric_af_stanzaContext ctx) {
      BgpProcess proc = _configuration.getBgpProcess();
      int metric = toInteger(ctx.metric);
      proc.setDefaultMetric(metric);
   }

   @Override
   public void exitDefault_metric_rb_stanza(Default_metric_rb_stanzaContext ctx) {
      BgpProcess proc = _configuration.getBgpProcess();
      int metric = toInteger(ctx.metric);
      proc.setDefaultMetric(metric);
   }

   @Override
   public void enterExtended_access_list_stanza(
         Extended_access_list_stanzaContext ctx) {
      String name;
      if (ctx.firstnum != null) {
         name = Integer.toString(toInteger(ctx.firstnum));
      }
      else {
         name = ctx.ip_access_list_extended_stanza().name.getText();
      }
      Map<String, ExtendedAccessList> extAcls = _configuration
            .getExtendedAcls();
      _currentExtendedAcl = new ExtendedAccessList(name);
      _currentExtendedAcl.setContext(ctx);
      extAcls.put(name, _currentExtendedAcl);
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

   @Override
   public void exitHostname_stanza(Hostname_stanzaContext ctx) {
      _configuration.setHostname(ctx.name.getText());
   }

   @Override
   public void enterInterface_stanza(Interface_stanzaContext ctx) {
      String name = ctx.iname.getText();
      double bandwidth = getDefaultBandwidth(name);
      Interface newInterface = new Interface(name);
      newInterface.setContext(ctx);
      newInterface.setBandwidth(bandwidth);
      _configuration.getInterfaces().put(name, newInterface);
      _currentInterface = newInterface;
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
      _currentInterface.setIp(new Ip(ctx.ip.getText()));
      _currentInterface.setSubnetMask(new Ip(ctx.subnet.getText()));
      _currentInterface.setIpAddressStanzaContext(ctx);
   }

   @Override
   public void exitIp_address_secondary_if_stanza(
         Ip_address_secondary_if_stanzaContext ctx) {
      // TODO implement
   }

   @Override
   public void enterIp_as_path_access_list_stanza(
         Ip_as_path_access_list_stanzaContext ctx) {
      String name = ctx.firstname.getText();
      _currentAsPathAcl = new IpAsPathAccessList(name);
      _currentAsPathAcl.setContext(ctx);
      _configuration.getAsPathAccessLists().put(name, _currentAsPathAcl);
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
   public void enterIp_community_list_expanded_stanza(
         Ip_community_list_expanded_stanzaContext ctx) {
      String name = ctx.firstname.getText();

      _currentExpandedCommunityList = new ExpandedCommunityList(name);
      _currentExpandedCommunityList.setContext(ctx);
      _configuration.getExpandedCommunityLists().put(name,
            _currentExpandedCommunityList);
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
   public void enterIp_community_list_standard_stanza(
         Ip_community_list_standard_stanzaContext ctx) {
      String name = ctx.firstname.getText();

      _currentStandardCommunityList = new StandardCommunityList(name);
      _currentStandardCommunityList.setContext(ctx);
      _configuration.getStandardCommunityLists().put(name,
            _currentStandardCommunityList);
   }

   @Override
   public void exitIp_community_list_standard_stanza(
         Ip_community_list_standard_stanzaContext ctx) {
      _currentStandardCommunityList = null;
   }

   @Override
   public void exitIp_default_gateway_stanza(
         Ip_default_gateway_stanzaContext ctx) {
      // TODO implement
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
   public void enterIp_prefix_list_stanza(Ip_prefix_list_stanzaContext ctx) {
      String name = ctx.name.getText();
      _currentPrefixList = new PrefixList(name);
      _currentPrefixList.setContext(ctx);
      _configuration.getPrefixLists().put(name, _currentPrefixList);
   }

   @Override
   public void exitIp_prefix_list_stanza(Ip_prefix_list_stanzaContext ctx) {
      _currentPrefixList = null;
   }

   @Override
   public void exitIp_prefix_list_tail(Ip_prefix_list_tailContext ctx) {
      /*
      if (ctx.seqnum != null) {
         int seqnum = toInteger(ctx.seqnum);
      }
      */
      LineAction action = getAccessListAction(ctx.action);
      Ip prefix = toIp(ctx.prefix);
      int prefixLength = toInteger(ctx.prefix_length);
      int minLen = prefixLength;
      int maxLen = 32;
      if (ctx.minpl != null) {
         minLen = toInteger(ctx.minpl);
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
      Ip prefix = toIp(ctx.prefix);
      Ip mask = toIp(ctx.mask);
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
      StaticRoute route = new StaticRoute(prefix, mask, nextHopIp, nextHopInterface, distance, tag, track, permanent);
      _configuration.getStaticRoutes().put(prefix.networkString(mask), route);
   }

   @Override
   public void exitIpv6_router_ospf_stanza(Ipv6_router_ospf_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMacro_stanza(Macro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMatch_as_path_access_list_rm_stanza(
         Match_as_path_access_list_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMatch_community_list_rm_stanza(
         Match_community_list_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMatch_ip_access_list_rm_stanza(
         Match_ip_access_list_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMatch_ip_prefix_list_rm_stanza(
         Match_ip_prefix_list_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMatch_ipv6_rm_stanza(Match_ipv6_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMatch_rm_stanza(Match_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMatch_tag_rm_stanza(Match_tag_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitMaximum_paths_ro_stanza(Maximum_paths_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_activate_af_stanza(
         Neighbor_activate_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_default_originate_af_stanza(
         Neighbor_default_originate_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_ebgp_multihop_rb_stanza(
         Neighbor_ebgp_multihop_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_ip_route_reflector_client_af_stanza(
         Neighbor_ip_route_reflector_client_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_next_hop_self_rb_stanza(
         Neighbor_next_hop_self_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_peer_group_assignment_af_stanza(
         Neighbor_peer_group_assignment_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_peer_group_assignment_rb_stanza(
         Neighbor_peer_group_assignment_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_peer_group_creation_rb_stanza(
         Neighbor_peer_group_creation_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_pg_prefix_list_rb_stanza(
         Neighbor_pg_prefix_list_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_pg_remote_as_rb_stanza(
         Neighbor_pg_remote_as_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_pg_route_map_rb_stanza(
         Neighbor_pg_route_map_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_pg_route_reflector_client_af_stanza(
         Neighbor_pg_route_reflector_client_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_prefix_list_af_stanza(
         Neighbor_prefix_list_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_remove_private_as_af_stanza(
         Neighbor_remove_private_as_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_route_map_af_stanza(
         Neighbor_route_map_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_route_reflector_client_af_stanza(
         Neighbor_route_reflector_client_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_send_community_af_stanza(
         Neighbor_send_community_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_send_community_rb_stanza(
         Neighbor_send_community_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_shutdown_rb_stanza(
         Neighbor_shutdown_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNeighbor_update_source_rb_stanza(
         Neighbor_update_source_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNetwork_af_stanza(Network_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNetwork_rb_stanza(Network_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNetwork_ro_stanza(Network_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNo_ip_address_if_stanza(No_ip_address_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitNo_neighbor_activate_af_stanza(
         No_neighbor_activate_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPassive_interface_default_ro_stanza(
         Passive_interface_default_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPassive_interface_ipv6_ro_stanza(
         Passive_interface_ipv6_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPassive_interface_ro_stanza(
         Passive_interface_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPort(PortContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitPort_specifier(Port_specifierContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitProtocol(ProtocolContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRange(RangeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRb_stanza(Rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRedistribute_bgp_ro_stanza(
         Redistribute_bgp_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRedistribute_connected_af_stanza(
         Redistribute_connected_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRedistribute_connected_rb_stanza(
         Redistribute_connected_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRedistribute_connected_ro_stanza(
         Redistribute_connected_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRedistribute_ipv6_ro_stanza(
         Redistribute_ipv6_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRedistribute_ospf_rb_stanza(
         Redistribute_ospf_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRedistribute_static_af_stanza(
         Redistribute_static_af_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRedistribute_static_rb_stanza(
         Redistribute_static_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRedistribute_static_ro_stanza(
         Redistribute_static_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRm_stanza(Rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRo_stanza(Ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRoute_map_stanza(Route_map_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRouter_bgp_stanza(Router_bgp_stanzaContext ctx) {
      int procNum = toInteger(ctx.procnum);
      BgpProcess proc = new BgpProcess(procNum);
      _configuration.setBgpProcess(proc, ctx);
   }

   @Override
   public void exitRouter_bgp_stanza(Router_bgp_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRouter_id_bgp_rb_stanza(Router_id_bgp_rb_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRouter_id_ipv6_ro_stanza(Router_id_ipv6_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitRouter_id_ro_stanza(Router_id_ro_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void enterRouter_ospf_stanza(Router_ospf_stanzaContext ctx) {
      int procNum = toInteger(ctx.procnum);
      OspfProcess proc = new OspfProcess(procNum);
      _configuration.setOspfProcess(proc, ctx);
   }

   @Override
   public void exitRouter_ospf_stanza(Router_ospf_stanzaContext ctx) {

   }

   @Override
   public void exitSet_as_path_prepend_rm_stanza(
         Set_as_path_prepend_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSet_as_path_rm_stanza(Set_as_path_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSet_comm_list_delete_rm_stanza(
         Set_comm_list_delete_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSet_community_additive_rm_stanza(
         Set_community_additive_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSet_community_rm_stanza(Set_community_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSet_ipv6_rm_stanza(Set_ipv6_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSet_local_preference_rm_stanza(
         Set_local_preference_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSet_metric_rm_stanza(Set_metric_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSet_next_hop_rm_stanza(Set_next_hop_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSet_origin_rm_stanza(Set_origin_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSet_rm_stanza(Set_rm_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitShutdown_if_stanza(Shutdown_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitStandard_access_list_stanza(
         Standard_access_list_stanzaContext ctx) {
      _currentStandardAcl = null;
   }

   @Override
   public void enterStandard_access_list_stanza(
         Standard_access_list_stanzaContext ctx) {
      String name;
      if (ctx.firstnum != null) {
         name = Integer.toString(toInteger(ctx.firstnum));
      }
      else {
         name = ctx.ip_access_list_standard_stanza().name.getText();
      }
      Map<String, StandardAccessList> standardAcls = _configuration
            .getStandardAcls();
      _currentStandardAcl = standardAcls.get(name);
      if (_currentStandardAcl == null) {
         _currentStandardAcl = new StandardAccessList(name);
         _currentStandardAcl.setContext(ctx);
         standardAcls.put(name, _currentStandardAcl);
      }
   }

   @Override
   public void exitStanza(StanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSubrange(SubrangeContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSwitchport_access_if_stanza(
         Switchport_access_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSwitchport_if_stanza(Switchport_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSwitchport_mode_access_stanza(
         Switchport_mode_access_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSwitchport_mode_dynamic_auto_stanza(
         Switchport_mode_dynamic_auto_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSwitchport_mode_dynamic_desirable_stanza(
         Switchport_mode_dynamic_desirable_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSwitchport_mode_trunk_stanza(
         Switchport_mode_trunk_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSwitchport_trunk_allowed_if_stanza(
         Switchport_trunk_allowed_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSwitchport_trunk_encapsulation_if_stanza(
         Switchport_trunk_encapsulation_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSwitchport_trunk_if_stanza(
         Switchport_trunk_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitSwitchport_trunk_native_if_stanza(
         Switchport_trunk_native_if_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

   @Override
   public void exitVrf_stanza(Vrf_stanzaContext ctx) {
      // TODO Auto-generated method stub

   }

}
