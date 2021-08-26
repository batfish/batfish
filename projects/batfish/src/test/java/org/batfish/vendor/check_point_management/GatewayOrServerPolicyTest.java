package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link GatewayOrServerPolicy}. */
public final class GatewayOrServerPolicyTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    {
      String input =
          "{"
              + "\"GARBAGE\":0,"
              + "\"accessPolicyInstalled\": true,"
              + "\"accessPolicyName\": \"p1\","
              + "\"threatPolicyInstalled\": true,"
              + "\"threatPolicyName\": \"p2\""
              + "}";
      assertThat(
          BatfishObjectMapper.ignoreUnknownMapper().readValue(input, GatewayOrServerPolicy.class),
          equalTo(new GatewayOrServerPolicy(true, "p1", true, "p2")));
    }
    {
      String input = "{}";
      assertThat(
          BatfishObjectMapper.ignoreUnknownMapper().readValue(input, GatewayOrServerPolicy.class),
          equalTo(new GatewayOrServerPolicy(false, null, false, null)));
    }
  }

  @Test
  public void testJavaSerialization() {
    GatewayOrServerPolicy obj = new GatewayOrServerPolicy(false, "p1", false, "p2");
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    GatewayOrServerPolicy obj = new GatewayOrServerPolicy(false, null, false, null);
    new EqualsTester()
        .addEqualityGroup(obj, new GatewayOrServerPolicy(false, null, false, null))
        .addEqualityGroup(new GatewayOrServerPolicy(true, null, false, null))
        .addEqualityGroup(new GatewayOrServerPolicy(false, "p1", false, null))
        .addEqualityGroup(new GatewayOrServerPolicy(false, null, true, null))
        .addEqualityGroup(new GatewayOrServerPolicy(false, null, false, "p2"))
        .testEquals();
  }
}
