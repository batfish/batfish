package org.batfish.bddreachability;

import static org.batfish.bddreachability.BDDOutgoingOriginalFlowFilterManager.forNetwork;
import static org.batfish.bddreachability.EdgeMatchers.edge;
import static org.batfish.bddreachability.LastHopOutgoingInterfaceManager.NO_LAST_HOP;
import static org.batfish.bddreachability.TransitionMatchers.mapsBackward;
import static org.batfish.bddreachability.TransitionMatchers.mapsForward;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.bddreachability.transition.Transitions;
import org.batfish.common.bdd.BDDFiniteDomain;
import org.batfish.common.bdd.BDDInteger;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.FirewallSessionInterfaceInfo.Action;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.Accept;
import org.batfish.datamodel.flow.ForwardOutInterface;
import org.batfish.datamodel.flow.OriginatingSessionScope;
import org.batfish.datamodel.flow.PostNatFibLookup;
import org.batfish.symbolic.state.NodeAccept;
import org.batfish.symbolic.state.NodeDropAclIn;
import org.batfish.symbolic.state.NodeDropAclOut;
import org.batfish.symbolic.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.symbolic.state.OriginateInterface;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.PostInVrfSession;
import org.batfish.symbolic.state.PreInInterface;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link SessionInstrumentation}. */
public final class SessionInstrumentationTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private static final String LAST_HOP_VAR_NAME = "lastHop";

  // hostnames
  private static final String FW = "fw";
  private static final String SOURCE1 = "source1";
  private static final String SOURCE2 = "source2";

  // vrf names
  private static final String FW_VRF = FW + ":VRF";

  // interface names
  private static final String FW_I1 = "FW:I1";
  private static final String SOURCE1_IFACE = "SOURCE1:IFACE";
  private static final String SOURCE2_IFACE = "SOURCE2:IFACE";
  private static final String FAKE_IFACE = "FAKE_IFACE";

  // ACLs
  private static final String PERMIT_TCP = "permit tcp";

  // BDD stuff
  private final BDDPacket _pkt = new BDDPacket();
  private final BDD _one = _pkt.getFactory().one();
  private final BDD _zero = _pkt.getFactory().zero();

  @Rule public ExpectedException _exception = ExpectedException.none();

  private BDDSourceManager _fwSrcMgr;
  private BDDSourceManager _source1SrcMgr;
  private Map<String, BDDSourceManager> _srcMgrs;
  private Map<String, BDDOutgoingOriginalFlowFilterManager> _outgoingOriginalFlowFilterMgrs;

  private LastHopOutgoingInterfaceManager _lastHopMgr;
  private Map<String, Map<String, Supplier<BDD>>> _filterBdds;

  private Configuration _fw;
  private Configuration _source1;
  private Configuration _source2;
  private BDD _invalidSrc;

  private Interface _fwI1;

  private BDD _permitTcpBdd;

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();

    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    // Setup FW
    {
      _fw = cb.setHostname(FW).build();
      Vrf vrf = nf.vrfBuilder().setOwner(_fw).setName(FW_VRF).build();
      Interface.Builder ib = nf.interfaceBuilder().setActive(true).setOwner(_fw).setVrf(vrf);
      _fwI1 = ib.setName(FW_I1).build();
    }

    // Setup source 1
    {
      _source1 = cb.setHostname(SOURCE1).build();
      Vrf vrf = nf.vrfBuilder().setOwner(_source1).build();
      Interface.Builder ib = nf.interfaceBuilder().setActive(true).setOwner(_source1).setVrf(vrf);
      ib.setName(SOURCE1_IFACE).build();
    }

    // Setup source 2
    {
      _source2 = cb.setHostname(SOURCE2).build();
      Vrf vrf = nf.vrfBuilder().setOwner(_source2).build();
      Interface.Builder ib = nf.interfaceBuilder().setActive(true).setOwner(_source2).setVrf(vrf);
      ib.setName(SOURCE2_IFACE).build();
    }

    // Setup last hop manager
    _lastHopMgr =
        new LastHopOutgoingInterfaceManager(
            _pkt,
            BDDFiniteDomain.domainsWithSharedVariable(
                _pkt,
                LAST_HOP_VAR_NAME,
                ImmutableMap.of(
                    NodeInterfacePair.of(FW, FW_I1),
                    ImmutableSet.of(
                        NO_LAST_HOP,
                        NodeInterfacePair.of(SOURCE1, SOURCE1_IFACE),
                        NodeInterfacePair.of(SOURCE2, SOURCE2_IFACE)))));

    // Setup source managers
    {
      BDDInteger srcVar = _pkt.allocateBDDInteger("Source", 3, false);
      // Setup source tracking for firewall
      _fwSrcMgr = BDDSourceManager.forInterfaces(srcVar, ImmutableSet.of(FW_I1, FAKE_IFACE));
      _invalidSrc = _fwSrcMgr.isValidValue().not();
      assert !_invalidSrc.isZero();

      _source1SrcMgr =
          BDDSourceManager.forInterfaces(srcVar, ImmutableSet.of(SOURCE1_IFACE, FAKE_IFACE));

      _srcMgrs = ImmutableMap.of(FW, _fwSrcMgr, SOURCE1, _source1SrcMgr);
      _outgoingOriginalFlowFilterMgrs =
          forNetwork(_pkt, ImmutableMap.of(FW, _fw, SOURCE1, _source1), _srcMgrs);
    }

    // Setup filter BDDs
    {
      _permitTcpBdd = _pkt.getIpProtocol().value(IpProtocol.TCP);
      _filterBdds = ImmutableMap.of(FW, ImmutableMap.of(PERMIT_TCP, () -> _permitTcpBdd));
    }
  }

  private Map<String, Configuration> configs() {
    return ImmutableMap.of(FW, _fw, SOURCE1, _source1, SOURCE2, _source2);
  }

  private List<Edge> deliveredToSubnetEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    return new SessionInstrumentation(
            _pkt, configs(), _srcMgrs, _lastHopMgr, _outgoingOriginalFlowFilterMgrs, _filterBdds)
        .nodeInterfaceDeliveredToSubnetEdges(sessionInfo)
        .collect(ImmutableList.toImmutableList());
  }

  private List<Edge> nodeAcceptEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    return new SessionInstrumentation(
            _pkt, configs(), _srcMgrs, _lastHopMgr, _outgoingOriginalFlowFilterMgrs, _filterBdds)
        .nodeAcceptEdges(sessionInfo)
        .collect(ImmutableList.toImmutableList());
  }

  private List<Edge> nodeDropAclInEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    return new SessionInstrumentation(
            _pkt, configs(), _srcMgrs, _lastHopMgr, _outgoingOriginalFlowFilterMgrs, _filterBdds)
        .nodeDropAclInEdges(sessionInfo)
        .collect(ImmutableList.toImmutableList());
  }

  private List<Edge> nodeDropAclOutEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    return new SessionInstrumentation(
            _pkt, configs(), _srcMgrs, _lastHopMgr, _outgoingOriginalFlowFilterMgrs, _filterBdds)
        .nodeDropAclOutEdges(sessionInfo)
        .collect(ImmutableList.toImmutableList());
  }

  private List<Edge> fibLookupSessionEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    return new SessionInstrumentation(
            _pkt, configs(), _srcMgrs, _lastHopMgr, _outgoingOriginalFlowFilterMgrs, _filterBdds)
        .fibLookupSessionEdges(sessionInfo)
        .collect(ImmutableList.toImmutableList());
  }

  private List<Edge> preInInterfaceEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    return new SessionInstrumentation(
            _pkt, configs(), _srcMgrs, _lastHopMgr, _outgoingOriginalFlowFilterMgrs, _filterBdds)
        .preInInterfaceEdges(sessionInfo)
        .collect(ImmutableList.toImmutableList());
  }

  @Test
  public void testNodeAcceptEdges() {
    BDD sessionHeaders = _pkt.getDstIp().value(10L);
    BDD srcFwI1 = _fwSrcMgr.getSourceInterfaceBDD(FW_I1);
    BDD validSrc = _fwSrcMgr.isValidValue();
    BDD lastHop1 = _lastHopMgr.getLastHopOutgoingInterfaceBdd(SOURCE1, SOURCE1_IFACE, FW, FW_I1);
    BDD noLastHop = _lastHopMgr.getNoLastHopOutgoingInterfaceBdd(FW, FW_I1);

    BDDFirewallSessionTraceInfo sessionInfo =
        new BDDFirewallSessionTraceInfo(
            FW, ImmutableSet.of(FW_I1), Accept.INSTANCE, sessionHeaders, IDENTITY);

    // No transformation, no ACLs
    assertThat(
        nodeAcceptEdges(sessionInfo),
        contains(
            edge(
                new PreInInterface(FW, FW_I1),
                new NodeAccept(FW),
                allOf(
                    mapsForward(srcFwI1, sessionHeaders),
                    mapsForward(srcFwI1.and(noLastHop), sessionHeaders),
                    mapsForward(srcFwI1.and(lastHop1), sessionHeaders),
                    mapsBackward(_one, validSrc.and(sessionHeaders))))));

    // FW_I1 has an incoming session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE, ImmutableList.of(FW_I1), PERMIT_TCP, null));
    assertThat(
        nodeAcceptEdges(sessionInfo),
        contains(
            edge(
                new PreInInterface(FW, FW_I1),
                new NodeAccept(FW),
                allOf(
                    mapsForward(srcFwI1, sessionHeaders.and(_permitTcpBdd)),
                    mapsForward(srcFwI1.and(noLastHop), sessionHeaders.and(_permitTcpBdd)),
                    mapsForward(srcFwI1.and(lastHop1), sessionHeaders.and(_permitTcpBdd)),
                    mapsBackward(_one, validSrc.and(sessionHeaders).and(_permitTcpBdd))))));
    _fwI1.setFirewallSessionInterfaceInfo(null);

    // Session has a transformation
    {
      BDD poolBdd = _pkt.getSrcIp().value(10L);
      Transition nat = Transitions.eraseAndSet(_pkt.getSrcIp(), poolBdd);
      BDDFirewallSessionTraceInfo natSessionInfo =
          new BDDFirewallSessionTraceInfo(
              FW, ImmutableSet.of(FW_I1), Accept.INSTANCE, sessionHeaders, nat);
      assertThat(
          nodeAcceptEdges(natSessionInfo),
          contains(
              edge(
                  new PreInInterface(FW, FW_I1),
                  new NodeAccept(FW),
                  allOf(
                      mapsForward(srcFwI1, sessionHeaders.and(poolBdd)),
                      mapsForward(srcFwI1.and(noLastHop), sessionHeaders.and(poolBdd)),
                      mapsForward(srcFwI1.and(lastHop1), sessionHeaders.and(poolBdd)),
                      mapsBackward(_one, validSrc.and(sessionHeaders)),
                      mapsBackward(poolBdd.not(), _zero)))));
    }
  }

  @Test
  public void testFibLookupSessionEdges() {
    BDD sessionHeaders = _pkt.getDstIp().value(10L);
    BDDFirewallSessionTraceInfo sessionInfo =
        new BDDFirewallSessionTraceInfo(
            FW, ImmutableSet.of(FW_I1), PostNatFibLookup.INSTANCE, sessionHeaders, IDENTITY);

    assertThat(
        fibLookupSessionEdges(sessionInfo),
        contains(
            edge(
                new PreInInterface(FW, FW_I1),
                new PostInVrfSession(FW, FW_VRF),
                allOf(
                    mapsForward(_one, sessionHeaders.and(_fwSrcMgr.getSourceInterfaceBDD(FW_I1))),
                    mapsBackward(_one, sessionHeaders)))));
  }

  @Test
  public void testFibLookupSessionEdges_transformation() {
    BDD sessionHeaders = _pkt.getDstIp().value(10L);
    BDD poolBdd = _pkt.getSrcIp().value(10L);
    Transition nat = Transitions.eraseAndSet(_pkt.getSrcIp(), poolBdd);
    BDDFirewallSessionTraceInfo sessionInfo =
        new BDDFirewallSessionTraceInfo(
            FW, ImmutableSet.of(FW_I1), PostNatFibLookup.INSTANCE, sessionHeaders, nat);

    assertThat(
        fibLookupSessionEdges(sessionInfo),
        contains(
            edge(
                new PreInInterface(FW, FW_I1),
                new PostInVrfSession(FW, FW_VRF),
                allOf(
                    mapsForward(
                        _one,
                        sessionHeaders.and(poolBdd).and(_fwSrcMgr.getSourceInterfaceBDD(FW_I1))),
                    mapsBackward(_one, sessionHeaders)))));
  }

  @Test
  public void testFibLookupSessionEdges_inboundSession() {
    BDD sessionHeaders = _pkt.getDstIp().value(10L);
    BDDFirewallSessionTraceInfo sessionInfo =
        new BDDFirewallSessionTraceInfo(
            FW,
            new OriginatingSessionScope(FW_VRF),
            PostNatFibLookup.INSTANCE,
            sessionHeaders,
            IDENTITY);

    BDD originating = _fwSrcMgr.getOriginatingFromDeviceBDD();
    Matcher<Transition> expectedTransition =
        allOf(
            mapsForward(_one, sessionHeaders.and(originating)), mapsBackward(_one, sessionHeaders));
    assertThat(
        fibLookupSessionEdges(sessionInfo),
        containsInAnyOrder(
            edge(
                new OriginateInterface(FW, FW_I1),
                new PostInVrfSession(FW, FW_VRF),
                expectedTransition),
            edge(
                new OriginateVrf(FW, FW_VRF),
                new PostInVrfSession(FW, FW_VRF),
                expectedTransition)));
  }

  @Test
  public void testFibLookupSessionEdges_inboundSessionWithTransformation() {
    BDD sessionHeaders = _pkt.getDstIp().value(10L);
    BDD poolBdd = _pkt.getSrcIp().value(10L);
    Transition nat = Transitions.eraseAndSet(_pkt.getSrcIp(), poolBdd);
    BDDFirewallSessionTraceInfo natSessionInfo =
        new BDDFirewallSessionTraceInfo(
            FW,
            new OriginatingSessionScope(FW_VRF),
            PostNatFibLookup.INSTANCE,
            sessionHeaders,
            nat);

    BDD originating = _fwSrcMgr.getOriginatingFromDeviceBDD();
    Matcher<Transition> expectedTransition =
        allOf(
            mapsForward(_one, sessionHeaders.and(originating).and(poolBdd)),
            mapsBackward(_one, sessionHeaders));
    assertThat(
        fibLookupSessionEdges(natSessionInfo),
        containsInAnyOrder(
            edge(
                new OriginateInterface(FW, FW_I1),
                new PostInVrfSession(FW, FW_VRF),
                expectedTransition),
            edge(
                new OriginateVrf(FW, FW_VRF),
                new PostInVrfSession(FW, FW_VRF),
                expectedTransition)));
  }

  @Test
  public void testPreInInterfaceEdges() {
    BDD sessionHeaders = _pkt.getDstIp().value(10L);
    BDD srcFwI1 = _fwSrcMgr.getSourceInterfaceBDD(FW_I1);
    BDD validSrc = _fwSrcMgr.isValidValue();
    BDD lastHop1 = _lastHopMgr.getLastHopOutgoingInterfaceBdd(SOURCE1, SOURCE1_IFACE, FW, FW_I1);
    BDD noLastHop = _lastHopMgr.getNoLastHopOutgoingInterfaceBdd(FW, FW_I1);

    BDD fakeIface = _source1SrcMgr.getSourceInterfaceBDD(FAKE_IFACE);
    BDD source1Iface = _source1SrcMgr.getSourceInterfaceBDD(SOURCE1_IFACE);

    BDDFirewallSessionTraceInfo sessionInfo =
        new BDDFirewallSessionTraceInfo(
            FW,
            ImmutableSet.of(FW_I1),
            new ForwardOutInterface(FW_I1, NodeInterfacePair.of(SOURCE1, SOURCE1_IFACE)),
            sessionHeaders,
            IDENTITY);

    // No transformation, no ACLs
    assertThat(
        preInInterfaceEdges(sessionInfo),
        contains(
            edge(
                new PreInInterface(FW, FW_I1),
                new PreInInterface(SOURCE1, SOURCE1_IFACE),
                allOf(
                    mapsForward(srcFwI1, sessionHeaders.and(source1Iface)),
                    mapsForward(srcFwI1.and(noLastHop), sessionHeaders.and(source1Iface)),
                    mapsForward(srcFwI1.and(lastHop1), sessionHeaders.and(source1Iface)),
                    mapsBackward(source1Iface, validSrc.and(sessionHeaders)),
                    mapsBackward(fakeIface, _zero)))));

    // FW_I1 has an incoming session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE, ImmutableList.of(FW_I1), PERMIT_TCP, null));
    assertThat(
        preInInterfaceEdges(sessionInfo),
        contains(
            edge(
                new PreInInterface(FW, FW_I1),
                new PreInInterface(SOURCE1, SOURCE1_IFACE),
                allOf(
                    mapsForward(srcFwI1, sessionHeaders.and(_permitTcpBdd).and(source1Iface)),
                    mapsForward(
                        srcFwI1.and(noLastHop),
                        sessionHeaders.and(_permitTcpBdd).and(source1Iface)),
                    mapsForward(
                        srcFwI1.and(lastHop1), sessionHeaders.and(_permitTcpBdd).and(source1Iface)),
                    mapsBackward(source1Iface, validSrc.and(sessionHeaders).and(_permitTcpBdd)),
                    mapsBackward(fakeIface, _zero)))));
    _fwI1.setFirewallSessionInterfaceInfo(null);

    // FW_I1 has an outgoing session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE, ImmutableList.of(FW_I1), null, PERMIT_TCP));
    assertThat(
        preInInterfaceEdges(sessionInfo),
        contains(
            edge(
                new PreInInterface(FW, FW_I1),
                new PreInInterface(SOURCE1, SOURCE1_IFACE),
                allOf(
                    mapsForward(srcFwI1, sessionHeaders.and(_permitTcpBdd).and(source1Iface)),
                    mapsForward(
                        srcFwI1.and(noLastHop),
                        sessionHeaders.and(_permitTcpBdd).and(source1Iface)),
                    mapsForward(
                        srcFwI1.and(lastHop1), sessionHeaders.and(_permitTcpBdd).and(source1Iface)),
                    mapsBackward(source1Iface, validSrc.and(sessionHeaders).and(_permitTcpBdd)),
                    mapsBackward(fakeIface, _zero)))));
    _fwI1.setFirewallSessionInterfaceInfo(null);

    // Session has a transformation
    {
      BDD poolBdd = _pkt.getSrcIp().value(10L);
      Transition nat = Transitions.eraseAndSet(_pkt.getSrcIp(), poolBdd);
      BDDFirewallSessionTraceInfo natSessionInfo =
          new BDDFirewallSessionTraceInfo(
              FW,
              ImmutableSet.of(FW_I1),
              new ForwardOutInterface(FW_I1, NodeInterfacePair.of(SOURCE1, SOURCE1_IFACE)),
              sessionHeaders,
              nat);
      assertThat(
          preInInterfaceEdges(natSessionInfo),
          contains(
              edge(
                  new PreInInterface(FW, FW_I1),
                  new PreInInterface(SOURCE1, SOURCE1_IFACE),
                  allOf(
                      mapsForward(srcFwI1, sessionHeaders.and(poolBdd).and(source1Iface)),
                      mapsForward(
                          srcFwI1.and(noLastHop), sessionHeaders.and(poolBdd).and(source1Iface)),
                      mapsForward(
                          srcFwI1.and(lastHop1), sessionHeaders.and(poolBdd).and(source1Iface)),
                      mapsBackward(source1Iface, validSrc.and(sessionHeaders)),
                      mapsBackward(source1Iface.and(poolBdd.not()), _zero),
                      mapsBackward(fakeIface, _zero)))));
    }
  }

  @Test
  public void testDeliveredToSubnetEdges() {
    BDD sessionHeaders = _pkt.getDstIp().value(10L);
    BDD srcFwI1 = _fwSrcMgr.getSourceInterfaceBDD(FW_I1);
    BDD lastHop1 = _lastHopMgr.getLastHopOutgoingInterfaceBdd(SOURCE1, SOURCE1_IFACE, FW, FW_I1);
    BDD noLastHop = _lastHopMgr.getNoLastHopOutgoingInterfaceBdd(FW, FW_I1);

    BDDFirewallSessionTraceInfo sessionInfo =
        new BDDFirewallSessionTraceInfo(
            FW,
            ImmutableSet.of(FW_I1),
            new ForwardOutInterface(FW_I1, null),
            sessionHeaders,
            IDENTITY);

    // No transformation, no ACLs
    List<Edge> actual = deliveredToSubnetEdges(sessionInfo);
    assertThat(
        actual,
        contains(
            edge(
                new PreInInterface(FW, FW_I1),
                new NodeInterfaceDeliveredToSubnet(FW, FW_I1),
                allOf(
                    mapsForward(srcFwI1, sessionHeaders.and(srcFwI1)),
                    mapsForward(srcFwI1.and(noLastHop), sessionHeaders.and(srcFwI1).and(noLastHop)),
                    mapsForward(srcFwI1.and(lastHop1), sessionHeaders.and(srcFwI1).and(lastHop1)),
                    mapsBackward(_one, sessionHeaders)))));

    // FW_I1 has an incoming session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE, ImmutableList.of(FW_I1), PERMIT_TCP, null));
    assertThat(
        deliveredToSubnetEdges(sessionInfo),
        contains(
            edge(
                new PreInInterface(FW, FW_I1),
                new NodeInterfaceDeliveredToSubnet(FW, FW_I1),
                allOf(
                    mapsForward(_one, sessionHeaders.and(_permitTcpBdd)),
                    mapsBackward(_one, sessionHeaders.and(_permitTcpBdd))))));
    _fwI1.setFirewallSessionInterfaceInfo(null);

    // FW_I1 has an outgoing session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE, ImmutableList.of(FW_I1), null, PERMIT_TCP));
    assertThat(
        deliveredToSubnetEdges(sessionInfo),
        contains(
            edge(
                new PreInInterface(FW, FW_I1),
                new NodeInterfaceDeliveredToSubnet(FW, FW_I1),
                allOf(
                    mapsForward(_one, sessionHeaders.and(_permitTcpBdd)),
                    mapsBackward(_one, sessionHeaders.and(_permitTcpBdd))))));
    _fwI1.setFirewallSessionInterfaceInfo(null);

    // Session has a transformation
    {
      BDD poolBdd = _pkt.getSrcIp().value(10L);
      Transition nat = Transitions.eraseAndSet(_pkt.getSrcIp(), poolBdd);
      BDDFirewallSessionTraceInfo natSessionInfo =
          new BDDFirewallSessionTraceInfo(
              FW,
              ImmutableSet.of(FW_I1),
              new ForwardOutInterface(FW_I1, null),
              sessionHeaders,
              nat);
      assertThat(
          deliveredToSubnetEdges(natSessionInfo),
          contains(
              edge(
                  new PreInInterface(FW, FW_I1),
                  new NodeInterfaceDeliveredToSubnet(FW, FW_I1),
                  allOf(
                      mapsForward(_one, sessionHeaders.and(poolBdd)),
                      mapsForward(poolBdd.not(), sessionHeaders.and(poolBdd)),
                      mapsBackward(_one, sessionHeaders),
                      mapsBackward(poolBdd.not(), _zero)))));
    }
  }

  @Test
  public void testDropAclInEdges() {
    BDD sessionHeaders = _pkt.getDstIp().value(10L);
    BDD srcFwI1 = _fwSrcMgr.getSourceInterfaceBDD(FW_I1);
    BDD validSrc = _fwSrcMgr.isValidValue();

    BDDFirewallSessionTraceInfo sessionInfo =
        new BDDFirewallSessionTraceInfo(
            FW, ImmutableSet.of(FW_I1), Accept.INSTANCE, sessionHeaders, IDENTITY);

    // No ACLs
    assertThat(nodeDropAclInEdges(sessionInfo), empty());

    // FW_I1 has an incoming session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE, ImmutableList.of(FW_I1), PERMIT_TCP, null));
    assertThat(
        nodeDropAclInEdges(sessionInfo),
        contains(
            edge(
                new PreInInterface(FW, FW_I1),
                new NodeDropAclIn(FW),
                allOf(
                    mapsForward(srcFwI1, sessionHeaders.and(_permitTcpBdd.not())),
                    mapsBackward(_one, validSrc.and(sessionHeaders).and(_permitTcpBdd.not()))))));
    _fwI1.setFirewallSessionInterfaceInfo(null);
  }

  @Test
  public void testDropAclOutEdges() {
    BDD sessionHeaders = _pkt.getDstIp().value(10L);
    BDD srcFwI1 = _fwSrcMgr.getSourceInterfaceBDD(FW_I1);
    BDD validSrc = _fwSrcMgr.isValidValue();

    BDDFirewallSessionTraceInfo sessionInfo =
        new BDDFirewallSessionTraceInfo(
            FW,
            ImmutableSet.of(FW_I1),
            new ForwardOutInterface(FW_I1, null),
            sessionHeaders,
            IDENTITY);

    // No ACLs
    assertThat(nodeDropAclOutEdges(sessionInfo), empty());

    // FW_I1 has an outgoing session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE, ImmutableList.of(FW_I1), null, PERMIT_TCP));
    assertThat(
        nodeDropAclOutEdges(sessionInfo),
        contains(
            edge(
                new PreInInterface(FW, FW_I1),
                new NodeDropAclOut(FW),
                allOf(
                    mapsForward(srcFwI1, sessionHeaders.and(_permitTcpBdd.not())),
                    mapsBackward(_one, validSrc.and(sessionHeaders).and(_permitTcpBdd.not()))))));
    _fwI1.setFirewallSessionInterfaceInfo(null);
  }
}
