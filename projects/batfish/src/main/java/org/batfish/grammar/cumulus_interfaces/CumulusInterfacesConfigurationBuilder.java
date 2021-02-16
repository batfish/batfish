package org.batfish.grammar.cumulus_interfaces;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MacAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.AddressContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_addressContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_address_virtualContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_aliasContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_bond_masterContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_bond_slavesContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_bridge_accessContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_bridge_portsContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_bridge_pvidContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_bridge_vidsContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_bridge_vlan_awareContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_clag_idContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_clagd_backup_ipContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_clagd_peer_ipContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_clagd_priorityContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_clagd_sys_macContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_clagd_vxlan_anycast_ipContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_link_speedContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_mtuContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_vlan_idContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_vlan_raw_deviceContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_vrfContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_vrf_tableContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_vxlan_idContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.I_vxlan_local_tunnel_ipContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.Interface_addressContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.Interface_nameContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.Ipuir_addContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.NumberContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.Number_or_rangeContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.PrefixContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.S_autoContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.S_ifaceContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.Si_inetContext;
import org.batfish.grammar.cumulus_interfaces.CumulusInterfacesParser.Si_no_inetContext;
import org.batfish.representation.cumulus.CumulusConcatenatedConfiguration;
import org.batfish.representation.cumulus.CumulusInterfacesConfiguration;
import org.batfish.representation.cumulus.CumulusStructureType;
import org.batfish.representation.cumulus.CumulusStructureUsage;
import org.batfish.representation.cumulus.InterfaceClagSettings;
import org.batfish.representation.cumulus.InterfacesInterface;
import org.batfish.representation.cumulus.StaticRoute;

/**
 * Populates {@link CumulusInterfacesConfiguration} from a parse tree from {@link
 * org.batfish.grammar.cumulus_interfaces.CumulusInterfacesCombinedParser}.
 */
public final class CumulusInterfacesConfigurationBuilder
    extends CumulusInterfacesParserBaseListener {
  private final CumulusConcatenatedConfiguration _config;

  private final CumulusInterfacesCombinedParser _parser;
  private final String _text;
  private final Warnings _w;
  private InterfacesInterface _currentIface;
  private @Nullable String _currentIfaceName;

  public CumulusInterfacesConfigurationBuilder(
      CumulusConcatenatedConfiguration config,
      CumulusInterfacesCombinedParser parser,
      String text,
      Warnings w) {
    _config = config;
    _parser = parser;
    _text = text;
    _w = w;
  }

  private String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    return _text.substring(start, end + 1);
  }

  @VisibleForTesting
  CumulusConcatenatedConfiguration getConfig() {
    return _config;
  }

  private static @Nonnull ConcreteInterfaceAddress toConcreteInterfaceAddress(
      Interface_addressContext ctx) {
    if (ctx.addr_32 != null) {
      return ConcreteInterfaceAddress.create(toIp(ctx.addr_32), Prefix.MAX_PREFIX_LENGTH);
    } else {
      assert ctx.addr_mask != null;
      return ConcreteInterfaceAddress.parse(ctx.addr_mask.getText());
    }
  }

  private static @Nonnull Ip toIp(AddressContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private static @Nonnull Prefix toPrefix(PrefixContext ctx) {
    return Prefix.parse(ctx.getText());
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
  public void enterS_iface(S_ifaceContext ctx) {
    _currentIfaceName = ctx.interface_name().getText();
  }

  @Override
  public void enterSi_inet(Si_inetContext ctx) {
    checkArgument(_currentIfaceName != null, "not find interface name");
    if (ctx.LOOPBACK() != null) {
      if (!_currentIfaceName.equals(CumulusConcatenatedConfiguration.LOOPBACK_INTERFACE_NAME)) {
        _w.addWarning(
            ctx, ctx.getStart().getText(), _parser, "expected loopback device to have name 'lo'");
      }
      _currentIface = _config.getInterfacesConfiguration().createOrGetInterface(_currentIfaceName);
    } else if (ctx.STATIC() != null) {
      _currentIface = _config.getInterfacesConfiguration().createOrGetInterface(_currentIfaceName);
    } else if (ctx.DHCP() != null) {
      // We are not assigning any address to this interface, so it won't really be usable unless
      // another address is explicitly configured
      _currentIface = _config.getInterfacesConfiguration().createOrGetInterface(_currentIfaceName);
    } else if (ctx.MANUAL() != null) {
      // 'manual' creates an interface without an IP address
      _currentIface = _config.getInterfacesConfiguration().createOrGetInterface(_currentIfaceName);
    } else {
      _w.addWarning(ctx, ctx.getStart().getText(), _parser, "syntax is not supported now");
    }
  }

  @Override
  public void enterSi_no_inet(Si_no_inetContext ctx) {
    checkArgument(_currentIfaceName != null, "not find interface name");
    _currentIface = _config.getInterfacesConfiguration().createOrGetInterface(_currentIfaceName);
  }

  @Override
  public void exitI_address(I_addressContext ctx) {
    if (ctx.v4 != null) {
      _currentIface.addAddress(toConcreteInterfaceAddress(ctx.v4));
    } else {
      assert ctx.v6 != null;
      // ignore v6
    }
  }

  @Override
  public void exitI_address_virtual(I_address_virtualContext ctx) {
    if (ctx.v4 != null) {
      _currentIface.setAddressVirtual(
          MacAddress.parse(ctx.MAC_ADDRESS().getText()), toConcreteInterfaceAddress(ctx.v4));
    } else {
      assert ctx.v6 != null;
      // ignore v6
    }
  }

  @Override
  public void exitI_alias(I_aliasContext ctx) {
    _currentIface.setDescription(ctx.TEXT().getText());
  }

  @Override
  public void exitI_vrf_table(I_vrf_tableContext ctx) {
    _currentIface.setVrfTable(ctx.vrf_table_name().getText());
  }

  @Override
  public void exitI_bond_master(I_bond_masterContext ctx) {
    _w.addWarning(
        ctx,
        "bond-master",
        _parser,
        "bond-master command is not supported. use bond-slaves to configure bonds.");
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
        interfaceNameCtxs.stream().map(RuleContext::getText).collect(Collectors.toSet()));
  }

  @Override
  public void exitI_bridge_access(I_bridge_accessContext ctx) {
    _currentIface.createOrGetBridgeSettings().setAccess(Integer.parseInt(ctx.number().getText()));
  }

  @Override
  public void exitI_bridge_ports(I_bridge_portsContext ctx) {
    List<Interface_nameContext> interfaceNameCtxs = ctx.interface_name();
    interfaceNameCtxs.forEach(
        ifaceNameCtx ->
            _config.referenceStructure(
                CumulusStructureType.ABSTRACT_INTERFACE,
                ifaceNameCtx.getText(),
                CumulusStructureUsage.BRIDGE_PORT,
                ifaceNameCtx.getStart().getLine()));
    _currentIface.setBridgePorts(
        interfaceNameCtxs.stream()
            .map(RuleContext::getText)
            .collect(ImmutableSet.toImmutableSet()));
  }

  @Override
  public void exitI_bridge_pvid(I_bridge_pvidContext ctx) {
    _currentIface.createOrGetBridgeSettings().setPvid(Integer.parseInt(ctx.vlan_id().getText()));
  }

  @Override
  public void exitI_bridge_vids(I_bridge_vidsContext ctx) {
    IntegerSpace vids =
        IntegerSpace.unionOf(
            ctx.number_or_range().stream()
                .map(CumulusInterfacesConfigurationBuilder::toInts)
                .toArray(IntegerSpace[]::new));
    _currentIface.createOrGetBridgeSettings().setVids(vids);
  }

  @Override
  public void exitI_bridge_vlan_aware(I_bridge_vlan_awareContext ctx) {
    if (ctx.NO() != null) {
      _w.todo(ctx, getFullText(ctx), _parser);
    }
  }

  @Override
  public void exitI_clag_id(I_clag_idContext ctx) {
    _currentIface.setClagId(Integer.parseInt(ctx.number().getText()));
  }

  @Override
  public void exitI_clagd_backup_ip(I_clagd_backup_ipContext ctx) {
    InterfaceClagSettings clag = _currentIface.createOrGetClagSettings();
    clag.setBackupIp(toIp(ctx.address()));
    if (ctx.VRF() != null) {
      String vrf = ctx.vrf_name().getText();
      clag.setBackupIpVrf(vrf);
      _config.referenceStructure(
          CumulusStructureType.VRF,
          vrf,
          CumulusStructureUsage.INTERFACE_CLAG_BACKUP_IP_VRF,
          ctx.getStart().getLine());
    } else {
      clag.setBackupIpVrf(DEFAULT_VRF_NAME);
    }
  }

  @Override
  public void exitI_clagd_peer_ip(I_clagd_peer_ipContext ctx) {
    InterfaceClagSettings clag = _currentIface.createOrGetClagSettings();
    if (ctx.address() != null) {
      clag.setPeerIp(toIp(ctx.address()));
    } else if (ctx.LINK_LOCAL() != null) {
      clag.setPeerIpLinkLocal(true);
    } else {
      throw new IllegalStateException("clagd-peer-ip without an IP or linklocal");
    }
  }

  @Override
  public void exitI_clagd_priority(I_clagd_priorityContext ctx) {
    InterfaceClagSettings clag = _currentIface.createOrGetClagSettings();
    clag.setPriority(Integer.parseInt(ctx.number().getText()));
  }

  @Override
  public void exitI_clagd_sys_mac(I_clagd_sys_macContext ctx) {
    InterfaceClagSettings clag = _currentIface.createOrGetClagSettings();
    clag.setSysMac(MacAddress.parse(ctx.MAC_ADDRESS().getText()));
  }

  @Override
  public void exitI_clagd_vxlan_anycast_ip(I_clagd_vxlan_anycast_ipContext ctx) {
    _currentIface.setClagVxlanAnycastIp(toIp(ctx.address()));
  }

  @Override
  public void exitI_link_speed(I_link_speedContext ctx) {
    _currentIface.setLinkSpeed(Integer.parseInt(ctx.number().getText()));
  }

  @Override
  public void exitI_mtu(I_mtuContext ctx) {
    _currentIface.setMtu(toInt(ctx.number()));
  }

  @Override
  public void exitI_vlan_id(I_vlan_idContext ctx) {
    String vlanId = ctx.number().getText();
    _config.defineStructure(CumulusStructureType.VLAN, vlanId, ctx);
    _config.referenceStructure(
        CumulusStructureType.VLAN,
        vlanId,
        CumulusStructureUsage.VLAN_SELF_REFERENCE,
        ctx.getStart().getLine());
    _currentIface.setVlanId(Integer.parseInt(vlanId));
  }

  @Override
  public void exitI_vlan_raw_device(I_vlan_raw_deviceContext ctx) {
    // intentionally not adding a reference to the raw device
    _currentIface.setVlanRawDevice(ctx.interface_name().getText());
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
  public void exitI_vxlan_id(I_vxlan_idContext ctx) {
    _currentIface.setVxlanId(Integer.parseInt(ctx.number().getText()));
  }

  @Override
  public void exitI_vxlan_local_tunnel_ip(I_vxlan_local_tunnel_ipContext ctx) {
    _currentIface.setVxlanLocalTunnelIp(toIp(ctx.address()));
  }

  @Override
  public void exitIpuir_add(Ipuir_addContext ctx) {
    Ip gatewayIp = null;
    String nextHopInterface = null;

    if (ctx.VIA().size() != 0) {
      if (ctx.VIA().size() > 1) {
        _w.addWarning(
            ctx,
            ctx.getStart().getText(),
            _parser,
            "Multiple occurrences of 'via' not allowed in 'post-up ip route add'");
        return;
      }
      gatewayIp = toIp(ctx.address(0));
    }

    if (ctx.DEV().size() != 0) {
      if (ctx.DEV().size() > 1) {
        _w.addWarning(
            ctx,
            ctx.getStart().getText(),
            _parser,
            "Multiple occurrences of 'dev' not allowed in 'post-up ip route add'");
        return;
      }
      nextHopInterface = ctx.interface_name(0).getText();
    }

    StaticRoute sr = new StaticRoute(toPrefix(ctx.prefix()), gatewayIp, nextHopInterface, null);
    _currentIface.addPostUpIpRoute(sr);
  }

  @Override
  public void exitS_auto(S_autoContext ctx) {
    String name = ctx.interface_name().getText();
    _config.getInterfacesConfiguration().setAuto(name);
  }

  @Override
  public void exitS_iface(S_ifaceContext ctx) {
    // _currentIface will be null for the loopback interface
    if (_currentIface != null) {
      _config.defineStructure(_currentIface.getType(), _currentIface.getName(), ctx);
      _config.referenceStructure(
          _currentIface.getType(),
          _currentIface.getName(),
          _currentIface.getType().selfReference(),
          ctx.getStart().getLine());
      _config.referenceStructure(
          _currentIface.getType(),
          _currentIface.getName(),
          _currentIface.getType().selfReference(),
          ctx.getStart().getLine());
      _currentIface = null;
      _currentIfaceName = null;
    }
  }

  private static int toInt(NumberContext ctx) {
    return Integer.parseInt(ctx.getText());
  }

  private static IntegerSpace toInts(Number_or_rangeContext ctx) {
    int lo = toInt(ctx.lo);
    if (ctx.hi != null) {
      return IntegerSpace.of(Range.closed(lo, toInt(ctx.hi)));
    } else {
      return IntegerSpace.of(lo);
    }
  }
}
