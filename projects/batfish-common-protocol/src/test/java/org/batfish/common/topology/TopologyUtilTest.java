package org.batfish.common.topology;

import static org.batfish.common.topology.TopologyUtil.computeIpInterfaceOwners;
import static org.batfish.common.topology.TopologyUtil.computeLayer2Topology;
import static org.batfish.common.topology.TopologyUtil.computeLayer3Topology;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Edge;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
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
  public void testComputeLayer1Topology() {
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
        TopologyUtil.computeLayer1Topology(rawLayer1Topology, configurations).getGraph().edges(),
        containsInAnyOrder(
            new Layer1Edge(new Layer1Node(c1Name, c1i1Name), new Layer1Node(c2Name, c2i1Name)),
            new Layer1Edge(new Layer1Node(c2Name, c2i1Name), new Layer1Node(c1Name, c1i1Name))));
  }

  private static Layer1Topology layer1Topology(String... names) {
    Preconditions.checkArgument(names.length % 4 == 0);
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
      c3i1.setAllowedVlans(IntegerSpace.of(new SubRange(0, 3)));
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
      c3i1.setAllowedVlans(IntegerSpace.of(new SubRange(0, 3)));
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
      c3i1.setAllowedVlans(IntegerSpace.of(new SubRange(0, 3)));
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
      /* c1i1 and c2i1 are not connected in layer1, and are connected to ACCESS ports with a TRUNK
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
      c3i2.setAllowedVlans(IntegerSpace.of(new SubRange(0, 3)));
      c3i2.setNativeVlan(1);

      Configuration c4 = _cb.setHostname(c4Name).build();
      Vrf v4 = _vb.setOwner(c4).build();
      _ib.setOwner(c4).setVrf(v4);
      Interface c4i1 = _ib.setName(c4i1Name).build();
      c4i1.setSwitchport(true);
      c4i1.setSwitchportMode(SwitchportMode.ACCESS);
      c4i1.setAccessVlan(2);

      Map<String, Configuration> configs =
          ImmutableMap.of(c1Name, c1, c2Name, c2, c3Name, c3, c4Name, c4);
      Layer1Topology layer1Topology =
          layer1Topology(
              c1Name, c1i1Name, c3Name, c3i1Name, //
              c3Name, c3i2Name, c4Name, c4i1Name, //
              c4Name, c4i1Name, c2Name, c2i1Name);
      Layer2Topology layer2Topology = computeLayer2Topology(layer1Topology, configs);
      assertThat(
          "c1:i1 and c2:i1 are not in the same broadcast domain",
          !layer2Topology.inSameBroadcastDomain(c1Name, c1i1Name, c2Name, c2i1Name));
    }
  }

  @Test
  public void testComputeLayer3Topology() {
    String c1Name = "c1";
    String c2Name = "c2";
    String c3Name = "c3";

    String c1i1Name = "c1i1";
    String c1i2Name = "c1i2";
    String c1i3Name = "c1i3";
    String c1i4Name = "c1i4";
    String c1i5Name = "c1i5";
    String c1i6Name = "c1i6";
    String c2i1Name = "c2i1";
    String c2i2Name = "c2i2";
    String c2i3Name = "c2i3";
    String c2i4Name = "c2i4";
    String c3i5Name = "c3i5";
    String c3i6Name = "c3i6";
    String vlan1Name = "Vlan1";
    String vlan2Name = "Vlan2";
    String vlan3Name = "Vlan3";
    String vlan4Name = "Vlan4";

    Prefix p1 = Prefix.parse("10.0.0.0/31");
    Prefix p3 = Prefix.parse("10.0.0.2/31");

    int vlanPrefixLength = 24;
    Ip c1Vlan1Ip = Ip.parse("10.10.1.1");
    Ip c1Vlan2Ip = Ip.parse("10.10.2.1");
    Ip c1Vlan3Ip = Ip.parse("10.10.3.1");
    Ip c1Vlan4Ip = Ip.parse("10.10.4.1");
    Ip c2Vlan1Ip = Ip.parse("10.10.1.2");
    Ip c2Vlan2Ip = Ip.parse("10.10.2.2");
    Ip c2Vlan3Ip = Ip.parse("10.10.3.2");
    Ip c2Vlan4Ip = Ip.parse("10.10.4.2");
    Ip c3i5Ip = Ip.parse("10.10.1.3");
    Ip c3i6Ip = Ip.parse("10.10.4.3");

    Configuration c1 = _cb.setHostname(c1Name).build();
    Vrf v1 = _vb.setOwner(c1).build();
    _ib.setOwner(c1).setVrf(v1);
    _ib.setName(c1i1Name)
        .setAddresses(new InterfaceAddress(p1.getStartIp(), p1.getPrefixLength()))
        .build();
    _ib.setName(c1i2Name).setAddresses(null).build();
    _ib.setName(c1i3Name)
        .setAddresses(new InterfaceAddress(p3.getStartIp(), p3.getPrefixLength()))
        .build();
    Interface c1i4 = _ib.setName(c1i4Name).setAddresses(null).build();
    c1i4.setSwitchport(true);
    c1i4.setSwitchportMode(SwitchportMode.TRUNK);
    c1i4.setAllowedVlans(IntegerSpace.of(new SubRange(0, 3)));
    c1i4.setNativeVlan(0);
    Interface c1i5 = _ib.setName(c1i5Name).build();
    c1i5.setSwitchport(true);
    c1i5.setSwitchportMode(SwitchportMode.ACCESS);
    c1i5.setAccessVlan(1);
    Interface c1i6 = _ib.setName(c1i6Name).build();
    c1i6.setSwitchport(true);
    c1i6.setSwitchportMode(SwitchportMode.TRUNK);
    c1i6.setAllowedVlans(IntegerSpace.of(4));
    c1i6.setNativeVlan(4);
    Interface c1Vlan1 =
        _ib.setName(vlan1Name)
            .setAddresses(new InterfaceAddress(c1Vlan1Ip, vlanPrefixLength))
            .build();
    c1Vlan1.setInterfaceType(InterfaceType.VLAN);
    c1Vlan1.setVlan(1);
    Interface c1Vlan2 =
        _ib.setName(vlan2Name)
            .setAddresses(new InterfaceAddress(c1Vlan2Ip, vlanPrefixLength))
            .build();
    c1Vlan2.setInterfaceType(InterfaceType.VLAN);
    c1Vlan2.setVlan(2);
    Interface c1Vlan3 =
        _ib.setName(vlan3Name)
            .setAddresses(new InterfaceAddress(c1Vlan3Ip, vlanPrefixLength))
            .build();
    c1Vlan3.setInterfaceType(InterfaceType.VLAN);
    c1Vlan3.setVlan(3);
    Interface c1Vlan4 =
        _ib.setName(vlan4Name)
            .setAddresses(new InterfaceAddress(c1Vlan4Ip, vlanPrefixLength))
            .build();
    c1Vlan4.setInterfaceType(InterfaceType.VLAN);
    c1Vlan4.setVlan(4);

    Configuration c2 = _cb.setHostname(c2Name).build();
    Vrf v2 = _vb.setOwner(c2).build();
    _ib.setOwner(c2).setVrf(v2);
    _ib.setName(c2i1Name)
        .setAddresses(new InterfaceAddress(p1.getEndIp(), p1.getPrefixLength()))
        .build();
    _ib.setName(c2i2Name).setAddresses(null).build();
    _ib.setName(c2i3Name)
        .setAddresses(new InterfaceAddress(p3.getStartIp(), p3.getPrefixLength()))
        .build();
    Interface c2i4 = _ib.setName(c2i4Name).setAddresses(null).build();
    c2i4.setSwitchport(true);
    c2i4.setSwitchportMode(SwitchportMode.TRUNK);
    c2i4.setAllowedVlans(IntegerSpace.of(new SubRange(1, 2)));
    c2i4.setNativeVlan(0);
    Interface c2Vlan1 =
        _ib.setName(vlan1Name)
            .setAddresses(new InterfaceAddress(c2Vlan1Ip, vlanPrefixLength))
            .build();
    c2Vlan1.setInterfaceType(InterfaceType.VLAN);
    c2Vlan1.setVlan(1);
    Interface c2Vlan2 =
        _ib.setName(vlan2Name)
            .setAddresses(new InterfaceAddress(c2Vlan2Ip, vlanPrefixLength))
            .build();
    c2Vlan2.setInterfaceType(InterfaceType.VLAN);
    c2Vlan2.setVlan(2);
    Interface c2Vlan3 =
        _ib.setName(vlan3Name)
            .setAddresses(new InterfaceAddress(c2Vlan3Ip, vlanPrefixLength))
            .build();
    c2Vlan3.setInterfaceType(InterfaceType.VLAN);
    c2Vlan3.setVlan(3);
    Interface c2Vlan4 =
        _ib.setName(vlan4Name)
            .setAddresses(new InterfaceAddress(c2Vlan4Ip, vlanPrefixLength))
            .build();
    c2Vlan4.setInterfaceType(InterfaceType.VLAN);
    c2Vlan4.setVlan(4);

    Configuration c3 = _cb.setHostname(c3Name).build();
    Vrf v3 = _vb.setOwner(c3).build();
    _ib.setOwner(c3).setVrf(v3);
    _ib.setName(c3i5Name).setAddresses(new InterfaceAddress(c3i5Ip, vlanPrefixLength)).build();
    _ib.setName(c3i6Name).setAddresses(new InterfaceAddress(c3i6Ip, vlanPrefixLength)).build();

    SortedMap<String, Configuration> configurations =
        ImmutableSortedMap.of(c1Name, c1, c2Name, c2, c3Name, c3);

    /*
     * c1i1 <=> c2i1 is link with proper l3 addressing.
     *
     * c1i2 <=> c2i2 is link with no l3 addressing.
     *
     * c1i3 <=> c2i3 is link with same subnet but same IP (conflict).
     *
     * c1i4 <=> c2i4 is trunk connection with:
     * - native vlan 0
     * - allowed vlans 1,2 on both c1 and c2
     * - allowed vlan 3 on c1 only
     *
     * c1i5 <=> c3i5 is link with:
     * - access vlan 1 on c1i5
     * - ordinary l3 addressing on c3i5
     *
     * c1i6 <=> c3i6 is link with:
     * - trunking with native vlan 4 on c1
     * - ordinary l3 addressing on c3i6
     */

    // all layer1 edges should make it into layer2, plus self and transitive edges for switchports
    Layer2Topology layer2Topology =
        Layer2Topology.fromDomains(
            ImmutableList.of(
                ImmutableSet.of(
                    new Layer2Node(c1Name, c1i1Name, null), new Layer2Node(c2Name, c2i1Name, null)),
                ImmutableSet.of(
                    new Layer2Node(c1Name, vlan1Name, null),
                    new Layer2Node(c2Name, vlan1Name, null),
                    new Layer2Node(c3Name, c3i5Name, null)),
                ImmutableSet.of(
                    new Layer2Node(c1Name, vlan2Name, null),
                    new Layer2Node(c2Name, vlan2Name, null)),
                ImmutableSet.of(
                    new Layer2Node(c1Name, vlan4Name, null),
                    new Layer2Node(c3Name, c3i6Name, null))));

    Topology layer3Topology = computeLayer3Topology(layer2Topology, configurations);

    // layer3 consists of layer2 interfaces with l3 addressing, plus Vlan/IRB interfaces
    assertThat(
        layer3Topology.getEdges(),
        containsInAnyOrder(
            Edge.of(c1Name, c1i1Name, c2Name, c2i1Name),
            Edge.of(c2Name, c2i1Name, c1Name, c1i1Name),
            Edge.of(c1Name, vlan1Name, c2Name, vlan1Name),
            Edge.of(c2Name, vlan1Name, c1Name, vlan1Name),
            Edge.of(c1Name, vlan2Name, c2Name, vlan2Name),
            Edge.of(c2Name, vlan2Name, c1Name, vlan2Name),
            Edge.of(c1Name, vlan1Name, c3Name, c3i5Name),
            Edge.of(c3Name, c3i5Name, c1Name, vlan1Name),
            Edge.of(c1Name, vlan4Name, c3Name, c3i6Name),
            Edge.of(c3Name, c3i6Name, c1Name, vlan4Name),
            Edge.of(c2Name, vlan1Name, c3Name, c3i5Name),
            Edge.of(c3Name, c3i5Name, c2Name, vlan1Name)));
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
