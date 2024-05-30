package org.batfish.dataplane.ibdp;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.CollectionUtil.toImmutableSortedMap;
import static org.batfish.common.util.StreamUtil.toListInRandomOrder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.EvpnRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FinalMainRib;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;

/** Dataplane computation result of incremental dataplane engine */
@ParametersAreNonnullByDefault
public final class IncrementalDataPlane implements Serializable, DataPlane {
  private static final Logger LOGGER = LogManager.getLogger(IncrementalDataPlane.class);

  private static @Nonnull Table<String, String, FinalMainRib> computeRibs(List<VirtualRouter> vrs) {
    return vrs.parallelStream()
        .collect(
            ImmutableTable.toImmutableTable(
                vr -> vr.getConfiguration().getHostname(),
                VirtualRouter::getName,
                vr ->
                    FinalMainRib.of(
                        vr.getMainRib().getRoutes().stream()
                            .map(AnnotatedRoute::getAbstractRoute))));
  }

  @Override
  public @Nonnull Map<String, Map<String, Fib>> getFibs() {
    return _fibs;
  }

  @Override
  public @Nonnull ForwardingAnalysis getForwardingAnalysis() {
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
    return _bgpRoutes;
  }

  @Override
  public @Nonnull Table<String, String, Set<Bgpv4Route>> getBgpBackupRoutes() {
    return _bgpBackupRoutes;
  }

  @Override
  public @Nonnull Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnRoutes() {
    return _evpnRoutes;
  }

  @Override
  public @Nonnull Table<String, String, Set<EvpnRoute<?, ?>>> getEvpnBackupRoutes() {
    return _evpnBackupRoutes;
  }

  @Override
  public @Nonnull SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      getPrefixTracingInfoSummary() {
    return _prefixTracerSummary;
  }

  @Override
  public @Nonnull Table<String, String, FinalMainRib> getRibs() {
    return _ribs;
  }

  @VisibleForTesting
  public @Nonnull SortedMap<String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>>
      getRibsForTesting() {
    return _annotatedRibs;
  }

  //////////
  // Builder
  //////////

  public static class Builder {

    private @Nullable Map<String, Node> _nodes;
    private @Nullable PartialDataplane _partialDataplane;

    public Builder setNodes(@Nonnull Map<String, Node> nodes) {
      _nodes = ImmutableMap.copyOf(nodes);
      return this;
    }

    public Builder setPartialDataplane(PartialDataplane partialDataplane) {
      _partialDataplane = partialDataplane;
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

  private final @Nonnull Table<String, String, Set<Bgpv4Route>> _bgpRoutes;
  private final @Nonnull Table<String, String, Set<Bgpv4Route>> _bgpBackupRoutes;
  private final @Nonnull Map<String, Map<String, Fib>> _fibs;
  private final @Nonnull ForwardingAnalysis _forwardingAnalysis;
  private final @Nonnull Table<String, String, Set<EvpnRoute<?, ?>>> _evpnRoutes;
  private final @Nonnull Table<String, String, Set<EvpnRoute<?, ?>>> _evpnBackupRoutes;
  private final @Nonnull Table<String, String, Set<Layer2Vni>> _layer2VniSettings;
  private final @Nonnull Table<String, String, Set<Layer3Vni>> _layer3VniSettings;

  private final @Nonnull SortedMap<
          String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>>
      _annotatedRibs;

  private final @Nonnull Table<String, String, FinalMainRib> _ribs;

  private final @Nonnull SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      _prefixTracerSummary;

  private IncrementalDataPlane(Builder builder) {
    checkArgument(builder._nodes != null, "Dataplane must have nodes to be constructed");
    checkArgument(builder._partialDataplane != null, "Must have partial dataplane");

    // Grab the already-finalized FIBs and ForwardingAnalysis.
    PartialDataplane dataplane = builder._partialDataplane;
    _fibs = dataplane.getFibs();
    _forwardingAnalysis = dataplane.getForwardingAnalysis();

    Map<String, Node> nodes = builder._nodes;
    List<VirtualRouter> vrs =
        toListInRandomOrder(nodes.values().stream().flatMap(n -> n.getVirtualRouters().stream()));
    LOGGER.info("Computing BGP routes");
    _bgpRoutes = DataplaneUtil.computeBgpRoutes(vrs);
    LOGGER.info("Computing BGP backup routes");
    _bgpBackupRoutes = DataplaneUtil.computeBgpBackupRoutes(nodes, _bgpRoutes);
    LOGGER.info("Computing EVPN routes");
    _evpnRoutes = DataplaneUtil.computeEvpnRoutes(vrs);
    LOGGER.info("Computing EVPN BGP backup routes");
    _evpnBackupRoutes = DataplaneUtil.computeEvpnBackupRoutes(nodes, _evpnRoutes);
    LOGGER.info("Computing main RIBs");
    _ribs = computeRibs(vrs);
    _prefixTracerSummary = computePrefixTracingInfo(nodes);
    _layer2VniSettings = DataplaneUtil.computeLayer2VniSettings(nodes);
    _layer3VniSettings = DataplaneUtil.computeLayer3VniSettings(nodes);

    // For testing only
    _annotatedRibs = computeAnnotatedRibs(nodes);
  }

  private static @Nonnull SortedMap<
          String, SortedMap<String, GenericRib<AnnotatedRoute<AbstractRoute>>>>
      computeAnnotatedRibs(Map<String, Node> nodes) {
    return toImmutableSortedMap(
        nodes,
        Entry::getKey,
        nodeEntry ->
            toImmutableSortedMap(
                nodeEntry.getValue().getVirtualRouters(),
                VirtualRouter::getName,
                VirtualRouter::getMainRib));
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
