package org.batfish.bddreachability;

import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.DENIED_IN;
import static org.batfish.datamodel.FlowDisposition.DENIED_OUT;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.INSUFFICIENT_INFO;
import static org.batfish.datamodel.FlowDisposition.NEIGHBOR_UNREACHABLE;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.batfish.datamodel.FlowDisposition.NULL_ROUTED;
import static org.batfish.datamodel.IpAccessListLine.acceptingHeaderSpace;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.z3.expr.NodeInterfaceNeighborUnreachableMatchers.hasHostname;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.HeaderSpaceToBDD;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.DestinationNat;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SourceNat;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.flow.TraceWrapperAsAnswerElement;
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
import org.batfish.specifier.LocationSpecifiers;
import org.batfish.specifier.SpecifierContext;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.state.Accept;
import org.batfish.z3.state.DropAclIn;
import org.batfish.z3.state.DropAclOut;
import org.batfish.z3.state.DropNoRoute;
import org.batfish.z3.state.DropNullRoute;
import org.batfish.z3.state.NodeAccept;
import org.batfish.z3.state.NodeDropAclIn;
import org.batfish.z3.state.NodeDropAclOut;
import org.batfish.z3.state.NodeDropNoRoute;
import org.batfish.z3.state.NodeDropNullRoute;
import org.batfish.z3.state.NodeInterfaceDeliveredToSubnet;
import org.batfish.z3.state.NodeInterfaceExitsNetwork;
import org.batfish.z3.state.NodeInterfaceInsufficientInfo;
import org.batfish.z3.state.NodeInterfaceNeighborUnreachable;
import org.batfish.z3.state.NodeInterfaceNeighborUnreachableOrExitsNetwork;
import org.batfish.z3.state.OriginateInterfaceLink;
import org.batfish.z3.state.OriginateVrf;
import org.batfish.z3.state.PostInVrf;
import org.batfish.z3.state.PreInInterface;
import org.batfish.z3.state.PreOutEdge;
import org.batfish.z3.state.PreOutEdgePostNat;
import org.batfish.z3.state.PreOutVrf;
import org.batfish.z3.state.Query;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class BDDReachabilityAnalysisFactoryTest {
  @Rule public TemporaryFolder temp = new TemporaryFolder();

  private static final BDDPacket PKT = new BDDPacket();
  private static final IpSpaceSpecifier CONSTANT_UNIVERSE_IPSPACE_SPECIFIER =
      new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE);
  private static final BDD ONE = PKT.getFactory().one();

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
    SpecifierContext ctxt = batfish.specifierContext();
    Set<Location> locations = LocationSpecifiers.ALL_LOCATIONS.resolve(ctxt);
    return CONSTANT_UNIVERSE_IPSPACE_SPECIFIER.resolve(locations, ctxt);
  }

  @Test
  public void testBDDFactory() throws IOException {
    TestNetworkIndirection net = new TestNetworkIndirection();
    Batfish batfish = BatfishTestUtils.getBatfish(net._configs, temp);
    batfish.computeDataPlane(false);
    DataPlane dataPlane = batfish.loadDataPlane();

    // Confirm factory building does not throw, even with IpSpace and ACL indirection
    new BDDReachabilityAnalysisFactory(PKT, net._configs, dataPlane.getForwardingAnalysis());
  }

  @Test
  public void testFinalNodes() throws IOException {
    SortedMap<String, Configuration> configs = TestNetworkSources.twoNodeNetwork();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane(false);
    DataPlane dataPlane = batfish.loadDataPlane();

    assertThat(configs.size(), equalTo(2));
    for (String node : configs.keySet()) {
      String otherNode = configs.keySet().stream().filter(n -> !n.equals(node)).findFirst().get();
      Map<StateExpr, Map<StateExpr, Edge>> edges =
          new BDDReachabilityAnalysisFactory(PKT, configs, dataPlane.getForwardingAnalysis())
              .bddReachabilityAnalysis(
                  ipSpaceAssignment(batfish),
                  matchDst(UniverseIpSpace.INSTANCE),
                  ImmutableSet.of(),
                  ImmutableSet.of(),
                  ImmutableSet.of(node),
                  ALL_DISPOSITIONS)
              .getEdges();
      assertThat(edges, hasEntry(equalTo(new NodeAccept(node)), hasKey(Accept.INSTANCE)));
      assertThat(edges, not(hasEntry(equalTo(new NodeAccept(otherNode)), hasKey(Accept.INSTANCE))));

      assertThat(edges, hasEntry(equalTo(new NodeDropAclIn(node)), hasKey(DropAclIn.INSTANCE)));
      assertThat(
          edges, not(hasEntry(equalTo(new NodeDropAclIn(otherNode)), hasKey(DropAclIn.INSTANCE))));

      assertThat(edges, hasEntry(equalTo(new NodeDropAclOut(node)), hasKey(DropAclOut.INSTANCE)));
      assertThat(
          edges,
          not(hasEntry(equalTo(new NodeDropAclOut(otherNode)), hasKey(DropAclOut.INSTANCE))));

      Set<NodeInterfaceNeighborUnreachableOrExitsNetwork> neighborUnreachables =
          edges
              .keySet()
              .stream()
              .filter(NodeInterfaceNeighborUnreachableOrExitsNetwork.class::isInstance)
              .map(NodeInterfaceNeighborUnreachableOrExitsNetwork.class::cast)
              .collect(Collectors.toSet());
      neighborUnreachables.forEach(nu -> assertThat(nu, hasHostname(node)));

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
    batfish.computeDataPlane(false);
    DataPlane dataPlane = batfish.loadDataPlane();

    assertThat(configs.size(), equalTo(2));
    for (String node : configs.keySet()) {
      String otherNode = configs.keySet().stream().filter(n -> !n.equals(node)).findFirst().get();
      BDDReachabilityAnalysisFactory bddReachabilityAnalysisFactory =
          new BDDReachabilityAnalysisFactory(PKT, configs, dataPlane.getForwardingAnalysis());
      Map<StateExpr, Map<StateExpr, Edge>> edgeMap =
          bddReachabilityAnalysisFactory
              .bddReachabilityAnalysis(
                  ipSpaceAssignment(batfish),
                  matchDst(UniverseIpSpace.INSTANCE),
                  ImmutableSet.of(node),
                  ImmutableSet.of(),
                  ImmutableSet.of(),
                  ALL_DISPOSITIONS)
              .getEdges();

      Set<Edge> edges =
          edgeMap.values().stream().flatMap(m -> m.values().stream()).collect(Collectors.toSet());

      assertThat(
          "Edges at which a forbiddenTransitNode would become transited should be removed.",
          edges
              .stream()
              .noneMatch(
                  edge ->
                      edge.getPreState() instanceof PreOutEdgePostNat
                          && edge.getPostState() instanceof PreInInterface
                          && ((PreOutEdgePostNat) edge.getPreState()).getSrcNode().equals(node)));
      assertThat(
          "Edges at which a non-forbiddenTransitNodes become transited should not be removed.",
          edges
              .stream()
              .anyMatch(
                  edge ->
                      edge.getPreState() instanceof PreOutEdgePostNat
                          && edge.getPostState() instanceof PreInInterface
                          && ((PreOutEdgePostNat) edge.getPreState())
                              .getSrcNode()
                              .equals(otherNode)));
    }
  }

  @Test
  public void testRequiredTransitNodes() throws IOException {
    SortedMap<String, Configuration> configs = TestNetworkSources.twoNodeNetwork();
    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane(false);
    DataPlane dataPlane = batfish.loadDataPlane();

    assertThat(configs.size(), equalTo(2));
    for (String node : configs.keySet()) {
      BDDReachabilityAnalysisFactory bddReachabilityAnalysisFactory =
          new BDDReachabilityAnalysisFactory(PKT, configs, dataPlane.getForwardingAnalysis());
      BDD requiredTransitNodesBDD = bddReachabilityAnalysisFactory.getRequiredTransitNodeBDD();
      BDD transited = requiredTransitNodesBDD;
      BDD notTransited = requiredTransitNodesBDD.not();
      Map<StateExpr, Map<StateExpr, Edge>> edgeMap =
          bddReachabilityAnalysisFactory
              .bddReachabilityAnalysis(
                  ipSpaceAssignment(batfish),
                  matchDst(UniverseIpSpace.INSTANCE),
                  ImmutableSet.of(),
                  ImmutableSet.of(node),
                  ImmutableSet.of(),
                  ALL_DISPOSITIONS)
              .getEdges();
      Set<Edge> edges =
          edgeMap.values().stream().flatMap(m -> m.values().stream()).collect(Collectors.toSet());

      // all edges into the query state require transit nodes transited bit to be set.
      edges
          .stream()
          .filter(edge -> edge.getPostState() == Query.INSTANCE)
          .forEach(
              edge -> {
                assertThat(
                    "Edge into query state must require requiredTransitNodes bit to be one",
                    edge.traverseForward(ONE).and(notTransited).isZero());
                assertThat(
                    "Edge into query state must require requiredTransitNodes bit to be one",
                    edge.traverseBackward(ONE).and(notTransited).isZero());
              });

      // all edges from originate states initialize the transit nodes bit to zero
      edges
          .stream()
          .filter(
              edge ->
                  edge.getPreState() instanceof OriginateVrf
                      || edge.getPreState() instanceof OriginateInterfaceLink)
          .forEach(
              edge -> {
                assertThat(
                    "Edge out of originate state must require requiredTransitNodes bit to be zero",
                    edge.traverseForward(ONE).and(transited).isZero());
                assertThat(
                    "Edge out of originate state must require requiredTransitNodes bit to be zero",
                    edge.traverseBackward(ONE).and(transited).isZero());
              });

      edges
          .stream()
          .filter(
              edge ->
                  edge.getPreState() instanceof PreOutEdgePostNat
                      && edge.getPostState() instanceof PreInInterface)
          .forEach(
              edge -> {
                String hostname = ((PreOutEdgePostNat) edge.getPreState()).getSrcNode();
                if (hostname.equals(node)) {
                  assertThat(
                      "Forward Edge from PreOutEdgePostNat to PreInInterface for a "
                          + "requiredTransitNode must set the requiredTransitNodes bit",
                      edge.traverseForward(ONE).and(notTransited).isZero());
                  BDD backwardOne = edge.traverseBackward(ONE);
                  assertThat(
                      "Backward Edge from PreOutEdgePostNat to PreInInterface for a "
                          + " requiredTransitNode must not constrain the bit after exit",
                      backwardOne.exist(requiredTransitNodesBDD).equals(backwardOne));
                } else {
                  assertThat(
                      "Edge from PreOutEdgePostNat to PreInInterface for a "
                          + "non-requiredTransitNode must not touch the requiredTransitNodes bit",
                      edge.traverseForward(transited).and(notTransited).isZero());
                  assertThat(
                      "Edge from PreOutEdgePostNat to PreInInterface for a "
                          + "non-requiredTransitNode must not touch the requiredTransitNodes bit",
                      edge.traverseForward(notTransited).and(transited).isZero());
                  assertThat(
                      "Edge from PreOutEdgePostNat to PreInInterface for a "
                          + "non-requiredTransitNode must not touch the requiredTransitNodes bit",
                      edge.traverseBackward(transited).and(notTransited).isZero());
                  assertThat(
                      "Edge from PreOutEdgePostNat to PreInInterface for a "
                          + "non-requiredTransitNode must not touch the requiredTransitNodes bit",
                      edge.traverseBackward(notTransited).and(transited).isZero());
                }
              });
    }
  }

  @Test
  public void testGetAllBDDsLoop() throws IOException {
    SortedMap<String, Configuration> configs = LoopNetwork.testLoopNetwork(true);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane(false);

    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setSourceLocationSpecifier(AllInterfacesLocationSpecifier.INSTANCE)
            .setActions(ImmutableSortedSet.of(FlowDisposition.LOOP))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setHeaderSpace(new HeaderSpace())
            .build();

    AnswerElement answer = batfish.bddSingleReachability(reachabilityParameters);

    assertThat(answer, instanceOf(TraceWrapperAsAnswerElement.class));
    Map<Flow, List<Trace>> flowTraces = ((TraceWrapperAsAnswerElement) answer).getFlowTraces();
    Set<Flow> flows = flowTraces.keySet();
    assertThat(flows, hasSize(2));

    Set<Ip> srcIps = flows.stream().map(Flow::getSrcIp).collect(Collectors.toSet());
    Set<Ip> dstIps = flows.stream().map(Flow::getDstIp).collect(Collectors.toSet());

    assertThat(srcIps, contains(new Ip("1.0.0.0"), new Ip("1.0.0.1")));
    assertThat(dstIps, contains(new Ip("2.0.0.0")));

    Set<FlowDisposition> flowDispositions =
        flowTraces
            .values()
            .stream()
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

    InterfaceAddress loopbackAddress = new InterfaceAddress("1.2.3.4/32");
    nf.interfaceBuilder()
        .setActive(true)
        .setOwner(loopbackConfig)
        .setVrf(vrf)
        .setAddress(loopbackAddress)
        .build();
    configs.put(loopbackConfig.getHostname(), loopbackConfig);

    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane(false);

    ReachabilityParameters reachabilityParameters =
        ReachabilityParameters.builder()
            .setSourceLocationSpecifier(AllInterfacesLocationSpecifier.INSTANCE)
            .setActions(ImmutableSortedSet.of(FlowDisposition.LOOP, FlowDisposition.NO_ROUTE))
            .setFinalNodesSpecifier(AllNodesNodeSpecifier.INSTANCE)
            .setHeaderSpace(HeaderSpace.builder().setDstIps(new Ip("2.0.0.0").toIpSpace()).build())
            .build();

    AnswerElement answer = batfish.bddSingleReachability(reachabilityParameters);

    assertThat(answer, instanceOf(TraceWrapperAsAnswerElement.class));
    Map<Flow, List<Trace>> flowTraces = ((TraceWrapperAsAnswerElement) answer).getFlowTraces();
    Set<Flow> flows = flowTraces.keySet();
    assertThat(flows, hasSize(3));

    Set<Ip> srcIps = flows.stream().map(Flow::getSrcIp).collect(Collectors.toSet());
    Set<Ip> dstIps = flows.stream().map(Flow::getDstIp).collect(Collectors.toSet());

    assertThat(srcIps, contains(new Ip("1.0.0.0"), new Ip("1.0.0.1"), new Ip("1.2.3.4")));
    assertThat(dstIps, contains(new Ip("2.0.0.0")));

    Set<FlowDisposition> flowDispositions =
        flowTraces
            .values()
            .stream()
            .flatMap(List::stream)
            .map(Trace::getDisposition)
            .collect(ImmutableSet.toImmutableSet());

    assertThat(
        flowDispositions, containsInAnyOrder(FlowDisposition.LOOP, FlowDisposition.NO_ROUTE));
  }

  @Test
  public void testInactiveInterface() throws IOException {
    Ip ip = new Ip("1.2.3.4");
    BDD ipBDD = new IpSpaceToBDD(PKT.getDstIp()).toBDD(ip);

    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setAddress(new InterfaceAddress(ip, 32))
            .setActive(true)
            .build();

    // when interface is active and not blacklisted, its Ip belongs to the VRF
    ImmutableSortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(config.getHostname(), config);
    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane(false);
    BDDReachabilityAnalysisFactory factory =
        new BDDReachabilityAnalysisFactory(
            PKT, configs, batfish.loadDataPlane().getForwardingAnalysis());
    assertThat(
        factory.getVrfAcceptBDDs(),
        hasEntry(equalTo(config.getHostname()), hasEntry(equalTo(vrf.getName()), equalTo(ipBDD))));

    // when interface is inactive, its Ip does not belong to the VRF
    iface.setActive(false);
    batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane(false);
    factory =
        new BDDReachabilityAnalysisFactory(
            PKT, configs, batfish.loadDataPlane().getForwardingAnalysis());
    assertThat(
        factory.getVrfAcceptBDDs(),
        hasEntry(
            equalTo(config.getHostname()),
            hasEntry(equalTo(vrf.getName()), equalTo(PKT.getFactory().zero()))));

    // when interface is blacklisted, its Ip does not belong to the VRF
    iface.setActive(true);
    iface.setBlacklisted(true);
    batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane(false);
    factory =
        new BDDReachabilityAnalysisFactory(
            PKT, configs, batfish.loadDataPlane().getForwardingAnalysis());
    assertThat(
        factory.getVrfAcceptBDDs(),
        hasEntry(
            equalTo(config.getHostname()),
            hasEntry(equalTo(vrf.getName()), equalTo(PKT.getFactory().zero()))));
  }

  @Test
  public void testNatBackwardEdge() {
    BDD var = Arrays.stream(PKT.getSrcIp().getBitvec()).reduce(PKT.getFactory().one(), BDD::and);

    IpSpaceToBDD dstToBDD = new IpSpaceToBDD(PKT.getDstIp());
    IpSpaceToBDD srcToBDD = new IpSpaceToBDD(PKT.getSrcIp());
    BDD dst123 = dstToBDD.toBDD(Prefix.parse("1.2.3.0/24"));
    BDD dst1234 = dstToBDD.toBDD(Prefix.parse("1.2.3.4/32"));
    BDD src1111 = srcToBDD.toBDD(new Ip("1.1.1.1"));
    BDD src2222 = srcToBDD.toBDD(new Ip("2.2.2.2"));
    Function<BDD, BDD> edge =
        BDDReachabilityAnalysisFactory.natBackwardEdge(
            ImmutableList.of(new BDDNat(dst1234, src1111), new BDDNat(dst123, src2222)), var);

    BDD noMatch = dst123.not().and(dst1234.not());
    assertThat(edge.apply(src1111), equalTo(dst1234.or(noMatch.and(src1111))));
    assertThat(edge.apply(src2222), equalTo(dst123.and(dst1234.not()).or(noMatch.and(src2222))));

    BDD src3333 = srcToBDD.toBDD(new Ip("3.3.3.3"));
    assertThat(edge.apply(src3333), equalTo(noMatch.and(src3333)));
  }

  @Test
  public void testNatBackwardEdge_nullPool() {
    BDD var = Arrays.stream(PKT.getSrcIp().getBitvec()).reduce(PKT.getFactory().one(), BDD::and);

    IpSpaceToBDD dstToBDD = new IpSpaceToBDD(PKT.getDstIp());
    IpSpaceToBDD srcToBDD = new IpSpaceToBDD(PKT.getSrcIp());
    BDD dst123 = dstToBDD.toBDD(Prefix.parse("1.2.3.0/24"));
    BDD dst1234 = dstToBDD.toBDD(Prefix.parse("1.2.3.4/32"));
    BDD dst1235 = dstToBDD.toBDD(Prefix.parse("1.2.3.5/32"));
    BDD src1111 = srcToBDD.toBDD(new Ip("1.1.1.1"));
    BDD src2222 = srcToBDD.toBDD(new Ip("2.2.2.2"));
    Function<BDD, BDD> edge =
        BDDReachabilityAnalysisFactory.natBackwardEdge(
            ImmutableList.of(
                new BDDNat(dst1234, src1111),
                new BDDNat(dst1235, null),
                new BDDNat(dst123, src2222)),
            var);

    // not(dst123) and not(dst1234) and not(dst1235) == not(dst123)
    BDD noMatch = dst123.not();
    // src IP is not rewritten if the dst1235 rule is matched or none matches
    BDD noRewrite = noMatch.or(dst1235);
    assertThat(edge.apply(src1111), equalTo(dst1234.or(src1111.and(noRewrite))));
    assertThat(
        edge.apply(src2222),
        equalTo(dst123.and(dst1234.not()).and(dst1235.not()).or(src2222.and(noRewrite))));

    BDD src3333 = srcToBDD.toBDD(new Ip("3.3.3.3"));
    assertThat(edge.apply(src3333), equalTo(noRewrite.and(src3333)));
  }

  @Test
  public void testDestNat() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Ip poolIp = new Ip("5.5.5.5");
    HeaderSpace ingressAclHeaderSpace =
        HeaderSpace.builder().setSrcIps(Prefix.parse("2.0.0.0/8").toIpSpace()).build();
    HeaderSpace natMatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(100, 100))).build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setActive(true)
            .setAddress(new InterfaceAddress("1.0.0.0/8"))
            .setIncomingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(ingressAclHeaderSpace)))
                    .build())
            .setDestinationNats(
                ImmutableList.of(
                    // if 100
                    DestinationNat.builder()
                        .setAcl(
                            nf.aclBuilder()
                                .setOwner(config)
                                .setLines(
                                    ImmutableList.of(acceptingHeaderSpace(natMatchHeaderSpace)))
                                .build())
                        .setPoolIpFirst(poolIp)
                        .setPoolIpLast(poolIp)
                        .build()))
            .build();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(config.getHostname(), config);
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane(false);

    BDDReachabilityAnalysisFactory factory =
        new BDDReachabilityAnalysisFactory(
            PKT, configurations, batfish.loadDataPlane().getForwardingAnalysis());

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLinkLocation(config.getHostname(), iface.getName()),
                    UniverseIpSpace.INSTANCE)
                .build());
    Edge edge =
        analysis
            .getEdges()
            .get(new PreInInterface(config.getHostname(), iface.getName()))
            .get(new PostInVrf(config.getHostname(), vrf.getName()));

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(PKT, ImmutableMap.of());
    BDD ingressAclBdd = toBDD.toBDD(ingressAclHeaderSpace);
    BDD natMatchBdd = toBDD.toBDD(natMatchHeaderSpace);
    BDD origDstIpBdd = toBDD.getDstIpSpaceToBdd().toBDD(new Ip("6.6.6.6"));
    BDD poolIpBdd = toBDD.getDstIpSpaceToBdd().toBDD(poolIp);

    assertThat(
        edge.traverseForward(origDstIpBdd),
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
    Ip poolIp = new Ip("5.5.5.5");
    HeaderSpace ingressAclHeaderSpace =
        HeaderSpace.builder().setSrcIps(Prefix.parse("2.0.0.0/8").toIpSpace()).build();
    HeaderSpace nat1MatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(105, 105))).build();
    HeaderSpace nat2MatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(100, 110))).build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setActive(true)
            .setAddress(new InterfaceAddress("1.0.0.0/8"))
            .setIncomingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(ingressAclHeaderSpace)))
                    .build())
            .setDestinationNats(
                ImmutableList.of(
                    // if srcPort = 105, don't NAT
                    DestinationNat.builder()
                        .setAcl(
                            nf.aclBuilder()
                                .setOwner(config)
                                .setLines(
                                    ImmutableList.of(acceptingHeaderSpace(nat1MatchHeaderSpace)))
                                .build())
                        .build(),
                    // if srcPort = 100-110, NAT
                    DestinationNat.builder()
                        .setAcl(
                            nf.aclBuilder()
                                .setOwner(config)
                                .setLines(
                                    ImmutableList.of(acceptingHeaderSpace(nat2MatchHeaderSpace)))
                                .build())
                        .setPoolIpFirst(poolIp)
                        .setPoolIpLast(poolIp)
                        .build()))
            .build();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(config.getHostname(), config);
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane(false);

    BDDReachabilityAnalysisFactory factory =
        new BDDReachabilityAnalysisFactory(
            PKT, configurations, batfish.loadDataPlane().getForwardingAnalysis());

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLinkLocation(config.getHostname(), iface.getName()),
                    UniverseIpSpace.INSTANCE)
                .build());
    Edge edge =
        analysis
            .getEdges()
            .get(new PreInInterface(config.getHostname(), iface.getName()))
            .get(new PostInVrf(config.getHostname(), vrf.getName()));

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(PKT, ImmutableMap.of());
    BDD ingressAclBdd = toBDD.toBDD(ingressAclHeaderSpace);
    BDD nat1MatchBdd = toBDD.toBDD(nat1MatchHeaderSpace);
    BDD nat2MatchBdd = toBDD.toBDD(nat2MatchHeaderSpace);
    BDD origDstIpBdd = toBDD.getDstIpSpaceToBdd().toBDD(new Ip("6.6.6.6"));
    BDD poolIpBdd = toBDD.getDstIpSpaceToBdd().toBDD(poolIp);

    assertThat(
        edge.traverseForward(origDstIpBdd),
        equalTo(
            ingressAclBdd.and(nat1MatchBdd.not().and(nat2MatchBdd).ite(poolIpBdd, origDstIpBdd))));
  }

  @Test
  public void testSrcNatNullPool() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration config =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Ip poolIp = new Ip("5.5.5.5");
    HeaderSpace preNatAclHeaderSpace =
        HeaderSpace.builder().setDstIps(Prefix.parse("2.0.0.0/24").toIpSpace()).build();
    HeaderSpace nat1MatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(105, 105))).build();
    HeaderSpace nat2MatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(100, 110))).build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setActive(true)
            .setAddress(new InterfaceAddress("1.0.0.1/24"))
            .setPreSourceNatOutgoingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(preNatAclHeaderSpace)))
                    .build())
            .setSourceNats(
                ImmutableList.of(
                    // if srcPort = 105, don't NAT
                    SourceNat.builder()
                        .setAcl(
                            nf.aclBuilder()
                                .setOwner(config)
                                .setLines(
                                    ImmutableList.of(acceptingHeaderSpace(nat1MatchHeaderSpace)))
                                .build())
                        .build(),
                    // if srcPort = 100-110, NAT
                    SourceNat.builder()
                        .setAcl(
                            nf.aclBuilder()
                                .setOwner(config)
                                .setLines(
                                    ImmutableList.of(acceptingHeaderSpace(nat2MatchHeaderSpace)))
                                .build())
                        .setPoolIpFirst(poolIp)
                        .setPoolIpLast(poolIp)
                        .build()))
            .build();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(config.getHostname(), config);
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane(false);

    BDDReachabilityAnalysisFactory factory =
        new BDDReachabilityAnalysisFactory(
            PKT, configurations, batfish.loadDataPlane().getForwardingAnalysis());

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLinkLocation(config.getHostname(), iface.getName()),
                    UniverseIpSpace.INSTANCE)
                .build());
    Edge edge =
        analysis
            .getEdges()
            .get(new PreOutVrf(config.getHostname(), vrf.getName()))
            .get(new NodeInterfaceDeliveredToSubnet(config.getHostname(), iface.getName()));

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(PKT, ImmutableMap.of());
    BDD preNatAclBdd = toBDD.toBDD(preNatAclHeaderSpace);
    BDD nat1MatchBdd = toBDD.toBDD(nat1MatchHeaderSpace);
    BDD nat2MatchBdd = toBDD.toBDD(nat2MatchHeaderSpace);
    BDD origSrcIpBdd = toBDD.getSrcIpSpaceToBdd().toBDD(new Ip("6.6.6.6"));
    BDD poolIpBdd = toBDD.getSrcIpSpaceToBdd().toBDD(poolIp);
    BDD routeBdd = toBDD.getDstIpSpaceToBdd().toBDD(Prefix.parse("1.0.0.0/24"));

    assertThat(
        edge.traverseForward(origSrcIpBdd),
        equalTo(
            routeBdd
                .and(preNatAclBdd)
                .and(nat1MatchBdd.not().and(nat2MatchBdd).ite(poolIpBdd, origSrcIpBdd))));
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
    Ip poolIp = new Ip("5.5.5.5");
    HeaderSpace postNatOutAclHeaderSpace =
        HeaderSpace.builder().setDstPorts(ImmutableList.of(new SubRange(80, 80))).build();
    HeaderSpace natMatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(100, 100))).build();
    HeaderSpace preNatOutAclHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(50, 150))).build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setActive(true)
            .setAddress(new InterfaceAddress("1.0.0.0/31"))
            .setPreSourceNatOutgoingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(preNatOutAclHeaderSpace)))
                    .build())
            .setOutgoingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(postNatOutAclHeaderSpace)))
                    .build())
            .setSourceNats(
                ImmutableList.of(
                    SourceNat.builder()
                        .setAcl(
                            nf.aclBuilder()
                                .setOwner(config)
                                .setLines(
                                    ImmutableList.of(acceptingHeaderSpace(natMatchHeaderSpace)))
                                .build())
                        .setPoolIpFirst(poolIp)
                        .setPoolIpLast(poolIp)
                        .build()))
            .build();
    String ifaceName = iface.getName();
    Prefix staticRoutePrefix = Prefix.parse("3.3.3.3/32");
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopInterface(ifaceName)
                .setAdministrativeCost(1)
                .setNetwork(staticRoutePrefix)
                .build()));

    String hostname = config.getHostname();

    SortedMap<String, Configuration> configurations = ImmutableSortedMap.of(hostname, config);
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane(false);

    BDDReachabilityAnalysisFactory factory =
        new BDDReachabilityAnalysisFactory(
            PKT, configurations, batfish.loadDataPlane().getForwardingAnalysis());

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(new InterfaceLocation(hostname, ifaceName), UniverseIpSpace.INSTANCE)
                .build());

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(PKT, ImmutableMap.of());
    IpSpaceToBDD dstToBdd = toBDD.getDstIpSpaceToBdd();
    IpSpaceToBDD srcToBdd = toBDD.getSrcIpSpaceToBdd();

    BDD preNatOutAclBdd = toBDD.toBDD(preNatOutAclHeaderSpace);
    BDD postNatOutAclBdd = toBDD.toBDD(postNatOutAclHeaderSpace);
    BDD natMatchBdd = toBDD.toBDD(natMatchHeaderSpace);
    BDD poolIpBdd = srcToBdd.toBDD(poolIp);

    Map<StateExpr, Edge> preOutVrfOutEdges =
        analysis.getEdges().get(new PreOutVrf(hostname, vrf.getName()));

    // DeliveredToSubnet
    Edge edge = preOutVrfOutEdges.get(new NodeInterfaceDeliveredToSubnet(hostname, ifaceName));

    BDD origSrcIpBdd = srcToBdd.toBDD(new Ip("6.6.6.6"));
    BDD subnetIp = dstToBdd.toBDD(new Ip("1.0.0.1"));

    assertThat(
        edge.traverseForward(origSrcIpBdd),
        equalTo(
            subnetIp
                .and(preNatOutAclBdd)
                .and(natMatchBdd.ite(poolIpBdd, origSrcIpBdd).and(postNatOutAclBdd))));

    // ExitsNetwork
    edge = preOutVrfOutEdges.get(new NodeInterfaceExitsNetwork(hostname, ifaceName));
    BDD staticRoutePrefixBDD = dstToBdd.toBDD(staticRoutePrefix);
    assertThat(
        edge.traverseForward(origSrcIpBdd),
        equalTo(
            staticRoutePrefixBDD
                .and(preNatOutAclBdd)
                .and(natMatchBdd.ite(poolIpBdd, origSrcIpBdd).and(postNatOutAclBdd))));

    // NeighborUnreachable
    edge = preOutVrfOutEdges.get(new NodeInterfaceNeighborUnreachable(hostname, ifaceName));

    origSrcIpBdd = srcToBdd.toBDD(new Ip("6.6.6.6"));
    BDD ifaceIp = dstToBdd.toBDD(new Ip("1.0.0.0"));

    assertThat(
        edge.traverseForward(origSrcIpBdd),
        equalTo(
            ifaceIp
                .and(preNatOutAclBdd)
                .and(natMatchBdd.ite(poolIpBdd, origSrcIpBdd).and(postNatOutAclBdd))));

    // DropAclOut
    edge = preOutVrfOutEdges.get(new NodeDropAclOut(hostname));
    BDD deniedAfterNat = natMatchBdd.ite(poolIpBdd, origSrcIpBdd).and(postNatOutAclBdd.not());
    BDD deniedBeforeNat = origSrcIpBdd.and(preNatOutAclBdd.not());
    BDD deniedViaDelivered = subnetIp.and(deniedBeforeNat.or(deniedAfterNat));
    BDD deniedViaExits = staticRoutePrefixBDD.and(deniedBeforeNat.or(deniedAfterNat));
    BDD deniedViaNU = ifaceIp.and(deniedBeforeNat.or(deniedAfterNat));
    assertThat(
        edge.traverseForward(origSrcIpBdd),
        equalTo(deniedViaDelivered.or(deniedViaExits).or(deniedViaNU)));
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
    Ip poolIp = new Ip("5.5.5.5");
    HeaderSpace postNatOutAclHeaderSpace =
        HeaderSpace.builder().setDstPorts(ImmutableList.of(new SubRange(80, 80))).build();
    HeaderSpace natMatchHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(100, 100))).build();
    HeaderSpace preNatOutAclHeaderSpace =
        HeaderSpace.builder().setSrcPorts(ImmutableList.of(new SubRange(50, 150))).build();
    Interface iface =
        nf.interfaceBuilder()
            .setOwner(config)
            .setVrf(vrf)
            .setActive(true)
            .setAddress(new InterfaceAddress("1.0.0.0/31"))
            .setPreSourceNatOutgoingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(preNatOutAclHeaderSpace)))
                    .build())
            .setOutgoingFilter(
                nf.aclBuilder()
                    .setOwner(config)
                    .setLines(ImmutableList.of(acceptingHeaderSpace(postNatOutAclHeaderSpace)))
                    .build())
            .setSourceNats(
                ImmutableList.of(
                    SourceNat.builder()
                        .setAcl(
                            nf.aclBuilder()
                                .setOwner(config)
                                .setLines(
                                    ImmutableList.of(acceptingHeaderSpace(natMatchHeaderSpace)))
                                .build())
                        .setPoolIpFirst(poolIp)
                        .setPoolIpLast(poolIp)
                        .build()))
            .build();
    String ifaceName = iface.getName();
    Prefix staticRoutePrefix = Prefix.parse("3.3.3.3/32");
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
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
            .setAddress(new InterfaceAddress("1.0.0.1/31"))
            .build();

    String hostname = config.getHostname();
    String peername = peer.getHostname();
    String peerIfaceName = peerIface.getName();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(hostname, config, peername, peer);
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane(false);

    BDDReachabilityAnalysisFactory factory =
        new BDDReachabilityAnalysisFactory(
            PKT, configurations, batfish.loadDataPlane().getForwardingAnalysis());

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(new InterfaceLocation(hostname, ifaceName), UniverseIpSpace.INSTANCE)
                .build());

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(PKT, ImmutableMap.of());
    IpSpaceToBDD srcToBdd = toBDD.getSrcIpSpaceToBdd();

    BDD preNatOutAclBdd = toBDD.toBDD(preNatOutAclHeaderSpace);
    BDD natMatchBdd = toBDD.toBDD(natMatchHeaderSpace);
    BDD poolIpBdd = srcToBdd.toBDD(poolIp);
    BDD origSrcIpBdd = srcToBdd.toBDD(new Ip("6.6.6.6"));

    Map<StateExpr, Edge> preOutEdgeOutEdges =
        analysis.getEdges().get(new PreOutEdge(hostname, ifaceName, peername, peerIfaceName));

    Edge edge =
        preOutEdgeOutEdges.get(new PreOutEdgePostNat(hostname, ifaceName, peername, peerIfaceName));
    assertThat(
        edge.traverseForward(origSrcIpBdd),
        equalTo(preNatOutAclBdd.and(natMatchBdd.ite(poolIpBdd, origSrcIpBdd))));

    edge = preOutEdgeOutEdges.get(new NodeDropAclOut(hostname));
    assertThat(edge.traverseForward(ONE), equalTo(preNatOutAclBdd.not()));
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
            .setAddress(new InterfaceAddress("1.0.0.1/30"))
            .setOwner(c1)
            .setVrf(v1)
            .build();

    // set up static route "8.8.8.0/24" -> 1.0.0.2
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopIp(new Ip("1.0.0.2"))
                .setNetwork(Prefix.parse("8.8.8.0/24"))
                .setAdministrativeCost(1)
                .build()));

    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(c1.getHostname(), c1);

    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);
    batfish.computeDataPlane(false);

    BDDReachabilityAnalysisFactory factory =
        new BDDReachabilityAnalysisFactory(
            PKT, configs, batfish.loadDataPlane().getForwardingAnalysis());

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLocation(c1.getHostname(), i1.getName()), UniverseIpSpace.INSTANCE)
                .build());

    Edge edge =
        analysis
            .getEdges()
            .get(new PreOutVrf(c1.getHostname(), v1.getName()))
            .get(new NodeInterfaceExitsNetwork(c1.getHostname(), i1.getName()));

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(PKT, ImmutableMap.of());
    IpSpaceToBDD dstToBdd = toBDD.getDstIpSpaceToBdd();

    BDD resultBDD = dstToBdd.toBDD(Prefix.parse("8.8.8.0/24"));

    assertThat(edge.traverseForward(resultBDD), equalTo(resultBDD));

    Edge edgeII =
        analysis
            .getEdges()
            .get(new PreOutVrf(c1.getHostname(), v1.getName()))
            .get(new NodeInterfaceInsufficientInfo(c1.getHostname(), i1.getName()));

    assertThat(edgeII, nullValue());
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
            .setAddress(new InterfaceAddress("1.0.0.1/30"))
            .setOwner(c1)
            .setVrf(v1)
            .build();

    // set up static route "8.8.8.0/24" -> 1.0.0.2
    v1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.builder()
                .setNextHopIp(new Ip("2.0.0.2"))
                .setNetwork(Prefix.parse("8.8.8.0/24"))
                .setNextHopInterface(i1.getName())
                .setAdministrativeCost(1)
                .build()));

    // set up another node
    Configuration c2 = cb.build();

    Vrf v2 = nf.vrfBuilder().setOwner(c2).build();
    nf.interfaceBuilder()
        .setAddresses(new InterfaceAddress("2.0.0.2/31"))
        .setOwner(c2)
        .setVrf(v2)
        .build();

    SortedMap<String, Configuration> configs =
        ImmutableSortedMap.of(c1.getHostname(), c1, c2.getHostname(), c2);

    Batfish batfish = BatfishTestUtils.getBatfish(configs, temp);

    batfish.computeDataPlane(false);

    BDDReachabilityAnalysisFactory factory =
        new BDDReachabilityAnalysisFactory(
            PKT, configs, batfish.loadDataPlane().getForwardingAnalysis());

    BDDReachabilityAnalysis analysis =
        factory.bddReachabilityAnalysis(
            IpSpaceAssignment.builder()
                .assign(
                    new InterfaceLocation(c1.getHostname(), i1.getName()), UniverseIpSpace.INSTANCE)
                .build());

    Edge edge =
        analysis
            .getEdges()
            .get(new PreOutVrf(c1.getHostname(), v1.getName()))
            .get(new NodeInterfaceInsufficientInfo(c1.getHostname(), i1.getName()));

    HeaderSpaceToBDD toBDD = new HeaderSpaceToBDD(PKT, ImmutableMap.of());
    IpSpaceToBDD dstToBdd = toBDD.getDstIpSpaceToBdd();

    BDD resultBDD = dstToBdd.toBDD(Prefix.parse("8.8.8.0/24"));

    assertThat(edge.traverseForward(resultBDD), equalTo(resultBDD));

    Edge edgeEN =
        analysis
            .getEdges()
            .get(new PreOutVrf(c1.getHostname(), v1.getName()))
            .get(new NodeInterfaceExitsNetwork(c1.getHostname(), i1.getName()));

    assertThat(edgeEN.traverseForward(resultBDD), equalTo(PKT.getFactory().zero()));
  }

  @Test
  public void testFinalHeaderSpaceBdd() throws IOException {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration config = cb.build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Ip srcNatPoolIp = new Ip("5.5.5.5");
    Ip dstNatPoolIp = new Ip("6.6.6.6");
    nf.interfaceBuilder()
        .setOwner(config)
        .setVrf(vrf)
        .setActive(true)
        .setAddress(new InterfaceAddress("1.0.0.0/31"))
        .setSourceNats(
            ImmutableList.of(
                SourceNat.builder()
                    .setPoolIpFirst(srcNatPoolIp)
                    .setPoolIpLast(srcNatPoolIp)
                    .build()))
        .setDestinationNats(
            ImmutableList.of(
                DestinationNat.builder()
                    .setPoolIpFirst(dstNatPoolIp)
                    .setPoolIpLast(dstNatPoolIp)
                    .build()))
        .build();

    String hostname = config.getHostname();

    SortedMap<String, Configuration> configurations = ImmutableSortedMap.of(hostname, config);
    Batfish batfish = BatfishTestUtils.getBatfish(configurations, temp);
    batfish.computeDataPlane(false);

    BDDReachabilityAnalysisFactory factory =
        new BDDReachabilityAnalysisFactory(
            PKT, configurations, batfish.loadDataPlane().getForwardingAnalysis());

    BDD one = PKT.getFactory().one();
    assertThat(factory.computeFinalHeaderSpaceBdd(one), equalTo(one));
    BDD dstIp1 = PKT.getDstIp().value(1);
    BDD dstNatPoolIpBdd = PKT.getDstIp().value(dstNatPoolIp.asLong());
    BDD srcIp1 = PKT.getSrcIp().value(1);
    BDD srcNatPoolIpBdd = PKT.getSrcIp().value(srcNatPoolIp.asLong());
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
}
