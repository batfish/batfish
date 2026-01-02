package org.batfish.common.topology;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.util.Comparator;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.collections.NodeInterfacePair;

/** Edge representing Layer 3 neighbors */
@ParametersAreNonnullByDefault
public class Layer3Edge implements Comparable<Layer3Edge> {
  private static final String PROP_NODE_INTERFACE1 = "nodeInterface1";
  private static final String PROP_NODE_INTERFACE2 = "nodeInterface2";
  private static final String PROP_INTERFACE_ADDRESSES1 = "interfaceAddresses1";
  private static final String PROP_INTERFACE_ADDRESSES2 = "interfaceAddresses2";

  private final @Nonnull NodeInterfacePair _nodeInterface1;

  private final @Nonnull NodeInterfacePair _nodeInterface2;

  private final @Nonnull SortedSet<InterfaceAddress> _interfaceAddresses1;

  private final @Nonnull SortedSet<InterfaceAddress> _interfaceAddresses2;

  public Layer3Edge(
      NodeInterfacePair nodeInterface1,
      NodeInterfacePair nodeInterface2,
      SortedSet<InterfaceAddress> interfaceAddresses1,
      SortedSet<InterfaceAddress> interfaceAddresses2) {
    _nodeInterface1 = nodeInterface1;
    _nodeInterface2 = nodeInterface2;
    _interfaceAddresses1 = interfaceAddresses1;
    _interfaceAddresses2 = interfaceAddresses2;
  }

  @JsonCreator
  private static Layer3Edge jsonCreator(
      @JsonProperty(PROP_NODE_INTERFACE1) @Nullable NodeInterfacePair nodeInterface1,
      @JsonProperty(PROP_NODE_INTERFACE2) @Nullable NodeInterfacePair nodeInterface2,
      @JsonProperty(PROP_INTERFACE_ADDRESSES1) @Nullable
          SortedSet<InterfaceAddress> interfaceAddresses1,
      @JsonProperty(PROP_INTERFACE_ADDRESSES2) @Nullable
          SortedSet<InterfaceAddress> interfaceAddresses2) {
    checkArgument(nodeInterface1 != null, "Missing %s", PROP_NODE_INTERFACE1);
    checkArgument(nodeInterface2 != null, "Missing %s", PROP_NODE_INTERFACE2);
    return new Layer3Edge(
        nodeInterface1,
        nodeInterface2,
        firstNonNull(interfaceAddresses1, ImmutableSortedSet.of()),
        firstNonNull(interfaceAddresses2, ImmutableSortedSet.of()));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Layer3Edge)) {
      return false;
    }
    Layer3Edge that = (Layer3Edge) o;
    return Objects.equals(_interfaceAddresses1, that._interfaceAddresses1)
        && Objects.equals(_interfaceAddresses2, that._interfaceAddresses2)
        && Objects.equals(_nodeInterface1, that._nodeInterface1)
        && Objects.equals(_nodeInterface2, that._nodeInterface2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _nodeInterface1, _nodeInterface2, _interfaceAddresses1, _interfaceAddresses2);
  }

  @JsonProperty(PROP_NODE_INTERFACE1)
  public @Nonnull NodeInterfacePair getNodeInterface1() {
    return _nodeInterface1;
  }

  @JsonProperty(PROP_NODE_INTERFACE2)
  public @Nonnull NodeInterfacePair getNodeInterface2() {
    return _nodeInterface2;
  }

  @JsonProperty(PROP_INTERFACE_ADDRESSES1)
  public @Nonnull SortedSet<InterfaceAddress> getInterfaceAddresses1() {
    return _interfaceAddresses1;
  }

  @JsonProperty(PROP_INTERFACE_ADDRESSES2)
  public @Nonnull SortedSet<InterfaceAddress> getInterfaceAddresses2() {
    return _interfaceAddresses2;
  }

  @Override
  public int compareTo(Layer3Edge o) {
    return Comparator.comparing(Layer3Edge::getNodeInterface1)
        .thenComparing(Layer3Edge::getNodeInterface2)
        .thenComparing(
            Layer3Edge::getInterfaceAddresses1, Comparators.lexicographical(Ordering.natural()))
        .thenComparing(
            Layer3Edge::getInterfaceAddresses2, Comparators.lexicographical(Ordering.natural()))
        .compare(this, o);
  }
}
