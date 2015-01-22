package batfish.grammar.flatjuniper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import batfish.grammar.ParseTreePrettyPrinter;
import batfish.grammar.flatjuniper.FlatJuniperParser.*;
import batfish.grammar.flatjuniper.FlatJuniperCombinedParser;
import batfish.grammar.flatjuniper.FlatJuniperParser.Named_communityContext;
import batfish.main.BatfishException;
import batfish.representation.AsPath;
import batfish.representation.AsSet;
import batfish.representation.Ip;
import batfish.representation.IpProtocol;
import batfish.representation.Prefix;
import batfish.representation.RoutingProtocol;
import batfish.representation.juniper.AggregateRoute;
import batfish.representation.juniper.BgpGroup;
import batfish.representation.juniper.BgpGroup.BgpGroupType;
import batfish.representation.juniper.CommunityList;
import batfish.representation.juniper.CommunityListLine;
import batfish.representation.juniper.Family;
import batfish.representation.juniper.FwFrom;
import batfish.representation.juniper.FwFromDestinationAddress;
import batfish.representation.juniper.FwFromDestinationPort;
import batfish.representation.juniper.FwFromProtocol;
import batfish.representation.juniper.FwFromSourceAddress;
import batfish.representation.juniper.FwFromSourcePort;
import batfish.representation.juniper.FwTerm;
import batfish.representation.juniper.FwThenAccept;
import batfish.representation.juniper.FwThenDiscard;
import batfish.representation.juniper.FwThenNextTerm;
import batfish.representation.juniper.PsFrom;
import batfish.representation.juniper.PsFromAsPath;
import batfish.representation.juniper.PsFromColor;
import batfish.representation.juniper.PsFromCommunity;
import batfish.representation.juniper.PsFromInterface;
import batfish.representation.juniper.PsFromPrefixList;
import batfish.representation.juniper.PsFromProtocol;
import batfish.representation.juniper.PsFromRouteFilter;
import batfish.representation.juniper.RouteFilterLine;
import batfish.representation.juniper.RouteFilterLineExact;
import batfish.representation.juniper.RouteFilterLineLengthRange;
import batfish.representation.juniper.RouteFilterLineOrLonger;
import batfish.representation.juniper.RouteFilterLineThrough;
import batfish.representation.juniper.RouteFilterLineUpTo;
import batfish.representation.juniper.FirewallFilter;
import batfish.representation.juniper.IpBgpGroup;
import batfish.representation.juniper.GeneratedRoute;
import batfish.representation.juniper.Interface;
import batfish.representation.juniper.JuniperVendorConfiguration;
import batfish.representation.juniper.NamedBgpGroup;
import batfish.representation.juniper.OspfArea;
import batfish.representation.juniper.PolicyStatement;
import batfish.representation.juniper.PrefixList;
import batfish.representation.juniper.RouteFilter;
import batfish.representation.juniper.RoutingInformationBase;
import batfish.representation.juniper.RoutingInstance;
import batfish.representation.juniper.StaticRoute;
import batfish.representation.juniper.PsTerm;
import batfish.representation.juniper.PsThen;
import batfish.representation.juniper.PsThenAccept;
import batfish.representation.juniper.PsThenCommunityAdd;
import batfish.representation.juniper.PsThenCommunityDelete;
import batfish.representation.juniper.PsThenCommunitySet;
import batfish.representation.juniper.PsThenLocalPreference;
import batfish.representation.juniper.PsThenMetric;
import batfish.representation.juniper.PsThenNextHopIp;
import batfish.representation.juniper.PsThenReject;
import batfish.util.SubRange;

public class ConfigurationBuilder extends FlatJuniperParserBaseListener {

   private static final AggregateRoute DUMMY_AGGREGATE_ROUTE = new AggregateRoute(
         Prefix.ZERO);

   private static final BgpGroup DUMMY_BGP_GROUP = new BgpGroup();

   private static final StaticRoute DUMMY_STATIC_ROUTE = new StaticRoute(
         Prefix.ZERO);

   public static SubRange getPortRange(PortContext ctx) {
      if (ctx.DEC() != null) {
         int port = toInt(ctx.DEC());
         return new SubRange(port, port);
      }
      else if (ctx.BOOTPC() != null) {
         return new SubRange(68, 68);
      }
      else if (ctx.BOOTPS() != null) {
         return new SubRange(67, 67);
      }
      else if (ctx.BGP() != null) {
         return new SubRange(179, 179);
      }
      else if (ctx.DHCP() != null) {
         return new SubRange(67, 68);
      }
      else if (ctx.DOMAIN() != null) {
         return new SubRange(53, 53);
      }
      else if (ctx.FTP() != null) {
         return new SubRange(21, 21);
      }
      else if (ctx.HTTP() != null) {
         return new SubRange(80, 80);
      }
      else if (ctx.NETBIOS_DGM() != null) {
         return new SubRange(138, 138);
      }
      else if (ctx.NETBIOS_NS() != null) {
         return new SubRange(137, 137);
      }
      else if (ctx.NTP() != null) {
         return new SubRange(123, 123);
      }
      else if (ctx.SMTP() != null) {
         return new SubRange(25, 25);
      }
      else if (ctx.SNMP() != null) {
         return new SubRange(161, 161);
      }
      else if (ctx.SSH() != null) {
         return new SubRange(22, 22);
      }
      else if (ctx.SYSLOG() != null) {
         return new SubRange(514, 514);
      }
      else if (ctx.TACACS() != null) {
         return new SubRange(49, 49);
      }
      else if (ctx.TELNET() != null) {
         return new SubRange(23, 23);
      }
      else if (ctx.TFTP() != null) {
         return new SubRange(69, 69);
      }
      else {
         throw new BatfishException("missing port-number mapping for port: \""
               + ctx.getText() + "\"");
      }
   }

   private static long toCommunityLong(Named_communityContext ctx) {
      if (ctx.NO_ADVERTISE() != null) {
         return 0xFFFFFF02l;
      }
      else {
         throw new BatfishException(
               "missing named-community-to-long mapping for: \""
                     + ctx.getText() + "\"");
      }
   }

   private static Integer toInt(TerminalNode node) {
      return toInt(node.getSymbol());
   }

   private static int toInt(Token token) {
      return Integer.parseInt(token.getText());
   }

   private static IpProtocol toIpProtocol(Ip_protocolContext ctx) {
      if (ctx.DEC() != null) {
         int protocolNum = toInt(ctx.DEC());
         return IpProtocol.fromNumber(protocolNum);
      }
      else if (ctx.AH() != null) {
         return IpProtocol.AHP;
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
      else if (ctx.IGMP() != null) {
         return IpProtocol.IGMP;
      }
      else if (ctx.OSPF() != null) {
         return IpProtocol.OSPF;
      }
      else if (ctx.PIM() != null) {
         return IpProtocol.PIM;
      }
      else if (ctx.RSVP() != null) {
         return IpProtocol.RSVP;
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
         throw new BatfishException(
               "missing protocol-enum mapping for protocol: \"" + ctx.getText()
                     + "\"");
      }
   }

   private static RoutingProtocol toRoutingProtocol(Routing_protocolContext ctx) {
      if (ctx.AGGREGATE() != null) {
         return RoutingProtocol.AGGREGATE;
      }
      else if (ctx.BGP() != null) {
         return RoutingProtocol.BGP;
      }
      else if (ctx.DIRECT() != null) {
         return RoutingProtocol.CONNECTED;
      }
      else if (ctx.ISIS() != null) {
         return RoutingProtocol.ISIS;
      }
      else if (ctx.OSPF() != null) {
         return RoutingProtocol.OSPF;
      }
      else if (ctx.STATIC() != null) {
         return RoutingProtocol.STATIC;
      }
      else {
         throw new BatfishException("missing routing protocol-enum mapping");
      }
   }

   private JuniperVendorConfiguration _configuration;

   private AggregateRoute _currentAggregateRoute;

   private OspfArea _currentArea;

   private BgpGroup _currentBgpGroup;

   private CommunityList _currentCommunityList;

   private FirewallFilter _currentFilter;

   private Family _currentFirewallFamily;

   private FwTerm _currentFwTerm;

   private GeneratedRoute _currentGeneratedRoute;

   private Interface _currentInterface;

   private Interface _currentMasterInterface;

   private Interface _currentOspfInterface;

   private PolicyStatement _currentPolicyStatement;

   private PrefixList _currentPrefixList;

   private PsTerm _currentPsTerm;

   private Set<PsThen> _currentPsThens;

   private RoutingInformationBase _currentRib;

   private RouteFilter _currentRouteFilter;

   private RouteFilterLine _currentRouteFilterLine;

   private Prefix _currentRouteFilterPrefix;

   private RoutingInstance _currentRoutingInstance;

   private StaticRoute _currentStaticRoute;

   private FlatJuniperCombinedParser _parser;

   private Set<String> _rulesWithSuppressedWarnings;

   private final Map<PsTerm, RouteFilter> _termRouteFilters;

   private String _text;

   private List<String> _warnings;

   public ConfigurationBuilder(FlatJuniperCombinedParser parser, String text,
         Set<String> rulesWithSuppressedWarnings, List<String> warnings) {
      _parser = parser;
      _text = text;
      _rulesWithSuppressedWarnings = rulesWithSuppressedWarnings;
      _warnings = warnings;
      _configuration = new JuniperVendorConfiguration();
      _currentRoutingInstance = _configuration.getDefaultRoutingInstance();
      _termRouteFilters = new HashMap<PsTerm, RouteFilter>();
   }

   @Override
   public void enterAt_interface(At_interfaceContext ctx) {
      String name = ctx.id.name.getText();
      String unit = null;
      if (ctx.id.unit != null) {
         unit = ctx.id.unit.getText();
      }
      String unitFullName = name + "." + unit;
      Map<String, Interface> interfaces = _currentRoutingInstance
            .getInterfaces();
      _currentOspfInterface = interfaces.get(name);
      if (_currentOspfInterface == null) {
         _currentOspfInterface = new Interface(name);
         interfaces.put(name, _currentOspfInterface);
      }
      if (unit != null) {
         Map<String, Interface> units = _currentOspfInterface.getUnits();
         _currentOspfInterface = units.get(unitFullName);
         if (_currentOspfInterface == null) {
            _currentOspfInterface = new Interface(unitFullName);
            units.put(unitFullName, _currentOspfInterface);
         }
      }
      Ip currentArea = _currentArea.getAreaIp();
      Ip interfaceArea = _currentOspfInterface.getOspfArea();
      if (interfaceArea != null && !currentArea.equals(interfaceArea)) {
         throw new BatfishException("Interface assigned to multiple areas");
      }
      _currentOspfInterface.setOspfArea(currentArea);
   }

   @Override
   public void enterBt_group(Bt_groupContext ctx) {
      String name = ctx.name.getText();
      Map<String, NamedBgpGroup> namedBgpGroups = _currentRoutingInstance
            .getNamedBgpGroups();
      NamedBgpGroup namedBgpGroup = namedBgpGroups.get(name);
      if (namedBgpGroup == null) {
         namedBgpGroup = new NamedBgpGroup(name);
         namedBgpGroup.setParent(_currentBgpGroup);
         namedBgpGroups.put(name, namedBgpGroup);
      }
      _currentBgpGroup = namedBgpGroup;
   }

   @Override
   public void enterBt_neighbor(Bt_neighborContext ctx) {
      if (ctx.IP_ADDRESS() != null) {
         Ip remoteAddress = new Ip(ctx.IP_ADDRESS().getText());
         Map<Ip, IpBgpGroup> ipBgpGroups = _currentRoutingInstance
               .getIpBgpGroups();
         IpBgpGroup ipBgpGroup = ipBgpGroups.get(remoteAddress);
         if (ipBgpGroup == null) {
            ipBgpGroup = new IpBgpGroup(remoteAddress);
            ipBgpGroup.setParent(_currentBgpGroup);
            ipBgpGroups.put(remoteAddress, ipBgpGroup);
         }
         _currentBgpGroup = ipBgpGroup;
      }
      else if (ctx.IPV6_ADDRESS() != null) {
         _currentBgpGroup = DUMMY_BGP_GROUP;
      }
   }

   @Override
   public void enterFromt_route_filter(Fromt_route_filterContext ctx) {
      if (ctx.IP_PREFIX() != null) {
         _currentRouteFilterPrefix = new Prefix(ctx.IP_PREFIX().getText());
         _currentRouteFilter = _termRouteFilters.get(_currentPsTerm);
         if (_currentRouteFilter == null) {
            String rfName = _currentPolicyStatement.getName() + ":"
                  + _currentPsTerm.getName();
            _currentRouteFilter = new RouteFilter(rfName);
            _termRouteFilters.put(_currentPsTerm, _currentRouteFilter);
            _configuration.getRouteFilters().put(rfName, _currentRouteFilter);
            PsFromRouteFilter from = new PsFromRouteFilter(rfName);
            _currentPsTerm.getFroms().add(from);
         }
      }
   }

   @Override
   public void enterFromt_route_filter_then(Fromt_route_filter_thenContext ctx) {
      RouteFilterLine line = _currentRouteFilterLine;
      _currentPsThens = line.getThens();
   }

   @Override
   public void enterFwft_term(Fwft_termContext ctx) {
      String name = ctx.name.getText();
      Map<String, FwTerm> terms = _currentFilter.getTerms();
      _currentFwTerm = terms.get(name);
      if (_currentFwTerm == null) {
         _currentFwTerm = new FwTerm(name);
         terms.put(name, _currentFwTerm);
      }
   }

   @Override
   public void enterFwt_family(Fwt_familyContext ctx) {
      if (ctx.INET() != null) {
         _currentFirewallFamily = Family.INET;
      }
      else if (ctx.INET6() != null) {
         _currentFirewallFamily = Family.INET6;
      }
      else if (ctx.MPLS() != null) {
         _currentFirewallFamily = Family.MPLS;
      }
   }

   @Override
   public void enterFwt_filter(Fwt_filterContext ctx) {
      String name = ctx.name.getText();
      Map<String, FirewallFilter> filters = _configuration.getFirewallFilters();
      _currentFilter = filters.get(name);
      if (_currentFilter == null) {
         _currentFilter = new FirewallFilter(name, _currentFirewallFamily);
         filters.put(name, _currentFilter);
      }
   }

   @Override
   public void enterIt_unit(It_unitContext ctx) {
      String unit = ctx.num.getText();
      String unitFullName = _currentMasterInterface.getName() + "." + unit;
      Map<String, Interface> units = _currentMasterInterface.getUnits();
      _currentInterface = units.get(unitFullName);
      if (_currentInterface == null) {
         _currentInterface = new Interface(unitFullName);
         units.put(unitFullName, _currentInterface);
      }
   }

   @Override
   public void enterOt_area(Ot_areaContext ctx) {
      Ip areaIp = new Ip(ctx.area.getText());
      Map<Ip, OspfArea> areas = _currentRoutingInstance.getOspfAreas();
      _currentArea = areas.get(areaIp);
      if (_currentArea == null) {
         _currentArea = new OspfArea(areaIp);
         areas.put(areaIp, _currentArea);
      }
   }

   @Override
   public void enterPot_community(Pot_communityContext ctx) {
      String name = ctx.name.getText();
      Map<String, CommunityList> communityLists = _configuration
            .getCommunityLists();
      _currentCommunityList = communityLists.get(name);
      if (_currentCommunityList == null) {
         _currentCommunityList = new CommunityList(name);
         communityLists.put(name, _currentCommunityList);
      }
   }

   @Override
   public void enterPot_policy_statement(Pot_policy_statementContext ctx) {
      String name = ctx.name.getText();
      Map<String, PolicyStatement> policyStatements = _configuration
            .getPolicyStatements();
      _currentPolicyStatement = policyStatements.get(name);
      if (_currentPolicyStatement == null) {
         _currentPolicyStatement = new PolicyStatement(name);
         policyStatements.put(name, _currentPolicyStatement);
      }
      _currentPsTerm = _currentPolicyStatement.getSingletonTerm();
      _currentPsThens = _currentPsTerm.getThens();
   }

   @Override
   public void enterPot_prefix_list(Pot_prefix_listContext ctx) {
      String name = ctx.name.getText();
      Map<String, PrefixList> prefixLists = _configuration.getPrefixLists();
      _currentPrefixList = prefixLists.get(name);
      if (_currentPrefixList == null) {
         _currentPrefixList = new PrefixList(name);
         prefixLists.put(name, _currentPrefixList);
      }
   }

   @Override
   public void enterPst_term(Pst_termContext ctx) {
      String name = ctx.name.getText();
      Map<String, PsTerm> terms = _currentPolicyStatement.getTerms();
      _currentPsTerm = terms.get(name);
      if (_currentPsTerm == null) {
         _currentPsTerm = new PsTerm(name);
         terms.put(name, _currentPsTerm);
      }
      _currentPsThens = _currentPsTerm.getThens();
   }

   @Override
   public void enterRibt_aggregate(Ribt_aggregateContext ctx) {
      if (ctx.IP_PREFIX() != null) {
         Prefix prefix = new Prefix(ctx.IP_PREFIX().getText());
         Map<Prefix, AggregateRoute> aggregateRoutes = _currentRib
               .getAggregateRoutes();
         _currentAggregateRoute = aggregateRoutes.get(prefix);
         if (_currentAggregateRoute == null) {
            _currentAggregateRoute = new AggregateRoute(prefix);
            aggregateRoutes.put(prefix, _currentAggregateRoute);
         }
      }
      else {
         _currentAggregateRoute = DUMMY_AGGREGATE_ROUTE;
      }
   }

   @Override
   public void enterRit_named_routing_instance(
         Rit_named_routing_instanceContext ctx) {
      String name;
      name = ctx.name.getText();
      _currentRoutingInstance = _configuration.getRoutingInstances().get(name);
      if (_currentRoutingInstance == null) {
         _currentRoutingInstance = new RoutingInstance(name);
         _configuration.getRoutingInstances()
               .put(name, _currentRoutingInstance);
      }
   }

   @Override
   public void enterRot_generate(Rot_generateContext ctx) {
      if (ctx.IP_PREFIX() != null) {
         Prefix prefix = new Prefix(ctx.IP_PREFIX().getText());
         Map<Prefix, GeneratedRoute> generatedRoutes = _currentRib
               .getGeneratedRoutes();
         _currentGeneratedRoute = generatedRoutes.get(prefix);
         if (_currentGeneratedRoute == null) {
            _currentGeneratedRoute = new GeneratedRoute(prefix);
            generatedRoutes.put(prefix, _currentGeneratedRoute);
         }
      }
   }

   @Override
   public void enterRot_rib(Rot_ribContext ctx) {
      String name = ctx.name.getText();
      Map<String, RoutingInformationBase> ribs = _currentRoutingInstance
            .getRibs();
      _currentRib = ribs.get(name);
      if (_currentRib == null) {
         _currentRib = new RoutingInformationBase(name);
         ribs.put(name, _currentRib);
      }
   }

   @Override
   public void enterRot_static(Rot_staticContext ctx) {
      if (ctx.IP_PREFIX() != null) {
         Prefix prefix = new Prefix(ctx.IP_PREFIX().getText());
         Map<Prefix, StaticRoute> staticRoutes = _currentRib.getStaticRoutes();
         _currentStaticRoute = staticRoutes.get(prefix);
         if (_currentStaticRoute == null) {
            _currentStaticRoute = new StaticRoute(prefix);
            staticRoutes.put(prefix, _currentStaticRoute);
         }
      }
      else if (ctx.IPV6_PREFIX() != null) {
         _currentStaticRoute = DUMMY_STATIC_ROUTE;
      }
   }

   @Override
   public void enterS_firewall(S_firewallContext ctx) {
      _currentFirewallFamily = Family.INET;
   }

   @Override
   public void enterS_interfaces(S_interfacesContext ctx) {
      if (ctx.name == null) {
         _currentInterface = _currentRoutingInstance.getGlobalMasterInterface();
      }
      else {
         String ifaceName = ctx.name.getText();
         Map<String, Interface> interfaces = _currentRoutingInstance
               .getInterfaces();
         _currentInterface = interfaces.get(ifaceName);
         if (_currentInterface == null) {
            _currentInterface = new Interface(ifaceName);
            interfaces.put(ifaceName, _currentInterface);
         }
      }
      _currentMasterInterface = _currentInterface;
   }

   @Override
   public void enterS_protocols_bgp(S_protocols_bgpContext ctx) {
      _currentBgpGroup = _currentRoutingInstance.getMasterBgpGroup();
   }

   @Override
   public void enterS_routing_options(S_routing_optionsContext ctx) {
      _currentRib = _currentRoutingInstance.getRibs().get(
            RoutingInformationBase.RIB_IPV4_UNICAST);
   }

   @Override
   public void exitAgt_as_path(Agt_as_pathContext ctx) {
      AsPath asPath = toAsPath(ctx.path);
      _currentAggregateRoute.setAsPath(asPath);
   }

   @Override
   public void exitAgt_preference(Agt_preferenceContext ctx) {
      int preference = toInt(ctx.preference);
      _currentAggregateRoute.setPreference(preference);
   }

   @Override
   public void exitAit_passive(Ait_passiveContext ctx) {
      _currentOspfInterface.setOspfPassive(true);
   }

   @Override
   public void exitAt_interface(At_interfaceContext ctx) {
      _currentOspfInterface = null;
   }

   @Override
   public void exitBt_description(Bt_descriptionContext ctx) {
      String description = ctx.s_description().description.getText();
      _currentBgpGroup.setDescription(description);
   }

   @Override
   public void exitBt_export(Bt_exportContext ctx) {
      Policy_expressionContext expr = ctx.expr;
      if (expr.variable() != null) {
         String name = expr.variable().getText();
         _currentBgpGroup.getExportPolicies().add(name);
      }
      else {
         todo(ctx, "complex policy expressions unsupported at this time");
      }
   }

   @Override
   public void exitBt_import(Bt_importContext ctx) {
      Policy_expressionContext expr = ctx.expr;
      if (expr.variable() != null) {
         String name = expr.variable().getText();
         _currentBgpGroup.getImportPolicies().add(name);
      }
      else {
         todo(ctx, "complex policy expressions unsupported at this time");
      }
   }

   @Override
   public void exitBt_local_address(Bt_local_addressContext ctx) {
      if (ctx.IP_ADDRESS() != null) {
         Ip localAddress = new Ip(ctx.IP_ADDRESS().getText());
         _currentBgpGroup.setLocalAddress(localAddress);
      }
   }

   @Override
   public void exitBt_local_as(Bt_local_asContext ctx) {
      int localAs = toInt(ctx.as);
      _currentBgpGroup.setLocalAs(localAs);
   }

   @Override
   public void exitBt_peer_as(Bt_peer_asContext ctx) {
      int peerAs = toInt(ctx.as);
      _currentBgpGroup.setPeerAs(peerAs);
   }

   @Override
   public void exitBt_remove_private(Bt_remove_privateContext ctx) {
      _currentBgpGroup.setRemovePrivate(true);
   }

   @Override
   public void exitBt_type(Bt_typeContext ctx) {
      if (ctx.INTERNAL() != null) {
         _currentBgpGroup.setType(BgpGroupType.INTERNAL);
      }
      else if (ctx.EXTERNAL() != null) {
         _currentBgpGroup.setType(BgpGroupType.EXTERNAL);
      }
   }

   @Override
   public void exitCt_members(Ct_membersContext ctx) {
      if (ctx.community_regex() != null) {
         _currentCommunityList.getLines().add(
               new CommunityListLine(ctx.community_regex().getText()));
      }
      else if (ctx.named_community() != null) {
         long communityVal = toCommunityLong(ctx.named_community());
         String communityStr = batfish.util.Util.longToCommunity(communityVal);
         _currentCommunityList.getLines().add(
               new CommunityListLine(communityStr));
      }
      else if (ctx.extended_community() != null) {
         todo(ctx, "extended communities not currently supported");
      }
   }

   @Override
   public void exitFromt_as_path(Fromt_as_pathContext ctx) {
      String name = ctx.name.getText();
      PsFromAsPath fromAsPath = new PsFromAsPath(name);
      _currentPsTerm.getFroms().add(fromAsPath);
   }

   @Override
   public void exitFromt_color(Fromt_colorContext ctx) {
      int color = toInt(ctx.color);
      PsFromColor fromColor = new PsFromColor(color);
      _currentPsTerm.getFroms().add(fromColor);
   }

   @Override
   public void exitFromt_community(Fromt_communityContext ctx) {
      String name = ctx.name.getText();
      PsFromCommunity fromCommunity = new PsFromCommunity(name);
      _currentPsTerm.getFroms().add(fromCommunity);
   }

   @Override
   public void exitFromt_interface(Fromt_interfaceContext ctx) {
      String name = ctx.id.name.getText();
      String unit = null;
      if (ctx.id.unit != null) {
         unit = ctx.id.unit.getText();
      }
      String unitFullName = name + "." + unit;
      Map<String, Interface> interfaces = _currentRoutingInstance
            .getInterfaces();
      Interface iface = interfaces.get(name);
      if (iface == null) {
         iface = new Interface(name);
         interfaces.put(name, iface);
      }
      PsFrom from;
      if (unit != null) {
         Map<String, Interface> units = iface.getUnits();
         iface = units.get(unitFullName);
         if (iface == null) {
            iface = new Interface(unitFullName);
            units.put(unitFullName, iface);
         }
         from = new PsFromInterface(unitFullName);
      }
      else {
         from = new PsFromInterface(name);
      }
      _currentPsTerm.getFroms().add(from);
   }

   @Override
   public void exitFromt_prefix_list(Fromt_prefix_listContext ctx) {
      String name = ctx.name.getText();
      PsFrom from = new PsFromPrefixList(name);
      _currentPsTerm.getFroms().add(from);
   }

   @Override
   public void exitFromt_protocol(Fromt_protocolContext ctx) {
      RoutingProtocol protocol = toRoutingProtocol(ctx.protocol);
      PsFromProtocol fromProtocol = new PsFromProtocol(protocol);
      _currentPsTerm.getFroms().add(fromProtocol);
   }

   @Override
   public void exitFromt_route_filter(Fromt_route_filterContext ctx) {
      _currentRouteFilterPrefix = null;
      _currentRouteFilter = null;
      _currentRouteFilterLine = null;
   }

   @Override
   public void exitFromt_route_filter_then(Fromt_route_filter_thenContext ctx) {
      _currentPsThens = _currentPsTerm.getThens();
   }

   @Override
   public void exitFwfromt_destination_address(
         Fwfromt_destination_addressContext ctx) {
      if (ctx.IP_PREFIX() != null) {
         Prefix prefix = new Prefix(ctx.IP_PREFIX().getText());
         FwFrom from = new FwFromDestinationAddress(prefix);
         _currentFwTerm.getFroms().add(from);
      }
   }

   @Override
   public void exitFwfromt_destination_port(Fwfromt_destination_portContext ctx) {
      if (ctx.port() != null) {
         SubRange subrange = getPortRange(ctx.port());
         FwFrom from = new FwFromDestinationPort(subrange);
         _currentFwTerm.getFroms().add(from);
      }
      else if (ctx.range() != null) {
         for (SubrangeContext subrangeContext : ctx.range().range_list) {
            int low = toInt(subrangeContext.low);
            int high = toInt(subrangeContext.high);
            SubRange subrange = new SubRange(low, high);
            FwFrom from = new FwFromDestinationPort(subrange);
            _currentFwTerm.getFroms().add(from);
         }
      }
   }

   @Override
   public void exitFwfromt_protocol(Fwfromt_protocolContext ctx) {
      IpProtocol protocol = toIpProtocol(ctx.ip_protocol());
      FwFrom from = new FwFromProtocol(protocol);
      _currentFwTerm.getFroms().add(from);
   }

   @Override
   public void exitFwfromt_source_address(Fwfromt_source_addressContext ctx) {
      if (ctx.IP_PREFIX() != null) {
         Prefix prefix = new Prefix(ctx.IP_PREFIX().getText());
         FwFrom from = new FwFromSourceAddress(prefix);
         _currentFwTerm.getFroms().add(from);
      }
   }

   @Override
   public void exitFwfromt_source_port(Fwfromt_source_portContext ctx) {
      if (ctx.port() != null) {
         SubRange subrange = getPortRange(ctx.port());
         FwFrom from = new FwFromSourcePort(subrange);
         _currentFwTerm.getFroms().add(from);
      }
      else if (ctx.range() != null) {
         for (SubrangeContext subrangeContext : ctx.range().range_list) {
            int low = toInt(subrangeContext.low);
            int high = toInt(subrangeContext.high);
            SubRange subrange = new SubRange(low, high);
            FwFrom from = new FwFromSourcePort(subrange);
            _currentFwTerm.getFroms().add(from);
         }
      }
   }

   @Override
   public void exitFwft_term(Fwft_termContext ctx) {
      _currentFwTerm = null;
   }

   @Override
   public void exitFwt_filter(Fwt_filterContext ctx) {
      _currentFilter = null;
   }

   @Override
   public void exitFwthent_accept(Fwthent_acceptContext ctx) {
      _currentFwTerm.getThens().add(FwThenAccept.INSTANCE);
   }

   @Override
   public void exitFwthent_discard(Fwthent_discardContext ctx) {
      _currentFwTerm.getThens().add(FwThenDiscard.INSTANCE);
   }

   @Override
   public void exitFwthent_next_term(Fwthent_next_termContext ctx) {
      _currentFwTerm.getThens().add(FwThenNextTerm.INSTANCE);
   }

   @Override
   public void exitFwthent_reject(Fwthent_rejectContext ctx) {
      _currentFwTerm.getThens().add(FwThenDiscard.INSTANCE);
   }

   @Override
   public void exitGt_metric(Gt_metricContext ctx) {
      int metric = toInt(ctx.metric);
      _currentGeneratedRoute.setMetric(metric);
   }

   @Override
   public void exitGt_policy(Gt_policyContext ctx) {
      String policy = ctx.policy.getText();
      _currentGeneratedRoute.getPolicies().add(policy);
   }

   @Override
   public void exitIfamt_address(Ifamt_addressContext ctx) {
      Prefix prefix = new Prefix(ctx.IP_PREFIX().getText());
      _currentInterface.setPrefix(prefix);
   }

   @Override
   public void exitIfamt_filter(Ifamt_filterContext ctx) {
      FilterContext filter = ctx.filter();
      String name = filter.name.getText();
      DirectionContext direction = ctx.filter().direction();
      if (direction.INPUT() != null) {
         _currentInterface.setIncomingFilter(name);
      }
      else if (direction.OUTPUT() != null) {
         _currentInterface.setOutgoingFilter(name);
      }
   }

   @Override
   public void exitIt_disable(It_disableContext ctx) {
      _currentInterface.setActive(false);
   }

   @Override
   public void exitIt_enable(It_enableContext ctx) {
      _currentInterface.setActive(true);
   }

   @Override
   public void exitIt_unit(It_unitContext ctx) {
      _currentInterface = _currentMasterInterface;
   }

   @Override
   public void exitOt_area(Ot_areaContext ctx) {
      _currentArea = null;
   }

   @Override
   public void exitOt_export(Ot_exportContext ctx) {
      String name = ctx.name.getText();
      _currentRoutingInstance.getOspfExportPolicies().add(name);
   }

   @Override
   public void exitPlt_network(Plt_networkContext ctx) {
      Prefix prefix = new Prefix(ctx.network.getText());
      _currentPrefixList.getPrefixes().add(prefix);
   }

   @Override
   public void exitPot_community(Pot_communityContext ctx) {
      _currentCommunityList = null;
   }

   @Override
   public void exitPot_policy_statement(Pot_policy_statementContext ctx) {
      _currentPolicyStatement = null;
   }

   public void exitPot_prefix_list(Pot_prefix_listContext ctx) {
      _currentPrefixList = null;
   }

   @Override
   public void exitPst_term_tail(Pst_term_tailContext ctx) {
      _currentPsTerm = null;
      _currentPsThens = null;
   }

   @Override
   public void exitRft_exact(Rft_exactContext ctx) {
      if (_currentRouteFilterPrefix != null) {
         RouteFilterLineExact fromRouteFilterExact = new RouteFilterLineExact(
               _currentRouteFilterPrefix);
         _currentRouteFilterLine = _currentRouteFilter
               .insertLine(fromRouteFilterExact);
      }
   }

   @Override
   public void exitRft_orlonger(Rft_orlongerContext ctx) {
      if (_currentRouteFilterPrefix != null) {
         RouteFilterLineOrLonger fromRouteFilterOrLonger = new RouteFilterLineOrLonger(
               _currentRouteFilterPrefix);
         _currentRouteFilterLine = _currentRouteFilter
               .insertLine(fromRouteFilterOrLonger);
      }
   }

   @Override
   public void exitRft_prefix_length_range(Rft_prefix_length_rangeContext ctx) {
      int minPrefixLength = toInt(ctx.low);
      int maxPrefixLength = toInt(ctx.high);
      if (_currentRouteFilterPrefix != null) {
         RouteFilterLineLengthRange fromRouteFilterLengthRange = new RouteFilterLineLengthRange(
               _currentRouteFilterPrefix, minPrefixLength, maxPrefixLength);
         _currentRouteFilterLine = _currentRouteFilter
               .insertLine(fromRouteFilterLengthRange);
      }
   }

   @Override
   public void exitRft_through(Rft_throughContext ctx) {
      if (_currentRouteFilterPrefix != null) {
         Prefix throughPrefix = new Prefix(ctx.IP_PREFIX().getText());
         RouteFilterLineThrough fromRouteFilterThrough = new RouteFilterLineThrough(
               _currentRouteFilterPrefix, throughPrefix);
         _currentRouteFilterLine = _currentRouteFilter
               .insertLine(fromRouteFilterThrough);
      }
   }

   @Override
   public void exitRft_upto(Rft_uptoContext ctx) {
      int maxPrefixLength = toInt(ctx.high);
      if (_currentRouteFilterPrefix != null) {
         RouteFilterLineUpTo fromRouteFilterUpTo = new RouteFilterLineUpTo(
               _currentRouteFilterPrefix, maxPrefixLength);
         _currentRouteFilterLine = _currentRouteFilter
               .insertLine(fromRouteFilterUpTo);
      }
   }

   @Override
   public void exitRibt_aggregate(Ribt_aggregateContext ctx) {
      _currentAggregateRoute = null;
   }

   @Override
   public void exitRot_autonomous_system(Rot_autonomous_systemContext ctx) {
      int as = toInt(ctx.as);
      _currentRoutingInstance.setAs(as);
   }

   @Override
   public void exitRot_generate(Rot_generateContext ctx) {
      _currentGeneratedRoute = null;
   }

   @Override
   public void exitRot_router_id(Rot_router_idContext ctx) {
      Ip id = new Ip(ctx.id.getText());
      _currentRoutingInstance.setRouterId(id);
   }

   @Override
   public void exitRot_static(Rot_staticContext ctx) {
      _currentStaticRoute = null;
   }

   @Override
   public void exitS_firewall(S_firewallContext ctx) {
      _currentFirewallFamily = null;
   }

   @Override
   public void exitS_interfaces(S_interfacesContext ctx) {
      _currentInterface = null;
      _currentMasterInterface = null;
   }

   @Override
   public void exitS_protocols_bgp(S_protocols_bgpContext ctx) {
      _currentBgpGroup = null;
   }

   @Override
   public void exitS_routing_options(S_routing_optionsContext ctx) {
      _currentRib = null;
   }

   @Override
   public void exitSrt_discard(Srt_discardContext ctx) {
      _currentStaticRoute.setDrop(true);
   }

   @Override
   public void exitSrt_reject(Srt_rejectContext ctx) {
      _currentStaticRoute.setDrop(true);
   }

   @Override
   public void exitSrt_tag(Srt_tagContext ctx) {
      int tag = toInt(ctx.tag);
      _currentStaticRoute.setTag(tag);
   }

   @Override
   public void exitSt_host_name(St_host_nameContext ctx) {
      String hostname = ctx.variable().getText();
      _currentRoutingInstance.setHostname(hostname);
   }

   @Override
   public void exitTht_accept(Tht_acceptContext ctx) {
      _currentPsThens.add(PsThenAccept.INSTANCE);
   }

   @Override
   public void exitTht_community_add(Tht_community_addContext ctx) {
      String name = ctx.name.getText();
      PsThenCommunityAdd then = new PsThenCommunityAdd(name);
      _currentPsThens.add(then);
   }

   @Override
   public void exitTht_community_delete(Tht_community_deleteContext ctx) {
      String name = ctx.name.getText();
      PsThenCommunityDelete then = new PsThenCommunityDelete(name);
      _currentPsThens.add(then);
   }

   @Override
   public void exitTht_community_set(Tht_community_setContext ctx) {
      String name = ctx.name.getText();
      PsThenCommunitySet then = new PsThenCommunitySet(name);
      _currentPsThens.add(then);
   }

   @Override
   public void exitTht_local_preference(Tht_local_preferenceContext ctx) {
      int localPreference = toInt(ctx.localpref);
      PsThenLocalPreference then = new PsThenLocalPreference(localPreference);
      _currentPsThens.add(then);
   }

   @Override
   public void exitTht_metric(Tht_metricContext ctx) {
      int metric = toInt(ctx.metric);
      PsThenMetric then = new PsThenMetric(metric);
      _currentPsThens.add(then);
   }

   @Override
   public void exitTht_next_hop(Tht_next_hopContext ctx) {
      PsThen then;
      if (ctx.IP_ADDRESS() != null) {
         Ip nextHopIp = new Ip(ctx.IP_ADDRESS().getText());
         then = new PsThenNextHopIp(nextHopIp);
      }
      else {
         todo(ctx, "not implemented");
         return;
      }
      _currentPsThens.add(then);
   }

   @Override
   public void exitTht_reject(Tht_rejectContext ctx) {
      _currentPsThens.add(PsThenReject.INSTANCE);
   }

   public JuniperVendorConfiguration getConfiguration() {
      return _configuration;
   }

   private AsPath toAsPath(As_path_exprContext path) {
      AsPath asPath = new AsPath();
      for (As_unitContext ctx : path.items) {
         AsSet asSet = new AsSet();
         if (ctx.DEC() != null) {
            asSet.add(toInt(ctx.DEC()));
         }
         else {
            for (Token token : ctx.as_set().items) {
               asSet.add(toInt(token));
            }
         }
         asPath.add(asSet);
      }
      return asPath;
   }

   private void todo(ParserRuleContext ctx, String reason) {
      String ruleName = _parser.getParser().getRuleNames()[ctx.getRuleIndex()];
      if (_rulesWithSuppressedWarnings.contains(ruleName)) {
         return;
      }
      String prefix = "WARNING " + (_warnings.size() + 1) + ": ";
      StringBuilder sb = new StringBuilder();
      List<String> ruleNames = Arrays.asList(FlatJuniperParser.ruleNames);
      String ruleStack = ctx.toString(ruleNames);
      sb.append(prefix
            + "Missing implementation for top (leftmost) parser rule in stack: '"
            + ruleStack + "'.\n");
      sb.append(prefix + "Reason: " + reason + "\n");
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
      sb.append(prefix + "Parse tree follows:\n");
      String parseTreePrefix = prefix + "PARSE TREE: ";
      String parseTreeText = ParseTreePrettyPrinter.print(ctx, _parser);
      String[] parseTreeLines = parseTreeText.split("\n");
      for (String parseTreeLine : parseTreeLines) {
         sb.append(parseTreePrefix + parseTreeLine + "\n");
      }
      _warnings.add(sb.toString());
   }

}
