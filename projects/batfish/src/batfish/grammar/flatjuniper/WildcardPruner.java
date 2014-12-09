package batfish.grammar.flatjuniper;

import org.antlr.v4.runtime.tree.TerminalNode;

import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.*;
import batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;

public class WildcardPruner extends FlatJuniperGrammarParserBaseListener {

   private HierarchyPath _currentPath;

   private boolean _enablePathRecording;

   @Override
   public void enterSet_line_tail(Set_line_tailContext ctx) {
      _enablePathRecording = true;
      _currentPath = new HierarchyPath();
   }

   @Override
   public void exitSet_line(Set_lineContext ctx) {
      if (_currentPath.containsWildcard()) {
         ctx.getParent().children.remove(ctx);
         System.out.println("would remove: " + _currentPath);
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
