package org.batfish.datamodel;

import org.batfish.common.Pair;
import org.batfish.datamodel.collections.NodeInterfacePair;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Edge extends Pair<NodeInterfacePair, NodeInterfacePair> {

   private static final long serialVersionUID = 1L;

   private static final String INT1_VAR = "int1";
   private static final String INT2_VAR = "int2";
   private static final String NODE1_VAR = "node1";
   private static final String NODE2_VAR = "node2";

   public Edge(NodeInterfacePair p1, NodeInterfacePair p2) {
      super(p1, p2);
   }

   @JsonCreator
   public Edge(@JsonProperty(NODE1_VAR) String node1,
         @JsonProperty(INT1_VAR) String int1,
         @JsonProperty(NODE2_VAR) String node2,
         @JsonProperty(INT2_VAR) String int2) {
      super(new NodeInterfacePair(node1, int1), new NodeInterfacePair(node2,
            int2));
   }

   @JsonProperty(INT1_VAR)
   public String getInt1() {
      return _first.getInterface();
   }

   @JsonProperty(INT2_VAR)
   public String getInt2() {
      return _second.getInterface();
   }

   @JsonIgnore
   public NodeInterfacePair getInterface1() {
      return _first;
   }

   @JsonIgnore
   public NodeInterfacePair getInterface2() {
      return _second;
   }

   @JsonProperty(NODE1_VAR)
   public String getNode1() {
      return _first.getHostname();
   }

   @JsonProperty(NODE2_VAR)
   public String getNode2() {
      return _second.getHostname();
   }

   @Override
   public String toString() {
      return getNode1() + ":" + getInt1() + ", " + getNode2() + ":" + getInt2();
   }

}
