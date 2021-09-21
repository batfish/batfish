package org.batfish.common.topology;

import static org.batfish.common.topology.IpOwners.computeHsrpPriority;
import static org.batfish.common.topology.IpOwners.computeInterfaceHostSubnetIps;
import static org.batfish.common.topology.IpOwners.computeIpIfaceOwners;
import static org.batfish.common.topology.IpOwners.computeIpVrfOwners;
import static org.batfish.common.topology.IpOwners.extractHsrp;
import static org.batfish.common.topology.IpOwners.extractVrrp;
import static org.batfish.common.topology.IpOwners.partitionVrrpCandidates;
import static org.batfish.common.topology.IpOwners.processHsrpGroups;
import static org.batfish.common.topology.IpOwners.processVrrpGroups;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.hsrp.HsrpGroup;
import org.batfish.datamodel.tracking.DecrementPriority;
import org.batfish.datamodel.tracking.TrackInterface;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link IpOwners}. */
public class IpOwnersTest {
  private Configuration.Builder _cb;
  private Interface.Builder _ib;
  private Vrf.Builder _vb;
  private static final Prefix P1 = Prefix.parse("1.0.0.0/8");
  private static final Prefix P2 = Prefix.parse("2.0.0.0/16");

  @Before
  public void setup() {
    NetworkFactory nf = new NetworkFactory();
    _cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    _vb = nf.vrfBuilder();
    _ib = nf.interfaceBuilder();
  }

  @Test
  public void testComputeInterfaceHostSubnetIps() {
    Configuration c1 = _cb.build();
    Map<String, Configuration> configs = ImmutableMap.of(c1.getHostname(), c1);
    Vrf vrf1 = _vb.setOwner(c1).build();
    Interface i1 =
        _ib.setOwner(c1)
            .setVrf(vrf1)
            .setAddress(ConcreteInterfaceAddress.create(P1.getFirstHostIp(), P1.getPrefixLength()))
            .build();
    Interface i2 =
        _ib.setOwner(c1)
            .setVrf(vrf1)
            .setAddress(ConcreteInterfaceAddress.create(P2.getFirstHostIp(), P2.getPrefixLength()))
            .setActive(false)
            .build();

    // Test with only active IPs.
    assertThat(
        computeInterfaceHostSubnetIps(configs, true),
        hasEntry(
            equalTo(c1.getHostname()),
            allOf(
                hasEntry(
                    equalTo(i1.getName()),
                    allOf(
                        not(containsIp(P1.getStartIp())),
                        containsIp(Ip.create(P1.getStartIp().asLong() + 1)),
                        containsIp(Ip.create(P1.getEndIp().asLong() - 1)),
                        not(containsIp(P1.getEndIp())))),
                not(hasKey(i2.getName())))));

    // Test including inactive IPs.
    assertThat(
        computeInterfaceHostSubnetIps(configs, false),
        hasEntry(
            equalTo(c1.getHostname()),
            allOf(
                hasEntry(
                    equalTo(i1.getName()),
                    allOf(
                        not(containsIp(P1.getStartIp())),
                        containsIp(Ip.create(P1.getStartIp().asLong() + 1)),
                        containsIp(Ip.create(P1.getEndIp().asLong() - 1)),
                        not(containsIp(P1.getEndIp())))),
                hasEntry(
                    equalTo(i2.getName()),
                    allOf(
                        not(containsIp(P2.getStartIp())),
                        containsIp(Ip.create(P2.getStartIp().asLong() + 1)),
                        containsIp(Ip.create(P2.getEndIp().asLong() - 1)),
                        not(containsIp(P2.getEndIp())))))));
  }

  @Test
  public void testComputeInterfaceHostSubnetIpsWithPrefixLength31() {
    Configuration c1 = _cb.build();
    Map<String, Configuration> configs = ImmutableMap.of(c1.getHostname(), c1);
    Vrf vrf1 = _vb.setOwner(c1).build();
    Prefix prefix = Prefix.parse("1.0.0.1/31");
    Interface i1 =
        _ib.setOwner(c1)
            .setVrf(vrf1)
            .setAddress(
                ConcreteInterfaceAddress.create(prefix.getStartIp(), prefix.getPrefixLength()))
            .build();
    Map<String, Map<String, IpSpace>> interfaceHostSubnetIps =
        computeInterfaceHostSubnetIps(configs, false);

    assertThat(
        interfaceHostSubnetIps,
        hasEntry(
            equalTo(c1.getHostname()),
            hasEntry(
                equalTo(i1.getName()),
                allOf(containsIp(prefix.getStartIp()), containsIp(prefix.getEndIp())))));
  }

  @Test
  public void testComputeIpIfaceOwners() {
    String node = "node";
    String vrfName = "vrf";
    Vrf vrf = new Vrf(vrfName);
    String activeName = "active";
    Ip activeIp = Ip.parse("1.1.1.1");
    String inactiveName = "inactive";
    Ip inactiveIp = Ip.parse("2.2.2.2");
    int networkBits = 31;
    Map<String, Set<Interface>> allInterfaces =
        ImmutableMap.of(
            node,
            ImmutableSet.of(
                Interface.builder()
                    .setAddress(ConcreteInterfaceAddress.create(activeIp, networkBits))
                    .setVrf(vrf)
                    .setName(activeName)
                    .build(),
                Interface.builder()
                    .setAddress(ConcreteInterfaceAddress.create(inactiveIp, networkBits))
                    .setActive(false)
                    .setName(inactiveName)
                    .setVrf(vrf)
                    .build()));
    Map<Ip, Map<String, Set<String>>> activeIps =
        ImmutableMap.of(activeIp, ImmutableMap.of(node, ImmutableSet.of(activeName)));

    // Test
    Map<Ip, Map<String, Map<String, Set<String>>>> ipIfaceOwners =
        computeIpIfaceOwners(allInterfaces, activeIps);

    assertThat(
        ipIfaceOwners,
        equalTo(
            ImmutableMap.of(
                activeIp,
                ImmutableMap.of(node, ImmutableMap.of(vrfName, ImmutableSet.of(activeName))))));
  }

  @Test
  public void testComputeIpVrfOwners() {
    String node = "node";
    String vrf1 = "vrf1";
    String vrf2 = "vrf2";
    Ip ip = Ip.parse("1.1.1.1");

    Map<Ip, Map<String, Map<String, Set<String>>>> ipIfaceOwners =
        ImmutableMap.of(
            ip,
            ImmutableMap.of(
                node,
                ImmutableMap.of(
                    vrf1, ImmutableSet.of("iface1", "iface2"), vrf2, ImmutableSet.of("iface3"))));

    Map<Ip, Map<String, Set<String>>> ipVrfOwners = computeIpVrfOwners(ipIfaceOwners);
    assertThat(
        ipVrfOwners,
        equalTo(ImmutableMap.of(ip, ImmutableMap.of(node, ImmutableSet.of(vrf1, vrf2)))));
  }

  @Test
  public void testExtractHsrp() {
    Table<Ip, Integer, Set<Interface>> groups = HashBasedTable.create();
    Interface i =
        Interface.builder()
            .setName("name")
            .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
            .build();
    extractHsrp(groups, i);
    assertTrue(groups.isEmpty());

    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    i.setHsrpGroups(
        ImmutableMap.of(
            1, HsrpGroup.builder().setIps(ImmutableSet.of(ip1, ip2)).setGroupNumber(1).build()));
    extractHsrp(groups, i);
    assertThat(groups.get(ip1, 1), equalTo(ImmutableSet.of(i)));
    assertThat(groups.get(ip2, 1), equalTo(ImmutableSet.of(i)));
  }

  @Test
  public void testProcessHsrpGroups() {
    Map<Ip, Map<String, Set<String>>> ipOwners = new HashMap<>();
    Configuration c1 =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Configuration c2 =
        Configuration.builder()
            .setHostname("c2")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();

    Table<Ip, Integer, Set<Interface>> groups = HashBasedTable.create();
    Interface i1 =
        Interface.builder()
            .setOwner(c1)
            .setName("i1")
            .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
            .build();
    Interface i2 =
        Interface.builder()
            .setOwner(c2)
            .setName("i2")
            .setAddress(ConcreteInterfaceAddress.parse("2.3.4.5/24"))
            .build();

    Ip ip = Ip.parse("1.1.1.1");
    i1.setHsrpGroups(
        ImmutableMap.of(
            1,
            HsrpGroup.builder()
                .setPriority(100)
                .setGroupNumber(1)
                .setIps(ImmutableSet.of(ip))
                .build()));
    i2.setHsrpGroups(
        ImmutableMap.of(
            1,
            HsrpGroup.builder()
                .setPriority(200)
                .setGroupNumber(1)
                .setIps(ImmutableSet.of(ip))
                .build()));
    extractHsrp(groups, i1);
    extractHsrp(groups, i2);

    // Test: expect c2/i2 to win
    processHsrpGroups(ipOwners, groups);
    assertThat(ipOwners.get(ip).get(c2.getHostname()), equalTo(ImmutableSet.of(i2.getName())));
  }

  @Test
  public void testProcessHsrpGroupPriorityTie() {
    Map<Ip, Map<String, Set<String>>> ipOwners = new HashMap<>();
    Table<Ip, Integer, Set<Interface>> groups = HashBasedTable.create();
    Ip hsrpIp = Ip.parse("1.1.1.1");
    HsrpGroup hsrpGroup =
        HsrpGroup.builder()
            .setPriority(100)
            .setGroupNumber(1)
            .setIps(ImmutableSet.of(hsrpIp))
            .build();

    Configuration c1 =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Interface i1 =
        Interface.builder()
            .setOwner(c1)
            .setName("i1")
            .setAddress(ConcreteInterfaceAddress.parse("1.1.1.2/24"))
            .build();
    i1.setHsrpGroups(ImmutableMap.of(1, hsrpGroup));

    Configuration c2 =
        Configuration.builder()
            .setHostname("c2")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Interface i2 =
        Interface.builder()
            .setOwner(c2)
            .setName("i2")
            .setAddress(ConcreteInterfaceAddress.parse("1.1.1.4/24"))
            .build();
    i2.setHsrpGroups(ImmutableMap.of(1, hsrpGroup));

    Configuration c3 =
        Configuration.builder()
            .setHostname("c3")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Interface i3 =
        Interface.builder()
            .setOwner(c3)
            .setName("i3")
            .setAddress(ConcreteInterfaceAddress.parse("1.1.1.3/24"))
            .build();
    i3.setHsrpGroups(ImmutableMap.of(1, hsrpGroup));

    extractHsrp(groups, i1);
    extractHsrp(groups, i2);
    extractHsrp(groups, i3);

    // Expect c2/i2 to win
    // Since priority is identical, highest IP address wins
    processHsrpGroups(ipOwners, groups);
    assertThat(ipOwners.get(hsrpIp).get(c2.getHostname()), equalTo(ImmutableSet.of(i2.getName())));
  }

  @Test
  public void testComputeHsrpPriority() {
    int basePriority = 100;
    int track1Decrement = 50;
    int track2Decrement = 25;

    Configuration c1 =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    HsrpGroup hsrpGroup =
        HsrpGroup.builder()
            .setPriority(basePriority)
            .setTrackActions(
                ImmutableSortedMap.of(
                    "1",
                    new DecrementPriority(track1Decrement),
                    "2",
                    new DecrementPriority(track2Decrement)))
            .setGroupNumber(1)
            .setIps(ImmutableSet.of(Ip.parse("10.10.10.1")))
            .build();
    Interface i1 =
        Interface.builder()
            .setOwner(c1)
            .setName("i1")
            .setAddress(ConcreteInterfaceAddress.parse("10.10.10.2/24"))
            .build();
    i1.setHsrpGroups(ImmutableMap.of(1, hsrpGroup));

    c1.setTrackingGroups(
        ImmutableMap.of(
            "1", new TrackInterface("i1tracked"), "2", new TrackInterface("i1trackedAlso")));
    // Tracked by track "1"
    Interface.builder()
        .setOwner(c1)
        .setName("i1tracked")
        .setAddress(ConcreteInterfaceAddress.parse("10.0.1.1/24"))
        .setActive(true)
        .build();
    // Tracked by track "2"
    Interface.builder()
        .setOwner(c1)
        .setName("i1trackedAlso")
        .setAddress(ConcreteInterfaceAddress.parse("10.0.2.1/24"))
        .setActive(false)
        .build();

    // Only track 2 is triggered, so only track 2 decrement is applied
    assertThat(computeHsrpPriority(i1, hsrpGroup), equalTo(basePriority - track2Decrement));
  }

  @Test
  public void testComputeHsrpPriorityUndefinedTrack() {
    int basePriority = 100;
    int track1Decrement = 50;

    Configuration c1 =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    HsrpGroup hsrpGroup =
        HsrpGroup.builder()
            .setPriority(basePriority)
            // Reference to undefined track method
            .setTrackActions(ImmutableSortedMap.of("1", new DecrementPriority(track1Decrement)))
            .setGroupNumber(1)
            .setIps(ImmutableSet.of(Ip.parse("10.10.10.1")))
            .build();
    Interface i1 =
        Interface.builder()
            .setOwner(c1)
            .setName("i1")
            .setAddress(ConcreteInterfaceAddress.parse("10.10.10.2/24"))
            .build();
    i1.setHsrpGroups(ImmutableMap.of(1, hsrpGroup));

    // Empty map of tracking methods
    c1.setTrackingGroups(ImmutableMap.of());
    // If VI model doesn't have references to undefined track groups we shouldn't crash
    // Also skip applying track action if that happens
    assertThat(computeHsrpPriority(i1, hsrpGroup), equalTo(basePriority));
  }

  @Test
  public void testExtractVrrp() {
    Table<ConcreteInterfaceAddress, Integer, Set<Interface>> groups = HashBasedTable.create();
    Interface i =
        Interface.builder()
            .setName("name")
            .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
            .build();
    extractVrrp(groups, i);
    assertTrue(groups.isEmpty());

    ConcreteInterfaceAddress ip1 = ConcreteInterfaceAddress.parse("1.1.1.1/28");
    i.setVrrpGroups(
        ImmutableSortedMap.of(1, VrrpGroup.builder().setVirtualAddress(ip1).setName(1).build()));
    extractVrrp(groups, i);
    assertThat(groups.get(ip1, 1), equalTo(ImmutableSet.of(i)));
  }

  @Test
  public void testProcessVrrpGroups() {
    Map<Ip, Map<String, Set<String>>> ipOwners = new HashMap<>();
    Configuration c1 =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Configuration c2 =
        Configuration.builder()
            .setHostname("c2")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();

    Table<ConcreteInterfaceAddress, Integer, Set<Interface>> groups = HashBasedTable.create();
    Interface i1 =
        Interface.builder()
            .setOwner(c1)
            .setName("i1")
            .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
            .build();
    Interface i2 =
        Interface.builder()
            .setOwner(c2)
            .setName("i2")
            .setAddress(ConcreteInterfaceAddress.parse("2.3.4.5/24"))
            .build();

    ConcreteInterfaceAddress ip = ConcreteInterfaceAddress.parse("1.1.1.1/28");
    i1.setVrrpGroups(
        ImmutableSortedMap.of(
            1, VrrpGroup.builder().setPriority(100).setName(1).setVirtualAddress(ip).build()));
    i2.setVrrpGroups(
        ImmutableSortedMap.of(
            1, VrrpGroup.builder().setPriority(200).setName(1).setVirtualAddress(ip).build()));
    extractVrrp(groups, i1);
    extractVrrp(groups, i2);

    // Test: expect c2/i2 to win
    processVrrpGroups(ipOwners, groups, null);
    assertThat(
        ipOwners.get(ip.getIp()).get(c2.getHostname()), equalTo(ImmutableSet.of(i2.getName())));
  }

  @Test
  public void testPartitionVrrpCandidates() {
    class MockL3Adjacencies implements L3Adjacencies {

      private final Map<NodeInterfacePair, NodeInterfacePair> _pairs;

      public MockL3Adjacencies(ImmutableMap<NodeInterfacePair, NodeInterfacePair> pairs) {
        _pairs = pairs;
      }

      @Override
      public boolean inSameBroadcastDomain(NodeInterfacePair i1, NodeInterfacePair i2) {
        return (_pairs.containsKey(i1) && _pairs.get(i1).equals(i2))
            || (_pairs.containsKey(i2) && _pairs.get(i2).equals(i1));
      }

      @Override
      public Optional<NodeInterfacePair> pairedPointToPointL3Interface(NodeInterfacePair iface) {
        throw new UnsupportedOperationException();
      }
    }

    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Interface i1 = Interface.builder().setName("i1").setOwner(c).build();
    Interface i2 = Interface.builder().setName("i2").setOwner(c).build();

    // common case of two interfaces in the same broadcast domain
    assertThat(
        partitionVrrpCandidates(
            ImmutableSet.of(i1, i2),
            new MockL3Adjacencies(
                ImmutableMap.of(NodeInterfacePair.of(i1), NodeInterfacePair.of(i2)))),
        equalTo(ImmutableSet.of(ImmutableSet.of(i1, i2))));

    Interface i3 = Interface.builder().setName("i3").setOwner(c).build();
    Interface i4 = Interface.builder().setName("i4").setOwner(c).build();

    // two groups of two
    assertThat(
        partitionVrrpCandidates(
            ImmutableSet.of(i1, i2, i3, i4),
            new MockL3Adjacencies(
                ImmutableMap.of(
                    NodeInterfacePair.of(i1),
                    NodeInterfacePair.of(i2),
                    NodeInterfacePair.of(i3),
                    NodeInterfacePair.of(i4)))),
        equalTo(ImmutableSet.of(ImmutableSet.of(i1, i2), ImmutableSet.of(i3, i4))));

    // one interface flying solo
    assertThat(
        partitionVrrpCandidates(
            ImmutableSet.of(i1, i2, i3),
            new MockL3Adjacencies(
                ImmutableMap.of(NodeInterfacePair.of(i1), NodeInterfacePair.of(i2)))),
        equalTo(ImmutableSet.of(ImmutableSet.of(i1, i2), ImmutableSet.of(i3))));
  }
}
