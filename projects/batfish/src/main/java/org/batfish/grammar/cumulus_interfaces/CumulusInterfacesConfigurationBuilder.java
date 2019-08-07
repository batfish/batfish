package org.batfish.grammar.cumulus_interfaces;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import java.util.List;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.Cumulus_interfaces_configurationContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_addressContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_bond_slavesContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_bridge_accessContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_bridge_portsContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_bridge_vidsContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_clag_idContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_link_speedContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_vlan_idContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_vrfContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_vrf_tableContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.Interface_nameContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.S_autoContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.S_ifaceContext;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.CumulusStructureType;
import org.batfish.representation.cumulus.CumulusStructureUsage;
import org.batfish.representation.cumulus_interfaces.Interface;
import org.batfish.representation.cumulus_interfaces.Interfaces;

/**
 * Populates an {@link Interfaces} from a parse tree from {@link
 * org.batfish.grammar.cumulus_interfaces.CumulusInterfacesCombinedParser}.
 */
public final class CumulusInterfacesConfigurationBuilder
    extends CumulusInterfacesParserBaseListener {
  private final CumulusNcluConfiguration _config;
  private final Interfaces _interfaces = new Interfaces();
  private final Warnings _w;
  private Interface _currentIface;

  public CumulusInterfacesConfigurationBuilder(CumulusNcluConfiguration config, Warnings w) {
    _config = config;
    _w = w;
  }

  @VisibleForTesting
  Interfaces getInterfaces() {
    return _interfaces;
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    int line = token.getLine();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    _config.setUnrecognized(true);

    if (token instanceof UnrecognizedLineToken) {
      UnrecognizedLineToken unrecToken = (UnrecognizedLineToken) token;
      _w.getParseWarnings()
          .add(
              new ParseWarning(
                  line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
    } else {
      String msg = String.format("Unrecognized Line: %d: %s", line, lineText);
      _w.redFlag(msg + " SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY");
    }
  }

  // Listener methods
  @Override
  public void exitI_address(I_addressContext ctx) {
    _currentIface.addAddress(ConcreteInterfaceAddress.parse(ctx.IP_PREFIX().getText()));
  }

  @Override
  public void exitI_vrf_table(I_vrf_tableContext ctx) {
    String tblName = ctx.vrf_table_name().getText();
    if (tblName.equals("auto")) {
      _currentIface.setIsVrf();
    } else {
      _w.unimplemented("Only `vrf-table auto` is supported");
    }
  }

  @Override
  public void exitI_bond_slaves(I_bond_slavesContext ctx) {
    List<Interface_nameContext> interfaceNameCtxs = ctx.interface_name();
    interfaceNameCtxs.forEach(
        ifaceNameCtx ->
            _config.referenceStructure(
                CumulusStructureType.INTERFACE,
                ifaceNameCtx.getText(),
                CumulusStructureUsage.BOND_SLAVE,
                ifaceNameCtx.getStart().getLine()));
    _currentIface.setBondSlaves(
        interfaceNameCtxs.stream()
            .map(RuleContext::getText)
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public void exitI_bridge_access(I_bridge_accessContext ctx) {
    _currentIface.setBridgeAccess(Integer.parseInt(ctx.NUMBER().getText()));
  }

  @Override
  public void exitI_bridge_ports(I_bridge_portsContext ctx) {
    List<Interface_nameContext> interfaceNameCtxs = ctx.interface_name();
    interfaceNameCtxs.forEach(
        ifaceNameCtx ->
            _config.referenceStructure(
                CumulusStructureType.INTERFACE,
                ifaceNameCtx.getText(),
                CumulusStructureUsage.BRIDGE_PORT,
                ifaceNameCtx.getStart().getLine()));
    _currentIface.setBridgePorts(
        interfaceNameCtxs.stream()
            .map(RuleContext::getText)
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public void exitI_bridge_vids(I_bridge_vidsContext ctx) {
    List<TerminalNode> vidCtxs = ctx.NUMBER();
    vidCtxs.forEach(
        vidCtx ->
            _config.referenceStructure(
                CumulusStructureType.VLAN,
                vidCtx.getText(),
                CumulusStructureUsage.BRIDGE_VID,
                ctx.getStart().getLine()));
    _currentIface.setBridgeVids(
        IntegerSpace.unionOf(
            vidCtxs.stream()
                .map(ParseTree::getText)
                .map(Integer::parseInt)
                .map(Range::singleton)
                .collect(ImmutableList.toImmutableList())));
  }

  @Override
  public void exitI_clag_id(I_clag_idContext ctx) {
    _currentIface.setClagId(Integer.parseInt(ctx.NUMBER().getText()));
  }

  @Override
  public void exitI_link_speed(I_link_speedContext ctx) {
    _currentIface.setLinkSpeed(Integer.parseInt(ctx.NUMBER().getText()));
  }

  @Override
  public void exitI_vlan_id(I_vlan_idContext ctx) {
    String vlanId = ctx.NUMBER().getText();
    _config.defineStructure(CumulusStructureType.VLAN, vlanId, ctx.getStart().getLine());
    _currentIface.setVlanId(Integer.parseInt(vlanId));
  }

  @Override
  public void exitI_vrf(I_vrfContext ctx) {
    String vrf = ctx.vrf_name().getText();
    _currentIface.setVrf(vrf);
    _config.referenceStructure(
        CumulusStructureType.VRF,
        vrf,
        CumulusStructureUsage.INTERFACE_VRF,
        ctx.vrf_name().getStart().getLine());
  }

  @Override
  public void exitS_auto(S_autoContext ctx) {
    String name = ctx.interface_name().getText();
    _interfaces.setAuto(name);
  }

  @Override
  public void enterS_iface(S_ifaceContext ctx) {
    String name = ctx.interface_name().getText();
    _currentIface = _interfaces.createOrGetInterface(name);
    _config.defineStructure(CumulusStructureType.INTERFACE, name, ctx.getStart().getLine());
  }

  @Override
  public void exitS_iface(S_ifaceContext ctx) {
    _currentIface = null;
  }

  @Override
  public void exitCumulus_interfaces_configuration(Cumulus_interfaces_configurationContext ctxt) {
    // TODO migrate _interfaces into _config
  }
}
