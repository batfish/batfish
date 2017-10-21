package org.batfish.symbolic.abstraction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.common.Pair;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.OspfNeighbor;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.answers.AbstractionAnswerElement;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.collections.Table2;
import org.batfish.symbolic.utils.PrefixUtils;
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

// - iBGP check source ACL?
// - add parent / client RRs?
// - Always assume multipath?

public class Abstraction implements Iterable<EquivalenceClass> {

  private IBatfish _batfish;

  private Graph _graph;

  private BDDNetwork _network;

  private HeaderSpace _headerspace;

  private int _possibleFailures;

  private final Object _lock;

  private Map<Set<String>, List<Prefix>> _destinationMap;

  private Abstraction(IBatfish batfish, @Nullable HeaderSpace h, int fails) {
    _batfish = batfish;
    _graph = new Graph(batfish);
    _network = BDDNetwork.create(_graph);
    _destinationMap = new HashMap<>();
    _headerspace = h;
    _possibleFailures = fails;
    _lock = new Object();
  }

  public static Abstraction create(IBatfish batfish, @Nullable HeaderSpace h, int fails) {
    Abstraction abs = new Abstraction(batfish, h, fails);
    abs.initDestinationMap();
    return abs;
  }

  public static Abstraction create(IBatfish batfish, int fails) {
    Abstraction abs = new Abstraction(batfish, null, fails);
    abs.initDestinationMap();
    return abs;
  }

  /*
   * Initialize a map from sets of nodes that represent possible destinations,
   * to a set of prefixes that represent the collection of destination
   * IP addresses for which those nodes might be physical destinations
   */
  private void initDestinationMap() {
    Map<String, List<Protocol>> protoMap = buildProtocolMap();

    // Convert headerspace to list of prefixes
    List<Prefix> dstIps = new ArrayList<>();
    List<Prefix> notDstIps = new ArrayList<>();
    if (_headerspace == null || _headerspace.getDstIps().isEmpty()) {
      dstIps.add(new Prefix("0.0.0.0/0"));
    } else {
      for (IpWildcard ip : _headerspace.getDstIps()) {
        if (!ip.isPrefix()) {
          throw new BatfishException("Unimplemented: IpWildcard that is not prefix: " + ip);
        }
        dstIps.add(ip.toPrefix());
      }
      for (IpWildcard ip : _headerspace.getNotDstIps()) {
        if (!ip.isPrefix()) {
          throw new BatfishException("Unimplemented: IpWildcard that is not prefix: " + ip);
        }
        notDstIps.add(ip.toPrefix());
      }
    }

    PrefixTrieMap pt = new PrefixTrieMap();

    for (Entry<String, Configuration> entry : _graph.getConfigurations().entrySet()) {
      String router = entry.getKey();
      Configuration conf = entry.getValue();
      // System.out.println("Looking at router: " + router);
      for (Protocol proto : protoMap.get(router)) {
        Set<Prefix> destinations = new HashSet<>();
        if (!proto.isStatic()) {
          destinations = Graph.getOriginatedNetworks(conf, proto);
        }
        // Add all destinations to the prefix trie relevant to this slice
        for (Prefix p : destinations) {
          if (_headerspace == null
              || (PrefixUtils.overlap(p, dstIps) && !PrefixUtils.overlap(p, notDstIps))) {
            pt.add(p, router);
          }
        }
      }
    }

    // Map collections of devices to the destination IP ranges that are rooted there
    _destinationMap = pt.createDestinationMap();
    _destinationMap.forEach(
        (devices, prefixes) -> System.out.println("Check for: " + devices + " --> " + prefixes));
  }

  /*
   * Initialize a mapping from router to collection of protocol
   */
  private Map<String, List<Protocol>> buildProtocolMap() {
    // Figure out which protocols are running on which devices
    Map<String, List<Protocol>> protocols = new HashMap<>();
    for (Entry<String, Configuration> entry : _graph.getConfigurations().entrySet()) {
      String router = entry.getKey();
      Configuration conf = entry.getValue();
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
    }
    return protocols;
  }


  public ArrayList<Supplier<EquivalenceClass>> equivalenceClasses() {
    ArrayList<Supplier<EquivalenceClass>> classes = new ArrayList<>();
    for (Entry<Set<String>, List<Prefix>> entry : _destinationMap.entrySet()) {
      Set<String> devices = entry.getKey();
      List<Prefix> prefixes = entry.getValue();
      Supplier<EquivalenceClass> sup = () -> computeAbstraction(devices, prefixes);
      classes.add(sup);
    }
    return classes;
  }

  @Nonnull
  @Override
  public Iterator<EquivalenceClass> iterator() {
    return new AbstractionIterator();
  }

  private HeaderSpace createHeaderSpace(List<Prefix> prefixes) {
    HeaderSpace h = new HeaderSpace();
    SortedSet<IpWildcard> ips = new TreeSet<>();
    for (Prefix pfx : prefixes) {
      IpWildcard ip = new IpWildcard(pfx);
      ips.add(ip);
    }
    h.setDstIps(ips);
    return h;
  }

  private EquivalenceClass computeAbstraction(Set<String> devices, List<Prefix> prefixes) {
    System.out.println("EC Devices: " + devices);
    System.out.println("EC Prefixes: " + prefixes);

    Set<String> allDevices = _graph.getConfigurations().keySet();

    Map<GraphEdge, InterfacePolicy> exportPol = new HashMap<>();;
    Map<GraphEdge, InterfacePolicy> importPol = new HashMap<>();

    // Specialize the BDDs for this EC. However, BDDs are not thread safe
    synchronized (_lock) {
      for (Entry<GraphEdge, InterfacePolicy> entry : _network.getExportPolicyMap().entrySet()) {
        GraphEdge ge = entry.getKey();
        InterfacePolicy pol = entry.getValue();
        exportPol.put(ge, pol.restrict(prefixes));
      }
      for (Entry<GraphEdge, InterfacePolicy> entry : _network.getImportPolicyMap().entrySet()) {
        GraphEdge ge = entry.getKey();
        InterfacePolicy pol = entry.getValue();
        importPol.put(ge, pol.restrict(prefixes));
      }
    }

    UnionSplit<String> workset = new UnionSplit<>(allDevices);

    // Split by the singleton set for each origination point
    for (String device : devices) {
      Set<String> ds = new TreeSet<>();
      ds.add(device);
      workset.split(ds);
    }

    // Repeatedly split the abstraction to a fixed point
    Set<Set<String>> todo;
    do {
      todo = new HashSet<>();
      List<Set<String>> ps = workset.partitions();

      // System.out.println("Todo set: " + todo);
      // System.out.println("Workset: " + workset);

      for (Set<String> partition : ps) {

        // Nothing to refine if already a concrete node
        if (partition.size() <= 1) {
          continue;
        }

        if (needUniversalAbstraction(partition)) {

          // Split by universal abstraction
          Map<String, Set<Tuple<String, EquivalenceEdge>>> universalMap = new HashMap<>();
          for (String router : partition) {
            List<GraphEdge> edges = _graph.getEdgeMap().get(router);
            for (GraphEdge edge : edges) {
              if (!edge.isAbstract()) {
                String peer = edge.getPeer();
                InterfacePolicy ipol = importPol.get(edge);
                GraphEdge otherEnd = _graph.getOtherEnd().get(edge);
                InterfacePolicy epol = null;
                if (otherEnd != null) {
                  epol = exportPol.get(otherEnd);
                }
                Integer peerGroup = (peer == null ? -1 : workset.getHandle(peer));
                EquivalenceEdge ee = new EquivalenceEdge(peerGroup, ipol, epol);
                Tuple<String, EquivalenceEdge> tup = new Tuple<>(peer, ee);
                Set<Tuple<String, EquivalenceEdge>> group =
                    universalMap.computeIfAbsent(router, k -> new HashSet<>());
                group.add(tup);
              }
            }
          }
          // Collect router by policy
          Map<Set<Tuple<String, EquivalenceEdge>>, Set<String>> inversePolicyMap = new HashMap<>();
          universalMap.forEach((router, set) -> {
            Set<String> routers = inversePolicyMap.computeIfAbsent(set, gs -> new HashSet<>());
            routers.add(router);
          });
          // Only add changed to the list
          for (Set<String> collection : inversePolicyMap.values()) {
            if (!ps.contains(collection)) {
              todo.add(collection);
            }
          }

        } else {

          // Split by existential abstraction
          Table2<String, EquivalenceEdge, Integer> existentialMap = new Table2<>();
          for (String router : partition) {
            List<GraphEdge> edges = _graph.getEdgeMap().get(router);
            for (GraphEdge edge : edges) {
              if (!edge.isAbstract()) {
                String peer = edge.getPeer();
                InterfacePolicy ipol = importPol.get(edge);
                GraphEdge otherEnd = _graph.getOtherEnd().get(edge);
                InterfacePolicy epol = null;
                if (otherEnd != null) {
                  epol = exportPol.get(otherEnd);
                }
                Integer peerGroup = (peer == null ? -1 : workset.getHandle(peer));
                EquivalenceEdge ee = new EquivalenceEdge(peerGroup, ipol, epol);
                Integer i = existentialMap.get(router, ee);
                i = (i == null ? 1 : i + 1);
                existentialMap.put(router, ee, i);
              }
            }
          }
          // Collect router by policy type
          Map<Set<EquivalenceEdge>, Set<String>> inversePolicyMap = new HashMap<>();
          existentialMap.forEach((router, map) -> {
            Set<EquivalenceEdge> edges = map.keySet();
            Set<String> routers = inversePolicyMap.computeIfAbsent(edges, gs -> new HashSet<>());
            routers.add(router);
          });
          // Only add changed to the list
          for (Set<String> collection : inversePolicyMap.values()) {
            if (!ps.contains(collection)) {
              todo.add(collection);
            }
          }
        }

        // TODO: universal abstraction

        // System.out.println("Todo now: " + todo);
      }

      // Now refine the abstraction further
      for (Set<String> partition : todo) {
        workset.split(partition);
      }

    } while (!todo.isEmpty());

    // System.out.println("  size: " + workset.partitions().size());
    // System.out.println("Abstraction for: " + prefixes);
    // System.out.println("ECs: \n" + workset.partitions());

    Tuple<Graph, AbstractionMap> abstractNetwork = createAbstractNetwork(workset, devices);
    Graph abstractGraph = abstractNetwork.getFirst();
    AbstractionMap abstraction = abstractNetwork.getSecond();

    // System.out.println("Groups: \n" + workset.partitions());
    //System.out.println("New graph: \n" + abstractGraph);
    System.out.println("Abstract Size: " + abstractGraph.getConfigurations().size());
    // System.out.println("Num Groups: " + workset.partitions().size());

    HeaderSpace h = createHeaderSpace(prefixes);
    return new EquivalenceClass(h, abstractGraph, abstraction);
  }

  // TODO: lookup based on local preference
  private boolean needUniversalAbstraction(Set<String> partition) {
    return _possibleFailures > 0;
  }

  /*
   * Given a collection of abstract roles, computes a set of canonical
   * representatives from each role that serve as the abstraction.
   */
  private Map<Integer, Set<String>> chooseCanonicalRouters(
      UnionSplit<String> us, Set<String> dests) {

    Map<Integer, Set<String>> choosen = new HashMap<>();
    Stack<Tuple<Integer, String>> todo = new Stack<>();

    // Start with the concrete nodes
    for (String d : dests) {
      Integer i = us.getHandle(d);
      Tuple<Integer, String> tup = new Tuple<>(i, d);
      todo.push(tup);
    }

    // Need to choose representatives that are connected
    while (!todo.isEmpty()) {
      Tuple<Integer, String> tup = todo.pop();
      Integer i = tup.getFirst();
      String router = tup.getSecond();
      Set<String> current = choosen.computeIfAbsent(i, k -> new HashSet<>());

      if (current.size() > _possibleFailures || current.contains(router)) {
        continue;
      }
      current.add(router);
      for (GraphEdge ge : _graph.getEdgeMap().get(router)) {
        String peer = ge.getPeer();
        if (peer != null) {
          Integer j = us.getHandle(peer);
          Tuple<Integer, String> peerTup = new Tuple<>(j, peer);
          todo.push(peerTup);
        }
      }
    }

    return choosen;
  }

  /*
   * Creates a new Configuration from an old one for an abstract router
   * by copying the old configuration, but removing any concrete interfaces,
   * neighbors etc that do not correpond to any abstract neighbors.
   */
  private Configuration createAbstractConfig(Set<String> abstractRouters, Configuration conf) {
    Configuration abstractConf = new Configuration(conf.getHostname());
    abstractConf.setDomainName(conf.getDomainName());
    abstractConf.setConfigurationFormat(conf.getConfigurationFormat());
    abstractConf.setDnsServers(conf.getDnsServers());
    abstractConf.setDnsSourceInterface(conf.getDnsSourceInterface());
    abstractConf.setAuthenticationKeyChains(conf.getAuthenticationKeyChains());
    abstractConf.setIkeGateways(conf.getIkeGateways());
    abstractConf.setDefaultCrossZoneAction(conf.getDefaultCrossZoneAction());
    abstractConf.setIkePolicies(conf.getIkePolicies());
    abstractConf.setIkeProposals(conf.getIkeProposals());
    abstractConf.setDefaultInboundAction(conf.getDefaultInboundAction());
    abstractConf.setIpAccessLists(conf.getIpAccessLists());
    abstractConf.setIp6AccessLists(conf.getIp6AccessLists());
    abstractConf.setRouteFilterLists(conf.getRouteFilterLists());
    abstractConf.setRoute6FilterLists(conf.getRoute6FilterLists());
    abstractConf.setIpsecPolicies(conf.getIpsecPolicies());
    abstractConf.setIpsecProposals(conf.getIpsecProposals());
    abstractConf.setIpsecVpns(conf.getIpsecVpns());
    abstractConf.setLoggingServers(conf.getLoggingServers());
    abstractConf.setLoggingSourceInterface(conf.getLoggingSourceInterface());
    abstractConf.setNormalVlanRange(conf.getNormalVlanRange());
    abstractConf.setNtpServers(conf.getNtpServers());
    abstractConf.setNtpSourceInterface(conf.getNtpSourceInterface());
    abstractConf.setRoles(conf.getRoles());
    abstractConf.setSnmpSourceInterface(conf.getSnmpSourceInterface());
    abstractConf.setSnmpTrapServers(conf.getSnmpTrapServers());
    abstractConf.setTacacsServers(conf.getTacacsServers());
    abstractConf.setTacacsSourceInterface(conf.getTacacsSourceInterface());
    abstractConf.setVendorFamily(conf.getVendorFamily());
    abstractConf.setZones(conf.getZones());
    abstractConf.setCommunityLists(conf.getCommunityLists());
    abstractConf.setRoutingPolicies(conf.getRoutingPolicies());
    abstractConf.setRoute6FilterLists(conf.getRoute6FilterLists());

    SortedSet<Interface> toRetain = new TreeSet<>();
    SortedSet<Pair<Ip, Ip>> ipNeighbors = new TreeSet<>();
    SortedSet<BgpNeighbor> bgpNeighbors = new TreeSet<>();

    List<GraphEdge> edges = _graph.getEdgeMap().get(conf.getName());
    for (GraphEdge ge : edges) {
      boolean leavesNetwork = (ge.getPeer() == null);
      if (leavesNetwork
          || (abstractRouters.contains(ge.getRouter()) && abstractRouters.contains(ge.getPeer()))) {
        toRetain.add(ge.getStart());
        Ip start = ge.getStart().getPrefix().getAddress();
        if (!leavesNetwork) {
          Ip end = ge.getEnd().getPrefix().getAddress();
          ipNeighbors.add(new Pair<>(start, end));
        }
        BgpNeighbor n = _graph.getEbgpNeighbors().get(ge);
        if (n != null) {
          bgpNeighbors.add(n);
        }
      }
    }

    // Update interfaces
    NavigableMap<String, Interface> abstractInterfaces = new TreeMap<>();
    for (Entry<String, Interface> entry : conf.getInterfaces().entrySet()) {
      String name = entry.getKey();
      Interface iface = entry.getValue();
      if (toRetain.contains(iface)) {
        abstractInterfaces.put(name, iface);
      }
    }
    abstractConf.setInterfaces(abstractInterfaces);

    // Update VRFs
    Map<String, Vrf> abstractVrfs = new HashMap<>();
    for (Entry<String, Vrf> entry : conf.getVrfs().entrySet()) {
      String name = entry.getKey();
      Vrf vrf = entry.getValue();
      Vrf abstractVrf = new Vrf(name);
      abstractVrf.setStaticRoutes(vrf.getStaticRoutes());
      abstractVrf.setIsisProcess(vrf.getIsisProcess());
      abstractVrf.setRipProcess(vrf.getRipProcess());
      abstractVrf.setSnmpServer(vrf.getSnmpServer());

      NavigableMap<String, Interface> abstractVrfInterfaces = new TreeMap<>();
      for (Entry<String, Interface> entry2 : vrf.getInterfaces().entrySet()) {
        String iname = entry2.getKey();
        Interface iface = entry2.getValue();
        if (toRetain.contains(iface)) {
          abstractVrfInterfaces.put(iname, iface);
        }
      }
      abstractVrf.setInterfaces(abstractVrfInterfaces);
      abstractVrf.setInterfaceNames(new TreeSet<>(abstractVrfInterfaces.keySet()));

      OspfProcess ospf = vrf.getOspfProcess();
      if (ospf != null) {
        OspfProcess abstractOspf = new OspfProcess();
        abstractOspf.setAreas(ospf.getAreas());
        abstractOspf.setExportPolicy(ospf.getExportPolicy());
        abstractOspf.setReferenceBandwidth(ospf.getReferenceBandwidth());
        abstractOspf.setRouterId(ospf.getRouterId());
        // Copy over neighbors
        Map<Pair<Ip, Ip>, OspfNeighbor> abstractNeighbors = new HashMap<>();
        if (ospf.getOspfNeighbors() != null) {
          for (Entry<Pair<Ip, Ip>, OspfNeighbor> entry2 : ospf.getOspfNeighbors().entrySet()) {
            Pair<Ip, Ip> pair = entry2.getKey();
            OspfNeighbor neighbor = entry2.getValue();
            if (ipNeighbors.contains(pair)) {
              abstractNeighbors.put(pair, neighbor);
            }
          }
        }
        abstractOspf.setOspfNeighbors(abstractNeighbors);
        abstractVrf.setOspfProcess(abstractOspf);
      }

      BgpProcess bgp = vrf.getBgpProcess();
      if (bgp != null) {
        BgpProcess abstractBgp = new BgpProcess();
        abstractBgp.setMultipathEbgp(bgp.getMultipathEbgp());
        abstractBgp.setMultipathIbgp(bgp.getMultipathIbgp());
        abstractBgp.setRouterId(bgp.getRouterId());
        abstractBgp.setOriginationSpace(bgp.getOriginationSpace());
        // TODO: set bgp neighbors accordingly
        // Copy over neighbors
        SortedMap<Prefix, BgpNeighbor> abstractBgpNeighbors = new TreeMap<>();
        if (bgp.getNeighbors() != null) {
          for (Entry<Prefix, BgpNeighbor> entry2 : bgp.getNeighbors().entrySet()) {
            Prefix prefix = entry2.getKey();
            BgpNeighbor neighbor = entry2.getValue();
            if (bgpNeighbors.contains(neighbor)) {
              abstractBgpNeighbors.put(prefix, neighbor);
            }
          }
        }
        abstractBgp.setNeighbors(abstractBgpNeighbors);
        abstractVrf.setBgpProcess(abstractBgp);
      }

      abstractVrfs.put(name, abstractVrf);
    }

    abstractConf.setVrfs(abstractVrfs);
    return abstractConf;
  }

  /*
   * Create a collection of abstract configurations given the roles computed
   * and the collection of concrete devices. Chooses a collection of canonical
   * representatives from each role, and then removes all their interfaces etc
   * that connect to non-canonical routers.
   */
  private Tuple<Graph, AbstractionMap> createAbstractNetwork(
      UnionSplit<String> us, Set<String> dests) {

    Map<Integer, Set<String>> canonicalChoices = chooseCanonicalRouters(us, dests);
    Set<String> abstractRouters = new HashSet<>();
    for (Set<String> canonical : canonicalChoices.values()) {
      abstractRouters.addAll(canonical);
    }
    // Create the abstract configurations
    Map<String, Configuration> newConfigs = new HashMap<>();
    for (Entry<String, Configuration> entry : _graph.getConfigurations().entrySet()) {
      String router = entry.getKey();
      Configuration conf = entry.getValue();
      if (abstractRouters.contains(router)) {
        Configuration abstractConf = createAbstractConfig(abstractRouters, conf);
        newConfigs.put(router, abstractConf);
      }
    }

    Graph abstractGraph = new Graph(_batfish, newConfigs);
    AbstractionMap map = new AbstractionMap(canonicalChoices, us.getParitionMap());
    return new Tuple<>(abstractGraph, map);
  }

  public AnswerElement asAnswer() {
    long start = System.currentTimeMillis();
    int i = 0;
    for (EquivalenceClass ec : this) {
      i++;
      System.out.println("EC: " + i);
      //System.out.println("EC: " + i + " has size " + ec.getGraph().getConfigurations().size());
    }
    AbstractionAnswerElement answer = new AbstractionAnswerElement();
    long end = System.currentTimeMillis();
    System.out.println("Total time (sec): " + ((double) end - start) / 1000);
    return answer;
  }

  private class AbstractionIterator implements Iterator<EquivalenceClass> {

    private Iterator<Entry<Set<String>, List<Prefix>>> _iter;

    AbstractionIterator() {
      _iter = _destinationMap.entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
      return _iter.hasNext();
    }

    @Override
    public EquivalenceClass next() {
      Entry<Set<String>, List<Prefix>> x = _iter.next();
      Set<String> devices = x.getKey();
      List<Prefix> prefixes = x.getValue();
      return computeAbstraction(devices, prefixes);
    }
  }
}
