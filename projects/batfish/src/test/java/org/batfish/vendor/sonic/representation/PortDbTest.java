package org.batfish.vendor.sonic.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.sonic.representation.PortDb.Port;
import org.junit.Test;

public class PortDbTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{\n"
            + "        \"Ethernet0\": {\n"
            + "            \"description\": \"L3-sbf00-fr001:Ethernet0\"\n"
            + "        },\n"
            + "        \"Ethernet8\": {\n"
            + "            \"mtu\": \"9212\"\n"
            + "        }"
            + "}";

    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, PortDb.class),
        equalTo(
            new PortDb(
                ImmutableMap.of(
                    "Ethernet0",
                    new Port(ImmutableMap.of("description", "L3-sbf00-fr001:Ethernet0")),
                    "Ethernet8",
                    new Port(ImmutableMap.of("mtu", "9212"))))));
  }

  @Test
  public void testJavaSerialization() {
    PortDb obj = new PortDb(ImmutableMap.of("iface", new Port(ImmutableMap.of())));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    PortDb obj = new PortDb(ImmutableMap.of());
    new EqualsTester()
        .addEqualityGroup(obj, new PortDb(ImmutableMap.of()))
        .addEqualityGroup(new PortDb(ImmutableMap.of("iface", new Port(ImmutableMap.of()))))
        .testEquals();
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testPortEquals() {
    Port obj = new Port(ImmutableMap.of());
    new EqualsTester()
        .addEqualityGroup(obj, new Port(ImmutableMap.of()))
        .addEqualityGroup(new Port(ImmutableMap.of("a", "b")))
        .testEquals();
  }
}
