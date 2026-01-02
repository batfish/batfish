package org.batfish.datamodel.eigrp;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;

public final class EigrpEdge implements Serializable, Comparable<EigrpEdge> {
  private static final String PROP_NODE1 = "node1";
  private static final String PROP_NODE2 = "node2";

  private final @Nonnull EigrpNeighborConfigId _node1;
  private final @Nonnull EigrpNeighborConfigId _node2;

  @JsonCreator
  public EigrpEdge(
      @JsonProperty(PROP_NODE1) @Nonnull EigrpNeighborConfigId node1,
      @JsonProperty(PROP_NODE2) @Nonnull EigrpNeighborConfigId node2) {
    _node1 = node1;
    _node2 = node2;
  }

  @Override
  public int compareTo(@Nonnull EigrpEdge o) {
    return Comparator.comparing(EigrpEdge::getNode1)
        .thenComparing(EigrpEdge::getNode2)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EigrpEdge)) {
      return false;
    }
    EigrpEdge rhs = (EigrpEdge) o;
    return _node1.equals(rhs._node1) && _node2.equals(rhs._node2);
  }

  @JsonProperty(PROP_NODE1)
  public @Nonnull EigrpNeighborConfigId getNode1() {
    return _node1;
  }

  @JsonProperty(PROP_NODE2)
  public @Nonnull EigrpNeighborConfigId getNode2() {
    return _node2;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_node1, _node2);
  }

  public @Nonnull EigrpEdge reverse() {
    return new EigrpEdge(_node2, _node1);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add("node1", _node1).add("node2", _node2).toString();
  }
}
