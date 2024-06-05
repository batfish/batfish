package org.batfish.vendor.check_point_management;

import static org.batfish.vendor.check_point_management.TestSharedInstances.NAT_SETTINGS_TEST_INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link Host}. */
public final class HostTest {

  private static final Host TEST_INSTANCE =
      new Host(Ip.ZERO, NAT_SETTINGS_TEST_INSTANCE, "foo", Uid.of("0"));

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"host\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"nat-settings\": {" // nat-settings
            + "\"auto-rule\":true,"
            + "\"hide-behind\":\"gateway\","
            + "\"install-on\":\"All\","
            + "\"method\":\"hide\""
            + "}," // nat-settings
            + "\"ipv4-address\":\"0.0.0.0\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, TypedManagementObject.class),
        equalTo(TEST_INSTANCE));
  }

  @Test
  public void testJacksonDeserialization_noIpv4Address() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"host\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"nat-settings\": {" // nat-settings
            + "\"auto-rule\":true,"
            + "\"hide-behind\":\"gateway\","
            + "\"install-on\":\"All\","
            + "\"method\":\"hide\""
            + "}" // nat-settings
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, Host.class),
        equalTo(new Host(null, NAT_SETTINGS_TEST_INSTANCE, "foo", Uid.of("0"))));
  }

  @Test
  public void testJavaSerialization() {
    assertEquals(TEST_INSTANCE, SerializationUtils.clone(TEST_INSTANCE));
  }

  @Test
  public void testEquals() {
    Host obj = new Host(Ip.ZERO, NAT_SETTINGS_TEST_INSTANCE, "foo", Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(obj, new Host(Ip.ZERO, NAT_SETTINGS_TEST_INSTANCE, "foo", Uid.of("0")))
        .addEqualityGroup(new Host(Ip.MAX, NAT_SETTINGS_TEST_INSTANCE, "foo", Uid.of("0")))
        .addEqualityGroup(
            new Host(Ip.ZERO, NatSettingsTest.TEST_INSTANCE_DIFFERENT, "foo", Uid.of("0")))
        .addEqualityGroup(new Host(Ip.ZERO, NAT_SETTINGS_TEST_INSTANCE, "bar", Uid.of("0")))
        .addEqualityGroup(new Host(Ip.ZERO, NAT_SETTINGS_TEST_INSTANCE, "foo", Uid.of("1")))
        .testEquals();
  }
}
