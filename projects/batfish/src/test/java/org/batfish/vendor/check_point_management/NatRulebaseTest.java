package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
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
            + "{" // object: CpmiAnyObject
            + "\"type\":\"CpmiAnyObject\","
            + "\"uid\":\"2\","
            + "\"name\":\"foo\""
            + "}," // object: CpmiAnyObject
            + "{" // object: Global
            + "\"type\":\"Global\","
            + "\"uid\":\"3\","
            + "\"name\":\"foo\""
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
            + "]" // object-dictionary
            + "}"; // rulebase
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, NatRulebase.class),
        equalTo(
            new NatRulebase(
                ImmutableMap.<Uid, AddressSpace>builder()
                    .put(
                        Uid.of("1"),
                        new AddressRange(
                            Ip.ZERO, Ip.parse("0.0.0.1"), null, null, "foo", Uid.of("1")))
                    .put(Uid.of("5"), new Host(Ip.ZERO, "foo", Uid.of("5")))
                    .put(Uid.of("6"), new Network("foo", Ip.ZERO, Ip.MAX, Uid.of("6")))
                    .build(),
                Uid.of("0"))));
  }

  @Test
  public void testSerialization() {
    NatRulebase obj =
        new NatRulebase(
            ImmutableMap.of(
                Uid.of("0"), new AddressRange(null, null, null, null, "foo", Uid.of("0"))),
            Uid.of("1"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    NatRulebase obj = new NatRulebase(ImmutableMap.of(), Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(obj, new NatRulebase(ImmutableMap.of(), Uid.of("0")))
        .addEqualityGroup(
            new NatRulebase(
                ImmutableMap.of(
                    Uid.of("1"), new AddressRange(null, null, null, null, "foo", Uid.of("1"))),
                Uid.of("0")))
        .addEqualityGroup(new NatRulebase(ImmutableMap.of(), Uid.of("2")))
        .testEquals();
  }
}
