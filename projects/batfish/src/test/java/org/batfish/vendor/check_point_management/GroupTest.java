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

/** Test of {@link Group}. */
public final class GroupTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"group\","
            + "\"uid\":\"1\","
            + "\"name\":\"foo\","
            + "\"members\":[\"2\", \"42\"]"
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, Group.class),
        equalTo(new Group("foo", ImmutableList.of(Uid.of("2"), Uid.of("42")), Uid.of("1"))));
  }

  @Test
  public void testJavaSerialization() {
    Group obj = new Group("foo", ImmutableList.of(Uid.of("2"), Uid.of("42")), Uid.of("1"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    Group obj = new Group("foo", ImmutableList.of(), Uid.of("1"));
    new EqualsTester()
        .addEqualityGroup(obj, new Group("foo", ImmutableList.of(), Uid.of("1")))
        .addEqualityGroup(new Group("foo0", ImmutableList.of(), Uid.of("1")))
        .addEqualityGroup(new Group("foo", ImmutableList.of(Uid.of("2")), Uid.of("1")))
        .addEqualityGroup(new Group("foo", ImmutableList.of(), Uid.of("10")))
        .testEquals();
  }
}
