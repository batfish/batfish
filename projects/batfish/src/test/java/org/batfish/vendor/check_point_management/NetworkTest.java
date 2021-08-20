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

/** Test of {@link Network}. */
public final class NetworkTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"network\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"subnet4\":\"0.0.0.0\","
            + "\"subnet-mask\":\"255.255.255.255\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, Network.class),
        equalTo(new Network("foo", Ip.ZERO, Ip.MAX, Uid.of("0"))));
  }

  @Test
  public void testJavaSerialization() {
    Network obj = new Network("foo", Ip.ZERO, Ip.MAX, Uid.of("0"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    Network obj = new Network("foo", Ip.ZERO, Ip.MAX, Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(obj, new Network("foo", Ip.ZERO, Ip.MAX, Uid.of("0")))
        .addEqualityGroup(new Network("bar", Ip.ZERO, Ip.MAX, Uid.of("0")))
        .addEqualityGroup(new Network("foo", Ip.MAX, Ip.MAX, Uid.of("0")))
        .addEqualityGroup(new Network("foo", Ip.ZERO, Ip.ZERO, Uid.of("0")))
        .addEqualityGroup(new Network("foo", Ip.ZERO, Ip.MAX, Uid.of("1")))
        .testEquals();
  }
}
