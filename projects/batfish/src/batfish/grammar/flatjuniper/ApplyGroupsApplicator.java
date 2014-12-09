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
import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.Set_line_tailContext;
import batfish.grammar.flatjuniper.Hierarchy.HierarchyTree;
import batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;
import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.*;

public class ApplyGroupsApplicator extends
      FlatJuniperGrammarParserBaseListener {

   private BatfishCombinedParser<?, ?> _combinedParser;

   private Flat_juniper_configurationContext _configurationContext;

   private HierarchyPath _currentPath;

   private Set_lineContext _currentSetLine;

   private boolean _enablePathRecording;

   private Hierarchy _hierarchy;

   private List<ParseTree> _newConfigurationLines;

   public ApplyGroupsApplicator(BatfishCombinedParser<?, ?> combinedParser, Hierarchy hierarchy) {
      _combinedParser = combinedParser;
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
   public void exitFlat_juniper_configuration(
         Flat_juniper_configurationContext ctx) {
      _configurationContext.children = _newConfigurationLines;
   }

   @Override
   public void enterS_apply_groups(S_apply_groupsContext ctx) {
      String groupName = ctx.name.getText();
      List<ParseTree> applyGroupsLines = _hierarchy.getApplyGroupsLines(
            groupName, _currentPath, _configurationContext);
      int insertionIndex = _newConfigurationLines.indexOf(_currentSetLine);
      _newConfigurationLines.remove(ctx);
      _newConfigurationLines.addAll(insertionIndex, applyGroupsLines);
   }

   @Override
   public void enterSet_line(Set_lineContext ctx) {
      _enablePathRecording = true;
      _currentPath = new HierarchyPath();
      _currentSetLine = ctx;
   }

   @Override
   public void enterSet_line_tail(Set_line_tailContext ctx) {
      _enablePathRecording = true;
      _currentPath = new HierarchyPath();
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
      List<Token> unfilteredTokens = _combinedParser.getTokens().getTokens(
            interval.a, interval.b);
      HierarchyPath path = new HierarchyPath();
      for (Token currentToken : unfilteredTokens) {
         if (currentToken.getChannel() != Lexer.HIDDEN) {
            String text = currentToken.getText();
            if (currentToken.getType() == FlatJuniperGrammarLexer.WILDCARD) {
               path.addWildcardNode(text);
            }
            else {
               path.addNode(text);
            }
         }
      }
      path.setStatement(statement);
      tree.addPath(path, _currentSetLine, null);
   }

   @Override
   public void exitSet_line(Set_lineContext ctx) {
      _hierarchy.getMasterTree().addPath(_currentPath, ctx, null);
      _currentSetLine = null;
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
