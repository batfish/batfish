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

public class ConfigDbTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input = "{ \"GARBAGE\": 1, \"INTERFACE\": {}}";
    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, ConfigDb.class),
        equalTo(
            new ConfigDb(
                ImmutableMap.of(
                    ConfigDbObject.Type.INTERFACE, new InterfaceDb(ImmutableMap.of())))));
  }

  @Test
  public void testJavaSerialization() {
    ConfigDb obj = new ConfigDb(ImmutableMap.of());
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new ConfigDb(ImmutableMap.of()), new ConfigDb(ImmutableMap.of()))
        .addEqualityGroup(
            new ConfigDb(
                ImmutableMap.of(ConfigDbObject.Type.INTERFACE, new InterfaceDb(ImmutableMap.of()))))
        .testEquals();
  }
}
