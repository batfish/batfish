package org.batfish.grammar.flatjuniper;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Tree used to group input statements hierarchically according to partial statement text, where
 * each word of a statement corresponds to a subtree.
 */
@ParametersAreNonnullByDefault
class StatementTree {

  @VisibleForTesting final @Nonnull Map<String, StatementTree> _children;
  @VisibleForTesting final @Nullable StatementTree _parent;

  StatementTree() {
    this(null);
  }

  private StatementTree(@Nullable StatementTree parent) {
    _children = new HashMap<>();
    _parent = parent;
  }

  /** Returns subtree for {@code partialStatementText}. Creates if absent. */
  public @Nonnull StatementTree getOrAddSubtree(String partialStatementText) {
    return _children.computeIfAbsent(partialStatementText, t -> new StatementTree(this));
  }

  /** Returns parent {@link StatementTree}. */
  public @Nullable StatementTree getParent() {
    return _parent;
  }

  /** Deletes subtree keyed by {@code word}. */
  public void deleteSubtree(String word) {
    _children.remove(word);
  }

  /** Return a stream of all subtrees, including self. */
  public @Nonnull Stream<StatementTree> getSubtrees() {
    return Stream.concat(
        Stream.of(this), _children.values().stream().flatMap(StatementTree::getSubtrees));
  }
}
