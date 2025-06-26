package org.batfish.grammar.hierarchical;

import com.google.common.collect.ImmutableList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Tree used to group input statements hierarchically according to partial statement text, where
 * each word of a statement corresponds to a subtree.
 */
@ParametersAreNonnullByDefault
public class StatementTree {

  /** Returns subtree for {@code partialStatementText}. Creates if absent. */
  public @Nonnull StatementTree getOrAddSubtree(String partialStatementText) {
    return _children.computeIfAbsent(partialStatementText, t -> new StatementTree(this));
  }

  /** Returns subtree for {@code partialStatementText} if it exists, else {@code null}. */
  public @Nullable StatementTree getSubtree(String partialStatementText) {
    return _children.get(partialStatementText);
  }

  /** Returns parent {@link StatementTree}. */
  public @Nullable StatementTree getParent() {
    return _parent;
  }

  /** Deletes all subtrees. */
  public void deleteAllSubtrees() {
    _children.clear();
  }

  /** Deletes subtree keyed by {@code word}. */
  public void deleteSubtree(String word) {
    _children.remove(word);
  }

  /** Return a stream of all subtrees in pre-order, including self. */
  public @Nonnull Stream<StatementTree> getSubtrees() {
    return Stream.concat(
        Stream.of(this), _children.values().stream().flatMap(StatementTree::getSubtrees));
  }

  public StatementTree() {
    this(null);
  }

  private final @Nonnull Map<String, StatementTree> _children;
  private final @Nullable StatementTree _parent;

  private StatementTree(@Nullable StatementTree parent) {
    _children = new LinkedHashMap<>();
    _parent = parent;
  }

  /** Insert a new tree with its new key before an existing reference key. */
  public void insertBefore(String referenceKey, String newKey, StatementTree newTree) {
    List<Entry<String, StatementTree>> entries = ImmutableList.copyOf(_children.entrySet());
    _children.clear();
    for (Entry<String, StatementTree> entry : entries) {
      String key = entry.getKey();
      if (key.equals(referenceKey)) {
        _children.put(newKey, newTree);
      }
      _children.put(key, entry.getValue());
    }
  }

  /** Insert a new tree with its new key after an existing reference key. */
  public void insertAfter(String referenceKey, String newKey, StatementTree newTree) {
    List<Entry<String, StatementTree>> entries = ImmutableList.copyOf(_children.entrySet());
    _children.clear();
    for (Entry<String, StatementTree> entry : entries) {
      String key = entry.getKey();
      _children.put(key, entry.getValue());
      if (key.equals(referenceKey)) {
        _children.put(newKey, newTree);
      }
    }
  }

  /** Insert a new tree with its new key as the first child. */
  public void insertTop(String newKey, StatementTree newTree) {
    List<Entry<String, StatementTree>> entries = ImmutableList.copyOf(_children.entrySet());
    _children.clear();
    _children.put(newKey, newTree);
    for (Entry<String, StatementTree> entry : entries) {
      _children.put(entry.getKey(), entry.getValue());
    }
  }

  /** Insert a new tree with its new key as the last child. */
  public void insertBottom(String newKey, StatementTree newTree) {
    _children.put(newKey, newTree);
  }
}
