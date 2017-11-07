package org.batfish.datamodel;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isIn;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import org.batfish.common.Warnings;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.junit.Test;

public class ConfigurationTests {

  @Test
  public void testComputeRoutingPolicySources() {
    String bgpExportPolicyName = "bgpExportPolicy";
    String bgpImportPolicyName = "bgpImportPolicy";
    String bgpMissingExportPolicyName = "bgpMissingExportPolicy";
    String bgpMissingImportPolicyName = null;
    String generatedRouteAttributePolicyName = "generatedRouteAttributePolicy";
    String generatedRouteGenerationPolicyName = "generatedRouteGenerationPolicy";
    String ospfExportPolicyName = "ospfExportPolicy";
    String ospfExportSubPolicyName = "ospfExportSubPolicy";
    Prefix neighborPrefix = new Prefix(Ip.ZERO, Prefix.MAX_PREFIX_LENGTH);
    Prefix generatedRoutePrefix = neighborPrefix;
    Prefix neigborWithMissingPoliciesPrefix = new Prefix(Ip.MAX, Prefix.MAX_PREFIX_LENGTH);

    Configuration c = new Configuration("test");
    Vrf vrf = c.getVrfs().computeIfAbsent(Configuration.DEFAULT_VRF_NAME, Vrf::new);

    // BGP
    BgpProcess bgpProcess = new BgpProcess();
    vrf.setBgpProcess(bgpProcess);
    BgpNeighbor neighbor =
        bgpProcess.getNeighbors().computeIfAbsent(neighborPrefix, BgpNeighbor::new);
    neighbor.setExportPolicy(
        c.getRoutingPolicies()
            .computeIfAbsent(bgpExportPolicyName, n -> new RoutingPolicy(n, c))
            .getName());
    neighbor.setImportPolicy(
        c.getRoutingPolicies()
            .computeIfAbsent(bgpImportPolicyName, n -> new RoutingPolicy(n, c))
            .getName());
    BgpNeighbor neighborWithMissingPolicies =
        bgpProcess
            .getNeighbors()
            .computeIfAbsent(neigborWithMissingPoliciesPrefix, BgpNeighbor::new);
    neighborWithMissingPolicies.setExportPolicy(bgpMissingExportPolicyName);
    neighborWithMissingPolicies.setImportPolicy(bgpMissingImportPolicyName);

    // Generated route
    GeneratedRoute gr =
        new GeneratedRoute.Builder()
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
    OspfProcess ospfProcess = new OspfProcess();
    vrf.setOspfProcess(ospfProcess);
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
        neighbor.getExportPolicySources(), equalTo(Collections.singleton(bgpExportPolicyName)));
    assertThat(
        neighbor.getImportPolicySources(), equalTo(Collections.singleton(bgpImportPolicyName)));
    assertThat(
        neighborWithMissingPolicies.getExportPolicySources(), equalTo(Collections.emptySet()));
    assertThat(
        neighborWithMissingPolicies.getImportPolicySources(), equalTo(Collections.emptySet()));
    // Generated route tests
    assertThat(
        gr.getAttributePolicySources(),
        equalTo(Collections.singleton(generatedRouteAttributePolicyName)));
    assertThat(
        gr.getGenerationPolicySources(),
        equalTo(Collections.singleton(generatedRouteGenerationPolicyName)));
    // OSPF tests
    assertThat(ospfExportPolicyName, isIn(ospfProcess.getExportPolicySources()));
    assertThat(ospfExportSubPolicyName, isIn(ospfProcess.getExportPolicySources()));
  }
}
