package org.batfish.grammar.juniper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Tree used to group input flat statement indices hierarchically according to partial statement
 * text at each level of the input configuration text.
 */
@ParametersAreNonnullByDefault
class FlatStatementTree {

  @VisibleForTesting final @Nonnull Map<String, FlatStatementTree> _children;
  @VisibleForTesting final @Nonnull ImmutableList.Builder<Integer> _lines;
  @VisibleForTesting final @Nullable FlatStatementTree _parent;

  FlatStatementTree() {
    this(null);
  }

  private FlatStatementTree(@Nullable FlatStatementTree parent) {
    _children = new HashMap<>();
    _lines = ImmutableList.builder();
    _parent = parent;
  }

  /** Add {@code flatStatementIndex} to this tree. */
  public void addFlatStatementIndex(int flatStatementIndex) {
    _lines.add(flatStatementIndex);
  }

  private void addFlatStatementsTo(ImmutableSet.Builder<Integer> flatStatementIndices) {
    flatStatementIndices.addAll(_lines.build());
    _children.values().forEach(child -> child.addFlatStatementsTo(flatStatementIndices));
  }

  /** Returns subtree for {@code partialStatementText}. Creates if absent. */
  public @Nonnull FlatStatementTree getOrAddSubtree(String partialStatementText) {
    return _children.computeIfAbsent(partialStatementText, t -> new FlatStatementTree(this));
  }

  /** Returns parent {@link FlatStatementTree}. */
  public @Nullable FlatStatementTree getParent() {
    return _parent;
  }

  /** Returns all flat statement indices stored in this tree. */
  public @Nonnull Set<Integer> getFlatStatementIndices() {
    ImmutableSet.Builder<Integer> flatStatements = ImmutableSet.builder();
    addFlatStatementsTo(flatStatements);
    return flatStatements.build();
  }

  /**
   * Replaces subtree for {@code partialStatementText} with an empty tree.
   *
   * @return the new empty tree
   */
  public FlatStatementTree replaceSubtree(String partialStatementText) {
    _children.remove(partialStatementText);
    return getOrAddSubtree(partialStatementText);
  }
}
