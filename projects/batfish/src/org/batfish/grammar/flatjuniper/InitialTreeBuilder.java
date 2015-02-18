package org.batfish.grammar.flatjuniper;

import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.*;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;

public class InitialTreeBuilder extends FlatJuniperParserBaseListener {

   private boolean _addLine;

   private HierarchyPath _currentPath;

   private boolean _enablePathRecording;

   private Hierarchy _hierarchy;

   public InitialTreeBuilder(Hierarchy hierarchy) {
      _hierarchy = hierarchy;
   }

   @Override
   public void enterS_apply_groups_except(S_apply_groups_exceptContext ctx) {
      _addLine = false;
      _hierarchy.addMasterPath(_currentPath, null);
      String groupName = ctx.name.getText();
      _hierarchy.setApplyGroupsExcept(_currentPath, groupName);
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
   public void exitS_apply_groups(S_apply_groupsContext ctx) {
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
         if (node.getSymbol().getType() == FlatJuniperLexer.WILDCARD) {
            _currentPath.addWildcardNode(text);
         }
         else {
            _currentPath.addNode(text);
         }
      }
   }

}
