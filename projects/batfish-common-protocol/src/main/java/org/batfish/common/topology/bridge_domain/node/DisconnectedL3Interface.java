package org.batfish.common.topology.bridge_domain.node;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * A layer-3 interface disconnected from the broadcast domain search graph:
 *
 * <ul>
 *   <li>A tunnel interface, for which adjacency is determined by outside code.
 *   <li>A layer-3 interface that is invalid in some way.
 * </ul>
 */
public final class DisconnectedL3Interface implements L3Interface {

  public DisconnectedL3Interface(NodeInterfacePair iface) {
    _interface = iface;
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    return ImmutableMap.of();
  }

  @Override
  public @Nonnull NodeInterfacePair getInterface() {
    return _interface;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof DisconnectedL3Interface)) {
      return false;
    }
    DisconnectedL3Interface that = (DisconnectedL3Interface) o;
    return _interface.equals(that._interface);
  }

  @Override
  public int hashCode() {
    return DisconnectedL3Interface.class.hashCode() * 31 + _interface.hashCode();
  }

  private final @Nonnull NodeInterfacePair _interface;
}
