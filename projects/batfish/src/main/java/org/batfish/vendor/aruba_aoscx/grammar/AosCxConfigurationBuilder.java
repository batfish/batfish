package org.batfish.vendor.aruba_aoscx.grammar;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.Interface_nameContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_hostnameContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_interfaceContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_addressContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_ospf_areaContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_ospf_networkContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_ip_routeContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_no_shutdownContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_router_idContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_router_ospfContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_shutdownContext;
import org.batfish.vendor.aruba_aoscx.grammar.AosCxParser.S_speedContext;
import org.batfish.vendor.aruba_aoscx.representation.AosCxConfiguration;
import org.batfish.vendor.aruba_aoscx.representation.AosCxInterface;
import org.batfish.vendor.aruba_aoscx.representation.AosCxInterface.OspfNetworkType;
import org.batfish.vendor.aruba_aoscx.representation.AosCxOspfProcess;
import org.batfish.vendor.aruba_aoscx.representation.AosCxStaticRoute;
import org.batfish.vendor.aruba_aoscx.representation.AosCxStaticRoute.NextHopType;

@ParametersAreNonnullByDefault
public final class AosCxConfigurationBuilder extends AosCxParserBaseListener
    implements SilentSyntaxListener {

  public AosCxConfigurationBuilder(
      AosCxCombinedParser parser,
      String text,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _parser = parser;
    _text = text;
    _w = warnings;
    _silentSyntax = silentSyntax;
    _configuration = new AosCxConfiguration();
  }

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    _configuration.setHostname(ctx.WORD().getText());
  }

  @Override
  public void exitS_interface(S_interfaceContext ctx) {
    String name = toInterfaceName(ctx.interface_name());
    _currentInterface = _configuration.getOrCreateInterface(name);
  }

  @Override
  public void exitS_ip_address(S_ip_addressContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring IP address outside interface context");
      return;
    }
    _currentInterface.setAddress(ConcreteInterfaceAddress.parse(ctx.WORD().getText()));
  }

  @Override
  public void exitS_ip_ospf_area(S_ip_ospf_areaContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring OSPF area command outside interface context");
      return;
    }
    _currentInterface.setOspfProcessId(Integer.parseInt(ctx.WORD(0).getText()));
    _currentInterface.setOspfArea(ctx.WORD(1).getText());
  }

  @Override
  public void exitS_ip_ospf_network(S_ip_ospf_networkContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring OSPF network command outside interface context");
      return;
    }
    _currentInterface.setOspfNetworkType(OspfNetworkType.POINT_TO_POINT);
  }

  @Override
  public void exitS_ip_route(S_ip_routeContext ctx) {
    Prefix prefix = Prefix.parse(ctx.WORD().getText());
    String nextHop = ctx.static_route_next_hop().getText();

    NextHopType nextHopType;
    if (ctx.static_route_next_hop().NULLROUTE() != null) {
      nextHopType = NextHopType.NULL_ROUTE;
    } else if (ctx.static_route_next_hop().REJECT() != null) {
      nextHopType = NextHopType.REJECT;
    } else if (Ip.tryParse(nextHop).isPresent()) {
      nextHopType = NextHopType.IP;
    } else {
      nextHopType = NextHopType.INTERFACE;
    }

    _configuration
        .getStaticRoutes()
        .add(new AosCxStaticRoute(prefix, nextHopType, nextHop));
  }

  @Override
  public void exitS_router_ospf(S_router_ospfContext ctx) {
    int processId = Integer.parseInt(ctx.WORD().getText());
    _currentOspfProcess = _configuration.getOrCreateOspfProcess(processId);
    _currentInterface = null;
  }

  @Override
  public void exitS_router_id(S_router_idContext ctx) {
    if (_currentOspfProcess == null) {
      warn(ctx, "Ignoring router-id outside OSPF context");
      return;
    }
    _currentOspfProcess.setRouterId(Ip.parse(ctx.WORD().getText()));
  }

  @Override
  public void exitS_no_shutdown(S_no_shutdownContext ctx) {
    if (_currentInterface != null) {
      _currentInterface.setEnabled(true);
    }
  }

  @Override
  public void exitS_shutdown(S_shutdownContext ctx) {
    if (_currentInterface != null) {
      _currentInterface.setEnabled(false);
    }
  }

  @Override
  public void exitS_speed(S_speedContext ctx) {
    if (_currentInterface == null) {
      warn(ctx, "Ignoring speed command outside interface context");
      return;
    }

    String speed = ctx.WORD(0).getText();

    // Auto-negotiation does not tell us the actual negotiated bandwidth.
    if (speed.equals("auto")) {
      return;
    }

    Double bandwidth =
        switch (speed) {
          case "10-half", "10-full" -> 10_000_000D;
          case "100-half", "100-full" -> 100_000_000D;
          case "1000-full", "1g" -> 1_000_000_000D;
          case "2.5g" -> 2_500_000_000D;
          case "5g" -> 5_000_000_000D;
          case "10g" -> 10_000_000_000D;
          case "25g" -> 25_000_000_000D;
          case "40g" -> 40_000_000_000D;
          case "50g" -> 50_000_000_000D;
          case "100g" -> 100_000_000_000D;
          case "200g" -> 200_000_000_000D;
          case "400g" -> 400_000_000_000D;
          default -> null;
        };

    if (bandwidth != null) {
      _currentInterface.setBandwidth(bandwidth);
    }
  }

  private static String toInterfaceName(Interface_nameContext ctx) {
    String id = ctx.WORD().getText();
    if (ctx.LOOPBACK() != null) {
      return "loopback " + id;
    }
    if (ctx.VLAN() != null) {
      return "vlan " + id;
    }
    return id;
  }

  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    tryProcessSilentSyntax(ctx);
  }

  @Override
  public @Nonnull SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  @Override
  public @Nonnull String getInputText() {
    return _text;
  }

  @Override
  public @Nonnull BatfishCombinedParser<?, ?> getParser() {
    return _parser;
  }

  @Override
  public @Nonnull Warnings getWarnings() {
    return _w;
  }

  public @Nonnull AosCxConfiguration getConfiguration() {
    return _configuration;
  }

  private final @Nonnull AosCxConfiguration _configuration;
  private final @Nonnull AosCxCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;
  private AosCxInterface _currentInterface;
  private AosCxOspfProcess _currentOspfProcess;
}
