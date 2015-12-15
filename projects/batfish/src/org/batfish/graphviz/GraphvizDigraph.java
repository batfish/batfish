package org.batfish.graphviz;

import java.util.Set;
import java.util.TreeSet;

public class GraphvizDigraph extends GraphvizInput {

   private final Set<GraphvizEdge> _edges;

   private final Set<GraphvizNode> _nodes;

   public GraphvizDigraph() {
      _edges = new TreeSet<GraphvizEdge>();
      _nodes = new TreeSet<GraphvizNode>();
   }

   public Set<GraphvizEdge> getEdges() {
      return _edges;
   }

   public Set<GraphvizNode> getNodes() {
      return _nodes;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("digraph G {\n");
      for (GraphvizNode node : _nodes) {
         sb.append("\t" + node.toString() + "\n");
      }
      for (GraphvizEdge edge : _edges) {
         sb.append("\t" + edge.toString() + "\n");
      }
      sb.append("}\n");
      return sb.toString();
   }

}
