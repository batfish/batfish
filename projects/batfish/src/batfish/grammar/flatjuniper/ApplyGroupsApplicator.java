package batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import batfish.grammar.BatfishCombinedParser;
import batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;
import batfish.grammar.flatjuniper.FlatJuniperParser.Set_line_tailContext;
import batfish.grammar.flatjuniper.Hierarchy.HierarchyTree;
import batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;
import batfish.grammar.flatjuniper.FlatJuniperParser.*;
import batfish.main.BatfishException;
import batfish.main.PedanticBatfishException;

public class ApplyGroupsApplicator extends FlatJuniperParserBaseListener {

   private BatfishCombinedParser<?, ?> _combinedParser;

   private Flat_juniper_configurationContext _configurationContext;

   private HierarchyPath _currentPath;

   private Set_lineContext _currentSetLine;

   private boolean _enablePathRecording;

   private Hierarchy _hierarchy;

   private List<ParseTree> _newConfigurationLines;

   private final boolean _pedantic;

   private final List<String> _warnings;

   public ApplyGroupsApplicator(BatfishCombinedParser<?, ?> combinedParser,
         Hierarchy hierarchy, List<String> warnings, boolean pedantic) {
      _combinedParser = combinedParser;
      _hierarchy = hierarchy;
      _pedantic = pedantic;
      _warnings = warnings;
   }

   @Override
   public void enterFlat_juniper_configuration(
         Flat_juniper_configurationContext ctx) {
      _configurationContext = ctx;
      _newConfigurationLines = new ArrayList<ParseTree>();
      _newConfigurationLines.addAll(ctx.children);
   }

   @Override
   public void enterS_apply_groups(S_apply_groupsContext ctx) {
      String groupName = ctx.name.getText();
      try {
         List<ParseTree> applyGroupsLines = _hierarchy.getApplyGroupsLines(
               groupName, _currentPath, _configurationContext);
         int insertionIndex = _newConfigurationLines.indexOf(_currentSetLine);
         _newConfigurationLines.remove(_currentSetLine);
         _newConfigurationLines.addAll(insertionIndex, applyGroupsLines);
      }
      catch (PedanticBatfishException e) {
         String message = "Exception processing apply-groups statement";
         if (_pedantic) {
            throw new BatfishException(message, e);
         }
         else {
            message += ": " + e.getMessage();
            _warnings.add(message);
         }
      }
   }

   @Override
   public void enterSet_line(Set_lineContext ctx) {
      _currentSetLine = ctx;
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
   public void exitS_groups_named(S_groups_namedContext ctx) {
      String groupName = ctx.name.getText();
      HierarchyTree tree = _hierarchy.getTree(groupName);
      if (tree == null) {
         tree = _hierarchy.newTree(groupName);
      }
      StatementContext statement = ctx.s_groups_tail().statement();
      if (statement == null) {
         return;
      }
      Interval interval = ctx.s_groups_tail().getSourceInterval();
      List<Token> unfilteredTokens = _combinedParser.getTokens().getTokens(
            interval.a, interval.b);
      HierarchyPath path = new HierarchyPath();
      for (Token currentToken : unfilteredTokens) {
         if (currentToken.getChannel() != Lexer.HIDDEN) {
            String text = currentToken.getText();
            if (currentToken.getType() == FlatJuniperLexer.WILDCARD) {
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
         if (node.getSymbol().getType() == FlatJuniperLexer.WILDCARD) {
            _currentPath.addWildcardNode(text);
         }
         else {
            _currentPath.addNode(text);
         }
      }
   }

}
