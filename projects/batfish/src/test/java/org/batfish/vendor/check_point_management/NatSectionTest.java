package org.batfish.vendor.check_point_management;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

/** Test of {@link NatSection}. */
final class NatSectionTest {

  public static final NatSection TEST_INSTANCE =
      new NatSection("foo", ImmutableList.of(), Uid.of("0"));

  @Test
  void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"nat-section\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"rulebase\":[]" // rulebase
            + "}"; // NatSection
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, NatRuleOrSection.class),
        equalTo(TEST_INSTANCE));
  }

  @Test
  void testJavaSerialization() {
    NatSection obj =
        new NatSection("foo", ImmutableList.of(NatRuleTest.TEST_INSTANCE), Uid.of("0"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  void testEquals() {
    NatSection obj = new NatSection("foo", ImmutableList.of(), Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(obj, new NatSection("foo", ImmutableList.of(), Uid.of("0")))
        .addEqualityGroup(new NatSection("bar", ImmutableList.of(), Uid.of("0")))
        .addEqualityGroup(
            new NatSection("foo", ImmutableList.of(NatRuleTest.TEST_INSTANCE), Uid.of("0")))
        .addEqualityGroup(new NatSection("foo", ImmutableList.of(), Uid.of("1")))
        .testEquals();
  }
}
