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

public class NtpServerTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input = "{ \"10.128.255.33\": {}, \"10.128.255.34\": {}}";

    assertThat(
        BatfishObjectMapper.ignoreUnknownMapper().readValue(input, NtpServer.class),
        equalTo(new NtpServer(ImmutableSet.of("10.128.255.33", "10.128.255.34"))));
  }

  @Test
  public void testJavaSerialization() {
    NtpServer obj = new NtpServer(ImmutableSet.of());
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    NtpServer obj = new NtpServer(ImmutableSet.of());
    new EqualsTester()
        .addEqualityGroup(obj, new NtpServer(ImmutableSet.of()))
        .addEqualityGroup(new NtpServer(ImmutableSet.of("server")))
        .testEquals();
  }
}
