package org.batfish.grammar.topology;

import java.util.ArrayList;
import java.util.List;

import org.batfish.grammar.topology.GNS3TopologyParser.*;
import org.batfish.representation.Edge;
import org.batfish.representation.Topology;

public class GNS3TopologyExtractor extends GNS3TopologyParserBaseListener
      implements TopologyExtractor {

   private String _currentRouter;
   private List<Edge> _edges;
   private Topology _topology;

   public GNS3TopologyExtractor() {
      _edges = new ArrayList<Edge>();
   }

   private String convertInterfaceName(String shortName) {
      return shortName.replace("f", "FastEthernet");
   }

   @Override
   public void enterRouter_line(Router_lineContext ctx) {
      _currentRouter = ctx.name.getText();
   }

   @Override
   public void exitEdge_line(Edge_lineContext ctx) {
      String int1 = convertInterfaceName(ctx.int1.getText());
      String node2 = ctx.host2.getText();
      String int2 = convertInterfaceName(ctx.int2.getText());
      Edge edge = new Edge(_currentRouter, int1, node2, int2);
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
