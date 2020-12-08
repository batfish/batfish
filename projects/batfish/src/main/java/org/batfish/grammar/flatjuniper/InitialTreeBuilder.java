package org.batfish.grammar.flatjuniper;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Apply_groupsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Apply_groups_exceptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Interface_idContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_line_tailContext;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;

public class InitialTreeBuilder extends FlatJuniperParserBaseListener {

  private boolean _addLine;

  private HierarchyPath _currentPath;

  private boolean _enablePathRecording;

  private Hierarchy _hierarchy;

  private boolean _reenablePathRecording;

  public InitialTreeBuilder(Hierarchy hierarchy) {
    _hierarchy = hierarchy;
  }

  @Override
  public void enterApply_groups_except(Apply_groups_exceptContext ctx) {
    _addLine = false;
    _hierarchy.addMasterPath(_currentPath, null);
    String groupName = ctx.name.getText();
    _hierarchy.setApplyGroupsExcept(_currentPath, groupName);
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
  public void enterSet_line(Set_lineContext ctx) {
    _addLine = true;
    _enablePathRecording = true;
    _currentPath = new HierarchyPath();
  }

  @Override
  public void enterSet_line_tail(Set_line_tailContext ctx) {
    _enablePathRecording = true;
    _currentPath = new HierarchyPath();
  }

  @Override
  public void exitApply_groups(Apply_groupsContext ctx) {
    _addLine = false;
  }

  @Override
  public void exitSet_line(Set_lineContext ctx) {
    if (_addLine) {
      _hierarchy.addMasterPath(_currentPath, ctx);
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
      int line = node.getSymbol().getLine();
      if (node.getSymbol().getType() == FlatJuniperLexer.WILDCARD) {
        _currentPath.addWildcardNode(text, line);
      } else {
        _currentPath.addNode(text, line);
      }
    }
  }
}
