package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciTenant;
import org.batfish.vendor.cisco_aci.representation.AciVrfModel;
import org.junit.Test;

/**
 * Tests for ACI core model deserialization.
 *
 * <p>This test class verifies that core ACI JSON configuration can be properly deserialized into
 * the corresponding representation classes, including child objects, tenants, and VRF models.
 */
public class AciCoreModelTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Note: AciChild tests removed as AciChild is an internal helper class used within
   * AciConfiguration deserialization and is not meant to be deserialized directly via ObjectMapper.
   */

  /** Test deserialization of AciTenant with basic fields. */
  @Test
  public void testDeserializeAciTenant_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"tenant1\","
            + "  \"descr\": \"Tenant 1\","
            + "  \"nameAlias\": \"t1\""
            + "}"
            + "}";

    AciTenant tenant = MAPPER.readValue(json, AciTenant.class);
    assertThat(tenant.getAttributes(), notNullValue());
    assertThat(tenant.getAttributes().getName(), equalTo("tenant1"));
    assertThat(tenant.getAttributes().getDescription(), equalTo("Tenant 1"));
    assertThat(tenant.getAttributes().getNameAlias(), equalTo("t1"));
  }

  /** Test deserialization of AciTenant with owner fields. */
  @Test
  public void testDeserializeAciTenant_withOwner() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"owned_tenant\","
            + "  \"ownerKey\": \"key123\","
            + "  \"ownerTag\": \"tag456\","
            + "  \"userdom\": \"all\""
            + "}"
            + "}";

    AciTenant tenant = MAPPER.readValue(json, AciTenant.class);
    assertThat(tenant.getAttributes(), notNullValue());
    assertThat(tenant.getAttributes().getName(), equalTo("owned_tenant"));
    assertThat(tenant.getAttributes().getOwnerKey(), equalTo("key123"));
    assertThat(tenant.getAttributes().getOwnerTag(), equalTo("tag456"));
    assertThat(tenant.getAttributes().getUserDomain(), equalTo("all"));
  }

  /** Test deserialization of AciTenant with annotation. */
  @Test
  public void testDeserializeAciTenant_withAnnotation() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"annotated_tenant\","
            + "  \"annotation\": \"production\","
            + "  \"dn\": \"uni/tn-annotated_tenant\""
            + "}"
            + "}";

    AciTenant tenant = MAPPER.readValue(json, AciTenant.class);
    assertThat(tenant.getAttributes(), notNullValue());
    assertThat(tenant.getAttributes().getAnnotation(), equalTo("production"));
    assertThat(tenant.getAttributes().getDistinguishedName(), equalTo("uni/tn-annotated_tenant"));
  }

  /** Test AciVrfModel constructor and basic operations. */
  @Test
  public void testAciVrfModel_constructor() {
    AciVrfModel vrf = new AciVrfModel("vrf1");
    assertThat(vrf.getName(), equalTo("vrf1"));
    assertThat(vrf.getTenant(), nullValue());
    assertThat(vrf.getDescription(), nullValue());
  }

  /** Test AciVrfModel setter methods. */
  @Test
  public void testAciVrfModel_setters() {
    AciVrfModel vrf = new AciVrfModel("vrf2");
    vrf.setTenant("tenant1");
    vrf.setDescription("Production VRF");

    assertThat(vrf.getName(), equalTo("vrf2"));
    assertThat(vrf.getTenant(), equalTo("tenant1"));
    assertThat(vrf.getDescription(), equalTo("Production VRF"));
  }

  /** Test AciVrfModel with null tenant. */
  @Test
  public void testAciVrfModel_nullTenant() {
    AciVrfModel vrf = new AciVrfModel("vrf3");
    vrf.setTenant(null);

    assertThat(vrf.getName(), equalTo("vrf3"));
    assertThat(vrf.getTenant(), nullValue());
  }

  /** Test AciVrfModel with null description. */
  @Test
  public void testAciVrfModel_nullDescription() {
    AciVrfModel vrf = new AciVrfModel("vrf4");
    vrf.setDescription(null);

    assertThat(vrf.getName(), equalTo("vrf4"));
    assertThat(vrf.getDescription(), nullValue());
  }

  /** Test full configuration with tenant and VRF model. */
  @Test
  public void testFullConfiguration_tenantAndVrf() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "  \"attributes\": {\"name\": \"test-fabric\"},"
            + "  \"children\": ["
            + "    {"
            + "      \"fvTenant\": {"
            + "        \"attributes\": {"
            + "          \"name\": \"tenant1\","
            + "          \"descr\": \"Test tenant\""
            + "        },"
            + "        \"children\": ["
            + "          {"
            + "            \"fvCtx\": {"
            + "              \"attributes\": {"
            + "                \"name\": \"vrf1\""
            + "              }"
            + "            }"
            + "          }"
            + "        ]"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("tenant_vrf.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
    assertThat(config.getVrfs(), hasKey("tenant1:vrf1"));
  }

  /** Note: Additional AciChild tests removed - class is internal to AciConfiguration */

  /** Test tenant with null attributes. */
  @Test
  public void testAciTenant_nullAttributes() throws IOException {
    String json = "{}";

    AciTenant tenant = MAPPER.readValue(json, AciTenant.class);
    assertThat(tenant.getAttributes(), nullValue());
  }

  /** Test full configuration with multiple tenants. */
  @Test
  public void testFullConfiguration_multipleTenants() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "  \"attributes\": {\"name\": \"test-fabric\"},"
            + "  \"children\": ["
            + "    {"
            + "      \"fvTenant\": {"
            + "        \"attributes\": {\"name\": \"tenant1\"}"
            + "      }"
            + "    },"
            + "    {"
            + "      \"fvTenant\": {"
            + "        \"attributes\": {\"name\": \"tenant2\"}"
            + "      }"
            + "    },"
            + "    {"
            + "      \"fvTenant\": {"
            + "        \"attributes\": {\"name\": \"tenant3\"}"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("multi_tenant.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
    assertThat(config.getTenants(), hasKey("tenant2"));
    assertThat(config.getTenants(), hasKey("tenant3"));
  }

  /** Note: AciChild nested children test removed - class is internal to AciConfiguration */

  /** Test tenant with all optional fields. */
  @Test
  public void testAciTenant_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"full_tenant\","
            + "  \"descr\": \"Complete tenant configuration\","
            + "  \"nameAlias\": \"tenant_alias\","
            + "  \"annotation\": \"production\","
            + "  \"dn\": \"uni/tn-full_tenant\","
            + "  \"ownerKey\": \"owner1\","
            + "  \"ownerTag\": \"tag1\","
            + "  \"userdom\": \"all\""
            + "}"
            + "}";

    AciTenant tenant = MAPPER.readValue(json, AciTenant.class);
    assertThat(tenant.getAttributes(), notNullValue());
    assertThat(tenant.getAttributes().getName(), equalTo("full_tenant"));
    assertThat(tenant.getAttributes().getDescription(), equalTo("Complete tenant configuration"));
    assertThat(tenant.getAttributes().getNameAlias(), equalTo("tenant_alias"));
    assertThat(tenant.getAttributes().getAnnotation(), equalTo("production"));
    assertThat(tenant.getAttributes().getDistinguishedName(), equalTo("uni/tn-full_tenant"));
    assertThat(tenant.getAttributes().getOwnerKey(), equalTo("owner1"));
    assertThat(tenant.getAttributes().getOwnerTag(), equalTo("tag1"));
    assertThat(tenant.getAttributes().getUserDomain(), equalTo("all"));
  }

  /** Test AciVrfModel with complete configuration. */
  @Test
  public void testAciVrfModel_complete() {
    AciVrfModel vrf = new AciVrfModel("production_vrf");
    vrf.setTenant("corp_tenant");
    vrf.setDescription("Production VRF with routing enabled");

    assertThat(vrf.getName(), equalTo("production_vrf"));
    assertThat(vrf.getTenant(), equalTo("corp_tenant"));
    assertThat(vrf.getDescription(), equalTo("Production VRF with routing enabled"));
  }

  /** Test VRF model name immutability. */
  @Test
  public void testAciVrfModel_nameIsImmutable() {
    AciVrfModel vrf = new AciVrfModel("immutable_name");
    // The name field is final and set in constructor
    assertThat(vrf.getName(), equalTo("immutable_name"));
    // There's no setName method, which is correct
  }

  /** Note: AciChild empty children test removed - class is internal to AciConfiguration */

  /** Test full configuration with tenant containing all child types. */
  @Test
  public void testFullConfiguration_tenantWithAllChildTypes() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "  \"attributes\": {\"name\": \"test-fabric\"},"
            + "  \"children\": ["
            + "    {"
            + "      \"fvTenant\": {"
            + "        \"attributes\": {\"name\": \"tenant1\"},"
            + "        \"children\": ["
            + "          {\"fvCtx\": {\"attributes\": {\"name\": \"vrf1\"}}},"
            + "          {\"fvBD\": {\"attributes\": {\"name\": \"bd1\"}}},"
            + "          {\"fvAp\": {\"attributes\": {\"name\": \"app1\"}}},"
            + "          {\"vzBrCP\": {\"attributes\": {\"name\": \"contract1\"}}},"
            + "          {\"vzFilter\": {\"attributes\": {\"name\": \"filter1\"}}},"
            + "          {\"l3extOut\": {\"attributes\": {\"name\": \"l3out1\"}}}"
            + "        ]"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("full_tenant.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
    assertThat(config.getVrfs(), hasKey("tenant1:vrf1"));
  }
}
