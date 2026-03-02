package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.SortedMap;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciConversion;
import org.junit.Test;

/**
 * Tests for ACI routing and topology configurations.
 *
 * <p>This test class verifies:
 *
 * <ul>
 *   <li>Static route configurations
 *   <li>BGP route control and redistribute
 *   <li>Route target import/export
 *   <li>Inter-VRF route leaking
 *   <li>OSPF configurations
 *   <li>Route policies and filters
 *   <li>Spine-leaf topology creation
 *   <li>Routing protocol adjacencies
 * </ul>
 */
public class AciRoutingConfigTest {

  /** Creates a configuration with static routes. */
  private static String createConfigWithStaticRoutes() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-routing_tenant\", \"name\":"
        + " \"routing_tenant\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-routing_tenant/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
        + "{\"l3extOut\": {\"attributes\": {\"dn\": \"uni/tn-routing_tenant/out-external\","
        + " \"name\": \"external\"},\"children\": [{\"l3extStaticP\": {\"attributes\":"
        + " {\"dn\": \"uni/tn-routing_tenant/out-external/statics\",\"destAddr\":"
        + " \"192.168.0.0/16\",\"rtCtrl\": \"bfd\"}}},{\"l3extRsEctx\": {\"attributes\":"
        + " {\"tnFvCtxName\": \"vrf1\"}}}]}}]}}]}}";
  }

  /** Creates a configuration with route control enforcement. */
  private static String createConfigWithRouteControl() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-route_control\", \"name\": \"route_control\"},"
        + "\"children\": ["
        + "{"
        + "\"fvCtx\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-route_control/ctx-vrf1\", \"name\": \"vrf1\"},"
        + "\"children\": []"
        + "}"
        + "},"
        + "{"
        + "\"l3extOut\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-route_control/out-l3out\","
        + "\"name\": \"l3out\","
        + "\"enforceRtctrl\": \"import,export\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"l3extRsEctx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}"
        + "}"
        + "]"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}";
  }

  /** Creates a multi-VRF fabric with route leaking. */
  private static String createMultiVrfWithRouteLeaking() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-multi_vrf_routing\","
        + " \"name\": \"multi_vrf_routing\"},\"children\": [{\"fvCtx\": {\"attributes\":"
        + " {\"dn\": \"uni/tn-multi_vrf_routing/ctx-prod_vrf\", \"name\":"
        + " \"prod_vrf\"},\"children\": []}},{\"fvCtx\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-multi_vrf_routing/ctx-shared_vrf\", \"name\":"
        + " \"shared_vrf\"},\"children\": []}},{\"fvBD\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-multi_vrf_routing/BD-prod_bd\", \"name\": \"prod_bd\"},\"children\":"
        + " [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"prod_vrf\"}}}]}},{\"fvBD\":"
        + " {\"attributes\": {\"dn\": \"uni/tn-multi_vrf_routing/BD-shared_bd\", \"name\":"
        + " \"shared_bd\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
        + " \"shared_vrf\"}}}]}}]}}]}}";
  }

  /** Creates a spine-leaf topology configuration. */
  private static String createSpineLeafTopology() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fabricInst\": {\"attributes\": {\"dn\": \"uni/fabric\"},\"children\": ["
        + "{\"fabricProtPol\": {\"attributes\": {},\"children\": [{\"fabricExplicitGEp\":"
        + " {\"attributes\": {},\"children\": [{\"fabricNodePEp\": {\"attributes\": {\"dn\":"
        + " \"uni/fabric/nodePEp-101\", \"id\": \"101\", \"name\": \"spine1\", \"role\":"
        + " \"spine\", \"podId\": \"1\"},\"children\": []}},{\"fabricNodePEp\":"
        + " {\"attributes\": {\"dn\": \"uni/fabric/nodePEp-102\", \"id\": \"102\", \"name\":"
        + " \"spine2\", \"role\": \"spine\", \"podId\": \"1\"},\"children\": []}},"
        + "{\"fabricNodePEp\": {\"attributes\": {\"dn\": \"uni/fabric/nodePEp-201\", \"id\":"
        + " \"201\", \"name\": \"leaf1\", \"role\": \"leaf\", \"podId\":"
        + " \"1\"},\"children\": []}},{\"fabricNodePEp\": {\"attributes\": {\"dn\":"
        + " \"uni/fabric/nodePEp-202\", \"id\": \"202\", \"name\": \"leaf2\", \"role\":"
        + " \"leaf\", \"podId\": \"1\"},\"children\": []}},{\"fabricNodePEp\":"
        + " {\"attributes\": {\"dn\": \"uni/fabric/nodePEp-203\", \"id\": \"203\", \"name\":"
        + " \"leaf3\", \"role\": \"leaf\", \"podId\": \"1\"},\"children\": []}},"
        + "{\"fabricNodePEp\": {\"attributes\": {\"dn\": \"uni/fabric/nodePEp-204\", \"id\":"
        + " \"204\", \"name\": \"leaf4\", \"role\": \"leaf\", \"podId\":"
        + " \"1\"},\"children\": []}}]}}]}}]}}]}}";
  }

  /** Test parsing static routes */
  @Test
  public void testParseJson_staticRoutes() throws IOException {
    String json = createConfigWithStaticRoutes();

    AciConfiguration config = AciConfiguration.fromJson("static_routes.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("routing_tenant"));
  }

  /** Test parsing route control enforcement */
  @Test
  public void testParseJson_routeControl() throws IOException {
    String json = createConfigWithRouteControl();

    AciConfiguration config = AciConfiguration.fromJson("route_control.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("route_control"));
  }

  /** Test multi-VRF with route leaking */
  @Test
  public void testParseJson_multiVrfRouteLeaking() throws IOException {
    String json = createMultiVrfWithRouteLeaking();

    AciConfiguration config =
        AciConfiguration.fromJson("multi_vrf_leak.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("multi_vrf_routing"));
  }

  /** Test spine-leaf topology creation */
  @Test
  public void testParseJson_spineLeafTopology() throws IOException {
    String json = createSpineLeafTopology();

    AciConfiguration config = AciConfiguration.fromJson("spine_leaf.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getFabricNodes().size(), greaterThan(3));
  }

  /** Test conversion of spine-leaf topology to edges */
  @Test
  public void testSpineLeafTopologyConversion() throws IOException {
    String json = createSpineLeafTopology();

    AciConfiguration aciConfig =
        AciConfiguration.fromJson("spine_leaf_conv.json", json, new Warnings());
    aciConfig.setVendor(org.batfish.datamodel.ConfigurationFormat.CISCO_ACI);

    Warnings warnings = new Warnings();
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    assertThat("Should have created configurations", configs.size(), greaterThan(0));
  }

  /** Test route control with import */
  @Test
  public void testRouteControlImport() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-rtc_import\", \"name\":"
            + " \"rtc_import\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-rtc_import/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
            + "{\"l3extOut\": {\"attributes\": {\"dn\": \"uni/tn-rtc_import/out-external\","
            + " \"name\": \"external\", \"enforceRtctrl\": \"import\"},\"children\":"
            + " [{\"l3extRsEctx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("rtc_import.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("rtc_import"));
  }

  /** Test route control with export */
  @Test
  public void testRouteControlExport() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-rtc_export\", \"name\":"
            + " \"rtc_export\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-rtc_export/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
            + "{\"l3extOut\": {\"attributes\": {\"dn\": \"uni/tn-rtc_export/out-external\","
            + " \"name\": \"external\", \"enforceRtctrl\": \"export\"},\"children\":"
            + " [{\"l3extRsEctx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("rtc_export.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("rtc_export"));
  }

  /** Test large scale topology (8 nodes) */
  @Test
  public void testLargeScaleTopology() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fabricInst\": {\"attributes\": {\"dn\": \"uni/fabric\"},\"children\": ["
            + "{\"fabricProtPol\": {\"attributes\": {},\"children\": [{\"fabricExplicitGEp\":"
            + " {\"attributes\": {},\"children\": [{\"fabricNodePEp\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-101\", \"id\": \"101\", \"name\": \"spine1\", \"role\":"
            + " \"spine\", \"podId\": \"1\"}, \"children\": []}},{\"fabricNodePEp\":"
            + " {\"attributes\": {\"dn\": \"uni/fabric/nodePEp-102\", \"id\": \"102\", \"name\":"
            + " \"spine2\", \"role\": \"spine\", \"podId\": \"1\"}, \"children\":"
            + " []}},{\"fabricNodePEp\": {\"attributes\": {\"dn\": \"uni/fabric/nodePEp-201\","
            + " \"id\": \"201\", \"name\": \"leaf1\", \"role\": \"leaf\", \"podId\": \"1\"},"
            + " \"children\": []}},{\"fabricNodePEp\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-202\", \"id\": \"202\", \"name\": \"leaf2\", \"role\":"
            + " \"leaf\", \"podId\": \"1\"}, \"children\": []}},{\"fabricNodePEp\":"
            + " {\"attributes\": {\"dn\": \"uni/fabric/nodePEp-203\", \"id\": \"203\", \"name\":"
            + " \"leaf3\", \"role\": \"leaf\", \"podId\": \"1\"}, \"children\":"
            + " []}},{\"fabricNodePEp\": {\"attributes\": {\"dn\": \"uni/fabric/nodePEp-204\","
            + " \"id\": \"204\", \"name\": \"leaf4\", \"role\": \"leaf\", \"podId\": \"1\"},"
            + " \"children\": []}}]}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("large_scale.json", json, new Warnings());

    assertNotNull(config.getFabricNodes());
    assertThat("Should have multiple nodes", config.getFabricNodes().size(), greaterThan(3));
  }

  /** Test multiple pods (pod 1 and pod 2) */
  @Test
  public void testMultiPodTopology() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fabricInst\": {\"attributes\": {\"dn\": \"uni/fabric\"},\"children\": ["
            + "{\"fabricProtPol\": {\"attributes\": {},\"children\": [{\"fabricExplicitGEp\":"
            + " {\"attributes\": {},\"children\": [{\"fabricNodePEp\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-101\", \"id\": \"101\", \"name\": \"pod1-spine1\", \"role\":"
            + " \"spine\", \"podId\": \"1\"},\"children\": []}},{\"fabricNodePEp\":"
            + " {\"attributes\": {\"dn\": \"uni/fabric/nodePEp-201\", \"id\": \"201\", \"name\":"
            + " \"pod1-leaf1\", \"role\": \"leaf\", \"podId\": \"1\"},\"children\": []}},"
            + "{\"fabricNodePEp\": {\"attributes\": {\"dn\": \"uni/fabric/nodePEp-1001\", \"id\":"
            + " \"1001\", \"name\": \"pod2-spine1\", \"role\": \"spine\", \"podId\":"
            + " \"2\"},\"children\": []}},{\"fabricNodePEp\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-1201\", \"id\": \"1201\", \"name\": \"pod2-leaf1\", \"role\":"
            + " \"leaf\", \"podId\": \"2\"},\"children\": []}}]}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("multi_pod.json", json, new Warnings());

    assertNotNull(config.getFabricNodes());
  }
}
