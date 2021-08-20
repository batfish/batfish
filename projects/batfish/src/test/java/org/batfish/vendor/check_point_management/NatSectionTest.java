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

/** Test of {@link NatSection}. */
public final class NatSectionTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"nat-section\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"rulebase\":["
            + "{" // nat-rule
            + "\"type\":\"nat-rule\","
            + "\"uid\":\"1\","
            + "\"comments\":\"a\","
            + "\"enabled\":true,"
            + "\"install-on\":\"All\","
            + "\"method\":\"hide\","
            + "\"original-destination\":\"0\","
            + "\"original-service\":\"0\","
            + "\"original-source\":\"0\","
            + "\"rule-number\":1,"
            + "\"translated-destination\":\"0\","
            + "\"translated-service\":\"0\","
            + "\"translated-source\":\"0\""
            + "}" // nat-rule
            + "]" // rulebase
            + "}"; // NatSection
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, NatSection.class),
        equalTo(
            new NatSection(
                "foo",
                ImmutableList.of(
                    new NatRule(
                        "a",
                        true,
                        AllNatInstallTarget.instance(),
                        NatMethod.HIDE,
                        Uid.of("0"),
                        Uid.of("0"),
                        Uid.of("0"),
                        1,
                        Uid.of("0"),
                        Uid.of("0"),
                        Uid.of("0"),
                        Uid.of("1"))),
                Uid.of("0"))));
  }

  @Test
  public void testJavaSerialization() {
    NatSection obj = new NatSection("foo", ImmutableList.of(), Uid.of("0"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    NatSection obj = new NatSection("foo", ImmutableList.of(), Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(obj, new NatSection("foo", ImmutableList.of(), Uid.of("0")))
        .addEqualityGroup(new NatSection("bar", ImmutableList.of(), Uid.of("0")))
        .addEqualityGroup(
            new NatSection(
                "foo",
                ImmutableList.of(
                    new NatRule(
                        "a",
                        true,
                        AllNatInstallTarget.instance(),
                        NatMethod.HIDE,
                        Uid.of("0"),
                        Uid.of("0"),
                        Uid.of("0"),
                        1,
                        Uid.of("0"),
                        Uid.of("0"),
                        Uid.of("0"),
                        Uid.of("1"))),
                Uid.of("0")))
        .addEqualityGroup(new NatSection("foo", ImmutableList.of(), Uid.of("1")))
        .testEquals();
  }
}
