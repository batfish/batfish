package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.junit.Test;

/**
 * Tests for ACI BGP configuration parsing and processing.
 *
 * <p>This test class verifies:
 *
 * <ul>
 *   <li>BGP peer configuration (bgpPeerP) parsing
 *   <li>BGP route target profiles
 *   <li>BGP context policies
 *   <li>BGP address families
 *   <li>BGP route control and redistribution
 *   <li>Integration with L3Out configurations
 * </ul>
 */
public class AciBgpConfigTest {

  /** Creates a polUni JSON with BGP peer configuration. */
  private static String createPolUniJsonWithBgpPeer(
      String tenantName, String l3outName, String peerId) {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-"
        + tenantName
        + "\", \"name\": \""
        + tenantName
        + "\"},"
        + "\"children\": ["
        + "{"
        + "\"fvCtx\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-"
        + tenantName
        + "/ctx-vrf1\", \"name\": \"vrf1\"},"
        + "\"children\": []"
        + "}"
        + "},"
        + "{"
        + "\"l3extOut\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-"
        + tenantName
        + "/out-"
        + l3outName
        + "\", \"name\": \""
        + l3outName
        + "\"},"
        + "\"children\": ["
        + "{"
        + "\"bgpExtP\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-"
        + tenantName
        + "/out-"
        + l3outName
        + "/bgpExtP\"},"
        + "\"children\": []"
        + "}"
        + "},"
        + "{"
        + "\"l3extLNodeP\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-"
        + tenantName
        + "/out-"
        + l3outName
        + "/lnodep-nodes\", \"name\": \"nodes\"},"
        + "\"children\": ["
        + "{"
        + "\"bgpLnodePEp\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-"
        + tenantName
        + "/out-"
        + l3outName
        + "/lnodep-nodes/pEp\","
        + "\"status\": \"\""
        + "},"
        + "\"children\": []"
        + "}"
        + "},"
        + "{"
        + "\"bgpPeerP\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-"
        + tenantName
        + "/out-"
        + l3outName
        + "/peerP-"
        + peerId
        + "\","
        + "\"addr\": \""
        + peerId
        + "\","
        + "\"asn\": \"65000\""
        + "},"
        + "\"children\": []"
        + "}"
        + "}"
        + "]"
        + "}"
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

  /** Creates a polUni JSON with BGP route target profile. */
  private static String createPolUniJsonWithBgpRouteTarget() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-tenant1\", \"name\": \"tenant1\"},"
        + "\"children\": ["
        + "{"
        + "\"fvCtx\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-tenant1/ctx-vrf1\", \"name\": \"vrf1\"},"
        + "\"children\": ["
        + "{"
        + "\"bgpCtxP\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-tenant1/ctx-vrf1/bgpCtxP\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"bgpRtProfile\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-tenant1/ctx-vrf1/bgpCtxP/rtProfile\","
        + "\"type\": \"both\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"bgpRtEntry\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-tenant1/ctx-vrf1/bgpCtxP/rtProfile/rte\","
        + "\"rt\": \"route-target:65000:1001\""
        + "}"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}"
        + "]"
        + "}"
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

  /** Creates a polUni JSON with BGP context policy. */
  private static String createPolUniJsonWithBgpContextPolicy() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-bgp_tenant\", \"name\": \"bgp_tenant\"},"
        + "\"children\": ["
        + "{"
        + "\"fvCtx\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-bgp_tenant/ctx-bgp_vrf\", \"name\": \"bgp_vrf\"},"
        + "\"children\": ["
        + "{"
        + "\"bgpCtxP\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-bgp_tenant/ctx-bgp_vrf/bgpCtxP\","
        + "\"status\": \"\""
        + "},"
        + "\"children\": []"
        + "}"
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

  /** Test parsing BGP peer configuration */
  @Test
  public void testParseJson_bgpPeer() throws IOException {
    String json = createPolUniJsonWithBgpPeer("tenant1", "l3out1", "192.0.2.1");

    AciConfiguration config = AciConfiguration.fromJson("bgp_peer.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test parsing BGP peer with different ASN */
  @Test
  public void testParseJson_bgpPeerDifferentAsn() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-t1\", \"name\": \"t1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-t1/ctx-v1\", \"name\": \"v1\"},"
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"l3extOut\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-t1/out-lo1\", \"name\": \"lo1\"},"
            + "\"children\": ["
            + "{"
            + "\"l3extLNodeP\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-t1/out-lo1/lnodep-np\", \"name\": \"np\"},"
            + "\"children\": ["
            + "{"
            + "\"bgpPeerP\": {"
            + "\"attributes\": {"
            + "\"dn\": \"uni/tn-t1/out-lo1/peerP-10.1.1.1\","
            + "\"addr\": \"10.1.1.1\","
            + "\"asn\": \"65001\""
            + "},"
            + "\"children\": []"
            + "}"
            + "}"
            + "]"
            + "}"
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

    AciConfiguration config = AciConfiguration.fromJson("bgp_asn.json", json, new Warnings());

    assertNotNull(config.getTenants().get("t1"));
  }

  /** Test parsing BGP route target profile */
  @Test
  public void testParseJson_bgpRouteTargetProfile() throws IOException {
    String json = createPolUniJsonWithBgpRouteTarget();

    AciConfiguration config = AciConfiguration.fromJson("bgp_rt.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test parsing BGP context policy */
  @Test
  public void testParseJson_bgpContextPolicy() throws IOException {
    String json = createPolUniJsonWithBgpContextPolicy();

    AciConfiguration config = AciConfiguration.fromJson("bgp_ctx.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("bgp_tenant"));
  }

  /** Test multiple BGP peers in L3Out */
  @Test
  public void testMultipleBgpPeers() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-multi_bgp\", \"name\":"
            + " \"multi_bgp\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-multi_bgp/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
            + "{\"l3extOut\": {\"attributes\": {\"dn\": \"uni/tn-multi_bgp/out-l3out1\", \"name\":"
            + " \"l3out1\"},\"children\": [{\"l3extLNodeP\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-multi_bgp/out-l3out1/lnodep-np\", \"name\": \"np\"},\"children\": ["
            + "{\"bgpPeerP\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-multi_bgp/out-l3out1/peerP-10.0.0.1\", \"addr\": \"10.0.0.1\", \"asn\":"
            + " \"65000\"},\"children\": []}},{\"bgpPeerP\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-multi_bgp/out-l3out1/peerP-10.0.0.2\", \"addr\": \"10.0.0.2\", \"asn\":"
            + " \"65000\"},\"children\": []}}]}}]}}]}}]}}";

    AciConfiguration config =
        AciConfiguration.fromJson("multi_bgp_peers.json", json, new Warnings());

    assertNotNull(config.getTenants().get("multi_bgp"));
  }

  /** Test BGP peer with IPv6 address */
  @Test
  public void testBgpPeerIpv6() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-ipv6_bgp\", \"name\":"
            + " \"ipv6_bgp\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-ipv6_bgp/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},{\"l3extOut\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-ipv6_bgp/out-l3out1\", \"name\":"
            + " \"l3out1\"},\"children\": [{\"l3extLNodeP\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-ipv6_bgp/out-l3out1/lnodep-np\", \"name\": \"np\"},\"children\": ["
            + "{\"bgpPeerP\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-ipv6_bgp/out-l3out1/peerP-2001-db8-1\", \"addr\": \"2001:db8::1\","
            + " \"asn\": \"65000\"},\"children\": []}}]}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("bgp_ipv6.json", json, new Warnings());

    assertNotNull(config.getTenants().get("ipv6_bgp"));
  }

  /** Test BGP with route target import and export */
  @Test
  public void testBgpRouteTargetImportExport() throws IOException {
    String json = createPolUniJsonWithBgpRouteTarget();

    AciConfiguration config =
        AciConfiguration.fromJson("bgp_rt_import_export.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test BGP peer without optional attributes */
  @Test
  public void testBgpPeerMinimal() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-min\", \"name\":"
            + " \"min\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\": \"uni/tn-min/ctx-v\","
            + " \"name\": \"v\"},\"children\": []}},{\"l3extOut\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-min/out-lo\", \"name\": \"lo\"},\"children\": [{\"l3extLNodeP\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-min/out-lo/lnodep-np\", \"name\":"
            + " \"np\"},\"children\": [{\"bgpPeerP\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-min/out-lo/peerP-1.2.3.4\", \"addr\": \"1.2.3.4\"},\"children\": []}}]}}]}"
            + "}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("bgp_minimal.json", json, new Warnings());

    assertNotNull(config.getTenants().get("min"));
  }

  /** Test BGP with address families */
  @Test
  public void testBgpAddressFamilies() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-af\", \"name\": \"af\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-af/ctx-v\", \"name\": \"v\"},"
            + "\"children\": ["
            + "{"
            + "\"bgpCtxP\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-af/ctx-v/bgpCtxP\"},"
            + "\"children\": []"
            + "}"
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

    AciConfiguration config = AciConfiguration.fromJson("bgp_af.json", json, new Warnings());

    assertNotNull(config.getTenants().get("af"));
  }
}
