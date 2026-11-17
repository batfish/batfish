package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.specifier.InterfaceWithConnectedIpsSpecifier.Factory;
import org.junit.jupiter.api.Test;

/** Tests of {@link InterfaceWithConnectedIpsSpecifier}. */
class InterfaceWithConnectedIpsSpecifierTest {

  @Test
  void testLoadNull() {
    Factory f = new Factory();

    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> f.buildInterfaceSpecifier(null));
    assertThat(
        exception.getMessage(), containsString("wildcard provided as a string input, not null"));
  }

  @Test
  void testLoadNotString() {
    Factory f = new Factory();

    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> f.buildInterfaceSpecifier(3));
    assertThat(
        exception.getMessage(), containsString("wildcard provided as a string input, not 3"));
  }

  @Test
  void testLoadNotIpSpace() {
    Factory f = new Factory();

    Throwable exception =
        assertThrows(IllegalArgumentException.class, () -> f.buildInterfaceSpecifier("45.6"));
    assertThat(exception.getMessage(), containsString("Invalid IPv4 address: 45.6"));
  }

  @Test
  void testResolve() {
    Configuration node1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);
    Interface iface11 =
        TestInterface.builder()
            .setName("iface11")
            .setOwner(node1)
            .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
            .build();
    Interface iface12 =
        TestInterface.builder()
            .setName("iface12")
            .setOwner(node1)
            .setAddress(ConcreteInterfaceAddress.parse("2.3.4.5/24"))
            .build();
    node1.setInterfaces(ImmutableSortedMap.of("iface11", iface11, "iface12", iface12));

    Configuration node2 = new Configuration("node2", ConfigurationFormat.CISCO_IOS);
    Interface iface2 =
        TestInterface.builder()
            .setName("iface2")
            .setOwner(node2)
            .setAddress(ConcreteInterfaceAddress.parse("1.2.3.5/30"))
            .build();
    node2.setInterfaces(ImmutableSortedMap.of("iface2", iface2));

    MockSpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableMap.of("node1", node1, "node2", node2))
            .build();

    assertThat(
        new InterfaceWithConnectedIpsSpecifier(Ip.parse("1.2.3.4").toIpSpace())
            .resolve(ImmutableSet.of("node1", "node2"), ctxt),
        equalTo(ImmutableSet.of(NodeInterfacePair.of(iface11), NodeInterfacePair.of(iface2))));

    assertThat(
        new InterfaceWithConnectedIpsSpecifier(Ip.parse("1.2.3.8").toIpSpace())
            .resolve(ImmutableSet.of("node1", "node2"), ctxt),
        equalTo(ImmutableSet.of(NodeInterfacePair.of(iface11))));

    assertThat(
        new InterfaceWithConnectedIpsSpecifier(Ip.parse("2.3.4.5").toIpSpace())
            .resolve(ImmutableSet.of("node1", "node2"), ctxt),
        equalTo(ImmutableSet.of(NodeInterfacePair.of(iface12))));

    assertThat(
        new InterfaceWithConnectedIpsSpecifier(Prefix.parse("3.0.0.0/24").toIpSpace())
            .resolve(ImmutableSet.of("node1", "node2"), ctxt),
        empty());
  }

  @Test
  void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new InterfaceWithConnectedIpsSpecifier(Ip.parse("1.2.3.4").toIpSpace()),
            new InterfaceWithConnectedIpsSpecifier(Ip.parse("1.2.3.4").toIpSpace()))
        .addEqualityGroup(new InterfaceWithConnectedIpsSpecifier(Ip.parse("1.2.3.5").toIpSpace()))
        .testEquals();
  }
}
