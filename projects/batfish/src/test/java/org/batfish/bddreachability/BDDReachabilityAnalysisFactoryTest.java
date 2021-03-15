package org.batfish.bddreachability;

import static org.batfish.bddreachability.EdgeMatchers.edge;
import static org.batfish.bddreachability.EdgeMatchers.hasTransition;
import static org.batfish.bddreachability.TransitionMatchers.mapsBackward;
import static org.batfish.bddreachability.TransitionMatchers.mapsForward;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.addOriginatingFromDeviceConstraint;
import static org.batfish.bddreachability.transition.Transitions.compose;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.datamodel.ExprAclLine.ACCEPT_ALL;
import static org.batfish.datamodel.ExprAclLine.REJECT_ALL;
import static org.batfish.datamodel.ExprAclLine.accepting;
import static org.batfish.datamodel.ExprAclLine.acceptingHeaderSpace;
import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.DENIED_IN;
import static org.batfish.datamodel.FlowDisposition.DENIED_OUT;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.INSUFFICIENT_INFO;
import static org.batfish.datamodel.FlowDisposition.NEIGHBOR_UNREACHABLE;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.batfish.datamodel.FlowDisposition.NULL_ROUTED;
import static org.batfish.datamodel.FlowDisposition.SUCCESS_DISPOSITIONS;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.match;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.transformation.Noop.NOOP_DEST_NAT;
import static org.batfish.datamodel.transformation.Noop.NOOP_SOURCE_NAT;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationPort;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourceIp;
import static org.batfish.datamodel.transformation.TransformationStep.assignSourcePort;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceWrapperAsAnswerElement;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.FibLookupOutgoingInterfaceIsOneOf;
import org.batfish.datamodel.packet_policy.If;
import org.batfish.datamodel.packet_policy.LiteralVrfName;
import org.batfish.datamodel.packet_policy.PacketMatchExpr;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.Return;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.batfish.question.ReachabilityParameters;
import org.batfish.question.loop.LoopNetwork;
import org.batfish.specifier.AllInterfacesLocationSpecifier;
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.ConstantIpSpaceSpecifier;
import org.batfish.specifier.InterfaceLinkLocation;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.IpSpaceSpecifier;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationSpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.symbolic.IngressLocation;
import org.batfish.symbolic.state.Accept;
import org.batfish.symbolic.state.DropAclIn;
import org.batfish.symbolic.state.DropAclOut;
import org.batfish.symbolic.state.DropNoRoute;
import org.batfish.symbolic.state.DropNullRoute;
import org.batfish.symbolic.state.NodeAccept;
import org.batfish.symbolic.state.NodeDropAclIn;
import org.batfish.symbolic.state.NodeDropAclOut;
import org.batfish.symbolic.state.NodeDropNoRoute;
import org.batfish.symbolic.state.NodeDropNullRoute;
import org.batfish.symbolic.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.symbolic.state.NodeInterfaceExitsNetwork;
import org.batfish.symbolic.state.NodeInterfaceInsufficientInfo;
import org.batfish.symbolic.state.NodeInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.OriginateInterface;
import org.batfish.symbolic.state.OriginateInterfaceLink;
import org.batfish.symbolic.state.OriginateVrf;
import org.batfish.symbolic.state.PbrFibLookup;
import org.batfish.symbolic.state.PostInInterface;
import org.batfish.symbolic.state.PostInVrf;
import org.batfish.symbolic.state.PreInInterface;
import org.batfish.symbolic.state.PreOutEdge;
import org.batfish.symbolic.state.PreOutEdgePostNat;
import org.batfish.symbolic.state.PreOutInterfaceDeliveredToSubnet;
import org.batfish.symbolic.state.PreOutInterfaceExitsNetwork;
import org.batfish.symbolic.state.PreOutInterfaceInsufficientInfo;
import org.batfish.symbolic.state.PreOutInterfaceNeighborUnreachable;
import org.batfish.symbolic.state.PreOutVrf;
import org.batfish.symbolic.state.Query;
import org.batfish.symbolic.state.StateExpr;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests of {@link BDDReachabilityAnalysisFactory}. */
public final class BDDReachabilityAnalysisFactoryTest {
  @Rule public TemporaryFolder temp = new TemporaryFolder();

  private final BDDPacket _pkt = new BDDPacket();
  private final BDD _one = _pkt.getFactory().one();
  private final BDD _zero = _pkt.getFactory().zero();

  private static final IpSpaceSpecifier CONSTANT_UNIVERSE_IPSPACE_SPECIFIER =
      new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE);
  private static final String INGRESS_NODE = "ingress_node";
  private static final String INGRESS_IFACE = "ingressIface";
  private static final String INGRESS_VRF = "ingressVrf";
  private static final String NEXT_VRF = "nextVrf";

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

  private static IpSpaceAssignment ipSpaceAssignment(Batfish batfish) {
    SpecifierContext ctxt = batfish.specifierContext(batfish.getSnapshot());
    Set<Location> locations = LocationSpecifier.ALL_LOCATIONS.resolve(ctxt);
    return CONSTANT_UNIVERSE_IPSPACE_SPECIFIER.resolve(locations, ctxt);
  }

  private BDDReachabilityAnalysisFactory makeBddReachabilityAnalysisFactory(
      SortedMap<String, Configuration> configs) throws IOException {
    return makeBddReachabilityAnalysisFactory(configs, false);
  }

  private BDDReachabilityAnalysisFactory makeBddReachabilityAnalysisFactory(
      SortedMap<String, Configuration> configs, boolean ignoreFilters) throws IOException {
    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane(batfish.getSnapshot());
    DataPlane dataPlane = batfish.loadDataPlane(batfish.getSnapshot());
    return new BDDReachabilityAnalysisFactory(
        _pkt,
        configs,
        dataPlane.getForwardingAnalysis(),
        new IpsRoutedOutInterfacesFactory(dataPlane.getFibs()),
        ignoreFilters,
        false);
  }

  @Test
  public void testBDDFactory() throws IOException {
    // Confirm factory building does not throw, even with IpSpace and ACL indirection
    TestNetworkIndirection net = new TestNetworkIndirection();
    makeBddReachabilityAnalysisFactory(net._configs);
  }

  @Test
  public void testAnalysisUseInterfaceRootsParam() throws IOException {
    SortedMap<String, Configuration> configs = TestNetworkSources.twoNodeNetwork();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    assertThat(configs.size(), equalTo(2));
    for (String node : configs.keySet()) {
      Map<StateExpr, Map<StateExpr, Transition>> ifaceRootsEdges =
          factory
              .bddReachabilityAnalysis(
                  ipSpaceAssignment(batfish),
                  TRUE,
                  ImmutableSet.of(),
                  ImmutableSet.of(),
                  ImmutableSet.of(node),
                  ALL_DISPOSITIONS,
                  true)
              .getForwardEdgeMap();
      Map<StateExpr, Map<StateExpr, Transition>> vrfRootsEdges =
          factory
              .bddReachabilityAnalysis(
                  ipSpaceAssignment(batfish),
                  TRUE,
                  ImmutableSet.of(),
                  ImmutableSet.of(),
                  ImmutableSet.of(node),
                  ALL_DISPOSITIONS,
                  false)
              .getForwardEdgeMap();

      // Test that the expected edges show up:
      // OriginateInterface -> PostInVrf if useInterfaceRoots is set, and no OriginateVrfs
      // OriginateVrf -> PostInVrf if useInterfaceRoots is not set, and no OriginateInterfaces
      Map<OriginateInterface, PostInVrf> originateIfaceEdges =
          toImmutableMap(
              configs.get(node).getActiveInterfaces(),
              ifaceEntry -> new OriginateInterface(node, ifaceEntry.getKey()),
              ifaceEntry -> new PostInVrf(node, ifaceEntry.getValue().getVrfName()));
      Map<OriginateVrf, PostInVrf> originateVrfEdges =
          toImmutableMap(
              configs.get(node).getVrfs().keySet(),
              vrf -> new OriginateVrf(node, vrf),
              vrf -> new PostInVrf(node, vrf));

      // sanity check
      assert !originateIfaceEdges.isEmpty() && !originateVrfEdges.isEmpty();

      originateIfaceEdges.forEach(
          (originateIface, postInVrf) -> {
            assertThat(ifaceRootsEdges, hasEntry(equalTo(originateIface), hasKey(postInVrf)));
            assertThat(vrfRootsEdges, not(hasKey(equalTo(originateIface))));
          });
      originateVrfEdges.forEach(
          (originateVrf, postInVrf) -> {
            assertThat(vrfRootsEdges, hasEntry(equalTo(originateVrf), hasKey(postInVrf)));
            assertThat(ifaceRootsEdges, not(hasKey(equalTo(originateVrf))));
          });
    }
  }

  @Test
  public void testFinalNodes() throws IOException {
    SortedMap<String, Configuration> configs = TestNetworkSources.twoNodeNetwork();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    assertThat(configs.size(), equalTo(2));
    for (String node : configs.keySet()) {
      String otherNode = configs.keySet().stream().filter(n -> !n.equals(node)).findFirst().get();
      Map<StateExpr, Map<StateExpr, Transition>> edges =
          factory
              .bddReachabilityAnalysis(
                  ipSpaceAssignment(batfish),
                  TRUE,
                  ImmutableSet.of(),
                  ImmutableSet.of(),
                  ImmutableSet.of(node),
                  ALL_DISPOSITIONS)
              .getForwardEdgeMap();
      assertThat(edges, hasEntry(equalTo(new NodeAccept(node)), hasKey(Accept.INSTANCE)));
      assertThat(edges, not(hasEntry(equalTo(new NodeAccept(otherNode)), hasKey(Accept.INSTANCE))));

      assertThat(edges, hasEntry(equalTo(new NodeDropAclIn(node)), hasKey(DropAclIn.INSTANCE)));
      assertThat(
          edges, not(hasEntry(equalTo(new NodeDropAclIn(otherNode)), hasKey(DropAclIn.INSTANCE))));

      assertThat(edges, hasEntry(equalTo(new NodeDropAclOut(node)), hasKey(DropAclOut.INSTANCE)));
      assertThat(
          edges,
          not(hasEntry(equalTo(new NodeDropAclOut(otherNode)), hasKey(DropAclOut.INSTANCE))));

      assertThat(edges, hasEntry(equalTo(new NodeDropNoRoute(node)), hasKey(DropNoRoute.INSTANCE)));
      assertThat(
          edges,
          not(hasEntry(equalTo(new NodeDropNoRoute(otherNode)), hasKey(DropNoRoute.INSTANCE))));

      assertThat(
          edges, hasEntry(equalTo(new NodeDropNullRoute(node)), hasKey(DropNullRoute.INSTANCE)));
      assertThat(
          edges,
          not(hasEntry(equalTo(new NodeDropNullRoute(otherNode)), hasKey(DropNullRoute.INSTANCE))));
    }
  }

  @Test
  public void testForbiddenTransitNodes() throws IOException {
    SortedMap<String, Configuration> configs = TestNetworkSources.twoNodeNetwork();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    assertThat(configs.size(), equalTo(2));
    for (String node : configs.keySet()) {
      String otherNode = configs.keySet().stream().filter(n -> !n.equals(node)).findFirst().get();
      Map<StateExpr, Map<StateExpr, Transition>> edgeMap =
          factory
              .bddReachabilityAnalysis(
                  ipSpaceAssignment(batfish),
                  TRUE,
                  ImmutableSet.of(node),
                  ImmutableSet.of(),
                  configs.keySet(),
                  ALL_DISPOSITIONS)
              .getForwardEdgeMap();
      Set<Edge> edges = getEdges(edgeMap);

      assertTrue(
          "Edges at which a forbiddenTransitNode would become transited should be removed.",
          edges.stream()
              .noneMatch(
                  edge ->
                      edge.getPreState() instanceof PreOutEdgePostNat
                          && edge.getPostState() instanceof PreInInterface
                          && ((PreOutEdgePostNat) edge.getPreState()).getSrcNode().equals(node)));
      assertTrue(
          "Edges at which a non-forbiddenTransitNodes become transited should not be removed.",
          edges.stream()
              .anyMatch(
                  edge ->
                      edge.getPreState() instanceof PreOutEdgePostNat
                          && edge.getPostState() instanceof PreInInterface
                          && ((PreOutEdgePostNat) edge.getPreState())
                              .getSrcNode()
                              .equals(otherNode)));
    }
  }

  @Nonnull
  private Set<Edge> getEdges(Map<StateExpr, Map<StateExpr, Transition>> edgeMap) {
    return edgeMap.entrySet().stream()
        .flatMap(
            srcEntry ->
                srcEntry.getValue().entrySet().stream()
                    .map(
                        tgtEntry ->
                            new Edge(srcEntry.getKey(), tgtEntry.getKey(), tgtEntry.getValue())))
        .collect(Collectors.toSet());
  }

  @Test
  public void testRequiredTransitNodes() throws IOException {
    SortedMap<String, Configuration> configs = TestNetworkSources.twoNodeNetwork();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    assertThat(configs.size(), equalTo(2));
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);
    for (String node : configs.keySet()) {
      BDD requiredTransitNodesBDD = factory.getRequiredTransitNodeBDD();
      BDD transited = requiredTransitNodesBDD;
      BDD notTransited = requiredTransitNodesBDD.not();
      Map<StateExpr, Map<StateExpr, Transition>> edgeMap =
          factory
              .bddReachabilityAnalysis(
                  ipSpaceAssignment(batfish),
                  TRUE,
                  ImmutableSet.of(),
                  ImmutableSet.of(node),
                  configs.keySet(),
                  ALL_DISPOSITIONS)
              .getForwardEdgeMap();
      Set<Edge> edges = getEdges(edgeMap);

      // all edges into the query state require transit nodes transited bit to be set.
      edges.stream()
          .filter(edge -> edge.getPostState() == Query.INSTANCE)
          .forEach(
              edge -> {
                assertFalse(
                    "Edge into query state must require requiredTransitNodes bit to be one",
                    edge.traverseForward(_one).andSat(notTransited));
                assertFalse(
                    "Edge into query state must require requiredTransitNodes bit to be one",
                    edge.traverseBackward(_one).andSat(notTransited));
              });

      // all edges from originate states initialize the transit nodes bit to zero
      edges.stream()
          .filter(
              edge ->
                  edge.getPreState() instanceof OriginateVrf
                      || edge.getPreState() instanceof OriginateInterfaceLink)
          .forEach(
              edge -> {
                StateExpr preState = edge.getPreState();
                String hostname = null;
                if (preState instanceof OriginateVrf) {
                  hostname = ((OriginateVrf) preState).getHostname();
                } else {
                  hostname = ((OriginateInterfaceLink) preState).getHostname();
                }
                assertFalse(
                    "Edge out of originate state must require requiredTransitNodes bit to be zero",
                    edge.traverseForward(_one).andSat(transited));
                assertFalse(
                    "Edge out of originate state must require requiredTransitNodes bit to be zero",
                    edge.traverseBackward(
                            factory.getBDDSourceManagers().get(hostname).isValidValue())
                        .andSat(transited));
              });

      edges.stream()
          .filter(
              edge ->
                  edge.getPreState() instanceof PreOutEdgePostNat
                      && edge.getPostState() instanceof PreInInterface)
          .forEach(
              edge -> {
                String hostname = ((PreOutEdgePostNat) edge.getPreState()).getSrcNode();
                String peername = ((PreInInterface) edge.getPostState()).getHostname();
                BDD validSrc = factory.getBDDSourceManagers().get(hostname).isValidValue();
                BDD peerValidSrc = factory.getBDDSourceManagers().get(peername).isValidValue();
                if (hostname.equals(node)) {
                  assertFalse(
                      "Forward Edge from PreOutEdgePostNat to PreInInterface for a "
                          + "requiredTransitNode must set the requiredTransitNodes bit",
                      edge.traverseForward(validSrc).andSat(notTransited));
                  BDD backwardOne = edge.traverseBackward(peerValidSrc);
                  assertTrue(
                      "Backward Edge from PreOutEdgePostNat to PreInInterface for a "
                          + " requiredTransitNode must not constrain the bit after exit",
                      backwardOne.exist(requiredTransitNodesBDD).equals(backwardOne));
                } else {
                  assertFalse(
                      "Edge from PreOutEdgePostNat to PreInInterface for a "
                          + "non-requiredTransitNode must not touch the requiredTransitNodes bit",
                      edge.traverseForward(transited.and(validSrc)).andSat(notTransited));
                  assertFalse(
                      "Edge from PreOutEdgePostNat to PreInInterface for a "
                          + "non-requiredTransitNode must not touch the requiredTransitNodes bit",
                      edge.traverseForward(notTransited.and(validSrc)).andSat(transited));
                  assertFalse(
                      "Edge from PreOutEdgePostNat to PreInInterface for a "
                          + "non-requiredTransitNode must not touch the requiredTransitNodes bit",
                      edge.traverseBackward(transited.and(peerValidSrc)).andSat(notTransited));
                  assertFalse(
                      "Edge from PreOutEdgePostNat to PreInInterface for a "
                          + "non-requiredTransitNode must not touch the requiredTransitNodes bit",
                      edge.traverseBackward(notTransited.and(peerValidSrc)).andSat(transited));
                }
              });
    }
  }

  @Test
  public void testGetAllBDDsLoop() throws IOException {
    SortedMap<String, Configuration> configs = LoopNetwork.testLoopNetwork(true);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane(batfish.getSnapshot());

    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setSourceLocationSpecifier(AllInterfacesLocationSpecifier.INSTANCE)
            .setActions(ImmutableSortedSet.of(FlowDisposition.LOOP))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setHeaderSpace(new HeaderSpace())
            .build();

    AnswerElement answer =
        batfish.bddSingleReachability(batfish.getSnapshot(), reachabilityParameters);

    assertThat(answer, instanceOf(TraceWrapperAsAnswerElement.class));
    Map<Flow, List<Trace>> flowTraces = ((TraceWrapperAsAnswerElement) answer).getFlowTraces();
    Set<Flow> flows = flowTraces.keySet();
    assertThat(flows, hasSize(2));

    Set<Ip> srcIps = flows.stream().map(Flow::getSrcIp).collect(Collectors.toSet());
    Set<Ip> dstIps = flows.stream().map(Flow::getDstIp).collect(Collectors.toSet());

    assertThat(srcIps, contains(Ip.parse("1.0.0.0"), Ip.parse("1.0.0.1")));
    assertThat(dstIps, contains(Ip.parse("2.0.0.0")));

    Set<FlowDisposition> flowDispositions =
        flowTraces.values().stream()
            .flatMap(List::stream)
            .map(Trace::getDisposition)
            .collect(ImmutableSet.toImmutableSet());
    assertThat(flowDispositions, contains(FlowDisposition.LOOP));
  }

  @Test
  public void testGetAllBDDsLoopWithNoroute() throws IOException {
    SortedMap<String, Configuration> configs = new TreeMap<>(LoopNetwork.testLoopNetwork(true));

    // adding an isolated config which has no route to the looping destination prefix
    NetworkFactory nf = new NetworkFactory();
    Configuration loopbackConfig =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("loopbackConfig")
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(loopbackConfig).build();

    ConcreteInterfaceAddress loopbackAddress = ConcreteInterfaceAddress.parse("1.2.3.4/32");
    nf.interfaceBuilder()
        .setActive(true)
        .setOwner(loopbackConfig)
        .setVrf(vrf)
        .setAddress(loopbackAddress)
        .build();
    configs.put(loopbackConfig.getHostname(), loopbackConfig);

    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane(batfish.getSnapshot());

    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setSourceLocationSpecifier(AllInterfacesLocationSpecifier.INSTANCE)
            .setActions(ImmutableSortedSet.of(FlowDisposition.LOOP, FlowDisposition.NO_ROUTE))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setHeaderSpace(
                HeaderSpace.builder().setDstIps(Ip.parse("2.0.0.0").toIpSpace()).build())
            .build();

    AnswerElement answer =
        batfish.bddSingleReachability(batfish.getSnapshot(), reachabilityParameters);

    assertThat(answer, instanceOf(TraceWrapperAsAnswerElement.class));
    Map<Flow, List<Trace>> flowTraces = ((TraceWrapperAsAnswerElement) answer).getFlowTraces();
    Set<Flow> flows = flowTraces.keySet();
    assertThat(flows, hasSize(3));

    Set<Ip> srcIps = flows.stream().map(Flow::getSrcIp).collect(Collectors.toSet());
    Set<Ip> dstIps = flows.stream().map(Flow::getDstIp).collect(Collectors.toSet());

    assertThat(srcIps, contains(Ip.parse("1.0.0.0"), Ip.parse("1.0.0.1"), Ip.parse("1.2.3.4")));
    assertThat(dstIps, contains(Ip.parse("2.0.0.0")));

    Set<FlowDisposition> flowDispositions =
        flowTraces.values().stream()
            .flatMap(List::stream)
            .map(Trace::getDisposition)
            .collect(ImmutableSet.toImmutableSet());

    assertThat(
        flowDispositions, containsInAnyOrder(FlowDisposition.LOOP, FlowDisposition.NO_ROUTE));
  }

  @Test
  public void testInactiveInterface() throws IOException {
    Ip ip = Ip.parse("1.2.3.4");
    BDD ipBDD = new IpSpaceToBDD(_pkt.getDstIp()).toBDD(ip);

    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setAddress(ConcreteInterfaceAddress.create(ip, 32))
            .setActive(true)
            .build();

    // when interface is active and not blacklisted, its Ip belongs to the VRF
    ImmutableSortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(config.getHostname(), config);
    {
      BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);
      Map<String, Map<String, Map<String, BDD>>> expectedAcceptBdds =
          ImmutableMap.of(
              config.getHostname(),
              ImmutableMap.of(vrf.getName(), ImmutableMap.of(iface.getName(), ipBDD)));
      assertThat(factory.getIfaceAcceptBDDs(), equalTo(expectedAcceptBdds));
    }

    // when interface is inactive, it doesn't own any IPs
    {
      iface.setActive(false);
      BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);
      Map<String, Map<String, Map<String, BDD>>> expectedAcceptBdds =
          ImmutableMap.of(
              config.getHostname(),
              ImmutableMap.of(
                  vrf.getName(), ImmutableMap.of(iface.getName(), _pkt.getFactory().zero())));
      assertThat(factory.getIfaceAcceptBDDs(), equalTo(expectedAcceptBdds));
    }

    // when interface is blacklisted, it doesn't own any IPs
    {
      iface.blacklist();
      BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);
      Map<String, Map<String, Map<String, BDD>>> expectedAcceptBdds =
          ImmutableMap.of(
              config.getHostname(),
              ImmutableMap.of(
                  vrf.getName(), ImmutableMap.of(iface.getName(), _pkt.getFactory().zero())));
      assertThat(factory.getIfaceAcceptBDDs(), equalTo(expectedAcceptBdds));
    }
  }

  @Test
  public void testDestNat() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Ip poolIp = Ip.parse("5.5.5.5");
    HeaderSpace ingressAclHeaderSpace =
        HeaderSpace.builder().setSrcIps(Prefix.parse("2.0.0.0/8").toIpSpace()).build();
    HeaderSpace natMatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(SubRange.singleton(100))).build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setActive(true)
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/8"))
            .setIncomingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(ingressAclHeaderSpace)))
                    .build())
            .setIncomingTransformation(
                when(match(natMatchHeaderSpace))
                    .apply(TransformationStep.assignDestinationIp(poolIp, poolIp))
                    .build())
            .build();

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(config.getHostname(), config);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLinkLocation(config.getHostname(), iface.getName()),
                    UniverseIpSpace.INSTANCE)
                .build());
    Transition transition =
        analysis
            .getForwardEdgeMap()
            .get(new PreInInterface(config.getHostname(), iface.getName()))
            .get(new PostInInterface(config.getHostname(), iface.getName()));

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(_pkt, ImmutableMap.of());
    BDD ingressAclBdd = toBDD.toBDD(ingressAclHeaderSpace);
    BDD natMatchBdd = toBDD.toBDD(natMatchHeaderSpace);
    BDD origDstIpBdd = toBDD.getDstIpSpaceToBdd().toBDD(Ip.parse("6.6.6.6"));
    BDD poolIpBdd = toBDD.getDstIpSpaceToBdd().toBDD(poolIp);

    assertThat(
        transition.transitForward(origDstIpBdd),
        equalTo(ingressAclBdd.and(natMatchBdd.ite(poolIpBdd, origDstIpBdd))));
  }

  /**
   * Test correctly handling of DestinationNat rules with null pools. If the packet matches the
   * rule, then the packet is not natted and no further rules are applied.
   */
  @Test
  public void testDestNatNullPool() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Ip poolIp = Ip.parse("5.5.5.5");
    HeaderSpace ingressAclHeaderSpace =
        HeaderSpace.builder().setSrcIps(Prefix.parse("2.0.0.0/8").toIpSpace()).build();
    HeaderSpace nat1MatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(SubRange.singleton(105))).build();
    HeaderSpace nat2MatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(100, 110))).build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setActive(true)
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/8"))
            .setIncomingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(ingressAclHeaderSpace)))
                    .build())
            .setIncomingTransformation(
                when(match(nat1MatchHeaderSpace))
                    .apply(NOOP_DEST_NAT)
                    .setOrElse(
                        when(match(nat2MatchHeaderSpace))
                            .apply(assignDestinationIp(poolIp, poolIp))
                            .build())
                    .build())
            .build();

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(config.getHostname(), config);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLinkLocation(config.getHostname(), iface.getName()),
                    UniverseIpSpace.INSTANCE)
                .build());
    Transition transition =
        analysis
            .getForwardEdgeMap()
            .get(new PreInInterface(config.getHostname(), iface.getName()))
            .get(new PostInInterface(config.getHostname(), iface.getName()));

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(_pkt, ImmutableMap.of());
    BDD ingressAclBdd = toBDD.toBDD(ingressAclHeaderSpace);
    BDD nat1MatchBdd = toBDD.toBDD(nat1MatchHeaderSpace);
    BDD nat2MatchBdd = toBDD.toBDD(nat2MatchHeaderSpace);
    BDD origDstIpBdd = toBDD.getDstIpSpaceToBdd().toBDD(Ip.parse("6.6.6.6"));
    BDD poolIpBdd = toBDD.getDstIpSpaceToBdd().toBDD(poolIp);

    assertThat(
        transition.transitForward(origDstIpBdd),
        equalTo(
            ingressAclBdd.and(nat1MatchBdd.not().and(nat2MatchBdd).ite(poolIpBdd, origDstIpBdd))));
  }

  @Test
  public void testSrcNatNullPool() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Ip poolIp = Ip.parse("5.5.5.5");
    HeaderSpace preNatAclHeaderSpace =
        HeaderSpace.builder().setDstIps(Prefix.parse("2.0.0.0/24").toIpSpace()).build();
    HeaderSpace nat1MatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(SubRange.singleton(105))).build();
    HeaderSpace nat2MatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(100, 110))).build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setActive(true)
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/24"))
            .setPreTransformationOutgoingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(preNatAclHeaderSpace)))
                    .build())
            .setOutgoingTransformation(
                when(match(nat1MatchHeaderSpace))
                    .apply(NOOP_SOURCE_NAT)
                    .setOrElse(
                        when(match(nat2MatchHeaderSpace))
                            .apply(assignSourceIp(poolIp, poolIp))
                            .build())
                    .build())
            .build();
    String ifaceName = iface.getName();

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(config.getHostname(), config);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLinkLocation(config.getHostname(), ifaceName),
                    UniverseIpSpace.INSTANCE)
                .build());
    Transition transition =
        analysis
            .getForwardEdgeMap()
            .get(new PreOutInterfaceDeliveredToSubnet(config.getHostname(), ifaceName))
            .get(new NodeInterfaceDeliveredToSubnet(config.getHostname(), ifaceName));

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(_pkt, ImmutableMap.of());
    BDD preNatAclBdd = toBDD.toBDD(preNatAclHeaderSpace);
    BDD nat1MatchBdd = toBDD.toBDD(nat1MatchHeaderSpace);
    BDD nat2MatchBdd = toBDD.toBDD(nat2MatchHeaderSpace);
    BDD origSrcIpBdd = toBDD.getSrcIpSpaceToBdd().toBDD(Ip.parse("6.6.6.6"));
    BDD poolIpBdd = toBDD.getSrcIpSpaceToBdd().toBDD(poolIp);

    assertThat(
        transition.transitForward(origSrcIpBdd),
        equalTo(
            preNatAclBdd.and(nat1MatchBdd.not().and(nat2MatchBdd).ite(poolIpBdd, origSrcIpBdd))));
  }

  @Test
  public void testSourceNatExitsNetwork() throws IOException {
    /*
     * Pre-NAT out ACL permits dest source ports 50 - 150
     * Source NAT will write source IP to 5.5.5.5 if source port is 100
     * Post-NAT out ACL permits dest port 80
     */
    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Ip poolIp = Ip.parse("5.5.5.5");
    HeaderSpace postNatOutAclHeaderSpace =
        HeaderSpace.builder().setDstPorts(ImmutableList.of(SubRange.singleton(80))).build();
    HeaderSpace natMatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(SubRange.singleton(100))).build();
    HeaderSpace preNatOutAclHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(50, 150))).build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setActive(true)
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/31"))
            .setPreTransformationOutgoingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(preNatOutAclHeaderSpace)))
                    .build())
            .setOutgoingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(postNatOutAclHeaderSpace)))
                    .build())
            .setOutgoingTransformation(
                when(match(natMatchHeaderSpace)).apply(assignSourceIp(poolIp, poolIp)).build())
            .build();
    String ifaceName = iface.getName();
    Prefix staticRoutePrefix = Prefix.parse("3.3.3.3/32");
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopInterface(ifaceName)
                .setAdministrativeCost(1)
                .setNetwork(staticRoutePrefix)
                .build()));

    String hostname = config.getHostname();

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(hostname, config);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(new InterfaceLocation(hostname, ifaceName), UniverseIpSpace.INSTANCE)
                .build());

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(_pkt, ImmutableMap.of());
    IpSpaceToBDD srcToBdd = toBDD.getSrcIpSpaceToBdd();

    BDD preNatOutAclBdd = toBDD.toBDD(preNatOutAclHeaderSpace);
    BDD postNatOutAclBdd = toBDD.toBDD(postNatOutAclHeaderSpace);
    BDD natMatchBdd = toBDD.toBDD(natMatchHeaderSpace);
    BDD poolIpBdd = srcToBdd.toBDD(poolIp);

    BDD origSrcIpBdd = srcToBdd.toBDD(Ip.parse("6.6.6.6"));
    BDD deniedAfterNat = natMatchBdd.ite(poolIpBdd, origSrcIpBdd).and(postNatOutAclBdd.not());
    BDD deniedBeforeNat = origSrcIpBdd.and(preNatOutAclBdd.not());
    BDD dropAclOut = deniedBeforeNat.or(deniedAfterNat);

    // BDD of packets that exit the interface and reach the disposition state.
    BDD dispositionBdd =
        preNatOutAclBdd.and(natMatchBdd.ite(poolIpBdd, origSrcIpBdd).and(postNatOutAclBdd));

    Map<StateExpr, Map<StateExpr, Transition>> forwardEdges = analysis.getForwardEdgeMap();

    // DeliveredToSubnet
    Transition transition =
        forwardEdges
            .get(new PreOutInterfaceDeliveredToSubnet(hostname, ifaceName))
            .get(new NodeInterfaceDeliveredToSubnet(hostname, ifaceName));
    assertThat(transition.transitForward(origSrcIpBdd), equalTo(dispositionBdd));

    // ExitsNetwork
    transition =
        forwardEdges
            .get(new PreOutInterfaceExitsNetwork(hostname, ifaceName))
            .get(new NodeInterfaceExitsNetwork(hostname, ifaceName));
    assertThat(transition.transitForward(origSrcIpBdd), equalTo(dispositionBdd));

    // InsufficientInfo
    transition =
        forwardEdges
            .get(new PreOutInterfaceInsufficientInfo(hostname, ifaceName))
            .get(new NodeInterfaceInsufficientInfo(hostname, ifaceName));
    assertThat(transition.transitForward(origSrcIpBdd), equalTo(dispositionBdd));

    // NeighborUnreachable
    transition =
        forwardEdges
            .get(new PreOutInterfaceNeighborUnreachable(hostname, ifaceName))
            .get(new NodeInterfaceNeighborUnreachable(hostname, ifaceName));
    assertThat(transition.transitForward(origSrcIpBdd), equalTo(dispositionBdd));

    // DropAclOut via DeliveredToSubnet
    transition =
        forwardEdges
            .get(new PreOutInterfaceDeliveredToSubnet(hostname, ifaceName))
            .get(new NodeDropAclOut(hostname));
    assertEquals(transition.transitForward(origSrcIpBdd), dropAclOut);

    // DropAclOut via ExitsNetwork
    transition =
        forwardEdges
            .get(new PreOutInterfaceExitsNetwork(hostname, ifaceName))
            .get(new NodeDropAclOut(hostname));
    assertEquals(transition.transitForward(origSrcIpBdd), dropAclOut);

    // DropAclOut via InsufficientInfo
    transition =
        forwardEdges
            .get(new PreOutInterfaceInsufficientInfo(hostname, ifaceName))
            .get(new NodeDropAclOut(hostname));
    assertEquals(transition.transitForward(origSrcIpBdd), dropAclOut);

    // DropAclOut via NeighborUnreachable
    transition =
        forwardEdges
            .get(new PreOutInterfaceNeighborUnreachable(hostname, ifaceName))
            .get(new NodeDropAclOut(hostname));
    assertEquals(transition.transitForward(origSrcIpBdd), dropAclOut);
  }

  @Test
  public void testEdgeWithPreNatAcl() throws IOException {
    /*
     * Pre-NAT out ACL permits dest source ports 50 - 150
     * Source NAT will write source IP to 5.5.5.5 if source port is 100
     * Post-NAT out ACL permits dest port 80
     */
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration config = cb.build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Ip poolIp = Ip.parse("5.5.5.5");
    HeaderSpace postNatOutAclHeaderSpace =
        HeaderSpace.builder().setDstPorts(ImmutableList.of(SubRange.singleton(80))).build();
    HeaderSpace natMatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(SubRange.singleton(100))).build();
    HeaderSpace preNatOutAclHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(50, 150))).build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setActive(true)
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/31"))
            .setPreTransformationOutgoingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(preNatOutAclHeaderSpace)))
                    .build())
            .setOutgoingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(postNatOutAclHeaderSpace)))
                    .build())
            .setOutgoingTransformation(
                when(match(natMatchHeaderSpace)).apply(assignSourceIp(poolIp, poolIp)).build())
            .build();
    String ifaceName = iface.getName();
    Prefix staticRoutePrefix = Prefix.parse("3.3.3.3/32");
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopInterface(ifaceName)
                .setAdministrativeCost(1)
                .setNetwork(staticRoutePrefix)
                .build()));

    Configuration peer = cb.build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(peer).build();
    Interface peerIface =
        nf.interfaceBuilder()
            .setOwner(peer)
            .setVrf(vrf2)
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/31"))
            .build();

    String hostname = config.getHostname();
    String peername = peer.getHostname();
    String peerIfaceName = peerIface.getName();

    SortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(hostname, config, peername, peer);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(new InterfaceLocation(hostname, ifaceName), UniverseIpSpace.INSTANCE)
                .build());

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(_pkt, ImmutableMap.of());
    IpSpaceToBDD srcToBdd = toBDD.getSrcIpSpaceToBdd();

    BDD preNatOutAclBdd = toBDD.toBDD(preNatOutAclHeaderSpace);
    BDD natMatchBdd = toBDD.toBDD(natMatchHeaderSpace);
    BDD poolIpBdd = srcToBdd.toBDD(poolIp);
    BDD origSrcIpBdd = srcToBdd.toBDD(Ip.parse("6.6.6.6"));

    Map<StateExpr, Transition> preOutEdgeOutEdges =
        analysis
            .getForwardEdgeMap()
            .get(new PreOutEdge(hostname, ifaceName, peername, peerIfaceName));

    Transition transition =
        preOutEdgeOutEdges.get(new PreOutEdgePostNat(hostname, ifaceName, peername, peerIfaceName));
    assertThat(
        transition.transitForward(origSrcIpBdd),
        equalTo(preNatOutAclBdd.and(natMatchBdd.ite(poolIpBdd, origSrcIpBdd))));

    transition = preOutEdgeOutEdges.get(new NodeDropAclOut(hostname));
    assertThat(transition.transitForward(_one), equalTo(preNatOutAclBdd.not()));
  }

  /*
   *  Test setup: Send packets with destination 8.8.8.0/24 to the next hop 1.0.0.2.
   *  Since the destination Ip 8.8.8.0/24 is outside the network and next hop Ip 1.0.0.2 is not
   *  owned by any device, so traffic of destination 8.8.8.0/24 should have disposition EXITS NETWORK.
   */
  @Test
  public void testInsufficientInfoVsExitsNetwork1() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration c1 = cb.build();

    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();

    // set up interface
    Interface i1 =
        nf.interfaceBuilder()
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/30"))
            .setOwner(c1)
            .setVrf(v1)
            .build();

    // set up static route "8.8.8.0/24" -> 1.0.0.2
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopIp(Ip.parse("1.0.0.2"))
                .setNetwork(Prefix.parse("8.8.8.0/24"))
                .setAdministrativeCost(1)
                .build()));

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(c1.getHostname(), c1);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLocation(c1.getHostname(), i1.getName()), UniverseIpSpace.INSTANCE)
                .build());

    Transition transition =
        analysis
            .getForwardEdgeMap()
            .get(new PreOutVrf(c1.getHostname(), v1.getName()))
            .get(new PreOutInterfaceExitsNetwork(c1.getHostname(), i1.getName()));

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(_pkt, ImmutableMap.of());
    IpSpaceToBDD dstToBdd = toBDD.getDstIpSpaceToBdd();

    BDD resultBDD = dstToBdd.toBDD(Prefix.parse("8.8.8.0/24"));

    assertThat(transition.transitForward(resultBDD), equalTo(resultBDD));

    Transition transitionII =
        analysis
            .getForwardEdgeMap()
            .get(new PreOutVrf(c1.getHostname(), v1.getName()))
            .get(new PreOutInterfaceInsufficientInfo(c1.getHostname(), i1.getName()));

    assertThat(transitionII, nullValue());
  }

  /*
   *  Test setup: Send packets with destination 8.8.8.0/24 to the next hop 2.0.0.2.
   *  Since the destination Ip 8.8.8.0/24 is outside the network and next hop Ip 2.0.0.2 is
   *  owned by a device, so traffic of destination 8.8.8.0/24 should have disposition INSUFFICIENT_INFO.
   */
  @Test
  public void testInsufficientInfoVsExitsNetwork2() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration c1 = cb.build();

    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();

    // set up interface
    Interface i1 =
        nf.interfaceBuilder()
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.1/30"))
            .setOwner(c1)
            .setVrf(v1)
            .build();

    // set up static route "8.8.8.0/24" -> 1.0.0.2
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNextHopIp(Ip.parse("2.0.0.2"))
                .setNetwork(Prefix.parse("8.8.8.0/24"))
                .setNextHopInterface(i1.getName())
                .setAdministrativeCost(1)
                .build()));

    // set up another node
    Configuration c2 = cb.build();

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();
    nf.interfaceBuilder()
        .setAddresses(ConcreteInterfaceAddress.parse("2.0.0.2/31"))
        .setOwner(c2)
        .setVrf(v2)
        .build();

    SortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLocation(c1.getHostname(), i1.getName()), UniverseIpSpace.INSTANCE)
                .build());

    Transition transition =
        analysis
            .getForwardEdgeMap()
            .get(new PreOutVrf(c1.getHostname(), v1.getName()))
            .get(new PreOutInterfaceInsufficientInfo(c1.getHostname(), i1.getName()));

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(_pkt, ImmutableMap.of());
    IpSpaceToBDD dstToBdd = toBDD.getDstIpSpaceToBdd();

    BDD resultBDD = dstToBdd.toBDD(Prefix.parse("8.8.8.0/24"));

    assertThat(transition.transitForward(resultBDD), equalTo(resultBDD));

    Transition transitionEN =
        analysis
            .getForwardEdgeMap()
            .get(new PreOutVrf(c1.getHostname(), v1.getName()))
            .get(new PreOutInterfaceExitsNetwork(c1.getHostname(), i1.getName()));

    assertThat(transitionEN.transitForward(resultBDD), equalTo(_pkt.getFactory().zero()));
  }

  @Test
  public void testFinalHeaderSpaceBdd() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration config = cb.build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Ip srcNatPoolIp = Ip.parse("5.5.5.5");
    Ip dstNatPoolIp = Ip.parse("6.6.6.6");
    nf.interfaceBuilder()
        .setOwner(config)
        .setVrf(vrf)
        .setActive(true)
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/31"))
        .setOutgoingTransformation(
            always().apply(assignSourceIp(srcNatPoolIp, srcNatPoolIp)).build())
        .setIncomingTransformation(
            always().apply(assignDestinationIp(dstNatPoolIp, dstNatPoolIp)).build())
        .build();

    String hostname = config.getHostname();

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(hostname, config);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    BDD one = _pkt.getFactory().one();
    assertThat(factory.computeFinalHeaderSpaceBdd(one), equalTo(one));
    BDD dstIp1 = _pkt.getDstIp().value(1);
    BDD dstNatPoolIpBdd = _pkt.getDstIp().value(dstNatPoolIp.asLong());
    BDD srcIp1 = _pkt.getSrcIp().value(1);
    BDD srcNatPoolIpBdd = _pkt.getSrcIp().value(srcNatPoolIp.asLong());
    assertThat(factory.computeFinalHeaderSpaceBdd(dstIp1), equalTo(dstIp1.or(dstNatPoolIpBdd)));
    assertThat(factory.computeFinalHeaderSpaceBdd(srcIp1), equalTo(srcIp1.or(srcNatPoolIpBdd)));
    assertThat(
        factory.computeFinalHeaderSpaceBdd(dstIp1.and(srcIp1)),
        equalTo(
            dstIp1
                .and(srcIp1)
                .or(dstIp1.and(srcNatPoolIpBdd))
                .or(dstNatPoolIpBdd.and(srcIp1))
                .or(dstNatPoolIpBdd.and(srcNatPoolIpBdd))));
  }

  @Test
  public void testFinalHeaderSpaceBddForPorts() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration config = cb.build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    nf.interfaceBuilder()
        .setOwner(config)
        .setVrf(vrf)
        .setActive(true)
        .setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/31"))
        .setOutgoingTransformation(always().apply(assignSourcePort(10000, 10000)).build())
        .setIncomingTransformation(always().apply(assignDestinationPort(30000, 30000)).build())
        .build();

    String hostname = config.getHostname();

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(hostname, config);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    BDD one = _pkt.getFactory().one();
    assertThat(factory.computeFinalHeaderSpaceBdd(one), equalTo(one));
    BDD dstPort1 = _pkt.getDstPort().value(3000);
    BDD dstNatPoolBdd = _pkt.getDstPort().value(30000);
    BDD srcPort1 = _pkt.getSrcPort().value(1000);
    BDD srcNatPoolBdd = _pkt.getSrcPort().value(10000);
    assertThat(factory.computeFinalHeaderSpaceBdd(dstPort1), equalTo(dstPort1.or(dstNatPoolBdd)));
    assertThat(factory.computeFinalHeaderSpaceBdd(srcPort1), equalTo(srcPort1.or(srcNatPoolBdd)));
    assertThat(
        factory.computeFinalHeaderSpaceBdd(dstPort1.and(srcPort1)),
        equalTo(
            dstPort1
                .and(srcPort1)
                .or(dstPort1.and(srcNatPoolBdd))
                .or(dstNatPoolBdd.and(srcPort1))
                .or(dstNatPoolBdd.and(srcNatPoolBdd))));
  }

  @Test
  public void testTransformationGuardWithIpSpaceReference() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration config = cb.build();

    // add a named IpSpace
    String ipSpaceName = "ip space";
    config.getIpSpaces().put(ipSpaceName, UniverseIpSpace.INSTANCE);

    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Ip srcNatPoolIp = Ip.parse("5.5.5.5");
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setActive(true)
            .setAddress(ConcreteInterfaceAddress.parse("1.0.0.0/31"))
            .setOutgoingTransformation(
                when(matchDst(new IpSpaceReference(ipSpaceName)))
                    .apply(assignSourceIp(srcNatPoolIp, srcNatPoolIp))
                    .build())
            // only allow NATed flows
            .setOutgoingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(
                        ImmutableList.of(
                            accepting().setMatchCondition(matchSrc(srcNatPoolIp)).build()))
                    .build())
            .build();

    String hostname = config.getHostname();

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(hostname, config);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    Ip dstIp = iface.getConcreteAddress().getPrefix().getLastHostIp();
    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(new InterfaceLocation(hostname, iface.getName()), UniverseIpSpace.INSTANCE)
                .build(),
            matchDst(dstIp),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(hostname),
            ImmutableSet.of(DELIVERED_TO_SUBNET));

    Map<IngressLocation, BDD> bdds = analysis.getIngressLocationReachableBDDs();

    assertEquals(
        bdds,
        ImmutableMap.of(
            IngressLocation.vrf(hostname, vrf.getName()), _pkt.getDstIp().value(dstIp.asLong())));
  }

  /**
   * Constructs a network with configs c1 and (if {@code withNeighbor}) c2.
   *
   * <p>c1 has VRFs vrf1 and vrf2, and interfaces INGRESS_IFACE and i1 in vrf1. INGRESS_IFACE has a
   * packet policy that does a FIB lookup in vrf2 for flows with dst IPs in 8.8.8.0/24 and otherwise
   * drops. vrf2 has a static route for 8.8.8.0/24 that sends traffic out i1.
   *
   * <p>If included, c2 has interface i1 on the same subnet as c1[i1].
   */
  private ImmutableSortedMap<String, Configuration> makePBRNetwork(boolean withNeighbor) {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration config = cb.setHostname("c1").build();

    Vrf vrf = nf.vrfBuilder().setName("vrf1").setOwner(config).build();
    Vrf vrf2 = nf.vrfBuilder().setName("vrf2").setOwner(config).build();

    // Create a packet policy that does a fib lookup in vrf2 for 8.8.8.0/24
    final String packetPolicyName = "packetPolicyName";
    Prefix dstPrefix = Prefix.parse("8.8.8.0/24");
    config.setPacketPolicies(
        ImmutableSortedMap.of(
            packetPolicyName,
            new PacketPolicy(
                packetPolicyName,
                ImmutableList.of(
                    new If(
                        new PacketMatchExpr(matchDst(dstPrefix)),
                        ImmutableList.of(
                            new Return(new FibLookup(new LiteralVrfName(vrf2.getName())))))),
                new Return(Drop.instance()))));

    Interface.Builder ib = nf.interfaceBuilder().setOwner(config).setVrf(vrf).setActive(true);
    Interface ingressIface =
        ib.setName(INGRESS_IFACE).setAddress(ConcreteInterfaceAddress.parse("1.1.1.0/24")).build();
    ingressIface.setRoutingPolicy(packetPolicyName);
    Interface i1 =
        ib.setName("i1").setAddress(ConcreteInterfaceAddress.parse("2.2.2.0/24")).build();

    StaticRoute sb =
        StaticRoute.testBuilder()
            .setAdministrativeCost(1)
            .setNetwork(dstPrefix)
            .setNextHopInterface(i1.getName())
            .build();
    vrf2.setStaticRoutes(ImmutableSortedSet.of(sb));

    if (!withNeighbor) {
      return ImmutableSortedMap.of(config.getHostname(), config);
    }

    // Add a second config which accepts 8.8.8.8 and is connected to C1
    Configuration c2 = cb.setHostname("c2").build();
    Vrf c2vrf = nf.vrfBuilder().setName("c2vrf").setOwner(c2).build();
    ib.setOwner(c2)
        .setVrf(c2vrf)
        .setName("i1")
        .setAddress(ConcreteInterfaceAddress.parse("2.2.2.2/24"))
        .build();
    ib.setName("loopback").setAddress(ConcreteInterfaceAddress.parse("8.8.8.8/32")).build();

    return ImmutableSortedMap.of(config.getHostname(), config, c2.getHostname(), c2);
  }

  /**
   * Test with a simple network where a lookup is done using policy-based routing in a different VRF
   */
  @Test
  public void testPBRCrossVrfLookupExitsNetwork() throws IOException {

    // no neighbor, expect exits network
    ImmutableSortedMap<String, Configuration> configs = makePBRNetwork(false);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    String hostname = "c1";

    Ip dstIp = Ip.parse("8.8.8.8");
    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLinkLocation(hostname, INGRESS_IFACE), UniverseIpSpace.INSTANCE)
                .build(),
            matchDst(dstIp),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(hostname),
            ImmutableSet.of(EXITS_NETWORK));

    // Check state edge presence (note, INGRESS_IFACE is in vrf1, not INGRESS_VRF)
    PbrFibLookup pbrFibLookup = new PbrFibLookup(hostname, "vrf1", "vrf2");
    PreOutVrf preOutVrf2 = new PreOutVrf(hostname, "vrf2");
    assertThat(
        analysis.getForwardEdgeMap(),
        hasEntry(
            equalTo(new PreInInterface(hostname, INGRESS_IFACE)), hasKey(equalTo(pbrFibLookup))));
    assertThat(
        analysis.getForwardEdgeMap(), hasEntry(equalTo(pbrFibLookup), hasKey(equalTo(preOutVrf2))));
    assertThat(
        analysis.getForwardEdgeMap(),
        hasEntry(
            equalTo(preOutVrf2), hasKey(equalTo(new PreOutInterfaceExitsNetwork(hostname, "i1")))));

    // End-to-end reachability based on reachable ingress locations
    Map<IngressLocation, BDD> bdds = analysis.getIngressLocationReachableBDDs();
    assertEquals(
        bdds,
        ImmutableMap.of(
            IngressLocation.interfaceLink(hostname, INGRESS_IFACE),
            _pkt.getDstIp().value(dstIp.asLong())));
  }

  /**
   * Test with a simple network where a lookup is done using policy-based routing in a different VRF
   */
  @Test
  public void testPBRCrossVrfLookupExitsEdge() throws IOException {

    // with neighbor, expect accepted disposition
    ImmutableSortedMap<String, Configuration> configs = makePBRNetwork(true);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    String hostname = "c1";
    String neighborHostname = "c2";
    String neighborIface = "i1";

    Ip dstIp = Ip.parse("8.8.8.8");
    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLinkLocation(hostname, INGRESS_IFACE), UniverseIpSpace.INSTANCE)
                .build(),
            matchDst(dstIp),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(neighborHostname),
            ImmutableSet.of(ACCEPTED));

    // Check state edge presence (note, INGRESS_IFACE is in vrf1, not INGRESS_VRF)
    PbrFibLookup pbrFibLookup = new PbrFibLookup(hostname, "vrf1", "vrf2");
    PreOutVrf preOutVrf2 = new PreOutVrf(hostname, "vrf2");
    NodeDropNoRoute nodeDropNoRoute = new NodeDropNoRoute(hostname);
    IpSpaceToBDD ipSpaceToBDD = new IpSpaceToBDD(_pkt.getDstIp());
    BDD routableFromLookupVrf = ipSpaceToBDD.toBDD(Prefix.parse("8.8.8.0/24"));
    BDD acceptedInIngressVrf =
        // These are the concrete addresses of the two interfaces in the ingress vrf
        ipSpaceToBDD.toBDD(Ip.parse("1.1.1.0")).or(ipSpaceToBDD.toBDD(Ip.parse("2.2.2.0")));
    assertThat(
        analysis.getForwardEdgeMap(),
        hasEntry(
            equalTo(new PreInInterface(hostname, INGRESS_IFACE)), hasKey(equalTo(pbrFibLookup))));
    assertThat(
        analysis.getForwardEdgeMap(),
        hasEntry(
            equalTo(pbrFibLookup),
            // edge to preOutVrf2 should be limited to traffic routable in vrf2
            hasEntry(equalTo(preOutVrf2), mapsForward(_one, routableFromLookupVrf))));
    assertThat(
        analysis.getForwardEdgeMap(),
        hasEntry(
            equalTo(pbrFibLookup),
            hasEntry(
                equalTo(nodeDropNoRoute),
                // edge to nodeDropNoRoute should have traffic not accepted in vrf1 and not routable
                // in vrf2
                mapsForward(_one, routableFromLookupVrf.nor(acceptedInIngressVrf)))));
    assertThat(
        analysis.getForwardEdgeMap(),
        hasEntry(
            equalTo(preOutVrf2),
            hasKey(equalTo(new PreOutEdge(hostname, "i1", neighborHostname, neighborIface)))));
  }

  private ImmutableSortedMap<String, Configuration> makeNextVrfNetwork(boolean withNeighbor) {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration ingressNode = cb.setHostname(INGRESS_NODE).build();

    Vrf ingressVrf = nf.vrfBuilder().setName(INGRESS_VRF).setOwner(ingressNode).build();
    Vrf nextVrf = nf.vrfBuilder().setName(NEXT_VRF).setOwner(ingressNode).build();
    StaticRoute ingressVrfNextVrfRoute =
        StaticRoute.testBuilder()
            .setAdministrativeCost(1)
            .setNetwork(Prefix.ZERO)
            .setNextHop(NextHopVrf.of(NEXT_VRF))
            .build();
    ingressVrf.setStaticRoutes(ImmutableSortedSet.of(ingressVrfNextVrfRoute));

    Interface.Builder ib = nf.interfaceBuilder().setOwner(ingressNode).setActive(true);
    ib.setName(INGRESS_IFACE)
        .setVrf(ingressVrf)
        .setAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"))
        .build();
    ib.setName("egressIface")
        .setVrf(nextVrf)
        .setAddress(ConcreteInterfaceAddress.parse("10.0.12.1/24"))
        .build();

    if (!withNeighbor) {
      return ImmutableSortedMap.of(ingressNode.getHostname(), ingressNode);
    }

    // Add a second config which 10.0.12.2 and is connected to C1
    Configuration neighbor = cb.setHostname("neighbor").build();
    Vrf neighborVrf = nf.vrfBuilder().setName("neighbor").setOwner(neighbor).build();
    ib.setOwner(neighbor)
        .setVrf(neighborVrf)
        .setName("neighborIface")
        .setAddress(ConcreteInterfaceAddress.parse("10.0.12.2/24"))
        .build();

    return ImmutableSortedMap.of(
        ingressNode.getHostname(), ingressNode, neighbor.getHostname(), neighbor);
  }

  @Test
  public void testNextVrfWithNeighbor() throws IOException {
    // with neighbor, expect accepted disposition
    ImmutableSortedMap<String, Configuration> configs = makeNextVrfNetwork(true);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    String neighborHostname = "neighbor";

    Ip dstIp = Ip.parse("10.0.12.2");
    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLinkLocation(INGRESS_NODE, INGRESS_IFACE),
                    UniverseIpSpace.INSTANCE)
                .build(),
            matchDst(dstIp),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(neighborHostname),
            ImmutableSet.of(ACCEPTED));

    // Check state edge presence
    assertThat(
        analysis.getForwardEdgeMap(),
        hasEntry(
            equalTo(new PostInVrf(INGRESS_NODE, INGRESS_VRF)),
            hasKey(new PostInVrf(INGRESS_NODE, NEXT_VRF))));

    BDD nextVrfDstIpsBDD =
        analysis
            .getForwardEdgeMap()
            .get(new PostInVrf(INGRESS_NODE, INGRESS_VRF))
            .get(new PostInVrf(INGRESS_NODE, NEXT_VRF))
            .transitForward(_one);
    IpSpaceToBDD ipSpaceToBDD = new IpSpaceToBDD(_pkt.getDstIp());

    // ingressVrf should delegate space associated with nextVrfRoute 0.0.0.0/0 minus more specific
    // space associated with connected route 10.0.0.0/24
    assertThat(
        nextVrfDstIpsBDD,
        equalTo(_one.diff(Prefix.parse("10.0.0.0/24").toIpSpace().accept(ipSpaceToBDD))));

    BDD acceptedEndToEndBDD =
        analysis
            .getIngressLocationReachableBDDs()
            .get(IngressLocation.interfaceLink(INGRESS_NODE, INGRESS_IFACE));

    // Any packet with destination IP 10.0.12.2 (that of neighbor interface) entering ingressIface
    // should be delivered and accepted
    assertThat(
        acceptedEndToEndBDD, equalTo(Ip.parse("10.0.12.2").toIpSpace().accept(ipSpaceToBDD)));
  }

  @Test
  public void testNextVrfWithoutNeighbor() throws IOException {
    // with neighbor, expect accepted disposition
    ImmutableSortedMap<String, Configuration> configs = makeNextVrfNetwork(false);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    IpSpace dstIpSpaceOfInterest =
        AclIpSpace.difference(
            Prefix.strict("10.0.12.0/24").toHostIpSpace(), Ip.parse("10.0.12.1").toIpSpace());
    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLinkLocation(INGRESS_NODE, INGRESS_IFACE),
                    UniverseIpSpace.INSTANCE)
                .build(),
            matchDst(dstIpSpaceOfInterest),
            ImmutableSet.of(),
            ImmutableSet.of(),
            configs.keySet(),
            ImmutableSet.of(DELIVERED_TO_SUBNET));

    // Check state edge presence
    assertThat(
        analysis.getForwardEdgeMap(),
        hasEntry(
            equalTo(new PostInVrf(INGRESS_NODE, INGRESS_VRF)),
            hasKey(new PostInVrf(INGRESS_NODE, NEXT_VRF))));

    BDD nextVrfDstIpsBDD =
        analysis
            .getForwardEdgeMap()
            .get(new PostInVrf(INGRESS_NODE, INGRESS_VRF))
            .get(new PostInVrf(INGRESS_NODE, NEXT_VRF))
            .transitForward(_one);
    IpSpaceToBDD ipSpaceToBDD = new IpSpaceToBDD(_pkt.getDstIp());

    // ingressVrf should delegate space associated with nextVrfRoute 0.0.0.0/0 minus more specific
    // space associated with connected route 10.0.0.0/24
    assertThat(
        nextVrfDstIpsBDD,
        equalTo(_one.diff(Prefix.parse("10.0.0.0/24").toIpSpace().accept(ipSpaceToBDD))));

    BDD deliveredToSubnetEndToEndBDD =
        analysis
            .getIngressLocationReachableBDDs()
            .get(IngressLocation.interfaceLink(INGRESS_NODE, INGRESS_IFACE));

    // Any packet with destination IP 10.0.12.0/24 \ 10.0.12.1 (egressInterface network minus its
    // IP) entering ingressIface should be delivered to subnet of egressInterface
    // TODO: Specify final interface where DELIVERED_TO_SUBNET occurs when that becomes possible
    assertThat(deliveredToSubnetEndToEndBDD, equalTo(dstIpSpaceOfInterest.accept(ipSpaceToBDD)));
  }

  /**
   * Create a network that uses {@link FibLookupOutgoingInterfaceIsOneOf} to forward traffic that
   * will be forwarded out interface i1, and drop otherwise.
   */
  private ImmutableSortedMap<String, Configuration> makeOutgoingInterfaceIsOneOfNetwork() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration config = cb.setHostname(INGRESS_NODE).build();

    Vrf vrf = nf.vrfBuilder().setName(INGRESS_VRF).setOwner(config).build();

    final String packetPolicyName = "packetPolicyName";
    config.setPacketPolicies(
        ImmutableSortedMap.of(
            packetPolicyName,
            new PacketPolicy(
                packetPolicyName,
                ImmutableList.of(
                    new If(
                        new FibLookupOutgoingInterfaceIsOneOf(
                            new LiteralVrfName(vrf.getName()), ImmutableSet.of("i1")),
                        ImmutableList.of(
                            new Return(new FibLookup(new LiteralVrfName(vrf.getName())))))),
                new Return(Drop.instance()))));

    Interface.Builder ib = nf.interfaceBuilder().setOwner(config).setVrf(vrf).setActive(true);
    Interface ingressIface =
        ib.setName(INGRESS_IFACE).setAddress(ConcreteInterfaceAddress.parse("10.0.0.0/24")).build();
    ingressIface.setRoutingPolicy(packetPolicyName);
    ib.setName("i1").setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24")).build();
    ib.setName("i2").setAddress(ConcreteInterfaceAddress.parse("2.2.2.1/24")).build();

    return ImmutableSortedMap.of(config.getHostname(), config);
  }

  @Test
  public void testOutgoingInterfaceIsOneOf() throws IOException {
    ImmutableSortedMap<String, Configuration> configs = makeOutgoingInterfaceIsOneOfNetwork();
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLinkLocation(INGRESS_NODE, INGRESS_IFACE),
                    UniverseIpSpace.INSTANCE)
                .build(),
            TRUE,
            ImmutableSet.of(),
            ImmutableSet.of(),
            configs.keySet(),
            SUCCESS_DISPOSITIONS);

    BDD deliveredToSubnetEndToEndBDD =
        analysis
            .getIngressLocationReachableBDDs()
            .get(IngressLocation.interfaceLink(INGRESS_NODE, INGRESS_IFACE));

    // policy allows traffic to i1's subnet
    BDD i1SubnetBdd = _pkt.getDstIpSpaceToBDD().toBDD(Ip.parse("1.1.1.2"));
    assertTrue(deliveredToSubnetEndToEndBDD.andSat(i1SubnetBdd));

    // policy allows traffic to i1's ip
    BDD i1Ip = _pkt.getDstIpSpaceToBDD().toBDD(Ip.parse("1.1.1.1"));
    assertTrue(deliveredToSubnetEndToEndBDD.andSat(i1Ip));

    // policy drops traffic to i2
    BDD i2SubnetBdd = _pkt.getDstIpSpaceToBDD().toBDD(Ip.parse("2.2.2.2"));
    assertFalse(deliveredToSubnetEndToEndBDD.andSat(i2SubnetBdd));
  }

  @Test
  public void testGenerateRootEdges_OriginateInterface_PostInVrf() throws IOException {
    // Create network with two interfaces
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(c).build();
    Interface.Builder ib = nf.interfaceBuilder().setOwner(c).setVrf(vrf).setActive(true);
    Interface iface1 = ib.setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24")).build();
    ib.setAddress(ConcreteInterfaceAddress.parse("2.2.2.2/24")).build();

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(c.getHostname(), c);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    // Generate edges for originating at first interface, with an arbitrary dst IP constraint
    OriginateInterface originateIface1 = new OriginateInterface(c.getHostname(), iface1.getName());
    BDD rootBdd = _pkt.getDstIp().value(Ip.parse("3.3.3.3").asLong());
    List<Edge> rootEdges =
        factory
            .generateRootEdges_OriginateInterface_PostInVrf(
                ImmutableMap.of(originateIface1, rootBdd))
            .collect(ImmutableList.toImmutableList());

    // Generated edges should constrain to flows originating from device with the given dst IP.
    // Should not include any edges starting from second interface since it wasn't a root state.
    assertThat(
        rootEdges,
        contains(
            new Edge(
                originateIface1,
                new PostInVrf(c.getHostname(), vrf.getName()),
                compose(
                    addOriginatingFromDeviceConstraint(
                        factory.getBDDSourceManagers().get(c.getHostname())),
                    constraint(rootBdd)))));
  }

  /**
   * Constructs a network with configs c1 and (optionally) c2. c1 has an interface i1 with every
   * type of outgoing filter:
   *
   * <ul>
   *   <li>{@link Interface#getOutgoingOriginalFlowFilter() outgoingOriginalFlowFilter} permits IPs
   *       1.0.0.0, 2.0.0.0, and 3.0.0.0
   *   <li>{@link Interface#getPreTransformationOutgoingFilter() preTransformationOutgoingFilter}
   *       permits IPs 1.0.0.0, 2.0.0.0, and 4.0.0.0
   *   <li>{@link Interface#getOutgoingFilter() outgoingFilter} permits IPs 1.0.0.0, 3.0.0.0, and
   *       4.0.0.0
   * </ul>
   *
   * <p>c1 can optionally have another interface i2 with no filters.
   *
   * <p>c2 (if included) has an interface i1 with no filters, connected to c1[i1]. This is necessary
   * if you want a topology edge.
   */
  private static SortedMap<String, Configuration> makeOutgoingFiltersNetwork(
      boolean includeC1I2, boolean includeC2) {
    AclLine acceptSrc1 = accepting(matchSrc(Ip.parse("1.0.0.0")));
    AclLine acceptSrc2 = accepting(matchSrc(Ip.parse("2.0.0.0")));
    AclLine acceptSrc3 = accepting(matchSrc(Ip.parse("3.0.0.0")));
    AclLine acceptSrc4 = accepting(matchSrc(Ip.parse("4.0.0.0")));

    // Create c1 and its filters
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.setHostname("c1").build();
    IpAccessList.Builder ab = nf.aclBuilder().setOwner(c1);
    IpAccessList originalFlowFilter =
        ab.setLines(acceptSrc1, acceptSrc2, acceptSrc3, REJECT_ALL).build();
    IpAccessList preTransformFilter =
        ab.setLines(acceptSrc1, acceptSrc2, acceptSrc4, REJECT_ALL).build();
    IpAccessList outgoingFilter =
        ab.setLines(acceptSrc1, acceptSrc3, acceptSrc4, REJECT_ALL).build();

    // Create i1 on c1 with the appropriate filters
    Vrf vrf = nf.vrfBuilder().setOwner(c1).build();
    nf.interfaceBuilder()
        .setName("i1")
        .setOwner(c1)
        .setVrf(vrf)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24"))
        .setOutgoingOriginalFlowFilter(originalFlowFilter)
        .setPreTransformationOutgoingFilter(preTransformFilter)
        .setOutgoingFilter(outgoingFilter)
        .build();

    if (includeC1I2) {
      // Create i2 on c1 with no filters
      nf.interfaceBuilder()
          .setName("i2")
          .setOwner(c1)
          .setVrf(vrf)
          .setAddress(ConcreteInterfaceAddress.parse("2.2.2.2/24"))
          .build();
    }

    if (!includeC2) {
      return ImmutableSortedMap.of(c1.getHostname(), c1);
    }

    // Create c2 and i2
    Configuration c2 = cb.setHostname("c2").build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(c2).build();
    nf.interfaceBuilder()
        .setName("i1")
        .setOwner(c2)
        .setVrf(vrf2)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.2/24"))
        .build();

    return ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);
  }

  /**
   * For {@link BDDReachabilityAnalysisFactory#generateRules_PreOutEdgePostNat_NodeDropAclOut}, test
   * that the outgoing original flow filter is still applied if outgoing filter is null.
   */
  @Test
  public void testOutgoingOriginalFlowFilterWithoutOutgoingFilter_arpSuccess() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    // c1
    Configuration c1 = cb.setHostname("c1").build();
    Vrf v1 = nf.vrfBuilder().setOwner(c1).build();

    IpAccessList originalFlowFilter =
        nf.aclBuilder()
            .setLines(ExprAclLine.accepting(AclLineMatchExprs.matchDst(Ip.parse("1.1.1.1"))))
            .build();

    Interface i1 =
        nf.interfaceBuilder()
            .setName("i1")
            .setOwner(c1)
            .setVrf(v1)
            .setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24"))
            .setOutgoingOriginalFlowFilter(originalFlowFilter)
            .build();

    // c2
    Configuration c2 = cb.setHostname("c2").build();
    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();
    nf.interfaceBuilder()
        .setName("i2")
        .setOwner(c2)
        .setVrf(v2)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.2/24"))
        .build();

    BDDReachabilityAnalysisFactory factory =
        makeBddReachabilityAnalysisFactory(
            ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2));

    BDD permittedBdd =
        factory
            .getBddOutgoingOriginalFlowFilterManagers()
            .get(c1.getHostname())
            .permittedByOriginalFlowEgressFilter(i1.getName());
    BDD deniedBdd =
        factory
            .getBddOutgoingOriginalFlowFilterManagers()
            .get(c1.getHostname())
            .deniedByOriginalFlowEgressFilter(i1.getName());

    Edge edge =
        Iterables.getOnlyElement(
            factory
                .generateRules_PreOutEdgePostNat_NodeDropAclOut()
                .filter(
                    e -> ((NodeDropAclOut) e.getPostState()).getHostname().equals(c1.getHostname()))
                .collect(Collectors.toList()));
    assertThat(
        edge,
        hasTransition(
            allOf(
                // every flow marked as denied can traverse this edge (and the marking is removed)
                mapsForward(deniedBdd, _one),
                // no flow marked as permitted can traverse this edge
                mapsForward(permittedBdd, _zero))));
  }

  /**
   * For {@link
   * BDDReachabilityAnalysisFactory#generateRules_PreOutInterfaceDisposition_NodeDropAclOut}, test
   * that the outgoing original flow filter is still applied if outgoing filter is null.
   */
  @Test
  public void testOutgoingOriginalFlowFilterWithoutOutgoingFilter_arpFailure() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.setHostname("c1").build();
    // Create i1 on c1 with the appropriate filters
    Vrf vrf = nf.vrfBuilder().setOwner(c1).build();

    IpAccessList originalFlowFilter =
        nf.aclBuilder()
            .setLines(ExprAclLine.accepting(AclLineMatchExprs.matchDst(Ip.parse("1.1.1.1"))))
            .build();

    Interface i1 =
        nf.interfaceBuilder()
            .setName("i1")
            .setOwner(c1)
            .setVrf(vrf)
            .setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24"))
            .setOutgoingOriginalFlowFilter(originalFlowFilter)
            .build();

    BDDReachabilityAnalysisFactory factory =
        makeBddReachabilityAnalysisFactory(ImmutableSortedMap.of(c1.getHostname(), c1));

    BDD permittedBdd =
        factory
            .getBddOutgoingOriginalFlowFilterManagers()
            .get(c1.getHostname())
            .permittedByOriginalFlowEgressFilter(i1.getName());
    BDD deniedBdd =
        factory
            .getBddOutgoingOriginalFlowFilterManagers()
            .get(c1.getHostname())
            .deniedByOriginalFlowEgressFilter(i1.getName());

    List<Edge> edges =
        factory
            .generateRules_PreOutInterfaceDisposition_NodeDropAclOut()
            .collect(Collectors.toList());
    assertFalse("edges must not be empty", edges.isEmpty());
    assertThat(
        edges,
        everyItem(
            hasTransition(
                allOf(
                    // every flow marked as denied can traverse this edge (and the marking is
                    // removed)
                    mapsForward(deniedBdd, _one),
                    // no flow marked as permitted can traverse this edge
                    mapsForward(permittedBdd, _zero)))));
  }

  /** Test that PBR takes precedence over incoming filter when both are present on an interface. */
  @Test
  public void testIncomingFilterVsPBR() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration n1 =
        Configuration.builder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("n1")
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(n1).build();
    PacketPolicy pbr = new PacketPolicy("pbr", ImmutableList.of(), new Return(Drop.instance()));
    n1.setPacketPolicies(ImmutableMap.of(pbr.getName(), pbr));
    Interface i1 =
        nf.interfaceBuilder()
            .setVrf(vrf)
            .setOwner(n1)
            .setActive(true)
            .setAddress(ConcreteInterfaceAddress.create(Ip.parse("1.1.1.1"), 24))
            .setIncomingFilter(nf.aclBuilder().setOwner(n1).setLines(ACCEPT_ALL).build())
            .setRoutingPolicy(pbr.getName())
            .build();
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(n1.getHostname(), n1);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);
    Edge edge =
        Iterables.getOnlyElement(
            factory.generateRules_PreInInterface_NodeDropAclIn().collect(Collectors.toList()));
    assertThat(
        edge,
        edge(
            new PreInInterface(n1.getHostname(), i1.getName()),
            new NodeDropAclIn(n1.getHostname()),
            equalTo(IDENTITY)));
  }

  @Test
  public void testOutgoingFilters() throws IOException {
    /*
    Test that flow is denied unless it matches all outgoing filters.
    - Src IP 1.0.0.0 permitted by all filters (original flow, pre-transformation, and outgoing)
    - Src IP 2.0.0.0 permitted by original flow and pre-transformation filters
    - Src IP 3.0.0.0 permitted by original flow and outgoing filters
    - Src IP 4.0.0.0 permitted by pre-transformation and outgoing filters
     */
    Ip srcIp1 = Ip.parse("1.0.0.0");
    String c1 = "c1";
    String i1 = "i1";
    SortedMap<String, Configuration> configs = makeOutgoingFiltersNetwork(false, false);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    StateExpr deliveredToSubnet = new PreOutInterfaceDeliveredToSubnet(c1, i1);
    StateExpr exitsNetwork = new PreOutInterfaceExitsNetwork(c1, i1);
    StateExpr neighborUnreachable = new PreOutInterfaceNeighborUnreachable(c1, i1);
    StateExpr insufficientInfo = new PreOutInterfaceInsufficientInfo(c1, i1);

    // Flow needs to start with the expected constraint for outgoing original flow filters
    BDD originalFlowFiltersConstraint =
        factory
            .getBddOutgoingOriginalFlowFilterManagers()
            .get(c1)
            .outgoingOriginalFlowFiltersConstraint();
    BDD src1Bdd = _pkt.getSrcIp().value(srcIp1.asLong());

    // Test permit edges, to NodeInterface[Disposition] states
    StateExpr nodeDeliveredToSubnet = new NodeInterfaceDeliveredToSubnet(c1, i1);
    StateExpr nodeExitsNetwork = new NodeInterfaceExitsNetwork(c1, i1);
    StateExpr nodeNeighborUnreachable = new NodeInterfaceNeighborUnreachable(c1, i1);
    StateExpr nodeInsufficientInfo = new NodeInterfaceInsufficientInfo(c1, i1);
    List<Edge> permitEdges =
        factory
            .generateRules_PreOutInterfaceDisposition_NodeInterfaceDisposition()
            .collect(ImmutableList.toImmutableList());

    // Node-specific constraints cleared later in generateRules_NodeInterfaceDisposition_Disposition
    Matcher<Transition> expectedTransition =
        mapsForward(originalFlowFiltersConstraint, originalFlowFiltersConstraint.and(src1Bdd));
    assertThat(
        permitEdges,
        containsInAnyOrder(
            edge(deliveredToSubnet, nodeDeliveredToSubnet, expectedTransition),
            edge(exitsNetwork, nodeExitsNetwork, expectedTransition),
            edge(neighborUnreachable, nodeNeighborUnreachable, expectedTransition),
            edge(insufficientInfo, nodeInsufficientInfo, expectedTransition)));

    // Test deny edges, to NodeDropAclOut state
    StateExpr nodeDropAclOut = new NodeDropAclOut(c1);
    List<Edge> denyEdges =
        factory
            .generateRules_PreOutInterfaceDisposition_NodeDropAclOut()
            .collect(ImmutableList.toImmutableList());
    Matcher<Transition> expectedDenyTransition =
        mapsForward(originalFlowFiltersConstraint, src1Bdd.not());
    assertThat(
        denyEdges,
        containsInAnyOrder(
            edge(deliveredToSubnet, nodeDropAclOut, expectedDenyTransition),
            edge(exitsNetwork, nodeDropAclOut, expectedDenyTransition),
            edge(neighborUnreachable, nodeDropAclOut, expectedDenyTransition),
            edge(insufficientInfo, nodeDropAclOut, expectedDenyTransition)));
  }

  @Test
  public void testOutgoingFilters_PreOutEdgePostNat() throws IOException {
    /*
    Test that NAT flow is denied unless it matches original flow filter and outgoing filter (the
    pre-transformation filter should not touch NAT flows). 1.0.0.0 and 3.0.0.0 should be allowed.
    - Src IP 1.0.0.0 permitted by original flow, pre-transformation, and outgoing filters
    - Src IP 2.0.0.0 permitted by original flow and pre-transformation filters
    - Src IP 3.0.0.0 permitted by original flow and outgoing filters
    - Src IP 4.0.0.0 permitted by pre-transformation and outgoing filters
     */
    Ip srcIp1 = Ip.parse("1.0.0.0");
    Ip srcIp3 = Ip.parse("3.0.0.0");
    String c1 = "c1";
    String c2 = "c2";
    String i1 = "i1";
    SortedMap<String, Configuration> configs = makeOutgoingFiltersNetwork(false, true);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);

    StateExpr preOutEdgePostNat = new PreOutEdgePostNat(c1, i1, c2, i1);

    // Flow needs to start with the expected constraint for outgoing original flow filters
    BDD originalFlowFiltersConstraint =
        factory
            .getBddOutgoingOriginalFlowFilterManagers()
            .get(c1)
            .outgoingOriginalFlowFiltersConstraint();
    BDD src1And3Bdd =
        _pkt.getSrcIp().value(srcIp1.asLong()).or(_pkt.getSrcIp().value(srcIp3.asLong()));

    // Test permit edge to PreInInterface states
    StateExpr preInInterface = new PreInInterface(c2, i1);
    List<Edge> permitEdges =
        factory
            .generateRules_PreOutEdgePostNat_PreInInterface()
            .collect(ImmutableList.toImmutableList());
    assertThat(
        permitEdges,
        containsInAnyOrder(
            edge(
                preOutEdgePostNat,
                preInInterface,
                mapsForward(originalFlowFiltersConstraint, src1And3Bdd)),
            // should be a second edge: PreOutEdgePostNat -> PreInInterface in opposite direction
            edge(
                new PreOutEdgePostNat(c2, i1, c1, i1),
                new PreInInterface(c1, i1),
                mapsForward(_one, _one))));

    // Test deny edge to NodeDropAclOut state
    StateExpr nodeDropAclOut = new NodeDropAclOut(c1);
    List<Edge> denyEdges =
        factory
            .generateRules_PreOutEdgePostNat_NodeDropAclOut()
            .collect(ImmutableList.toImmutableList());
    assertThat(
        denyEdges,
        contains(
            edge(
                preOutEdgePostNat,
                nodeDropAclOut,
                mapsForward(originalFlowFiltersConstraint, src1And3Bdd.not()))));
  }

  @Test
  public void testOutgoingOriginalFlowFilterManagersTrivialIfIgnoreFiltersOn() throws IOException {
    /* When ignoreFilters is on, we artificially make all original flow filter managers trivial. */
    SortedMap<String, Configuration> configs = makeOutgoingFiltersNetwork(false, false);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs, true);
    assertTrue(factory.getBddOutgoingOriginalFlowFilterManagers().get("c1").isTrivial());
  }

  @Test
  public void testAddOutgoingOriginalFlowFiltersConstraint_PostInInterface() throws IOException {
    /*
    Test that the correct outgoingOriginalFlowFiltersConstraint is placed on flows entering a node
    with an interface with an outgoingOriginalFlowFilter. In this case, c1 has interfaces i1 and i2,
    and i1 has an outgoingOriginalFlowFilter.
     */
    String c1 = "c1";
    String i1 = "i1";
    String i2 = "i2";
    SortedMap<String, Configuration> configs = makeOutgoingFiltersNetwork(true, false);
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);
    BDDOutgoingOriginalFlowFilterManager originalFlowFilterMgr =
        factory.getBddOutgoingOriginalFlowFilterManagers().get(c1);
    BDD outgoingConstraint = originalFlowFilterMgr.outgoingOriginalFlowFiltersConstraint();
    BDD permittedOutI1 = originalFlowFilterMgr.permittedByOriginalFlowEgressFilter(i1);
    // Sanity check: make sure there really is a constraint due to an outgoingOriginalFlowFilter
    assertFalse(outgoingConstraint.isOne());
    assertFalse(permittedOutI1.isOne());

    // Create BDD of header space permitted by the outgoing original flows filter, which permits
    // flows with these three src IPs
    BDD permittedByFilter =
        Stream.of(Ip.parse("1.0.0.0"), Ip.parse("2.0.0.0"), Ip.parse("3.0.0.0"))
            .map(ip -> _pkt.getSrcIp().value(ip.asLong()))
            .reduce(BDD::or)
            .orElse(null);
    assert permittedByFilter != null;

    List<Edge> edges =
        factory
            .generateRules_PreInInterface_PostInInterface()
            .collect(ImmutableList.toImmutableList());

    // Whichever interface the flow enters, the constraint for going out i1 should be added
    StateExpr preInInterface1 = new PreInInterface(c1, i1);
    StateExpr preInInterface2 = new PreInInterface(c1, i2);
    StateExpr postInInterface1 = new PostInInterface(c1, i1);
    StateExpr postInInterface2 = new PostInInterface(c1, i2);
    Matcher<Transition> addsOriginalFlowFiltersConstraint =
        allOf(
            mapsForward(_one, outgoingConstraint), mapsBackward(permittedOutI1, permittedByFilter));
    assertThat(
        edges,
        containsInAnyOrder(
            edge(preInInterface1, postInInterface1, addsOriginalFlowFiltersConstraint),
            edge(preInInterface2, postInInterface2, addsOriginalFlowFiltersConstraint)));
  }

  @Test
  public void testAddOutgoingOriginalFlowFiltersConstraint_Originate() throws IOException {
    /*
    Test that the correct outgoingOriginalFlowFiltersConstraint is placed on flows originating in a
    VRF or interface on a node with an interface with an outgoingOriginalFlowFilter. In this case,
    c1 has interfaces i1 and i2, and i1 has an outgoingOriginalFlowFilter.
     */
    String c1 = "c1";
    String i1 = "i1";
    String i2 = "i2";
    SortedMap<String, Configuration> configs = makeOutgoingFiltersNetwork(true, false);
    String vrf = configs.get(c1).getAllInterfaces().get(i1).getVrfName();
    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);
    BDDOutgoingOriginalFlowFilterManager originalFlowFilterMgr =
        factory.getBddOutgoingOriginalFlowFilterManagers().get(c1);
    BDD outgoingConstraint = originalFlowFilterMgr.outgoingOriginalFlowFiltersConstraint();
    BDD permittedOutI1 = originalFlowFilterMgr.permittedByOriginalFlowEgressFilter(i1);
    // Sanity check: make sure there really is a constraint due to an outgoingOriginalFlowFilter
    assertFalse(outgoingConstraint.isOne());
    assertFalse(permittedOutI1.isOne());

    // Create BDD of header space permitted by the outgoing original flows filter, which permits
    // flows with these three src IPs
    BDD permittedByFilter =
        Stream.of(Ip.parse("1.0.0.0"), Ip.parse("2.0.0.0"), Ip.parse("3.0.0.0"))
            .map(ip -> _pkt.getSrcIp().value(ip.asLong()))
            .reduce(BDD::or)
            .orElse(null);

    StateExpr originateIface1 = new OriginateInterface(c1, i1);
    StateExpr originateIface2 = new OriginateInterface(c1, i2);
    StateExpr originateVrf = new OriginateVrf(c1, vrf);
    List<Edge> edges =
        Streams.concat(
                factory.generateRootEdges_OriginateInterface_PostInVrf(
                    ImmutableMap.of(originateIface1, _one, originateIface2, _one)),
                factory.generateRootEdges_OriginateVrf_PostInVrf(
                    ImmutableMap.of(originateVrf, _one)))
            .collect(ImmutableList.toImmutableList());

    // Wherever the flow originates, the constraint for going out i1 should be added. (No need to
    // add originatingFromDevice constraint because source manager is trivial.)
    StateExpr postInVrf = new PostInVrf(c1, vrf);
    Matcher<Transition> addsOriginalFlowFiltersConstraint =
        allOf(
            mapsForward(_one, outgoingConstraint), mapsBackward(permittedOutI1, permittedByFilter));
    assertThat(
        edges,
        containsInAnyOrder(
            edge(originateIface1, postInVrf, addsOriginalFlowFiltersConstraint),
            edge(originateIface2, postInVrf, addsOriginalFlowFiltersConstraint),
            edge(originateVrf, postInVrf, addsOriginalFlowFiltersConstraint)));
  }

  @Test
  public void testAddOutgoingOriginalFlowFiltersConstraint_PbrFibLookup() throws IOException {
    /*
    Test that the correct outgoingOriginalFlowFiltersConstraint is placed on flows that match a
    packet policy. In this case, the config has INGRESS_IFACE with a packet policy and interface i1
    with an outgoingOriginalFlowFilter.
     */
    String c1 = "c1";
    String i1 = "i1";
    Ip srcIp1 = Ip.parse("1.0.0.0");
    SortedMap<String, Configuration> configs = makePBRNetwork(false);
    // Add an outgoingOriginalFlowFilter on the one of the interfaces on the node with PBR
    NetworkFactory nf = new NetworkFactory();
    Configuration c = configs.get(c1);
    IpAccessList filter =
        nf.aclBuilder().setOwner(c).setLines(accepting(matchSrc(srcIp1)), REJECT_ALL).build();
    c.getAllInterfaces().get(i1).setOutgoingOriginalFlowFilter(filter);
    BDD permittedByFilter = _pkt.getSrcIp().value(srcIp1.asLong());

    BDDReachabilityAnalysisFactory factory = makeBddReachabilityAnalysisFactory(configs);
    BDDOutgoingOriginalFlowFilterManager originalFlowsMgr =
        factory.getBddOutgoingOriginalFlowFilterManagers().get(c1);
    BDD originalFlowFiltersConstraint = originalFlowsMgr.outgoingOriginalFlowFiltersConstraint();
    BDD permittedOutI1 = originalFlowsMgr.permittedByOriginalFlowEgressFilter(i1);
    // Sanity check: make sure there really is a constraint due to an outgoingOriginalFlowFilter
    assertFalse(originalFlowFiltersConstraint.isOne());
    assertFalse(permittedOutI1.isOne());

    // Flows will also be constrained to those not dropped by the packet policy
    Prefix pbrPrefix = Prefix.parse("8.8.8.0/24");
    BDD notDroppedByPbr = new IpSpaceToBDD(_pkt.getDstIp()).toBDD(pbrPrefix);

    List<Edge> edges =
        factory
            .generateRules_PreInInterface_PbrFibLookup()
            .collect(ImmutableList.toImmutableList());

    StateExpr preInInterface = new PreInInterface(c1, INGRESS_IFACE);
    StateExpr pbrFibLookup = new PbrFibLookup(c1, "vrf1", "vrf2"); // (see makePBRNetwork for names)
    assertThat(
        edges,
        contains(
            edge(
                preInInterface,
                pbrFibLookup,
                allOf(
                    mapsForward(_one, originalFlowFiltersConstraint.and(notDroppedByPbr)),
                    mapsBackward(permittedOutI1, permittedByFilter.and(notDroppedByPbr))))));
  }
}
