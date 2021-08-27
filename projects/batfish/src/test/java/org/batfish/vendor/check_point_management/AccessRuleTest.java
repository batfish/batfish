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

/** Test of {@link AccessRule}. */
public final class AccessRuleTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    {
      String input =
          "{"
              + "\"GARBAGE\":\"0\","
              + "\"type\":\"access-rule\","
              + "\"action\":\"1\","
              + "\"comments\":\"foo\","
              + "\"content\":[\"2\"],"
              + "\"content-direction\":\"any\","
              + "\"content-negate\":false,"
              + "\"destination\":[\"3\"],"
              + "\"destination-negate\":true,"
              + "\"enabled\":true,"
              + "\"install-on\":[\"4\"],"
              + "\"name\":\"bar\","
              + "\"rule-number\":5,"
              + "\"service\":[\"6\"],"
              + "\"service-negate\":false,"
              + "\"source\":[\"7\"],"
              + "\"source-negate\":false,"
              + "\"uid\":\"8\","
              + "\"vpn\":[\"9\"]"
              + "}";
      assertThat(
          BatfishObjectMapper.ignoreUnknownMapper().readValue(input, AccessRule.class),
          equalTo(
              new AccessRule(
                  Uid.of("1"),
                  "foo",
                  ImmutableList.of(Uid.of("2")),
                  "any",
                  false,
                  ImmutableList.of(Uid.of("3")),
                  true,
                  true,
                  ImmutableList.of(Uid.of("4")),
                  "bar",
                  5,
                  ImmutableList.of(Uid.of("6")),
                  false,
                  ImmutableList.of(Uid.of("7")),
                  false,
                  Uid.of("8"),
                  ImmutableList.of(Uid.of("9")))));
    }
  }

  @Test
  public void testJavaSerialization() {
    AccessRule obj =
        new AccessRule(
            Uid.of("1"),
            "foo",
            ImmutableList.of(Uid.of("2")),
            "any",
            false,
            ImmutableList.of(Uid.of("3")),
            true,
            true,
            ImmutableList.of(Uid.of("4")),
            "bar",
            5,
            ImmutableList.of(Uid.of("6")),
            false,
            ImmutableList.of(Uid.of("7")),
            false,
            Uid.of("8"),
            ImmutableList.of(Uid.of("9")));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    AccessRule obj =
        new AccessRule(
            Uid.of("1"),
            "foo",
            ImmutableList.of(Uid.of("2")),
            "any",
            false,
            ImmutableList.of(Uid.of("3")),
            true,
            true,
            ImmutableList.of(Uid.of("4")),
            "bar",
            5,
            ImmutableList.of(Uid.of("6")),
            false,
            ImmutableList.of(Uid.of("7")),
            false,
            Uid.of("8"),
            ImmutableList.of(Uid.of("9")));
    new EqualsTester()
        .addEqualityGroup(
            obj,
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("10"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo0",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("20")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any0",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                true,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("30")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                false,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                false,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("40")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar0",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                50,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("60")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                true,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("70")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                true,
                Uid.of("8"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("80"),
                ImmutableList.of(Uid.of("9"))))
        .addEqualityGroup(
            new AccessRule(
                Uid.of("1"),
                "foo",
                ImmutableList.of(Uid.of("2")),
                "any",
                false,
                ImmutableList.of(Uid.of("3")),
                true,
                true,
                ImmutableList.of(Uid.of("4")),
                "bar",
                5,
                ImmutableList.of(Uid.of("6")),
                false,
                ImmutableList.of(Uid.of("7")),
                false,
                Uid.of("8"),
                ImmutableList.of(Uid.of("90"))))
        .testEquals();
  }
}
