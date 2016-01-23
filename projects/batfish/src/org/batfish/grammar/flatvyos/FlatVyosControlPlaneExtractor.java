package org.batfish.grammar.flatvyos;

import java.util.Set;
import java.util.TreeSet;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.BatfishException;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.flatvyos.FlatVyosParser.*;
import org.batfish.main.Warnings;
import org.batfish.representation.Ip;
import org.batfish.representation.LineAction;
import org.batfish.representation.Prefix;
import org.batfish.representation.VendorConfiguration;
import org.batfish.representation.vyos.BgpNeighbor;
import org.batfish.representation.vyos.BgpProcess;
import org.batfish.representation.vyos.Interface;
import org.batfish.representation.vyos.InterfaceType;
import org.batfish.representation.vyos.PrefixList;
import org.batfish.representation.vyos.PrefixListRule;
import org.batfish.representation.vyos.RouteMap;
import org.batfish.representation.vyos.RouteMapMatch;
import org.batfish.representation.vyos.RouteMapRule;
import org.batfish.representation.vyos.StaticNextHopRoute;
import org.batfish.representation.vyos.VyosConfiguration;
import org.batfish.representation.vyos.VyosVendorConfiguration;

public class FlatVyosControlPlaneExtractor extends FlatVyosParserBaseListener
      implements ControlPlaneExtractor {

   private static final String F_INTERFACES_ADDRESS_DHCP = "interfaces address dhcp";

   private static LineAction toAction(Line_actionContext ctx) {
      if (ctx.DENY() != null) {
         return LineAction.REJECT;
      }
      else if (ctx.PERMIT() != null) {
         return LineAction.ACCEPT;
      }
      else {
         throw new BatfishException("invalid line_action: " + ctx.getText());
      }
   }

   private static int toInteger(Token t) {
      String text = t.getText();
      try {
         int result = Integer.parseInt(text);
         return result;
      }
      catch (NumberFormatException e) {
         throw new BatfishException("Not an integer: \"" + text + "\"", e);
      }
   }

   private static InterfaceType toInterfaceType(Interface_typeContext ctx) {
      if (ctx.BONDING() != null) {
         return InterfaceType.BONDING;
      }
      else if (ctx.BRIDGE() != null) {
         return InterfaceType.BRIDGE;
      }
      else if (ctx.DUMMY() != null) {
         return InterfaceType.DUMMY;
      }
      else if (ctx.ETHERNET() != null) {
         return InterfaceType.ETHERNET;
      }
      else if (ctx.INPUT() != null) {
         return InterfaceType.INPUT;
      }
      else if (ctx.L2TPV3() != null) {
         return InterfaceType.L2TPV3;
      }
      else if (ctx.LOOPBACK() != null) {
         return InterfaceType.LOOPBACK;
      }
      else if (ctx.OPENVPN() != null) {
         return InterfaceType.OPENVPN;
      }
      else if (ctx.PSEUDO_ETHERNET() != null) {
         return InterfaceType.PSEUDO_ETHERNET;
      }
      else if (ctx.TUNNEL() != null) {
         return InterfaceType.TUNNEL;
      }
      else if (ctx.VTI() != null) {
         return InterfaceType.VTI;
      }
      else if (ctx.VXLAN() != null) {
         return InterfaceType.VXLAN;
      }
      else if (ctx.WIRELESS() != null) {
         return InterfaceType.WIRELESS;
      }
      else if (ctx.WIRELESSMODEM() != null) {
         return InterfaceType.WIRELESSMODEM;
      }
      else {
         throw new BatfishException("Unsupported interface type: "
               + ctx.getText());
      }
   }

   private BgpProcess _bgpProcess;

   private VyosConfiguration _configuration;

   private BgpNeighbor _currentBgpNeighbor;

   private Interface _currentInterface;

   private PrefixList _currentPrefixList;

   private PrefixListRule _currentPrefixListRule;

   private RouteMap _currentRouteMap;

   private RouteMapRule _currentRouteMapRule;

   private Prefix _currentStaticRoutePrefix;

   private final FlatVyosCombinedParser _parser;

   private final String _text;

   private final Set<String> _unimplementedFeatures;

   private VyosVendorConfiguration _vendorConfiguration;

   private final Warnings _w;

   public FlatVyosControlPlaneExtractor(String text,
         FlatVyosCombinedParser parser, Warnings warnings) {
      _text = text;
      _parser = parser;
      _unimplementedFeatures = new TreeSet<String>();
      _w = warnings;
   }

   @Override
   public void enterBt_neighbor(Bt_neighborContext ctx) {
      Ip neighborIp = new Ip(ctx.IP_ADDRESS().getText());
      _currentBgpNeighbor = _bgpProcess.getNeighbors().get(neighborIp);
      if (_currentBgpNeighbor == null) {
         _currentBgpNeighbor = new BgpNeighbor(neighborIp);
         _bgpProcess.getNeighbors().put(neighborIp, _currentBgpNeighbor);
      }
   }

   @Override
   public void enterFlat_vyos_configuration(Flat_vyos_configurationContext ctx) {
      _vendorConfiguration = new VyosVendorConfiguration();
      _configuration = _vendorConfiguration;
   }

   @Override
   public void enterPlt_rule(Plt_ruleContext ctx) {
      int num = toInteger(ctx.num);
      _currentPrefixListRule = _currentPrefixList.getRules().get(num);
      if (_currentPrefixListRule == null) {
         _currentPrefixListRule = new PrefixListRule(num);
         _currentPrefixList.getRules().put(num, _currentPrefixListRule);
      }
   }

   @Override
   public void enterPt_prefix_list(Pt_prefix_listContext ctx) {
      String name = ctx.name.getText();
      _currentPrefixList = _configuration.getPrefixLists().get(name);
      if (_currentPrefixList == null) {
         _currentPrefixList = new PrefixList(name);
         _configuration.getPrefixLists().put(name, _currentPrefixList);
      }
   }

   @Override
   public void enterPt_route_map(Pt_route_mapContext ctx) {
      String name = ctx.name.getText();
      _currentRouteMap = _configuration.getRouteMaps().get(name);
      if (_currentRouteMap == null) {
         _currentRouteMap = new RouteMap(name);
         _configuration.getRouteMaps().put(name, _currentRouteMap);
      }
   }

   @Override
   public void enterRmt_rule(Rmt_ruleContext ctx) {
      int num = toInteger(ctx.num);
      _currentRouteMapRule = _currentRouteMap.getRules().get(num);
      if (_currentRouteMapRule == null) {
         _currentRouteMapRule = new RouteMapRule(num);
         _currentRouteMap.getRules().put(num, _currentRouteMapRule);
      }
   }

   @Override
   public void enterS_interfaces(S_interfacesContext ctx) {
      String name = ctx.name.getText();
      _currentInterface = _configuration.getInterfaces().get(name);
      if (_currentInterface == null) {
         _currentInterface = new Interface(name);
         _configuration.getInterfaces().put(name, _currentInterface);
         InterfaceType type = toInterfaceType(ctx.interface_type());
         double bandwidth = Interface.getDefaultBandwidth(type);
         _currentInterface.setBandwidth(bandwidth);
      }
   }

   @Override
   public void enterS_protocols_bgp(S_protocols_bgpContext ctx) {
      int as = toInteger(ctx.asnum);
      if (_bgpProcess == null) {
         _bgpProcess = new BgpProcess(as);
         _configuration.setBgpProcess(_bgpProcess);
      }
      else {
         if (_bgpProcess.getLocalAs() != as) {
            throw new BatfishException(
                  "Do not support multiple BGP processes at this time");
         }
      }
   }

   @Override
   public void enterStatict_route(Statict_routeContext ctx) {
      Prefix prefix = new Prefix(ctx.IP_PREFIX().getText());
      _currentStaticRoutePrefix = prefix;
   }

   @Override
   public void exitBnt_nexthop_self(Bnt_nexthop_selfContext ctx) {
      _currentBgpNeighbor.setNextHopSelf(true);
   }

   @Override
   public void exitBnt_remote_as(Bnt_remote_asContext ctx) {
      int remoteAs = toInteger(ctx.asnum);
      _currentBgpNeighbor.setRemoteAs(remoteAs);
   }

   @Override
   public void exitBnt_route_map_export(Bnt_route_map_exportContext ctx) {
      String name = ctx.name.getText();
      _currentBgpNeighbor.setExportRouteMap(name);
   }

   @Override
   public void exitBnt_route_map_import(Bnt_route_map_importContext ctx) {
      String name = ctx.name.getText();
      _currentBgpNeighbor.setImportRouteMap(name);
   }

   @Override
   public void exitBt_neighbor(Bt_neighborContext ctx) {
      _currentBgpNeighbor = null;
   }

   @Override
   public void exitIt_address(It_addressContext ctx) {
      if (ctx.DHCP() != null) {
         todo(ctx, F_INTERFACES_ADDRESS_DHCP);
      }
      else if (ctx.IP_PREFIX() != null) {
         Prefix prefix = new Prefix(ctx.IP_PREFIX().getText());
         _currentInterface.setPrefix(prefix);
      }
   }

   @Override
   public void exitIt_description(It_descriptionContext ctx) {
      String description = ctx.description().DESCRIPTION_TEXT().getText();
      _currentInterface.setDescription(description);
   }

   @Override
   public void exitPlrt_action(Plrt_actionContext ctx) {
      LineAction action = toAction(ctx.action);
      _currentPrefixListRule.setAction(action);
   }

   @Override
   public void exitPlrt_description(Plrt_descriptionContext ctx) {
      String description = ctx.description().DESCRIPTION_TEXT().getText();
      _currentPrefixListRule.setDescription(description);
   }

   @Override
   public void exitPlrt_ge(Plrt_geContext ctx) {
      int ge = toInteger(ctx.num);
      _currentPrefixListRule.setGe(ge);
   }

   @Override
   public void exitPlrt_le(Plrt_leContext ctx) {
      int le = toInteger(ctx.num);
      _currentPrefixListRule.setLe(le);
   }

   @Override
   public void exitPlrt_prefix(Plrt_prefixContext ctx) {
      Prefix prefix = new Prefix(ctx.prefix.getText());
      _currentPrefixListRule.setPrefix(prefix);
   }

   @Override
   public void exitPlt_description(Plt_descriptionContext ctx) {
      String description = ctx.description().DESCRIPTION_TEXT().getText();
      _currentPrefixList.setDescription(description);
   }

   @Override
   public void exitPlt_rule(Plt_ruleContext ctx) {
      _currentPrefixListRule = null;
   }

   @Override
   public void exitPt_prefix_list(Pt_prefix_listContext ctx) {
      _currentPrefixList = null;
   }

   @Override
   public void exitPt_route_map(Pt_route_mapContext ctx) {
      _currentRouteMap = null;
   }

   @Override
   public void exitRmmt_ip_address_prefix_list(
         Rmmt_ip_address_prefix_listContext ctx) {
      String name = ctx.name.getText();
      RouteMapMatch match = new RouteMapMatchPrefixList(name);
      _currentRouteMapRule.getMatches().add(match);
   }

   @Override
   public void exitRmrt_action(Rmrt_actionContext ctx) {
      LineAction action = toAction(ctx.action);
      _currentRouteMapRule.setAction(action);
   }

   @Override
   public void exitRmrt_description(Rmrt_descriptionContext ctx) {
      String description = ctx.description().DESCRIPTION_TEXT().getText();
      _currentRouteMapRule.setDescription(description);
   }

   @Override
   public void exitRmt_rule(Rmt_ruleContext ctx) {
      _currentRouteMapRule = null;
   }

   @Override
   public void exitS_interfaces(S_interfacesContext ctx) {
      _currentInterface = null;
   }

   @Override
   public void exitSrt_next_hop(Srt_next_hopContext ctx) {
      Ip nextHopIp = new Ip(ctx.nexthop.getText());
      int distance = toInteger(ctx.distance);
      StaticNextHopRoute staticRoute = new StaticNextHopRoute(
            _currentStaticRoutePrefix, nextHopIp, distance);
      _configuration.getStaticNextHopRoutes().add(staticRoute);
   }

   @Override
   public void exitSt_host_name(St_host_nameContext ctx) {
      String hostname = ctx.name.getText();
      _configuration.setHostname(hostname);
   }

   @Override
   public void exitStatict_route(Statict_routeContext ctx) {
      _currentStaticRoutePrefix = null;
   }

   @Override
   public Set<String> getUnimplementedFeatures() {
      return _unimplementedFeatures;
   }

   @Override
   public VendorConfiguration getVendorConfiguration() {
      return _vendorConfiguration;
   }

   @Override
   public void processParseTree(ParserRuleContext tree) {
      ParseTreeWalker walker = new ParseTreeWalker();
      walker.walk(this, tree);
   }

   private void todo(ParserRuleContext ctx, String feature) {
      _w.todo(ctx, feature, _parser, _text);
      _unimplementedFeatures.add("Vyos: " + feature);
   }

}
