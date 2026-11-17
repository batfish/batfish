package org.batfish.vendor.sonic.representation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

class TacplusTest {

  @Test
  void testJavaSerialization() {
    Tacplus obj = Tacplus.builder().setSrcIntf("ll").build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  void testEquals() {
    Tacplus.Builder builder = Tacplus.builder();
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setSrcIntf("ll").build())
        .testEquals();
  }

  @Test
  void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        "{" + "    \"auth_type\": \"login\"," + "    \"src_intf\": \"Loopback0\"" + "    }";
    assertEquals(
        Tacplus.builder().setSrcIntf("Loopback0").build(),
        BatfishObjectMapper.mapper().readValue(input, Tacplus.class));
  }
}
