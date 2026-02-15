package org.batfish.vendor.huawei.grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Acls_ruleContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Bs_peerContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Bs_router_idContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Is_descriptionContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Is_ip_addressContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Is_shutdownContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Is_undo_shutdownContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Os_areaContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Os_router_idContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_aclContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_bgpContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_interfaceContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_ospfContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_sysnameContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_vlanContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Si_route_staticContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Si_vpn_instanceContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Vs_route_distinguisherContext;
import org.batfish.vendor.huawei.representation.HuaweiAcl;
import org.batfish.vendor.huawei.representation.HuaweiAclRule;
import org.batfish.vendor.huawei.representation.HuaweiAclRule.Action;
import org.batfish.vendor.huawei.representation.HuaweiBgpPeer;
import org.batfish.vendor.huawei.representation.HuaweiBgpProcess;
import org.batfish.vendor.huawei.representation.HuaweiConfiguration;
import org.batfish.vendor.huawei.representation.HuaweiInterface;
import org.batfish.vendor.huawei.representation.HuaweiOspfArea;
import org.batfish.vendor.huawei.representation.HuaweiOspfProcess;
import org.batfish.vendor.huawei.representation.HuaweiStaticRoute;
import org.batfish.vendor.huawei.representation.HuaweiVlan;
import org.batfish.vendor.huawei.representation.HuaweiVrf;

/** Extracts control plane information from Huawei configuration. */
public class HuaweiControlPlaneExtractor extends HuaweiParserBaseListener
    implements ControlPlaneExtractor {

  private final HuaweiConfiguration _configuration;
  private final Warnings _warnings;

  // Current parsing context
  private HuaweiInterface _currentInterface;
  private HuaweiBgpProcess _currentBgpProcess;
  private HuaweiOspfProcess _currentOspfProcess;
  private HuaweiAcl _currentAcl;
  private HuaweiVrf _currentVrf;

  public HuaweiControlPlaneExtractor(String text, HuaweiCombinedParser parser, Warnings warnings) {
    _warnings = warnings;
    _configuration = new HuaweiConfiguration();
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(this, tree);
  }

  // System name
  @Override
  public void exitS_sysname(S_sysnameContext ctx) {
    String hostname = ctx.hostname.getText();
    _configuration.setHostname(hostname);
  }

  // Interface configuration
  @Override
  public void enterS_interface(S_interfaceContext ctx) {
    String name = ctx.name.getText();
    _currentInterface = new HuaweiInterface(name);
    _configuration.getInterfaces().put(name, _currentInterface);
  }

  @Override
  public void exitS_interface(S_interfaceContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void exitIs_description(Is_descriptionContext ctx) {
    if (_currentInterface != null) {
      StringBuilder desc = new StringBuilder();
      for (int i = 1; i < ctx.getChildCount() - 1; i++) { // Skip DESCRIPTION and NEWLINE
        if (desc.length() > 0) {
          desc.append(" ");
        }
        desc.append(ctx.getChild(i).getText());
      }
      _currentInterface.setDescription(desc.toString());
    }
  }

  @Override
  public void exitIs_ip_address(Is_ip_addressContext ctx) {
    if (_currentInterface != null) {
      Ip addr = Ip.parse(ctx.addr.getText());
      Ip mask = Ip.parse(ctx.mask.getText());
      try {
        ConcreteInterfaceAddress address = ConcreteInterfaceAddress.create(addr, mask);
        _currentInterface.setAddress(address);
      } catch (Exception e) {
        _warnings.redFlag("Invalid IP address: " + addr + " " + mask);
      }
    }
  }

  @Override
  public void exitIs_shutdown(Is_shutdownContext ctx) {
    if (_currentInterface != null) {
      _currentInterface.setShutdown(true);
    }
  }

  @Override
  public void exitIs_undo_shutdown(Is_undo_shutdownContext ctx) {
    if (_currentInterface != null) {
      _currentInterface.setShutdown(false);
    }
  }

  // Static routes
  @Override
  public void exitSi_route_static(Si_route_staticContext ctx) {
    Ip network = Ip.parse(ctx.dest.getText());
    Ip mask = Ip.parse(ctx.mask.getText());
    Ip nextHop = Ip.parse(ctx.nexthop.getText());
    _configuration.getStaticRoutes().add(new HuaweiStaticRoute(network, mask, nextHop));
  }

  // BGP configuration
  @Override
  public void enterS_bgp(S_bgpContext ctx) {
    long asn = Long.parseLong(ctx.asn.getText());
    _currentBgpProcess = new HuaweiBgpProcess(asn);
    _configuration.setBgpProcess(_currentBgpProcess);
  }

  @Override
  public void exitS_bgp(S_bgpContext ctx) {
    _currentBgpProcess = null;
  }

  @Override
  public void exitBs_router_id(Bs_router_idContext ctx) {
    if (_currentBgpProcess != null) {
      _currentBgpProcess.setRouterId(Ip.parse(ctx.id.getText()));
    }
  }

  @Override
  public void exitBs_peer(Bs_peerContext ctx) {
    if (_currentBgpProcess != null) {
      Ip peerIp = Ip.parse(ctx.ip.getText());
      HuaweiBgpPeer peer = new HuaweiBgpPeer(peerIp);
      if (ctx.asn != null) {
        peer.setAsNum(Long.parseLong(ctx.asn.getText()));
      }
      _currentBgpProcess.getPeers().put(peerIp, peer);
    }
  }

  // OSPF configuration
  @Override
  public void enterS_ospf(S_ospfContext ctx) {
    long processId = Long.parseLong(ctx.proc.getText());
    _currentOspfProcess = new HuaweiOspfProcess(processId);
    _configuration.setOspfProcess(_currentOspfProcess);
  }

  @Override
  public void exitS_ospf(S_ospfContext ctx) {
    _currentOspfProcess = null;
  }

  @Override
  public void exitOs_router_id(Os_router_idContext ctx) {
    if (_currentOspfProcess != null) {
      _currentOspfProcess.setRouterId(Ip.parse(ctx.id.getText()));
    }
  }

  @Override
  public void enterOs_area(Os_areaContext ctx) {
    if (_currentOspfProcess != null) {
      long areaId = Long.parseLong(ctx.area_id.getText());
      _currentOspfProcess.getAreas().put(areaId, new HuaweiOspfArea(areaId));
    }
  }

  // VLAN configuration
  @Override
  public void exitS_vlan(S_vlanContext ctx) {
    if (ctx.vlan_id() != null) {
      int vlanId = Integer.parseInt(ctx.vlan_id().dec().getText());
      _configuration.getVlans().put(vlanId, new HuaweiVlan(vlanId));
    } else if (ctx.vlan_batch() != null) {
      // TODO: Parse VLAN batch (e.g., "10,20,30-40")
      _warnings.unimplemented("VLAN batch parsing");
    }
  }

  // ACL configuration
  @Override
  public void enterS_acl(S_aclContext ctx) {
    String name = ctx.acl_name().getText();
    _currentAcl = new HuaweiAcl(name);
    _configuration.getAcls().put(name, _currentAcl);
  }

  @Override
  public void exitS_acl(S_aclContext ctx) {
    _currentAcl = null;
  }

  @Override
  public void exitAcls_rule(Acls_ruleContext ctx) {
    if (_currentAcl != null) {
      int num = Integer.parseInt(ctx.num.getText());
      Action action = ctx.action.getType() == HuaweiLexer.PERMIT ? Action.PERMIT : Action.DENY;
      _currentAcl.getRules().add(new HuaweiAclRule(num, action));
    }
  }

  @Override
  public void exitVs_route_distinguisher(Vs_route_distinguisherContext ctx) {
    if (_currentVrf != null) {
      _currentVrf.setRouteDistinguisher(ctx.word().getText());
    }
  }

  // VRF/VPN instance under IP (ip vpn-instance)
  @Override
  public void enterSi_vpn_instance(Si_vpn_instanceContext ctx) {
    String name = ctx.name.getText();
    _currentVrf = new HuaweiVrf(name);
    _configuration.getVrfs().put(name, _currentVrf);
  }

  @Override
  public void exitSi_vpn_instance(Si_vpn_instanceContext ctx) {
    _currentVrf = null;
  }
}
