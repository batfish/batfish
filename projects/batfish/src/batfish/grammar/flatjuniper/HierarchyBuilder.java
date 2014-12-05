package batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.Flat_juniper_configurationContext;
import batfish.grammar.flatjuniper.Hierarchy.HierarchyTree;
import batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;
import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.*;

public class HierarchyBuilder extends FlatJuniperGrammarParserBaseListener {

   private HierarchyPath _currentPath;

   private BatfishCombinedParser<?, ?> _combinedParser;
   private Hierarchy _hierarchy;
   private boolean _enablePathRecording;

   private List<ParseTree> _applyGroupsLines;

   private Flat_juniper_configurationContext _configurationContext;

   public HierarchyBuilder(BatfishCombinedParser<?, ?> combinedParser) {
      _combinedParser = combinedParser;
      _hierarchy = new Hierarchy();
   }

   @Override
   public void exitS_groups_named(S_groups_namedContext ctx) {
      String groupName = ctx.name.getText();
      StatementContext statement = ctx.s_groups_tail().statement();
      HierarchyTree tree = _hierarchy.getTree(groupName);
      if (tree == null) {
         tree = _hierarchy.newTree(groupName);
      }
      Interval interval = ctx.s_groups_tail().getSourceInterval();
      List<Token> unfilteredTokens = _combinedParser.getTokens().getTokens(interval.a,
            interval.b);
      List<Token> tokens = new ArrayList<Token>();
      for (Token t : unfilteredTokens) {
         if (t.getChannel() != Lexer.HIDDEN) {
            tokens.add(t);
         }
      }
      HierarchyPath path = new HierarchyPath();
      for (Token currentToken : tokens) {
         if (currentToken.getChannel() == Lexer.HIDDEN) {
            continue;
         }
         String text = currentToken.getText();
         if (currentToken.getType() == FlatJuniperGrammarLexer.WILDCARD) {
            path.addWildcardNode(text);
         }
         else {
            path.addNode(text);
         }
      }
      path.setStatement(statement);
      tree.addPath(path, tokens);
   }

   public Hierarchy getHierarchy() {
      return _hierarchy;
   }

   @Override
   public void enterSet_line_tail(Set_line_tailContext ctx) {
      _enablePathRecording = true;
      _currentPath = new HierarchyPath();
   }

   @Override
   public void exitSet_line_tail(Set_line_tailContext ctx) {
      _enablePathRecording = false;
      _currentPath = null;

   }

   @Override
   public void exitSet_line(Set_lineContext ctx) {
      if (_applyGroupsLines != null) {
         List<ParseTree> allLines = ctx.getParent().children;
         int insertionIndex = allLines.indexOf(ctx) + 1;
         //allLines.remove(ctx);
         allLines.addAll(insertionIndex, _applyGroupsLines);
         _applyGroupsLines = null;
      }
   }

   @Override
   public void visitTerminal(TerminalNode node) {
      if (_enablePathRecording) {
         String text = node.getText();
         _currentPath.addNode(text);
         assert Boolean.TRUE;
      }
   }

   @Override
   public void enterFlat_juniper_configuration(Flat_juniper_configurationContext ctx) {
      _configurationContext = ctx;
   }

   @Override
   public void enterS_apply_groups(S_apply_groupsContext ctx) {
      String groupName = ctx.name.getText();
      _applyGroupsLines = _hierarchy.getApplyGroupsLines(groupName,
            _currentPath, _configurationContext);
   }
}
