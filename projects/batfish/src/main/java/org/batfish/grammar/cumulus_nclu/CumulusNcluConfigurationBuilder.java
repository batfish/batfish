package org.batfish.grammar.cumulus_nclu;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.batfish.common.Warnings;
import org.batfish.common.Warnings.ParseWarning;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Ip;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_bgpContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_bondContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_bridgeContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_hostnameContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_interfaceContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_loopbackContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_routingContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_timeContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_vlanContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_vrfContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.A_vxlanContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Bob_accessContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Bob_vidsContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Bobo_slavesContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Bond_clag_idContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Cumulus_nclu_configurationContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Dn4Context;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.GlobContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Glob_range_setContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Ip_addressContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.RangeContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Range_setContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.S_extra_configurationContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.S_net_add_unrecognizedContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Uint16Context;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Vlan_idContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Vlan_rangeContext;
import org.batfish.grammar.cumulus_nclu.CumulusNcluParser.Vlan_range_setContext;
import org.batfish.representation.cumulus.Bond;
import org.batfish.representation.cumulus.CumulusNcluConfiguration;
import org.batfish.representation.cumulus.CumulusStructureType;
import org.batfish.representation.cumulus.CumulusStructureUsage;
import org.batfish.representation.cumulus.Interface;

/**
 * A listener that builds a {@link CumulusNcluConfiguration} while walking a parse tree produced by
 * {@link CumulusNcluCombinedParser#parse}.
 */
public class CumulusNcluConfigurationBuilder extends CumulusNcluParserBaseListener {

  private static final Pattern NUMBERED_WORD_PATTERN = Pattern.compile("^(.*[^0-9])([0-9]+)$");

  private static int toInteger(Uint16Context ctx) {
    return Integer.parseInt(ctx.getText(), 10);
  }

  private static int toInteger(Vlan_idContext ctx) {
    return Integer.parseInt(ctx.getText(), 10);
  }

  private static @Nonnull Ip toIp(Ip_addressContext ctx) {
    return Ip.parse(ctx.getText());
  }

  private static @Nonnull Range<Integer> toRange(RangeContext ctx) {
    int low = toInteger(ctx.low);
    int high = ctx.high != null ? toInteger(ctx.high) : low;
    return Range.closed(low, high);
  }

  private static @Nonnull Range<Integer> toRange(Vlan_rangeContext ctx) {
    int low = toInteger(ctx.low);
    int high = ctx.high != null ? toInteger(ctx.high) : low;
    return Range.closed(low, high);
  }

  private static @Nonnull RangeSet<Integer> toRangeSet(Range_setContext ctx) {
    return ctx.range().stream()
        .map(CumulusNcluConfigurationBuilder::toRange)
        .collect(ImmutableRangeSet.toImmutableRangeSet());
  }

  private static @Nonnull RangeSet<Integer> toRangeSet(Vlan_range_setContext ctx) {
    return ctx.vlan_range().stream()
        .map(CumulusNcluConfigurationBuilder::toRange)
        .collect(ImmutableRangeSet.toImmutableRangeSet());
  }

  private static @Nonnull Set<String> toStrings(Glob_range_setContext ctx) {
    if (ctx.unnumbered != null) {
      return ImmutableSet.of(ctx.unnumbered.getText());
    }
    String baseWord = ctx.base_word.getText();
    if (ctx.first_interval_end == null && ctx.other_numeric_ranges == null) {
      return ImmutableSet.of(baseWord);
    }
    Matcher matcher = NUMBERED_WORD_PATTERN.matcher(baseWord);
    boolean matches = matcher.matches();
    assert matches; // parser+lexer should ensure this
    String prefix = matcher.group(1);
    int firstIntervalStart = Integer.parseInt(matcher.group(2), 10);
    int firstIntervalEnd =
        ctx.first_interval_end != null
            ? Integer.parseInt(ctx.first_interval_end.getText(), 10)
            : firstIntervalStart;
    // add first interval
    ImmutableRangeSet.Builder<Integer> builder =
        ImmutableRangeSet.<Integer>builder()
            .add(Range.closed(firstIntervalStart, firstIntervalEnd));
    if (ctx.other_numeric_ranges != null) {
      // add other intervals
      builder.addAll(toRangeSet(ctx.other_numeric_ranges));
    }
    return builder.build().asRanges().stream()
        .flatMapToInt(r -> IntStream.rangeClosed(r.lowerEndpoint(), r.upperEndpoint()))
        .mapToObj(i -> String.format("%s%d", prefix, i))
        .collect(ImmutableSet.toImmutableSet());
  }

  private static @Nonnull Set<String> toStrings(GlobContext ctx) {
    return ctx.glob_range_set().stream()
        .flatMap(grs -> toStrings(grs).stream())
        .collect(ImmutableSet.toImmutableSet());
  }

  private @Nullable CumulusNcluConfiguration _c;
  private @Nullable Bond _currentBond;
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
  public void enterA_bond(A_bondContext ctx) {
    String name = ctx.name.getText();
    int line = ctx.getStart().getLine();
    _c.defineStructure(CumulusStructureType.BOND, name, line);
    _c.referenceStructure(
        CumulusStructureType.BOND, name, CumulusStructureUsage.BOND_SELF_REFERENCE, line);
    _currentBond = _c.getBonds().computeIfAbsent(name, Bond::new);
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
    _currentBond = null;
  }

  @Override
  public void exitA_bridge(A_bridgeContext ctx) {
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
  public void exitBob_access(Bob_accessContext ctx) {
    _currentBond.getBridge().setAccess(toInteger(ctx.vlan));
  }

  @Override
  public void exitBob_vids(Bob_vidsContext ctx) {
    _currentBond.getBridge().setVids(IntegerSpace.of(toRangeSet(ctx.vlans)));
  }

  @Override
  public void exitBobo_slaves(Bobo_slavesContext ctx) {
    Set<String> slaves = toStrings(ctx.slaves);
    int line = ctx.getStart().getLine();
    slaves.forEach(slave -> initInterfaceIfAbsent(slave, line));
    slaves.forEach(
        slave ->
            _c.referenceStructure(
                CumulusStructureType.INTERFACE, slave, CumulusStructureUsage.BOND_SLAVE, line));
    _currentBond.setSlaves(slaves);
  }

  @Override
  public void exitBond_clag_id(Bond_clag_idContext ctx) {
    _currentBond.setClagId(toInteger(ctx.id));
  }

  @Override
  public void exitDn4(Dn4Context ctx) {
    _c.getIpv4Nameservers().add(toIp(ctx.address));
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

  private void initInterfaceIfAbsent(String name, int line) {
    if (_c.getInterfaces().containsKey(name)) {
      return;
    }
    _c.getInterfaces().computeIfAbsent(name, Interface::new);
    _c.defineStructure(CumulusStructureType.INTERFACE, name, line);
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
