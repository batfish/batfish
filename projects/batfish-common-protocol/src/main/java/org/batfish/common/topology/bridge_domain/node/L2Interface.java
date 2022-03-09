package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.edge.L2ToBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L2ToPhysical;
import org.batfish.datamodel.collections.NodeInterfacePair;

/**
 * A layer-2 aspect or a subinterface of a physical or aggregated interface:
 *
 * <ul>
 *   <li>A traditional access-mode switchport
 *   <li>A traditional trunk-mode switchport
 *   <li>An IOS-XR-style l2transport interface
 * </ul>
 */
public final class L2Interface implements Node {
  public L2Interface(NodeInterfacePair iface) {
    _interface = iface;
  }

  public @Nonnull NodeInterfacePair getInterface() {
    return _interface;
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    assert _bridgeDomain != null;
    assert _physicalInterface != null;
    return ImmutableMap.<Node, Edge>builderWithExpectedSize(2)
        .put(_bridgeDomain, _toBridgeDomain)
        .put(_physicalInterface, _toPhysical)
        .build();
  }

  public void connectToBridgeDomain(BridgeDomain bridgeDomain, L2ToBridgeDomain edge) {
    checkState(
        _bridgeDomain == null, "Already connected to bridge domain: %s", bridgeDomain.getId());
    _bridgeDomain = bridgeDomain;
    _toBridgeDomain = edge;
  }

  public void connectToPhysicalInterface(PhysicalInterface physicalInterface, L2ToPhysical edge) {
    checkState(
        _physicalInterface == null,
        "Already connected to physical interface: %s",
        physicalInterface.getInterface());
    _physicalInterface = physicalInterface;
    _toPhysical = edge;
  }

  // Internal implementation details

  @VisibleForTesting
  public BridgeDomain getBridgeDomainForTest() {
    return _bridgeDomain;
  }

  @VisibleForTesting
  public L2ToBridgeDomain getToBridgeDomainForTest() {
    return _toBridgeDomain;
  }

  @VisibleForTesting
  public PhysicalInterface getPhysicalInterfaceForTest() {
    return _physicalInterface;
  }

  @VisibleForTesting
  public L2ToPhysical getToPhysicalForTest() {
    return _toPhysical;
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
  private L2ToBridgeDomain _toBridgeDomain;
  private BridgeDomain _bridgeDomain;
  private PhysicalInterface _physicalInterface;
  private L2ToPhysical _toPhysical;
}
