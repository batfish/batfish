package org.batfish.symbolic.abstraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import org.batfish.common.Pair;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OspfNeighbor;
import org.batfish.datamodel.OspfProcess;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.collections.Table2;
import org.batfish.symbolic.collections.UnionSplit;
import org.batfish.symbolic.utils.Tuple;

class AbstractionBuilder {

  private static final int EBGP_INDEX = -1;

  private static final int HOST_OR_LOOPBACK_INDEX = -2;

  private static final boolean ENABLE_SELF_LOOP_OPTIMIZATION = true;

  private static final boolean ENABLE_MONOTINICITY_OPTIMIZATION = true;

  private IBatfish _batfish;

  private Graph _graph;

  private BDDNetwork _network;

  private int _possibleFailures;

  private Set<String> _destinations;

  private HeaderSpace _headerspace;

  private List<Prefix> _prefixes;

  private Map<GraphEdge, InterfacePolicy> _exportPol;

  private Map<GraphEdge, InterfacePolicy> _importPol;

  private Table2<String, Integer, Set<EdgePolicyToRole>> _eeMap;

  private Table2<String, Integer, Set<EdgePolicy>> _polMap;

  private Table2<String, EdgePolicyToRole, Integer> _existentialMap;

  private Map<String, Set<Tuple<String, EdgePolicyToRole>>> _universalMap;

  private UnionSplit<String> _abstractGroups;

  private AbstractionBuilder(
      DestinationClasses a,
      BDDNetwork network,
      Set<String> devices,
      HeaderSpace h,
      List<Prefix> pfxs,
      int fails) {
    this._batfish = a.getBatfish();
    this._graph = a.getGraph();
    this._network = network;
    this._possibleFailures = fails;
    this._destinations = devices;
    this._headerspace = h;
    this._prefixes = pfxs;
    this._eeMap = new Table2<>();
    this._polMap = new Table2<>();
    this._existentialMap = new Table2<>();
    this._universalMap = new HashMap<>();
  }

  static NetworkSlice createGraph(
      DestinationClasses a,
      BDDNetwork network,
      Set<String> devices,
      HeaderSpace h,
      List<Prefix> pfxs,
      int fails) {
    AbstractionBuilder g = new AbstractionBuilder(a, network, devices, h, pfxs, fails);
    return g.computeAbstraction();
  }

  /*
   * Create an abstract network that is forwarding-equivalent to the original
   * network for a given destination-based slice of the original network.
   */
  private NetworkSlice computeAbstraction() {

    _exportPol = new HashMap<>();
    _importPol = new HashMap<>();

    if (_prefixes == null) {
      _exportPol = _network.getExportPolicyMap();
      _importPol = _network.getImportPolicyMap();
    } else {
      specialize(false);
    }

    _abstractGroups = new UnionSplit<>(_graph.getRouters());

    // Each origination point will remain concrete
    for (String device : _destinations) {
      _abstractGroups.split(device);
    }

    // Repeatedly split the abstraction to a fixed point
    Set<Set<String>> todo;
    do {
      todo = new HashSet<>();
      _eeMap.clear();
      _polMap.clear();

      Collection<Set<String>> ps = _abstractGroups.partitions();

      // System.out.println("Partitions: " + ps);

      for (Set<String> partition : ps) {
        // Nothing to refine if already a concrete node
        if (partition.size() <= 1) {
          continue;
        }

        if (needUniversalAbstraction()) {
          refineAbstraction(todo, ps, partition, false, true);
        } else if (_possibleFailures > 0) {
          refineAbstraction(todo, ps, partition, true, false);
        } else {
          refineAbstraction(todo, ps, partition, false, false);
        }
        // If something changed, then start over early
        // Helps the next iteration to use the newly reflected information.
        if (todo.size() > 0) {
          break;
        }
      }

      // Now refine the abstraction further
      for (Set<String> newPartition : todo) {
        _abstractGroups.split(newPartition);
      }

    } while (!todo.isEmpty());

    //System.out.println("EC Devices: " + _destinations);
    //System.out.println("EC Prefixes: " + _prefixes);
    //System.out.println("Groups: \n" + _abstractGroups.partitions());
    //System.out.println("New graph: \n" + abstractGraph);
    //System.out.println("Num Groups: " + workset.partitions().size());
    Tuple<Graph, AbstractionMap> abstractNetwork = createAbstractNetwork();
    Graph abstractGraph = abstractNetwork.getFirst();
    AbstractionMap abstractionMap = abstractNetwork.getSecond();
    //System.out.println("Num configs: " + abstractGraph.getConfigurations().size());
    Abstraction a = new Abstraction(abstractGraph, abstractionMap);
    return new NetworkSlice(_headerspace, a);
  }

  /*
   * Specialize the collection of BDDs representing ACL and route map policies on
   * each edge. Must be synchronized since BDDs are not thread-safe.
   */
  private synchronized void specialize(boolean specializeBdds) {
    for (Entry<GraphEdge, InterfacePolicy> entry : _network.getExportPolicyMap().entrySet()) {
      GraphEdge ge = entry.getKey();
      InterfacePolicy pol = entry.getValue();
      InterfacePolicy newPol = pol.restrictStatic(_prefixes);
      newPol = (specializeBdds ? newPol.restrict(_prefixes) : newPol);
      _exportPol.put(ge, newPol);
    }
    for (Entry<GraphEdge, InterfacePolicy> entry : _network.getImportPolicyMap().entrySet()) {
      GraphEdge ge = entry.getKey();
      InterfacePolicy pol = entry.getValue();
      InterfacePolicy newPol = pol.restrictStatic(_prefixes);
      newPol = (specializeBdds ? newPol.restrict(_prefixes) : newPol);
      _importPol.put(ge, newPol);
    }
  }

  /*
   * Refine the abstraction for the given partition of nodes so that
   * we have an existential abstraction. For each node there exists an
   * edge to the same abstract neighbor.
   */
  private void refineAbstraction(
      Set<Set<String>> todo,
      Collection<Set<String>> ps,
      Set<String> partition,
      boolean countMatters,
      boolean isUniversal) {

    // Split by existential abstraction
    _existentialMap.clear();
    _universalMap.clear();

    collectInterfaceInformation(partition, isUniversal);

    if (!countMatters) {
      applyMonotonicityOptimization(partition, isUniversal);
    }

    applySelfLoopOptimization(partition, isUniversal);

    // If there is more than one policy to the same abstract neighbor, we make concrete
    // Since by definition this can not be a valid abstraction
    Set<String> makeConcrete = new HashSet<>();
    _eeMap.forEach(
        (router, peerGroup, edges) -> {
          if (partition.contains(router) && edges.size() > 1) {
            makeConcrete.add(router);
          }
        });

    Collection<Set<String>> newPartitions = new HashSet<>();

    // Add concrete devices
    for (String router : makeConcrete) {
      Set<String> singleDevice = new HashSet<>();
      singleDevice.add(router);
      newPartitions.add(singleDevice);
    }

    // Collect router by policy type
    if (isUniversal) {
      // Universal Abstraction
      Map<Set<Tuple<String, EdgePolicyToRole>>, Set<String>> inversePolicyMap = new HashMap<>();
      _universalMap.forEach(
          (router, set) -> {
            if (!makeConcrete.contains(router)) {
              Set<String> routers = inversePolicyMap.computeIfAbsent(set, gs -> new HashSet<>());
              routers.add(router);
            }
          });
    } else {
      if (countMatters) {
        Map<Map<EdgePolicyToRole, Integer>, Set<String>> inversePolicyMap = new HashMap<>();
        _existentialMap.forEach(
            (router, map) -> {
              if (partition.contains(router) && !makeConcrete.contains(router)) {
                Set<String> routers = inversePolicyMap.computeIfAbsent(map, k -> new HashSet<>());
                routers.add(router);
              }
            });
        newPartitions.addAll(inversePolicyMap.values());

      } else {
        Map<Set<EdgePolicyToRole>, Set<String>> inversePolicyMap = new HashMap<>();
        _existentialMap.forEach(
            (router, map) -> {
              if (partition.contains(router) && !makeConcrete.contains(router)) {
                Set<EdgePolicyToRole> edges = map.keySet();
                Set<String> routers = inversePolicyMap.computeIfAbsent(edges, k -> new HashSet<>());
                routers.add(router);
              }
            });
        newPartitions.addAll(inversePolicyMap.values());
      }
    }

    // Only add changed to the list
    for (Set<String> collection : newPartitions) {
      if (!ps.contains(collection)) {
        todo.add(collection);
      }
    }
  }

  private void applySelfLoopOptimization(Set<String> partition, boolean isUniversal) {
    if (ENABLE_SELF_LOOP_OPTIMIZATION) {
      if (isUniversal) {
        // Optimization: Find if we can ignore self loops, and if so, delete the entries
        Set<String> canIgnoreSelfLoops = canIgnoreSelfLoops(partition);
        for (String router : canIgnoreSelfLoops) {
          Integer j = _abstractGroups.getHandle(router);
          Map<Integer, Set<EdgePolicyToRole>> map = _eeMap.get(router);
          map.remove(j);
          _universalMap.get(router).removeIf(tup -> tup.getSecond().getAbstractId().equals(j));
        }
      } else {
        // Optimization: Find if we can ignore self loops, and if so, delete the entries
        Set<String> canIgnoreSelfLoops = canIgnoreSelfLoops(partition);
        for (String router : canIgnoreSelfLoops) {
          Integer j = _abstractGroups.getHandle(router);
          Map<Integer, Set<EdgePolicyToRole>> map = _eeMap.get(router);
          map.remove(j);
          _existentialMap
              .get(router)
              .entrySet()
              .removeIf(entry -> entry.getKey().getAbstractId().equals(j));
        }
      }
    }
  }

  private void applyMonotonicityOptimization(Set<String> partition, boolean isUniversal) {
    // Optimization: monotonicity
    if (ENABLE_MONOTINICITY_OPTIMIZATION) {
      Integer currentIdx = _abstractGroups.getHandle(partition.iterator().next());
      Set<Integer> toDelete = new HashSet<>();
      Set<Integer> neighborGroups = collectNeighborGroups(partition);
      // System.out.println("Current Partition: " + partition);
      for (Integer i : neighborGroups) {
        Set<String> neighbors = _abstractGroups.getPartition(i);
        // System.out.println("  Looking at neighbors: " + neighbors + " with " + i);
        if (neighbors.size() == 1 && _destinations.containsAll(neighbors)) {
          continue;
        }
        // Check if a single policy to some neighbor
        Set<Set<EdgePolicy>> allPolicies = collectAllPolicies(i, partition);
        //System.out.println("  All current policies: " + allPolicies);
        if (allPolicies.size() != 1) {
          continue;
        }
        // System.out.println("  HAS SINGLE POLICY");

        collectInterfaceInformation(neighbors, isUniversal);

        Set<Integer> groups = collectNeighborGroups(neighbors);
        groups.remove(HOST_OR_LOOPBACK_INDEX);
        groups.remove(i);
        //System.out.println("  All groups that neighbor has: " + groups);
        if (groups.size() != 1) {
          continue;
        }
        // System.out.println("  NEIGHBORS ARE ISOLATED");
        // All have same policy towards this group
        Set<Set<EdgePolicy>> allPols = collectAllPolicies(currentIdx, neighbors);
        //System.out.println("  All policies from neighbor: " + allPols);
        //System.out.println("  All pols: " + allPols);
        if (allPols.size() == 1) {
          // System.out.println("  NEIGHBORS ALSO ONLY HAVE ONE");
          toDelete.add(i);
          break; // only do this once
        }
      }
      for (Integer i : toDelete) {
        for (String router : partition) {
          _existentialMap
              .get(router)
              .entrySet()
              .removeIf(entry -> entry.getKey().getAbstractId().equals(i));
        }
      }
    }
  }

  @Nonnull
  private Set<Set<EdgePolicy>> collectAllPolicies(Integer currentIdx, Set<String> neighbors) {
    Set<Set<EdgePolicy>> allPols = new HashSet<>();
    for (String router : neighbors) {
      Set<EdgePolicy> pols = _polMap.get(router, currentIdx);
      if (pols != null) {
        allPols.add(pols);
      }
    }
    return allPols;
  }

  private void collectInterfaceInformation(Set<String> partition, boolean isUniversal) {
    for (String router : partition) {
      List<GraphEdge> edges = _graph.getEdgeMap().get(router);
      for (GraphEdge edge : edges) {
        String peer = edge.getPeer();
        InterfacePolicy ipol = _importPol.get(edge);
        GraphEdge otherEnd = _graph.getOtherEnd().get(edge);
        InterfacePolicy epol = null;
        if (otherEnd != null) {
          epol = _exportPol.get(otherEnd);
        }
        // Update the existential map
        Integer peerGroup =
            (peer == null
                ? (_graph.isExternal(edge) ? EBGP_INDEX : HOST_OR_LOOPBACK_INDEX)
                : _abstractGroups.getHandle(peer));
        EdgePolicy pair = new EdgePolicy(ipol, epol);
        EdgePolicyToRole ee = new EdgePolicyToRole(peerGroup, pair);

        if (isUniversal) {
          // Universal abstraction
          Tuple<String, EdgePolicyToRole> tup = new Tuple<>(peer, ee);
          Set<Tuple<String, EdgePolicyToRole>> group =
              _universalMap.computeIfAbsent(router, k -> new HashSet<>());
          group.add(tup);
        } else {
          // Existential abstraction
          Integer i = _existentialMap.get(router, ee);
          i = (i == null ? 1 : i + 1);
          _existentialMap.put(router, ee, i);
        }

        // Update the id map
        if (peerGroup >= 0) {
          Set<EdgePolicyToRole> x =
              _eeMap.computeIfAbsent(router, peerGroup, (k1, k2) -> new HashSet<>());
          x.add(ee);

          Set<EdgePolicy> y =
              _polMap.computeIfAbsent(router, peerGroup, (k1, k2) -> new HashSet<>());
          y.add(pair);
        }
      }
    }
  }

  @Nonnull
  private Set<Integer> collectNeighborGroups(Set<String> partition) {
    Set<Integer> neighborGroups = new HashSet<>();
    for (String router : partition) {
      for (GraphEdge ge : _graph.getEdgeMap().get(router)) {
        if (ge.getPeer() != null) {
          Integer peerGroup = _abstractGroups.getHandle(ge.getPeer());
          if (peerGroup >= 0) {
            neighborGroups.add(peerGroup);
          }
        }
      }
    }
    return neighborGroups;
  }

  // TODO: lookup based on local preference
  private boolean needUniversalAbstraction() {
    return false;
  }

  /*
   * Collect concrete neighbors by their abstract ids
   */
  private Table2<String, Integer, Set<String>> collectNeighborByAbstractId() {
    // organize neighbors by abstract id
    Table2<String, Integer, Set<String>> neighborByAbstractId = new Table2<>();
    for (Entry<String, List<GraphEdge>> entry : _graph.getEdgeMap().entrySet()) {
      String router = entry.getKey();
      List<GraphEdge> edges = entry.getValue();
      for (GraphEdge ge : edges) {
        String peer = ge.getPeer();
        if (peer != null) {
          Integer j = _abstractGroups.getHandle(peer);
          Set<String> existing = neighborByAbstractId.get(router, j);
          if (existing != null) {
            existing.add(peer);
          } else {
            Set<String> neighbors = new HashSet<>();
            neighbors.add(peer);
            neighborByAbstractId.put(router, j, neighbors);
          }
        }
      }
    }
    return neighborByAbstractId;
  }

  /*
   * Given a collection of abstract roles, selects a set of canonical
   * representatives from each role that serve as the abstraction.
   */
  private Map<Integer, Set<String>> pickCanonicalRouters() {
    Set<String> avoidNodes =
        (ENABLE_MONOTINICITY_OPTIMIZATION ? findDeadRouters() : new HashSet<>());

    Table2<String, Integer, Set<String>> neighborByAbstractId = collectNeighborByAbstractId();
    Map<Integer, Set<String>> chosen = new HashMap<>();
    Stack<String> stack = new Stack<>();
    List<String> options = new ArrayList<>();

    if (_destinations.isEmpty()) {
      // When destination only can be from external
      // Pick a representative from each externally-facing abstract group
      Set<Integer> picked = new HashSet<>();
      for (GraphEdge ge : _graph.getAllRealEdges()) {
        if (_graph.isExternal(ge)) {
          String d = ge.getRouter();
          Integer i = _abstractGroups.getHandle(d);
          if (!picked.contains(i)) {
            Set<String> dest = new HashSet<>();
            dest.add(d);
            stack.push(d);
            chosen.put(i, dest);
            picked.add(i);
          }
        }
      }
    } else {
      // Start with the concrete nodes
      for (String d : _destinations) {
        stack.push(d);
        Set<String> dest = new HashSet<>();
        dest.add(d);
        chosen.put(_abstractGroups.getHandle(d), dest);
      }
    }

    // Need to choose representatives that are connected
    while (!stack.isEmpty()) {
      String router = stack.pop();

      Map<Integer, Set<String>> neighbors = neighborByAbstractId.get(router);
      if (neighbors == null) {
        continue;
      }
      for (Entry<Integer, Set<String>> entry : neighbors.entrySet()) {
        Integer j = entry.getKey();

        if (ENABLE_SELF_LOOP_OPTIMIZATION) {
          if (_abstractGroups.getPartition(j).contains(router)) {
            if (isMonotonic(_polMap.get(router), j)) {
              continue;
            }
          }
        }

        Set<String> peers = entry.getValue();
        Set<String> chosenPeers = chosen.computeIfAbsent(j, k -> new HashSet<>());
        // Find how many choices we need, and collect the options
        int numNeeded = _possibleFailures + 1;
        options.clear();
        for (String x : peers) {
          if (avoidNodes.contains(x)) {
            continue;
          }
          if (chosenPeers.contains(x)) {
            numNeeded--;
          } else {
            options.add(x);
          }
        }
        // Add new neighbors until satisfied
        for (int k = 0; k < Math.min(numNeeded, options.size()); k++) {
          String y = options.get(k);
          chosenPeers.add(y);
          stack.push(y);
        }
      }
    }

    return chosen;
  }

  private Set<String> canIgnoreSelfLoops(Set<String> partition) {
    Set<String> ignore = new HashSet<>();
    for (String router : partition) {
      Map<Integer, Set<EdgePolicy>> map = _polMap.get(router);
      map.forEach(
          (i, set) -> {
            if (isMonotonic(map, i)) {
              ignore.add(router);
            }
          });
    }
    return ignore;
  }

  // TODO: this should be carefully checked and optimized
  private boolean isMonotonic(Map<Integer, Set<EdgePolicy>> neighborPols, Integer j) {
    // Check if the same policy is used for all neighbors
    Set<EdgePolicy> pols = neighborPols.get(j);
    if (pols == null) {
      return true;
    }
    for (Entry<Integer, Set<EdgePolicy>> entry : neighborPols.entrySet()) {
      Integer i = entry.getKey();
      Set<EdgePolicy> pols2 = entry.getValue();
      if (!i.equals(j)) {
        if (!Objects.equals(pols, pols2)) {
          return false;
        }
      }
    }
    return true;
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
  private Tuple<Graph, AbstractionMap> createAbstractNetwork() {
    Map<Integer, Set<String>> canonicalChoices = pickCanonicalRouters();
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
    AbstractionMap map = new AbstractionMap(canonicalChoices, _abstractGroups.getParitionMap());
    return new Tuple<>(abstractGraph, map);
  }

  private Set<String> findDeadRouters() {
    Set<String> deadNodes = new HashSet<>();
    boolean changed = true;
    while (changed) {
      changed = false;
      for (Set<String> partition : _abstractGroups.partitions()) {
        Set<Integer> partitionGroups = collectNeighborGroups(partition);
        partitionGroups.remove(HOST_OR_LOOPBACK_INDEX);
        for (String router : partition) {
          if (deadNodes.contains(router) || _destinations.contains(router)) {
            continue;
          }
          Integer i = _abstractGroups.getHandle(router);
          Set<Integer> individualGroups = new HashSet<>();
          for (String neighbor : _graph.getNeighbors().get(router)) {
            if (deadNodes.contains(neighbor)) {
              continue;
            }
            Integer j = _abstractGroups.getHandle(neighbor);
            if (!i.equals(j)) {
              individualGroups.add(j);
            }
          }
          if (!individualGroups.equals(partitionGroups)) {
            deadNodes.add(router);
            changed = true;
          }
        }
      }
    }
    return deadNodes;
  }
}
