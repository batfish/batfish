package org.batfish.grammar.f5_bigip_imish;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.Warnings;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.grammar.f5_bigip_imish.F5BigipImishParser.F5_bigip_imish_configurationContext;
import org.batfish.grammar.f5_bigip_imish.F5BigipImishParser.Ip_specContext;
import org.batfish.grammar.f5_bigip_imish.F5BigipImishParser.Line_actionContext;
import org.batfish.grammar.f5_bigip_imish.F5BigipImishParser.Rmm_ip_addressContext;
import org.batfish.grammar.f5_bigip_imish.F5BigipImishParser.S_access_listContext;
import org.batfish.grammar.f5_bigip_imish.F5BigipImishParser.S_route_mapContext;
import org.batfish.grammar.f5_bigip_imish.F5BigipImishParser.WordContext;
import org.batfish.representation.f5_bigip.AccessList;
import org.batfish.representation.f5_bigip.AccessListLine;
import org.batfish.representation.f5_bigip.F5BigipConfiguration;
import org.batfish.representation.f5_bigip.F5BigipStructureType;
import org.batfish.representation.f5_bigip.F5BigipStructureUsage;
import org.batfish.representation.f5_bigip.MatchAccessList;
import org.batfish.representation.f5_bigip.RouteMap;
import org.batfish.representation.f5_bigip.RouteMapEntry;
import org.batfish.vendor.StructureType;

public class F5BigipImishConfigurationBuilder extends F5BigipImishParserBaseListener {

  private final @Nonnull F5BigipConfiguration _c;

  @SuppressWarnings("unused")
  private final @Nonnull F5BigipImishCombinedParser _parser;

  private final @Nonnull String _text;
  private final @Nonnull Warnings _w;
  private @Nullable RouteMapEntry _currentRouteMapEntry;

  public F5BigipImishConfigurationBuilder(
      F5BigipImishCombinedParser parser,
      String text,
      Warnings w,
      F5BigipConfiguration configuration) {
    _parser = parser;
    _text = text;
    _w = w;
    _c = configuration;
  }

  private String convErrorMessage(Class<?> type, ParserRuleContext ctx) {
    return String.format("Could not convert to %s: %s", type.getSimpleName(), getFullText(ctx));
  }

  private @Nullable <T, U extends T> T convProblem(
      Class<T> returnType, ParserRuleContext ctx, @Nullable U defaultReturnValue) {
    _w.redFlag(convErrorMessage(returnType, ctx));
    return defaultReturnValue;
  }

  /** Mark the specified structure as defined on each line in the supplied context */
  private void defineStructure(StructureType type, String name, RuleContext ctx) {
    /* Recursively process children to find all relevant definition lines for the specified context */
    for (int i = 0; i < ctx.getChildCount(); i++) {
      ParseTree child = ctx.getChild(i);
      if (child instanceof TerminalNode) {
        _c.defineStructure(type, name, ((TerminalNode) child).getSymbol().getLine());
      } else if (child instanceof RuleContext) {
        defineStructure(type, name, (RuleContext) child);
      }
    }
  }

  @Override
  public void enterF5_bigip_imish_configuration(F5_bigip_imish_configurationContext ctx) {
    _c.setImish(true);
  }

  @Override
  public void enterS_access_list(S_access_listContext ctx) {
    String name = ctx.name.getText();
    Prefix prefix = toPrefix(ctx.ip_spec());
    if (prefix == null) {
      _w.redFlag(
          String.format(
              "Invalid source IP specifier: '%s' in: '%s'",
              ctx.ip_spec().getText(), ctx.getText()));
      return;
    }
    defineStructure(F5BigipStructureType.ACCESS_LIST, name, ctx);
    _c.getAccessLists()
        .computeIfAbsent(name, AccessList::new)
        .getLines()
        .add(new AccessListLine(toLineAction(ctx.action), prefix, getFullText(ctx)));
  }

  @Override
  public void enterS_route_map(S_route_mapContext ctx) {
    String name = ctx.name.getText();
    Integer num = toInteger(ctx.num);
    if (num == null) {
      _w.redFlag(
          String.format("Invalid entry number: '%s' in: '%s", ctx.num.getText(), getFullText(ctx)));
      return;
    }
    defineStructure(F5BigipStructureType.ROUTE_MAP, name, ctx);
    _currentRouteMapEntry =
        _c.getRouteMaps()
            .computeIfAbsent(name, RouteMap::new)
            .getEntries()
            .computeIfAbsent(num, RouteMapEntry::new);
  }

  @Override
  public void exitS_route_map(S_route_mapContext ctx) {
    _currentRouteMapEntry = null;
  }

  @Override
  public void enterRmm_ip_address(Rmm_ip_addressContext ctx) {
    String name = ctx.name.getText();
    _c.referenceStructure(
        F5BigipStructureType.ACCESS_LIST,
        name,
        F5BigipStructureUsage.ROUTE_MAP_MATCH_IP_ADDRESS,
        ctx.name.getStart().getLine());
    _currentRouteMapEntry.setMatchAccessList(new MatchAccessList(name));
  }

  private @Nullable Integer toInteger(WordContext ctx) {
    try {
      return Integer.parseInt(ctx.getText(), 10);
    } catch (IllegalArgumentException e) {
      return convProblem(Integer.class, ctx, null);
    }
  }

  private @Nonnull String getFullText(ParserRuleContext ctx) {
    int start = ctx.getStart().getStartIndex();
    int end = ctx.getStop().getStopIndex();
    String text = _text.substring(start, end + 1);
    return text;
  }

  private @Nonnull LineAction toLineAction(Line_actionContext ctx) {
    return ctx.PERMIT() != null ? LineAction.PERMIT : LineAction.DENY;
  }

  private @Nullable Prefix toPrefix(Ip_specContext ctx) {
    if (ctx.ANY() != null) {
      return Prefix.ZERO;
    } else if (ctx.prefix != null) {
      return Prefix.tryParse(ctx.getText()).orElse(convProblem(Prefix.class, ctx, null));
    } else {
      return convProblem(Prefix.class, ctx, null);
    }
  }
}
