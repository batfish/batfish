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

/** Test of {@link AccessRulebase}. */
public final class AccessRulebaseTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"uid\":\"0\","
            + "\"name\":\"baz\","
            + "\"objects-dictionary\":["
            + "{" // object: address-range
            + "\"type\":\"address-range\","
            + "\"uid\":\"1\","
            + "\"name\":\"foo\","
            + "\"ipv4-address-first\":\"0.0.0.0\","
            + "\"ipv4-address-last\":\"0.0.0.1\""
            + "}," // object: address-range
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
            + "{" // access-section TODO
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
                ImmutableMap.<Uid, TypedManagementObject>builder()
                    .put(
                        Uid.of("1"),
                        new AddressRange(
                            Ip.ZERO, Ip.parse("0.0.0.1"), null, null, "foo", Uid.of("1")))
                    .put(Uid.of("2"), new CpmiAnyObject(Uid.of("2")))
                    .put(Uid.of("3"), new Original(Uid.of("3")))
                    .put(Uid.of("4"), new Group("foo", Uid.of("4")))
                    .put(Uid.of("5"), new Host(Ip.ZERO, "foo", Uid.of("5")))
                    .put(Uid.of("6"), new Network("foo", Ip.ZERO, Ip.MAX, Uid.of("6")))
                    .put(Uid.of("7"), new ServiceGroup("foo", Uid.of("7")))
                    .put(Uid.of("8"), new ServiceTcp("foo", Uid.of("8")))
                    .build(),
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
