package org.batfish.dataplane.ibdp;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.common.topology.TopologyContainer;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.isis.IsisTopology;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.vxlan.VxlanTopology;

/** Container for various topologies used during data plane computation. */
@ParametersAreNonnullByDefault
public final class TopologyContext implements TopologyContainer {

  public static final class Builder {

    private @Nonnull BgpTopology _bgpTopology;
    private @Nonnull Set<Edge> _edgeBlacklist;
    private @Nonnull EigrpTopology _eigrpTopology;
    private @Nonnull Set<NodeInterfacePair> _interfaceBlacklist;
    private @Nonnull IsisTopology _isisTopology;
    private @Nonnull Optional<Layer1Topology> _layer1LogicalTopology;
    private @Nonnull Optional<Layer2Topology> _layer2Topology;
    private @Nonnull Topology _layer3Topology;
    private @Nonnull Set<String> _nodeBlacklist;
    private @Nonnull OspfTopology _ospfTopology;
    private @Nonnull Optional<Layer1Topology> _rawLayer1PhysicalTopology;
    private @Nonnull VxlanTopology _vxlanTopology;

    public @Nonnull TopologyContext build() {
      return new TopologyContext(
          _bgpTopology,
          _edgeBlacklist,
          _eigrpTopology,
          _interfaceBlacklist,
          _isisTopology,
          _layer1LogicalTopology,
          _layer2Topology,
          _layer3Topology,
          _nodeBlacklist,
          _ospfTopology,
          _rawLayer1PhysicalTopology,
          _vxlanTopology);
    }

    private Builder() {
      _bgpTopology = BgpTopology.EMPTY;
      _edgeBlacklist = ImmutableSet.of();
      _eigrpTopology = EigrpTopology.EMPTY;
      _interfaceBlacklist = ImmutableSet.of();
      _isisTopology = IsisTopology.EMPTY;
      _layer1LogicalTopology = Optional.empty();
      _layer2Topology = Optional.empty();
      _layer3Topology = Topology.EMPTY;
      _nodeBlacklist = ImmutableSet.of();
      _ospfTopology = OspfTopology.EMPTY;
      _rawLayer1PhysicalTopology = Optional.empty();
      _vxlanTopology = VxlanTopology.EMPTY;
    }

    public @Nonnull Builder setBgpTopology(BgpTopology bgpTopology) {
      _bgpTopology = bgpTopology;
      return this;
    }

    public @Nonnull Builder setEdgeBlacklist(Set<Edge> edgeBlacklist) {
      _edgeBlacklist = edgeBlacklist;
      return this;
    }

    public @Nonnull Builder setEigrpTopology(EigrpTopology eigrpTopology) {
      _eigrpTopology = eigrpTopology;
      return this;
    }

    public @Nonnull Builder setInterfaceBlacklist(Set<NodeInterfacePair> interfaceBlacklist) {
      _interfaceBlacklist = interfaceBlacklist;
      return this;
    }

    public @Nonnull Builder setIsisTopology(IsisTopology isisTopology) {
      _isisTopology = isisTopology;
      return this;
    }

    public @Nonnull Builder setLayer1LogicalTopology(
        Optional<Layer1Topology> layer1LogicalTopology) {
      _layer1LogicalTopology = layer1LogicalTopology;
      return this;
    }

    public @Nonnull Builder setLayer2Topology(Optional<Layer2Topology> layer2Topology) {
      _layer2Topology = layer2Topology;
      return this;
    }

    public @Nonnull Builder setLayer3Topology(Topology layer3Topology) {
      _layer3Topology = layer3Topology;
      return this;
    }

    public @Nonnull Builder setNodeBlacklist(Set<String> nodeBlacklist) {
      _nodeBlacklist = nodeBlacklist;
      return this;
    }

    public @Nonnull Builder setOspfTopology(OspfTopology ospfTopology) {
      _ospfTopology = ospfTopology;
      return this;
    }

    public @Nonnull Builder setRawLayer1PhysicalTopology(
        Optional<Layer1Topology> rawLayer1PhysicalTopology) {
      _rawLayer1PhysicalTopology = rawLayer1PhysicalTopology;
      return this;
    }

    public @Nonnull Builder setVxlanTopology(VxlanTopology vxlanTopology) {
      _vxlanTopology = vxlanTopology;
      return this;
    }
  }

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nonnull BgpTopology _bgpTopology;
  private final @Nonnull Set<Edge> _edgeBlacklist;
  private final @Nonnull EigrpTopology _eigrpTopology;
  private final @Nonnull Set<NodeInterfacePair> _interfaceBlacklist;
  private final @Nonnull IsisTopology _isisTopology;
  private final @Nonnull Optional<Layer1Topology> _layer1LogicalTopology;
  private final @Nonnull Optional<Layer2Topology> _layer2Topology;
  private final @Nonnull Topology _layer3Topology;
  private final @Nonnull Set<String> _nodeBlacklist;
  private final @Nonnull OspfTopology _ospfTopology;
  private final @Nonnull Optional<Layer1Topology> _rawLayer1PhysicalTopology;
  private final @Nonnull VxlanTopology _vxlanTopology;

  private TopologyContext(
      BgpTopology bgpTopology,
      Set<Edge> edgeBlacklist,
      EigrpTopology eigrpTopology,
      Set<NodeInterfacePair> interfaceBlacklist,
      IsisTopology isisTopology,
      Optional<Layer1Topology> layer1LogicalTopology,
      Optional<Layer2Topology> layer2Topology,
      Topology layer3Topology,
      Set<String> nodeBlacklist,
      OspfTopology ospfTopology,
      Optional<Layer1Topology> rawLayer1PhysicalTopology,
      VxlanTopology vxlanTopology) {
    _bgpTopology = bgpTopology;
    _edgeBlacklist = edgeBlacklist;
    _eigrpTopology = eigrpTopology;
    _interfaceBlacklist = interfaceBlacklist;
    _isisTopology = isisTopology;
    _layer1LogicalTopology = layer1LogicalTopology;
    _layer2Topology = layer2Topology;
    _layer3Topology = layer3Topology;
    _nodeBlacklist = nodeBlacklist;
    _ospfTopology = ospfTopology;
    _rawLayer1PhysicalTopology = rawLayer1PhysicalTopology;
    _vxlanTopology = vxlanTopology;
  }

  @Override
  public @Nonnull BgpTopology getBgpTopology() {
    return _bgpTopology;
  }

  public @Nonnull Set<Edge> getEdgeBlacklist() {
    return _edgeBlacklist;
  }

  @Override
  public @Nonnull EigrpTopology getEigrpTopology() {
    return _eigrpTopology;
  }

  public @Nonnull Set<NodeInterfacePair> getInterfaceBlacklist() {
    return _interfaceBlacklist;
  }

  @Override
  public @Nonnull IsisTopology getIsisTopology() {
    return _isisTopology;
  }

  public @Nonnull Optional<Layer1Topology> getLayer1LogicalTopology() {
    return _layer1LogicalTopology;
  }

  @Override
  public @Nonnull Optional<Layer2Topology> getLayer2Topology() {
    return _layer2Topology;
  }

  @Override
  public @Nonnull Topology getLayer3Topology() {
    return _layer3Topology;
  }

  public @Nonnull Set<String> getNodeBlacklist() {
    return _nodeBlacklist;
  }

  @Override
  public @Nonnull OspfTopology getOspfTopology() {
    return _ospfTopology;
  }

  public @Nonnull Optional<Layer1Topology> getRawLayer1PhysicalTopology() {
    return _rawLayer1PhysicalTopology;
  }

  @Override
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
        && _edgeBlacklist.equals(rhs._edgeBlacklist)
        && _eigrpTopology.equals(rhs._eigrpTopology)
        && _interfaceBlacklist.equals(rhs._interfaceBlacklist)
        && _isisTopology.equals(rhs._isisTopology)
        && _layer1LogicalTopology.equals(rhs._layer1LogicalTopology)
        && _layer2Topology.equals(rhs._layer2Topology)
        && _layer3Topology.equals(rhs._layer3Topology)
        && _nodeBlacklist.equals(rhs._nodeBlacklist)
        && _ospfTopology.equals(rhs._ospfTopology)
        && _rawLayer1PhysicalTopology.equals(rhs._rawLayer1PhysicalTopology)
        && _vxlanTopology.equals(rhs._vxlanTopology);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _bgpTopology,
        _edgeBlacklist,
        _eigrpTopology,
        _interfaceBlacklist,
        _isisTopology,
        _layer1LogicalTopology,
        _layer2Topology,
        _layer3Topology,
        _nodeBlacklist,
        _ospfTopology,
        _rawLayer1PhysicalTopology,
        _vxlanTopology);
  }

  public @Nonnull Builder toBuilder() {
    return builder()
        .setBgpTopology(_bgpTopology)
        .setEdgeBlacklist(_edgeBlacklist)
        .setEigrpTopology(_eigrpTopology)
        .setInterfaceBlacklist(_interfaceBlacklist)
        .setIsisTopology(_isisTopology)
        .setLayer1LogicalTopology(_layer1LogicalTopology)
        .setLayer2Topology(_layer2Topology)
        .setLayer3Topology(_layer3Topology)
        .setNodeBlacklist(_nodeBlacklist)
        .setOspfTopology(_ospfTopology)
        .setRawLayer1PhysicalTopology(_rawLayer1PhysicalTopology)
        .setVxlanTopology(_vxlanTopology);
  }
}
