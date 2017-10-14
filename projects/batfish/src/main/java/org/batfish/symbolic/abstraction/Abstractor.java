package org.batfish.symbolic.abstraction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.smt.EquivalenceType;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.answers.AbstractionAnswerElement;
import org.batfish.symbolic.answers.RoleAnswerElement;
import org.batfish.symbolic.utils.Tuple;

/**
 * Creates an abstraction(s) of the network by splitting the network into a collection of
 * equivalence classes and compressing the representation of each equivalence class. Each
 * equivalence class has the property that all of its stable solutions are bisimilar to the original
 * network. That is, there is a bug in the abstracted network iff there is a bug in the concrete
 * network.
 *
 * <p>How the compression occurs does depend on the property we want to check, since we can only
 * check properties for all concrete nodes that map to the same abstract node. For example, if we
 * want to check reachability between two concrete nodes, then these 2 nodes must remain distinct in
 * the compressed form.
 */

// TODO list
// - How does this interact with the following:
//    + iBGP
//    + Route Reflectors
//    + Router ID stuff
//    + Multipath routing
//    + MEDs
//    + Redistribution (Treat as local pref?)

public class Abstractor {

  // TODO: take a parameter indicating the set of devices that must remain concrete.

  private static final String EXTERNAL_NAME = "PEER";

  private IBatfish _batfish;

  private Graph _graph;

  private Map<GraphEdge, InterfacePolicy> _importPolicyMap;

  private Map<GraphEdge, InterfacePolicy> _exportPolicyMap;

  private Map<GraphEdge, BDDRecord> _importBgpPolicies;

  private Map<GraphEdge, BDDRecord> _exportBgpPolicies;

  private Map<GraphEdge, BDD> _inAcls;

  private Map<GraphEdge, BDDPacket> _inAclsPkts;

  private Map<GraphEdge, BDD> _outAcls;

  private Map<GraphEdge, BDDPacket> _outAclsPkts;

  public Abstractor(IBatfish batfish) {
    _batfish = batfish;
    _graph = new Graph(batfish);
    _importPolicyMap = new HashMap<>();
    _exportPolicyMap = new HashMap<>();
    _importBgpPolicies = new HashMap<>();
    _exportBgpPolicies = new HashMap<>();
    _inAcls = new HashMap<>();
    _outAcls = new HashMap<>();
    _inAclsPkts = new HashMap<>();
    _outAclsPkts = new HashMap<>();
  }

  /*
   * Compute a BDD representation of a routing policy.
   */
  public BDDRecord computeBDD(Graph g, Configuration conf, RoutingPolicy pol) {
    TransferBDD t = new TransferBDD(g, conf, pol.getStatements());
    BDDRecord rec = t.compute();
    return rec;
  }

  /*
   * Determine which protocols are running on which devices
   */
  private Map<String, List<Protocol>> buildProtocolMap() {
    // Figure out which protocols are running on which devices
    Map<String, List<Protocol>> protocols = new HashMap<>();
    _graph
        .getConfigurations()
        .forEach(
            (router, conf) -> {
              List<Protocol> protos = new ArrayList<>();
              protocols.put(router, protos);

              if (conf.getDefaultVrf().getOspfProcess() != null) {
                protos.add(Protocol.OSPF);
              }

              if (conf.getDefaultVrf().getBgpProcess() != null) {
                protos.add(Protocol.BGP);
              }

              if (!conf.getDefaultVrf().getStaticRoutes().isEmpty()) {
                protos.add(Protocol.STATIC);
              }

              if (!conf.getInterfaces().isEmpty()) {
                protos.add(Protocol.CONNECTED);
              }
            });
    return protocols;
  }

  /*
   * For each interface in the network, creates a canonical
   * representation of the import and export policies on this interface.
   */
  private void computeInterfacePolicies() {

    _graph
        .getConfigurations()
        .forEach(
            (router, conf) -> {
              List<GraphEdge> edges = _graph.getEdgeMap().get(router);
              for (GraphEdge ge : edges) {
                // Import BGP policy
                RoutingPolicy importBgp = _graph.findImportRoutingPolicy(router, Protocol.BGP, ge);
                if (importBgp != null) {
                  BDDRecord rec = computeBDD(_graph, conf, importBgp);
                  _importBgpPolicies.put(ge, rec);
                }
                // Export BGP policy
                RoutingPolicy exportBgp = _graph.findExportRoutingPolicy(router, Protocol.BGP, ge);
                if (exportBgp != null) {
                  BDDRecord rec = computeBDD(_graph, conf, exportBgp);
                  _exportBgpPolicies.put(ge, rec);
                }

                IpAccessList in = ge.getStart().getIncomingFilter();
                IpAccessList out = ge.getStart().getOutgoingFilter();
                // Incoming ACL
                if (in != null) {
                  AclBDD x = new AclBDD(in);
                  BDD acl = x.computeACL();
                  _inAcls.put(ge, acl);
                  _inAclsPkts.put(ge, x.getPkt());
                }
                // Outgoing ACL
                if (out != null) {
                  AclBDD x = new AclBDD(out);
                  BDD acl = x.computeACL();
                  _outAcls.put(ge, acl);
                  _outAclsPkts.put(ge, x.getPkt());
                }
              }
            });

    _graph
        .getEdgeMap()
        .forEach(
            (router, edges) -> {
              Configuration conf = _graph.getConfigurations().get(router);
              for (GraphEdge ge : edges) {
                BDDRecord bgpIn = _importBgpPolicies.get(ge);
                BDDRecord bgpOut = _exportBgpPolicies.get(ge);
                BDD aclIn = _inAcls.get(ge);
                BDD aclOut = _outAcls.get(ge);
                BDDPacket aclInPkt = _inAclsPkts.get(ge);
                BDDPacket aclOutPkt = _outAclsPkts.get(ge);
                Integer ospfCost = ge.getStart().getOspfCost();
                SortedSet<StaticRoute> staticRoutes = conf.getDefaultVrf().getStaticRoutes();
                InterfacePolicy ipol =
                    new InterfacePolicy(aclIn, aclInPkt, bgpIn, null, staticRoutes);
                InterfacePolicy epol =
                    new InterfacePolicy(aclOut, aclOutPkt, bgpOut, ospfCost, null);
                _importPolicyMap.put(ge, ipol);
                _exportPolicyMap.put(ge, epol);
              }
            });
  }

  /*
   * Helper functions to sort the sets by minimum element
   */
  private @Nullable String min(SortedSet<String> set) {
    String x = null;
    for (String s : set) {
      if (x == null || s.compareTo(x) < 0) {
        x = s;
      }
    }
    return x;
  }

  private Comparator<SortedSet<String>> comparator() {
    return (o1, o2) -> {
      String min1 = min(o1);
      String min2 = min(o2);
      return min1.compareTo(min2);
    };
  }

  /*
   * Compute all the devices/interfaces configured with the
   * equivalent policies.
   */
  public AnswerElement computeRoles(EquivalenceType t) {
    computeInterfacePolicies();
    Map<BDDRecord, SortedSet<String>> importBgpEcs = new HashMap<>();
    Map<BDDRecord, SortedSet<String>> exportBgpEcs = new HashMap<>();
    Map<BDD, SortedSet<String>> incomingAclEcs = new HashMap<>();
    Map<BDD, SortedSet<String>> outgoingAclEcs = new HashMap<>();
    Map<Tuple<InterfacePolicy, InterfacePolicy>, SortedSet<String>> interfaceEcs = new HashMap<>();
    Map<Set<Tuple<InterfacePolicy, InterfacePolicy>>, SortedSet<String>> nodeEcs = new HashMap<>();

    SortedSet<String> importBgpNull = new TreeSet<>();
    SortedSet<String> exportBgpNull = new TreeSet<>();
    SortedSet<String> incomingAclNull = new TreeSet<>();
    SortedSet<String> outgoingAclNull = new TreeSet<>();

    Prefix pfx = new Prefix("0.0.0.0/0");

    _graph
        .getEdgeMap()
        .forEach(
            (router, ges) -> {
              Set<Tuple<InterfacePolicy, InterfacePolicy>> nodeEc = new HashSet<>();

              for (GraphEdge ge : ges) {
                String s = ge.toString();

                if (t == EquivalenceType.POLICY) {
                  BDDRecord x1 = _importBgpPolicies.get(ge);
                  if (x1 == null) {
                    importBgpNull.add(s);
                  } else {
                    SortedSet<String> ec =
                        importBgpEcs.computeIfAbsent(x1.restrict(pfx), k -> new TreeSet<>());
                    ec.add(s);
                  }

                  BDDRecord x2 = _exportBgpPolicies.get(ge);
                  if (x2 == null) {
                    exportBgpNull.add(s);
                  } else {
                    SortedSet<String> ec =
                        exportBgpEcs.computeIfAbsent(x2.restrict(pfx), k -> new TreeSet<>());
                    ec.add(s);
                  }

                  BDD x4 = _inAcls.get(ge);
                  if (x4 == null) {
                    incomingAclNull.add(s);
                  } else {
                    SortedSet<String> ec = incomingAclEcs.computeIfAbsent(x4, k -> new TreeSet<>());
                    ec.add(s);
                  }

                  BDD x5 = _outAcls.get(ge);
                  if (x4 == null) {
                    outgoingAclNull.add(s);
                  } else {
                    SortedSet<String> ec = outgoingAclEcs.computeIfAbsent(x5, k -> new TreeSet<>());
                    ec.add(s);
                  }
                }

                InterfacePolicy x6 = _importPolicyMap.get(ge);
                InterfacePolicy x7 = _exportPolicyMap.get(ge);
                x6 = x6.restrict(pfx);
                x7 = x7.restrict(pfx);

                Tuple<InterfacePolicy, InterfacePolicy> tup = new Tuple<>(x6, x7);

                if (t == EquivalenceType.INTERFACE) {
                  SortedSet<String> ec = interfaceEcs.computeIfAbsent(tup, k -> new TreeSet<>());
                  ec.add(s);
                }

                if (t == EquivalenceType.NODE) {
                  nodeEc.add(tup);
                }
              }

              if (t == EquivalenceType.NODE) {
                SortedSet<String> ec = nodeEcs.computeIfAbsent(nodeEc, k -> new TreeSet<>());
                ec.add(router);
              }
            });

    List<SortedSet<String>> x1 = null;
    List<SortedSet<String>> x2 = null;
    List<SortedSet<String>> x4 = null;
    List<SortedSet<String>> x5 = null;
    List<SortedSet<String>> x6 = null;
    List<SortedSet<String>> x7 = null;

    Comparator<SortedSet<String>> c = comparator();

    if (t == EquivalenceType.POLICY) {
      x1 = new ArrayList<>(importBgpEcs.values());
      if (!importBgpNull.isEmpty()) {
        x1.add(importBgpNull);
      }
      x1.sort(c);
      x2 = new ArrayList<>(exportBgpEcs.values());
      if (!exportBgpNull.isEmpty()) {
        x2.add(exportBgpNull);
      }
      x2.sort(c);
      x4 = new ArrayList<>(incomingAclEcs.values());
      if (!incomingAclNull.isEmpty()) {
        x4.add(incomingAclNull);
      }
      x4.sort(c);
      x5 = new ArrayList<>(outgoingAclEcs.values());
      if (!outgoingAclNull.isEmpty()) {
        x5.add(outgoingAclNull);
      }
      x5.sort(c);
    }

    if (t == EquivalenceType.INTERFACE) {
      x6 = new ArrayList<>(interfaceEcs.values());
      x6.sort(c);
    }

    if (t == EquivalenceType.NODE) {
      x7 = new ArrayList<>(nodeEcs.values());
      x7.sort(c);
    }

    RoleAnswerElement ae = new RoleAnswerElement();
    ae.setImportBgpEcs(x1);
    ae.setExportBgpEcs(x2);
    ae.setIncomingAclEcs(x4);
    ae.setOutgoingAclEcs(x5);
    ae.setInterfaceEcs(x6);
    ae.setNodeEcs(x7);
    return ae;
  }

  public AnswerElement computeAbstraction() {
    long start = System.currentTimeMillis();
    computeInterfacePolicies();
    Map<String, List<Protocol>> protoMap = buildProtocolMap();

    // Create the trie
    PrefixTrieMap pt = new PrefixTrieMap();

    // Iterate through the destinations
    _graph
        .getConfigurations()
        .forEach(
            (router, conf) -> {
              // System.out.println("Looking at router: " + router);
              for (Protocol proto : protoMap.get(router)) {
                List<Prefix> destinations;
                // For connected interfaces add address if there is a peer
                // Otherwise, add the entire prefix since we don't know
                /* if (proto.isConnected()) {
                  destinations = new ArrayList<>();
                  List<GraphEdge> edges = g.getEdgeMap().get(router);
                  for (GraphEdge ge : edges) {
                    if (ge.getPeer() == null) {
                      destinations.add(ge.getStart().getPrefix());
                    } else {
                      Ip ip = ge.getStart().getPrefix().getAddress();
                      Prefix pfx = new Prefix(ip,32);
                      destinations.add(pfx);
                    }
                  }
                } else { */
                // System.out.println("  Looking at protocol: " + proto.name());
                destinations = Graph.getOriginatedNetworks(conf, proto);
                //}

                // Add all destinations to the prefix trie
                for (Prefix p : destinations) {
                  //System.out.println(
                  // "Destination for " + router + "," + proto.name() + " has: " + p);
                  pt.add(p, router);
                }
              }
            });

    // Map collections of devices to the destination IP ranges that are rooted there
    Map<Set<String>, List<Prefix>> destMap = pt.createDestinationMap();

    System.out.println("Destination Map:");
    destMap.forEach(
        (devices, prefixes) -> System.out.println("Devices: " + devices + " --> " + prefixes));

    // For each collection of destinations, we need to create an abstract network.
    // First we will figure out sets of nodes that can map together.
    Set<String> allDevices = _graph.getConfigurations().keySet();

    int min = -1;
    int max = 0;
    int totalAbstract = 0;
    int count = 0;
    double average = 0.0;

    for (Entry<Set<String>, List<Prefix>> entry : destMap.entrySet()) {
      Set<String> devices = entry.getKey();
      // List<Prefix> prefixes = entry.getValue();

      UnionSplit<String> workset = new UnionSplit<>(allDevices);

      // System.out.println("Workset: " + workset);

      // Split by the singleton set for each origination point
      for (String device : devices) {
        Set<String> ds = new TreeSet<>();
        ds.add(device);
        workset.split(ds);

        // Don't abstract neighbors
        //for (String neigh : g.getNeighbors().get(device)) {
        //  ds = new TreeSet<>();
        //  ds.add(neigh);
        //  workset.split(ds);
        //}
      }

      // System.out.println("Computing abstraction for: " + devices);

      // Repeatedly split the abstraction to a fixed point
      Set<Set<String>> todo;
      do {
        todo = new HashSet<>();
        Set<Set<String>> ps = workset.partitions();

        // System.out.println("Todo set: " + todo);
        // System.out.println("Workset: " + workset);

        for (Set<String> partition : ps) {
          // Create the map from interface policy to neighboring group

          // Nothing to refine if already a concrete node
          if (partition.size() <= 1) {
            continue;
          }

          Map<String, Set<Tuple<Integer, Tuple<InterfacePolicy, InterfacePolicy>>>> groupMap =
              new HashMap<>();

          for (String router : partition) {

            // System.out.println("  Looking at router: " + router);

            Set<Tuple<Integer, Tuple<InterfacePolicy, InterfacePolicy>>> groups = new HashSet<>();
            groupMap.put(router, groups);

            List<GraphEdge> edges = _graph.getEdgeMap().get(router);
            for (GraphEdge edge : edges) {
              if (!edge.isAbstract()) {
                String peer = edge.getPeer();

                Interface i = edge.getStart();
                InterfacePolicy ipol = _importPolicyMap.get(i);
                GraphEdge otherEnd = _graph.getOtherEnd().get(edge);
                InterfacePolicy epol = null;
                if (otherEnd != null) {
                  epol = _exportPolicyMap.get(otherEnd.getStart());
                }

                // For external neighbors, we don't split a partition
                Integer peerGroup;
                if (peer != null) {

                  Configuration peerConf = _graph.getConfigurations().get(peer);

                  peerGroup = workset.getHandle(peer);
                  // else {
                  // peerGroup = new TreeSet<>();
                  // peerGroup.add(EXTERNAL_NAME);

                  Tuple<InterfacePolicy, InterfacePolicy> pols = new Tuple<>(ipol, epol);
                  Tuple<Integer, Tuple<InterfacePolicy, InterfacePolicy>> pair =
                      new Tuple<>(peerGroup, pols);
                  groups.add(pair);

                  // System.out.println("    Group: " + pair.getKey() + "," + pair.getValue());
                }
              }
            }
          }

          Map<Set<Tuple<Integer, Tuple<InterfacePolicy, InterfacePolicy>>>, Set<String>>
              inversePolicyMap = new HashMap<>();
          groupMap.forEach(
              (router, groupPairs) -> {
                Set<String> routers =
                    inversePolicyMap.computeIfAbsent(groupPairs, gs -> new HashSet<>());
                routers.add(router);
              });

          // Only add changed to the list
          for (Set<String> collection : inversePolicyMap.values()) {
            if (!ps.contains(collection)) {
              todo.add(collection);
            }
          }

          // System.out.println("Todo now: " + todo);
        }

        // Now divide the abstraction further
        for (Set<String> partition : todo) {
          workset.split(partition);
        }

      } while (!todo.isEmpty());

      // Update the metrics
      int abstractSize = workset.partitions().size();
      min = (min < 0 ? abstractSize : Math.min(min, abstractSize));
      max = Math.max(max, abstractSize);
      totalAbstract = totalAbstract + abstractSize;
      count = count + 1;
      average = (double) totalAbstract / (double) count;

      // System.out.println("EC: " + prefixes);
      // System.out.println("Final abstraction: " + workset.partitions());
      // System.out.println("Original Size: " + allDevices.size());
      // System.out.println("Compressed: " + workset.partitions().size());
    }

    System.out.println("===============================================");
    System.out.println("Number of ECs: " + count);
    System.out.println("Concrete size: " + allDevices.size());
    System.out.println("Smallest abstraction: " + min);
    System.out.println("Largest abstraction: " + max);
    System.out.println("Average abstraction: " + average);
    System.out.println("===============================================");

    AbstractionAnswerElement answer = new AbstractionAnswerElement();
    answer.setAverage(average);
    answer.setMax(max);
    answer.setMin(min);
    answer.setNumClasses(count);
    answer.setOriginalSize(allDevices.size());

    long end = System.currentTimeMillis();

    System.out.println("Total time (sec): " + ((double) end - start) / 1000);

    return answer;
  }
}
