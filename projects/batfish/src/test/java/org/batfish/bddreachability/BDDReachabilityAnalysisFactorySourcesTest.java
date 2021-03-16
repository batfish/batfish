package org.batfish.bddreachability;

import static org.batfish.bddreachability.TestNetworkSources.CONFIG_NAME;
import static org.batfish.bddreachability.TestNetworkSources.INGRESS_IFACE_NAME;
import static org.batfish.bddreachability.TestNetworkSources.MATCH_SRC_INTERFACE_ACL_IFACE_NAME;
import static org.batfish.bddreachability.TestNetworkSources.ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME;
import static org.batfish.bddreachability.TestNetworkSources.PEER_IFACE_NAME;
import static org.batfish.bddreachability.TestNetworkSources.PEER_NAME;
import static org.batfish.bddreachability.TestNetworkSources.VRF_NAME;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.removeSourceConstraint;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
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
import org.batfish.symbolic.state.VrfAccept;
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
  private static Map<StateExpr, Map<StateExpr, Transition>> edges;
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
    batfish.computeDataPlane(batfish.getSnapshot());
    DataPlane dataPlane = batfish.loadDataPlane(batfish.getSnapshot());
    BDDReachabilityAnalysisFactory factory =
        new BDDReachabilityAnalysisFactory(
            pkt,
            configs,
            dataPlane.getForwardingAnalysis(),
            new IpsRoutedOutInterfacesFactory(dataPlane.getFibs()),
            false,
            false);
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
    Transition transition =
        edges
            .get(
                new PreOutInterfaceNeighborUnreachable(
                    CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME))
            .get(
                new NodeInterfaceNeighborUnreachable(
                    CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME));
    assertEquals(transition, constraint(originatingFromDeviceBdd));
  }

  /**
   * Test the PreOutInterfaceDeliveredToSubnet -> NodeInterfaceDeliveredToSubnet edge for the
   * interface with the OriginatingFromDevice ACL.
   */
  @Test
  public void preOutInterfaceDelivered_NodeInterfaceDelivered_OriginatingFromDevice() {
    Transition transition =
        edges
            .get(
                new PreOutInterfaceDeliveredToSubnet(
                    CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME))
            .get(
                new NodeInterfaceDeliveredToSubnet(
                    CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME));

    assertEquals(transition, constraint(originatingFromDeviceBdd));
  }

  /*
   * Test the PreOutInterfaceExitsNetwork -> NodeInterfaceExitsNetwork edge for the interface with the
   * OriginatingFromDevice ACL.
   */
  @Test
  public void preOutInterfaceExitsNetwork_NodeInterfaceExitsNetwork_OriginatingFromDevice() {
    Transition transition =
        edges
            .get(
                new PreOutInterfaceExitsNetwork(
                    CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME))
            .get(
                new NodeInterfaceExitsNetwork(CONFIG_NAME, ORIGINATING_FROM_DEVICE_ACL_IFACE_NAME));
    assertEquals(transition, constraint(originatingFromDeviceBdd));
  }

  /*
   * Test the PreOutVrf -> DeliveredToSubnet edge for the interface with the
   * MatchSrcInterface ACL.
   */
  @Test
  public void preOutInterfaceDelivered_NodeInterfaceDelivered_MatchSrcInterface() {
    Transition transition =
        edges
            .get(
                new PreOutInterfaceDeliveredToSubnet(
                    CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME))
            .get(
                new NodeInterfaceDeliveredToSubnet(
                    CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME));
    assertEquals(transition, constraint(matchSrcInterfaceBdd));
  }

  /**
   * Test the PreOutInterfaceExitsNetwork -> NodeInterfaceExitsNetwork edge for the interface with
   * the MatchSrcInterface ACL.
   */
  @Test
  public void preOutInterfaceExitsNetwork_NodeInterfaceExitsNetwork_MatchSrcInterface() {
    Transition transition =
        edges
            .get(new PreOutInterfaceExitsNetwork(CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME))
            .get(new NodeInterfaceExitsNetwork(CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME));
    assertEquals(transition, constraint(matchSrcInterfaceBdd));
  }

  /**
   * Test the PreOutInterfaceInsufficientInfo -> NodeInterfaceInsufficientInfo edge for the
   * interface with the MatchSrcInterface ACL.
   */
  @Test
  public void preOutVrf_NodeInterfaceInsufficientInfo_MatchSrcInterface() {
    Transition transition =
        edges
            .get(
                new PreOutInterfaceInsufficientInfo(
                    CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME))
            .get(
                new NodeInterfaceInsufficientInfo(CONFIG_NAME, MATCH_SRC_INTERFACE_ACL_IFACE_NAME));
    assertEquals(transition, constraint(matchSrcInterfaceBdd));
  }

  /*
   * Test one of several edges that don't constrain the source variable, but just erase it in the
   * forward direction.
   */
  @Test
  public void vrfAccept_NodeAccept() {
    // NodeAccept -> Accept edge.
    Transition transition =
        edges.get(new VrfAccept(CONFIG_NAME, VRF_NAME)).get(new NodeAccept(CONFIG_NAME));

    assertThat(transition, equalTo(removeSourceConstraint(srcMgr)));
  }

  @Test
  public void originateInterfaceLink_PreInInterface() {
    Transition transition =
        edges
            .get(new OriginateInterfaceLink(CONFIG_NAME, INGRESS_IFACE_NAME))
            .get(new PreInInterface(CONFIG_NAME, INGRESS_IFACE_NAME));
    assertThat(
        transition.transitForward(one), equalTo(ingressIfaceSrcIpBdd.and(matchSrcInterfaceBdd)));
    assertThat(transition.transitBackward(srcMgr.isValidValue()), equalTo(ingressIfaceSrcIpBdd));
    assertThat(transition.transitBackward(originatingFromDeviceBdd), equalTo(zero));
    assertThat(transition.transitBackward(matchSrcInterfaceBdd), equalTo(ingressIfaceSrcIpBdd));
  }

  @Test
  public void originateVrf_PostInVrf() {
    Transition transition =
        edges
            .get(new OriginateVrf(CONFIG_NAME, VRF_NAME))
            .get(new PostInVrf(CONFIG_NAME, VRF_NAME));
    assertThat(
        transition.transitForward(one),
        equalTo(originatingFromDeviceSrcIpBdd.and(originatingFromDeviceBdd)));
    assertThat(
        transition.transitBackward(srcMgr.isValidValue()), equalTo(originatingFromDeviceSrcIpBdd));
    assertThat(
        transition.transitBackward(originatingFromDeviceBdd),
        equalTo(originatingFromDeviceSrcIpBdd));
    assertThat(transition.transitBackward(matchSrcInterfaceBdd), equalTo(zero));
  }

  @Test
  public void preOutEdgePostNat_PreInInterface() {
    Transition transition =
        edges
            .get(new PreOutEdgePostNat(PEER_NAME, PEER_IFACE_NAME, CONFIG_NAME, INGRESS_IFACE_NAME))
            .get(new PreInInterface(CONFIG_NAME, INGRESS_IFACE_NAME));
    assertThat(transition.transitForward(peerSrcMgr.isValidValue()), equalTo(matchSrcInterfaceBdd));
    assertThat(
        transition.transitBackward(matchSrcInterfaceBdd), equalTo(peerSrcMgr.isValidValue()));
    assertThat(transition.transitBackward(originatingFromDeviceBdd), equalTo(zero));
  }
}
