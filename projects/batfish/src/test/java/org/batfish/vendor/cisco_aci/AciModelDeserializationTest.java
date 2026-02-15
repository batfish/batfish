package org.batfish.vendor.cisco_aci;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.vendor.cisco_aci.representation.AciConfiguration;
import org.batfish.vendor.cisco_aci.representation.AciContract;
import org.batfish.vendor.cisco_aci.representation.AciContractSubject;
import org.batfish.vendor.cisco_aci.representation.AciEntry;
import org.batfish.vendor.cisco_aci.representation.AciFilter;
import org.batfish.vendor.cisco_aci.representation.AciL2Out;
import org.batfish.vendor.cisco_aci.representation.AciL3Out;
import org.batfish.vendor.cisco_aci.representation.AciVrf;
import org.junit.Test;

/**
 * Tests for ACI representation model deserialization.
 *
 * <p>This test class verifies that ACI JSON configuration can be properly deserialized into the
 * corresponding representation classes, covering various model types including entries, filters,
 * contracts, VRFs, L3Outs, and L2Outs.
 */
public class AciModelDeserializationTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Test deserialization of AciEntry with all basic fields. */
  @Test
  public void testDeserializeAciEntry_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"allow_http\","
            + "  \"descr\": \"Allow HTTP traffic\","
            + "  \"etherT\": \"ip\","
            + "  \"prot\": \"tcp\","
            + "  \"dPort\": \"80\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertThat(entry.getAttributes(), notNullValue());
    assertThat(entry.getAttributes().getName(), equalTo("allow_http"));
    assertThat(entry.getAttributes().getDescription(), equalTo("Allow HTTP traffic"));
    assertThat(entry.getAttributes().getEtherType(), equalTo("ip"));
    assertThat(entry.getAttributes().getProtocol(), equalTo("tcp"));
    assertThat(entry.getAttributes().getDestinationPort(), equalTo("80"));
  }

  /** Test deserialization of AciEntry with ICMP fields. */
  @Test
  public void testDeserializeAciEntry_icmp() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"allow_ping\","
            + "  \"prot\": \"icmp\","
            + "  \"icmpv4T\": \"echo\","
            + "  \"icmpv4C\": \"0\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertThat(entry.getAttributes(), notNullValue());
    assertThat(entry.getAttributes().getProtocol(), equalTo("icmp"));
    assertThat(entry.getAttributes().getIcmpv4Type(), equalTo("echo"));
    assertThat(entry.getAttributes().getIcmpv4Code(), equalTo("0"));
  }

  /** Test deserialization of AciEntry with port ranges. */
  @Test
  public void testDeserializeAciEntry_portRanges() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"dynamic_ports\","
            + "  \"dFromPort\": \"49152\","
            + "  \"dToPort\": \"65535\","
            + "  \"sFromPort\": \"1024\","
            + "  \"sToPort\": \"65535\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertThat(entry.getAttributes(), notNullValue());
    assertThat(entry.getAttributes().getDestinationFromPort(), equalTo("49152"));
    assertThat(entry.getAttributes().getDestinationToPort(), equalTo("65535"));
    assertThat(entry.getAttributes().getSourceFromPort(), equalTo("1024"));
    assertThat(entry.getAttributes().getSourceToPort(), equalTo("65535"));
  }

  /** Test deserialization of AciEntry with IP address fields. */
  @Test
  public void testDeserializeAciEntry_ipAddresses() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"specific_subnet\","
            + "  \"srcAddr\": \"10.0.0.0/24\","
            + "  \"dstAddr\": \"192.168.1.0/24\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertThat(entry.getAttributes(), notNullValue());
    assertThat(entry.getAttributes().getSourceAddress(), equalTo("10.0.0.0/24"));
    assertThat(entry.getAttributes().getDestinationAddress(), equalTo("192.168.1.0/24"));
  }

  /** Test deserialization of AciEntry with ARP and TCP options. */
  @Test
  public void testDeserializeAciEntry_arpAndTcp() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"arp_tcp_entry\","
            + "  \"arpOpc\": \"request\","
            + "  \"tcpRules\": \"established\","
            + "  \"stateful\": \"yes\","
            + "  \"applyToFrag\": \"yes\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertThat(entry.getAttributes(), notNullValue());
    assertThat(entry.getAttributes().getArpOpcode(), equalTo("request"));
    assertThat(entry.getAttributes().getTcpRules(), equalTo("established"));
    assertThat(entry.getAttributes().getStateful(), equalTo("yes"));
    assertThat(entry.getAttributes().getApplyToFragments(), equalTo("yes"));
  }

  /** Test deserialization of AciFilter with entry children. */
  @Test
  public void testDeserializeAciFilter_withEntries() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"web_filter\","
            + "  \"descr\": \"Web traffic filter\""
            + "},"
            + "\"children\": ["
            + "  {\"vzEntry\": {\"attributes\": {\"name\": \"http\"}}},"
            + "  {\"vzEntry\": {\"attributes\": {\"name\": \"https\"}}}"
            + "]"
            + "}";

    AciFilter filter = MAPPER.readValue(json, AciFilter.class);
    assertThat(filter.getAttributes(), notNullValue());
    assertThat(filter.getAttributes().getName(), equalTo("web_filter"));
    assertThat(filter.getAttributes().getDescription(), equalTo("Web traffic filter"));
    assertThat(filter.getChildren(), notNullValue());
    assertThat(filter.getChildren(), hasSize(2));
  }

  /** Test deserialization of AciContract with subject children. */
  @Test
  public void testDeserializeAciContract_withSubjects() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"web_contract\","
            + "  \"descr\": \"Web access contract\","
            + "  \"prio\": \"level1\","
            + "  \"scope\": \"context\""
            + "},"
            + "\"children\": ["
            + "  {\"vzSubj\": {\"attributes\": {\"name\": \"web_traffic\"}}},"
            + "  {\"vzSubj\": {\"attributes\": {\"name\": \"db_traffic\"}}}"
            + "]"
            + "}";

    AciContract contract = MAPPER.readValue(json, AciContract.class);
    assertThat(contract.getAttributes(), notNullValue());
    assertThat(contract.getAttributes().getName(), equalTo("web_contract"));
    assertThat(contract.getAttributes().getDescription(), equalTo("Web access contract"));
    assertThat(contract.getAttributes().getPriority(), equalTo("level1"));
    assertThat(contract.getAttributes().getScope(), equalTo("context"));
    assertThat(contract.getChildren(), notNullValue());
    assertThat(contract.getChildren(), hasSize(2));
  }

  /** Test deserialization of AciContractSubject with match types. */
  @Test
  public void testDeserializeAciContractSubject_matchTypes() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"subject1\","
            + "  \"consMatchT\": \"AtleastOne\","
            + "  \"provMatchT\": \"AtleastOne\","
            + "  \"revFltPorts\": \"yes\""
            + "}"
            + "}";

    AciContractSubject subject = MAPPER.readValue(json, AciContractSubject.class);
    assertThat(subject.getAttributes(), notNullValue());
    assertThat(subject.getAttributes().getName(), equalTo("subject1"));
    assertThat(subject.getAttributes().getConsumerMatchType(), equalTo("AtleastOne"));
    assertThat(subject.getAttributes().getProviderMatchType(), equalTo("AtleastOne"));
    assertThat(subject.getAttributes().getReverseFilterPorts(), equalTo("yes"));
  }

  /** Test deserialization of AciVrf with all common fields. */
  @Test
  public void testDeserializeAciVrf_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"prod_vrf\","
            + "  \"descr\": \"Production VRF\","
            + "  \"vrfIndex\": \"1\","
            + "  \"pcEnfPref\": \"enforced\""
            + "}"
            + "}";

    AciVrf vrf = MAPPER.readValue(json, AciVrf.class);
    assertThat(vrf.getAttributes(), notNullValue());
    assertThat(vrf.getAttributes().getName(), equalTo("prod_vrf"));
    assertThat(vrf.getAttributes().getDescription(), equalTo("Production VRF"));
    assertThat(vrf.getAttributes().getVrfIndex(), equalTo("1"));
    assertThat(vrf.getAttributes().getPolicyEnforcementPreference(), equalTo("enforced"));
  }

  /** Test deserialization of AciVrf with learning options. */
  @Test
  public void testDeserializeAciVrf_learningOptions() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"learning_vrf\","
            + "  \"ipDataPlaneLearning\": \"enabled\","
            + "  \"knwMcastAct\": \"permit\""
            + "}"
            + "}";

    AciVrf vrf = MAPPER.readValue(json, AciVrf.class);
    assertThat(vrf.getAttributes(), notNullValue());
    assertThat(vrf.getAttributes().getIpDataPlaneLearning(), equalTo("enabled"));
    assertThat(vrf.getAttributes().getKnownMcastAction(), equalTo("permit"));
  }

  /** Test deserialization of AciL3Out with basic fields. */
  @Test
  public void testDeserializeAciL3Out_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"external_l3out\","
            + "  \"descr\": \"External L3 connectivity\","
            + "  \"maxEcmp\": \"8\","
            + "  \"mplsEnabled\": \"yes\""
            + "}"
            + "}";

    AciL3Out l3out = MAPPER.readValue(json, AciL3Out.class);
    assertThat(l3out.getAttributes(), notNullValue());
    assertThat(l3out.getAttributes().getName(), equalTo("external_l3out"));
    assertThat(l3out.getAttributes().getDescription(), equalTo("External L3 connectivity"));
    assertThat(l3out.getAttributes().getMaxEcmp(), equalTo("8"));
    assertThat(l3out.getAttributes().getMplsEnabled(), equalTo("yes"));
  }

  /** Test deserialization of AciL3Out with route control. */
  @Test
  public void testDeserializeAciL3Out_routeControl() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"controlled_l3out\","
            + "  \"enforceRtctrl\": \"yes\","
            + "  \"hostBasedRoute\": \"yes\","
            + "  \"matchT\": \"AtleastOne\""
            + "}"
            + "}";

    AciL3Out l3out = MAPPER.readValue(json, AciL3Out.class);
    assertThat(l3out.getAttributes(), notNullValue());
    assertThat(l3out.getAttributes().getEnforceRouteControl(), equalTo("yes"));
    assertThat(l3out.getAttributes().getHostBasedRoute(), equalTo("yes"));
    assertThat(l3out.getAttributes().getMatchType(), equalTo("AtleastOne"));
  }

  /** Test deserialization of AciL2Out with basic fields. */
  @Test
  public void testDeserializeAciL2Out_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"external_l2out\","
            + "  \"descr\": \"External L2 connectivity\","
            + "  \"targetDscp\": \"46\""
            + "}"
            + "}";

    AciL2Out l2out = MAPPER.readValue(json, AciL2Out.class);
    assertThat(l2out.getAttributes(), notNullValue());
    assertThat(l2out.getAttributes().getName(), equalTo("external_l2out"));
    assertThat(l2out.getAttributes().getDescription(), equalTo("External L2 connectivity"));
    assertThat(l2out.getAttributes().getTargetDscp(), equalTo("46"));
  }

  /** Test deserialization with null attributes (graceful handling). */
  @Test
  public void testDeserialize_nullAttributes() throws IOException {
    String json = "{}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertThat(entry.getAttributes(), nullValue());
  }

  /** Test full configuration deserialization with entry and filter. */
  @Test
  public void testFullConfiguration_entryAndFilter() throws IOException {
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
            + "            \"vzFilter\": {"
            + "              \"attributes\": {\"name\": \"allow_http\"},"
            + "              \"children\": ["
            + "                {"
            + "                  \"vzEntry\": {"
            + "                    \"attributes\": {"
            + "                      \"name\": \"http_entry\","
            + "                      \"prot\": \"tcp\","
            + "                      \"dPort\": \"80\""
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

    AciConfiguration config = AciConfiguration.fromJson("entry_filter.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test full configuration with contract and subjects. */
  @Test
  public void testFullConfiguration_contractAndSubjects() throws IOException {
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
            + "            \"vzBrCP\": {"
            + "              \"attributes\": {"
            + "                \"name\": \"web_contract\","
            + "                \"prio\": \"level1\""
            + "              },"
            + "              \"children\": ["
            + "                {"
            + "                  \"vzSubj\": {"
            + "                    \"attributes\": {"
            + "                      \"name\": \"web_subject\","
            + "                      \"consMatchT\": \"AtleastOne\""
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
        AciConfiguration.fromJson("contract_subjects.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
    assertThat(config.getContracts(), hasKey("tenant1:web_contract"));
  }

  /** Test full configuration with VRF. */
  @Test
  public void testFullConfiguration_vrf() throws IOException {
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
            + "            \"fvCtx\": {"
            + "              \"attributes\": {"
            + "                \"name\": \"vrf1\","
            + "                \"vrfIndex\": \"1\","
            + "                \"pcEnfPref\": \"enforced\""
            + "              }"
            + "            }"
            + "          }"
            + "        ]"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("vrf_config.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
    assertThat(config.getVrfs(), hasKey("tenant1:vrf1"));
  }

  /** Test full configuration with L3Out. */
  @Test
  public void testFullConfiguration_l3out() throws IOException {
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
            + "              \"attributes\": {"
            + "                \"name\": \"external_l3out\","
            + "                \"maxEcmp\": \"8\""
            + "              }"
            + "            }"
            + "          }"
            + "        ]"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("l3out_config.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test full configuration with L2Out. */
  @Test
  public void testFullConfiguration_l2out() throws IOException {
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
            + "            \"l2extOut\": {"
            + "              \"attributes\": {"
            + "                \"name\": \"external_l2out\","
            + "                \"descr\": \"L2 external connectivity\""
            + "              }"
            + "            }"
            + "          }"
            + "        ]"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("l2out_config.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test entry with all ICMPv6 fields. */
  @Test
  public void testDeserializeAciEntry_icmpv6() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"ipv6_entry\","
            + "  \"prot\": \"icmpv6\","
            + "  \"icmpv6T\": \"echo-request\","
            + "  \"icmpv6C\": \"0\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertThat(entry.getAttributes(), notNullValue());
    assertThat(entry.getAttributes().getIcmpv6Type(), equalTo("echo-request"));
    assertThat(entry.getAttributes().getIcmpv6Code(), equalTo("0"));
  }

  /** Test entry with DSCP matching. */
  @Test
  public void testDeserializeAciEntry_dscp() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"dscp_entry\","
            + "  \"matchDscp\": \"46\""
            + "}"
            + "}";

    AciEntry entry = MAPPER.readValue(json, AciEntry.class);
    assertThat(entry.getAttributes(), notNullValue());
    assertThat(entry.getAttributes().getMatchDscp(), equalTo("46"));
  }

  /** Test contract with target DSCP. */
  @Test
  public void testDeserializeAciContract_targetDscp() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"qos_contract\","
            + "  \"targetDscp\": \"34\","
            + "  \"prio\": \"level1\""
            + "}"
            + "}";

    AciContract contract = MAPPER.readValue(json, AciContract.class);
    assertThat(contract.getAttributes(), notNullValue());
    assertThat(contract.getAttributes().getTargetDscp(), equalTo("34"));
  }

  /** Test L3Out with all optional fields. */
  @Test
  public void testDeserializeAciL3Out_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"full_l3out\","
            + "  \"descr\": \"Complete L3Out configuration\","
            + "  \"enforceRtctrl\": \"yes\","
            + "  \"hostBasedRoute\": \"yes\","
            + "  \"matchT\": \"AtleastOne\","
            + "  \"maxEcmp\": \"16\","
            + "  \"mplsEnabled\": \"no\","
            + "  \"targetDscp\": \"0\""
            + "}"
            + "}";

    AciL3Out l3out = MAPPER.readValue(json, AciL3Out.class);
    assertThat(l3out.getAttributes(), notNullValue());
    assertThat(l3out.getAttributes().getName(), equalTo("full_l3out"));
    assertThat(l3out.getAttributes().getEnforceRouteControl(), equalTo("yes"));
    assertThat(l3out.getAttributes().getHostBasedRoute(), equalTo("yes"));
    assertThat(l3out.getAttributes().getMatchType(), equalTo("AtleastOne"));
    assertThat(l3out.getAttributes().getMaxEcmp(), equalTo("16"));
    assertThat(l3out.getAttributes().getMplsEnabled(), equalTo("no"));
    assertThat(l3out.getAttributes().getTargetDscp(), equalTo("0"));
  }

  /** Test VRF with policy enforcement direction. */
  @Test
  public void testDeserializeAciVrf_policyEnforcement() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"enforced_vrf\","
            + "  \"pcEnfDir\": \"ingress\","
            + "  \"pcEnfPref\": \"enforced\","
            + "  \"bdEnforcedEnable\": \"yes\""
            + "}"
            + "}";

    AciVrf vrf = MAPPER.readValue(json, AciVrf.class);
    assertThat(vrf.getAttributes(), notNullValue());
    assertThat(vrf.getAttributes().getPolicyEnforcementDirection(), equalTo("ingress"));
    assertThat(vrf.getAttributes().getPolicyEnforcementPreference(), equalTo("enforced"));
    assertThat(vrf.getAttributes().getBdEnforcedEnable(), equalTo("yes"));
  }
}
