package org.batfish.common.topology.bridge_domain;

import static com.google.common.base.MoreObjects.toStringHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.node.Node;

/** A pair of a {@link Node} and a {@link State} seen at that node. */
final class NodeAndState {

  public static @Nonnull NodeAndState of(Node node, State state) {
    return new NodeAndState(node, state);
  }

  public @Nonnull Node getNode() {
    return _node;
  }

  public @Nonnull State getState() {
    return _state;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof NodeAndState)) {
      return false;
    }
    NodeAndState that = (NodeAndState) o;
    return _node.equals(that._node) && _state.equals(that._state);
  }

  @Override
  public int hashCode() {
    return _node.hashCode() * 31 + _state.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_node", _node).add("_state", _state).toString();
  }

  private NodeAndState(Node node, State state) {
    _node = node;
    _state = state;
  }

  private final @Nonnull Node _node;
  private final @Nonnull State _state;
}
