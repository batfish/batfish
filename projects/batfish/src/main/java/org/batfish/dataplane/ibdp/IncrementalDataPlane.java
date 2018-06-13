package org.batfish.dataplane.ibdp;

import static org.batfish.common.util.CommonUtil.toImmutableMap;
import static org.batfish.common.util.CommonUtil.toImmutableSortedMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.graph.Network;
import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpSession;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.ForwardingAnalysis;
import org.batfish.datamodel.ForwardingAnalysisImpl;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Topology;

public final class IncrementalDataPlane implements Serializable, DataPlane {

  private static final long serialVersionUID = 1L;

  public static class Builder {

    private Network<BgpNeighbor, BgpSession> _bgpTopology;

    private Map<Ip, Set<String>> _ipOwners;

    private Map<Ip, String> _ipOwnersSimple;

    private Map<Ip, Map<String, Set<String>>> _ipVrfOwners;

    private Map<String, Node> _nodes;

    private Topology _topology;

    public Builder setBgpTopology(Network<BgpNeighbor, BgpSession> bgpTopology) {
      _bgpTopology = bgpTopology;
      return this;
    }

    public Builder setIpOwners(Map<Ip, Set<String>> ipOwners) {
      _ipOwners = ImmutableMap.copyOf(ipOwners);
      return this;
    }

    public Builder setIpOwnersSimple(Map<Ip, String> ipOwnersSimple) {
      _ipOwnersSimple = ImmutableMap.copyOf(ipOwnersSimple);
      return this;
    }

    public Builder setIpVrfOwners(Map<Ip, Map<String, Set<String>>> ipVrfOwners) {
      _ipVrfOwners = ImmutableMap.copyOf(ipVrfOwners);
      return this;
    }

    public Builder setNodes(Map<String, Node> nodes) {
      _nodes = ImmutableMap.copyOf(nodes);
      return this;
    }

    public Builder setTopology(Topology topology) {
      _topology = topology;
      return this;
    }

    public IncrementalDataPlane build() {
      return new IncrementalDataPlane(this);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private final transient Network<BgpNeighbor, BgpSession> _bgpTopology;

  private transient Map<String, Configuration> _configurations;

  private transient Map<String, Map<String, Fib>> _fibs;

  private transient ForwardingAnalysis _forwardingAnalysis;

  private final Map<Ip, Set<String>> _ipOwners;

  private final Map<Ip, String> _ipOwnersSimple;

  private final Map<Ip, Map<String, Set<String>>> _ipVrfOwners;

  private final Map<String, Node> _nodes;

  private transient SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> _ribs;

  private final Topology _topology;

  private IncrementalDataPlane(Builder builder) {
    _bgpTopology = builder._bgpTopology;
    _ipOwners = builder._ipOwners;
    _ipOwnersSimple = builder._ipOwnersSimple;
    _ipVrfOwners = builder._ipVrfOwners;
    _nodes = builder._nodes;
    _topology = builder._topology;
  }

  @Override
  public Network<BgpNeighbor, BgpSession> getBgpTopology() {
    return _bgpTopology;
  }

  @Override
  public Map<String, Configuration> getConfigurations() {
    if (_configurations == null) {
      _configurations = initConfigurations();
    }
    return _configurations;
  }

  @Override
  public Map<String, Map<String, Fib>> getFibs() {
    if (_fibs == null) {
      _fibs = initFibs();
    }
    return _fibs;
  }

  @Override
  public ForwardingAnalysis getForwardingAnalysis() {
    if (_forwardingAnalysis == null) {
      _forwardingAnalysis = initForwardingAnalysis();
    }
    return _forwardingAnalysis;
  }

  @Override
  public Map<Ip, Set<String>> getIpOwners() {
    return _ipOwners;
  }

  @Override
  public Map<Ip, Map<String, Set<String>>> getIpVrfOwners() {
    return _ipVrfOwners;
  }

  public Map<Ip, String> getIpOwnersSimple() {
    return _ipOwnersSimple;
  }

  public Map<String, Node> getNodes() {
    return _nodes;
  }

  /**
   * Retrieve the {@link PrefixTracer} for each {@link VirtualRouter} after dataplane computation.
   * Map structure: Hostname -> VRF name -> prefix tracer.
   */
  public SortedMap<String, SortedMap<String, PrefixTracer>> getPrefixTracingInfo() {
    /*
     * Iterate over nodes, then virtual routers, and extract prefix tracer from each.
     * Sort hostnames and VRF names
     */
    return toImmutableSortedMap(
        _nodes,
        Entry::getKey,
        nodeEntry ->
            toImmutableSortedMap(
                nodeEntry.getValue().getVirtualRouters(),
                Entry::getKey,
                vrfEntry -> vrfEntry.getValue().getPrefixTracer()));
  }

  @Override
  public SortedMap<String, SortedMap<String, Map<Prefix, Map<String, Set<String>>>>>
      getPrefixTracingInfoSummary() {
    /*
     * Iterate over nodes, then virtual routers, and extract prefix tracer from each.
     * Sort hostnames and VRF names
     */
    return toImmutableSortedMap(
        _nodes,
        Entry::getKey,
        nodeEntry ->
            toImmutableSortedMap(
                nodeEntry.getValue().getVirtualRouters(),
                Entry::getKey,
                vrfEntry -> vrfEntry.getValue().getPrefixTracer().summarize()));
  }

  @Override
  public SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> getRibs() {
    if (_ribs == null) {
      _ribs = initRibs();
    }
    return _ribs;
  }

  @Override
  public Topology getTopology() {
    return _topology;
  }

  @Override
  public SortedSet<Edge> getTopologyEdges() {
    return _topology.getEdges();
  }

  private Map<String, Configuration> initConfigurations() {
    return _nodes
        .entrySet()
        .stream()
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, e -> e.getValue().getConfiguration()));
  }

  private Map<String, Map<String, Fib>> initFibs() {
    return toImmutableMap(
        _nodes,
        Entry::getKey,
        nodeEntry ->
            toImmutableMap(
                nodeEntry.getValue().getVirtualRouters(),
                Entry::getKey,
                vrfEntry -> vrfEntry.getValue().getFib()));
  }

  private ForwardingAnalysis initForwardingAnalysis() {
    return new ForwardingAnalysisImpl(getConfigurations(), getRibs(), getFibs(), getTopology());
  }

  private SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> initRibs() {
    return toImmutableSortedMap(
        _nodes,
        Entry::getKey,
        nodeEntry ->
            toImmutableSortedMap(
                nodeEntry.getValue().getVirtualRouters(),
                Entry::getKey,
                vrfEntry -> vrfEntry.getValue().getMainRib()));
  }
}
