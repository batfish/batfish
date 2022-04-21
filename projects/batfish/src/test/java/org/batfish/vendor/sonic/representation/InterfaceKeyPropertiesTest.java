package org.batfish.vendor.sonic.representation;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class InterfaceKeyPropertiesTest {

  @Test
  public void testJavaSerialization() {
    InterfaceKeyProperties obj =
        InterfaceKeyProperties.builder()
            .setForcedMgmtRoutes(ImmutableList.of())
            .setGwAddr("1.1.1.1")
            .setSecondary(true)
            .build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    InterfaceKeyProperties.Builder builder = InterfaceKeyProperties.builder();
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setForcedMgmtRoutes(ImmutableList.of("1.1.1.1")).build())
        .addEqualityGroup(builder.setGwAddr("2.2.2.2").build())
        .addEqualityGroup(builder.setSecondary(true).build())
        .testEquals();
  }

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{\"secondary\": \"true\", \"forced_mgmt_routes\": [\"1.1.1.1\"], \"gwaddr\": \"2.2.2.2\"}";
    assertEquals(
        InterfaceKeyProperties.builder()
            .setForcedMgmtRoutes(ImmutableList.of("1.1.1.1"))
            .setGwAddr("2.2.2.2")
            .setSecondary(true)
            .build(),
        BatfishObjectMapper.mapper().readValue(input, InterfaceKeyProperties.class));
  }
}
