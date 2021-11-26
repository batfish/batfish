package org.batfish.vendor.sonic.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class DeviceMetadataTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input = "{" + "\"localhost\": { \"hostname\": \"myname\" }}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, DeviceMetadata.class),
        equalTo(new DeviceMetadata(ImmutableMap.of("hostname", "myname"))));
  }

  @Test
  public void testJavaSerialization() {
    DeviceMetadata obj = new DeviceMetadata(ImmutableMap.of("hostname", "myname"));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testEquals() {
    DeviceMetadata obj = new DeviceMetadata(ImmutableMap.of());
    new EqualsTester()
        .addEqualityGroup(obj, new DeviceMetadata(ImmutableMap.of()))
        .addEqualityGroup(new DeviceMetadata(ImmutableMap.of("hostname", "myname")))
        .testEquals();
  }
}
