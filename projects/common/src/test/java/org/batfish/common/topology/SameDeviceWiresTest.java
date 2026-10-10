package org.batfish.common.topology;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.util.Map;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.junit.Test;

public final class SameDeviceWiresTest {
  private static final NodeInterfacePair BOND9 = NodeInterfacePair.of("dos", "bond9");
  private static final NodeInterfacePair BOND9_192 = NodeInterfacePair.of("dos", "bond9.192");
  private static final NodeInterfacePair BOND13 = NodeInterfacePair.of("dos", "bond13");
  private static final NodeInterfacePair BOND13_128 = NodeInterfacePair.of("dos", "bond13.128");
  private static final NodeInterfacePair BOND1 = NodeInterfacePair.of("dos", "bond1");
  private static final NodeInterfacePair PEER_ETH0 = NodeInterfacePair.of("peer", "eth0");
  private static final NodeInterfacePair PEER_BOND13 = NodeInterfacePair.of("peer", "bond13");

  private static Map<String, Configuration> configs() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setHostname("dos")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    nf.interfaceBuilder().setOwner(c).setName("bond9").build();
    nf.interfaceBuilder().setOwner(c).setName("bond13").build();
    nf.interfaceBuilder().setOwner(c).setName("bond1").build();
    nf.interfaceBuilder()
        .setOwner(c)
        .setName("bond9.192")
        .setDependencies(ImmutableList.of(new Dependency("bond9", DependencyType.BIND)))
        .build();
    nf.interfaceBuilder()
        .setOwner(c)
        .setName("bond13.128")
        .setDependencies(ImmutableList.of(new Dependency("bond13", DependencyType.BIND)))
        .build();
    return ImmutableMap.of("dos", c);
  }

  private static Layer1Topology l1(NodeInterfacePair... pairs) {
    ImmutableList.Builder<Layer1Edge> edges = ImmutableList.builder();
    for (int i = 0; i < pairs.length; i += 2) {
      edges.add(
          new Layer1Edge(
              pairs[i].getHostname(),
              pairs[i].getInterface(),
              pairs[i + 1].getHostname(),
              pairs[i + 1].getInterface()));
    }
    return new Layer1Topology(edges.build());
  }

  @Test
  public void testNoSelfEdges() {
    SameDeviceWires wires = SameDeviceWires.create(l1(BOND1, PEER_ETH0), configs());
    assertThat(wires, equalTo(SameDeviceWires.EMPTY));
    assertFalse(wires.isWired(BOND9, BOND13));
  }

  @Test
  public void testParentEdge() {
    SameDeviceWires wires = SameDeviceWires.create(l1(BOND1, PEER_ETH0, BOND9, BOND13), configs());
    // the parents themselves, both directions
    assertTrue(wires.isWired(BOND9, BOND13));
    assertTrue(wires.isWired(BOND13, BOND9));
    // subinterfaces of the wired parents, and a subinterface with the other parent
    assertTrue(wires.isWired(BOND9_192, BOND13_128));
    assertTrue(wires.isWired(BOND9_192, BOND13));
    assertTrue(wires.isWired(BOND9, BOND13_128));
    // not wired: an unrelated interface, and the same-named interface on another device
    assertFalse(wires.isWired(BOND1, BOND9));
    assertFalse(wires.isWired(BOND9, PEER_BOND13));
  }

  @Test
  public void testSubinterfaceEdge() {
    SameDeviceWires wires = SameDeviceWires.create(l1(BOND9_192, BOND13_128), configs());
    assertTrue(wires.isWired(BOND9_192, BOND13_128));
    assertTrue(wires.isWired(BOND13_128, BOND9_192));
    // the parents are not wired to each other by a subinterface edge
    assertFalse(wires.isWired(BOND9, BOND13));
  }

  @Test
  public void testSerialization() {
    SameDeviceWires wires = SameDeviceWires.create(l1(BOND9, BOND13), configs());
    assertThat(SerializationUtils.clone(wires), equalTo(wires));
  }

  @Test
  public void testEquals() {
    Map<String, Configuration> configs = configs();
    new EqualsTester()
        .addEqualityGroup(SameDeviceWires.EMPTY, SameDeviceWires.create(l1(), configs))
        .addEqualityGroup(
            SameDeviceWires.create(l1(BOND9, BOND13), configs),
            SameDeviceWires.create(l1(BOND13, BOND9), configs))
        .addEqualityGroup(SameDeviceWires.create(l1(BOND9_192, BOND13_128), configs))
        .testEquals();
  }
}
