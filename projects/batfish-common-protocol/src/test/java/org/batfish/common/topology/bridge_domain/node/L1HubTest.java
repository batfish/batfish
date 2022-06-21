package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Test of {@link L1Hub}. */
public final class L1HubTest {

  @Test
  public void testEquals() {
    L1Hub obj = new L1Hub("a");
    new EqualsTester()
        .addEqualityGroup(obj, new L1Hub("a"))
        .addEqualityGroup(new L1Hub("b"))
        .testEquals();
  }
}
