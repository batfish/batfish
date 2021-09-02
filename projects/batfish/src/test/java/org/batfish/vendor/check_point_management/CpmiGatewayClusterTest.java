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

/** Test of {@link CpmiGatewayCluster}. */
public final class CpmiGatewayClusterTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"type\":\"CpmiGatewayCluster\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"ipv4-address\":\"0.0.0.0\","
            + "\"interfaces\": [],"
            + "\"cluster-member-names\":[\"m1\"],"
            + "\"policy\":{"
            + "\"accessPolicyInstalled\": true,"
            + "\"accessPolicyName\": \"p1\","
            + "\"threatPolicyInstalled\": true,"
            + "\"threatPolicyName\": \"p2\""
            + "}" // policy
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, CpmiGatewayCluster.class),
        equalTo(
            new CpmiGatewayCluster(
                ImmutableList.of("m1"),
                Ip.ZERO,
                "foo",
                ImmutableList.of(),
                new GatewayOrServerPolicy("p1", "p2"),
                Uid.of("0"))));
  }

  @Test
  public void testJavaSerialization() {
    CpmiGatewayCluster obj =
        new CpmiGatewayCluster(
            ImmutableList.of(),
            Ip.ZERO,
            "foo",
            ImmutableList.of(),
            GatewayOrServerPolicy.empty(),
            Uid.of("0"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    CpmiGatewayCluster obj =
        new CpmiGatewayCluster(
            ImmutableList.of(),
            Ip.ZERO,
            "foo",
            ImmutableList.of(),
            GatewayOrServerPolicy.empty(),
            Uid.of("0"));
    new EqualsTester()
        .addEqualityGroup(
            obj,
            new CpmiGatewayCluster(
                ImmutableList.of(),
                Ip.ZERO,
                "foo",
                ImmutableList.of(),
                GatewayOrServerPolicy.empty(),
                Uid.of("0")))
        .addEqualityGroup(
            new CpmiGatewayCluster(
                ImmutableList.of("m1"),
                Ip.ZERO,
                "foo",
                ImmutableList.of(),
                GatewayOrServerPolicy.empty(),
                Uid.of("0")))
        .addEqualityGroup(
            new CpmiGatewayCluster(
                ImmutableList.of(),
                Ip.parse("0.0.0.1"),
                "foo",
                ImmutableList.of(),
                GatewayOrServerPolicy.empty(),
                Uid.of("0")))
        .addEqualityGroup(
            new CpmiGatewayCluster(
                ImmutableList.of(),
                Ip.ZERO,
                "bar",
                ImmutableList.of(),
                GatewayOrServerPolicy.empty(),
                Uid.of("0")))
        .addEqualityGroup(
            new CpmiGatewayCluster(
                ImmutableList.of(),
                Ip.ZERO,
                "foo",
                ImmutableList.of(InterfaceTest.TEST_INSTANCE),
                GatewayOrServerPolicy.empty(),
                Uid.of("0")))
        .addEqualityGroup(
            new CpmiGatewayCluster(
                ImmutableList.of(),
                Ip.ZERO,
                "foo",
                ImmutableList.of(),
                new GatewayOrServerPolicy("p1", null),
                Uid.of("0")))
        .addEqualityGroup(
            new CpmiGatewayCluster(
                ImmutableList.of(),
                Ip.ZERO,
                "foo",
                ImmutableList.of(),
                GatewayOrServerPolicy.empty(),
                Uid.of("1")))
        .testEquals();
  }
}
