package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class InterfaceTest {
  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"interface-name\": \"iface\","
            + "\"topology\": {\"leads-to-internet\":true}"
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, Interface.class),
        equalTo(new Interface("iface", new InterfaceTopology(true))));
  }

  @Test
  public void testJavaSerialization() {
    Interface obj = new Interface("iface", new InterfaceTopology(true));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    Interface obj = new Interface("iface", new InterfaceTopology(true));
    new EqualsTester()
        .addEqualityGroup(obj, new Interface("iface", new InterfaceTopology(true)))
        .addEqualityGroup(new Interface("foo", new InterfaceTopology(true)))
        .addEqualityGroup(new Interface("iface", new InterfaceTopology(false)))
        .testEquals();
  }
}
