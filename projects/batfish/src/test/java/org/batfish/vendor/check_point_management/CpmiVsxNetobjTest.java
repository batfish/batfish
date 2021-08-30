package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

/** Test of {@link CpmiVsxNetobj}. */
public final class CpmiVsxNetobjTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"CpmiVsxNetobj\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"interfaces\": [],"
            + "\"ipv4-address\":\"0.0.0.0\","
            + "\"policy\":{"
            + "\"accessPolicyInstalled\": true,"
            + "\"accessPolicyName\": \"p1\","
            + "\"threatPolicyInstalled\": true,"
            + "\"threatPolicyName\": \"p2\""
            + "}" // policy
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, CpmiVsxNetobj.class),
        equalTo(
            new CpmiVsxNetobj(
                Ip.ZERO,
                "foo",
                ImmutableList.of(),
                new GatewayOrServerPolicy("p1", "p2"),
                Uid.of("0"))));
  }

  @Test
  public void testJavaSerialization() {
    CpmiVsxNetobj obj =
        new CpmiVsxNetobj(
            Ip.ZERO, "foo", ImmutableList.of(), GatewayOrServerPolicy.empty(), Uid.of("0"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    CpmiVsxNetobj obj =
        new CpmiVsxNetobj(
            Ip.ZERO, "foo", ImmutableList.of(), GatewayOrServerPolicy.empty(), Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(
            obj,
            new CpmiVsxNetobj(
                Ip.ZERO, "foo", ImmutableList.of(), GatewayOrServerPolicy.empty(), Uid.of("0")))
        .addEqualityGroup(
            new CpmiVsxNetobj(
                Ip.parse("0.0.0.1"),
                "foo",
                ImmutableList.of(),
                GatewayOrServerPolicy.empty(),
                Uid.of("0")))
        .addEqualityGroup(
            new CpmiVsxNetobj(
                Ip.ZERO, "bar", ImmutableList.of(), GatewayOrServerPolicy.empty(), Uid.of("0")))
        .addEqualityGroup(
            new CpmiVsxNetobj(
                Ip.ZERO,
                "foo",
                ImmutableList.of(new Interface("iface", new InterfaceTopology(true))),
                GatewayOrServerPolicy.empty(),
                Uid.of("0")))
        .addEqualityGroup(
            new CpmiVsxNetobj(
                Ip.ZERO,
                "foo",
                ImmutableList.of(),
                new GatewayOrServerPolicy("t1", null),
                Uid.of("0")))
        .addEqualityGroup(
            new CpmiVsxNetobj(
                Ip.ZERO, "foo", ImmutableList.of(), GatewayOrServerPolicy.empty(), Uid.of("1")))
        .testEquals();
  }
}
