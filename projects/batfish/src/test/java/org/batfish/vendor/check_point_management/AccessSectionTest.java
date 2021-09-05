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

/** Test of {@link AccessSection}. */
public final class AccessSectionTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
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
            + "]" // rulebase
            + "}"; // AccessSection
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, AccessSection.class),
        equalTo(
            new AccessSection(
                "foo",
                ImmutableList.of(
                    AccessRule.testBuilder()
                        .setAction(Uid.of("1"))
                        .setComments("foo")
                        .setContent(ImmutableList.of(Uid.of("2")))
                        .setContentDirection("any")
                        .setDestination(ImmutableList.of(Uid.of("3")))
                        .setDestinationNegate(true)
                        .setInstallOn(ImmutableList.of(Uid.of("4")))
                        .setName("bar")
                        .setRuleNumber(5)
                        .setService(ImmutableList.of(Uid.of("6")))
                        .setSource(ImmutableList.of(Uid.of("7")))
                        .setUid(Uid.of("8"))
                        .setVpn(ImmutableList.of(Uid.of("9")))
                        .build()),
                Uid.of("0"))));
  }

  @Test
  public void testJacksonDeserialization_noName() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"access-section\","
            + "\"uid\":\"0\","
            + "\"rulebase\":[]"
            + "}"; // AccessSection
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, AccessSection.class),
        equalTo(new AccessSection("Section 0", ImmutableList.of(), Uid.of("0"))));
  }

  @Test
  public void testJavaSerialization() {
    AccessSection obj = new AccessSection("foo", ImmutableList.of(), Uid.of("0"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    AccessSection obj = new AccessSection("foo", ImmutableList.of(), Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(obj, new AccessSection("foo", ImmutableList.of(), Uid.of("0")))
        .addEqualityGroup(new AccessSection("bar", ImmutableList.of(), Uid.of("0")))
        .addEqualityGroup(
            new AccessSection(
                "foo",
                ImmutableList.of(
                    AccessRule.testBuilder()
                        .setAction(Uid.of("1"))
                        .setComments("foo")
                        .setContent(ImmutableList.of(Uid.of("2")))
                        .setContentDirection("any")
                        .setDestination(ImmutableList.of(Uid.of("3")))
                        .setDestinationNegate(true)
                        .setInstallOn(ImmutableList.of(Uid.of("4")))
                        .setName("bar")
                        .setRuleNumber(5)
                        .setService(ImmutableList.of(Uid.of("6")))
                        .setSource(ImmutableList.of(Uid.of("7")))
                        .setUid(Uid.of("8"))
                        .setVpn(ImmutableList.of(Uid.of("9")))
                        .build()),
                Uid.of("0")))
        .addEqualityGroup(new AccessSection("foo", ImmutableList.of(), Uid.of("1")))
        .testEquals();
  }
}
