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

/** Test of {@link CpmiVsNetobj}. */
public final class CpmiVsNetobjTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"CpmiVsNetobj\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"ipv4-address\":\"0.0.0.0\","
            + "\"policy\":{"
            + "\"accessPolicyInstalled\": true,"
            + "\"accessPolicyName\": \"p1\","
            + "\"threatPolicyInstalled\": true,"
            + "\"threatPolicyName\": \"p2\""
            + "}" // policy
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, CpmiVsNetobj.class),
        equalTo(
            new CpmiVsNetobj(Ip.ZERO, "foo", new GatewayOrServerPolicy("p1", "p2"), Uid.of("0"))));
  }

  @Test
  public void testJavaSerialization() {
    CpmiVsNetobj obj = new CpmiVsNetobj(Ip.ZERO, "foo", GatewayOrServerPolicy.empty(), Uid.of("0"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    CpmiVsNetobj obj = new CpmiVsNetobj(Ip.ZERO, "foo", GatewayOrServerPolicy.empty(), Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(
            obj, new CpmiVsNetobj(Ip.ZERO, "foo", GatewayOrServerPolicy.empty(), Uid.of("0")))
        .addEqualityGroup(
            new CpmiVsNetobj(
                Ip.parse("0.0.0.1"), "foo", GatewayOrServerPolicy.empty(), Uid.of("0")))
        .addEqualityGroup(
            new CpmiVsNetobj(Ip.ZERO, "bar", GatewayOrServerPolicy.empty(), Uid.of("0")))
        .addEqualityGroup(
            new CpmiVsNetobj(Ip.ZERO, "foo", new GatewayOrServerPolicy("t1", null), Uid.of("0")))
        .addEqualityGroup(
            new CpmiVsNetobj(Ip.ZERO, "foo", GatewayOrServerPolicy.empty(), Uid.of("1")))
        .testEquals();
  }
}
