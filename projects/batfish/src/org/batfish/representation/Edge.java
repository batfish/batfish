package org.batfish.representation;

import java.io.Serializable;

import org.batfish.collections.NodeInterfacePair;

public class Edge implements Serializable {

   private static final long serialVersionUID = 1L;

   private String _int1;
   private String _int2;
   private String _node1;
   private String _node2;

   public Edge(NodeInterfacePair p1, NodeInterfacePair p2) {
      _node1 = p1.getHostname();
      _node2 = p2.getHostname();
      _int1 = p1.getInterface();
      _int2 = p2.getInterface();
   }

   public Edge(String node1, String int1, String node2, String int2) {
      _node1 = node1;
      _node2 = node2;
      _int1 = int1;
      _int2 = int2;
   }

   @Override
   public boolean equals(Object obj) {
      Edge edge = (Edge) obj;
      return _int1.equals(edge._int1) && _int2.equals(edge._int2)
            && _node1.equals(edge._node1) && _node2.equals(edge._node2);
   }

   public String getInt1() {
      return _int1;
   }

   public String getInt2() {
      return _int2;
   }

   public String getNode1() {
      return _node1;
   }

   public String getNode2() {
      return _node2;
   }

   @Override
   public int hashCode() {
      return toString().hashCode();
   }

   @Override
   public String toString() {
      return _node1 + ", " + _int1 + ", " + _node2 + ", " + _int2;
   }

}
