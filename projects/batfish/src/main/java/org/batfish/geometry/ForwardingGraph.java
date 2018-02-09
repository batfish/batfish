package org.batfish.geometry;

import com.google.common.base.Objects;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.main.Batfish;
import org.batfish.symbolic.utils.Tuple;

/*
 * Attempt to encode the dataplane rules as a collection of
 * hyper-rectangles in a high-dimensional space.
 */
public class ForwardingGraph {

  // Equivalence classes indexed from 0
  private ArrayList<HyperRectangle> _ecs;

  // Edges labelled with equivalence classes
  private Map<GraphLink, BitSet> _labels;

  // Priority rules for each router, equivalence class pair
  private Map<Integer, Map<GraphNode, NavigableSet<Rule>>> _ownerMap;

  // Efficient searching for equivalence class overlap
  private KDTree _kdtree;

  // Map from routers to graph nodes in this extended graph
  private Map<String, GraphNode> _nodeMap;

  // Map from ACLs to graph nodes in this extended graph
  private Map<String, AclGraphNode> _aclMap;

  // Map from interfaces to links in this extened graph
  private Map<NodeInterfacePair, GraphLink> _linkMap;

  // All the nodes in the graph
  private List<GraphNode> _allNodes;

  // All the links in the graph
  private List<GraphLink> _allLinks;

  // Adjacency list for the graph
  private Map<GraphNode, List<GraphLink>> _adjacencyLists;

  public ForwardingGraph(Batfish batfish, DataPlane dp) {
    long t = System.currentTimeMillis();
    initGraph(batfish, dp);

    HyperRectangle fullRange = GeometricSpace.fullSpace();
    fullRange.setAlphaIndex(0);
    _ecs = new ArrayList<>();
    _ecs.add(fullRange);
    _labels = new HashMap<>();
    _ownerMap = new HashMap<>();
    _kdtree = new KDTree(GeometricSpace.NUM_FIELDS);
    _kdtree.insert(fullRange);

    // initialize the labels
    for (GraphLink link : _allLinks) {
      _labels.put(link, new BitSet());
    }

    // initialize owners
    Map<GraphNode, NavigableSet<Rule>> map = new HashMap<>();
    _allNodes.forEach(r -> map.put(r, new TreeSet<>()));
    _ownerMap.put(0, map);

    // add the FIB rules
    List<Rule> rules = new ArrayList<>();
    for (Entry<String, Map<String, SortedSet<FibRow>>> entry : dp.getFibs().entrySet()) {
      String router = entry.getKey();
      for (Entry<String, SortedSet<FibRow>> entry2 : entry.getValue().entrySet()) {
        SortedSet<FibRow> fibs = entry2.getValue();
        for (FibRow fib : fibs) {
          Rule r = createFibRule(router, fib);
          rules.add(r);
        }
      }
    }

    // add the ACL rules
    for (AclGraphNode aclNode : _aclMap.values()) {
      List<GraphLink> links = _adjacencyLists.get(aclNode);
      GraphLink drop = links.get(0);
      GraphLink accept = links.get(1);
      List<IpAccessListLine> lines = aclNode.getAcl().getLines();
      int i = lines.size();
      for (IpAccessListLine aclLine : aclNode.getAcl().getLines()) {
        Rule r = createAclRule(aclLine, drop, accept, i);
        rules.add(r);
        i--;
      }
    }

    // Deterministically shuffle the input to get a better balanced KD tree
    Random rand = new Random(7);
    Collections.shuffle(rules, rand);
    for (Rule rule : rules) {
      addRule(rule);
    }

    System.out.println("Total time was: " + (System.currentTimeMillis() - t));
    System.out.println("Number of classes: " + (_ecs.size()));

    /* HeaderSpace h = new HeaderSpace();
    List<IpWildcard> wcs = new ArrayList<>();
    Ip ip = new Ip("70.0.2.0");
    Prefix p = new Prefix(ip, 32);
    IpWildcard wc = new IpWildcard(p);
    wcs.add(wc);
    h.setDstIps(wcs);
    HyperRectangle counterExample = reachable(h, "edge-4", "edge-2");
    System.out.println("Counter Example: " + counterExample); */
  }

  private Rule createFibRule(String router, FibRow fib) {
    NodeInterfacePair nip = new NodeInterfacePair(router, fib.getInterface());
    GraphLink link = _linkMap.get(nip);
    Prefix p = fib.getPrefix();
    long start = p.getStartIp().asLong();
    long end = p.getEndIp().asLong() + 1;
    HyperRectangle hr = GeometricSpace.fullSpace();
    hr.getBounds()[0] = start;
    hr.getBounds()[1] = end;
    return new Rule(link, hr, fib.getPrefix().getPrefixLength());
  }

  private Rule createAclRule(
      IpAccessListLine aclLine, GraphLink drop, GraphLink accept, int priority) {
    HyperRectangle rect = GeometricSpace.fromAcl(aclLine);
    GraphLink link = (aclLine.getAction() == LineAction.ACCEPT ? accept : drop);
    return new Rule(link, rect, priority);
  }

  private String getAclName(String router, String ifaceName, IpAccessList acl, boolean in) {
    return "ACL-" + router + "-" + (in ? "IN-" : "OUT-") + ifaceName + "-" + acl.getName();
  }

  private void initGraph(Batfish batfish, DataPlane dp) {
    _nodeMap = new HashMap<>();
    _aclMap = new HashMap<>();
    _linkMap = new HashMap<>();
    _adjacencyLists = new HashMap<>();
    _allNodes = new ArrayList<>();
    _allLinks = new ArrayList<>();

    Map<String, Configuration> configs = batfish.loadConfigurations();

    // Create the nodes
    GraphNode dropNode = new GraphNode("(none)");
    _nodeMap.put("(none)", dropNode);
    for (Entry<String, Configuration> entry : configs.entrySet()) {
      String router = entry.getKey();
      Configuration config = entry.getValue();
      GraphNode node = new GraphNode(router);
      _nodeMap.put(router, node);
      _allNodes.add(node);
      // Create ACL nodes
      for (Entry<String, Interface> e : config.getInterfaces().entrySet()) {
        String ifaceName = e.getKey();
        Interface iface = e.getValue();
        IpAccessList outAcl = iface.getOutgoingFilter();
        if (outAcl != null) {
          String aclName = getAclName(router, ifaceName, outAcl, false);
          AclGraphNode aclNode = new AclGraphNode(aclName, outAcl);
          _aclMap.put(aclName, aclNode);
          _allNodes.add(aclNode);
        }
        IpAccessList inAcl = iface.getIncomingFilter();
        if (inAcl != null) {
          String aclName = getAclName(router, ifaceName, inAcl, true);
          AclGraphNode aclNode = new AclGraphNode(aclName, inAcl);
          _aclMap.put(aclName, aclNode);
          _allNodes.add(aclNode);
        }
      }
    }

    // Initialize the node adjacencies
    for (GraphNode node : _nodeMap.values()) {
      _adjacencyLists.put(node, new ArrayList<>());
    }
    // Initialize the node adjacencies
    for (GraphNode node : _aclMap.values()) {
      _adjacencyLists.put(node, new ArrayList<>());
    }

    Map<NodeInterfacePair, NodeInterfacePair> edgeMap = new HashMap<>();
    for (Edge edge : dp.getTopologyEdges()) {
      edgeMap.put(edge.getInterface1(), edge.getInterface2());
    }

    // add edges that don't have a neighbor on the other side
    NodeInterfacePair nullPair = new NodeInterfacePair("(none)", "null_interface");
    for (Entry<String, Configuration> entry : configs.entrySet()) {
      String router = entry.getKey();
      Configuration config = entry.getValue();
      for (Entry<String, Interface> e : config.getInterfaces().entrySet()) {
        NodeInterfacePair nip = new NodeInterfacePair(router, e.getKey());
        if (!edgeMap.containsKey(nip)) {
          edgeMap.put(nip, nullPair);
        }
      }
    }

    // Create the edges
    for (GraphNode aclNode : _aclMap.values()) {
      GraphLink nullLink = new GraphLink(aclNode, "null_interface", dropNode, "null_interface");
      _adjacencyLists.get(aclNode).add(nullLink);
      _allLinks.add(nullLink);
    }
    for (Entry<NodeInterfacePair, NodeInterfacePair> entry : edgeMap.entrySet()) {
      NodeInterfacePair nip1 = entry.getKey();
      NodeInterfacePair nip2 = entry.getValue();

      // Add a special null edge
      GraphNode src = _nodeMap.get(nip1.getHostname());
      GraphLink nullLink = new GraphLink(src, "null_interface", dropNode, "null_interface");
      _linkMap.put(new NodeInterfacePair(nip1.getHostname(), "null_interface"), nullLink);
      _allLinks.add(nullLink);

      String router1 = nip1.getHostname();
      String router2 = nip2.getHostname();
      Configuration config1 = configs.get(router1);
      Configuration config2 = configs.get(router2);
      String ifaceName1 = nip1.getInterface();
      String ifaceName2 = nip2.getInterface();
      Interface iface1 = config1.getInterfaces().get(ifaceName1);
      Interface iface2 = config2 == null ? null : config2.getInterfaces().get(ifaceName2);
      IpAccessList outAcl = iface1.getOutgoingFilter();
      IpAccessList inAcl = iface2 == null ? null : iface2.getIncomingFilter();

      if (outAcl != null) {
        // add a link to the ACL
        String outAclName = getAclName(router1, ifaceName1, outAcl, false);
        GraphNode tgt1 = _aclMap.get(outAclName);
        GraphLink l1 = new GraphLink(src, ifaceName1, tgt1, "enter-outbound-acl");
        _linkMap.put(nip1, l1);
        _adjacencyLists.get(src).add(l1);
        _allLinks.add(l1);
        // if inbound acl, then add that
        if (inAcl != null) {
          String inAclName = getAclName(router2, ifaceName2, inAcl, true);
          GraphNode tgt2 = _aclMap.get(inAclName);
          GraphLink l2 = new GraphLink(tgt1, "exit-outbound-acl", tgt2, "enter-inbound-acl");
          _adjacencyLists.get(tgt1).add(l2);
          _allLinks.add(l2);
          // add a link from ACL to peer
          GraphNode tgt3 = _nodeMap.get(router2);
          GraphLink l3 = new GraphLink(tgt2, "exit-inbound-acl", tgt3, ifaceName2);
          _adjacencyLists.get(tgt2).add(l3);
          _allLinks.add(l3);
        } else {
          // add a link from ACL to peer
          GraphNode tgt2 = _nodeMap.get(router2);
          GraphLink l2 = new GraphLink(tgt1, "exit-outbound-acl", tgt2, ifaceName2);
          _adjacencyLists.get(tgt1).add(l2);
          _allLinks.add(l2);
        }
      } else {
        if (inAcl != null) {
          String inAclName = getAclName(router2, ifaceName2, inAcl, true);
          GraphNode tgt1 = _aclMap.get(inAclName);
          GraphLink l1 = new GraphLink(src, ifaceName1, tgt1, "enter-inbound-acl");
          _linkMap.put(nip1, l1);
          _adjacencyLists.get(src).add(l1);
          _allLinks.add(l1);
          // add a link from ACL to peer
          GraphNode tgt2 = _nodeMap.get(router2);
          GraphLink l2 = new GraphLink(tgt1, "exit-inbound-acl", tgt2, ifaceName2);
          _adjacencyLists.get(tgt1).add(l2);
          _allLinks.add(l2);
        } else {
          GraphNode tgt = _nodeMap.get(router2);
          GraphLink l = new GraphLink(src, ifaceName1, tgt, ifaceName2);
          _linkMap.put(nip1, l);
          _adjacencyLists.get(src).add(l);
          _allLinks.add(l);
        }
      }
    }
  }

  /* private void showStatus() {
    System.out.println("=====================");
    for (int i = 0; i < _ecs.size(); i++) {
      HyperRectangle r = _ecs.get(i);
      System.out.println(i + " --> " + r);
    }
    System.out.println("=====================");
  } */

  /*
   * Check reachability and possibly return a
   * counterexample or null if all reachable
   */
  @Nullable
  public HeaderSpace reachable(HeaderSpace h, Set<String> src, Set<String> dst) {
    Set<GraphNode> sources = new HashSet<>();
    Set<GraphNode> sinks = new HashSet<>();
    for (String s : src) {
      sources.add(_nodeMap.get(s));
    }
    for (String d : dst) {
      sinks.add(_nodeMap.get(d));
    }
    Collection<HyperRectangle> space = HyperRectangle.fromHeaderSpace(h);
    for (HyperRectangle rect : space) {
      List<HyperRectangle> relevant = _kdtree.intersect(rect);
      for (HyperRectangle r : relevant) {
        boolean b = reachable(r.getAlphaIndex(), sources, sinks);
        if (!b) {
          return r.example();
        }
      }
    }
    return null;
  }

  private boolean reachable(int alphaIdx, Set<GraphNode> sources, Set<GraphNode> sinks) {
    Queue<GraphNode> todo = new ArrayDeque<>();
    Set<GraphNode> visited = new HashSet<>();
    todo.addAll(sources);
    while (!todo.isEmpty()) {
      GraphNode current = todo.remove();
      if (sinks.contains(current)) {
        return true;
      }
      visited.add(current);
      for (GraphLink link : _adjacencyLists.get(current)) {
        if (_labels.get(link).get(alphaIdx)) {
          GraphNode neighbor = link.getTarget();
          if (!visited.contains(neighbor)) {
            todo.add(neighbor);
          }
        }
      }
    }
    return false;
  }

  private Map<GraphNode, NavigableSet<Rule>> copyMap(Map<GraphNode, NavigableSet<Rule>> map) {
    Map<GraphNode, NavigableSet<Rule>> newMap = new HashMap<>(map.size());
    for (Entry<GraphNode, NavigableSet<Rule>> entry : map.entrySet()) {
      newMap.put(entry.getKey(), new TreeSet<>(entry.getValue()));
    }
    return newMap;
  }

  public void addRule(Rule r) {
    HyperRectangle hr = r.getRectangle();

    // showStatus();
    List<HyperRectangle> overlapping = new ArrayList<>();
    List<Tuple<HyperRectangle, HyperRectangle>> delta = new ArrayList<>();
    for (HyperRectangle other : _kdtree.intersect(hr)) {
      HyperRectangle overlap = hr.overlap(other);
      assert (overlap != null);
      Collection<HyperRectangle> newRects = other.divide(overlap);
      if (newRects == null) {
        overlapping.add(other);
      } else {
        _kdtree.delete(other);
        boolean first = true;
        for (HyperRectangle rect : newRects) {
          if (first && !rect.equals(other)) {
            other.setBounds(rect.getBounds());
            first = false;
            rect = other;
          } else {
            rect.setAlphaIndex(_ecs.size());
            _ecs.add(rect);
            delta.add(new Tuple<>(other, rect));
          }
          _kdtree.insert(rect);
          if (rect.equals(overlap)) {
            overlapping.add(rect);
          }
        }
      }
    }

    // create new rectangles
    for (Tuple<HyperRectangle, HyperRectangle> d : delta) {
      HyperRectangle alpha = d.getFirst();
      HyperRectangle alphaPrime = d.getSecond();
      Map<GraphNode, NavigableSet<Rule>> existing = _ownerMap.get(alpha.getAlphaIndex());
      _ownerMap.put(alphaPrime.getAlphaIndex(), copyMap(existing));
      for (Entry<GraphNode, NavigableSet<Rule>> entry : existing.entrySet()) {
        NavigableSet<Rule> bst = entry.getValue();
        if (!bst.isEmpty()) {
          Rule highestPriority = bst.descendingIterator().next();
          GraphLink link = highestPriority.getLink();
          _labels.get(link).set(alphaPrime.getAlphaIndex());
        }
      }
    }

    // Update data structures
    for (HyperRectangle alpha : overlapping) {
      Rule rPrime = null;
      NavigableSet<Rule> bst = _ownerMap.get(alpha.getAlphaIndex()).get(r.getLink().getSource());
      if (!bst.isEmpty()) {
        rPrime = bst.descendingIterator().next();
      }
      if (rPrime == null || rPrime.compareTo(r) < 0) {
        _labels.get(r.getLink()).set(alpha.getAlphaIndex());
        if (rPrime != null && !(Objects.equal(r.getLink(), rPrime.getLink()))) {
          _labels.get(rPrime.getLink()).set(alpha.getAlphaIndex(), false);
        }
      }
      bst.add(r);
    }
  }
}
