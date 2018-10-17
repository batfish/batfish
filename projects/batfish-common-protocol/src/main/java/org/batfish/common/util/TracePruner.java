package org.batfish.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
  // Invariant: the traces in these maps are in input order
  private final Map<FlowDisposition, List<Trace>> _dispositionTraces;
  private final Map<String, List<Trace>> _nodeTraces;
  private final List<Trace> _traces;
  private final SortedSet<FlowDisposition> _unpickedDispositions;
  private final SortedSet<String> _unpickedNodes;

  private TracePruner(List<Trace> traces) {
    _traces = traces;
    _dispositionTraces = new HashMap<>();
    _nodeTraces = new HashMap<>();

    for (Trace trace : traces) {
      _dispositionTraces
          .computeIfAbsent(trace.getDisposition(), key -> new ArrayList<>())
          .add(trace);
      for (Hop hop : trace.getHops()) {
        _nodeTraces.computeIfAbsent(hop.getNode().getName(), key -> new ArrayList<>()).add(trace);
      }
    }

    _unpickedDispositions = new TreeSet<>(_dispositionTraces.keySet());
    _unpickedNodes = new TreeSet<>(_nodeTraces.keySet());
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
      if (!_unpickedDispositions.isEmpty()) {
        usedTraces.add(chooseTraceByDisposition(usedTraces));
      } else if (!_unpickedNodes.isEmpty()) {
        usedTraces.add(chooseTraceByNode(usedTraces));
      } else {
        _traces
            .stream()
            .filter(trace -> !usedTraces.contains(trace))
            .limit(maxSize - usedTraces.size())
            .forEach(usedTraces::add);
        break;
      }
    }

    return usedTraces;
  }

  private Trace chooseTraceByDisposition(List<Trace> usedTraces) {
    Trace t =
        _unpickedDispositions
            .stream()
            .flatMap(disposition -> _dispositionTraces.get(disposition).stream())
            .filter(trace -> !usedTraces.contains(trace))
            .findFirst()
            .orElseThrow(() -> new BatfishException("No trace with unused disposition"));
    updateUnpickedMaps(t);
    return t;
  }

  private Trace chooseTraceByNode(List<Trace> usedTraces) {
    Trace t =
        _unpickedNodes
            .stream()
            .flatMap(node -> _nodeTraces.get(node).stream())
            .filter(trace -> !usedTraces.contains(trace))
            .findFirst()
            .orElseThrow(() -> new BatfishException("No trace with unused node"));
    updateUnpickedMaps(t);
    return t;
  }

  private void updateUnpickedMaps(Trace t) {
    _unpickedDispositions.remove(t.getDisposition());
    t.getHops().stream().map(hop -> hop.getNode().getName()).forEach(_unpickedNodes::remove);
  }
}
