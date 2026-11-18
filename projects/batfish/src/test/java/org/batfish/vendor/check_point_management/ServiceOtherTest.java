package org.batfish.vendor.check_point_management;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.check_point_management.parsing.serviceother.UnhandledAstNode;
import org.junit.jupiter.api.Test;

/** Test of {@link ServiceOther}. */
final class ServiceOtherTest {

  @Test
  void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"service-other\","
            + "\"uid\":\"1\","
            + "\"name\":\"foo\","
            + "\"ip-protocol\":17,"
            + "\"match\":\"uh_dport > 33000\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, TypedManagementObject.class),
        equalTo(ServiceOther.of("foo", 17, "uh_dport > 33000", Uid.of("1"))));
  }

  @Test
  void testJavaSerialization() {
    ServiceOther obj = ServiceOther.of("foo", 86, "bar", Uid.of("1"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  void testMatchParsing() {
    ServiceOther obj = ServiceOther.of("foo", 86, "bar", Uid.of("1"));
    assertEquals(obj.getMatchAst(), UnhandledAstNode.of("bar"));
  }

  @Test
  void testEquals() {
    ServiceOther obj = ServiceOther.of("foo", 86, null, Uid.of("1"));
    new EqualsTester()
        .addEqualityGroup(obj, ServiceOther.of("foo", 86, null, Uid.of("1")))
        .addEqualityGroup(ServiceOther.of("bar", 86, null, Uid.of("1")))
        .addEqualityGroup(ServiceOther.of("bar", 87, null, Uid.of("1")))
        .addEqualityGroup(ServiceOther.of("bar", 87, "foo", Uid.of("1")))
        .addEqualityGroup(ServiceOther.of("bar", 87, "foo", Uid.of("2")))
        .testEquals();
  }
}
