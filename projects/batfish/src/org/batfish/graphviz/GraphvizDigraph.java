package org.batfish.graphviz;

import java.util.Set;
import java.util.TreeSet;

import org.batfish.datamodel.Prefix;

public class GraphvizDigraph extends GraphvizInput {

   public static String getGraphName(Prefix prefix) {
      return "graph_" + prefix.toString().replace('/', '_').replace('.', '_');
   }

   private final Set<GraphvizEdge> _edges;

   private final Set<GraphvizNode> _nodes;

   public GraphvizDigraph(String name) {
      super(name);
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
      sb.append("digraph " + _name + " {\n");
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
