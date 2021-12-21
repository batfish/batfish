package org.batfish.vendor.sonic.representation;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class PortTest {

  @Test
  public void testJavaSerialization() {
    Port obj =
        Port.builder()
            .setAdminStatusUp(true)
            .setAlias("alias")
            .setDescription("desc")
            .setMtu(32)
            .setSpeed(42)
            .build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    Port.Builder builder = Port.builder();
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setAdminStatusUp(true).build())
        .addEqualityGroup(builder.setAlias("alias").build())
        .addEqualityGroup(builder.setDescription("desc").build())
        .addEqualityGroup(builder.setMtu(23).build())
        .addEqualityGroup(builder.setSpeed(42).build())
        .testEquals();
  }

  @Test
  public void testDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "    \"admin_status\": \"up\","
            + "    \"alias\": \"Eth1/1/1\","
            + "    \"description\": \"test\","
            + "    \"fec\": \"none\","
            + "    \"index\": \"1\","
            + "    \"lanes\": \"33\","
            + "    \"mtu\": \"9100\","
            + "    \"override_unreliable_los\": \"off\","
            + "    \"speed\": \"25000\""
            + "}";
    Port port = BatfishObjectMapper.mapper().readValue(input, Port.class);
    assertEquals(
        Port.builder()
            .setAdminStatusUp(true)
            .setAlias("Eth1/1/1")
            .setDescription("test")
            .setMtu(9100)
            .setSpeed(25000)
            .build(),
        port);
  }
}
