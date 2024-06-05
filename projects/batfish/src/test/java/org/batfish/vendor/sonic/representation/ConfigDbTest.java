package org.batfish.vendor.sonic.representation;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.batfish.vendor.sonic.representation.ConfigDb.createInterfaces;
import static org.batfish.vendor.sonic.representation.ConfigDb.deserialize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Prefix;
import org.batfish.vendor.sonic.representation.AclRule.PacketAction;
import org.batfish.vendor.sonic.representation.AclTable.Stage;
import org.batfish.vendor.sonic.representation.AclTable.Type;
import org.batfish.vendor.sonic.representation.VlanMember.TaggingMode;
import org.junit.Test;

public class ConfigDbTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input = "{ \"GARBAGE\": 1, \"INTERFACE\": {\"Ethernet0\": {}}}";
    Warnings warnings = new Warnings(true, true, true);
    assertThat(
        deserialize(input, warnings).getInterfaces(),
        equalTo(ImmutableMap.of("Ethernet0", new L3Interface(ImmutableMap.of()))));
    assertThat(
        Iterables.getOnlyElement(warnings.getUnimplementedWarnings()).getText(),
        equalTo("Unimplemented configdb table 'GARBAGE'"));
  }

  @Test
  public void testDeserializationAclTable() throws JsonProcessingException {
    String input =
        "{\"ACL_TABLE\": {"
            + "        \"ctrl-plane-snmp-acl\": {"
            + "            \"ports\": ["
            + "                \"CtrlPlane\""
            + "            ],"
            + "            \"stage\": \"INGRESS\","
            + "            \"type\": \"L3\""
            + "        }"
            + "    }}";
    assertThat(
        deserialize(input, new Warnings()).getAclTables(),
        equalTo(
            ImmutableMap.of(
                "ctrl-plane-snmp-acl",
                AclTable.builder()
                    .setPorts(ImmutableList.of("CtrlPlane"))
                    .setStage(Stage.INGRESS)
                    .setType(Type.L3)
                    .build())));
  }

  @Test
  public void testDeserializationAclRule() throws JsonProcessingException {
    String input =
        "{\"ACL_RULE\": {\n"
            + "        \"ctrl-plane-snmp-acl|RULE_10\": {\n"
            + "            \"IP_PROTOCOL\": \"17\",\n"
            + "            \"L4_DST_PORT\": \"161\",\n"
            + "            \"PACKET_ACTION\": \"FORWARD\",\n"
            + "            \"PRIORITY\": \"10\",\n"
            + "            \"SRC_IP\": \"10.1.4.0/22\"\n"
            + "        }}}";
    assertThat(
        deserialize(input, new Warnings()).getAclRules(),
        equalTo(
            ImmutableMap.of(
                "ctrl-plane-snmp-acl|RULE_10",
                AclRule.builder()
                    .setIpProtocol(17)
                    .setL4DstPort(161)
                    .setPacketAction(PacketAction.FORWARD)
                    .setPriority(10)
                    .setSrcIp(Prefix.parse("10.1.4.0/22"))
                    .build())));
  }

  @Test
  public void testDeserializationDeviceMetadata() throws JsonProcessingException {
    String input = "{ \"DEVICE_METADATA\": {\"localhost\": {\"hostname\": \"name\"}}}";
    assertThat(
        deserialize(input, new Warnings()).getDeviceMetadata(),
        equalTo(ImmutableMap.of("localhost", new DeviceMetadata("name"))));
  }

  @Test
  public void testDeserializationInterface() throws JsonProcessingException {
    String input =
        "{ \"INTERFACE\": {"
            + "\"Ethernet136\": {},"
            + "\"Ethernet136|0:0:0:0:0:ffff:ac13:5d00/127\": {},"
            + "\"Ethernet136|172.19.93.0/31\": {},"
            + "\"Ethernet136|172.19.94.0/31\": {\"secondary\": \"true\"},"
            + "\"Ethernet137\": {}"
            + "}}";
    assertThat(
        deserialize(input, new Warnings()).getInterfaces(),
        equalTo(
            ImmutableMap.of(
                "Ethernet136",
                new L3Interface(
                    ImmutableMap.of(
                        ConcreteInterfaceAddress.parse("172.19.93.0/31"),
                        InterfaceKeyProperties.builder().build(),
                        ConcreteInterfaceAddress.parse("172.19.94.0/31"),
                        InterfaceKeyProperties.builder().setSecondary(true).build())),
                "Ethernet137",
                new L3Interface(ImmutableMap.of()))));
  }

  @Test
  public void testDeserializationLoopback() throws JsonProcessingException {
    String input = "{ \"LOOPBACK\": {\"Loopback0\": {}}}";
    assertThat(
        deserialize(input, new Warnings()).getLoopbacks(), equalTo(ImmutableSet.of("Loopback0")));
  }

  @Test
  public void testDeserializationLoopbackInterface() throws JsonProcessingException {
    String input = "{ \"LOOPBACK_INTERFACE\": {\"Loopback0\": {}}}";
    assertThat(
        deserialize(input, new Warnings()).getLoopbackInterfaces(),
        equalTo(ImmutableMap.of("Loopback0", new L3Interface(ImmutableMap.of()))));
  }

  @Test
  public void testDeserializationMgmtInterface() throws JsonProcessingException {
    String input = "{ \"MGMT_INTERFACE\": {\"eth0|10.11.150.11/16\": {\"gwaddr\": \"10.11.0.1\"}}}";
    Warnings warnings = new Warnings(true, true, true);
    assertThat(
        deserialize(input, warnings).getMgmtInterfaces(),
        equalTo(
            ImmutableMap.of(
                "eth0",
                new L3Interface(
                    ImmutableMap.of(
                        ConcreteInterfaceAddress.parse("10.11.150.11/16"),
                        InterfaceKeyProperties.builder().setGwAddr("10.11.0.1").build())))));
  }

  @Test
  public void testJacksonDeserializationMgmtPort() throws JsonProcessingException {
    String input =
        "{\"MGMT_PORT\" :{"
            + "        \"eth0\": {"
            + "            \"description\": \"Management0\","
            + "            \"speed\": \"1000\""
            + "        }"
            + "}}";
    assertThat(
        deserialize(input, new Warnings()).getMgmtPorts(),
        equalTo(
            ImmutableMap.of(
                "eth0", Port.builder().setDescription("Management0").setSpeed(1000).build())));
  }

  @Test
  public void testJacksonDeserializationMgmtVrfConfig() throws JsonProcessingException {
    String input =
        "{\"MGMT_VRF_CONFIG\" :{"
            + "        \"vrf_global\": {"
            + "            \"mgmtVrfEnabled\": \"true\""
            + "        }"
            + "}}";
    assertThat(
        deserialize(input, new Warnings()).getMgmtVrfs(),
        equalTo(ImmutableMap.of("vrf_global", MgmtVrf.builder().setMgmtVrfEnabled(true).build())));
  }

  @Test
  public void testDeserializationNtpServer() throws JsonProcessingException {
    String input = "{ \"NTP_SERVER\": {\"23.92.29.245\": {}, \"2.debian.pool.ntp.org\": {}}}";
    assertThat(
        deserialize(input, new Warnings()).getNtpServers(),
        equalTo(ImmutableSet.of("23.92.29.245", "2.debian.pool.ntp.org")));
  }

  @Test
  public void testJacksonDeserializationPort() throws JsonProcessingException {
    String input =
        "{\"PORT\" :{"
            + "        \"Ethernet0\": {"
            + "            \"description\": \"L3-sbf00-fr001:Ethernet0\""
            + "        },"
            + "        \"Ethernet8\": {"
            + "            \"mtu\": \"9212\""
            + "        }"
            + "}}";
    assertThat(
        deserialize(input, new Warnings()).getPorts(),
        equalTo(
            ImmutableMap.of(
                "Ethernet0",
                Port.builder().setDescription("L3-sbf00-fr001:Ethernet0").build(),
                "Ethernet8",
                Port.builder().setMtu(9212).build())));
  }

  @Test
  public void testDeserializationSyslogServer() throws JsonProcessingException {
    String input = "{ \"SYSLOG_SERVER\": {\"23.92.29.245\": {}, \"10.11.150.5\": {}}}";
    assertThat(
        deserialize(input, new Warnings()).getSyslogServers(),
        equalTo(ImmutableSet.of("23.92.29.245", "10.11.150.5")));
  }

  @Test
  public void testDeserializationTacplus() throws JsonProcessingException {
    String input =
        "{ \"TACPLUS\": {\"global\": {"
            + "    \"auth_type\": \"login\","
            + "    \"src_intf\": \"Loopback0\""
            + "}}}";
    assertThat(
        deserialize(input, new Warnings()).getTacplusses(),
        equalTo(ImmutableMap.of("global", Tacplus.builder().setSrcIntf("Loopback0").build())));
  }

  @Test
  public void testDeserializationTacplusServer() throws JsonProcessingException {
    String input =
        "{ \"TACPLUS_SERVER\": {\"23.92.29.245\": {"
            + "    \"auth_type\": \"pap\","
            + "    \"passkey\": \"U2FsdGVkX19is+CjcG3V8dSm539YTkwEby6VzzfkDN0=\","
            + "    \"priority\": \"1\","
            + "    \"tcp_port\": \"49\","
            + "    \"timeout\": \"5\""
            + "}}}";
    assertThat(
        deserialize(input, new Warnings()).getTacplusServers(),
        equalTo(ImmutableSet.of("23.92.29.245")));
  }

  @Test
  public void testDeserializationVlan() throws JsonProcessingException {
    String input =
        "{ \"VLAN\": { \"Vlan2\": {\"dhcp_servers\": [\"1.1.1.1\"], \"members\": [\"Ethernet0\"],"
            + " \"vlanid\": \"2\"}}}";
    assertThat(
        deserialize(input, new Warnings()).getVlans(),
        equalTo(
            ImmutableMap.of(
                "Vlan2",
                Vlan.builder()
                    .setDhcpServers(ImmutableList.of("1.1.1.1"))
                    .setMembers(ImmutableList.of("Ethernet0"))
                    .setVlanId(2)
                    .build())));
  }

  @Test
  public void testDeserializationVlanList() throws JsonProcessingException {
    String input =
        "{ \"VLAN\": { \"Vlan2\": {\"dhcp_servers\": [\"1.1.1.1\"], \"members\": [\"Ethernet0\"],"
            + " \"vlanid\": [\"2\"]}}}";
    assertThat(
        deserialize(input, new Warnings()).getVlans(),
        equalTo(
            ImmutableMap.of(
                "Vlan2",
                Vlan.builder()
                    .setDhcpServers(ImmutableList.of("1.1.1.1"))
                    .setMembers(ImmutableList.of("Ethernet0"))
                    .setVlanId(2)
                    .build())));
  }

  @Test
  public void testDeserializationVlanInterface() throws JsonProcessingException {
    String input = "{ \"VLAN_INTERFACE\": {\"Vlan2|10.11.150.11/16\": {}}}";
    assertThat(
        deserialize(input, new Warnings()).getVlanInterfaces(),
        equalTo(
            ImmutableMap.of(
                "Vlan2",
                new L3Interface(
                    ImmutableMap.of(
                        ConcreteInterfaceAddress.parse("10.11.150.11/16"),
                        InterfaceKeyProperties.builder().build())))));
  }

  @Test
  public void testJacksonDeserializationVlanMember() throws JsonProcessingException {
    String input =
        "{\"VLAN_MEMBER\" :{"
            + "        \"Vlan2|Ethernet2\": {"
            + "            \"tagging_mode\": \"tagged\""
            + "        }"
            + "}}";
    assertThat(
        deserialize(input, new Warnings()).getVlanMembers(),
        equalTo(
            ImmutableMap.of(
                "Vlan2|Ethernet2",
                VlanMember.builder().setTaggingMode(TaggingMode.TAGGED).build())));
  }

  @Test
  public void testCreateInterfaces() {
    Warnings warnings = new Warnings(true, true, true);
    assertThat(
        createInterfaces(
            ImmutableMap.of(
                "Ethernet136",
                InterfaceKeyProperties.builder().build(),
                "Ethernet136|172.19.93.0/31",
                InterfaceKeyProperties.builder().build(),
                "Ethernet136|172.19.94.0/31",
                InterfaceKeyProperties.builder()
                    .setGwAddr("1.1.1.1")
                    .setForcedMgmtRoutes(ImmutableList.of("2.2.2.2"))
                    .setSecondary(true)
                    .build(),
                "Ethernet136|0:0:0:0:0:ffff:ac13:5d00/127",
                InterfaceKeyProperties.builder().build(),
                "Ethernet137",
                InterfaceKeyProperties.builder().build()),
            warnings),
        equalTo(
            ImmutableMap.of(
                "Ethernet136",
                new L3Interface(
                    ImmutableMap.of(
                        ConcreteInterfaceAddress.parse("172.19.93.0/31"),
                        InterfaceKeyProperties.builder().build(),
                        ConcreteInterfaceAddress.parse("172.19.94.0/31"),
                        InterfaceKeyProperties.builder()
                            .setGwAddr("1.1.1.1")
                            .setForcedMgmtRoutes(ImmutableList.of("2.2.2.2"))
                            .setSecondary(true)
                            .build())),
                "Ethernet137",
                new L3Interface(ImmutableMap.of()))));

    assertThat(
        warnings.getUnimplementedWarnings(),
        contains(
            hasText(
                "Property 'forced_mgmt_routes' of 'Ethernet136|172.19.94.0/31' is not implemented"),
            hasText("Property 'gwaddr' of 'Ethernet136|172.19.94.0/31' is not implemented")));
    //        equalTo("Unimplemented MGMT_INTERFACE property 'gwaddr'"));
  }
}
