package org.batfish.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.batfish.collections.EdgeSet;
import org.batfish.collections.NodeInterfacePair;
import org.batfish.common.BatfishException;

public class Topology implements Serializable {

   private static final long serialVersionUID = 1L;

   private final EdgeSet _edges;

   private final transient Map<NodeInterfacePair, EdgeSet> _interfaceEdges;

   private final transient Map<String, EdgeSet> _nodeEdges;

   public Topology(EdgeSet edges) {
      _edges = edges;
      _nodeEdges = new HashMap<String, EdgeSet>();
      _interfaceEdges = new HashMap<NodeInterfacePair, EdgeSet>();
      for (Edge edge : edges) {
         String node1 = edge.getNode1();
         String node2 = edge.getNode2();
         NodeInterfacePair int1 = edge.getInterface1();
         NodeInterfacePair int2 = edge.getInterface2();

         EdgeSet node1Edges = _nodeEdges.get(node1);
         if (node1Edges == null) {
            node1Edges = new EdgeSet();
            _nodeEdges.put(node1, node1Edges);
         }
         node1Edges.add(edge);

         EdgeSet node2Edges = _nodeEdges.get(node2);
         if (node2Edges == null) {
            node2Edges = new EdgeSet();
            _nodeEdges.put(node2, node2Edges);
         }
         node2Edges.add(edge);

         EdgeSet interface1Edges = _interfaceEdges.get(int1);
         if (interface1Edges == null) {
            interface1Edges = new EdgeSet();
            _interfaceEdges.put(int1, interface1Edges);
         }
         interface1Edges.add(edge);

         EdgeSet interface2Edges = _interfaceEdges.get(int2);
         if (interface2Edges == null) {
            interface2Edges = new EdgeSet();
            _interfaceEdges.put(int2, interface2Edges);
         }
         interface2Edges.add(edge);
      }
   }

   public EdgeSet getEdges() {
      return _edges;
   }

   public void removeEdge(Edge edge) {
      _edges.remove(edge);
   }

   public void removeInterface(NodeInterfacePair iface) {
      if (_interfaceEdges == null) {
         throw new BatfishException(
               "Operation not supported on deserialized topology");
      }
      EdgeSet interfaceEdges = _interfaceEdges.get(iface);
      if (interfaceEdges != null) {
         _edges.removeAll(interfaceEdges);
      }
   }

   public void removeNode(String hostname) {
      if (_nodeEdges == null) {
         throw new BatfishException(
               "Operation not supported on deserialized topology");
      }
      EdgeSet nodeEdges = _nodeEdges.get(hostname);
      if (nodeEdges != null) {
         _edges.removeAll(nodeEdges);
      }
   }

}
