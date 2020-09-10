package org.batfish.dataplane.traceroute;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

/**
 * A DAG representation of a collection of {@link TraceAndReverseFlow traces}. Every path through
 * the DAG from a root to a leaf is a valid {@link TraceAndReverseFlow trace}, i.e. one that {@link
 * FlowTracer} would create.
 */
public class TraceDag {
  /** A node in the DAG contains everything needed to reconstruct traces through the node. */
  public static final class Node {
    private final Hop _hop;
    private final @Nullable FirewallSessionTraceInfo _firewallSessionTraceInfo;
    private final @Nullable FlowDisposition _flowDisposition;
    private final @Nullable Flow _returnFlow;
    private final List<Integer> _successors;

    public Node(
        Hop hop,
        @Nullable FirewallSessionTraceInfo firewallSessionTraceInfo,
        @Nullable FlowDisposition flowDisposition,
        @Nullable Flow returnFlow,
        List<Integer> successors) {
      checkArgument(
          (flowDisposition != null && flowDisposition.isSuccessful()) == (returnFlow != null),
          "returnFlow is present iff disposition is successful");
      _hop = hop;
      _firewallSessionTraceInfo = firewallSessionTraceInfo;
      _flowDisposition = flowDisposition;
      _returnFlow = returnFlow;
      _successors = ImmutableList.copyOf(successors);
    }
  }

  private final List<Node> _nodes;
  private final List<Integer> _rootIds;

  public TraceDag(List<Node> nodes, List<Integer> rootIds) {
    _nodes = nodes;
    _rootIds = rootIds;
  }

  public int size() {
    return new SizeComputer().size();
  }

  /** Efficiently compute the number of traces in the DAG. */
  private class SizeComputer {
    private final Integer[] _cache;

    SizeComputer() {
      _cache = new Integer[_nodes.size()];
    }

    int size(int nodeId) {
      @Nullable Integer cachedSize = _cache[nodeId];
      if (cachedSize != null) {
        return cachedSize;
      }
      Node node = _nodes.get(nodeId);
      if (node._successors.isEmpty()) {
        // leaf
        _cache[nodeId] = 1;
      } else {
        _cache[nodeId] = node._successors.stream().mapToInt(this::size).sum();
      }
      return _cache[nodeId];
    }

    int size() {
      return _rootIds.stream().mapToInt(this::size).sum();
    }
  }

  private Stream<TraceAndReverseFlow> getTraces(
      List<Hop> hopsInput, List<FirewallSessionTraceInfo> sessionsInput, int rootId) {
    Node node = _nodes.get(rootId);

    List<Hop> hops = new ArrayList<>(hopsInput);
    hops.add(node._hop);

    List<FirewallSessionTraceInfo> sessions;
    FirewallSessionTraceInfo firewallSessionTraceInfo = node._firewallSessionTraceInfo;
    if (firewallSessionTraceInfo != null) {
      sessions = new ArrayList<>(sessionsInput);
      sessions.add(firewallSessionTraceInfo);
    } else {
      sessions = sessionsInput;
    }

    List<Integer> successors = node._successors;
    if (successors.isEmpty()) {
      FlowDisposition disposition =
          checkNotNull(node._flowDisposition, "failed to determine disposition from hop");
      Trace trace = new Trace(disposition, hops);
      return Stream.of(new TraceAndReverseFlow(trace, node._returnFlow, sessions));
    } else {
      return successors.stream().flatMap(successorId -> getTraces(hops, sessions, successorId));
    }
  }

  int countEdges() {
    return _nodes.stream().map(node -> node._successors).mapToInt(List::size).sum();
  }

  int countNodes() {
    return _nodes.size();
  }

  private Stream<TraceAndReverseFlow> getTraces(int rootId) {
    return getTraces(ImmutableList.of(), new Stack<>(), rootId);
  }

  public Stream<TraceAndReverseFlow> getTraces() {
    return _rootIds.stream().map(this::getTraces).flatMap(Function.identity());
  }
}
