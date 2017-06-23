package org.batfish.datamodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.batfish.datamodel.collections.EdgeSet;
import org.batfish.datamodel.collections.NodeInterfacePair;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public class Topology implements Serializable {

   private static final long serialVersionUID = 1L;

   @JsonCreator
   private static Topology jacksonCreateTopology(SortedSet<Edge> edges) {
      return new Topology(new EdgeSet(edges));
   }

   private final EdgeSet _edges;

   private final Map<NodeInterfacePair, EdgeSet> _interfaceEdges;

   private final Map<String, EdgeSet> _nodeEdges;

   public Topology(EdgeSet edges) {
      _edges = edges;
      _nodeEdges = new HashMap<>();
      _interfaceEdges = new HashMap<>();
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

   @JsonIgnore
   public EdgeSet getEdges() {
      return _edges;
   }

   @JsonIgnore
   public Map<NodeInterfacePair, EdgeSet> getInterfaceEdges() {
      return _interfaceEdges;
   }

   @JsonIgnore
   public Map<String, EdgeSet> getNodeEdges() {
      return _nodeEdges;
   }

   public void removeEdge(Edge edge) {
      _edges.remove(edge);
   }

   public void removeInterface(NodeInterfacePair iface) {
      EdgeSet interfaceEdges = _interfaceEdges.get(iface);
      if (interfaceEdges != null) {
         _edges.removeAll(interfaceEdges);
      }
   }

   public void removeNode(String hostname) {
      EdgeSet nodeEdges = _nodeEdges.get(hostname);
      if (nodeEdges != null) {
         _edges.removeAll(nodeEdges);
      }
   }

   @JsonValue
   public SortedSet<Edge> sortedEdges() {
      return new TreeSet<>(_edges);
   }

}
