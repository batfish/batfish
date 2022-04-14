package org.batfish.common.topology.bridge_domain;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.PointToPointComputer;
import org.batfish.common.topology.PointToPointInterfaces;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.vxlan.VxlanTopology;

/**
 * An {@link L3Adjacencies} implementation that directly models in configuration the behavior of and
 * relationships between L1/2/3 interfaces, bridge domains, and VNIs.
 *
 * <p>Key design features:
 *
 * <ul>
 *   <li>Edges and the relationships between interfaces, VNIs, and bridge domains are populated
 *       during conversion. See {@link org.batfish.datamodel.topology.InterfaceTopology} and {@link
 *       org.batfish.datamodel.vxlan.Layer2Vni}.
 *   <li>Nodes store edges (and not vice-versa), and thus comprise the graph. Nodes are computed at
 *       topology computation time from configuration, layer-1 topology, and VxLAN topology.
 *   <li>Edges contain reified functions modeling filters and transformations of frames and
 *       classification state thereof. The reified nature enables both concrete and symbolic
 *       reachability analysis of L2 traffic between nodes.
 *   <li>Edge transformation types are restricted based on the source and destination node types.
 *       For instance, a VLAN ID cannot be set while traversing from an L1 interface to an L1 HUB.
 * </ul>
 *
 * @see L3AdjacencyComputer
 */
public final class BridgeDomainL3Adjacencies implements L3Adjacencies {
  public static @Nonnull BridgeDomainL3Adjacencies create(
      Layer1Topologies l1, VxlanTopology vxlan, Map<String, Configuration> configs) {
    PointToPointInterfaces p2p = PointToPointComputer.compute(l1.getLogicalL1(), configs);
    L3AdjacencyComputer adj = new L3AdjacencyComputer(configs, l1, vxlan);
    return new BridgeDomainL3Adjacencies(adj.findAllBroadcastDomains(), p2p);
  }

  @Override
  public boolean inSameBroadcastDomain(NodeInterfacePair i1, NodeInterfacePair i2) {
    checkArgument(_domains.containsKey(i1), "Missing domain for %s: is it an L3 interface?", i1);
    checkArgument(_domains.containsKey(i2), "Missing domain for %s: is it an L3 interface?", i2);
    return _domains.get(i1).equals(_domains.get(i2));
  }

  @Override
  public @Nonnull Optional<NodeInterfacePair> pairedPointToPointL3Interface(
      NodeInterfacePair iface) {
    checkArgument(
        _domains.containsKey(iface), "Missing domain for %s: is it an L3 interface?", iface);
    NodeInterfacePair ret = null;
    for (NodeInterfacePair otherIf : _pointToPointInterfaces.pointToPointInterfaces(iface)) {
      if (!_domains.containsKey(otherIf) || !inSameBroadcastDomain(iface, otherIf)) {
        // otherIf is not L3 or not in same domain.
        continue;
      } else if (ret != null) {
        // Two p2p neighbors, not unique. Should not happen.
        return Optional.empty();
      }
      ret = otherIf;
    }
    return Optional.ofNullable(ret);
  }

  private BridgeDomainL3Adjacencies(
      Map<NodeInterfacePair, NodeInterfacePair> domains,
      PointToPointInterfaces pointToPointInterfaces) {
    _domains = ImmutableMap.copyOf(domains);
    _pointToPointInterfaces = pointToPointInterfaces;
  }

  private final @Nonnull Map<NodeInterfacePair, NodeInterfacePair> _domains;
  private final @Nonnull PointToPointInterfaces _pointToPointInterfaces;
}
