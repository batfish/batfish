package org.batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.*;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;

public class WildcardApplicator extends FlatJuniperParserBaseListener {

   private Flat_juniper_configurationContext _configurationContext;

   private HierarchyPath _currentPath;

   private boolean _enablePathRecording;

   private Hierarchy _hierarchy;

   private List<ParseTree> _newConfigurationLines;

   public WildcardApplicator(Hierarchy hierarchy) {
      _hierarchy = hierarchy;
   }

   @Override
   public void enterFlat_juniper_configuration(
         Flat_juniper_configurationContext ctx) {
      _configurationContext = ctx;
      _newConfigurationLines = new ArrayList<ParseTree>();
      _newConfigurationLines.addAll(ctx.children);
   }

   @Override
   public void enterSet_line_tail(Set_line_tailContext ctx) {
      _enablePathRecording = true;
      _currentPath = new HierarchyPath();
   }

   @Override
   public void exitFlat_juniper_configuration(
         Flat_juniper_configurationContext ctx) {
      _configurationContext.children = _newConfigurationLines;
   }

   @Override
   public void exitSet_line(Set_lineContext ctx) {
      if (_currentPath.containsWildcard()) {
         List<ParseTree> lines = _hierarchy.getMasterTree().applyWildcardPath(
               _currentPath, _configurationContext);
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
         if (node.getSymbol().getType() == FlatJuniperLexer.WILDCARD) {
            _currentPath.addWildcardNode(text);
         }
         else {
            _currentPath.addNode(text);
         }
      }
   }
}
