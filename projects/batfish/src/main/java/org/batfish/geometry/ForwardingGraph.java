package org.batfish.geometry;

import com.google.common.base.Objects;
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
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.collections.FibRow;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.symbolic.utils.Tuple;

/*
 * Attempt to encode the dataplane rules as a collection of
 * hyper-rectangles in a high-dimensional space.
 */
public class ForwardingGraph {

  private static long DSTIP_LOW = 0;
  private static long DSTIP_HIGH = (long) Math.pow(2, 32);

  private ArrayList<HyperRectangle> _ecs;

  private Map<NodeInterfacePair, BitSet> _labels;

  private Map<Integer, Map<String, NavigableSet<Rule>>> _ownerMap;

  private KDTree _kdtree;

  public ForwardingGraph(DataPlane dp) {
    long t = System.currentTimeMillis();
    // initialize
    long[] bounds = {DSTIP_LOW, DSTIP_HIGH};
    HyperRectangle fullRange = new HyperRectangle(bounds, 0);
    _ecs = new ArrayList<>();
    _ecs.add(fullRange);
    _labels = new HashMap<>();
    _ownerMap = new HashMap<>();
    _kdtree = new KDTree();
    _kdtree.insert(fullRange);

    // get all the links
    Set<String> allRouters = new HashSet<>();
    Set<NodeInterfacePair> allLinks = new HashSet<>();
    for (Entry<String, Map<String, SortedSet<FibRow>>> entry : dp.getFibs().entrySet()) {
      String router = entry.getKey();
      allRouters.add(router);
      for (Entry<String, SortedSet<FibRow>> entry2 : entry.getValue().entrySet()) {
        SortedSet<FibRow> fibs = entry2.getValue();
        for (FibRow fib : fibs) {
          NodeInterfacePair nip = new NodeInterfacePair(router, fib.getInterface());
          allLinks.add(nip);
        }
      }
    }

    // initialize the labels
    for (NodeInterfacePair nip : allLinks) {
      _labels.put(nip, new BitSet());
    }

    // initialize owners
    Map<String, NavigableSet<Rule>> map = new HashMap<>();
    allRouters.forEach(r -> map.put(r, new TreeSet<>()));
    _ownerMap.put(0, map);

    // add the rules
    List<Rule> rules = new ArrayList<>();
    for (Entry<String, Map<String, SortedSet<FibRow>>> entry : dp.getFibs().entrySet()) {
      String router = entry.getKey();
      for (Entry<String, SortedSet<FibRow>> entry2 : entry.getValue().entrySet()) {
        SortedSet<FibRow> fibs = entry2.getValue();
        for (FibRow fib : fibs) {
          NodeInterfacePair nip = new NodeInterfacePair(router, fib.getInterface());
          Rule r = new Rule(nip, fib);
          rules.add(r);
        }
      }
    }

    // Deterministically shuffle to input to get a better balanced KD tree
    Random rand = new Random(7);
    Collections.shuffle(rules, rand);
    for (Rule rule : rules) {
      addRule(rule);
    }

    System.out.println("Total time was: " + (System.currentTimeMillis() - t));
    System.out.println("Number of classes: " + (_ecs.size()));
    // for (Entry<NodeInterfacePair, BitSet> entry : _labels.entrySet()) {
    //  System.out.println("Link: " + entry.getKey());
    //  System.out.println("  num classes: " + entry.getValue().cardinality());
    // }

    /* HeaderSpace h = new HeaderSpace();
    List<IpWildcard> wcs = new ArrayList<>();
    Ip ip = new Ip("70.0.100.0");
    Prefix p = new Prefix(ip, 32);
    IpWildcard wc = new IpWildcard(p);
    wcs.add(wc);
    h.setDstIps(wcs);
    showForwarding(h); */
  }

  private void showStatus() {
    System.out.println("=====================");
    for (int i = 0; i < _ecs.size(); i++) {
      HyperRectangle r = _ecs.get(i);
      System.out.println(i + " --> " + r);
    }
    System.out.println("=====================");
  }

  /* public void showForwarding(HeaderSpace h) {
    Collection<HyperRectangle> space = HyperRectangle.fromHeaderSpace(h);
    System.out.println("Got rectangles for headerspace: " + space);
    for (HyperRectangle rect : space) {
      System.out.println("From headerspace for 70.0.100.0: " + rect);
      List<HyperRectangle> relevant = _kdtree.intersect(rect);
      for (HyperRectangle r : relevant) {
        System.out.println("Relevant: " + r);
        _labels.forEach(
            (nip, labels) -> {
              if (labels.get(r.getAlphaIndex())) {
                System.out.println("Forwards: " + nip);
              }
            });
      }
    }
  } */

  private Map<String, NavigableSet<Rule>> copyMap(Map<String, NavigableSet<Rule>> map) {
    Map<String, NavigableSet<Rule>> newMap = new HashMap<>(map.size());
    for (Entry<String, NavigableSet<Rule>> entry : map.entrySet()) {
      newMap.put(entry.getKey(), new TreeSet<>(entry.getValue()));
    }
    return newMap;
  }

  public void addRule(Rule r) {
    // System.out.println(
    //    "Adding rule: " + r.getFib().getPrefix() + " at " + r.getFib().getInterface());
    Prefix p = r.getFib().getPrefix();
    long start = p.getStartIp().asLong();
    long end = p.getEndIp().asLong() + 1;
    long[] bounds = {start, end};
    HyperRectangle hr = new HyperRectangle(bounds);

    // showStatus();

    // System.out.println("Adding rule for: " + r.getFib().getPrefix() + " out " + r.getLink());
    // System.out.println("Rectangle: " + hr);

    // Find all of the overlapping intervals
    List<HyperRectangle> overlapping = new ArrayList<>();
    List<Tuple<HyperRectangle, HyperRectangle>> delta = new ArrayList<>();

    // System.out.println("Looking for overlapping rectangles...");
    for (HyperRectangle other : _kdtree.intersect(hr)) {
      HyperRectangle overlap = hr.overlap(other);
      // If there is an overlap
      if (overlap != null) {
        // System.out.println("  found overlap with: " + other);
        // System.out.println("  overlap is: " + overlap);
        Collection<HyperRectangle> newRects = other.divide(overlap);

        // If empty, then it is an exact match
        if (newRects.isEmpty()) {
          overlapping.add(other);
        } else {
          _kdtree.delete(other);
        }

        boolean first = true;
        for (HyperRectangle rect : newRects) {
          // System.out.println("  divided out: " + rect);
          // modify the other rectangle to reuse the atom number
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
    // System.out.println("Create new regions");
    for (Tuple<HyperRectangle, HyperRectangle> d : delta) {
      // System.out.println("  Got delta: " + d.getFirst() + " |-->" + d.getSecond());
      HyperRectangle alpha = d.getFirst();
      HyperRectangle alphaPrime = d.getSecond();

      Map<String, NavigableSet<Rule>> existing = _ownerMap.get(alpha.getAlphaIndex());

      _ownerMap.put(alphaPrime.getAlphaIndex(), copyMap(existing));
      for (Entry<String, NavigableSet<Rule>> entry : existing.entrySet()) {
        NavigableSet<Rule> bst = entry.getValue();
        // TODO: what is the right thing here? E.g., when there is no rule?
        if (!bst.isEmpty()) {
          Rule highestPriority = bst.descendingIterator().next();
          NodeInterfacePair link = highestPriority.getLink();
          // System.out.println("  Adding " + alphaPrime.getAlphaIndex() + " to link " + link);
          _labels.get(link).set(alphaPrime.getAlphaIndex());
        }
      }
    }

    // showStatus();

    // Update data structures
    // System.out.println("Updating labels");
    for (HyperRectangle alpha : overlapping) {
      // System.out.println("  overlapping: " + alpha);
      Rule rPrime = null;
      NavigableSet<Rule> bst = _ownerMap.get(alpha.getAlphaIndex()).get(r.getLink().getHostname());
      // System.out.println("  existing rules: " + bst.size());
      if (!bst.isEmpty()) {
        rPrime = bst.descendingIterator().next();
        // System.out.println(
        //    "  current best rule: " + rPrime.getLink() + " for " + rPrime.getFib().getPrefix());
      }
      if (rPrime == null || rPrime.compareTo(r) > 0) {
        // System.out.println("  doing an update");
        _labels.get(r.getLink()).set(alpha.getAlphaIndex());
        // System.out.println("  added " + alpha.getAlphaIndex() + " for link " + r.getLink());
        if (rPrime != null && !(Objects.equal(r.getLink(), rPrime.getLink()))) {
          // System.out.println(
          //    "  removed " + alpha.getAlphaIndex() + " from link " + rPrime.getLink());
          _labels.get(rPrime.getLink()).set(alpha.getAlphaIndex(), false);
        }
      }
      bst.add(r);
    }
  }
}
