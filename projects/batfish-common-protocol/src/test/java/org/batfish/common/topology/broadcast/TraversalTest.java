package org.batfish.common.topology.broadcast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

/** Tests some interesting topologies of {@link Node nodes} and {@link Edge edges}. */
public class TraversalTest {
  private static Set<L3Interface> getDomain(L3Interface iface) {
    Set<L3Interface> ret = new HashSet<>();
    Set<NodeAndData<?, ?>> visited = new HashSet<>();
    iface.originate(ret, visited);
    return ret;
  }

  /** Untagged L3 interface, connected directly to another. */
  @Test
  public void testSimpleUntagged() {
    NodeInterfacePair i1 = NodeInterfacePair.of("h1", "i1");
    NodeInterfacePair i2 = NodeInterfacePair.of("h2", "i2");

    L3Interface l31 = new L3Interface(i1);
    PhysicalInterface p1 = new PhysicalInterface(i1);
    EthernetHub hub = new EthernetHub("hub");
    PhysicalInterface p2 = new PhysicalInterface(i2);
    L3Interface l32 = new L3Interface(i2);

    Edges.connectL3Untagged(l31, p1);
    Edges.connectToHub(hub, p1);
    Edges.connectToHub(hub, p2);
    Edges.connectL3Untagged(l32, p2);

    assertThat(getDomain(l31), containsInAnyOrder(l31, l32));
    assertThat(getDomain(l32), containsInAnyOrder(l31, l32));
  }

  /** L3 dot1q interface, connected directly to another. */
  @Test
  public void testSimpleTagged() {
    NodeInterfacePair i1 = NodeInterfacePair.of("h1", "i1");
    NodeInterfacePair i1_5 = NodeInterfacePair.of("h1", "i1.5");
    NodeInterfacePair i1_10 = NodeInterfacePair.of("h1", "i1.10");
    NodeInterfacePair i2 = NodeInterfacePair.of("h2", "i2");
    NodeInterfacePair i2_5 = NodeInterfacePair.of("h2", "i2.5");
    NodeInterfacePair i2_10 = NodeInterfacePair.of("h2", "i2.10");

    L3Interface l31_5 = new L3Interface(i1_5);
    L3Interface l31_10 = new L3Interface(i1_10);
    PhysicalInterface p1 = new PhysicalInterface(i1);
    EthernetHub hub = new EthernetHub("hub");
    PhysicalInterface p2 = new PhysicalInterface(i2);
    L3Interface l32_5 = new L3Interface(i2_5);
    L3Interface l32_10 = new L3Interface(i2_10);

    Edges.connectL3Dot1q(l31_5, p1, 5);
    Edges.connectL3Dot1q(l31_10, p1, 10);
    Edges.connectToHub(hub, p1);
    Edges.connectToHub(hub, p2);
    Edges.connectL3Dot1q(l32_5, p2, 5);
    Edges.connectL3Dot1q(l32_10, p2, 10);

    assertThat(getDomain(l31_5), containsInAnyOrder(l31_5, l32_5));
    assertThat(getDomain(l32_10), containsInAnyOrder(l31_10, l32_10));
  }

  /** Untagged L3 interface, connected to an IRB through a switchport in access mode. */
  @Test
  public void testAccessMode() {
    L3Interface l31 = new L3Interface(NodeInterfacePair.of("h1", "Eth1"));
    PhysicalInterface p1 = new PhysicalInterface(l31.getIface());
    EthernetHub hub = new EthernetHub("hub");
    PhysicalInterface p2 = new PhysicalInterface(NodeInterfacePair.of("h2", "Eth1"));
    DeviceBroadcastDomain switch2 = new DeviceBroadcastDomain("h2");
    L3Interface l32 = new L3Interface(NodeInterfacePair.of("h2", "Vlan5"));

    Edges.connectL3Untagged(l31, p1);
    Edges.connectToHub(hub, p1);
    Edges.connectToHub(hub, p2);
    Edges.connectInAccessMode(5, p2, switch2);
    Edges.connectIRB(l32, switch2, 5);

    assertThat(getDomain(l31), containsInAnyOrder(l31, l32));
    assertThat(getDomain(l32), containsInAnyOrder(l31, l32));
  }

  /** Two IRBs connected with vlan translation in access mode. */
  @Test
  public void testVlanTranslation() {
    L3Interface l31 = new L3Interface(NodeInterfacePair.of("h1", "Vlan5"));
    DeviceBroadcastDomain switch1 = new DeviceBroadcastDomain("h1");
    PhysicalInterface p1 = new PhysicalInterface(NodeInterfacePair.of("h1", "Eth1"));
    EthernetHub hub = new EthernetHub("hub");
    PhysicalInterface p2 = new PhysicalInterface(NodeInterfacePair.of("h2", "Eth1"));
    DeviceBroadcastDomain switch2 = new DeviceBroadcastDomain("h2");
    L3Interface l32 = new L3Interface(NodeInterfacePair.of("h2", "Vlan6"));

    Edges.connectIRB(l31, switch1, 5);
    Edges.connectInAccessMode(5, p1, switch1);
    Edges.connectToHub(hub, p1);
    Edges.connectToHub(hub, p2);
    Edges.connectInAccessMode(6, p2, switch2);
    Edges.connectIRB(l32, switch2, 6);

    assertThat(getDomain(l31), containsInAnyOrder(l31, l32));
    assertThat(getDomain(l32), containsInAnyOrder(l31, l32));
  }
}
