package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link PolicyTargets}. */
public final class PolicyTargetsTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"Global\","
            + "\"uid\":\"0\","
            + "\"name\":\"Policy Targets\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, Global.class),
        equalTo(new PolicyTargets(Uid.of("0"))));
  }

  @Test
  public void testJavaSerialization() {
    PolicyTargets obj = new PolicyTargets(Uid.of("1"));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    PolicyTargets obj = new PolicyTargets(Uid.of("1"));
    new EqualsTester()
        .addEqualityGroup(obj, new PolicyTargets(Uid.of("1")))
        .addEqualityGroup(new PolicyTargets(Uid.of("2")))
        .testEquals();
  }
}
