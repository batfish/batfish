package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.community.StandardCommunity;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.AllStandardCommunities;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.specifier.InterfaceLocation;
import org.batfish.specifier.Location;
import org.batfish.specifier.LocationInfo;
import org.junit.Before;
import org.junit.Test;

/** Test of {@link Configuration}. */
public final class ConfigurationTest {

  private NetworkFactory _factory;

  @Before
  public void setup() {
    _factory = new NetworkFactory();
  }

  @Test
  public void testComputeRoutingPolicySources() {
    String bgpExportPolicyName = "bgpExportPolicy";
    String bgpImportPolicyName = "bgpImportPolicy";
    String bgpMissingExportPolicyName = "bgpMissingExportPolicy";
    String generatedRouteAttributePolicyName = "generatedRouteAttributePolicy";
    String generatedRouteGenerationPolicyName = "generatedRouteGenerationPolicy";
    String ospfExportPolicyName = "ospfExportPolicy";
    String ospfExportSubPolicyName = "ospfExportSubPolicy";
    Prefix generatedRoutePrefix = Ip.ZERO.toPrefix();

    Configuration c = new Configuration("test", ConfigurationFormat.CISCO_IOS);
    Vrf vrf = c.getVrfs().computeIfAbsent(Configuration.DEFAULT_VRF_NAME, Vrf::new);

    // BGP
    BgpProcess bgpProcess = BgpProcess.testBgpProcess(Ip.parse("1.1.1.1"));
    vrf.setBgpProcess(bgpProcess);
    BgpPeerConfig neighbor =
        _factory
            .bgpNeighborBuilder()
            .setPeerAddress(Ip.ZERO)
            .setBgpProcess(bgpProcess)
            .setIpv4UnicastAddressFamily(
                Ipv4UnicastAddressFamily.builder()
                    .setExportPolicy(
                        c.getRoutingPolicies()
                            .computeIfAbsent(bgpExportPolicyName, n -> new RoutingPolicy(n, c))
                            .getName())
                    .setImportPolicy(
                        c.getRoutingPolicies()
                            .computeIfAbsent(bgpImportPolicyName, n -> new RoutingPolicy(n, c))
                            .getName())
                    .build())
            .build();
    BgpPeerConfig neighborWithMissingPolicies =
        _factory
            .bgpNeighborBuilder()
            .setPeerAddress(Ip.MAX)
            .setBgpProcess(bgpProcess)
            .setIpv4UnicastAddressFamily(
                Ipv4UnicastAddressFamily.builder()
                    .setExportPolicy(bgpMissingExportPolicyName)
                    .build())
            .build();

    // Generated route
    GeneratedRoute gr =
        GeneratedRoute.builder()
            .setNetwork(generatedRoutePrefix)
            .setAttributePolicy(
                c.getRoutingPolicies()
                    .computeIfAbsent(
                        generatedRouteAttributePolicyName, n -> new RoutingPolicy(n, c))
                    .getName())
            .setGenerationPolicy(
                c.getRoutingPolicies()
                    .computeIfAbsent(
                        generatedRouteGenerationPolicyName, n -> new RoutingPolicy(n, c))
                    .getName())
            .build();
    vrf.getGeneratedRoutes().add(gr);

    // OSPF
    OspfProcess ospfProcess = _factory.ospfProcessBuilder().setRouterId(Ip.ZERO).build();
    vrf.setOspfProcesses(Stream.of(ospfProcess));
    RoutingPolicy ospfExportPolicy =
        c.getRoutingPolicies().computeIfAbsent(ospfExportPolicyName, n -> new RoutingPolicy(n, c));
    ospfProcess.setExportPolicy(ospfExportPolicyName);
    ospfExportPolicy
        .getStatements()
        .add(
            new CallStatement(
                c.getRoutingPolicies()
                    .computeIfAbsent(ospfExportSubPolicyName, n -> new RoutingPolicy(n, c))
                    .getName()));

    // Compute policy sources
    Warnings w = new Warnings();
    c.computeRoutingPolicySources(w);

    // BGP tests
    assertThat(
        neighbor.getIpv4UnicastAddressFamily().getExportPolicySources(),
        equalTo(Collections.singleton(bgpExportPolicyName)));
    assertThat(
        neighbor.getIpv4UnicastAddressFamily().getImportPolicySources(),
        equalTo(Collections.singleton(bgpImportPolicyName)));
    assertThat(
        neighborWithMissingPolicies.getIpv4UnicastAddressFamily().getExportPolicySources(),
        equalTo(Collections.emptySet()));
    assertThat(
        neighborWithMissingPolicies.getIpv4UnicastAddressFamily().getImportPolicySources(),
        equalTo(Collections.emptySet()));
    // Generated route tests
    assertThat(
        gr.getAttributePolicySources(),
        equalTo(Collections.singleton(generatedRouteAttributePolicyName)));
    assertThat(
        gr.getGenerationPolicySources(),
        equalTo(Collections.singleton(generatedRouteGenerationPolicyName)));
    // OSPF tests
    assertThat(
        ospfProcess.getExportPolicySources(),
        containsInAnyOrder(ospfExportPolicyName, ospfExportSubPolicyName));
  }

  @Test
  public void testJacksonSerialization() {
    // TODO: other properties
    Map<String, CommunityMatchExpr> communityMatchExprs =
        ImmutableMap.of("cme", AllStandardCommunities.instance());
    Map<String, CommunitySetExpr> communitySetExprs =
        ImmutableMap.of("cse", new LiteralCommunitySet(CommunitySet.of(StandardCommunity.of(2L))));
    Map<String, CommunitySetMatchExpr> communitySetMatchExprs =
        ImmutableMap.of("csme", new HasCommunity(AllStandardCommunities.instance()));
    Map<String, CommunitySet> communitySets =
        ImmutableMap.of("cs", CommunitySet.of(StandardCommunity.of(1L)));
    Configuration c = new Configuration("h", ConfigurationFormat.CISCO_IOS);
    c.setCommunityMatchExprs(communityMatchExprs);
    c.setCommunitySetExprs(communitySetExprs);
    c.setCommunitySetMatchExprs(communitySetMatchExprs);
    c.setCommunitySets(communitySets);
    c.setExportBgpFromBgpRib(true);
    Configuration cloned = BatfishObjectMapper.clone(c, Configuration.class);

    assertThat(cloned.getCommunityMatchExprs(), equalTo(communityMatchExprs));
    assertThat(cloned.getCommunitySetExprs(), equalTo(communitySetExprs));
    assertThat(cloned.getCommunitySetMatchExprs(), equalTo(communitySetMatchExprs));
    assertThat(cloned.getCommunitySets(), equalTo(communitySets));
  }

  @Test
  public void testJavaSerialization() {
    // TODO: other properties
    Map<String, CommunityMatchExpr> communityMatchExprs =
        ImmutableMap.of("cme", AllStandardCommunities.instance());
    Map<String, CommunitySetExpr> communitySetExprs =
        ImmutableMap.of("cse", new LiteralCommunitySet(CommunitySet.of(StandardCommunity.of(2L))));
    Map<String, CommunitySetMatchExpr> communitySetMatchExprs =
        ImmutableMap.of("csme", new HasCommunity(AllStandardCommunities.instance()));
    Map<String, CommunitySet> communitySets =
        ImmutableMap.of("cs", CommunitySet.of(StandardCommunity.of(1L)));
    Map<Location, LocationInfo> locationInfo =
        ImmutableMap.of(
            new InterfaceLocation("n", "i"),
            new LocationInfo(true, UniverseIpSpace.INSTANCE, EmptyIpSpace.INSTANCE));
    Configuration c = new Configuration("h", ConfigurationFormat.CISCO_IOS);
    c.setCommunityMatchExprs(communityMatchExprs);
    c.setCommunitySetExprs(communitySetExprs);
    c.setCommunitySetMatchExprs(communitySetMatchExprs);
    c.setCommunitySets(communitySets);
    c.setExportBgpFromBgpRib(true);
    c.setLocationInfo(locationInfo);
    Configuration cloned = SerializationUtils.clone(c);

    assertThat(cloned.getCommunityMatchExprs(), equalTo(communityMatchExprs));
    assertThat(cloned.getCommunitySetExprs(), equalTo(communitySetExprs));
    assertThat(cloned.getCommunitySetMatchExprs(), equalTo(communitySetMatchExprs));
    assertThat(cloned.getCommunitySets(), equalTo(communitySets));
    assertThat(cloned.getLocationInfo(), equalTo(locationInfo));
  }
}
