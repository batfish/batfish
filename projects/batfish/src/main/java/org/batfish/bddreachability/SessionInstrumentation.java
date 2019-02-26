package org.batfish.bddreachability;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.bddreachability.OriginationStateToTerminationState.originationStateToTerminationState;
import static org.batfish.bddreachability.transition.Transitions.addLastHopConstraint;
import static org.batfish.bddreachability.transition.Transitions.addSourceInterfaceConstraint;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.removeLastHopConstraint;
import static org.batfish.bddreachability.transition.Transitions.removeSourceConstraint;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.NodeAccept;
import org.batfish.z3.state.NodeDropAclIn;
import org.batfish.z3.state.NodeDropAclOut;
import org.batfish.z3.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.z3.state.PreInInterface;

/**
 * Instruments a {@link BDDReachabilityAnalysis} graph to install {@link BDDFirewallSessionTraceInfo
 * initialized firewall sessions}.
 */
public class SessionInstrumentation {
  private final BDDPacket _bddPacket;

  private final Map<String, Configuration> _configs;
  private final Map<String, BDDSourceManager> _srcMgrs;
  private final LastHopOutgoingInterfaceManager _lastHopMgr;

  // Can be null for ignoreFilters
  private final @Nullable Map<String, Map<String, Supplier<BDD>>> _filterBdds;

  private final BDD _one;

  @VisibleForTesting
  SessionInstrumentation(
      BDDPacket bddPacket,
      Map<String, Configuration> configs,
      Map<String, BDDSourceManager> srcMgrs,
      LastHopOutgoingInterfaceManager lastHopMgr,
      @Nullable Map<String, Map<String, Supplier<BDD>>> filterBdds) {
    _bddPacket = bddPacket;
    _configs = configs;
    _srcMgrs = srcMgrs;
    _lastHopMgr = lastHopMgr;
    _filterBdds = filterBdds;
    _one = bddPacket.getFactory().one();
  }

  public static Stream<Edge> sessionInstrumentation(
      BDDPacket bddPacket,
      Map<String, Configuration> configs,
      Map<String, BDDSourceManager> srcMgrs,
      LastHopOutgoingInterfaceManager lastHopMgr,
      Map<String, Map<String, Supplier<BDD>>> filterBdds,
      Stream<Edge> originalEdges,
      Map<String, List<BDDFirewallSessionTraceInfo>> initializedSessions) {
    SessionInstrumentation instrumentation =
        new SessionInstrumentation(bddPacket, configs, srcMgrs, lastHopMgr, filterBdds);
    Stream<Edge> newEdges = instrumentation.computeNewEdges(initializedSessions);

    // new constraints on existing out-edges from a particular state expr
    Map<StateExpr, BDD> existingOutEdgeConstraints =
        computeExistingOutEdgeConstraints(initializedSessions);

    Stream<Edge> instrumentedEdges = instrumentEdges(originalEdges, existingOutEdgeConstraints);

    return Stream.concat(newEdges, instrumentedEdges);
  }

  @Nonnull
  private static Stream<Edge> instrumentEdges(
      Stream<Edge> originalEdges, Map<StateExpr, BDD> existingOutEdgeConstraints) {
    return originalEdges.map(
        edge -> {
          BDD additionalConstraint = existingOutEdgeConstraints.get(edge.getPreState());
          return additionalConstraint == null
              ? edge
              : new Edge(
                  edge.getPreState(),
                  edge.getPostState(),
                  compose(constraint(additionalConstraint), edge.getTransition()));
        });
  }

  /** State -> New constraints on out-edges of that state. */
  private static Map<StateExpr, BDD> computeExistingOutEdgeConstraints(
      Map<String, List<BDDFirewallSessionTraceInfo>> initializedSessions) {
    return initializedSessions.values().stream()
        .flatMap(Collection::stream)
        .flatMap(SessionInstrumentation::computeExistingOutEdgeConstraints)
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue, BDD::and));
  }

  /**
   * For each incoming interface of a session, constrain existing out-edges from the {@link
   * PreInInterface} state for that interface to exclude session flows.
   */
  private static Stream<Entry<StateExpr, BDD>> computeExistingOutEdgeConstraints(
      BDDFirewallSessionTraceInfo sessionInfo) {
    return sessionInfo.getIncomingInterfaces().stream()
        .map(
            incomingIface -> {
              String hostname = sessionInfo.getHostname();
              BDD nonSessionFlows = sessionInfo.getSessionFlows().not();
              return Maps.immutableEntry(
                  new PreInInterface(hostname, incomingIface), nonSessionFlows);
            });
  }

  /**
   * For each pair of (incoming interface, outgoing interface) of a session, add an edge for session
   * flows from the incoming interface's {@link PreInInterface} to each of the outgoing interface's
   * states corresponding to traffic successfully being forwarded out the node.
   */
  private Stream<Edge> computeNewEdges(
      Map<String, List<BDDFirewallSessionTraceInfo>> initializedSessions) {
    return initializedSessions.values().stream()
        .flatMap(Collection::stream)
        .flatMap(this::computeNewEdges);
  }

  @Nonnull
  private Stream<Edge> computeNewEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    return Streams.concat(
        computeNewSuccessEdges(sessionInfo),
        nodeDropAclInEdges(sessionInfo),
        nodeDropAclOutEdges(sessionInfo));
  }

  @Nonnull
  private Stream<Edge> computeNewSuccessEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    String outIface = sessionInfo.getOutgoingInterface();

    NodeInterfacePair nextHop = sessionInfo.getNextHop();

    if (outIface == null) {
      return nodeAcceptEdges(sessionInfo);
    } else if (nextHop == null) {
      /* Forward query started with OriginateInterfaceLink(hostname, outIface).
       * ReversePassOriginationState maps both NodeInterfaceDeliveredToSubnet
       * and NodeInterfaceExitsNetwork to OriginateInterfaceLink, so we can use
       * either here and they will match up.
       */
      return nodeInterfaceDeliveredToSubnetEdges(sessionInfo);
    } else {
      return preInInterfaceEdges(sessionInfo);
    }
  }

  @VisibleForTesting
  Stream<Edge> preInInterfaceEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    checkArgument(
        sessionInfo.getOutgoingInterface() != null && sessionInfo.getNextHop() != null,
        "Not a PreInInterface session");

    String hostname = sessionInfo.getHostname();
    String outIface = sessionInfo.getOutgoingInterface();
    NodeInterfacePair nextHop = sessionInfo.getNextHop();
    BDDSourceManager srcMgr = _srcMgrs.get(hostname);
    StateExpr postState = new PreInInterface(nextHop.getHostname(), nextHop.getInterface());

    Transition outAcl = constraint(getOutgoingSessionFilterBdd(hostname, outIface));

    Transition addSourceIfaceConstraint =
        addSourceInterfaceConstraint(_srcMgrs.get(nextHop.getHostname()), nextHop.getInterface());
    Transition addLastHop =
        addLastHopConstraint(
            _lastHopMgr, hostname, outIface, nextHop.getHostname(), nextHop.getInterface());
    return sessionInfo.getIncomingInterfaces().stream()
        .map(
            inIface -> {
              StateExpr preState = new PreInInterface(hostname, inIface);
              BDD sessionFlows = sessionInfo.getSessionFlows();
              BDD inAclBdd = getIncomingingSessionFilterBdd(hostname, inIface);

              // TODO need drop edges for session ACLs
              Transition transition =
                  compose(
                      constraint(sessionFlows.and(inAclBdd)),
                      sessionInfo.getTransformation(),
                      outAcl,
                      removeSourceConstraint(srcMgr),
                      removeLastHopConstraint(_lastHopMgr, hostname),
                      addSourceIfaceConstraint,
                      addLastHop);
              return new Edge(preState, postState, transition);
            });
  }

  @VisibleForTesting
  Stream<Edge> nodeDropAclOutEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    if (_filterBdds == null) {
      // ignoreAcls
      return Stream.of();
    }

    String outIface = sessionInfo.getOutgoingInterface();

    if (outIface == null) {
      return Stream.of();
    }

    String hostname = sessionInfo.getHostname();
    BDDSourceManager srcMgr = _srcMgrs.get(hostname);
    StateExpr postState = new NodeDropAclOut(hostname);
    BDD sessionFlows = sessionInfo.getSessionFlows();

    Transition denyOutAcl = constraint(getOutgoingSessionFilterBdd(hostname, outIface).not());

    return sessionInfo.getIncomingInterfaces().stream()
        .map(
            inIface -> {
              StateExpr preState = new PreInInterface(hostname, inIface);
              BDD inAclBdd = getIncomingingSessionFilterBdd(hostname, inIface);

              Transition transition =
                  compose(
                      constraint(sessionFlows.and(inAclBdd)),
                      sessionInfo.getTransformation(),
                      denyOutAcl,
                      removeSourceConstraint(srcMgr),
                      removeLastHopConstraint(_lastHopMgr, hostname));
              return new Edge(preState, postState, transition);
            });
  }

  @VisibleForTesting
  Stream<Edge> nodeInterfaceDeliveredToSubnetEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    checkArgument(
        sessionInfo.getOutgoingInterface() != null && sessionInfo.getNextHop() == null,
        "Not a delivered to subnet session");
    String hostname = sessionInfo.getHostname();
    String outIface = sessionInfo.getOutgoingInterface();
    StateExpr postState = new NodeInterfaceDeliveredToSubnet(hostname, outIface);
    Transition outAcl = constraint(getOutgoingSessionFilterBdd(hostname, outIface));
    return sessionInfo.getIncomingInterfaces().stream()
        .map(
            inIface -> {
              StateExpr preState = new PreInInterface(hostname, inIface);
              BDD inAclBdd = getIncomingingSessionFilterBdd(hostname, inIface);
              BDD sessionFlows = sessionInfo.getSessionFlows();

              /* Don't remove the source interface/last hop constraints here. They get removed in
               * the transition from NodeInterfaceDeliveredToSubnet to DeliveredToSubnet.
               */
              Transition transition =
                  compose(
                      constraint(sessionFlows.and(inAclBdd)),
                      sessionInfo.getTransformation(),
                      outAcl);
              return new Edge(preState, postState, transition);
            });
  }

  @VisibleForTesting
  Stream<Edge> nodeAcceptEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    checkArgument(
        sessionInfo.getOutgoingInterface() == null && sessionInfo.getNextHop() == null,
        "Not an accept session");
    String hostname = sessionInfo.getHostname();
    BDDSourceManager srcMgr = _srcMgrs.get(hostname);

    StateExpr postState = new NodeAccept(hostname);
    return sessionInfo.getIncomingInterfaces().stream()
        .map(
            inIface -> {
              StateExpr preState = new PreInInterface(hostname, inIface);
              BDD inAclBdd = getIncomingingSessionFilterBdd(hostname, inIface);

              Transition transition =
                  compose(
                      constraint(sessionInfo.getSessionFlows().and(inAclBdd)),
                      sessionInfo.getTransformation(),
                      removeSourceConstraint(srcMgr),
                      removeLastHopConstraint(_lastHopMgr, hostname));
              return new Edge(preState, postState, transition);
            });
  }

  @VisibleForTesting
  Stream<Edge> nodeDropAclInEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    if (_filterBdds == null) {
      // ignoreAcls
      return Stream.of();
    }

    String hostname = sessionInfo.getHostname();
    BDDSourceManager srcMgr = _srcMgrs.get(hostname);

    StateExpr postState = new NodeDropAclIn(hostname);
    BDD sessionFlows = sessionInfo.getSessionFlows();
    return sessionInfo.getIncomingInterfaces().stream()
        .map(
            inIface -> {
              StateExpr preState = new PreInInterface(hostname, inIface);
              BDD inAclDenyBdd = getIncomingingSessionFilterBdd(hostname, inIface).not();

              Transition transition =
                  compose(
                      constraint(sessionFlows.and(inAclDenyBdd)),
                      sessionInfo.getTransformation(),
                      removeSourceConstraint(srcMgr),
                      removeLastHopConstraint(_lastHopMgr, hostname));
              return new Edge(preState, postState, transition);
            });
  }

  private BDD getIncomingingSessionFilterBdd(String hostname, String inIface) {
    return getSessionFilterBDD(hostname, inIface, FirewallSessionInterfaceInfo::getIncomingAclName);
  }

  private BDD getOutgoingSessionFilterBdd(String hostname, String outIface) {
    return getSessionFilterBDD(
        hostname, outIface, FirewallSessionInterfaceInfo::getOutgoingAclName);
  }

  private BDD getSessionFilterBDD(
      String hostname, String outIface, Function<FirewallSessionInterfaceInfo, String> getter) {
    if (_filterBdds == null) {
      // ignoreFilters
      return _one;
    }
    FirewallSessionInterfaceInfo sessionInfo =
        _configs.get(hostname).getAllInterfaces().get(outIface).getFirewallSessionInterfaceInfo();
    if (sessionInfo == null) {
      // interface does not have session configuration
      return _one;
    }
    String aclName = getter.apply(sessionInfo);
    return aclName == null ? _one : _filterBdds.get(hostname).get(aclName).get();
  }

  /**
   * Convert forward-pass origination constraints to return-pass query constraints. For a
   * bidirectional query, we want return flows that make it back to corresponding origination
   * points. This method defines that correspondence: it connects both the forward and return flows
   * (by swapping source and destination fields) and the origination states in the forward pass
   * graph to termination states in the return pass graph.
   */
  public static Map<StateExpr, BDD> returnPassQueryConstraints(
      BDDPacket packet, Map<StateExpr, BDD> forwardPassOriginationConstraints) {
    return forwardPassOriginationConstraints.entrySet().stream()
        .flatMap(
            entry -> {
              List<StateExpr> terminationStates =
                  originationStateToTerminationState(entry.getKey());
              if (terminationStates == null) {
                return Stream.of();
              }

              BDD queryConstraint = packet.swapSourceAndDestinationFields(entry.getValue());
              return terminationStates.stream()
                  .map(state -> Maps.immutableEntry(state, queryConstraint));
            })
        .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue, BDD::or));
  }
}
