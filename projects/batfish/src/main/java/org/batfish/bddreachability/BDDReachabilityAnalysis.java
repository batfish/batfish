package org.batfish.bddreachability;

import static org.batfish.bddreachability.BDDReachabilityUtils.computeForwardEdgeTable;
import static org.batfish.bddreachability.BDDReachabilityUtils.getIngressLocationBdds;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.specifier.Location;
import org.batfish.symbolic.IngressLocation;
import org.batfish.symbolic.state.Query;
import org.batfish.symbolic.state.StateExpr;

/**
 * A new reachability analysis engine using BDDs. The analysis maintains a graph that describes how
 * packets flow through the network and through logical phases of a router. In particular, the graph
 * nodes are {@link StateExpr StateExprs} and the edges are mostly the same as the NOD program
 * rules/transitions. {@link BDD BDDs} label the nodes and edges of the graph. A node label
 * represent the set of packets that can reach that node, and an edge label represents the set of
 * packets that can traverse the edge. There is a single designated {@link Query} StateExpr that we
 * compute reachability sets (i.e. sets of packets that reach the query state). The query state
 * never has any out-edges, and has in-edges from the dispositions of interest.
 *
 * <p>The two main departures from the NOD program are: 1) ACLs are encoded as a single BDD that
 * labels an edge (rather than a series of states/transitions in NOD programs). 2) Source NAT is
 * handled differently -- we don't maintain separate original and current source IP variables.
 * Instead, we keep track of where/how the packet is transformed as it flows through the network,
 * and reconstruct it after the fact. This requires some work that can't be expressed in BDDs.
 *
 * <p>We currently implement backward all-pairs reachability. Forward reachability is useful for
 * questions with a tight source constraint, e.g. "find me packets send from node A that get
 * dropped". When reasoning about many sources simultaneously, we have to somehow remember the
 * source, which is very expensive for a large number of sources. For queries that have to consider
 * all packets that can reach the query state, backward reachability is much more efficient.
 */
public class BDDReachabilityAnalysis implements Serializable {
  private final BDDPacket _bddPacket;

  // preState --> postState --> transition from pre to post
  private final Table<StateExpr, StateExpr, Transition> _forwardEdgeTable;
  // postState --> preState --> transition from pre to post
  private transient Supplier<Table<StateExpr, StateExpr, Transition>> _transposedEdgeTable;

  // stateExprs that correspond to the IngressLocations of interest
  private final ImmutableSet<StateExpr> _ingressLocationStates;

  private final BDD _queryHeaderSpaceBdd;

  public BDDReachabilityAnalysis(
      BDDPacket packet,
      Set<StateExpr> ingressLocationStates,
      Stream<Edge> edges,
      BDD queryHeaderSpaceBdd) {
    _bddPacket = packet;
    _forwardEdgeTable = computeForwardEdgeTable(edges);
    _ingressLocationStates = ImmutableSet.copyOf(ingressLocationStates);
    _queryHeaderSpaceBdd = queryHeaderSpaceBdd;
    initTransientFields();
  }

  private void initTransientFields() {
    _transposedEdgeTable =
        Suppliers.memoize(() -> BDDReachabilityUtils.transposeAndMaterialize(_forwardEdgeTable));
  }

  @Serial
  private void readObject(java.io.ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    initTransientFields();
  }

  Map<StateExpr, BDD> computeReverseReachableStates() {
    Map<StateExpr, BDD> reverseReachableStates = new HashMap<>();
    reverseReachableStates.put(Query.INSTANCE, _queryHeaderSpaceBdd);
    BDDReachabilityUtils.backwardFixpointTransposed(
        _transposedEdgeTable.get(), reverseReachableStates);
    return ImmutableMap.copyOf(reverseReachableStates);
  }

  /**
   * Compute the reverse reachability ("X can reach a destination" rather than "a source can reach
   * X"), starting with the initial roots marked as being able to reach themselves with the
   * corresponding headerspace BDDs.
   */
  public Map<StateExpr, BDD> computeReverseReachableStates(Map<StateExpr, BDD> roots) {
    Map<StateExpr, BDD> reverseReachableStates = new HashMap<>(roots);
    BDDReachabilityUtils.backwardFixpointTransposed(
        _transposedEdgeTable.get(), reverseReachableStates);
    return ImmutableMap.copyOf(reverseReachableStates);
  }

  public Map<StateExpr, BDD> computeForwardReachableStates() {
    Map<StateExpr, BDD> forwardReachableStates = new LinkedHashMap<>();
    _ingressLocationStates.forEach(
        state -> forwardReachableStates.put(state, _bddPacket.getFactory().one()));
    BDDReachabilityUtils.forwardFixpoint(_forwardEdgeTable, forwardReachableStates);
    return ImmutableMap.copyOf(forwardReachableStates);
  }

  /**
   * Compute the flows (represented as BDDs) that can reach each state {@link StateExpr} in the
   * reachability graph given the initial flows existing at some states.
   */
  public Map<StateExpr, BDD> computeForwardReachableStates(
      Map<StateExpr, BDD> initialReachableStates) {
    Map<StateExpr, BDD> forwardReachableStates = new LinkedHashMap<>(initialReachableStates);
    BDDReachabilityUtils.forwardFixpoint(_forwardEdgeTable, forwardReachableStates);
    return ImmutableMap.copyOf(forwardReachableStates);
  }

  public BDDPacket getBDDPacket() {
    return _bddPacket;
  }

  public ImmutableSet<StateExpr> getIngressLocationStates() {
    return _ingressLocationStates;
  }

  public Map<IngressLocation, BDD> getIngressLocationReachableBDDs() {
    Map<StateExpr, BDD> reverseReachableStates = computeReverseReachableStates();
    return getIngressLocationBDDs(reverseReachableStates);
  }

  /**
   * Like {@link #getIngressLocationReachableBDDs()}, but for only the specified states (with
   * initial BDDs).
   */
  public Map<IngressLocation, BDD> getIngressLocationReachableBDDs(Map<StateExpr, BDD> roots) {
    Map<StateExpr, BDD> reverseReachableStates = computeReverseReachableStates(roots);
    return getIngressLocationBDDs(reverseReachableStates);
  }

  /** Map of all src {@link Location} to {@link BDD} of successful flows from that location */
  public Map<Location, BDD> getSrcLocationBdds() {
    Map<StateExpr, BDD> reverseReachableStates = computeReverseReachableStates();
    ImmutableMap.Builder<Location, BDD> srcLocBdds = ImmutableMap.builder();
    for (Entry<StateExpr, BDD> e : reverseReachableStates.entrySet()) {
      Optional<Location> srcLoc = OriginationStateExprToLocation.toLocation(e.getKey());
      srcLoc.ifPresent(loc -> srcLocBdds.put(loc, e.getValue()));
    }
    return srcLocBdds.build();
  }

  private Map<IngressLocation, BDD> getIngressLocationBDDs(
      Map<StateExpr, BDD> reverseReachableStates) {
    return getIngressLocationBdds(
        reverseReachableStates, _ingressLocationStates, _bddPacket.getFactory().zero());
  }

  public BDD getQueryHeaderSpaceBdd() {
    return _queryHeaderSpaceBdd;
  }

  public Map<StateExpr, Map<StateExpr, Transition>> getForwardEdgeMap() {
    return _forwardEdgeTable.rowMap();
  }

  public Table<StateExpr, StateExpr, Transition> getForwardEdgeTable() {
    return ImmutableTable.copyOf(_forwardEdgeTable);
  }
}
