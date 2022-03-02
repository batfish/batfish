package org.batfish.common.topology.broadcast;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.vxlan.VxlanNode;

/**
 * Represents a Layer-2 VNI (virtual network identifier).
 *
 * <p>Each VNI connects to one {@link DeviceBroadcastDomain} on a particular VLAN.
 *
 * <p>VNIs connect to each other via {@link L2VNIHub}.
 */
public final class L2VNI extends Node<L2VNI.Unit> {
  /** There is no data needed for {@link L2VNI}, but we can't have a non-null {@link Void} */
  public enum Unit {
    /** The only legal value. */
    VALUE
  }

  public L2VNI(VxlanNode node) {
    _node = node;
  }

  public @Nonnull VxlanNode getNode() {
    return _node;
  }

  public void connectToVlan(DeviceBroadcastDomain sw, Edge<Unit, Integer> edge) {
    if (_connectedVlan != null) {
      throw new IllegalArgumentException(
          String.format(
              "L2 VNI %s is already connected to switch %s, cannot connect to switch %s",
              _node, _connectedVlan.getHostname(), sw.getHostname()));
    }
    _connectedVlan = sw;
    _connectedVlanEdge = edge;
  }

  public void attachToHub(L2VNIHub hub, Edge<Unit, Unit> edge) {
    if (_attachedHub != null) {
      throw new IllegalArgumentException(
          String.format(
              "L2 VNI %s is already connected to hub %s, cannot connect to hub %s",
              _node, _attachedHub.getName(), hub.getName()));
    }
    _attachedHub = hub;
    _attachedHubEdge = edge;
  }

  public void enter(Unit unit, Set<L3Interface> domain, Set<NodeAndData<?, ?>> visited) {
    if (_connectedVlan != null) {
      assert _connectedVlanEdge != null; // contract
      _connectedVlanEdge
          .traverse(unit)
          .ifPresent(vlan -> _connectedVlan.broadcast(vlan, domain, visited));
    }
  }

  public void exit(Unit unit, Set<L3Interface> domain, Set<NodeAndData<?, ?>> visited) {
    if (_attachedHub != null) {
      assert _attachedHubEdge != null; // contract
      _attachedHubEdge.traverse(unit).ifPresent(u -> _attachedHub.broadcast(u, domain, visited));
    }
  }

  // Internal details
  private @Nullable DeviceBroadcastDomain _connectedVlan;
  private @Nullable Edge<Unit, Integer> _connectedVlanEdge;
  private @Nullable L2VNIHub _attachedHub;
  private @Nullable Edge<Unit, Unit> _attachedHubEdge;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof L2VNI)) {
      return false;
    }
    L2VNI that = (L2VNI) o;
    return _node.equals(that._node);
  }

  @Override
  public int hashCode() {
    return 31 * L2VNI.class.hashCode() + _node.hashCode();
  }

  private final @Nonnull VxlanNode _node;
}
