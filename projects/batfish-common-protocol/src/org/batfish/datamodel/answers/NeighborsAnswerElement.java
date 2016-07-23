package org.batfish.datamodel.answers;

import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.collections.IpEdge;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class NeighborsAnswerElement implements AnswerElement {

   private static final String EBGP_NEIGHBORS_VAR = "ebgpNeighbors";

   private static final String IBGP_NEIGHBORS_VAR = "ibgpNeighbors";

   private final static String LAN_NEIGHBORS_VAR = "lanNeighbors";

   private SortedSet<IpEdge> _ebgpNeighbors;

   private SortedSet<IpEdge> _ibgpNeighbors;

   private SortedSet<Edge> _lanNeighbors;

   public void addLanEdge(Edge edge) {
      _lanNeighbors.add(edge);
   }

   @JsonProperty(EBGP_NEIGHBORS_VAR)
   public SortedSet<IpEdge> getEbgpNeighbors() {
      return _ebgpNeighbors;
   }

   @JsonProperty(IBGP_NEIGHBORS_VAR)
   public SortedSet<IpEdge> getIbgpNeighbors() {
      return _ibgpNeighbors;
   }

   @JsonProperty(LAN_NEIGHBORS_VAR)
   public SortedSet<Edge> getLanNeighbors() {
      return _lanNeighbors;
   }

   public void initEbgpNeighbors() {
      _ebgpNeighbors = new TreeSet<IpEdge>();
   }

   public void initIbgpNeighbors() {
      _ibgpNeighbors = new TreeSet<IpEdge>();
   }

   public void initLanNeighbors() {
      _lanNeighbors = new TreeSet<Edge>();
   }

   @JsonProperty(EBGP_NEIGHBORS_VAR)
   public void setEbgpNeighbors(SortedSet<IpEdge> ebgpNeighbors) {
      _ebgpNeighbors = ebgpNeighbors;
   }

   @JsonProperty(IBGP_NEIGHBORS_VAR)
   public void setIbgpNeighbors(SortedSet<IpEdge> ibgpNeighbors) {
      _ibgpNeighbors = ibgpNeighbors;
   }

   @JsonProperty(LAN_NEIGHBORS_VAR)
   public void setLanNeighbors(SortedSet<Edge> lanNeighbors) {
      _lanNeighbors = lanNeighbors;
   }
   
   @Override
   public String prettyPrint() throws JsonProcessingException {
      //TODO: change this function to pretty print the answer
      ObjectMapper mapper = new BatfishObjectMapper();
      return mapper.writeValueAsString(this);
   }
}
