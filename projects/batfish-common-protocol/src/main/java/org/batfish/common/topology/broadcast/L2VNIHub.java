package org.batfish.common.topology.broadcast;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Represents the connection between a set of {@link L2VNI L2 VNIs}. Used to turn cycles (for 3+
 * {@link L2VNI VNIs}) into a graph.
 */
public final class L2VNIHub extends Node<L2VNI.Unit> {
  public L2VNIHub(String name) {
    _name = name;
    _attachedVNIs = new HashMap<>();
  }

  public @Nonnull String getName() {
    return _name;
  }

  public void attachL2VNI(L2VNI vni, Edge<L2VNI.Unit, L2VNI.Unit> edge) {
    Edge<L2VNI.Unit, L2VNI.Unit> previous = _attachedVNIs.putIfAbsent(vni, edge);
    checkArgument(previous == null, "Cannot connect the same L2VNI %s twice", vni.getNode());
  }

  public void broadcast(L2VNI.Unit unit, Set<L3Interface> domain, Set<NodeAndData<?, ?>> visited) {
    if (!visited.add(new NodeAndData<>(this, L2VNI.Unit.VALUE))) {
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
    } else if (!(o instanceof L2VNIHub)) {
      return false;
    }
    L2VNIHub that = (L2VNIHub) o;
    return _name.equals(that._name);
  }

  @Override
  public int hashCode() {
    return 31 * L2VNIHub.class.hashCode() + _name.hashCode();
  }

  private final @Nonnull Map<L2VNI, Edge<L2VNI.Unit, L2VNI.Unit>> _attachedVNIs;
  private final @Nonnull String _name;
}
