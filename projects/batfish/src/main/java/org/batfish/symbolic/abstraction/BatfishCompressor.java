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

  private void addAll(Map<GraphEdge, PrefixSpace> newMap, Map<GraphEdge, PrefixSpace> oldMap) {
    for (Entry<GraphEdge, PrefixSpace> entry : oldMap.entrySet()) {
      PrefixSpace s = newMap.get(entry.getKey());
      if (s == null) {
        newMap.put(entry.getKey(), entry.getValue());
      } else {
        s.addSpace(entry.getValue());
      }
    }
  }

  private Map<GraphEdge, PrefixSpace> mergeFilters(
      Map<GraphEdge, PrefixSpace> x, Map<GraphEdge, PrefixSpace> y) {
    Map<GraphEdge, PrefixSpace> newMap = new HashMap<>();
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

  private Map<GraphEdge, PrefixSpace> processSlice(NetworkSlice slice) {
    Map<GraphEdge, PrefixSpace> filters = new HashMap<>();
    for (GraphEdge edge : slice.getGraph().getAllEdges()) {
      if (!edge.isAbstract() && edge.getPeer() != null) {
        PrefixSpace space = new PrefixSpace();
        for (IpWildcard wc : slice.getHeaderSpace().getDstIps()) {
          // TODO: properly cover the entire space
          Prefix p = wc.toPrefix();
          for (int i = 0; i < p.getPrefixLength(); i++) {
            Prefix pfx = new Prefix(p.getAddress(), i);
            PrefixRange r = new PrefixRange(makeCanonical(pfx), new SubRange(i, i));
            space.addPrefixRange(r);
          }
        }
        filters.put(edge, space);
      }
    }
    return filters;
  }

  private void applyFilters(@Nullable RoutingPolicy pol, @Nullable PrefixSpace filters) {
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
      ExplicitPrefixSet eps = new ExplicitPrefixSet(filters);
      MatchPrefixSet match = new MatchPrefixSet(new DestinationNetwork(), eps);
      i.setGuard(match);
    }
    i.setFalseStatements(falseStatements);
    i.setTrueStatements(pol.getStatements());
    newStatements.add(i);
    pol.setStatements(newStatements);
  }

  private Map<String, Configuration> applyFilters(
      Table2<String, GraphEdge, PrefixSpace> filtersByRouter) {
    Map<String, Configuration> newConfigs = new HashMap<>();
    for (Entry<String, Configuration> entry : _graph.getConfigurations().entrySet()) {
      String router = entry.getKey();
      Map<GraphEdge, PrefixSpace> filters = filtersByRouter.get(router);
      if (filters != null) {
        for (GraphEdge ge : _graph.getEdgeMap().get(router)) {
          PrefixSpace space = filters.get(ge);
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
    Map<GraphEdge, PrefixSpace> filters =
        ecs.parallelStream().map(s -> processSlice(s.get())).reduce(this::mergeFilters).get();
    Table2<String, GraphEdge, PrefixSpace> filtersByRouter = new Table2<>();
    for (Entry<GraphEdge, PrefixSpace> entry : filters.entrySet()) {
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
