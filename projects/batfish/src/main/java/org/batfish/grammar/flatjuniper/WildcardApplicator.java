package org.batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_line_tailContext;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;

public class WildcardApplicator extends FlatJuniperParserBaseListener {

  private Flat_juniper_configurationContext _configurationContext;

  private HierarchyPath _currentPath;

  private boolean _enablePathRecording;

  private final Hierarchy _hierarchy;

  private List<ParseTree> _newConfigurationLines;

  public WildcardApplicator(Hierarchy hierarchy) {
    _hierarchy = hierarchy;
  }

  @Override
  public void enterFlat_juniper_configuration(Flat_juniper_configurationContext ctx) {
    _configurationContext = ctx;
    _newConfigurationLines = new ArrayList<>(ctx.children);
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
      List<ParseTree> lines =
          _hierarchy
              .getMasterTree()
              .applyWildcardPath(_currentPath, _configurationContext, _hierarchy.getTokenInputs());
      int insertionIndex = _newConfigurationLines.indexOf(ctx);
      _newConfigurationLines.addAll(insertionIndex, lines);
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
      // WildcardApplicator expands wildcards introduced by apply-groups; they are always valid.
      if (node.getSymbol().getType() == FlatJuniperLexer.WILDCARD_ARTIFACT) {
        _currentPath.addWildcardNode(text, line);
      } else {
        _currentPath.addNode(text, line);
      }
    }
  }
}
