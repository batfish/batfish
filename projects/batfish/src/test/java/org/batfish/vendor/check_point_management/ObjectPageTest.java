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

/** Test of {@link ObjectPage}. */
public final class ObjectPageTest {
  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"objects\":["
            + "{" // object: CpmiClusterMember
            + "\"type\":\"CpmiClusterMember\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"interfaces\":[],"
            + "\"ipv4-address\":\"0.0.0.0\","
            + "\"policy\": {}"
            + "}," // object: CpmiClusterMember
            + "{" // object: Network
            + "\"GARBAGE\":0,"
            + "\"type\":\"network\","
            + "\"uid\":\"0\","
            + "\"name\":\"foo\","
            + "\"nat-settings\": {" // nat-settings
            + "\"auto-rule\":true,"
            + "\"hide-behind\":\"gateway\","
            + "\"install-on\":\"All\","
            + "\"method\":\"hide\""
            + "}," // nat-settings
            + "\"subnet4\":\"0.0.0.0\","
            + "\"subnet-mask\":\"255.255.255.255\""
            + "}" // object: Network
            + "]"
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, ObjectPage.class),
        equalTo(
            new ObjectPage(
                ImmutableList.of(
                    new CpmiClusterMember(
                        Ip.parse("0.0.0.0"),
                        "foo",
                        ImmutableList.of(),
                        new GatewayOrServerPolicy(null, null),
                        Uid.of("0")),
                    NetworkTest.TEST_INSTANCE))));
  }

  @Test
  public void testJavaSerialization() {
    ObjectPage obj =
        new ObjectPage(
            ImmutableList.of(
                new CpmiClusterMember(
                    Ip.parse("0.0.0.0"),
                    "foo",
                    ImmutableList.of(),
                    new GatewayOrServerPolicy(null, null),
                    Uid.of("0")),
                NetworkTest.TEST_INSTANCE));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    ObjectPage obj = new ObjectPage(ImmutableList.of(NetworkTest.TEST_INSTANCE));
    new EqualsTester()
        .addEqualityGroup(obj, new ObjectPage(ImmutableList.of(NetworkTest.TEST_INSTANCE)))
        .addEqualityGroup(new ObjectPage(ImmutableList.of()))
        .testEquals();
  }
}
