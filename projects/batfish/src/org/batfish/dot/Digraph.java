package org.batfish.dot;

import java.util.Set;
import java.util.TreeSet;

public class Digraph extends DotInput {

   private final Set<Edge> _edges;

   private final Set<Node> _nodes;

   public Digraph() {
      _edges = new TreeSet<Edge>();
      _nodes = new TreeSet<Node>();
   }

   public Set<Edge> getEdges() {
      return _edges;
   }

   public Set<Node> getNodes() {
      return _nodes;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("digraph G {\n");
      for (Node node : _nodes) {
         sb.append("\t" + node.toString() + "\n");
      }
      for (Edge edge : _edges) {
         sb.append("\t" + edge.toString() + "\n");
      }
      sb.append("}\n");
      return sb.toString();
   }

}
