package org.batfish.grammar.huawei;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.huawei.HuaweiParser.Description_lineContext;
import org.batfish.grammar.huawei.HuaweiParser.If_dot1q_terminationContext;
import org.batfish.grammar.huawei.HuaweiParser.If_ip_addressContext;
import org.batfish.grammar.huawei.HuaweiParser.If_shutdownContext;
import org.batfish.grammar.huawei.HuaweiParser.S_interfaceContext;
import org.batfish.grammar.huawei.HuaweiParser.S_returnContext;
import org.batfish.grammar.huawei.HuaweiParser.S_static_routeContext;
import org.batfish.grammar.huawei.HuaweiParser.S_sysnameContext;
import org.batfish.grammar.huawei.HuaweiParser.S_vlanContext;
import org.batfish.grammar.huawei.HuaweiParser.V_descriptionContext;
import org.batfish.grammar.huawei.HuaweiParser.V_nameContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.representation.huawei.HuaweiConfiguration;
import org.batfish.representation.huawei.HuaweiInterface;
import org.batfish.representation.huawei.HuaweiStaticRoute;
import org.batfish.representation.huawei.HuaweiVlan;
import org.batfish.vendor.VendorConfiguration;

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
  private final SilentSyntaxCollection _silentSyntax;
  private String _currentInterfaceName;

  public HuaweiControlPlaneExtractor(
      String text, HuaweiCombinedParser parser, Warnings w, SilentSyntaxCollection silentSyntax) {
    _text = text;
    _parser = parser;
    _w = w;
    _silentSyntax = silentSyntax;
    _configuration = new HuaweiConfiguration();
    _currentInterfaceName = null;
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
  }

  /**
   * Process exit from s_vlan rule - extract VLAN configuration.
   *
   * <p>Extracts VLAN ID from "vlan <id>" command and creates HuaweiVlan object. For "vlan batch"
   * commands, creates multiple VLANs.
   */
  @Override
  public void exitS_vlan(S_vlanContext ctx) {
    // Handle "vlan batch" command (create multiple VLANs)
    if (ctx.vlan_batch_range() != null) {
      // Iterate through all uint8 contexts in vlan_batch_range
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
    // Handle individual "vlan <id>" command
    else if (ctx.vlan_id != null) {
      try {
        int vlanId = Integer.parseInt(ctx.vlan_id.getText());
        HuaweiVlan vlan = _configuration.getVlan(vlanId);
        if (vlan == null) {
          vlan = new HuaweiVlan(vlanId);
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
    // Similar to v_name, this requires tracking the current VLAN context
    // For now, this is a stub
  }

  /**
   * Process exit from if_dot1q_termination rule - extract subinterface VLAN assignment.
   *
   * <p>Extracts VLAN ID from "dot1q termination vid <vid>" command on subinterfaces.
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
      int vid = Integer.parseInt(ctx.vid.getText());
      // Store the VLAN ID for this subinterface
      // This can be used later to associate the subinterface with a VLAN
      // For now, we just note it - the actual VLAN-to-subinterface mapping
      // will be done during conversion to Batfish model
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
}
