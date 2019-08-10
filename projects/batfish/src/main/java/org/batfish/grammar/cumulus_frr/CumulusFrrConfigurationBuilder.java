package org.batfish.grammar.cumulus_frr;

import static com.google.common.base.Preconditions.checkState;
import static java.lang.Long.parseLong;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.Rm_descriptionContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.S_bgpContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.S_routemapContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.S_vrfContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.Sb_neighborContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.Sb_router_idContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.Sbn_interfaceContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.Sbn_peer_groupContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.Sv_routeContext;
import org.batfish.grammar.cumulus_frr.CumulusFrrParser.Sv_vniContext;
import org.batfish.representation.cumulus.BgpInterfaceNeighbor;
import org.batfish.representation.cumulus.BgpNeighbor;
import org.batfish.representation.cumulus.BgpPeerGroupNeighbor;
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
  private final CumulusNcluConfiguration _c;
  private final CumulusFrrCombinedParser _parser;
  private final Warnings _w;

  private @Nullable Vrf _currentVrf;
  private @Nullable RouteMapEntry _currentRouteMapEntry;
  private @Nullable BgpVrf _currentBgpVrf;
  private @Nullable BgpNeighbor _currentBgpNeighbor;

  public CumulusFrrConfigurationBuilder(
      CumulusNcluConfiguration configuration, CumulusFrrCombinedParser parser, Warnings w) {
    _c = configuration;
    _parser = parser;
    _w = w;
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
  public void enterSb_neighbor(Sb_neighborContext ctx) {
    // if neighbor does not already exist, get will return null. That's ok -- the listener for
    // child parse node will create it.
    _currentBgpNeighbor = _currentBgpVrf.getNeighbors().get(ctx.name.getText());
  }

  @Override
  public void exitSb_neighbor(Sb_neighborContext ctx) {
    _currentBgpNeighbor = null;
  }

  @Override
  public void enterSbn_interface(Sbn_interfaceContext ctx) {
    if (_currentBgpNeighbor != null) {
      // warn if it's not an interface
      if (!(_currentBgpNeighbor instanceof BgpInterfaceNeighbor)) {
        String line = ctx.getParent().getText() + ctx.getText();
        _w.addWarning(
            ctx,
            line,
            _parser,
            String.format(
                "neighbor %s not declared to be an interface", _currentBgpNeighbor.getName()));
      }
    } else {
      Sb_neighborContext parentCtx = (Sb_neighborContext) ctx.getParent();
      String ifaceName = parentCtx.name.getText();
      _currentBgpNeighbor = new BgpInterfaceNeighbor(ifaceName);
      checkState(
          _currentBgpVrf.getNeighbors().put(ifaceName, _currentBgpNeighbor) == null,
          "neighbor should not already exist since _currentBgpNeighbor was null");
    }
  }

  @Override
  public void exitSbn_peer_group(Sbn_peer_groupContext ctx) {
    if (_currentBgpNeighbor != null) {
      String line = ctx.getParent().getText() + ctx.getText();
      _w.addWarning(
          ctx,
          line,
          _parser,
          String.format("neighbor %s already defined", _currentBgpNeighbor.getName()));
    }

    Sb_neighborContext parentCtx = (Sb_neighborContext) ctx.getParent();
    String peerGroupName = parentCtx.name.getText();
    checkState(
        _currentBgpVrf.getNeighbors().put(peerGroupName, new BgpPeerGroupNeighbor(peerGroupName))
            == null,
        "neighbor should not already exist since _currentBgpNeighbor was null");
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
