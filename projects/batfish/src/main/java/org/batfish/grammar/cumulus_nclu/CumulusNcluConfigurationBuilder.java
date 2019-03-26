package org.batfish.grammar.cumulus_nclu;

import static com.google.common.base.Preconditions.checkState;

import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_bgpContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_bondContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_bridgeContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_dnsContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_hostnameContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_interfaceContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_loopbackContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_routingContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_timeContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_vlanContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_vrfContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_vxlanContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Cumulus_nclu_configurationContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.S_extra_configurationContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.S_net_add_unrecognizedContext;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;

/**
 * A listener that builds a {@link CumulusNcluConfiguration} while walking a parse tree produced by
 * {@link CumulusNcluCombinedParser#parse}.
 */
public class CumulusNcluConfigurationBuilder extends CumulusNcluParserBaseListener {

  private @Nullable CumulusNcluConfiguration _c;
  private final @Nonnull CumulusNcluCombinedParser _parser;
  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;

  public CumulusNcluConfigurationBuilder(
      CumulusNcluCombinedParser parser, String text, Warnings w) {
    _parser = parser;
    _text = text;
    _w = w;
  }

  @SuppressWarnings("unused")
  private @Nullable <T, U extends T> T convProblem(
      Class<T> returnType, ParserRuleContext ctx, @Nullable U defaultReturnValue) {
    _w.redFlag(
        String.format("Could not convert to %s: %s", returnType.getSimpleName(), getFullText(ctx)));
    return defaultReturnValue;
  }

  @Override
  public void enterCumulus_nclu_configuration(Cumulus_nclu_configurationContext ctx) {
    _c = new CumulusNcluConfiguration();
  }

  @Override
  public void exitA_bgp(A_bgpContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_bond(A_bondContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_bridge(A_bridgeContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_dns(A_dnsContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_hostname(A_hostnameContext ctx) {
    _c.setHostname(ctx.hostname.getText());
  }

  @Override
  public void exitA_interface(A_interfaceContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_loopback(A_loopbackContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_routing(A_routingContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_time(A_timeContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_vlan(A_vlanContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_vrf(A_vrfContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitA_vxlan(A_vxlanContext ctx) {
    todo(ctx);
  }

  @Override
  public void exitS_extra_configuration(S_extra_configurationContext ctx) {
    unrecognized(ctx);
  }

  @Override
  public void exitS_net_add_unrecognized(S_net_add_unrecognizedContext ctx) {
    unrecognized(ctx);
  }

  /**
   * Returns built {@link CumulusNcluConfiguration}.
   *
   * @throws IllegalStateException if called before walking parse tree produced by {@link
   *     CumulusNcluCombinedParser#parse}
   */
  public @Nonnull CumulusNcluConfiguration getConfiguration() {
    checkState(
        _c != null,
        "Cannot return vendor configuration before walking valid Cumulus NCLU parse tree");
    return _c;
  }

  private @Nonnull String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    String text = _text.substring(start, end + 1);
    return text;
  }

  @SuppressWarnings("unused")
  private void todo(ParserRuleContext ctx) {
    _w.todo(ctx, getFullText(ctx), _parser);
  }

  private void unrecognized(ParserRuleContext ctx) {
    _w.getParseWarnings()
        .add(
            new ParseWarning(
                ctx.getStart().getLine(),
                getFullText(ctx),
                ctx.toString(Arrays.asList(_parser.getParser().getRuleNames())),
                "This syntax is unrecognized"));
    _c.setUnrecognized(true);
  }
}
