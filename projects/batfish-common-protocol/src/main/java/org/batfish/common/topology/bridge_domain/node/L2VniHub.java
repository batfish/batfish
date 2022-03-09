package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.NodeAndState;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.node.L2Vni.Unit;

/**
 * Represents the connection between a set of {@link L2Vni L2 VNIs}. Used to turn cycles (for 3+
 * {@link L2Vni VNIs}) into a graph.
 */
public final class L2VniHub extends Node<Unit> {
  public L2VniHub(String name) {
    _name = name;
    _attachedVNIs = new HashMap<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void attachL2VNI(L2Vni vni, Edge<Unit, Unit> edge) {
    Edge<L2Vni.Unit, L2Vni.Unit> previous = _attachedVNIs.putIfAbsent(vni, edge);
    checkArgument(previous == null, "Cannot connect the same L2VNI %s twice", vni.getNode());
  }

  public void broadcast(L2Vni.Unit unit, Set<L3Interface> domain, Set<NodeAndState<?, ?>> visited) {
    if (!visited.add(new NodeAndState<>(this, L2Vni.Unit.VALUE))) {
      return;
    }

    _attachedVNIs.forEach(
        (vni, edge) -> edge.traverse(unit).ifPresent(u -> vni.enter(u, domain, visited)));
  }

  // Internal details
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof L2VniHub)) {
      return false;
    }
    L2VniHub that = (L2VniHub) o;
    return _name.equals(that._name);
  }

  @Override
  public int hashCode() {
    return 31 * L2VniHub.class.hashCode() + _name.hashCode();
  }

  private final @Nonnull Map<L2Vni, Edge<L2Vni.Unit, L2Vni.Unit>> _attachedVNIs;
  private final @Nonnull String _name;
}
