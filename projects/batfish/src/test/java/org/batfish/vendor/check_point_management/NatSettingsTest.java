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

public class NatSettingsTest {
  /**
   * Instance of this class populated with arbitrary values. Useful for generating a valid object
   * for use in tests.
   */
  public static final NatSettings TEST_INSTANCE =
      new NatSettings(true, "gateway", "All", null, "hide");
  /** Another test instance, that is not-equal to the previous instance. */
  public static final NatSettings TEST_INSTANCE_DIFFERENT =
      new NatSettings(true, "gateway", "All", Ip.parse("2.3.4.5"), "hide");

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{"
            + "\"GARBAGE\":0,"
            + "\"auto-rule\":true,"
            + "\"hide-behind\":\"gateway\","
            + "\"install-on\":\"All\","
            + "\"ipv4-address\":\"2.3.4.5\","
            + "\"method\":\"hide\""
            + "}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, NatSettings.class),
        equalTo(TEST_INSTANCE_DIFFERENT));
  }

  @Test
  public void testJavaSerialization() {
    NatSettings obj = TEST_INSTANCE;
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    NatSettings obj = new NatSettings(true, "gateway", "All", null, "hide");
    new EqualsTester()
        .addEqualityGroup(obj, new NatSettings(true, "gateway", "All", null, "hide"))
        .addEqualityGroup(new NatSettings(false, "gateway", "All", null, "hide"))
        .addEqualityGroup(new NatSettings(true, "server", "All", null, "hide"))
        .addEqualityGroup(new NatSettings(true, "gateway", "None", null, "hide"))
        .addEqualityGroup(new NatSettings(true, "gateway", "None", null, "dontHide"))
        .addEqualityGroup(new NatSettings(true, "gateway", "None", Ip.parse("1.2.3.4"), "hide"))
        .testEquals();
  }
}
