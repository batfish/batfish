package org.batfish.bddreachability;

import static org.batfish.bddreachability.BDDFirewallSessionTraceInfoMatchers.hasAction;
import static org.batfish.bddreachability.BDDFirewallSessionTraceInfoMatchers.hasIncomingInterfaces;
import static org.batfish.bddreachability.BDDFirewallSessionTraceInfoMatchers.hasSessionFlows;
import static org.batfish.bddreachability.BDDFirewallSessionTraceInfoMatchers.hasTransformation;
import static org.batfish.bddreachability.BDDReachabilityAnalysisSessionFactory.computeInitializedSesssions;
import static org.batfish.bddreachability.BDDReverseTransformationRangesImpl.TransformationType.INCOMING;
import static org.batfish.bddreachability.BDDReverseTransformationRangesImpl.TransformationType.OUTGOING;
import static org.batfish.bddreachability.BidirectionalReachabilityAnalysis.computeReturnPassQueryConstraints;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.datamodel.AclIpSpace.difference;
import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.DENIED_IN;
import static org.batfish.datamodel.FlowDisposition.DENIED_OUT;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.INSUFFICIENT_INFO;
import static org.batfish.datamodel.FlowDisposition.NEIGHBOR_UNREACHABLE;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.batfish.datamodel.FlowDisposition.NULL_ROUTED;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.BDDReverseTransformationRangesImpl.Key;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.flow.ForwardOutInterface;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.question.bidirectionalreachability.BidirectionalReachabilityResult;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.Location;
import org.batfish.symbolic.state.NodeAccept;
import org.batfish.symbolic.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.symbolic.state.NodeInterfaceExitsNetwork;
import org.batfish.symbolic.state.NodeInterfaceInsufficientInfo;
import org.batfish.symbolic.state.NodeInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.OriginateInterfaceLink;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.PreInInterface;
import org.batfish.symbolic.state.StateExpr;
import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for {@link BidirectionalReachabilityAnalysis}. */
public final class BidirectionalReachabilityAnalysisTest {

  private static final Set<FlowDisposition> ALL_DISPOSITIONS =
      ImmutableSet.of(
          ACCEPTED,
          DENIED_IN,
          DENIED_OUT,
          NO_ROUTE,
          NULL_ROUTED,
          DELIVERED_TO_SUBNET,
          EXITS_NETWORK,
          NEIGHBOR_UNREACHABLE,
          INSUFFICIENT_INFO);

  private static BDDPacket PKT;

  // nodes
  private static final String INGRESS_NODE = "ingressNode";
  private static final String NEIGHBOR = "neighbor";

  // vrfs
  private static final String EGRESS_VRF = "egressVrf";
  private static final String INGRESS_VRF = "ingressVrf";
  private static final String NEIGHBOR_VRF = "neighborVrf";

  // interfaces
  private static final String EGRESS_IFACE = "egressIface";
  private static final String INGRESS_IFACE = "ingressIface";
  private static final String NEIGHBOR_IFACE = "neighborIface";

  // addresses
  private static final ConcreteInterfaceAddress EGRESS_IFACE_ADDRESS =
      ConcreteInterfaceAddress.parse("10.0.12.1/24");
  private static final ConcreteInterfaceAddress INGRESS_IFACE_ADDRESS =
      ConcreteInterfaceAddress.parse("10.0.0.1/24");
  private static final ConcreteInterfaceAddress NEIGHBOR_IFACE_ADDRESS =
      ConcreteInterfaceAddress.parse("10.0.12.2/24");

  // locations
  private static Location INGRESS_LOCATION;

  private static IpSpaceToBDD TO_DST_IP_BDD;
  private static IpSpaceToBDD TO_SRC_IP_BDD;

  private static IpSpace DST_IP_SPACE_SINGLE_NODE;
  private static IpSpace DST_IP_SPACE_DUAL_NODE;

  @Rule public TemporaryFolder temp = new TemporaryFolder();

  @BeforeClass
  public static void setup() {
    PKT = new BDDPacket();
    INGRESS_LOCATION = new InterfaceLinkLocation(INGRESS_NODE, INGRESS_IFACE);
    TO_DST_IP_BDD = new IpSpaceToBDD(PKT.getDstIp());
    TO_SRC_IP_BDD = new IpSpaceToBDD(PKT.getSrcIp());
    DST_IP_SPACE_SINGLE_NODE =
        AclIpSpace.difference(
            EGRESS_IFACE_ADDRESS.getPrefix().toIpSpace(), EGRESS_IFACE_ADDRESS.getIp().toIpSpace());
    DST_IP_SPACE_DUAL_NODE = NEIGHBOR_IFACE_ADDRESS.getIp().toIpSpace();
  }

  @Test
  public void testReturnPassQueryConstraints() {
    BDD dst1 = PKT.getDstIp().value(1L);
    BDD dst2 = PKT.getDstIp().value(2L);
    BDD src1 = PKT.getSrcIp().value(1L);
    BDD src2 = PKT.getSrcIp().value(2L);

    assertThat(
        computeReturnPassQueryConstraints(
            PKT,
            ImmutableMap.of(
                new OriginateVrf("NODE", "VRF"),
                dst1.and(src2),
                new OriginateInterfaceLink("NODE", "IFACE"),
                dst2.and(src1))),
        equalTo(
            ImmutableMap.of(
                new NodeAccept("NODE"),
                dst2.and(src1),
                new NodeInterfaceDeliveredToSubnet("NODE", "IFACE"),
                dst1.and(src2),
                new NodeInterfaceExitsNetwork("NODE", "IFACE"),
                dst1.and(src2),
                new NodeInterfaceInsufficientInfo("NODE", "IFACE"),
                dst1.and(src2),
                new NodeInterfaceNeighborUnreachable("NODE", "IFACE"),
                dst1.and(src2))));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testInitializeSessions() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setActive(true);

    Configuration source1 = cb.build();
    Vrf vrf = nf.vrfBuilder().setOwner(source1).build();
    ib.setOwner(source1).setVrf(vrf);
    Interface source1Iface = ib.setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/29")).build();
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNetwork(Prefix.parse("255.255.255.0/24"))
                .setAdministrativeCost(1)
                .setNextHopInterface(source1Iface.getName())
                .setNextHopIp(Ip.parse("1.0.0.3"))
                .build()));

    Configuration source2 = cb.build();
    vrf = nf.vrfBuilder().setOwner(source2).build();
    ib.setOwner(source2).setVrf(vrf);
    Interface source2Iface = ib.setAddress(ConcreteInterfaceAddress.parse("1.0.0.2/29")).build();
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNetwork(Prefix.parse("255.255.255.0/24"))
                .setAdministrativeCost(1)
                .setNextHopInterface(source2Iface.getName())
                .setNextHopIp(Ip.parse("1.0.0.3"))
                .build()));

    Configuration fw = cb.build();
    ib.setOwner(fw).setVrf(nf.vrfBuilder().setOwner(fw).build());
    Interface fwI1 = ib.setAddress(ConcreteInterfaceAddress.parse("1.0.0.3/29")).build();
    Interface fwI2 = ib.setAddress(ConcreteInterfaceAddress.parse("255.255.255.0/24")).build();

    fwI2.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(false, ImmutableList.of(fwI2.getName()), null, null));

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(
            source1.getHostname(), source1, source2.getHostname(), source2, fw.getHostname(), fw);
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane();

    IpSpaceAssignment assignment =
        IpSpaceAssignment.builder()
            .assign(
                new InterfaceLocation(source1.getHostname(), source1Iface.getName()),
                Ip.parse("10.0.0.1").toIpSpace())
            .assign(
                new InterfaceLocation(source2.getHostname(), source2Iface.getName()),
                Ip.parse("10.0.0.2").toIpSpace())
            .assign(
                new InterfaceLinkLocation(fw.getHostname(), fwI1.getName()),
                Ip.parse("10.0.0.3").toIpSpace())
            .build();

    BDDReachabilityAnalysisFactory factory =
        new BDDReachabilityAnalysisFactory(
            PKT, configurations, batfish.loadDataPlane().getForwardingAnalysis(), false, true);
    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            assignment,
            matchDst(Ip.parse("255.255.255.1")),
            ImmutableSet.of(),
            ImmutableSet.of(),
            configurations.keySet(),
            ImmutableSet.of(DELIVERED_TO_SUBNET));
    Map<StateExpr, BDD> forwardReachableBdds = analysis.computeForwardReachableStates();

    LastHopOutgoingInterfaceManager lastHopManager = factory.getLastHopManager();
    assertNotNull(lastHopManager);

    Transition i1Transition = new MockTransition(fwI1.getName());
    Transition i2Transition = new MockTransition(fwI2.getName());

    NodeInterfacePair source1Source1Iface =
        new NodeInterfacePair(source1.getHostname(), source1Iface.getName());
    NodeInterfacePair source2Source2Iface =
        new NodeInterfacePair(source2.getHostname(), source2Iface.getName());
    BDD source1InRange = PKT.getDstPort().value(1);
    BDD source2InRange = PKT.getDstPort().value(2);
    BDD fwInRange = PKT.getDstPort().value(3);

    BDD source1OutRange = PKT.getSrcPort().value(1);
    BDD source2OutRange = PKT.getSrcPort().value(2);
    BDD fwOutRange = PKT.getSrcPort().value(3);

    ImmutableMap<Key, BDD> transformationRanges =
        ImmutableMap.<Key, BDD>builder()
            .put(
                // incoming transformation ranges
                new Key(
                    fw.getHostname(),
                    fwI1.getName(),
                    INCOMING,
                    fwI1.getName(),
                    source1Source1Iface),
                source1InRange)
            .put(
                new Key(
                    fw.getHostname(),
                    fwI1.getName(),
                    INCOMING,
                    fwI1.getName(),
                    source2Source2Iface),
                source2InRange)
            .put(
                new Key(fw.getHostname(), fwI1.getName(), INCOMING, fwI1.getName(), null),
                fwInRange)

            // outgoing transformation ranges
            .put(
                new Key(
                    fw.getHostname(),
                    fwI2.getName(),
                    OUTGOING,
                    fwI1.getName(),
                    source1Source1Iface),
                source1OutRange)
            .put(
                new Key(
                    fw.getHostname(),
                    fwI2.getName(),
                    OUTGOING,
                    fwI1.getName(),
                    source2Source2Iface),
                source2OutRange)
            .put(
                new Key(fw.getHostname(), fwI2.getName(), OUTGOING, fwI1.getName(), null),
                fwOutRange)
            .build();

    Map<String, List<BDDFirewallSessionTraceInfo>> sessions =
        computeInitializedSesssions(
            PKT,
            configurations,
            factory.getBDDSourceManagers(),
            lastHopManager,
            forwardReachableBdds,
            new MockBDDReverseFlowTransformationFactory(
                ImmutableMap.of(
                    new NodeInterfacePair(fw.getHostname(), fwI1.getName()), i1Transition),
                ImmutableMap.of(
                    new NodeInterfacePair(fw.getHostname(), fwI2.getName()), i2Transition)),
            new MockBDDReverseTransformationRanges(PKT.getFactory().zero(), transformationRanges));

    BDD source1SessionFlows =
        PKT.getSrcIp()
            .value(Ip.parse("255.255.255.1").asLong())
            .and(PKT.getDstIp().value(Ip.parse("10.0.0.1").asLong()));
    BDD source2SessionFlows =
        PKT.getSrcIp()
            .value(Ip.parse("255.255.255.1").asLong())
            .and(PKT.getDstIp().value(Ip.parse("10.0.0.2").asLong()));
    BDD enterFlows =
        PKT.getSrcIp()
            .value(Ip.parse("255.255.255.1").asLong())
            .and(PKT.getDstIp().value(Ip.parse("10.0.0.3").asLong()));

    assertThat(
        sessions,
        hasEntry(
            equalTo(fw.getHostname()),
            containsInAnyOrder(
                allOf(
                    BDDFirewallSessionTraceInfoMatchers.hasHostname(fw.getHostname()),
                    hasIncomingInterfaces(contains(fwI2.getName())),
                    hasAction(
                        new ForwardOutInterface(
                            fwI1.getName(),
                            new NodeInterfacePair(source1.getHostname(), source1Iface.getName()))),
                    hasSessionFlows(source1SessionFlows),
                    hasTransformation(
                        compose(
                            i2Transition,
                            constraint(source1OutRange),
                            i1Transition,
                            constraint(source1InRange)))),
                allOf(
                    BDDFirewallSessionTraceInfoMatchers.hasHostname(fw.getHostname()),
                    hasIncomingInterfaces(contains(fwI2.getName())),
                    hasAction(
                        new ForwardOutInterface(
                            fwI1.getName(),
                            new NodeInterfacePair(source2.getHostname(), source2Iface.getName()))),
                    hasSessionFlows(source2SessionFlows),
                    hasTransformation(
                        compose(
                            i2Transition,
                            constraint(source2OutRange),
                            i1Transition,
                            constraint(source2InRange)))),
                allOf(
                    BDDFirewallSessionTraceInfoMatchers.hasHostname(fw.getHostname()),
                    hasIncomingInterfaces(contains(fwI2.getName())),
                    hasAction(new ForwardOutInterface(fwI1.getName(), null)),
                    hasSessionFlows(enterFlows),
                    hasTransformation(
                        compose(
                            i2Transition,
                            constraint(fwOutRange),
                            i1Transition,
                            constraint(fwInRange)))))));
  }

  /**
   * A fake {@link Transition} only used to test construction of BDD reachability graph transitions.
   */
  static final class MockTransition implements Transition {
    private final String _id;

    MockTransition(String id) {
      _id = id;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof MockTransition)) {
        return false;
      }
      MockTransition that = (MockTransition) o;
      return Objects.equals(_id, that._id);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_id);
    }

    @Override
    public BDD transitForward(BDD bdd) {
      throw new IllegalStateException("Cannot transit MockTransition");
    }

    @Override
    public BDD transitBackward(BDD bdd) {
      throw new IllegalStateException("Cannot transit MockTransition");
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBidirectionalReachabilitySuccess() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setActive(true);

    Configuration source1 = cb.build();
    Vrf vrf = nf.vrfBuilder().setOwner(source1).build();
    ib.setOwner(source1).setVrf(vrf);
    Interface source1Iface = ib.setAddress(ConcreteInterfaceAddress.parse("2.0.0.1/29")).build();
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNetwork(Prefix.parse("255.255.255.0/24"))
                .setAdministrativeCost(1)
                .setNextHopInterface(source1Iface.getName())
                .setNextHopIp(Ip.parse("2.0.0.3"))
                .build()));

    Configuration source2 = cb.build();
    vrf = nf.vrfBuilder().setOwner(source2).build();
    ib.setOwner(source2).setVrf(vrf);
    Interface source2Iface1 = ib.setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/31")).build();
    Interface source2Iface2 = ib.setAddress(ConcreteInterfaceAddress.parse("2.0.0.2/29")).build();
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNetwork(Prefix.parse("255.255.255.0/24"))
                .setAdministrativeCost(1)
                .setNextHopInterface(source2Iface2.getName())
                .setNextHopIp(Ip.parse("2.0.0.3"))
                .build()));

    Configuration fw = cb.build();
    ib.setOwner(fw).setVrf(nf.vrfBuilder().setOwner(fw).build());
    Interface fwI1 = ib.setAddress(ConcreteInterfaceAddress.parse("2.0.0.3/29")).build();
    Interface fwI2 = ib.setAddress(ConcreteInterfaceAddress.parse("255.255.255.0/24")).build();
    // transform source IP before setting up session on fwI2
    Ip poolIp = Ip.parse("5.5.5.5");
    fwI2.setOutgoingTransformation(always().apply(assignSourceIp(poolIp, poolIp)).build());
    // drop all non-session flows entering fwI2
    fwI2.setIncomingFilter(
        nf.aclBuilder()
            .setOwner(fw)
            .setLines(ImmutableList.of(IpAccessListLine.REJECT_ALL))
            .build());

    fwI2.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(false, ImmutableList.of(fwI2.getName()), null, null));

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(
            source1.getHostname(), source1, source2.getHostname(), source2, fw.getHostname(), fw);
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane();

    Location source1Loc = new InterfaceLocation(source1.getHostname(), source1Iface.getName());
    Location source2Loc = new InterfaceLinkLocation(source2.getHostname(), source2Iface1.getName());
    Location fwLoc = new InterfaceLinkLocation(fw.getHostname(), fwI1.getName());
    Ip source1LocIp = Ip.parse("2.0.0.1");
    Ip source2LocIp = Ip.parse("1.0.0.1");
    Ip fwLocIp = Ip.parse("1.0.0.5");

    IpSpaceAssignment assignment =
        IpSpaceAssignment.builder()
            .assign(source1Loc, source1LocIp.toIpSpace())
            .assign(source2Loc, source2LocIp.toIpSpace())
            .assign(fwLoc, fwLocIp.toIpSpace())
            .build();

    BDD source1LocIpBdd = PKT.getSrcIp().value(source1LocIp.asLong());
    BDD source2LocIpBdd = PKT.getSrcIp().value(source2LocIp.asLong());
    BDD fwLocIpBdd = PKT.getSrcIp().value(fwLocIp.asLong());

    Ip dstIp = Ip.parse("255.255.255.1");
    BDD dstIpBdd = PKT.getDstIp().value(dstIp.asLong());

    BidirectionalReachabilityAnalysis analysis =
        new BidirectionalReachabilityAnalysis(
            PKT,
            configurations,
            batfish.loadDataPlane().getForwardingAnalysis(),
            assignment,
            matchDst(dstIp),
            ImmutableSet.of(),
            ImmutableSet.of(),
            configurations.keySet(),
            ALL_DISPOSITIONS);

    // test transformation ranges
    {
      BDDReverseTransformationRanges ranges = analysis.getReverseTransformationRanges();
      NodeInterfacePair source1I1 =
          new NodeInterfacePair(source1.getHostname(), source1Iface.getName());
      assertEquals(
          ranges.reverseOutgoingTransformationRange(
              fw.getHostname(), fwI2.getName(), fwI1.getName(), source1I1),
          PKT.swapSourceAndDestinationFields(source1LocIpBdd.and(dstIpBdd)));
      assertEquals(
          ranges.reverseIncomingTransformationRange(
              fw.getHostname(), fwI1.getName(), fwI1.getName(), source1I1),
          PKT.swapSourceAndDestinationFields(source1LocIpBdd.and(dstIpBdd)));

      NodeInterfacePair source2I2 =
          new NodeInterfacePair(source2.getHostname(), source2Iface2.getName());
      assertEquals(
          ranges.reverseOutgoingTransformationRange(
              fw.getHostname(), fwI2.getName(), fwI1.getName(), source2I2),
          PKT.swapSourceAndDestinationFields(source2LocIpBdd.and(dstIpBdd)));
      assertEquals(
          ranges.reverseIncomingTransformationRange(
              fw.getHostname(), fwI1.getName(), fwI1.getName(), source2I2),
          PKT.swapSourceAndDestinationFields(source2LocIpBdd.and(dstIpBdd)));

      assertEquals(
          ranges.reverseOutgoingTransformationRange(
              fw.getHostname(), fwI2.getName(), fwI1.getName(), null),
          PKT.swapSourceAndDestinationFields(fwLocIpBdd.and(dstIpBdd)));
      assertEquals(
          ranges.reverseIncomingTransformationRange(
              fw.getHostname(), fwI1.getName(), fwI1.getName(), null),
          PKT.swapSourceAndDestinationFields(fwLocIpBdd.and(dstIpBdd)));
    }

    // test the forward-reachable post-state of session edges in the return pass
    {
      Map<StateExpr, BDD> returnPassForwardReachable = analysis.getReturnPassForwardReachableBdds();

      BDD preInSource1 =
          returnPassForwardReachable.get(
              new PreInInterface(source1.getHostname(), source1Iface.getName()));
      assertEquals(preInSource1, PKT.swapSourceAndDestinationFields(source1LocIpBdd.and(dstIpBdd)));

      BDD preInSource2 =
          returnPassForwardReachable.get(
              new PreInInterface(source2.getHostname(), source2Iface2.getName()));
      assertEquals(preInSource2, PKT.swapSourceAndDestinationFields(source2LocIpBdd.and(dstIpBdd)));

      BDD fwDelivered =
          returnPassForwardReachable.get(
              new NodeInterfaceDeliveredToSubnet(fw.getHostname(), fwI1.getName()));
      assertTrue(
          fwDelivered.imp(PKT.swapSourceAndDestinationFields(fwLocIpBdd.and(dstIpBdd))).isOne());
    }

    BidirectionalReachabilityResult result = analysis.getResult();

    assertEquals(
        result.getStartLocationReturnPassSuccessBdds(),
        ImmutableMap.of(
            source1Loc,
            source1LocIpBdd.and(dstIpBdd),
            source2Loc,
            source2LocIpBdd.and(dstIpBdd),
            fwLoc,
            fwLocIpBdd.and(dstIpBdd)));
    assertThat(result.getStartLocationReturnPassFailureBdds().entrySet(), Matchers.empty());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testBidirectionalReachabilityFailure() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setActive(true);

    HeaderSpace tcpHeaderSpace =
        HeaderSpace.builder().setIpProtocols(ImmutableList.of(IpProtocol.TCP)).build();
    IpAccessListLine permitTcpLine =
        IpAccessListLine.accepting().setMatchCondition(match(tcpHeaderSpace)).build();

    HeaderSpace udpHeaderSpace =
        HeaderSpace.builder().setIpProtocols(ImmutableList.of(IpProtocol.UDP)).build();
    IpAccessListLine permitUdpLine =
        IpAccessListLine.accepting().setMatchCondition(match(udpHeaderSpace)).build();

    Configuration source1 = cb.build();
    Vrf vrf = nf.vrfBuilder().setOwner(source1).build();
    ib.setOwner(source1).setVrf(vrf);
    Interface source1Iface = ib.setAddress(ConcreteInterfaceAddress.parse("2.0.0.1/29")).build();
    source1Iface.setIncomingFilter(
        nf.aclBuilder().setOwner(source1).setLines(ImmutableList.of(permitTcpLine)).build());
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNetwork(Prefix.parse("255.255.255.0/24"))
                .setAdministrativeCost(1)
                .setNextHopInterface(source1Iface.getName())
                .setNextHopIp(Ip.parse("2.0.0.3"))
                .build()));

    Configuration source2 = cb.build();
    vrf = nf.vrfBuilder().setOwner(source2).build();
    ib.setOwner(source2).setVrf(vrf);
    Interface source2Iface1 = ib.setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/31")).build();
    Interface source2Iface2 = ib.setAddress(ConcreteInterfaceAddress.parse("2.0.0.2/29")).build();
    Prefix source2Iface1RoutePrefix = Prefix.parse("9.9.9.9/32");
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNetwork(source2Iface1RoutePrefix)
                .setAdministrativeCost(1)
                .setNextHopInterface(source2Iface1.getName())
                .build(),
            StaticRoute.builder()
                .setNetwork(Prefix.parse("255.255.255.0/24"))
                .setAdministrativeCost(1)
                .setNextHopInterface(source2Iface2.getName())
                .setNextHopIp(Ip.parse("2.0.0.3"))
                .build()));

    Configuration fw = cb.build();
    ib.setOwner(fw).setVrf(nf.vrfBuilder().setOwner(fw).build());
    ib.setAddress(ConcreteInterfaceAddress.parse("2.0.0.3/29")).build();
    Interface fwI2 = ib.setAddress(ConcreteInterfaceAddress.parse("3.0.0.1/29")).build();
    Interface fwI3 = ib.setAddress(ConcreteInterfaceAddress.parse("255.255.255.0/24")).build();

    IpAccessList permitUdpAcl =
        nf.aclBuilder().setOwner(fw).setLines(ImmutableList.of(permitUdpLine)).build();
    fwI2.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(false, ImmutableList.of(), null, permitUdpAcl.getName()));

    // transform source IP before setting up session on fwI3
    Ip poolIp = Ip.parse("5.5.5.5");
    fwI3.setOutgoingTransformation(always().apply(assignSourceIp(poolIp, poolIp)).build());
    // drop all non-session flows entering fwI3
    fwI3.setIncomingFilter(
        nf.aclBuilder()
            .setOwner(fw)
            .setLines(ImmutableList.of(IpAccessListLine.REJECT_ALL))
            .build());

    fwI3.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(false, ImmutableList.of(fwI3.getName()), null, null));

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(
            source1.getHostname(), source1, source2.getHostname(), source2, fw.getHostname(), fw);
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane();

    Location source1Loc = new InterfaceLocation(source1.getHostname(), source1Iface.getName());
    Location source2Loc = new InterfaceLinkLocation(source2.getHostname(), source2Iface1.getName());
    Location fwI2Loc = new InterfaceLinkLocation(fw.getHostname(), fwI2.getName());
    Ip source1LocIp = Ip.parse("2.0.0.1");
    Prefix source2LocPrefix = Prefix.parse("9.9.9.0/24");
    Ip fwI2LocIp = Ip.parse("1.0.0.5");

    // return flow to this IP will fail with no route

    IpSpaceAssignment assignment =
        IpSpaceAssignment.builder()
            .assign(source1Loc, source1LocIp.toIpSpace())
            .assign(source2Loc, source2LocPrefix.toIpSpace())
            .assign(fwI2Loc, fwI2LocIp.toIpSpace())
            .build();

    HeaderSpaceToBDD headerSpaceToBDD = new HeaderSpaceToBDD(PKT, ImmutableMap.of());
    IpSpaceToBDD srcIpSpaceToBdd = headerSpaceToBDD.getSrcIpSpaceToBdd();

    BDD source1LocIpBdd = PKT.getSrcIp().value(source1LocIp.asLong());
    BDD source1LocSuccessBdd = source1LocIpBdd.and(headerSpaceToBDD.toBDD(tcpHeaderSpace));
    BDD source1LocFailBdd = source1LocIpBdd.and(headerSpaceToBDD.toBDD(tcpHeaderSpace).not());

    BDD source2LocPrefixBdd = srcIpSpaceToBdd.toBDD(source2LocPrefix);

    BDD source2LocSuccessBdd = srcIpSpaceToBdd.toBDD(source2Iface1RoutePrefix);
    BDD source2LocFailBdd = source2LocPrefixBdd.and(source2LocSuccessBdd.not());

    BDD fwI2LocIpBdd = PKT.getSrcIp().value(fwI2LocIp.asLong());
    BDD fwI2LocSuccessBdd = fwI2LocIpBdd.and(headerSpaceToBDD.toBDD(udpHeaderSpace));
    BDD fwLocFailBdd = fwI2LocIpBdd.and(headerSpaceToBDD.toBDD(udpHeaderSpace).not());

    Ip dstIp = Ip.parse("255.255.255.1");
    BDD dstIpBdd = PKT.getDstIp().value(dstIp.asLong());

    BidirectionalReachabilityResult result =
        new BidirectionalReachabilityAnalysis(
                PKT,
                configurations,
                batfish.loadDataPlane().getForwardingAnalysis(),
                assignment,
                matchDst(dstIp),
                ImmutableSet.of(),
                ImmutableSet.of(),
                configurations.keySet(),
                ALL_DISPOSITIONS)
            .getResult();

    assertEquals(
        result.getStartLocationReturnPassSuccessBdds(),
        ImmutableMap.of(
            source1Loc,
            source1LocSuccessBdd.and(dstIpBdd),
            source2Loc,
            source2LocSuccessBdd.and(dstIpBdd),
            fwI2Loc,
            fwI2LocSuccessBdd.and(dstIpBdd)));

    assertEquals(
        result.getStartLocationReturnPassFailureBdds(),
        ImmutableMap.of(
            source1Loc,
            source1LocFailBdd.and(dstIpBdd),
            source2Loc,
            source2LocFailBdd.and(dstIpBdd),
            fwI2Loc,
            fwLocFailBdd.and(dstIpBdd)));
  }

  private static @Nonnull SortedMap<String, Configuration> makeFibLookupNetwork(
      boolean withNeighbor,
      boolean blockNonSessionReverse,
      boolean separateEgressVrf,
      boolean missingEgressVrfNextVrfRoute) {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration ingressNode = cb.setHostname(INGRESS_NODE).build();

    Vrf ingressVrf = nf.vrfBuilder().setName(INGRESS_VRF).setOwner(ingressNode).build();
    Vrf egressVrf = nf.vrfBuilder().setName(EGRESS_VRF).setOwner(ingressNode).build();

    Interface.Builder ib = Interface.builder().setOwner(ingressNode).setActive(true);
    ib.setName(INGRESS_IFACE).setVrf(ingressVrf).setAddress(INGRESS_IFACE_ADDRESS).build();
    ib.setName(EGRESS_IFACE)
        .setVrf(separateEgressVrf ? egressVrf : ingressVrf)
        .setAddress(EGRESS_IFACE_ADDRESS)
        .setFirewallSessionInterfaceInfo(
            new FirewallSessionInterfaceInfo(true, ImmutableSet.of(EGRESS_IFACE), null, null))
        .setIncomingFilter(
            blockNonSessionReverse
                ? IpAccessList.builder()
                    .setOwner(ingressNode)
                    .setName("blockAll")
                    .setLines(ImmutableList.of())
                    .build()
                : null)
        .build();
    if (separateEgressVrf) {
      ingressVrf
          .getStaticRoutes()
          .add(
              StaticRoute.builder()
                  .setNetwork(Prefix.ZERO)
                  .setNextVrf(EGRESS_VRF)
                  .setAdmin(1)
                  .build());
      if (!missingEgressVrfNextVrfRoute) {
        egressVrf
            .getStaticRoutes()
            .add(
                StaticRoute.builder()
                    .setNetwork(Prefix.ZERO)
                    .setNextVrf(INGRESS_VRF)
                    .setAdmin(1)
                    .build());
      }
    }

    if (!withNeighbor) {
      return ImmutableSortedMap.of(ingressNode.getHostname(), ingressNode);
    }

    Configuration neighbor = cb.setHostname(NEIGHBOR).build();
    Vrf neighborVrf = nf.vrfBuilder().setName(NEIGHBOR_VRF).setOwner(neighbor).build();
    Interface.builder()
        .setActive(true)
        .setOwner(neighbor)
        .setVrf(neighborVrf)
        .setName(NEIGHBOR_IFACE)
        .setAddress(NEIGHBOR_IFACE_ADDRESS)
        .build();
    neighborVrf
        .getStaticRoutes()
        .add(
            StaticRoute.builder()
                .setNetwork(Prefix.ZERO)
                .setAdmin(1)
                .setNextHopIp(EGRESS_IFACE_ADDRESS.getIp())
                .build());

    return ImmutableSortedMap.of(INGRESS_NODE, ingressNode, NEIGHBOR, neighbor);
  }

  @Test
  public void testFibLookupBidirAcceptSingleNodeSingleVrf() throws IOException {
    assertBidirAcceptSingleNode(makeFibLookupNetwork(false, false, false, false));
  }

  @Test
  public void testFibLookupBidirAcceptDualNodeSingleVrf() throws IOException {
    assertBidirAcceptDualNode(makeFibLookupNetwork(true, false, false, false));
  }

  @Test
  public void testFibLookupBidirAcceptSingleNodeSingleVrfBlockNonSessionReverse()
      throws IOException {
    assertBidirAcceptSingleNode(makeFibLookupNetwork(false, true, false, false));
  }

  @Test
  public void testFibLookupBidirAcceptDualNodeSingleVrfBlockNonSessionReverse() throws IOException {
    assertBidirAcceptDualNode(makeFibLookupNetwork(true, true, false, false));
  }

  @Test
  public void testFibLookupBidirAcceptSingleNodeDualVrf() throws IOException {
    assertBidirAcceptSingleNode(makeFibLookupNetwork(false, false, true, false));
  }

  @Test
  public void testFibLookupBidirAcceptDualNodeDualVrf() throws IOException {
    assertBidirAcceptDualNode(makeFibLookupNetwork(true, false, true, false));
  }

  @Test
  public void testFibLookupBidirAcceptSingleNodeDualVrfMissingEgressRoute() throws IOException {
    assertBidirReturnNoRouteSingleNode(makeFibLookupNetwork(false, false, true, true));
  }

  @Test
  public void testFibLookupBidirAcceptDualNodeDualVrfMissingEgressRoute() throws IOException {
    assertBidirReturnNoRouteDualNode(makeFibLookupNetwork(true, false, true, true));
  }

  private void assertBidirAcceptSingleNode(SortedMap<String, Configuration> configurations)
      throws IOException {
    assertBidirAccept(
        configurations, DST_IP_SPACE_SINGLE_NODE, ImmutableSet.of(DELIVERED_TO_SUBNET));
  }

  private void assertBidirAcceptDualNode(SortedMap<String, Configuration> configurations)
      throws IOException {
    assertBidirAccept(configurations, DST_IP_SPACE_DUAL_NODE, ALL_DISPOSITIONS);
  }

  private void assertBidirReturnNoRouteSingleNode(SortedMap<String, Configuration> configurations)
      throws IOException {
    assertBidirReturnNoRoute(
        configurations, DST_IP_SPACE_SINGLE_NODE, ImmutableSet.of(DELIVERED_TO_SUBNET));
  }

  private void assertBidirReturnNoRouteDualNode(SortedMap<String, Configuration> configurations)
      throws IOException {
    assertBidirReturnNoRoute(configurations, DST_IP_SPACE_DUAL_NODE, ALL_DISPOSITIONS);
  }

  private void assertBidirAccept(
      SortedMap<String, Configuration> configurations,
      IpSpace dstIpSpaceOfInterest,
      Set<FlowDisposition> forwardDispositions)
      throws IOException {
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane();

    // Bidirectional analysis
    BidirectionalReachabilityAnalysis analysis =
        new BidirectionalReachabilityAnalysis(
            PKT,
            configurations,
            batfish.loadDataPlane().getForwardingAnalysis(),
            IpSpaceAssignment.builder().assign(INGRESS_LOCATION, UniverseIpSpace.INSTANCE).build(),
            matchDst(dstIpSpaceOfInterest),
            ImmutableSet.of(),
            ImmutableSet.of(),
            configurations.keySet(),
            forwardDispositions);
    BidirectionalReachabilityResult result = analysis.getResult();

    assertThat(
        result.getStartLocationReturnPassSuccessBdds(),
        equalTo(
            ImmutableMap.of(
                INGRESS_LOCATION,
                dstIpSpaceOfInterest
                    .accept(TO_DST_IP_BDD)
                    .and(
                        difference(
                                INGRESS_IFACE_ADDRESS.getPrefix().toIpSpace(),
                                INGRESS_IFACE_ADDRESS.getIp().toIpSpace())
                            .accept(TO_SRC_IP_BDD)))));
  }

  private void assertBidirReturnNoRoute(
      SortedMap<String, Configuration> configurations,
      IpSpace dstIpSpaceOfInterest,
      Set<FlowDisposition> forwardDispositions)
      throws IOException {
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane();

    IpSpace srcIpSpaceOfInterest =
        AclIpSpace.rejecting(NEIGHBOR_IFACE_ADDRESS.getPrefix().toIpSpace())
            .thenPermitting(UniverseIpSpace.INSTANCE)
            .build();

    // Bidirectional analysis
    BidirectionalReachabilityAnalysis analysis =
        new BidirectionalReachabilityAnalysis(
            PKT,
            configurations,
            batfish.loadDataPlane().getForwardingAnalysis(),
            IpSpaceAssignment.builder().assign(INGRESS_LOCATION, UniverseIpSpace.INSTANCE).build(),
            AclLineMatchExprs.match(
                HeaderSpace.builder()
                    .setDstIps(dstIpSpaceOfInterest)
                    .setSrcIps(srcIpSpaceOfInterest)
                    .build()),
            ImmutableSet.of(),
            ImmutableSet.of(),
            configurations.keySet(),
            forwardDispositions);
    BidirectionalReachabilityResult result = analysis.getResult();

    assertThat(
        result.getStartLocationReturnPassFailureBdds(),
        equalTo(
            ImmutableMap.of(
                INGRESS_LOCATION,
                dstIpSpaceOfInterest
                    .accept(TO_DST_IP_BDD)
                    .and(srcIpSpaceOfInterest.accept(TO_SRC_IP_BDD)))));
  }
}
