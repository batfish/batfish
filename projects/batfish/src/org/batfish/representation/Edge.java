package org.batfish.representation;

import org.batfish.collections.NodeInterfacePair;
import org.batfish.common.Pair;

public class Edge extends Pair<NodeInterfacePair, NodeInterfacePair> {

   private static final long serialVersionUID = 1L;

   public Edge(NodeInterfacePair p1, NodeInterfacePair p2) {
      super(p1, p2);
   }

   public Edge(String node1, String int1, String node2, String int2) {
      super(new NodeInterfacePair(node1, int1), new NodeInterfacePair(node2,
            int2));
   }

   public String getInt1() {
      return _t1.getInterface();
   }

   public String getInt2() {
      return _t2.getInterface();
   }

   public NodeInterfacePair getInterface1() {
      return _t1;
   }

   public NodeInterfacePair getInterface2() {
      return _t2;
   }

   public String getNode1() {
      return _t1.getHostname();
   }

   public String getNode2() {
      return _t2.getHostname();
   }

   @Override
   public String toString() {
      return getNode1() + ", " + getInt1() + ", " + getNode2() + ", "
            + getInt2();
   }

}
