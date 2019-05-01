package org.batfish.dataplane.ibdp;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.ImmutableNetwork;
import com.google.common.graph.ImmutableValueGraph;
import com.google.common.graph.Network;
import com.google.common.graph.ValueGraph;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.datamodel.BgpPeerConfigId;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.bgp.BgpTopologyUtils;
import org.batfish.datamodel.eigrp.EigrpEdge;
import org.batfish.datamodel.eigrp.EigrpInterface;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.isis.IsisEdge;
import org.batfish.datamodel.isis.IsisNode;
import org.batfish.datamodel.isis.IsisTopology;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.vxlan.VxlanTopology;

/** Container for various topologies used during data plane computation. */
@ParametersAreNonnullByDefault
final class TopologyContext {

  static final class Builder {

    private @Nonnull ImmutableValueGraph<BgpPeerConfigId, BgpSessionProperties> _bgpTopology;
    private @Nonnull ImmutableNetwork<EigrpInterface, EigrpEdge> _eigrpTopology;
    private @Nonnull ImmutableNetwork<IsisNode, IsisEdge> _isisTopology;
    private @Nonnull Layer2Topology _layer2Topology;
    private @Nonnull Topology _layer3Topology;
    private @Nonnull OspfTopology _ospfTopology;
    private @Nonnull VxlanTopology _vxlanTopology;

    @Nonnull
    TopologyContext build() {
      return new TopologyContext(
          _bgpTopology,
          _eigrpTopology,
          _isisTopology,
          _layer2Topology,
          _layer3Topology,
          _ospfTopology,
          _vxlanTopology);
    }

    private Builder() {
      _bgpTopology =
          ImmutableValueGraph.copyOf(
              BgpTopologyUtils.initBgpTopology(ImmutableMap.of(), ImmutableMap.of(), false));
      _layer3Topology = new Topology(ImmutableSortedSet.of());
      _eigrpTopology =
          ImmutableNetwork.copyOf(
              EigrpTopology.initEigrpTopology(ImmutableMap.of(), _layer3Topology));
      _isisTopology =
          ImmutableNetwork.copyOf(
              IsisTopology.initIsisTopology(ImmutableMap.of(), _layer3Topology));
      _layer2Topology = Layer2Topology.EMPTY;
      _ospfTopology = OspfTopology.empty();
      _vxlanTopology = VxlanTopology.EMPTY;
    }

    public @Nonnull ImmutableValueGraph<BgpPeerConfigId, BgpSessionProperties> getBgpTopology() {
      return _bgpTopology;
    }

    public @Nonnull ImmutableNetwork<EigrpInterface, EigrpEdge> getEigrpTopology() {
      return _eigrpTopology;
    }

    public @Nonnull ImmutableNetwork<IsisNode, IsisEdge> getIsisTopology() {
      return _isisTopology;
    }

    public @Nonnull Layer2Topology getLayer2Topology() {
      return _layer2Topology;
    }

    public @Nonnull Topology getLayer3Topology() {
      return _layer3Topology;
    }

    public @Nonnull OspfTopology getOspfTopology() {
      return _ospfTopology;
    }

    public @Nonnull VxlanTopology getVxlanTopology() {
      return _vxlanTopology;
    }

    public @Nonnull Builder setBgpTopology(
        ValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology) {
      _bgpTopology = ImmutableValueGraph.copyOf(bgpTopology);
      return this;
    }

    public @Nonnull Builder setEigrpTopology(Network<EigrpInterface, EigrpEdge> eigrpTopology) {
      _eigrpTopology = ImmutableNetwork.copyOf(eigrpTopology);
      return this;
    }

    public @Nonnull Builder setIsisTopology(Network<IsisNode, IsisEdge> isisTopology) {
      _isisTopology = ImmutableNetwork.copyOf(isisTopology);
      return this;
    }

    public @Nonnull Builder setLayer2Topology(Layer2Topology layer2Topology) {
      _layer2Topology = layer2Topology;
      return this;
    }

    public @Nonnull Builder setLayer3Topology(Topology layer3Topology) {
      _layer3Topology = layer3Topology;
      return this;
    }

    public @Nonnull Builder setOspfTopology(OspfTopology ospfTopology) {
      _ospfTopology = ospfTopology;
      return this;
    }

    public @Nonnull Builder setVxlanTopology(VxlanTopology vxlanTopology) {
      _vxlanTopology = vxlanTopology;
      return this;
    }
  }

  static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nonnull ImmutableValueGraph<BgpPeerConfigId, BgpSessionProperties> _bgpTopology;
  private final @Nonnull ImmutableNetwork<EigrpInterface, EigrpEdge> _eigrpTopology;
  private final @Nonnull ImmutableNetwork<IsisNode, IsisEdge> _isisTopology;
  private final @Nonnull Layer2Topology _layer2Topology;
  private final @Nonnull Topology _layer3Topology;
  private final @Nonnull OspfTopology _ospfTopology;
  private final @Nonnull VxlanTopology _vxlanTopology;

  private TopologyContext(
      ImmutableValueGraph<BgpPeerConfigId, BgpSessionProperties> bgpTopology,
      ImmutableNetwork<EigrpInterface, EigrpEdge> eigrpTopology,
      ImmutableNetwork<IsisNode, IsisEdge> isisTopology,
      Layer2Topology layer2Topology,
      Topology layer3Topology,
      OspfTopology ospfTopology,
      VxlanTopology vxlanTopology) {
    _bgpTopology = bgpTopology;
    _eigrpTopology = eigrpTopology;
    _isisTopology = isisTopology;
    _layer2Topology = layer2Topology;
    _layer3Topology = layer3Topology;
    _ospfTopology = ospfTopology;
    _vxlanTopology = vxlanTopology;
  }

  public @Nonnull ImmutableValueGraph<BgpPeerConfigId, BgpSessionProperties> getBgpTopology() {
    return _bgpTopology;
  }

  public @Nonnull ImmutableNetwork<EigrpInterface, EigrpEdge> getEigrpTopology() {
    return _eigrpTopology;
  }

  public @Nonnull ImmutableNetwork<IsisNode, IsisEdge> getIsisTopology() {
    return _isisTopology;
  }

  public @Nonnull Layer2Topology getLayer2Topology() {
    return _layer2Topology;
  }

  public @Nonnull Topology getLayer3Topology() {
    return _layer3Topology;
  }

  public @Nonnull OspfTopology getOspfTopology() {
    return _ospfTopology;
  }

  public @Nonnull VxlanTopology getVxlanTopology() {
    return _vxlanTopology;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TopologyContext)) {
      return false;
    }
    TopologyContext rhs = (TopologyContext) obj;
    return _bgpTopology.equals(rhs._bgpTopology)
        && _eigrpTopology.equals(rhs._eigrpTopology)
        && _isisTopology.equals(rhs._isisTopology)
        && _layer2Topology.equals(rhs._layer2Topology)
        && _layer3Topology.equals(rhs._layer3Topology)
        && _ospfTopology.equals(rhs._ospfTopology)
        && _vxlanTopology.equals(rhs._vxlanTopology);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _bgpTopology,
        _eigrpTopology,
        _isisTopology,
        _layer2Topology,
        _layer3Topology,
        _ospfTopology,
        _vxlanTopology);
  }

  @Nonnull
  Builder toBuilder() {
    return builder()
        .setBgpTopology(_bgpTopology)
        .setEigrpTopology(_eigrpTopology)
        .setIsisTopology(_isisTopology)
        .setLayer2Topology(_layer2Topology)
        .setLayer3Topology(_layer3Topology)
        .setOspfTopology(_ospfTopology)
        .setVxlanTopology(_vxlanTopology);
  }
}
