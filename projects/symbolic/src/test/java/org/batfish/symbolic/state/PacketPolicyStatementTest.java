package org.batfish.symbolic.state;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public final class PacketPolicyStatementTest {
  @Test
  public void testEquals() {
    String h1 = "h1";
    String h2 = "h2";
    String v1 = "v1";
    String v2 = "v2";
    String p1 = "p1";
    String p2 = "p2";
    new EqualsTester()
        .addEqualityGroup(
            new PacketPolicyStatement(h1, v1, p1, 1), new PacketPolicyStatement(h1, v1, p1, 1))
        .addEqualityGroup(new PacketPolicyStatement(h2, v1, p1, 1))
        .addEqualityGroup(new PacketPolicyStatement(h1, v2, p1, 1))
        .addEqualityGroup(new PacketPolicyStatement(h1, v1, p2, 1))
        .addEqualityGroup(new PacketPolicyStatement(h1, v1, p1, 2))
        .testEquals();
  }
}
