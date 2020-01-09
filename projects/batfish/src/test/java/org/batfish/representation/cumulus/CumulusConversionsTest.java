package org.batfish.representation.cumulus;

import static com.google.common.base.MoreObjects.firstNonNull;
import static junit.framework.TestCase.assertNotNull;
import static org.batfish.common.Warnings.TAG_RED_FLAG;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.InterfaceType.PHYSICAL;
import static org.batfish.representation.cumulus.CumulusConversions.GENERATED_DEFAULT_ROUTE;
import static org.batfish.representation.cumulus.CumulusConversions.REJECT_DEFAULT_ROUTE;
import static org.batfish.representation.cumulus.CumulusConversions.addOspfInterfaces;
import static org.batfish.representation.cumulus.CumulusConversions.computeBgpCommonExportPolicyName;
import static org.batfish.representation.cumulus.CumulusConversions.computeBgpGenerationPolicyName;
import static org.batfish.representation.cumulus.CumulusConversions.computeBgpNeighborImportRoutingPolicy;
import static org.batfish.representation.cumulus.CumulusConversions.computeBgpPeerExportPolicyName;
import static org.batfish.representation.cumulus.CumulusConversions.computeLocalIpForBgpNeighbor;
import static org.batfish.representation.cumulus.CumulusConversions.computeMatchSuppressedSummaryOnlyPolicyName;
import static org.batfish.representation.cumulus.CumulusConversions.computeOspfAreas;
import static org.batfish.representation.cumulus.CumulusConversions.convertIpv4UnicastAddressFamily;
import static org.batfish.representation.cumulus.CumulusConversions.generateBgpCommonPeerConfig;
import static org.batfish.representation.cumulus.CumulusConversions.generateExportAggregateConditions;
import static org.batfish.representation.cumulus.CumulusConversions.generateGeneratedRoutes;
import static org.batfish.representation.cumulus.CumulusConversions.generateGenerationPolicy;
import static org.batfish.representation.cumulus.CumulusConversions.getSetNextHop;
import static org.batfish.representation.cumulus.CumulusConversions.inferRouterId;
import static org.batfish.representation.cumulus.CumulusConversions.resolveLocalIpFromUpdateSource;
import static org.batfish.representation.cumulus.CumulusConversions.suppressSummarizedPrefixes;
import static org.batfish.representation.cumulus.CumulusConversions.toAsPathAccessList;
import static org.batfish.representation.cumulus.CumulusConversions.toBgpProcess;
import static org.batfish.representation.cumulus.CumulusConversions.toCommunityList;
import static org.batfish.representation.cumulus.CumulusConversions.toOspfProcess;
import static org.batfish.representation.cumulus.CumulusConversions.toRouteFilterLine;
import static org.batfish.representation.cumulus.CumulusConversions.toRouteFilterList;
import static org.batfish.representation.cumulus.CumulusNodeConfiguration.LOOPBACK_INTERFACE_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsPathAccessList;
import org.batfish.datamodel.AsPathAccessListLine;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Bgpv4Route.Builder;
import org.batfish.datamodel.CommunityList;
import org.batfish.datamodel.CommunityListLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RouteFilterLine;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.ospf.OspfArea;
import org.batfish.datamodel.routing_policy.Common;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralCommunity;
import org.batfish.datamodel.routing_policy.expr.MatchCommunitySet;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.Not;
import org.batfish.datamodel.routing_policy.expr.SelfNextHop;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetNextHop;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/** Test for {@link CumulusConversions}. */
public final class CumulusConversionsTest {
  private NetworkFactory _nf;
  private Configuration _c;
  private Vrf _v;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _c = _nf.configurationBuilder().build();
    _v = _nf.vrfBuilder().setOwner(_c).build();
  }

  private Environment finalEnvironment(Statement statement, String network) {
    RoutingPolicy policy =
        RoutingPolicy.builder(_nf).setOwner(_c).setStatements(ImmutableList.of(statement)).build();
    Environment env =
        Environment.builder(_c)
            .setOriginalRoute(
                Bgpv4Route.builder()
                    .setNetwork(Prefix.parse(network))
                    // Only network matters for these tests, but Bgp4Route requires these have
                    // values.
                    .setOriginatorIp(Ip.parse("1.1.1.1"))
                    .setOriginType(OriginType.IGP)
                    .setProtocol(RoutingProtocol.BGP)
                    .build())
            .build();
    policy.call(env);
    return env;
  }

  private boolean value(BooleanExpr expr, Environment env) {
    RoutingPolicy policy =
        RoutingPolicy.builder(_nf)
            .setOwner(_c)
            .setStatements(
                ImmutableList.of(
                    new If(
                        expr,
                        ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                        ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))))
            .build();
    return policy.call(env).getBooleanValue();
  }

  private boolean value(RoutingPolicy policy, String network) {
    return value(policy, Prefix.parse(network));
  }

  /**
   * Evaluate the policy in an environment with the original route being a static route with the
   * input network.
   */
  private boolean value(RoutingPolicy policy, Prefix network) {
    return policy
        .call(
            Environment.builder(_c)
                .setOriginalRoute(
                    StaticRoute.builder().setAdministrativeCost(0).setNetwork(network).build())
                .build())
        .getBooleanValue();
  }

  @Test
  public void testGenerateExportAggregateConditions() {
    BooleanExpr booleanExpr =
        generateExportAggregateConditions(
            ImmutableMap.of(
                Prefix.parse("1.2.3.0/24"),
                new BgpVrfAddressFamilyAggregateNetworkConfiguration()));

    // longer not exported
    {
      Environment env =
          Environment.builder(_c)
              .setOriginalRoute(
                  GeneratedRoute.builder().setNetwork(Prefix.parse("1.2.3.4/32")).build())
              .build();
      assertFalse(value(booleanExpr, env));
    }

    // shorter not exported
    {
      Environment env =
          Environment.builder(_c)
              .setOriginalRoute(
                  GeneratedRoute.builder().setNetwork(Prefix.parse("1.2.0.0/16")).build())
              .build();
      assertFalse(value(booleanExpr, env));
    }

    // exact match exported
    {
      Environment env =
          Environment.builder(_c)
              .setOriginalRoute(
                  GeneratedRoute.builder().setNetwork(Prefix.parse("1.2.3.0/24")).build())
              .build();
      assertTrue(value(booleanExpr, env));
    }
  }

  @Test
  public void testGenerateGeneratedRoutes() {
    Prefix prefix = Prefix.parse("1.2.3.0/24");
    generateGeneratedRoutes(
        _c, _v, ImmutableMap.of(prefix, new BgpVrfAddressFamilyAggregateNetworkConfiguration()));
    String policyName = computeBgpGenerationPolicyName(true, _v.getName(), prefix.toString());

    // configuration has the generation policy
    assertThat(_c.getRoutingPolicies(), Matchers.hasKey(policyName));

    // vrf has generated route
    ImmutableList<GeneratedRoute> grs =
        _v.getGeneratedRoutes().stream()
            .filter(gr -> gr.getNetwork().equals(prefix))
            .collect(ImmutableList.toImmutableList());
    assertThat(grs, hasSize(1));

    GeneratedRoute gr = grs.get(0);
    assertTrue(gr.getDiscard());
    assertThat(gr.getGenerationPolicy(), equalTo(policyName));
  }

  @Test
  public void testGenerateGenerationPolicy() {
    Prefix prefix = Prefix.parse("1.2.3.0/24");
    generateGenerationPolicy(_c, _v.getName(), prefix);

    RoutingPolicy policy =
        _c.getRoutingPolicies()
            .get(computeBgpGenerationPolicyName(true, _v.getName(), prefix.toString()));

    assertTrue(value(policy, "1.2.3.4/32"));
    assertFalse(value(policy, "1.2.3.0/24"));
    assertFalse(value(policy, "1.2.0.0/16"));
  }

  @Test
  public void testSuppressSummarizedPrefixes() {
    Prefix suppressedPrefix = Prefix.parse("1.2.3.0/24");
    If stmt = suppressSummarizedPrefixes(_c, _v.getName(), Stream.of(suppressedPrefix));
    assertNotNull(stmt);
    assertTrue(firstNonNull(finalEnvironment(stmt, "1.2.3.4/32").getSuppressed(), false));
    assertFalse(firstNonNull(finalEnvironment(stmt, "1.2.3.0/24").getSuppressed(), false));
    assertFalse(firstNonNull(finalEnvironment(stmt, "1.2.0.0/16").getSuppressed(), false));
  }

  @Test
  public void testToRouteFilterLine() {
    IpPrefixListLine prefixListLine =
        new IpPrefixListLine(
            LineAction.PERMIT, 10, Prefix.parse("10.0.0.1/24"), new SubRange(27, 30));

    RouteFilterLine rfl = toRouteFilterLine(prefixListLine);
    assertThat(
        rfl,
        equalTo(
            new RouteFilterLine(
                LineAction.PERMIT, Prefix.parse("10.0.0.1/24"), new SubRange(27, 30))));
  }

  @Test
  public void testToRouteFilter() {
    IpPrefixList prefixList = new IpPrefixList("name");
    prefixList
        .getLines()
        .put(
            10L,
            new IpPrefixListLine(
                LineAction.DENY, 10, Prefix.parse("10.0.0.1/24"), new SubRange(27, 30)));
    prefixList
        .getLines()
        .put(
            20L,
            new IpPrefixListLine(
                LineAction.PERMIT, 20, Prefix.parse("10.0.2.1/24"), new SubRange(28, 31)));

    RouteFilterList rfl = toRouteFilterList(prefixList);

    assertThat(
        rfl,
        equalTo(
            new RouteFilterList(
                "name",
                ImmutableList.of(
                    new RouteFilterLine(
                        LineAction.DENY, Prefix.parse("10.0.0.1/24"), new SubRange(27, 30)),
                    new RouteFilterLine(
                        LineAction.PERMIT, Prefix.parse("10.0.2.1/24"), new SubRange(28, 31))))));
  }

  @Test
  public void testToAsPathAccessList() {
    long permitted = 11111;
    long denied = 22222;
    IpAsPathAccessList asPathAccessList = new IpAsPathAccessList("name");
    asPathAccessList.addLine(new IpAsPathAccessListLine(LineAction.DENY, denied));
    asPathAccessList.addLine(new IpAsPathAccessListLine(LineAction.PERMIT, permitted));
    AsPathAccessList viList = toAsPathAccessList(asPathAccessList);

    // Cache initialization only happens in AsPathAccessList on deserialization o.O
    viList = SerializationUtils.clone(viList);

    List<AsPathAccessListLine> expectedViLines =
        ImmutableList.of(
            new AsPathAccessListLine(LineAction.DENY, String.format("(^| )%s($| )", denied)),
            new AsPathAccessListLine(LineAction.PERMIT, String.format("(^| )%s($| )", permitted)));
    assertThat(viList, equalTo(new AsPathAccessList("name", expectedViLines)));

    // Matches paths containing permitted ASN
    long other = 33333;
    assertTrue(viList.permits(AsPath.ofSingletonAsSets(permitted)));
    assertTrue(viList.permits(AsPath.ofSingletonAsSets(permitted, other)));
    assertTrue(viList.permits(AsPath.ofSingletonAsSets(other, permitted)));
    assertTrue(viList.permits(AsPath.ofSingletonAsSets(other, permitted, other)));

    // Does not match if denied ASN is in path, even if permitted is also there
    assertFalse(viList.permits(AsPath.ofSingletonAsSets(denied)));
    assertFalse(viList.permits(AsPath.ofSingletonAsSets(denied, permitted)));
    assertFalse(viList.permits(AsPath.ofSingletonAsSets(permitted, denied)));
    assertFalse(viList.permits(AsPath.ofSingletonAsSets(permitted, denied, permitted)));

    // Does not match by default
    assertFalse(viList.permits(AsPath.ofSingletonAsSets(other)));
  }

  @Test
  public void testToIpCommunityList() {
    String name = "name";
    IpCommunityListExpanded ipCommunityList =
        new IpCommunityListExpanded(
            name,
            LineAction.PERMIT,
            ImmutableList.of(StandardCommunity.of(10000, 1), StandardCommunity.of(20000, 2)));
    CommunityList result = toCommunityList(ipCommunityList);
    assertThat(
        result,
        equalTo(
            new CommunityList(
                name,
                ImmutableList.of(
                    new CommunityListLine(
                        LineAction.PERMIT, new LiteralCommunity(StandardCommunity.of(10000, 1))),
                    new CommunityListLine(
                        LineAction.PERMIT, new LiteralCommunity(StandardCommunity.of(20000, 2)))),
                false)));
  }

  @Test
  public void testGetAcceptStatements() {
    BgpNeighbor bgpNeighbor = new BgpIpNeighbor("10.0.0.1");
    BgpVrf bgpVrf = new BgpVrf("bgpVrf");

    {
      // if no as set, do not set next-hop-self
      SetNextHop setNextHop = getSetNextHop(bgpNeighbor, bgpVrf);
      assertNull(setNextHop);
    }

    {
      // if is not ibgp, do not set next-hop-self
      bgpNeighbor.setRemoteAs(10000L);
      bgpVrf.setAutonomousSystem(20000L);
      SetNextHop setNextHop = getSetNextHop(bgpNeighbor, bgpVrf);
      assertNull(setNextHop);
    }

    {
      // if is ibgp but no address family set, do not set next-hop-self
      bgpNeighbor.setRemoteAs(10000L);
      bgpVrf.setAutonomousSystem(10000L);
      SetNextHop setNextHop = getSetNextHop(bgpNeighbor, bgpVrf);
      assertNull(setNextHop);
    }

    {
      // if is ibgp and has address family set but no neighbor configuration, do not set
      // next-hop-self
      bgpNeighbor.setRemoteAs(10000L);
      bgpVrf.setAutonomousSystem(10000L);
      bgpVrf.setIpv4Unicast(new BgpIpv4UnicastAddressFamily());
      SetNextHop setNextHop = getSetNextHop(bgpNeighbor, bgpVrf);
      assertNull(setNextHop);
    }

    {
      // if is ibgp but neighbor configuration does not have next-hop-self set, do not set
      // next-hop-self
      bgpNeighbor.setRemoteAs(10000L);
      bgpVrf.setAutonomousSystem(10000L);
      bgpNeighbor.setIpv4UnicastAddressFamily(new BgpNeighborIpv4UnicastAddressFamily());

      assertNull(getSetNextHop(bgpNeighbor, bgpVrf));
    }

    {
      // if is ibgp and neighbor configuration set next-hop-self set, then set
      // next-hop-self
      bgpNeighbor.setRemoteAs(10000L);
      bgpVrf.setAutonomousSystem(10000L);
      BgpNeighborIpv4UnicastAddressFamily ipv4af = new BgpNeighborIpv4UnicastAddressFamily();
      bgpNeighbor.setIpv4UnicastAddressFamily(ipv4af);
      ipv4af.setNextHopSelf(true);

      assertThat(
          getSetNextHop(bgpNeighbor, bgpVrf), equalTo(new SetNextHop(SelfNextHop.getInstance())));
    }
  }

  @Test
  public void testComputeLocalIpForBgpNeighbor() {
    Ip remoteIp = Ip.parse("1.1.1.1");
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();
    String vrfName = "vrf";
    Vrf vrf = nf.vrfBuilder().setName(vrfName).setOwner(c).build();
    org.batfish.datamodel.Interface.Builder ib = nf.interfaceBuilder().setVrf(vrf);

    // Should not accept interface whose subnet doesn't include the remote IP
    InterfaceAddress addr1 = ConcreteInterfaceAddress.create(Ip.parse("2.2.2.2"), 24);
    ib.setType(PHYSICAL).setOwner(c).setName("i1").setAddress(addr1).build();
    assertNull(computeLocalIpForBgpNeighbor(remoteIp, c, vrfName));

    // Should not accept interface that owns the remote IP
    InterfaceAddress addr2 = ConcreteInterfaceAddress.create(remoteIp, 24);
    ib.setType(PHYSICAL).setOwner(c).setName("i2").setAddress(addr2).build();
    assertNull(computeLocalIpForBgpNeighbor(remoteIp, c, vrfName));

    // Should accept interface that doesn't own the remote IP but whose subnet does include it
    Ip ifaceIp = Ip.parse("1.1.1.2");
    InterfaceAddress addr3 = ConcreteInterfaceAddress.create(ifaceIp, 24);
    ib.setType(PHYSICAL).setOwner(c).setName("i3").setAddress(addr3).build();
    assertThat(computeLocalIpForBgpNeighbor(remoteIp, c, vrfName), equalTo(ifaceIp));
  }

  @Test
  public void testComputeLocalIpForBgpNeighbor_vrf() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();

    Vrf vrf1 = nf.vrfBuilder().setOwner(c).build();
    Vrf vrf2 = nf.vrfBuilder().setOwner(c).build();

    org.batfish.datamodel.Interface.Builder ib =
        nf.interfaceBuilder().setOwner(c).setType(PHYSICAL);
    ib.setVrf(vrf1).setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24")).build();
    ib.setVrf(vrf2).setAddress(ConcreteInterfaceAddress.parse("2.2.2.2/24")).build();

    Ip remoteIp = Ip.parse("1.1.1.3");

    // vrf1 owns the compatible localIp = 1.1.1.1
    assertThat(
        computeLocalIpForBgpNeighbor(remoteIp, c, vrf1.getName()), equalTo(Ip.parse("1.1.1.1")));

    // vrf2 does not own a compatible localIp
    assertNull(computeLocalIpForBgpNeighbor(remoteIp, c, vrf2.getName()));
  }

  @Test
  public void testToBgpProcess_aggregateRoutes() {
    testToBgpProcess_aggregateRoutes(true);
    testToBgpProcess_aggregateRoutes(false);
  }

  private static void testToBgpProcess_aggregateRoutes(boolean summaryOnly) {
    // setup VI model
    NetworkFactory nf = new NetworkFactory();
    Configuration viConfig =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();
    Vrf viVrf = nf.vrfBuilder().setOwner(viConfig).setName(DEFAULT_VRF_NAME).build();

    // setup VS model
    CumulusNcluConfiguration vsConfig = new CumulusNcluConfiguration();
    BgpProcess bgpProcess = new BgpProcess();
    vsConfig.setBgpProcess(bgpProcess);
    vsConfig.setConfiguration(viConfig);

    // setup BgpVrf
    BgpVrf vrf = bgpProcess.getDefaultVrf();
    vrf.setRouterId(Ip.parse("1.1.1.1"));
    BgpIpv4UnicastAddressFamily ipv4Unicast = new BgpIpv4UnicastAddressFamily();
    Prefix prefix = Prefix.parse("1.2.3.0/24");

    BgpVrfAddressFamilyAggregateNetworkConfiguration agg =
        new BgpVrfAddressFamilyAggregateNetworkConfiguration();
    agg.setSummaryOnly(summaryOnly);

    ipv4Unicast.getAggregateNetworks().put(prefix, agg);
    vrf.setIpv4Unicast(ipv4Unicast);

    // the method under test
    toBgpProcess(viConfig, vsConfig, DEFAULT_VRF_NAME, vrf);

    // generation policy exists
    assertThat(
        viConfig.getRoutingPolicies(),
        hasKey(computeBgpGenerationPolicyName(true, DEFAULT_VRF_NAME, prefix.toString())));

    // generated route exists
    assertTrue(viVrf.getGeneratedRoutes().stream().anyMatch(gr -> gr.getNetwork().equals(prefix)));

    if (summaryOnly) {
      // suppress summary only filter list exists
      assertThat(
          viConfig.getRouteFilterLists(),
          hasKey(computeMatchSuppressedSummaryOnlyPolicyName(viVrf.getName())));
    } else {
      // suppress summary only filter list does not exist
      assertThat(
          viConfig.getRouteFilterLists(),
          not(hasKey(computeMatchSuppressedSummaryOnlyPolicyName(viVrf.getName()))));
    }
  }

  /**
   * Test that networks statements at BGP VRF level (outside of ipv4 address family stanza) are
   * accounted for
   */
  @Test
  public void testToBgpProcess_vrfLevelNetworks() {
    // setup VI model
    NetworkFactory nf = new NetworkFactory();
    Configuration viConfig =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();
    Vrf viVrf = nf.vrfBuilder().setOwner(viConfig).setName(DEFAULT_VRF_NAME).build();

    // setup VS model
    CumulusNcluConfiguration vsConfig = new CumulusNcluConfiguration();
    BgpProcess bgpProcess = new BgpProcess();
    vsConfig.setBgpProcess(bgpProcess);
    vsConfig.setConfiguration(viConfig);

    // setup BgpVrf
    Prefix prefix = Prefix.parse("1.2.3.0/24");
    BgpVrf vrf = bgpProcess.getDefaultVrf();
    vrf.setRouterId(Ip.parse("1.1.1.1"));
    vrf.addNetwork(prefix);

    // the method under test
    org.batfish.datamodel.BgpProcess viBgp =
        toBgpProcess(viConfig, vsConfig, DEFAULT_VRF_NAME, vrf);

    // generation policy exists
    assertTrue(viBgp.getOriginationSpace().containsPrefix(prefix));

    RoutingPolicy bgpCommonExportPolicy =
        viConfig.getRoutingPolicies().get(computeBgpCommonExportPolicyName(DEFAULT_VRF_NAME));

    assertThat(
        ((Conjunction)
                ((Disjunction) ((If) bgpCommonExportPolicy.getStatements().get(0)).getGuard())
                    .getDisjuncts()
                    .get(1))
            .getConjuncts()
            .get(0),
        equalTo(
            new MatchPrefixSet(
                DestinationNetwork.instance(),
                new ExplicitPrefixSet(new PrefixSpace(PrefixRange.fromPrefix(prefix))))));
  }

  @Test
  public void testGenerateBgpCommonPeerConfig_rejectDefault() {
    Ip peerIp = Ip.parse("10.0.0.2");
    BgpIpNeighbor neighbor = new BgpIpNeighbor("BgpNeighbor");
    neighbor.setRemoteAs(10000L);
    neighbor.setRemoteAsType(RemoteAsType.INTERNAL);
    neighbor.setPeerIp(peerIp);

    org.batfish.datamodel.BgpProcess newProc =
        new org.batfish.datamodel.BgpProcess(
            Ip.parse("10.0.0.1"), ConfigurationFormat.CUMULUS_NCLU);

    Configuration viConfig =
        _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();

    CumulusNcluConfiguration vsConfig = new CumulusNcluConfiguration();
    vsConfig.setConfiguration(viConfig);

    BgpActivePeerConfig.Builder peerConfigBuilder =
        BgpActivePeerConfig.builder().setPeerAddress(peerIp);
    generateBgpCommonPeerConfig(
        viConfig, vsConfig, neighbor, 10000L, new BgpVrf("vrf"), newProc, peerConfigBuilder);

    // We test exact match with the constant REJECT_DEFAULT_ROUTE here. The constant is
    // tested in testRejectDefaultRoute()
    assertThat(
        viConfig
            .getRoutingPolicies()
            .get(computeBgpPeerExportPolicyName("vrf", neighbor.getName()))
            .getStatements()
            .get(0),
        equalTo(REJECT_DEFAULT_ROUTE));
  }

  @Test
  public void testRejectDefaultRoute() {
    Configuration viConfig =
        _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();

    RoutingPolicy rejectDefaultPolicy =
        RoutingPolicy.builder()
            .setOwner(viConfig)
            .setName("policy")
            .addStatement(REJECT_DEFAULT_ROUTE)
            .addStatement(
                // accept non-default
                new If(
                    new Not(Common.matchDefaultRoute()),
                    ImmutableList.of(Statements.ReturnTrue.toStaticStatement())))
            .build();

    AbstractRoute defaultRoute = new ConnectedRoute(Prefix.ZERO, "dummy");
    AbstractRoute nonDefaultRoute = new ConnectedRoute(Prefix.parse("1.1.1.1/32"), "dummy");

    assertFalse(
        rejectDefaultPolicy.process(
            defaultRoute,
            Bgpv4Route.builder().setNetwork(defaultRoute.getNetwork()),
            Direction.OUT));

    assertTrue(
        rejectDefaultPolicy.process(
            nonDefaultRoute,
            Bgpv4Route.builder().setNetwork(nonDefaultRoute.getNetwork()),
            Direction.OUT));
  }

  @Test
  public void testGenerateBgpCommonPeerConfig_defaultOriginate_unset() {
    // set bgp neighbor without default originate
    Ip peerIp = Ip.parse("10.0.0.2");
    BgpIpNeighbor neighbor = new BgpIpNeighbor("BgpNeighbor");
    neighbor.setRemoteAs(10000L);
    neighbor.setRemoteAsType(RemoteAsType.INTERNAL);
    neighbor.setPeerIp(peerIp);

    org.batfish.datamodel.BgpProcess newProc =
        new org.batfish.datamodel.BgpProcess(
            Ip.parse("10.0.0.1"), ConfigurationFormat.CUMULUS_NCLU);

    Configuration viConfig =
        _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();

    CumulusNcluConfiguration vsConfig = new CumulusNcluConfiguration();
    vsConfig.setConfiguration(viConfig);

    BgpActivePeerConfig.Builder peerConfigBuilder =
        BgpActivePeerConfig.builder().setPeerAddress(peerIp);
    generateBgpCommonPeerConfig(
        viConfig, vsConfig, neighbor, 10000L, new BgpVrf("vrf"), newProc, peerConfigBuilder);

    // there should be no generated default route
    assertThat(
        newProc.getActiveNeighbors().get(peerIp.toPrefix()).getGeneratedRoutes(),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testGenerateBgpCommonPeerConfig_defaultOriginate_set() {
    // set bgp neighbor with default originate
    Ip peerIp = Ip.parse("10.0.0.2");
    BgpIpNeighbor neighbor = new BgpIpNeighbor("BgpNeighbor");
    neighbor.setRemoteAs(10000L);
    neighbor.setRemoteAsType(RemoteAsType.INTERNAL);
    neighbor.setPeerIp(peerIp);
    BgpNeighborIpv4UnicastAddressFamily ipv4UnicastAddressFamily =
        new BgpNeighborIpv4UnicastAddressFamily();
    ipv4UnicastAddressFamily.setDefaultOriginate(true);
    neighbor.setIpv4UnicastAddressFamily(ipv4UnicastAddressFamily);

    org.batfish.datamodel.BgpProcess newProc =
        new org.batfish.datamodel.BgpProcess(
            Ip.parse("10.0.0.1"), ConfigurationFormat.CUMULUS_NCLU);

    Configuration viConfig =
        _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();

    CumulusNcluConfiguration vsConfig = new CumulusNcluConfiguration();
    vsConfig.setConfiguration(viConfig);

    BgpActivePeerConfig.Builder peerConfigBuilder =
        BgpActivePeerConfig.builder().setPeerAddress(peerIp);
    generateBgpCommonPeerConfig(
        viConfig, vsConfig, neighbor, 10000L, new BgpVrf("vrf"), newProc, peerConfigBuilder);

    // there should be a generated default route
    assertThat(
        newProc.getActiveNeighbors().get(peerIp.toPrefix()).getGeneratedRoutes(),
        equalTo(ImmutableSet.of(GENERATED_DEFAULT_ROUTE)));
  }

  @Test
  public void testGenerateBgpCommonPeerConfig_SetEbgpMultiHop() {
    // set bgp neighbor
    Ip peerIp = Ip.parse("10.0.0.2");
    BgpIpNeighbor neighbor = new BgpIpNeighbor("BgpNeighbor");
    neighbor.setRemoteAs(10000L);
    neighbor.setRemoteAsType(RemoteAsType.INTERNAL);
    neighbor.setPeerIp(peerIp);
    neighbor.setEbgpMultihop(3L);

    // set bgp process
    org.batfish.datamodel.BgpProcess newProc =
        new org.batfish.datamodel.BgpProcess(
            Ip.parse("10.0.0.1"), ConfigurationFormat.CUMULUS_NCLU);

    Configuration viConfig =
        new NetworkFactory()
            .configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU)
            .build();
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();

    BgpActivePeerConfig.Builder peerConfigBuilder =
        BgpActivePeerConfig.builder().setPeerAddress(peerIp);
    generateBgpCommonPeerConfig(
        viConfig,
        ncluConfiguration,
        neighbor,
        10000L,
        new BgpVrf("Vrf"),
        newProc,
        peerConfigBuilder);

    BgpActivePeerConfig peerConfig = newProc.getActiveNeighbors().get(peerIp.toPrefix());
    assertTrue(peerConfig.getEbgpMultihop());
  }

  @Test
  public void testConvertIpv4UnicastAddressFamily_deactivated() {
    // setup vi model
    NetworkFactory nf = new NetworkFactory();
    Configuration viConfig =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();
    RoutingPolicy policy = nf.routingPolicyBuilder().build();

    // setup vs model
    CumulusNcluConfiguration vsConfig = new CumulusNcluConfiguration();
    vsConfig.setConfiguration(viConfig);
    BgpNeighborIpv4UnicastAddressFamily af = new BgpNeighborIpv4UnicastAddressFamily();
    af.setRouteReflectorClient(true);

    // explicitly deactivated
    af.setActivated(false);
    assertNull(convertIpv4UnicastAddressFamily(af, true, policy, null));
    assertNull(convertIpv4UnicastAddressFamily(af, false, policy, null));

    // explicitly activated
    af.setActivated(true);
    assertNotNull(convertIpv4UnicastAddressFamily(af, true, policy, null));
    assertNotNull(convertIpv4UnicastAddressFamily(af, false, policy, null));

    // no explicit configuration
    af.setActivated(null);
    assertNotNull(convertIpv4UnicastAddressFamily(af, true, policy, null));
    assertNull(convertIpv4UnicastAddressFamily(af, false, policy, null));
  }

  @Test
  public void testConvertIpv4UnicastAddressFamily_allowAsIn() {
    // setup vi model
    NetworkFactory nf = new NetworkFactory();
    Configuration viConfig =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();
    RoutingPolicy policy = nf.routingPolicyBuilder().build();

    // setup vs model
    CumulusNcluConfiguration vsConfig = new CumulusNcluConfiguration();
    vsConfig.setConfiguration(viConfig);

    {
      // address family is null
      assertFalse(
          convertIpv4UnicastAddressFamily(null, true, policy, null)
              .getAddressFamilyCapabilities()
              .getAllowLocalAsIn());
    }

    {
      // address family is non-null but allowasin is not set
      BgpNeighborIpv4UnicastAddressFamily af = new BgpNeighborIpv4UnicastAddressFamily();
      assertFalse(
          convertIpv4UnicastAddressFamily(af, true, policy, null)
              .getAddressFamilyCapabilities()
              .getAllowLocalAsIn());
    }

    {
      // address family is non-null and allowasin is  set
      BgpNeighborIpv4UnicastAddressFamily af = new BgpNeighborIpv4UnicastAddressFamily();
      af.setAllowAsIn(5);
      assertTrue(
          convertIpv4UnicastAddressFamily(af, true, policy, null)
              .getAddressFamilyCapabilities()
              .getAllowLocalAsIn());
    }
  }

  @Test
  public void testConvertIpv4UnicastAddressFamily_routeReflectorClient() {

    // setup vi model
    NetworkFactory nf = new NetworkFactory();
    Configuration viConfig =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();
    RoutingPolicy policy = nf.routingPolicyBuilder().build();

    // setup vs model
    CumulusNcluConfiguration vsConfig = new CumulusNcluConfiguration();
    vsConfig.setConfiguration(viConfig);

    // route-reflector-client is false if ipv4af is null
    assertFalse(
        convertIpv4UnicastAddressFamily(null, true, policy, null).getRouteReflectorClient());

    // VI route-reflector-client is true if VS activate and route-reflector-client are both true
    {
      BgpNeighborIpv4UnicastAddressFamily af = new BgpNeighborIpv4UnicastAddressFamily();
      af.setActivated(true);
      af.setRouteReflectorClient(true);
      assertTrue(convertIpv4UnicastAddressFamily(af, true, policy, null).getRouteReflectorClient());
    }

    // Despite cumulus docs, GNS3 testing confirms VI route-reflector-client should be true even if
    // activate is null (i.e. not explicitly activated).
    {
      BgpNeighborIpv4UnicastAddressFamily af = new BgpNeighborIpv4UnicastAddressFamily();
      af.setRouteReflectorClient(true);
      assertTrue(convertIpv4UnicastAddressFamily(af, true, policy, null).getRouteReflectorClient());
    }
  }

  @Test
  public void testComputeBgpNeighborImportRoutingPolicy() {
    BgpNeighbor neighbor = new BgpIpNeighbor("neighbor");

    BgpNeighborIpv4UnicastAddressFamily neighborIpv4UnicastFamily =
        new BgpNeighborIpv4UnicastAddressFamily();

    neighborIpv4UnicastFamily.setRouteMapIn("peerMapIn");

    neighbor.setIpv4UnicastAddressFamily(neighborIpv4UnicastFamily);

    BgpVrf bgpVrf = new BgpVrf("bgpVrf");

    Configuration config = new Configuration("host", ConfigurationFormat.CUMULUS_NCLU);

    RoutingPolicy importPolicy = computeBgpNeighborImportRoutingPolicy(config, neighbor, bgpVrf);

    // checking the import policy is correct (i.e. it successfully calls the route map to
    // permite/block route).
    RoutingPolicy routemapPolicy =
        RoutingPolicy.builder()
            .setOwner(config)
            .setName("peerMapIn")
            .addStatement(
                new If(
                    "match community 10000:1 routes",
                    new MatchCommunitySet(new LiteralCommunity(StandardCommunity.parse("10000:1"))),
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement())))
            .build();

    Builder builder =
        Bgpv4Route.builder()
            .setOriginatorIp(Ip.parse("10.0.0.1"))
            .setOriginType(OriginType.EGP)
            .setProtocol(RoutingProtocol.BGP)
            .setNetwork(Prefix.parse("10.0.0.0/24"));
    {
      // should permit route
      Bgpv4Route ipv4Route =
          builder.setCommunities(ImmutableSet.of(StandardCommunity.parse("10000:1"))).build();

      Environment envIpv4 =
          Environment.builder(config)
              .setRoutingPolicies(ImmutableMap.of("peerMapIn", routemapPolicy))
              .setOriginalRoute(ipv4Route)
              .setDirection(Direction.IN)
              .build();
      Result result = importPolicy.call(envIpv4);
      assertTrue(result.getBooleanValue());
    }

    {
      // should block route
      Bgpv4Route ipv4Route =
          builder.setCommunities(ImmutableSet.of(StandardCommunity.parse("20000:1"))).build();

      Environment envIpv4 =
          Environment.builder(config)
              .setRoutingPolicies(ImmutableMap.of("peerMapIn", routemapPolicy))
              .setOriginalRoute(ipv4Route)
              .setDirection(Direction.IN)
              .build();
      Result result = importPolicy.call(envIpv4);
      assertFalse(result.getBooleanValue());
    }
  }

  private static Configuration getConfigurationWithLoopback(
      @Nullable ConcreteInterfaceAddress address) {
    Configuration c = new Configuration("test", ConfigurationFormat.CUMULUS_NCLU);
    org.batfish.datamodel.Interface loopback =
        org.batfish.datamodel.Interface.builder()
            .setName(LOOPBACK_INTERFACE_NAME)
            .setOwner(c)
            .build();
    if (address != null) {
      loopback.setAddress(address);
      loopback.setAllAddresses(ImmutableSet.of(address));
    }
    return c;
  }

  @Test
  public void testToOspfProcess_NoRouterId() {
    OspfVrf ospfVrf = new OspfVrf(DEFAULT_VRF_NAME);

    org.batfish.datamodel.ospf.OspfProcess ospfProcess =
        toOspfProcess(
            new Configuration("dummy", ConfigurationFormat.CUMULUS_NCLU),
            new CumulusNcluConfiguration(),
            ospfVrf,
            ImmutableMap.of(),
            new Warnings());
    assertThat(ospfProcess.getRouterId(), equalTo(Ip.parse("0.0.0.0")));
    assertThat(ospfProcess.getProcessId(), equalTo("default"));
    assertThat(
        ospfProcess.getReferenceBandwidth(),
        equalTo(org.batfish.representation.cumulus.OspfProcess.DEFAULT_REFERENCE_BANDWIDTH));
  }

  @Test
  public void testToOspfProcess_InferRouterId() {
    OspfVrf ospfVrf = new OspfVrf(DEFAULT_VRF_NAME);

    org.batfish.datamodel.ospf.OspfProcess ospfProcess =
        toOspfProcess(
            getConfigurationWithLoopback(ConcreteInterfaceAddress.parse("1.1.1.1/24")),
            new CumulusNcluConfiguration(),
            ospfVrf,
            ImmutableMap.of(),
            new Warnings());
    assertThat(ospfProcess.getRouterId(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(ospfProcess.getProcessId(), equalTo("default"));
    assertThat(
        ospfProcess.getReferenceBandwidth(),
        equalTo(org.batfish.representation.cumulus.OspfProcess.DEFAULT_REFERENCE_BANDWIDTH));
  }

  @Test
  public void testToOspfProcess_ConfigedRouterId() {
    OspfVrf ospfVrf = new OspfVrf(DEFAULT_VRF_NAME);
    ospfVrf.setRouterId(Ip.parse("1.2.3.4"));

    org.batfish.datamodel.ospf.OspfProcess ospfProcess =
        toOspfProcess(
            new Configuration("dummy", ConfigurationFormat.CUMULUS_NCLU),
            new CumulusNcluConfiguration(),
            ospfVrf,
            ImmutableMap.of(),
            new Warnings());
    assertThat(ospfProcess.getRouterId(), equalTo(Ip.parse("1.2.3.4")));
    assertThat(ospfProcess.getProcessId(), equalTo("default"));
    assertThat(
        ospfProcess.getReferenceBandwidth(),
        equalTo(org.batfish.representation.cumulus.OspfProcess.DEFAULT_REFERENCE_BANDWIDTH));
  }

  @Test
  public void testInferRouterID_DefaultCase() {
    assertThat(
        inferRouterId(new Configuration("dummy", ConfigurationFormat.CUMULUS_NCLU)),
        equalTo(Ip.parse("0.0.0.0")));
  }

  @Test
  public void testInferRouterID_Loopback() {
    assertThat(
        inferRouterId(getConfigurationWithLoopback(ConcreteInterfaceAddress.parse("1.1.1.1/31"))),
        equalTo(Ip.parse("1.1.1.1")));
  }

  @Test
  public void testInferRouterID_LoopbackNotUsable() {
    assertThat(
        inferRouterId(getConfigurationWithLoopback(ConcreteInterfaceAddress.parse("127.0.0.2/31"))),
        equalTo(Ip.parse("0.0.0.0")));
  }

  @Test
  public void testInferRouterID_MaxInterfaceIp() {
    Configuration c = new Configuration("test", ConfigurationFormat.CUMULUS_NCLU);
    org.batfish.datamodel.Interface.builder()
        .setName("eth1")
        .setOwner(c)
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/30"))
        .build();
    org.batfish.datamodel.Interface.builder()
        .setName("eth2")
        .setOwner(c)
        .setAddress(ConcreteInterfaceAddress.parse("2.2.2.2/30"))
        .build();

    assertThat(inferRouterId(c), equalTo(Ip.parse("2.2.2.2")));
  }

  @Test
  public void testAddOspfInterfaces_HasArea() {
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();
    Interface vsIface = new Interface("iface", CumulusInterfaceType.PHYSICAL, null, null);
    vsIface.getOrCreateOspf().setOspfArea(1L);
    ncluConfiguration.getInterfaces().put("iface", vsIface);

    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    org.batfish.datamodel.Interface viIface =
        org.batfish.datamodel.Interface.builder().setName("iface").setVrf(vrf).build();

    Map<String, org.batfish.datamodel.Interface> ifaceMap =
        ImmutableMap.of(viIface.getName(), viIface);
    addOspfInterfaces(ncluConfiguration, ifaceMap, "1", new Warnings());
    assertThat(viIface.getOspfAreaName(), equalTo(1L));
  }

  @Test
  public void testAddOspfInterfaces_NoArea() {
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();
    Interface vsIface = new Interface("iface", CumulusInterfaceType.PHYSICAL, null, null);
    ncluConfiguration.getInterfaces().put("iface", vsIface);
    vsIface.getOrCreateOspf();

    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    org.batfish.datamodel.Interface viIface =
        org.batfish.datamodel.Interface.builder().setName("iface").setVrf(vrf).build();

    assertNull(viIface.getOspfAreaName());
  }

  @Test
  public void testAddOspfInterfaces_NoNetworkType() {
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();
    Interface vsIface = new Interface("iface", CumulusInterfaceType.PHYSICAL, null, null);
    ncluConfiguration.getInterfaces().put("iface", vsIface);
    vsIface.getOrCreateOspf().setOspfArea(0L);

    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    org.batfish.datamodel.Interface viIface =
        org.batfish.datamodel.Interface.builder().setName("iface").setVrf(vrf).build();
    Map<String, org.batfish.datamodel.Interface> ifaceMap =
        ImmutableMap.of(viIface.getName(), viIface);

    addOspfInterfaces(ncluConfiguration, ifaceMap, "1", new Warnings());
    assertNull(viIface.getOspfNetworkType());
  }

  @Test
  public void testAddOspfInterfaces_NoPassiveInterface() {
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();
    Interface vsIface = new Interface("iface", CumulusInterfaceType.PHYSICAL, null, null);
    ncluConfiguration.getInterfaces().put("iface", vsIface);
    vsIface.getOrCreateOspf();

    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    org.batfish.datamodel.Interface viIface =
        org.batfish.datamodel.Interface.builder().setName("iface").setVrf(vrf).build();
    Map<String, org.batfish.datamodel.Interface> ifaceMap =
        ImmutableMap.of(viIface.getName(), viIface);

    addOspfInterfaces(ncluConfiguration, ifaceMap, "1", new Warnings());
    assertFalse(viIface.getOspfPassive());
  }

  @Test
  public void testAddOspfInterfaces_PassiveInterface() {
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();
    Interface vsIface = new Interface("iface", CumulusInterfaceType.PHYSICAL, null, null);
    ncluConfiguration.getInterfaces().put("iface", vsIface);
    OspfInterface ospf = vsIface.getOrCreateOspf();
    ospf.setOspfArea(0L);
    ospf.setPassive(true);

    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    org.batfish.datamodel.Interface viIface =
        org.batfish.datamodel.Interface.builder().setName("iface").setVrf(vrf).build();
    Map<String, org.batfish.datamodel.Interface> ifaceMap =
        ImmutableMap.of(viIface.getName(), viIface);

    addOspfInterfaces(ncluConfiguration, ifaceMap, "1", new Warnings());
    assertTrue(viIface.getOspfPassive());
  }

  @Test
  public void testAddOspfInterfaces_NetworkTypeP2P() {
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();
    Interface vsIface = new Interface("iface", CumulusInterfaceType.PHYSICAL, null, null);
    ncluConfiguration.getInterfaces().put("iface", vsIface);
    vsIface.getOrCreateOspf().setOspfArea(0L);
    vsIface.getOrCreateOspf().setNetwork(OspfNetworkType.POINT_TO_POINT);

    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    org.batfish.datamodel.Interface viIface =
        org.batfish.datamodel.Interface.builder().setName("iface").setVrf(vrf).build();
    Map<String, org.batfish.datamodel.Interface> ifaceMap =
        ImmutableMap.of(viIface.getName(), viIface);

    addOspfInterfaces(ncluConfiguration, ifaceMap, "1", new Warnings());
    assertThat(
        viIface.getOspfNetworkType(),
        equalTo(org.batfish.datamodel.ospf.OspfNetworkType.POINT_TO_POINT));
  }

  @Test
  public void testAddOspfInterfaces_HelloInterval() {
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();
    Interface vsIface = new Interface("iface", CumulusInterfaceType.PHYSICAL, null, null);
    ncluConfiguration.getInterfaces().put("iface", vsIface);
    vsIface.getOrCreateOspf().setOspfArea(0L);

    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    org.batfish.datamodel.Interface viIface =
        org.batfish.datamodel.Interface.builder().setName("iface").setVrf(vrf).build();
    Map<String, org.batfish.datamodel.Interface> ifaceMap =
        ImmutableMap.of(viIface.getName(), viIface);

    addOspfInterfaces(ncluConfiguration, ifaceMap, "1", new Warnings());

    // default hello interval
    assertThat(
        viIface.getOspfSettings().getHelloInterval(),
        equalTo(OspfInterface.DEFAULT_OSPF_HELLO_INTERVAL));

    // set hello interval
    vsIface.getOrCreateOspf().setHelloInterval(1);
    addOspfInterfaces(ncluConfiguration, ifaceMap, "1", new Warnings());
    assertThat(viIface.getOspfSettings().getHelloInterval(), equalTo(1));
  }

  @Test
  public void testAddOspfInterfaces_DeadInterval() {
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();
    Interface vsIface = new Interface("iface", CumulusInterfaceType.PHYSICAL, null, null);
    ncluConfiguration.getInterfaces().put("iface", vsIface);
    vsIface.getOrCreateOspf().setOspfArea(0L);

    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    org.batfish.datamodel.Interface viIface =
        org.batfish.datamodel.Interface.builder().setName("iface").setVrf(vrf).build();
    Map<String, org.batfish.datamodel.Interface> ifaceMap =
        ImmutableMap.of(viIface.getName(), viIface);

    addOspfInterfaces(ncluConfiguration, ifaceMap, "1", new Warnings());

    // default dead interval
    assertThat(
        viIface.getOspfSettings().getDeadInterval(),
        equalTo(OspfInterface.DEFAULT_OSPF_DEAD_INTERVAL));

    // set dead interval
    vsIface.getOrCreateOspf().setDeadInterval(1);
    addOspfInterfaces(ncluConfiguration, ifaceMap, "1", new Warnings());
    assertThat(viIface.getOspfSettings().getDeadInterval(), equalTo(1));
  }

  @Test
  public void testAddOspfInterfaces_ProcessId() {
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();
    Interface vsIface = new Interface("iface", CumulusInterfaceType.PHYSICAL, null, null);
    ncluConfiguration.getInterfaces().put("iface", vsIface);
    vsIface.getOrCreateOspf().setOspfArea(0L);

    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    org.batfish.datamodel.Interface viIface =
        org.batfish.datamodel.Interface.builder().setName("iface").setVrf(vrf).build();

    Map<String, org.batfish.datamodel.Interface> ifaceMap =
        ImmutableMap.of(viIface.getName(), viIface);
    addOspfInterfaces(ncluConfiguration, ifaceMap, "1", new Warnings());

    // default dead interval
    assertThat(viIface.getOspfSettings().getProcess(), equalTo("1"));
  }

  @Test
  public void testAddOspfInterfaces_NoInterface() {
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();

    Vrf vrf = new Vrf(DEFAULT_VRF_NAME);
    org.batfish.datamodel.Interface viIface =
        org.batfish.datamodel.Interface.builder().setName("iface").setVrf(vrf).build();

    Map<String, org.batfish.datamodel.Interface> ifaceMap =
        ImmutableMap.of(viIface.getName(), viIface);
    addOspfInterfaces(ncluConfiguration, ifaceMap, "1", new Warnings());

    assertNull(viIface.getOspfSettings());
  }

  @Test
  public void testComputeOspfProcess_HasArea() {
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();
    Interface vsIface = new Interface("iface", CumulusInterfaceType.PHYSICAL, null, null);
    vsIface.getOrCreateOspf().setOspfArea(1L);
    ncluConfiguration.getInterfaces().put("iface", vsIface);

    SortedMap<Long, OspfArea> areas =
        computeOspfAreas(ncluConfiguration, ImmutableList.of("iface"));
    assertThat(
        areas,
        equalTo(
            ImmutableSortedMap.of(
                1L, OspfArea.builder().addInterface("iface").setNumber(1L).build())));
  }

  @Test
  public void testComputeOspfProcess_NoArea() {
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();
    Interface vsIface = new Interface("iface", CumulusInterfaceType.PHYSICAL, null, null);
    ncluConfiguration.getInterfaces().put("iface", vsIface);
    vsIface.getOrCreateOspf();

    SortedMap<Long, OspfArea> areas =
        computeOspfAreas(ncluConfiguration, ImmutableList.of("iface"));
    assertThat(areas, equalTo(ImmutableSortedMap.of()));
  }

  @Test
  public void testComputeOspfProcess_NoInterface() {
    CumulusNcluConfiguration ncluConfiguration = new CumulusNcluConfiguration();

    SortedMap<Long, OspfArea> areas =
        computeOspfAreas(ncluConfiguration, ImmutableList.of("iface"));
    assertThat(areas, equalTo(ImmutableSortedMap.of()));
  }

  @Test
  public void testResolveLocalIpFromUpdateSource_Null() {
    Warnings warnings = new Warnings(false, true, false);

    // create vi config
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();

    assertNull(resolveLocalIpFromUpdateSource(null, c, warnings));
  }

  @Test
  public void testResolveLocalIpFromUpdateSource_Ip() {
    Warnings warnings = new Warnings(false, true, false);
    BgpNeighborSourceAddress source = new BgpNeighborSourceAddress(Ip.parse("1.1.1.1"));

    // create vi config
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();

    assertEquals(resolveLocalIpFromUpdateSource(source, c, warnings), Ip.parse("1.1.1.1"));
  }

  @Test
  public void testResolveLocalIpFromUpdateSource_Interface_NoInterface() {
    Warnings warnings = new Warnings(false, true, false);
    BgpNeighborSourceInterface source = new BgpNeighborSourceInterface("lo");

    // create vi config
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();

    assertNull(resolveLocalIpFromUpdateSource(source, c, warnings));
    assertThat(
        warnings.getRedFlagWarnings(),
        equalTo(
            ImmutableList.of(
                new Warning("cannot find interface named lo for update-source", TAG_RED_FLAG))));
  }

  @Test
  public void testResolveLocalIpFromUpdateSource_Interface_NoInterfaceAddress() {
    Warnings warnings = new Warnings(false, true, false);
    BgpNeighborSourceInterface source = new BgpNeighborSourceInterface("lo");

    // create vi config
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();

    nf.interfaceBuilder().setType(PHYSICAL).setOwner(c).setName("lo").build();

    assertNull(resolveLocalIpFromUpdateSource(source, c, warnings));
    assertThat(
        warnings.getRedFlagWarnings(),
        equalTo(
            ImmutableList.of(
                new Warning(
                    "cannot find an address for interface named lo for update-source",
                    TAG_RED_FLAG))));
  }

  @Test
  public void testResolveLocalIpFromUpdateSource_Interface() {
    Warnings warnings = new Warnings(false, true, false);
    BgpNeighborSourceInterface source = new BgpNeighborSourceInterface("lo");

    // create vi config
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CUMULUS_NCLU).build();

    nf.interfaceBuilder()
        .setType(PHYSICAL)
        .setOwner(c)
        .setName("lo")
        .setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/24"))
        .build();

    assertEquals(resolveLocalIpFromUpdateSource(source, c, warnings), Ip.parse("1.1.1.1"));
  }
}
