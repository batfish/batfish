package org.batfish.grammar.topology;

import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.Topology;
import org.batfish.grammar.topology.GNS3TopologyParser.Edge_lineContext;
import org.batfish.grammar.topology.GNS3TopologyParser.Router_lineContext;
import org.batfish.grammar.topology.GNS3TopologyParser.TopologyContext;

public class GNS3TopologyExtractor extends GNS3TopologyParserBaseListener
    implements TopologyExtractor {

  private String _currentRouter;
  private SortedSet<Edge> _edges;
  private Topology _topology;

  public GNS3TopologyExtractor() {
    _edges = new TreeSet<>();
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
