package org.batfish.bddreachability;

import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.INSUFFICIENT_INFO;
import static org.batfish.datamodel.FlowDisposition.NEIGHBOR_UNREACHABLE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.main.BatfishTestUtils.getBatfish;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Stream;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DataPlane;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.batfish.main.Batfish;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.IpSpaceAssignment;
import org.batfish.specifier.Location;
import org.batfish.symbolic.IngressLocation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Test the dispostions when ARP failures occur. */
public class BDDReachabilityAnalysisArpFailureDispositionsTest {
  private final BDDPacket _pkt = new BDDPacket();
  private final IpSpaceToBDD _dstToBdd = _pkt.getDstIpSpaceToBDD();
  private static final ImmutableSet<FlowDisposition> DISPOSITIONS =
      ImmutableSet.of(DELIVERED_TO_SUBNET, EXITS_NETWORK, INSUFFICIENT_INFO, NEIGHBOR_UNREACHABLE);

  public @Rule TemporaryFolder temp = new TemporaryFolder();

  private static final Ip DST_IP = Ip.parse("3.3.3.3");
  private static final String NODE1 = "node1";
  private static final String NODE2 = "node2";
  private static final String NEXT_HOP_INTERFACE = "iface1";
  private static final String LOOPBACK_INTERFACE = "iface2";
  private static final ConcreteInterfaceAddress NEXT_HOP_INTERFACE_NOT_FULL_ADDR =
      ConcreteInterfaceAddress.parse("2.0.0.1/30");
  private static final ConcreteInterfaceAddress NEXT_HOP_INTERFACE_FULL_ADDR =
      ConcreteInterfaceAddress.parse("2.0.0.1/32");
  private static final ConcreteInterfaceAddress NEXT_HOP_INTERFACE_NEIGHBOR_ADDR =
      ConcreteInterfaceAddress.parse("2.0.0.2/32");

  private static final Ip NEXT_HOP_IP = Ip.parse("7.7.7.7");

  private NetworkFactory _nf;
  private Configuration.Builder _cb;
  private Vrf.Builder _vb;
  private Interface.Builder _ib;
  private SortedMap<String, Configuration> _configs;
  IngressLocation _loc;

  private BDD dstIpToBDD(Ip dstIp) {
    return _dstToBdd.toBDD(dstIp);
  }

  private Batfish initBatfish(SortedMap<String, Configuration> configs) throws IOException {
    Batfish batfish = getBatfish(configs, temp);
    batfish.computeDataPlane(batfish.getSnapshot());
    return batfish;
  }

  private BDDReachabilityAnalysisFactory initFactory() throws IOException {
    Batfish batfish = initBatfish(_configs);
    DataPlane dataPlane = batfish.loadDataPlane(batfish.getSnapshot());
    return new BDDReachabilityAnalysisFactory(
        _pkt,
        _configs,
        dataPlane.getForwardingAnalysis(),
        new IpsRoutedOutInterfacesFactory(dataPlane.getFibs()),
        false,
        false);
  }

  private BDDReachabilityAnalysis initAnalysis(FlowDisposition disposition) throws IOException {
    return initFactory()
        .bddReachabilityAnalysis(
            srcIpSpaceAssignment(),
            matchDst(UniverseIpSpace.INSTANCE),
            ImmutableSet.of(),
            ImmutableSet.of(),
            ImmutableSet.of(NODE1),
            ImmutableSet.of(disposition));
  }

  private IpSpaceAssignment srcIpSpaceAssignment() {
    Set<Location> locations =
        _configs.get(NODE1).getAllInterfaces(Configuration.DEFAULT_VRF_NAME).values().stream()
            .limit(1)
            .map(iface -> new InterfaceLocation(iface.getOwner().getHostname(), iface.getName()))
            .collect(ImmutableSet.toImmutableSet());
    return IpSpaceAssignment.builder().assign(locations, UniverseIpSpace.INSTANCE).build();
  }

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _vb = _nf.vrfBuilder();
    _ib = _nf.interfaceBuilder();
    _loc = IngressLocation.vrf(NODE1, Configuration.DEFAULT_VRF_NAME);
  }

  private void checkDispositionsDisjoint() {
    Map<FlowDisposition, BDD> dispositionBDDs =
        toImmutableMap(
            DISPOSITIONS,
            Function.identity(),
            disposition -> {
              try {
                return initAnalysis(disposition).getIngressLocationReachableBDDs().get(_loc);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
    dispositionBDDs.forEach(
        (disposition1, bdd1) ->
            dispositionBDDs.forEach(
                (disposition2, bdd2) -> {
                  if (disposition1 == disposition2) {
                    return;
                  }
                  assertFalse(
                      String.format(
                          "dispositions %s and %s should not intersect",
                          disposition1, disposition2),
                      bdd1.andSat(bdd2));
                }));
  }

  private void setup_dstInSubnet_dstOwned() {
    Configuration node1 = _cb.setHostname(NODE1).build();
    Vrf vrf = _vb.setOwner(node1).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(node1).setVrf(vrf);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_NOT_FULL_ADDR).build();

    // setup a loopback on NODE2 that owns the neighbor IP
    Configuration node2 = _cb.setHostname(NODE2).build();
    Vrf vrf2 = _vb.setOwner(node2).build();
    _ib.setOwner(node2)
        .setVrf(vrf2)
        .setName(LOOPBACK_INTERFACE)
        .setAddress(NEXT_HOP_INTERFACE_NEIGHBOR_ADDR)
        .build();

    _configs = ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
  }

  @Test
  public void dstInSubnet_dstOwned() throws IOException {
    setup_dstInSubnet_dstOwned();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(NEIGHBOR_UNREACHABLE);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    BDD neighborUnreachableIps =
        Stream.of(
                NEXT_HOP_INTERFACE_NEIGHBOR_ADDR.getIp(),
                /*
                 * Note: sometimes the network and broadcast IPs are EXITS_NETWORK, sometimes
                 * NEIGHBOR_UNREACHABLE. We don't really care what disposition these have, and at
                 * some point we will disallow using them as dst IPs in reachability or traceroute.
                 */
                NEXT_HOP_INTERFACE_NOT_FULL_ADDR.getPrefix().getStartIp(),
                NEXT_HOP_INTERFACE_NOT_FULL_ADDR.getPrefix().getEndIp())
            .map(this::dstIpToBDD)
            .reduce(_pkt.getFactory().zero(), BDD::or);
    assertThat(reach, hasEntry(_loc, neighborUnreachableIps));
  }

  private void setup_dstInSubnet_dstUnowned() {
    Configuration node1 = _cb.setHostname(NODE1).build();
    Vrf vrf = _vb.setOwner(node1).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(node1).setVrf(vrf);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_NOT_FULL_ADDR).build();
    _configs = ImmutableSortedMap.of(NODE1, node1);
  }

  @Test
  public void dstInSubnet_dstUnowned() throws IOException {
    setup_dstInSubnet_dstUnowned();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(DELIVERED_TO_SUBNET);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    assertThat(reach, hasEntry(_loc, dstIpToBDD(NEXT_HOP_INTERFACE_NEIGHBOR_ADDR.getIp())));
  }

  private void setup_noNHIp_subnetFull_internalDstIp() {
    Configuration node1 = _cb.setHostname(NODE1).build();
    Vrf vrf = _vb.setOwner(node1).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(node1).setVrf(vrf);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_FULL_ADDR).build();

    // setup a loopback on NODE2 that owns DST_IP
    Configuration node2 = _cb.setHostname(NODE2).build();
    Vrf vrf2 = _vb.setOwner(node2).build();
    _ib.setOwner(node2)
        .setVrf(vrf2)
        .setName(LOOPBACK_INTERFACE)
        .setAddress(ConcreteInterfaceAddress.create(DST_IP, 32))
        .build();

    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopInterface(NEXT_HOP_INTERFACE)
                .setAdministrativeCost(1)
                .build()));
    _configs = ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
  }

  @Test
  public void noNHIp_subnetFull_internalDstIp() throws IOException {
    setup_noNHIp_subnetFull_internalDstIp();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(NEIGHBOR_UNREACHABLE);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    assertThat(reach, hasEntry(_loc, dstIpToBDD(DST_IP)));
  }

  private void setup_noNHIp_subnetFull_externalDstIp() {
    Configuration config = _cb.setHostname(NODE1).build();
    Vrf vrf = _vb.setOwner(config).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(config).setVrf(vrf);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_FULL_ADDR).build();
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopInterface(NEXT_HOP_INTERFACE)
                .setAdministrativeCost(1)
                .build()));
    _configs = ImmutableSortedMap.of(NODE1, config);
  }

  @Test
  public void noNHIp_subnetFull_externalDstIp() throws IOException {
    setup_noNHIp_subnetFull_externalDstIp();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(NEIGHBOR_UNREACHABLE);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    assertThat(reach, hasEntry(_loc, dstIpToBDD(DST_IP)));
  }

  private void setup_noNHIp_subnetNotFull_internalDstIp() {
    Configuration node1 = _cb.setHostname(NODE1).build();
    Vrf vrf = _vb.setOwner(node1).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(node1).setVrf(vrf);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_NOT_FULL_ADDR).build();

    // setup a loopback on NODE2 that owns DST_IP
    Configuration node2 = _cb.setHostname(NODE2).build();
    Vrf vrf2 = _vb.setOwner(node2).build();
    _ib.setOwner(node2)
        .setVrf(vrf2)
        .setName(LOOPBACK_INTERFACE)
        .setAddress(ConcreteInterfaceAddress.create(DST_IP, 32))
        .build();

    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopInterface(NEXT_HOP_INTERFACE)
                .setAdministrativeCost(1)
                .build()));
    _configs = ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
  }

  @Test
  public void noNHIp_subnetNotFull_internalDstIp() throws IOException {
    setup_noNHIp_subnetNotFull_internalDstIp();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(INSUFFICIENT_INFO);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    assertThat(reach, hasEntry(_loc, dstIpToBDD(DST_IP)));
  }

  private void setup_noNHIp_subnetNotFull_externalDstIp() {
    Configuration config = _cb.setHostname(NODE1).build();
    Vrf vrf = _vb.setOwner(config).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(config).setVrf(vrf);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_NOT_FULL_ADDR).build();
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopInterface(NEXT_HOP_INTERFACE)
                .setAdministrativeCost(1)
                .build()));
    _configs = ImmutableSortedMap.of(NODE1, config);
  }

  @Test
  public void noNHIp_subnetNotFull_externalDstIp() throws IOException {
    setup_noNHIp_subnetNotFull_externalDstIp();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(EXITS_NETWORK);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    BDD exitsNetworkIps =
        Stream.of(
                DST_IP,
                NEXT_HOP_INTERFACE_NOT_FULL_ADDR.getPrefix().getStartIp(),
                NEXT_HOP_INTERFACE_NOT_FULL_ADDR.getPrefix().getEndIp())
            .map(this::dstIpToBDD)
            .reduce(_pkt.getFactory().zero(), BDD::or);
    assertThat(reach, hasEntry(_loc, exitsNetworkIps));
  }

  private void setup_externalNHIp_subnetFull_internalDstIp() {
    Configuration node1 = _cb.setHostname(NODE1).build();
    Vrf vrf = _vb.setOwner(node1).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(node1).setVrf(vrf);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_FULL_ADDR).build();

    // setup a loopback on NODE2 that owns DST_IP
    Configuration node2 = _cb.setHostname(NODE2).build();
    Vrf vrf2 = _vb.setOwner(node2).build();
    _ib.setOwner(node2)
        .setVrf(vrf2)
        .setName(LOOPBACK_INTERFACE)
        .setAddress(ConcreteInterfaceAddress.create(DST_IP, 32))
        .build();

    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopInterface(NEXT_HOP_INTERFACE)
                .setNextHopIp(NEXT_HOP_IP)
                .setAdministrativeCost(1)
                .build()));
    _configs = ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
  }

  @Test
  public void externalNHIp_subnetFull_internalDstIp() throws IOException {
    setup_externalNHIp_subnetFull_internalDstIp();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(NEIGHBOR_UNREACHABLE);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    assertThat(reach, hasEntry(_loc, dstIpToBDD(DST_IP)));
  }

  private void setup_externalNHIp_subnetFull_externalDstIp() {
    Configuration node1 = _cb.setHostname(NODE1).build();
    Vrf vrf = _vb.setOwner(node1).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(node1).setVrf(vrf);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_FULL_ADDR).build();

    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopInterface(NEXT_HOP_INTERFACE)
                .setNextHopIp(NEXT_HOP_IP)
                .setAdministrativeCost(1)
                .build()));
    _configs = ImmutableSortedMap.of(NODE1, node1);
  }

  @Test
  public void externalNHIp_subnetFull_externalDstIp() throws IOException {
    setup_externalNHIp_subnetFull_externalDstIp();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(NEIGHBOR_UNREACHABLE);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    assertThat(reach, hasEntry(_loc, dstIpToBDD(DST_IP)));
  }

  private void setup_externalNHIp_subnetNotFull_internalDstIp() {
    /*
     * dst IP is internal, next hop IP is external.
     */

    Configuration node1 = _cb.setHostname(NODE1).build();
    Vrf vrf1 = _vb.setOwner(node1).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(node1).setVrf(vrf1);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_NOT_FULL_ADDR).build();

    // setup a loopback on NODE2 that owns DST_IP
    Configuration node2 = _cb.setHostname(NODE2).build();
    Vrf vrf2 = _vb.setOwner(node2).build();
    _ib.setOwner(node2)
        .setVrf(vrf2)
        .setName(LOOPBACK_INTERFACE)
        .setAddress(ConcreteInterfaceAddress.create(DST_IP, 32))
        .build();

    vrf1.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopInterface(NEXT_HOP_INTERFACE)
                .setNextHopIp(NEXT_HOP_IP)
                .setAdministrativeCost(1)
                .build()));
    _configs = ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
  }

  @Test
  public void externalNHIp_subnetNotFull_internalDstIp() throws IOException {
    setup_externalNHIp_subnetNotFull_internalDstIp();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(INSUFFICIENT_INFO);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    BDD neighborUnreachableIps = dstIpToBDD(DST_IP);
    assertThat(reach, hasEntry(_loc, neighborUnreachableIps));
  }

  private void setup_externalNHIp_subnetNotFull_externalDstIp() {
    Configuration config = _cb.setHostname(NODE1).build();
    Vrf vrf = _vb.setOwner(config).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(config).setVrf(vrf);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_NOT_FULL_ADDR).build();
    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopInterface(NEXT_HOP_INTERFACE)
                .setNextHopIp(NEXT_HOP_IP)
                .setAdministrativeCost(1)
                .build()));
    _configs = ImmutableSortedMap.of(NODE1, config);
  }

  @Test
  public void externalNHIp_subnetNotFull_externalDstIp() throws IOException {
    setup_externalNHIp_subnetNotFull_externalDstIp();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(EXITS_NETWORK);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    BDD exitsNetworkIps =
        Stream.of(
                DST_IP,
                NEXT_HOP_INTERFACE_NOT_FULL_ADDR.getPrefix().getStartIp(),
                NEXT_HOP_INTERFACE_NOT_FULL_ADDR.getPrefix().getEndIp())
            .map(this::dstIpToBDD)
            .reduce(_pkt.getFactory().zero(), BDD::or);
    assertThat(reach, hasEntry(_loc, exitsNetworkIps));
  }

  private void setup_internalNHIp_subnetFull_internalDstIp() {
    Configuration node1 = _cb.setHostname(NODE1).build();
    Vrf vrf = _vb.setOwner(node1).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(node1).setVrf(vrf);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_FULL_ADDR).build();

    // setup a loopback on NODE2 that owns NEXT_HOP_IP and DST_IP
    Configuration node2 = _cb.setHostname(NODE2).build();
    Vrf vrf2 = _vb.setOwner(node2).build();
    _ib.setOwner(node2)
        .setVrf(vrf2)
        .setName(LOOPBACK_INTERFACE)
        .setAddresses(
            ConcreteInterfaceAddress.create(DST_IP, 32),
            ConcreteInterfaceAddress.create(NEXT_HOP_IP, 32))
        .build();

    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopInterface(NEXT_HOP_INTERFACE)
                .setNextHopIp(NEXT_HOP_IP)
                .setAdministrativeCost(1)
                .build()));
    _configs = ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
  }

  @Test
  public void internalNHIp_subnetFull_internalDstIp() throws IOException {
    setup_internalNHIp_subnetFull_internalDstIp();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(NEIGHBOR_UNREACHABLE);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    assertThat(reach, hasEntry(_loc, dstIpToBDD(DST_IP)));
  }

  private void setup_internalNHIp_subnetFull_externalDstIp() {
    Configuration node1 = _cb.setHostname(NODE1).build();
    Vrf vrf = _vb.setOwner(node1).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(node1).setVrf(vrf);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_FULL_ADDR).build();

    // setup a loopback on NODE2 that owns NEXT_HOP_IP
    Configuration node2 = _cb.setHostname(NODE2).build();
    Vrf vrf2 = _vb.setOwner(node2).build();
    _ib.setOwner(node2)
        .setVrf(vrf2)
        .setName(LOOPBACK_INTERFACE)
        .setAddresses(ConcreteInterfaceAddress.create(NEXT_HOP_IP, 32))
        .build();

    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopInterface(NEXT_HOP_INTERFACE)
                .setNextHopIp(NEXT_HOP_IP)
                .setAdministrativeCost(1)
                .build()));
    _configs = ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
  }

  @Test
  public void internalNHIp_subnetFull_externalDstIp() throws IOException {
    setup_internalNHIp_subnetFull_externalDstIp();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(NEIGHBOR_UNREACHABLE);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    assertThat(reach, hasEntry(_loc, dstIpToBDD(DST_IP)));
  }

  private void setup_internalNHIp_subnetNotFull_internalDstIp() {
    Configuration node1 = _cb.setHostname(NODE1).build();
    Vrf vrf = _vb.setOwner(node1).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(node1).setVrf(vrf);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_NOT_FULL_ADDR).build();

    // setup a loopback on NODE2 that owns NEXT_HOP_IP and DST_IP
    Configuration node2 = _cb.setHostname(NODE2).build();
    Vrf vrf2 = _vb.setOwner(node2).build();
    _ib.setOwner(node2)
        .setVrf(vrf2)
        .setName(LOOPBACK_INTERFACE)
        .setAddresses(
            ConcreteInterfaceAddress.create(DST_IP, 32),
            ConcreteInterfaceAddress.create(NEXT_HOP_IP, 32))
        .build();

    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopInterface(NEXT_HOP_INTERFACE)
                .setNextHopIp(NEXT_HOP_IP)
                .setAdministrativeCost(1)
                .build()));
    _configs = ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
  }

  @Test
  public void internalNHIp_subnetNotFull_internalDstIp() throws IOException {
    setup_internalNHIp_subnetNotFull_internalDstIp();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(INSUFFICIENT_INFO);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    assertThat(reach, hasEntry(_loc, dstIpToBDD(DST_IP)));
  }

  private void setup_internalNHIp_subnetNotFull_externalDstIp() {
    Configuration node1 = _cb.setHostname(NODE1).build();
    Vrf vrf = _vb.setOwner(node1).setName(Configuration.DEFAULT_VRF_NAME).build();
    _ib.setOwner(node1).setVrf(vrf);
    _ib.setName(NEXT_HOP_INTERFACE).setAddresses(NEXT_HOP_INTERFACE_NOT_FULL_ADDR).build();

    vrf.setStaticRoutes(
        ImmutableSortedSet.of(
            StaticRoute.testBuilder()
                .setNetwork(DST_IP.toPrefix())
                .setNextHopInterface(NEXT_HOP_INTERFACE)
                .setNextHopIp(NEXT_HOP_IP)
                .setAdministrativeCost(1)
                .build()));

    // setup a loopback on NODE2 that owns NEXT_HOP_IP
    Configuration node2 = _cb.setHostname(NODE2).build();
    Vrf vrf2 = _vb.setOwner(node2).build();
    _ib.setOwner(node2)
        .setVrf(vrf2)
        .setName(LOOPBACK_INTERFACE)
        .setAddresses(ConcreteInterfaceAddress.create(NEXT_HOP_IP, 32))
        .build();

    _configs = ImmutableSortedMap.of(NODE1, node1, NODE2, node2);
  }

  @Test
  public void internalNHIp_subnetNotFull_externalDstIp() throws IOException {
    setup_internalNHIp_subnetNotFull_externalDstIp();
    checkDispositionsDisjoint();
    BDDReachabilityAnalysis analysis = initAnalysis(INSUFFICIENT_INFO);
    Map<IngressLocation, BDD> reach = analysis.getIngressLocationReachableBDDs();
    assertThat(reach, hasEntry(_loc, dstIpToBDD(DST_IP)));
  }
}
