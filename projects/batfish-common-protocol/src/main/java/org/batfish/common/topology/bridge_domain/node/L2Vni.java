package org.batfish.common.topology.bridge_domain.node;

import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.NodeAndState;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.node.L2Vni.Unit;
import org.batfish.datamodel.vxlan.VxlanNode;

/**
 * Represents a Layer-2 VNI (virtual network identifier).
 *
 * <p>Each VNI connects to one {@link BridgeDomain} on a particular VLAN.
 *
 * <p>VNIs connect to each other via {@link L2VniHub}.
 */
public final class L2Vni extends Node<Unit> {
  /** There is no data needed for {@link L2Vni}, but we can't have a non-null {@link Void} */
  public enum Unit {
    /** The only legal value. */
    VALUE
  }

  public L2Vni(VxlanNode node) {
    _node = node;
  }

  public @Nonnull VxlanNode getNode() {
    return _node;
  }

  public void connectToVlan(BridgeDomain sw, Edge<Unit, Integer> edge) {
    if (_connectedVlan != null) {
      throw new IllegalArgumentException(
          String.format(
              "L2 VNI %s is already connected to switch %s, cannot connect to switch %s",
              _node, _connectedVlan.getHostname(), sw.getHostname()));
    }
    _connectedVlan = sw;
    _connectedVlanEdge = edge;
  }

  public void attachToHub(L2VniHub hub, Edge<Unit, Unit> edge) {
    if (_attachedHub != null) {
      throw new IllegalArgumentException(
          String.format(
              "L2 VNI %s is already connected to hub %s, cannot connect to hub %s",
              _node, _attachedHub.getName(), hub.getName()));
    }
    _attachedHub = hub;
    _attachedHubEdge = edge;
  }

  public void enter(Unit unit, Set<L3Interface> domain, Set<NodeAndState<?, ?>> visited) {
    if (_connectedVlan != null) {
      assert _connectedVlanEdge != null; // contract
      _connectedVlanEdge
          .traverse(unit)
          .ifPresent(vlan -> _connectedVlan.broadcast(vlan, domain, visited));
    }
  }

  public void exit(Unit unit, Set<L3Interface> domain, Set<NodeAndState<?, ?>> visited) {
    if (_attachedHub != null) {
      assert _attachedHubEdge != null; // contract
      _attachedHubEdge.traverse(unit).ifPresent(u -> _attachedHub.broadcast(u, domain, visited));
    }
  }

  // Internal details
  private @Nullable BridgeDomain _connectedVlan;
  private @Nullable Edge<Unit, Integer> _connectedVlanEdge;
  private @Nullable L2VniHub _attachedHub;
  private @Nullable Edge<Unit, Unit> _attachedHubEdge;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof L2Vni)) {
      return false;
    }
    L2Vni that = (L2Vni) o;
    return _node.equals(that._node);
  }

  @Override
  public int hashCode() {
    return 31 * L2Vni.class.hashCode() + _node.hashCode();
  }

  private final @Nonnull VxlanNode _node;
}
