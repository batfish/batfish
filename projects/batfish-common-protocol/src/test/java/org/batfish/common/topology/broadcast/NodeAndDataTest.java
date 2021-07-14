package org.batfish.common.topology.broadcast;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class NodeAndDataTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new NodeAndData<>(new EthernetHub("1"), EthernetTag.untagged()),
            new NodeAndData<>(new EthernetHub("1"), EthernetTag.untagged()))
        .addEqualityGroup(new NodeAndData<>(new EthernetHub("2"), EthernetTag.untagged()))
        .addEqualityGroup(new NodeAndData<>(new EthernetHub("2"), EthernetTag.tagged(3)))
        .testEquals();
  }
}
