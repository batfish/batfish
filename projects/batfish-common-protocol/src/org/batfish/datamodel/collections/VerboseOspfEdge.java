package org.batfish.datamodel.collections;

import org.batfish.common.Pair;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OspfNeighbor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VerboseOspfEdge extends Pair<NodeOspfSessionPair, NodeOspfSessionPair> {

   private static final String EDGE_VAR = "edgesummary";

   private static final String SESSION1_VAR = "node1session";

   private static final String SESSION2_VAR = "node2session";

   private static final String NODE1_VAR = "node1";

   private static final String NODE2_VAR = "node2";
 
   protected final IpEdge edge;
   
   private static final long serialVersionUID = 1L;

   @JsonCreator
   public VerboseOspfEdge(@JsonProperty(NODE1_VAR) Configuration node1,
         @JsonProperty(SESSION1_VAR) OspfNeighbor s1,         
         @JsonProperty(NODE2_VAR) Configuration node2,
         @JsonProperty(SESSION2_VAR) OspfNeighbor s2,
         @JsonProperty(EDGE_VAR) IpEdge e) {
      super(new NodeOspfSessionPair(node1, s1),
            new NodeOspfSessionPair(node2, s2));
      this.edge = e;
   }

   @JsonProperty(EDGE_VAR)
   public IpEdge getEdgeSummary() {
      return edge;
   }
   
   @JsonProperty(NODE1_VAR)
   public Configuration getNode1() {
      return _first.getHost();
   }
   
   @JsonProperty(SESSION1_VAR)
   public OspfNeighbor getSession1() {
      return _first.getSession();
   }
   
   @JsonProperty(NODE2_VAR)
   public Configuration getNode2() {
      return _second.getHost();
   }
   
   @JsonProperty(SESSION2_VAR)
   public OspfNeighbor getSession2() {
      return _second.getSession();
   }
}
