package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link AddressRange}. */
public final class UnknownTypedManagementObjectTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"some type that isn't handled yet\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"a field that also isn't handled\":\"0.0.0.0\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, TypedManagementObject.class),
        equalTo(
            new UnknownTypedManagementObject(
                "foo", Uid.of("0"), "some type that isn't handled yet")));
  }

  @Test
  public void testJavaSerialization() {
    UnknownTypedManagementObject obj =
        new UnknownTypedManagementObject("foo", Uid.of("0"), "some type that isn't handled yet");
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    UnknownTypedManagementObject obj = new UnknownTypedManagementObject("foo", Uid.of("1"), "type");
    new EqualsTester()
        .addEqualityGroup(obj, new UnknownTypedManagementObject("foo", Uid.of("1"), "type"))
        .addEqualityGroup(new UnknownTypedManagementObject("foo0", Uid.of("1"), "type"))
        .addEqualityGroup(new UnknownTypedManagementObject("foo", Uid.of("10"), "type"))
        .addEqualityGroup(new UnknownTypedManagementObject("foo", Uid.of("1"), "type0"))
        .testEquals();
  }
}
