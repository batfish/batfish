package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.edge.L1HubToL1;

/**
 * Sends all frames received out all interfaces. Used to reduce quadratic complexity to linear for
 * l1-l1 Ethernet links.
 *
 * <p>Only connects to {@link L1Interface}.
 */
public final class L1Hub implements Node {

  public L1Hub(String id) {
    _id = id;
    _toL1 = new HashMap<>();
  }

  public @Nonnull String getId() {
    return _id;
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    return ImmutableMap.copyOf(_toL1);
  }

  public void addAttachedInterface(L1Interface p) {
    _toL1.put(p, L1HubToL1.instance());
  }

  // Internal implementation details

  @VisibleForTesting
  public @Nonnull Map<L1Interface, L1HubToL1> getToL1ForTest() {
    return _toL1;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof L1Hub)) {
      return false;
    }
    L1Hub that = (L1Hub) o;
    return _id.equals(that._id);
  }

  @Override
  public int hashCode() {
    return L1Hub.class.hashCode() * 31 + _id.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_id", _id).toString();
  }

  private final @Nonnull String _id;
  private final @Nonnull Map<L1Interface, L1HubToL1> _toL1;
}
