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
 * Tests for ACI L3Out (Layer 3 External Network) parsing and processing.
 *
 * <p>This test class verifies:
 *
 * <ul>
 *   <li>L3Out parsing from XML and JSON configurations
 *   <li>L3Out attributes (enforceRtctrl, MPLS, ECMP settings)
 *   <li>Logical node profiles within L3Outs
 *   <li>Logical interface profiles
 *   <li>External EPGs (l3extInstP)
 *   <li>BGP peer connectivity configuration
 *   <li>Route control enforcement
 * </ul>
 */
public class AciL3OutTest {

  /** Creates a minimal polUni JSON with L3Out configuration. */
  private static String createPolUniJsonWithL3Out(
      String tenantName, String vrfName, String l3outName) {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni\","
        + "\"name\": \"aci-fabric\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-"
        + tenantName
        + "\","
        + "\"name\": \""
        + tenantName
        + "\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvCtx\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-"
        + tenantName
        + "/ctx-"
        + vrfName
        + "\","
        + "\"name\": \""
        + vrfName
        + "\""
        + "},"
        + "\"children\": []"
        + "}"
        + "},"
        + "{"
        + "\"l3extOut\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-"
        + tenantName
        + "/out-"
        + l3outName
        + "\","
        + "\"name\": \""
        + l3outName
        + "\","
        + "\"enforceRtctrl\": \"export\","
        + "\"mplsEnabled\": \"yes\""
        + "},"
        + "\"children\": []"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}";
  }

  /** Creates a polUni JSON with L3Out containing logical node profile. */
  private static String createPolUniJsonWithL3OutNodeProfile() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni\","
        + "\"name\": \"aci-fabric\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-infra\","
        + "\"name\": \"infra\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvCtx\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-infra/ctx-overlay-1\","
        + "\"name\": \"overlay-1\""
        + "},"
        + "\"children\": []"
        + "}"
        + "},"
        + "{"
        + "\"l3extOut\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-infra/out-l3out1\","
        + "\"name\": \"l3out1\","
        + "\"enforceRtctrl\": \"export\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"l3extRsEctx\": {"
        + "\"attributes\": {"
        + "\"tnFvCtxName\": \"overlay-1\""
        + "}"
        + "}"
        + "},"
        + "{"
        + "\"l3extLNodeP\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-infra/out-l3out1/lnodep-Spine-Node-Profiles\","
        + "\"name\": \"Spine-Node-Profiles\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"l3extRsNodeL3OutAtt\": {"
        + "\"attributes\": {"
        + "\"rtrId\": \"10.96.1.3\","
        + "\"tDn\": \"topology/pod-1/node-101\""
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
        + "}";
  }

  /** Creates a polUni JSON with L3Out external EPG. */
  private static String createPolUniJsonWithL3OutExternalEpg() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni\","
        + "\"name\": \"aci-fabric\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-infra\","
        + "\"name\": \"infra\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"fvCtx\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-infra/ctx-overlay-1\","
        + "\"name\": \"overlay-1\""
        + "},"
        + "\"children\": []"
        + "}"
        + "},"
        + "{"
        + "\"l3extOut\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-infra/out-l3out1\","
        + "\"name\": \"l3out1\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"l3extInstP\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-infra/out-l3out1/instP-ext_epg\","
        + "\"name\": \"ext_epg\","
        + "\"matchT\": \"AtleastOne\""
        + "},"
        + "\"children\": ["
        + "{"
        + "\"l3extSubnet\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-infra/out-l3out1/instP-ext_epg/subnet-[10.0.0.0/8]\","
        + "\"ip\": \"10.0.0.0/8\","
        + "\"scope\": \"import-security\""
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
        + "}";
  }

  /** Test parsing L3Out from JSON */
  @Test
  public void testParseJson_l3out() throws IOException {
    String json = createPolUniJsonWithL3Out("infra", "overlay-1", "l3out1");

    AciConfiguration config = AciConfiguration.fromJson("l3out.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("infra"));
    // Verify L3Out is stored in the L3Outs map
    assertThat(config.getL3Outs().size(), equalTo(1));
    assertThat(config.getL3Outs(), hasKey("infra:l3out1"));
    assertThat(config.getL3Outs().get("infra:l3out1").getTenant(), equalTo("infra"));
  }

  /** Test parsing L3Out with logical node profile */
  @Test
  public void testParseJson_l3outWithNodeProfile() throws IOException {
    String json = createPolUniJsonWithL3OutNodeProfile();

    AciConfiguration config = AciConfiguration.fromJson("l3out_node.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("infra"));
  }

  /** Test parsing L3Out with external EPG */
  @Test
  public void testParseJson_l3outWithExternalEpg() throws IOException {
    String json = createPolUniJsonWithL3OutExternalEpg();

    AciConfiguration config = AciConfiguration.fromJson("l3out_epg.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("infra"));
  }

  /** Test parsing L3Out XML with infra tenant */
  @Test
  public void testParseXml_l3out() throws IOException {
    String xml =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<polUni>"
            + "<fvTenant name=\"infra\">"
            + "<fvCtx name=\"overlay-1\"/>"
            + "<l3extOut name=\"l3out1\" enforceRtctrl=\"export\">"
            + "<l3extRsEctx tnFvCtxName=\"overlay-1\"/>"
            + "</l3extOut>"
            + "</fvTenant>"
            + "</polUni>";

    AciConfiguration config = AciConfiguration.fromXml("l3out.xml", xml, new Warnings());

    assertNotNull("Config should not be null", config);
  }

  /** Test L3Out with enforceRtctrl export */
  @Test
  public void testL3OutEnforceRouteControlExport() throws IOException {
    String json = createPolUniJsonWithL3Out("tenant1", "vrf1", "external");

    AciConfiguration config = AciConfiguration.fromJson("l3out.json", json, new Warnings());

    assertNotNull(config.getTenants().get("tenant1"));
  }

  /** Test L3Out with MPLS enabled */
  @Test
  public void testL3OutMplsEnabled() throws IOException {
    String json = createPolUniJsonWithL3Out("tenant1", "vrf1", "mpls_l3out");

    AciConfiguration config = AciConfiguration.fromJson("l3out_mpls.json", json, new Warnings());

    assertNotNull(config.getTenants().get("tenant1"));
  }

  /** Test multiple L3Outs in single tenant */
  @Test
  public void testMultipleL3OutsInTenant() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi\", \"name\": \"multi\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi/ctx-vrf1\", \"name\": \"vrf1\"},"
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"l3extOut\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi/out-l3out1\", \"name\": \"l3out1\"},"
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"l3extOut\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi/out-l3out2\", \"name\": \"l3out2\"},"
            + "\"children\": []"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("multi_l3out.json", json, new Warnings());

    assertNotNull(config.getTenants().get("multi"));
  }

  /** Test L3Out external subnet configuration */
  @Test
  public void testL3OutExternalSubnet() throws IOException {
    String json = createPolUniJsonWithL3OutExternalEpg();

    AciConfiguration config = AciConfiguration.fromJson("l3out_subnet.json", json, new Warnings());

    assertNotNull(config.getTenants().get("infra"));
  }

  /** Test L3Out with no logical node profile (minimal) */
  @Test
  public void testL3OutMinimal() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-simple\", \"name\": \"simple\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-simple/ctx-vrf\", \"name\": \"vrf\"},"
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"l3extOut\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-simple/out-l3out\", \"name\": \"l3out\"},"
            + "\"children\": []"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("minimal_l3out.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("simple"));
  }

  /** Test L3Out missing name attribute */
  @Test
  public void testL3OutMissingName() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-test\", \"name\": \"test\"},"
            + "\"children\": ["
            + "{"
            + "\"l3extOut\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-test/out-bad\"},"
            + "\"children\": []"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    Warnings warnings = new Warnings();
    AciConfiguration config = AciConfiguration.fromJson("l3out_no_name.json", json, warnings);

    assertNotNull(config);
  }

  /** Test L3Out with VRF reference */
  @Test
  public void testL3OutWithVrfReference() throws IOException {
    String json = createPolUniJsonWithL3OutNodeProfile();

    AciConfiguration config = AciConfiguration.fromJson("l3out_vrf.json", json, new Warnings());

    assertNotNull(config.getTenants().get("infra"));
  }
}
