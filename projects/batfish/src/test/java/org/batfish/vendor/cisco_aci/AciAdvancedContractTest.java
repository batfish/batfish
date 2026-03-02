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
 * Tests for advanced ACI contract and EPG scenarios.
 *
 * <p>This test class verifies:
 *
 * <ul>
 *   <li>Complex contract scenarios with multiple subjects
 *   <li>Contract provider and consumer relationships
 *   <li>EPG placement and assignment
 *   <li>Contract inheritance and overrides
 *   <li>Filter combinations and protocol specifications
 *   <li>Port ranges in contract filters
 *   <li>Stateful contract enforcement
 *   <li>Contract scope (tenant, VRF, application profile)
 * </ul>
 */
public class AciAdvancedContractTest {

  /** Creates a contract with multiple subjects and filters. */
  private static String createContractWithMultipleSubjects() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-app_tenant\", \"name\":"
        + " \"app_tenant\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-app_tenant/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
        + "{\"fvBD\": {\"attributes\": {\"dn\": \"uni/tn-app_tenant/BD-bd1\", \"name\":"
        + " \"bd1\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
        + " \"vrf1\"}}}]}},{\"fvAp\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-app_tenant/ap-web_app\", \"name\": \"web_app\"},\"children\": ["
        + "{\"fvAEPg\": {\"attributes\": {\"dn\": \"uni/tn-app_tenant/ap-web_app/epg-web\","
        + " \"name\": \"web\"},\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\":"
        + " \"bd1\"}}}]}},{\"fvAEPg\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-app_tenant/ap-web_app/epg-app\", \"name\": \"app\"},\"children\":"
        + " [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\": \"bd1\"}}}]}},{\"fvAEPg\":"
        + " {\"attributes\": {\"dn\": \"uni/tn-app_tenant/ap-web_app/epg-db\", \"name\":"
        + " \"db\"},\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\":"
        + " \"bd1\"}}}]}}]}},{\"vzBrCP\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-app_tenant/brc-web_to_app\", \"name\": \"web_to_app\", \"scope\":"
        + " \"tenant\"},\"children\": [{\"vzSubj\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-app_tenant/brc-web_to_app/subj-http\", \"name\":"
        + " \"http\"},\"children\": [{\"vzRsSubjFiltAtt\": {\"attributes\":"
        + " {\"tnVzFilterName\": \"allow_http\", \"action\": \"permit\"}}}]}},{\"vzSubj\":"
        + " {\"attributes\": {\"dn\": \"uni/tn-app_tenant/brc-web_to_app/subj-https\","
        + " \"name\": \"https\"},\"children\": [{\"vzRsSubjFiltAtt\": {\"attributes\":"
        + " {\"tnVzFilterName\": \"allow_https\", \"action\": \"permit\"}}}]}}]}},"
        + "{\"vzBrCP\": {\"attributes\": {\"dn\": \"uni/tn-app_tenant/brc-app_to_db\","
        + " \"name\": \"app_to_db\", \"scope\": \"tenant\"},\"children\": [{\"vzSubj\":"
        + " {\"attributes\": {\"dn\": \"uni/tn-app_tenant/brc-app_to_db/subj-db_access\","
        + " \"name\": \"db_access\"},\"children\": [{\"vzRsSubjFiltAtt\": {\"attributes\":"
        + " {\"tnVzFilterName\": \"db_traffic\", \"action\": \"permit\"}}}]}}]}},"
        + "{\"vzFilter\": {\"attributes\": {\"dn\": \"uni/tn-app_tenant/flt-allow_http\","
        + " \"name\": \"allow_http\"},\"children\": [{\"vzEntry\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-app_tenant/flt-allow_http/e-http\", \"name\": \"http\"},\"children\":"
        + " []}}]}},{\"vzFilter\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-app_tenant/flt-allow_https\", \"name\": \"allow_https\"},\"children\":"
        + " [{\"vzEntry\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-app_tenant/flt-allow_https/e-https\", \"name\":"
        + " \"https\"},\"children\": []}}]}},{\"vzFilter\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-app_tenant/flt-db_traffic\", \"name\": \"db_traffic\"},\"children\": ["
        + "{\"vzEntry\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-app_tenant/flt-db_traffic/e-sql\", \"name\": \"sql\"},\"children\": []}"
        + "}]}}]}}]}}";
  }

  /** Creates a contract with port ranges. */
  private static String createContractWithPortRanges() {
    return "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\":"
        + " [{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-port_tenant\", \"name\":"
        + " \"port_tenant\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-port_tenant/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
        + "{\"fvBD\": {\"attributes\": {\"dn\": \"uni/tn-port_tenant/BD-bd1\", \"name\":"
        + " \"bd1\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
        + " \"vrf1\"}}}]}},{\"fvAp\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-port_tenant/ap-app\", \"name\": \"app\"},\"children\": [{\"fvAEPg\":"
        + " {\"attributes\": {\"dn\": \"uni/tn-port_tenant/ap-app/epg-servers\", \"name\":"
        + " \"servers\"},\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\":"
        + " \"bd1\"}}}]}}]}},{\"vzBrCP\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-port_tenant/brc-port_range_contract\", \"name\":"
        + " \"port_range_contract\"},\"children\": [{\"vzSubj\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-port_tenant/brc-port_range_contract/subj-multi_port\", \"name\":"
        + " \"multi_port\"},\"children\": [{\"vzRsSubjFiltAtt\": {\"attributes\":"
        + " {\"tnVzFilterName\": \"port_range_filter\", \"action\": \"permit\"}}}]}}]}},"
        + "{\"vzFilter\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-port_tenant/flt-port_range_filter\", \"name\":"
        + " \"port_range_filter\"},\"children\": [{\"vzEntry\": {\"attributes\": {\"dn\":"
        + " \"uni/tn-port_tenant/flt-port_range_filter/e-tcp_range\", \"name\":"
        + " \"tcp_range\", \"protocol\": \"tcp\"},\"children\": []}}]}}]}}]}}";
  }

  /** Test parsing contract with multiple subjects */
  @Test
  public void testParseJson_contractMultipleSubjects() throws IOException {
    String json = createContractWithMultipleSubjects();

    AciConfiguration config =
        AciConfiguration.fromJson("contract_multi.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("app_tenant"));
  }

  /** Test parsing contract with port ranges */
  @Test
  public void testParseJson_contractPortRanges() throws IOException {
    String json = createContractWithPortRanges();

    AciConfiguration config =
        AciConfiguration.fromJson("contract_ports.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertThat(config.getTenants(), hasKey("port_tenant"));
  }

  /** Test EPG provider and consumer relationships */
  @Test
  public void testEpgProviderConsumerRelationship() throws IOException {
    String json = createContractWithMultipleSubjects();

    AciConfiguration config = AciConfiguration.fromJson("epg_relations.json", json, new Warnings());

    assertNotNull(config.getTenants().get("app_tenant"));
  }

  /** Test multiple EPGs in single application profile */
  @Test
  public void testMultipleEpgsInApplicationProfile() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi_epg\", \"name\": \"multi_epg\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi_epg/ctx-vrf1\", \"name\": \"vrf1\"},"
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi_epg/BD-bd1\", \"name\": \"bd1\"},"
            + "\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
            + "}"
            + "},"
            + "{"
            + "\"fvAp\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi_epg/ap-app\", \"name\": \"app\"},"
            + "\"children\": ["
            + "{"
            + "\"fvAEPg\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi_epg/ap-app/epg-epg1\", \"name\": \"epg1\"},"
            + "\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\": \"bd1\"}}}]"
            + "}"
            + "},"
            + "{"
            + "\"fvAEPg\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi_epg/ap-app/epg-epg2\", \"name\": \"epg2\"},"
            + "\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\": \"bd1\"}}}]"
            + "}"
            + "},"
            + "{"
            + "\"fvAEPg\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi_epg/ap-app/epg-epg3\", \"name\": \"epg3\"},"
            + "\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\": \"bd1\"}}}]"
            + "}"
            + "},"
            + "{"
            + "\"fvAEPg\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-multi_epg/ap-app/epg-epg4\", \"name\": \"epg4\"},"
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

    AciConfiguration config = AciConfiguration.fromJson("multi_epg_app.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("multi_epg"));
  }

  /** Test contract with different scopes */
  @Test
  public void testContractDifferentScopes() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-scope_test\", \"name\":"
            + " \"scope_test\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-scope_test/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},{\"vzBrCP\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-scope_test/brc-tenant_scope\", \"name\":"
            + " \"tenant_scope\", \"scope\": \"tenant\"},\"children\": [{\"vzSubj\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-scope_test/brc-tenant_scope/subj-s1\", \"name\":"
            + " \"s1\"}, \"children\": []}}]}},{\"vzBrCP\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-scope_test/brc-vrf_scope\", \"name\": \"vrf_scope\", \"scope\":"
            + " \"context\"},\"children\": [{\"vzSubj\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-scope_test/brc-vrf_scope/subj-s2\", \"name\": \"s2\"}, \"children\": []}}]"
            + "}}]}}]}}";

    AciConfiguration config =
        AciConfiguration.fromJson("contract_scopes.json", json, new Warnings());

    assertNotNull(config.getTenants().get("scope_test"));
  }

  /** Test EPG to bridge domain binding */
  @Test
  public void testEpgBridgeDomainBinding() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-bd_bind\", \"name\": \"bd_bind\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-bd_bind/ctx-vrf1\", \"name\": \"vrf1\"},"
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-bd_bind/BD-bd1\", \"name\": \"bd1\"},"
            + "\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
            + "}"
            + "},"
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-bd_bind/BD-bd2\", \"name\": \"bd2\"},"
            + "\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
            + "}"
            + "},"
            + "{"
            + "\"fvAp\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-bd_bind/ap-app\", \"name\": \"app\"},"
            + "\"children\": ["
            + "{"
            + "\"fvAEPg\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-bd_bind/ap-app/epg-web\", \"name\": \"web\"},"
            + "\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\": \"bd1\"}}}]"
            + "}"
            + "},"
            + "{"
            + "\"fvAEPg\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-bd_bind/ap-app/epg-app\", \"name\": \"app\"},"
            + "\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\": \"bd2\"}}}]"
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

    AciConfiguration config =
        AciConfiguration.fromJson("epg_bd_binding.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("bd_bind"));
  }

  /** Test contract with deny action */
  @Test
  public void testContractWithDenyAction() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-deny_contract\", \"name\":"
            + " \"deny_contract\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-deny_contract/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
            + "{\"vzBrCP\": {\"attributes\": {\"dn\": \"uni/tn-deny_contract/brc-deny_all\","
            + " \"name\": \"deny_all\"},\"children\": [{\"vzSubj\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-deny_contract/brc-deny_all/subj-s\", \"name\": \"s\"},\"children\":"
            + " [{\"vzRsSubjFiltAtt\": {\"attributes\": {\"tnVzFilterName\": \"deny_filter\","
            + " \"action\": \"deny\"}}}]}}]}},{\"vzFilter\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-deny_contract/flt-deny_filter\", \"name\": \"deny_filter\"},\"children\":"
            + " [{\"vzEntry\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-deny_contract/flt-deny_filter/e-all\", \"name\": \"all\"}, \"children\":"
            + " []}}]}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("contract_deny.json", json, new Warnings());

    assertNotNull(config.getTenants().get("deny_contract"));
  }

  /** Test EPG with multiple subnets via bridge domain */
  @Test
  public void testEpgMultipleSubnets() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-multi_subnet\", \"name\":"
            + " \"multi_subnet\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-multi_subnet/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},{\"fvBD\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-multi_subnet/BD-bd1\", \"name\":"
            + " \"bd1\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
            + " \"vrf1\"}}},{\"fvSubnet\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-multi_subnet/BD-bd1/subnet-[10.1.1.0/24]\", \"ip\":"
            + " \"10.1.1.0/24\"}}},{\"fvSubnet\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-multi_subnet/BD-bd1/subnet-[10.1.2.0/24]\", \"ip\": \"10.1.2.0/24\"}}}]}},"
            + "{\"fvAp\": {\"attributes\": {\"dn\": \"uni/tn-multi_subnet/ap-app\", \"name\":"
            + " \"app\"},\"children\": [{\"fvAEPg\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-multi_subnet/ap-app/epg-web\", \"name\": \"web\"},\"children\":"
            + " [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\": \"bd1\"}}}]}}]}}]}}]}}";

    AciConfiguration config =
        AciConfiguration.fromJson("epg_multi_subnet.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("multi_subnet"));
  }
}
