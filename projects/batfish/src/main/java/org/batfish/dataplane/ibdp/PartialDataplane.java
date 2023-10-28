package org.batfish.dataplane.ibdp;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.dataplane.ibdp.DataplaneUtil.computeConfigurations;
import static org.batfish.dataplane.ibdp.DataplaneUtil.computeFibs;
import static org.batfish.dataplane.ibdp.DataplaneUtil.computeForwardingAnalysis;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.common.topology.IpOwners;
import org.batfish.common.topology.L3Adjacencies;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;

/**
 * Partial dataplane to be used during dataplane computation. Should not be used outside of the
 * engine. Does not implement full dataplane, only FIBs and Forwarding analysis, to enable
 * traceroutes within dataplane computation engine.
 */
@ParametersAreNonnullByDefault
public final class PartialDataplane implements DataPlane {
  private static final Logger LOGGER = LogManager.getLogger(PartialDataplane.class);

  @Override
  public Map<String, Map<String, Fib>> getFibs() {
    return _fibs;
  }

  @Override
  public ForwardingAnalysis getForwardingAnalysis() {
    return _forwardingAnalysis;
  }

  @Override
  public @Nonnull Table<String, String, Set<Layer2Vni>> getLayer2Vnis() {
    return _layer2VniSettings;
  }

  @Override
  public @Nonnull Table<String, String, Set<Layer3Vni>> getLayer3Vnis() {
    return _layer3VniSettings;
  }

  @Override
  public @Nonnull Table<String, String, Set<Bgpv4Route>> getBgpRoutes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Table<String, String, Set<Bgpv4Route>> getBgpBackupRoutes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnRoutes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnBackupRoutes() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      getPrefixTracingInfoSummary() {
    throw new UnsupportedOperationException();
  }

  @Override
  public @Nonnull Table<String, String, FinalMainRib> getRibs() {
    throw new UnsupportedOperationException();
  }

  //////////
  // Builder
  //////////

  public static class Builder {

    private @Nullable IpOwners _ipOwners;
    private @Nullable Map<String, Node> _nodes;
    private @Nullable Topology _layer3Topology;
    private @Nullable L3Adjacencies _l3Adjacencies;

    public @Nonnull Builder setIpOwners(@Nonnull IpOwners ipOwners) {
      _ipOwners = ipOwners;
      return this;
    }

    public Builder setNodes(@Nonnull Map<String, Node> nodes) {
      _nodes = ImmutableMap.copyOf(nodes);
      return this;
    }

    public Builder setLayer3Topology(@Nonnull Topology layer3Topology) {
      _layer3Topology = layer3Topology;
      return this;
    }

    public Builder setL3Adjacencies(@Nonnull L3Adjacencies l3Adjacencies) {
      _l3Adjacencies = l3Adjacencies;
      return this;
    }

    public PartialDataplane build() {
      return new PartialDataplane(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /////////////////////////
  // Private implementation
  /////////////////////////

  private final @Nonnull Map<String, Map<String, Fib>> _fibs;
  private final @Nonnull ForwardingAnalysis _forwardingAnalysis;
  private final @Nonnull Table<String, String, Set<Layer2Vni>> _layer2VniSettings;
  private final @Nonnull Table<String, String, Set<Layer3Vni>> _layer3VniSettings;
  private final @Nonnull Topology _layer3Topology;
  private final @Nonnull L3Adjacencies _l3Adjacencies;

  private PartialDataplane(Builder builder) {
    checkArgument(builder._nodes != null, "Dataplane must have nodes to be constructed");
    checkArgument(builder._layer3Topology != null, "Dataplane must have an L3 topology set");
    checkArgument(builder._l3Adjacencies != null, "Dataplane must have an L3 adjacencies set");

    Map<String, Node> nodes = builder._nodes;
    LOGGER.info("Building dataplane");
    Map<String, Configuration> configs = computeConfigurations(nodes);
    _fibs = computeFibs(nodes);
    LOGGER.info("Building forwarding analysis");
    _forwardingAnalysis =
        computeForwardingAnalysis(_fibs, configs, builder._layer3Topology, builder._ipOwners);
    LOGGER.info("Computing VNI settings");
    _layer2VniSettings = DataplaneUtil.computeLayer2VniSettings(nodes);
    _layer3VniSettings = DataplaneUtil.computeLayer3VniSettings(nodes);
    _l3Adjacencies = builder._l3Adjacencies;
    _layer3Topology = builder._layer3Topology;
  }
}
