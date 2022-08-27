package org.batfish.role;

import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An abstract edge between two nodes. */
@ParametersAreNonnullByDefault
public final class NodeEdge implements Comparable<NodeEdge> {

  private final @Nonnull String _n1;
  private final @Nonnull String _n2;

  public NodeEdge(String n1, String n2) {
    _n1 = n1;
    _n2 = n2;
  }

  public @Nonnull String getNode1() {
    return _n1;
  }

  public @Nonnull String getNode2() {
    return _n2;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof NodeEdge)) {
      return false;
    }
    NodeEdge that = (NodeEdge) o;
    return _n1.equals(that._n1) && _n2.equals(that._n2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_n1, _n2);
  }

  @Override
  public int compareTo(NodeEdge o) {
    return Comparator.comparing(NodeEdge::getNode1)
        .thenComparing(NodeEdge::getNode2)
        .compare(this, o);
  }
}
