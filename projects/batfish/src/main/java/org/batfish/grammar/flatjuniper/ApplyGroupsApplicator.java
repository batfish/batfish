package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.flatjuniper.ConfigurationBuilder.unquote;

import com.google.common.base.Throwables;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
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

  public ApplyGroupsApplicator(Hierarchy hierarchy, Warnings warnings) {
    _hierarchy = hierarchy;
    _w = warnings;
  }

  private String applyGroupsExceptionMessage(String groupName, Throwable e) {
    return String.format(
        "Exception processing apply-groups statement at %s with group '%s': %s: caused by: %s",
        pathString(), groupName, e.getMessage(), Throwables.getStackTraceAsString(e));
  }

  @Override
  public void enterApply_groups(Apply_groupsContext ctx) {
    if (_inGroup) {
      return;
    }
    String groupName = unquote(ctx.name.getText());
    if (groupName.equals("${node}")) {
      processGroup("node0", true, false);
      processGroup("node1", true, true);
    } else {
      processGroup(groupName, false, true);
    }
  }

  private void processGroup(String groupName, boolean clusterGroup, boolean removeApplyLine) {
    try {
      List<ParseTree> applyGroupsLines =
          _hierarchy.getApplyGroupsLines(
              groupName, _currentPath, _configurationContext, clusterGroup);
      int insertionIndex = _newConfigurationLines.indexOf(_currentSetLine);
      _newConfigurationLines.addAll(insertionIndex, applyGroupsLines);
    } catch (PartialGroupMatchException e) {
      _w.pedantic(applyGroupsExceptionMessage(groupName, e));
    } catch (UndefinedGroupBatfishException e) {
      String message =
          String.format(
              "apply-groups statement at %s refers to non-existent group: '%s'\n",
              pathString(), groupName);

      _w.redFlag(message);
    } catch (BatfishException e) {
      _w.redFlag(applyGroupsExceptionMessage(groupName, e));
    }
    if (removeApplyLine) {
      _newConfigurationLines.remove(_currentSetLine);
    }
    _changed = true;
  }

  @Override
  public void enterApply_groups_except(Apply_groups_exceptContext ctx) {
    if (_inGroup) {
      _w.redFlag("Do not know how to handle apply-groups-except occurring within group statement");
    }
    _newConfigurationLines.remove(_currentSetLine);
  }

  @Override
  public void enterFlat_juniper_configuration(Flat_juniper_configurationContext ctx) {
    _configurationContext = ctx;
    _newConfigurationLines = new ArrayList<>(ctx.children);
  }

  @Override
  public void enterInterface_id(Interface_idContext ctx) {
    if (_enablePathRecording && (ctx.unit != null || ctx.chnl != null || ctx.node != null)) {
      _enablePathRecording = false;
      _reenablePathRecording = true;
      String text = ctx.getText();
      _currentPath.addNode(text, ctx.getStart().getLine());
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

  private String pathString() {
    String currentPathString = _currentPath.pathString();
    return currentPathString.isEmpty()
        ? "top level"
        : String.format("path: '%s'", currentPathString);
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    if (_enablePathRecording) {
      String text = node.getText();
      int line = node.getSymbol().getLine();
      if (node.getSymbol().getType() == FlatJuniperLexer.WILDCARD) {
        _currentPath.addWildcardNode(text, line);
      } else {
        _currentPath.addNode(text, line);
      }
    }
  }
}
