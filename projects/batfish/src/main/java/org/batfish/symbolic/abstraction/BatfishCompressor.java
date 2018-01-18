package org.batfish.symbolic.abstraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs.StaticBooleanExpr;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
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

  private void addAll(
      Map<GraphEdge, Set<PrefixRange>> newMap, Map<GraphEdge, Set<PrefixRange>> oldMap) {
    for (Entry<GraphEdge, Set<PrefixRange>> entry : oldMap.entrySet()) {
      Set<PrefixRange> s = newMap.get(entry.getKey());
      if (s == null) {
        newMap.put(entry.getKey(), entry.getValue());
      } else {
        s.addAll(entry.getValue());
      }
    }
  }

  private Map<GraphEdge, Set<PrefixRange>> mergeFilters(
      Map<GraphEdge, Set<PrefixRange>> x, Map<GraphEdge, Set<PrefixRange>> y) {
    Map<GraphEdge, Set<PrefixRange>> newMap = new HashMap<>();
    addAll(newMap, x);
    addAll(newMap, y);
    return newMap;
  }

  private Map<GraphEdge, Set<PrefixRange>> processSlice(NetworkSlice slice) {
    Map<GraphEdge, Set<PrefixRange>> filters = new HashMap<>();
    for (GraphEdge edge : slice.getGraph().getAllEdges()) {
      if (!edge.isAbstract() && !edge.isNullEdge()) {
        Set<PrefixRange> ranges = new HashSet<>();
        for (IpWildcard wc : slice.getHeaderSpace().getDstIps()) {
          Prefix p = wc.toPrefix();
          PrefixRange r = new PrefixRange(p, new SubRange(p.getPrefixLength(), 32));
          ranges.add(r);
        }
        filters.put(edge, ranges);
      }
    }
    return filters;
  }

  private void applyFilters(@Nullable RoutingPolicy pol, Set<PrefixRange> filters) {
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
      PrefixSpace ps = new PrefixSpace(filters);
      ExplicitPrefixSet eps = new ExplicitPrefixSet(ps);
      MatchPrefixSet match = new MatchPrefixSet(new DestinationNetwork(), eps);
      i.setGuard(match);
    }
    i.setFalseStatements(falseStatements);
    i.setTrueStatements(pol.getStatements());
    newStatements.add(i);
    pol.setStatements(newStatements);
  }

  private void applyFilters(Table2<String, GraphEdge, Set<PrefixRange>> filtersByRouter) {
    for (Entry<String, Configuration> entry : _graph.getConfigurations().entrySet()) {
      String router = entry.getKey();
      Map<GraphEdge, Set<PrefixRange>> filters = filtersByRouter.get(router);
      for (GraphEdge ge : _graph.getEdgeMap().get(router)) {
        Set<PrefixRange> ranges = filters.get(ge);
        RoutingPolicy ipol = _graph.findImportRoutingPolicy(router, Protocol.BGP, ge);
        RoutingPolicy epol = _graph.findExportRoutingPolicy(router, Protocol.BGP, ge);
        applyFilters(ipol, ranges);
        applyFilters(epol, ranges);
      }
    }
  }

  public Map<String, Configuration> compress() {
    HeaderSpace h = new HeaderSpace();
    DestinationClasses dcs = DestinationClasses.create(_batfish, h, true);
    ArrayList<Supplier<NetworkSlice>> ecs = NetworkSlice.allSlices(dcs, 0);
    Map<GraphEdge, Set<PrefixRange>> filters =
        ecs.parallelStream().map(s -> processSlice(s.get())).reduce(this::mergeFilters).get();
    Table2<String, GraphEdge, Set<PrefixRange>> filtersByRouter = new Table2<>();
    for (Entry<GraphEdge,Set<PrefixRange>> entry : filters.entrySet()) {
      GraphEdge ge = entry.getKey();
      filtersByRouter.put(ge.getRouter(), ge, entry.getValue());
    }
    applyFilters(filtersByRouter);
    return _graph.getConfigurations();
  }
}
