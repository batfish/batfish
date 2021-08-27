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
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link NatRulebase}. */
public final class NatRulebaseTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"uid\":\"0\","
            + "\"objects-dictionary\":["
            + "{" // object: address-range
            + "\"type\":\"address-range\","
            + "\"uid\":\"1\","
            + "\"name\":\"foo\","
            + "\"ipv4-address-first\":\"0.0.0.0\","
            + "\"ipv4-address-last\":\"0.0.0.1\""
            + "}," // object: address-range
            + "{" // object: unknown
            + "\"type\":\"unknown\","
            + "\"uid\":\"100\","
            + "\"name\":\"unknown-foo\""
            + "}," // object: unknown
            + "{" // object: CpmiAnyObject
            + "\"type\":\"CpmiAnyObject\","
            + "\"uid\":\"2\","
            + "\"name\":\"Any\""
            + "}," // object: CpmiAnyObject
            + "{" // object: Global
            + "\"type\":\"Global\","
            + "\"uid\":\"3\","
            + "\"name\":\"Original\""
            + "}," // object: Global
            + "{" // object: group
            + "\"type\":\"group\","
            + "\"uid\":\"4\","
            + "\"name\":\"foo\""
            + "}," // object: group
            + "{" // object: host
            + "\"type\":\"host\","
            + "\"uid\":\"5\","
            + "\"name\":\"foo\","
            + "\"ipv4-address\":\"0.0.0.0\""
            + "}," // object: host
            + "{" // object: network
            + "\"type\":\"network\","
            + "\"uid\":\"6\","
            + "\"name\":\"foo\","
            + "\"subnet4\":\"0.0.0.0\","
            + "\"subnet-mask\":\"255.255.255.255\""
            + "}," // object: network
            + "{" // object: service-group
            + "\"type\":\"service-group\","
            + "\"uid\":\"7\","
            + "\"name\":\"foo\""
            + "}," // object: service-group
            + "{" // object: service-tcp
            + "\"type\":\"service-tcp\","
            + "\"uid\":\"8\","
            + "\"name\":\"foo\""
            + "}" // object: service-tcp
            + "]," // object-dictionary
            + "\"rulebase\":["
            + "{" // nat-rule
            + "\"type\":\"nat-rule\","
            + "\"uid\":\"9\","
            + "\"comments\":\"a\","
            + "\"enabled\":true,"
            + "\"install-on\": [\"100\"],"
            + "\"method\":\"hide\","
            + "\"original-destination\":\"0\","
            + "\"original-service\":\"0\","
            + "\"original-source\":\"0\","
            + "\"rule-number\":1,"
            + "\"translated-destination\":\"0\","
            + "\"translated-service\":\"0\","
            + "\"translated-source\":\"0\""
            + "}," // nat-rule
            + "{" // nat-section
            + "\"type\":\"nat-section\","
            + "\"uid\":\"10\","
            + "\"name\":\"n\","
            + "\"rulebase\":[" // in nat-section
            + "{" // nat-rule in nat-section
            + "\"type\":\"nat-rule\","
            + "\"uid\":\"11\","
            + "\"comments\":\"a\","
            + "\"enabled\":true,"
            + "\"install-on\": [\"100\"],"
            + "\"method\":\"hide\","
            + "\"original-destination\":\"0\","
            + "\"original-service\":\"0\","
            + "\"original-source\":\"0\","
            + "\"rule-number\":2,"
            + "\"translated-destination\":\"0\","
            + "\"translated-service\":\"0\","
            + "\"translated-source\":\"0\""
            + "}" // nat-rule in nat-section
            + "]" // rulebase in nat-section
            + "}" // nat-section
            + "]" // rulebase
            + "}"; // NatRulebase
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, NatRulebase.class),
        equalTo(
            new NatRulebase(
                ImmutableMap.<Uid, TypedManagementObject>builder()
                    .put(
                        Uid.of("1"),
                        new AddressRange(
                            Ip.ZERO, Ip.parse("0.0.0.1"), null, null, "foo", Uid.of("1")))
                    .put(
                        Uid.of("100"),
                        new UnknownTypedManagementObject("unknown-foo", Uid.of("100"), "unknown"))
                    .put(Uid.of("2"), new CpmiAnyObject(Uid.of("2")))
                    .put(Uid.of("3"), new Original(Uid.of("3")))
                    .put(Uid.of("4"), new Group("foo", Uid.of("4")))
                    .put(Uid.of("5"), new Host(Ip.ZERO, "foo", Uid.of("5")))
                    .put(Uid.of("6"), new Network("foo", Ip.ZERO, Ip.MAX, Uid.of("6")))
                    .put(Uid.of("7"), new ServiceGroup("foo", Uid.of("7")))
                    .put(Uid.of("8"), new ServiceTcp("foo", Uid.of("8")))
                    .build(),
                ImmutableList.of(
                    new NatRule(
                        "a",
                        true,
                        ImmutableList.of(Uid.of("100")),
                        NatMethod.HIDE,
                        Uid.of("0"),
                        Uid.of("0"),
                        Uid.of("0"),
                        1,
                        Uid.of("0"),
                        Uid.of("0"),
                        Uid.of("0"),
                        Uid.of("9")),
                    new NatSection(
                        "n",
                        ImmutableList.of(
                            new NatRule(
                                "a",
                                true,
                                ImmutableList.of(Uid.of("100")),
                                NatMethod.HIDE,
                                Uid.of("0"),
                                Uid.of("0"),
                                Uid.of("0"),
                                2,
                                Uid.of("0"),
                                Uid.of("0"),
                                Uid.of("0"),
                                Uid.of("11"))),
                        Uid.of("10"))),
                Uid.of("0"))));
  }

  @Test
  public void testSerialization() {
    NatRulebase obj =
        new NatRulebase(
            ImmutableMap.of(
                Uid.of("0"), new AddressRange(null, null, null, null, "foo", Uid.of("0"))),
            ImmutableList.of(),
            Uid.of("1"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    NatRulebase obj = new NatRulebase(ImmutableMap.of(), ImmutableList.of(), Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(obj, new NatRulebase(ImmutableMap.of(), ImmutableList.of(), Uid.of("0")))
        .addEqualityGroup(
            new NatRulebase(
                ImmutableMap.of(
                    Uid.of("1"), new AddressRange(null, null, null, null, "foo", Uid.of("1"))),
                ImmutableList.of(),
                Uid.of("0")))
        .addEqualityGroup(
            new NatRulebase(
                ImmutableMap.of(),
                ImmutableList.of(new NatSection("n", ImmutableList.of(), Uid.of("1"))),
                Uid.of("0")))
        .addEqualityGroup(new NatRulebase(ImmutableMap.of(), ImmutableList.of(), Uid.of("2")))
        .testEquals();
  }
}
