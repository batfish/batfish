package org.batfish.dataplane.ibdp;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.Layer2Topology;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.isis.IsisTopology;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.vxlan.VxlanTopology;

/** Container for various topologies used during data plane computation. */
@ParametersAreNonnullByDefault
public final class TopologyContext {

  public static final class Builder {

    private @Nonnull BgpTopology _bgpTopology;
    private @Nonnull EigrpTopology _eigrpTopology;
    private @Nonnull IsisTopology _isisTopology;
    private @Nonnull Layer2Topology _layer2Topology;
    private @Nonnull Topology _layer3Topology;
    private @Nonnull OspfTopology _ospfTopology;
    private @Nonnull VxlanTopology _vxlanTopology;

    public @Nonnull TopologyContext build() {
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
      _bgpTopology = BgpTopology.EMPTY;
      _layer3Topology = new Topology(ImmutableSortedSet.of());
      _eigrpTopology = EigrpTopology.EMPTY;
      _isisTopology = IsisTopology.EMPTY;
      _layer2Topology = Layer2Topology.EMPTY;
      _ospfTopology = OspfTopology.empty();
      _vxlanTopology = VxlanTopology.EMPTY;
    }

    public @Nonnull BgpTopology getBgpTopology() {
      return _bgpTopology;
    }

    public @Nonnull EigrpTopology getEigrpTopology() {
      return _eigrpTopology;
    }

    public @Nonnull IsisTopology getIsisTopology() {
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

    public @Nonnull Builder setBgpTopology(BgpTopology bgpTopology) {
      _bgpTopology = bgpTopology;
      return this;
    }

    public @Nonnull Builder setEigrpTopology(EigrpTopology eigrpTopology) {
      _eigrpTopology = eigrpTopology;
      return this;
    }

    public @Nonnull Builder setIsisTopology(IsisTopology isisTopology) {
      _isisTopology = isisTopology;
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

  public static @Nonnull Builder builder() {
    return new Builder();
  }

  private final @Nonnull BgpTopology _bgpTopology;
  private final @Nonnull EigrpTopology _eigrpTopology;
  private final @Nonnull IsisTopology _isisTopology;
  private final @Nonnull Layer2Topology _layer2Topology;
  private final @Nonnull Topology _layer3Topology;
  private final @Nonnull OspfTopology _ospfTopology;
  private final @Nonnull VxlanTopology _vxlanTopology;

  private TopologyContext(
      BgpTopology bgpTopology,
      EigrpTopology eigrpTopology,
      IsisTopology isisTopology,
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

  public @Nonnull BgpTopology getBgpTopology() {
    return _bgpTopology;
  }

  public @Nonnull EigrpTopology getEigrpTopology() {
    return _eigrpTopology;
  }

  public @Nonnull IsisTopology getIsisTopology() {
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

  public @Nonnull Builder toBuilder() {
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
