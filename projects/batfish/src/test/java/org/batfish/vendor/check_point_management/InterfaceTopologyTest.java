package org.batfish.vendor.check_point_management;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class InterfaceTopologyTest {
  /**
   * Instance of this class populated with arbitrary values. Useful for generating a valid object
   * for use in tests.
   */
  public static final InterfaceTopology TEST_INSTANCE = new InterfaceTopology(false);

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input = "{ \"GARBAGE\":0, \"leads-to-internet\":\"true\" }";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, InterfaceTopology.class),
        equalTo(new InterfaceTopology(true)));
  }

  @Test
  public void testJavaSerialization() {
    InterfaceTopology obj = TEST_INSTANCE;
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    InterfaceTopology obj = new InterfaceTopology(true);
    new EqualsTester()
        .addEqualityGroup(obj, new InterfaceTopology(true))
        .addEqualityGroup(new InterfaceTopology(false))
        .testEquals();
  }
}
