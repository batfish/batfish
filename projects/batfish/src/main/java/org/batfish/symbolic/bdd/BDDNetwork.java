package org.batfish.symbolic.bdd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import net.sf.javabdd.BDD;
import org.batfish.common.Pair;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.TransferResult;
import org.batfish.symbolic.abstraction.InterfacePolicy;
import org.batfish.symbolic.bdd.BDDRouteFactory.BDDRoute;

public class BDDNetwork {

  private Graph _graph;

  private BDDRouteFactory _routeFactory;

  private NodesSpecifier _nodeSpecifier;

  private PolicyQuotient _policyQuotient;

  private Map<GraphEdge, BDDTransferFunction> _exportBgpPolicies;

  private Map<GraphEdge, BDDTransferFunction> _importBgpPolicies;

  private Map<GraphEdge, InterfacePolicy> _exportPolicyMap;

  private Map<GraphEdge, InterfacePolicy> _importPolicyMap;

  private Map<GraphEdge, BDDAcl> _inAcls;

  private Map<GraphEdge, BDDAcl> _outAcls;

  private BDDNetwork(
      Graph graph, NodesSpecifier nodesSpecifier, BDDRouteConfig config, PolicyQuotient pq) {
    _graph = graph;
    _nodeSpecifier = nodesSpecifier;
    _routeFactory = new BDDRouteFactory(graph, config);
    _policyQuotient = pq;
    _importPolicyMap = new HashMap<>();
    _exportPolicyMap = new HashMap<>();
    _importBgpPolicies = new HashMap<>();
    _exportBgpPolicies = new HashMap<>();
    _inAcls = new HashMap<>();
    _outAcls = new HashMap<>();
  }

  public static BDDNetwork create(Graph g, BDDRouteConfig config) {
    return create(g, NodesSpecifier.ALL, config);
  }

  public static BDDNetwork create(Graph g, NodesSpecifier nodesSpecifier, BDDRouteConfig config) {
    PolicyQuotient pq = new PolicyQuotient(g);
    BDDNetwork network = new BDDNetwork(g, nodesSpecifier, config, pq);
    network.computeInterfacePolicies();
    return network;
  }

  /*
   * Compute a BDD representation of a routing policy.
   */
  private BDDTransferFunction computeBDD(
      Graph g, Configuration conf, RoutingPolicy pol, boolean ignoreNetworks) {
    Set<Prefix> networks = null;
    if (ignoreNetworks) {
      networks = Graph.getOriginatedNetworks(conf);
    }
    TransferBuilder t = new TransferBuilder(g, conf, pol.getStatements(), _policyQuotient);
    TransferResult<BDDTransferFunction, BDD> result = t.compute(_routeFactory, networks);
    return result.getReturnValue();
  }

  /*
   * For each interface in the network, creates a canonical
   * representation of the import and export policies on this interface.
   */
  private void computeInterfacePolicies() {
    for (Entry<String, Configuration> entry : _graph.getConfigurations().entrySet()) {
      String router = entry.getKey();
      // Skip if doesn't match the node regex
      Matcher m = _nodeSpecifier.getRegex().matcher(router);
      if (!m.matches()) {
        continue;
      }
      Configuration conf = entry.getValue();
      List<GraphEdge> edges = _graph.getEdgeMap().get(router);
      for (GraphEdge ge : edges) {
        // Import BGP policy
        RoutingPolicy importBgp = _graph.findImportRoutingPolicy(router, Protocol.BGP, ge);
        if (importBgp != null) {
          BDDTransferFunction rec = computeBDD(_graph, conf, importBgp, true);
          _importBgpPolicies.put(ge, rec);
        }
        // Export BGP policy
        RoutingPolicy exportBgp = _graph.findExportRoutingPolicy(router, Protocol.BGP, ge);
        if (exportBgp != null) {
          BDDTransferFunction rec = computeBDD(_graph, conf, exportBgp, true);
          _exportBgpPolicies.put(ge, rec);
        }

        IpAccessList in = ge.getStart().getIncomingFilter();
        IpAccessList out = ge.getStart().getOutgoingFilter();

        // Incoming ACL
        if (in != null) {
          BDDAcl x = BDDAcl.create(in);
          _inAcls.put(ge, x);
        }
        // Outgoing ACL
        if (out != null) {
          BDDAcl x = BDDAcl.create(out);
          _outAcls.put(ge, x);
        }
      }
    }

    for (Entry<String, List<GraphEdge>> entry : _graph.getEdgeMap().entrySet()) {
      String router = entry.getKey();
      // Skip if doesn't match the node regex
      Matcher m = _nodeSpecifier.getRegex().matcher(router);
      if (!m.matches()) {
        continue;
      }
      List<GraphEdge> edges = entry.getValue();
      Configuration conf = _graph.getConfigurations().get(router);
      for (GraphEdge ge : edges) {
        BDDTransferFunction bgpIn = _importBgpPolicies.get(ge);
        BDDTransferFunction bgpOut = _exportBgpPolicies.get(ge);
        BDDAcl aclIn = _inAcls.get(ge);
        BDDAcl aclOut = _outAcls.get(ge);
        Integer ospfCost = ge.getStart().getOspfCost();
        SortedSet<Pair<Prefix, Integer>> staticPrefixes = new TreeSet<>();
        SortedSet<StaticRoute> staticRoutes = conf.getDefaultVrf().getStaticRoutes();
        for (StaticRoute sr : staticRoutes) {
          Prefix pfx = sr.getNetwork();
          Integer adminCost = sr.getAdministrativeCost();
          Pair<Prefix, Integer> tup = new Pair<>(pfx, adminCost);
          staticPrefixes.add(tup);
        }
        BDDRoute in = (bgpIn == null) ? null : bgpIn.getRoute();
        BDDRoute out = (bgpOut == null) ? null : bgpOut.getRoute();
        InterfacePolicy ipol = new InterfacePolicy(aclIn, in, null, staticPrefixes);
        InterfacePolicy epol = new InterfacePolicy(aclOut, out, ospfCost, null);
        _importPolicyMap.put(ge, ipol);
        _exportPolicyMap.put(ge, epol);
      }
    }
  }

  public Map<GraphEdge, BDDTransferFunction> getExportBgpPolicies() {
    return _exportBgpPolicies;
  }

  public Map<GraphEdge, BDDTransferFunction> getImportBgpPolicies() {
    return _importBgpPolicies;
  }

  public Map<GraphEdge, InterfacePolicy> getExportPolicyMap() {
    return _exportPolicyMap;
  }

  public Map<GraphEdge, InterfacePolicy> getImportPolicyMap() {
    return _importPolicyMap;
  }

  public Map<GraphEdge, BDDAcl> getInAcls() {
    return _inAcls;
  }

  public Map<GraphEdge, BDDAcl> getOutAcls() {
    return _outAcls;
  }
}
