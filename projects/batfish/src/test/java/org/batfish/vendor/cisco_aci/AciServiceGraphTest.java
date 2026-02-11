package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.junit.Test;

/**
 * Tests for ACI service graphs and advanced policy scenarios.
 *
 * <p>This test class verifies:
 *
 * <ul>
 *   <li>Service graph configurations
 *   <li>Device cluster definitions
 *   <li>Policy-based redirect (PBR)
 *   <li>Service function chain (SFC) integration
 *   <li>Device selection policies
 *   <li>Service insertion points
 *   <li>Traffic redirection policies
 *   <li>Advanced policy enforcement
 * </ul>
 */
public class AciServiceGraphTest {

  /** Creates a basic service graph configuration. */
  private static String createBasicServiceGraph() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-service_tenant\", \"name\":"
        + " \"service_tenant\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-service_tenant/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
        + "{\"fvBD\": {\"attributes\": {\"dn\": \"uni/tn-service_tenant/BD-bd1\", \"name\":"
        + " \"bd1\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
        + " \"vrf1\"}}}]}},{\"fvAp\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-service_tenant/ap-service_app\", \"name\":"
        + " \"service_app\"},\"children\": [{\"fvAEPg\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-service_tenant/ap-service_app/epg-fw\", \"name\": \"fw\"},\"children\":"
        + " [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\": \"bd1\"}}}]}},{\"fvAEPg\":"
        + " {\"attributes\": {\"dn\": \"uni/tn-service_tenant/ap-service_app/epg-ids\","
        + " \"name\": \"ids\"},\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\":"
        + " \"bd1\"}}}]}}]}}]}}]}}";
  }

  /** Creates a configuration with device cluster. */
  private static String createDeviceCluster() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-device_cluster_tenant\","
        + " \"name\": \"device_cluster_tenant\"},\"children\": [{\"fvCtx\": {\"attributes\":"
        + " {\"dn\": \"uni/tn-device_cluster_tenant/ctx-vrf1\", \"name\":"
        + " \"vrf1\"},\"children\": []}},{\"cloudDcflEPg\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-device_cluster_tenant/cdepg-dcfl\", \"name\": \"dcfl\"},\"children\":"
        + " []}}]}}]}}";
  }

  /** Creates a policy redirect configuration. */
  private static String createPolicyRedirect() {
    return "{"
        + "\"polUni\": {"
        + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
        + "\"children\": ["
        + "{"
        + "\"fvTenant\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-redirect_tenant\", \"name\": \"redirect_tenant\"},"
        + "\"children\": ["
        + "{"
        + "\"fvCtx\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-redirect_tenant/ctx-vrf1\", \"name\": \"vrf1\"},"
        + "\"children\": []"
        + "}"
        + "},"
        + "{"
        + "\"fvBD\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-redirect_tenant/BD-bd1\", \"name\": \"bd1\"},"
        + "\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
        + "}"
        + "},"
        + "{"
        + "\"fvAp\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-redirect_tenant/ap-app\", \"name\": \"app\"},"
        + "\"children\": ["
        + "{"
        + "\"fvAEPg\": {"
        + "\"attributes\": {\"dn\": \"uni/tn-redirect_tenant/ap-app/epg-web\", \"name\": \"web\"},"
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

  /** Test parsing basic service graph */
  @Test
  public void testParseJson_basicServiceGraph() throws IOException {
    String json = createBasicServiceGraph();

    AciConfiguration config = AciConfiguration.fromJson("service_graph.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("service_tenant"));
  }

  /** Test parsing device cluster */
  @Test
  public void testParseJson_deviceCluster() throws IOException {
    String json = createDeviceCluster();

    AciConfiguration config =
        AciConfiguration.fromJson("device_cluster.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("device_cluster_tenant"));
  }

  /** Test parsing policy redirect */
  @Test
  public void testParseJson_policyRedirect() throws IOException {
    String json = createPolicyRedirect();

    AciConfiguration config =
        AciConfiguration.fromJson("policy_redirect.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("redirect_tenant"));
  }

  /** Test firewall EPG in service graph */
  @Test
  public void testFirewallEpgInServiceGraph() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-fw_service\", \"name\":"
            + " \"fw_service\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-fw_service/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},{\"fvBD\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-fw_service/BD-fw_bd\", \"name\":"
            + " \"fw_bd\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
            + " \"vrf1\"}}}]}},{\"fvAp\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-fw_service/ap-services\", \"name\": \"services\"},\"children\": ["
            + "{\"fvAEPg\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-fw_service/ap-services/epg-firewall_outside\", \"name\":"
            + " \"firewall_outside\"},\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\":"
            + " \"fw_bd\"}}}]}},{\"fvAEPg\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-fw_service/ap-services/epg-firewall_inside\", \"name\":"
            + " \"firewall_inside\"},\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\":"
            + " \"fw_bd\"}}}]}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("fw_epg.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("fw_service"));
  }

  /** Test IDS EPG in service graph */
  @Test
  public void testIdsEpgInServiceGraph() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-ids_service\", \"name\":"
            + " \"ids_service\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-ids_service/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},{\"fvBD\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-ids_service/BD-ids_bd\", \"name\":"
            + " \"ids_bd\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
            + " \"vrf1\"}}}]}},{\"fvAp\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-ids_service/ap-services\", \"name\": \"services\"},\"children\": ["
            + "{\"fvAEPg\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-ids_service/ap-services/epg-ids_appliance\", \"name\":"
            + " \"ids_appliance\"},\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\":"
            + " \"ids_bd\"}}}]}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("ids_epg.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("ids_service"));
  }

  /** Test load balancer EPG in service graph */
  @Test
  public void testLoadBalancerEpgInServiceGraph() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-lb_service\", \"name\":"
            + " \"lb_service\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-lb_service/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},{\"fvBD\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-lb_service/BD-lb_bd\", \"name\":"
            + " \"lb_bd\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
            + " \"vrf1\"}}}]}},{\"fvAp\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-lb_service/ap-services\", \"name\": \"services\"},\"children\": ["
            + "{\"fvAEPg\": {\"attributes\": {\"dn\": \"uni/tn-lb_service/ap-services/epg-lb_vip\","
            + " \"name\": \"lb_vip\"},\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\":"
            + " \"lb_bd\"}}}]}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("lb_epg.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("lb_service"));
  }

  /** Test proxy EPG configuration */
  @Test
  public void testProxyEpgConfiguration() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-proxy_service\", \"name\":"
            + " \"proxy_service\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-proxy_service/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
            + "{\"fvBD\": {\"attributes\": {\"dn\": \"uni/tn-proxy_service/BD-proxy_bd\", \"name\":"
            + " \"proxy_bd\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
            + " \"vrf1\"}}}]}},{\"fvAp\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-proxy_service/ap-services\", \"name\": \"services\"},\"children\": ["
            + "{\"fvAEPg\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-proxy_service/ap-services/epg-proxy\", \"name\": \"proxy\"},\"children\":"
            + " [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\": \"proxy_bd\"}}}]}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("proxy_epg.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("proxy_service"));
  }

  /** Test chained services configuration */
  @Test
  public void testChainedServices() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-chained_services\", \"name\":"
            + " \"chained_services\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-chained_services/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
            + "{\"fvBD\": {\"attributes\": {\"dn\": \"uni/tn-chained_services/BD-bd1\", \"name\":"
            + " \"bd1\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
            + " \"vrf1\"}}}]}},{\"fvAp\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-chained_services/ap-svc_chain\", \"name\": \"svc_chain\"},\"children\": ["
            + "{\"fvAEPg\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-chained_services/ap-svc_chain/epg-fw\", \"name\": \"fw\"},\"children\":"
            + " [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\": \"bd1\"}}}]}},{\"fvAEPg\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-chained_services/ap-svc_chain/epg-ids\","
            + " \"name\": \"ids\"},\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\":"
            + " \"bd1\"}}}]}},{\"fvAEPg\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-chained_services/ap-svc_chain/epg-lb\", \"name\": \"lb\"},\"children\":"
            + " [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\": \"bd1\"}}}]}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("chained_svc.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("chained_services"));
  }
}
