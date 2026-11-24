package org.batfish.vendor.sonic.representation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

class DeviceMetadataTest {

  @Test
  void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "    \"bgp_asn\": \"65044\","
            + "    \"default_config_profile\": \"l3\","
            + "    \"docker_routing_config_mode\": \"split\","
            + "    \"hostname\": \"name\","
            + "    \"hwsku\": \"XXXYYY\","
            + "    \"mac\": \"c8:f7:50:ec:07:41\","
            + "    \"platform\": \"x86_64-XXX\","
            + "    \"type\": \"LeafRouter\""
            + "}";
    assertThat(
        BatfishObjectMapper.mapper().readValue(input, DeviceMetadata.class),
        equalTo(new DeviceMetadata("name")));
  }

  @Test
  void testJavaSerialization() {
    DeviceMetadata obj = new DeviceMetadata("name");
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new DeviceMetadata(null), new DeviceMetadata(null))
        .addEqualityGroup(new DeviceMetadata("hostname"))
        .testEquals();
  }
}
