package org.batfish.vendor.check_point_management;

import static org.batfish.vendor.check_point_management.RulebaseAction.NAME_ACCEPT;
import static org.batfish.vendor.check_point_management.RulebaseAction.NAME_DROP;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.check_point_management.RulebaseAction.Action;
import org.junit.Test;

/** Test of {@link RulebaseAction}. */
public final class RulebaseActionTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"RulebaseAction\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"comments\":\"bar\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, RulebaseAction.class),
        equalTo(new RulebaseAction("foo", Uid.of("0"), "bar")));
  }

  @Test
  public void testJavaSerialization() {
    RulebaseAction obj = new RulebaseAction("foo", Uid.of("0"), "some type that isn't handled yet");
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    RulebaseAction obj = new RulebaseAction("foo", Uid.of("1"), "comments");
    new EqualsTester()
        .addEqualityGroup(obj, new RulebaseAction("foo", Uid.of("1"), "comments"))
        .addEqualityGroup(new RulebaseAction("foo0", Uid.of("1"), "comments"))
        .addEqualityGroup(new RulebaseAction("foo", Uid.of("10"), "comments"))
        .addEqualityGroup(new RulebaseAction("foo", Uid.of("1"), "comments0"))
        .testEquals();
  }

  @Test
  public void testGetAction() {
    assertThat(
        new RulebaseAction(NAME_ACCEPT, Uid.of("0"), "").getAction(), equalTo(Action.ACCEPT));
    assertThat(new RulebaseAction(NAME_DROP, Uid.of("0"), "").getAction(), equalTo(Action.DROP));
    assertThat(
        new RulebaseAction("some unhandled action", Uid.of("0"), "").getAction(),
        equalTo(Action.UNHANDLED));
  }
}
