package org.batfish.bddreachability;

import static org.batfish.bddreachability.EdgeMatchers.hasPostState;
import static org.batfish.bddreachability.EdgeMatchers.hasPreState;
import static org.batfish.bddreachability.EdgeMatchers.hasTransition;
import static org.batfish.bddreachability.LastHopOutgoingInterfaceManager.NO_LAST_HOP;
import static org.batfish.bddreachability.TransitionMatchers.mapsBackward;
import static org.batfish.bddreachability.TransitionMatchers.mapsForward;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;

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
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.z3.state.NodeAccept;
import org.batfish.z3.state.NodeDropAclIn;
import org.batfish.z3.state.NodeDropAclOut;
import org.batfish.z3.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.z3.state.PreInInterface;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Tests for {@link SessionInstrumentation}. */
public final class SessionInstrumentationTest {
  private static final String LAST_HOP_VAR_NAME = "lastHop";

  // hostnames
  private static String FW = "FW";
  private static String SOURCE1 = "SOURCE1";
  private static String SOURCE2 = "SOURCE2";

  // interface names
  private static String FW_I1 = "FW:I1";
  private static String SOURCE1_IFACE = "SOURCE1:IFACE";
  private static String SOURCE2_IFACE = "SOURCE2:IFACE";
  private static String FAKE_IFACE = "FAKE_IFACE";

  // ACLs
  private static String PERMIT_TCP = "permit tcp";

  // BDD stuff
  private static BDDPacket PKT = new BDDPacket();
  private static BDD ONE = PKT.getFactory().one();
  private static BDD ZERO = PKT.getFactory().zero();

  @Rule public ExpectedException _exception = ExpectedException.none();

  private BDDSourceManager _fwSrcMgr;
  private BDDSourceManager _source1SrcMgr;
  private Map<String, BDDSourceManager> _srcMgrs;

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
      Vrf vrf = nf.vrfBuilder().setOwner(_fw).build();
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
      _source2 = cb.setHostname(SOURCE1).build();
      Vrf vrf = nf.vrfBuilder().setOwner(_source2).build();
      Interface.Builder ib = nf.interfaceBuilder().setActive(true).setOwner(_source2).setVrf(vrf);
      ib.setName(SOURCE2_IFACE).build();
    }

    // Setup last hop manager
    _lastHopMgr =
        new LastHopOutgoingInterfaceManager(
            PKT,
            BDDFiniteDomain.domainsWithSharedVariable(
                PKT,
                LAST_HOP_VAR_NAME,
                ImmutableMap.of(
                    new NodeInterfacePair(FW, FW_I1),
                    ImmutableSet.of(
                        NO_LAST_HOP,
                        new NodeInterfacePair(SOURCE1, SOURCE1_IFACE),
                        new NodeInterfacePair(SOURCE2, SOURCE2_IFACE)))));

    // Setup source managers
    {
      BDDInteger srcVar = PKT.allocateBDDInteger("Source", 3, false);
      // Setup source tracking for firewall
      _fwSrcMgr = BDDSourceManager.forInterfaces(srcVar, ImmutableSet.of(FW_I1, FAKE_IFACE));
      _invalidSrc = _fwSrcMgr.isValidValue().not();
      assert !_invalidSrc.isZero();

      _source1SrcMgr =
          BDDSourceManager.forInterfaces(srcVar, ImmutableSet.of(SOURCE1_IFACE, FAKE_IFACE));

      _srcMgrs = ImmutableMap.of(FW, _fwSrcMgr, SOURCE1, _source1SrcMgr);
    }

    // Setup filter BDDs
    {
      _permitTcpBdd = PKT.getIpProtocol().value(IpProtocol.TCP.number());
      _filterBdds = ImmutableMap.of(FW, ImmutableMap.of(PERMIT_TCP, () -> _permitTcpBdd));
    }
  }

  private Map<String, Configuration> configs() {
    return ImmutableMap.of(FW, _fw, SOURCE1, _source1, SOURCE2, _source2);
  }

  private List<Edge> deliveredToSubnetEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    return new SessionInstrumentation(PKT, configs(), _srcMgrs, _lastHopMgr, _filterBdds)
        .nodeInterfaceDeliveredToSubnetEdges(sessionInfo)
        .collect(ImmutableList.toImmutableList());
  }

  private List<Edge> nodeAcceptEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    return new SessionInstrumentation(PKT, configs(), _srcMgrs, _lastHopMgr, _filterBdds)
        .nodeAcceptEdges(sessionInfo)
        .collect(ImmutableList.toImmutableList());
  }

  private List<Edge> nodeDropAclInEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    return new SessionInstrumentation(PKT, configs(), _srcMgrs, _lastHopMgr, _filterBdds)
        .nodeDropAclInEdges(sessionInfo)
        .collect(ImmutableList.toImmutableList());
  }

  private List<Edge> nodeDropAclOutEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    return new SessionInstrumentation(PKT, configs(), _srcMgrs, _lastHopMgr, _filterBdds)
        .nodeDropAclOutEdges(sessionInfo)
        .collect(ImmutableList.toImmutableList());
  }

  private List<Edge> preInInterfaceEdges(BDDFirewallSessionTraceInfo sessionInfo) {
    return new SessionInstrumentation(PKT, configs(), _srcMgrs, _lastHopMgr, _filterBdds)
        .preInInterfaceEdges(sessionInfo)
        .collect(ImmutableList.toImmutableList());
  }

  @Test
  public void testNodeAcceptEdges() {
    BDD sessionHeaders = PKT.getDstIp().value(10L);
    BDD srcFwI1 = _fwSrcMgr.getSourceInterfaceBDD(FW_I1);
    BDD validSrc = _fwSrcMgr.isValidValue();
    BDD lastHop1 = _lastHopMgr.getLastHopOutgoingInterfaceBdd(SOURCE1, SOURCE1_IFACE, FW, FW_I1);
    BDD noLastHop = _lastHopMgr.getNoLastHopOutgoingInterfaceBdd(FW, FW_I1);

    BDDFirewallSessionTraceInfo sessionInfo =
        new BDDFirewallSessionTraceInfo(
            FW, ImmutableSet.of(FW_I1), null, null, sessionHeaders, IDENTITY);

    // No transformation, no ACLs
    assertThat(
        nodeAcceptEdges(sessionInfo),
        contains(
            allOf(
                hasPreState(new PreInInterface(FW, FW_I1)),
                hasPostState(new NodeAccept(FW)),
                hasTransition(
                    allOf(
                        mapsForward(srcFwI1, sessionHeaders),
                        mapsForward(srcFwI1.and(noLastHop), sessionHeaders),
                        mapsForward(srcFwI1.and(lastHop1), sessionHeaders),
                        mapsBackward(ONE, validSrc.and(sessionHeaders)))))));

    // FW_I1 has an incoming session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(ImmutableList.of(FW_I1), PERMIT_TCP, null));
    assertThat(
        nodeAcceptEdges(sessionInfo),
        contains(
            allOf(
                hasPreState(new PreInInterface(FW, FW_I1)),
                hasPostState(new NodeAccept(FW)),
                hasTransition(
                    allOf(
                        mapsForward(srcFwI1, sessionHeaders.and(_permitTcpBdd)),
                        mapsForward(srcFwI1.and(noLastHop), sessionHeaders.and(_permitTcpBdd)),
                        mapsForward(srcFwI1.and(lastHop1), sessionHeaders.and(_permitTcpBdd)),
                        mapsBackward(ONE, validSrc.and(sessionHeaders).and(_permitTcpBdd)))))));
    _fwI1.setFirewallSessionInterfaceInfo(null);

    // Session has a transformation
    {
      BDD poolBdd = PKT.getSrcIp().value(10L);
      Transition nat = Transitions.eraseAndSet(PKT.getSrcIp(), poolBdd);
      BDDFirewallSessionTraceInfo natSessionInfo =
          new BDDFirewallSessionTraceInfo(
              FW, ImmutableSet.of(FW_I1), null, null, sessionHeaders, nat);
      assertThat(
          nodeAcceptEdges(natSessionInfo),
          contains(
              allOf(
                  hasPreState(new PreInInterface(FW, FW_I1)),
                  hasPostState(new NodeAccept(FW)),
                  hasTransition(
                      allOf(
                          mapsForward(srcFwI1, sessionHeaders.and(poolBdd)),
                          mapsForward(srcFwI1.and(noLastHop), sessionHeaders.and(poolBdd)),
                          mapsForward(srcFwI1.and(lastHop1), sessionHeaders.and(poolBdd)),
                          mapsBackward(ONE, validSrc.and(sessionHeaders)),
                          mapsBackward(poolBdd.not(), ZERO))))));
    }
  }

  @Test
  public void testPreInInterfaceEdges() {
    BDD sessionHeaders = PKT.getDstIp().value(10L);
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
            new NodeInterfacePair(SOURCE1, SOURCE1_IFACE),
            FW_I1,
            sessionHeaders,
            IDENTITY);

    // No transformation, no ACLs
    assertThat(
        preInInterfaceEdges(sessionInfo),
        contains(
            allOf(
                hasPreState(new PreInInterface(FW, FW_I1)),
                hasPostState(new PreInInterface(SOURCE1, SOURCE1_IFACE)),
                hasTransition(
                    allOf(
                        mapsForward(srcFwI1, sessionHeaders.and(source1Iface)),
                        mapsForward(srcFwI1.and(noLastHop), sessionHeaders.and(source1Iface)),
                        mapsForward(srcFwI1.and(lastHop1), sessionHeaders.and(source1Iface)),
                        mapsBackward(source1Iface, validSrc.and(sessionHeaders)),
                        mapsBackward(fakeIface, ZERO))))));

    // FW_I1 has an incoming session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(ImmutableList.of(FW_I1), PERMIT_TCP, null));
    assertThat(
        preInInterfaceEdges(sessionInfo),
        contains(
            allOf(
                hasPreState(new PreInInterface(FW, FW_I1)),
                hasPostState(new PreInInterface(SOURCE1, SOURCE1_IFACE)),
                hasTransition(
                    allOf(
                        mapsForward(srcFwI1, sessionHeaders.and(_permitTcpBdd).and(source1Iface)),
                        mapsForward(
                            srcFwI1.and(noLastHop),
                            sessionHeaders.and(_permitTcpBdd).and(source1Iface)),
                        mapsForward(
                            srcFwI1.and(lastHop1),
                            sessionHeaders.and(_permitTcpBdd).and(source1Iface)),
                        mapsBackward(source1Iface, validSrc.and(sessionHeaders).and(_permitTcpBdd)),
                        mapsBackward(fakeIface, ZERO))))));
    _fwI1.setFirewallSessionInterfaceInfo(null);

    // FW_I1 has an outgoing session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(ImmutableList.of(FW_I1), null, PERMIT_TCP));
    assertThat(
        preInInterfaceEdges(sessionInfo),
        contains(
            allOf(
                hasPreState(new PreInInterface(FW, FW_I1)),
                hasPostState(new PreInInterface(SOURCE1, SOURCE1_IFACE)),
                hasTransition(
                    allOf(
                        mapsForward(srcFwI1, sessionHeaders.and(_permitTcpBdd).and(source1Iface)),
                        mapsForward(
                            srcFwI1.and(noLastHop),
                            sessionHeaders.and(_permitTcpBdd).and(source1Iface)),
                        mapsForward(
                            srcFwI1.and(lastHop1),
                            sessionHeaders.and(_permitTcpBdd).and(source1Iface)),
                        mapsBackward(source1Iface, validSrc.and(sessionHeaders).and(_permitTcpBdd)),
                        mapsBackward(fakeIface, ZERO))))));
    _fwI1.setFirewallSessionInterfaceInfo(null);

    // Session has a transformation
    {
      BDD poolBdd = PKT.getSrcIp().value(10L);
      Transition nat = Transitions.eraseAndSet(PKT.getSrcIp(), poolBdd);
      BDDFirewallSessionTraceInfo natSessionInfo =
          new BDDFirewallSessionTraceInfo(
              FW,
              ImmutableSet.of(FW_I1),
              new NodeInterfacePair(SOURCE1, SOURCE1_IFACE),
              FW_I1,
              sessionHeaders,
              nat);
      assertThat(
          preInInterfaceEdges(natSessionInfo),
          contains(
              allOf(
                  hasPreState(new PreInInterface(FW, FW_I1)),
                  hasPostState(new PreInInterface(SOURCE1, SOURCE1_IFACE)),
                  hasTransition(
                      allOf(
                          mapsForward(srcFwI1, sessionHeaders.and(poolBdd).and(source1Iface)),
                          mapsForward(
                              srcFwI1.and(noLastHop),
                              sessionHeaders.and(poolBdd).and(source1Iface)),
                          mapsForward(
                              srcFwI1.and(lastHop1), sessionHeaders.and(poolBdd).and(source1Iface)),
                          mapsBackward(source1Iface, validSrc.and(sessionHeaders)),
                          mapsBackward(source1Iface.and(poolBdd.not()), ZERO),
                          mapsBackward(fakeIface, ZERO))))));
    }
  }

  @Test
  public void testDeliveredToSubnetEdges() {
    BDD sessionHeaders = PKT.getDstIp().value(10L);
    BDD srcFwI1 = _fwSrcMgr.getSourceInterfaceBDD(FW_I1);
    BDD lastHop1 = _lastHopMgr.getLastHopOutgoingInterfaceBdd(SOURCE1, SOURCE1_IFACE, FW, FW_I1);
    BDD noLastHop = _lastHopMgr.getNoLastHopOutgoingInterfaceBdd(FW, FW_I1);

    BDDFirewallSessionTraceInfo sessionInfo =
        new BDDFirewallSessionTraceInfo(
            FW, ImmutableSet.of(FW_I1), null, FW_I1, sessionHeaders, IDENTITY);

    // No transformation, no ACLs
    List<Edge> actual = deliveredToSubnetEdges(sessionInfo);
    assertThat(
        actual,
        contains(
            allOf(
                hasPreState(new PreInInterface(FW, FW_I1)),
                hasPostState(new NodeInterfaceDeliveredToSubnet(FW, FW_I1)),
                hasTransition(
                    allOf(
                        mapsForward(srcFwI1, sessionHeaders.and(srcFwI1)),
                        mapsForward(
                            srcFwI1.and(noLastHop), sessionHeaders.and(srcFwI1).and(noLastHop)),
                        mapsForward(
                            srcFwI1.and(lastHop1), sessionHeaders.and(srcFwI1).and(lastHop1)),
                        mapsBackward(ONE, sessionHeaders))))));

    // FW_I1 has an incoming session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(ImmutableList.of(FW_I1), PERMIT_TCP, null));
    assertThat(
        deliveredToSubnetEdges(sessionInfo),
        contains(
            allOf(
                hasPreState(new PreInInterface(FW, FW_I1)),
                hasPostState(new NodeInterfaceDeliveredToSubnet(FW, FW_I1)),
                hasTransition(
                    allOf(
                        mapsForward(ONE, sessionHeaders.and(_permitTcpBdd)),
                        mapsBackward(ONE, sessionHeaders.and(_permitTcpBdd)))))));
    _fwI1.setFirewallSessionInterfaceInfo(null);

    // FW_I1 has an outgoing session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(ImmutableList.of(FW_I1), null, PERMIT_TCP));
    assertThat(
        deliveredToSubnetEdges(sessionInfo),
        contains(
            allOf(
                hasPreState(new PreInInterface(FW, FW_I1)),
                hasPostState(new NodeInterfaceDeliveredToSubnet(FW, FW_I1)),
                hasTransition(
                    allOf(
                        mapsForward(ONE, sessionHeaders.and(_permitTcpBdd)),
                        mapsBackward(ONE, sessionHeaders.and(_permitTcpBdd)))))));
    _fwI1.setFirewallSessionInterfaceInfo(null);

    // Session has a transformation
    {
      BDD poolBdd = PKT.getSrcIp().value(10L);
      Transition nat = Transitions.eraseAndSet(PKT.getSrcIp(), poolBdd);
      BDDFirewallSessionTraceInfo natSessionInfo =
          new BDDFirewallSessionTraceInfo(
              FW, ImmutableSet.of(FW_I1), null, FW_I1, sessionHeaders, nat);
      assertThat(
          deliveredToSubnetEdges(natSessionInfo),
          contains(
              allOf(
                  hasPreState(new PreInInterface(FW, FW_I1)),
                  hasPostState(new NodeInterfaceDeliveredToSubnet(FW, FW_I1)),
                  hasTransition(
                      allOf(
                          mapsForward(ONE, sessionHeaders.and(poolBdd)),
                          mapsForward(poolBdd.not(), sessionHeaders.and(poolBdd)),
                          mapsBackward(ONE, sessionHeaders),
                          mapsBackward(poolBdd.not(), ZERO))))));
    }
  }

  @Test
  public void testDropAclInEdges() {
    BDD sessionHeaders = PKT.getDstIp().value(10L);
    BDD srcFwI1 = _fwSrcMgr.getSourceInterfaceBDD(FW_I1);
    BDD validSrc = _fwSrcMgr.isValidValue();

    BDDFirewallSessionTraceInfo sessionInfo =
        new BDDFirewallSessionTraceInfo(
            FW, ImmutableSet.of(FW_I1), null, null, sessionHeaders, IDENTITY);

    // No ACLs
    assertThat(
        nodeDropAclInEdges(sessionInfo),
        contains(
            allOf(
                hasPreState(new PreInInterface(FW, FW_I1)),
                hasPostState(new NodeDropAclIn(FW)),
                hasTransition(allOf(mapsForward(srcFwI1, ZERO), mapsBackward(ONE, ZERO))))));

    // FW_I1 has an incoming session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(ImmutableList.of(FW_I1), PERMIT_TCP, null));
    assertThat(
        nodeDropAclInEdges(sessionInfo),
        contains(
            allOf(
                hasPreState(new PreInInterface(FW, FW_I1)),
                hasPostState(new NodeDropAclIn(FW)),
                hasTransition(
                    allOf(
                        mapsForward(srcFwI1, sessionHeaders.and(_permitTcpBdd.not())),
                        mapsBackward(
                            ONE, validSrc.and(sessionHeaders).and(_permitTcpBdd.not())))))));
    _fwI1.setFirewallSessionInterfaceInfo(null);
  }

  @Test
  public void testDropAclOutEdges() {
    BDD sessionHeaders = PKT.getDstIp().value(10L);
    BDD srcFwI1 = _fwSrcMgr.getSourceInterfaceBDD(FW_I1);
    BDD validSrc = _fwSrcMgr.isValidValue();

    BDDFirewallSessionTraceInfo sessionInfo =
        new BDDFirewallSessionTraceInfo(
            FW, ImmutableSet.of(FW_I1), null, FW_I1, sessionHeaders, IDENTITY);

    // No ACLs
    assertThat(
        nodeDropAclOutEdges(sessionInfo),
        contains(
            allOf(
                hasPreState(new PreInInterface(FW, FW_I1)),
                hasPostState(new NodeDropAclOut(FW)),
                hasTransition(allOf(mapsForward(srcFwI1, ZERO), mapsBackward(ONE, ZERO))))));

    // FW_I1 has an outgoing session ACL
    _fwI1.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(ImmutableList.of(FW_I1), null, PERMIT_TCP));
    assertThat(
        nodeDropAclOutEdges(sessionInfo),
        contains(
            allOf(
                hasPreState(new PreInInterface(FW, FW_I1)),
                hasPostState(new NodeDropAclOut(FW)),
                hasTransition(
                    allOf(
                        mapsForward(srcFwI1, sessionHeaders.and(_permitTcpBdd.not())),
                        mapsBackward(
                            ONE, validSrc.and(sessionHeaders).and(_permitTcpBdd.not())))))));
    _fwI1.setFirewallSessionInterfaceInfo(null);
  }
}
