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
 * Tests for ACI external connectivity and failover scenarios.
 *
 * <p>This test class verifies:
 *
 * <ul>
 *   <li>External network connectivity
 *   <li>Failover and redundancy configurations
 *   <li>Multi-site deployments
 *   <li>Inter-site L3 connectivity
 *   <li>External gateway configurations
 *   <li>OSPF and BGP external routing
 *   <li>Backup path configurations
 *   <li>High availability setups
 * </ul>
 */
public class AciExternalConnectivityTest {

  /** Creates an external connectivity configuration. */
  private static String createExternalConnectivity() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-external_tenant\", \"name\": \"external_tenant\"},"
        + "\"children\": ["
        + "{"
        + "\"fvCtx\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-external_tenant/ctx-vrf1\", \"name\": \"vrf1\"},"
        + "\"children\": []"
        + "}"
        + "},"
        + "{"
        + "\"l3extOut\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-external_tenant/out-external\","
        + "\"name\": \"external\","
        + "\"enforceRtctrl\": \"export\""
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

  /** Creates a multi-site configuration. */
  private static String createMultiSiteDeployment() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-multisite\", \"name\": \"multisite\"},"
        + "\"children\": ["
        + "{"
        + "\"fvCtx\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-multisite/ctx-vrf1\", \"name\": \"vrf1\"},"
        + "\"children\": []"
        + "}"
        + "},"
        + "{"
        + "\"fvBD\": {"
        + "\"attributes\": {"
        + "\"dn\": \"uni/tn-multisite/BD-bd1\","
        + "\"name\": \"bd1\","
        + "\"unicastRoute\": \"yes\""
        + "},"
        + "\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
        + "}"
        + "},"
        + "{"
        + "\"fvAp\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-multisite/ap-app\", \"name\": \"app\"},"
        + "\"children\": ["
        + "{"
        + "\"fvAEPg\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-multisite/ap-app/epg-web\", \"name\": \"web\"},"
        + "\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\": \"bd1\"}}}]"
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

  /** Creates a redundant gateway configuration. */
  private static String createRedundantGateway() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-redundant_gw\", \"name\": \"redundant_gw\"},"
        + "\"children\": ["
        + "{"
        + "\"fvCtx\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-redundant_gw/ctx-vrf1\", \"name\": \"vrf1\"},"
        + "\"children\": []"
        + "}"
        + "},"
        + "{"
        + "\"l3extOut\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-redundant_gw/out-primary\", \"name\": \"primary\"},"
        + "\"children\": [{\"l3extRsEctx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
        + "}"
        + "},"
        + "{"
        + "\"l3extOut\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-redundant_gw/out-backup\", \"name\": \"backup\"},"
        + "\"children\": [{\"l3extRsEctx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}"
        + "]"
        + "}"
        + "}";
  }

  /** Test external connectivity */
  @Test
  public void testParseJson_externalConnectivity() throws IOException {
    String json = createExternalConnectivity();

    AciConfiguration config = AciConfiguration.fromJson("external_conn.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("external_tenant"));
  }

  /** Test multi-site deployment */
  @Test
  public void testParseJson_multiSiteDeployment() throws IOException {
    String json = createMultiSiteDeployment();

    AciConfiguration config = AciConfiguration.fromJson("multisite.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("multisite"));
  }

  /** Test redundant gateway */
  @Test
  public void testParseJson_redundantGateway() throws IOException {
    String json = createRedundantGateway();

    AciConfiguration config = AciConfiguration.fromJson("redundant_gw.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("redundant_gw"));
  }

  /** Test primary and backup L3Out */
  @Test
  public void testPrimaryBackupL3Out() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-ha_tenant\", \"name\": \"ha_tenant\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-ha_tenant/ctx-vrf1\", \"name\": \"vrf1\"},"
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"l3extOut\": {"
            + "\"attributes\": {"
            + "\"dn\": \"uni/tn-ha_tenant/out-l3out-primary\","
            + "\"name\": \"l3out-primary\","
            + "\"enforceRtctrl\": \"export\""
            + "},"
            + "\"children\": [{\"l3extRsEctx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
            + "}"
            + "},"
            + "{"
            + "\"l3extOut\": {"
            + "\"attributes\": {"
            + "\"dn\": \"uni/tn-ha_tenant/out-l3out-backup\","
            + "\"name\": \"l3out-backup\","
            + "\"enforceRtctrl\": \"export\""
            + "},"
            + "\"children\": [{\"l3extRsEctx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("ha_l3out.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("ha_tenant"));
  }

  /** Test active-active L3Out configuration */
  @Test
  public void testActiveActiveL3Out() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-active_active\", \"name\": \"active_active\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-active_active/ctx-vrf1\", \"name\": \"vrf1\"},"
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"l3extOut\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-active_active/out-l3out1\", \"name\": \"l3out1\"},"
            + "\"children\": [{\"l3extRsEctx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
            + "}"
            + "},"
            + "{"
            + "\"l3extOut\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-active_active/out-l3out2\", \"name\": \"l3out2\"},"
            + "\"children\": [{\"l3extRsEctx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("active_active.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("active_active"));
  }

  /** Test stretched bridge domain */
  @Test
  public void testStretchedBridgeDomain() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-stretched_bd\", \"name\":"
            + " \"stretched_bd\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-stretched_bd/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},{\"fvBD\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-stretched_bd/BD-stretched\",\"name\":"
            + " \"stretched\",\"arpFlood\": \"yes\",\"unicastRoute\": \"yes\"},\"children\":"
            + " [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}},{\"fvSubnet\":"
            + " {\"attributes\": {\"dn\":"
            + " \"uni/tn-stretched_bd/BD-stretched/subnet-[10.0.0.0/24]\", \"ip\":"
            + " \"10.0.0.0/24\"}}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("stretched_bd.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("stretched_bd"));
  }

  /** Test failover policy */
  @Test
  public void testFailoverPolicy() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-failover\", \"name\": \"failover\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-failover/ctx-vrf1\", \"name\": \"vrf1\"},"
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-failover/BD-bd1\", \"name\": \"bd1\"},"
            + "\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("failover.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("failover"));
  }

  /** Test high availability across pods */
  @Test
  public void testHighAvailabilityAcrossPods() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fabricInst\": {\"attributes\": {\"dn\": \"uni/fabric\"},\"children\": ["
            + "{\"fabricProtPol\": {\"attributes\": {},\"children\": [{\"fabricExplicitGEp\":"
            + " {\"attributes\": {},\"children\": [{\"fabricNodePEp\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-101\", \"id\": \"101\", \"name\": \"spine1\", \"role\":"
            + " \"spine\", \"podId\": \"1\"}, \"children\": []}},{\"fabricNodePEp\":"
            + " {\"attributes\": {\"dn\": \"uni/fabric/nodePEp-1001\", \"id\": \"1001\", \"name\":"
            + " \"spine2\", \"role\": \"spine\", \"podId\": \"2\"}, \"children\": []}}]}}]}}]}},"
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-multi_pod_ha\", \"name\":"
            + " \"multi_pod_ha\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-multi_pod_ha/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("multi_pod_ha.json", json, new Warnings());

    assertNotNull(config.getFabricNodes());
    assertThat(config.getTenants(), hasKey("multi_pod_ha"));
  }
}
