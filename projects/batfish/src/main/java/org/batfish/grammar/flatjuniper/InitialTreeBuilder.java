package org.batfish.grammar.flatjuniper;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.S_groupsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_line_tailContext;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;

public final class InitialTreeBuilder extends FlatJuniperParserBaseListener {

  private HierarchyPath _currentPath;

  /** Whether the subtrees of this node go into the {@link #_currentPath}. */
  private boolean _enablePathRecording;

  /** Whether wildcards are valid in the rest of the current line, or normal text otherwise. */
  private boolean _enableWildcards;

  private final Hierarchy _hierarchy;

  private HierarchyPath _lastPath;

  public InitialTreeBuilder(Hierarchy hierarchy) {
    _hierarchy = hierarchy;
  }

  @Override
  public void enterSet_line_tail(Set_line_tailContext ctx) {
    _enablePathRecording = true;
    _enableWildcards = false;
    _currentPath = new HierarchyPath();
  }

  @Override
  public void exitSet_line(Set_lineContext ctx) {
    _hierarchy.addMasterPath(_currentPath, ctx, null);
    _lastPath = _currentPath;
    _currentPath = null;
  }

  @Override
  public void visitErrorNode(ErrorNode node) {
    if (_lastPath == null) {
      _hierarchy.addMasterRootErrorNode(node);
    } else {
      _hierarchy.addMasterPath(_lastPath, null, node);
    }
  }

  @Override
  public void enterS_groups(S_groupsContext ctx) {
    _enableWildcards = true;
  }

  @Override
  public void exitS_groups(S_groupsContext ctx) {
    _enableWildcards = false;
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
      if (_enableWildcards && node.getSymbol().getType() == FlatJuniperLexer.WILDCARD) {
        _currentPath.addWildcardNode(text, line);
      } else {
        _currentPath.addNode(text, line);
      }
    }
  }
}
