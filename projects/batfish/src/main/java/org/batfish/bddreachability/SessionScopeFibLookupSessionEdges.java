package org.batfish.bddreachability;

import static org.batfish.bddreachability.transition.Transitions.addOriginatingFromDeviceConstraint;
import static org.batfish.bddreachability.transition.Transitions.addSourceInterfaceConstraint;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.flow.IncomingSessionScope;
import org.batfish.datamodel.flow.OriginatingSessionScope;
import org.batfish.datamodel.flow.PostNatFibLookup;
import org.batfish.datamodel.flow.SessionScope;
import org.batfish.datamodel.flow.SessionScopeVisitor;
import org.batfish.symbolic.state.OriginateInterface;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.PostInVrfSession;
import org.batfish.symbolic.state.PreInInterface;
import org.batfish.symbolic.state.StateExpr;

/**
 * Visitor for a {@link SessionScope} that returns the {@link Edge edges} that flows can traverse
 * when they match a session with action {@link PostNatFibLookup}. These edges all terminate in
 * {@link PostInVrfSession}, and may start with {@link PreInInterface} (for {@link
 * IncomingSessionScope}) or {@link OriginateInterface} or {@link OriginateVrf} (for {@link
 * OriginatingSessionScope}).
 */
@ParametersAreNonnullByDefault
public class SessionScopeFibLookupSessionEdges implements SessionScopeVisitor<Stream<Edge>> {
  private final String _hostname;
  private final Map<String, Interface> _ifaces;
  private final BDD _sessionFlows;
  private final Transition _transformation;
  private final BDDSourceManager _srcMgr;
  private final SessionEdgePreStates _sessionEdgePreStates;

  SessionScopeFibLookupSessionEdges(
      String hostname,
      Map<String, Interface> ifaces,
      BDD sessionFlows,
      Transition transformation,
      BDDSourceManager srcMgr) {
    _hostname = hostname;
    _ifaces = ImmutableMap.copyOf(ifaces);
    _sessionFlows = sessionFlows;
    _transformation = transformation;
    _srcMgr = srcMgr;
    _sessionEdgePreStates = new SessionEdgePreStates(_hostname, _ifaces.values());
  }

  @Override
  public Stream<Edge> visitIncomingSessionScope(IncomingSessionScope incomingSessionScope) {
    return incomingSessionScope.getIncomingInterfaces().stream()
        .map(
            incomingInterface ->
                new Edge(
                    new PreInInterface(_hostname, incomingInterface),
                    new PostInVrfSession(
                        _hostname, _ifaces.get(incomingInterface).getVrf().getName()),
                    compose(
                        constraint(_sessionFlows),
                        _transformation,
                        addSourceInterfaceConstraint(_srcMgr, incomingInterface))));
  }

  @Override
  public Stream<Edge> visitOriginatingSessionScope(
      OriginatingSessionScope originatingSessionScope) {
    // Create edges for originating flows to match this session
    String vrf = originatingSessionScope.getOriginatingVrf();
    StateExpr postState = new PostInVrfSession(_hostname, vrf);

    // Build transition for the session-matching edges.
    // Keep this transition in sync with transition on OriginateVrf->PostInVrf edge, defined in
    // BDDReachabilityAnalysisFactory#generateRootEdges_OriginateVrf_PostInVrf.
    Transition transition =
        compose(
            constraint(_sessionFlows),
            _transformation,
            addOriginatingFromDeviceConstraint(_srcMgr));

    // Create an edge per preceding state.
    return _sessionEdgePreStates
        .visitOriginatingSessionScope(originatingSessionScope)
        .map(preState -> new Edge(preState, postState, transition));
  }
}
