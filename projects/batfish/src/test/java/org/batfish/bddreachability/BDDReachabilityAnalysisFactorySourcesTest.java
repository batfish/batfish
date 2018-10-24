package org.batfish.bddreachability;

import static org.batfish.bddreachability.TestNetworkSources.CONFIG_NAME;
import static org.batfish.bddreachability.TestNetworkSources.INGRESS_IFACE_NAME;
import static org.batfish.bddreachability.TestNetworkSources.MATCH_SRC_INTERFACE_ACL_IFACE_NAME;
import static org.batfish.bddreachability.TestNetworkSources.ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME;
import static org.batfish.bddreachability.TestNetworkSources.PEER_IFACE_NAME;
import static org.batfish.bddreachability.TestNetworkSources.PEER_NAME;
import static org.batfish.bddreachability.TestNetworkSources.VRF_NAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

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
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.NodeAccept;
import org.batfish.z3.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.z3.state.NodeInterfaceExitsNetwork;
import org.batfish.z3.state.NodeInterfaceInsufficientInfo;
import org.batfish.z3.state.NodeInterfaceNeighborUnreachable;
import org.batfish.z3.state.OriginateInterfaceLink;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.PostInVrf;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOutEdgePostNat;
import org.batfish.z3.state.PreOutVrf;
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
    batfish.computeDataPlane(false);
    DataPlane dataPlane = batfish.loadDataPlane();
    factory = new BDDReachabilityAnalysisFactory(pkt, configs, dataPlane.getForwardingAnalysis());
    peerSrcMgr = factory.getBDDSourceManagers().get(PEER_NAME);
    srcMgr = factory.getBDDSourceManagers().get(CONFIG_NAME);

    Ip ingressIfaceSrcIp = new Ip("6.6.6.6");
    ingressIfaceSrcIpBdd = pkt.getSrcIp().value(ingressIfaceSrcIp.asLong());

    Ip originatingFromDeviceSrcIp = new Ip("7.7.7.7");
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
            .getEdges();
  }

  /*
   * Test the PreOutVrf -> NodeInterfaceNeighborUnreachable edge for the interface with the
   * OriginatingFromDevice ACL.
   */
  @Test
  public void preOutVrf_NodeInterfaceNeighborUnreachable_OriginatingFromDevice() {
    Edge edge =
        edges
            .get(new PreOutVrf(CONFIG_NAME, VRF_NAME))
            .get(
                new NodeInterfaceNeighborUnreachable(
                    CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME));
    BDD headerSpaceBdd =
        factory
            .getNeighborUnreachableBDDs()
            .get(CONFIG_NAME)
            .get(VRF_NAME)
            .get(ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME);
    assertThat(edge.traverseForward(one), equalTo(headerSpaceBdd));
    assertThat(edge.traverseForward(originatingFromDeviceBdd), equalTo(headerSpaceBdd));
    assertThat(edge.traverseForward(matchSrcInterfaceBdd), equalTo(zero));
    assertThat(edge.traverseBackward(one), equalTo(headerSpaceBdd.and(originatingFromDeviceBdd)));
  }

  /*
   * Test the PreOutVrf -> DeliveredToSubnet edge for the interface with the
   * OriginatingFromDevice ACL.
   */
  @Test
  public void preOutVrf_NodeInterfaceDeliveredToSubnet_OriginatingFromDevice() {
    Edge edge =
        edges
            .get(new PreOutVrf(CONFIG_NAME, VRF_NAME))
            .get(
                new NodeInterfaceDeliveredToSubnet(
                    CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME));
    BDD headerSpaceBdd =
        factory
            .getDeliveredToSubnetBDDs()
            .get(CONFIG_NAME)
            .get(VRF_NAME)
            .get(ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME);
    assertThat(edge.traverseForward(one), equalTo(headerSpaceBdd));
    assertThat(edge.traverseForward(originatingFromDeviceBdd), equalTo(headerSpaceBdd));
    assertThat(edge.traverseForward(matchSrcInterfaceBdd), equalTo(zero));
    assertThat(edge.traverseBackward(one), equalTo(headerSpaceBdd.and(originatingFromDeviceBdd)));
  }

  /*
   * Test the PreOutVrf -> ExitsNetwork edge for the interface with the
   * OriginatingFromDevice ACL.
   */
  @Test
  public void preOutVrf_NodeInterfaceExitsNetwork_OriginatingFromDevice() {
    Edge edge =
        edges
            .get(new PreOutVrf(CONFIG_NAME, VRF_NAME))
            .get(
                new NodeInterfaceExitsNetwork(CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME));
    BDD headerSpaceBdd =
        factory
            .getExitsNetworkBDDs()
            .get(CONFIG_NAME)
            .get(VRF_NAME)
            .get(ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME);
    assertThat(edge.traverseForward(one), equalTo(headerSpaceBdd));
    assertThat(edge.traverseForward(originatingFromDeviceBdd), equalTo(headerSpaceBdd));
    assertThat(edge.traverseForward(matchSrcInterfaceBdd), equalTo(zero));
    assertThat(edge.traverseBackward(one), equalTo(headerSpaceBdd.and(originatingFromDeviceBdd)));
  }

  /*
   * Test the PreOutVrf -> DeliveredToSubnet edge for the interface with the
   * MatchSrcInterface ACL.
   */
  @Test
  public void preOutVrf_NodeInterfaceDeliveredToSubnet_MatchSrcInterface() {
    Edge edge =
        edges
            .get(new PreOutVrf(CONFIG_NAME, VRF_NAME))
            .get(
                new NodeInterfaceDeliveredToSubnet(
                    CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME));
    BDD headerSpaceBdd =
        factory
            .getDeliveredToSubnetBDDs()
            .get(CONFIG_NAME)
            .get(VRF_NAME)
            .get(MATCH_SRC_INTERFACE_ACL_IFACE_NAME);

    assertThat(edge.traverseForward(one), equalTo(headerSpaceBdd));
    assertThat(edge.traverseForward(originatingFromDeviceBdd), equalTo(zero));
    assertThat(edge.traverseForward(matchSrcInterfaceBdd), equalTo(headerSpaceBdd));
    assertThat(edge.traverseBackward(one), equalTo(headerSpaceBdd.and(matchSrcInterfaceBdd)));
  }

  /*
   * Test the PreOutVrf -> ExitsNetwork edge for the interface with the
   * MatchSrcInterface ACL.
   */
  @Test
  public void preOutVrf_NodeInterfaceExitsNetwork_MatchSrcInterface() {
    Edge edge =
        edges
            .get(new PreOutVrf(CONFIG_NAME, VRF_NAME))
            .get(new NodeInterfaceExitsNetwork(CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME));
    BDD headerSpaceBdd =
        factory
            .getExitsNetworkBDDs()
            .get(CONFIG_NAME)
            .get(VRF_NAME)
            .get(MATCH_SRC_INTERFACE_ACL_IFACE_NAME);
    assertThat(edge.traverseForward(one), equalTo(headerSpaceBdd));
    assertThat(edge.traverseForward(originatingFromDeviceBdd), equalTo(zero));
    assertThat(edge.traverseForward(matchSrcInterfaceBdd), equalTo(headerSpaceBdd));
    assertThat(edge.traverseBackward(one), equalTo(headerSpaceBdd.and(matchSrcInterfaceBdd)));
  }

  /*
   * Test the PreOutVrf -> Insufficient edge for the interface with the
   * MatchSrcInterface ACL.
   */
  @Test
  public void preOutVrf_NodeInterfaceInsufficientInfo_MatchSrcInterface() {
    Edge edge =
        edges
            .get(new PreOutVrf(CONFIG_NAME, VRF_NAME))
            .get(
                new NodeInterfaceInsufficientInfo(CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME));
    BDD headerSpaceBdd =
        factory
            .getInsufficientInfoBDDs()
            .get(CONFIG_NAME)
            .get(VRF_NAME)
            .get(MATCH_SRC_INTERFACE_ACL_IFACE_NAME);

    assertThat(edge, nullValue());
    assertThat(headerSpaceBdd, equalTo(zero));
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
    BDD validSrcBdd = srcMgr.isValidValue();
    assertThat(edge.traverseForward(one), equalTo(headerSpaceBdd));
    assertThat(edge.traverseForward(originatingFromDeviceBdd), equalTo(headerSpaceBdd));
    assertThat(edge.traverseForward(matchSrcInterfaceBdd), equalTo(headerSpaceBdd));
    assertThat(edge.traverseBackward(one), equalTo(headerSpaceBdd.and(validSrcBdd)));
  }

  @Test
  public void originateInterfaceLink_PreInInterface() {
    Edge edge =
        edges
            .get(new OriginateInterfaceLink(CONFIG_NAME, INGRESS_IFACE_NAME))
            .get(new PreInInterface(CONFIG_NAME, INGRESS_IFACE_NAME));
    assertThat(edge.traverseForward(one), equalTo(ingressIfaceSrcIpBdd.and(matchSrcInterfaceBdd)));
    assertThat(edge.traverseBackward(one), equalTo(ingressIfaceSrcIpBdd));
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
    assertThat(edge.traverseBackward(one), equalTo(originatingFromDeviceSrcIpBdd));
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
    assertThat(edge.traverseForward(one), equalTo(matchSrcInterfaceBdd));
    assertThat(edge.traverseBackward(matchSrcInterfaceBdd), equalTo(peerSrcMgr.isValidValue()));
    assertThat(edge.traverseBackward(originatingFromDeviceBdd), equalTo(zero));
  }
}
