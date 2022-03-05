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
import org.batfish.common.topology.bridge_domain.function.FilterByOuterTagImpl;
import org.batfish.common.topology.bridge_domain.function.FilterByVlanIdImpl;
import org.batfish.common.topology.bridge_domain.function.Identity;
import org.batfish.common.topology.bridge_domain.function.PopTagImpl;
import org.batfish.common.topology.bridge_domain.function.PushTag;
import org.batfish.common.topology.bridge_domain.function.PushVlanId;
import org.batfish.common.topology.bridge_domain.function.SetVlanId;
import org.batfish.common.topology.bridge_domain.function.StateFunctionVisitor;
import org.batfish.common.topology.bridge_domain.function.TranslateVlanImpl;
import org.batfish.common.topology.bridge_domain.node.L3Interface;
import org.batfish.common.topology.bridge_domain.node.Node;

final class Search {

  /** Returns the broadcast domain reachable from a given {@code originator}. */
  public static @Nonnull Set<L3Interface> originate(L3Interface originator) {
    return new Search(originator).getBroadcastDomain();
  }

  private @Nonnull Set<L3Interface> getBroadcastDomain() {
    return _broadcastDomain;
  }

  private void search() {
    _broadcastDomain.add(_originator);
    NodeAndState initialNodeAndState = NodeAndState.of(_originator, State.empty());
    _visited.add(initialNodeAndState);
    _originator
        .getOutEdges()
        .forEach(
            (nextNode, edgeToNextNode) ->
                _remainingTraversals.add(
                    () -> traverseEdge(initialNodeAndState, edgeToNextNode, nextNode)));
    // TODO: parallelize
    while (!_remainingTraversals.empty()) {
      _remainingTraversals.pop().run();
    }
  }

  private void traverseEdge(NodeAndState currentNodeAndState, Edge edge, Node toNode) {
    Node currentNode = currentNodeAndState.getNode();
    STATE_FUNCTION_EVALUATOR
        .visit(edge.getStateFunction(), currentNodeAndState.getState())
        .ifPresent(nextState -> visit(currentNode, NodeAndState.of(toNode, nextState)));
  }

  private void visit(Node lastNode, NodeAndState nodeAndState) {
    if (_visited.contains(nodeAndState)) {
      return;
    }
    _visited.add(nodeAndState);
    if (nodeAndState.getNode() instanceof L3Interface) {
      _broadcastDomain.add((L3Interface) nodeAndState.getNode());
    } else {
      nodeAndState
          .getNode()
          .getOutEdges()
          .forEach(
              (nextNode, edgeToNextNode) -> {
                if (nextNode.equals(lastNode)) {
                  return;
                }
                _remainingTraversals.push(
                    () -> traverseEdge(nodeAndState, edgeToNextNode, nextNode));
              });
    }
  }

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
