package batfish.grammar.flatjuniper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import batfish.grammar.ParseTreePrettyPrinter;
import batfish.grammar.flatjuniper.FlatJuniperParser.*;
import batfish.grammar.flatjuniper.FlatJuniperCombinedParser;
import batfish.representation.AsPath;
import batfish.representation.AsSet;
import batfish.representation.Ip;
import batfish.representation.Prefix;
import batfish.representation.juniper.AggregateRoute;
import batfish.representation.juniper.BgpGroup;
import batfish.representation.juniper.BgpGroup.BgpGroupType;
import batfish.representation.juniper.IpBgpGroup;
import batfish.representation.juniper.GeneratedRoute;
import batfish.representation.juniper.Interface;
import batfish.representation.juniper.JuniperVendorConfiguration;
import batfish.representation.juniper.NamedBgpGroup;
import batfish.representation.juniper.RoutingInformationBase;
import batfish.representation.juniper.RoutingInstance;
import batfish.representation.juniper.StaticRoute;

public class ConfigurationBuilder extends FlatJuniperParserBaseListener {

   private static final AggregateRoute DUMMY_AGGREGATE_ROUTE = new AggregateRoute(
         Prefix.ZERO);

   private static final BgpGroup DUMMY_BGP_GROUP = new BgpGroup();

   private static Integer toInt(TerminalNode node) {
      return toInt(node.getSymbol());
   }

   private static int toInt(Token token) {
      return Integer.parseInt(token.getText());
   }

   private JuniperVendorConfiguration _configuration;

   private AggregateRoute _currentAggregateRoute;

   private BgpGroup _currentBgpGroup;

   private GeneratedRoute _currentGeneratedRoute;

   private Interface _currentInterface;

   private Interface _currentMasterInterface;

   private RoutingInformationBase _currentRib;

   private RoutingInstance _currentRoutingInstance;

   private StaticRoute _currentStaticRoute;

   private FlatJuniperCombinedParser _parser;

   private Set<String> _rulesWithSuppressedWarnings;

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
   public void enterIt_unit(It_unitContext ctx) {
      String unit = ctx.num.getText();
      Map<String, Interface> units = _currentMasterInterface.getUnits();
      _currentInterface = units.get(unit);
      if (_currentInterface == null) {
         _currentInterface = new Interface(unit);
         units.put(unit, _currentInterface);
      }
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
         if (_currentStaticRoute == null) {
            _currentStaticRoute = new StaticRoute(prefix);
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
   }

   @Override
   public void enterS_interfaces(S_interfacesContext ctx) {
      String ifaceName = ctx.name.getText();
      Map<String, Interface> interfaces = _currentRoutingInstance
            .getInterfaces();
      _currentInterface = interfaces.get(ifaceName);
      if (_currentInterface == null) {
         _currentInterface = new Interface(ifaceName);
         interfaces.put(ifaceName, _currentInterface);
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
   public void exitBt_type(Bt_typeContext ctx) {
      if (ctx.INTERNAL() != null) {
         _currentBgpGroup.setType(BgpGroupType.INTERNAL);
      }
      else if (ctx.EXTERNAL() != null) {
         _currentBgpGroup.setType(BgpGroupType.EXTERNAL);
      }
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
   public void exitIt_unit(It_unitContext ctx) {
      _currentInterface = _currentMasterInterface;
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

   @SuppressWarnings("unused")
   private void todo(ParserRuleContext ctx) {
      todo(ctx, "Unknown");
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
