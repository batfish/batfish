package org.batfish.dataplane.bdp;

import static org.batfish.datamodel.Prefix.MAX_PREFIX_LENGTH;
import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasPrefix;
import static org.batfish.datamodel.matchers.AbstractRouteMatchers.hasProtocol;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.batfish.common.BatfishLogger;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.BdpAnswerElement;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.junit.Before;
import org.junit.Test;

public class BgpLocalRoutesTest {

  private static final int AS_EXPORT_LOCAL = 1;

  private static final int AS_RECEIVE = 2;

  private static final String C_EXPORT_NAME = "export-local";

  private static final String C_RECEIVE_NAME = "receive";

  private static final InterfaceAddress LAN_ADDRESS = new InterfaceAddress("4.4.4.2/30");

  private static final InterfaceAddress LB_ADDRESS = new InterfaceAddress("2.2.2.2/32");

  private static final Prefix PEERING_PREFIX = Prefix.parse("10.0.0.0/31");

  private static final InterfaceAddress PTP_ADDRESS = new InterfaceAddress("3.3.3.1/31");

  private static void assertEbgpRoute(
      SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routesByNode,
      String hostname,
      Prefix prefix) {
    assertThat(routesByNode, hasKey(hostname));
    SortedMap<String, SortedSet<AbstractRoute>> routesByVrf = routesByNode.get(hostname);
    assertThat(routesByVrf, hasKey(Configuration.DEFAULT_VRF_NAME));
    SortedSet<AbstractRoute> routes = routesByVrf.get(Configuration.DEFAULT_VRF_NAME);
    assertThat(routes, hasItem(hasPrefix(prefix)));
    AbstractRoute route =
        routes.stream().filter(r -> r.getNetwork().equals(prefix)).findAny().get();
    assertThat(route, hasProtocol(RoutingProtocol.BGP));
  }

  private static void assertNoRoute(
      SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routesByNode,
      String hostname,
      Prefix prefix) {
    assertThat(routesByNode, hasKey(hostname));
    SortedMap<String, SortedSet<AbstractRoute>> routesByVrf = routesByNode.get(hostname);
    assertThat(routesByVrf, hasKey(Configuration.DEFAULT_VRF_NAME));
    SortedSet<AbstractRoute> routes = routesByVrf.get(Configuration.DEFAULT_VRF_NAME);
    assertThat(routes, not(hasItem(hasPrefix(prefix))));
  }

  private Configuration.Builder _cb;

  private RoutingPolicy.Builder _exportLocalPolicyBuilder;

  private Interface.Builder _ib;

  private BgpNeighbor.Builder _nb;

  private NetworkFactory _nf;

  private RoutingPolicy.Builder _nullExportPolicyBuilder;

  private BgpProcess.Builder _pb;

  private Vrf.Builder _vb;

  private SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> computeRoutes(
      Set<SubRange> exportLocalPrefixLengthRange) {
    Configuration cExportLocal = _cb.setHostname(C_EXPORT_NAME).build();
    Configuration cReceive = _cb.setHostname(C_RECEIVE_NAME).build();
    Vrf vExportLocal =
        _vb.setOwner(cExportLocal)
            .setExportLocalRoutePrefixLengthRange(exportLocalPrefixLengthRange)
            .build();
    Vrf vReceive = _vb.setOwner(cReceive).build();
    _ib.setOwner(cExportLocal)
        .setVrf(vExportLocal)
        .setAddress(
            new InterfaceAddress(PEERING_PREFIX.getStartIp(), PEERING_PREFIX.getPrefixLength()))
        .build();
    ImmutableList.of(LB_ADDRESS, PTP_ADDRESS, LAN_ADDRESS)
        .forEach(address -> _ib.setAddress(address).build());
    _ib.setOwner(cReceive)
        .setVrf(vReceive)
        .setAddress(
            new InterfaceAddress(PEERING_PREFIX.getEndIp(), PEERING_PREFIX.getPrefixLength()))
        .build();
    BgpProcess bpExportLocal =
        _pb.setRouterId(PEERING_PREFIX.getStartIp()).setVrf(vExportLocal).build();
    RoutingPolicy rpExportLocalOut = _exportLocalPolicyBuilder.setOwner(cExportLocal).build();
    RoutingPolicy rpReceiveOut = _nullExportPolicyBuilder.setOwner(cReceive).build();
    BgpProcess bpReceive = _pb.setRouterId(PEERING_PREFIX.getEndIp()).setVrf(vReceive).build();
    _nb.setOwner(cExportLocal)
        .setVrf(vExportLocal)
        .setBgpProcess(bpExportLocal)
        .setRemoteAs(AS_RECEIVE)
        .setLocalAs(AS_EXPORT_LOCAL)
        .setPeerAddress(PEERING_PREFIX.getEndIp())
        .setLocalIp(PEERING_PREFIX.getStartIp())
        .setExportPolicy(rpExportLocalOut.getName())
        .build();
    _nb.setOwner(cReceive)
        .setVrf(vReceive)
        .setBgpProcess(bpReceive)
        .setRemoteAs(AS_EXPORT_LOCAL)
        .setLocalAs(AS_RECEIVE)
        .setPeerAddress(PEERING_PREFIX.getStartIp())
        .setLocalIp(PEERING_PREFIX.getEndIp())
        .setExportPolicy(rpReceiveOut.getName())
        .build();
    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(C_EXPORT_NAME, cExportLocal, C_RECEIVE_NAME, cReceive);
    BdpEngine engine =
        new BdpEngine(
            new MockBdpSettings(),
            new BatfishLogger(BatfishLogger.LEVELSTR_OUTPUT, false),
            (s, i) -> new AtomicInteger());
    Topology topology = CommonUtil.synthesizeTopology(configurations);
    BdpDataPlane dp =
        engine.computeDataPlane(
            false, configurations, topology, ImmutableSet.of(), new BdpAnswerElement());
    return engine.getRoutes(dp);
  }

  /** Initialize builders with values common to all tests */
  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _ib = _nf.interfaceBuilder().setActive(true);
    _nb = _nf.bgpNeighborBuilder().setLocalAs(2);
    _pb = _nf.bgpProcessBuilder();
    _vb = _nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    If acceptIffLocal = new If();
    BooleanExpr guard = new MatchProtocol(RoutingProtocol.LOCAL);
    acceptIffLocal.setGuard(guard);
    acceptIffLocal.setTrueStatements(
        ImmutableList.of(
            new SetOrigin(new LiteralOrigin(OriginType.INCOMPLETE, null)),
            Statements.ExitAccept.toStaticStatement()));
    acceptIffLocal.setFalseStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()));

    /* Builder that creates default BGP export policy */
    _exportLocalPolicyBuilder =
        _nf.routingPolicyBuilder().setStatements(ImmutableList.of(acceptIffLocal));

    /* Builder that creates BGP export policy that rejects everything */
    _nullExportPolicyBuilder =
        _nf.routingPolicyBuilder()
            .setStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()));
  }

  @Test
  public void testBgpExportLocalRoutesLanAndPtp() {
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        computeRoutes(ImmutableSet.of(new SubRange(0, MAX_PREFIX_LENGTH - 1)));

    assertEbgpRoute(routes, C_RECEIVE_NAME, new Prefix(LAN_ADDRESS.getIp(), MAX_PREFIX_LENGTH));
    assertNoRoute(routes, C_RECEIVE_NAME, new Prefix(LB_ADDRESS.getIp(), MAX_PREFIX_LENGTH));
    assertEbgpRoute(routes, C_RECEIVE_NAME, new Prefix(PTP_ADDRESS.getIp(), MAX_PREFIX_LENGTH));
  }

  @Test
  public void testBgpExportLocalRoutesLanOnly() {
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        computeRoutes(ImmutableSet.of(new SubRange(0, MAX_PREFIX_LENGTH - 2)));

    assertEbgpRoute(routes, C_RECEIVE_NAME, new Prefix(LAN_ADDRESS.getIp(), MAX_PREFIX_LENGTH));
    assertNoRoute(routes, C_RECEIVE_NAME, new Prefix(LB_ADDRESS.getIp(), MAX_PREFIX_LENGTH));
    assertNoRoute(routes, C_RECEIVE_NAME, new Prefix(PTP_ADDRESS.getIp(), MAX_PREFIX_LENGTH));
  }

  @Test
  public void testBgpExportLocalRoutesNone() {
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        computeRoutes(ImmutableSet.of());

    assertNoRoute(routes, C_RECEIVE_NAME, new Prefix(LAN_ADDRESS.getIp(), MAX_PREFIX_LENGTH));
    assertNoRoute(routes, C_RECEIVE_NAME, new Prefix(LB_ADDRESS.getIp(), MAX_PREFIX_LENGTH));
    assertNoRoute(routes, C_RECEIVE_NAME, new Prefix(PTP_ADDRESS.getIp(), MAX_PREFIX_LENGTH));
  }

  @Test
  public void testBgpExportLocalRoutesPtpOnly() {
    SortedMap<String, SortedMap<String, SortedSet<AbstractRoute>>> routes =
        computeRoutes(ImmutableSet.of(new SubRange(0, MAX_PREFIX_LENGTH - 1)));

    assertNoRoute(routes, C_RECEIVE_NAME, new Prefix(LAN_ADDRESS.getIp(), MAX_PREFIX_LENGTH));
    assertNoRoute(routes, C_RECEIVE_NAME, new Prefix(LB_ADDRESS.getIp(), MAX_PREFIX_LENGTH));
    assertEbgpRoute(routes, C_RECEIVE_NAME, new Prefix(PTP_ADDRESS.getIp(), MAX_PREFIX_LENGTH));
  }
}
