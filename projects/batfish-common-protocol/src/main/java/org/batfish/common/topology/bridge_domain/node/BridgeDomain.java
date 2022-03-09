package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToBridgedL3;
import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToL2;
import org.batfish.common.topology.bridge_domain.edge.BridgeDomainToL2Vni;
import org.batfish.common.topology.bridge_domain.edge.Edge;

/**
 * A vlan-aware or non-vlan-aware bridge domain that propagates each frame to a subset of its
 * bridged interfaces.
 *
 * <ul>
 *   <li>Frames reaching a vlan-aware bridge domain will propagate to the subset of its bridged
 *       interfaces that carry traffic for the VLAN ID set in the state. A device typically can have
 *       up to one vlan-aware bridge.
 *   <li>Frames reaching a non-vlan-aware bridge domain will propagate to a subset of its bridged
 *       interfaces determined by an arbitrary filter on the state. A device may generally may have
 *       any number of named non-vlan-aware bridges.
 *       <ul>
 *         <li>On IOS-XR, a frame will be propagated to all bridged interfaces except the
 *             routed-interface if any tag remains in the tag stack. If the tag stack is empty, then
 *             a frame is propagated to all bridged interfaces.
 *       </ul>
 * </ul>
 */
public final class BridgeDomain implements Node {

  public static final class BridgeId {
    public static @Nonnull BridgeId vlanAwareBridgeId(String hostname) {
      return new BridgeId(hostname, VLAN_AWARE_BRIDGE_NAME);
    }

    public static @Nonnull BridgeId nonVlanAwareBridgeId(String hostname, String bridgeName) {
      checkArgument(
          !bridgeName.equals(VLAN_AWARE_BRIDGE_NAME),
          "Use vlanAwareBridgeId to create the ID for a vlan-aware-bridge");
      return new BridgeId(hostname, bridgeName);
    }

    public @Nonnull String getHostname() {
      return _hostname;
    }

    public @Nonnull String getBridgeName() {
      return _bridgeName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (!(o instanceof BridgeId)) {
        return false;
      }
      BridgeId bridgeId = (BridgeId) o;
      return _hostname.equals(bridgeId._hostname) && _bridgeName.equals(bridgeId._bridgeName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_hostname, _bridgeName);
    }

    @Override
    public String toString() {
      return toStringHelper(this)
          .add("_hostname", _hostname)
          .add("_bridgeName", _bridgeName)
          .toString();
    }

    private BridgeId(String hostname, String bridgeName) {
      _hostname = hostname;
      _bridgeName = bridgeName;
    }

    private final @Nonnull String _hostname;
    private final @Nonnull String _bridgeName;
  }

  public static @Nonnull BridgeDomain newNonVlanAwareBridge(String hostname, String bridgeName) {
    return new BridgeDomain(BridgeId.nonVlanAwareBridgeId(hostname, bridgeName));
  }

  public static @Nonnull BridgeDomain newVlanAwareBridge(String hostname) {
    return new BridgeDomain(BridgeId.vlanAwareBridgeId(hostname));
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    return ImmutableMap.<Node, Edge>builderWithExpectedSize(_toBridgedL3.size() + _toL2.size())
        .putAll(_toBridgedL3)
        .putAll(_toL2)
        .putAll(_toL2Vni)
        .build();
  }

  public void connectToL2Interface(L2Interface l2Interface, BridgeDomainToL2 edge) {
    checkState(
        !_toL2.containsKey(l2Interface),
        "Already connected to L2 interface: %s",
        l2Interface.getInterface());
    _toL2.put(l2Interface, edge);
  }

  public void connectToBridgedL3Interface(
      BridgedL3Interface bridgedL3Interface, BridgeDomainToBridgedL3 edge) {
    checkState(
        !_toBridgedL3.containsKey(bridgedL3Interface),
        "Already connected to bridged L3 interface: %s",
        bridgedL3Interface.getInterface());
    _toBridgedL3.put(bridgedL3Interface, edge);
  }

  public void connectToL2Vni(L2Vni l2Vni, BridgeDomainToL2Vni edge) {
    BridgeDomainToL2Vni existing = _toL2Vni.putIfAbsent(l2Vni, edge);
    checkState(existing == null, "Already connected to L2Vni: %s", l2Vni.getNode());
  }

  public @Nonnull BridgeId getId() {
    return _id;
  }

  // Internal implementation details

  private BridgeDomain(BridgeId id) {
    _id = id;
    _toBridgedL3 = new HashMap<>();
    _toL2 = new HashMap<>();
    _toL2Vni = new HashMap<>();
  }

  @VisibleForTesting
  public @Nonnull Map<L2Interface, BridgeDomainToL2> getToL2ForTest() {
    return _toL2;
  }

  @VisibleForTesting
  public @Nonnull Map<L2Vni, BridgeDomainToL2Vni> getToL2VniForTest() {
    return _toL2Vni;
  }

  @VisibleForTesting
  public @Nonnull Map<BridgedL3Interface, BridgeDomainToBridgedL3> getToBridgedL3ForTest() {
    return _toBridgedL3;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof BridgeDomain)) {
      return false;
    }
    BridgeDomain that = (BridgeDomain) o;
    return _id.equals(that._id);
  }

  @Override
  public int hashCode() {
    return BridgeDomain.class.hashCode() * 31 + _id.hashCode();
  }

  @Override
  public String toString() {
    return toStringHelper(this).add("_id", _id).toString();
  }

  private static final String VLAN_AWARE_BRIDGE_NAME = "Batfish vlan-aware bridge";

  private final @Nonnull BridgeId _id;
  private final @Nonnull Map<BridgedL3Interface, BridgeDomainToBridgedL3> _toBridgedL3;
  private final @Nonnull Map<L2Interface, BridgeDomainToL2> _toL2;
  private final @Nonnull Map<L2Vni, BridgeDomainToL2Vni> _toL2Vni;
}
