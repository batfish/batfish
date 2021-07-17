package org.batfish.vendor.check_point_gateway.grammar;

import static org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayLexer.WORD;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Check_point_gateway_configurationContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Double_quoted_stringContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.HostnameContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Interface_nameContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Ip_addressContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Ip_mask_lengthContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Ip_prefixContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Link_speedContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.MtuContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.On_or_offContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Quoted_textContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.S_hostnameContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.S_interfaceContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.S_static_routeContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Si_auto_negotiationContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Si_commentsContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Si_ipv4_addressContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Si_link_speedContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Si_mtuContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Si_stateContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Siia_maskContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Single_quoted_stringContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Ssr_commentContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Ssr_nexthopContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.SsrnContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.SsrngContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Ssrng_priorityContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Static_route_commentContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Static_route_prefixContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Static_route_priorityContext;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Uint16Context;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Uint32Context;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.Uint8Context;
import org.batfish.vendor.check_point_gateway.grammar.CheckPointGatewayParser.WordContext;
import org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConfiguration;
import org.batfish.vendor.check_point_gateway.representation.Interface;
import org.batfish.vendor.check_point_gateway.representation.Interface.LinkSpeed;
import org.batfish.vendor.check_point_gateway.representation.Nexthop;
import org.batfish.vendor.check_point_gateway.representation.NexthopAddress;
import org.batfish.vendor.check_point_gateway.representation.NexthopBlackhole;
import org.batfish.vendor.check_point_gateway.representation.NexthopLogical;
import org.batfish.vendor.check_point_gateway.representation.NexthopReject;
import org.batfish.vendor.check_point_gateway.representation.NexthopTarget;
import org.batfish.vendor.check_point_gateway.representation.StaticRoute;

@ParametersAreNonnullByDefault
public class CheckPointGatewayConfigurationBuilder extends CheckPointGatewayParserBaseListener
    implements SilentSyntaxListener {

  public CheckPointGatewayConfigurationBuilder(
      CheckPointGatewayCombinedParser parser,
      String text,
      Warnings warnings,
      CheckPointGatewayConfiguration configuration,
      SilentSyntaxCollection silentSyntax) {
    _parser = parser;
    _text = text;
    _w = warnings;
    _configuration = configuration;
    _silentSyntax = silentSyntax;
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    int line = token.getLine();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    _configuration.setUnrecognized(true);

    if (token instanceof UnrecognizedLineToken) {
      UnrecognizedLineToken unrecToken = (UnrecognizedLineToken) token;
      _w.getParseWarnings()
          .add(
              new ParseWarning(
                  line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
    } else {
      _w.redFlag(
          String.format(
              "Unrecognized Line: %d: %s SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY",
              line, lineText));
    }
  }

  @Nonnull
  public CheckPointGatewayConfiguration getConfiguration() {
    return _configuration;
  }

  @Nonnull
  @Override
  public String getInputText() {
    return _text;
  }

  @Nonnull
  @Override
  public BatfishCombinedParser<?, ?> getParser() {
    return _parser;
  }

  @Nonnull
  @Override
  public SilentSyntaxCollection getSilentSyntax() {
    return _silentSyntax;
  }

  @Nonnull
  @Override
  public Warnings getWarnings() {
    return _w;
  }

  @Override
  public void enterCheck_point_gateway_configuration(
      Check_point_gateway_configurationContext ctx) {}

  @Override
  public void exitS_hostname(S_hostnameContext ctx) {
    toString(ctx, ctx.hostname()).ifPresent(_configuration::setHostname);
  }

  @Override
  public void enterS_interface(S_interfaceContext ctx) {
    _currentInterface =
        toString(ctx, ctx.interface_name())
            .map(n -> _configuration.getInterfaces().computeIfAbsent(n, Interface::new))
            .orElse(new Interface(ctx.interface_name().getText()));
  }

  @Override
  public void exitS_interface(S_interfaceContext ctx) {
    _currentInterface = null;
  }

  @Override
  public void exitSi_auto_negotiation(Si_auto_negotiationContext ctx) {
    _currentInterface.setAutoNegotiate(toBoolean(ctx.on_or_off()));
  }

  @Override
  public void exitSi_comments(Si_commentsContext ctx) {
    _currentInterface.setComments(toString(ctx.word()));
  }

  @Override
  public void exitSi_ipv4_address(Si_ipv4_addressContext ctx) {
    Ip ip = toIp(ctx.ip_address());
    Optional<Integer> subnetBits = toSubnetBits(ctx, ctx.siia_mask());
    subnetBits.ifPresent(
        sb -> _currentInterface.setAddress(ConcreteInterfaceAddress.create(ip, sb)));
  }

  @Override
  public void exitSi_link_speed(Si_link_speedContext ctx) {
    _currentInterface.setLinkSpeed(toLinkSpeed(ctx.link_speed()));
  }

  @Override
  public void exitSi_mtu(Si_mtuContext ctx) {
    toInteger(ctx, ctx.mtu()).ifPresent(m -> _currentInterface.setMtu(m));
  }

  @Override
  public void exitSi_state(Si_stateContext ctx) {
    _currentInterface.setState(toBoolean(ctx.on_or_off()));
  }

  @Override
  public void enterS_static_route(S_static_routeContext ctx) {
    Optional<Prefix> prefix = toPrefix(ctx, ctx.static_route_prefix());
    if (!prefix.isPresent()) {
      // Dummy
      _currentStaticRoute = new StaticRoute(Prefix.ZERO);
      return;
    }

    _currentStaticRoute =
        _configuration.getStaticRoutes().computeIfAbsent(prefix.get(), StaticRoute::new);
  }

  @Override
  public void exitS_static_route(S_static_routeContext ctx) {
    _currentStaticRoute = null;
  }

  @Override
  public void exitSsr_comment(Ssr_commentContext ctx) {
    toString(ctx, ctx.static_route_comment()).ifPresent(c -> _currentStaticRoute.setComment(c));
  }

  @Override
  public void enterSsr_nexthop(Ssr_nexthopContext ctx) {
    Optional<NexthopTarget> nexthopTarget = toNexthopTarget(ctx.ssrn());
    if (!nexthopTarget.isPresent()) {
      // Dummy
      _currentStaticRouteNextHop = new Nexthop(NexthopBlackhole.INSTANCE);
      return;
    }

    _currentStaticRouteNextHop =
        _currentStaticRoute.getNexthops().computeIfAbsent(nexthopTarget.get(), Nexthop::new);
  }

  @Override
  public void exitSsrng_priority(Ssrng_priorityContext ctx) {
    toInteger(ctx, ctx.static_route_priority())
        .ifPresent(p -> _currentStaticRouteNextHop.setPriority(p));
  }

  @Override
  public void exitSsr_nexthop(Ssr_nexthopContext ctx) {
    _currentStaticRouteNextHop = null;
  }

  private @Nonnull Optional<NexthopTarget> toNexthopTarget(SsrnContext ctx) {
    if (ctx.ssrn_blackhole() != null) {
      return Optional.of(NexthopBlackhole.INSTANCE);
    } else if (ctx.ssrn_reject() != null) {
      return Optional.of(NexthopReject.INSTANCE);
    }
    assert ctx.ssrn_gateway() != null;
    return toNexthopTarget(ctx.ssrn_gateway().ssrng());
  }

  private @Nonnull Optional<NexthopTarget> toNexthopTarget(SsrngContext ctx) {
    if (ctx.ssrng_address() != null) {
      return Optional.of(new NexthopAddress(toIp(ctx.ssrng_address().ip_address())));
    }
    assert ctx.ssrng_logical() != null;
    String iface = toString(ctx.ssrng_logical().iface);
    if (!_configuration.getInterfaces().containsKey(iface)) {
      warn(ctx, "Cannot set nexthop gateway to non-existent interface");
      return Optional.empty();
    }
    return Optional.of(new NexthopLogical(iface));
  }

  private @Nonnull Optional<Prefix> toPrefix(
      ParserRuleContext messageCtx, Static_route_prefixContext ctx) {
    if (ctx.ip_prefix() != null) {
      Prefix prefix = toPrefix(ctx.ip_prefix());
      if (prefix.equals(Prefix.ZERO)) {
        warn(
            messageCtx,
            String.format(
                "Static-route prefix %s is not valid, use the 'default' keyword instead.", prefix));
        return Optional.empty();
      }
      return Optional.of(prefix);
    }
    assert ctx.DEFAULT() != null;
    return Optional.of(Prefix.ZERO);
  }

  private @Nonnull Prefix toPrefix(Ip_prefixContext ctx) {
    return Prefix.parse(ctx.getText());
  }

  private LinkSpeed toLinkSpeed(Link_speedContext ctx) {
    if (ctx.HUNDRED_M_FULL() != null) {
      return LinkSpeed.HUNDRED_M_FULL;
    } else if (ctx.HUNDRED_M_HALF() != null) {
      return LinkSpeed.HUNDRED_M_HALF;
    } else if (ctx.TEN_M_FULL() != null) {
      return LinkSpeed.TEN_M_FULL;
    } else if (ctx.TEN_M_HALF() != null) {
      return LinkSpeed.TEN_M_HALF;
    }
    assert ctx.THOUSAND_M_FULL() != null;
    return LinkSpeed.THOUSAND_M_FULL;
  }

  private @Nonnull Optional<Integer> toInteger(ParserRuleContext messageCtx, MtuContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint16(), MTU_SPACE, "mtu");
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Static_route_priorityContext ctx) {
    return toIntegerInSpace(
        messageCtx,
        ctx.uint8(),
        STATIC_ROUTE_NEXTHOP_PRIORITY_SPACE,
        "static-route nexthop priority");
  }

  private @Nonnull Optional<Integer> toSubnetBits(
      ParserRuleContext messageCtx, Siia_maskContext ctx) {
    if (ctx.siia_mask_length() != null) {
      return toInteger(ctx, ctx.siia_mask_length().ip_mask_length());
    }
    assert ctx.siia_subnet_mask() != null;
    Ip mask = toIp(ctx.siia_subnet_mask().ip_address());
    if (!mask.isValidNetmask1sLeading()) {
      warn(messageCtx, String.format("Subnet-mask %s is not valid.", mask));
      return Optional.empty();
    }
    return Optional.of(mask.numSubnetBits());
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Ip_mask_lengthContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint8(), MASK_LENGTH_SPACE, "mask-length");
  }

  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, Uint8Context ctx, IntegerSpace space, String name) {
    return toIntegerInSpace(messageCtx, ctx.getText(), space, name);
  }

  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, Uint16Context ctx, IntegerSpace space, String name) {
    return toIntegerInSpace(messageCtx, ctx.getText(), space, name);
  }

  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, Uint32Context ctx, IntegerSpace space, String name) {
    return toIntegerInSpace(messageCtx, ctx.getText(), space, name);
  }

  /**
   * Convert a {@link String} to an {@link Integer} if it represents a number that is contained in
   * the provided {@code space}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, String str, IntegerSpace space, String name) {
    Integer num = Ints.tryParse(str);
    if (num == null || !space.contains(num)) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  private static @Nonnull Ip toIp(Ip_addressContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private boolean toBoolean(On_or_offContext ctx) {
    if (ctx.ON() != null) {
      return true;
    }
    assert ctx.OFF() != null;
    return false;
  }

  /** Handle hostname special string replacements, like {@code %m} for chassis identifier. */
  private @Nonnull String preprocessHostname(String original) {
    // Assume this is the first chassis and (?)blade
    return original.replace("%m", "ch01-01");
  }

  private @Nonnull Optional<String> toString(ParserRuleContext messageCtx, HostnameContext ctx) {
    return toString(
        messageCtx,
        ctx.word(),
        "device hostname",
        this::preprocessHostname,
        DEVICE_HOSTNAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Interface_nameContext ctx) {
    return toString(messageCtx, ctx.word(), "interface name", INTERFACE_NAME_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Static_route_commentContext ctx) {
    return toString(messageCtx, ctx.word(), "static-route comment", STATIC_ROUTE_COMMENT_PATTERN);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, WordContext ctx, String type, Pattern pattern) {
    return toString(messageCtx, ctx, type, s -> pattern.matcher(s).matches());
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx,
      WordContext ctx,
      String type,
      Function<String, String> preprocessFunction,
      Pattern pattern) {
    return toString(messageCtx, ctx, type, preprocessFunction, s -> pattern.matcher(s).matches());
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, WordContext ctx, String type, Predicate<String> predicate) {
    String text = toString(ctx);
    return toString(messageCtx, text, type, predicate);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx,
      WordContext ctx,
      String type,
      Function<String, String> preprocessFunction,
      Predicate<String> predicate) {
    String text = preprocessFunction.apply(toString(ctx));
    return toString(messageCtx, text, type, predicate);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, String text, String type, Predicate<String> predicate) {
    if (!predicate.test(text)) {
      warn(messageCtx, String.format("Illegal value for %s", type));
      return Optional.empty();
    }
    return Optional.of(text);
  }

  private static @Nonnull String toString(WordContext ctx) {
    return ctx.word_content().children.stream()
        .map(
            child -> {
              if (child instanceof Double_quoted_stringContext) {
                return toString(((Double_quoted_stringContext) child).text);
              } else if (child instanceof Single_quoted_stringContext) {
                return toString(((Single_quoted_stringContext) child).text);
              } else {
                assert child instanceof TerminalNode;
                int type = ((TerminalNode) child).getSymbol().getType();
                assert type == WORD;
                return child.getText();
              }
            })
        .collect(Collectors.joining(""));
  }

  private static @Nonnull String toString(@Nullable Quoted_textContext text) {
    if (text == null) {
      return "";
    }
    // Device appears to just remove backslashes from quoted strings
    return text.getText().replaceAll("\\\\", "");
  }

  private static final Pattern DEVICE_HOSTNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");
  // Only certain prefixes are allowed, so this is more broad than what the device accepts
  private static final Pattern INTERFACE_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9.-]+$");
  private static final Pattern STATIC_ROUTE_COMMENT_PATTERN = Pattern.compile("^[A-Za-z0-9,. ]+$");

  private static final IntegerSpace MASK_LENGTH_SPACE = IntegerSpace.of(Range.closed(1, 32));
  private static final IntegerSpace MTU_SPACE = IntegerSpace.of(Range.closed(68, 16000));
  private static final IntegerSpace STATIC_ROUTE_NEXTHOP_PRIORITY_SPACE =
      IntegerSpace.of(Range.closed(1, 8));

  private Interface _currentInterface;

  private StaticRoute _currentStaticRoute;

  private Nexthop _currentStaticRouteNextHop;

  @Nonnull private CheckPointGatewayConfiguration _configuration;

  @Nonnull private CheckPointGatewayCombinedParser _parser;

  @Nonnull private final String _text;

  @Nonnull private final Warnings _w;

  @Nonnull private final SilentSyntaxCollection _silentSyntax;
}
