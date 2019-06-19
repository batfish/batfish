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

public class DeactivatedLinePruner extends FlatJuniperParserBaseListener {

  private boolean _checkLine;

  private Flat_juniper_configurationContext _configurationContext;

  private HierarchyPath _currentPath;

  private boolean _enablePathRecording;

  private Hierarchy _hierarchy;

  private List<ParseTree> _newConfigurationLines;

  private boolean _reenablePathRecording;

  public DeactivatedLinePruner(Hierarchy hierarchy) {
    _hierarchy = hierarchy;
  }

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
  public void enterSet_line(Set_lineContext ctx) {
    _checkLine = true;
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
  public void exitInterface_id(Interface_idContext ctx) {
    if (_reenablePathRecording) {
      _enablePathRecording = true;
      _reenablePathRecording = false;
    }
  }

  @Override
  public void exitSet_line(Set_lineContext ctx) {
    if (_checkLine) {
      if (_hierarchy.isDeactivated(_currentPath)) {
        _newConfigurationLines.remove(ctx);
      }
      _currentPath = null;
    }
  }

  @Override
  public void exitSet_line_tail(Set_line_tailContext ctx) {
    _enablePathRecording = false;
  }

  public Hierarchy getHierarchy() {
    return _hierarchy;
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    if (_enablePathRecording) {
      String text = node.getText();
      _currentPath.addNode(text, node.getSymbol().getLine());
    }
  }
}
