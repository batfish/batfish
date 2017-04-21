package org.batfish.grammar.routing_table.eos;

import java.util.SortedSet;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.collections.RoutesByVrf;
import org.batfish.grammar.RoutingTableExtractor;
import org.batfish.grammar.routing_table.eos.EosRoutingTableParser.*;

public class EosRoutingTableExtractor extends EosRoutingTableParserBaseListener
      implements RoutingTableExtractor {

   private static int toInteger(Token t) {
      return Integer.parseInt(t.getText());
   }

   private String _currentVrfName;

   private SortedSet<Route> _currentVrfRoutes;

   private final String _hostname;

   @SuppressWarnings("unused")
   private EosRoutingTableCombinedParser _parser;

   private RoutesByVrf _routesByVrf;

   private final String _text;

   @SuppressWarnings("unused")
   private final Warnings _w;

   public EosRoutingTableExtractor(String hostname, String text,
         EosRoutingTableCombinedParser parser, Warnings w) {
      _hostname = hostname;
      _text = text;
      _parser = parser;
      _w = w;
   }

   private BatfishException convError(Class<?> type, ParserRuleContext ctx) {
      String typeName = type.getSimpleName();
      String txt = getFullText(ctx);
      return new BatfishException(
            "Could not convert to " + typeName + ": " + txt);
   }

   @Override
   public void enterEos_routing_table(Eos_routing_tableContext ctx) {
      _routesByVrf = new RoutesByVrf();
   }

   @Override
   public void enterVrf_declaration(Vrf_declarationContext ctx) {
      _currentVrfName = ctx.identifier().getText();
      initVrf(_currentVrfName);
   }

   @Override
   public void exitRoute(RouteContext ctx) {
      if (_currentVrfName == null) {
         _currentVrfName = Configuration.DEFAULT_VRF_NAME;
         initVrf(_currentVrfName);
      }
      RoutingProtocol protocol = toProtocol(ctx.protocol());
      Prefix prefix = new Prefix(ctx.IP_PREFIX().getText());
      int admin;
      int cost;
      Ip nextHopIp;
      for (int i = 0; i < ctx.nexthopifaces.size(); i++) {
         String nextHopInterface = ctx.nexthopifaces.get(i).getText();
         // TODO: support EOS route tags
         int tag = Route.UNSET_ROUTE_TAG;
         // TODO: support EOS next hop
         String nextHop = Route.UNSET_NEXT_HOP;
         if (ctx.IS_DIRECTLY_CONNECTED() != null) {
            admin = 0;
            cost = 0;
            nextHopIp = Route.UNSET_ROUTE_NEXT_HOP_IP;
         }
         else {
            admin = toInteger(ctx.admin);
            cost = toInteger(ctx.cost);
            nextHopIp = new Ip(ctx.nexthops.get(i).getText());
         }
         Route route = new Route(_hostname, _currentVrfName, prefix, nextHopIp,
               nextHop, nextHopInterface, admin, cost, protocol, tag);
         _currentVrfRoutes.add(route);
      }
   }

   private String getFullText(ParserRuleContext ctx) {
      int start = ctx.getStart().getStartIndex();
      int end = ctx.getStop().getStopIndex();
      String text = _text.substring(start, end + 1);
      return text;
   }

   @Override
   public RoutesByVrf getRoutesByVrf() {
      return _routesByVrf;
   }

   private void initVrf(String vrfName) {
      _currentVrfRoutes = _routesByVrf.get(vrfName);
      if (_currentVrfRoutes == null) {
         _currentVrfRoutes = new TreeSet<>();
         _routesByVrf.put(vrfName, _currentVrfRoutes);
      }
   }

   @Override
   public void processParseTree(ParserRuleContext tree) {
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(this, tree);
   }

   private RoutingProtocol toProtocol(ProtocolContext ctx) {
      String protocolStr = ctx.getText();
      switch (protocolStr) {

      case "BE":
         return RoutingProtocol.BGP;

      case "BI":
         return RoutingProtocol.IBGP;

      case "C":
         return RoutingProtocol.CONNECTED;

      case "S":
         return RoutingProtocol.STATIC;

      case "O":
         return RoutingProtocol.OSPF;

      case "IA":
         return RoutingProtocol.OSPF_IA;

      case "OE1":
         return RoutingProtocol.OSPF_E1;

      case "OE2":
         return RoutingProtocol.OSPF_E2;

      case "IL1":
         return RoutingProtocol.ISIS_L1;

      case "IL2":
         return RoutingProtocol.ISIS_L2;

      case "AB":
      case "AO":
         return RoutingProtocol.AGGREGATE;

      default:
         throw convError(RoutingProtocol.class, ctx);
      }
   }

}
