package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.edge.L3ToBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L3ToNonVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L3ToVlanAwareBridgeDomain;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * A {@link Node} representing a bridged layer-3 interface:
 *
 * <ul>
 *   <li>An IRB/Vlan interface, which belongs to a device's vlan-aware bridge
 *   <li>A BVI, Juniper bridged interface, or traditional linux Bridge interface, which belongs to a
 *       named non-vlan-aware bridge
 * </ul>
 */
public final class L3BridgedInterface implements L3Interface {
  public L3BridgedInterface(NodeInterfacePair iface) {
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

  public void connectToVlanAwareBridgeDomain(
      VlanAwareBridgeDomain bridgeDomain, L3ToVlanAwareBridgeDomain edge) {
    checkState(_bridgeDomain == null, "Already connected to bridge domain: %s", bridgeDomain);
    _bridgeDomain = bridgeDomain;
    _toBridgeDomain = edge;
  }

  public void connectToNonVlanAwareBridgeDomain(
      NonVlanAwareBridgeDomain bridgeDomain, L3ToNonVlanAwareBridgeDomain edge) {
    checkState(_bridgeDomain == null, "Already connected to bridge domain: %s", bridgeDomain);
    _bridgeDomain = bridgeDomain;
    _toBridgeDomain = edge;
  }

  // Internal implementation details

  @VisibleForTesting
  public BridgeDomain getBridgeDomainForTest() {
    return _bridgeDomain;
  }

  @VisibleForTesting
  public L3ToBridgeDomain getToBridgeDomainForTest() {
    return _toBridgeDomain;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof L3BridgedInterface)) {
      return false;
    }
    L3BridgedInterface that = (L3BridgedInterface) o;
    return _interface.equals(that._interface);
  }

  @Override
  public int hashCode() {
    return L3BridgedInterface.class.hashCode() * 31 + _interface.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_interface", _interface).toString();
  }

  private final @Nonnull NodeInterfacePair _interface;
  private BridgeDomain _bridgeDomain;
  private L3ToBridgeDomain _toBridgeDomain;
}
