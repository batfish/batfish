package org.batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.*;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;
import org.batfish.common.BatfishException;
import org.batfish.main.Warnings;

public class ApplyPathApplicator extends FlatJuniperParserBaseListener {

   private Flat_juniper_configurationContext _configurationContext;

   private HierarchyPath _currentPath;

   private Set_lineContext _currentSetLine;

   private boolean _enablePathRecording;

   private Hierarchy _hierarchy;

   private List<ParseTree> _newConfigurationLines;

   private boolean _reenablePathRecording;

   private final Warnings _w;

   public ApplyPathApplicator(Hierarchy hierarchy, Warnings warnings) {
      _hierarchy = hierarchy;
      _w = warnings;
   }

   @Override
   public void enterFlat_juniper_configuration(
         Flat_juniper_configurationContext ctx) {
      _configurationContext = ctx;
      _newConfigurationLines = new ArrayList<ParseTree>();
      _newConfigurationLines.addAll(ctx.children);
   }

   @Override
   public void enterInterface_id(Interface_idContext ctx) {
      if (_enablePathRecording && ctx.unit != null) {
         _enablePathRecording = false;
         _reenablePathRecording = true;
         String text = ctx.getText();
         _currentPath.addNode(text);
      }
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
      int insertionIndex = _newConfigurationLines.indexOf(_currentSetLine);
      List<ParseTree> newLines = null;
      try {
         newLines = _hierarchy.getApplyPathLines(_currentPath, applyPathPath,
               _configurationContext);
      }
      catch (BatfishException e) {
         _w.redFlag("Could not apply path: "
               + pathQuoted
               + ": make sure path is terminated by wildcard (e.g. <*>) representing ip(v6) addresses or prefixes");
      }
      _newConfigurationLines.remove(_currentSetLine);
      if (newLines != null) {
         _newConfigurationLines.addAll(insertionIndex, newLines);
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
   public void exitInterface_id(Interface_idContext ctx) {
      if (_reenablePathRecording) {
         _enablePathRecording = true;
         _reenablePathRecording = false;
      }
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
         if (node.getSymbol().getType() == FlatJuniperLexer.WILDCARD) {
            _currentPath.addWildcardNode(text);
         }
         else {
            _currentPath.addNode(text);
         }
      }
   }

}
