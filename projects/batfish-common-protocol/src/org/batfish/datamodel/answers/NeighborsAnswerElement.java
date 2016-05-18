package org.batfish.datamodel.answers;

import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.EdgeSet;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NeighborsAnswerElement implements AnswerElement {

   private final static String IP_NEIGHBORS_VAR = "ipNeighbors";
   
   private EdgeSet _ipNeighbors;

   public NeighborsAnswerElement() {
      _ipNeighbors = new EdgeSet();
   }
   
   public void addIpEdge(Edge edge) {
      _ipNeighbors.add(edge);
   }

   @JsonProperty(IP_NEIGHBORS_VAR)
   public EdgeSet getIpNeighbors() {
      return _ipNeighbors;
   }

   public void setIpNeighbors(EdgeSet edges) {
      _ipNeighbors = edges;
   }
}
