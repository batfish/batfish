package org.batfish.minesweeper.bdd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.minesweeper.Graph;
import org.batfish.minesweeper.GraphEdge;
import org.batfish.minesweeper.Protocol;
import org.batfish.minesweeper.abstraction.InterfacePolicy;
import org.batfish.minesweeper.utils.MsPair;

public class BDDNetwork {

  private final BDDPacket _pkt;
  private final NetworkSnapshot _snapshot;
  private Graph _graph;
  private NodesSpecifier _nodeSpecifier;

  private Map<GraphEdge, BDDRoute> _exportBgpPolicies;

  private Map<GraphEdge, BDDRoute> _importBgpPolicies;

  private Map<GraphEdge, InterfacePolicy> _exportPolicyMap;

  private Map<GraphEdge, InterfacePolicy> _importPolicyMap;

  private Map<GraphEdge, BDDAcl> _inAcls;

  private Map<GraphEdge, BDDAcl> _outAcls;

  public static BDDNetwork create(NetworkSnapshot snapshot, BDDPacket pkt, Graph g) {
    return create(snapshot, pkt, g, NodesSpecifier.ALL);
  }

  public static BDDNetwork create(
      NetworkSnapshot snapshot, BDDPacket pkt, Graph g, NodesSpecifier nodesSpecifier) {
    BDDNetwork network = new BDDNetwork(snapshot, pkt, g, nodesSpecifier);
    network.computeInterfacePolicies();
    return network;
  }

  private BDDNetwork(
      NetworkSnapshot snapshot, BDDPacket pkt, Graph graph, NodesSpecifier nodesSpecifier) {
    _graph = graph;
    _nodeSpecifier = nodesSpecifier;
    _snapshot = snapshot;
    _pkt = pkt;
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
  private BDDRoute computeBDD(
      Graph g, Configuration conf, RoutingPolicy pol, boolean ignoreNetworks) {
    Set<Prefix> networks = null;
    if (ignoreNetworks) {
      networks = Graph.getOriginatedNetworks(conf);
    }
    TransferBDD t = new TransferBDD(g, conf, pol.getStatements());
    return t.compute(networks).getReturnValue().getFirst();
  }

  /*
   * For each interface in the network, creates a canonical
   * representation of the import and export policies on this interface.
   */
  private void computeInterfacePolicies() {
    IBatfish batfish = _graph.getBatfish();
    Set<String> includeNodes = _nodeSpecifier.getMatchingNodes(batfish, _snapshot);
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
          BDDRoute rec = computeBDD(_graph, conf, importBgp, true);
          _importBgpPolicies.put(ge, rec);
        }
        // Export BGP policy
        RoutingPolicy exportBgp = _graph.findExportRoutingPolicy(router, Protocol.BGP, ge);
        if (exportBgp != null) {
          BDDRoute rec = computeBDD(_graph, conf, exportBgp, true);
          _exportBgpPolicies.put(ge, rec);
        }

        IpAccessList in = ge.getStart().getIncomingFilter();
        IpAccessList out = ge.getStart().getOutgoingFilter();

        // Incoming ACL
        if (in != null) {
          BDDAcl x = BDDAcl.create(_pkt, in, conf.getIpAccessLists(), conf.getIpSpaces());
          _inAcls.put(ge, x);
        }
        // Outgoing ACL
        if (out != null) {
          BDDAcl x = BDDAcl.create(_pkt, out, conf.getIpAccessLists(), conf.getIpSpaces());
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
        BDDRoute bgpIn = _importBgpPolicies.get(ge);
        BDDRoute bgpOut = _exportBgpPolicies.get(ge);
        BDDAcl aclIn = _inAcls.get(ge);
        BDDAcl aclOut = _outAcls.get(ge);
        Integer ospfCost = ge.getStart().getOspfCost();
        SortedSet<MsPair<Prefix, Integer>> staticPrefixes = new TreeSet<>();
        SortedSet<StaticRoute> staticRoutes = conf.getDefaultVrf().getStaticRoutes();
        for (StaticRoute sr : staticRoutes) {
          Prefix pfx = sr.getNetwork();
          Integer adminCost = sr.getAdministrativeCost();
          MsPair<Prefix, Integer> tup = new MsPair<>(pfx, adminCost);
          staticPrefixes.add(tup);
        }
        InterfacePolicy ipol = new InterfacePolicy(aclIn, bgpIn, null, staticPrefixes);
        InterfacePolicy epol = new InterfacePolicy(aclOut, bgpOut, ospfCost, null);
        _importPolicyMap.put(ge, ipol);
        _exportPolicyMap.put(ge, epol);
      }
    }
  }

  public Map<GraphEdge, BDDRoute> getExportBgpPolicies() {
    return _exportBgpPolicies;
  }

  public Map<GraphEdge, BDDRoute> getImportBgpPolicies() {
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
