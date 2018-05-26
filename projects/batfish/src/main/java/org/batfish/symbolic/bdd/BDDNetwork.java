package org.batfish.symbolic.bdd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
import org.batfish.symbolic.collections.Table2;

public class BDDNetwork {

  private Table2<String, String, TransferResult<BDDTransferFunction, BDD>> _transferCache;

  private Graph _graph;

  private BDDNetFactory _netFactory;

  private NodesSpecifier _nodeSpecifier;

  private boolean _ignoreNetworks;

  private PolicyQuotient _policyQuotient;

  private Map<GraphEdge, BDDTransferFunction> _exportBgpPolicies;

  private Map<GraphEdge, BDDTransferFunction> _importBgpPolicies;

  private Map<GraphEdge, BDDTransferFunction> _exportOspfPolicies;

  private Map<GraphEdge, BDDTransferFunction> _importOspfPolicies;

  private Map<GraphEdge, InterfacePolicy> _exportPolicyMap;

  private Map<GraphEdge, InterfacePolicy> _importPolicyMap;

  private Map<GraphEdge, BDDAcl> _inAcls;

  private Map<GraphEdge, BDDAcl> _outAcls;

  private BDDNetwork(
      Graph graph,
      NodesSpecifier nodesSpecifier,
      BDDNetConfig config,
      PolicyQuotient pq,
      boolean ignoreNetworks) {
    _transferCache = new Table2<>();
    _graph = graph;
    _nodeSpecifier = nodesSpecifier;
    _ignoreNetworks = ignoreNetworks;
    _netFactory = new BDDNetFactory(graph, config);
    _policyQuotient = pq;
    _importPolicyMap = new HashMap<>();
    _exportPolicyMap = new HashMap<>();
    _importBgpPolicies = new HashMap<>();
    _exportBgpPolicies = new HashMap<>();
    _importOspfPolicies = new HashMap<>();
    _exportOspfPolicies = new HashMap<>();
    _inAcls = new HashMap<>();
    _outAcls = new HashMap<>();
  }

  public static BDDNetwork create(Graph g, BDDNetConfig config, boolean ignoreNetworks) {
    return create(g, NodesSpecifier.ALL, config, ignoreNetworks);
  }

  public static BDDNetwork create(
      Graph g, NodesSpecifier nodesSpecifier, BDDNetConfig config, boolean ignoreNetworks) {
    PolicyQuotient pq = new PolicyQuotient(g);
    BDDNetwork network = new BDDNetwork(g, nodesSpecifier, config, pq, ignoreNetworks);
    network.computeInterfacePolicies();
    return network;
  }

  /*
   * Compute a BDD representation of a routing policy.
   */
  private BDDTransferFunction computeBDD(Configuration conf, RoutingPolicy pol) {
    Set<Prefix> networks = new HashSet<>();
    if (_ignoreNetworks) {
      networks = Graph.getOriginatedNetworks(conf);
    }
    TransferBuilder t =
        new TransferBuilder(this, _graph, conf, pol.getStatements(), _policyQuotient);
    TransferResult<BDDTransferFunction, BDD> result = t.compute(networks);
    return result.getReturnValue();
  }

  /*
   * For each interface in the network, creates a canonical
   * representation of the import and export policies on this interface.
   */
  private void computeInterfacePolicies() {
    Set<String> includeNodes = _nodeSpecifier.getMatchingNodes(_graph.getBatfish());
    for (Entry<String, Configuration> entry : _graph.getConfigurations().entrySet()) {
      String router = entry.getKey();
      if (!includeNodes.contains(router)) { // skip if we don't care about this node
        continue;
      }
      Configuration conf = entry.getValue();
      List<GraphEdge> edges = _graph.getEdgeMap().get(router);
      for (GraphEdge ge : edges) {

        // Import BGP policy
        RoutingPolicy importBgp = _graph.findImportRoutingPolicy(router, Protocol.BGP, ge);
        if (importBgp != null) {
          BDDTransferFunction rec = computeBDD(conf, importBgp);
          _importBgpPolicies.put(ge, rec);
        }
        // Export BGP policy
        RoutingPolicy exportBgp = _graph.findExportRoutingPolicy(router, Protocol.BGP, ge);
        if (exportBgp != null) {
          BDDTransferFunction rec = computeBDD(conf, exportBgp);
          _exportBgpPolicies.put(ge, rec);
        }
        // Import OSPF policy
        RoutingPolicy importOspf = _graph.findImportRoutingPolicy(router, Protocol.OSPF, ge);
        if (importOspf != null) {
          BDDTransferFunction rec = computeBDD(conf, importOspf);
          _importOspfPolicies.put(ge, rec);
        }
        // Export OSPF policy
        RoutingPolicy exportOspf = _graph.findExportRoutingPolicy(router, Protocol.OSPF, ge);
        if (exportOspf != null) {
          BDDTransferFunction rec = computeBDD(conf, exportOspf);
          _exportOspfPolicies.put(ge, rec);
        }

        IpAccessList in = ge.getStart().getIncomingFilter();
        IpAccessList out = ge.getStart().getOutgoingFilter();

        // Incoming ACL
        if (in != null) {
          BDDAcl x = BDDAcl.create(_netFactory, in);
          _inAcls.put(ge, x);
        }
        // Outgoing ACL
        if (out != null) {
          BDDAcl x = BDDAcl.create(_netFactory, out);
          _outAcls.put(ge, x);
        }
      }
    }

    for (Entry<String, List<GraphEdge>> entry : _graph.getEdgeMap().entrySet()) {
      String router = entry.getKey();
      if (!includeNodes.contains(router)) { // skip if we don't care about this node
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

  public Map<GraphEdge, BDDTransferFunction> getExportOspfPolicies() {
    return _exportOspfPolicies;
  }

  public Map<GraphEdge, BDDTransferFunction> getImportOspfPolicies() {
    return _importOspfPolicies;
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

  public BDDNetFactory getNetFactory() {
    return _netFactory;
  }

  public Table2<String, String, TransferResult<BDDTransferFunction, BDD>> getTransferCache() {
    return _transferCache;
  }
}
