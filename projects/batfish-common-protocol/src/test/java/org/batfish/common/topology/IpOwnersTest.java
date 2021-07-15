package org.batfish.common.topology;

import static org.batfish.common.topology.IpOwners.computeHsrpPriority;
import static org.batfish.common.topology.IpOwners.computeIpIfaceOwners;
import static org.batfish.common.topology.IpOwners.computeIpVrfOwners;
import static org.batfish.common.topology.IpOwners.extractHsrp;
import static org.batfish.common.topology.IpOwners.processHsrpGroups;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.hsrp.HsrpGroup;
import org.batfish.datamodel.tracking.DecrementPriority;
import org.batfish.datamodel.tracking.TrackInterface;
import org.junit.Test;

/** Tests of {@link IpOwners}. */
public class IpOwnersTest {
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
}
