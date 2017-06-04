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

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      FlowTraceHop other = (FlowTraceHop) obj;
      if (_edge == null) {
         if (other._edge != null) {
            return false;
         }
      }
      else if (!_edge.equals(other._edge)) {
         return false;
      }
      if (_routes == null) {
         if (other._routes != null) {
            return false;
         }
      }
      else if (!_routes.equals(other._routes)) {
         return false;
      }
      if (_transformedFlow == null) {
         if (other._transformedFlow != null) {
            return false;
         }
      }
      else if (!_transformedFlow.equals(other._transformedFlow)) {
         return false;
      }
      return true;
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

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((_edge == null) ? 0 : _edge.hashCode());
      result = prime * result + ((_routes == null) ? 0 : _routes.hashCode());
      result = prime * result
            + ((_transformedFlow == null) ? 0 : _transformedFlow.hashCode());
      return result;
   }

}
