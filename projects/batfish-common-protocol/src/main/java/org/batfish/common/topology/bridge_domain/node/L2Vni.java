package org.batfish.common.topology.bridge_domain.node;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.edge.L2VniToBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L2VniToL2VniHub;
import org.batfish.common.topology.bridge_domain.edge.L2VniToNonVlanAwareBridgeDomain;
import org.batfish.common.topology.bridge_domain.edge.L2VniToVlanAwareBridgeDomain;
import org.batfish.datamodel.vxlan.VniLayer;
import org.batfish.datamodel.vxlan.VxlanNode;

/**
 * A node-specific VNI associated with a bridge domain:
 *
 * <p>If the associated bridge domain is vlan-aware, then the VNI should be associated with a VLAN.
 */
public final class L2Vni implements Node {

  public static @Nonnull L2Vni of(VxlanNode vxlanNode) {
    checkArgument(vxlanNode.getVniLayer() == VniLayer.LAYER_2, "Expected a layer-2 VNI");
    return new L2Vni(vxlanNode);
  }

  @Override
  public @Nonnull Map<Node, Edge> getOutEdges() {
    ImmutableMap.Builder<Node, Edge> builder =
        ImmutableMap.<Node, Edge>builder().put(_bridgeDomain, _toBridgeDomain);
    if (_l2VniHub != null) {
      builder.put(_l2VniHub, L2VniToL2VniHub.instance());
    }
    return builder.build();
  }

  public @Nonnull VxlanNode getNode() {
    return _node;
  }

  public void connectToVlanAwareBridgeDomain(
      VlanAwareBridgeDomain bridgeDomain, L2VniToVlanAwareBridgeDomain edge) {
    checkState(_bridgeDomain == null, "Already attached to bridge domain: %s", _bridgeDomain);
    _bridgeDomain = bridgeDomain;
    _toBridgeDomain = edge;
  }

  public void connectToNonVlanAwareBridgeDomain(
      NonVlanAwareBridgeDomain bridgeDomain, L2VniToNonVlanAwareBridgeDomain edge) {
    checkState(_bridgeDomain == null, "Already attached to bridge domain: %s", _bridgeDomain);
    _bridgeDomain = bridgeDomain;
    _toBridgeDomain = edge;
  }

  public void connectToL2VniHub(L2VniHub l2VniHub) {
    checkState(_l2VniHub == null, "Already attached to L2VniHub: %s", _l2VniHub);
    _l2VniHub = l2VniHub;
  }

  public void clearHub() {
    _l2VniHub = null;
  }

  // internal implementation details

  @VisibleForTesting
  public BridgeDomain getBridgeDomainForTest() {
    return _bridgeDomain;
  }

  @VisibleForTesting
  public L2VniHub getL2VniHubForTest() {
    return _l2VniHub;
  }

  @VisibleForTesting
  public L2VniToBridgeDomain getToBridgeDomainForTest() {
    return _toBridgeDomain;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof L2Vni)) {
      return false;
    }
    L2Vni that = (L2Vni) o;
    return _node.equals(that._node);
  }

  @Override
  public int hashCode() {
    return L2Vni.class.hashCode() * 31 + _node.hashCode();
  }

  private L2Vni(VxlanNode node) {
    _node = node;
  }

  private final @Nonnull VxlanNode _node;
  private transient L2VniHub _l2VniHub;
  private BridgeDomain _bridgeDomain;
  private L2VniToBridgeDomain _toBridgeDomain;
}
