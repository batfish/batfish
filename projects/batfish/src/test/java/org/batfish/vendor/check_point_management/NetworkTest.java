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

/** Test of {@link Network}. */
public final class NetworkTest {

  public static final Network TEST_INSTANCE =
      new Network("foo", NatSettingsTest.TEST_INSTANCE, Ip.ZERO, Ip.MAX, Uid.of("0"));

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"network\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"nat-settings\": {" // nat-settings
            + "\"auto-rule\":true,"
            + "\"hide-behind\":\"gateway\","
            + "\"install-on\":\"All\","
            + "\"method\":\"hide\""
            + "}," // nat-settings
            + "\"subnet4\":\"0.0.0.0\","
            + "\"subnet-mask\":\"255.255.255.255\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, TypedManagementObject.class),
        equalTo(TEST_INSTANCE));
  }

  @Test
  public void testJavaSerialization() {
    assertEquals(TEST_INSTANCE, SerializationUtils.clone(TEST_INSTANCE));
  }

  @Test
  public void testEquals() {
    Network obj = new Network("foo", NatSettingsTest.TEST_INSTANCE, Ip.ZERO, Ip.MAX, Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(
            obj, new Network("foo", NatSettingsTest.TEST_INSTANCE, Ip.ZERO, Ip.MAX, Uid.of("0")))
        .addEqualityGroup(
            new Network("bar", NatSettingsTest.TEST_INSTANCE, Ip.ZERO, Ip.MAX, Uid.of("0")))
        .addEqualityGroup(
            new Network(
                "foo", NatSettingsTest.TEST_INSTANCE_DIFFERENT, Ip.ZERO, Ip.MAX, Uid.of("0")))
        .addEqualityGroup(
            new Network("foo", NatSettingsTest.TEST_INSTANCE, Ip.MAX, Ip.MAX, Uid.of("0")))
        .addEqualityGroup(
            new Network("foo", NatSettingsTest.TEST_INSTANCE, Ip.ZERO, Ip.ZERO, Uid.of("0")))
        .addEqualityGroup(
            new Network("foo", NatSettingsTest.TEST_INSTANCE, Ip.ZERO, Ip.MAX, Uid.of("1")))
        .testEquals();
  }
}
