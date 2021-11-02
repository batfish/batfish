package org.batfish.symbolic.state;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.packet_policy.Action;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.LiteralVrfName;
import org.junit.Test;

public final class PacketPolicyActionTest {
  @Test
  public void testEquals() {
    String h1 = "h1";
    String h2 = "h2";
    String v1 = "v1";
    String v2 = "v2";
    String p1 = "p1";
    String p2 = "p2";
    Action a1 = Drop.instance();
    Action a2 = new FibLookup(new LiteralVrfName("v"));
    new EqualsTester()
        .addEqualityGroup(
            new PacketPolicyAction(h1, v1, p1, a1), new PacketPolicyAction(h1, v1, p1, a1))
        .addEqualityGroup(new PacketPolicyAction(h2, v1, p1, a1))
        .addEqualityGroup(new PacketPolicyAction(h1, v2, p1, a1))
        .addEqualityGroup(new PacketPolicyAction(h1, v1, p2, a1))
        .addEqualityGroup(new PacketPolicyAction(h1, v1, p1, a2))
        .testEquals();
  }
}
