package org.batfish.grammar.flatjuniper;

import static org.batfish.grammar.flatjuniper.ConfigurationBuilder.unquote;

import java.util.Optional;
import javax.annotation.Nonnull;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.batfish.common.Warnings;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Apply_groupsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Apply_groups_exceptContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Junos_nameContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.S_groupsContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_lineContext;
import org.batfish.grammar.flatjuniper.FlatJuniperParser.Set_line_tailContext;
import org.batfish.grammar.flatjuniper.Hierarchy.HierarchyTree.HierarchyPath;

/**
 * Flat Juniper parse tree visitor that marks {@code apply-groups} and {@code apply-groups-except}
 * at the hierarchy nodes at which they occur.
 */
public class ApplyGroupsMarker extends FlatJuniperParserBaseListener {

  private HierarchyPath _currentPath;

  /** Whether the subtrees of this node go into the {@link #_currentPath}. */
  private boolean _enablePathRecording;

  /** Whether applying and excluding groups is enabled. */
  private boolean _enableMarking;

  private final @Nonnull Hierarchy _hierarchy;
  private final @Nonnull Warnings _warnings;

  public ApplyGroupsMarker(@Nonnull Hierarchy hierarchy, @Nonnull Warnings warnings) {
    _hierarchy = hierarchy;
    _warnings = warnings;
  }

  @Override
  public void enterApply_groups_except(Apply_groups_exceptContext ctx) {
    if (!_enableMarking) {
      return;
    }
    Optional<String> maybeGroupName = toString(ctx.name);
    if (!maybeGroupName.isPresent()) {
      // already warned
      return;
    }
    String groupName = maybeGroupName.get();
    _hierarchy.markApplyGroupsExcept(_currentPath, groupName);
  }

  @Override
  public void enterApply_groups(Apply_groupsContext ctx) {
    if (!_enableMarking) {
      return;
    }
    Optional<String> maybeGroupName = toString(ctx.name);
    if (!maybeGroupName.isPresent()) {
      // already warned
      return;
    }
    String groupName = maybeGroupName.get();
    if (_hierarchy.getTree(groupName) == null) {
      _warnings.redFlagf(
          "apply-groups statement at %s refers to non-existent group: '%s'\n",
          pathString(), groupName);
    }
    if (groupName.equals("${node}")) {
      _hierarchy.markApplyGroups(_currentPath, "node0");
      _hierarchy.markApplyGroups(_currentPath, "node1");
    } else {
      _hierarchy.markApplyGroups(_currentPath, groupName);
    }
  }

  private @Nonnull String pathString() {
    String currentPathString = _currentPath.pathString();
    return currentPathString.isEmpty()
        ? "top level"
        : String.format("path: '%s'", currentPathString);
  }

  private @Nonnull Optional<String> toString(Junos_nameContext ctx) {
    String text = ctx.getText();
    Optional<String> ret = unquote(text);
    if (!ret.isPresent()) {
      _warnings.redFlagf("Improperly quoted group name at %s: %s", pathString(), text);
    }
    return ret;
  }

  @Override
  public void enterSet_line_tail(Set_line_tailContext ctx) {
    _enablePathRecording = true;
    _currentPath = new HierarchyPath();
  }

  @Override
  public void enterSet_line(Set_lineContext ctx) {
    _enableMarking = true;
  }

  @Override
  public void exitSet_line(Set_lineContext ctx) {
    _hierarchy.addMasterPath(_currentPath, ctx, null);
    _currentPath = null;
  }

  @Override
  public void enterS_groups(S_groupsContext ctx) {
    _enableMarking = false;
  }

  @Override
  public void exitSet_line_tail(Set_line_tailContext ctx) {
    _enablePathRecording = false;
  }

  public @Nonnull Hierarchy getHierarchy() {
    return _hierarchy;
  }

  @Override
  public void visitTerminal(TerminalNode node) {
    if (!_enablePathRecording) {
      return;
    }
    String text = node.getText();
    int line = node.getSymbol().getLine();
    _currentPath.addNode(text, line);
  }
}
