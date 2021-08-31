package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link ServiceTcp}. */
public final class ServiceTcpTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"service-tcp\","
            + "\"uid\":\"1\","
            + "\"name\":\"foo\","
            + "\"port\":\"8642\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, ServiceTcp.class),
        equalTo(new ServiceTcp("foo", "8642", Uid.of("1"))));
  }

  @Test
  public void testJavaSerialization() {
    ServiceTcp obj = new ServiceTcp("foo", "8642", Uid.of("1"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    ServiceTcp obj = new ServiceTcp("foo", "8642", Uid.of("1"));
    new EqualsTester()
        .addEqualityGroup(obj, new ServiceTcp("foo", "8642", Uid.of("1")))
        .addEqualityGroup(new ServiceTcp("foo0", "8642", Uid.of("1")))
        .addEqualityGroup(new ServiceTcp("foo", "9999", Uid.of("1")))
        .addEqualityGroup(new ServiceTcp("foo", "8642", Uid.of("10")))
        .testEquals();
  }
}
