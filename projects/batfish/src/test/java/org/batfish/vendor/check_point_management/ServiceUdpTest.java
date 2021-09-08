package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link ServiceUdp}. */
public final class ServiceUdpTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"service-udp\","
            + "\"uid\":\"1\","
            + "\"name\":\"foo\","
            + "\"port\":\"8642\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, TypedManagementObject.class),
        equalTo(new ServiceUdp("foo", "8642", Uid.of("1"))));
  }

  @Test
  public void testJavaSerialization() {
    ServiceUdp obj = new ServiceUdp("foo", "8642", Uid.of("1"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    ServiceUdp obj = new ServiceUdp("foo", "8642", Uid.of("1"));
    new EqualsTester()
        .addEqualityGroup(obj, new ServiceUdp("foo", "8642", Uid.of("1")))
        .addEqualityGroup(new ServiceUdp("foo0", "8642", Uid.of("1")))
        .addEqualityGroup(new ServiceUdp("foo", "9999", Uid.of("1")))
        .addEqualityGroup(new ServiceUdp("foo", "8642", Uid.of("10")))
        .testEquals();
  }
}
