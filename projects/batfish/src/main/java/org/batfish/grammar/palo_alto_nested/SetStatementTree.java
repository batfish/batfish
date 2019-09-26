package org.batfish.grammar.palo_alto_nested;

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
 * Tree used to group input set statement indices hierarchically according to partial statement text
 * at each level of the input configuration text.
 */
@ParametersAreNonnullByDefault
class SetStatementTree {

  @VisibleForTesting final @Nonnull Map<String, SetStatementTree> _children;
  @VisibleForTesting final @Nonnull ImmutableList.Builder<Integer> _lines;
  @VisibleForTesting final @Nullable SetStatementTree _parent;

  SetStatementTree() {
    this(null);
  }

  private SetStatementTree(@Nullable SetStatementTree parent) {
    _children = new HashMap<>();
    _lines = ImmutableList.builder();
    _parent = parent;
  }

  /** Add {@code setStatementIndex} to this tree. */
  public void addSetStatementIndex(int setStatementIndex) {
    _lines.add(setStatementIndex);
  }

  private void addSetStatementsTo(ImmutableSet.Builder<Integer> setStatementIndices) {
    setStatementIndices.addAll(_lines.build());
    _children.values().forEach(child -> child.addSetStatementsTo(setStatementIndices));
  }

  /** Returns subtree for {@code partialStatementText}. Creates if absent. */
  public @Nonnull SetStatementTree getOrAddSubtree(String partialStatementText) {
    return _children.computeIfAbsent(partialStatementText, t -> new SetStatementTree(this));
  }

  /** Returns parent {@link SetStatementTree}. */
  public @Nullable SetStatementTree getParent() {
    return _parent;
  }

  /** Returns all set statement indices stored in this tree. */
  public @Nonnull Set<Integer> getSetStatementIndices() {
    ImmutableSet.Builder<Integer> setStatements = ImmutableSet.builder();
    addSetStatementsTo(setStatements);
    return setStatements.build();
  }

  /**
   * Replaces subtree for {@code partialStatementText} with an empty tree.
   *
   * @return the new empty tree
   */
  public SetStatementTree replaceSubtree(String partialStatementText) {
    _children.remove(partialStatementText);
    return getOrAddSubtree(partialStatementText);
  }
}
