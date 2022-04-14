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
import org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL2;
import org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL2Vni;
import org.batfish.common.topology.bridge_domain.edge.VlanAwareBridgeDomainToL3;

/**
 * A vlan-aware bridge domain that propagates each frame to a subset of its bridged interfaces.
 *
 * <p>Frames reaching a vlan-aware bridge domain will propagate to the subset of its bridged
 * interfaces that carry traffic for the VLAN ID set in the state. Most devices have exactly one
 * vlan-aware bridge, but on others an arbitrary number are supported.
 */
public final class VlanAwareBridgeDomain implements BridgeDomain {

  public static final String DEFAULT_VLAN_AWARE_BRIDGE_DOMAIN_NAME =
      "Batfish default vlan-aware bridge domain";

  public VlanAwareBridgeDomain(Id id) {
    _id = id;
    _toL3 = new HashMap<>();
    _toL2 = new HashMap<>();
    _toL2Vni = new HashMap<>();
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    return ImmutableMap.<Node, Edge>builderWithExpectedSize(
            _toL3.size() + _toL2.size() + _toL2Vni.size())
        .putAll(_toL3)
        .putAll(_toL2)
        .putAll(_toL2Vni)
        .build();
  }

  public void connectToL2Interface(L2Interface l2Interface, VlanAwareBridgeDomainToL2 edge) {
    VlanAwareBridgeDomainToL2 existing = _toL2.putIfAbsent(l2Interface, edge);
    checkState(
        existing == null, "Already connected to L2 interface: %s", l2Interface.getInterface());
  }

  public void connectToL3Interface(
      L3BridgedInterface bridgedL3Interface, VlanAwareBridgeDomainToL3 edge) {
    VlanAwareBridgeDomainToL3 existing = _toL3.putIfAbsent(bridgedL3Interface, edge);
    checkState(
        existing == null,
        "Already connected to bridged L3 interface: %s",
        bridgedL3Interface.getInterface());
  }

  public void connectToL2Vni(L2Vni l2Vni, VlanAwareBridgeDomainToL2Vni edge) {
    VlanAwareBridgeDomainToL2Vni existing = _toL2Vni.putIfAbsent(l2Vni, edge);
    checkState(existing == null, "Already connected to L2Vni: %s", l2Vni.getNode());
  }

  public @Nonnull Id getId() {
    return _id;
  }

  // Internal implementation details

  @VisibleForTesting
  public @Nonnull Map<L2Interface, VlanAwareBridgeDomainToL2> getToL2ForTest() {
    return _toL2;
  }

  @VisibleForTesting
  public @Nonnull Map<L2Vni, VlanAwareBridgeDomainToL2Vni> getToL2VniForTest() {
    return _toL2Vni;
  }

  @VisibleForTesting
  public @Nonnull Map<L3BridgedInterface, VlanAwareBridgeDomainToL3> getToBridgedL3ForTest() {
    return _toL3;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof VlanAwareBridgeDomain)) {
      return false;
    }
    VlanAwareBridgeDomain that = (VlanAwareBridgeDomain) o;
    return _id.equals(that._id);
  }

  @Override
  public int hashCode() {
    return VlanAwareBridgeDomain.class.hashCode() * 31 + _id.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_id", _id).toString();
  }

  private final @Nonnull Id _id;
  private @Nonnull Map<L2Interface, VlanAwareBridgeDomainToL2> _toL2;
  private @Nonnull Map<L2Vni, VlanAwareBridgeDomainToL2Vni> _toL2Vni;
  private @Nonnull Map<L3BridgedInterface, VlanAwareBridgeDomainToL3> _toL3;
}
