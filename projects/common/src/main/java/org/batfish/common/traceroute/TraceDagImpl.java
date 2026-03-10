package org.batfish.common.traceroute;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.common.util.FlatMapIterator.flatMapIterator;

import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.util.FlatMapIterator;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.flow.FirewallSessionTraceInfo;
import org.batfish.datamodel.flow.Hop;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceAndReverseFlow;

/**
 * A DAG representation of a collection of {@link TraceAndReverseFlow traces}. Every path through
 * the DAG from a root to a leaf is a valid {@link TraceAndReverseFlow trace}, i.e. one that {@link
 * org.batfish.common.plugin.TracerouteEngine} would create.
 */
public final class TraceDagImpl implements TraceDag {
  /** A node in the DAG contains everything needed to reconstruct traces through the node. */
  public static final class Node {
    private final @Nonnull Hop _hop;
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
      checkArgument(
          (flowDisposition != null) == successors.isEmpty(),
          "flowDisposition is present iff successors is empty");
      _hop = hop;
      _firewallSessionTraceInfo = firewallSessionTraceInfo;
      _flowDisposition = flowDisposition;
      _returnFlow = returnFlow;
      _successors = ImmutableList.copyOf(successors);
    }

    public @Nonnull Hop getHop() {
      return _hop;
    }

    public @Nullable FirewallSessionTraceInfo getFirewallSessionTraceInfo() {
      return _firewallSessionTraceInfo;
    }

    public @Nullable FlowDisposition getFlowDisposition() {
      return _flowDisposition;
    }

    public @Nullable Flow getReturnFlow() {
      return _returnFlow;
    }

    public @Nonnull List<Integer> getSuccessors() {
      return _successors;
    }
  }

  private final List<Node> _nodes;
  private final List<Integer> _rootIds;

  public TraceDagImpl(List<Node> nodes, List<Integer> rootIds) {
    _nodes = nodes;
    _rootIds = rootIds;
  }

  public @Nonnull List<Node> getNodes() {
    return _nodes;
  }

  @Override
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

  private Iterator<TraceAndReverseFlow> getTraces(
      List<Hop> hopsInput, List<FirewallSessionTraceInfo> sessionsInput, int rootId) {
    Node node = _nodes.get(rootId);

    List<Hop> hops =
        ImmutableList.<Hop>builderWithExpectedSize(hopsInput.size() + 1)
            .addAll(hopsInput)
            .add(node._hop)
            .build();

    List<FirewallSessionTraceInfo> sessions;
    FirewallSessionTraceInfo firewallSessionTraceInfo = node._firewallSessionTraceInfo;
    if (firewallSessionTraceInfo != null) {
      sessions =
          ImmutableList.<FirewallSessionTraceInfo>builderWithExpectedSize(sessionsInput.size() + 1)
              .addAll(sessionsInput)
              .add(firewallSessionTraceInfo)
              .build();
    } else {
      sessions = sessionsInput;
    }

    List<Integer> successors = node._successors;
    if (successors.isEmpty()) {
      FlowDisposition disposition =
          checkNotNull(node._flowDisposition, "failed to determine disposition from hop");
      Trace trace = new Trace(disposition, hops);
      return ImmutableList.of(new TraceAndReverseFlow(trace, node._returnFlow, sessions))
          .iterator();
    } else {
      return new FlatMapIterator<>(
          successors.iterator(), successorId -> getTraces(hops, sessions, successorId));
    }
  }

  @Override
  public int countEdges() {
    return _nodes.stream().map(node -> node._successors).mapToInt(List::size).sum();
  }

  @Override
  public int countNodes() {
    return _nodes.size();
  }

  private Iterator<TraceAndReverseFlow> getTraces(int rootId) {
    return getTraces(ImmutableList.of(), new Stack<>(), rootId);
  }

  @Override
  public Stream<TraceAndReverseFlow> getAllTraces() {
    Iterable<TraceAndReverseFlow> iterable =
        () -> flatMapIterator(_rootIds.iterator(), this::getTraces);
    return StreamSupport.stream(iterable.spliterator(), false);
  }
}
