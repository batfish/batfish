package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link Original}. */
public final class OriginalTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"Global\","
            + "\"uid\":\"0\","
            + "\"name\":\"Original\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, TypedManagementObject.class),
        equalTo(new Original(Uid.of("0"))));
  }

  @Test
  public void testJavaSerialization() {
    Original obj = new Original(Uid.of("1"));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testEquals() {
    Original obj = new Original(Uid.of("1"));
    new EqualsTester()
        .addEqualityGroup(obj, new Original(Uid.of("1")))
        .addEqualityGroup(new Original(Uid.of("2")))
        .testEquals();
  }
}
