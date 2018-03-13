package org.batfish.datamodel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

public class TestDataPlane implements DataPlane {

  public static class Builder {

    private Map<String, Map<String, Fib>> _fibs;

    private SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> _ribs;

    private SortedSet<Edge> _topologyEdges;

    private Builder() {
      _fibs = ImmutableMap.of();
      _ribs = ImmutableSortedMap.of();
      _topologyEdges = ImmutableSortedSet.of();
    }

    public TestDataPlane build() {
      return new TestDataPlane(_fibs, _ribs, _topologyEdges);
    }

    public Builder setRibs(SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs) {
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

  private final Map<String, Map<String, Fib>> _fibs;

  private final SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> _ribs;

  private final SortedSet<Edge> _topologyEdges;

  private TestDataPlane(
      Map<String, Map<String, Fib>> fibs,
      SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> ribs,
      SortedSet<Edge> topologyEdges) {
    _fibs = fibs;
    _ribs = ImmutableSortedMap.copyOf(ribs);
    _topologyEdges = ImmutableSortedSet.copyOf(topologyEdges);
  }

  public Map<String, Map<String, Fib>> getFibs() {
    return _fibs;
  }

  @Override
  public SortedMap<String, SortedMap<String, GenericRib<AbstractRoute>>> getRibs() {
    return _ribs;
  }

  @Override
  public SortedSet<Edge> getTopologyEdges() {
    return _topologyEdges;
  }
}
