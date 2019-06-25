package org.batfish.minesweeper.answers;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Flow;

public final class FlowTraceHop implements Comparable<FlowTraceHop>, Serializable {
  private static final String PROP_EDGE = "edge";
  private static final String PROP_FILTER_IN = "filterIn";
  private static final String PROP_FILTER_OUT = "filterOut";
  private static final String PROP_ROUTES = "routes";

  private static final String PROP_TRANSFORMED_FLOW = "transformedFlow";

  private final Edge _edge;

  @Nullable private String _filterIn;

  @Nullable private String _filterOut;

  @Nonnull private final SortedSet<String> _routes;

  @Nullable private final Flow _transformedFlow;

  @JsonCreator
  public FlowTraceHop(
      @JsonProperty(PROP_EDGE) Edge edge,
      @Nullable @JsonProperty(PROP_ROUTES) SortedSet<String> routes,
      @Nullable @JsonProperty(PROP_FILTER_OUT) String filterOut,
      @Nullable @JsonProperty(PROP_FILTER_IN) String filterIn,
      @Nullable @JsonProperty(PROP_TRANSFORMED_FLOW) Flow transformedFlow) {
    _edge = edge;
    _routes = firstNonNull(routes, ImmutableSortedSet.of());
    _filterOut = filterOut;
    _filterIn = filterIn;
    _transformedFlow = transformedFlow;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof FlowTraceHop)) {
      return false;
    }
    FlowTraceHop other = (FlowTraceHop) obj;
    return Objects.equals(_edge, other._edge)
        && Objects.equals(_routes, other._routes)
        && Objects.equals(_filterOut, other._filterOut)
        && Objects.equals(_filterIn, other._filterIn)
        && Objects.equals(_transformedFlow, other._transformedFlow);
  }

  @JsonProperty(PROP_EDGE)
  public Edge getEdge() {
    return _edge;
  }

  @JsonProperty(PROP_FILTER_IN)
  @Nullable
  public String getFilterIn() {
    return _filterIn;
  }

  @JsonProperty(PROP_FILTER_OUT)
  @Nullable
  public String getFilterOut() {
    return _filterOut;
  }

  @JsonProperty(PROP_ROUTES)
  @Nonnull
  public SortedSet<String> getRoutes() {
    return _routes;
  }

  @JsonProperty(PROP_TRANSFORMED_FLOW)
  @Nullable
  public Flow getTransformedFlow() {
    return _transformedFlow;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_edge == null) ? 0 : _edge.hashCode());
    result = prime * result + _routes.hashCode();
    result = prime * result + ((_transformedFlow == null) ? 0 : _transformedFlow.hashCode());
    return result;
  }

  public void setFilterIn(@Nullable String filterIn) {
    _filterIn = filterIn;
  }

  public void setFilterOut(@Nullable String filterOut) {
    _filterOut = filterOut;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(FlowTraceHop.class)
        .omitNullValues()
        .add("edge", _edge)
        .add("routes", _routes)
        .add("filterIn", _filterIn)
        .add("filterOut", _filterOut)
        .add("transformedFlow", _transformedFlow)
        .toString();
  }

  @Override
  public int compareTo(FlowTraceHop o) {
    return Comparator.comparing(FlowTraceHop::getEdge)
        .thenComparing(
            FlowTraceHop::getTransformedFlow, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(FlowTraceHop::getRoutes, Comparators.lexicographical(Ordering.natural()))
        .thenComparing(FlowTraceHop::getFilterIn, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(FlowTraceHop::getFilterOut, Comparator.nullsFirst(Comparator.naturalOrder()))
        .compare(this, o);
  }
}
