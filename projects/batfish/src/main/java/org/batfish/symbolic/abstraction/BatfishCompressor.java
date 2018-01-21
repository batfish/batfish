package org.batfish.symbolic.abstraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixTrie;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.AbstractionPrefixSet;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs.StaticBooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.Statements.StaticStatement;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.collections.Table2;

/*
 * Create a simpler network for use use by Batfish by adding
 * Filters on interfaces that correspond to the abstract network
 */
public class BatfishCompressor {

  private IBatfish _batfish;

  private Graph _graph;

  public BatfishCompressor(IBatfish batfish) {
    _batfish = batfish;
    _graph = new Graph(batfish);
  }

  private void addAll(Map<GraphEdge, PrefixTrie> newMap, Map<GraphEdge, PrefixTrie> oldMap) {
    for (Entry<GraphEdge, PrefixTrie> entry : oldMap.entrySet()) {
      PrefixTrie s = newMap.get(entry.getKey());
      if (s == null) {
        newMap.put(entry.getKey(), entry.getValue());
      } else {
        s.addAll(entry.getValue().getPrefixes());
      }
    }
  }

  private Map<GraphEdge, PrefixTrie> mergeFilters(
      Map<GraphEdge, PrefixTrie> x, Map<GraphEdge, PrefixTrie> y) {
    Map<GraphEdge, PrefixTrie> newMap = new HashMap<>();
    addAll(newMap, x);
    addAll(newMap, y);
    return newMap;
  }

  private Prefix makeCanonical(Prefix p) {
    int length = p.getPrefixLength();
    long l = p.getAddress().asLong();
    l = (l >> 32 - length);
    l = (l << 32 - length);
    Ip ip = new Ip(l);
    return new Prefix(ip, length);
  }

  private Map<GraphEdge, PrefixTrie> processSlice(NetworkSlice slice) {
    System.out.println("Slice DstIps:    " + slice.getHeaderSpace().getDstIps());
    System.out.println("Slice NotDstIps: " + slice.getHeaderSpace().getNotDstIps());
    Map<GraphEdge, PrefixTrie> filters = new HashMap<>();
    for (GraphEdge edge : slice.getGraph().getAllEdges()) {
      if (!edge.isAbstract() && !_graph.isLoopback(edge)) {
        PrefixTrie pt = new PrefixTrie();
        for (IpWildcard wc : slice.getHeaderSpace().getDstIps()) {
          // TODO: properly cover the entire space
          pt.add(wc.toPrefix());
        }
        filters.put(edge, pt);
      }
    }
    return filters;
  }

  private void applyFilters(@Nullable RoutingPolicy pol, @Nullable PrefixTrie filters) {
    if (pol == null) {
      return;
    }
    If i = new If();
    List<Statement> newStatements = new ArrayList<>();
    List<Statement> falseStatements = new ArrayList<>();
    Statement reject = new StaticStatement(Statements.ExitReject);
    falseStatements.add(reject);
    if (filters == null) {
      StaticBooleanExpr sbe = new StaticBooleanExpr(BooleanExprs.False);
      i.setGuard(sbe);
    } else {
      AbstractionPrefixSet eps = new AbstractionPrefixSet(filters);
      MatchPrefixSet match = new MatchPrefixSet(new DestinationNetwork(), eps);
      i.setGuard(match);
    }
    i.setFalseStatements(falseStatements);
    i.setTrueStatements(pol.getStatements());
    newStatements.add(i);
    pol.setStatements(newStatements);
  }

  private Map<String, Configuration> applyFilters(
      Table2<String, GraphEdge, PrefixTrie> filtersByRouter) {
    Map<String, Configuration> newConfigs = new HashMap<>();
    for (Entry<String, Configuration> entry : _graph.getConfigurations().entrySet()) {
      String router = entry.getKey();
      Map<GraphEdge, PrefixTrie> filters = filtersByRouter.get(router);
      if (filters != null) {
        for (GraphEdge ge : _graph.getEdgeMap().get(router)) {
          PrefixTrie space = filters.get(ge);
          RoutingPolicy ipol = _graph.findImportRoutingPolicy(router, Protocol.BGP, ge);
          RoutingPolicy epol = _graph.findExportRoutingPolicy(router, Protocol.BGP, ge);
          applyFilters(ipol, space);
          applyFilters(epol, space);
        }
        newConfigs.put(router, entry.getValue());
      }
    }
    return newConfigs;
  }

  public Map<String, Configuration> compress(HeaderSpace h) {
    DestinationClasses dcs = DestinationClasses.create(_batfish, h, true);
    ArrayList<Supplier<NetworkSlice>> ecs = NetworkSlice.allSlices(dcs, 0);
    Map<GraphEdge, PrefixTrie> filters =
        ecs.parallelStream().map(s -> processSlice(s.get())).reduce(this::mergeFilters).get();
    Table2<String, GraphEdge, PrefixTrie> filtersByRouter = new Table2<>();
    for (Entry<GraphEdge, PrefixTrie> entry : filters.entrySet()) {
      GraphEdge ge = entry.getKey();
      filtersByRouter.put(ge.getRouter(), ge, entry.getValue());
    }
    /* filters.forEach(
        (ge, filts) -> {
          System.out.println("Edge: " + ge);
          for (PrefixRange filt : filts.getPrefixRanges()) {
            System.out.println("  filter: " + filt);
          }
        }); */
    Map<String, Configuration> newConfigs = applyFilters(filtersByRouter);
    return newConfigs;
  }
}