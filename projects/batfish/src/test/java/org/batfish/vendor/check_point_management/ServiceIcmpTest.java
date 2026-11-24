package org.batfish.vendor.check_point_management;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link ServiceIcmp}. */
final class ServiceIcmpTest {

  @Test
  void testJacksonDeserialization() throws JsonProcessingException {
    {
      String input =
          "{"
              + "\"GARBAGE\":0,"
              + "\"type\":\"service-icmp\","
              + "\"uid\":\"1\","
              + "\"name\":\"foo\","
              + "\"icmp-code\":3,"
              + "\"icmp-type\":8"
              + "}";
      assertThat(
          BatfishObjectMapper.ignoreUnknownMapper().readValue(input, TypedManagementObject.class),
          equalTo(new ServiceIcmp("foo", 8, 3, Uid.of("1"))));
    }
    {
      // missing icmp-code
      String input =
          "{"
              + "\"GARBAGE\":0,"
              + "\"type\":\"service-icmp\","
              + "\"uid\":\"1\","
              + "\"name\":\"foo\","
              + "\"icmp-type\":8"
              + "}";
      assertThat(
          BatfishObjectMapper.ignoreUnknownMapper().readValue(input, TypedManagementObject.class),
          equalTo(new ServiceIcmp("foo", 8, null, Uid.of("1"))));
    }
  }

  @Test
  void testJavaSerialization() {
    ServiceIcmp obj = new ServiceIcmp("foo", 8, 3, Uid.of("1"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  void testEquals() {
    ServiceIcmp obj = new ServiceIcmp("foo", 8, 3, Uid.of("1"));
    new EqualsTester()
        .addEqualityGroup(obj, new ServiceIcmp("foo", 8, 3, Uid.of("1")))
        .addEqualityGroup(new ServiceIcmp("bar", 8, 3, Uid.of("1")))
        .addEqualityGroup(new ServiceIcmp("bar", 7, 3, Uid.of("1")))
        .addEqualityGroup(new ServiceIcmp("bar", 7, 4, Uid.of("1")))
        .addEqualityGroup(new ServiceIcmp("bar", 7, 4, Uid.of("3")))
        .testEquals();
  }
}
