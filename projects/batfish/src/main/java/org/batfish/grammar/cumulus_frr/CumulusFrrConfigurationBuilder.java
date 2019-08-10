package org.batfish.grammar.cumulus_frr;

import static java.lang.Long.parseLong;

import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.Rm_descriptionContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.S_bgpContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.S_routemapContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.S_vrfContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.Sb_router_idContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.Sv_routeContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.Sv_vniContext;
import org.batfish.representation.cumulus.BgpProcess;
import org.batfish.representation.cumulus.BgpVrf;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.CumulusStructureType;
import org.batfish.representation.cumulus.CumulusStructureUsage;
import org.batfish.representation.cumulus.RouteMap;
import org.batfish.representation.cumulus.RouteMapEntry;
import org.batfish.representation.cumulus.StaticRoute;
import org.batfish.representation.cumulus.Vrf;

public class CumulusFrrConfigurationBuilder extends CumulusFrrParserBaseListener {
  private CumulusNcluConfiguration _c;
  private @Nullable Vrf _currentVrf;
  private RouteMapEntry _currentRouteMapEntry;
  private @Nullable BgpVrf _currentBgpVrf;

  public CumulusFrrConfigurationBuilder(CumulusNcluConfiguration configuration) {
    _c = configuration;
  }

  CumulusNcluConfiguration getVendorConfiguration() {
    return _c;
  }

  @Override
  public void enterS_bgp(S_bgpContext ctx) {
    if (_c.getBgpProcess() == null) {
      _c.setBgpProcess(new BgpProcess());
    }

    if (ctx.vrfName == null) {
      _currentBgpVrf = _c.getBgpProcess().getDefaultVrf();
    } else {
      String vrfName = ctx.vrfName.getText();
      _currentBgpVrf = new BgpVrf(vrfName);
      _c.getBgpProcess().getVrfs().put(vrfName, _currentBgpVrf);
      _c.referenceStructure(
          CumulusStructureType.VRF,
          vrfName,
          CumulusStructureUsage.BGP_VRF,
          ctx.vrfName.getStart().getLine());
    }
    _currentBgpVrf.setAutonomousSystem(parseLong(ctx.autonomousSystem.getText()));
  }

  @Override
  public void exitS_bgp(S_bgpContext ctx) {
    _currentBgpVrf = null;
  }

  @Override
  public void exitSb_router_id(Sb_router_idContext ctx) {
    _currentBgpVrf.setRouterId(Ip.parse(ctx.IP_ADDRESS().getText()));
  }

  @Override
  public void enterS_vrf(S_vrfContext ctx) {
    String name = ctx.name.getText();
    _currentVrf = new Vrf(name);
    _c.getVrfs().put(name, _currentVrf);
    _c.defineStructure(CumulusStructureType.VRF, name, ctx.getStart().getLine());
  }

  @Override
  public void exitS_vrf(S_vrfContext ctx) {
    _currentVrf = null;
  }

  @Override
  public void exitSv_vni(Sv_vniContext ctx) {
    int vni = Integer.parseInt(ctx.vni.v.getText());
    _currentVrf.setVni(vni);
  }

  @Override
  public void exitSv_route(Sv_routeContext ctx) {
    Ip nextHop = Ip.parse(ctx.ip_address().getText());
    Prefix network = Prefix.parse(ctx.prefix().getText());
    _currentVrf.getStaticRoutes().add(new StaticRoute(network, nextHop, null));
  }

  @Override
  public void enterS_routemap(S_routemapContext ctx) {
    int sequence = Integer.parseInt(ctx.sequence.getText());
    String name = ctx.name.getText();
    LineAction action;
    if (ctx.action.permit != null) {
      action = LineAction.PERMIT;
    } else if (ctx.action.deny != null) {
      action = LineAction.DENY;
    } else {
      throw new IllegalStateException("only support permit and deny in route map");
    }
    _currentRouteMapEntry =
        _c.getRouteMaps()
            .computeIfAbsent(name, RouteMap::new)
            .getEntries()
            .computeIfAbsent(
                sequence, k -> new RouteMapEntry(Integer.parseInt(ctx.sequence.getText()), action));
    _c.defineStructure(CumulusStructureType.VRF, name, ctx.getStart().getLine());
    _c.defineStructure(CumulusStructureType.ROUTE_MAP, name, ctx.getStart().getLine());
  }

  @Override
  public void exitRm_description(Rm_descriptionContext ctx) {
    _currentRouteMapEntry.setDescription(ctx.route_map_description().getText());
    super.exitRm_description(ctx);
  }

  @Override
  public void exitS_routemap(S_routemapContext ctx) {
    _currentRouteMapEntry = null;
  }
}
