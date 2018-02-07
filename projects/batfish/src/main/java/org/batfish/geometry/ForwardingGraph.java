package org.batfish.geometry;

import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
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
 *
 */
public class ForwardingGraph {

  private static long LOWER_VALUE = 0;
  private static long UPPER_VALUE = (long) Math.pow(2, 32);

  private ArrayList<HyperRectangle> _ecs;

  private Map<NodeInterfacePair, BitSet> _labels;

  private Map<Integer, Map<String, NavigableSet<Rule>>> _ownerMap;

  private KDTree _kdtree;

  public ForwardingGraph(DataPlane dp) {
    long t = System.currentTimeMillis();
    // initialize
    HyperRectangle fullRange = new HyperRectangle(LOWER_VALUE, UPPER_VALUE, 0);
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
    for (Entry<String, Map<String, SortedSet<FibRow>>> entry : dp.getFibs().entrySet()) {
      String router = entry.getKey();
      for (Entry<String, SortedSet<FibRow>> entry2 : entry.getValue().entrySet()) {
        SortedSet<FibRow> fibs = entry2.getValue();
        for (FibRow fib : fibs) {
          NodeInterfacePair nip = new NodeInterfacePair(router, fib.getInterface());
          Rule r = new Rule(nip, fib);
          addRule(r);
        }
      }
    }

    System.out.println("Total time was: " + (System.currentTimeMillis() - t));
    System.out.println("Number of classes: " + (_ecs.size()));
    //for (Entry<NodeInterfacePair, BitSet> entry : _labels.entrySet()) {
    //  System.out.println("Link: " + entry.getKey());
    //  System.out.println("  num classes: " + entry.getValue().cardinality());
    //}
  }

  private void showStatus() {
    System.out.println("=====================");
    for (int i = 0; i <_ecs.size(); i++) {
      HyperRectangle r = _ecs.get(i);
      System.out.println(i + " --> " + r);
    }
    System.out.println("=====================");
  }

  public void addRule(Rule r) {
    //System.out.println(
    //    "Adding rule: " + r.getFib().getPrefix() + " at " + r.getFib().getInterface());
    Prefix p = r.getFib().getPrefix();
    long start = p.getStartIp().asLong();
    long end = p.getEndIp().asLong() + 1;
    HyperRectangle hr = new HyperRectangle(start, end, -1);

    // showStatus();
    // System.out.println("Adding rule for: " + r.getFib().getPrefix());
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
            other.setX1(rect.getX1());
            other.setX2(rect.getX2());
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
      // System.out.println("Got delta: " + d.getFirst() + " |-->" + d.getSecond());
      HyperRectangle alpha = d.getFirst();
      HyperRectangle alphaPrime = d.getSecond();
      Map<String, NavigableSet<Rule>> existing = _ownerMap.get(alpha.getAlphaIndex());
      // System.out.println("Adding index: " + alphaPrime.getAlphaIndex());
      // System.out.println("Result: " + _ecMap.get(alphaPrime.getAlphaIndex()));
      _ownerMap.put(alphaPrime.getAlphaIndex(), new HashMap<>(existing));
      for (Entry<String, NavigableSet<Rule>> entry : existing.entrySet()) {
        NavigableSet<Rule> bst = entry.getValue();
        // TODO: what is the right thing here? E.g., when there is no rule?
        if (!bst.isEmpty()) {
          Rule highestPriority = bst.descendingIterator().next();
          NodeInterfacePair link = highestPriority.getLink();
          _labels.get(link).set(alphaPrime.getAlphaIndex());
        }
      }
    }

    // showStatus();

    // Update data structures
    for (HyperRectangle alpha : overlapping) {
      // System.out.println("  overlapping: " + alpha.getRangeStart());
      Rule rPrime = null;

      // System.out.println("Retrieving index: " + alpha.getAlphaIndex());
      // System.out.println("Result: " + _ecMap.get(alpha.getAlphaIndex()));

      NavigableSet<Rule> bst = _ownerMap.get(alpha.getAlphaIndex()).get(r.getLink().getHostname());
      // System.out.println("  existing rules: " + bst.size());
      if (!bst.isEmpty()) {
        rPrime = bst.descendingIterator().next();
      }
      if (rPrime == null || rPrime.compareTo(r) > 0) {
        // System.out.println("  doing an update");
        _labels.get(r.getLink()).set(alpha.getAlphaIndex());
        if (rPrime != null && !(Objects.equal(r.getLink(), rPrime.getLink()))) {
          _labels.get(rPrime.getLink()).set(alpha.getAlphaIndex(), false);
        }
      }
      bst.add(r);
    }
  }
}
