package org.batfish.vendor.sros.grammar;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.Warnings;

/**
 * Preprocesses the canonical {@link SrosStatementTree} before feature extraction, applying the two
 * SR-OS mechanisms that were characterized in P1 and deferred to P4 (see {@code
 * docs/parsing/vendors/sros.md}). Both operate purely on the tree, so they are independent of
 * whether the input was the brace, flat, or mixed form.
 *
 * <p>Order (documented so it is stable):
 *
 * <ol>
 *   <li><b>apply-groups expansion</b> — materializes config inherited from {@code groups}, so a
 *       subsequent {@code delete} can remove inherited content;
 *   <li><b>delete edits</b> — applies incremental {@code delete}/{@code -} mutations;
 *   <li><b>cleanup</b> — prunes the {@code groups} definition container, which is not configuration
 *       itself.
 * </ol>
 */
@ParametersAreNonnullByDefault
public final class SrosPreprocessor {

  /** Maximum apply-groups expansion passes before assuming a group reference cycle. */
  private static final int MAX_APPLY_GROUPS_PASSES = 100;

  public static void preprocess(SrosStatementTree root, Warnings w) {
    new SrosPreprocessor(root, w).run();
  }

  private SrosPreprocessor(SrosStatementTree root, Warnings w) {
    _root = root;
    _w = w;
  }

  private void run() {
    expandApplyGroups();
    applyDeletes(_root);
    pruneGroups();
  }

  // ---------------------------------------------------------------------------------------------
  // apply-groups expansion
  // ---------------------------------------------------------------------------------------------

  /**
   * Expands every {@code apply-groups [...]} reference by grafting the matching subtree of the
   * named {@code groups group "<name>"} definition onto the branch that applied it. Replicates the
   * SR-OS inheritance rules characterized in P1 (MD-CLI Guide §4.13): local config wins over
   * inherited; first-listed group wins over later ones; {@code apply-groups-exclude} suppresses a
   * group at a branch; group list keys may be regexes ({@code "<int-.*>"}) matched against the
   * branch's path. Runs to convergence so groups that themselves apply groups are fully resolved.
   */
  private void expandApplyGroups() {
    SrosStatementTree configure = _root.getChild("configure");
    if (configure == null) {
      return;
    }
    SrosStatementTree groups = navigate(configure, List.of("groups", "group"));
    if (groups == null || !groups.hasChildren()) {
      return;
    }

    for (int pass = 0; pass < MAX_APPLY_GROUPS_PASSES; pass++) {
      List<Branch> pending = new ArrayList<>();
      collectApplyGroupsBranches(configure, new ArrayDeque<>(), pending);
      if (pending.isEmpty()) {
        return;
      }
      for (Branch branch : pending) {
        expandBranch(branch, groups);
      }
    }
    _w.redFlagf("SR-OS apply-groups did not converge; possible group reference cycle");
  }

  /**
   * Records each node carrying an {@code apply-groups} child along with its path under configure.
   */
  private void collectApplyGroupsBranches(
      SrosStatementTree node, Deque<String> path, List<Branch> out) {
    if (node.getChild("apply-groups") != null) {
      out.add(new Branch(node, new ArrayList<>(path)));
    }
    for (Map.Entry<String, SrosStatementTree> e : node.getChildren().entrySet()) {
      String word = e.getKey();
      // Do not descend into the apply-groups/exclude marker subtrees themselves.
      if (word.equals("apply-groups") || word.equals("apply-groups-exclude")) {
        continue;
      }
      path.addLast(word);
      collectApplyGroupsBranches(e.getValue(), path, out);
      path.removeLast();
    }
  }

  private void expandBranch(Branch branch, SrosStatementTree groups) {
    SrosStatementTree node = branch._node;
    SrosStatementTree applyGroups = node.getChild("apply-groups");
    if (applyGroups == null) {
      return; // Already handled in an earlier branch this pass.
    }
    Set<String> exclude = new LinkedHashSet<>();
    SrosStatementTree excludeNode = node.getChild("apply-groups-exclude");
    if (excludeNode != null) {
      exclude.addAll(excludeNode.getChildren().keySet());
    }
    // First-listed group has highest precedence; copyInto never overwrites, so applying in listed
    // order yields first-wins, and local config (already present) beats all groups.
    for (String groupName : applyGroups.getChildren().keySet()) {
      if (exclude.contains(groupName)) {
        continue;
      }
      SrosStatementTree groupDef = groups.getChild(groupName);
      if (groupDef == null) {
        continue; // Reference to an undefined group: leave for another file/source, do not warn.
      }
      SrosStatementTree groupSubtree = navigate(groupDef, branch._path);
      if (groupSubtree != null) {
        groupSubtree.copyInto(node);
      }
    }
    node.removeChild("apply-groups");
    node.removeChild("apply-groups-exclude");
  }

  // ---------------------------------------------------------------------------------------------
  // delete edits
  // ---------------------------------------------------------------------------------------------

  /**
   * Applies incremental {@code delete <relative-path>} (and its {@code -} alias) edits. A {@code
   * delete} child of a node names, relative to that node, the subtree(s) to remove — e.g. a node at
   * {@code router "Base"} with child {@code delete -> interface -> "to-r2"} removes {@code router
   * "Base" interface "to-r2"}. Deleting an absent element is a silent no-op (MD-CLI §4.5).
   */
  private void applyDeletes(SrosStatementTree node) {
    for (String verb : new String[] {"delete", "-"}) {
      SrosStatementTree deleteNode = node.getChild(verb);
      if (deleteNode != null) {
        for (List<String> targetPath : leafPaths(deleteNode)) {
          removePath(node, targetPath);
        }
        node.removeChild(verb);
      }
    }
    // Recurse into remaining children (a copy of values avoids concurrent-modification surprises).
    for (SrosStatementTree child : new ArrayList<>(node.getChildren().values())) {
      applyDeletes(child);
    }
  }

  /** Enumerates every root-to-leaf path under {@code node} (each a deletion target). */
  private static @Nonnull List<List<String>> leafPaths(SrosStatementTree node) {
    List<List<String>> out = new ArrayList<>();
    collectLeafPaths(node, new ArrayDeque<>(), out);
    return out;
  }

  private static void collectLeafPaths(
      SrosStatementTree node, Deque<String> path, List<List<String>> out) {
    if (!node.hasChildren()) {
      out.add(new ArrayList<>(path));
      return;
    }
    for (Map.Entry<String, SrosStatementTree> e : node.getChildren().entrySet()) {
      path.addLast(e.getKey());
      collectLeafPaths(e.getValue(), path, out);
      path.removeLast();
    }
  }

  /** Removes the subtree at {@code path} below {@code from}, if present. */
  private static void removePath(SrosStatementTree from, List<String> path) {
    SrosStatementTree parent = from;
    for (int i = 0; i < path.size() - 1; i++) {
      parent = parent.getChild(path.get(i));
      if (parent == null) {
        return; // Path does not exist: silent no-op.
      }
    }
    if (!path.isEmpty()) {
      parent.removeChild(path.get(path.size() - 1));
    }
  }

  // ---------------------------------------------------------------------------------------------
  // cleanup
  // ---------------------------------------------------------------------------------------------

  /** Removes the {@code groups} definition container; it is inheritance source, not config. */
  private void pruneGroups() {
    SrosStatementTree configure = _root.getChild("configure");
    if (configure != null) {
      configure.removeChild("groups");
    }
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * Navigates from {@code start} along {@code path}, returning the reached node or {@code null}. At
   * each step an exact child match is preferred; failing that, a group-definition regex key of the
   * form {@code "<regex>"} whose inner pattern matches the (unquoted) path word is used, supporting
   * group targets like {@code interface "<int-.*>"}.
   */
  private static @Nullable SrosStatementTree navigate(SrosStatementTree start, List<String> path) {
    SrosStatementTree node = start;
    for (String word : path) {
      SrosStatementTree next = node.getChild(word);
      if (next == null) {
        next = matchRegexKey(node, word);
      }
      if (next == null) {
        return null;
      }
      node = next;
    }
    return node;
  }

  /**
   * Returns the child reached via a {@code "<regex>"} key matching {@code word}, or {@code null}.
   */
  private static @Nullable SrosStatementTree matchRegexKey(SrosStatementTree node, String word) {
    String target = unquote(word);
    for (Map.Entry<String, SrosStatementTree> e : node.getChildren().entrySet()) {
      String key = unquote(e.getKey());
      if (key.length() >= 2 && key.charAt(0) == '<' && key.charAt(key.length() - 1) == '>') {
        String regex = key.substring(1, key.length() - 1);
        try {
          if (Pattern.matches(regex, target)) {
            return e.getValue();
          }
        } catch (PatternSyntaxException ignored) {
          // Not a usable regex; ignore this key.
        }
      }
    }
    return null;
  }

  private static @Nonnull String unquote(String text) {
    if (text.length() >= 2 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
      return text.substring(1, text.length() - 1);
    }
    return text;
  }

  /**
   * A tree node that carries an {@code apply-groups} child, plus its path under {@code configure}.
   */
  private static final class Branch {
    final @Nonnull SrosStatementTree _node;
    final @Nonnull List<String> _path;

    Branch(SrosStatementTree node, List<String> path) {
      _node = node;
      _path = path;
    }
  }

  private final @Nonnull SrosStatementTree _root;
  private final @Nonnull Warnings _w;
}
