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

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    {
      String input =
          "{"
              + "\"GARBAGE\":0,"
              + "\"type\":\"nat-rule\","
              + "\"uid\":\"0\","
              + "\"comments\":\"foo\","
              + "\"enabled\":true,"
              + "\"install-on\":\"All\","
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
          BatfishObjectMapper.ignoreUnknownMapper().readValue(input, NatRule.class),
          equalTo(
              new NatRule(
                  "foo",
                  true,
                  AllNatInstallTarget.instance(),
                  NatMethod.HIDE,
                  Uid.of("1"),
                  Uid.of("2"),
                  Uid.of("3"),
                  1,
                  Uid.of("4"),
                  Uid.of("5"),
                  Uid.of("6"),
                  Uid.of("0"))));
    }
    {
      String input =
          "{"
              + "\"GARBAGE\":0,"
              + "\"type\":\"nat-rule\","
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
          BatfishObjectMapper.ignoreUnknownMapper().readValue(input, NatRule.class),
          equalTo(
              new NatRule(
                  "foo",
                  true,
                  new ListNatInstallTarget(ImmutableList.of(Uid.of("100"))),
                  NatMethod.HIDE,
                  Uid.of("1"),
                  Uid.of("2"),
                  Uid.of("3"),
                  1,
                  Uid.of("4"),
                  Uid.of("5"),
                  Uid.of("6"),
                  Uid.of("0"))));
    }
  }

  @Test
  public void testJavaSerialization() {
    NatRule obj =
        new NatRule(
            "foo",
            true,
            AllNatInstallTarget.instance(),
            NatMethod.HIDE,
            Uid.of("1"),
            Uid.of("2"),
            Uid.of("3"),
            1,
            Uid.of("4"),
            Uid.of("5"),
            Uid.of("6"),
            Uid.of("0"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    NatRule obj =
        new NatRule(
            "foo",
            true,
            AllNatInstallTarget.instance(),
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
                "foo",
                true,
                AllNatInstallTarget.instance(),
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
                "bar",
                true,
                AllNatInstallTarget.instance(),
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
                "foo",
                false,
                AllNatInstallTarget.instance(),
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
                "foo",
                true,
                new ListNatInstallTarget(ImmutableList.of()),
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
                "foo",
                true,
                AllNatInstallTarget.instance(),
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
                "foo",
                true,
                AllNatInstallTarget.instance(),
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
                "foo",
                true,
                AllNatInstallTarget.instance(),
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
                "foo",
                true,
                AllNatInstallTarget.instance(),
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
                "foo",
                true,
                AllNatInstallTarget.instance(),
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
                "foo",
                true,
                AllNatInstallTarget.instance(),
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
                "foo",
                true,
                AllNatInstallTarget.instance(),
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
                "foo",
                true,
                AllNatInstallTarget.instance(),
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
                "foo",
                true,
                AllNatInstallTarget.instance(),
                NatMethod.HIDE,
                Uid.of("1"),
                Uid.of("2"),
                Uid.of("3"),
                1,
                Uid.of("4"),
                Uid.of("5"),
                Uid.of("6"),
                Uid.of("10")))
        .testEquals();
  }
}
