package org.batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Interface_idContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_line_tailContext;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;

public class WildcardPruner extends FlatJuniperParserBaseListener {

  private Flat_juniper_configurationContext _configurationContext;

  private HierarchyPath _currentPath;

  private boolean _enablePathRecording;

  private boolean _reenablePathRecording;

  private List<ParseTree> _newConfigurationLines;

  @Override
  public void enterFlat_juniper_configuration(Flat_juniper_configurationContext ctx) {
    _configurationContext = ctx;
    _newConfigurationLines = new ArrayList<>(ctx.children);
  }

  @Override
  public void enterInterface_id(Interface_idContext ctx) {
    if (_enablePathRecording && (ctx.unit != null || ctx.chnl != null || ctx.node != null)) {
      _enablePathRecording = false;
      _reenablePathRecording = true;
      String text = ctx.getText();
      _currentPath.addNode(text, ctx.getStart().getLine());
    }
  }

  @Override
  public void exitInterface_id(Interface_idContext ctx) {
    if (_reenablePathRecording) {
      _enablePathRecording = true;
      _reenablePathRecording = false;
    }
  }

  @Override
  public void enterSet_line_tail(Set_line_tailContext ctx) {
    _enablePathRecording = true;
    _currentPath = new HierarchyPath();
  }

  @Override
  public void exitFlat_juniper_configuration(Flat_juniper_configurationContext ctx) {
    _configurationContext.children = _newConfigurationLines;
  }

  @Override
  public void exitSet_line(Set_lineContext ctx) {
    if (_currentPath.containsWildcard()) {
      _newConfigurationLines.remove(ctx);
    }
    _currentPath = null;
  }

  @Override
  public void exitSet_line_tail(Set_line_tailContext ctx) {
    _enablePathRecording = false;
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    if (_enablePathRecording) {
      String text = node.getText();
      int line = node.getSymbol().getLine();
      if (node.getSymbol().getType() == FlatJuniperLexer.WILDCARD_ARTIFACT) {
        _currentPath.addWildcardNode(text, line);
      } else {
        _currentPath.addNode(text, line);
      }
    }
  }
}
