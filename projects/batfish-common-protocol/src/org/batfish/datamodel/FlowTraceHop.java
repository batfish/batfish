package org.batfish.datamodel;

import java.io.Serializable;
import java.util.SortedSet;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class FlowTraceHop implements Serializable {

   private static final String EDGE_VAR = "edge";

   private static final String ROUTES_VAR = "routes";

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private static final String TRANSFORMED_FLOW_VAR = "transformedFlow";

   private final Edge _edge;

   private final SortedSet<String> _routes;

   private final Flow _transformedFlow;

   @JsonCreator
   public FlowTraceHop(@JsonProperty(EDGE_VAR) Edge edge,
         @JsonProperty(ROUTES_VAR) SortedSet<String> routes,
         @JsonProperty(TRANSFORMED_FLOW_VAR) Flow transformedFlow) {
      _edge = edge;
      _routes = routes;
      _transformedFlow = transformedFlow;
   }

   @JsonProperty(EDGE_VAR)
   public Edge getEdge() {
      return _edge;
   }

   @JsonProperty(ROUTES_VAR)
   public SortedSet<String> getRoutes() {
      return _routes;
   }

   @JsonProperty(TRANSFORMED_FLOW_VAR)
   public Flow getTransformedFlow() {
      return _transformedFlow;
   }

}
