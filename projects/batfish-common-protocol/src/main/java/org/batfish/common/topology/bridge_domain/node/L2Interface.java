package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.edge.L2ToBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L2ToL1;
import org.batfish.common.topology.bridge_domain.edge.L2ToNonVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L2ToVlanAwareBridgeDomain;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * A layer-2 aspect or a subinterface of a logical l1 interface:
 *
 * <ul>
 *   <li>A traditional access-mode switchport
 *   <li>A traditional trunk-mode switchport
 *   <li>An IOS-XR-style l2transport interface
 * </ul>
 *
 * <p>A layer-2 interface may connect to an arbitrary number of bridge domains, though the input tag
 * spaces leading to each bridge domain must be distinct.
 */
public final class L2Interface implements Node {
  public L2Interface(NodeInterfacePair iface) {
    _interface = iface;
    _toBridgeDomain = new HashMap<>();
  }

  public @Nonnull NodeInterfacePair getInterface() {
    return _interface;
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    assert _l1Interface != null;
    return ImmutableMap.<Node, Edge>builder()
        .put(_l1Interface, _toL1)
        .putAll(_toBridgeDomain)
        .build();
  }

  public void connectToNonVlanAwareBridgeDomain(
      NonVlanAwareBridgeDomain bridgeDomain, L2ToNonVlanAwareBridgeDomain edge) {
    L2ToBridgeDomain existing = _toBridgeDomain.putIfAbsent(bridgeDomain, edge);
    checkState(existing == null, "Already connected to bridge domain: %s", bridgeDomain);
  }

  public void connectToVlanAwareBridgeDomain(
      VlanAwareBridgeDomain bridgeDomain, L2ToVlanAwareBridgeDomain edge) {
    L2ToBridgeDomain existing = _toBridgeDomain.putIfAbsent(bridgeDomain, edge);
    checkState(existing == null, "Already connected to bridge domain: %s", bridgeDomain);
  }

  public void connectToL1Interface(L1Interface l1Interface, L2ToL1 edge) {
    checkState(
        _l1Interface == null, "Already connected to l1 interface: %s", l1Interface.getInterface());
    _l1Interface = l1Interface;
    _toL1 = edge;
  }

  // Internal implementation details

  @VisibleForTesting
  public Map<BridgeDomain, L2ToBridgeDomain> getToBridgeDomainForTest() {
    return _toBridgeDomain;
  }

  @VisibleForTesting
  public L1Interface getL1InterfaceForTest() {
    return _l1Interface;
  }

  @VisibleForTesting
  public L2ToL1 getToL1ForTest() {
    return _toL1;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof L2Interface)) {
      return false;
    }
    L2Interface that = (L2Interface) o;
    return _interface.equals(that._interface);
  }

  @Override
  public int hashCode() {
    return L2Interface.class.hashCode() * 31 + _interface.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_interface", _interface).toString();
  }

  private final @Nonnull NodeInterfacePair _interface;
  private @Nonnull Map<BridgeDomain, L2ToBridgeDomain> _toBridgeDomain;
  private L1Interface _l1Interface;
  private L2ToL1 _toL1;
}
