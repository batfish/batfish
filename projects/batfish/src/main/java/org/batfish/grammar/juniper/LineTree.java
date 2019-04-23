package org.batfish.grammar.juniper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Tree used to group input line indices by configuration hierarchy */
@ParametersAreNonnullByDefault
class LineTree {

  static class Node {

    private final @Nonnull Map<String, Node> _children;
    private final @Nonnull ImmutableList.Builder<Integer> _lines;
    private final @Nullable Node _parent;

    Node(@Nullable Node parent) {
      _children = new HashMap<>();
      _lines = ImmutableList.builder();
      _parent = parent;
    }

    public void addSetStatementsTo(ImmutableSet.Builder<Integer> setStatementIndices) {
      setStatementIndices.addAll(_lines.build());
      _children.values().forEach(child -> child.addSetStatementsTo(setStatementIndices));
    }

    public @Nonnull Map<String, Node> getChildren() {
      return _children;
    }

    public @Nonnull ImmutableList.Builder<Integer> getLines() {
      return _lines;
    }

    public @Nullable Node getParent() {
      return _parent;
    }
  }

  private final @Nonnull Node _root;

  LineTree() {
    _root = new Node(null);
  }

  public @Nonnull Node getRoot() {
    return _root;
  }

  public @Nonnull Set<Integer> getSetStatements() {
    ImmutableSet.Builder<Integer> setStatements = ImmutableSet.builder();
    _root.addSetStatementsTo(setStatements);
    return setStatements.build();
  }
}
