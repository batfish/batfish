package org.batfish.datamodel;

import org.batfish.common.Pair;
import org.batfish.datamodel.collections.NodeInterfacePair;

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
      return _first.getInterface();
   }

   public String getInt2() {
      return _second.getInterface();
   }

   public NodeInterfacePair getInterface1() {
      return _first;
   }

   public NodeInterfacePair getInterface2() {
      return _second;
   }

   public String getNode1() {
      return _first.getHostname();
   }

   public String getNode2() {
      return _second.getHostname();
   }

   @Override
   public String toString() {
      return getNode1() + ", " + getInt1() + ", " + getNode2() + ", "
            + getInt2();
   }

}
