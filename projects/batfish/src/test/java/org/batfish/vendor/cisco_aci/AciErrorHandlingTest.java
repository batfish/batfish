package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.batfish.common.Warnings;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.junit.Test;

/** Tests for {@link AciConfiguration} error handling and malformed input. */
public class AciErrorHandlingTest {

  /** Test handling of malformed JSON syntax. */
  @Test
  public void testMalformedJsonSyntax() throws Exception {
    String malformedJson = "{\"polUni\": {\"attributes\": {\"name\": \"test\"";

    try {
      AciConfiguration.fromJson("malformed.json", malformedJson, new Warnings());
      fail("Expected exception for malformed JSON");
    } catch (Exception e) {
      // Expected exception - JSON parsing should fail
      assertTrue(
          "Expected JSON parsing error",
          e.getMessage() != null
              && (e.getMessage().contains("Unexpected")
                  || e.getMessage().contains("JSON")
                  || e.getMessage().contains("parsing")));
    }
  }

  /** Test handling of malformed XML syntax. */
  @Test
  public void testMalformedXmlSyntax() throws Exception {
    String malformedXml = "<polUni><fvTenant name=\"test\"";

    try {
      AciConfiguration.fromXml("malformed.xml", malformedXml, new Warnings());
      fail("Expected exception for malformed XML");
    } catch (Exception e) {
      // Expected exception - XML parsing should fail
      assertNotNull(e); // XML parser exceptions vary by implementation
    }
  }

  /** Test handling of invalid IP address. */
  @Test
  public void testInvalidIpAddress() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"tenant1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"name\": \"bd1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvSubnet\": {"
            + "\"attributes\": {\"ip\": \"999.999.999.999/24\"}"
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

    // Should parse without exception - validation may happen later
    AciConfiguration config = AciConfiguration.fromJson("invalid-ip.json", json, new Warnings());
    assertNotNull(config);
  }

  /** Test handling of invalid subnet format. */
  @Test
  public void testInvalidSubnetFormat() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"tenant1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"name\": \"bd1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvSubnet\": {"
            + "\"attributes\": {\"ip\": \"not-a-subnet\"}"
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

    // Should parse without exception
    AciConfiguration config =
        AciConfiguration.fromJson("invalid-subnet.json", json, new Warnings());
    assertNotNull(config);
  }

  /** Test handling of potential circular references. */
  @Test
  public void testCircularReferences() throws Exception {
    // VRF references BD, BD references VRF (through fvRsCtx)
    // This is actually valid in ACI, not truly circular
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"tenant1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"name\": \"vrf1\"}"
            + "}"
            + "},"
            + "{"
            + "\"fvBD\": {"
            + "\"attributes\": {\"name\": \"bd1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvRsCtx\": {"
            + "\"attributes\": {\"tnFvCtxName\": \"vrf1\"}"
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

    AciConfiguration config = AciConfiguration.fromJson("circular-ref.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("tenant1"));
    AciConfiguration.Tenant tenant = config.getTenants().get("tenant1");
    assertThat(tenant.getVrfs(), hasKey("tenant1:vrf1"));
    assertThat(tenant.getBridgeDomains(), hasKey("tenant1:bd1"));
  }

  /** Test handling of missing required attributes. */
  @Test
  public void testMissingRequiredAttribute() throws Exception {
    // Missing name attribute on tenant
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"descr\": \"No name\"},"
            + "\"children\": []"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("missing-name.json", json, new Warnings());

    // Config should still parse, tenant may have empty/null name
    assertNotNull(config);
  }

  /** Test handling of null in mandatory fields. */
  @Test
  public void testNullMandatoryFields() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": null},"
            + "\"children\": []"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("null-hostname.json", json, new Warnings());

    // Should parse with null hostname
    assertNotNull(config);
  }

  /** Test handling of empty children array. */
  @Test
  public void testEmptyArrayOfChildren() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": []"
            + "}"
            + "}";

    AciConfiguration config =
        AciConfiguration.fromJson("empty-children.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), aMapWithSize(0));
  }

  /** Test handling of heterogeneous children types. */
  @Test
  public void testHeterogeneousChildren() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"tenant1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {\"attributes\": {\"name\": \"vrf1\"}}"
            + "},"
            + "{"
            + "\"fvBD\": {\"attributes\": {\"name\": \"bd1\"}}"
            + "},"
            + "{"
            + "\"vzBrCP\": {\"attributes\": {\"name\": \"contract1\"}}"
            + "},"
            + "{"
            + "\"fvAp\": {\"attributes\": {\"name\": \"app1\"}}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("heterogeneous.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("tenant1"));
    // Fabric nodes are parsed separately, not in this test
  }

  /** Test handling of large configuration (100+ objects). */
  @Test
  public void testLargeConfiguration() throws Exception {
    // Create a simpler test for larger configuration
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"large-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {\"attributes\": {\"name\": \"tenant1\"}}"
            + "},"
            + "{"
            + "\"fvTenant\": {\"attributes\": {\"name\": \"tenant2\"}}"
            + "},"
            + "{"
            + "\"fvTenant\": {\"attributes\": {\"name\": \"tenant3\"}}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("large-config.json", json, new Warnings());

    // Should parse all 3 tenants
    assertThat(config.getTenants(), aMapWithSize(3));
  }

  /** Test handling of duplicate object names in same scope. */
  @Test
  public void testDuplicateObjectNames() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"tenant1\"},"
            + "\"children\": ["
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"name\": \"vrf1\"}"
            + "}"
            + "},"
            + "{"
            + "\"fvCtx\": {"
            + "\"attributes\": {\"name\": \"vrf1\"}"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config =
        AciConfiguration.fromJson("duplicate-names.json", json, new Warnings());

    // Second VRF with same name should overwrite first
    assertThat(config.getTenants(), hasKey("tenant1"));
    AciConfiguration.Tenant tenant = config.getTenants().get("tenant1");
    assertThat(tenant.getVrfs(), hasKey("tenant1:vrf1"));
  }

  /** Test handling of extremely long names. */
  @Test
  public void testVeryLongNames() throws Exception {
    StringBuilder longName = new StringBuilder();
    for (int i = 0; i < 300; i++) {
      longName.append("a");
    }

    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \""
            + longName.toString()
            + "\"}"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("long-names.json", json, new Warnings());

    // Should handle long names
    assertNotNull(config);
  }

  /** Test handling of special characters in names. */
  @Test
  public void testSpecialCharactersInNames() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {\"name\": \"tenant_test-01@example.com\"}"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("special-chars.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("tenant_test-01@example.com"));
  }

  /** Test handling of Unicode characters in descriptions. */
  @Test
  public void testUnicodeCharacters() throws Exception {
    String json =
        "{"
            + "\"polUni\": {"
            + "\"attributes\": {\"name\": \"test-fabric\"},"
            + "\"children\": ["
            + "{"
            + "\"fvTenant\": {"
            + "\"attributes\": {"
            + "\"name\": \"tenant1\","
            + "\"descr\": \"测试租户 Тестовый Тенант\""
            + "}"
            + "}"
            + "}"
            + "]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("unicode.json", json, new Warnings());

    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test handling of missing polUni root element. */
  @Test
  public void testMissingPolUni() throws Exception {
    String json = "{\"notPolUni\": {\"attributes\": {\"name\": \"test\"}}}";

    AciConfiguration config = AciConfiguration.fromJson("no-poluni.json", json, new Warnings());

    // Should parse but with minimal structure
    assertNotNull(config);
  }

  /** Test handling of null polUni children. */
  @Test
  public void testNullPolUniChildren() throws Exception {
    String json = "{\"polUni\": {\"attributes\": {\"name\": \"test\"},\"children\": null}}";

    AciConfiguration config = AciConfiguration.fromJson("null-children.json", json, new Warnings());

    assertNotNull(config);
    assertThat(config.getHostname(), equalTo("test"));
  }

  /** Test handling of empty JSON object. */
  @Test
  public void testEmptyJsonObject() throws Exception {
    String json = "{}";

    AciConfiguration config = AciConfiguration.fromJson("empty.json", json, new Warnings());

    assertNotNull(config);
  }

  /** Test handling of whitespace only JSON. */
  @Test
  public void testWhitespaceOnlyJson() throws Exception {
    String json = "   \n\t  ";

    try {
      AciConfiguration.fromJson("whitespace.json", json, new Warnings());
      fail("Expected exception for whitespace-only input");
    } catch (Exception e) {
      // Expected exception
      assertNotNull(e);
    }
  }
}
