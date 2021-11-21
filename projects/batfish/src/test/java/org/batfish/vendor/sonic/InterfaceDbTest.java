package org.batfish.vendor.sonic;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.vendor.sonic.InterfaceDb.Interface;
import org.junit.Test;

public class InterfaceDbTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"Ethernet136\": {},"
            + "\"Ethernet136|0:0:0:0:0:ffff:ac13:5d00/127\": {},"
            + "\"Ethernet136|172.19.93.0/31\": {},"
            + "\"Ethernet137\": {}"
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, InterfaceDb.class),
        equalTo(
            new InterfaceDb(
                ImmutableMap.of(
                    "Ethernet136",
                    new Interface(ConcreteInterfaceAddress.parse("172.19.93.0/31")),
                    "Ethernet137",
                    new Interface(null)))));
  }

  @Test
  public void testJavaSerialization() {
    InterfaceDb obj = new InterfaceDb(ImmutableMap.of());
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    InterfaceDb obj = new InterfaceDb(ImmutableMap.of());
    new EqualsTester()
        .addEqualityGroup(obj, new InterfaceDb(ImmutableMap.of()))
        .addEqualityGroup(new InterfaceDb(ImmutableMap.of("iface", new Interface(null))))
        .testEquals();
  }

  @Test
  public void testInterfaceEquals() {
    Interface obj = new Interface(null);
    new EqualsTester()
        .addEqualityGroup(obj, new Interface(null))
        .addEqualityGroup(new Interface(ConcreteInterfaceAddress.parse("1.1.1.1/24")))
        .testEquals();
  }
}
