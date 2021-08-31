package org.batfish.vendor.check_point_management;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class InterfaceTopologyTest {
  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input = "{ \"GARBAGE\":0, \"leads-to-internet\":\"true\" }";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, InterfaceTopology.class),
        equalTo(new InterfaceTopology(true)));
  }

  @Test
  public void testJavaSerialization() {
    InterfaceTopology obj = new InterfaceTopology(true);
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
