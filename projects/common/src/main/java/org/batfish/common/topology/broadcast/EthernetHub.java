package org.batfish.common.topology.broadcast;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Sends all frames received out all interfaces. Used to reduce quadratic complexity to linear for
 * physical-physical Ethernet links, and to prevent loops.
 *
 * <p>Only connects to {@link PhysicalInterface}.
 */
public final class EthernetHub extends Node<EthernetTag> {
  public EthernetHub(String id) {
    _id = id;
    _attachedInterfaces = new HashMap<>();
  }

  public @Nonnull String getId() {
    return _id;
  }

  public void addAttachedInterface(PhysicalInterface iface, Edge<EthernetTag, EthernetTag> edge) {
    Edge<EthernetTag, EthernetTag> oldEdge = _attachedInterfaces.putIfAbsent(iface, edge);
    assert oldEdge == null;
  }

  public void broadcast(EthernetTag tag, Set<L3Interface> domain, Set<NodeAndData<?, ?>> visited) {
    if (!visited.add(new NodeAndData<>(this, tag))) {
      return;
    }

    _attachedInterfaces.forEach(
        (iface, edge) ->
            edge.traverse(tag).ifPresent(newTag -> iface.receive(newTag, domain, visited)));
  }

  // Internal implementation details.

  @VisibleForTesting
  @Nonnull
  Map<PhysicalInterface, Edge<EthernetTag, EthernetTag>> getAttachedInterfacesForTesting() {
    return _attachedInterfaces;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof EthernetHub)) {
      return false;
    }
    EthernetHub that = (EthernetHub) o;
    return _id.equals(that._id);
  }

  @Override
  public int hashCode() {
    return 31 * EthernetHub.class.hashCode() + _id.hashCode();
  }

  private final @Nonnull String _id;
  private final @Nonnull Map<PhysicalInterface, Edge<EthernetTag, EthernetTag>> _attachedInterfaces;
}
