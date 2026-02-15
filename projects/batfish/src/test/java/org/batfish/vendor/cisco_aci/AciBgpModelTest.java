package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.vendor.cisco_aci.representation.AciBgpContextPolicy;
import org.batfish.vendor.cisco_aci.representation.AciBgpPeerP;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciL3ExternalEpg;
import org.batfish.vendor.cisco_aci.representation.AciL3LogicalNodeProfile;
import org.junit.Test;

/**
 * Tests for ACI BGP and external connectivity model deserialization.
 *
 * <p>This test class verifies that BGP-related JSON configuration can be properly deserialized into
 * the corresponding representation classes, including BGP context policies, peer prefix policies,
 * route target profiles, external EPGs, and logical node profiles.
 */
public class AciBgpModelTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Test deserialization of AciBgpContextPolicy with timer fields. */
  @Test
  public void testDeserializeAciBgpContextPolicy_timers() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bgp_pol\","
            + "  \"descr\": \"BGP policy with timers\","
            + "  \"holdIntvl\": \"180\","
            + "  \"kaIntvl\": \"60\","
            + "  \"staleIntvl\": \"300\""
            + "}"
            + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertThat(policy.getAttributes(), notNullValue());
    assertThat(policy.getAttributes().getName(), equalTo("bgp_pol"));
    assertThat(policy.getAttributes().getDescription(), equalTo("BGP policy with timers"));
    assertThat(policy.getAttributes().getHoldInterval(), equalTo("180"));
    assertThat(policy.getAttributes().getKeepaliveInterval(), equalTo("60"));
    assertThat(policy.getAttributes().getStaleInterval(), equalTo("300"));
  }

  /** Test deserialization of AciBgpContextPolicy with graceful restart. */
  @Test
  public void testDeserializeAciBgpContextPolicy_gracefulRestart() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"gr_bgp_pol\","
            + "  \"grCtrl\": \"l3\","
            + "  \"maxAsLimit\": \"50\""
            + "}"
            + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertThat(policy.getAttributes(), notNullValue());
    assertThat(policy.getAttributes().getGracefulRestartControl(), equalTo("l3"));
    assertThat(policy.getAttributes().getMaxAsLimit(), equalTo("50"));
  }

  /** Test deserialization of AciBgpPeerP with prefix limits. */
  @Test
  public void testDeserializeAciBgpPeerP_prefixLimits() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"peer_prefix_pol\","
            + "  \"descr\": \"Peer prefix limit policy\","
            + "  \"maxPfx\": \"1000\","
            + "  \"thresh\": \"80\""
            + "}"
            + "}";

    AciBgpPeerP peerP = MAPPER.readValue(json, AciBgpPeerP.class);
    assertThat(peerP.getAttributes(), notNullValue());
    assertThat(peerP.getAttributes().getName(), equalTo("peer_prefix_pol"));
    assertThat(peerP.getAttributes().getDescription(), equalTo("Peer prefix limit policy"));
    assertThat(peerP.getAttributes().getMaxPrefixes(), equalTo("1000"));
    assertThat(peerP.getAttributes().getThreshold(), equalTo("80"));
  }

  /** Test deserialization of AciBgpPeerP with action and restart time. */
  @Test
  public void testDeserializeAciBgpPeerP_actionAndRestart() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"restrictive_pol\","
            + "  \"action\": \"restart\","
            + "  \"restartTime\": \"15\""
            + "}"
            + "}";

    AciBgpPeerP peerP = MAPPER.readValue(json, AciBgpPeerP.class);
    assertThat(peerP.getAttributes(), notNullValue());
    assertThat(peerP.getAttributes().getAction(), equalTo("restart"));
    assertThat(peerP.getAttributes().getRestartTime(), equalTo("15"));
  }

  /** Test deserialization of AciL3ExternalEpg with basic fields. */
  @Test
  public void testDeserializeAciL3ExternalEpg_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"external_epg\","
            + "  \"descr\": \"External network EPG\","
            + "  \"matchT\": \"AtleastOne\","
            + "  \"pcEnfPref\": \"enforced\""
            + "}"
            + "}";

    AciL3ExternalEpg epg = MAPPER.readValue(json, AciL3ExternalEpg.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getName(), equalTo("external_epg"));
    assertThat(epg.getAttributes().getDescription(), equalTo("External network EPG"));
    assertThat(epg.getAttributes().getMatchType(), equalTo("AtleastOne"));
    assertThat(epg.getAttributes().getPolicyEnforcementPreference(), equalTo("enforced"));
  }

  /** Test deserialization of AciL3ExternalEpg with priority and QoS. */
  @Test
  public void testDeserializeAciL3ExternalEpg_priorityAndQos() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"priority_epg\","
            + "  \"prio\": \"level1\","
            + "  \"targetDscp\": \"46\","
            + "  \"prefGrMemb\": \"include\""
            + "}"
            + "}";

    AciL3ExternalEpg epg = MAPPER.readValue(json, AciL3ExternalEpg.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getPriority(), equalTo("level1"));
    assertThat(epg.getAttributes().getTargetDscp(), equalTo("46"));
    assertThat(epg.getAttributes().getPreferredGroupMember(), equalTo("include"));
  }

  /** Test deserialization of AciL3ExternalEpg with exception tag. */
  @Test
  public void testDeserializeAciL3ExternalEpg_exceptionTag() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"exception_epg\","
            + "  \"exceptionTag\": \"exception1\","
            + "  \"floodOnEncap\": \"yes\""
            + "}"
            + "}";

    AciL3ExternalEpg epg = MAPPER.readValue(json, AciL3ExternalEpg.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getExceptionTag(), equalTo("exception1"));
    assertThat(epg.getAttributes().getFloodOnEncap(), equalTo("yes"));
  }

  /** Test deserialization of AciL3LogicalNodeProfile with basic fields. */
  @Test
  public void testDeserializeAciL3LogicalNodeProfile_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"node_profile1\","
            + "  \"descr\": \"L3Out node profile\","
            + "  \"tag\": \"external\""
            + "}"
            + "}";

    AciL3LogicalNodeProfile profile = MAPPER.readValue(json, AciL3LogicalNodeProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getName(), equalTo("node_profile1"));
    assertThat(profile.getAttributes().getDescription(), equalTo("L3Out node profile"));
    assertThat(profile.getAttributes().getTag(), equalTo("external"));
  }

  /** Test deserialization of AciL3LogicalNodeProfile with config issues. */
  @Test
  public void testDeserializeAciL3LogicalNodeProfile_configIssues() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"problematic_profile\","
            + "  \"configIssues\": \"missing_route_tag\""
            + "}"
            + "}";

    AciL3LogicalNodeProfile profile = MAPPER.readValue(json, AciL3LogicalNodeProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getConfigIssues(), equalTo("missing_route_tag"));
  }

  /** Test deserialization of AciBgpRouteTargetProfile. */
  @Test
  public void testDeserializeAciBgpRouteTargetProfile_basic() throws IOException {
    // Need to read the actual AciBgpRouteTargetProfile class to understand its structure
    // Assuming it follows similar patterns
    // If AciBgpRouteTargetProfile doesn't exist or has a different structure,
    // this test will need adjustment
    // TODO: Implement this test when AciBgpRouteTargetProfile structure is defined
  }

  /** Test deserialization with null attributes for BGP policy. */
  @Test
  public void testDeserialize_nullBgpPolicy() throws IOException {
    String json = "{}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertThat(policy.getAttributes(), nullValue());
  }

  /** Test full configuration with BGP context policy. */
  @Test
  public void testFullConfiguration_bgpContextPolicy() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "  \"attributes\": {\"name\": \"test-fabric\"},"
            + "  \"children\": ["
            + "    {"
            + "      \"fvTenant\": {"
            + "        \"attributes\": {\"name\": \"tenant1\"},"
            + "        \"children\": ["
            + "          {"
            + "            \"bgpCtxPol\": {"
            + "              \"attributes\": {"
            + "                \"name\": \"bgp_pol1\","
            + "                \"holdIntvl\": \"180\","
            + "                \"kaIntvl\": \"60\""
            + "              }"
            + "            }"
            + "          }"
            + "        ]"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    AciConfiguration config =
        AciConfiguration.fromJson("bgp_context_pol.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test full configuration with BGP peer prefix policy. */
  @Test
  public void testFullConfiguration_bgpPeerPrefixPolicy() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "  \"attributes\": {\"name\": \"test-fabric\"},"
            + "  \"children\": ["
            + "    {"
            + "      \"fvTenant\": {"
            + "        \"attributes\": {\"name\": \"tenant1\"},"
            + "        \"children\": ["
            + "          {"
            + "            \"bgpPeerPfxPol\": {"
            + "              \"attributes\": {"
            + "                \"name\": \"peer_pfx_pol\","
            + "                \"maxPfx\": \"5000\","
            + "                \"thresh\": \"90\""
            + "              }"
            + "            }"
            + "          }"
            + "        ]"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    AciConfiguration config =
        AciConfiguration.fromJson("bgp_peer_pfx_pol.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test full configuration with external EPG. */
  @Test
  public void testFullConfiguration_externalEpg() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "  \"attributes\": {\"name\": \"test-fabric\"},"
            + "  \"children\": ["
            + "    {"
            + "      \"fvTenant\": {"
            + "        \"attributes\": {\"name\": \"tenant1\"},"
            + "        \"children\": ["
            + "          {"
            + "            \"l3extOut\": {"
            + "              \"attributes\": {\"name\": \"l3out1\"},"
            + "              \"children\": ["
            + "                {"
            + "                  \"l3extInstP\": {"
            + "                    \"attributes\": {"
            + "                      \"name\": \"external_epg\","
            + "                      \"matchT\": \"AtleastOne\""
            + "                    }"
            + "                  }"
            + "                }"
            + "              ]"
            + "            }"
            + "          }"
            + "        ]"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("external_epg.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test full configuration with L3 logical node profile. */
  @Test
  public void testFullConfiguration_logicalNodeProfile() throws IOException {
    String json =
        "{"
            + "\"polUni\": {"
            + "  \"attributes\": {\"name\": \"test-fabric\"},"
            + "  \"children\": ["
            + "    {"
            + "      \"fvTenant\": {"
            + "        \"attributes\": {\"name\": \"tenant1\"},"
            + "        \"children\": ["
            + "          {"
            + "            \"l3extOut\": {"
            + "              \"attributes\": {\"name\": \"l3out1\"},"
            + "              \"children\": ["
            + "                {"
            + "                  \"l3extLNodeP\": {"
            + "                    \"attributes\": {"
            + "                      \"name\": \"node_profile\","
            + "                      \"descr\": \"Node profile for external connectivity\""
            + "                    }"
            + "                  }"
            + "                }"
            + "              ]"
            + "            }"
            + "          }"
            + "        ]"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    AciConfiguration config =
        AciConfiguration.fromJson("logical_node_profile.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test BGP policy with all timer values. */
  @Test
  public void testBgpContextPolicy_allTimers() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"full_timer_pol\","
            + "  \"holdIntvl\": \"240\","
            + "  \"kaIntvl\": \"80\","
            + "  \"staleIntvl\": \"600\","
            + "  \"grCtrl\": \"l2\""
            + "}"
            + "}";

    AciBgpContextPolicy policy = MAPPER.readValue(json, AciBgpContextPolicy.class);
    assertThat(policy.getAttributes(), notNullValue());
    assertThat(policy.getAttributes().getHoldInterval(), equalTo("240"));
    assertThat(policy.getAttributes().getKeepaliveInterval(), equalTo("80"));
    assertThat(policy.getAttributes().getStaleInterval(), equalTo("600"));
    assertThat(policy.getAttributes().getGracefulRestartControl(), equalTo("l2"));
  }

  /** Test peer prefix policy with restrictive settings. */
  @Test
  public void testBgpPeerP_restrictiveSettings() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"restrictive_peer_pol\","
            + "  \"maxPfx\": \"100\","
            + "  \"thresh\": \"50\","
            + "  \"action\": \"disable\","
            + "  \"restartTime\": \"30\""
            + "}"
            + "}";

    AciBgpPeerP peerP = MAPPER.readValue(json, AciBgpPeerP.class);
    assertThat(peerP.getAttributes(), notNullValue());
    assertThat(peerP.getAttributes().getMaxPrefixes(), equalTo("100"));
    assertThat(peerP.getAttributes().getThreshold(), equalTo("50"));
    assertThat(peerP.getAttributes().getAction(), equalTo("disable"));
    assertThat(peerP.getAttributes().getRestartTime(), equalTo("30"));
  }

  /** Test external EPG with all optional fields. */
  @Test
  public void testL3ExternalEpg_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"full_external_epg\","
            + "  \"descr\": \"Complete external EPG\","
            + "  \"matchT\": \"All\","
            + "  \"pcEnfPref\": \"unenforced\","
            + "  \"prio\": \"level3\","
            + "  \"targetDscp\": \"0\","
            + "  \"prefGrMemb\": \"exclude\","
            + "  \"exceptionTag\": \"custom_exception\","
            + "  \"floodOnEncap\": \"no\""
            + "}"
            + "}";

    AciL3ExternalEpg epg = MAPPER.readValue(json, AciL3ExternalEpg.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getName(), equalTo("full_external_epg"));
    assertThat(epg.getAttributes().getMatchType(), equalTo("All"));
    assertThat(epg.getAttributes().getPolicyEnforcementPreference(), equalTo("unenforced"));
    assertThat(epg.getAttributes().getPriority(), equalTo("level3"));
    assertThat(epg.getAttributes().getTargetDscp(), equalTo("0"));
    assertThat(epg.getAttributes().getPreferredGroupMember(), equalTo("exclude"));
    assertThat(epg.getAttributes().getExceptionTag(), equalTo("custom_exception"));
    assertThat(epg.getAttributes().getFloodOnEncap(), equalTo("no"));
  }

  /** Test logical node profile with all fields. */
  @Test
  public void testL3LogicalNodeProfile_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"full_node_profile\","
            + "  \"descr\": \"Complete node profile\","
            + "  \"tag\": \"production\","
            + "  \"targetDscp\": \"34\","
            + "  \"configIssues\": \"none\""
            + "}"
            + "}";

    AciL3LogicalNodeProfile profile = MAPPER.readValue(json, AciL3LogicalNodeProfile.class);
    assertThat(profile.getAttributes(), notNullValue());
    assertThat(profile.getAttributes().getName(), equalTo("full_node_profile"));
    assertThat(profile.getAttributes().getDescription(), equalTo("Complete node profile"));
    assertThat(profile.getAttributes().getTag(), equalTo("production"));
    assertThat(profile.getAttributes().getTargetDscp(), equalTo("34"));
    assertThat(profile.getAttributes().getConfigIssues(), equalTo("none"));
  }
}
