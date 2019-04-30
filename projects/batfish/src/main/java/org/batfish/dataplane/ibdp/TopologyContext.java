package org.batfish.dataplane.ibdp;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.ImmutableNetwork;
import com.google.common.graph.Network;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.eigrp.EigrpEdge;
import org.batfish.datamodel.eigrp.EigrpInterface;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.isis.IsisEdge;
import org.batfish.datamodel.isis.IsisNode;
import org.batfish.datamodel.isis.IsisTopology;
import org.batfish.datamodel.ospf.OspfTopology;

/** Container for various topologies used during data plane computation. */
@ParametersAreNonnullByDefault
class TopologyContext {

  static class Builder {

    private @Nonnull ImmutableNetwork<EigrpInterface, EigrpEdge> _eigrpTopology;
    private @Nonnull ImmutableNetwork<IsisNode, IsisEdge> _isisTopology;
    private @Nonnull Layer2Topology _layer2Topology;
    private @Nonnull Topology _layer3Topology;
    private @Nonnull OspfTopology _ospfTopology;

    @Nonnull
    TopologyContext build() {
      return new TopologyContext(
          _eigrpTopology, _isisTopology, _layer2Topology, _layer3Topology, _ospfTopology);
    }

    private Builder() {
      _layer3Topology = new Topology(ImmutableSortedSet.of());
      _eigrpTopology =
          ImmutableNetwork.copyOf(
              EigrpTopology.initEigrpTopology(ImmutableMap.of(), _layer3Topology));
      _isisTopology =
          ImmutableNetwork.copyOf(
              IsisTopology.initIsisTopology(ImmutableMap.of(), _layer3Topology));
      _layer2Topology = Layer2Topology.EMPTY;
      _ospfTopology = OspfTopology.empty();
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
  }

  static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nonnull ImmutableNetwork<EigrpInterface, EigrpEdge> _eigrpTopology;
  private final @Nonnull ImmutableNetwork<IsisNode, IsisEdge> _isisTopology;
  private final @Nonnull Layer2Topology _layer2Topology;
  private final @Nonnull Topology _layer3Topology;
  private final @Nonnull OspfTopology _ospfTopology;

  private TopologyContext(
      ImmutableNetwork<EigrpInterface, EigrpEdge> eigrpTopology,
      ImmutableNetwork<IsisNode, IsisEdge> isisTopology,
      Layer2Topology layer2Topology,
      Topology layer3Topology,
      OspfTopology ospfTopology) {
    _eigrpTopology = eigrpTopology;
    _isisTopology = isisTopology;
    _layer2Topology = layer2Topology;
    _layer3Topology = layer3Topology;
    _ospfTopology = ospfTopology;
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

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TopologyContext)) {
      return false;
    }
    TopologyContext rhs = (TopologyContext) obj;
    return _eigrpTopology.equals(rhs._eigrpTopology)
        && _isisTopology.equals(rhs._isisTopology)
        && _layer2Topology.equals(rhs._layer2Topology)
        && _layer3Topology.equals(rhs._layer3Topology)
        && _ospfTopology.equals(rhs._ospfTopology);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _eigrpTopology, _isisTopology, _layer2Topology, _layer3Topology, _ospfTopology);
  }

  @Nonnull
  Builder toBuilder() {
    return builder()
        .setEigrpTopology(_eigrpTopology)
        .setIsisTopology(_isisTopology)
        .setLayer2Topology(_layer2Topology)
        .setLayer3Topology(_layer3Topology)
        .setOspfTopology(_ospfTopology);
  }
}
