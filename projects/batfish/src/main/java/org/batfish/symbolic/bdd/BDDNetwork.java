package org.batfish.symbolic.bdd;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.batfish.common.Pair;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.abstraction.InterfacePolicy;


public class BDDNetwork {

  private Graph _graph;

  private Pattern _nodeRegex;

  private Map<GraphEdge, BDDRoute> _exportBgpPolicies;

  private Map<GraphEdge, BDDRoute> _importBgpPolicies;

  private Map<GraphEdge, InterfacePolicy> _exportPolicyMap;

  private Map<GraphEdge, InterfacePolicy> _importPolicyMap;

  private Map<GraphEdge, BDDAcl> _inAcls;

  private Map<GraphEdge, BDDAcl> _outAcls;

  public static BDDNetwork create(Graph g) {
    return create(g, Pattern.compile(".*"));
  }

  public static BDDNetwork create(Graph g, Pattern nodeRegex) {
    long l = System.currentTimeMillis();
    BDDNetwork network = new BDDNetwork(g, nodeRegex);
    network.computeInterfacePolicies();
    System.out.println("Time to build BDD network: " + (System.currentTimeMillis() - l));
    return network;
  }


  private BDDNetwork(Graph graph, Pattern nodeRegex) {
    _graph = graph;
    _nodeRegex = nodeRegex;
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
    return t.compute(networks);
  }

  /*
   * For each interface in the network, creates a canonical
   * representation of the import and export policies on this interface.
   */
  private void computeInterfacePolicies() {

    long l1 = 0;
    long l2 = 0;
    long l3 = 0;
    long l;

    for (Entry<String, Configuration> entry : _graph.getConfigurations().entrySet()) {
      String router = entry.getKey();
      // Skip if doesn't match the node regex
      Matcher m = _nodeRegex.matcher(router);
      if (!m.matches()) {
        continue;
      }
      Configuration conf = entry.getValue();
      List<GraphEdge> edges = _graph.getEdgeMap().get(router);
      for (GraphEdge ge : edges) {
        // Import BGP policy
        RoutingPolicy importBgp = _graph.findImportRoutingPolicy(router, Protocol.BGP, ge);
        if (importBgp != null) {
          l = System.currentTimeMillis();
          BDDRoute rec = computeBDD(_graph, conf, importBgp, true);
          _importBgpPolicies.put(ge, rec);
          l1 = l1 + (System.currentTimeMillis() - l);
        }
        // Export BGP policy
        RoutingPolicy exportBgp = _graph.findExportRoutingPolicy(router, Protocol.BGP, ge);
        if (exportBgp != null) {
          l = System.currentTimeMillis();
          BDDRoute rec = computeBDD(_graph, conf, exportBgp, true);
          _exportBgpPolicies.put(ge, rec);
          l2 = l2 + (System.currentTimeMillis() - l);
        }

        IpAccessList in = ge.getStart().getIncomingFilter();
        IpAccessList out = ge.getStart().getOutgoingFilter();

        // Incoming ACL
        if (in != null) {
          BDDAcl x = BDDAcl.create(conf, in, true);
          _inAcls.put(ge, x);
        }
        // Outgoing ACL
        if (out != null) {
          BDDAcl x = BDDAcl.create(conf, out, true);
          _outAcls.put(ge, x);
        }
      }
    }

    l = System.currentTimeMillis();
    for (Entry<String, List<GraphEdge>> entry : _graph.getEdgeMap().entrySet()) {
      String router = entry.getKey();
      // Skip if doesn't match the node regex
      Matcher m = _nodeRegex.matcher(router);
      if (!m.matches()) {
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
        SortedSet<Pair<Prefix,Integer>> staticPrefixes = new TreeSet<>();
        SortedSet<StaticRoute> staticRoutes = conf.getDefaultVrf().getStaticRoutes();
        for (StaticRoute sr : staticRoutes) {
          Prefix pfx = sr.getNetwork().getNetworkPrefix();
          Integer adminCost = sr.getAdministrativeCost();
          Pair<Prefix,Integer> tup = new Pair<>(pfx, adminCost);
          staticPrefixes.add(tup);
        }
        InterfacePolicy ipol = new InterfacePolicy(aclIn, bgpIn, null, staticPrefixes);
        InterfacePolicy epol = new InterfacePolicy(aclOut, bgpOut, ospfCost, null);
        _importPolicyMap.put(ge, ipol);
        _exportPolicyMap.put(ge, epol);
      }
    }
    l3 = l3 + (System.currentTimeMillis() - l);

    System.out.println("Import BGP Time: " + l1);
    System.out.println("Export BGP Time: " + l2);
    System.out.println("Interface Pol Time: " + l3);
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
