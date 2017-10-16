package org.batfish.symbolic.abstraction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;

public class BDDNetwork {

  private Graph _graph;

  private Map<GraphEdge, BDDRecord> _exportBgpPolicies;

  private Map<GraphEdge, BDDRecord> _importBgpPolicies;

  private Map<GraphEdge, InterfacePolicy> _exportPolicyMap;

  private Map<GraphEdge, InterfacePolicy> _importPolicyMap;

  private Map<GraphEdge, AclBDD> _inAcls;

  private Map<GraphEdge, AclBDD> _outAcls;

  public static BDDNetwork create(Graph g) {
    BDDNetwork network = new BDDNetwork(g);
    network.computeInterfacePolicies();
    return network;
  }


  private BDDNetwork(Graph graph) {
    _graph = graph;
    _importPolicyMap = new HashMap<>();
    _exportPolicyMap = new HashMap<>();
    _importBgpPolicies = new HashMap<>();
    _exportBgpPolicies = new HashMap<>();
    _inAcls = new HashMap<>();
    _outAcls = new HashMap<>();
  }

  /*
   * Compute a BDD representation of a routing policy.
   */
  private BDDRecord computeBDD(
      Graph g, Configuration conf, RoutingPolicy pol, boolean ignoreNetworks) {
    TransferBDD t = new TransferBDD(g, conf, pol.getStatements());
    return t.compute(ignoreNetworks);
  }

  /*
   * For each interface in the network, creates a canonical
   * representation of the import and export policies on this interface.
   */
  private void computeInterfacePolicies() {

    for (Entry<String, Configuration> entry : _graph.getConfigurations().entrySet()) {
      String router = entry.getKey();
      Configuration conf = entry.getValue();
      List<GraphEdge> edges = _graph.getEdgeMap().get(router);
      for (GraphEdge ge : edges) {
        // Import BGP policy
        RoutingPolicy importBgp = _graph.findImportRoutingPolicy(router, Protocol.BGP, ge);
        if (importBgp != null) {
          BDDRecord rec = computeBDD(_graph, conf, importBgp, true);
          _importBgpPolicies.put(ge, rec);
        }
        // Export BGP policy
        RoutingPolicy exportBgp = _graph.findExportRoutingPolicy(router, Protocol.BGP, ge);
        if (exportBgp != null) {
          BDDRecord rec = computeBDD(_graph, conf, exportBgp, true);
          _exportBgpPolicies.put(ge, rec);
        }

        IpAccessList in = ge.getStart().getIncomingFilter();
        IpAccessList out = ge.getStart().getOutgoingFilter();
        // Incoming ACL
        if (in != null) {
          AclBDD x = AclBDD.create(in);
          _inAcls.put(ge, x);
        }
        // Outgoing ACL
        if (out != null) {
          AclBDD x = AclBDD.create(out);
          _outAcls.put(ge, x);
        }
      }
    }

    for (Entry<String, List<GraphEdge>> entry : _graph.getEdgeMap().entrySet()) {
      String router = entry.getKey();
      List<GraphEdge> edges = entry.getValue();
      Configuration conf = _graph.getConfigurations().get(router);
      for (GraphEdge ge : edges) {
        BDDRecord bgpIn = _importBgpPolicies.get(ge);
        BDDRecord bgpOut = _exportBgpPolicies.get(ge);
        AclBDD aclIn = _inAcls.get(ge);
        AclBDD aclOut = _outAcls.get(ge);
        Integer ospfCost = ge.getStart().getOspfCost();
        SortedSet<StaticRoute> staticRoutes = conf.getDefaultVrf().getStaticRoutes();
        InterfacePolicy ipol = new InterfacePolicy(aclIn, bgpIn, null, staticRoutes);
        InterfacePolicy epol = new InterfacePolicy(aclOut, bgpOut, ospfCost, null);
        _importPolicyMap.put(ge, ipol);
        _exportPolicyMap.put(ge, epol);
      }
    }
  }

  public Map<GraphEdge, BDDRecord> getExportBgpPolicies() {
    return _exportBgpPolicies;
  }

  public Map<GraphEdge, BDDRecord> getImportBgpPolicies() {
    return _importBgpPolicies;
  }

  public Map<GraphEdge, InterfacePolicy> getExportPolicyMap() {
    return _exportPolicyMap;
  }

  public Map<GraphEdge, InterfacePolicy> getImportPolicyMap() {
    return _importPolicyMap;
  }

  public Map<GraphEdge, AclBDD> getInAcls() {
    return _inAcls;
  }

  public Map<GraphEdge, AclBDD> getOutAcls() {
    return _outAcls;
  }
}
