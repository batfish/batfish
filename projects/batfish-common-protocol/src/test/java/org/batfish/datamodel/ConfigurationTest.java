package org.batfish.datamodel;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.stream.Stream;
import org.batfish.common.Warnings;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.junit.Before;
import org.junit.Test;

public class ConfigurationTest {

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
    Prefix generatedRoutePrefix = Prefix.create(Ip.ZERO, Prefix.MAX_PREFIX_LENGTH);

    Configuration c = new Configuration("test", ConfigurationFormat.CISCO_IOS);
    Vrf vrf = c.getVrfs().computeIfAbsent(Configuration.DEFAULT_VRF_NAME, Vrf::new);

    // BGP
    BgpProcess bgpProcess = new BgpProcess(Ip.parse("1.1.1.1"), ConfigurationFormat.CISCO_IOS);
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
    OspfProcess ospfProcess = _factory.ospfProcessBuilder().build();
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
}
