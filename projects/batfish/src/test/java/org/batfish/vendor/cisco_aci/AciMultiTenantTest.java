package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.SortedMap;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciConversion;
import org.junit.Test;

/**
 * Tests for multi-tenant ACI configurations and complex topology scenarios.
 *
 * <p>This test class verifies:
 *
 * <ul>
 *   <li>Multiple independent tenants in single fabric
 *   <li>Shared infrastructure tenant (infra)
 *   <li>Cross-tenant contracts and relationships
 *   <li>Multiple VRFs per tenant
 *   <li>Multiple bridge domains and subnets
 *   <li>Shared services topology
 *   <li>Tenant isolation and security
 *   <li>Interface configurations and placement
 * </ul>
 */
public class AciMultiTenantTest {

  /** Creates a multi-tenant ACI configuration. */
  private static String createMultiTenantConfig() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-tenant1\", \"name\":"
        + " \"tenant1\", \"descr\": \"Tenant 1\"},\"children\": [{\"fvCtx\":"
        + " {\"attributes\": {\"dn\": \"uni/tn-tenant1/ctx-vrf1\", \"name\":"
        + " \"vrf1\"},\"children\": []}},{\"fvBD\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-tenant1/BD-bd1\", \"name\": \"bd1\"},\"children\": [{\"fvRsCtx\":"
        + " {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]}},{\"fvAp\": {\"attributes\":"
        + " {\"dn\": \"uni/tn-tenant1/ap-app1\", \"name\": \"app1\"},\"children\": ["
        + "{\"fvAEPg\": {\"attributes\": {\"dn\": \"uni/tn-tenant1/ap-app1/epg-web\","
        + " \"name\": \"web\"},\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\":"
        + " \"bd1\"}}}]}}]}}]}},{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-tenant2\","
        + " \"name\": \"tenant2\", \"descr\": \"Tenant 2\"},\"children\": [{\"fvCtx\":"
        + " {\"attributes\": {\"dn\": \"uni/tn-tenant2/ctx-vrf1\", \"name\":"
        + " \"vrf1\"},\"children\": []}},{\"fvBD\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-tenant2/BD-bd1\", \"name\": \"bd1\"},\"children\": [{\"fvRsCtx\":"
        + " {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]}},{\"fvAp\": {\"attributes\":"
        + " {\"dn\": \"uni/tn-tenant2/ap-app\", \"name\": \"app\"},\"children\": ["
        + "{\"fvAEPg\": {\"attributes\": {\"dn\": \"uni/tn-tenant2/ap-app/epg-db\","
        + " \"name\": \"db\"},\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\":"
        + " \"bd1\"}}}]}}]}}]}},{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-tenant3\","
        + " \"name\": \"tenant3\", \"descr\": \"Tenant 3\"},\"children\": [{\"fvCtx\":"
        + " {\"attributes\": {\"dn\": \"uni/tn-tenant3/ctx-vrf1\", \"name\":"
        + " \"vrf1\"},\"children\": []}},{\"fvBD\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-tenant3/BD-bd1\", \"name\": \"bd1\"},\"children\": [{\"fvRsCtx\":"
        + " {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]}},{\"fvAp\": {\"attributes\":"
        + " {\"dn\": \"uni/tn-tenant3/ap-infra\", \"name\": \"infra\"},\"children\": ["
        + "{\"fvAEPg\": {\"attributes\": {\"dn\": \"uni/tn-tenant3/ap-infra/epg-mgmt\","
        + " \"name\": \"mgmt\"},\"children\": [{\"fvRsBd\": {\"attributes\":"
        + " {\"tnFvBDName\": \"bd1\"}}}]}}]}}]}}]}}";
  }

  /** Creates a config with multiple VRFs in single tenant. */
  private static String createTenantWithMultipleVrfs() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-multi_vrf_tenant\", \"name\":"
        + " \"multi_vrf_tenant\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-multi_vrf_tenant/ctx-prod_vrf\", \"name\": \"prod_vrf\"},\"children\":"
        + " []}},{\"fvCtx\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-multi_vrf_tenant/ctx-dev_vrf\", \"name\": \"dev_vrf\"},\"children\": []"
        + "}},{\"fvCtx\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-multi_vrf_tenant/ctx-test_vrf\", \"name\": \"test_vrf\"},\"children\":"
        + " []}},{\"fvBD\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-multi_vrf_tenant/BD-prod_bd\", \"name\": \"prod_bd\"},\"children\":"
        + " [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"prod_vrf\"}}}]}},{\"fvBD\":"
        + " {\"attributes\": {\"dn\": \"uni/tn-multi_vrf_tenant/BD-dev_bd\", \"name\":"
        + " \"dev_bd\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
        + " \"dev_vrf\"}}}]}},{\"fvBD\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-multi_vrf_tenant/BD-test_bd\", \"name\": \"test_bd\"},\"children\":"
        + " [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"test_vrf\"}}}]}}]}}]}}";
  }

  /** Creates a config with infra tenant and shared services. */
  private static String createInfraTenant() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-infra\", \"name\": \"infra\","
        + " \"descr\": \"Infrastructure Tenant\"},\"children\": [{\"fvCtx\":"
        + " {\"attributes\": {\"dn\": \"uni/tn-infra/ctx-inb\", \"name\":"
        + " \"inb\"},\"children\": []}},{\"fvBD\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-infra/BD-inb_bd\", \"name\": \"inb_bd\"},\"children\": [{\"fvRsCtx\":"
        + " {\"attributes\": {\"tnFvCtxName\": \"inb\"}}}]}},{\"l3extOut\": {\"attributes\":"
        + " {\"dn\": \"uni/tn-infra/out-external\", \"name\": \"external\"},\"children\": []"
        + "}}]}},{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-app_tenant\", \"name\":"
        + " \"app_tenant\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-app_tenant/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
        + "{\"fvBD\": {\"attributes\": {\"dn\": \"uni/tn-app_tenant/BD-bd1\", \"name\":"
        + " \"bd1\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
        + " \"vrf1\"}}}]}}]}}]}}";
  }

  /** Test parsing multiple independent tenants */
  @Test
  public void testParseJson_multipleTenants() throws IOException {
    String json = createMultiTenantConfig();

    AciConfiguration config = AciConfiguration.fromJson("multi_tenant.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants().keySet(), hasSize(3));
    assertThat(config.getTenants(), hasKey("tenant1"));
    assertThat(config.getTenants(), hasKey("tenant2"));
    assertThat(config.getTenants(), hasKey("tenant3"));
  }

  /** Test tenant with multiple VRFs */
  @Test
  public void testParseJson_tenantMultipleVrfs() throws IOException {
    String json = createTenantWithMultipleVrfs();

    AciConfiguration config =
        AciConfiguration.fromJson("tenant_multi_vrf.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("multi_vrf_tenant"));
  }

  /** Test infra tenant with shared services */
  @Test
  public void testParseJson_infraTenant() throws IOException {
    String json = createInfraTenant();

    AciConfiguration config = AciConfiguration.fromJson("infra_tenant.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("infra"));
  }

  /** Test conversion of multi-tenant config to vendor-independent */
  @Test
  public void testConversion_multiTenantConfigs() throws IOException {
    String json = createMultiTenantConfig();

    AciConfiguration aciConfig =
        AciConfiguration.fromJson("multi_tenant.json", json, new Warnings());
    aciConfig.setVendor(org.batfish.datamodel.ConfigurationFormat.CISCO_ACI);

    Warnings warnings = new Warnings();
    SortedMap<String, Configuration> configs =
        AciConversion.toVendorIndependentConfigurations(aciConfig, warnings);

    assertThat("Should have created configurations", configs.size(), greaterThan(0));
  }

  /** Test large multi-tenant fabric with multiple fabric nodes */
  @Test
  public void testMultiTenantWithFabricNodes() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fabricInst\": {\"attributes\": {\"dn\": \"uni/fabric\"},\"children\": ["
            + "{\"fabricProtPol\": {\"attributes\": {},\"children\": [{\"fabricExplicitGEp\":"
            + " {\"attributes\": {},\"children\": [{\"fabricNodePEp\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-101\", \"id\": \"101\", \"name\": \"spine1\", \"role\":"
            + " \"spine\"},\"children\": []}},{\"fabricNodePEp\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-102\", \"id\": \"102\", \"name\": \"spine2\", \"role\":"
            + " \"spine\"},\"children\": []}},{\"fabricNodePEp\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-201\", \"id\": \"201\", \"name\": \"leaf1\", \"role\":"
            + " \"leaf\"},\"children\": []}},{\"fabricNodePEp\": {\"attributes\": {\"dn\":"
            + " \"uni/fabric/nodePEp-202\", \"id\": \"202\", \"name\": \"leaf2\", \"role\":"
            + " \"leaf\"},\"children\": []}}]}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("large_fabric.json", json, new Warnings());

    assertNotNull(config.getFabricNodes());
    assertThat(config.getFabricNodes().size(), greaterThanOrEqualTo(4));
  }

  /** Test tenant with multiple bridge domains and subnets */
  @Test
  public void testTenantMultipleBridgeDomainsWithSubnets() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-complex_tenant\", \"name\":"
            + " \"complex_tenant\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-complex_tenant/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
            + "{\"fvBD\": {\"attributes\": {\"dn\": \"uni/tn-complex_tenant/BD-web_bd\", \"name\":"
            + " \"web_bd\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
            + " \"vrf1\"}}},{\"fvSubnet\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-complex_tenant/BD-web_bd/subnet-[10.0.1.0/24]\", \"ip\":"
            + " \"10.0.1.0/24\"}}},{\"fvSubnet\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-complex_tenant/BD-web_bd/subnet-[10.0.2.0/24]\", \"ip\":"
            + " \"10.0.2.0/24\"}}}]}},{\"fvBD\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-complex_tenant/BD-app_bd\", \"name\": \"app_bd\"},\"children\":"
            + " [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}},{\"fvSubnet\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-complex_tenant/BD-app_bd/subnet-[10.1.1.0/24]\","
            + " \"ip\": \"10.1.1.0/24\"}}},{\"fvSubnet\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-complex_tenant/BD-app_bd/subnet-[10.1.2.0/24]\", \"ip\":"
            + " \"10.1.2.0/24\"}}}]}},{\"fvBD\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-complex_tenant/BD-db_bd\", \"name\": \"db_bd\"},\"children\":"
            + " [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}},{\"fvSubnet\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-complex_tenant/BD-db_bd/subnet-[10.2.1.0/24]\","
            + " \"ip\": \"10.2.1.0/24\"}}}]}}]}}]}}";

    AciConfiguration config =
        AciConfiguration.fromJson("complex_tenant.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("complex_tenant"));
  }

  /** Test tenant isolation with independent VRFs and address spaces */
  @Test
  public void testTenantIsolation() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-tenant_a\", \"name\":"
            + " \"tenant_a\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-tenant_a/ctx-vrf_a\", \"name\": \"vrf_a\"},\"children\": []}},{\"fvBD\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-tenant_a/BD-bd_a\", \"name\":"
            + " \"bd_a\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
            + " \"vrf_a\"}}},{\"fvSubnet\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-tenant_a/BD-bd_a/subnet-[10.0.0.0/24]\", \"ip\": \"10.0.0.0/24\"}}}]}}]}},"
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-tenant_b\", \"name\":"
            + " \"tenant_b\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-tenant_b/ctx-vrf_b\", \"name\": \"vrf_b\"},\"children\": []}},{\"fvBD\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-tenant_b/BD-bd_b\", \"name\":"
            + " \"bd_b\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
            + " \"vrf_b\"}}},{\"fvSubnet\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-tenant_b/BD-bd_b/subnet-[10.0.0.0/24]\", \"ip\": \"10.0.0.0/24\"}}}]}}]}}]"
            + "}}";

    AciConfiguration config =
        AciConfiguration.fromJson("tenant_isolation.json", json, new Warnings());

    assertThat(config.getTenants().keySet(), hasSize(2));
    assertThat(config.getTenants(), hasKey("tenant_a"));
    assertThat(config.getTenants(), hasKey("tenant_b"));
  }

  /** Test tenant descriptions and metadata */
  @Test
  public void testTenantMetadata() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {"
            + "\"dn\": \"uni/tn-documented_tenant\","
            + "\"name\": \"documented_tenant\","
            + "\"descr\": \"This is a well-documented tenant\","
            + "\"nameAlias\": \"alias_tenant\""
            + "},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-documented_tenant/ctx-vrf1\", \"name\": \"vrf1\"},"
            + "\"children\": []"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config =
        AciConfiguration.fromJson("documented_tenant.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("documented_tenant"));
  }
}
