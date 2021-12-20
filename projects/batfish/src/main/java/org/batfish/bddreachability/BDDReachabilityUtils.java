package org.batfish.bddreachability;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableTable.toImmutableTable;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.bddreachability.transition.Transitions;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.BDDIpProtocol;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Flow.Builder;
import org.batfish.datamodel.transformation.AssignPortFromPool;
import org.batfish.symbolic.IngressLocation;
import org.batfish.symbolic.state.OriginateInterfaceLink;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.StateExpr;

/**
 * Utility methods for {@link BDDReachabilityAnalysis} and {@link BDDReachabilityAnalysisFactory}.
 */
public final class BDDReachabilityUtils {
  public static Table<StateExpr, StateExpr, Transition> computeForwardEdgeTable(
      Iterable<Edge> edges) {
    return computeForwardEdgeTable(Streams.stream(edges));
  }

  static Table<StateExpr, StateExpr, Transition> computeForwardEdgeTable(Stream<Edge> edges) {
    return edges.collect(
        toImmutableTable(
            Edge::getPreState, Edge::getPostState, Edge::getTransition, Transitions::or));
  }

  /** Apply edges to the reachableSets until a fixed point is reached. */
  @VisibleForTesting
  static void fixpoint(
      Map<StateExpr, BDD> reachableSets,
      Table<StateExpr, StateExpr, Transition> edges,
      BiFunction<Transition, BDD, BDD> traverse) {
    Span span = GlobalTracer.get().buildSpan("BDDReachabilityAnalysis.fixpoint").start();
    try (Scope scope = GlobalTracer.get().scopeManager().activate(span)) {
      assert scope != null; // avoid unused warning

      if (reachableSets.isEmpty()) {
        // No work to do.
        return;
      }
      // Get a BDDFactory for zero and orAll.
      BDDFactory factory = reachableSets.entrySet().iterator().next().getValue().getFactory();

      // For each state to process in the next round, all the incoming BDDs.
      SetMultimap<StateExpr, BDD> dirtyInputs = HashMultimap.create();

      // To (try to) minimize how many times we're transiting the same edges, dirtyStates will be
      // removed in order of increasing visitCounts.
      // invariants:
      // 1. the queue never contains duplicate elements.
      // 2. visitCounts are never incremented while the state is in the queue.
      HashMap<StateExpr, Integer> visitCounts = new HashMap<>();
      PriorityQueue<StateExpr> dirtyStates =
          new PriorityQueue<>(Comparator.comparingInt(st -> visitCounts.getOrDefault(st, 0)));

      // Seed the dirty inputs with the initial reachable sets, then clear the reachable sets.
      reachableSets.forEach(
          (key, value) -> {
            dirtyInputs.put(key, value.id());
            dirtyStates.add(key);
          });
      reachableSets.clear();

      while (!dirtyStates.isEmpty()) {
        StateExpr dirtyState = dirtyStates.remove();
        visitCounts.compute(dirtyState, (unused, oldCount) -> oldCount == null ? 1 : oldCount + 1);
        Set<BDD> inputs = dirtyInputs.removeAll(dirtyState);
        assert !inputs.isEmpty();
        BDD prior = reachableSets.get(dirtyState);
        BDD newValue =
            prior == null
                ? factory.orAll(inputs)
                : factory.orAll(Sets.union(inputs, ImmutableSet.of(prior)));
        if (newValue.equals(prior)) {
          // No change, so no need to update neighbors.
          newValue.free();
          inputs.forEach(BDD::free);
          continue;
        }

        // Update the value and free the old one.
        reachableSets.put(dirtyState, newValue);
        if (prior != null) {
          prior.free();
        }

        Map<StateExpr, Transition> dirtyStateEdges = edges.row(dirtyState);
        if (dirtyStateEdges.isEmpty()) {
          inputs.forEach(BDD::free);
          continue;
        }

        // Compute the newly learned BDDs (union of inputs) and then free them.
        BDD learned = prior == null ? newValue.id() : factory.orAll(inputs);
        inputs.forEach(BDD::free);

        // Forward the learned BDDs along each outgoing edge.
        dirtyStateEdges.forEach(
            (neighbor, edge) -> {
              BDD result = traverse.apply(edge, learned);
              if (!result.isZero()) {
                // this is a new result. add it to neighbor's inputs. if neighbor isn't already in
                // the dirtyStates queue, add it.
                if (!dirtyInputs.containsKey(neighbor)) {
                  dirtyStates.add(neighbor);
                }
                dirtyInputs.put(neighbor, result);
              }
            });
        learned.free();
      }
    } finally {
      span.finish();
    }
  }

  @VisibleForTesting
  public static IngressLocation toIngressLocation(StateExpr stateExpr) {
    checkArgument(stateExpr instanceof OriginateVrf || stateExpr instanceof OriginateInterfaceLink);

    if (stateExpr instanceof OriginateVrf) {
      OriginateVrf originateVrf = (OriginateVrf) stateExpr;
      return IngressLocation.vrf(originateVrf.getHostname(), originateVrf.getVrf());
    } else {
      OriginateInterfaceLink originateInterfaceLink = (OriginateInterfaceLink) stateExpr;
      return IngressLocation.interfaceLink(
          originateInterfaceLink.getHostname(), originateInterfaceLink.getInterface());
    }
  }

  /**
   * Runs a fixpoint through the given graph backwards from the given states.
   *
   * <p>If this function will be called more than once on the same edge table, prefer {@link
   * #backwardFixpointTransposed(Table, Map)} on a transposed, materialized edge table (see {@link
   * BDDReachabilityUtils#transposeAndMaterialize(Table)}) to save redundant computations.
   */
  public static void backwardFixpoint(
      Table<StateExpr, StateExpr, Transition> forwardEdgeTable,
      Map<StateExpr, BDD> reverseReachable) {
    backwardFixpointTransposed(transposeAndMaterialize(forwardEdgeTable), reverseReachable);
  }

  /** See {@link #backwardFixpoint(Table, Map)}. */
  public static void backwardFixpointTransposed(
      Table<StateExpr, StateExpr, Transition> transposedEdgeTable,
      Map<StateExpr, BDD> reverseReachable) {
    fixpoint(reverseReachable, transposedEdgeTable, Transition::transitBackward);
  }

  /**
   * Returns an immutable copy of the input table that has been materialized in transposed form.
   *
   * <p>Use this instead of {@link Tables#transpose(Table)} if the result will be iterated on in
   * row-major order. Transposing the table alone does not change the row-major vs column-major
   * internal representation so the performance of row-oriented operations is abysmal. Instead, we
   * need to actually materialize the transposed representation.
   */
  public static <R, C, V> Table<C, R, V> transposeAndMaterialize(Table<R, C, V> edgeTable) {
    return ImmutableTable.copyOf(Tables.transpose(edgeTable));
  }

  public static Table<StateExpr, StateExpr, Transition> transposeAndMaterialize(
      Collection<Edge> edges) {
    return edges.stream()
        .collect(
            ImmutableTable.toImmutableTable(
                Edge::getPostState, Edge::getPreState, Edge::getTransition));
  }

  public static void forwardFixpoint(
      Table<StateExpr, StateExpr, Transition> forwardEdgeTable, Map<StateExpr, BDD> reachable) {
    fixpoint(reachable, forwardEdgeTable, Transition::transitForward);
  }

  static Map<StateExpr, BDD> getIngressStateExprBdds(
      Map<StateExpr, BDD> stateReachableBdds, Set<StateExpr> ingressLocationStates, BDD zero) {
    return toImmutableMap(
        ingressLocationStates,
        Function.identity(),
        stateExpr -> stateReachableBdds.getOrDefault(stateExpr, zero));
  }

  static Map<IngressLocation, BDD> getIngressLocationBdds(
      Map<StateExpr, BDD> stateReachableBdds, Set<StateExpr> ingressLocationStates, BDD zero) {
    return toImmutableMap(
        ingressLocationStates,
        BDDReachabilityUtils::toIngressLocation,
        stateExpr -> stateReachableBdds.getOrDefault(stateExpr, zero));
  }

  public static BDD computePortTransformationProtocolsBdd(BDDIpProtocol ipProtocol) {
    return AssignPortFromPool.PORT_TRANSFORMATION_PROTOCOLS.stream()
        .map(ipProtocol::value)
        .reduce(BDD::or)
        .get();
  }

  public static Set<Flow> constructFlows(BDDPacket pkt, Map<IngressLocation, BDD> reachableBdds) {
    return reachableBdds.entrySet().stream()
        .flatMap(
            entry -> {
              IngressLocation loc = entry.getKey();
              BDD headerSpace = entry.getValue();
              Optional<Builder> optionalFlow = pkt.getFlow(headerSpace);
              if (!optionalFlow.isPresent()) {
                return Stream.of();
              }
              Flow.Builder flow = optionalFlow.get();
              flow.setIngressNode(loc.getNode());
              switch (loc.getType()) {
                case INTERFACE_LINK:
                  flow.setIngressInterface(loc.getInterface());
                  break;
                case VRF:
                  flow.setIngressVrf(loc.getVrf());
                  break;
                default:
                  throw new BatfishException(
                      "Unexpected IngressLocation Type: " + loc.getType().name());
              }
              return Stream.of(flow.build());
            })
        .collect(ImmutableSet.toImmutableSet());
  }
}
