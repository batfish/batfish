package org.batfish.vendor.check_point_management;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link Domain}. */
final class DomainTest {

  @Test
  void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"domain-type\":\"domain\","
            + "\"uid\":\"1\","
            + "\"name\":\"bar\""
            + "}"; // Domain

    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, Domain.class),
        equalTo(new Domain("bar", Uid.of("1"))));
  }

  @Test
  void testJavaSerialization() {
    Domain obj = new Domain("bar", Uid.of("1"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  void testEquals() {
    Domain obj = new Domain("bar", Uid.of("1"));
    new EqualsTester()
        .addEqualityGroup(obj, new Domain("bar", Uid.of("1")))
        .addEqualityGroup(new Domain("foo", Uid.of("1")))
        .addEqualityGroup(new Domain("bar", Uid.of("2")))
        .testEquals();
  }
}
