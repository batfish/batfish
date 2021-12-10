package org.batfish.vendor.sonic.representation;

import static org.batfish.vendor.sonic.representation.ConfigDb.createInterfaces;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.junit.Test;

public class ConfigDbTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input = "{ \"GARBAGE\": 1, \"INTERFACE\": {}}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, ConfigDb.class),
        equalTo(ConfigDb.builder().build()));
  }

  @Test
  public void testDeserializationDeviceMetadata() throws JsonProcessingException {
    String input = "{ \"DEVICE_METADATA\": {\"localhost\": {\"hostname\": \"name\"}}}";
    assertThat(
        BatfishObjectMapper.mapper().readValue(input, ConfigDb.class),
        equalTo(
            ConfigDb.builder()
                .setDeviceMetadata(ImmutableMap.of("localhost", new DeviceMetadata("name")))
                .build()));
  }

  @Test
  public void testDeserializationInterface() throws JsonProcessingException {
    String input =
        "{ \"INTERFACE\": {"
            + "\"Ethernet136\": {},"
            + "\"Ethernet136|0:0:0:0:0:ffff:ac13:5d00/127\": {},"
            + "\"Ethernet136|172.19.93.0/31\": {},"
            + "\"Ethernet137\": {}"
            + "}}";
    assertThat(
        BatfishObjectMapper.mapper().readValue(input, ConfigDb.class),
        equalTo(
            ConfigDb.builder()
                .setInterfaces(
                    ImmutableMap.of(
                        "Ethernet136",
                        new L3Interface(ConcreteInterfaceAddress.parse("172.19.93.0/31")),
                        "Ethernet137",
                        new L3Interface(null)))
                .build()));
  }

  @Test
  public void testDeserializationLoopback() throws JsonProcessingException {
    String input = "{ \"LOOPBACK\": {\"Loopback0\": {}}}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, ConfigDb.class),
        equalTo(
            ConfigDb.builder()
                .setLoopbacks(ImmutableMap.of("Loopback0", new L3Interface(null)))
                .build()));
  }

  @Test
  public void testDeserializationMgmtInterface() throws JsonProcessingException {
    String input = "{ \"MGMT_INTERFACE\": {\"eth0|10.11.150.11/16\": {\"gwaddr\": \"10.11.0.1\"}}}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, ConfigDb.class),
        equalTo(
            ConfigDb.builder()
                .setMgmtInterfaces(
                    ImmutableMap.of(
                        "eth0", new L3Interface(ConcreteInterfaceAddress.parse("10.11.150.11/16"))))
                .build()));
  }

  @Test
  public void testJacksonDeserializationMgmtPort() throws JsonProcessingException {
    String input =
        "{\"MGMT_PORT\" :{\n"
            + "        \"eth0\": {\n"
            + "            \"description\": \"Management0\",\n"
            + "            \"speed\": \"1000\""
            + "        }\n"
            + "}}";

    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, ConfigDb.class),
        equalTo(
            ConfigDb.builder()
                .setMgmtPorts(
                    ImmutableMap.of(
                        "eth0",
                        Port.builder().setDescription("Management0").setSpeed(1000).build()))
                .build()));
  }

  @Test
  public void testJacksonDeserializationMgmtVrfConfig() throws JsonProcessingException {
    String input =
        "{\"MGMT_VRF_CONFIG\" :{\n"
            + "        \"vrf_global\": {\n"
            + "            \"mgmtVrfEnabled\": \"true\""
            + "        }\n"
            + "}}";

    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, ConfigDb.class),
        equalTo(
            ConfigDb.builder()
                .setMgmtVrfs(
                    ImmutableMap.of(
                        "vrf_global", MgmtVrf.builder().setMgmtVrfEnabled(true).build()))
                .build()));
  }

  @Test
  public void testDeserializationNtpServer() throws JsonProcessingException {
    String input = "{ \"NTP_SERVER\": {\"23.92.29.245\": {}, \"2.debian.pool.ntp.org\": {}}}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, ConfigDb.class),
        equalTo(
            ConfigDb.builder()
                .setNtpServers(ImmutableSet.of("23.92.29.245", "2.debian.pool.ntp.org"))
                .build()));
  }

  @Test
  public void testJacksonDeserializationPort() throws JsonProcessingException {
    String input =
        "{\"PORT\" :{\n"
            + "        \"Ethernet0\": {\n"
            + "            \"description\": \"L3-sbf00-fr001:Ethernet0\"\n"
            + "        },\n"
            + "        \"Ethernet8\": {\n"
            + "            \"mtu\": \"9212\"\n"
            + "        }"
            + "}}";

    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, ConfigDb.class),
        equalTo(
            ConfigDb.builder()
                .setPorts(
                    ImmutableMap.of(
                        "Ethernet0",
                        Port.builder().setDescription("L3-sbf00-fr001:Ethernet0").build(),
                        "Ethernet8",
                        Port.builder().setMtu(9212).build()))
                .build()));
  }

  @Test
  public void testDeserializationSyslogServer() throws JsonProcessingException {
    String input = "{ \"SYSLOG_SERVER\": {\"23.92.29.245\": {}, \"10.11.150.5\": {}}}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, ConfigDb.class),
        equalTo(
            ConfigDb.builder()
                .setSyslogServers(ImmutableSet.of("23.92.29.245", "10.11.150.5"))
                .build()));
  }

  @Test
  public void testCreateInterfaces() {
    assertThat(
        createInterfaces(
            ImmutableSet.of(
                "Ethernet136",
                "Ethernet136|172.19.93.0/31",
                "Ethernet136|0:0:0:0:0:ffff:ac13:5d00/127",
                "Ethernet137")),
        equalTo(
            ImmutableMap.of(
                "Ethernet136",
                new L3Interface(ConcreteInterfaceAddress.parse("172.19.93.0/31")),
                "Ethernet137",
                new L3Interface(null))));
  }

  @Test
  public void testJavaSerialization() {
    ConfigDb obj = ConfigDb.builder().build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    ConfigDb.Builder builder = ConfigDb.builder();
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(
            builder
                .setDeviceMetadata(ImmutableMap.of("localhost", new DeviceMetadata(null)))
                .build())
        .addEqualityGroup(
            builder.setInterfaces(ImmutableMap.of("iface", new L3Interface(null))).build())
        .addEqualityGroup(
            builder.setLoopbacks(ImmutableMap.of("l0", new L3Interface(null))).build())
        .addEqualityGroup(
            builder.setMgmtInterfaces(ImmutableMap.of("eth0", new L3Interface(null))).build())
        .addEqualityGroup(
            builder.setMgmtPorts(ImmutableMap.of("eth0", Port.builder().build())).build())
        .addEqualityGroup(
            builder.setMgmtVrfs(ImmutableMap.of("vrf_global", MgmtVrf.builder().build())).build())
        .addEqualityGroup(builder.setNtpServers(ImmutableSet.of("ntp")).build())
        .addEqualityGroup(builder.setPorts(ImmutableMap.of("a", Port.builder().build())))
        .addEqualityGroup(builder.setSyslogServers(ImmutableSet.of("aa")).build())
        .testEquals();
  }
}
