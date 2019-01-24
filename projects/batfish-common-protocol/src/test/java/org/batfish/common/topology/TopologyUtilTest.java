package org.batfish.common.topology;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.topology.TopologyUtil.computeIpInterfaceOwners;
import static org.batfish.common.topology.TopologyUtil.computeLayer2Topology;
import static org.batfish.common.topology.TopologyUtil.computeLayer3Topology;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.Topology;
import org.batfish.datamodel.Vrf;
import org.junit.Before;
import org.junit.Test;

public final class TopologyUtilTest {

  private Builder _cb;
  private Interface.Builder _ib;
  private NetworkFactory _nf;
  private Vrf.Builder _vb;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _cb = _nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _vb = _nf.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME);
    _ib = _nf.interfaceBuilder();
  }

  /** Make an interface with the specified parameters */
  private Interface iface(String interfaceName, String ip, boolean active, boolean blacklisted) {
    return _nf.interfaceBuilder()
        .setName(interfaceName)
        .setActive(active)
        .setAddress(new InterfaceAddress(ip))
        .setBlacklisted(blacklisted)
        .build();
  }

  @Test
  public void testComputeLayer1LogicalTopologyAggregate() {
    String c1Name = "c1";
    String c2Name = "c2";
    String c1a1Name = "c1a1";
    String c1i1aName = "c1i1a";
    String c1i1bName = "c1i1b";
    String c2a1Name = "c2a1";
    String c2i1aName = "c2i1a";
    String c2i1bName = "c2i1b";

    Configuration c1 = _cb.setHostname(c1Name).build();
    Vrf v1 = _vb.setOwner(c1).build();
    _ib.setOwner(c1).setVrf(v1);
    _ib.setName(c1i1aName).build().setChannelGroup(c1a1Name);
    _ib.setName(c1i1bName).build().setChannelGroup(c1a1Name);
    _ib.setName(c1a1Name).build();

    Configuration c2 = _cb.setHostname(c2Name).build();
    Vrf v2 = _vb.setOwner(c2).build();
    _ib.setOwner(c2).setVrf(v2);
    _ib.setName(c2a1Name).build();
    _ib.setName(c2i1aName).build().setChannelGroup(c2a1Name);
    _ib.setName(c2i1bName).build().setChannelGroup(c2a1Name);

    Map<String, Configuration> configurations = ImmutableMap.of(c1Name, c1, c2Name, c2);
    Layer1Topology layer1PhysicalTopology =
        new Layer1Topology(
            ImmutableSet.of(
                new Layer1Edge(
                    new Layer1Node(c1Name, c1i1aName), new Layer1Node(c2Name, c2i1aName)),
                new Layer1Edge(
                    new Layer1Node(c2Name, c2i1aName), new Layer1Node(c1Name, c1i1aName)),
                new Layer1Edge(
                    new Layer1Node(c1Name, c1i1bName), new Layer1Node(c2Name, c2i1bName)),
                new Layer1Edge(
                    new Layer1Node(c2Name, c2i1bName), new Layer1Node(c1Name, c1i1bName))));

    // Test that physical interface edges where the interfaces are members of aggregates get
    // aggregated in the layer-1 logical topology.
    assertThat(
        TopologyUtil.computeLayer1LogicalTopology(layer1PhysicalTopology, configurations)
            .getGraph()
            .edges(),
        containsInAnyOrder(
            new Layer1Edge(new Layer1Node(c1Name, c1a1Name), new Layer1Node(c2Name, c2a1Name)),
            new Layer1Edge(new Layer1Node(c2Name, c2a1Name), new Layer1Node(c1Name, c1a1Name))));
  }

  @Test
  public void testComputeLayer1PhysicalTopology() {
    String c1Name = "c1";
    String c2Name = "c2";
    String c1i1Name = "c1i1";
    String c1i2Name = "c1i2";
    String c2i1Name = "c2i1";
    String c2i2Name = "c2i2";

    Configuration c1 = _cb.setHostname(c1Name).build();
    Vrf v1 = _vb.setOwner(c1).build();
    _ib.setOwner(c1).setVrf(v1);
    _ib.setName(c1i1Name).build();
    _ib.setName(c1i2Name).build();

    Configuration c2 = _cb.setHostname(c2Name).build();
    Vrf v2 = _vb.setOwner(c2).build();
    _ib.setOwner(c2).setVrf(v2);
    _ib.setName(c2i1Name).build();
    _ib.setName(c2i2Name).setActive(false).build();

    Map<String, Configuration> configurations = ImmutableMap.of(c1Name, c1, c2Name, c2);
    Layer1Topology rawLayer1Topology =
        new Layer1Topology(
            ImmutableSet.of(
                new Layer1Edge(new Layer1Node(c1Name, c1i1Name), new Layer1Node(c2Name, c2i1Name)),
                new Layer1Edge(new Layer1Node(c2Name, c2i1Name), new Layer1Node(c1Name, c1i1Name)),
                new Layer1Edge(new Layer1Node(c1Name, c1i2Name), new Layer1Node(c2Name, c2i2Name)),
                new Layer1Edge(
                    new Layer1Node(c2Name, c2i2Name), new Layer1Node(c1Name, c1i2Name))));

    // inactive c2i2 should break c1i2<=>c2i2 link
    assertThat(
        TopologyUtil.computeLayer1PhysicalTopology(rawLayer1Topology, configurations)
            .getGraph()
            .edges(),
        containsInAnyOrder(
            new Layer1Edge(new Layer1Node(c1Name, c1i1Name), new Layer1Node(c2Name, c2i1Name)),
            new Layer1Edge(new Layer1Node(c2Name, c2i1Name), new Layer1Node(c1Name, c1i1Name))));
  }

  private static Layer1Topology layer1Topology(String... names) {
    checkArgument(names.length % 4 == 0);
    Set<Layer1Edge> edges = new HashSet<>();
    for (int i = 0; i < names.length; i += 4) {
      String h1 = names[i];
      String i1 = names[i + 1];
      String h2 = names[i + 2];
      String i2 = names[i + 3];
      Layer1Node n1 = new Layer1Node(h1, i1);
      Layer1Node n2 = new Layer1Node(h2, i2);
      edges.add(new Layer1Edge(n1, n2));
      edges.add(new Layer1Edge(n2, n1));
    }
    return new Layer1Topology(edges);
  }

  @Test
  public void testComputeLayer2Topology_layer1() {
    String c1Name = "c1";
    String c2Name = "c2";
    String c3Name = "c3";
    String c4Name = "c4";

    String c1i1Name = "c1i1";
    String c2i1Name = "c2i1";
    String c3i1Name = "c3i1";
    String c3i2Name = "c3i2";
    String c4i1Name = "c4i1";
    String c4i2Name = "c4i2";
    Configuration c1 = _cb.setHostname(c1Name).build();
    Vrf v1 = _vb.setOwner(c1).build();
    _ib.setOwner(c1).setVrf(v1).setActive(true);
    _ib.setName(c1i1Name).build();

    Configuration c2 = _cb.setHostname(c2Name).build();
    Vrf v2 = _vb.setOwner(c2).build();
    _ib.setOwner(c2).setVrf(v2);
    _ib.setName(c2i1Name).build();

    {
      /* c1i1 and c2i1 are non-switchport interfaces, connected in layer1. Thus, they are connected
       * in layer2
       */
      Map<String, Configuration> configs = ImmutableMap.of(c1Name, c1, c2Name, c2);
      Layer1Topology layer1Topology = layer1Topology(c1Name, c1i1Name, c2Name, c2i1Name);
      Layer2Topology layer2Topology = computeLayer2Topology(layer1Topology, configs);
      assertThat(
          "c1:i1 and c2:i1 are in the same broadcast domain",
          layer2Topology.inSameBroadcastDomain(c1Name, c1i1Name, c2Name, c2i1Name));
    }

    {
      /* c1i1 and c2i1 are not connected in layer1, but are connected to ACCESS ports on the same
       * VLAN
       */
      Configuration c3 = _cb.setHostname(c3Name).build();
      Vrf v3 = _vb.setOwner(c3).build();
      _ib.setOwner(c3).setVrf(v3);
      Interface c3i1 = _ib.setName(c3i1Name).build();
      c3i1.setSwitchport(true);
      c3i1.setSwitchportMode(SwitchportMode.ACCESS);
      c3i1.setAccessVlan(1);
      Interface c3i2 = _ib.setName(c3i2Name).build();
      c3i2.setSwitchport(true);
      c3i2.setSwitchportMode(SwitchportMode.ACCESS);
      c3i2.setAccessVlan(1);
      Map<String, Configuration> configs = ImmutableMap.of(c1Name, c1, c2Name, c2, c3Name, c3);
      Layer1Topology layer1Topology =
          layer1Topology(
              c1Name, c1i1Name, c3Name, c3i1Name, //
              c2Name, c2i1Name, c3Name, c3i2Name);
      Layer2Topology layer2Topology = computeLayer2Topology(layer1Topology, configs);
      assertThat(
          "c1:i1 and c2:i1 are in the same broadcast domain",
          layer2Topology.inSameBroadcastDomain(c1Name, c1i1Name, c2Name, c2i1Name));
    }

    {
      /* c1i1 and c2i1 are not connected in layer1, and are connected to ACCESS ports on different
       * VLANs. So they are not in the same broadcast domain
       */
      Configuration c3 = _cb.setHostname(c3Name).build();
      Vrf v3 = _vb.setOwner(c3).build();
      _ib.setOwner(c3).setVrf(v3);
      Interface c3i1 = _ib.setName(c3i1Name).build();
      c3i1.setSwitchport(true);
      c3i1.setSwitchportMode(SwitchportMode.ACCESS);
      c3i1.setAccessVlan(1);
      Interface c3i2 = _ib.setName(c3i2Name).build();
      c3i2.setSwitchport(true);
      c3i2.setSwitchportMode(SwitchportMode.ACCESS);
      c3i2.setAccessVlan(2);
      Map<String, Configuration> configs = ImmutableMap.of(c1Name, c1, c2Name, c2, c3Name, c3);
      Layer1Topology layer1Topology =
          layer1Topology(
              c1Name, c1i1Name, c3Name, c3i1Name, //
              c2Name, c2i1Name, c3Name, c3i2Name);
      Layer2Topology layer2Topology = computeLayer2Topology(layer1Topology, configs);
      assertThat(
          "c1:i1 and c2:i1 are not in the same broadcast domain",
          !layer2Topology.inSameBroadcastDomain(c1Name, c1i1Name, c2Name, c2i1Name));
    }

    {
      /* c1i1 and c2i1 are not connected in layer1, but are connected to TRUNK and ACCESS ports, and
       * the ACCESS port's VLAN is the TRUNK's native VLAN
       */
      Configuration c3 = _cb.setHostname(c3Name).build();
      Vrf v3 = _vb.setOwner(c3).build();
      _ib.setOwner(c3).setVrf(v3);
      Interface c3i1 = _ib.setName(c3i1Name).build();
      c3i1.setSwitchport(true);
      c3i1.setSwitchportMode(SwitchportMode.TRUNK);
      c3i1.setAllowedVlans(IntegerSpace.of(new SubRange(1, 3)));
      c3i1.setNativeVlan(1);
      Interface c3i2 = _ib.setName(c3i2Name).build();
      c3i2.setSwitchport(true);
      c3i2.setSwitchportMode(SwitchportMode.ACCESS);
      c3i2.setAccessVlan(1);
      Map<String, Configuration> configs = ImmutableMap.of(c1Name, c1, c2Name, c2, c3Name, c3);
      Layer1Topology layer1Topology =
          layer1Topology(
              c1Name, c1i1Name, c3Name, c3i1Name, //
              c2Name, c2i1Name, c3Name, c3i2Name);
      Layer2Topology layer2Topology = computeLayer2Topology(layer1Topology, configs);
      assertThat(
          "c1:i1 and c2:i1 are in the same broadcast domain",
          layer2Topology.inSameBroadcastDomain(c1Name, c1i1Name, c2Name, c2i1Name));
    }

    {
      /* c1i1 and c2i1 are not connected in layer1, but are connected to TRUNK and ACCESS ports, and
       * the ACCESS port's VLAN is allowed by the TRUNK, but not it's native VLAN.
       */
      Configuration c3 = _cb.setHostname(c3Name).build();
      Vrf v3 = _vb.setOwner(c3).build();
      _ib.setOwner(c3).setVrf(v3);
      Interface c3i1 = _ib.setName(c3i1Name).build();
      c3i1.setSwitchport(true);
      c3i1.setSwitchportMode(SwitchportMode.TRUNK);
      c3i1.setAllowedVlans(IntegerSpace.of(new SubRange(1, 3)));
      c3i1.setNativeVlan(1);
      Interface c3i2 = _ib.setName(c3i2Name).build();
      c3i2.setSwitchport(true);
      c3i2.setSwitchportMode(SwitchportMode.ACCESS);
      c3i2.setAccessVlan(2);
      Map<String, Configuration> configs = ImmutableMap.of(c1Name, c1, c2Name, c2, c3Name, c3);
      Layer1Topology layer1Topology =
          layer1Topology(
              c1Name, c1i1Name, c3Name, c3i1Name, //
              c2Name, c2i1Name, c3Name, c3i2Name);
      Layer2Topology layer2Topology = computeLayer2Topology(layer1Topology, configs);
      assertThat(
          "c1:i1 and c2:i1 are not in the same broadcast domain",
          !layer2Topology.inSameBroadcastDomain(c1Name, c1i1Name, c2Name, c2i1Name));
    }

    {
      /* c1i1 and c2i1 are not connected in layer1, and are connected to TRUNK and ACCESS ports with
       * incompatible VLANs.
       */
      Configuration c3 = _cb.setHostname(c3Name).build();
      Vrf v3 = _vb.setOwner(c3).build();
      _ib.setOwner(c3).setVrf(v3);
      Interface c3i1 = _ib.setName(c3i1Name).build();
      c3i1.setSwitchport(true);
      c3i1.setSwitchportMode(SwitchportMode.TRUNK);
      c3i1.setAllowedVlans(IntegerSpace.of(new SubRange(1, 3)));
      c3i1.setNativeVlan(1);
      Interface c3i2 = _ib.setName(c3i2Name).build();
      c3i2.setSwitchport(true);
      c3i2.setSwitchportMode(SwitchportMode.ACCESS);
      c3i2.setAccessVlan(4);
      Map<String, Configuration> configs = ImmutableMap.of(c1Name, c1, c2Name, c2, c3Name, c3);
      Layer1Topology layer1Topology =
          layer1Topology(
              c1Name, c1i1Name, c3Name, c3i1Name, //
              c2Name, c2i1Name, c3Name, c3i2Name);
      Layer2Topology layer2Topology = computeLayer2Topology(layer1Topology, configs);
      assertThat(
          "c1:i1 and c2:i1 are not in the same broadcast domain",
          !layer2Topology.inSameBroadcastDomain(c1Name, c1i1Name, c2Name, c2i1Name));
    }

    {
      /* c1i1 and c2i1 are not connected in layer1, and are connected to ACCESS ports with two
       * TRUNKs between them.
       */
      Configuration c3 = _cb.setHostname(c3Name).build();
      Vrf v3 = _vb.setOwner(c3).build();
      _ib.setOwner(c3).setVrf(v3);
      Interface c3i1 = _ib.setName(c3i1Name).build();
      c3i1.setSwitchport(true);
      c3i1.setSwitchportMode(SwitchportMode.ACCESS);
      c3i1.setAccessVlan(2);
      Interface c3i2 = _ib.setName(c3i2Name).build();
      c3i2.setSwitchport(true);
      c3i2.setSwitchportMode(SwitchportMode.TRUNK);
      c3i2.setAllowedVlans(IntegerSpace.of(new SubRange(1, 3)));
      c3i2.setNativeVlan(1);

      Configuration c4 = _cb.setHostname(c4Name).build();
      Vrf v4 = _vb.setOwner(c4).build();
      _ib.setOwner(c4).setVrf(v4);
      Interface c4i1 = _ib.setName(c4i1Name).build();
      c4i1.setSwitchport(true);
      c4i1.setSwitchportMode(SwitchportMode.TRUNK);
      c4i1.setAllowedVlans(IntegerSpace.of(new SubRange(1, 3)));
      c4i1.setNativeVlan(1);
      Interface c4i2 = _ib.setName(c4i2Name).build();
      c4i2.setSwitchport(true);
      c4i2.setSwitchportMode(SwitchportMode.ACCESS);
      c4i2.setAccessVlan(2);

      Map<String, Configuration> configs =
          ImmutableMap.of(c1Name, c1, c2Name, c2, c3Name, c3, c4Name, c4);
      Layer1Topology layer1Topology =
          layer1Topology(
              c1Name, c1i1Name, c3Name, c3i1Name, //
              c3Name, c3i2Name, c4Name, c4i1Name, //
              c4Name, c4i2Name, c2Name, c2i1Name);
      Layer2Topology layer2Topology = computeLayer2Topology(layer1Topology, configs);
      assertThat(
          "c1:i1 and c2:i1 are in the same broadcast domain",
          layer2Topology.inSameBroadcastDomain(c1Name, c1i1Name, c2Name, c2i1Name));
    }

    {
      /* c1i1 and c2i1 are not connected in layer1, and are connected to ACCESS ports with TRUNKs
       * between them. Not in the same broadcast domain, because the VLAN tagging doesn't line up
       */
      Configuration c3 = _cb.setHostname(c3Name).build();
      Vrf v3 = _vb.setOwner(c3).build();
      _ib.setOwner(c3).setVrf(v3);
      Interface c3i1 = _ib.setName(c3i1Name).build();
      c3i1.setSwitchport(true);
      c3i1.setSwitchportMode(SwitchportMode.ACCESS);
      c3i1.setAccessVlan(2);
      Interface c3i2 = _ib.setName(c3i2Name).build();
      c3i2.setSwitchport(true);
      c3i2.setSwitchportMode(SwitchportMode.TRUNK);
      c3i2.setAllowedVlans(IntegerSpace.of(new SubRange(1, 3)));
      c3i2.setNativeVlan(1);

      Configuration c4 = _cb.setHostname(c4Name).build();
      Vrf v4 = _vb.setOwner(c4).build();
      _ib.setOwner(c4).setVrf(v4);
      Interface c4i1 = _ib.setName(c4i1Name).build();
      c4i1.setSwitchport(true);
      c4i1.setSwitchportMode(SwitchportMode.ACCESS);
      c4i1.setAccessVlan(2);

      Interface c4i2 = _ib.setName(c4i2Name).build();
      c4i2.setSwitchport(true);
      c4i2.setSwitchportMode(SwitchportMode.ACCESS);
      c4i2.setAccessVlan(2);

      Map<String, Configuration> configs =
          ImmutableMap.of(c1Name, c1, c2Name, c2, c3Name, c3, c4Name, c4);
      Layer1Topology layer1Topology =
          layer1Topology(
              c1Name, c1i1Name, c3Name, c3i1Name, //
              c3Name, c3i2Name, c4Name, c4i2Name, //
              c4Name, c4i1Name, c2Name, c2i1Name);
      Layer2Topology layer2Topology = computeLayer2Topology(layer1Topology, configs);
      assertThat(
          "c1:i1 and c2:i1 are not in the same broadcast domain",
          !layer2Topology.inSameBroadcastDomain(c1Name, c1i1Name, c2Name, c2i1Name));
    }
  }

  @Test
  public void testComputeLayer2TopologyTaggedLayer3ToTaggedLayer3() {
    String n1Name = "n1";
    String n2Name = "n2";
    String i1Name = "i1";
    String i1aName = "i1a";
    String i1bName = "i1b";
    int i1aVlan = 10;
    int i1bVlan = 20;

    // Nodes
    Configuration n1 = _cb.setHostname(n1Name).build();
    Configuration n2 = _cb.setHostname(n2Name).build();

    // Vrfs
    Vrf v1 = _vb.setOwner(n1).build();
    Vrf v2 = _vb.setOwner(n2).build();

    // Interfaces
    _ib.setActive(true);
    // n1 interfaces
    _ib.setOwner(n1).setVrf(v1);
    _ib.setName(i1Name).setDependencies(ImmutableList.of()).setEncapsulationVlan(null).build();
    _ib.setDependencies(ImmutableList.of(new Dependency(i1Name, DependencyType.BIND)));
    _ib.setName(i1aName).setEncapsulationVlan(i1aVlan).build();
    _ib.setName(i1bName).setEncapsulationVlan(i1bVlan).build();
    // n2 interfaces
    _ib.setOwner(n2).setVrf(v2);
    _ib.setName(i1Name).setDependencies(ImmutableList.of()).setEncapsulationVlan(null).build();
    _ib.setDependencies(ImmutableList.of(new Dependency(i1Name, DependencyType.BIND)));
    _ib.setName(i1aName).setEncapsulationVlan(i1aVlan).build();
    _ib.setName(i1bName).setEncapsulationVlan(i1bVlan).build();

    // Layer1
    Layer1Topology layer1LogicalTopology =
        layer1Topology(
            n1Name, i1Name, n2Name, i1Name //
            );

    // Layer2
    Layer2Topology layer2Topology =
        computeLayer2Topology(layer1LogicalTopology, ImmutableMap.of(n1Name, n1, n2Name, n2));

    assertThat(
        "n1:i1a and n2:i1a are in the same broadcast domain",
        layer2Topology.inSameBroadcastDomain(n1Name, i1aName, n2Name, i1aName));
    assertThat(
        "n1:i1b and n2:i1b are in the same broadcast domain",
        layer2Topology.inSameBroadcastDomain(n1Name, i1bName, n2Name, i1bName));
    assertThat(
        "n1:i1a and n2:i1b are NOT in the same broadcast domain",
        !layer2Topology.inSameBroadcastDomain(n1Name, i1aName, n2Name, i1bName));
  }

  @Test
  public void testComputeLayer2TopologyTaggedLayer3ToTrunk() {
    // n1:iTagged <=> n2:iTrunkParent

    // n1:iTagged children:
    // - n1:ia - tag is 10
    // - n1:ib - tag is 20
    // - n1:ic - tag is 30

    // n2:iTrunkParent child is n2:iTrunk
    // n2:iTrunk:
    // - native 20
    // - allowed: 10,20
    // n2 has three IRB interfaces:
    // - n2:ia - vlan 10
    // - n2:ib - vlan 20
    // - n2:ic - vlan 30

    // we expect:
    // D(n1:ia)=D(n2:ia) // tags match (10=10)
    // D(n1:ib)!=D(n2:ib) // trunk does not send tag on native vlan
    // D(n1:ic)!=D(n2:ic) // trunk does not allow traffic with this tag

    String n1Name = "n1";
    String n2Name = "n2";
    String iTaggedName = "iTagged";
    String iTrunkParentName = "iTrunkParent";
    String iTrunkName = "iTrunk";
    String iaName = "ia";
    String ibName = "ib";
    String icName = "ic";
    int iaVlan = 10;
    int ibVlan = 20;
    int icVlan = 30;

    // Nodes
    Configuration n1 = _cb.setHostname(n1Name).build();
    Configuration n2 = _cb.setHostname(n2Name).build();

    // Vrfs
    Vrf v1 = _vb.setOwner(n1).build();
    Vrf v2 = _vb.setOwner(n2).build();

    // Interfaces
    _ib.setActive(true);
    // n1 interfaces
    _ib.setOwner(n1).setVrf(v1);
    // parent interface that multiplexes based on tags
    _ib.setName(iTaggedName).setDependencies(ImmutableList.of()).setEncapsulationVlan(null).build();
    _ib.setDependencies(ImmutableList.of(new Dependency(iTaggedName, DependencyType.BIND)));
    _ib.setName(iaName).setEncapsulationVlan(iaVlan).build();
    _ib.setName(ibName).setEncapsulationVlan(ibVlan).build();
    _ib.setName(icName).setEncapsulationVlan(icVlan).build();
    // n2 interfaces
    _ib.setOwner(n2).setVrf(v2);
    _ib.setDependencies(ImmutableList.of()).setEncapsulationVlan(null);
    Interface vlanA = _ib.setName(iaName).build();
    vlanA.setInterfaceType(InterfaceType.VLAN);
    vlanA.setVlan(iaVlan);
    Interface vlanB = _ib.setName(ibName).build();
    vlanB.setInterfaceType(InterfaceType.VLAN);
    vlanB.setVlan(ibVlan);
    Interface vlanC = _ib.setName(icName).build();
    vlanC.setInterfaceType(InterfaceType.VLAN);
    vlanC.setVlan(icVlan);
    _ib.setName(iTrunkParentName).build();
    Interface trunk =
        _ib.setName(iTrunkName)
            .setDependencies(
                ImmutableList.of(new Dependency(iTrunkParentName, DependencyType.BIND)))
            .build();
    trunk.setNativeVlan(ibVlan);
    trunk.setAllowedVlans(IntegerSpace.builder().including(iaVlan).including(ibVlan).build());
    trunk.setSwitchport(true);
    trunk.setSwitchportMode(SwitchportMode.TRUNK);

    // Layer1
    Layer1Topology layer1LogicalTopology =
        layer1Topology(
            n1Name, iTaggedName, n2Name, iTrunkParentName //
            );

    // Layer2
    Layer2Topology layer2Topology =
        computeLayer2Topology(layer1LogicalTopology, ImmutableMap.of(n1Name, n1, n2Name, n2));

    assertThat(
        "n1:ia and n2:ia are in the same broadcast domain",
        layer2Topology.inSameBroadcastDomain(n1Name, iaName, n2Name, iaName));
    assertThat(
        "n1:ib and n2:ib are NOT in the same broadcast domain",
        !layer2Topology.inSameBroadcastDomain(n1Name, ibName, n2Name, ibName));
    assertThat(
        "n1:ic and n2:ic are NOT in the same broadcast domain",
        !layer2Topology.inSameBroadcastDomain(n1Name, icName, n2Name, icName));
    assertThat(
        "n1:ia and n2:ib are NOT in the same broadcast domain",
        !layer2Topology.inSameBroadcastDomain(n1Name, iaName, n2Name, ibName));
    assertThat(
        "n1:ia and n2:ic are NOT in the same broadcast domain",
        !layer2Topology.inSameBroadcastDomain(n1Name, iaName, n2Name, icName));
  }

  @Test
  public void testComputeLayer3Topology() {
    String c1Name = "c1";
    String c2Name = "c2";

    String c1i1Name = "c1i1";
    String c2i1Name = "c2i1";

    Layer2Node c1i1 = new Layer2Node(c1Name, c1i1Name, null);
    Layer2Node c2i1 = new Layer2Node(c2Name, c2i1Name, null);

    Edge c1i1c2i1 = Edge.of(c1Name, c1i1Name, c2Name, c2i1Name);
    Edge c2i1c1i1 = Edge.of(c2Name, c2i1Name, c1Name, c1i1Name);

    Configuration c1 = _cb.setHostname(c1Name).build();
    Vrf v1 = _vb.setOwner(c1).build();
    _ib.setOwner(c1).setVrf(v1).setActive(true);

    Configuration c2 = _cb.setHostname(c2Name).build();
    Vrf v2 = _vb.setOwner(c2).build();
    _ib.setOwner(c2).setVrf(v2);

    InterfaceAddress p1Addr1 = new InterfaceAddress("1.0.0.1/24");
    InterfaceAddress p1Addr2 = new InterfaceAddress("1.0.0.2/24");
    InterfaceAddress p2Addr1 = new InterfaceAddress("2.0.0.1/24");

    Layer2Topology sameDomain =
        Layer2Topology.fromDomains(ImmutableList.of(ImmutableSet.of(c1i1, c2i1)));
    Layer2Topology differentDomains =
        Layer2Topology.fromDomains(ImmutableList.of(ImmutableSet.of(c1i1), ImmutableSet.of(c2i1)));

    {
      // c1i1 and c2i1 are in the same subnet and the same broadcast domain, so connected at layer3
      _ib.setOwner(c1).setVrf(v1).setName(c1i1Name).setAddress(p1Addr1).build();
      _ib.setOwner(c2).setVrf(v2).setName(c2i1Name).setAddress(p1Addr2).build();

      Map<String, Configuration> configs = ImmutableMap.of(c1Name, c1, c2Name, c2);
      Topology layer3Topology = computeLayer3Topology(sameDomain, configs);
      assertThat(layer3Topology.getEdges(), containsInAnyOrder(c1i1c2i1, c2i1c1i1));
    }

    {
      // c1i1 and c2i1 are in different subnets, and different broadcast domains. not connected
      _ib.setOwner(c1).setVrf(v1).setName(c1i1Name).setAddress(p1Addr1).build();
      _ib.setOwner(c2).setVrf(v2).setName(c2i1Name).setAddress(p2Addr1).build();

      Map<String, Configuration> configs = ImmutableMap.of(c1Name, c1, c2Name, c2);
      Topology layer3Topology = computeLayer3Topology(differentDomains, configs);
      assertThat(layer3Topology.getEdges(), empty());
    }

    {
      // c1i1 and c2i1 are in the same broadcast domain but different subnets. not connected
      _ib.setOwner(c1).setVrf(v1).setName(c1i1Name).setAddress(p1Addr1).build();
      _ib.setOwner(c2).setVrf(v2).setName(c2i1Name).setAddress(p2Addr1).build();

      Map<String, Configuration> configs = ImmutableMap.of(c1Name, c1, c2Name, c2);
      Topology layer3Topology = computeLayer3Topology(sameDomain, configs);
      assertThat(layer3Topology.getEdges(), empty());
    }

    {
      // c1i1 and c2i1 are in the same subnet but different broadcast domains. not connected
      _ib.setOwner(c1).setVrf(v1).setName(c1i1Name).setAddress(p1Addr1).build();
      _ib.setOwner(c2).setVrf(v2).setName(c2i1Name).setAddress(p1Addr2).build();

      Map<String, Configuration> configs = ImmutableMap.of(c1Name, c1, c2Name, c2);
      Topology layer3Topology = computeLayer3Topology(differentDomains, configs);
      assertThat(layer3Topology.getEdges(), empty());
    }
  }

  /**
   * Tests that inactive and blacklisted interfaces are properly included or excluded from the
   * output of {@link TopologyUtil#computeIpInterfaceOwners(Map, boolean)}
   */
  @Test
  public void testIpInterfaceOwnersActiveInclusion() {
    Map<String, Set<Interface>> nodeInterfaces =
        ImmutableMap.of(
            "node",
            ImmutableSet.of(
                iface("active", "1.1.1.1/32", true, false),
                iface("shut", "1.1.1.1/32", false, false),
                iface("active-black", "1.1.1.1/32", true, true),
                iface("shut-black", "1.1.1.1/32", false, true)));

    assertThat(
        computeIpInterfaceOwners(nodeInterfaces, true),
        equalTo(
            ImmutableMap.of(
                Ip.parse("1.1.1.1"), ImmutableMap.of("node", ImmutableSet.of("active")))));

    assertThat(
        computeIpInterfaceOwners(nodeInterfaces, false),
        equalTo(
            ImmutableMap.of(
                Ip.parse("1.1.1.1"),
                ImmutableMap.of(
                    "node", ImmutableSet.of("active", "shut", "active-black", "shut-black")))));
  }

  @Test
  public void testSynthesizeTopology_asymmetric() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Configuration c2 = cb.build();
    Interface i1 =
        nf.interfaceBuilder().setOwner(c1).setAddresses(new InterfaceAddress("1.2.3.4/24")).build();
    Interface i2 =
        nf.interfaceBuilder().setOwner(c2).setAddresses(new InterfaceAddress("1.2.3.5/28")).build();
    Topology t =
        TopologyUtil.synthesizeL3Topology(
            ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2));
    assertThat(t.getEdges(), equalTo(ImmutableSet.of(new Edge(i1, i2), new Edge(i2, i1))));
  }

  @Test
  public void testSynthesizeTopology_selfEdges() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS).build();
    Vrf v1 = nf.vrfBuilder().setOwner(c).setName("v1").build();
    Vrf v2 = nf.vrfBuilder().setOwner(c).setName("v2").build();
    Interface.Builder builder = nf.interfaceBuilder().setOwner(c);
    Interface i1 = builder.setAddresses(new InterfaceAddress("1.2.3.4/24")).setVrf(v1).build();
    Interface i2 = builder.setAddresses(new InterfaceAddress("1.2.3.5/24")).setVrf(v1).build();
    Interface i3 = builder.setAddresses(new InterfaceAddress("1.2.3.6/24")).setVrf(v2).build();
    Topology t = TopologyUtil.synthesizeL3Topology(ImmutableMap.of(c.getHostname(), c));
    assertThat(
        t.getEdges(),
        equalTo(
            ImmutableSet.of(
                new Edge(i1, i3), new Edge(i3, i1), new Edge(i2, i3), new Edge(i3, i2))));
  }

  @Test
  public void testSynthesizeTopology_asymmetricPartialOverlap() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Configuration c2 = cb.build();
    nf.interfaceBuilder().setOwner(c1).setAddresses(new InterfaceAddress("1.2.3.4/24")).build();
    nf.interfaceBuilder().setOwner(c2).setAddresses(new InterfaceAddress("1.2.3.17/28")).build();
    Topology t =
        TopologyUtil.synthesizeL3Topology(
            ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2));
    assertThat(t.getEdges(), empty());
  }

  @Test
  public void testSynthesizeTopology_asymmetricSharedIp() {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb =
        nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Configuration c1 = cb.build();
    Configuration c2 = cb.build();
    nf.interfaceBuilder().setOwner(c1).setAddresses(new InterfaceAddress("1.2.3.4/24")).build();
    nf.interfaceBuilder().setOwner(c2).setAddresses(new InterfaceAddress("1.2.3.4/28")).build();
    Topology t =
        TopologyUtil.synthesizeL3Topology(
            ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2));
    assertThat(t.getEdges(), empty());
  }
}
