package org.batfish.vendor.huawei.grammar;

import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.grammar.BatfishParseTreeWalker;
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
  private final HuaweiCombinedParser _parser;
  private final Warnings _warnings;

  // Current parsing context
  private HuaweiInterface _currentInterface;
  private HuaweiBgpProcess _currentBgpProcess;
  private HuaweiOspfProcess _currentOspfProcess;
  private HuaweiAcl _currentAcl;
  private HuaweiVrf _currentVrf;

  public HuaweiControlPlaneExtractor(String text, HuaweiCombinedParser parser, Warnings warnings) {
    _parser = parser;
    _warnings = warnings;
    _configuration = new HuaweiConfiguration();
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    BatfishParseTreeWalker walker = new BatfishParseTreeWalker(_parser);
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
    _currentInterface = _configuration.getInterfaces().computeIfAbsent(name, HuaweiInterface::new);
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
      Ip addr = parseIp(ctx.addr.getText(), ctx, "interface address");
      Ip mask = parseIp(ctx.mask.getText(), ctx, "interface mask");
      if (addr == null || mask == null) {
        return;
      }
      try {
        ConcreteInterfaceAddress address = ConcreteInterfaceAddress.create(addr, mask);
        _currentInterface.setAddress(address);
      } catch (Exception e) {
        _warnings.redFlagf(
            "Invalid interface address at line %d: %s %s",
            ctx.getStart().getLine(), ctx.addr.getText(), ctx.mask.getText());
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
    Ip network = parseIp(ctx.dest.getText(), ctx, "static route network");
    Ip mask = parseIp(ctx.mask.getText(), ctx, "static route mask");
    Ip nextHop = parseIp(ctx.nexthop.getText(), ctx, "static route next-hop");
    if (network == null || mask == null || nextHop == null) {
      return;
    }
    _configuration.getStaticRoutes().add(new HuaweiStaticRoute(network, mask, nextHop));
  }

  // BGP configuration
  @Override
  public void enterS_bgp(S_bgpContext ctx) {
    Long asn = parseLong(ctx.asn.getText(), ctx, "BGP ASN");
    if (asn == null) {
      _currentBgpProcess = null;
      return;
    }
    HuaweiBgpProcess existing = _configuration.getBgpProcess();
    if (existing == null) {
      existing = new HuaweiBgpProcess(asn);
      _configuration.setBgpProcess(existing);
    } else if (existing.getAsNum() != asn) {
      _warnings.redFlagf(
          "Conflicting BGP ASN at line %d: existing %d, new %d; keeping existing",
          ctx.getStart().getLine(), existing.getAsNum(), asn);
    }
    _currentBgpProcess = existing;
  }

  @Override
  public void exitS_bgp(S_bgpContext ctx) {
    _currentBgpProcess = null;
  }

  @Override
  public void exitBs_router_id(Bs_router_idContext ctx) {
    if (_currentBgpProcess != null) {
      Ip routerId = parseIp(ctx.id.getText(), ctx, "BGP router-id");
      if (routerId != null) {
        _currentBgpProcess.setRouterId(routerId);
      }
    }
  }

  @Override
  public void exitBs_peer(Bs_peerContext ctx) {
    if (_currentBgpProcess != null) {
      Ip peerIp = parseIp(ctx.ip.getText(), ctx, "BGP peer IP");
      if (peerIp == null) {
        return;
      }
      HuaweiBgpPeer peer = new HuaweiBgpPeer(peerIp);
      if (ctx.asn != null) {
        Long asn = parseLong(ctx.asn.getText(), ctx, "BGP peer ASN");
        if (asn != null) {
          peer.setAsNum(asn);
        }
      }
      _currentBgpProcess.getPeers().put(peerIp, peer);
    }
  }

  // OSPF configuration
  @Override
  public void enterS_ospf(S_ospfContext ctx) {
    Long processId = parseLong(ctx.proc.getText(), ctx, "OSPF process ID");
    if (processId == null) {
      _currentOspfProcess = null;
      return;
    }
    HuaweiOspfProcess existing = _configuration.getOspfProcess();
    if (existing == null) {
      existing = new HuaweiOspfProcess(processId);
      _configuration.setOspfProcess(existing);
    } else if (existing.getProcessId() != processId) {
      _warnings.redFlagf(
          "Conflicting OSPF process ID at line %d: existing %d, new %d; keeping existing",
          ctx.getStart().getLine(), existing.getProcessId(), processId);
    }
    _currentOspfProcess = existing;
  }

  @Override
  public void exitS_ospf(S_ospfContext ctx) {
    _currentOspfProcess = null;
  }

  @Override
  public void exitOs_router_id(Os_router_idContext ctx) {
    if (_currentOspfProcess != null) {
      Ip routerId = parseIp(ctx.id.getText(), ctx, "OSPF router-id");
      if (routerId != null) {
        _currentOspfProcess.setRouterId(routerId);
      }
    }
  }

  @Override
  public void enterOs_area(Os_areaContext ctx) {
    if (_currentOspfProcess != null) {
      Long areaId = parseLong(ctx.area_id.getText(), ctx, "OSPF area ID");
      if (areaId == null) {
        return;
      }
      _currentOspfProcess.getAreas().put(areaId, new HuaweiOspfArea(areaId));
    }
  }

  // VLAN configuration
  @Override
  public void exitS_vlan(S_vlanContext ctx) {
    if (ctx.vlan_id() != null) {
      Integer vlanId = parseInteger(ctx.vlan_id().dec().getText(), ctx, "VLAN ID");
      if (vlanId == null) {
        return;
      }
      _configuration.getVlans().put(vlanId, new HuaweiVlan(vlanId));
    } else if (ctx.vlan_batch() != null) {
      addVlansFromBatch(ctx);
    }
  }

  // ACL configuration
  @Override
  public void enterS_acl(S_aclContext ctx) {
    String name = ctx.acl_name().getText();
    _currentAcl = _configuration.getAcls().computeIfAbsent(name, HuaweiAcl::new);
  }

  @Override
  public void exitS_acl(S_aclContext ctx) {
    _currentAcl = null;
  }

  @Override
  public void exitAcls_rule(Acls_ruleContext ctx) {
    if (_currentAcl != null) {
      Integer num = parseInteger(ctx.num.getText(), ctx, "ACL rule number");
      if (num == null) {
        return;
      }
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
    _currentVrf = _configuration.getVrfs().computeIfAbsent(name, HuaweiVrf::new);
  }

  @Override
  public void exitSi_vpn_instance(Si_vpn_instanceContext ctx) {
    _currentVrf = null;
  }

  private void addVlansFromBatch(S_vlanContext ctx) {
    HuaweiParser.Vlan_listContext vlanList = ctx.vlan_batch().vlan_list();
    if (vlanList.vlan_item().isEmpty()) {
      return;
    }
    for (HuaweiParser.Vlan_itemContext item : vlanList.vlan_item()) {
      String itemText = item.getText();
      int dash = itemText.indexOf('-');
      if (dash < 0) {
        Integer vlanId = parseInteger(itemText, ctx, "VLAN ID");
        if (vlanId != null) {
          _configuration.getVlans().put(vlanId, new HuaweiVlan(vlanId));
        }
        continue;
      }
      String startText = itemText.substring(0, dash);
      String endText = itemText.substring(dash + 1);
      Integer start = parseInteger(startText, ctx, "VLAN range start");
      Integer end = parseInteger(endText, ctx, "VLAN range end");
      if (start == null || end == null) {
        continue;
      }
      if (start > end) {
        _warnings.redFlagf(
            "Invalid VLAN batch range at line %d: %d-%d", ctx.getStart().getLine(), start, end);
        continue;
      }
      for (int vlanId = start; vlanId <= end; vlanId++) {
        _configuration.getVlans().put(vlanId, new HuaweiVlan(vlanId));
      }
    }
  }

  private Integer parseInteger(String text, ParserRuleContext ctx, String field) {
    try {
      return Integer.parseInt(text);
    } catch (NumberFormatException e) {
      _warnings.redFlagf("Invalid %s at line %d: %s", field, ctx.getStart().getLine(), text);
      return null;
    }
  }

  private Long parseLong(String text, ParserRuleContext ctx, String field) {
    try {
      return Long.parseLong(text);
    } catch (NumberFormatException e) {
      _warnings.redFlagf("Invalid %s at line %d: %s", field, ctx.getStart().getLine(), text);
      return null;
    }
  }

  private Ip parseIp(String text, ParserRuleContext ctx, String field) {
    try {
      return Ip.parse(text);
    } catch (IllegalArgumentException e) {
      _warnings.redFlagf("Invalid %s at line %d: %s", field, ctx.getStart().getLine(), text);
      return null;
    }
  }
}
