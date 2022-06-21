package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.edge.L3ToL1;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * A {@link Node} representing a non-bridged layer-3 interface:
 *
 * <ul>
 *   <li>The layer-3 aspect of a logical layer-1 interface
 *   <li>An optionally tagged layer-3 sub-interface of a logical layer-1 interface
 * </ul>
 *
 * See for comparison {@link L3BridgedInterface}.
 */
public final class L3NonBridgedInterface implements L3Interface {

  public L3NonBridgedInterface(NodeInterfacePair iface) {
    _interface = iface;
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    assert _toL1 != null;
    assert _l1Interface != null;
    return ImmutableMap.of(_l1Interface, _toL1);
  }

  public void connectToL1Interface(L1Interface l1Interface, L3ToL1 edge) {
    checkState(
        _l1Interface == null, "Already connected to l1 interface: %s", l1Interface.getInterface());
    _l1Interface = l1Interface;
    _toL1 = edge;
  }

  @Override
  public @Nonnull NodeInterfacePair getInterface() {
    return _interface;
  }

  // Internal implementation details

  @VisibleForTesting
  public L1Interface getL1InterfaceForTest() {
    return _l1Interface;
  }

  @VisibleForTesting
  public L3ToL1 getToL1ForTest() {
    return _toL1;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof L3NonBridgedInterface)) {
      return false;
    }
    L3NonBridgedInterface that = (L3NonBridgedInterface) o;
    return _interface.equals(that._interface);
  }

  @Override
  public int hashCode() {
    return L3NonBridgedInterface.class.hashCode() * 31 + _interface.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_interface", _interface).toString();
  }

  private final @Nonnull NodeInterfacePair _interface;
  private L1Interface _l1Interface;
  private L3ToL1 _toL1;
}
