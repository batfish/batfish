package org.batfish.datamodel.answers;

import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.datamodel.Edge;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NeighborsAnswerElement implements AnswerElement {

   private final static String IP_NEIGHBORS_VAR = "ipNeighbors";

   private SortedSet<Edge> _ipNeighbors;

   public NeighborsAnswerElement() {
      _ipNeighbors = new TreeSet<Edge>();
   }

   public void addIpEdge(Edge edge) {
      _ipNeighbors.add(edge);
   }

   @JsonProperty(IP_NEIGHBORS_VAR)
   public SortedSet<Edge> getIpNeighbors() {
      return _ipNeighbors;
   }

   public void setIpNeighbors(SortedSet<Edge> edges) {
      _ipNeighbors = edges;
   }
}
