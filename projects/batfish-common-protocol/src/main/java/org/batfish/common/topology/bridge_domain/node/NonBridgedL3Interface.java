package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.edge.NonBridgedL3ToPhysical;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * A {@link Node} representing a non-bridged layer-3 interface:
 *
 * <ul>
 *   <li>The layer-3 aspect of a physical or aggregated interface
 *   <li>An optionally tagged layer-3 sub-interface of a physical or aggregated interface
 * </ul>
 *
 * See for comparison {@link BridgedL3Interface}.
 */
public final class NonBridgedL3Interface implements L3Interface {

  public NonBridgedL3Interface(NodeInterfacePair iface) {
    _interface = iface;
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    assert _toPhysical != null;
    assert _physicalInterface != null;
    return ImmutableMap.of(_physicalInterface, _toPhysical);
  }

  public void connectToPhysicalInterface(
      PhysicalInterface physicalInterface, NonBridgedL3ToPhysical edge) {
    checkState(
        _physicalInterface == null,
        "Already connected to physical interface: %s",
        physicalInterface.getInterface());
    _physicalInterface = physicalInterface;
    _toPhysical = edge;
  }

  @Override
  public @Nonnull NodeInterfacePair getInterface() {
    return _interface;
  }

  // Internal implementation details

  @VisibleForTesting
  public PhysicalInterface getPhysicalInterfaceForTest() {
    return _physicalInterface;
  }

  @VisibleForTesting
  public NonBridgedL3ToPhysical getToPhysicalForTest() {
    return _toPhysical;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof NonBridgedL3Interface)) {
      return false;
    }
    NonBridgedL3Interface that = (NonBridgedL3Interface) o;
    return _interface.equals(that._interface);
  }

  @Override
  public int hashCode() {
    return NonBridgedL3Interface.class.hashCode() * 31 + _interface.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_interface", _interface).toString();
  }

  private final @Nonnull NodeInterfacePair _interface;
  private PhysicalInterface _physicalInterface;
  private NonBridgedL3ToPhysical _toPhysical;
}
