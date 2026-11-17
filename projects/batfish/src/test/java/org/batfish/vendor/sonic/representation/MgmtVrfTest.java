package org.batfish.vendor.sonic.representation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

class MgmtVrfTest {

  @Test
  void testJavaSerialization() {
    MgmtVrf obj = MgmtVrf.builder().setMgmtVrfEnabled(true).build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  void testEquals() {
    MgmtVrf.Builder builder = MgmtVrf.builder();
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setMgmtVrfEnabled(true).build())
        .testEquals();
  }

  @Test
  void testJacksonDeserialization() throws JsonProcessingException {
    String input = "{\"in_band_mgmt_enabled\": \"false\", \"mgmtVrfEnabled\": \"true\"}";
    assertEquals(
        MgmtVrf.builder().setMgmtVrfEnabled(true).build(),
        BatfishObjectMapper.mapper().readValue(input, MgmtVrf.class));
  }
}
