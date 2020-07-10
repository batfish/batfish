package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

public class FirewallSessionVrfInfoTest {
  @Test
  public void testJsonSerialization() {
    FirewallSessionVrfInfo info = new FirewallSessionVrfInfo(true);
    FirewallSessionVrfInfo clone = BatfishObjectMapper.clone(info, FirewallSessionVrfInfo.class);
    assertEquals(info, clone);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new FirewallSessionVrfInfo(true), new FirewallSessionVrfInfo(true))
        .addEqualityGroup(new FirewallSessionVrfInfo(false))
        .testEquals();
  }
}
