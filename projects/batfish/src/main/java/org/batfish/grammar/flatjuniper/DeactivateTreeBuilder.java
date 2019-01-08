package org.batfish.grammar.flatjuniper;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Deactivate_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Deactivate_line_tailContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Interface_idContext;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;

public class DeactivateTreeBuilder extends FlatJuniperParserBaseListener {

  private boolean _addLine;

  private HierarchyPath _currentPath;

  private boolean _enablePathRecording;

  private Hierarchy _hierarchy;

  private boolean _reenablePathRecording;

  public DeactivateTreeBuilder(Hierarchy hierarchy) {
    _hierarchy = hierarchy;
  }

  @Override
  public void enterDeactivate_line(Deactivate_lineContext ctx) {
    _addLine = true;
  }

  @Override
  public void enterDeactivate_line_tail(Deactivate_line_tailContext ctx) {
    _enablePathRecording = true;
    _currentPath = new HierarchyPath();
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
  public void exitDeactivate_line(Deactivate_lineContext ctx) {
    if (_addLine) {
      _hierarchy.addDeactivatePath(_currentPath, ctx);
      _currentPath = null;
    }
  }

  @Override
  public void exitDeactivate_line_tail(Deactivate_line_tailContext ctx) {
    _enablePathRecording = false;
  }

  @Override
  public void exitInterface_id(Interface_idContext ctx) {
    if (_reenablePathRecording) {
      _enablePathRecording = true;
      _reenablePathRecording = false;
    }
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
