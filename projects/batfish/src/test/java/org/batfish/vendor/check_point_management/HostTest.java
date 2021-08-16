package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link Host}. */
public final class HostTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"host\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"ipv4-address\":\"0.0.0.0\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, Host.class),
        equalTo(new Host(Ip.ZERO, "foo", Uid.of("0"))));
  }

  @Test
  public void testJavaSerialization() {
    Host obj = new Host(Ip.ZERO, "foo", Uid.of("0"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    Host obj = new Host(Ip.ZERO, "foo", Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(obj, new Host(Ip.ZERO, "foo", Uid.of("0")))
        .addEqualityGroup(new Host(Ip.MAX, "foo", Uid.of("0")))
        .addEqualityGroup(new Host(Ip.ZERO, "bar", Uid.of("0")))
        .addEqualityGroup(new Host(Ip.ZERO, "foo", Uid.of("1")))
        .testEquals();
  }
}
