package org.batfish.common.topology.broadcast;

import java.util.Objects;
import javax.annotation.Nonnull;

/** Represents a specific piece of data being processed at a specific node. */
public final class NodeAndData<D, N extends Node<D>> {
  public NodeAndData(N node, D data) {
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
    } else if (!(o instanceof NodeAndData)) {
      return false;
    }
    NodeAndData<?, ?> that = (NodeAndData<?, ?>) o;
    return _node.equals(that._node) && _data.equals(that._data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_node, _data);
  }

  private final @Nonnull N _node;
  private final @Nonnull D _data;
}
