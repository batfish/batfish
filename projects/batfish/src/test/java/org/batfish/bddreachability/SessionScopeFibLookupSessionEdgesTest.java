package org.batfish.bddreachability;

import static org.batfish.bddreachability.EdgeMatchers.edge;
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
import org.batfish.datamodel.TestInterface;
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
  private static final String HOSTNAME = "node";
  private static final String VRF_NAME = "vrf";
  private static final String IFACE_NAME = "iface";
  private static final Interface IFACE =
      TestInterface.builder().setName(IFACE_NAME).setVrf(new Vrf(VRF_NAME)).build();

  private final BDDPacket _pkt = new BDDPacket();
  private final BDD _one = _pkt.getFactory().one();
  private final BDD _sessionHeaders = _pkt.getDstIp().value(10L);
  private final BDD _poolBdd = _pkt.getSrcIp().value(10L);
  private final BDDSourceManager _srcMgr =
      BDDSourceManager.forInterfaces(_pkt, ImmutableSet.of(IFACE_NAME));
  private final Transition _transformation = Transitions.eraseAndSet(_pkt.getSrcIp(), _poolBdd);

  /**
   * Visitor set up with hostname and interface defined above, with session headers SESSION_HEADERS
   * and transformation TRANSFORMATION
   */
  private final SessionScopeFibLookupSessionEdges _visitor =
      new SessionScopeFibLookupSessionEdges(
          HOSTNAME, ImmutableMap.of(IFACE_NAME, IFACE), _sessionHeaders, _transformation, _srcMgr);

  @Test
  public void testIncomingSessionScope() {
    // Applying visitor to IncomingSessionScope for the known interface should give edge from
    // PreInInterface to PostInVrf
    IncomingSessionScope scope = new IncomingSessionScope(ImmutableSet.of(IFACE_NAME));
    List<Edge> actualEdges = scope.accept(_visitor).collect(ImmutableList.toImmutableList());
    BDD expectedForwardFlows =
        _sessionHeaders.and(_poolBdd).and(_srcMgr.getSourceInterfaceBDD(IFACE_NAME));
    assertThat(
        actualEdges,
        contains(
            edge(
                new PreInInterface(HOSTNAME, IFACE_NAME),
                new PostInVrfSession(HOSTNAME, VRF_NAME),
                allOf(
                    mapsForward(_one, expectedForwardFlows),
                    mapsBackward(_one, _sessionHeaders)))));
  }

  @Test
  public void testOriginatingSessionScope() {
    // Applying visitor to OriginatingSessionScope for the known VRF should give edges from
    // OriginateInterface and OriginateVrf to PostInVrf
    OriginatingSessionScope scope = new OriginatingSessionScope(VRF_NAME);
    List<Edge> edges = scope.accept(_visitor).collect(ImmutableList.toImmutableList());

    Matcher<Transition> expectedTransition =
        allOf(
            mapsForward(
                _one, _sessionHeaders.and(_poolBdd).and(_srcMgr.getOriginatingFromDeviceBDD())),
            mapsBackward(_one, _sessionHeaders));
    assertThat(
        edges,
        containsInAnyOrder(
            edge(
                new OriginateInterface(HOSTNAME, IFACE_NAME),
                new PostInVrfSession(HOSTNAME, VRF_NAME),
                expectedTransition),
            edge(
                new OriginateVrf(HOSTNAME, VRF_NAME),
                new PostInVrfSession(HOSTNAME, VRF_NAME),
                expectedTransition)));
  }
}
