package org.batfish.grammar.topology;

import org.batfish.collections.EdgeSet;
import org.batfish.grammar.topology.BatfishTopologyParser.*;
import org.batfish.representation.Edge;
import org.batfish.representation.Topology;

public class BatfishTopologyExtractor extends BatfishTopologyParserBaseListener
      implements TopologyExtractor {

   private EdgeSet _edges;

   private Topology _topology;

   public BatfishTopologyExtractor() {
      _edges = new EdgeSet();
   }

   @Override
   public void exitEdge_line(Edge_lineContext ctx) {
      String node1 = ctx.node1.getText();
      String node2 = ctx.node2.getText();
      String int1 = ctx.int1.getText();
      String int2 = ctx.int2.getText();
      Edge edge = new Edge(node1, int1, node2, int2);
      _edges.add(edge);
   }

   @Override
   public void exitTopology(TopologyContext ctx) {
      _topology = new Topology(_edges);
   }

   @Override
   public Topology getTopology() {
      return _topology;
   }

}
