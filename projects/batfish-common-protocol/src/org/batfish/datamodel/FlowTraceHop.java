package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Set;
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

   private final Edge _edge;

   private final SortedSet<String> _routes;

   @JsonCreator
   public FlowTraceHop(@JsonProperty(EDGE_VAR) Edge edge,
         @JsonProperty(ROUTES_VAR) SortedSet<String> routes) {
      _edge = edge;
      _routes = routes;
   }

   @JsonProperty(EDGE_VAR)
   public Edge getEdge() {
      return _edge;
   }

   @JsonProperty(ROUTES_VAR)
   public Set<String> getRoutes() {
      return _routes;
   }

}
