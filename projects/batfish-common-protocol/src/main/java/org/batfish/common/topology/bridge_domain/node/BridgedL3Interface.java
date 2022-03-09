package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.edge.BridgedL3ToBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * A {@link Node} representing a bridged layer-3 interface:
 *
 * <ul>
 *   <li>An IRB/Vlan interface, which belongs to a device's vlan-aware bridge
 *   <li>A BVI, Juniper bridged interface, or traditional linux Bridge interface, which belongs to a
 *       named non-vlan-aware bridge
 * </ul>
 *
 * See for comparison {@link NonBridgedL3Interface}.
 */
public final class BridgedL3Interface implements L3Interface {
  public BridgedL3Interface(NodeInterfacePair iface) {
    _interface = iface;
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    assert _toBridgeDomain != null;
    assert _bridgeDomain != null;
    return ImmutableMap.of(_bridgeDomain, _toBridgeDomain);
  }

  @Override
  public @Nonnull NodeInterfacePair getInterface() {
    return _interface;
  }

  /**
   * Set the bridge domain to which this interface belongs, as well as the correpsonding edge to it.
   *
   * <p>This function should be called exactly once.
   */
  public void connectToBridgeDomain(BridgeDomain bridgeDomain, BridgedL3ToBridgeDomain edge) {
    checkState(
        _bridgeDomain == null, "Already connected to bridge domain: %s", bridgeDomain.getId());
    _bridgeDomain = bridgeDomain;
    _toBridgeDomain = edge;
  }

  // Internal implementation details

  @VisibleForTesting
  public BridgeDomain getBridgeDomainForTest() {
    return _bridgeDomain;
  }

  @VisibleForTesting
  public BridgedL3ToBridgeDomain getToBridgeDomainForTest() {
    return _toBridgeDomain;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof BridgedL3Interface)) {
      return false;
    }
    BridgedL3Interface that = (BridgedL3Interface) o;
    return _interface.equals(that._interface);
  }

  @Override
  public int hashCode() {
    return BridgedL3Interface.class.hashCode() * 31 + _interface.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_interface", _interface).toString();
  }

  private final @Nonnull NodeInterfacePair _interface;
  private BridgeDomain _bridgeDomain;
  private BridgedL3ToBridgeDomain _toBridgeDomain;
}
