package org.batfish.vendor.sros.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Intermediate representation of an SR-OS configuration as a tree of per-word path segments.
 *
 * <p>Every word of every canonical statement becomes one level of the tree, so a leaf's value is
 * itself a child node. For example {@code configure card 1 card-type iom-1} produces the path
 * {@code configure -> card -> 1 -> card-type -> iom-1}; reading the card type means looking at the
 * (single) child key under the {@code card-type} node. A {@code policy [ "a" "b" ]} leaf-list
 * produces ordered children {@code "a"}, {@code "b"} under the {@code policy} node.
 *
 * <p>Because the {@link SrosConfigurationBuilder} feeds the same word stream regardless of whether
 * the input was the brace, flat {@code /configure ...}, or mixed form, the resulting tree is
 * identical across all three forms. Children preserve insertion order (a {@link LinkedHashMap}),
 * which matters for {@code ordered-by user} leaf-lists such as BGP import/export policies.
 *
 * <p>The mutators ({@link #removeChild}, {@link #copyInto}) support the preprocessor: applying
 * {@code delete} edits and grafting {@code apply-groups} subtrees onto their inheriting branches.
 *
 * <p>Each node records the {@link ParserRuleContext}(s) of the statement(s) that created it (see
 * {@link #addDefContext}). This is the configuration's source provenance: the deepest node of a
 * statement is both its value node (e.g. {@code card-type -> iom-1}, context = that one line) and,
 * for a keyed list entry, its structure-key node (e.g. {@code router -> "Base"}, context = the
 * whole brace block from {@code start} to {@code stop}). Extraction uses these contexts to emit
 * line-stamped {@link org.batfish.common.Warnings.ParseWarning}s for bad values and to record
 * structure definitions/references. A node may carry several contexts when the same path is
 * configured by more than one statement (mixed brace + flat input, or apply-groups inheritance,
 * which copies the source group's contexts via {@link #copyInto}). The contexts are transient: this
 * tree is built per-parse and discarded after extraction, never serialized into the configuration.
 *
 * <p><b>Visit tracking.</b> To let the {@code annotate} tool report SR-OS statements that are
 * silently ignored (parsed and accepted, but never read by {@link SrosFeatureExtractor}), every
 * tree shares a {@link VisitRecorder}. While the recorder is {@linkplain #beginVisitTracking()
 * active} the read accessors ({@link #getChild}, {@link #getChildren}, {@link #hasChildren}) record
 * the nodes they touch. The extractor's normal reads thus mark visited nodes automatically — it
 * needs no visit-tracking code of its own. Tracking is activated only for the feature-extraction
 * pass (not the build or the {@link SrosPreprocessor} walk, whose accesses must not count as
 * "read"), after which {@link SrosSilentSyntax} sweeps the tree for unvisited subtrees. The
 * recorder is transient, like the contexts: it is per-parse and never serialized.
 */
@ParametersAreNonnullByDefault
public final class SrosStatementTree {

  /** Creates a root node, with a fresh (inactive) visit recorder shared by the whole tree. */
  public SrosStatementTree() {
    this(new VisitRecorder());
  }

  private SrosStatementTree(VisitRecorder recorder) {
    _children = new LinkedHashMap<>();
    _defContexts = new ArrayList<>(1);
    _recorder = recorder;
  }

  /** Returns the child subtree for {@code word}, creating it (sharing the recorder) if absent. */
  public @Nonnull SrosStatementTree getOrAddChild(String word) {
    return _children.computeIfAbsent(word, w -> new SrosStatementTree(_recorder));
  }

  /** Returns the child subtree for {@code word}, or {@code null} if absent. */
  public @Nullable SrosStatementTree getChild(String word) {
    _recorder.record(this);
    SrosStatementTree child = _children.get(word);
    if (child != null) {
      _recorder.record(child);
    }
    return child;
  }

  /** The children of this node, keyed by path word, in insertion order. */
  public @Nonnull Map<String, SrosStatementTree> getChildren() {
    _recorder.record(this);
    _children.values().forEach(_recorder::record);
    return _children;
  }

  public boolean hasChildren() {
    _recorder.record(this);
    return !_children.isEmpty();
  }

  /**
   * Records the parse-tree context of a statement that defines this node (its source provenance).
   */
  public void addDefContext(ParserRuleContext ctx) {
    _defContexts.add(ctx);
  }

  /**
   * The parse-tree contexts of the statements that created this node, in the order seen. Empty for
   * the implicit root and for nodes that exist only as path prefixes of a deeper statement.
   */
  public @Nonnull List<ParserRuleContext> getDefContexts() {
    return _defContexts;
  }

  /**
   * The first defining context, or {@code null} if this node has none (e.g. an internal path word).
   */
  public @Nullable ParserRuleContext firstDefContext() {
    return _defContexts.isEmpty() ? null : _defContexts.get(0);
  }

  /** Removes the child subtree keyed by {@code word} (no-op if absent). Used by {@code delete}. */
  public void removeChild(String word) {
    _children.remove(word);
  }

  /**
   * Deep-merges this tree's structure into {@code target}: every path present here is ensured to
   * exist under {@code target}, without overwriting branches {@code target} already has at the leaf
   * level. Used to expand {@code apply-groups} (group content is inherited only where the target
   * does not already configure it — local config wins).
   */
  public void copyInto(SrosStatementTree target) {
    for (Map.Entry<String, SrosStatementTree> e : _children.entrySet()) {
      String word = e.getKey();
      SrosStatementTree sourceChild = e.getValue();
      SrosStatementTree existing = target.getChild(word);
      if (existing == null) {
        SrosStatementTree created = target.getOrAddChild(word);
        // Carry the group's source contexts onto the inherited node, so a warning about an
        // inherited value (or a reference inherited from the group) points back at the group
        // definition rather than at nothing.
        created._defContexts.addAll(sourceChild._defContexts);
        sourceChild.copyInto(created);
      } else {
        // Branch already present in target. Recurse so deeper, non-conflicting group content is
        // still inherited, but do not disturb values the target already set (local wins).
        sourceChild.copyInto(existing);
      }
    }
  }

  // --- visit tracking (annotate-only; see class javadoc) ----------------------------------------

  /**
   * Begins recording read accesses on this whole tree. Call once, on the root, immediately before
   * the feature-extraction pass so that {@link SrosFeatureExtractor}'s reads mark visited nodes.
   */
  public void beginVisitTracking() {
    _recorder.activate();
  }

  /**
   * Stops recording read accesses. Call before {@link SrosSilentSyntax} sweeps the tree, so the
   * sweep's own accessor calls do not mark nodes as visited.
   */
  public void endVisitTracking() {
    _recorder.deactivate();
  }

  /** Whether this node was read while visit tracking was active. */
  public boolean wasVisited() {
    return _recorder.isVisited(this);
  }

  private final @Nonnull Map<String, SrosStatementTree> _children;
  private final @Nonnull List<ParserRuleContext> _defContexts;

  /** The visit recorder shared by every node of one tree. */
  private final @Nonnull VisitRecorder _recorder;

  /**
   * Records which {@link SrosStatementTree} nodes are read while {@linkplain #active active}. One
   * instance is shared by all nodes of a tree (threaded through {@link #getOrAddChild}). Recording
   * is by object identity; it is a no-op until {@link #activate} is called, so the build and the
   * {@link SrosPreprocessor} walk do not count as reads.
   */
  private static final class VisitRecorder {
    private boolean _active;
    private final @Nonnull Set<SrosStatementTree> _visited =
        Collections.newSetFromMap(new IdentityHashMap<>());

    void activate() {
      _active = true;
    }

    void deactivate() {
      _active = false;
    }

    void record(SrosStatementTree node) {
      if (_active) {
        _visited.add(node);
      }
    }

    boolean isVisited(SrosStatementTree node) {
      return _visited.contains(node);
    }
  }
}
