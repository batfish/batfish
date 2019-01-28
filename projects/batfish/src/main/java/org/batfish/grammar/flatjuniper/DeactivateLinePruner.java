package org.batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTree;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Deactivate_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;

public class DeactivateLinePruner extends FlatJuniperParserBaseListener {

  private Flat_juniper_configurationContext _configurationContext;

  private List<ParseTree> _newConfigurationLines;

  @Override
  public void enterFlat_juniper_configuration(Flat_juniper_configurationContext ctx) {
    _configurationContext = ctx;
    _newConfigurationLines = new ArrayList<>(ctx.children);
  }

  @Override
  public void exitDeactivate_line(Deactivate_lineContext ctx) {
    _newConfigurationLines.remove(ctx);
  }

  @Override
  public void exitFlat_juniper_configuration(Flat_juniper_configurationContext ctx) {
    _configurationContext.children = _newConfigurationLines;
  }
}
