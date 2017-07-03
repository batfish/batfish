package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.batfish.common.Pair;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;

public class VerboseBgpEdge
      extends Pair<NodeBgpSessionPair, NodeBgpSessionPair> {

   private static final String EDGE_SUMMARY_VAR = "edgeSummary";

   private static final String NODE1_SESSION_VAR = "node1Session";

   private static final String NODE1_VAR = "node1";

   private static final String NODE2_SESSION_VAR = "node2Session";

   private static final String NODE2_VAR = "node2";

   private static final long serialVersionUID = 1L;

   protected final IpEdge edge;

   @JsonCreator
   public VerboseBgpEdge(@JsonProperty(NODE1_VAR) Configuration node1,
         @JsonProperty(NODE1_SESSION_VAR) BgpNeighbor s1,
         @JsonProperty(NODE2_VAR) Configuration node2,
         @JsonProperty(NODE2_SESSION_VAR) BgpNeighbor s2,
         @JsonProperty(EDGE_SUMMARY_VAR) IpEdge e) {
      super(new NodeBgpSessionPair(node1, s1),
            new NodeBgpSessionPair(node2, s2));
      this.edge = e;
   }

   @JsonProperty(EDGE_SUMMARY_VAR)
   public IpEdge getEdgeSummary() {
      return edge;
   }

   @JsonProperty(NODE1_VAR)
   public Configuration getNode1() {
      return _first.getNode();
   }

   @JsonProperty(NODE1_SESSION_VAR)
   public BgpNeighbor getNode1Session() {
      return _first.getSession();
   }

   @JsonProperty(NODE2_VAR)
   public Configuration getNode2() {
      return _second.getNode();
   }

   @JsonProperty(NODE2_SESSION_VAR)
   public BgpNeighbor getNode2Session() {
      return _second.getSession();
   }
}
