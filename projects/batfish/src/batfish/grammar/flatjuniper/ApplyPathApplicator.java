package batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.Set_lineContext;
import batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;
import batfish.grammar.flatjuniper.FlatJuniperGrammarParser.*;

public class ApplyPathApplicator extends FlatJuniperGrammarParserBaseListener {

   private Flat_juniper_configurationContext _configurationContext;

   private HierarchyPath _currentPath;

   private Set_lineContext _currentSetLine;

   private boolean _enablePathRecording;

   private Hierarchy _hierarchy;

   private List<ParseTree> _newConfigurationLines;

   public ApplyPathApplicator(Hierarchy hierarchy) {
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
   public void enterPlt_apply_path(Plt_apply_pathContext ctx) {
      HierarchyPath applyPathPath = new HierarchyPath();
      String pathQuoted = ctx.path.getText();
      String pathWithoutQuotes = pathQuoted.substring(1,
            pathQuoted.length() - 1);
      String[] pathComponents = pathWithoutQuotes.split(" ");
      for (String pathComponent : pathComponents) {
         boolean isWildcard = pathComponent.charAt(0) == '<';
         if (isWildcard) {
            applyPathPath.addWildcardNode(pathComponent);
         }
         else {
            applyPathPath.addNode(pathComponent);
         }
      }
      List<ParseTree> newLines = _hierarchy.getApplyPathLines(_currentPath,
            applyPathPath, _configurationContext);
      int insertionIndex = _newConfigurationLines.indexOf(_currentSetLine);
      _newConfigurationLines.remove(_currentSetLine);
      _newConfigurationLines.addAll(insertionIndex, newLines);
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
   public void exitSet_line(Set_lineContext ctx) {
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
