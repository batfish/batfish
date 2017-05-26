package org.batfish.datamodel;

import org.batfish.common.Pair;
import org.batfish.datamodel.collections.VerboseNodeInterfacePair;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class VerboseEdge
      extends Pair<VerboseNodeInterfacePair, VerboseNodeInterfacePair> {
   private static final String EDGE_VAR = "edgesummary";

   private static final String INT1_VAR = "node1interface";

   private static final String INT2_VAR = "node2interface";

   private static final String NODE1_VAR = "node1";

   private static final String NODE2_VAR = "node2";

   private static final long serialVersionUID = 1L;

   protected final Edge edge;

   @JsonCreator
   public VerboseEdge(@JsonProperty(NODE1_VAR) Configuration node1,
         @JsonProperty(INT1_VAR) Interface int1,
         @JsonProperty(NODE2_VAR) Configuration node2,
         @JsonProperty(INT2_VAR) Interface int2,
         @JsonProperty(EDGE_VAR) Edge e) {
      this(new VerboseNodeInterfacePair(node1, int1),
            new VerboseNodeInterfacePair(node2, int2), e);
   }

   public VerboseEdge(VerboseNodeInterfacePair p1, VerboseNodeInterfacePair p2,
         Edge e) {
      super(p1, p2);
      this.edge = e;
   }

   @JsonProperty(EDGE_VAR)
   public Edge getEdgeSummary() {
      return edge;
   }

   @JsonProperty(INT1_VAR)
   public Interface getInt1() {
      return _first.getInterface();
   }

   @JsonProperty(INT2_VAR)
   public Interface getInt2() {
      return _second.getInterface();
   }

   @JsonIgnore
   public VerboseNodeInterfacePair getInterface1() {
      return _first;
   }

   @JsonIgnore
   public VerboseNodeInterfacePair getInterface2() {
      return _second;
   }

   @JsonProperty(NODE1_VAR)
   public Configuration getNode1() {
      return _first.getHost();
   }

   @JsonProperty(NODE2_VAR)
   public Configuration getNode2() {
      return _second.getHost();
   }

   @Override
   public String toString() {
      return "<" + getNode1() + ":" + getInt1() + ", " + getNode2() + ":"
            + getInt2() + ">";
   }

}
