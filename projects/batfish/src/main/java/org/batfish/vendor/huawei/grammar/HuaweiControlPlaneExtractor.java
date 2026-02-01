package org.batfish.vendor.huawei.grammar;

import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Bgp_peerContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Bgp_router_idContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Description_lineContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_dot1q_terminationContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_ip_addressContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.If_shutdownContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_areaContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_networkContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Ospf_router_idContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_aclContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_bgpContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_interfaceContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_natContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_ospfContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_returnContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_static_routeContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_sysnameContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_vlanContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.S_vrfContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.V_descriptionContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.V_nameContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Vrf_route_distinguisherContext;
import org.batfish.vendor.huawei.grammar.HuaweiParser.Vrf_vpn_targetContext;
import org.batfish.vendor.huawei.representation.HuaweiAcl;
import org.batfish.vendor.huawei.representation.HuaweiAcl.AclType;
import org.batfish.vendor.huawei.representation.HuaweiAclLine;
import org.batfish.vendor.huawei.representation.HuaweiBgpProcess;
import org.batfish.vendor.huawei.representation.HuaweiConfiguration;
import org.batfish.vendor.huawei.representation.HuaweiInterface;
import org.batfish.vendor.huawei.representation.HuaweiNatRule;
import org.batfish.vendor.huawei.representation.HuaweiNatRule.NatType;
import org.batfish.vendor.huawei.representation.HuaweiOspfProcess;
import org.batfish.vendor.huawei.representation.HuaweiStaticRoute;
import org.batfish.vendor.huawei.representation.HuaweiVlan;
import org.batfish.vendor.huawei.representation.HuaweiVrf;

/**
 * Control plane extractor for Huawei VRP configurations.
 *
 * <p>This class extracts configuration data from Huawei VRP parse trees using ANTLR listener
 * pattern. It processes system settings (hostname), interfaces, and other configuration elements.
 */
public class HuaweiControlPlaneExtractor extends HuaweiParserBaseListener
    implements ControlPlaneExtractor {

  private final HuaweiConfiguration _configuration;
  private final String _text;
  private final HuaweiCombinedParser _parser;
  private final Warnings _w;
  private String _currentInterfaceName;
  private HuaweiAcl _currentAcl;
  private HuaweiVrf _currentVrf;
  private Integer _currentVlanId;
  private String _pendingVlanDescription;

  public HuaweiControlPlaneExtractor(
      String text, HuaweiCombinedParser parser, Warnings w, SilentSyntaxCollection silentSyntax) {
    _text = text;
    _parser = parser;
    _w = w;
    _configuration = new HuaweiConfiguration();
    _currentInterfaceName = null;
    _currentAcl = null;
    _currentVrf = null;
    _currentVlanId = null;
    _pendingVlanDescription = null;
  }

  public String getInputText() {
    return _text;
  }

  public HuaweiCombinedParser getParser() {
    return _parser;
  }

  public Warnings getWarnings() {
    return _w;
  }

  @Override
  public VendorConfiguration getVendorConfiguration() {
    return _configuration;
  }

  @Override
  public void processParseTree(NetworkSnapshot snapshot, ParserRuleContext tree) {
    ParseTreeWalker.DEFAULT.walk(this, tree);
  }

  /**
   * Extracts configuration from a Huawei configuration text.
   *
   * @param text The configuration text to parse
   * @param parser The combined parser to use
   * @param w Warnings object to collect parsing warnings
   * @return A populated HuaweiConfiguration object
   */
  public static HuaweiConfiguration extract(String text, HuaweiCombinedParser parser, Warnings w) {
    return extract(text, parser, w, new SilentSyntaxCollection());
  }

  /**
   * Extracts configuration from a Huawei configuration text.
   *
   * @param text The configuration text to parse
   * @param parser The combined parser to use
   * @param w Warnings object to collect parsing warnings
   * @param silentSyntax Collection of silent syntax patterns
   * @return A populated HuaweiConfiguration object
   */
  public static HuaweiConfiguration extract(
      String text, HuaweiCombinedParser parser, Warnings w, SilentSyntaxCollection silentSyntax) {
    HuaweiParser.Huawei_configurationContext tree = parser.parse();
    HuaweiControlPlaneExtractor extractor =
        new HuaweiControlPlaneExtractor(text, parser, w, silentSyntax);
    ParseTreeWalker.DEFAULT.walk(extractor, tree);
    return extractor._configuration;
  }

  /**
   * Process exit from s_sysname rule - extract hostname.
   *
   * <p>Extracts hostname from the sysname command (e.g., "sysname Router1").
   */
  @Override
  public void exitS_sysname(S_sysnameContext ctx) {
    if (ctx.hostname != null) {
      String hostname = ctx.hostname.getText();
      _configuration.setHostname(hostname);
    }
  }

  /**
   * Process entry to s_interface rule - begin tracking a new interface.
   *
   * <p>Extracts the interface name and prepares to collect interface-specific configuration.
   */
  @Override
  public void enterS_interface(S_interfaceContext ctx) {
    if (ctx.iname != null) {
      _currentInterfaceName = ctx.iname.getText();
      // Create or get the interface
      HuaweiInterface iface = _configuration.getInterfaces().get(_currentInterfaceName);
      if (iface == null) {
        iface = new HuaweiInterface(_currentInterfaceName);
        _configuration.addInterface(_currentInterfaceName, iface);
      }
    }
  }

  /**
   * Process exit from if_ip_address rule - extract interface IP address.
   *
   * <p>Extracts IPv4 address and subnet mask from the "ip address A.B.C.D A.B.C.D" command.
   */
  @Override
  public void exitIf_ip_address(If_ip_addressContext ctx) {
    if (_currentInterfaceName == null) {
      return;
    }

    HuaweiInterface iface = _configuration.getInterfaces().get(_currentInterfaceName);
    if (iface == null || ctx.addr == null || ctx.mask == null) {
      return;
    }

    try {
      String addrStr = ctx.addr.getText();
      String maskStr = ctx.mask.getText();

      org.batfish.datamodel.Ip addr = org.batfish.datamodel.Ip.parse(addrStr);
      org.batfish.datamodel.Ip mask = org.batfish.datamodel.Ip.parse(maskStr);

      // Create interface address using IP and subnet mask
      org.batfish.datamodel.ConcreteInterfaceAddress address =
          org.batfish.datamodel.ConcreteInterfaceAddress.create(addr, mask);

      iface.setAddress(address);
    } catch (IllegalArgumentException e) {
      // Invalid IP address or mask - record warning and continue
      String warning =
          String.format(
              "Invalid IP address configuration on interface %s at line %d: %s",
              _currentInterfaceName, ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from if_description rule - extract interface description.
   *
   * <p>Extracts the description text from the "description" command.
   */
  @Override
  public void exitIf_description(HuaweiParser.If_descriptionContext ctx) {
    if (_currentInterfaceName == null) {
      return;
    }

    HuaweiInterface iface = _configuration.getInterfaces().get(_currentInterfaceName);
    if (iface == null) {
      return;
    }

    Description_lineContext descCtx = ctx.description_line();
    if (descCtx != null && descCtx.text != null) {
      // Get all VARIABLE tokens and join them with spaces
      StringBuilder description = new StringBuilder();
      if (descCtx.text.getStart() != null && descCtx.text.getStop() != null) {
        org.antlr.v4.runtime.TokenStream tokens = _parser.getParser().getTokenStream();
        int start = descCtx.text.getStart().getTokenIndex();
        int stop = descCtx.text.getStop().getTokenIndex();
        for (int i = start; i <= stop; i++) {
          org.antlr.v4.runtime.Token token = tokens.get(i);
          if (token.getChannel() == org.antlr.v4.runtime.Token.DEFAULT_CHANNEL) {
            if (description.length() > 0) {
              description.append(" ");
            }
            description.append(token.getText());
          }
        }
      }
      iface.setDescription(description.toString());
    }
  }

  /**
   * Process exit from if_shutdown rule - track interface admin status.
   *
   * <p>Sets the shutdown flag when "shutdown" command is present.
   */
  @Override
  public void exitIf_shutdown(If_shutdownContext ctx) {
    if (_currentInterfaceName == null) {
      return;
    }

    HuaweiInterface iface = _configuration.getInterfaces().get(_currentInterfaceName);
    if (iface == null) {
      return;
    }

    // If SHUTDOWN token is present, the interface is shutdown
    // If UNDO SHUTDOWN, the interface is not shutdown (enabled)
    boolean isShutdown = ctx.SHUTDOWN() != null && ctx.UNDO() == null;
    iface.setShutdown(isShutdown);
  }

  /**
   * Process exit from s_return rule - clear current interface context.
   *
   * <p>Called when exiting an interface configuration block (return command).
   */
  @Override
  public void exitS_return(S_returnContext ctx) {
    // Clear the current interface context when we exit the interface block
    _currentInterfaceName = null;
    // Also clear current VLAN context
    _currentVlanId = null;
    // Clear pending VLAN description
    _pendingVlanDescription = null;
  }

  /**
   * Process entry to s_vlan rule - track current VLAN context.
   *
   * <p>Sets the current VLAN ID when entering a VLAN configuration block (not for batch).
   */
  @Override
  public void enterS_vlan(S_vlanContext ctx) {
    // Only track current VLAN for single VLAN configuration (not batch)
    if (ctx.vlan_id != null) {
      try {
        _currentVlanId = Integer.parseInt(ctx.vlan_id.getText());
      } catch (NumberFormatException e) {
        // Invalid VLAN ID will be handled in exitS_vlan
        _currentVlanId = null;
      }
    } else {
      // For "vlan batch", don't set current VLAN context
      _currentVlanId = null;
    }
  }

  /**
   * Process exit from s_vlan rule - extract VLAN configuration.
   *
   * <p>Extracts VLAN ID from "vlan {@code <id>}" command and creates HuaweiVlan object. For "vlan
   * batch" commands, creates multiple VLANs.
   */
  @Override
  public void exitS_vlan(S_vlanContext ctx) {
    // Handle "vlan batch" command (create multiple VLANs)
    if (ctx.vlan_batch_range() != null) {
      // Check if this is a range specification with "to" keyword
      if (ctx.vlan_batch_range().TO() != null) {
        // Handle "vlan batch X to Y" - create range from X to Y-1 (exclusive at end)
        // "2 to 10" creates VLANs 2-9, not 2-10
        List<HuaweiParser.Uint8Context> uint8Contexts = ctx.vlan_batch_range().uint8();
        if (uint8Contexts.size() >= 2) {
          try {
            int startVlan = Integer.parseInt(uint8Contexts.get(0).getText());
            int endVlan = Integer.parseInt(uint8Contexts.get(uint8Contexts.size() - 1).getText());
            // Create VLANs from start to end-1 (exclusive at end)
            for (int vlanId = startVlan; vlanId < endVlan; vlanId++) {
              HuaweiVlan vlan = _configuration.getVlan(vlanId);
              if (vlan == null) {
                vlan = new HuaweiVlan(vlanId);
                _configuration.addVlan(vlanId, vlan);
              }
            }
          } catch (NumberFormatException e) {
            String warning =
                String.format(
                    "Invalid VLAN ID range at line %d",
                    ctx.vlan_batch_range().getStart().getLine());
            _w.redFlag(warning);
          }
        }
      } else {
        // Handle "vlan batch X Y Z" - create specific VLANs
        for (HuaweiParser.Uint8Context uint8Ctx : ctx.vlan_batch_range().uint8()) {
          try {
            int vlanId = Integer.parseInt(uint8Ctx.getText());
            HuaweiVlan vlan = _configuration.getVlan(vlanId);
            if (vlan == null) {
              vlan = new HuaweiVlan(vlanId);
              _configuration.addVlan(vlanId, vlan);
            }
          } catch (NumberFormatException e) {
            String warning =
                String.format(
                    "Invalid VLAN ID at line %d: %s",
                    uint8Ctx.getStart().getLine(), uint8Ctx.getText());
            _w.redFlag(warning);
          }
        }
      }
    }
    // Handle individual "vlan <id>" command
    else if (ctx.vlan_id != null) {
      try {
        int vlanId = Integer.parseInt(ctx.vlan_id.getText());
        HuaweiVlan vlan = _configuration.getVlan(vlanId);
        if (vlan == null) {
          vlan = new HuaweiVlan(vlanId);
          // Apply pending description if exists
          if (_pendingVlanDescription != null) {
            vlan.setDescription(_pendingVlanDescription);
            _pendingVlanDescription = null;
          }
          _configuration.addVlan(vlanId, vlan);
        }
      } catch (NumberFormatException e) {
        String warning =
            String.format(
                "Invalid VLAN ID at line %d: %s",
                ctx.vlan_id.getStart().getLine(), ctx.vlan_id.getText());
        _w.redFlag(warning);
      }
    }
  }

  /**
   * Process exit from v_name rule - extract VLAN name.
   *
   * <p>Extracts VLAN name from the "name" command within a VLAN configuration block.
   */
  @Override
  public void exitV_name(V_nameContext ctx) {
    // We need to find which VLAN we're currently configuring
    // This is tricky because the grammar doesn't give us direct context
    // We'll need to track the current VLAN similar to how we track current interface
    // For now, this is a stub that will be enhanced when we add full VLAN tracking
  }

  /**
   * Process exit from v_description rule - extract VLAN description.
   *
   * <p>Extracts description from the "description" command within a VLAN configuration block.
   */
  @Override
  public void exitV_description(V_descriptionContext ctx) {
    if (_currentVlanId == null) {
      return;
    }

    // Extract description text and store it temporarily
    // It will be applied to the VLAN when exitS_vlan creates the VLAN object
    if (ctx.description_line() != null && ctx.description_line().text != null) {
      String text = ctx.description_line().text.getText();
      if (!text.isEmpty()) {
        _pendingVlanDescription = text.trim();
      }
    }
  }

  /** Process entry to v_description rule - for debugging */
  @Override
  public void enterV_description(V_descriptionContext ctx) {
    // Debug: check if we have a current VLAN ID
    if (_currentVlanId == null) {
      // No current VLAN - this might be the problem
    }
  }

  /**
   * Process exit from if_dot1q_termination rule - extract subinterface VLAN assignment.
   *
   * <p>Extracts VLAN ID from "dot1q termination vid {@code <vid>}" command on subinterfaces.
   */
  @Override
  public void exitIf_dot1q_termination(If_dot1q_terminationContext ctx) {
    if (_currentInterfaceName == null) {
      return;
    }

    HuaweiInterface iface = _configuration.getInterfaces().get(_currentInterfaceName);
    if (iface == null || ctx.vid == null) {
      return;
    }

    try {
      // Store the VLAN ID for this subinterface
      // This can be used later to associate the subinterface with a VLAN
      // For now, we just note it - the actual VLAN-to-subinterface mapping
      // will be done during conversion to Batfish model
      Integer.parseInt(ctx.vid.getText());
    } catch (NumberFormatException e) {
      String warning =
          String.format(
              "Invalid VLAN ID in dot1q termination on interface %s at line %d: %s",
              _currentInterfaceName, ctx.getStart().getLine(), ctx.vid.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from s_static_route rule - extract static route configuration.
   *
   * <p>Extracts static route information including destination, next-hop, preference, etc.
   */
  @Override
  public void exitS_static_route(S_static_routeContext ctx) {
    if (ctx.static_route_body() == null) {
      return;
    }

    HuaweiParser.Static_route_bodyContext body = ctx.static_route_body();

    try {
      HuaweiStaticRoute route = null;

      // Extract destination and next-hop based on format
      if (body.dest_prefix != null) {
        // CIDR notation: ip route-static 10.0.0.0/24 192.168.1.1
        Prefix destPrefix = Prefix.parse(body.dest_prefix.getText());
        route = new HuaweiStaticRoute(destPrefix);

        if (body.next_hop != null) {
          Ip nextHop = Ip.parse(body.next_hop.getText());
          route.setNextHopIp(nextHop);
        }
      } else if (body.dest_addr != null) {
        // Traditional notation with mask
        Ip destIp = Ip.parse(body.dest_addr.getText());

        if (body.dest_mask != null) {
          Ip mask = Ip.parse(body.dest_mask.getText());
          Prefix destPrefix = Prefix.create(destIp, mask);
          route = new HuaweiStaticRoute(destPrefix);
        } else {
          // No mask - treat as /32
          Prefix destPrefix = Prefix.create(destIp, 32);
          route = new HuaweiStaticRoute(destPrefix);
        }

        // Set next-hop
        if (body.next_hop != null) {
          Ip nextHop = Ip.parse(body.next_hop.getText());
          route.setNextHopIp(nextHop);
        } else if (body.next_hop2 != null) {
          Ip nextHop = Ip.parse(body.next_hop2.getText());
          route.setNextHopIp(nextHop);
        }

        // Set outgoing interface if present
        if (body.out_if != null) {
          route.setNextHopInterface(body.out_if.getText());
        }
      } else if (body.dest_addr2 != null) {
        // Alternative format with interface
        Ip destIp = Ip.parse(body.dest_addr2.getText());

        if (body.dest_mask2 != null) {
          Ip mask = Ip.parse(body.dest_mask2.getText());
          Prefix destPrefix = Prefix.create(destIp, mask);
          route = new HuaweiStaticRoute(destPrefix);
        } else {
          Prefix destPrefix = Prefix.create(destIp, 32);
          route = new HuaweiStaticRoute(destPrefix);
        }

        // Set outgoing interface
        if (body.out_if != null) {
          route.setNextHopInterface(body.out_if.getText());
        }

        // Set next-hop
        if (body.next_hop2 != null) {
          org.batfish.datamodel.Ip nextHop =
              org.batfish.datamodel.Ip.parse(body.next_hop2.getText());
          route.setNextHopIp(nextHop);
        }
      }

      // Set preference if present
      if (route != null && body.pref != null) {
        try {
          int preference = Integer.parseInt(body.pref.getText());
          route.setPreference(preference);
        } catch (NumberFormatException e) {
          String warning =
              String.format(
                  "Invalid preference value at line %d: %s",
                  body.pref.getStart().getLine(), body.pref.getText());
          _w.redFlag(warning);
        }
      }

      // Set VRF if present
      if (route != null && body.vrf != null) {
        route.setVrfName(body.vrf.getText());
      }

      // Add route to configuration
      if (route != null) {
        _configuration.addStaticRoute(route);
      }
    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing static route at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process entry to s_bgp rule - create BGP process.
   *
   * <p>Creates HuaweiBgpProcess object with AS number.
   */
  @Override
  public void enterS_bgp(S_bgpContext ctx) {
    if (ctx.as_num != null) {
      try {
        long asNum = Long.parseLong(ctx.as_num.getText());
        HuaweiBgpProcess bgpProcess = new HuaweiBgpProcess(asNum);
        _configuration.setBgpProcess(bgpProcess);
      } catch (NumberFormatException e) {
        String warning =
            String.format(
                "Invalid BGP AS number at line %d: %s",
                ctx.as_num.getStart().getLine(), ctx.as_num.getText());
        _w.redFlag(warning);
      }
    }
  }

  /**
   * Process exit from bgp_router_id rule - extract router ID.
   *
   * <p>Extracts BGP router ID from the "router-id" command.
   */
  @Override
  public void exitBgp_router_id(Bgp_router_idContext ctx) {
    HuaweiBgpProcess bgpProcess = _configuration.getBgpProcess();
    if (bgpProcess == null || ctx.router_ip == null) {
      return;
    }

    try {
      Ip routerId = Ip.parse(ctx.router_ip.getText());
      bgpProcess.setRouterId(routerId);
    } catch (Exception e) {
      String warning =
          String.format(
              "Invalid BGP router ID at line %d: %s",
              ctx.router_ip.getStart().getLine(), ctx.router_ip.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from bgp_peer rule - extract BGP peer configuration.
   *
   * <p>Extracts BGP peer IP address and AS number.
   */
  @Override
  public void exitBgp_peer(Bgp_peerContext ctx) {
    HuaweiBgpProcess bgpProcess = _configuration.getBgpProcess();
    if (bgpProcess == null || ctx.peer_ip == null || ctx.peer_as == null) {
      return;
    }

    try {
      // Store peer info in a simple map for now (Phase 5)
      // Full BGP conversion will be implemented in future phases
      // For now, we just track that BGP is configured with peers
      Ip.parse(ctx.peer_ip.getText());
      Long.parseLong(ctx.peer_as.getText());

    } catch (Exception e) {
      String warning =
          String.format(
              "Invalid BGP peer configuration at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process entry to s_acl rule - create ACL object.
   *
   * <p>Creates HuaweiAcl object with name/number and type.
   */
  @Override
  public void enterS_acl(S_aclContext ctx) {
    String aclName = null;
    AclType aclType = AclType.ADVANCED; // Default to advanced

    // Extract ACL name/number
    if (ctx.acl_name != null) {
      aclName = ctx.acl_name.getText();
    } else if (ctx.acl_num != null) {
      aclName = ctx.acl_num.getText();
    }

    // Determine ACL type based on keyword or number range
    if (ctx.acl_type != null) {
      aclType = ctx.acl_type.getText().equals("basic") ? AclType.BASIC : AclType.ADVANCED;
    } else if (ctx.acl_num != null) {
      // Determine type from ACL number range
      try {
        int aclNum = Integer.parseInt(ctx.acl_num.getText());
        if (aclNum >= 2000 && aclNum < 3000) {
          aclType = AclType.BASIC;
        } else if (aclNum >= 3000 && aclNum < 4000) {
          aclType = AclType.ADVANCED;
        }
      } catch (NumberFormatException e) {
        // Invalid ACL number - will be handled as warning
      }
    }

    if (aclName != null) {
      _currentAcl = new HuaweiAcl(aclName, aclType);
      _configuration.addAcl(aclName, _currentAcl);
    }
  }

  /**
   * Process exit from s_acl rule - clear current ACL context.
   *
   * <p>Called when exiting an ACL configuration block.
   */
  @Override
  public void exitS_acl(S_aclContext ctx) {
    _currentAcl = null;
  }

  /**
   * Process exit from acl_rule rule - extract permit/deny rule.
   *
   * <p>Extracts ACL rule information including action, protocol, source, destination, and ports.
   */
  @Override
  public void exitAcl_rule(HuaweiParser.Acl_ruleContext ctx) {
    if (_currentAcl == null) {
      return;
    }

    try {
      // Extract action (permit/deny)
      String action = "deny"; // Default to deny
      if (ctx.action != null) {
        action = ctx.action.getText().toLowerCase();
      }

      // Create ACL line with sequence number (use size of existing lines + 1)
      int seqNum = _currentAcl.getLines().size() + 1;
      HuaweiAclLine line = new HuaweiAclLine(seqNum, action);

      // Extract protocol
      String protocol = "ip"; // Default to IP (any protocol)
      if (ctx.TCP() != null) {
        protocol = "tcp";
      } else if (ctx.UDP() != null) {
        protocol = "udp";
      } else if (ctx.ICMP() != null) {
        protocol = "icmp";
      } else if (ctx.IP() != null) {
        protocol = "ip";
      } else if (ctx.variable() != null) {
        protocol = ctx.variable().getText().toLowerCase();
      }
      line.setProtocol(protocol);

      // Extract source address
      if (ctx.src_addr != null) {
        String srcAddr = ctx.src_addr.getText();
        // Handle wildcard format
        if (ctx.src_wildcard != null) {
          // Convert address+wildcard to prefix format
          // For now, store as "address wildcard"
          line.setSource(srcAddr + " " + ctx.src_wildcard.getText());
        } else if (ctx.src_prefix_len != null) {
          // CIDR notation
          line.setSource(srcAddr + "/" + ctx.src_prefix_len.getText());
        } else {
          line.setSource(srcAddr);
        }
      } else if (ctx.src_any != null) {
        line.setSource("any");
      }

      // Extract destination address
      if (ctx.dest_addr != null) {
        String destAddr = ctx.dest_addr.getText();
        // Handle wildcard format
        if (ctx.dest_wildcard != null) {
          // Convert address+wildcard to prefix format
          line.setDestination(destAddr + " " + ctx.dest_wildcard.getText());
        } else if (ctx.dest_prefix_len != null) {
          // CIDR notation
          line.setDestination(destAddr + "/" + ctx.dest_prefix_len.getText());
        } else {
          line.setDestination(destAddr);
        }
      } else if (ctx.dest_any != null) {
        line.setDestination("any");
      }

      // Extract source port
      if (ctx.src_port != null) {
        String portOp = "";
        if (ctx.eq != null) {
          portOp = "eq ";
        } else if (ctx.gt != null) {
          portOp = "gt ";
        } else if (ctx.lt != null) {
          portOp = "lt ";
        } else if (ctx.range != null && ctx.src_port_start != null && ctx.src_port_end != null) {
          portOp = "range " + ctx.src_port_start.getText() + " ";
          line.setSourcePort(portOp + ctx.src_port_end.getText());
        }
        if (!portOp.isEmpty() && ctx.src_port != null) {
          line.setSourcePort(portOp + ctx.src_port.getText());
        }
      }

      // Extract destination port
      if (ctx.dest_port != null) {
        String portOp = "";
        if (ctx.eq2 != null) {
          portOp = "eq ";
        } else if (ctx.gt2 != null) {
          portOp = "gt ";
        } else if (ctx.lt2 != null) {
          portOp = "lt ";
        } else if (ctx.range2 != null && ctx.dest_port_start != null && ctx.dest_port_end != null) {
          portOp = "range " + ctx.dest_port_start.getText() + " ";
          line.setDestinationPort(portOp + ctx.dest_port_end.getText());
        }
        if (!portOp.isEmpty() && ctx.dest_port != null) {
          line.setDestinationPort(portOp + ctx.dest_port.getText());
        }
      }

      // Add the line to the ACL
      _currentAcl.addLine(line);

    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing ACL rule at line %d: %s", ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from s_nat rule - extract NAT configuration.
   *
   * <p>Extracts NAT configuration including address groups, outbound rules, static NAT, and NAT
   * server.
   */
  @Override
  public void exitS_nat(S_natContext ctx) {
    // Check if this is a "no nat" command (undo NAT)
    if (ctx.NO() != null) {
      // Undo NAT - ignore for now
      return;
    }

    try {
      // Handle nat address-group
      if (ctx.ADDRESS_GROUP() != null && ctx.uint16() != null) {
        // For now, just note that address groups exist
        // Full implementation would parse and store the address pool
      }

      // Handle nat outbound (dynamic NAT / Easy IP)
      else if (ctx.OUTBOUND() != null) {
        // Create NAT rule for outbound
        String ruleName = "outbound_" + System.currentTimeMillis();
        HuaweiNatRule natRule = new HuaweiNatRule(ruleName, NatType.DYNAMIC);

        // Extract ACL number/name
        if (ctx.acl_num != null) {
          natRule.setAclName(ctx.acl_num.getText());
        } else if (ctx.acl_name != null) {
          natRule.setAclName(ctx.acl_name.getText());
        }

        // Check if using interface (Easy IP)
        if (ctx.INTERFACE() != null) {
          natRule.setType(NatType.EASY_IP);
        }
        // Check if using pool name
        else if (ctx.pool_name != null) {
          natRule.setPoolName(ctx.pool_name.getText());
        }

        // Extract VRF name if present
        if (ctx.vrf_name != null) {
          natRule.setVrfName(ctx.vrf_name.getText());
        }

        _configuration.addNatRule(natRule);
      }

      // Handle nat static (static one-to-one NAT)
      else if (ctx.STATIC() != null) {
        // Create NAT rule for static NAT
        String ruleName = "static_" + System.currentTimeMillis();
        HuaweiNatRule natRule = new HuaweiNatRule(ruleName, NatType.STATIC);

        // Extract global IP (first IP address)
        if (ctx.ip_address() != null && !ctx.ip_address().isEmpty()) {
          Ip globalIp = Ip.parse(ctx.ip_address(0).getText());
          natRule.setGlobalIp(globalIp);
        }

        // Extract inside IP (second IP address)
        if (ctx.ip_address() != null && !ctx.ip_address().isEmpty()) {
          Ip insideIp = Ip.parse(ctx.ip_address(1).getText());
          natRule.setInsideLocalIp(insideIp);
        }

        // Extract VRF name if present
        if (ctx.VPN_INSTANCE() != null && ctx.VARIABLE() != null) {
          natRule.setVrfName(ctx.VARIABLE().getText());
        }

        _configuration.addNatRule(natRule);
      }

      // Handle nat server (port forwarding)
      else if (ctx.SERVER() != null) {
        // Create NAT rule for NAT server
        String ruleName = "server_" + System.currentTimeMillis();
        HuaweiNatRule natRule = new HuaweiNatRule(ruleName, NatType.NAT_SERVER);

        // Check if protocol specified
        if (ctx.PROTOCOL() != null) {
          if (ctx.TCP() != null) {
            natRule.setProtocol("tcp");
          } else if (ctx.UDP() != null) {
            natRule.setProtocol("udp");
          }

          // Extract ports if protocol specified
          if (ctx.global_port_proto != null) {
            try {
              natRule.setGlobalPort(Integer.parseInt(ctx.global_port_proto.getText()));
            } catch (NumberFormatException e) {
              // Invalid port number
            }
          }
          if (ctx.inside_port_proto != null) {
            try {
              natRule.setInsideLocalPort(Integer.parseInt(ctx.inside_port_proto.getText()));
            } catch (NumberFormatException e) {
              // Invalid port number
            }
          }

          // Extract IPs
          if (ctx.ip_address() != null && !ctx.ip_address().isEmpty()) {
            Ip globalIp = Ip.parse(ctx.ip_address(0).getText());
            natRule.setGlobalIp(globalIp);
          }
          if (ctx.ip_address() != null && !ctx.ip_address().isEmpty()) {
            Ip insideIp = Ip.parse(ctx.ip_address(1).getText());
            natRule.setInsideLocalIp(insideIp);
          }
        } else {
          // No protocol - check for simple IP mapping or single port
          if (ctx.ip_address() != null && !ctx.ip_address().isEmpty()) {
            Ip globalIp = Ip.parse(ctx.ip_address(0).getText());
            natRule.setGlobalIp(globalIp);
          }
          if (ctx.ip_address() != null && !ctx.ip_address().isEmpty()) {
            Ip insideIp = Ip.parse(ctx.ip_address(1).getText());
            natRule.setInsideLocalIp(insideIp);
          }

          // Check for single port (format without protocol keyword)
          if (ctx.global_port_simple != null) {
            try {
              natRule.setGlobalPort(Integer.parseInt(ctx.global_port_simple.getText()));
            } catch (NumberFormatException e) {
              // Invalid port number
            }
          }
        }

        // Extract VRF name if present
        if (ctx.VPN_INSTANCE() != null && ctx.VARIABLE() != null) {
          natRule.setVrfName(ctx.VARIABLE().getText());
        }

        _configuration.addNatRule(natRule);
      }

    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing NAT configuration at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process entry to s_ospf rule - create OSPF process.
   *
   * <p>Creates HuaweiOspfProcess object with process ID.
   */
  @Override
  public void enterS_ospf(S_ospfContext ctx) {
    // Only create OSPF process if it doesn't already exist
    // This prevents resetting the areas map if enterS_ospf is called multiple times
    if (_configuration.getOspfProcess() == null && ctx.process_id != null) {
      try {
        long processId = Long.parseLong(ctx.process_id.getText());
        HuaweiOspfProcess ospfProcess = new HuaweiOspfProcess(processId);
        _configuration.setOspfProcess(ospfProcess);
      } catch (NumberFormatException e) {
        String warning =
            String.format(
                "Invalid OSPF process ID at line %d: %s",
                ctx.process_id.getStart().getLine(), ctx.process_id.getText());
        _w.redFlag(warning);
      }
    }
  }

  /**
   * Process exit from ospf_area rule - extract OSPF area configuration.
   *
   * <p>Extracts area ID from the "area" command and creates area in OSPF process.
   */
  @Override
  public void exitOspf_area(Ospf_areaContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null) {
      // OSPF process not initialized yet - shouldn't happen if config is well-formed
      String warning =
          String.format(
              "OSPF process not initialized when processing area at line %d",
              ctx.getStart().getLine());
      _w.redFlag(warning);
      return;
    }

    if (ctx.area_id == null) {
      // No area ID in parse tree - this is a grammar issue
      String warning = String.format("OSPf area ID is null at line %d", ctx.getStart().getLine());
      _w.redFlag(warning);
      return;
    }

    try {
      long areaId = Long.parseLong(ctx.area_id.getText());
      // Create or get the area
      HuaweiOspfProcess.HuaweiOspfArea area = ospfProcess.getOrCreateArea(areaId);
      // Verify area was added
      if (area != null) {
        // Successfully created/retrieved area
      }
    } catch (NumberFormatException e) {
      String warning =
          String.format(
              "Invalid OSPF area ID at line %d: %s",
              ctx.area_id.getStart().getLine(), ctx.area_id.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from ospf_network rule - extract OSPF network configuration.
   *
   * <p>Extracts network prefix and area ID from "network {@code <prefix>} area {@code <area-id>}"
   * command.
   */
  @Override
  public void exitOspf_network(Ospf_networkContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null || ctx.ip == null || ctx.area_id == null) {
      return;
    }

    try {
      Prefix network = Prefix.parse(ctx.ip.getText());
      long areaId = Long.parseLong(ctx.area_id.getText());

      // Create OSPF network object
      HuaweiOspfProcess.HuaweiOspfNetwork ospfNetwork =
          new HuaweiOspfProcess.HuaweiOspfNetwork(network, areaId);
      ospfProcess.addNetwork(ospfNetwork);

      // Also ensure the area exists
      ospfProcess.getOrCreateArea(areaId);
    } catch (Exception e) {
      String warning =
          String.format(
              "Error parsing OSPF network at line %d: %s",
              ctx.getStart().getLine(), e.getMessage());
      _w.redFlag(warning);
    }
  }

  /**
   * Process exit from ospf_router_id rule - extract OSPF router ID.
   *
   * <p>Extracts router ID from the "router-id" command.
   */
  @Override
  public void exitOspf_router_id(Ospf_router_idContext ctx) {
    HuaweiOspfProcess ospfProcess = _configuration.getOspfProcess();
    if (ospfProcess == null || ctx.router_ip == null) {
      return;
    }

    try {
      Ip routerId = Ip.parse(ctx.router_ip.getText());
      ospfProcess.setRouterId(routerId);
    } catch (Exception e) {
      String warning =
          String.format(
              "Invalid OSPF router ID at line %d: %s",
              ctx.router_ip.getStart().getLine(), ctx.router_ip.getText());
      _w.redFlag(warning);
    }
  }

  /**
   * Process entry to s_vrf rule - create VRF object.
   *
   * <p>Creates HuaweiVrf object with VRF name.
   */
  @Override
  public void enterS_vrf(S_vrfContext ctx) {
    if (ctx.vrf_name != null) {
      String vrfName = ctx.vrf_name.getText();
      HuaweiVrf vrf = new HuaweiVrf(vrfName);
      _configuration.addVrf(vrfName, vrf);
      _currentVrf = vrf;
    }
  }

  /**
   * Process exit from vrf_route_distinguisher rule - extract RD value.
   *
   * <p>Extracts route distinguisher from "route-distinguisher {@code <rd>}" command.
   */
  @Override
  public void exitVrf_route_distinguisher(Vrf_route_distinguisherContext ctx) {
    if (_currentVrf == null || ctx.rd == null) {
      return;
    }

    String rd = ctx.rd.getText();
    _currentVrf.setRouteDistinguisher(rd);
  }

  /**
   * Process exit from vrf_vpn_target rule - extract route target values.
   *
   * <p>Extracts VPN target (route target) from "vpn-target <rt> import/export/both" command.
   */
  @Override
  public void exitVrf_vpn_target(Vrf_vpn_targetContext ctx) {
    if (_currentVrf == null || ctx.rt_value == null) {
      return;
    }

    String rt = ctx.rt_value.getText();

    // Determine if this is import, export, or both
    boolean isImport = ctx.IMPORT() != null || ctx.BOTH() != null;
    boolean isExport = ctx.EXPORT() != null || ctx.BOTH() != null;

    if (isImport) {
      _currentVrf.addImportRouteTarget(rt);
    }
    if (isExport) {
      _currentVrf.addExportRouteTarget(rt);
    }
  }

  /**
   * Process exit from s_vrf rule - clear current VRF context.
   *
   * <p>Called when exiting a VRF configuration block.
   */
  @Override
  public void exitS_vrf(S_vrfContext ctx) {
    _currentVrf = null;
  }
}
