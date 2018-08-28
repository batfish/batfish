package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nullable;

public final class FlowTraceHop implements Serializable {

  private static final String PROP_EDGE = "edge";

  private static final String PROP_FILTER_IN = "filterIn";

  private static final String PROP_FILTER_OUT = "filterOut";

  private static final String PROP_ROUTES = "routes";

  /** */
  private static final long serialVersionUID = 1L;

  private static final String PROP_TRANSFORMED_FLOW = "transformedFlow";

  private final Edge _edge;

  @Nullable private String _filterIn;

  @Nullable private String _filterOut;

  private final SortedSet<String> _routes;

  private final Flow _transformedFlow;

  @JsonCreator
  public FlowTraceHop(
      @JsonProperty(PROP_EDGE) Edge edge,
      @JsonProperty(PROP_ROUTES) SortedSet<String> routes,
      @JsonProperty(PROP_FILTER_OUT) String filterOut,
      @JsonProperty(PROP_FILTER_IN) String filterIn,
      @JsonProperty(PROP_TRANSFORMED_FLOW) Flow transformedFlow) {
    _edge = edge;
    _routes = routes;
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
  public String getFilterIn() {
    return _filterIn;
  }

  @JsonProperty(PROP_FILTER_OUT)
  public String getFilterOut() {
    return _filterOut;
  }

  @JsonProperty(PROP_ROUTES)
  public SortedSet<String> getRoutes() {
    return _routes;
  }

  @JsonProperty(PROP_TRANSFORMED_FLOW)
  public Flow getTransformedFlow() {
    return _transformedFlow;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_edge == null) ? 0 : _edge.hashCode());
    result = prime * result + ((_routes == null) ? 0 : _routes.hashCode());
    result = prime * result + ((_transformedFlow == null) ? 0 : _transformedFlow.hashCode());
    return result;
  }

  public void setFilterIn(String filterIn) {
    _filterIn = filterIn;
  }

  public void setFilterOut(String filterOut) {
    _filterOut = filterOut;
  }
}
