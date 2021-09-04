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
              + "\"access-policy-installed\": true,"
              + "\"access-policy-name\": \"p1\","
              + "\"threat-policy-installed\": true,"
              + "\"threat-policy-name\": \"p2\""
              + "}";
      assertThat(
          BatfishObjectMapper.ignoreUnknownMapper().readValue(input, GatewayOrServerPolicy.class),
          equalTo(new GatewayOrServerPolicy("p1", "p2")));
    }
    {
      String input = "{}";
      assertThat(
          BatfishObjectMapper.ignoreUnknownMapper().readValue(input, GatewayOrServerPolicy.class),
          equalTo(GatewayOrServerPolicy.empty()));
    }
  }

  @Test
  public void testJavaSerialization() {
    GatewayOrServerPolicy obj = new GatewayOrServerPolicy("p1", "p2");
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    GatewayOrServerPolicy obj = GatewayOrServerPolicy.empty();
    new EqualsTester()
        .addEqualityGroup(obj, new GatewayOrServerPolicy(null, null))
        .addEqualityGroup(new GatewayOrServerPolicy("p1", null))
        .addEqualityGroup(new GatewayOrServerPolicy(null, "p2"))
        .testEquals();
  }
}
