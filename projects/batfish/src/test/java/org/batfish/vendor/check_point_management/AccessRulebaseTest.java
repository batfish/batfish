package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link AccessRulebase}. */
public final class AccessRulebaseTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"uid\":\"0\","
            + "\"name\":\"baz\","
            + "\"objects-dictionary\":[]," // object-dictionary
            + "\"rulebase\":["
            + "{" // access-rule
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
            + "\"type\":\"access-rule\","
            + "\"uid\":\"8\","
            + "\"vpn\":[\"9\"]"
            + "}," // access-rule
            + "{" // access-section
            + "\"type\":\"access-section\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"rulebase\":["
            + "{" // access-rule
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
            + "}" // access-rule
            + "]" // rulebase in access-section
            + "}" // access-section
            + "]" // rulebase
            + "}"; // AccessRulebase
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, AccessRulebase.class),
        equalTo(
            new AccessRulebase(
                ImmutableMap.of(),
                ImmutableList.of(
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
                        ImmutableList.of(Uid.of("9"))),
                    new AccessSection(
                        "foo",
                        ImmutableList.of(
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
                                ImmutableList.of(Uid.of("9")))),
                        Uid.of("0"))),
                Uid.of("0"),
                "baz")));
  }

  @Test
  public void testSerialization() {
    AccessRulebase obj =
        new AccessRulebase(
            ImmutableMap.of(
                Uid.of("0"), new AddressRange(null, null, null, null, "foo", Uid.of("0"))),
            ImmutableList.of(),
            Uid.of("1"),
            "foo");
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    AccessRulebase obj =
        new AccessRulebase(ImmutableMap.of(), ImmutableList.of(), Uid.of("0"), "foo");
    new EqualsTester()
        .addEqualityGroup(
            obj, new AccessRulebase(ImmutableMap.of(), ImmutableList.of(), Uid.of("0"), "foo"))
        .addEqualityGroup(
            new AccessRulebase(
                ImmutableMap.of(
                    Uid.of("1"), new AddressRange(null, null, null, null, "foo", Uid.of("1"))),
                ImmutableList.of(),
                Uid.of("0"),
                "foo"))
        .addEqualityGroup(
            new AccessRulebase(
                ImmutableMap.of(),
                ImmutableList.of(new AccessSection("n", ImmutableList.of(), Uid.of("1"))),
                Uid.of("0"),
                "foo"))
        .addEqualityGroup(
            new AccessRulebase(ImmutableMap.of(), ImmutableList.of(), Uid.of("2"), "foo"))
        .addEqualityGroup(
            new AccessRulebase(ImmutableMap.of(), ImmutableList.of(), Uid.of("2"), "bar"))
        .testEquals();
  }
}
