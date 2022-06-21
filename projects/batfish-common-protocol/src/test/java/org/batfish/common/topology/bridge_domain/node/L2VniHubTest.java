package org.batfish.common.topology.bridge_domain.node;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

/** Test of {@link L2VniHub}. */
public final class L2VniHubTest {

  @Test
  public void testEquals() {
    L2VniHub obj = new L2VniHub("a");
    new EqualsTester()
        .addEqualityGroup(obj, new L2VniHub("a"))
        .addEqualityGroup(new L2VniHub("b"))
        .testEquals();
  }
}
