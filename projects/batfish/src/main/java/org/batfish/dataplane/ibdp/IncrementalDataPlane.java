package org.batfish.dataplane.ibdp;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.CollectionUtil.toImmutableSortedMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.vxlan.Layer2Vni;

/** Dataplane computation result of incremental dataplane engine */
@ParametersAreNonnullByDefault
public final class IncrementalDataPlane implements Serializable, DataPlane {

  @Override
  public Map<String, Map<String, Fib>> getFibs() {
    return _fibs;
  }

  @Override
  public ForwardingAnalysis getForwardingAnalysis() {
    return _forwardingAnalysis;
  }

  @Nonnull
  @Override
  public Table<String, String, Set<Layer2Vni>> getLayer2Vnis() {
    return _vniSettings;
  }

  @Nonnull
  @Override
  public Table<String, String, Set<Bgpv4Route>> getBgpRoutes() {
    return _bgpRoutes;
  }

  @Override
  @Nonnull
  public Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnRoutes() {
    return _evpnRoutes;
  }

  @Override
  public SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      getPrefixTracingInfoSummary() {
    return _prefixTracerSummary;
  }

  @Override
  public SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>> getRibs() {
    return _ribs;
  }

  //////////
  // Builder
  //////////

  public static class Builder {

    @Nullable private Map<String, Node> _nodes;
    @Nullable private Topology _layer3Topology;

    public Builder setNodes(@Nonnull Map<String, Node> nodes) {
      _nodes = ImmutableMap.copyOf(nodes);
      return this;
    }

    public Builder setLayer3Topology(@Nonnull Topology layer3Topology) {
      _layer3Topology = layer3Topology;
      return this;
    }

    public IncrementalDataPlane build() {
      return new IncrementalDataPlane(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  /////////////////////////
  // Private implementation
  /////////////////////////

  @Nonnull private final Table<String, String, Set<Bgpv4Route>> _bgpRoutes;
  @Nonnull private final Map<String, Map<String, Fib>> _fibs;
  @Nonnull private final ForwardingAnalysis _forwardingAnalysis;
  @Nonnull private final Table<String, String, Set<EvpnRoute<?, ?>>> _evpnRoutes;
  @Nonnull private final Table<String, String, Set<Layer2Vni>> _vniSettings;

  @Nonnull
  private final SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>>
      _ribs;

  @Nonnull
  private final SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      _prefixTracerSummary;

  private IncrementalDataPlane(Builder builder) {
    checkArgument(builder._nodes != null, "Dataplane must have nodes to be constructed");
    checkArgument(builder._layer3Topology != null, "Dataplane must have an L3 topology set");

    Map<String, Node> nodes = builder._nodes;
    Map<String, Configuration> configs = DataplaneUtil.computeConfigurations(nodes);
    // Order of initialization matters:
    _bgpRoutes = DataplaneUtil.computeBgpRoutes(nodes);
    _evpnRoutes = DataplaneUtil.computeEvpnRoutes(nodes);
    _ribs = DataplaneUtil.computeRibs(nodes);
    _fibs = DataplaneUtil.computeFibs(nodes);
    _forwardingAnalysis =
        DataplaneUtil.computeForwardingAnalysis(_fibs, configs, builder._layer3Topology);
    _prefixTracerSummary = computePrefixTracingInfo(nodes);
    _vniSettings = DataplaneUtil.computeVniSettings(nodes);
  }

  private static SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      computePrefixTracingInfo(Map<String, Node> nodes) {
    /*
     * Iterate over nodes, then virtual routers, and extract prefix tracer from each.
     * Sort hostnames and VRF names
     */
    return toImmutableSortedMap(
        nodes,
        Entry::getKey,
        nodeEntry ->
            toImmutableSortedMap(
                nodeEntry.getValue().getVirtualRouters(),
                VirtualRouter::getName,
                vr -> vr.getPrefixTracer().summarize()));
  }
}
