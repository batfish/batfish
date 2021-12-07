package org.batfish.vendor.sonic.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class DeviceMetadataTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input = "{ \"hostname\": \"name\", \"GARBAGE\": 1 }";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, DeviceMetadata.class),
        equalTo(new DeviceMetadata("name")));
  }

  @Test
  public void testJavaSerialization() {
    DeviceMetadata obj = new DeviceMetadata("name");
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new DeviceMetadata(null), new DeviceMetadata(null))
        .addEqualityGroup(new DeviceMetadata("hostname"))
        .testEquals();
  }
}
