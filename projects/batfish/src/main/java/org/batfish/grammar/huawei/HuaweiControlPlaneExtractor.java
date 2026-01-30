package org.batfish.grammar.huawei;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.Warnings;
import org.batfish.grammar.ControlPlaneExtractor;
import org.batfish.grammar.huawei.HuaweiParser.Description_lineContext;
import org.batfish.grammar.huawei.HuaweiParser.If_ip_addressContext;
import org.batfish.grammar.huawei.HuaweiParser.If_shutdownContext;
import org.batfish.grammar.huawei.HuaweiParser.S_interfaceContext;
import org.batfish.grammar.huawei.HuaweiParser.S_returnContext;
import org.batfish.grammar.huawei.HuaweiParser.S_sysnameContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.representation.huawei.HuaweiConfiguration;
import org.batfish.representation.huawei.HuaweiInterface;
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
}
