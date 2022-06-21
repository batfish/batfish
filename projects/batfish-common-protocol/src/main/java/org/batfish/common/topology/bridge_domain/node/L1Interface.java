package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.edge.L1ToL1Hub;
import org.batfish.common.topology.bridge_domain.edge.L1ToL2;
import org.batfish.common.topology.bridge_domain.edge.L1ToL3;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * A logical layer-1 Ethernet interface sends possibly-tagged frames.
 *
 * <p>Can have edges to any number of {@link L3NonBridgedInterface}s when receiving frames tagged
 * for a dot1q encapsulated subinterface/untagged frames directly, and/or to any number of {@link
 * L2Interface}s that handle untagged/tagged frames. The sets of tags handled by the {@link
 * L2Interface}s and {@link L3NonBridgedInterface}s must be mutually disjoint.
 */
public final class L1Interface implements Node {

  public L1Interface(NodeInterfacePair iface) {
    _interface = iface;
    _toL2 = new HashMap<>();
    _toL3 = new HashMap<>();
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    ImmutableMap.Builder<Node, Edge> builder =
        ImmutableMap.<Node, Edge>builder().putAll(_toL2).putAll(_toL3);
    if (_hub != null) {
      builder.put(_hub, L1ToL1Hub.instance());
    }
    return builder.build();
  }

  public void connectToHub(L1Hub hub) {
    checkState(_hub == null, "Cannot connect an l1 interface to multiple l1 hubs");
    _hub = hub;
  }

  public void connectToL2Interface(L2Interface l2Interface, L1ToL2 edge) {
    checkState(
        !_toL2.containsKey(l2Interface),
        "Already connected to L2 interface: %s",
        l2Interface.getInterface());
    _toL2.put(l2Interface, edge);
  }

  public void connectToL3NonBridgedInterface(
      L3NonBridgedInterface l3NonBridgedInterface, L1ToL3 edge) {
    checkState(
        !_toL3.containsKey(l3NonBridgedInterface),
        "Already connected to non-bridged L3 interface: %s",
        l3NonBridgedInterface.getInterface());
    _toL3.put(l3NonBridgedInterface, edge);
  }

  public @Nonnull NodeInterfacePair getInterface() {
    return _interface;
  }

  // Internal implementation details.

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof L1Interface)) {
      return false;
    }
    L1Interface that = (L1Interface) o;
    return _interface.equals(that._interface);
  }

  @Override
  public int hashCode() {
    return L1Interface.class.hashCode() * 31 + _interface.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_interface", _interface).toString();
  }

  @VisibleForTesting
  public L1Hub getHubForTest() {
    return _hub;
  }

  @VisibleForTesting
  public Map<L2Interface, L1ToL2> getToL2ForTest() {
    return _toL2;
  }

  @VisibleForTesting
  public Map<L3NonBridgedInterface, L1ToL3> getToL3ForTest() {
    return _toL3;
  }

  private final @Nonnull NodeInterfacePair _interface;
  private @Nullable L1Hub _hub;

  private @Nonnull Map<L2Interface, L1ToL2> _toL2;
  private @Nonnull Map<L3NonBridgedInterface, L1ToL3> _toL3;
}
