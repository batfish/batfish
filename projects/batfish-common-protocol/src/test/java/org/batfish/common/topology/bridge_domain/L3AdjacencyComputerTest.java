package org.batfish.common.topology.bridge_domain;

import static org.batfish.common.topology.bridge_domain.L3AdjacencyComputer.BATFISH_GLOBAL_HUB;
import static org.batfish.common.topology.bridge_domain.L3AdjacencyComputer.computeL1Hubs;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.InterfaceType.PHYSICAL;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.topology.Layer1Topologies;
import org.batfish.common.topology.Layer1TopologiesFactory;
import org.batfish.common.topology.Layer1Topology;
import org.batfish.common.topology.bridge_domain.node.L1Hub;
import org.batfish.common.topology.bridge_domain.node.L1Interface;
import org.batfish.datamodel.BumTransportMethod;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.VxlanTopology;
import org.batfish.datamodel.vxlan.VxlanTopologyUtils;
import org.junit.Test;

/** Test of {@link L3AdjacencyComputer}. */
public final class L3AdjacencyComputerTest {

  private static Map<NodeInterfacePair, L1Interface> makeL1Map(Interface... interfaces) {
    return Arrays.stream(interfaces)
        .map(NodeInterfacePair::of)
        .collect(ImmutableMap.toImmutableMap(nip -> nip, L1Interface::new));
  }

  private static Set<NodeInterfacePair> getPairs(Collection<L1Interface> interfaces) {
    return interfaces.stream()
        .map(L1Interface::getInterface)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Test
  public void testComputeEthernetHubs() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c = nf.configurationBuilder().build();
    Interface i1 = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();
    NodeInterfacePair n1 = NodeInterfacePair.of(i1);
    Interface i2 = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();
    NodeInterfacePair n2 = NodeInterfacePair.of(i2);
    Interface i3 = nf.interfaceBuilder().setOwner(c).setType(PHYSICAL).build();
    NodeInterfacePair n3 = NodeInterfacePair.of(i3);
    {
      // No l1 topology.
      Map<NodeInterfacePair, L1Interface> l1 = makeL1Map(i1, i2, i3);
      Map<String, L1Hub> hubs = computeL1Hubs(l1, ImmutableMap.of(), Layer1Topology.EMPTY);
      assertThat(hubs.keySet(), contains(BATFISH_GLOBAL_HUB));
      assertThat(
          getPairs(hubs.get(BATFISH_GLOBAL_HUB).getToL1ForTest().keySet()),
          containsInAnyOrder(n1, n2, n3));
    }
    {
      // One edge.
      Map<String, L1Hub> hubs =
          computeL1Hubs(
              makeL1Map(i1, i2, i3),
              ImmutableMap.of(),
              new Layer1Topology(
                  ImmutableSet.of(
                      new Layer1Edge(
                          c.getHostname(), i1.getName(), c.getHostname(), i2.getName()))));
      assertThat(hubs, aMapWithSize(2));
      String otherKey =
          Iterables.getOnlyElement(
              Sets.difference(hubs.keySet(), ImmutableSet.of(BATFISH_GLOBAL_HUB)));
      assertThat(getPairs(hubs.get(BATFISH_GLOBAL_HUB).getToL1ForTest().keySet()), contains(n3));
      assertThat(
          getPairs(hubs.get(otherKey).getToL1ForTest().keySet()), containsInAnyOrder(n1, n2));
    }
    {
      // One interface with disconnected edge.
      Map<String, L1Hub> hubs =
          computeL1Hubs(
              makeL1Map(i1, i2, i3),
              ImmutableMap.of(),
              new Layer1Topology(
                  ImmutableSet.of(
                      new Layer1Edge(
                          c.getHostname(), i1.getName(), "no-such-hostname", "no-such-iface"))));
      assertThat(hubs, aMapWithSize(2));
      String otherKey =
          Iterables.getOnlyElement(
              Sets.difference(hubs.keySet(), ImmutableSet.of(BATFISH_GLOBAL_HUB)));
      assertThat(
          getPairs(hubs.get(BATFISH_GLOBAL_HUB).getToL1ForTest().keySet()),
          containsInAnyOrder(n2, n3));
      assertThat(getPairs(hubs.get(otherKey).getToL1ForTest().keySet()), contains(n1));
    }
    {
      // Line.
      Map<String, L1Hub> hubs =
          computeL1Hubs(
              makeL1Map(i1, i2, i3),
              ImmutableMap.of(),
              new Layer1Topology(
                  ImmutableSet.of(
                      new Layer1Edge(c.getHostname(), i1.getName(), c.getHostname(), i2.getName()),
                      new Layer1Edge(
                          c.getHostname(), i3.getName(), c.getHostname(), i2.getName()))));
      assertThat(hubs, aMapWithSize(1));
      assertThat(
          getPairs(Iterables.getOnlyElement(hubs.values()).getToL1ForTest().keySet()),
          containsInAnyOrder(n1, n2, n3));
    }
  }

  private static Map<String, Configuration> simple3InterfaceNetwork() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 = nf.configurationBuilder().setHostname("c1").build();
    nf.interfaceBuilder()
        .setOwner(c1)
        .setName("i1")
        .setType(PHYSICAL)
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.1/24"))
        .build();
    Configuration c2 = nf.configurationBuilder().setHostname("c2").build();
    nf.interfaceBuilder()
        .setOwner(c2)
        .setName("i2")
        .setType(PHYSICAL)
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.2/24"))
        .build();
    Configuration c3 = nf.configurationBuilder().setHostname("c3").build();
    nf.interfaceBuilder()
        .setOwner(c3)
        .setName("i3")
        .setType(PHYSICAL)
        .setAddress(ConcreteInterfaceAddress.parse("1.2.3.3/24"))
        .build();
    return ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2, c3.getHostname(), c3);
  }

  @Test
  public void testE2e_noL1() {
    Map<String, Configuration> configs = simple3InterfaceNetwork();
    // With no L1 topology, all 3 interfaces in same domain.
    L3AdjacencyComputer l3 =
        new L3AdjacencyComputer(configs, Layer1Topologies.empty(), VxlanTopology.EMPTY);
    Map<NodeInterfacePair, Integer> domains = l3.findAllBroadcastDomains();
    assertThat("all interfaces have a domain", domains, aMapWithSize(3));
    assertThat("all interfaces in same domain", ImmutableSet.copyOf(domains.values()), hasSize(1));
  }

  @Test
  public void testE2e_L1() {
    // With L1 topology, only connected interfaces in same domain.
    Map<String, Configuration> configs = simple3InterfaceNetwork();
    NodeInterfacePair n1 = NodeInterfacePair.of("c1", "i1");
    NodeInterfacePair n2 = NodeInterfacePair.of("c2", "i2");
    NodeInterfacePair n3 = NodeInterfacePair.of("c3", "i3");
    Layer1Topology physical =
        new Layer1Topology(
            new Layer1Edge(
                n1.getHostname(), n1.getInterface(), n3.getHostname(), n3.getInterface()));
    L3AdjacencyComputer l3 =
        new L3AdjacencyComputer(
            configs,
            Layer1TopologiesFactory.create(physical, Layer1Topology.EMPTY, configs),
            VxlanTopology.EMPTY);
    Map<NodeInterfacePair, Integer> domains = l3.findAllBroadcastDomains();
    assertThat("all interfaces have a domain", domains, aMapWithSize(3));
    assertThat("connected ifaces in same domain", domains.get(n1), equalTo(domains.get(n3)));
    assertThat(
        "global hub not connected to other interfaces",
        domains.get(n2),
        not(equalTo(domains.get(n1))));
  }

  @Test
  public void testE2e_disconnected() {
    Map<String, Configuration> configs = simple3InterfaceNetwork();
    NodeInterfacePair n1 = NodeInterfacePair.of("c1", "i1");
    NodeInterfacePair n2 = NodeInterfacePair.of("c2", "i2");
    NodeInterfacePair n3 = NodeInterfacePair.of("c3", "i3");
    // With L1 topology to disconnected interface, only global hub connected.
    Layer1Topology physical =
        new Layer1Topology(
            new Layer1Edge(n1.getHostname(), n1.getInterface(), "no-such-host", "no-such-iface"));
    L3AdjacencyComputer l3 =
        new L3AdjacencyComputer(
            configs,
            Layer1TopologiesFactory.create(physical, Layer1Topology.EMPTY, configs),
            VxlanTopology.EMPTY);
    Map<NodeInterfacePair, Integer> domains = l3.findAllBroadcastDomains();
    assertThat("all interfaces have a domain", domains, aMapWithSize(3));
    assertThat("global hub ifaces in same domain", domains.get(n2), equalTo(domains.get(n3)));
    assertThat(
        "n1 is disconnected, not connected to other interfaces",
        domains.get(n1),
        not(equalTo(domains.get(n2))));
  }

  @Test
  public void testE2e_encapsulation() {
    // Encapsulation vlan honored, even with no L1.
    Map<String, Configuration> configs = simple3InterfaceNetwork();
    NodeInterfacePair n1 = NodeInterfacePair.of("c1", "i1");
    NodeInterfacePair n2 = NodeInterfacePair.of("c2", "i2");
    NodeInterfacePair n3 = NodeInterfacePair.of("c3", "i3");
    configs.get(n1.getHostname()).getAllInterfaces().get(n1.getInterface()).setEncapsulationVlan(4);
    L3AdjacencyComputer l3 =
        new L3AdjacencyComputer(configs, Layer1Topologies.empty(), VxlanTopology.EMPTY);
    Map<NodeInterfacePair, Integer> domains = l3.findAllBroadcastDomains();
    assertThat("all interfaces have a domain", domains, aMapWithSize(3));
    assertThat(
        "unencapsulated interface in same domain", domains.get(n2), equalTo(domains.get(n3)));
    assertThat(
        "encapsulated interface not connected to unencapsulated interfaces",
        domains.get(n2),
        not(equalTo(domains.get(n1))));
  }

  @Test
  public void testE2e_vxlan() {
    // Topology:
    // h11 <=>           <=> h21
    //     vlan10     vlan10
    //         r1 <=> r2
    //     vlan20     vlan20
    // h12 <=>           <=> h22
    //
    // r1 and r2 only have underlay and vxlan tunnel between them - no vlans
    // vlans 10 and 20 should be bridged across vxlan so that:
    // - h11 and h21 are in the same broadcast domain
    // - h12 and h22 are in the same broadcast domain
    // vlan 10 corresponds to VNI 10010
    // vlan 20 corresponds to VNI 10020
    Configuration h11 =
        Configuration.builder().setHostname("h11").setConfigurationFormat(CISCO_IOS).build();
    Vrf h11Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(h11).build();
    Interface h11i =
        Interface.builder()
            .setName("h11i")
            .setAddress(ConcreteInterfaceAddress.parse("10.0.10.1/24"))
            .setType(PHYSICAL)
            .setVrf(h11Vrf)
            .setOwner(h11)
            .build();

    Configuration h12 =
        Configuration.builder().setHostname("h12").setConfigurationFormat(CISCO_IOS).build();
    Vrf h12Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(h12).build();
    Interface h12i =
        Interface.builder()
            .setName("h12i")
            .setAddress(ConcreteInterfaceAddress.parse("10.0.20.1/24"))
            .setType(PHYSICAL)
            .setVrf(h12Vrf)
            .setOwner(h12)
            .build();

    Configuration h21 =
        Configuration.builder().setHostname("h21").setConfigurationFormat(CISCO_IOS).build();
    Vrf h21Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(h21).build();
    Interface h21i =
        Interface.builder()
            .setName("h21i")
            .setAddress(ConcreteInterfaceAddress.parse("10.0.10.2/24"))
            .setType(PHYSICAL)
            .setVrf(h21Vrf)
            .setOwner(h21)
            .build();

    Configuration h22 =
        Configuration.builder().setHostname("h22").setConfigurationFormat(CISCO_IOS).build();
    Vrf h22Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(h22).build();
    Interface h22i =
        Interface.builder()
            .setName("h22i")
            .setAddress(ConcreteInterfaceAddress.parse("10.0.20.2/24"))
            .setType(PHYSICAL)
            .setVrf(h22Vrf)
            .setOwner(h22)
            .build();

    Configuration r1 =
        Configuration.builder().setHostname("r1").setConfigurationFormat(CISCO_IOS).build();
    Vrf r1Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(r1).build();
    Interface r1h11 =
        Interface.builder()
            .setName("r1h11")
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setAccessVlan(10)
            .setType(PHYSICAL)
            .setVrf(r1Vrf)
            .setOwner(r1)
            .build();
    Interface r1h12 =
        Interface.builder()
            .setName("r1h12")
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setAccessVlan(20)
            .setType(PHYSICAL)
            .setVrf(r1Vrf)
            .setOwner(r1)
            .build();
    Interface r1r2 =
        Interface.builder()
            .setName("r1r2")
            .setAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24"))
            .setType(PHYSICAL)
            .setVrf(r1Vrf)
            .setOwner(r1)
            .build();
    r1Vrf.addLayer2Vni(
        Layer2Vni.builder()
            .setVni(10010)
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSrcVrf(DEFAULT_VRF_NAME)
            .setVlan(10)
            .setSourceAddress(Ip.parse("10.0.0.1"))
            .setBumTransportIps(ImmutableSet.of(Ip.parse("10.0.0.2")))
            .build());
    r1Vrf.addLayer2Vni(
        Layer2Vni.builder()
            .setVni(10020)
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSrcVrf(DEFAULT_VRF_NAME)
            .setVlan(20)
            .setSourceAddress(Ip.parse("10.0.0.1"))
            .setBumTransportIps(ImmutableSet.of(Ip.parse("10.0.0.2")))
            .build());

    Configuration r2 =
        Configuration.builder().setHostname("r2").setConfigurationFormat(CISCO_IOS).build();
    Vrf r2Vrf = Vrf.builder().setName(DEFAULT_VRF_NAME).setOwner(r2).build();
    Interface r2h21 =
        Interface.builder()
            .setName("r2h21")
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setAccessVlan(10)
            .setType(PHYSICAL)
            .setVrf(r2Vrf)
            .setOwner(r2)
            .build();
    Interface r2h22 =
        Interface.builder()
            .setName("r2h22")
            .setSwitchport(true)
            .setSwitchportMode(SwitchportMode.ACCESS)
            .setAccessVlan(20)
            .setType(PHYSICAL)
            .setVrf(r2Vrf)
            .setOwner(r2)
            .build();
    Interface r2r1 =
        Interface.builder()
            .setName("r2r1")
            .setAddress(ConcreteInterfaceAddress.parse("10.0.0.2/24"))
            .setType(PHYSICAL)
            .setVrf(r2Vrf)
            .setOwner(r2)
            .build();
    r2Vrf.addLayer2Vni(
        Layer2Vni.builder()
            .setVni(10010)
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSrcVrf(DEFAULT_VRF_NAME)
            .setVlan(10)
            .setSourceAddress(Ip.parse("10.0.0.2"))
            .setBumTransportIps(ImmutableSet.of(Ip.parse("10.0.0.1")))
            .build());
    r2Vrf.addLayer2Vni(
        Layer2Vni.builder()
            .setVni(10020)
            .setBumTransportMethod(BumTransportMethod.UNICAST_FLOOD_GROUP)
            .setSrcVrf(DEFAULT_VRF_NAME)
            .setVlan(20)
            .setSourceAddress(Ip.parse("10.0.0.2"))
            .setBumTransportIps(ImmutableSet.of(Ip.parse("10.0.0.1")))
            .build());

    Map<String, Configuration> configurations =
        ImmutableMap.of(
            h11.getHostname(), h11,
            h12.getHostname(), h12,
            h21.getHostname(), h21,
            h22.getHostname(), h22,
            r1.getHostname(), r1,
            r2.getHostname(), r2);
    Set<Layer1Edge> l1EdgesOneSided =
        ImmutableSet.of(
            new Layer1Edge(h11.getHostname(), h11i.getName(), r1.getHostname(), r1h11.getName()),
            new Layer1Edge(h12.getHostname(), h12i.getName(), r1.getHostname(), r1h12.getName()),
            new Layer1Edge(r1.getHostname(), r1r2.getName(), r2.getHostname(), r2r1.getName()),
            new Layer1Edge(h21.getHostname(), h21i.getName(), r2.getHostname(), r2h21.getName()),
            new Layer1Edge(h22.getHostname(), h22i.getName(), r2.getHostname(), r2h22.getName()));
    Set<Layer1Edge> l1Edges =
        l1EdgesOneSided.stream()
            .flatMap(e -> Stream.of(e, e.reverse()))
            .collect(ImmutableSet.toImmutableSet());
    Layer1Topology l1 = new Layer1Topology(l1Edges);
    Layer1Topologies layer1Topologies =
        Layer1TopologiesFactory.create(l1, Layer1Topology.EMPTY, configurations);
    VxlanTopology vxlanTopology = VxlanTopologyUtils.computeInitialVxlanTopology(configurations);
    L3AdjacencyComputer l3 =
        new L3AdjacencyComputer(configurations, layer1Topologies, vxlanTopology);
    Map<NodeInterfacePair, Integer> bds = l3.findAllBroadcastDomains();

    assertThat(bds.get(NodeInterfacePair.of(h11i)), equalTo(bds.get(NodeInterfacePair.of(h21i))));
    assertThat(bds.get(NodeInterfacePair.of(h12i)), equalTo(bds.get(NodeInterfacePair.of(h22i))));
    assertThat(
        bds.get(NodeInterfacePair.of(h11i)), not(equalTo(bds.get(NodeInterfacePair.of(h22i)))));
  }
}
