package org.batfish.common.util;

import static org.glassfish.jersey.internal.guava.Predicates.not;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Trace;

/**
 * Prune sets of traces to some reasonable limit, while maximizing coverage of possible behaviors.
 *
 * <p>Prioritize traces with different dispositions first, then traces that transit different nodes.
 * Once all dispositions and nodes are covered, pick traces until the maxSize is reached. To ensure
 * determinism, we choose dispositions, nodes, and traces in a consistent order.
 */
public class TracePruner {
  private final Multimap<FlowDisposition, Trace> _dispositionTraces;
  private final Multimap<String, Trace> _nodeTraces;
  private final List<Trace> _traces;
  private final SortedSet<FlowDisposition> _unusedDispositions;
  private final SortedSet<String> _unusedNodes;

  private TracePruner(List<Trace> traces) {
    _traces = traces;
    _dispositionTraces = HashMultimap.create();
    _nodeTraces = HashMultimap.create();

    for (Trace trace : traces) {
      _dispositionTraces.put(trace.getDisposition(), trace);
      for (Hop hop : trace.getHops()) {
        _nodeTraces.put(hop.getNode().getName(), trace);
      }
    }

    _unusedDispositions = new TreeSet<>(_dispositionTraces.keySet());
    _unusedNodes = new TreeSet<>(_nodeTraces.keySet());
  }

  public static List<Trace> prune(List<Trace> traces, int maxSize) {
    if (traces.size() <= maxSize) {
      return traces;
    }
    return new TracePruner(traces).prune(maxSize);
  }

  private List<Trace> prune(int maxSize) {
    List<Trace> usedTraces = new ArrayList<>();

    while (usedTraces.size() < maxSize) {
      if (!_unusedDispositions.isEmpty()) {
        usedTraces.add(chooseTraceByDisposition(usedTraces));
      } else if (!_unusedNodes.isEmpty()) {
        usedTraces.add(chooseTraceByNode(usedTraces));
      } else {
        _traces
            .stream()
            .filter(not(usedTraces::contains))
            .limit(maxSize - usedTraces.size())
            .forEach(usedTraces::add);
        break;
      }
    }

    return usedTraces;
  }

  private Trace chooseTraceByDisposition(List<Trace> usedTraces) {
    Trace t =
        _unusedDispositions
            .stream()
            .flatMap(disposition -> _dispositionTraces.get(disposition).stream())
            .filter(not(usedTraces::contains))
            .findFirst()
            .orElseThrow(() -> new BatfishException("No trace with unused disposition"));
    _unusedDispositions.remove(t.getDisposition());
    return t;
  }

  private Trace chooseTraceByNode(List<Trace> usedTraces) {
    Trace t =
        _unusedDispositions
            .stream()
            .flatMap(disposition -> _dispositionTraces.get(disposition).stream())
            .filter(not(usedTraces::contains))
            .findFirst()
            .orElseThrow(() -> new BatfishException("No trace with unused node"));
    t.getHops().stream().map(hop -> hop.getNode().getName()).forEach(_unusedNodes::remove);
    return t;
  }
}
