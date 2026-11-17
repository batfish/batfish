package org.batfish.datamodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.jupiter.api.Test;

class FirewallSessionVrfInfoTest {
  @Test
  void testJsonSerialization() {
    FirewallSessionVrfInfo info = new FirewallSessionVrfInfo(true);
    FirewallSessionVrfInfo clone = BatfishObjectMapper.clone(info, FirewallSessionVrfInfo.class);
    assertEquals(info, clone);
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new FirewallSessionVrfInfo(true), new FirewallSessionVrfInfo(true))
        .addEqualityGroup(new FirewallSessionVrfInfo(false))
        .testEquals();
  }
}
