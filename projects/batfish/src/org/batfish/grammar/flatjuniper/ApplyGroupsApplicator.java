package org.batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.*;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;
import org.batfish.common.BatfishException;
import org.batfish.main.PartialGroupMatchBatfishException;
import org.batfish.main.UndefinedGroupBatfishException;
import org.batfish.main.Warnings;

public class ApplyGroupsApplicator extends FlatJuniperParserBaseListener {

   private boolean _changed;

   private Flat_juniper_configurationContext _configurationContext;

   private HierarchyPath _currentPath;

   private Set_lineContext _currentSetLine;

   private boolean _enablePathRecording;

   private final Hierarchy _hierarchy;

   private boolean _inGroup;

   private List<ParseTree> _newConfigurationLines;

   private boolean _reenablePathRecording;

   private final Warnings _w;

   public ApplyGroupsApplicator(FlatJuniperCombinedParser combinedParser,
         Hierarchy hierarchy, Warnings warnings) {
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
   public void enterS_apply_groups(S_apply_groupsContext ctx) {
      if (_inGroup) {
         return;
      }
      String groupName = ctx.name.getText();
      try {
         List<ParseTree> applyGroupsLines = _hierarchy.getApplyGroupsLines(
               groupName, _currentPath, _configurationContext);
         int insertionIndex = _newConfigurationLines.indexOf(_currentSetLine);
         _newConfigurationLines.addAll(insertionIndex, applyGroupsLines);
      }
      catch (PartialGroupMatchBatfishException e) {
         String message = "Exception processing apply-groups statement at path: \""
               + _currentPath.pathString()
               + "\" with group \""
               + groupName
               + "\": "
               + e.getMessage()
               + ": caused by: "
               + ExceptionUtils.getFullStackTrace(e);
         _w.pedantic(message);
      }
      catch (UndefinedGroupBatfishException e) {
         String message = "apply-groups statement at path: \""
               + _currentPath.pathString()
               + "\" refers to non-existent group \"" + groupName + "\n";
         _w.redFlag(message);
      }
      catch (BatfishException e) {
         String message = "Exception processing apply-groups statement at path: \""
               + _currentPath.pathString()
               + "\" with group \""
               + groupName
               + "\": "
               + e.getMessage()
               + ": caused by: "
               + ExceptionUtils.getFullStackTrace(e);
         _w.redFlag(message);
      }
      _newConfigurationLines.remove(_currentSetLine);
      _changed = true;
   }

   @Override
   public void enterS_apply_groups_except(S_apply_groups_exceptContext ctx) {
      if (_inGroup) {
         _w.redFlag("Do not know how to handle apply-groups-except occcurring within group statement");
      }
      _newConfigurationLines.remove(_currentSetLine);
   }

   @Override
   public void enterS_groups_named(S_groups_namedContext ctx) {
      _inGroup = true;
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
   public void exitS_groups_named(S_groups_namedContext ctx) {
      _inGroup = false;
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

   public boolean getChanged() {
      return _changed;
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
