package org.batfish.symbolic.abstraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javafx.util.Pair;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.answers.AbstractionAnswerElement;

/**
 * <p>Creates an abstraction(s) of the network by splitting the network into a collection of
 * equivalence classes and compressing the representation of each equivalence class. Each
 * equivalence class has the property that all of its stable solutions are bisimilar to the original
 * network. That is, there is a bug in the abstracted network iff there is a bug in the concrete
 * network.</p>
 *
 * <p>How the compression occurs does depend on the property we want to check, since we can only
 * check properties for all concrete nodes that map to the same abstract node. For example, if we
 * want to check reachability between two concrete nodes, then these 2 nodes must remain distinct in
 * the compressed form.</p>
 */

// TODO list
// - Keep ACLs and static routes as separate BDDs?
// - How does this interact with the following:
//    + iBGP
//    + Route Reflectors
//    + Router ID stuff
//    + Multipath routing
//    + MEDs
//    + Redistribution (Treat as local pref?)

public class Abstractor {

  // TODO: take a parameter indicating the set of devices that must remain concrete.

  private static final int DEFAULT_CISCO_VLAN_OSPF_COST = 1;

  private static final String EXTERNAL_NAME = "PEER";


  /** TODO: This was copied from BdpDataPlanePlugin.java to initialize the OSPF inteface costs */
  private static void initOspfInterfaceCosts(Configuration conf) {
    if (conf.getDefaultVrf().getOspfProcess() != null) {
      conf.getInterfaces()
          .forEach(
              (interfaceName, i) -> {
                if (i.getActive()) {
                  Integer ospfCost = i.getOspfCost();
                  if (ospfCost == null) {
                    if (interfaceName.startsWith("Vlan")) {
                      // TODO: fix for non-cisco
                      ospfCost = DEFAULT_CISCO_VLAN_OSPF_COST;
                    } else {
                      if (i.getBandwidth() != null) {
                        ospfCost =
                            Math.max(
                                (int)
                                    (conf.getDefaultVrf().getOspfProcess().getReferenceBandwidth()
                                        / i.getBandwidth()),
                                1);
                      } else {
                        throw new BatfishException(
                            "Expected non-null interface "
                                + "bandwidth"
                                + " for \""
                                + conf.getHostname()
                                + "\":\""
                                + interfaceName
                                + "\"");
                      }
                    }
                  }
                  i.setOspfCost(ospfCost);
                }
              });
    }
  }

  public static BDDRecord computeBDD(Graph g, Configuration conf, RoutingPolicy pol) {
    TransferBDD t = new TransferBDD(g, conf, pol.getStatements());
    BDDRecord rec = t.compute();
    rec.getCommunities().forEach((cvar, bdd) -> {
      //System.out.println("CVAR: " + cvar.getValue() + ", " + cvar.getType());
      //System.out.println("DOT: \n" + rec.getDot(bdd));
    });

    // BDD bdd = rec.getMetric().getBitvec()[31];
    // System.out.println("DOT: \n" + rec.getDot(bdd));
    return rec;
  }


  public static AnswerElement computeAbstraction(IBatfish batfish) {

    long start = System.currentTimeMillis();

    Graph g = new Graph(batfish);

    g.getConfigurations().forEach((router, conf) -> {
      initOspfInterfaceCosts(conf);
    });

    // Figure out which protocols are running on which devices
    Map<String, List<Protocol>> protocols = new HashMap<>();
    g.getConfigurations()
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

    // Compute BDD policies
    Map<GraphEdge, BDDRecord> importPolicies = new HashMap<>();
    Map<GraphEdge, BDDRecord> exportPolicies = new HashMap<>();

    Map<GraphEdge, BDD> inAcls = new HashMap<>();
    Map<GraphEdge, BDD> outAcls = new HashMap<>();

    Map<BDDRecord, Set<GraphEdge>> importPolMap = new HashMap<>();
    Map<BDDRecord, Set<GraphEdge>> exportPolMap = new HashMap<>();

    Map<BDD, Set<GraphEdge>> inAclMap = new HashMap<>();
    Map<BDD, Set<GraphEdge>> outAclMap = new HashMap<>();


    g.getConfigurations()
        .forEach(
            (router, conf) -> {
              List<GraphEdge> edges = g.getEdgeMap().get(router);
              for (GraphEdge ge : edges) {
                if (ge.getEnd() == null) {
                  // Import policy
                  RoutingPolicy importPol = g.findImportRoutingPolicy(router, Protocol.BGP, ge);
                  if (importPol != null) {
                    System.out.println("Looking at: " + ge);
                    BDDRecord rec = computeBDD(g, conf, importPol);
                    importPolicies.put(ge, rec);
                    Set<GraphEdge> ifaces = importPolMap.computeIfAbsent(rec, k -> new HashSet<>());
                    ifaces.add(ge);
                  }
                  // Export policy
                  RoutingPolicy exportPol = g.findExportRoutingPolicy(router, Protocol.BGP, ge);
                  if (exportPol != null) {
                    System.out.println("Looking at: " + ge);
                    BDDRecord rec = computeBDD(g, conf, exportPol);
                    exportPolicies.put(ge, rec);
                    Set<GraphEdge> ifaces = exportPolMap.computeIfAbsent(rec, k -> new HashSet<>());
                    ifaces.add(ge);
                  }
                  IpAccessList in = ge.getStart().getIncomingFilter();
                  IpAccessList out = ge.getStart().getOutgoingFilter();
                  // Incoming ACL
                  if (in != null) {
                    System.out.println("IN ACL");
                    AclBDD x = new AclBDD(in);
                    BDD acl = x.computeACL();
                    inAcls.put(ge, acl);
                    Set<GraphEdge> ifaces = inAclMap.computeIfAbsent(acl, k -> new HashSet<>());
                    ifaces.add(ge);
                  }
                  // Outgoing ACL
                  if (out != null) {
                    System.out.println("OUT ACL");
                    AclBDD x = new AclBDD(out);
                    BDD acl = x.computeACL();
                    outAcls.put(ge, acl);
                    Set<GraphEdge> ifaces = outAclMap.computeIfAbsent(acl, k -> new HashSet<>());
                    ifaces.add(ge);
                  }
                }
              }
            });

    // Print equivalence policies
    importPolMap.forEach(
        (rec, ifaces) -> {
          System.out.println("IMPORT EC: " + ifaces);
        });

    exportPolMap.forEach(
        (rec, ifaces) -> {
          System.out.println("EXPORT EC: " + ifaces);
        });

    inAclMap.forEach(
        (bdd, ifaces) -> {
          System.out.println("IN ACL EC: " + ifaces);
        });

    outAclMap.forEach(
        (bdd, ifaces) -> {
          System.out.println("OUT ACL EC: " + ifaces);
        });

    // Create the trie
    PrefixTrieMap pt = new PrefixTrieMap();

    // Iterate through the destinations
    g.getConfigurations()
        .forEach(
            (router, conf) -> {
              // System.out.println("Looking at router: " + router);
              for (Protocol proto : protocols.get(router)) {
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
                  //    "Destination for " + router + "," + proto.name() + " has: " + p);
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
    Set<String> allDevices = g.getConfigurations().keySet();

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

          Map<String, Set<Pair<Integer, InterfacePolicy>>> groupMap = new HashMap<>();

          for (String router : partition) {

            // System.out.println("  Looking at router: " + router);

            Set<Pair<Integer, InterfacePolicy>> groups = new HashSet<>();
            groupMap.put(router, groups);

            // TODO: convert ACLS

            List<GraphEdge> edges = g.getEdgeMap().get(router);
            for (GraphEdge edge : edges) {
              if (!edge.isAbstract()) {
                String peer = edge.getPeer();
                GraphEdge otherEnd = g.getOtherEnd().get(edge);
                BDDRecord importPol = importPolicies.get(edge);
                BDDRecord exportPol = null;
                if (otherEnd != null) {
                  exportPol = exportPolicies.get(otherEnd);
                }
                Integer ospfCost = edge.getStart().getOspfCost();
                InterfacePolicy pol = new InterfacePolicy(ospfCost, importPol, exportPol);

                // For external neighbors, we don't split a partition
                Integer peerGroup;
                if (peer != null) {

                  Configuration peerConf = g.getConfigurations().get(peer);

                  peerGroup = workset.getHandle(peer);
                  // else {
                  // peerGroup = new TreeSet<>();
                  // peerGroup.add(EXTERNAL_NAME);

                  Pair<Integer, InterfacePolicy> pair = new Pair<>(peerGroup, pol);
                  groups.add(pair);

                  // System.out.println("    Group: " + pair.getKey() + "," + pair.getValue());
                }
              }
            }
          }

          Map<Set<Pair<Integer, InterfacePolicy>>, Set<String>> inversePolicyMap =
              new HashMap<>();
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
