package org.batfish.common.topology;

import static com.google.common.collect.Maps.immutableEntry;
import static org.batfish.common.topology.IpOwnersBaseImpl.computeHsrpPriority;
import static org.batfish.common.topology.IpOwnersBaseImpl.computeInterfaceHostSubnetIps;
import static org.batfish.common.topology.IpOwnersBaseImpl.computeInterfaceOwners;
import static org.batfish.common.topology.IpOwnersBaseImpl.computeIpIfaceOwners;
import static org.batfish.common.topology.IpOwnersBaseImpl.computeIpVrfOwners;
import static org.batfish.common.topology.IpOwnersBaseImpl.computeNodeOwners;
import static org.batfish.common.topology.IpOwnersBaseImpl.computeVrrpPriority;
import static org.batfish.common.topology.IpOwnersBaseImpl.extractHsrp;
import static org.batfish.common.topology.IpOwnersBaseImpl.extractVrrp;
import static org.batfish.common.topology.IpOwnersBaseImpl.partitionCandidates;
import static org.batfish.common.topology.IpOwnersBaseImpl.processHsrpGroups;
import static org.batfish.common.topology.IpOwnersBaseImpl.processVrrpGroups;
import static org.batfish.datamodel.Configuration.DEFAULT_VRF_NAME;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.batfish.datamodel.matchers.MapMatchers.hasKeys;
import static org.batfish.datamodel.tracking.TrackMethods.alwaysFalse;
import static org.batfish.datamodel.tracking.TrackMethods.alwaysTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.topology.IpOwnersBaseImpl.ElectionDetails;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.NetworkConfigurations;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.TestInterface;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrrpGroup;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.hsrp.HsrpGroup;
import org.batfish.datamodel.tracking.DecrementPriority;
import org.batfish.datamodel.tracking.PreDataPlaneTrackMethodEvaluator;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link IpOwnersBaseImpl}. */
@ParametersAreNonnullByDefault
public class IpOwnersBaseImplTest {
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
            .setAdminUp(false)
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
                TestInterface.builder()
                    .setAddress(ConcreteInterfaceAddress.create(activeIp, networkBits))
                    .setVrf(vrf)
                    .setName(activeName)
                    .build(),
                TestInterface.builder()
                    .setAddress(ConcreteInterfaceAddress.create(inactiveIp, networkBits))
                    .setAdminUp(false)
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
    ConcreteInterfaceAddress sourceAddress = ConcreteInterfaceAddress.parse("1.2.3.4/24");
    Configuration c =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    {
      // no HsrpGroups
      Map<Integer, Map<NodeInterfacePair, Set<Ip>>> groups = new HashMap<>();
      Interface i = TestInterface.builder().setName("name").setAddress(sourceAddress).build();
      extractHsrp(groups, i);

      assertThat(groups, anEmptyMap());
    }
    {
      // no source-address
      Map<Integer, Map<NodeInterfacePair, Set<Ip>>> groups = new HashMap<>();
      Ip ip1 = Ip.parse("1.1.1.1");
      Interface i =
          TestInterface.builder()
              .setName("name")
              .setOwner(c)
              .setAddress(sourceAddress)
              .setHsrpGroups(
                  ImmutableSortedMap.of(
                      1, HsrpGroup.builder().setVirtualAddresses(ImmutableSet.of(ip1)).build()))
              .build();
      extractHsrp(groups, i);

      assertThat(groups, anEmptyMap());
    }
    {
      // valid HsrpGroup
      Map<Integer, Map<NodeInterfacePair, Set<Ip>>> groups = new HashMap<>();
      Ip ip1 = Ip.parse("1.1.1.1");
      Interface i =
          TestInterface.builder()
              .setName("name")
              .setOwner(c)
              .setAddress(sourceAddress)
              .setHsrpGroups(
                  ImmutableSortedMap.of(
                      1,
                      HsrpGroup.builder()
                          .setSourceAddress(sourceAddress)
                          .setVirtualAddresses(ImmutableSet.of(ip1))
                          .build()))
              .build();
      extractHsrp(groups, i);
      assertThat(
          groups.get(1), equalTo(ImmutableMap.of(NodeInterfacePair.of(i), ImmutableSet.of(ip1))));
    }
    {
      // inactive
      Map<Integer, Map<NodeInterfacePair, Set<Ip>>> groups = new HashMap<>();
      Ip ip1 = Ip.parse("1.1.1.1");
      Interface i =
          TestInterface.builder()
              .setName("name")
              .setOwner(c)
              .setAddress(sourceAddress)
              .setHsrpGroups(
                  ImmutableSortedMap.of(
                      1,
                      HsrpGroup.builder()
                          .setSourceAddress(sourceAddress)
                          .setVirtualAddresses(ImmutableSet.of(ip1))
                          .build()))
              .setAdminUp(false)
              .build();
      extractHsrp(groups, i);
      assertTrue(groups.isEmpty());
    }
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

    Map<Integer, Map<NodeInterfacePair, Set<Ip>>> groups = new HashMap<>();
    ConcreteInterfaceAddress i1SourceAddress = ConcreteInterfaceAddress.parse("1.2.3.4/24");
    Interface i1 =
        TestInterface.builder().setOwner(c1).setName("i1").setAddress(i1SourceAddress).build();
    ConcreteInterfaceAddress i2SourceAddress = ConcreteInterfaceAddress.parse("1.2.3.5/24");
    Interface i2 =
        TestInterface.builder().setOwner(c2).setName("i2").setAddress(i2SourceAddress).build();

    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip12 = Ip.parse("1.1.1.2");
    Ip ip22 = Ip.parse("2.2.2.2");

    Set<Ip> i1VirtualAddresses = ImmutableSet.of(ip1, ip12);
    i1.setHsrpGroups(
        ImmutableSortedMap.of(
            1,
            HsrpGroup.builder()
                .setPriority(100)
                .setSourceAddress(i1SourceAddress)
                .setVirtualAddresses(i1VirtualAddresses)
                .build()));
    Set<Ip> i2VirtualAddresses = ImmutableSet.of(ip1, ip22);
    i2.setHsrpGroups(
        ImmutableSortedMap.of(
            1,
            HsrpGroup.builder()
                .setPriority(200)
                .setSourceAddress(i2SourceAddress)
                .setVirtualAddresses(i2VirtualAddresses)
                .build()));
    extractHsrp(groups, i1);
    extractHsrp(groups, i2);

    // Test: expect c2/i2 to win. Only c2/i2's virtual IPs for group 1 should be owned.
    processHsrpGroups(
        ipOwners,
        groups,
        GlobalBroadcastNoPointToPoint.instance(),
        NetworkConfigurations.of(ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2)),
        PreDataPlaneTrackMethodEvaluator::new,
        null);
    assertThat(ipOwners, hasKeys(ip1, ip22));
    assertThat(ipOwners.get(ip1), hasKeys(c2.getHostname()));
    assertThat(ipOwners.get(ip22), hasKeys(c2.getHostname()));
    assertThat(ipOwners.get(ip1).get(c2.getHostname()), equalTo(ImmutableSet.of(i2.getName())));
    assertThat(ipOwners.get(ip22).get(c2.getHostname()), equalTo(ImmutableSet.of(i2.getName())));
  }

  @Test
  public void testProcessHsrpGroupsPriorityTie() {
    Map<Ip, Map<String, Set<String>>> ipOwners = new HashMap<>();
    Map<Integer, Map<NodeInterfacePair, Set<Ip>>> groups = new HashMap<>();
    Ip hsrpIp = Ip.parse("1.1.1.1");
    HsrpGroup.Builder hsrpGroup =
        HsrpGroup.builder().setPriority(100).setVirtualAddresses(ImmutableSet.of(hsrpIp));

    Configuration c1 =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Interface i1 =
        TestInterface.builder()
            .setOwner(c1)
            .setName("i1")
            .setAddress(ConcreteInterfaceAddress.parse("1.1.1.2/24"))
            .build();
    i1.setHsrpGroups(
        ImmutableMap.of(
            1, hsrpGroup.setSourceAddress(ConcreteInterfaceAddress.parse("10.0.0.1/24")).build()));

    Configuration c2 =
        Configuration.builder()
            .setHostname("c2")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Interface i2 =
        TestInterface.builder()
            .setOwner(c2)
            .setName("i2")
            .setAddress(ConcreteInterfaceAddress.parse("1.1.1.4/24"))
            .build();
    i2.setHsrpGroups(
        ImmutableMap.of(
            1, hsrpGroup.setSourceAddress(ConcreteInterfaceAddress.parse("10.0.0.2/24")).build()));

    Configuration c3 =
        Configuration.builder()
            .setHostname("c3")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Interface i3 =
        TestInterface.builder()
            .setOwner(c3)
            .setName("i3")
            .setAddress(ConcreteInterfaceAddress.parse("1.1.1.3/24"))
            .build();
    i3.setHsrpGroups(
        ImmutableMap.of(
            1, hsrpGroup.setSourceAddress(ConcreteInterfaceAddress.parse("10.0.0.3/24")).build()));

    NetworkConfigurations nc =
        NetworkConfigurations.of(
            ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2, c3.getHostname(), c3));

    extractHsrp(groups, i1);
    extractHsrp(groups, i2);
    extractHsrp(groups, i3);

    // Expect c2/i2 to win
    // Since priority is identical, highest IP address wins
    processHsrpGroups(
        ipOwners,
        groups,
        GlobalBroadcastNoPointToPoint.instance(),
        nc,
        PreDataPlaneTrackMethodEvaluator::new,
        null);
    assertThat(ipOwners.get(hsrpIp).get(c2.getHostname()), equalTo(ImmutableSet.of(i2.getName())));
  }

  @Test
  public void testHsrpPriorityApplied() {
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
    Vrf v1 = Vrf.builder().setName("v1").setOwner(c1).build();
    Vrf v2 = Vrf.builder().setName("v2").setOwner(c2).build();
    Interface i1 = TestInterface.builder().setName("i1").setVrf(v1).setOwner(c1).build();
    Interface i2 = TestInterface.builder().setName("i2").setVrf(v2).setOwner(c2).build();
    c1.setTrackingGroups(ImmutableMap.of("1", alwaysTrue()));
    HsrpGroup i1HsrpGroup =
        HsrpGroup.builder()
            .setPriority(100)
            .setTrackActions(ImmutableSortedMap.of("1", new DecrementPriority(10)))
            // higher ip would win in a tie
            .setSourceAddress(ConcreteInterfaceAddress.parse("10.10.10.100/24"))
            .setVirtualAddresses(ImmutableSet.of(Ip.parse("10.10.10.1")))
            .build();
    HsrpGroup i2HsrpGroup =
        HsrpGroup.builder()
            .setPriority(100)
            // lower ip would lose in a tie
            .setSourceAddress(ConcreteInterfaceAddress.parse("10.10.10.50/24"))
            .setVirtualAddresses(ImmutableSet.of(Ip.parse("10.10.10.1")))
            .build();
    i1.setHsrpGroups(ImmutableMap.of(1, i1HsrpGroup));
    i2.setHsrpGroups(ImmutableMap.of(1, i2HsrpGroup));

    IpOwners ipOwners = new TestIpOwners(ImmutableMap.of("c1", c1, "c2", c2));

    // i2 should win, since i1 decrements priority unconditionally.
    assertThat(
        ipOwners
            .getInterfaceOwners(true)
            .getOrDefault("c1", ImmutableMap.of())
            .getOrDefault("i1", ImmutableSet.of()),
        not(hasItem(Ip.parse("10.10.10.1"))));
    assertThat(
        ipOwners
            .getInterfaceOwners(true)
            .getOrDefault("c2", ImmutableMap.of())
            .getOrDefault("i2", ImmutableSet.of()),
        hasItem(Ip.parse("10.10.10.1")));
  }

  @Test
  public void testVrrpPriorityApplied() {
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
    Vrf v1 = Vrf.builder().setName("v1").setOwner(c1).build();
    Vrf v2 = Vrf.builder().setName("v2").setOwner(c2).build();
    Interface i1 = TestInterface.builder().setName("i1").setVrf(v1).setOwner(c1).build();
    Interface i2 = TestInterface.builder().setName("i2").setVrf(v2).setOwner(c2).build();
    c1.setTrackingGroups(ImmutableMap.of("1", alwaysTrue()));
    VrrpGroup i1VrrpGroup =
        VrrpGroup.builder()
            .setPriority(100)
            .setTrackActions(ImmutableSortedMap.of("1", new DecrementPriority(10)))
            // higher ip would win in a tie
            .setSourceAddress(ConcreteInterfaceAddress.parse("10.10.10.100/24"))
            .addVirtualAddress("i1", Ip.parse("10.10.10.1"))
            .build();
    VrrpGroup i2VrrpGroup =
        VrrpGroup.builder()
            .setPriority(100)
            // lower ip would lose in a tie
            .setSourceAddress(ConcreteInterfaceAddress.parse("10.10.10.50/24"))
            .addVirtualAddress("i2", Ip.parse("10.10.10.1"))
            .build();
    i1.setVrrpGroups(ImmutableSortedMap.of(1, i1VrrpGroup));
    i2.setVrrpGroups(ImmutableSortedMap.of(1, i2VrrpGroup));

    IpOwners ipOwners = new TestIpOwners(ImmutableMap.of("c1", c1, "c2", c2));

    // i2 should win, since i1 decrements priority unconditionally.
    assertThat(
        ipOwners
            .getInterfaceOwners(true)
            .getOrDefault("c1", ImmutableMap.of())
            .getOrDefault("i1", ImmutableSet.of()),
        not(hasItem(Ip.parse("10.10.10.1"))));
    assertThat(
        ipOwners
            .getInterfaceOwners(true)
            .getOrDefault("c2", ImmutableMap.of())
            .getOrDefault("i2", ImmutableSet.of()),
        hasItem(Ip.parse("10.10.10.1")));
  }

  @Test
  public void testComputeHsrpPriority() {
    int basePriority = 100;
    int track1Decrement = 50; // never applied
    int track2Decrement = 25; // always applied

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
                    "1", // never applied
                    new DecrementPriority(track1Decrement),
                    "2", // always applied
                    new DecrementPriority(track2Decrement)))
            .build();
    Interface i1 =
        TestInterface.builder()
            .setOwner(c1)
            .setName("i1")
            .setAddress(ConcreteInterfaceAddress.parse("10.10.10.2/24"))
            .build();
    i1.setHsrpGroups(ImmutableMap.of(1, hsrpGroup));

    c1.setTrackingGroups(
        ImmutableMap.of(
            "1", // never succeeds
            alwaysFalse(),
            "2", // always succeeds
            alwaysTrue()));

    // Only track 2 is triggered, so only track 2 decrement is applied
    assertThat(
        computeHsrpPriority(i1, hsrpGroup, 1, PreDataPlaneTrackMethodEvaluator::new, null),
        equalTo(basePriority - track2Decrement));
  }

  @Test
  public void testComputeVrrpPriority() {
    int basePriority = 100;
    int track1Decrement = 50; // never applied
    int track2Decrement = 25; // always applied

    Configuration c1 =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    VrrpGroup vrrpGroup =
        VrrpGroup.builder()
            .setPriority(basePriority)
            .setTrackActions(
                ImmutableMap.of(
                    "1", // never applied
                    new DecrementPriority(track1Decrement),
                    "2", // always applied
                    new DecrementPriority(track2Decrement)))
            .addVirtualAddress("i1", Ip.parse("10.10.10.1"))
            .build();
    Interface i1 =
        TestInterface.builder()
            .setOwner(c1)
            .setName("i1")
            .setAddress(ConcreteInterfaceAddress.parse("10.10.10.2/24"))
            .build();
    i1.setVrrpGroups(ImmutableSortedMap.of(1, vrrpGroup));

    c1.setTrackingGroups(
        ImmutableMap.of(
            "1", // never succeeds
            alwaysFalse(),
            "2", // always succeeds
            alwaysTrue()));

    // Only track 2 is triggered, so only track 2 decrement is applied
    assertThat(
        computeVrrpPriority(i1, vrrpGroup, 1, PreDataPlaneTrackMethodEvaluator::new, null),
        equalTo(basePriority - track2Decrement));
  }

  @Test
  public void testExtractVrrp() {
    ConcreteInterfaceAddress sourceAddress = ConcreteInterfaceAddress.parse("1.2.3.4/24");
    Configuration c =
        Configuration.builder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    {
      // no VrrpGroups
      Map<Integer, Map<NodeInterfacePair, Map<String, Set<Ip>>>> groups = new HashMap<>();
      Interface i = TestInterface.builder().setName("name").setAddress(sourceAddress).build();
      extractVrrp(groups, i);

      assertThat(groups, anEmptyMap());
    }
    {
      // no source-address
      Map<Integer, Map<NodeInterfacePair, Map<String, Set<Ip>>>> groups = new HashMap<>();
      Ip ip1 = Ip.parse("1.1.1.1");
      Interface i =
          TestInterface.builder()
              .setName("name")
              .setOwner(c)
              .setAddress(sourceAddress)
              .setVrrpGroups(
                  ImmutableSortedMap.of(
                      1, VrrpGroup.builder().setVirtualAddresses("name", ip1).build()))
              .build();
      extractVrrp(groups, i);

      assertThat(groups, anEmptyMap());
    }
    {
      // valid VrrpGroup
      Map<Integer, Map<NodeInterfacePair, Map<String, Set<Ip>>>> groups = new HashMap<>();
      Ip ip1 = Ip.parse("1.1.1.1");
      Interface i =
          TestInterface.builder()
              .setName("name")
              .setOwner(c)
              .setAddress(sourceAddress)
              .setVrrpGroups(
                  ImmutableSortedMap.of(
                      1,
                      VrrpGroup.builder()
                          .setSourceAddress(sourceAddress)
                          .setVirtualAddresses("name", ip1)
                          .build()))
              .build();
      extractVrrp(groups, i);
      assertThat(
          groups.get(1),
          equalTo(
              ImmutableMap.of(
                  NodeInterfacePair.of(i), ImmutableMap.of(i.getName(), ImmutableSet.of(ip1)))));
    }
    {
      // inactive
      Map<Integer, Map<NodeInterfacePair, Map<String, Set<Ip>>>> groups = new HashMap<>();
      Ip ip1 = Ip.parse("1.1.1.1");
      Interface i =
          TestInterface.builder()
              .setName("name")
              .setOwner(c)
              .setAddress(sourceAddress)
              .setVrrpGroups(
                  ImmutableSortedMap.of(
                      1,
                      VrrpGroup.builder()
                          .setSourceAddress(sourceAddress)
                          .setVirtualAddresses("name", ip1)
                          .build()))
              .setAdminUp(false)
              .build();
      extractVrrp(groups, i);
      assertTrue(groups.isEmpty());
    }
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

    Map<Integer, Map<NodeInterfacePair, Map<String, Set<Ip>>>> groups = new HashMap<>();
    ConcreteInterfaceAddress i1SourceAddress = ConcreteInterfaceAddress.parse("1.2.3.4/24");
    Interface i1 =
        TestInterface.builder().setOwner(c1).setName("i1").setAddress(i1SourceAddress).build();
    ConcreteInterfaceAddress i2SourceAddress = ConcreteInterfaceAddress.parse("1.2.3.5/24");
    Interface i2 =
        TestInterface.builder().setOwner(c2).setName("i2").setAddress(i2SourceAddress).build();
    Interface i3 =
        TestInterface.builder().setOwner(c2).setName("i3").setAddress(i2SourceAddress).build();

    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip12 = Ip.parse("1.1.1.2");
    Ip ip22 = Ip.parse("2.2.2.2");
    Ip ip3 = Ip.parse("3.3.3.3");

    Set<Ip> i1VirtualAddresses = ImmutableSet.of(ip1, ip12);
    i1.setVrrpGroups(
        ImmutableSortedMap.of(
            1,
            VrrpGroup.builder()
                .setPriority(100)
                .setSourceAddress(i1SourceAddress)
                .setVirtualAddresses(i1.getName(), i1VirtualAddresses)
                .build()));
    Set<Ip> i2VirtualAddresses = ImmutableSet.of(ip1, ip22);
    i2.setVrrpGroups(
        ImmutableSortedMap.of(
            1,
            VrrpGroup.builder()
                .setPriority(200)
                .setSourceAddress(i2SourceAddress)
                .setVirtualAddresses(i2.getName(), i2VirtualAddresses)
                .addVirtualAddress(i3.getName(), ip3)
                .build()));
    extractVrrp(groups, i1);
    extractVrrp(groups, i2);

    // Test: expect c2/i2 to win. Only c2/i2's virtual IPs for vrid 1 should be owned.
    processVrrpGroups(
        ipOwners,
        groups,
        GlobalBroadcastNoPointToPoint.instance(),
        NetworkConfigurations.of(ImmutableMap.of(c1.getHostname(), c1, c2.getHostname(), c2)),
        PreDataPlaneTrackMethodEvaluator::new,
        null);
    assertThat(ipOwners, hasKeys(ip1, ip22, ip3));
    assertThat(ipOwners.get(ip1), hasKeys(c2.getHostname()));
    assertThat(ipOwners.get(ip22), hasKeys(c2.getHostname()));
    assertThat(ipOwners.get(ip1).get(c2.getHostname()), equalTo(ImmutableSet.of(i2.getName())));
    assertThat(ipOwners.get(ip22).get(c2.getHostname()), equalTo(ImmutableSet.of(i2.getName())));
    assertThat(ipOwners.get(ip3).get(c2.getHostname()), equalTo(ImmutableSet.of(i3.getName())));
  }

  @Test
  public void testPartitionCandidates() {
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

    NodeInterfacePair i1 = NodeInterfacePair.of("c1", "i1");
    // using same name on purpose
    NodeInterfacePair i2 = NodeInterfacePair.of("c2", "i1");

    // common case of two interfaces in the same broadcast domain
    Set<NodeInterfacePair> sameDomain = ImmutableSet.of(i1, i2);
    assertThat(
        partitionCandidates(sameDomain, new MockL3Adjacencies(ImmutableMap.of(i1, i2))),
        contains(containsInAnyOrder(i1, i2)));

    NodeInterfacePair i3 = NodeInterfacePair.of("c1", "i3");
    NodeInterfacePair i4 = NodeInterfacePair.of("c1", "i4");

    // two groups of two
    Set<NodeInterfacePair> twoGroupsOfTwo = ImmutableSet.of(i1, i2, i3, i4);
    assertThat(
        partitionCandidates(twoGroupsOfTwo, new MockL3Adjacencies(ImmutableMap.of(i1, i2, i3, i4))),
        containsInAnyOrder(containsInAnyOrder(i1, i2), containsInAnyOrder(i3, i4)));

    // one interface flying solo
    Set<NodeInterfacePair> oneSolo = ImmutableSet.of(i1, i2, i3);
    assertThat(
        partitionCandidates(oneSolo, new MockL3Adjacencies(ImmutableMap.of(i1, i2))),
        containsInAnyOrder(containsInAnyOrder(i1, i2), containsInAnyOrder(i3)));
  }

  @Test
  public void testComputeInterfaceOwners() {
    Map<Ip, Map<String, Set<String>>> deviceOwnedIps =
        ImmutableMap.of(
            Ip.ZERO,
            ImmutableMap.of("c1", ImmutableSet.of("i1")),
            Ip.MAX,
            ImmutableMap.of("c1", ImmutableSet.of("i1")));

    assertThat(
        computeInterfaceOwners(deviceOwnedIps),
        equalTo(ImmutableMap.of("c1", ImmutableMap.of("i1", ImmutableSet.of(Ip.ZERO, Ip.MAX)))));
  }

  @Test
  public void testComputeNodeOwners() {
    Map<Ip, Map<String, Set<String>>> deviceOwnedIps =
        ImmutableMap.of(
            Ip.ZERO, ImmutableMap.of("c1", ImmutableSet.of("i1"), "c2", ImmutableSet.of("i2")));

    assertThat(
        computeNodeOwners(deviceOwnedIps),
        equalTo(ImmutableMap.of(Ip.ZERO, ImmutableSet.of("c1", "c2"))));
  }

  private static final class TestIpOwners extends IpOwnersBaseImpl {
    protected TestIpOwners(Map<String, Configuration> configurations) {
      super(
          configurations,
          GlobalBroadcastNoPointToPoint.instance(),
          PreDataPlaneTrackMethodEvaluator::new,
          false);
    }
  }

  private static final class TestIpOwnersRecordElections extends IpOwnersBaseImpl {

    protected TestIpOwnersRecordElections(Map<String, Configuration> configurations) {
      super(
          configurations,
          GlobalBroadcastNoPointToPoint.instance(),
          PreDataPlaneTrackMethodEvaluator::new,
          true);
    }
  }

  @Test
  public void testRecordElections() {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    c.setTrackingGroups(
        ImmutableMap.of(
            "true", alwaysTrue(),
            "false", alwaysFalse()));
    Vrf v = Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    Interface i1 = TestInterface.builder().setName("i1").setOwner(c).setVrf(v).build();
    Interface i2 = TestInterface.builder().setName("i2").setOwner(c).setVrf(v).build();
    i1.setHsrpGroups(
        ImmutableMap.of(
            1,
            HsrpGroup.builder()
                .setSourceAddress(ConcreteInterfaceAddress.create(Ip.parse("10.0.0.1"), 24))
                .setTrackActions(ImmutableSortedMap.of("true", new DecrementPriority(10)))
                .setVirtualAddresses(ImmutableSet.of(Ip.parse("10.0.1.1")))
                .setPriority(100)
                .build()));
    i1.setVrrpGroups(
        ImmutableSortedMap.of(
            2,
            VrrpGroup.builder()
                .setSourceAddress(ConcreteInterfaceAddress.create(Ip.parse("10.0.0.1"), 24))
                .setTrackActions(ImmutableSortedMap.of("false", new DecrementPriority(20)))
                .addVirtualAddress("i2", Ip.parse("10.0.2.1"))
                .setPriority(100)
                .build()));
    i2.setHsrpGroups(
        ImmutableMap.of(
            1,
            HsrpGroup.builder()
                .setSourceAddress(ConcreteInterfaceAddress.create(Ip.parse("10.0.0.2"), 24))
                .setTrackActions(ImmutableSortedMap.of("false", new DecrementPriority(10)))
                .setVirtualAddresses(ImmutableSet.of(Ip.parse("10.0.1.1")))
                .setPriority(100)
                .build()));
    i2.setVrrpGroups(
        ImmutableSortedMap.of(
            2,
            VrrpGroup.builder()
                .setSourceAddress(ConcreteInterfaceAddress.create(Ip.parse("10.0.0.2"), 24))
                .setTrackActions(ImmutableSortedMap.of("true", new DecrementPriority(20)))
                .addVirtualAddress("i2", Ip.parse("10.0.2.1"))
                .setPriority(100)
                .build()));
    IpOwners ipOwners = new TestIpOwnersRecordElections(ImmutableMap.of(c.getHostname(), c));
    NodeInterfacePair ni1 = NodeInterfacePair.of(i1);
    NodeInterfacePair ni2 = NodeInterfacePair.of(i2);

    {
      ElectionDetails details = ipOwners.getHsrpElectionDetails();
      assertNotNull(details);
      assertThat(
          details.getActualPriorities(),
          equalTo(ImmutableMap.of(ni1, ImmutableMap.of(1, 90), ni2, ImmutableMap.of(1, 100))));
      assertThat(
          details.getWinnerByCandidate(),
          equalTo(ImmutableMap.of(ni1, ImmutableMap.of(1, ni2), ni2, ImmutableMap.of(1, ni2))));
      assertThat(
          details.getCandidatesByCandidate(),
          equalTo(
              ImmutableMap.of(
                  ni1,
                  ImmutableMap.of(1, ImmutableSet.of(ni1, ni2)),
                  ni2,
                  ImmutableMap.of(1, ImmutableSet.of(ni1, ni2)))));
      assertThat(
          details.getSuccessfulTracks(),
          equalTo(
              ImmutableMap.of(
                  ni1,
                  ImmutableMap.of(
                      1,
                      ImmutableMap.of(
                          "true", immutableEntry(alwaysTrue(), new DecrementPriority(10)))),
                  ni2,
                  ImmutableMap.of(1, ImmutableMap.of()))));
      assertThat(
          details.getFailedTracks(),
          equalTo(
              ImmutableMap.of(
                  ni1,
                  ImmutableMap.of(1, ImmutableMap.of()),
                  ni2,
                  ImmutableMap.of(
                      1,
                      ImmutableMap.of(
                          "false", immutableEntry(alwaysFalse(), new DecrementPriority(10)))))));
    }
    {
      ElectionDetails details = ipOwners.getVrrpElectionDetails();
      assertNotNull(details);
      assertThat(
          details.getActualPriorities(),
          equalTo(ImmutableMap.of(ni1, ImmutableMap.of(2, 100), ni2, ImmutableMap.of(2, 80))));
      assertThat(
          details.getWinnerByCandidate(),
          equalTo(ImmutableMap.of(ni1, ImmutableMap.of(2, ni1), ni2, ImmutableMap.of(2, ni1))));
      assertThat(
          details.getCandidatesByCandidate(),
          equalTo(
              ImmutableMap.of(
                  ni1,
                  ImmutableMap.of(2, ImmutableSet.of(ni1, ni2)),
                  ni2,
                  ImmutableMap.of(2, ImmutableSet.of(ni1, ni2)))));
      assertThat(
          details.getSuccessfulTracks(),
          equalTo(
              ImmutableMap.of(
                  ni1,
                  ImmutableMap.of(2, ImmutableMap.of()),
                  ni2,
                  ImmutableMap.of(
                      2,
                      ImmutableMap.of(
                          "true", immutableEntry(alwaysTrue(), new DecrementPriority(20)))))));
      assertThat(
          details.getFailedTracks(),
          equalTo(
              ImmutableMap.of(
                  ni1,
                  ImmutableMap.of(
                      2,
                      ImmutableMap.of(
                          "false", immutableEntry(alwaysFalse(), new DecrementPriority(20)))),
                  ni2,
                  ImmutableMap.of(2, ImmutableMap.of()))));
    }
  }

  @Test
  public void testRecordElectionPrioritiesSingleCandidate() {
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    c.setTrackingGroups(
        ImmutableMap.of(
            "true", alwaysTrue(),
            "false", alwaysFalse()));
    Vrf v = Vrf.builder().setOwner(c).setName(DEFAULT_VRF_NAME).build();
    Interface i1 = TestInterface.builder().setName("i1").setOwner(c).setVrf(v).build();
    i1.setHsrpGroups(
        ImmutableMap.of(
            1,
            HsrpGroup.builder()
                .setSourceAddress(ConcreteInterfaceAddress.create(Ip.parse("10.0.0.1"), 24))
                .setTrackActions(ImmutableSortedMap.of("true", new DecrementPriority(10)))
                .setVirtualAddresses(ImmutableSet.of(Ip.parse("10.0.1.1")))
                .setPriority(100)
                .build()));
    i1.setVrrpGroups(
        ImmutableSortedMap.of(
            2,
            VrrpGroup.builder()
                .setSourceAddress(ConcreteInterfaceAddress.create(Ip.parse("10.0.0.1"), 24))
                .setTrackActions(ImmutableSortedMap.of("true", new DecrementPriority(20)))
                .addVirtualAddress("i1", Ip.parse("10.0.2.1"))
                .setPriority(100)
                .build()));
    IpOwners ipOwners = new TestIpOwnersRecordElections(ImmutableMap.of(c.getHostname(), c));
    NodeInterfacePair ni1 = NodeInterfacePair.of(i1);
    {
      ElectionDetails details = ipOwners.getHsrpElectionDetails();
      assertNotNull(details);
      assertThat(
          details.getActualPriorities(), equalTo(ImmutableMap.of(ni1, ImmutableMap.of(1, 90))));
      assertThat(
          details.getWinnerByCandidate(), equalTo(ImmutableMap.of(ni1, ImmutableMap.of(1, ni1))));
      assertThat(
          details.getCandidatesByCandidate(),
          equalTo(ImmutableMap.of(ni1, ImmutableMap.of(1, ImmutableSet.of(ni1)))));
    }
    {
      ElectionDetails details = ipOwners.getVrrpElectionDetails();
      assertNotNull(details);
      assertThat(
          details.getActualPriorities(), equalTo(ImmutableMap.of(ni1, ImmutableMap.of(2, 80))));
      assertThat(
          details.getWinnerByCandidate(), equalTo(ImmutableMap.of(ni1, ImmutableMap.of(2, ni1))));
      assertThat(
          details.getCandidatesByCandidate(),
          equalTo(ImmutableMap.of(ni1, ImmutableMap.of(2, ImmutableSet.of(ni1)))));
    }
  }
}
