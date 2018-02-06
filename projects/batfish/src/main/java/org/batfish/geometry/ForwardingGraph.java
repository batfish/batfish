package org.batfish.geometry;

import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.BitSet;
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

public class ForwardingGraph {

  private static long LOWER_VALUE = 0;
  private static long UPPER_VALUE = (long) Math.pow(2, 32) + 1;

  private int _nextIndex = 1;

  private Map<Integer, HyperRectangle> _ecMap;

  private Map<HyperRectangle, Integer> _rectangleMap;

  private Map<NodeInterfacePair, BitSet> _labels;

  private Map<Integer, Map<String, NavigableSet<Rule>>> _ownerMap;

  public ForwardingGraph(DataPlane dp) {
    long t = System.currentTimeMillis();
    // initialize
    HyperRectangle fullRange = new HyperRectangle(LOWER_VALUE, UPPER_VALUE, 0);
    // System.out.println("Initial lower: " + lower.getRangeStart());
    // System.out.println("Initial upper: " + upper.getRangeStart());
    _ecMap = new HashMap<>();
    _rectangleMap = new HashMap<>();
    _ecMap.put(0, fullRange);
    _rectangleMap.put(fullRange, 0);
    _labels = new HashMap<>();
    _ownerMap = new HashMap<>();

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
    System.out.println("Number of classes: " + (_nextIndex - 1));
    //for (Entry<NodeInterfacePair, BitSet> entry : _labels.entrySet()) {
    //  System.out.println("Link: " + entry.getKey());
    //  System.out.println("  num classes: " + entry.getValue().cardinality());
    //}
  }

  public void addRule(Rule r) {
    //System.out.println(
    //    "Adding rule: " + r.getFib().getPrefix() + " at " + r.getFib().getInterface());
    Prefix p = r.getFib().getPrefix();
    long start = p.getStartIp().asLong();
    long end = p.getEndIp().asLong();
    HyperRectangle hr = new HyperRectangle(start, end, -1);

    System.out.println("=====================");
    for (Entry<Integer, HyperRectangle> entry : _ecMap.entrySet()) {
      HyperRectangle rect = entry.getValue();
      BitSet b = rect.getDifference();
      System.out.println(entry.getKey() + " --> " + entry.getValue() + " - " + b);
    }
    System.out.println("=====================");

    System.out.println("Adding rule for: " + r.getFib().getPrefix());
    System.out.println("Rectangle: " + hr);

    // Add the new rectangle (or get the existing one for that range)
    boolean isSame = false;
    Integer i = _rectangleMap.get(hr);
    if (i == null) {
      hr.setAlphaIndex(_nextIndex);
      _rectangleMap.put(hr, _nextIndex);
      _ecMap.put(_nextIndex, hr);
      _nextIndex++;
    } else {
      hr = _ecMap.get(i);
      isSame = true;
    }

    System.out.println("Index of rectangle: " + hr.getAlphaIndex());

    // Find all of the overlapping intervals
    List<HyperRectangle> overlapping = new ArrayList<>();
    List<Tuple<HyperRectangle, HyperRectangle>> delta = new ArrayList<>();
    List<HyperRectangle> allRects = new ArrayList<>(_ecMap.values());

    System.out.println("Looking for overlapping rectangles...");
    for (HyperRectangle other : allRects) {
      HyperRectangle overlap = hr.overlap(other);
      // If there is an overlap
      if (overlap != null) {
        System.out.println("  found overlap with: " + other);
        boolean b1 = hr.isSubsumedBy(other);
        boolean b2 = other.isSubsumedBy(hr);
        if (b1 && b2) {
          System.out.println("  exact match, nothing to do");
        } else if (b1) {
          System.out.println("  other subsumes this rule");
          // Case:
          // ________________
          // |     other    |
          // |  __________  |
          // |  |        |  |
          // |  |   hr   |  |
          // |  |________|  |
          // |______________|
          //
          other.getDifference().set(hr.getAlphaIndex());
          if (!isSame) {
            delta.add(new Tuple<>(other, hr));
          }
          overlapping.add(hr);
          overlapping.add(other);
        } else if (b2) {
          System.out.println("  this rule subsumes other");
          // Case:
          // ________________
          // |      hr      |
          // |  __________  |
          // |  |        |  |
          // |  |  other |  |
          // |  |________|  |
          // |______________|
          //
          hr.getDifference().set(other.getAlphaIndex());
          if (!isSame) {
            delta.add(new Tuple<>(hr, other));
          }
          overlapping.add(hr);
          overlapping.add(other);
        } else {
          System.out.println("  Non-trivial overlap");
            // Case:
            //
            //         ______
            // _______|__ hr |
            // |      | |    |
            // |other |_|____|
            // |________|
            //
            overlap.setAlphaIndex(_nextIndex);
            _ecMap.put(_nextIndex, overlap);
            _rectangleMap.put(overlap, _nextIndex);
            _nextIndex++;
            hr.getDifference().set(overlap.getAlphaIndex());
            other.getDifference().set(overlap.getAlphaIndex());
            if (!isSame) {
              delta.add(new Tuple<>(other, overlap));
            }
            overlapping.add(hr);
            overlapping.add(other);
            overlapping.add(overlap);
          }
        }
    }


    // create new rectangles
    for (Tuple<HyperRectangle, HyperRectangle> d : delta) {
      System.out.println("Got delta: " + d.getFirst() + " |-->" + d.getSecond());
      HyperRectangle alpha = d.getFirst();
      HyperRectangle alphaPrime = d.getSecond();
      Map<String, NavigableSet<Rule>> existing = _ownerMap.get(alpha.getAlphaIndex());
      System.out.println("Adding index: " + alphaPrime.getAlphaIndex());
      System.out.println("Result: " + _ecMap.get(alphaPrime.getAlphaIndex()));
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

    // Update data structures
    for (HyperRectangle alpha : overlapping) {
      // System.out.println("  overlapping: " + alpha.getRangeStart());
      Rule rPrime = null;

      System.out.println("Retrieving index: " + alpha.getAlphaIndex());
      System.out.println("Result: " + _ecMap.get(alpha.getAlphaIndex()));

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
