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
import org.batfish.vendor.cisco_aci.representation.apic.AciApplicationProfile;
import org.batfish.vendor.cisco_aci.representation.apic.AciBridgeDomain;
import org.batfish.vendor.cisco_aci.representation.apic.AciEndpointGroup;
import org.batfish.vendor.cisco_aci.representation.apic.AciInterface;
import org.junit.Test;

/**
 * Tests for ACI network model deserialization.
 *
 * <p>This test class verifies that network-related JSON configuration can be properly deserialized
 * into the corresponding representation classes, including bridge domains, application profiles,
 * endpoint groups, and interfaces.
 */
public class AciNetworkModelTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Test deserialization of AciBridgeDomain with basic fields. */
  @Test
  public void testDeserializeAciBridgeDomain_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"bd1\","
            + "  \"descr\": \"Bridge Domain 1\","
            + "  \"mac\": \"00:22:BD:F8:19:FF\","
            + "  \"type\": \"regular\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertThat(bd.getAttributes(), notNullValue());
    assertThat(bd.getAttributes().getName(), equalTo("bd1"));
    assertThat(bd.getAttributes().getDescription(), equalTo("Bridge Domain 1"));
    assertThat(bd.getAttributes().getMacAddress(), equalTo("00:22:BD:F8:19:FF"));
    assertThat(bd.getAttributes().getType(), equalTo("regular"));
  }

  /** Test deserialization of AciBridgeDomain with L2/L3 options. */
  @Test
  public void testDeserializeAciBridgeDomain_l2L3Options() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"l2_bd\","
            + "  \"unicastRoute\": \"yes\","
            + "  \"multiDstPktAct\": \"bd-flood\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertThat(bd.getAttributes(), notNullValue());
    assertThat(bd.getAttributes().getUnicastRoute(), equalTo("yes"));
    assertThat(bd.getAttributes().getMultiDestPacketAction(), equalTo("bd-flood"));
  }

  /** Test deserialization of AciBridgeDomain with learning options. */
  @Test
  public void testDeserializeAciBridgeDomain_learningOptions() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"learning_bd\","
            + "  \"ipLearning\": \"enabled\","
            + "  \"epMoveDetectMode\": \"garp\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertThat(bd.getAttributes(), notNullValue());
    assertThat(bd.getAttributes().getIpLearning(), equalTo("enabled"));
    assertThat(bd.getAttributes().getEpMoveDetectMode(), equalTo("garp"));
  }

  /** Test deserialization of AciBridgeDomain with ARP and multicast. */
  @Test
  public void testDeserializeAciBridgeDomain_arpAndMulticast() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"arp_mcast_bd\","
            + "  \"arpFlood\": \"yes\","
            + "  \"mcastAllow\": \"yes\","
            + "  \"hostBasedRouting\": \"yes\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertThat(bd.getAttributes(), notNullValue());
    assertThat(bd.getAttributes().getArpFlood(), equalTo("yes"));
    assertThat(bd.getAttributes().getMulticastAllow(), equalTo("yes"));
    assertThat(bd.getAttributes().getHostBasedRouting(), equalTo("yes"));
  }

  /** Test deserialization of AciBridgeDomain with link local and VMAC. */
  @Test
  public void testDeserializeAciBridgeDomain_linkLocalAndVmac() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"advanced_bd\","
            + "  \"llAddr\": \"fe80::/10\","
            + "  \"vmac\": \"yes\","
            + "  \"epClear\": \"yes\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertThat(bd.getAttributes(), notNullValue());
    assertThat(bd.getAttributes().getLinkLocalAddr(), equalTo("fe80::/10"));
    assertThat(bd.getAttributes().getVirtualMac(), equalTo("yes"));
    assertThat(bd.getAttributes().getEpClear(), equalTo("yes"));
  }

  /** Test deserialization of AciApplicationProfile with basic fields. */
  @Test
  public void testDeserializeAciApplicationProfile_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"web_app\","
            + "  \"descr\": \"Web application profile\""
            + "}"
            + "}";

    AciApplicationProfile ap = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(ap.getAttributes(), notNullValue());
    assertThat(ap.getAttributes().getName(), equalTo("web_app"));
    assertThat(ap.getAttributes().getDescription(), equalTo("Web application profile"));
  }

  /** Test deserialization of AciApplicationProfile with priority and enforcement. */
  @Test
  public void testDeserializeAciApplicationProfile_priorityAndEnforcement() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"priority_app\","
            + "  \"prio\": \"level1\","
            + "  \"pcEnfPref\": \"enforced\""
            + "}"
            + "}";

    AciApplicationProfile ap = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(ap.getAttributes(), notNullValue());
    assertThat(ap.getAttributes().getPriority(), equalTo("level1"));
    assertThat(ap.getAttributes().getPolicyEnforcementPreference(), equalTo("enforced"));
  }

  /** Test deserialization of AciApplicationProfile with forwarding control. */
  @Test
  public void testDeserializeAciApplicationProfile_forwardingControl() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"fwd_app\","
            + "  \"fwdCtrl\": \"none\","
            + "  \"hasMcastSource\": \"yes\""
            + "}"
            + "}";

    AciApplicationProfile ap = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(ap.getAttributes(), notNullValue());
    assertThat(ap.getAttributes().getForwardingControl(), equalTo("none"));
    assertThat(ap.getAttributes().getHasMulticastSource(), equalTo("yes"));
  }

  /** Test deserialization of AciApplicationProfile with shutdown. */
  @Test
  public void testDeserializeAciApplicationProfile_shutdown() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"shutdown_app\","
            + "  \"shutdown\": \"yes\","
            + "  \"isAttrBasedEPg\": \"no\""
            + "}"
            + "}";

    AciApplicationProfile ap = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(ap.getAttributes(), notNullValue());
    assertThat(ap.getAttributes().getShutdown(), equalTo("yes"));
    assertThat(ap.getAttributes().getIsAttributeBasedEpg(), equalTo("no"));
  }

  /** Test deserialization of AciEndpointGroup with basic fields. */
  @Test
  public void testDeserializeAciEndpointGroup_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"web_epg\","
            + "  \"descr\": \"Web server EPG\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getName(), equalTo("web_epg"));
    assertThat(epg.getAttributes().getDescription(), equalTo("Web server EPG"));
  }

  /** Test deserialization of AciEndpointGroup with policy enforcement. */
  @Test
  public void testDeserializeAciEndpointGroup_policyEnforcement() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"enforced_epg\","
            + "  \"pcEnfPref\": \"enforced\","
            + "  \"fwdCtrl\": \"proxy\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getPolicyEnforcementPreference(), equalTo("enforced"));
    assertThat(epg.getAttributes().getForwardingControl(), equalTo("proxy"));
  }

  /** Test deserialization of AciEndpointGroup with priority and group. */
  @Test
  public void testDeserializeAciEndpointGroup_priorityAndGroup() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"priority_epg\","
            + "  \"prio\": \"level1\","
            + "  \"prefGrMemb\": \"include\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getPriority(), equalTo("level1"));
    assertThat(epg.getAttributes().getPreferredGroupMember(), equalTo("include"));
  }

  /** Test deserialization of AciEndpointGroup with flood and multicast. */
  @Test
  public void testDeserializeAciEndpointGroup_floodAndMulticast() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"flood_epg\","
            + "  \"floodOnEncap\": \"yes\","
            + "  \"hasMcastSource\": \"yes\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getFloodOnEncap(), equalTo("yes"));
    assertThat(epg.getAttributes().getHasMcastSource(), equalTo("yes"));
  }

  /** Test deserialization of AciEndpointGroup with match type. */
  @Test
  public void testDeserializeAciEndpointGroup_matchType() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"attr_epg\","
            + "  \"isAttrBasedEPg\": \"yes\","
            + "  \"matchT\": \"AtleastOne\","
            + "  \"exceptionTag\": \"custom\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getIsAttributeBasedEpg(), equalTo("yes"));
    assertThat(epg.getAttributes().getMatchType(), equalTo("AtleastOne"));
    assertThat(epg.getAttributes().getExceptionTag(), equalTo("custom"));
  }

  /** Test deserialization of AciInterface with basic fields. */
  @Test
  public void testDeserializeAciInterface_basic() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"eth1/1\","
            + "  \"descr\": \"Interface 1/1\","
            + "  \"id\": \"topology/pod-1/node-101/sys-1/phys-[eth1/1]\""
            + "}"
            + "}";

    AciInterface iface = MAPPER.readValue(json, AciInterface.class);
    assertThat(iface.getAttributes(), notNullValue());
    assertThat(iface.getAttributes().getName(), equalTo("eth1/1"));
    assertThat(iface.getAttributes().getDescription(), equalTo("Interface 1/1"));
    assertThat(
        iface.getAttributes().getId(), equalTo("topology/pod-1/node-101/sys-1/phys-[eth1/1]"));
  }

  /** Test deserialization of AciInterface with layer and mode. */
  @Test
  public void testDeserializeAciInterface_layerAndMode() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"eth2/2\","
            + "  \"layer\": \"layer3\","
            + "  \"mode\": \"routed\""
            + "}"
            + "}";

    AciInterface iface = MAPPER.readValue(json, AciInterface.class);
    assertThat(iface.getAttributes(), notNullValue());
    assertThat(iface.getAttributes().getLayer(), equalTo("layer3"));
    assertThat(iface.getAttributes().getMode(), equalTo("routed"));
  }

  /** Test deserialization of AciInterface with speed and MTU. */
  @Test
  public void testDeserializeAciInterface_speedAndMtu() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"eth3/3\","
            + "  \"speed\": \"10G\","
            + "  \"mtu\": \"9000\""
            + "}"
            + "}";

    AciInterface iface = MAPPER.readValue(json, AciInterface.class);
    assertThat(iface.getAttributes(), notNullValue());
    assertThat(iface.getAttributes().getSpeed(), equalTo("10G"));
    assertThat(iface.getAttributes().getMtu(), equalTo("9000"));
  }

  /** Test deserialization of AciInterface with auto negotiation. */
  @Test
  public void testDeserializeAciInterface_autoNegotiation() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"eth4/4\","
            + "  \"autoneg\": \"on\","
            + "  \"flowCtrl\": \"on\""
            + "}"
            + "}";

    AciInterface iface = MAPPER.readValue(json, AciInterface.class);
    assertThat(iface.getAttributes(), notNullValue());
    assertThat(iface.getAttributes().getAutoNegotiation(), equalTo("on"));
    assertThat(iface.getAttributes().getFlowControl(), equalTo("on"));
  }

  /** Test deserialization of AciInterface with link debounce and MDIX. */
  @Test
  public void testDeserializeAciInterface_linkOptions() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"eth5/5\","
            + "  \"linkDebounce\": \"100\","
            + "  \"mdix\": \"auto\""
            + "}"
            + "}";

    AciInterface iface = MAPPER.readValue(json, AciInterface.class);
    assertThat(iface.getAttributes(), notNullValue());
    assertThat(iface.getAttributes().getLinkDebounce(), equalTo("100"));
    assertThat(iface.getAttributes().getMdix(), equalTo("auto"));
  }

  /** Test full configuration with bridge domain. */
  @Test
  public void testFullConfiguration_bridgeDomain() throws IOException {
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
            + "            \"fvBD\": {"
            + "              \"attributes\": {"
            + "                \"name\": \"bd1\","
            + "                \"mac\": \"00:22:BD:F8:19:FF\""
            + "              }"
            + "            }"
            + "          }"
            + "        ]"
            + "      }"
            + "    }"
            + "  ]"
            + "}"
            + "}";

    AciConfiguration config = AciConfiguration.fromJson("bridge_domain.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
  }

  /** Test full configuration with application profile and EPG. */
  @Test
  public void testFullConfiguration_appProfileAndEpg() throws IOException {
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
            + "            \"fvAp\": {"
            + "              \"attributes\": {\"name\": \"web_app\"},"
            + "              \"children\": ["
            + "                {"
            + "                  \"fvAEPg\": {"
            + "                    \"attributes\": {"
            + "                      \"name\": \"web_epg\","
            + "                      \"pcEnfPref\": \"enforced\""
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
        AciConfiguration.fromJson("app_profile_epg.json", json, new Warnings());

    assertThat(config.getHostname(), equalTo("test-fabric"));
    assertThat(config.getTenants(), hasKey("tenant1"));
    assertThat(config.getEpgs(), hasKey("tenant1:web_app:web_epg"));
  }

  /** Test deserialization with null attributes for network models. */
  @Test
  public void testDeserialize_nullNetworkAttributes() throws IOException {
    String json = "{}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertThat(bd.getAttributes(), nullValue());

    AciApplicationProfile ap = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(ap.getAttributes(), nullValue());

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), nullValue());

    AciInterface iface = MAPPER.readValue(json, AciInterface.class);
    assertThat(iface.getAttributes(), nullValue());
  }

  /** Test bridge domain with all multicast options. */
  @Test
  public void testAciBridgeDomain_allMulticastOptions() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"mcast_bd\","
            + "  \"mcastAllow\": \"yes\","
            + "  \"v6unkMcastAct\": \"drop\""
            + "}"
            + "}";

    AciBridgeDomain bd = MAPPER.readValue(json, AciBridgeDomain.class);
    assertThat(bd.getAttributes(), notNullValue());
    assertThat(bd.getAttributes().getMulticastAllow(), equalTo("yes"));
    assertThat(bd.getAttributes().getV6UnknownMcastAction(), equalTo("drop"));
  }

  /** Test application profile with all optional fields. */
  @Test
  public void testAciApplicationProfile_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"full_ap\","
            + "  \"descr\": \"Complete app profile\","
            + "  \"prio\": \"level1\","
            + "  \"pcEnfPref\": \"enforced\","
            + "  \"prefGrMemb\": \"include\","
            + "  \"fwdCtrl\": \"proxy\","
            + "  \"hasMcastSource\": \"yes\","
            + "  \"shutdown\": \"no\","
            + "  \"isAttrBasedEPg\": \"no\","
            + "  \"matchT\": \"AtleastOne\","
            + "  \"floodOnEncap\": \"yes\","
            + "  \"exceptionTag\": \"custom\""
            + "}"
            + "}";

    AciApplicationProfile ap = MAPPER.readValue(json, AciApplicationProfile.class);
    assertThat(ap.getAttributes(), notNullValue());
    assertThat(ap.getAttributes().getName(), equalTo("full_ap"));
    assertThat(ap.getAttributes().getPriority(), equalTo("level1"));
    assertThat(ap.getAttributes().getPolicyEnforcementPreference(), equalTo("enforced"));
    assertThat(ap.getAttributes().getPreferredGroupMember(), equalTo("include"));
    assertThat(ap.getAttributes().getForwardingControl(), equalTo("proxy"));
    assertThat(ap.getAttributes().getHasMulticastSource(), equalTo("yes"));
    assertThat(ap.getAttributes().getShutdown(), equalTo("no"));
    assertThat(ap.getAttributes().getIsAttributeBasedEpg(), equalTo("no"));
    assertThat(ap.getAttributes().getMatchType(), equalTo("AtleastOne"));
    assertThat(ap.getAttributes().getFloodOnEncap(), equalTo("yes"));
    assertThat(ap.getAttributes().getExceptionTag(), equalTo("custom"));
  }

  /** Test endpoint group with all optional fields. */
  @Test
  public void testAciEndpointGroup_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"full_epg\","
            + "  \"descr\": \"Complete EPG\","
            + "  \"prio\": \"level2\","
            + "  \"pcEnfPref\": \"unenforced\","
            + "  \"prefGrMemb\": \"exclude\","
            + "  \"fwdCtrl\": \"none\","
            + "  \"hasMcastSource\": \"no\","
            + "  \"shutdown\": \"no\","
            + "  \"isAttrBasedEPg\": \"yes\","
            + "  \"matchT\": \"All\","
            + "  \"floodOnEncap\": \"no\","
            + "  \"exceptionTag\": \"epg_exception\""
            + "}"
            + "}";

    AciEndpointGroup epg = MAPPER.readValue(json, AciEndpointGroup.class);
    assertThat(epg.getAttributes(), notNullValue());
    assertThat(epg.getAttributes().getName(), equalTo("full_epg"));
    assertThat(epg.getAttributes().getPriority(), equalTo("level2"));
    assertThat(epg.getAttributes().getPolicyEnforcementPreference(), equalTo("unenforced"));
    assertThat(epg.getAttributes().getPreferredGroupMember(), equalTo("exclude"));
    assertThat(epg.getAttributes().getForwardingControl(), equalTo("none"));
    assertThat(epg.getAttributes().getHasMcastSource(), equalTo("no"));
    assertThat(epg.getAttributes().getShutdown(), equalTo("no"));
    assertThat(epg.getAttributes().getIsAttributeBasedEpg(), equalTo("yes"));
    assertThat(epg.getAttributes().getMatchType(), equalTo("All"));
    assertThat(epg.getAttributes().getFloodOnEncap(), equalTo("no"));
    assertThat(epg.getAttributes().getExceptionTag(), equalTo("epg_exception"));
  }

  /** Test interface with all optional fields. */
  @Test
  public void testAciInterface_allFields() throws IOException {
    String json =
        "{"
            + "\"attributes\": {"
            + "  \"name\": \"eth1/1\","
            + "  \"descr\": \"Complete interface\","
            + "  \"id\": \"topology/pod-1/node-101/sys-1/phys-[eth1/1]\","
            + "  \"layer\": \"layer3\","
            + "  \"mode\": \"routed\","
            + "  \"autoneg\": \"on\","
            + "  \"speed\": \"100G\","
            + "  \"mtu\": \"9216\","
            + "  \"flowCtrl\": \"on\","
            + "  \"linkDebounce\": \"100\","
            + "  \"mdix\": \"auto\""
            + "}"
            + "}";

    AciInterface iface = MAPPER.readValue(json, AciInterface.class);
    assertThat(iface.getAttributes(), notNullValue());
    assertThat(iface.getAttributes().getName(), equalTo("eth1/1"));
    assertThat(iface.getAttributes().getDescription(), equalTo("Complete interface"));
    assertThat(
        iface.getAttributes().getId(), equalTo("topology/pod-1/node-101/sys-1/phys-[eth1/1]"));
    assertThat(iface.getAttributes().getLayer(), equalTo("layer3"));
    assertThat(iface.getAttributes().getMode(), equalTo("routed"));
    assertThat(iface.getAttributes().getAutoNegotiation(), equalTo("on"));
    assertThat(iface.getAttributes().getSpeed(), equalTo("100G"));
    assertThat(iface.getAttributes().getMtu(), equalTo("9216"));
    assertThat(iface.getAttributes().getFlowControl(), equalTo("on"));
    assertThat(iface.getAttributes().getLinkDebounce(), equalTo("100"));
    assertThat(iface.getAttributes().getMdix(), equalTo("auto"));
  }
}
