package org.batfish.vendor.sonic.representation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class SyslogServerTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input = "{ \"10.128.255.33\": {}, \"10.128.255.34\": {}}";

    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, SyslogServer.class),
        equalTo(new SyslogServer(ImmutableSet.of("10.128.255.33", "10.128.255.34"))));
  }

  @Test
  public void testJavaSerialization() {
    SyslogServer obj = new SyslogServer(ImmutableSet.of());
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    SyslogServer obj = new SyslogServer(ImmutableSet.of());
    new EqualsTester()
        .addEqualityGroup(obj, new SyslogServer(ImmutableSet.of()))
        .addEqualityGroup(new SyslogServer(ImmutableSet.of("server")))
        .testEquals();
  }
}
