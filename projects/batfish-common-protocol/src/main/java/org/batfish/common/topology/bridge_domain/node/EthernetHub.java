package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.edge.EthernetHubToPhysical;

/**
 * Sends all frames received out all interfaces. Used to reduce quadratic complexity to linear for
 * physical-physical Ethernet links.
 *
 * <p>Only connects to {@link PhysicalInterface}.
 */
public final class EthernetHub implements Node {

  public EthernetHub(String id) {
    _id = id;
    _toPhysical = new HashMap<>();
  }

  public @Nonnull String getId() {
    return _id;
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    return ImmutableMap.copyOf(_toPhysical);
  }

  public void addAttachedInterface(PhysicalInterface p) {
    _toPhysical.put(p, EthernetHubToPhysical.instance());
  }

  // Internal implementation details

  @VisibleForTesting
  public @Nonnull Map<PhysicalInterface, EthernetHubToPhysical> getToPhysicalForTest() {
    return _toPhysical;
  }

  @Override
  public boolean equals(@Nullable Object o) {
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
    return EthernetHub.class.hashCode() * 31 + _id.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_id", _id).toString();
  }

  private final @Nonnull String _id;
  private final @Nonnull Map<PhysicalInterface, EthernetHubToPhysical> _toPhysical;
}
