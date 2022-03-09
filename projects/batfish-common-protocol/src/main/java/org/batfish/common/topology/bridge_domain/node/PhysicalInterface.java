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
import org.batfish.common.topology.bridge_domain.edge.PhysicalToEthernetHub;
import org.batfish.common.topology.bridge_domain.edge.PhysicalToL2;
import org.batfish.common.topology.bridge_domain.edge.PhysicalToNonBridgedL3;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * A physical Ethernet interface sends possibly-tagged frames.
 *
 * <p>Can have edges to any number of {@link NonBridgedL3Interface}s when receiving frames tagged
 * for a dot1q encapsulated subinterface/untagged frames directly, and/or to any number of {@link
 * L2Interface}s that handle untagged/tagged frames. The sets of tags handled by the {@link
 * L2Interface}s and {@link NonBridgedL3Interface}s must be mutually disjoint.
 */
public final class PhysicalInterface implements Node {

  public PhysicalInterface(NodeInterfacePair iface) {
    _interface = iface;
    _toL2 = new HashMap<>();
    _toNonBridgedL3 = new HashMap<>();
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    ImmutableMap.Builder<Node, Edge> builder =
        ImmutableMap.<Node, Edge>builder().putAll(_toL2).putAll(_toNonBridgedL3);
    if (_attachedHub != null) {
      builder.put(_attachedHub, _toEthernetHub);
    }
    return builder.build();
  }

  public void attachToHub(EthernetHub hub) {
    checkState(
        _attachedHub == null, "Cannot connect a physical interface to multiple Ethernet hubs");
    _attachedHub = hub;
    _toEthernetHub = PhysicalToEthernetHub.instance();
  }

  public void connectToL2Interface(L2Interface l2Interface, PhysicalToL2 edge) {
    checkState(
        !_toL2.containsKey(l2Interface),
        "Already connected to L2 interface: %s",
        l2Interface.getInterface());
    _toL2.put(l2Interface, edge);
  }

  public void connectToNonBridgedL3Interface(
      NonBridgedL3Interface nonBridgedL3Interface, PhysicalToNonBridgedL3 edge) {
    checkState(
        !_toNonBridgedL3.containsKey(nonBridgedL3Interface),
        "Already connected to non-bridged L3 interface: %s",
        nonBridgedL3Interface.getInterface());
    _toNonBridgedL3.put(nonBridgedL3Interface, edge);
  }

  public @Nonnull NodeInterfacePair getInterface() {
    return _interface;
  }

  // Internal implementation details.

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof PhysicalInterface)) {
      return false;
    }
    PhysicalInterface that = (PhysicalInterface) o;
    return _interface.equals(that._interface);
  }

  @Override
  public int hashCode() {
    return PhysicalInterface.class.hashCode() * 31 + _interface.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_interface", _interface).toString();
  }

  @VisibleForTesting
  public EthernetHub getAttachedHubForTest() {
    return _attachedHub;
  }

  @VisibleForTesting
  public Map<L2Interface, PhysicalToL2> getToL2ForTest() {
    return _toL2;
  }

  @VisibleForTesting
  public Map<NonBridgedL3Interface, PhysicalToNonBridgedL3> getToNonBridgedL3ForTest() {
    return _toNonBridgedL3;
  }

  private final @Nonnull NodeInterfacePair _interface;

  private final @Nonnull Map<L2Interface, PhysicalToL2> _toL2;
  private final @Nonnull Map<NonBridgedL3Interface, PhysicalToNonBridgedL3> _toNonBridgedL3;
  private PhysicalToEthernetHub _toEthernetHub;
  private EthernetHub _attachedHub;
}
