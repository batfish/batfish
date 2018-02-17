package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;

public class TestDataPlane implements DataPlane {

  public static class Builder {
    private Map<String, Map<String, SortedSet<FibRow>>> _fibs;

    private Set<NodeInterfacePair> _flowSinks;

    private SortedMap<String, SortedMap<String, IRib<AbstractRoute>>> _ribs;

    private SortedSet<Edge> _topologyEdges;

    private Builder() {
      _fibs = ImmutableMap.of();
      _flowSinks = ImmutableSet.of();
      _ribs = ImmutableSortedMap.of();
      _topologyEdges = ImmutableSortedSet.of();
    }

    public TestDataPlane build() {
      return new TestDataPlane(_fibs, _flowSinks, _ribs, _topologyEdges);
    }

    public Builder setFibs(Map<String, Map<String, SortedSet<FibRow>>> fibs) {
      _fibs = fibs;
      return this;
    }

    public Builder setFlowSinks(Set<NodeInterfacePair> flowSinks) {
      _flowSinks = flowSinks;
      return this;
    }

    public Builder setRibs(SortedMap<String, SortedMap<String, IRib<AbstractRoute>>> ribs) {
      _ribs = ribs;
      return this;
    }

    public Builder setTopologyEdges(SortedSet<Edge> topologyEdges) {
      _topologyEdges = topologyEdges;
      return this;
    }
  }

  /** */
  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  private final Map<String, Map<String, SortedSet<FibRow>>> _fibs;

  private final Set<NodeInterfacePair> _flowSinks;

  private final SortedMap<String, SortedMap<String, IRib<AbstractRoute>>> _ribs;

  private final SortedSet<Edge> _topologyEdges;

  private TestDataPlane(
      Map<String, Map<String, SortedSet<FibRow>>> fibs,
      Set<NodeInterfacePair> flowSinks,
      SortedMap<String, SortedMap<String, IRib<AbstractRoute>>> ribs,
      SortedSet<Edge> topologyEdges) {
    _fibs = ImmutableMap.copyOf(fibs);
    _flowSinks = ImmutableSet.copyOf(flowSinks);
    _ribs = ImmutableSortedMap.copyOf(ribs);
    _topologyEdges = ImmutableSortedSet.copyOf(topologyEdges);
  }

  @Override
  public Map<String, Map<String, SortedSet<FibRow>>> getFibs() {
    return _fibs;
  }

  @Override
  public Set<NodeInterfacePair> getFlowSinks() {
    return _flowSinks;
  }

  @Override
  public SortedMap<String, Map<Ip, SortedSet<Edge>>> getPolicyRouteFibNodeMap() {
    throw new UnsupportedOperationException("not supported");
  }

  @Override
  public SortedMap<String, SortedMap<String, IRib<AbstractRoute>>> getRibs() {
    return _ribs;
  }

  @Override
  public SortedSet<Edge> getTopologyEdges() {
    return _topologyEdges;
  }
}
