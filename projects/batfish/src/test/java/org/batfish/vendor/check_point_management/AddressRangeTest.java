package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.junit.Test;

/** Test of {@link AddressRange}. */
public final class AddressRangeTest {

  public static final AddressRange TEST_INSTANCE =
      new AddressRange(
          Ip.ZERO,
          Ip.parse("0.0.0.1"),
          Ip6.ZERO,
          Ip6.parse("::1"),
          NatSettingsTest.TEST_INSTANCE,
          "foo",
          Uid.of("0"));

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"address-range\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"nat-settings\": {" // nat-settings
            + "\"auto-rule\":true,"
            + "\"hide-behind\":\"gateway\","
            + "\"install-on\":\"All\","
            + "\"method\":\"hide\""
            + "}," // nat-settings
            + "\"ipv4-address-first\":\"0.0.0.0\","
            + "\"ipv4-address-last\":\"0.0.0.1\","
            + "\"ipv6-address-first\":\"::\","
            + "\"ipv6-address-last\":\"::1\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, AddressRange.class),
        equalTo(
            new AddressRange(
                Ip.ZERO,
                Ip.parse("0.0.0.1"),
                Ip6.ZERO,
                Ip6.parse("::1"),
                NatSettingsTest.TEST_INSTANCE,
                "foo",
                Uid.of("0"))));
  }

  @Test
  public void testJavaSerialization() {
    AddressRange obj =
        new AddressRange(
            Ip.ZERO,
            Ip.parse("0.0.0.1"),
            Ip6.ZERO,
            Ip6.parse("::1"),
            NatSettingsTest.TEST_INSTANCE,
            "foo",
            Uid.of("0"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    AddressRange obj =
        new AddressRange(null, null, null, null, NatSettingsTest.TEST_INSTANCE, "foo", Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(
            obj,
            new AddressRange(
                null, null, null, null, NatSettingsTest.TEST_INSTANCE, "foo", Uid.of("0")))
        .addEqualityGroup(
            new AddressRange(
                Ip.ZERO, null, null, null, NatSettingsTest.TEST_INSTANCE, "foo", Uid.of("0")))
        .addEqualityGroup(
            new AddressRange(
                null, Ip.ZERO, null, null, NatSettingsTest.TEST_INSTANCE, "foo", Uid.of("0")))
        .addEqualityGroup(
            new AddressRange(
                null, null, Ip6.ZERO, null, NatSettingsTest.TEST_INSTANCE, "foo", Uid.of("0")))
        .addEqualityGroup(
            new AddressRange(
                null, null, null, Ip6.ZERO, NatSettingsTest.TEST_INSTANCE, "foo", Uid.of("0")))
        .addEqualityGroup(
            new AddressRange(
                null,
                null,
                null,
                null,
                NatSettingsTest.TEST_INSTANCE_DIFFERENT,
                "foo",
                Uid.of("0")))
        .addEqualityGroup(
            new AddressRange(
                null, null, null, null, NatSettingsTest.TEST_INSTANCE, "bar", Uid.of("0")))
        .addEqualityGroup(
            new AddressRange(
                null, null, null, null, NatSettingsTest.TEST_INSTANCE, "foo", Uid.of("1")))
        .testEquals();
  }
}
