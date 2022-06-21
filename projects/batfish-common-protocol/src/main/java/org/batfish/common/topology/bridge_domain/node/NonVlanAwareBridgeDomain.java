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
import org.batfish.common.topology.bridge_domain.edge.NonVlanAwareBridgeDomainToL2;
import org.batfish.common.topology.bridge_domain.edge.NonVlanAwareBridgeDomainToL2Vni;
import org.batfish.common.topology.bridge_domain.edge.NonVlanAwareBridgeDomainToL3;

/**
 * A non-vlan-aware bridge domain that propagates each frame to a subset of its bridged interfaces.
 *
 * <p>Frames reaching a non-vlan-aware bridge domain will propagate to a subset of its bridged
 * interfaces determined by an arbitrary filter on the state. A device may generally may have any
 * number of named non-vlan-aware bridges.
 *
 * <ul>
 *   <li>On IOS-XR, a frame will be propagated to all bridged interfaces except the routed-interface
 *       if any tag remains in the tag stack. If the tag stack is empty, then a frame is propagated
 *       to all bridged interfaces.
 * </ul>
 */
public final class NonVlanAwareBridgeDomain implements BridgeDomain {

  public NonVlanAwareBridgeDomain(Id id) {
    _id = id;
    _toL2 = new HashMap<>();
    _toL2Vni = new HashMap<>();
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    ImmutableMap.Builder<Node, Edge> builder =
        ImmutableMap.<Node, Edge>builderWithExpectedSize(
                (_toL3 != null ? 1 : 0) + _toL2.size() + _toL2Vni.size())
            .putAll(_toL2)
            .putAll(_toL2Vni);
    if (_toL3 != null) {
      assert _l3Interface != null;
      builder.put(_l3Interface, _toL3);
    }
    return builder.build();
  }

  public void connectToL2Interface(L2Interface l2Interface, NonVlanAwareBridgeDomainToL2 edge) {
    NonVlanAwareBridgeDomainToL2 existing = _toL2.putIfAbsent(l2Interface, edge);
    checkState(
        existing == null, "Already connected to L2 interface: %s", l2Interface.getInterface());
  }

  public void connectToL2Vni(L2Vni l2Vni, NonVlanAwareBridgeDomainToL2Vni edge) {
    NonVlanAwareBridgeDomainToL2Vni existing = _toL2Vni.putIfAbsent(l2Vni, edge);
    checkState(existing == null, "Already connected to L2Vni: %s", l2Vni.getNode());
  }

  public void connectToL3Interface(
      L3BridgedInterface bridgedL3Interface, NonVlanAwareBridgeDomainToL3 edge) {
    checkState(
        _l3Interface == null, "Already connected to a bridged L3 interface: %s", _l3Interface);
    _l3Interface = bridgedL3Interface;
    _toL3 = edge;
  }

  public @Nonnull Id getId() {
    return _id;
  }

  // Internal implementation details

  @VisibleForTesting
  public @Nonnull Map<L2Interface, NonVlanAwareBridgeDomainToL2> getToL2ForTest() {
    return _toL2;
  }

  @VisibleForTesting
  public @Nonnull Map<L2Vni, NonVlanAwareBridgeDomainToL2Vni> getToL2VniForTest() {
    return _toL2Vni;
  }

  @VisibleForTesting
  public @Nullable L3BridgedInterface getBridgedL3InterfaceForTest() {
    return _l3Interface;
  }

  @VisibleForTesting
  public @Nullable NonVlanAwareBridgeDomainToL3 getToBridgedL3ForTest() {
    return _toL3;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof NonVlanAwareBridgeDomain)) {
      return false;
    }
    NonVlanAwareBridgeDomain that = (NonVlanAwareBridgeDomain) o;
    return _id.equals(that._id);
  }

  @Override
  public int hashCode() {
    return NonVlanAwareBridgeDomain.class.hashCode() * 31 + _id.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_id", _id).toString();
  }

  private final @Nonnull Id _id;
  private @Nullable L3BridgedInterface _l3Interface;
  private @Nonnull Map<L2Interface, NonVlanAwareBridgeDomainToL2> _toL2;
  private @Nonnull Map<L2Vni, NonVlanAwareBridgeDomainToL2Vni> _toL2Vni;
  private @Nullable NonVlanAwareBridgeDomainToL3 _toL3;
}
