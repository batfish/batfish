package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

public class InterfaceTest {
  /**
   * Instance of this class populated with arbitrary values. Useful for generating a valid object
   * for use in tests.
   */
  public static final Interface TEST_INSTANCE =
      new Interface("eth0", InterfaceTopologyTest.TEST_INSTANCE, Ip.parse("10.0.1.1"), 24);

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"interface-name\": \"iface\","
            + "\"ipv4-address\": \"10.10.10.1\","
            + "\"ipv4-mask-length\": 24,"
            + "\"topology\": {\"leads-to-internet\":true}"
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, Interface.class),
        equalTo(new Interface("iface", new InterfaceTopology(true), Ip.parse("10.10.10.1"), 24)));
  }

  @Test
  public void testJacksonDeserialization_noIpv4Address() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"interface-name\": \"iface\","
            + "\"topology\": {\"leads-to-internet\":true}"
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, Interface.class),
        equalTo(new Interface("iface", new InterfaceTopology(true), null, null)));
  }

  @Test
  public void testJavaSerialization() {
    Interface obj = TEST_INSTANCE;
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    Interface obj = new Interface("iface", new InterfaceTopology(true), Ip.parse("10.10.10.1"), 24);
    new EqualsTester()
        .addEqualityGroup(
            obj, new Interface("iface", new InterfaceTopology(true), Ip.parse("10.10.10.1"), 24))
        .addEqualityGroup(
            new Interface("foo", new InterfaceTopology(true), Ip.parse("10.10.10.1"), 24))
        .addEqualityGroup(
            new Interface("iface", new InterfaceTopology(false), Ip.parse("10.10.10.1"), 24))
        .addEqualityGroup(
            new Interface("iface", new InterfaceTopology(true), Ip.parse("10.10.10.10"), 24))
        .addEqualityGroup(
            new Interface("iface", new InterfaceTopology(true), Ip.parse("10.10.10.1"), 25))
        .testEquals();
  }
}
