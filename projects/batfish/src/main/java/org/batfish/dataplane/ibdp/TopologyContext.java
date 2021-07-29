package org.batfish.dataplane.ibdp;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.GlobalBroadcastNoPointToPoint;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.TopologyContainer;
import org.batfish.common.topology.TunnelTopology;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.bgp.BgpTopology;
import org.batfish.datamodel.eigrp.EigrpTopology;
import org.batfish.datamodel.ipsec.IpsecTopology;
import org.batfish.datamodel.isis.IsisTopology;
import org.batfish.datamodel.ospf.OspfTopology;
import org.batfish.datamodel.vxlan.VxlanTopology;

/** Container for various topologies used during data plane computation. */
@ParametersAreNonnullByDefault
public final class TopologyContext implements TopologyContainer {

  public static final class Builder {

    private @Nonnull BgpTopology _bgpTopology;
    private @Nonnull EigrpTopology _eigrpTopology;
    private @Nonnull IpsecTopology _ipsecTopology;
    private @Nonnull IsisTopology _isisTopology;
    private @Nonnull Topology _layer3Topology;
    private @Nonnull Layer1Topologies _layer1Topologies;
    private @Nonnull L3Adjacencies _l3Adjacencies;
    private @Nonnull OspfTopology _ospfTopology;
    @Nonnull private TunnelTopology _tunnelTopology;
    private @Nonnull VxlanTopology _vxlanTopology;

    public @Nonnull TopologyContext build() {
      return new TopologyContext(
          _bgpTopology,
          _eigrpTopology,
          _ipsecTopology,
          _isisTopology,
          _layer3Topology,
          _layer1Topologies,
          _l3Adjacencies,
          _ospfTopology,
          _tunnelTopology,
          _vxlanTopology);
    }

    private Builder() {
      _bgpTopology = BgpTopology.EMPTY;
      _eigrpTopology = EigrpTopology.EMPTY;
      _ipsecTopology = IpsecTopology.EMPTY;
      _isisTopology = IsisTopology.EMPTY;
      _layer3Topology = Topology.EMPTY;
      _layer1Topologies = Layer1Topologies.empty();
      _l3Adjacencies = GlobalBroadcastNoPointToPoint.instance();
      _ospfTopology = OspfTopology.EMPTY;
      _tunnelTopology = TunnelTopology.EMPTY;
      _vxlanTopology = VxlanTopology.EMPTY;
    }

    public @Nonnull Builder setBgpTopology(BgpTopology bgpTopology) {
      _bgpTopology = bgpTopology;
      return this;
    }

    public @Nonnull Builder setEigrpTopology(EigrpTopology eigrpTopology) {
      _eigrpTopology = eigrpTopology;
      return this;
    }

    public @Nonnull Builder setIpsecTopology(@Nonnull IpsecTopology ipsecTopology) {
      _ipsecTopology = ipsecTopology;
      return this;
    }

    public @Nonnull Builder setIsisTopology(IsisTopology isisTopology) {
      _isisTopology = isisTopology;
      return this;
    }

    public @Nonnull Builder setLayer3Topology(Topology layer3Topology) {
      _layer3Topology = layer3Topology;
      return this;
    }

    public @Nonnull Builder setLayer1Topologies(Layer1Topologies layer1Topologies) {
      _layer1Topologies = layer1Topologies;
      return this;
    }

    public @Nonnull Builder setL3Adjacencies(L3Adjacencies l3Adjacencies) {
      _l3Adjacencies = l3Adjacencies;
      return this;
    }

    public @Nonnull Builder setOspfTopology(OspfTopology ospfTopology) {
      _ospfTopology = ospfTopology;
      return this;
    }

    public Builder setTunnelTopology(TunnelTopology tunnelTopology) {
      _tunnelTopology = tunnelTopology;
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
  private final @Nonnull IpsecTopology _ipsecTopology;
  private final @Nonnull IsisTopology _isisTopology;
  private final @Nonnull Topology _layer3Topology;
  private final @Nonnull Layer1Topologies _layer1Topologies;
  private final @Nonnull L3Adjacencies _l3Adjacencies;
  private final @Nonnull OspfTopology _ospfTopology;
  @Nonnull private final TunnelTopology _tunnelTopology;
  private final @Nonnull VxlanTopology _vxlanTopology;

  private TopologyContext(
      BgpTopology bgpTopology,
      EigrpTopology eigrpTopology,
      IpsecTopology ipsecTopology,
      IsisTopology isisTopology,
      Topology layer3Topology,
      Layer1Topologies layer1Topologies,
      L3Adjacencies l3Adjacencies,
      OspfTopology ospfTopology,
      TunnelTopology tunnelTopology,
      VxlanTopology vxlanTopology) {
    _bgpTopology = bgpTopology;
    _eigrpTopology = eigrpTopology;
    _ipsecTopology = ipsecTopology;
    _isisTopology = isisTopology;
    _layer3Topology = layer3Topology;
    _layer1Topologies = layer1Topologies;
    _l3Adjacencies = l3Adjacencies;
    _ospfTopology = ospfTopology;
    _vxlanTopology = vxlanTopology;
    _tunnelTopology = tunnelTopology;
  }

  @Override
  public @Nonnull BgpTopology getBgpTopology() {
    return _bgpTopology;
  }

  @Override
  public @Nonnull EigrpTopology getEigrpTopology() {
    return _eigrpTopology;
  }

  public @Nonnull IpsecTopology getIpsecTopology() {
    return _ipsecTopology;
  }

  @Override
  public @Nonnull IsisTopology getIsisTopology() {
    return _isisTopology;
  }

  @Override
  public @Nonnull Topology getLayer3Topology() {
    return _layer3Topology;
  }

  @Override
  public @Nonnull Layer1Topologies getLayer1Topologies() {
    return _layer1Topologies;
  }

  @Override
  public @Nonnull L3Adjacencies getL3Adjacencies() {
    return _l3Adjacencies;
  }

  @Override
  public @Nonnull OspfTopology getOspfTopology() {
    return _ospfTopology;
  }

  @Override
  public @Nonnull VxlanTopology getVxlanTopology() {
    return _vxlanTopology;
  }

  @Override
  @Nonnull
  public TunnelTopology getTunnelTopology() {
    return _tunnelTopology;
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
        && _ipsecTopology.equals(rhs._ipsecTopology)
        && _isisTopology.equals(rhs._isisTopology)
        && _layer3Topology.equals(rhs._layer3Topology)
        && _layer1Topologies.equals(rhs._layer1Topologies)
        && _l3Adjacencies.equals(rhs._l3Adjacencies)
        && _ospfTopology.equals(rhs._ospfTopology)
        && _tunnelTopology.equals(rhs._tunnelTopology)
        && _vxlanTopology.equals(rhs._vxlanTopology);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _bgpTopology,
        _eigrpTopology,
        _ipsecTopology,
        _isisTopology,
        _layer3Topology,
        _layer1Topologies,
        _l3Adjacencies,
        _ospfTopology,
        _tunnelTopology,
        _vxlanTopology);
  }

  public @Nonnull Builder toBuilder() {
    return builder()
        .setBgpTopology(_bgpTopology)
        .setEigrpTopology(_eigrpTopology)
        .setIpsecTopology(_ipsecTopology)
        .setIsisTopology(_isisTopology)
        .setLayer3Topology(_layer3Topology)
        .setLayer1Topologies(_layer1Topologies)
        .setL3Adjacencies(_l3Adjacencies)
        .setOspfTopology(_ospfTopology)
        .setTunnelTopology(_tunnelTopology)
        .setVxlanTopology(_vxlanTopology);
  }
}
