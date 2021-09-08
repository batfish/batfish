package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link ServiceGroup}. */
public final class ServiceGroupTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"service-group\","
            + "\"uid\":\"1\","
            + "\"name\":\"foo\","
            + "\"members\":[\"2\"]"
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, ServiceGroup.class),
        equalTo(new ServiceGroup("foo", ImmutableList.of(Uid.of("2")), Uid.of("1"))));
  }

  @Test
  public void testJavaSerialization() {
    ServiceGroup obj = new ServiceGroup("foo", ImmutableList.of(Uid.of("2")), Uid.of("1"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    ServiceGroup obj = new ServiceGroup("foo", ImmutableList.of(), Uid.of("1"));
    new EqualsTester()
        .addEqualityGroup(obj, new ServiceGroup("foo", ImmutableList.of(), Uid.of("1")))
        .addEqualityGroup(new ServiceGroup("foo0", ImmutableList.of(), Uid.of("1")))
        .addEqualityGroup(new ServiceGroup("foo", ImmutableList.of(Uid.of("2")), Uid.of("1")))
        .addEqualityGroup(new ServiceGroup("foo", ImmutableList.of(), Uid.of("10")))
        .testEquals();
  }
}
