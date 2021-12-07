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
        equalTo(new ConfigDb(ImmutableMap.of(), ImmutableMap.of())));
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
        .addEqualityGroup(ConfigDb.builder().build(), ConfigDb.builder().build())
        .addEqualityGroup(
            builder
                .setDeviceMetadata(ImmutableMap.of("localhost", new DeviceMetadata(null)))
                .build())
        .addEqualityGroup(
            builder.setInterfaces(ImmutableMap.of("iface", new L3Interface(null))).build())
        .testEquals();
  }
}
