package org.batfish.common.topology.bridge_domain;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import javax.annotation.Nonnull;
import org.batfish.common.topology.bridge_domain.edge.Edge;
import org.batfish.common.topology.bridge_domain.function.AssignVlanFromOuterTag;
import org.batfish.common.topology.bridge_domain.function.ClearVlanId;
import org.batfish.common.topology.bridge_domain.function.ComposeBaseImpl;
import org.batfish.common.topology.bridge_domain.function.FilterByOuterTag.FilterByOuterTagImpl;
import org.batfish.common.topology.bridge_domain.function.FilterByVlanId.FilterByVlanIdImpl;
import org.batfish.common.topology.bridge_domain.function.Identity;
import org.batfish.common.topology.bridge_domain.function.PopTag.PopTagImpl;
import org.batfish.common.topology.bridge_domain.function.PushTag;
import org.batfish.common.topology.bridge_domain.function.PushVlanId;
import org.batfish.common.topology.bridge_domain.function.SetVlanId;
import org.batfish.common.topology.bridge_domain.function.StateFunctionVisitor;
import org.batfish.common.topology.bridge_domain.function.TranslateVlan.TranslateVlanImpl;
import org.batfish.common.topology.bridge_domain.node.L3Interface;
import org.batfish.common.topology.bridge_domain.node.Node;

/**
 * Functionality for finding the {@link L3Interface}s in the broadcast domain of an originating
 * {@link L3Interface}.
 */
final class Search {

  /** Returns the broadcast domain reachable from a given {@code originator}. */
  public static @Nonnull Set<L3Interface> originate(L3Interface originator) {
    return new Search(originator).getBroadcastDomain();
  }

  /** The discovered broadcast domain. */
  private @Nonnull Set<L3Interface> getBroadcastDomain() {
    return _broadcastDomain;
  }

  private void search() {
    // Add the originator to the broadcast domain
    _broadcastDomain.add(_originator);
    // Add the origination node and empty state to the seen <node, state> pairs.
    NodeAndState initialNodeAndState = NodeAndState.of(_originator, State.empty());
    _visited.add(initialNodeAndState);
    // Queue a traversal of the out-edge of the origination node
    _originator
        .getOutEdges()
        .forEach(
            (nextNode, edgeToNextNode) ->
                _remainingTraversals.add(
                    () -> traverseEdge(initialNodeAndState, edgeToNextNode, nextNode)));
    // Keep traversing queued edges until none remain.
    // TODO: parallelize
    while (!_remainingTraversals.empty()) {
      _remainingTraversals.pop().run();
    }
  }

  private void traverseEdge(NodeAndState currentNodeAndState, Edge edge, Node toNode) {
    Node currentNode = currentNodeAndState.getNode();
    STATE_FUNCTION_EVALUATOR
        // Get the new state resulting from traversing the edge
        .visit(edge.getStateFunction(), currentNodeAndState.getState())
        // If no state is returned, the edge cannot be traversed given the current state.
        // Else, visit the destination node of the edge using resulting state.
        .ifPresent(nextState -> visit(currentNode, NodeAndState.of(toNode, nextState)));
  }

  private void visit(Node lastNode, NodeAndState nodeAndState) {
    if (_visited.contains(nodeAndState)) {
      // Terminate this branch since we have reached a cycle. This is a common situation, since
      // we do not cut the graph into a tree with STP.
      return;
    }
    // Record the current node and state so we can terminate traversal at a cycle above.
    _visited.add(nodeAndState);
    if (nodeAndState.getNode() instanceof L3Interface) {
      // We have reached a layer-3 interface, which is a terminal node. Add it to the broadcast
      // domain and terminate traversal of this branch.
      _broadcastDomain.add((L3Interface) nodeAndState.getNode());
    } else {
      // We have reached a non-terminal intermediate node. Split the traversal into branches for
      // based on the out-edges from this node.
      nodeAndState
          .getNode()
          .getOutEdges()
          .forEach(
              (nextNode, edgeToNextNode) -> {
                if (nextNode.equals(lastNode)) {
                  // Do not go backwards, for two reasons:
                  // 1. Frames should not be reflected.
                  // 2. Applying the back-edge state function is not guaranteed to preserve state
                  //    invariants.
                  return;
                }
                // Queue traversals of the remaining out edges.
                _remainingTraversals.push(
                    () -> traverseEdge(nodeAndState, edgeToNextNode, nextNode));
              });
    }
  }

  /**
   * A visitor that evaluates the state function of an out-edge of a node on the state at that node,
   * yielding either:
   *
   * <ul>
   *   <li>{@link Optional#empty()}, indicating that the edge may not be traversed given the current
   *       {@link State} at the node.
   *   <li>{@link Optional#of(Object)} of a new {@link State} at the destination node of the
   *       out-edge.
   * </ul>
   */
  static class StateFunctionEvaluator implements StateFunctionVisitor<Optional<State>, State> {

    @Override
    public Optional<State> visitAssignVlanFromOuterTag(
        AssignVlanFromOuterTag assignVlanFromOuterTag, State state) {
      Integer outerTag = state.getOuterTag();
      Integer nativeVlan = assignVlanFromOuterTag.getNativeVlan();
      assert outerTag != null || nativeVlan != null;
      assert state.getVlan() == null;
      if (outerTag == null) {
        return Optional.of(State.of(null, nativeVlan));
      } else {
        // TODO: preserve rest of tag stack in this case when tag stacks are supported.
        return Optional.of(State.of(null, outerTag));
      }
    }

    @Override
    public Optional<State> visitClearVlanId(ClearVlanId clearVlanId, State state) {
      assert state.getVlan() != null;
      return Optional.of(State.of(state.getOuterTag(), null));
    }

    @Override
    public Optional<State> visitCompose(ComposeBaseImpl<?> compose, State state) {
      return visit(compose.getFunc1(), state).flatMap(s1 -> visit(compose.getFunc2(), s1));
    }

    @Override
    public Optional<State> visitFilterByOuterTag(
        FilterByOuterTagImpl filterByOuterTag, State state) {
      Integer outerTag = state.getOuterTag();
      if (outerTag == null) {
        return filterByOuterTag.getAllowUntagged() ? Optional.of(state) : Optional.empty();
      } else {
        return filterByOuterTag.getAllowedOuterTags().contains(outerTag)
            ? Optional.of(state)
            : Optional.empty();
      }
    }

    @Override
    public Optional<State> visitFilterByVlanId(FilterByVlanIdImpl filterByVlanId, State state) {
      assert state.getVlan() != null;
      return Optional.of(state)
          .filter(s -> filterByVlanId.getAllowedVlanIds().contains(s.getVlan()));
    }

    @Override
    public Optional<State> visitIdentity(Identity identity, State state) {
      return Optional.of(state);
    }

    @Override
    public Optional<State> visitPopTag(PopTagImpl popTag, State state) {
      // TODO: remove this assertion when tag stacks are supported
      assert popTag.getCount() == 1;
      assert state.getOuterTag() != null;
      return Optional.of(State.of(null, state.getVlan()));
    }

    @Override
    public Optional<State> visitPushTag(PushTag pushTag, State state) {
      // TODO: remove this assertion when tag stacks are supported
      assert state.getOuterTag() == null;
      return Optional.of(State.of(pushTag.getTagToPush(), state.getVlan()));
    }

    @Override
    public Optional<State> visitPushVlanId(PushVlanId pushVlanId, State state) {
      // TODO: remove this assertion when tag stacks are supported
      assert state.getOuterTag() == null;
      assert state.getVlan() != null;
      int vlan = state.getVlan();
      return Optional.of(
          State.of(Objects.equals(vlan, pushVlanId.getExceptVlan()) ? null : vlan, vlan));
    }

    @Override
    public Optional<State> visitSetVlanId(SetVlanId setVlanId, State state) {
      assert state.getVlan() == null;
      return Optional.of(State.of(state.getOuterTag(), setVlanId.getVlanId()));
    }

    @Override
    public Optional<State> visitTranslateVlan(TranslateVlanImpl translateVlan, State state) {
      assert state.getVlan() != null;
      int oldVlan = state.getVlan();
      int newVlan = translateVlan.getTranslations().getOrDefault(oldVlan, oldVlan);
      return Optional.of(State.of(state.getOuterTag(), newVlan));
    }
  }

  private Search(L3Interface originator) {
    _originator = originator;
    _broadcastDomain = new HashSet<>();
    _visited = new HashSet<>();
    _remainingTraversals = new Stack<>();
    search();
  }

  @VisibleForTesting
  static final StateFunctionEvaluator STATE_FUNCTION_EVALUATOR = new StateFunctionEvaluator();

  private final @Nonnull Set<NodeAndState> _visited;
  private final @Nonnull L3Interface _originator;
  private final @Nonnull Set<L3Interface> _broadcastDomain;
  private final @Nonnull Stack<Runnable> _remainingTraversals;
}
