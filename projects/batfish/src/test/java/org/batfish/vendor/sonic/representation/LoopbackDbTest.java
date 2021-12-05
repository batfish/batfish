package org.batfish.vendor.sonic.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.junit.Test;

public class LoopbackDbTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input = "{\"Loopback0|172.19.93.0/31\": {}}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, InterfaceDb.class),
        equalTo(
            new InterfaceDb(
                ImmutableMap.of(
                    "Loopback0",
                    new L3Interface(ConcreteInterfaceAddress.parse("172.19.93.0/31"))))));
  }

  @Test
  public void testJavaSerialization() {
    InterfaceDb obj =
        new InterfaceDb(
            ImmutableMap.of(
                "iface", new L3Interface(ConcreteInterfaceAddress.parse("172.19.93.0/31"))));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    InterfaceDb obj = new InterfaceDb(ImmutableMap.of());
    new EqualsTester()
        .addEqualityGroup(obj, new InterfaceDb(ImmutableMap.of()))
        .addEqualityGroup(new InterfaceDb(ImmutableMap.of("iface", new L3Interface(null))))
        .testEquals();
  }
}
