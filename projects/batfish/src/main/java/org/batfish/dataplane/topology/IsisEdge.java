package org.batfish.dataplane.topology;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IsisLevel;

public abstract class IsisEdge implements Comparable<IsisEdge> {

  private final IsisLevel _circuitType;

  private final IsisNode _node1;

  private final IsisNode _node2;

  protected IsisEdge(
      @Nonnull IsisLevel circuitType, @Nonnull IsisNode node1, @Nonnull IsisNode node2) {
    _circuitType = circuitType;
    _node1 = node1;
    _node2 = node2;
  }

  @Override
  public final int compareTo(IsisEdge o) {
    return Comparator.comparing(IsisEdge::getNode1)
        .thenComparing(IsisEdge::getNode2)
        .thenComparing(IsisEdge::getCircuitType)
        .compare(this, o);
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    IsisEdge rhs = (IsisEdge) o;
    return _circuitType == rhs._circuitType
        && _node1.equals(rhs._node1)
        && _node2.equals(rhs._node2);
  }

  public final @Nonnull IsisLevel getCircuitType() {
    return _circuitType;
  }

  public final @Nonnull IsisNode getNode1() {
    return _node1;
  }

  public final @Nonnull IsisNode getNode2() {
    return _node2;
  }

  @Override
  public final int hashCode() {
    return Objects.hash(_circuitType.ordinal(), _node1, _node2);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass())
        .add("node1", _node1)
        .add("node2", _node2)
        .add("circuitType", _circuitType)
        .toString();
  }
}
