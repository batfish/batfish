package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** A (directional) OSPF session. */
@ParametersAreNonnullByDefault
public final class OspfEdge implements Comparable<OspfEdge> {
  private static final String PROP_NODE1 = "node1";
  private static final String PROP_NODE2 = "node2";

  @Nonnull private final OspfNode _node1;
  @Nonnull private final OspfNode _node2;

  @JsonCreator
  private static OspfEdge jsonCreator(
      @Nullable @JsonProperty(PROP_NODE1) OspfNode node1,
      @Nullable @JsonProperty(PROP_NODE2) OspfNode node2) {
    checkArgument(node1 != null, "Missing %s", PROP_NODE1);
    checkArgument(node2 != null, "Missing %s", PROP_NODE2);
    return new OspfEdge(node1, node2);
  }

  public OspfEdge(OspfNode node1, OspfNode node2) {
    _node1 = node1;
    _node2 = node2;
  }

  @JsonProperty(PROP_NODE1)
  @Nonnull
  public OspfNode getNode1() {
    return _node1;
  }

  @JsonProperty(PROP_NODE2)
  @Nonnull
  public OspfNode getNode2() {
    return _node2;
  }

  /** Returns an {@link OspfEdge} pointing in the reverse direction. */
  @Nonnull
  public OspfEdge reverse() {
    return new OspfEdge(_node2, _node1);
  }

  @Override
  public int compareTo(OspfEdge o) {
    return Comparator.comparing(OspfEdge::getNode1)
        .thenComparing(OspfEdge::getNode2)
        .compare(this, o);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OspfEdge)) {
      return false;
    }
    OspfEdge rhs = (OspfEdge) o;
    return _node1.equals(rhs._node1) && _node2.equals(rhs._node2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_node1, _node2);
  }

  @Override
  public String toString() {
    return toStringHelper(getClass()).add(PROP_NODE1, _node1).add(PROP_NODE2, _node2).toString();
  }
}
