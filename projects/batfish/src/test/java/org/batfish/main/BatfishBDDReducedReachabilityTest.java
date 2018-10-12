package org.batfish.main;

import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.FlowDisposition.ACCEPTED;
import static org.batfish.datamodel.FlowDisposition.DENIED_IN;
import static org.batfish.datamodel.FlowDisposition.DENIED_OUT;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.NEIGHBOR_UNREACHABLE;
import static org.batfish.datamodel.FlowDisposition.NO_ROUTE;
import static org.batfish.datamodel.FlowDisposition.NULL_ROUTED;
import static org.batfish.datamodel.Interface.NULL_INTERFACE_NAME;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasDstIp;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressInterface;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressNode;
import static org.batfish.datamodel.matchers.FlowMatchers.hasIngressVrf;
import static org.batfish.datamodel.matchers.FlowMatchers.hasSrcIp;
import static org.batfish.main.BatfishTestUtils.getBatfish;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.FlowTrace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.question.reducedreachability.DifferentialReachabilityResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

/** Test of {@link Batfish#bddReducedReachability} */
public class BatfishBDDReducedReachabilityTest {
  private static final Ip DST_IP = new Ip("3.3.3.3");
  private static final String NODE1 = "node1";
  private static final String NODE2 = "node2";
  private static final String PHYSICAL = "FastEthernet0/0";
  private static final InterfaceAddress NODE1_PHYSICAL_NETWORK = new InterfaceAddress("2.0.0.0/8");
  private static final Ip NODE1_PHYSICAL_IP = NODE1_PHYSICAL_NETWORK.getIp();
  // the lowest IP addr of the network other than NODE1_PHYSICAL_IP.
  private static final Ip NODE1_PHYSICAL_LINK_IP = new Ip("2.0.0.1");

  private static final InterfaceAddress NODE2_PHYSICAL_NETWORK = new InterfaceAddress("2.0.0.1/8");
  private static final Ip NODE2_PHYSICAL_IP = NODE1_PHYSICAL_NETWORK.getIp();

  private Configuration.Builder _cb;

  @Rule public TemporaryFolder _folder = new TemporaryFolder();
  @Rule public ExpectedException _exception = ExpectedException.none();

  private org.batfish.datamodel.Interface.Builder _ib;

  private NetworkFactory _nf;

  private org.batfish.datamodel.Vrf.Builder _vb;

  interface NetworkGenerator {
    SortedMap<String, Configuration> generateConfigs(boolean delta);
  }

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(CISCO_IOS);
    _vb = _nf.vrfBuilder().setName(DEFAULT_VRF_NAME);
    _ib = _nf.interfaceBuilder();
  }

  private Batfish initBatfish(NetworkGenerator generator) throws IOException {
    SortedMap<String, Configuration> baseConfigs = generator.generateConfigs(false);
    SortedMap<String, Configuration> deltaConfigs = generator.generateConfigs(true);
    Batfish batfish = getBatfish(baseConfigs, deltaConfigs, _folder);

    batfish.pushBaseEnvironment();
    batfish.computeDataPlane(true);
    batfish.popEnvironment();

    batfish.pushDeltaEnvironment();
    batfish.computeDataPlane(true);
    batfish.popEnvironment();

    return batfish;
  }

  private static void checkDispositions(
      Batfish batfish, Set<Flow> flows, FlowDisposition disposition) {

    batfish.pushBaseEnvironment();
    batfish.processFlows(flows, false);
    List<FlowTrace> traces =
        batfish.getDataPlanePlugin().getHistoryFlowTraces(batfish.loadDataPlane());
    assertThat(
        String.format("all traces should have disposition %s in the base environment", disposition),
        traces.stream().allMatch(flowTrace -> flowTrace.getDisposition().equals(disposition)));
    batfish.popEnvironment();

    batfish.pushDeltaEnvironment();
    batfish.processFlows(flows, false);
    traces = batfish.getDataPlanePlugin().getHistoryFlowTraces(batfish.loadDataPlane());
    assertThat(
        String.format("no traces should have disposition %s in the delta environment", disposition),
        traces.stream().noneMatch(flowTrace -> flowTrace.getDisposition().equals(disposition)));
    batfish.popEnvironment();
  }

  class NeighborUnreachableNetworkGenerator implements NetworkGenerator {
    @Override
    public SortedMap<String, Configuration> generateConfigs(boolean delta) {
      Configuration node1 = _cb.setHostname(NODE1).build();
      Vrf v1 = _vb.setOwner(node1).build();
      _ib.setOwner(node1).setVrf(v1);
      _ib.setName(PHYSICAL).setAddresses(NODE1_PHYSICAL_NETWORK).build();
      if (!delta) {
        v1.setStaticRoutes(
            ImmutableSortedSet.of(
                StaticRoute.builder()
                    .setNetwork(new Prefix(DST_IP, 32))
                    .setNextHopInterface(PHYSICAL)
                    .setAdministrativeCost(1)
                    .build()));
      }
      return ImmutableSortedMap.of(NODE1, node1);
    }
  }

  @Test
  public void testHeaderSpace() throws IOException {
    Batfish batfish = initBatfish(new NeighborUnreachableNetworkGenerator());

    _exception.expect(IllegalArgumentException.class);
    _exception.expectMessage("No sources are compatible with the headerspace constraint");
    batfish.bddReducedReachability(
        ImmutableSet.of(NEIGHBOR_UNREACHABLE),
        batfish.getAllSourcesInferFromLocationIpSpaceAssignment(),
        matchSrcIp("7.7.7.7"));
  }

  class NeighborUnreachableNetworkGenerator implements NetworkGenerator {
    @Override
    public SortedMap<String, Configuration> generateConfigs(boolean delta) {
      Configuration node1 = _cb.setHostname(NODE1).build();
      Vrf v1 = _vb.setOwner(node1).build();
      _ib.setOwner(node1).setVrf(v1);
      _ib.setName(PHYSICAL).setAddresses(NODE1_PHYSICAL_NETWORK).build();
      if (!delta) {
        v1.setStaticRoutes(
            ImmutableSortedSet.of(
                StaticRoute.builder()
                    .setNetwork(new Prefix(DST_IP, 32))
                    .setNextHopInterface(PHYSICAL)
                    .setAdministrativeCost(1)
                    .build()));
      }
      return ImmutableSortedMap.of(NODE1, node1);
    }
  }

  @Test
  public void testNeighborUnreachable() throws IOException {
    Batfish batfish = initBatfish(new NeighborUnreachableNetworkGenerator());
    DifferentialReachabilityResult differentialReachabilityResult =
        batfish.bddReducedReachability(
            ImmutableSet.of(NEIGHBOR_UNREACHABLE_OR_EXITS_NETWORK),
            batfish.getAllSourcesInferFromLocationIpSpaceAssignment(),
            TRUE);
    assertThat(differentialReachabilityResult.getIncreasedReachabilityFlows(), empty());
    Set<Flow> flows = differentialReachabilityResult.getDecreasedReachabilityFlows();
    assertThat(flows, hasSize(2));
    assertThat(
        flows,
        containsInAnyOrder(
            ImmutableList.of(
                allOf(hasDstIp(DST_IP), hasSrcIp(NODE1_PHYSICAL_IP)),
                allOf(hasDstIp(DST_IP), hasSrcIp(NODE1_PHYSICAL_LINK_IP)))));
    checkDispositions(batfish, flows, EXITS_NETWORK);
  }

  // TODO
  /*
  @Test
  public void testDeliveredToSubnet() throws IOException {
    Batfish batfish = initBatfish(new ExitNetworkGenerator());
    Set<Flow> flows = batfish.bddReducedReachability(ImmutableSet.of(DELIVERED_TO_SUBNET));
    assertThat(flows, hasSize(0));
  }

  // TODO
  @Test
  public void testExitsNetwork() throws IOException {
    Batfish batfish = initBatfish(new ExitNetworkGenerator());
    Set<Flow> flows = batfish.bddReducedReachability(ImmutableSet.of(EXITS_NETWORK));
    assertThat(flows, hasSize(2));
    assertThat(
        flows,
        containsInAnyOrder(
            ImmutableList.of(
                allOf(hasDstIp(DST_IP), hasSrcIp(NODE1_PHYSICAL_IP)),
                allOf(hasDstIp(DST_IP), hasSrcIp(NODE1_PHYSICAL_LINK_IP)))));
    checkDispositions(batfish, flows, EXITS_NETWORK);
  }
  */

  class AcceptedNetworkGenerator implements NetworkGenerator {
    @Override
    public SortedMap<String, Configuration> generateConfigs(boolean delta) {
      Configuration node1 = _cb.setHostname(NODE1).build();
      Vrf v1 = _vb.setOwner(node1).build();
      _ib.setOwner(node1).setVrf(v1);
      _ib.setName(PHYSICAL).setAddresses(NODE1_PHYSICAL_NETWORK).build();
      if (!delta) {
        v1.setStaticRoutes(
            ImmutableSortedSet.of(
                StaticRoute.builder()
                    .setNetwork(new Prefix(DST_IP, 32))
                    .setNextHopInterface(PHYSICAL)
                    .setAdministrativeCost(1)
                    .build()));
      }

      Configuration node2 = _cb.setHostname(NODE2).build();
      Vrf v2 = _vb.setOwner(node2).build();
      _ib.setOwner(node2).setVrf(v2);
      _ib.setName(PHYSICAL)
          .setAddresses(NODE2_PHYSICAL_NETWORK, new InterfaceAddress(DST_IP, 32))
          .build();

      return ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
    }
  }

  @Test
  public void testAccepted() throws IOException {
    Batfish batfish = initBatfish(new AcceptedNetworkGenerator());
    DifferentialReachabilityResult differentialReachabilityResult =
        batfish.bddReducedReachability(
            ImmutableSet.of(FlowDisposition.ACCEPTED),
            batfish.getAllSourcesInferFromLocationIpSpaceAssignment(),
            TRUE);
    assertThat(differentialReachabilityResult.getIncreasedReachabilityFlows(), empty());
    Set<Flow> flows = differentialReachabilityResult.getDecreasedReachabilityFlows();
    assertThat(flows, hasSize(2));
    assertThat(
        flows,
        containsInAnyOrder(
            ImmutableList.of(
                allOf(hasDstIp(DST_IP), hasSrcIp(NODE1_PHYSICAL_IP)),
                allOf(hasDstIp(DST_IP), hasSrcIp(NODE1_PHYSICAL_LINK_IP)))));
    checkDispositions(batfish, flows, ACCEPTED);
  }

  class DeniedInNetworkGenerator implements NetworkGenerator {
    @Override
    public SortedMap<String, Configuration> generateConfigs(boolean delta) {
      Configuration node1 = _cb.setHostname(NODE1).build();
      Vrf v1 = _vb.setOwner(node1).build();
      _ib.setOwner(node1).setVrf(v1);
      _ib.setName(PHYSICAL).setAddresses(NODE1_PHYSICAL_NETWORK).build();
      v1.setStaticRoutes(
          ImmutableSortedSet.of(
              StaticRoute.builder()
                  .setNetwork(new Prefix(DST_IP, 32))
                  .setNextHopInterface(PHYSICAL)
                  .setAdministrativeCost(1)
                  .build()));

      Configuration node2 = _cb.setHostname(NODE2).build();
      Vrf v2 = _vb.setOwner(node2).build();
      _ib.setOwner(node2).setVrf(v2);
      if (!delta) {
        IpAccessList acl =
            _nf.aclBuilder()
                .setOwner(node2)
                .setLines(
                    ImmutableList.of(
                        IpAccessListLine.rejectingHeaderSpace(
                            HeaderSpace.builder().setDstIps(DST_IP.toIpSpace()).build()),
                        IpAccessListLine.ACCEPT_ALL))
                .build();
        _ib.setIncomingFilter(acl);
      }
      _ib.setName(PHYSICAL)
          .setAddresses(NODE2_PHYSICAL_NETWORK, new InterfaceAddress(DST_IP, 32))
          .build();
      _ib.setIncomingFilter(null);

      return ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
    }
  }

  @Test
  public void testDeniedIn() throws IOException {
    Batfish batfish = initBatfish(new DeniedInNetworkGenerator());
    DifferentialReachabilityResult differentialReachabilityResult =
        batfish.bddReducedReachability(
            ImmutableSet.of(DENIED_IN),
            batfish.getAllSourcesInferFromLocationIpSpaceAssignment(),
            TRUE);
    assertThat(differentialReachabilityResult.getIncreasedReachabilityFlows(), empty());
    Set<Flow> flows = differentialReachabilityResult.getDecreasedReachabilityFlows();
    assertThat(flows, hasSize(3));
    assertThat(
        flows,
        containsInAnyOrder(
            ImmutableList.of(
                allOf(
                    hasDstIp(DST_IP),
                    hasIngressNode(NODE1),
                    hasIngressVrf(DEFAULT_VRF_NAME),
                    hasSrcIp(NODE1_PHYSICAL_IP)),
                allOf(
                    hasDstIp(DST_IP),
                    hasIngressNode(NODE1),
                    hasIngressInterface(PHYSICAL),
                    hasSrcIp(NODE1_PHYSICAL_LINK_IP)),
                allOf(
                    hasDstIp(DST_IP),
                    hasIngressNode(NODE2),
                    hasIngressInterface(PHYSICAL),
                    hasSrcIp(NODE2_PHYSICAL_IP)))));
    checkDispositions(batfish, flows, DENIED_IN);
  }

  /*
   * In the base network, an outgoing filter drops traffic that would otherwise have disposition
   * neighbor unreachable.
   */
  class DeniedOutNeighborUnreachableNetworkGenerator implements NetworkGenerator {
    @Override
    public SortedMap<String, Configuration> generateConfigs(boolean delta) {
      Configuration node1 = _cb.setHostname(NODE1).build();
      Vrf v1 = _vb.setOwner(node1).build();
      _ib.setOwner(node1).setVrf(v1);
      if (!delta) {
        IpAccessList acl =
            _nf.aclBuilder()
                .setOwner(node1)
                .setLines(
                    ImmutableList.of(
                        IpAccessListLine.rejectingHeaderSpace(
                            HeaderSpace.builder().setDstIps(DST_IP.toIpSpace()).build()),
                        IpAccessListLine.ACCEPT_ALL))
                .build();
        _ib.setOutgoingFilter(acl);
      }
      _ib.setName(PHYSICAL).setAddresses(NODE1_PHYSICAL_NETWORK).build();
      _ib = _nf.interfaceBuilder();
      v1.setStaticRoutes(
          ImmutableSortedSet.of(
              StaticRoute.builder()
                  .setNetwork(new Prefix(DST_IP, 32))
                  .setNextHopInterface(PHYSICAL)
                  .setAdministrativeCost(1)
                  .build()));
      return ImmutableSortedMap.of(NODE1, node1);
    }
  }

  @Test
  public void testDeniedOutNeighborUnreachable() throws IOException {
    Batfish batfish = initBatfish(new DeniedOutNeighborUnreachableNetworkGenerator());
    DifferentialReachabilityResult differentialReachabilityResult =
        batfish.bddReducedReachability(
            ImmutableSet.of(DENIED_OUT),
            batfish.getAllSourcesInferFromLocationIpSpaceAssignment(),
            TRUE);
    assertThat(differentialReachabilityResult.getIncreasedReachabilityFlows(), empty());
    Set<Flow> flows = differentialReachabilityResult.getDecreasedReachabilityFlows();
    assertThat(flows, hasSize(2));
    assertThat(
        flows,
        containsInAnyOrder(
            ImmutableList.of(
                allOf(
                    hasDstIp(DST_IP),
                    hasIngressNode(NODE1),
                    hasIngressVrf(DEFAULT_VRF_NAME),
                    hasSrcIp(NODE1_PHYSICAL_IP)),
                allOf(
                    hasDstIp(DST_IP),
                    hasIngressNode(NODE1),
                    hasIngressInterface(PHYSICAL),
                    hasSrcIp(NODE1_PHYSICAL_LINK_IP)))));
    checkDispositions(batfish, flows, DENIED_OUT);
  }

  /*
   * In the base network, an outgoing filter drops traffic that would otherwise be forwarded to the
   * neighbor.
   */
  class DeniedOutForwardNetworkGenerator implements NetworkGenerator {
    @Override
    public SortedMap<String, Configuration> generateConfigs(boolean delta) {
      Configuration node1 = _cb.setHostname(NODE1).build();
      Vrf v1 = _vb.setOwner(node1).build();
      _ib.setOwner(node1).setVrf(v1);
      if (!delta) {
        IpAccessList acl =
            _nf.aclBuilder()
                .setOwner(node1)
                .setLines(
                    ImmutableList.of(
                        IpAccessListLine.rejectingHeaderSpace(
                            HeaderSpace.builder().setDstIps(DST_IP.toIpSpace()).build()),
                        IpAccessListLine.ACCEPT_ALL))
                .build();
        _ib.setOutgoingFilter(acl);
      }
      _ib.setName(PHYSICAL).setAddresses(NODE1_PHYSICAL_NETWORK).build();
      _ib = _nf.interfaceBuilder();

      v1.setStaticRoutes(
          ImmutableSortedSet.of(
              StaticRoute.builder()
                  .setNetwork(new Prefix(DST_IP, 32))
                  .setNextHopInterface(PHYSICAL)
                  .setAdministrativeCost(1)
                  .build()));

      Configuration node2 = _cb.setHostname(NODE2).build();
      Vrf v2 = _vb.setOwner(node2).build();
      _ib.setOwner(node2).setVrf(v2);
      _ib.setName(PHYSICAL)
          .setAddresses(NODE2_PHYSICAL_NETWORK, new InterfaceAddress(DST_IP, 32))
          .build();

      return ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
    }
  }

  @Test
  public void testDeniedOutForward() throws IOException {
    Batfish batfish = initBatfish(new DeniedOutForwardNetworkGenerator());
    DifferentialReachabilityResult differentialReachabilityResult =
        batfish.bddReducedReachability(
            ImmutableSet.of(DENIED_OUT),
            batfish.getAllSourcesInferFromLocationIpSpaceAssignment(),
            TRUE);
    assertThat(differentialReachabilityResult.getIncreasedReachabilityFlows(), empty());
    Set<Flow> flows = differentialReachabilityResult.getDecreasedReachabilityFlows();
    assertThat(flows, hasSize(2));
    assertThat(
        flows,
        containsInAnyOrder(
            ImmutableList.of(
                allOf(
                    hasDstIp(DST_IP),
                    hasIngressNode(NODE1),
                    hasIngressVrf(DEFAULT_VRF_NAME),
                    hasSrcIp(NODE1_PHYSICAL_IP)),
                allOf(
                    hasDstIp(DST_IP),
                    hasIngressNode(NODE1),
                    hasIngressInterface(PHYSICAL),
                    hasSrcIp(NODE1_PHYSICAL_LINK_IP)))));
    checkDispositions(batfish, flows, DENIED_OUT);
  }

  class NoRouteNetworkGenerator implements NetworkGenerator {
    @Override
    public SortedMap<String, Configuration> generateConfigs(boolean delta) {
      Configuration node1 = _cb.setHostname(NODE1).build();
      Vrf v1 = _vb.setOwner(node1).build();
      _ib.setOwner(node1).setVrf(v1);
      _ib.setName(PHYSICAL).setAddresses(NODE1_PHYSICAL_NETWORK).build();
      _ib = _nf.interfaceBuilder();

      if (delta) {
        v1.setStaticRoutes(
            ImmutableSortedSet.of(
                StaticRoute.builder()
                    .setNetwork(new Prefix(DST_IP, 32))
                    .setNextHopInterface(PHYSICAL)
                    .setAdministrativeCost(1)
                    .build()));
      }

      Configuration node2 = _cb.setHostname(NODE2).build();
      Vrf v2 = _vb.setOwner(node2).build();
      _ib.setOwner(node2).setVrf(v2);
      _ib.setName(PHYSICAL)
          .setAddresses(NODE2_PHYSICAL_NETWORK, new InterfaceAddress(DST_IP, 32))
          .build();

      return ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
    }
  }

  @Test
  public void testNoRoute() throws IOException {
    Batfish batfish = initBatfish(new NoRouteNetworkGenerator());
    DifferentialReachabilityResult differentialReachabilityResult =
        batfish.bddReducedReachability(
            ImmutableSet.of(NO_ROUTE),
            batfish.getAllSourcesInferFromLocationIpSpaceAssignment(),
            TRUE);
    assertThat(differentialReachabilityResult.getIncreasedReachabilityFlows(), empty());
    Set<Flow> flows = differentialReachabilityResult.getDecreasedReachabilityFlows();
    assertThat(flows, hasSize(2));
    assertThat(
        flows,
        containsInAnyOrder(
            ImmutableList.of(
                allOf(
                    hasDstIp(DST_IP),
                    hasIngressNode(NODE1),
                    hasIngressVrf(DEFAULT_VRF_NAME),
                    hasSrcIp(NODE1_PHYSICAL_IP)),
                allOf(
                    hasDstIp(DST_IP),
                    hasIngressNode(NODE1),
                    hasIngressInterface(PHYSICAL),
                    hasSrcIp(NODE1_PHYSICAL_LINK_IP)))));
    checkDispositions(batfish, flows, NO_ROUTE);
  }

  class NullRoutedNetworkGenerator implements NetworkGenerator {
    @Override
    public SortedMap<String, Configuration> generateConfigs(boolean delta) {
      Configuration node1 = _cb.setHostname(NODE1).build();
      Vrf v1 = _vb.setOwner(node1).build();
      _ib.setOwner(node1).setVrf(v1);
      _ib.setName(PHYSICAL).setAddresses(NODE1_PHYSICAL_NETWORK).build();
      _ib = _nf.interfaceBuilder();

      if (!delta) {
        v1.setStaticRoutes(
            ImmutableSortedSet.of(
                StaticRoute.builder()
                    .setNetwork(new Prefix(DST_IP, 32))
                    .setNextHopInterface(NULL_INTERFACE_NAME)
                    .setAdministrativeCost(1)
                    .build()));
      }

      Configuration node2 = _cb.setHostname(NODE2).build();
      Vrf v2 = _vb.setOwner(node2).build();
      _ib.setOwner(node2).setVrf(v2);
      _ib.setName(PHYSICAL)
          .setAddresses(NODE2_PHYSICAL_NETWORK, new InterfaceAddress(DST_IP, 32))
          .build();

      return ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
    }
  }

  @Test
  public void testNullRouted() throws IOException {
    Batfish batfish = initBatfish(new NullRoutedNetworkGenerator());
    DifferentialReachabilityResult differentialReachabilityResult =
        batfish.bddReducedReachability(
            ImmutableSet.of(NULL_ROUTED),
            batfish.getAllSourcesInferFromLocationIpSpaceAssignment(),
            TRUE);
    assertThat(differentialReachabilityResult.getIncreasedReachabilityFlows(), empty());
    Set<Flow> flows = differentialReachabilityResult.getDecreasedReachabilityFlows();
    assertThat(flows, hasSize(2));
    assertThat(
        flows,
        containsInAnyOrder(
            ImmutableList.of(
                allOf(
                    hasDstIp(DST_IP),
                    hasIngressNode(NODE1),
                    hasIngressVrf(DEFAULT_VRF_NAME),
                    hasSrcIp(NODE1_PHYSICAL_IP)),
                allOf(
                    hasDstIp(DST_IP),
                    hasIngressNode(NODE1),
                    hasIngressInterface(PHYSICAL),
                    hasSrcIp(NODE1_PHYSICAL_LINK_IP)))));
    checkDispositions(batfish, flows, NULL_ROUTED);
  }
}
