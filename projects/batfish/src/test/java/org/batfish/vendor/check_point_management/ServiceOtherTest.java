package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link ServiceOther}. */
public final class ServiceOtherTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"service-other\","
            + "\"uid\":\"1\","
            + "\"name\":\"foo\","
            + "\"ip-protocol\":86"
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, TypedManagementObject.class),
        equalTo(new ServiceOther("foo", 86, Uid.of("1"))));
  }

  @Test
  public void testJavaSerialization() {
    ServiceOther obj = new ServiceOther("foo", 86, Uid.of("1"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    ServiceOther obj = new ServiceOther("foo", 86, Uid.of("1"));
    new EqualsTester()
        .addEqualityGroup(obj, new ServiceOther("foo", 86, Uid.of("1")))
        .addEqualityGroup(new ServiceOther("bar", 86, Uid.of("1")))
        .addEqualityGroup(new ServiceOther("bar", 87, Uid.of("1")))
        .addEqualityGroup(new ServiceOther("bar", 87, Uid.of("2")))
        .testEquals();
  }
}
