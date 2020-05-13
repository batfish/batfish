package org.batfish.bddreachability;

import static org.batfish.bddreachability.EdgeMatchers.hasPostState;
import static org.batfish.bddreachability.EdgeMatchers.hasPreState;
import static org.batfish.bddreachability.EdgeMatchers.hasTransition;
import static org.batfish.bddreachability.TransitionMatchers.mapsBackward;
import static org.batfish.bddreachability.TransitionMatchers.mapsForward;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.bddreachability.transition.Transitions;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.flow.IncomingSessionScope;
import org.batfish.datamodel.flow.OriginatingSessionScope;
import org.batfish.symbolic.state.OriginateInterface;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.PostInVrfSession;
import org.batfish.symbolic.state.PreInInterface;
import org.hamcrest.Matcher;
import org.junit.Test;

public class SessionScopeFibLookupSessionEdgesTest {
  private static final BDDPacket PKT = new BDDPacket();
  private static final BDD ONE = PKT.getFactory().one();
  private static final BDD SESSION_HEADERS = PKT.getDstIp().value(10L);
  private static final BDD POOL_BDD = PKT.getSrcIp().value(10L);
  private static final String HOSTNAME = "node";
  private static final String VRF_NAME = "vrf";
  private static final String IFACE_NAME = "iface";
  private static final Interface IFACE =
      Interface.builder().setName(IFACE_NAME).setVrf(new Vrf(VRF_NAME)).build();

  private static final BDDSourceManager SRC_MGR =
      BDDSourceManager.forInterfaces(PKT, ImmutableSet.of(IFACE_NAME));
  private static final Transition TRANSFORMATION =
      Transitions.eraseAndSet(PKT.getSrcIp(), POOL_BDD);

  /**
   * Visitor set up with hostname and interface defined above, with session headers SESSION_HEADERS
   * and transformation TRANSFORMATION
   */
  private static final SessionScopeFibLookupSessionEdges VISITOR =
      new SessionScopeFibLookupSessionEdges(
          HOSTNAME, ImmutableMap.of(IFACE_NAME, IFACE), SESSION_HEADERS, TRANSFORMATION, SRC_MGR);

  @Test
  public void testIncomingSessionScope() {
    // Applying visitor to IncomingSessionScope for the known interface should give edge from
    // PreInInterface to PostInVrf
    IncomingSessionScope scope = new IncomingSessionScope(ImmutableSet.of(IFACE_NAME));
    Edge expectedEdge =
        new Edge(
            new PreInInterface(HOSTNAME, IFACE_NAME),
            new PostInVrfSession(HOSTNAME, VRF_NAME),
            // TODO Currently no source or transformation constraint here, but there should be.
            SESSION_HEADERS);
    List<Edge> actualEdges = scope.accept(VISITOR).collect(ImmutableList.toImmutableList());
    assertThat(actualEdges, contains(expectedEdge));
  }

  @Test
  public void testOriginatingSessionScope() {
    // Applying visitor to OriginatingSessionScope for the known VRF should give edges from
    // OriginateInterface and OriginateVrf to PostInVrf
    OriginatingSessionScope scope = new OriginatingSessionScope(VRF_NAME);
    List<Edge> edges = scope.accept(VISITOR).collect(ImmutableList.toImmutableList());

    Matcher<Transition> expectedTransition =
        allOf(
            mapsForward(
                ONE, SESSION_HEADERS.and(POOL_BDD).and(SRC_MGR.getOriginatingFromDeviceBDD())),
            mapsBackward(ONE, SESSION_HEADERS));
    assertThat(
        edges,
        containsInAnyOrder(
            allOf(
                hasPreState(new OriginateInterface(HOSTNAME, IFACE_NAME)),
                hasPostState(new PostInVrfSession(HOSTNAME, VRF_NAME)),
                hasTransition(expectedTransition)),
            allOf(
                hasPreState(new OriginateVrf(HOSTNAME, VRF_NAME)),
                hasPostState(new PostInVrfSession(HOSTNAME, VRF_NAME)),
                hasTransition(expectedTransition))));
  }
}
