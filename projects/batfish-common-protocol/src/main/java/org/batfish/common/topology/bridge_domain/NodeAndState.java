package org.batfish.common.topology.bridge_domain;

import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.node.Node;

/** Represents a specific piece of data being processed at a specific node. */
public final class NodeAndState<D, N extends Node<D>> {
  public NodeAndState(N node, D data) {
    _node = node;
    _data = data;
  }

  public @Nonnull N getNode() {
    return _node;
  }

  public @Nonnull D getData() {
    return _data;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof NodeAndState)) {
      return false;
    }
    NodeAndState<?, ?> that = (NodeAndState<?, ?>) o;
    return _node.equals(that._node) && _data.equals(that._data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_node, _data);
  }

  private final @Nonnull N _node;
  private final @Nonnull D _data;
}
