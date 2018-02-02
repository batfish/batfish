package org.batfish.deltanet;

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

public class Dataplane {

  private static int LOWER_INDEX = 0;
  private static int UPPER_INDEX = 1;
  private static long LOWER_VALUE = 0;
  private static long UPPER_VALUE = (long) Math.pow(2, 32) + 1;

  private int _nextIndex = 2;

  private NavigableSet<HalfInterval> _intervalSet;

  private Map<NodeInterfacePair, BitSet> _labels;

  private Map<Integer, Map<String, NavigableSet<Rule>>> _ownerMap;

  public Dataplane(DataPlane dp) {
    long t = System.currentTimeMillis();
    // initialize
    HalfInterval lower = new HalfInterval(LOWER_VALUE, LOWER_INDEX);
    HalfInterval upper = new HalfInterval(UPPER_VALUE, UPPER_INDEX);
    // System.out.println("Initial lower: " + lower.getRangeStart());
    // System.out.println("Initial upper: " + upper.getRangeStart());
    _intervalSet = new TreeSet<>();
    _intervalSet.add(lower);
    _intervalSet.add(upper);
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
    _ownerMap.put(1, new HashMap<>(map));

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

  private List<Tuple<HalfInterval, HalfInterval>> createAtoms(HalfInterval from, HalfInterval to) {
    List<Tuple<HalfInterval, HalfInterval>> delta = new ArrayList<>();
    // System.out.println("  checking for lower: " + from.getRangeStart());
    if (!_intervalSet.contains(from)) {
      // System.out.println("  not already there");
      from.setAlphaIndex(_nextIndex);
      _intervalSet.add(from);
      _nextIndex++;
      HalfInterval alpha = _intervalSet.lower(from);
      delta.add(new Tuple<>(alpha, from));
    }
    // System.out.println("  checking for upper: " + to.getRangeStart());
    if (from.getRangeStart() != to.getRangeStart() && !_intervalSet.contains(to)) {
      // System.out.println("  not already there");
      to.setAlphaIndex(_nextIndex);
      _intervalSet.add(to);
      _nextIndex++;
      HalfInterval alpha = _intervalSet.lower(to);
      delta.add(new Tuple<>(alpha, to));
    }
    return delta;
  }

  public void addRule(Rule r) {
    //System.out.println(
    //    "Adding rule: " + r.getFib().getPrefix() + " at " + r.getFib().getInterface());

    Prefix p = r.getFib().getPrefix();
    long start = p.getStartIp().asLong();
    long end = p.getEndIp().asLong();
    HalfInterval from = new HalfInterval(start, -1);
    HalfInterval to = new HalfInterval(end, -1);
    List<Tuple<HalfInterval, HalfInterval>> delta = createAtoms(from, to);

    // create new intervals
    for (Tuple<HalfInterval, HalfInterval> d : delta) {
      HalfInterval alpha = d.getFirst();
      HalfInterval alphaPrime = d.getSecond();
      //System.out.println(
      //    "  Delta interval: " + alpha.getRangeStart() + " |-> " + alphaPrime.getRangeStart());
      // System.out.println("  Alpha idx: " + alpha.getAlphaIndex());
      Map<String, NavigableSet<Rule>> existing = _ownerMap.get(alpha.getAlphaIndex());
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
    // TODO: not sure, but exact match may be special because it is a point on the interval
    boolean inclusive = r.getFib().getPrefix().getPrefixLength() == 32;
    SortedSet<HalfInterval> overlapping = _intervalSet.subSet(from, true, to, inclusive);
    for (HalfInterval alpha : overlapping) {
      // System.out.println("  overlapping: " + alpha.getRangeStart());
      Rule rPrime = null;
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
