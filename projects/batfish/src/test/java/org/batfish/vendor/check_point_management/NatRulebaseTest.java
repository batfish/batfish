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

/** Test of {@link NatRulebase}. */
public final class NatRulebaseTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"uid\":\"0\","
            + "\"objects-dictionary\":[],"
            + "\"rulebase\":[]" // rulebase
            + "}"; // NatRulebase
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, NatRulebase.class),
        equalTo(new NatRulebase(ImmutableMap.of(), ImmutableList.of(), Uid.of("0"))));
  }

  @Test
  public void testSerialization() {
    NatRulebase obj =
        new NatRulebase(
            ImmutableMap.of(Uid.of("0"), AddressRangeTest.TEST_INSTANCE),
            ImmutableList.of(NatRuleTest.TEST_INSTANCE, NatSectionTest.TEST_INSTANCE),
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
                ImmutableMap.of(Uid.of("1"), AddressRangeTest.TEST_INSTANCE),
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
