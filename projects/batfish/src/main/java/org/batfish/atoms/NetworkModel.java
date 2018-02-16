package org.batfish.atoms;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowHistory;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.FlowTraceHop;
import org.batfish.datamodel.ForwardingAction;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IRib;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.TcpFlags;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.pojo.Environment;
import org.batfish.main.Batfish;
import org.batfish.symbolic.bdd.BDDAcl;
import org.batfish.symbolic.bdd.BDDPacket;
import org.batfish.symbolic.collections.PList;
import org.batfish.symbolic.utils.Tuple;

public class NetworkModel {

  private static int ACCEPT_FLAG = 0;
  private static int DROP_FLAG = 1;
  private static int DROP_ACL_FLAG = 2;
  private static int DROP_ACL_IN_FLAG = 3;
  private static int DROP_ACL_OUT_FLAG = 4;
  private static int DROP_NULL_ROUTE_FLAG = 5;
  private static int DROP_NO_ROUTE_FLAG = 6;

  // Keep a reference to the batfish object
  private Batfish _batfish;

  // Keep a reference to the dataplane object
  private DataPlane _dataPlane;

  // Map from an edge to the outbound ACL along the edge
  private Map<GraphLink, IpAccessList> _outAcls;

  // Map from an edge to the inbound ACL along the edge
  private Map<GraphLink, IpAccessList> _inAcls;

  // Map from an edge to the set of atomic predicates for the inbound ACL
  private Map<GraphLink, BitSet> _aclInPredicates;

  // Map from an edge to the set of atomic predicates for the outbound ACL
  private Map<GraphLink, BitSet> _aclOutPredicates;

  // A map from an edge the set of atomic predicates for forwarding
  private Map<GraphLink, BitSet> _forwardingPredicates;

  // The actual BDDs for each of the atomic predicates for ACLs
  private ArrayList<BDD> _aclBdds;

  // The actual BDDs for each of the atomic predicates for forwarding
  private ArrayList<BDD> _forwardingBdds;

  // Access to the collection of BDD variables representing each bit of some field
  private BDDPacket _bddPkt;

  // All the nodes in the graph
  private List<GraphNode> _allNodes;

  // All the links in the graph
  private List<GraphLink> _allLinks;

  // Adjacency list for the graph indexed by GraphNode index
  private ArrayList<List<GraphLink>> _adjacencyLists;

  // Map from routers to graph nodes in this extended graph
  private Map<String, GraphNode> _nodeMap;

  // Map from interfaces to links in this extened graph
  private Map<NodeInterfacePair, GraphLink> _linkMap;

  // Given a (directed) link, find the opposite link if one exists
  private Map<GraphLink, GraphLink> _otherEnd;

  /*
   * Model of the network in terms of atomic predicates on ports.
   * There are atomic predicates for both forwarding and for ACLs.
   */
  public NetworkModel(Batfish batfish, DataPlane dataPlane) {
    _batfish = batfish;
    _dataPlane = dataPlane;
    _outAcls = new HashMap<>();
    _inAcls = new HashMap<>();
    _aclInPredicates = new HashMap<>();
    _aclOutPredicates = new HashMap<>();
    _forwardingPredicates = new HashMap<>();
    _aclBdds = new ArrayList<>();
    _forwardingBdds = new ArrayList<>();
    _bddPkt = new BDDPacket();
    _nodeMap = new HashMap<>();
    _linkMap = new HashMap<>();
    _allNodes = new ArrayList<>();
    _allLinks = new ArrayList<>();
    _otherEnd = new HashMap<>();

    Map<String, Configuration> configs = batfish.loadConfigurations();
    initGraph(configs);

    long l = System.currentTimeMillis();
    computeAclPredicates();
    computeForwardingPredicates();

    System.out.println("Computing predicates took: " + (System.currentTimeMillis() - l));
    System.out.println("Number ACL atoms: " + _aclBdds.size());
    System.out.println("Number Fwd atoms: " + _forwardingBdds.size());
  }

  /*
   * Initialize the edge-labelled graph by creating nodes for
   * every router, and special ACL nodes for every ACL.
   */
  private void initGraph(Map<String, Configuration> configs) {
    Topology topo = _batfish.getEnvironmentTopology();

    // Special drop node for the null interface
    GraphNode dropNode = new GraphNode("(none)", 0);
    _nodeMap.put("(none)", dropNode);
    _allNodes.add(dropNode);

    // Special ingress node for an entry point into the network
    GraphNode ingressNode = new GraphNode("(ingress)", 1);
    _nodeMap.put("(ingress)", ingressNode);
    _allNodes.add(ingressNode);

    // Special egress node for an exit point to the network
    GraphNode egressNode = new GraphNode("(egress)", 2);
    _nodeMap.put("(egress)", egressNode);
    _allNodes.add(egressNode);

    int nodeIndex = 3;
    for (Entry<String, Configuration> entry : configs.entrySet()) {
      String router = entry.getKey();
      GraphNode node = new GraphNode(router, nodeIndex);
      nodeIndex++;
      _nodeMap.put(router, node);
      _allNodes.add(node);
    }

    // Initialize the node adjacencies
    _adjacencyLists = new ArrayList<>(_allNodes.size());
    for (int i = 0; i < _allNodes.size(); i++) {
      _adjacencyLists.add(new ArrayList<>());
    }

    Map<NodeInterfacePair, Set<NodeInterfacePair>> edgeMap = new HashMap<>();
    for (Edge edge : topo.getEdges()) {
      Set<NodeInterfacePair> nips = new HashSet<>();
      nips.add(edge.getInterface2());
      edgeMap.put(edge.getInterface1(), nips);
    }

    NodeInterfacePair ingressPair = new NodeInterfacePair("(ingress)", "ingress_interface");
    NodeInterfacePair egressPair = new NodeInterfacePair("(egress)", "egress_interface");
    NodeInterfacePair nullPair = new NodeInterfacePair("(none)", "null_interface");

    // add a null interface edge for each router
    for (Entry<String, Configuration> entry : configs.entrySet()) {
      String router = entry.getKey();
      Configuration config = entry.getValue();
      for (Entry<String, Interface> e : config.getInterfaces().entrySet()) {
        NodeInterfacePair nip = new NodeInterfacePair(router, e.getKey());
        if (!edgeMap.containsKey(nip)) {
          Set<NodeInterfacePair> nips = new HashSet<>();
          // add egress edge
          nips.add(egressPair);
          edgeMap.put(nip, nips);
          // add ingress edge
          Set<NodeInterfacePair> ingressNips =
              edgeMap.computeIfAbsent(ingressPair, k -> new HashSet<>());
          ingressNips.add(nip);
          edgeMap.put(ingressPair, ingressNips);
        }
      }
    }

    // Add null interface edges to the drop node for every router and ACL node
    for (Configuration config : configs.values()) {
      NodeInterfacePair nip = new NodeInterfacePair(config.getName(), "null_interface");
      Set<NodeInterfacePair> nips = new HashSet<>();
      nips.add(nullPair);
      edgeMap.put(nip, nips);
    }

    // Create the links
    Map<NodeInterfacePair, IpAccessList> inAcls = new HashMap<>();
    Map<NodeInterfacePair, IpAccessList> outAcls = new HashMap<>();
    int linkIndex = 0;
    for (Entry<NodeInterfacePair, Set<NodeInterfacePair>> entry : edgeMap.entrySet()) {
      NodeInterfacePair nip1 = entry.getKey();
      for (NodeInterfacePair nip2 : entry.getValue()) {

        GraphNode src = _nodeMap.get(nip1.getHostname());
        String router1 = nip1.getHostname();
        String router2 = nip2.getHostname();

        Configuration config1 = configs.get(router1);
        Configuration config2 = configs.get(router2);
        String ifaceName1 = nip1.getInterface();
        String ifaceName2 = nip2.getInterface();

        Interface iface1 = (config1 == null ? null : config1.getInterfaces().get(ifaceName1));
        Interface iface2 = (config2 == null ? null : config2.getInterfaces().get(ifaceName2));
        IpAccessList aclOut = (iface1 == null ? null : iface1.getOutgoingFilter());
        IpAccessList aclIn = (iface2 == null ? null : iface2.getIncomingFilter());

        GraphLink l;
        if (ifaceName1.equals("null_interface")) {
          l = new GraphLink(src, ifaceName1, dropNode, "null_interface", linkIndex);
          linkIndex++;
          _linkMap.put(nip1, l);
          _adjacencyLists.get(src.getIndex()).add(l);
          _allLinks.add(l);
        } else {
          GraphNode tgt = _nodeMap.get(router2);
          l = new GraphLink(src, ifaceName1, tgt, ifaceName2, linkIndex);
          linkIndex++;
          _linkMap.put(nip1, l);
          _adjacencyLists.get(src.getIndex()).add(l);
          _allLinks.add(l);
        }

        if (aclOut != null) {
          outAcls.put(nip1, aclOut);
        }
        if (aclIn != null) {
          inAcls.put(nip2, aclIn);
        }
      }
    }

    // Place the acls for the correct graph links
    for (Entry<NodeInterfacePair, IpAccessList> e : outAcls.entrySet()) {
      _outAcls.put(_linkMap.get(e.getKey()), e.getValue());
    }
    for (Entry<NodeInterfacePair, IpAccessList> e : inAcls.entrySet()) {
      _inAcls.put(_linkMap.get(e.getKey()), e.getValue());
    }

    // Map links to their opposite end
    for (GraphLink link : _allLinks) {
      String otherName = link.getTarget().getName();
      String otherIfaceName = link.getTargetIface();
      NodeInterfacePair otherNip = new NodeInterfacePair(otherName, otherIfaceName);
      GraphLink other = _linkMap.get(otherNip);
      _otherEnd.put(link, other);
    }
  }

  // Right now this does 32 exists operations -- one for each srcIp variable
  // It may be more efficent to first create a bdd representing the AND of
  // all variabels and then call exists one time on this bdd.
  /* private BDD existensialQuantifySrcIp(BDD bdd) {
    BDDInteger srcip = _bddPkt.getSrcIp();
    for (int i = 0; i < srcip.getBitvec().length; i++) {
      bdd = bdd.exist(_bddPkt.getSrcIp().getBitvec()[i]);
    }
    return bdd;
  } */

  private void computeAclPredicates() {
    // Collect all the acls
    List<IpAccessList> acls = new ArrayList<>();
    List<Boolean> isOutbound = new ArrayList<>();
    List<GraphLink> links = new ArrayList<>();

    for (Entry<GraphLink, IpAccessList> e : _inAcls.entrySet()) {
      acls.add(e.getValue());
      isOutbound.add(false);
      links.add(e.getKey());
    }

    for (Entry<GraphLink, IpAccessList> e : _outAcls.entrySet()) {
      acls.add(e.getValue());
      isOutbound.add(true);
      links.add(e.getKey());
    }

    // Sort ACLs increasing by size
    acls.sort(Comparator.comparingInt(a -> a.getLines().size()));

    List<Atom> atoms = new ArrayList<>();
    for (int i = 0; i < acls.size(); i++) {
      IpAccessList acl = acls.get(i);
      BDDAcl aclBdd = BDDAcl.create(null, acl, false);
      atoms.add(new Atom(i, aclBdd.getBdd()));
    }

    Set<Atom> allPredicates = computeAtomicPredicates(atoms);

    // Create bitsets for each interface
    int i = 0;
    for (Atom pred : allPredicates) {
      _aclBdds.add(pred.getBdd());
      for (Integer label : pred.getLabels()) {
        Boolean out = isOutbound.get(label);
        GraphLink nip = links.get(label);
        if (out) {
          BitSet b = _aclOutPredicates.computeIfAbsent(nip, k -> new BitSet());
          b.set(i);
          _aclOutPredicates.put(nip, b);
        } else {
          BitSet b = _aclInPredicates.computeIfAbsent(nip, k -> new BitSet());
          b.set(i);
          _aclInPredicates.put(nip, b);
        }
      }
      i++;
    }
  }

  private void computeForwardingPredicates() {
    // create a BDD for each port
    List<Atom> atoms = new ArrayList<>();
    List<GraphLink> links = new ArrayList<>();

    for (Entry<String, Map<String, SortedSet<FibRow>>> entry : _dataPlane.getFibs().entrySet()) {
      String router = entry.getKey();
      // for each router, compute the
      List<Tuple<GraphLink, FibRow>> fibsForRouter = new ArrayList<>();

      Set<GraphLink> allLinks = new HashSet<>();
      for (Entry<String, SortedSet<FibRow>> entry2 : entry.getValue().entrySet()) {
        SortedSet<FibRow> fibs = entry2.getValue();
        for (FibRow fib : fibs) {
          NodeInterfacePair nip = new NodeInterfacePair(router, fib.getInterface());
          GraphLink link = _linkMap.get(nip);
          fibsForRouter.add(new Tuple<>(link, fib));
          // System.out.println(fib + "  for " + link);
          allLinks.add(link);
        }
      }

      // sort by decreasing prefix length
      fibsForRouter.sort(
          (t1, t2) -> {
            int len1 = t1.getSecond().getPrefix().getPrefixLength();
            int len2 = t2.getSecond().getPrefix().getPrefixLength();
            return len2 - len1;
          });

      // Compute the BDDs
      Map<GraphLink, BDD> fwdPredicates = new HashMap<>(allLinks.size());
      for (GraphLink nip : allLinks) {
        fwdPredicates.put(nip, BDDPacket.factory.zero());
      }
      BDD fwd = BDDPacket.factory.zero();
      for (Tuple<GraphLink, FibRow> tup : fibsForRouter) {
        GraphLink link = tup.getFirst();
        FibRow fib = tup.getSecond();
        BDD port = fwdPredicates.get(link);
        BDD pfx = destinationIpInPrefix(fib.getPrefix());
        BDD newPort = port.or(pfx.and(fwd.not()));
        fwd = fwd.or(pfx);
        fwdPredicates.put(link, newPort);
      }

      // create the atoms
      for (Entry<GraphLink, BDD> e : fwdPredicates.entrySet()) {
        GraphLink nip = e.getKey();
        BDD bdd = e.getValue();
        Atom a = new Atom(atoms.size(), bdd);
        atoms.add(a);
        links.add(nip);
      }
    }

    Set<Atom> disjointAtoms = computeAtomicPredicates(atoms);

    // Create bitsets for each interface
    int i = 0;
    for (Atom pred : disjointAtoms) {
      _forwardingBdds.add(pred.getBdd());
      for (Integer label : pred.getLabels()) {
        GraphLink nip = links.get(label);
        BitSet b = _forwardingPredicates.computeIfAbsent(nip, k -> new BitSet());
        b.set(i);
        _forwardingPredicates.put(nip, b);
      }
      i++;
    }
  }

  /*
   * Does the 32 bit integer match the prefix using lpm?
   * Here the 32 bits are all symbolic variables
   */
  private BDD destinationIpInPrefix(Prefix p) {
    BDD[] bits = _bddPkt.getDstIp().getBitvec();
    BitSet b = p.getStartIp().getAddressBits();
    BDD acc = BDDPacket.factory.one();
    for (int i1 = 0; i1 < p.getPrefixLength(); i1++) {
      boolean res = b.get(i1);
      if (res) {
        acc = acc.and(bits[i1]);
      } else {
        acc = acc.and(bits[i1].not());
      }
    }
    return acc;
  }

  // P<l>        and {R1<ls1>, ..., Rn<lsn>}  == {(P and R1)<l::ls1>, ..., (P and Rn)<l::lsn>}
  // (not p)<l>  and {R1<ls1>, ..., Rn<lsn>}  ==
  @Nonnull
  private Set<Atom> computeAtomicPredicates(List<Atom> atoms) {
    Set<Atom> allPredicates = new HashSet<>();
    if (!atoms.isEmpty()) {
      Atom initial = atoms.get(0);
      Atom initialNot = new Atom(initial.getBdd().not());
      allPredicates.add(initial);
      allPredicates.add(initialNot);
      for (int i = 1; i < atoms.size(); i++) {
        Atom current = atoms.get(i);
        Set<Atom> newBdds = new HashSet<>();
        for (Atom pred : allPredicates) {
          BDD x = pred.getBdd().and(current.getBdd());
          if (!x.isZero()) {
            Set<Integer> labels = new HashSet<>(pred.getLabels());
            labels.addAll(current.getLabels());
            Atom a = new Atom(labels, x);
            newBdds.add(a);
          }
        }
        if (!newBdds.isEmpty()) {
          for (Atom pred : allPredicates) {
            BDD x = pred.getBdd().and(current.getBdd().not());
            if (!x.isZero()) {
              Atom a = new Atom(pred.getLabels(), x);
              newBdds.add(a);
            }
          }
          allPredicates = newBdds;
        }
      }
    }
    return allPredicates;
  }

  /*
   * Return an example of a flow satisfying the user's query.
   * This will be the standard FlowHistory object for reachability.
   * Finds all relevant equivalence classes and checks reachability on
   * them each in turn.
   */
  public AnswerElement reachable(
      HeaderSpace h, Set<ForwardingAction> actions, Set<String> src, Set<String> dst) {

    long l = System.currentTimeMillis();

    Set<GraphNode> sources = new HashSet<>();
    Set<GraphNode> sinks = new HashSet<>();
    for (String s : src) {
      sources.add(_nodeMap.get(s));
    }
    for (String d : dst) {
      sinks.add(_nodeMap.get(d));
    }

    BitSet flags = actionFlags(actions);

    // Find the relevant labels for the headerspace
    BDD query = _bddPkt.fromHeaderSpace(h);

    List<Tuple<Flow, FlowTrace>> traces = new ArrayList<>();
    for (GraphNode source : sources) {
      Tuple<Flow, FlowTrace> trace = reachable(query, flags, source, sinks);
      if (trace != null) {
        traces.add(trace);
      }
    }

    System.out.println("Reachability time: " + (System.currentTimeMillis() - l));
    return createReachabilityAnswer(traces);
  }

  /*
   * Convert a set of forwarding actions to a bitset, so that
   * we can check if flags exist more quickly when traversing
   * the forwarding graph.
   */
  private BitSet actionFlags(Set<ForwardingAction> actions) {
    BitSet actionFlags = new BitSet();
    boolean accept = actions.contains(ForwardingAction.ACCEPT);
    boolean drop = actions.contains(ForwardingAction.DROP);
    boolean dropAclIn = actions.contains(ForwardingAction.DROP_ACL_IN);
    boolean dropAclOut = actions.contains(ForwardingAction.DROP_ACL_OUT);
    boolean dropAcl = actions.contains(ForwardingAction.DROP_ACL);
    boolean dropNullRoute = actions.contains(ForwardingAction.DROP_NULL_ROUTE);
    boolean dropNoRoute = actions.contains(ForwardingAction.DROP_NO_ROUTE);

    if (accept) {
      actionFlags.set(ACCEPT_FLAG);
    }
    if (drop) {
      actionFlags.set(DROP_FLAG);
    }
    if (dropAcl) {
      actionFlags.set(DROP_ACL_FLAG);
    }
    if (dropAclIn) {
      actionFlags.set(DROP_ACL_IN_FLAG);
    }
    if (dropAclOut) {
      actionFlags.set(DROP_ACL_OUT_FLAG);
    }
    if (dropNullRoute) {
      actionFlags.set(DROP_NULL_ROUTE_FLAG);
    }
    if (dropNoRoute) {
      actionFlags.set(DROP_NO_ROUTE_FLAG);
    }

    return actionFlags;
  }

  /*
   * Create a reachability answer element for compatibility
   * with the standard Batfish reachability question.
   */
  private Tuple<Flow, FlowTrace> createTrace(HeaderSpace h, Tuple<Path, FlowDisposition> tup) {
    Flow.Builder b = new Flow.Builder();
    b.setIngressNode(tup.getFirst().getSource().getName());
    if (!h.getSrcIps().isEmpty()) {
      b.setSrcIp(h.getSrcIps().first().getIp());
    }
    if (!h.getDstIps().isEmpty()) {
      b.setDstIp(h.getDstIps().first().getIp());
    }
    if (!h.getSrcPorts().isEmpty()) {
      b.setSrcPort(h.getSrcPorts().first().getStart());
    }
    if (!h.getDstPorts().isEmpty()) {
      b.setDstPort(h.getDstPorts().first().getStart());
    }
    if (!h.getIpProtocols().isEmpty()) {
      b.setIpProtocol(h.getIpProtocols().first());
    }
    if (!h.getIcmpTypes().isEmpty()) {
      b.setIcmpType(h.getIcmpTypes().first().getStart());
    }
    if (!h.getIcmpCodes().isEmpty()) {
      b.setIcmpCode(h.getIcmpCodes().first().getStart());
    }

    if (!h.getTcpFlags().isEmpty()) {
      TcpFlags flags = h.getTcpFlags().get(0);
      int tcpCwr = flags.getCwr() ? 1 : 0;
      int tcpEce = flags.getEce() ? 1 : 0;
      int tcpUrg = flags.getUrg() ? 1 : 0;
      int tcpAck = flags.getAck() ? 1 : 0;
      int tcpPsh = flags.getPsh() ? 1 : 0;
      int tcpRst = flags.getRst() ? 1 : 0;
      int tcpSyn = flags.getSyn() ? 1 : 0;
      int tcpFin = flags.getFin() ? 1 : 0;
      b.setTcpFlagsCwr(tcpCwr);
      b.setTcpFlagsEce(tcpEce);
      b.setTcpFlagsUrg(tcpUrg);
      b.setTcpFlagsAck(tcpAck);
      b.setTcpFlagsPsh(tcpPsh);
      b.setTcpFlagsRst(tcpRst);
      b.setTcpFlagsSyn(tcpSyn);
      b.setTcpFlagsFin(tcpFin);
    }

    b.setTag("ATOMIC");

    Flow flow = b.build();

    String note;
    Path path = tup.getFirst();
    List<GraphLink> links = new ArrayList<>(path.getLinks());
    Collections.reverse(links);

    FlowDisposition fd = tup.getSecond();
    if (fd == FlowDisposition.DENIED_OUT || fd == FlowDisposition.DENIED_IN) {
      GraphLink lastLink = path.getLinks().get(0);
      IpAccessList acl;
      if (fd == FlowDisposition.DENIED_IN) {
        acl = _inAcls.get(_otherEnd.get(lastLink));
      } else {
        acl = _outAcls.get(lastLink);
      }

      FilterResult fr = acl.filter(flow);
      String line = "default deny";
      if (fr.getMatchLine() != null) {
        line = acl.getLines().get(fr.getMatchLine()).getName();
      }
      String type = (fd == FlowDisposition.DENIED_OUT) ? "OUT" : "IN";
      note = String.format("DENIED_%s{%s}{%s}", type, acl.getName(), line);
    } else {
      note = fd.name();
    }

    Map<String, Configuration> configs = _batfish.loadConfigurations();

    List<FlowTraceHop> hops = new ArrayList<>();

    // We want to skip over ACL nodes when displaying the path
    List<GraphNode> routers = new ArrayList<>();
    List<String> ifaces = new ArrayList<>();
    for (GraphLink link : links) {
      GraphNode src = link.getSource();
      GraphNode tgt = link.getTarget();
      String srcIface = link.getSourceIface();
      String tgtIface = link.getTargetIface();
      routers.add(src);
      ifaces.add(srcIface);
      routers.add(tgt);
      ifaces.add(tgtIface);
    }

    for (int i = 0; i < routers.size() - 1; i++) {
      GraphNode src = routers.get(i);
      GraphNode tgt = routers.get(i + 1);
      String srcIface = ifaces.get(i);
      String tgtIface = ifaces.get(i + 1);
      if (src.getName().equals(tgt.getName())) {
        continue;
      }
      Edge edge = new Edge(src.getName(), srcIface, tgt.getName(), tgtIface);
      SortedSet<String> routeStrings = new TreeSet<>();
      Configuration config = configs.get(src.getName());
      IRib<AbstractRoute> rib =
          _dataPlane.getRibs().get(src.getName()).get(config.getDefaultVrf().getName());
      Set<AbstractRoute> routes = rib.longestPrefixMatch(flow.getDstIp());
      for (AbstractRoute route : routes) {
        String protoName = route.getProtocol().protocolName();
        protoName = Character.toString(protoName.charAt(0)).toUpperCase() + protoName.substring(1);
        String s =
            String.format(
                "%sRoute<%s,nhip:%s,nhint:%s>_fnhip:???????",
                protoName,
                route.getNetwork(),
                route.getNextHopIp().toString(),
                route.getNextHopInterface());
        routeStrings.add(s);
      }
      FlowTraceHop hop = new FlowTraceHop(edge, routeStrings, null);
      hops.add(hop);
    }

    return new Tuple<>(flow, new FlowTrace(fd, hops, note));
  }

  private AnswerElement createReachabilityAnswer(List<Tuple<Flow, FlowTrace>> traces) {
    FlowHistory fh = new FlowHistory();
    String testRigName = _batfish.getTestrigName();
    Environment environment =
        new Environment(
            "BASE", testRigName, new TreeSet<>(), null, null, null, null, new TreeSet<>());
    for (Tuple<Flow, FlowTrace> tup : traces) {
      Flow flow = tup.getFirst();
      FlowTrace trace = tup.getSecond();
      fh.addFlowTrace(flow, "BASE", environment, trace);
    }
    return fh;
  }

  /*
   * Check reachability for an individual equivalence class.
   * Depending on the action requested from the query, it will
   * stop the search when it has found a relevant path and return it.
   * Will return null if it found no path.
   */
  @Nullable
  private Tuple<Flow, FlowTrace> reachable(
      BDD query, BitSet flags, GraphNode source, Set<GraphNode> sinks) {

    Map<GraphLink, List<PortReachabilitySummary>> outboundSummaries = new HashMap<>();
    Map<GraphLink, List<PortReachabilitySummary>> inboundSummaries = new HashMap<>();

    // Create the initial summary
    Path p = new Path(PList.empty(), source, source);
    BitSet fwd = new BitSet(_forwardingBdds.size());
    BitSet acl = new BitSet(_aclBdds.size());
    fwd.set(0, _forwardingBdds.size());
    acl.set(0, _aclBdds.size());
    PortReachabilitySummary summary = new PortReachabilitySummary(p, fwd, acl);

    Stack<Tuple<GraphNode, PortReachabilitySummary>> stack = new Stack<>();
    stack.add(new Tuple<>(source, summary));

    while (!stack.isEmpty()) {
      Tuple<GraphNode, PortReachabilitySummary> tup = stack.pop();
      GraphNode current = tup.getFirst();
      PortReachabilitySummary s = tup.getSecond();

      // System.out.println("Looking at: " + current);
      // System.out.println("Path is: " + s.getPath().getLinks());
      // System.out.println("Fwd bits: " + s.getForwarding());
      // System.out.println("Acl bits: " + s.getAcl());

      for (GraphLink link : _adjacencyLists.get(current.getIndex())) {
        // TODO: make sure the link is active etc

        // System.out.println("  Extend by link: " + link);

        BitSet fwdLink = _forwardingPredicates.get(link);
        BitSet aclLink = _aclOutPredicates.get(link);

        // For forwarding, null means no FIB, so we use the empty bitset
        fwdLink = (fwdLink == null ? new BitSet() : fwdLink);
        // For acls, null means there is no acl, so we use the full bitset
        aclLink = (aclLink == null ? acl : aclLink);
        PortReachabilitySummary afterOut = s.extendBy(link).applyPort(fwdLink, aclLink);
        // If an inbound acl -- i.e., a real router on the other side with an inbound ACL
        GraphLink other = _otherEnd.get(link);
        // System.out.println("  Other link: " + other);
        BitSet aclIn = (other == null ? acl : _aclInPredicates.get(other));
        // System.out.println("  Other ACL in: " + aclIn);
        aclIn = (aclIn == null ? acl : aclIn);
        PortReachabilitySummary afterIn = afterOut.applyPort(fwd, aclIn);
        // System.out.println("  After ACL in: " + afterIn.getAcl());

        GraphNode neighbor = link.getTarget();
        boolean notLoop = !s.getPath().containsNode(neighbor);
        boolean nonEmptyOut =
            !afterOut.getForwarding().isEmpty()
                && (!afterOut.getAcl().isEmpty() || _aclBdds.isEmpty());
        boolean nonEmptyIn =
            !afterIn.getForwarding().isEmpty()
                && (!afterIn.getAcl().isEmpty() || _aclBdds.isEmpty());

        // Add the neighbor
        if (notLoop && nonEmptyIn) {
          stack.push(new Tuple<>(neighbor, afterIn));
        }

        // Update the reachability information
        if (nonEmptyOut) {
          List<PortReachabilitySummary> info =
              outboundSummaries.computeIfAbsent(link, k -> new ArrayList<>());
          info.add(afterOut);
        }
        if (nonEmptyIn && other != null) {
          List<PortReachabilitySummary> info =
              inboundSummaries.computeIfAbsent(other, k -> new ArrayList<>());
          info.add(afterIn);
        }
      }
    }

    return analyzeSummaries(query, outboundSummaries, inboundSummaries, flags, sinks);
    // return analyzeSummaries(query, summaries);
  }

  @Nullable
  private Tuple<Flow, FlowTrace> analyzeSummaries(
      BDD query,
      Map<GraphLink, List<PortReachabilitySummary>> outboundSummaries,
      Map<GraphLink, List<PortReachabilitySummary>> inboundSummaries,
      BitSet flags,
      Set<GraphNode> sinks) {

    for (GraphNode sink : sinks) {

      List<GraphLink> local = _adjacencyLists.get(sink.getIndex());
      List<GraphLink> adjacent = new ArrayList<>();

      for (GraphLink link : local) {
        GraphLink adj = _otherEnd.get(link);
        if (adj != null) {
          adjacent.add(adj);
        }
      }

      Map<GraphLink, BDD> reachableIn = new HashMap<>();
      Map<GraphLink, BDD> reachableOut = new HashMap<>();

      Map<GraphLink, PortReachabilitySummary> nonzeroIn = new HashMap<>();
      Map<GraphLink, PortReachabilitySummary> nonzeroOut = new HashMap<>();

      computeBddSummaries(query, inboundSummaries, local, reachableIn, nonzeroIn);
      computeBddSummaries(query, inboundSummaries, adjacent, reachableIn, nonzeroIn);
      computeBddSummaries(query, outboundSummaries, local, reachableOut, nonzeroOut);
      computeBddSummaries(query, outboundSummaries, adjacent, reachableOut, nonzeroOut);

      // All traffic leaving from some neighbor
      BDD exitFromNeighbor = BDDPacket.factory.zero();
      for (GraphLink link : adjacent) {
        BDD x = reachableOut.get(link);
        if (x != null) {
          exitFromNeighbor = exitFromNeighbor.or(x);
        }
      }

      // All traffic that enters
      BDD enterFromNeighbor = BDDPacket.factory.zero();
      for (GraphLink link : local) {
        BDD x = reachableIn.get(link);
        if (x != null) {
          enterFromNeighbor = enterFromNeighbor.or(x);
        }
      }

      // All traffic that leaves to another neighbor
      BDD exitsToNeighbor = BDDPacket.factory.zero();
      for (GraphLink link : local) {
        BDD x = reachableIn.get(link);
        if (x != null) {
          exitsToNeighbor = exitsToNeighbor.or(x);
        }
      }

      boolean checkForNoRoute = flags.get(DROP_FLAG) || flags.get(DROP_NO_ROUTE_FLAG);
      boolean checkForNullRouted = flags.get(DROP_FLAG) || flags.get(DROP_NULL_ROUTE_FLAG);
      boolean checkForAccept = flags.get(ACCEPT_FLAG);
      boolean checkForDeniedIn =
          flags.get(DROP_FLAG) || flags.get(DROP_ACL_FLAG) || flags.get(DROP_ACL_IN_FLAG);
      boolean checkForDeniedOut =
          flags.get(DROP_FLAG) || flags.get(DROP_ACL_FLAG) || flags.get(DROP_ACL_OUT_FLAG);

      if (checkForNoRoute && !enterFromNeighbor.isZero() && exitsToNeighbor.isZero()) {
        HeaderSpace h = _bddPkt.toHeaderSpace(enterFromNeighbor);
        Path p = nonzeroIn.values().iterator().next().getPath();
        return createTrace(h, new Tuple<>(p, FlowDisposition.NO_ROUTE));
      }

      if (checkForAccept) {
        for (GraphLink link : local) {
          String neighbor = link.getTarget().getName();
          if (neighbor.equals("(egress)")) {
            BDD reach = reachableOut.get(link);
            if (!reach.isZero()) {
              HeaderSpace h = _bddPkt.toHeaderSpace(reach);
              Path p = nonzeroOut.get(link).getPath();
              return createTrace(h, new Tuple<>(p, FlowDisposition.ACCEPTED));
            }
          }
        }
      }

      if (checkForNullRouted) {
        for (GraphLink link : local) {
          String iface = link.getSourceIface();
          if (iface.equals("null_interface")) {
            BDD reach = reachableOut.get(link);
            if (!reach.isZero()) {
              HeaderSpace h = _bddPkt.toHeaderSpace(reach);
              Path p = nonzeroOut.get(link).getPath();
              return createTrace(h, new Tuple<>(p, FlowDisposition.NULL_ROUTED));
            }
          }
        }
      }

      if (checkForDeniedIn) {
        for (GraphLink link : local) {
          GraphLink other = _otherEnd.get(link);
          if (other != null) {
            BDD reachIn = reachableIn.get(link);
            BDD reachOut = reachableOut.get(other);
            BDD diff = reachOut.and(reachIn.not());
            if (!diff.isZero()) {
              HeaderSpace h = _bddPkt.toHeaderSpace(diff);
              Path p = nonzeroIn.get(link).getPath();
              return createTrace(h, new Tuple<>(p, FlowDisposition.DENIED_IN));
            }
          }
        }
      }

      if (checkForDeniedOut) {
        for (GraphLink link : local) {
          GraphLink other = _otherEnd.get(link);
          if (other != null) {
            BDD reachOut = reachableOut.get(link);
            BDD diff = reachOut.and(enterFromNeighbor.not());
            if (!diff.isZero()) {
              HeaderSpace h = _bddPkt.toHeaderSpace(diff);
              PortReachabilitySummary summary = nonzeroIn.values().iterator().next();
              summary = summary.extendBy(link);
              return createTrace(h, new Tuple<>(summary.getPath(), FlowDisposition.DENIED_OUT));
            }
          }
        }
      }
    }
    return null;
  }

  private void computeBddSummaries(
      BDD query,
      Map<GraphLink, List<PortReachabilitySummary>> summaries,
      List<GraphLink> links,
      Map<GraphLink, BDD> reachable,
      Map<GraphLink, PortReachabilitySummary> nonzero) {
    for (GraphLink link : links) {
      List<PortReachabilitySummary> s = summaries.get(link);
      if (s != null) {
        BDD atLink = BDDPacket.factory.zero();
        for (PortReachabilitySummary portSummary : s) {
          // System.out.println("FWD summary for " + link + " as " + portSummary.getForwarding());
          // System.out.println("ACL summary for " + link + " as " + portSummary.getAcl());
          BitSet f = portSummary.getForwarding();
          BitSet a = portSummary.getAcl();
          BDD withQuery = bddFromSummary(query, f, a);
          if (!withQuery.isZero()) {
            nonzero.put(link, portSummary);
          }
          atLink = atLink.or(withQuery);
        }
        reachable.put(link, atLink);
      } else {
        reachable.put(link, BDDPacket.factory.zero());
      }
    }
  }

  private BDD bddFromSummary(BDD query, BitSet f, BitSet a) {
    BDD acc1 = BDDPacket.factory.zero();
    for (int i = f.nextSetBit(0); i >= 0; i = f.nextSetBit(i + 1)) {
      BDD fwd = _forwardingBdds.get(i);
      acc1 = acc1.or(fwd);
    }
    if (_aclBdds.isEmpty()) {
      return query.and(acc1);
    }
    BDD acc2 = BDDPacket.factory.zero();
    for (int i = a.nextSetBit(0); i >= 0; i = a.nextSetBit(i + 1)) {
      BDD acl = _aclBdds.get(i);
      acc2 = acc2.or(acl);
    }
    return query.and(acc1.and(acc2));
  }
}
