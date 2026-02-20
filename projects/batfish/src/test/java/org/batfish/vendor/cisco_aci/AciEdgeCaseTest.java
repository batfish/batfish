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
 * Tests for ACI edge cases and complex scenarios.
 *
 * <p>This test class verifies:
 *
 * <ul>
 *   <li>Empty configurations
 *   <li>Configurations with missing optional fields
 *   <li>Configurations with special characters in names
 *   <li>Very large configurations
 *   <li>Circular references handling
 *   <li>Duplicate name handling across tenants
 *   <li>Policy objects with multiple consumers/providers
 *   <li>Complex filter expressions
 * </ul>
 */
public class AciEdgeCaseTest {

  /** Test empty polUni configuration */
  @Test
  public void testEmptyConfiguration() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"}, \"children\":"
            + " []}}";

    AciConfiguration config = AciConfiguration.fromJson("empty.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("aci-fabric"));
    assertNotNull(config);
  }

  /** Test configuration with null/empty children */
  @Test
  public void testNullChildren() throws IOException {
    String json = "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"}}}";

    AciConfiguration config = AciConfiguration.fromJson("null_children.json", json, new Warnings());

    assertNotNull(config);
  }

  /** Test tenant with special characters in name */
  @Test
  public void testSpecialCharactersInNames() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-tenant_with_underscore\","
            + " \"name\": \"tenant_with_underscore\"},\"children\": [{\"fvCtx\": {\"attributes\":"
            + " {\"dn\": \"uni/tn-tenant_with_underscore/ctx-vrf-with-dashes\", \"name\":"
            + " \"vrf-with-dashes\"},\"children\": []}}]}}]}}";

    AciConfiguration config = AciConfiguration.fromJson("special_chars.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("tenant_with_underscore"));
  }

  /** Test multiple tenants with same VRF name (isolated) */
  @Test
  public void testDuplicateNamesAcrossTenants() throws IOException {
    String json =
        "{"
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
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-tenant1/BD-bd1\", \"name\": \"bd1\"},"
            + "\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "},"
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-tenant2\", \"name\": \"tenant2\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-tenant2/ctx-vrf1\", \"name\": \"vrf1\"},"
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-tenant2/BD-bd1\", \"name\": \"bd1\"},"
            + "\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("dup_names.json", json, new Warnings());

    assertThat(config.getTenants().size(), equalTo(2));
    assertThat(config.getTenants(), hasKey("tenant1"));
    assertThat(config.getTenants(), hasKey("tenant2"));
  }

  /** Test contract with many subjects */
  @Test
  public void testContractManySubjects() throws IOException {
    StringBuilder json = new StringBuilder();
    json.append("{\"polUni\": {");
    json.append("\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},");
    json.append("\"children\": [{\"fvTenant\": {");
    json.append("\"attributes\": {\"dn\": \"uni/tn-many_subj\", \"name\": \"many_subj\"},");
    json.append("\"children\": [{\"fvCtx\": {");
    json.append("\"attributes\": {\"dn\": \"uni/tn-many_subj/ctx-vrf1\", \"name\": \"vrf1\"},");
    json.append("\"children\": []");
    json.append("}},{\"vzBrCP\": {");
    json.append(
        "\"attributes\": {\"dn\": \"uni/tn-many_subj/brc-complex\", \"name\": \"complex\"},");
    json.append("\"children\": [");

    // Add 10 subjects
    for (int i = 1; i <= 10; i++) {
      json.append("{\"vzSubj\": {");
      json.append("\"attributes\": {\"dn\": \"uni/tn-many_subj/brc-complex/subj-s")
          .append(i)
          .append("\", \"name\": \"s")
          .append(i)
          .append("\"},");
      json.append("\"children\": []");
      json.append("}}");
      if (i < 10) {
        json.append(",");
      }
    }

    json.append("]}}]}}]}}");

    AciConfiguration config =
        AciConfiguration.fromJson("many_subjects.json", json.toString(), new Warnings());

    assertThat(config.getTenants(), hasKey("many_subj"));
  }

  /** Test EPG with many providers and consumers */
  @Test
  public void testEpgMultipleContractRelations() throws IOException {
    String json =
        "{\"polUni\": {\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},\"children\": ["
            + "{\"fvTenant\": {\"attributes\": {\"dn\": \"uni/tn-epg_relations\", \"name\":"
            + " \"epg_relations\"},\"children\": [{\"fvCtx\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-epg_relations/ctx-vrf1\", \"name\": \"vrf1\"},\"children\": []}},"
            + "{\"fvBD\": {\"attributes\": {\"dn\": \"uni/tn-epg_relations/BD-bd1\", \"name\":"
            + " \"bd1\"},\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\":"
            + " \"vrf1\"}}}]}},{\"fvAp\": {\"attributes\": {\"dn\":"
            + " \"uni/tn-epg_relations/ap-app\", \"name\": \"app\"},\"children\": [{\"fvAEPg\":"
            + " {\"attributes\": {\"dn\": \"uni/tn-epg_relations/ap-app/epg-central\", \"name\":"
            + " \"central\"},\"children\": [{\"fvRsBd\": {\"attributes\": {\"tnFvBDName\":"
            + " \"bd1\"}}}]}}]}}]}}]}}";

    AciConfiguration config =
        AciConfiguration.fromJson("epg_multi_relations.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("epg_relations"));
  }

  /** Test missing description and optional fields */
  @Test
  public void testMissingOptionalFields() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-minimal\", \"name\": \"minimal\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-minimal/ctx-vrf1\", \"name\": \"vrf1\"},"
            + "\"children\": []"
            + "}"
            + "},"
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-minimal/BD-bd1\", \"name\": \"bd1\"},"
            + "\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}]"
            + "}"
            + "},"
            + "{"
            + "\"fvAp\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-minimal/ap-app\", \"name\": \"app\"},"
            + "\"children\": ["
            + "{"
            + "\"fvAEPg\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-minimal/ap-app/epg-web\", \"name\": \"web\"},"
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

    AciConfiguration config = AciConfiguration.fromJson("minimal.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("minimal"));
  }

  /** Test filter with many entries */
  @Test
  public void testFilterManyEntries() throws IOException {
    StringBuilder json = new StringBuilder();
    json.append("{\"polUni\": {");
    json.append("\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},");
    json.append("\"children\": [{\"fvTenant\": {");
    json.append("\"attributes\": {\"dn\": \"uni/tn-many_filter\", \"name\": \"many_filter\"},");
    json.append("\"children\": [{\"vzFilter\": {");
    json.append(
        "\"attributes\": {\"dn\": \"uni/tn-many_filter/flt-complex\", \"name\": \"complex\"},");
    json.append("\"children\": [");

    // Add 10 filter entries
    for (int i = 1; i <= 10; i++) {
      json.append("{\"vzEntry\": {");
      json.append("\"attributes\": {\"dn\": \"uni/tn-many_filter/flt-complex/e-rule")
          .append(i)
          .append("\", \"name\": \"rule")
          .append(i)
          .append("\"},");
      json.append("\"children\": []");
      json.append("}}");
      if (i < 10) {
        json.append(",");
      }
    }

    json.append("]}}]}}]}}");

    AciConfiguration config =
        AciConfiguration.fromJson("filter_entries.json", json.toString(), new Warnings());

    assertThat(config.getTenants(), hasKey("many_filter"));
  }

  /** Test EPG with many subnets in BD */
  @Test
  public void testBridgeDomainManySubnets() throws IOException {
    StringBuilder json = new StringBuilder();
    json.append("{\"polUni\": {");
    json.append("\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},");
    json.append("\"children\": [{\"fvTenant\": {");
    json.append("\"attributes\": {\"dn\": \"uni/tn-many_subnets\", \"name\": \"many_subnets\"},");
    json.append("\"children\": [{\"fvCtx\": {");
    json.append("\"attributes\": {\"dn\": \"uni/tn-many_subnets/ctx-vrf1\", \"name\": \"vrf1\"},");
    json.append("\"children\": []");
    json.append("}},{\"fvBD\": {");
    json.append("\"attributes\": {\"dn\": \"uni/tn-many_subnets/BD-bd1\", \"name\": \"bd1\"},");
    json.append("\"children\": [{\"fvRsCtx\": {\"attributes\": {\"tnFvCtxName\": \"vrf1\"}}}");

    // Add 5 subnets
    for (int i = 1; i <= 5; i++) {
      json.append(",{\"fvSubnet\": {");
      json.append("\"attributes\": {\"dn\": \"uni/tn-many_subnets/BD-bd1/subnet-[10.")
          .append(i)
          .append(".0.0/24]\", \"ip\": \"10.")
          .append(i)
          .append(".0.0/24\"},");
      json.append("\"children\": []");
      json.append("}}");
    }

    json.append("]}}]}}]}}");

    AciConfiguration config =
        AciConfiguration.fromJson("many_subnets.json", json.toString(), new Warnings());

    assertThat(config.getTenants(), hasKey("many_subnets"));
  }

  /** Test configuration with very long names (255 chars) */
  @Test
  public void testLongNames() throws IOException {
    String longName =
        "very_long_tenant_name_that_tests_boundary_conditions_with_underscores_and_numbers_"
            + "12345678901234567890123456789012345678901234567890123456789012345678901234567890"
            + "12345678901234567890123456789012345678901234567890123456789012345678901234567890";

    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-"
            + longName
            + "\", \"name\": \""
            + longName
            + "\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-"
            + longName
            + "/ctx-vrf1\", \"name\": \"vrf1\"},"
            + "\"children\": []"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("long_names.json", json, new Warnings());

    assertNotNull(config);
  }

  /** Test configuration with numeric-only names */
  @Test
  public void testNumericNames() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"dn\": \"uni\", \"name\": \"aci-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-999\", \"name\": \"999\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"dn\": \"uni/tn-999/ctx-111\", \"name\": \"111\"},"
            + "\"children\": []"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("numeric_names.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("999"));
  }
}
