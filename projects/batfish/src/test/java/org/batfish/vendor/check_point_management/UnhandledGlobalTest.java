package org.batfish.vendor.check_point_management;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link UnhandledGlobal}. */
public final class UnhandledGlobalTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"Global\","
            + "\"uid\":\"0\","
            + "\"name\":\"what\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, TypedManagementObject.class),
        equalTo(new UnhandledGlobal("what", Uid.of("0"))));
  }

  @Test
  public void testJavaSerialization() {
    UnhandledGlobal obj = new UnhandledGlobal("what", Uid.of("1"));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    UnhandledGlobal obj = new UnhandledGlobal("what", Uid.of("1"));
    new EqualsTester()
        .addEqualityGroup(obj, new UnhandledGlobal("what", Uid.of("1")))
        .addEqualityGroup(new UnhandledGlobal("where", Uid.of("1")))
        .addEqualityGroup(new UnhandledGlobal("what", Uid.of("2")))
        .testEquals();
  }
}
