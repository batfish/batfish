package org.batfish.grammar.flatjuniper;

import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Apply_groupsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Apply_groups_exceptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Flat_juniper_configurationContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Interface_idContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.S_groups_namedContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_line_tailContext;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;
import org.batfish.main.PartialGroupMatchException;
import org.batfish.main.UndefinedGroupBatfishException;

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

  public ApplyGroupsApplicator(
      FlatJuniperCombinedParser combinedParser, Hierarchy hierarchy, Warnings warnings) {
    _hierarchy = hierarchy;
    _w = warnings;
  }

  @Override
  public void enterApply_groups(Apply_groupsContext ctx) {
    if (_inGroup) {
      return;
    }
    String groupName = ctx.name.getText();
    try {
      List<ParseTree> applyGroupsLines =
          _hierarchy.getApplyGroupsLines(groupName, _currentPath, _configurationContext);
      int insertionIndex = _newConfigurationLines.indexOf(_currentSetLine);
      _newConfigurationLines.addAll(insertionIndex, applyGroupsLines);
    } catch (PartialGroupMatchException e) {
      String message =
          "Exception processing apply-groups statement at path: \""
              + _currentPath.pathString()
              + "\" with group \""
              + groupName
              + "\": "
              + e.getMessage()
              + ": caused by: "
              + ExceptionUtils.getStackTrace(e);
      _w.pedantic(message);
    } catch (UndefinedGroupBatfishException e) {
      String message =
          "apply-groups statement at path: \""
              + _currentPath.pathString()
              + "\" refers to non-existent group \""
              + groupName
              + "\n";
      _w.redFlag(message);
    } catch (BatfishException e) {
      String message =
          "Exception processing apply-groups statement at path: \""
              + _currentPath.pathString()
              + "\" with group \""
              + groupName
              + "\": "
              + e.getMessage()
              + ": caused by: "
              + ExceptionUtils.getStackTrace(e);
      _w.redFlag(message);
    }
    _newConfigurationLines.remove(_currentSetLine);
    _changed = true;
  }

  @Override
  public void enterApply_groups_except(Apply_groups_exceptContext ctx) {
    if (_inGroup) {
      _w.redFlag("Do not know how to handle apply-groups-except occcurring within group statement");
    }
    _newConfigurationLines.remove(_currentSetLine);
  }

  @Override
  public void enterFlat_juniper_configuration(Flat_juniper_configurationContext ctx) {
    _configurationContext = ctx;
    _newConfigurationLines = new ArrayList<>();
    _newConfigurationLines.addAll(ctx.children);
  }

  @Override
  public void enterInterface_id(Interface_idContext ctx) {
    if (_enablePathRecording && (ctx.unit != null || ctx.suffix != null || ctx.node != null)) {
      _enablePathRecording = false;
      _reenablePathRecording = true;
      String text = ctx.getText();
      _currentPath.addNode(text);
    }
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
  public void exitFlat_juniper_configuration(Flat_juniper_configurationContext ctx) {
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
      } else {
        _currentPath.addNode(text);
      }
    }
  }
}
