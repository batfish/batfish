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

/** Test of {@link NatRule}. */
public final class NatRuleTest {

  public static final NatRule TEST_INSTANCE =
      new NatRule(
          true,
          "foo",
          true,
          ImmutableList.of(Uid.of("100")),
          NatMethod.HIDE,
          Uid.of("1"),
          Uid.of("2"),
          Uid.of("3"),
          1,
          Uid.of("4"),
          Uid.of("5"),
          Uid.of("6"),
          Uid.of("0"));

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    {
      String input =
          "{"
              + "\"GARBAGE\":0,"
              + "\"type\":\"nat-rule\","
              + "\"auto-generated\":true,"
              + "\"uid\":\"0\","
              + "\"comments\":\"foo\","
              + "\"enabled\":true,"
              + "\"install-on\":[\"100\"],"
              + "\"method\":\"hide\","
              + "\"original-destination\":\"1\","
              + "\"original-service\":\"2\","
              + "\"original-source\":\"3\","
              + "\"rule-number\":1,"
              + "\"translated-destination\":\"4\","
              + "\"translated-service\":\"5\","
              + "\"translated-source\":\"6\""
              + "}";
      assertThat(
          BatfishObjectMapper.ignoreUnknownMapper().readValue(input, NatRuleOrSection.class),
          equalTo(TEST_INSTANCE));
    }
  }

  @Test
  public void testJavaSerialization() {
    assertEquals(TEST_INSTANCE, SerializationUtils.clone(TEST_INSTANCE));
  }

  @Test
  public void testEquals() {
    NatRule obj =
        new NatRule(
            true,
            "foo",
            true,
            ImmutableList.of(),
            NatMethod.HIDE,
            Uid.of("1"),
            Uid.of("2"),
            Uid.of("3"),
            1,
            Uid.of("4"),
            Uid.of("5"),
            Uid.of("6"),
            Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(
            obj,
            new NatRule(
                true,
                "foo",
                true,
                ImmutableList.of(),
                NatMethod.HIDE,
                Uid.of("1"),
                Uid.of("2"),
                Uid.of("3"),
                1,
                Uid.of("4"),
                Uid.of("5"),
                Uid.of("6"),
                Uid.of("0")))
        .addEqualityGroup(
            new NatRule(
                true,
                "bar",
                true,
                ImmutableList.of(),
                NatMethod.HIDE,
                Uid.of("1"),
                Uid.of("2"),
                Uid.of("3"),
                1,
                Uid.of("4"),
                Uid.of("5"),
                Uid.of("6"),
                Uid.of("0")))
        .addEqualityGroup(
            new NatRule(
                true,
                "foo",
                false,
                ImmutableList.of(),
                NatMethod.HIDE,
                Uid.of("1"),
                Uid.of("2"),
                Uid.of("3"),
                1,
                Uid.of("4"),
                Uid.of("5"),
                Uid.of("6"),
                Uid.of("0")))
        .addEqualityGroup(
            new NatRule(
                true,
                "foo",
                true,
                ImmutableList.of(),
                NatMethod.STATIC,
                Uid.of("1"),
                Uid.of("2"),
                Uid.of("3"),
                1,
                Uid.of("4"),
                Uid.of("5"),
                Uid.of("6"),
                Uid.of("0")))
        .addEqualityGroup(
            new NatRule(
                true,
                "foo",
                true,
                ImmutableList.of(),
                NatMethod.HIDE,
                Uid.of("11"),
                Uid.of("2"),
                Uid.of("3"),
                1,
                Uid.of("4"),
                Uid.of("5"),
                Uid.of("6"),
                Uid.of("0")))
        .addEqualityGroup(
            new NatRule(
                true,
                "foo",
                true,
                ImmutableList.of(),
                NatMethod.HIDE,
                Uid.of("1"),
                Uid.of("12"),
                Uid.of("3"),
                1,
                Uid.of("4"),
                Uid.of("5"),
                Uid.of("6"),
                Uid.of("0")))
        .addEqualityGroup(
            new NatRule(
                true,
                "foo",
                true,
                ImmutableList.of(),
                NatMethod.HIDE,
                Uid.of("1"),
                Uid.of("2"),
                Uid.of("13"),
                1,
                Uid.of("4"),
                Uid.of("5"),
                Uid.of("6"),
                Uid.of("0")))
        .addEqualityGroup(
            new NatRule(
                true,
                "foo",
                true,
                ImmutableList.of(),
                NatMethod.HIDE,
                Uid.of("1"),
                Uid.of("2"),
                Uid.of("3"),
                11,
                Uid.of("4"),
                Uid.of("5"),
                Uid.of("6"),
                Uid.of("0")))
        .addEqualityGroup(
            new NatRule(
                true,
                "foo",
                true,
                ImmutableList.of(),
                NatMethod.HIDE,
                Uid.of("1"),
                Uid.of("2"),
                Uid.of("3"),
                1,
                Uid.of("14"),
                Uid.of("5"),
                Uid.of("6"),
                Uid.of("0")))
        .addEqualityGroup(
            new NatRule(
                true,
                "foo",
                true,
                ImmutableList.of(),
                NatMethod.HIDE,
                Uid.of("1"),
                Uid.of("2"),
                Uid.of("3"),
                1,
                Uid.of("4"),
                Uid.of("15"),
                Uid.of("6"),
                Uid.of("0")))
        .addEqualityGroup(
            new NatRule(
                true,
                "foo",
                true,
                ImmutableList.of(),
                NatMethod.HIDE,
                Uid.of("1"),
                Uid.of("2"),
                Uid.of("3"),
                1,
                Uid.of("4"),
                Uid.of("5"),
                Uid.of("16"),
                Uid.of("0")))
        .addEqualityGroup(
            new NatRule(
                true,
                "foo",
                true,
                ImmutableList.of(),
                NatMethod.HIDE,
                Uid.of("1"),
                Uid.of("2"),
                Uid.of("3"),
                1,
                Uid.of("4"),
                Uid.of("5"),
                Uid.of("6"),
                Uid.of("10")))
        .addEqualityGroup(
            new NatRule(
                true,
                "foo",
                true,
                ImmutableList.of(Uid.of("10")),
                NatMethod.HIDE,
                Uid.of("1"),
                Uid.of("2"),
                Uid.of("3"),
                1,
                Uid.of("4"),
                Uid.of("5"),
                Uid.of("6"),
                Uid.of("0")))
        .addEqualityGroup(
            new NatRule(
                false,
                "foo",
                true,
                ImmutableList.of(),
                NatMethod.HIDE,
                Uid.of("1"),
                Uid.of("2"),
                Uid.of("3"),
                1,
                Uid.of("4"),
                Uid.of("5"),
                Uid.of("6"),
                Uid.of("0")))
        .testEquals();
  }
}
