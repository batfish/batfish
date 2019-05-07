package org.batfish.bddreachability;

import static org.batfish.bddreachability.TestNetworkSources.CONFIG_NAME;
import static org.batfish.bddreachability.TestNetworkSources.INGRESS_IFACE_NAME;
import static org.batfish.bddreachability.TestNetworkSources.MATCH_SRC_INTERFACE_ACL_IFACE_NAME;
import static org.batfish.bddreachability.TestNetworkSources.ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME;
import static org.batfish.bddreachability.TestNetworkSources.PEER_IFACE_NAME;
import static org.batfish.bddreachability.TestNetworkSources.PEER_NAME;
import static org.batfish.bddreachability.TestNetworkSources.VRF_NAME;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.removeSourceConstraint;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.symbolic.state.NodeAccept;
import org.batfish.symbolic.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.symbolic.state.NodeInterfaceExitsNetwork;
import org.batfish.symbolic.state.NodeInterfaceInsufficientInfo;
import org.batfish.symbolic.state.NodeInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.OriginateInterfaceLink;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.PostInVrf;
import org.batfish.symbolic.state.PreInInterface;
import org.batfish.symbolic.state.PreOutEdgePostNat;
import org.batfish.symbolic.state.PreOutInterfaceDeliveredToSubnet;
import org.batfish.symbolic.state.PreOutInterfaceExitsNetwork;
import org.batfish.symbolic.state.PreOutInterfaceInsufficientInfo;
import org.batfish.symbolic.state.PreOutInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.StateExpr;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests source tracking (for ACLs that use {@link org.batfish.datamodel.acl.MatchSrcInterface} and
 * {@link org.batfish.datamodel.acl.OriginatingFromDevice} expressions).
 */
public class BDDReachabilityAnalysisFactorySourcesTest {
  @ClassRule public static TemporaryFolder temp = new TemporaryFolder();

  private static final BDDPacket pkt = new BDDPacket();
  private static Map<StateExpr, Map<StateExpr, Edge>> edges;
  private static BDDReachabilityAnalysisFactory factory;
  private static BDD ingressIfaceSrcIpBdd;
  private static BDD matchSrcInterfaceBdd;
  private static BDD originatingFromDeviceSrcIpBdd;
  private static BDD one;
  private static BDD originatingFromDeviceBdd;
  private static BDD zero;
  private static BDDSourceManager srcMgr;
  private static BDDSourceManager peerSrcMgr;

  @BeforeClass
  public static void setup() throws IOException {
    SortedMap<String, Configuration> configs = TestNetworkSources.twoNodeNetwork();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane();
    DataPlane dataPlane = batfish.loadDataPlane();
    factory =
        new BDDReachabilityAnalysisFactory(
            pkt, configs, dataPlane.getForwardingAnalysis(), false, false);
    peerSrcMgr = factory.getBDDSourceManagers().get(PEER_NAME);
    srcMgr = factory.getBDDSourceManagers().get(CONFIG_NAME);

    Ip ingressIfaceSrcIp = Ip.parse("6.6.6.6");
    ingressIfaceSrcIpBdd = pkt.getSrcIp().value(ingressIfaceSrcIp.asLong());

    Ip originatingFromDeviceSrcIp = Ip.parse("7.7.7.7");
    originatingFromDeviceSrcIpBdd = pkt.getSrcIp().value(originatingFromDeviceSrcIp.asLong());

    one = pkt.getFactory().one();
    zero = pkt.getFactory().zero();
    originatingFromDeviceBdd = srcMgr.getOriginatingFromDeviceBDD();
    matchSrcInterfaceBdd = srcMgr.getSourceInterfaceBDD(INGRESS_IFACE_NAME);

    edges =
        factory
            .bddReachabilityAnalysis(
                IpSpaceAssignment.builder()
                    .assign(
                        new InterfaceLinkLocation(CONFIG_NAME, INGRESS_IFACE_NAME),
                        ingressIfaceSrcIp.toIpSpace())
                    .assign(
                        new InterfaceLocation(CONFIG_NAME, INGRESS_IFACE_NAME),
                        originatingFromDeviceSrcIp.toIpSpace())
                    .assign(
                        new InterfaceLocation(PEER_NAME, PEER_IFACE_NAME), UniverseIpSpace.INSTANCE)
                    .build())
            .getForwardEdgeMap();
  }

  /**
   * Test the PreOutInterfaceNeighborUnreachable -> NodeInterfaceNeighborUnreachable edge for the
   * interface with the OriginatingFromDevice ACL.
   */
  @Test
  public void preOutVrf_NodeInterfaceNeighborUnreachable_OriginatingFromDevice() {
    Edge edge =
        edges
            .get(
                new PreOutInterfaceNeighborUnreachable(
                    CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME))
            .get(
                new NodeInterfaceNeighborUnreachable(
                    CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME));
    assertEquals(edge.getTransition(), constraint(originatingFromDeviceBdd));
  }

  /**
   * Test the PreOutInterfaceDeliveredToSubnet -> NodeInterfaceDeliveredToSubnet edge for the
   * interface with the OriginatingFromDevice ACL.
   */
  @Test
  public void preOutInterfaceDelivered_NodeInterfaceDelivered_OriginatingFromDevice() {
    Edge edge =
        edges
            .get(
                new PreOutInterfaceDeliveredToSubnet(
                    CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME))
            .get(
                new NodeInterfaceDeliveredToSubnet(
                    CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME));

    assertEquals(edge.getTransition(), constraint(originatingFromDeviceBdd));
  }

  /*
   * Test the PreOutInterfaceExitsNetwork -> NodeInterfaceExitsNetwork edge for the interface with the
   * OriginatingFromDevice ACL.
   */
  @Test
  public void preOutInterfaceExitsNetwork_NodeInterfaceExitsNetwork_OriginatingFromDevice() {
    Edge edge =
        edges
            .get(
                new PreOutInterfaceExitsNetwork(
                    CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME))
            .get(
                new NodeInterfaceExitsNetwork(CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME));
    assertEquals(edge.getTransition(), constraint(originatingFromDeviceBdd));
  }

  /*
   * Test the PreOutVrf -> DeliveredToSubnet edge for the interface with the
   * MatchSrcInterface ACL.
   */
  @Test
  public void preOutInterfaceDelivered_NodeInterfaceDelivered_MatchSrcInterface() {
    Edge edge =
        edges
            .get(
                new PreOutInterfaceDeliveredToSubnet(
                    CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME))
            .get(
                new NodeInterfaceDeliveredToSubnet(
                    CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME));
    assertEquals(edge.getTransition(), constraint(matchSrcInterfaceBdd));
  }

  /**
   * Test the PreOutInterfaceExitsNetwork -> NodeInterfaceExitsNetwork edge for the interface with
   * the MatchSrcInterface ACL.
   */
  @Test
  public void preOutInterfaceExitsNetwork_NodeInterfaceExitsNetwork_MatchSrcInterface() {
    Edge edge =
        edges
            .get(new PreOutInterfaceExitsNetwork(CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME))
            .get(new NodeInterfaceExitsNetwork(CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME));
    assertEquals(edge.getTransition(), constraint(matchSrcInterfaceBdd));
  }

  /**
   * Test the PreOutInterfaceInsufficientInfo -> NodeInterfaceInsufficientInfo edge for the
   * interface with the MatchSrcInterface ACL.
   */
  @Test
  public void preOutVrf_NodeInterfaceInsufficientInfo_MatchSrcInterface() {
    Edge edge =
        edges
            .get(
                new PreOutInterfaceInsufficientInfo(
                    CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME))
            .get(
                new NodeInterfaceInsufficientInfo(CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME));
    assertEquals(edge.getTransition(), constraint(matchSrcInterfaceBdd));
  }

  /*
   * Test one of several edges that don't constrain the source variable, but just erase it in the
   * forward direction.
   */
  @Test
  public void postInVrf_NodeAccept() {
    // PostInVrf -> NodeAccept edge.
    Edge edge = edges.get(new PostInVrf(CONFIG_NAME, VRF_NAME)).get(new NodeAccept(CONFIG_NAME));
    BDD headerSpaceBdd = factory.getVrfAcceptBDDs().get(CONFIG_NAME).get(VRF_NAME);
    assertEquals(
        edge.getTransition(), compose(constraint(headerSpaceBdd), removeSourceConstraint(srcMgr)));
  }

  @Test
  public void originateInterfaceLink_PreInInterface() {
    Edge edge =
        edges
            .get(new OriginateInterfaceLink(CONFIG_NAME, INGRESS_IFACE_NAME))
            .get(new PreInInterface(CONFIG_NAME, INGRESS_IFACE_NAME));
    assertThat(edge.traverseForward(one), equalTo(ingressIfaceSrcIpBdd.and(matchSrcInterfaceBdd)));
    assertThat(edge.traverseBackward(srcMgr.isValidValue()), equalTo(ingressIfaceSrcIpBdd));
    assertThat(edge.traverseBackward(originatingFromDeviceBdd), equalTo(zero));
    assertThat(edge.traverseBackward(matchSrcInterfaceBdd), equalTo(ingressIfaceSrcIpBdd));
  }

  @Test
  public void originateVrf_PostInVrf() {
    Edge edge =
        edges
            .get(new OriginateVrf(CONFIG_NAME, VRF_NAME))
            .get(new PostInVrf(CONFIG_NAME, VRF_NAME));
    assertThat(
        edge.traverseForward(one),
        equalTo(originatingFromDeviceSrcIpBdd.and(originatingFromDeviceBdd)));
    assertThat(
        edge.traverseBackward(srcMgr.isValidValue()), equalTo(originatingFromDeviceSrcIpBdd));
    assertThat(
        edge.traverseBackward(originatingFromDeviceBdd), equalTo(originatingFromDeviceSrcIpBdd));
    assertThat(edge.traverseBackward(matchSrcInterfaceBdd), equalTo(zero));
  }

  @Test
  public void preOutEdgePostNat_PreInInterface() {
    Edge edge =
        edges
            .get(new PreOutEdgePostNat(PEER_NAME, PEER_IFACE_NAME, CONFIG_NAME, INGRESS_IFACE_NAME))
            .get(new PreInInterface(CONFIG_NAME, INGRESS_IFACE_NAME));
    assertThat(edge.traverseForward(peerSrcMgr.isValidValue()), equalTo(matchSrcInterfaceBdd));
    assertThat(edge.traverseBackward(matchSrcInterfaceBdd), equalTo(peerSrcMgr.isValidValue()));
    assertThat(edge.traverseBackward(originatingFromDeviceBdd), equalTo(zero));
  }
}
