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
    List<ParseTree> applyGroupsLines;
    try {
      applyGroupsLines =
          _hierarchy.getApplyGroupsLines(
              groupName, _currentPath, _configurationContext, clusterGroup);
      // Insert the new configuration lines for each group at the top of the list of parse trees,
      // so that:
      //
      // 1. Inherited lines resulting from the first applied group come after lines from subsequent
      //    applied groups at the same level of hierarchy. This conforms to priority indicated in:
      // https://www.juniper.net/documentation/us/en/software/junos/bgp/topics/ref/statement/apply-groups.html#apply-groups__d65612e42
      //
      // 2. Ordinary non-inherited lines come after all inherited lines resulting from applied
      //    groups, i.e. non-inherited lines take priority.
      _newConfigurationLines.addAll(0, applyGroupsLines);
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
      _currentPath.addNode(text, line);
    }
  }
}
