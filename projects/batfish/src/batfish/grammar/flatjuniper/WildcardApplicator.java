package batfish.grammar.flatjuniper;

import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.*;
import batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;

public class WildcardApplicator extends FlatJuniperGrammarParserBaseListener {

   private HierarchyPath _currentPath;

   private boolean _enablePathRecording;

   private Hierarchy _hierarchy;

   public WildcardApplicator(Hierarchy hierarchy) {
      _hierarchy = hierarchy;
   }

   @Override
   public void enterSet_line_tail(Set_line_tailContext ctx) {
      _enablePathRecording = true;
      _currentPath = new HierarchyPath();
   }

   @Override
   public void exitSet_line(Set_lineContext ctx) {
      if (_currentPath.containsWildcard()) {
         List<ParseTree> lines = _hierarchy.getMasterTree().applyWildcardPath(
               _currentPath, ctx);
         List<ParseTree> children = ctx.getParent().children;
         int insertionIndex = children.indexOf(ctx) + 1;
         children.addAll(insertionIndex, lines);
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
         if (node.getSymbol().getType() == FlatJuniperGrammarLexer.WILDCARD) {
            _currentPath.addWildcardNode(text);
         }
         else {
            _currentPath.addNode(text);
         }
      }
   }
}
