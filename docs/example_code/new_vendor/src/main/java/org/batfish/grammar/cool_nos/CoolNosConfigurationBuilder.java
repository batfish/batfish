package org.batfish.grammar.cool_nos;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.Range;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.BatfishCombinedParser;
import org.batfish.grammar.SilentSyntaxListener;
import org.batfish.grammar.UnrecognizedLineToken;
import org.batfish.grammar.cool_nos.CoolNosParser.Host_nameContext;
import org.batfish.grammar.cool_nos.CoolNosParser.Interface_nameContext;
import org.batfish.grammar.cool_nos.CoolNosParser.Ipv4_addressContext;
import org.batfish.grammar.cool_nos.CoolNosParser.Ipv4_prefixContext;
import org.batfish.grammar.cool_nos.CoolNosParser.S_lineContext;
import org.batfish.grammar.cool_nos.CoolNosParser.Ss_addContext;
import org.batfish.grammar.cool_nos.CoolNosParser.Ss_deleteContext;
import org.batfish.grammar.cool_nos.CoolNosParser.Ss_disableContext;
import org.batfish.grammar.cool_nos.CoolNosParser.Ss_enableContext;
import org.batfish.grammar.cool_nos.CoolNosParser.Ss_modifyContext;
import org.batfish.grammar.cool_nos.CoolNosParser.Ssa_discardContext;
import org.batfish.grammar.cool_nos.CoolNosParser.Ssa_gatewayContext;
import org.batfish.grammar.cool_nos.CoolNosParser.Ssa_interfaceContext;
import org.batfish.grammar.cool_nos.CoolNosParser.Ssy_host_nameContext;
import org.batfish.grammar.cool_nos.CoolNosParser.StringContext;
import org.batfish.grammar.cool_nos.CoolNosParser.Uint16Context;
import org.batfish.grammar.cool_nos.CoolNosParser.Uint8Context;
import org.batfish.grammar.cool_nos.CoolNosParser.Vlan_numberContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.vendor.cool_nos.CoolNosConfiguration;
import org.batfish.vendor.cool_nos.NextHop;
import org.batfish.vendor.cool_nos.NextHopDiscard;
import org.batfish.vendor.cool_nos.NextHopGateway;
import org.batfish.vendor.cool_nos.NextHopInterface;
import org.batfish.vendor.cool_nos.StaticRoute;

@ParametersAreNonnullByDefault
public final class CoolNosConfigurationBuilder extends CoolNosParserBaseListener
    implements SilentSyntaxListener {

  public CoolNosConfigurationBuilder(
      CoolNosCombinedParser parser,
      String text,
      Warnings warnings,
      SilentSyntaxCollection silentSyntax) {
    _parser = parser;
    _text = text;
    _c = new CoolNosConfiguration();
    _c.setExtraLines(_parser.getExtraLines());
    _w = warnings;
    _silentSyntax = silentSyntax;
  }

  @Override
  public void exitSsy_host_name(Ssy_host_nameContext ctx) {
    toString(ctx, ctx.hostname).ifPresent(_c::setHostname);
  }

  @Override
  public void enterSs_add(Ss_addContext ctx) {
    Prefix prefix = toPrefix(ctx.prefix);
    _currentStaticRoute = new StaticRoute();
    if (_c.getStaticRoutes().containsKey(prefix)) {
      warn(ctx, String.format("Attempt to redefine existing static route for prefix %s", prefix));
      return;
    }
    _c.getStaticRoutes().put(prefix, _currentStaticRoute);
  }

  @Override
  public void exitSs_add(Ss_addContext ctx) {
    if (_currentNextHop == null) {
      // invalid next hop, so remove the route we added
      _c.getStaticRoutes().remove(toPrefix(ctx.prefix));
    } else {
      _currentStaticRoute.setNextHop(_currentNextHop);
    }
    _currentNextHop = null;
    _currentStaticRoute = null;
  }

  @Override
  public void enterSs_modify(Ss_modifyContext ctx) {
    Prefix prefix = toPrefix(ctx.prefix);
    if (!_c.getStaticRoutes().containsKey(prefix)) {
      warn(ctx, String.format("Attempt to modify non-existent static route for prefix %s", prefix));
      // set to a dummy so modification further down the parse tree does not NPE
      _currentStaticRoute = new StaticRoute();
      return;
    }
    _currentStaticRoute = _c.getStaticRoutes().get(prefix);
  }

  @Override
  public void exitSs_modify(Ss_modifyContext ctx) {
    if (_currentNextHop != null) {
      _currentStaticRoute.setNextHop(_currentNextHop);
    }
    _currentNextHop = null;
    _currentStaticRoute = null;
  }

  @Override
  public void exitSsa_discard(Ssa_discardContext ctx) {
    _currentNextHop = NextHopDiscard.instance();
  }

  @Override
  public void exitSsa_gateway(Ssa_gatewayContext ctx) {
    _currentNextHop = new NextHopGateway(toIp(ctx.ip));
  }

  @Override
  public void exitSsa_interface(Ssa_interfaceContext ctx) {
    toString(ctx, ctx.interface_name())
        .ifPresent(name -> _currentNextHop = new NextHopInterface(name));
  }

  @Override
  public void exitSs_delete(Ss_deleteContext ctx) {
    Prefix prefix = toPrefix(ctx.prefix);
    if (!_c.getStaticRoutes().containsKey(prefix)) {
      warn(
          ctx, String.format("Attempt to delete non-existent static route with prefix %s", prefix));
      return;
    }
    _c.getStaticRoutes().remove(prefix);
  }

  @Override
  public void exitSs_enable(Ss_enableContext ctx) {
    _currentStaticRoute.setEnable(true);
  }

  @Override
  public void exitSs_disable(Ss_disableContext ctx) {
    _currentStaticRoute.setEnable(false);
  }

  @Override
  public void exitS_line(S_lineContext ctx) {
    todo(ctx);
  }

  private @Nonnull Optional<String> toString(
      ParserRuleContext messageCtx, Interface_nameContext ctx) {
    if (ctx.ETHERNET() != null) {
      return Optional.of(String.format("ethernet %d", toInteger(ctx.ethernet_num)));
    } else {
      assert ctx.VLAN() != null;
      Optional<Integer> maybeVlan = toInteger(messageCtx, ctx.vlan);
      if (!maybeVlan.isPresent()) {
        // already warned
        return Optional.empty();
      }
      return Optional.of(String.format("vlan %d", maybeVlan.get()));
    }
  }

  private static int toInteger(Uint8Context ctx) {
    return Integer.parseInt(ctx.getText());
  }

  private @Nonnull Optional<Integer> toInteger(
      ParserRuleContext messageCtx, Vlan_numberContext ctx) {
    return toIntegerInSpace(messageCtx, ctx.uint16(), VLAN_NUMBER_RANGE, "vlan number");
  }

  private static @Nonnull Ip toIp(Ipv4_addressContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private static @Nonnull Prefix toPrefix(Ipv4_prefixContext ctx) {
    return Prefix.parse(ctx.getText());
  }

  private @Nonnull Optional<String> toString(ParserRuleContext messageCtx, Host_nameContext ctx) {
    if (!toStringWithLengthInSpace(messageCtx, ctx.string(), HOSTNAME_LENGTH_RANGE, "hostname")
        .isPresent()) {
      // already warned
      return Optional.empty();
    }
    return toStringMatchingPattern(messageCtx, ctx.string(), HOSTNAME_PATTERN, "hostname");
  }

  /**
   * Return the text of the provided {@code ctx} if its length is within the provided {@link
   * IntegerSpace lengthSpace}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<String> toStringWithLengthInSpace(
      ParserRuleContext messageCtx, StringContext ctx, IntegerSpace lengthSpace, String name) {
    String text = unquote(ctx.getText());
    if (!lengthSpace.contains(text.length())) {
      warn(
          messageCtx,
          String.format(
              "Expected %s with length in range %s, but got '%s'", name, lengthSpace, text));
      return Optional.empty();
    }
    return Optional.of(text);
  }

  /**
   * Return the text of the provided {@code ctx} if it is matched by the provided {@link Pattern
   * lengthSpace}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<String> toStringMatchingPattern(
      ParserRuleContext messageCtx, StringContext ctx, Pattern pattern, String name) {
    String text = unquote(ctx.getText());
    if (!pattern.matcher(text).matches()) {
      warn(
          messageCtx,
          String.format("Invalid %s '%s' does not match regex: %s", name, text, pattern));
      return Optional.empty();
    }
    return Optional.of(text);
  }

  /**
   * Convert a {@link Uint16Context} to an {@link Integer} if it is contained in the provided {@code
   * space}, or else {@link Optional#empty}.
   */
  private @Nonnull Optional<Integer> toIntegerInSpace(
      ParserRuleContext messageCtx, Uint16Context ctx, IntegerSpace space, String name) {
    return toIntegerInSpace_helper(messageCtx, ctx, space, name);
  }

  /**
   * Convert a {@link ParserRuleContext} whose text is guaranteed to represent a valid signed 32-bit
   * decimal integer to an {@link Integer} if it is contained in the provided {@code space}, or else
   * {@link Optional#empty}.
   *
   * <p>This function should only be called by more strictly typed overloads of {@code
   * toIntegerSpace}.
   */
  private @Nonnull Optional<Integer> toIntegerInSpace_helper(
      ParserRuleContext messageCtx, ParserRuleContext ctx, IntegerSpace space, String name) {
    int num = Integer.parseInt(ctx.getText());
    if (!space.contains(num)) {
      warn(messageCtx, String.format("Expected %s in range %s, but got '%d'", name, space, num));
      return Optional.empty();
    }
    return Optional.of(num);
  }

  private static @Nonnull String unquote(String text) {
    if (text.isEmpty()) {
      return text;
    }
    if (text.charAt(0) != '"') {
      return text;
    }
    // Here for sanity, but should not trigger unless the definition of string rule is broken.
    checkArgument(text.charAt(text.length() - 1) == '"', "Improperly-quoted string: %s", text);
    return text.substring(1, text.length() - 1);
  }

  @Override
  public void visitErrorNode(ErrorNode errorNode) {
    Token token = errorNode.getSymbol();
    int line = token.getLine();
    String lineText = errorNode.getText().replace("\n", "").replace("\r", "").trim();
    _c.setUnrecognized(true);

    if (token instanceof UnrecognizedLineToken) {
      UnrecognizedLineToken unrecToken = (UnrecognizedLineToken) token;
      _w.getParseWarnings()
          .add(
              new ParseWarning(
                  line, lineText, unrecToken.getParserContext(), "This syntax is unrecognized"));
    } else {
      _w.redFlagf(
          "Unrecognized Line: %d: %s SUBSEQUENT LINES MAY NOT BE PROCESSED CORRECTLY",
          line, lineText);
    }
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

  public @Nonnull CoolNosConfiguration getConfiguration() {
    return _c;
  }

  private final @Nonnull CoolNosCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull CoolNosConfiguration _c;
  private final @Nonnull Warnings _w;
  private final @Nonnull SilentSyntaxCollection _silentSyntax;

  private StaticRoute _currentStaticRoute;
  private NextHop _currentNextHop;

  private static final IntegerSpace HOSTNAME_LENGTH_RANGE = IntegerSpace.of(Range.closed(1, 32));
  private static final IntegerSpace VLAN_NUMBER_RANGE = IntegerSpace.of(Range.closed(1, 4094));
  private static final Pattern HOSTNAME_PATTERN =
      Pattern.compile("[-A-Za-z0-9]+(\\.[-A-Za-z0-9]+)*");
}
