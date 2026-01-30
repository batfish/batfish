package org.batfish.representation.f5_bigip;

import static org.batfish.representation.f5_bigip.F5BigipConfiguration.toAddressGroup;
import static org.batfish.representation.f5_bigip.F5BigipConfiguration.toRouteFilterList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.GeneratedRefBookUtils;
import org.batfish.referencelibrary.GeneratedRefBookUtils.BookType;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

/** Tests for {@link F5BigipConfiguration} */
public class F5BigipConfigurationTest {

  /**
   * Tests if {@link F5BigipConfiguration#toVendorIndependentConfigurations()} includes reference
   * book for its pools
   */
  @Test
  public void testPoolReferenceBooks() {
    Pool p1 = new Pool("p1");
    F5BigipConfiguration f5Config = new F5BigipConfiguration();
    f5Config.setHostname("node");
    f5Config.getPools().put(p1.getName(), p1);

    Configuration configuration =
        Iterables.getOnlyElement(f5Config.toVendorIndependentConfigurations());

    String refbookName = GeneratedRefBookUtils.getName("node", BookType.PoolAddresses);
    assertThat(
        configuration.getGeneratedReferenceBooks().get(refbookName),
        equalTo(
            ReferenceBook.builder(refbookName)
                .setAddressGroups(ImmutableList.of(toAddressGroup(p1)))
                .build()));
  }

  @Test
  public void testToAddressGroupVirtualAddress() {
    VirtualAddress v1 = new VirtualAddress("v1");
    v1.setAddress(Ip.parse("1.1.1.1"));
    assertThat(
        toAddressGroup(v1),
        equalTo(new AddressGroup(ImmutableSortedSet.of("1.1.1.1"), v1.getName())));
  }

  @Test
  public void testToAddressGroupVirtualAddressNullAddress() {
    VirtualAddress v1 = new VirtualAddress("v1");
    assertThat(
        toAddressGroup(v1), equalTo(new AddressGroup(ImmutableSortedSet.of(), v1.getName())));
  }

  @Test
  public void testToAddressGroupVirtualAddressMask() {
    VirtualAddress v1 = new VirtualAddress("v1");
    v1.setAddress(Ip.parse("1.1.1.1"));
    v1.setMask(Ip.parse("255.255.255.254"));
    assertThat(
        toAddressGroup(v1),
        equalTo(new AddressGroup(ImmutableSortedSet.of("1.1.1.1", "1.1.1.0/31"), v1.getName())));
  }

  @Test
  public void testToAddressGroupPool() {
    PoolMember m1 = new PoolMember("m1", null, 1);
    m1.setAddress(Ip.parse("1.1.1.1"));

    // null address
    PoolMember m2 = new PoolMember("m2", null, 1);

    Pool p1 = new Pool("p1");
    p1.getMembers().put(m1.getName(), m1);
    p1.getMembers().put(m2.getName(), m2);

    assertThat(
        toAddressGroup(p1),
        equalTo(new AddressGroup(ImmutableSortedSet.of("1.1.1.1"), p1.getName())));
  }

  @Test
  public void testToAddressGroupPoolNoMember() {
    Pool p1 = new Pool("p1");
    assertThat(
        toAddressGroup(p1), equalTo(new AddressGroup(ImmutableSortedSet.of(), p1.getName())));
  }

  @Test
  public void testToAddressGroupPoolNoAddresses() {
    PoolMember m1 = new PoolMember("m1", null, 1);
    Pool p1 = new Pool("p1");
    p1.getMembers().put(m1.getName(), m1);

    assertThat(
        toAddressGroup(p1), equalTo(new AddressGroup(ImmutableSortedSet.of(), p1.getName())));
  }

  /**
   * Tests if {@link F5BigipConfiguration#toVendorIndependentConfigurations()} includes reference
   * book for its virtual addresses
   */
  @Test
  public void testVirtualAddressReferenceBooks() {
    VirtualAddress v1 = new VirtualAddress("v1");

    F5BigipConfiguration f5Config = new F5BigipConfiguration();
    f5Config.setHostname("node");
    f5Config.getVirtualAddresses().put(v1.getName(), v1);

    Configuration configuration =
        Iterables.getOnlyElement(f5Config.toVendorIndependentConfigurations());

    String refbookName = GeneratedRefBookUtils.getName("node", BookType.VirtualAddresses);
    assertThat(
        configuration.getGeneratedReferenceBooks().get(refbookName),
        equalTo(
            ReferenceBook.builder(refbookName)
                .setAddressGroups(ImmutableList.of(toAddressGroup(v1)))
                .build()));
  }

  /** Test the case of a vlan with multiple selves and addresses */
  @Test
  public void testMultipleVlanAddresses() {
    F5BigipConfiguration f5Config = new F5BigipConfiguration();
    f5Config.setHostname("node");
    f5Config.setVendor(ConfigurationFormat.F5_BIGIP_STRUCTURED);

    String vlanName = "vlan10";
    f5Config.getInterfaces().put("vlan10", new Interface(vlanName));

    Self s1 = new Self("s1");
    s1.setVlan(vlanName);
    ConcreteInterfaceAddress a1 = ConcreteInterfaceAddress.parse("1.1.1.1/24");
    s1.setAddress(a1);

    Self s2 = new Self("s2");
    s2.setVlan(vlanName);
    ConcreteInterfaceAddress a2 = ConcreteInterfaceAddress.parse("1.1.1.2/24");
    s2.setAddress(a2);

    f5Config.getSelves().putAll(ImmutableMap.of(s1.getName(), s1, s2.getName(), s2));

    Configuration configuration =
        Iterables.getOnlyElement(f5Config.toVendorIndependentConfigurations());

    org.batfish.datamodel.Interface iface = configuration.getAllInterfaces().get(vlanName);

    assertThat(iface.getAllAddresses(), equalTo(ImmutableSet.of(a1, a2)));
  }

  /** Check that vendorStructureId is set when ACL is converted to route filter list */
  @Test
  public void testToRouterFilterList_AccessList_vendorStructureId() {
    AccessList acl = new AccessList("name");
    RouteFilterList rfl = toRouteFilterList(acl, "file");
    assertThat(
        rfl.getVendorStructureId(),
        equalTo(
            new VendorStructureId(
                "file", F5BigipStructureType.ACCESS_LIST.getDescription(), "name")));
  }

  /** Check that vendorStructureId is set when prefix list is converted to route filter list */
  @Test
  public void testToRouterFilterList_prefixList_vendorStructureId() {
    PrefixList plist = new PrefixList("name");
    RouteFilterList rfl = toRouteFilterList(plist, new Warnings(), "file");
    assertThat(
        rfl.getVendorStructureId(),
        equalTo(
            new VendorStructureId(
                "file", F5BigipStructureType.PREFIX_LIST.getDescription(), "name")));
  }

  /** Tests for {@link SnmpCommunity} */
  @Test
  public void testSnmpCommunityBuilder() {
    SnmpCommunity community =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource("192.168.1.1")
            .build();
    assertThat(community.getName(), equalTo("testName"));
    assertThat(community.getCommunityName(), equalTo("testCommunity"));
    assertThat(community.getSource(), equalTo("192.168.1.1"));
  }

  @Test
  public void testSnmpCommunityBuilderWithNullSource() {
    SnmpCommunity community =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource(null)
            .build();
    assertThat(community.getName(), equalTo("testName"));
    assertThat(community.getCommunityName(), equalTo("testCommunity"));
    assertThat(community.getSource(), equalTo(null));
  }

  @Test(expected = NullPointerException.class)
  public void testSnmpCommunityBuilderNullName() {
    SnmpCommunity.builder().setCommunityName("testCommunity").setSource("192.168.1.1").build();
  }

  @Test(expected = NullPointerException.class)
  public void testSnmpCommunityBuilderNullCommunityName() {
    SnmpCommunity.builder().setName("testName").setSource("192.168.1.1").build();
  }

  @Test
  public void testSnmpCommunityEquals() {
    SnmpCommunity c1 =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource("192.168.1.1")
            .build();
    SnmpCommunity c2 =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource("192.168.1.1")
            .build();

    // Same instance
    assertThat(c1.equals(c1), equalTo(true));

    // Different instances, equal values
    assertThat(c1.equals(c2), equalTo(true));
    assertThat(c2.equals(c1), equalTo(true));

    // Different values
    SnmpCommunity c3 =
        SnmpCommunity.builder()
            .setName("otherName")
            .setCommunityName("testCommunity")
            .setSource("192.168.1.1")
            .build();
    assertThat(c1.equals(c3), equalTo(false));

    // Null
    assertThat(c1.equals(null), equalTo(false));

    // Wrong type
    assertThat(c1.equals("string"), equalTo(false));
  }

  @Test
  public void testSnmpCommunityEqualsNullFields() {
    SnmpCommunity c1 =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource(null)
            .build();
    SnmpCommunity c2 =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource(null)
            .build();

    assertThat(c1.equals(c2), equalTo(true));

    SnmpCommunity c3 =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource("192.168.1.1")
            .build();

    assertThat(c1.equals(c3), equalTo(false));
  }

  @Test
  public void testSnmpCommunityHashCode() {
    SnmpCommunity c1 =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource("192.168.1.1")
            .build();
    SnmpCommunity c2 =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource("192.168.1.1")
            .build();

    assertThat(c1.hashCode(), equalTo(c2.hashCode()));
  }

  @Test
  public void testSnmpCommunityHashCodeNullFields() {
    SnmpCommunity c1 =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource(null)
            .build();
    SnmpCommunity c2 =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource(null)
            .build();

    assertThat(c1.hashCode(), equalTo(c2.hashCode()));
  }

  @Test
  public void testSnmpCommunityToString() {
    SnmpCommunity community =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource("192.168.1.1")
            .build();

    String result = community.toString();
    assertThat(result.contains("name: \"testName\""), equalTo(true));
    assertThat(result.contains("community-name: testCommunity"), equalTo(true));
    assertThat(result.contains("source: 192.168.1.1"), equalTo(true));
    assertThat(result.contains("\n}"), equalTo(true));
  }

  @Test
  public void testSnmpCommunityToStringWithDaml() {
    SnmpCommunity community =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource("192.168.1.1")
            .build();

    String result = community.toString(true);
    assertThat(result.contains("name: \"testName\""), equalTo(true));
    assertThat(result.contains("community-name: testCommunity"), equalTo(true));
    assertThat(result.contains("source: 192.168.1.1"), equalTo(true));
  }

  @Test
  public void testSnmpCommunityToStringWithoutDaml() {
    SnmpCommunity community =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource("192.168.1.1")
            .build();

    String result = community.toString(false);
    assertThat(result.contains("name: \"testName\""), equalTo(true));
    assertThat(result.contains("community-name: testCommunity"), equalTo(true));
    assertThat(result.contains("source: 192.168.1.1"), equalTo(true));
  }

  @Test
  public void testSnmpCommunityToBuilder() {
    SnmpCommunity original =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource("192.168.1.1")
            .build();

    SnmpCommunity copy = original.toBuilder().build();

    assertThat(original.equals(copy), equalTo(true));
    assertThat(original.hashCode(), equalTo(copy.hashCode()));
  }

  @Test
  public void testSnmpCommunityToBuilderWithModification() {
    SnmpCommunity original =
        SnmpCommunity.builder()
            .setName("testName")
            .setCommunityName("testCommunity")
            .setSource("192.168.1.1")
            .build();

    SnmpCommunity modified = original.toBuilder().setSource("192.168.1.2").build();

    assertThat(modified.getSource(), equalTo("192.168.1.2"));
    assertThat(original.getSource(), equalTo("192.168.1.1"));
  }

  /** Tests for {@link SnmpDiskMonitor} */
  @Test
  public void testSnmpDiskMonitorBuilder() {
    SnmpDiskMonitor monitor =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(100).build();
    assertThat(monitor.getName(), equalTo("disk1"));
    assertThat(monitor.getPath(), equalTo("/var/log"));
    assertThat(monitor.getMinSpace(), equalTo(100));
  }

  @Test
  public void testSnmpDiskMonitorBuilderWithNullMinSpace() {
    SnmpDiskMonitor monitor =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(null).build();
    assertThat(monitor.getName(), equalTo("disk1"));
    assertThat(monitor.getPath(), equalTo("/var/log"));
    assertThat(monitor.getMinSpace(), equalTo(null));
  }

  @Test(expected = NullPointerException.class)
  public void testSnmpDiskMonitorBuilderNullName() {
    SnmpDiskMonitor.builder().setPath("/var/log").setMinSpace(100).build();
  }

  @Test(expected = NullPointerException.class)
  public void testSnmpDiskMonitorBuilderNullPath() {
    SnmpDiskMonitor.builder().setName("disk1").setMinSpace(100).build();
  }

  @Test
  public void testSnmpDiskMonitorEquals() {
    SnmpDiskMonitor m1 =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(100).build();
    SnmpDiskMonitor m2 =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(100).build();

    // Same instance
    assertThat(m1.equals(m1), equalTo(true));

    // Different instances, equal values
    assertThat(m1.equals(m2), equalTo(true));
    assertThat(m2.equals(m1), equalTo(true));

    // Different values
    SnmpDiskMonitor m3 =
        SnmpDiskMonitor.builder().setName("disk2").setPath("/var/log").setMinSpace(100).build();
    assertThat(m1.equals(m3), equalTo(false));

    // Null
    assertThat(m1.equals(null), equalTo(false));

    // Wrong type
    assertThat(m1.equals("string"), equalTo(false));
  }

  @Test
  public void testSnmpDiskMonitorEqualsNullFields() {
    SnmpDiskMonitor m1 =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(null).build();
    SnmpDiskMonitor m2 =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(null).build();

    assertThat(m1.equals(m2), equalTo(true));

    SnmpDiskMonitor m3 =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(100).build();

    assertThat(m1.equals(m3), equalTo(false));
  }

  @Test
  public void testSnmpDiskMonitorHashCode() {
    SnmpDiskMonitor m1 =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(100).build();
    SnmpDiskMonitor m2 =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(100).build();

    assertThat(m1.hashCode(), equalTo(m2.hashCode()));
  }

  @Test
  public void testSnmpDiskMonitorHashCodeNullFields() {
    SnmpDiskMonitor m1 =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(null).build();
    SnmpDiskMonitor m2 =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(null).build();

    assertThat(m1.hashCode(), equalTo(m2.hashCode()));
  }

  @Test
  public void testSnmpDiskMonitorToString() {
    SnmpDiskMonitor monitor =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(100).build();

    String result = monitor.toString();
    assertThat(result.contains("name: \"disk1\""), equalTo(true));
    assertThat(result.contains("path: /var/log"), equalTo(true));
    assertThat(result.contains("minspace: 100"), equalTo(true));
  }

  @Test
  public void testSnmpDiskMonitorToStringWithDaml() {
    SnmpDiskMonitor monitor =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(100).build();

    String result = monitor.toString(true);
    assertThat(result.contains("name: \"disk1\""), equalTo(true));
    assertThat(result.contains("path: /var/log"), equalTo(true));
    assertThat(result.contains("minspace: 100"), equalTo(true));
  }

  @Test
  public void testSnmpDiskMonitorToStringWithoutDaml() {
    SnmpDiskMonitor monitor =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(100).build();

    String result = monitor.toString(false);
    assertThat(result.contains("name: \"disk1\""), equalTo(true));
    assertThat(result.contains("path: /var/log"), equalTo(true));
    assertThat(result.contains("minspace: 100"), equalTo(true));
  }

  @Test
  public void testSnmpDiskMonitorToBuilder() {
    SnmpDiskMonitor original =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(100).build();

    SnmpDiskMonitor copy = original.toBuilder().build();

    assertThat(original.equals(copy), equalTo(true));
    assertThat(original.hashCode(), equalTo(copy.hashCode()));
  }

  @Test
  public void testSnmpDiskMonitorToBuilderWithModification() {
    SnmpDiskMonitor original =
        SnmpDiskMonitor.builder().setName("disk1").setPath("/var/log").setMinSpace(100).build();

    SnmpDiskMonitor modified = original.toBuilder().setMinSpace(200).build();

    assertThat(modified.getMinSpace(), equalTo(200));
    assertThat(original.getMinSpace(), equalTo(100));
  }

  /** Tests for {@link SnmpProcessMonitor} */
  @Test
  public void testSnmpProcessMonitorBuilder() {
    SnmpProcessMonitor monitor =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(10)
            .build();
    assertThat(monitor.getName(), equalTo("process1"));
    assertThat(monitor.getProcess(), equalTo("httpd"));
    assertThat(monitor.getMaxProcesses(), equalTo(10));
  }

  @Test
  public void testSnmpProcessMonitorBuilderWithNullMaxProcesses() {
    SnmpProcessMonitor monitor =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(null)
            .build();
    assertThat(monitor.getName(), equalTo("process1"));
    assertThat(monitor.getProcess(), equalTo("httpd"));
    assertThat(monitor.getMaxProcesses(), equalTo(null));
  }

  @Test(expected = NullPointerException.class)
  public void testSnmpProcessMonitorBuilderNullName() {
    SnmpProcessMonitor.builder().setProcess("httpd").setMaxProcesses(10).build();
  }

  @Test(expected = NullPointerException.class)
  public void testSnmpProcessMonitorBuilderNullProcess() {
    SnmpProcessMonitor.builder().setName("process1").setMaxProcesses(10).build();
  }

  @Test
  public void testSnmpProcessMonitorEquals() {
    SnmpProcessMonitor m1 =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(10)
            .build();
    SnmpProcessMonitor m2 =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(10)
            .build();

    // Same instance
    assertThat(m1.equals(m1), equalTo(true));

    // Different instances, equal values
    assertThat(m1.equals(m2), equalTo(true));
    assertThat(m2.equals(m1), equalTo(true));

    // Different values
    SnmpProcessMonitor m3 =
        SnmpProcessMonitor.builder()
            .setName("process2")
            .setProcess("httpd")
            .setMaxProcesses(10)
            .build();
    assertThat(m1.equals(m3), equalTo(false));

    // Null
    assertThat(m1.equals(null), equalTo(false));

    // Wrong type
    assertThat(m1.equals("string"), equalTo(false));
  }

  @Test
  public void testSnmpProcessMonitorEqualsNullFields() {
    SnmpProcessMonitor m1 =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(null)
            .build();
    SnmpProcessMonitor m2 =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(null)
            .build();

    assertThat(m1.equals(m2), equalTo(true));

    SnmpProcessMonitor m3 =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(10)
            .build();

    assertThat(m1.equals(m3), equalTo(false));
  }

  @Test
  public void testSnmpProcessMonitorHashCode() {
    SnmpProcessMonitor m1 =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(10)
            .build();
    SnmpProcessMonitor m2 =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(10)
            .build();

    assertThat(m1.hashCode(), equalTo(m2.hashCode()));
  }

  @Test
  public void testSnmpProcessMonitorHashCodeNullFields() {
    SnmpProcessMonitor m1 =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(null)
            .build();
    SnmpProcessMonitor m2 =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(null)
            .build();

    assertThat(m1.hashCode(), equalTo(m2.hashCode()));
  }

  @Test
  public void testSnmpProcessMonitorToString() {
    SnmpProcessMonitor monitor =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(10)
            .build();

    String result = monitor.toString();
    assertThat(result.contains("name: \"process1\""), equalTo(true));
    assertThat(result.contains("process: httpd"), equalTo(true));
    assertThat(result.contains("max-processes: 10"), equalTo(true));
  }

  @Test
  public void testSnmpProcessMonitorToStringWithDaml() {
    SnmpProcessMonitor monitor =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(10)
            .build();

    String result = monitor.toString(true);
    assertThat(result.contains("name: \"process1\""), equalTo(true));
    assertThat(result.contains("process: httpd"), equalTo(true));
    assertThat(result.contains("max-processes: 10"), equalTo(true));
  }

  @Test
  public void testSnmpProcessMonitorToStringWithoutDaml() {
    SnmpProcessMonitor monitor =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(10)
            .build();

    String result = monitor.toString(false);
    assertThat(result.contains("name: \"process1\""), equalTo(true));
    assertThat(result.contains("process: httpd"), equalTo(true));
    assertThat(result.contains("max-processes: 10"), equalTo(true));
  }

  @Test
  public void testSnmpProcessMonitorToBuilder() {
    SnmpProcessMonitor original =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(10)
            .build();

    SnmpProcessMonitor copy = original.toBuilder().build();

    assertThat(original.equals(copy), equalTo(true));
    assertThat(original.hashCode(), equalTo(copy.hashCode()));
  }

  @Test
  public void testSnmpProcessMonitorToBuilderWithModification() {
    SnmpProcessMonitor original =
        SnmpProcessMonitor.builder()
            .setName("process1")
            .setProcess("httpd")
            .setMaxProcesses(10)
            .build();

    SnmpProcessMonitor modified = original.toBuilder().setMaxProcesses(20).build();

    assertThat(modified.getMaxProcesses(), equalTo(20));
    assertThat(original.getMaxProcesses(), equalTo(10));
  }

  // ==================== FirewallRule Tests ====================

  @Test
  public void testFirewallRuleBuilder_withAllFields() {
    FirewallRule rule =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    assertThat(rule.getName(), equalTo("rule1"));
    assertThat(rule.getAction(), equalTo("accept"));
    assertThat(rule.getIpProtocol(), equalTo("tcp"));
  }

  @Test
  public void testFirewallRuleBuilder_withNullAction() {
    FirewallRule rule = FirewallRule.builder().setName("rule1").setAction(null).build();

    assertThat(rule.getName(), equalTo("rule1"));
    assertThat(rule.getAction(), equalTo(null));
  }

  @Test
  public void testFirewallRuleBuilder_withNullIpProtocol() {
    FirewallRule rule =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol(null).build();

    assertThat(rule.getName(), equalTo("rule1"));
    assertThat(rule.getAction(), equalTo("accept"));
    assertThat(rule.getIpProtocol(), equalTo(null));
  }

  @Test(expected = NullPointerException.class)
  public void testFirewallRuleBuilder_withNullName() {
    FirewallRule.builder().setAction("accept").setIpProtocol("tcp").build();
  }

  @Test
  public void testFirewallRule_getAction_withNullAction() {
    FirewallRule rule = FirewallRule.builder().setName("rule1").build();
    assertThat(rule.getAction(), equalTo(null));
  }

  @Test
  public void testFirewallRule_getIpProtocol_withNullProtocol() {
    FirewallRule rule = FirewallRule.builder().setName("rule1").build();
    assertThat(rule.getIpProtocol(), equalTo(null));
  }

  @Test
  public void testFirewallRule_toBuilder() {
    FirewallRule original =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    FirewallRule rebuilt = original.toBuilder().build();

    assertThat(rebuilt, equalTo(original));
    assertThat(rebuilt.getName(), equalTo("rule1"));
    assertThat(rebuilt.getAction(), equalTo("accept"));
    assertThat(rebuilt.getIpProtocol(), equalTo("tcp"));
  }

  @Test
  public void testFirewallRule_toBuilder_withModifications() {
    FirewallRule original =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    FirewallRule modified = original.toBuilder().setAction("drop").build();

    assertThat(modified.getName(), equalTo("rule1"));
    assertThat(modified.getAction(), equalTo("drop"));
    assertThat(modified.getIpProtocol(), equalTo("tcp"));
  }

  @Test
  public void testFirewallRule_equals_sameObject() {
    FirewallRule rule =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    //noinspection EqualsWithItself
    assertThat(rule.equals(rule), equalTo(true));
  }

  @Test
  public void testFirewallRule_equals_equalObjects() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();
    FirewallRule rule2 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    assertThat(rule1.equals(rule2), equalTo(true));
    assertThat(rule1.hashCode(), equalTo(rule2.hashCode()));
  }

  @Test
  public void testFirewallRule_equals_differentName() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();
    FirewallRule rule2 =
        FirewallRule.builder().setName("rule2").setAction("accept").setIpProtocol("tcp").build();

    assertThat(rule1.equals(rule2), equalTo(false));
  }

  @Test
  public void testFirewallRule_equals_differentAction() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();
    FirewallRule rule2 =
        FirewallRule.builder().setName("rule1").setAction("drop").setIpProtocol("tcp").build();

    assertThat(rule1.equals(rule2), equalTo(false));
  }

  @Test
  public void testFirewallRule_equals_differentProtocol() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();
    FirewallRule rule2 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("udp").build();

    assertThat(rule1.equals(rule2), equalTo(false));
  }

  @Test
  public void testFirewallRule_equals_nullObject() {
    FirewallRule rule =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    //noinspection ObjectEqualsNull
    assertThat(rule.equals(null), equalTo(false));
  }

  @Test
  public void testFirewallRule_equals_differentType() {
    FirewallRule rule =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    assertThat(rule.equals("not a FirewallRule"), equalTo(false));
  }

  @Test
  public void testFirewallRule_hashCode() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();
    FirewallRule rule2 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    assertThat(rule1.hashCode(), equalTo(rule2.hashCode()));
  }

  @Test
  public void testFirewallRule_hashCode_withNulls() {
    FirewallRule rule1 = FirewallRule.builder().setName("rule1").build();
    FirewallRule rule2 = FirewallRule.builder().setName("rule1").build();

    assertThat(rule1.hashCode(), equalTo(rule2.hashCode()));
  }

  @Test
  public void testFirewallRule_toString_withAllFields() {
    FirewallRule rule =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    String expected = "name: \"rule1\",\n" + "action: accept,\n" + "ip-protocol: tcp";
    assertThat(rule.toString(), equalTo(expected));
  }

  @Test
  public void testFirewallRule_toString_withNullAction() {
    FirewallRule rule = FirewallRule.builder().setName("rule1").setIpProtocol("tcp").build();

    String expected = "name: \"rule1\",\n" + "ip-protocol: tcp";
    assertThat(rule.toString(), equalTo(expected));
  }

  @Test
  public void testFirewallRule_toString_withNullProtocol() {
    FirewallRule rule = FirewallRule.builder().setName("rule1").setAction("accept").build();

    String expected = "name: \"rule1\",\n" + "action: accept";
    assertThat(rule.toString(), equalTo(expected));
  }

  @Test
  public void testFirewallRule_toString_withOnlyName() {
    FirewallRule rule = FirewallRule.builder().setName("rule1").build();

    String expected = "name: \"rule1\"";
    assertThat(rule.toString(), equalTo(expected));
  }

  @Test
  public void testFirewallRule_differentActions() {
    String[] actions = {"accept", "drop", "reject", "continue"};

    for (String action : actions) {
      FirewallRule rule =
          FirewallRule.builder().setName("rule_" + action).setAction(action).build();
      assertThat(rule.getAction(), equalTo(action));
    }
  }

  // ==================== FirewallRuleList Tests ====================

  @Test
  public void testFirewallRuleListBuilder_withName() {
    FirewallRuleList list = FirewallRuleList.builder().setName("list1").build();

    assertThat(list.getName(), equalTo("list1"));
    assertThat(list.getRules().isEmpty(), equalTo(true));
  }

  @Test
  public void testFirewallRuleListBuilder_withRules() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();
    FirewallRule rule2 =
        FirewallRule.builder().setName("rule2").setAction("drop").setIpProtocol("udp").build();

    FirewallRuleList list =
        FirewallRuleList.builder().setName("list1").addRule(rule1).addRule(rule2).build();

    assertThat(list.getName(), equalTo("list1"));
    assertThat(list.getRules().size(), equalTo(2));
    assertThat(list.getRules().get(0), equalTo(rule1));
    assertThat(list.getRules().get(1), equalTo(rule2));
  }

  @Test(expected = NullPointerException.class)
  public void testFirewallRuleListBuilder_withNullName() {
    FirewallRuleList.builder().build();
  }

  @Test
  public void testFirewallRuleList_getRules_empty() {
    FirewallRuleList list = FirewallRuleList.builder().setName("list1").build();

    assertThat(list.getRules().isEmpty(), equalTo(true));
  }

  @Test
  public void testFirewallRuleList_getRules_withRules() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    FirewallRuleList list = FirewallRuleList.builder().setName("list1").addRule(rule1).build();

    assertThat(list.getRules().size(), equalTo(1));
    assertThat(list.getRules().get(0), equalTo(rule1));
  }

  @Test
  public void testFirewallRuleList_toBuilder_empty() {
    FirewallRuleList original = FirewallRuleList.builder().setName("list1").build();

    FirewallRuleList rebuilt = original.toBuilder().build();

    assertThat(rebuilt, equalTo(original));
    assertThat(rebuilt.getName(), equalTo("list1"));
    assertThat(rebuilt.getRules().isEmpty(), equalTo(true));
  }

  @Test
  public void testFirewallRuleList_toBuilder_withRules() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();
    FirewallRule rule2 =
        FirewallRule.builder().setName("rule2").setAction("drop").setIpProtocol("udp").build();

    FirewallRuleList original =
        FirewallRuleList.builder().setName("list1").addRule(rule1).addRule(rule2).build();

    FirewallRuleList rebuilt = original.toBuilder().build();

    assertThat(rebuilt, equalTo(original));
    assertThat(rebuilt.getName(), equalTo("list1"));
    assertThat(rebuilt.getRules().size(), equalTo(2));
    assertThat(rebuilt.getRules().get(0), equalTo(rule1));
    assertThat(rebuilt.getRules().get(1), equalTo(rule2));
  }

  @Test
  public void testFirewallRuleList_toBuilder_withModifications() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    FirewallRuleList original = FirewallRuleList.builder().setName("list1").addRule(rule1).build();

    FirewallRule rule2 =
        FirewallRule.builder().setName("rule2").setAction("drop").setIpProtocol("udp").build();

    FirewallRuleList modified = original.toBuilder().addRule(rule2).build();

    assertThat(modified.getName(), equalTo("list1"));
    assertThat(modified.getRules().size(), equalTo(2));
    assertThat(modified.getRules().get(0), equalTo(rule1));
    assertThat(modified.getRules().get(1), equalTo(rule2));
  }

  @Test
  public void testFirewallRuleList_equals_sameObject() {
    FirewallRuleList list =
        FirewallRuleList.builder()
            .setName("list1")
            .addRule(
                FirewallRule.builder()
                    .setName("rule1")
                    .setAction("accept")
                    .setIpProtocol("tcp")
                    .build())
            .build();

    //noinspection EqualsWithItself
    assertThat(list.equals(list), equalTo(true));
  }

  @Test
  public void testFirewallRuleList_equals_equalObjects() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    FirewallRuleList list1 = FirewallRuleList.builder().setName("list1").addRule(rule1).build();
    FirewallRuleList list2 = FirewallRuleList.builder().setName("list1").addRule(rule1).build();

    assertThat(list1.equals(list2), equalTo(true));
    assertThat(list1.hashCode(), equalTo(list2.hashCode()));
  }

  @Test
  public void testFirewallRuleList_equals_differentName() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    FirewallRuleList list1 = FirewallRuleList.builder().setName("list1").addRule(rule1).build();
    FirewallRuleList list2 = FirewallRuleList.builder().setName("list2").addRule(rule1).build();

    assertThat(list1.equals(list2), equalTo(false));
  }

  @Test
  public void testFirewallRuleList_equals_differentRules() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();
    FirewallRule rule2 =
        FirewallRule.builder().setName("rule2").setAction("drop").setIpProtocol("udp").build();

    FirewallRuleList list1 = FirewallRuleList.builder().setName("list1").addRule(rule1).build();
    FirewallRuleList list2 = FirewallRuleList.builder().setName("list1").addRule(rule2).build();

    assertThat(list1.equals(list2), equalTo(false));
  }

  @Test
  public void testFirewallRuleList_equals_nullObject() {
    FirewallRuleList list =
        FirewallRuleList.builder()
            .setName("list1")
            .addRule(
                FirewallRule.builder()
                    .setName("rule1")
                    .setAction("accept")
                    .setIpProtocol("tcp")
                    .build())
            .build();

    //noinspection ObjectEqualsNull
    assertThat(list.equals(null), equalTo(false));
  }

  @Test
  public void testFirewallRuleList_equals_differentType() {
    FirewallRuleList list =
        FirewallRuleList.builder()
            .setName("list1")
            .addRule(
                FirewallRule.builder()
                    .setName("rule1")
                    .setAction("accept")
                    .setIpProtocol("tcp")
                    .build())
            .build();

    assertThat(list.equals("not a FirewallRuleList"), equalTo(false));
  }

  @Test
  public void testFirewallRuleList_hashCode() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();

    FirewallRuleList list1 = FirewallRuleList.builder().setName("list1").addRule(rule1).build();
    FirewallRuleList list2 = FirewallRuleList.builder().setName("list1").addRule(rule1).build();

    assertThat(list1.hashCode(), equalTo(list2.hashCode()));
  }

  @Test
  public void testFirewallRuleList_hashCode_empty() {
    FirewallRuleList list1 = FirewallRuleList.builder().setName("list1").build();
    FirewallRuleList list2 = FirewallRuleList.builder().setName("list1").build();

    assertThat(list1.hashCode(), equalTo(list2.hashCode()));
  }

  @Test
  public void testFirewallRuleList_toString_emptyRules() {
    FirewallRuleList list = FirewallRuleList.builder().setName("list1").build();

    String expected = "name: \"list1\"";
    assertThat(list.toString(), equalTo(expected));
  }

  @Test
  public void testFirewallRuleList_toString_withRules() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();
    FirewallRule rule2 =
        FirewallRule.builder().setName("rule2").setAction("drop").setIpProtocol("udp").build();

    FirewallRuleList list =
        FirewallRuleList.builder().setName("list1").addRule(rule1).addRule(rule2).build();

    String result = list.toString();
    assertThat(result, org.hamcrest.Matchers.containsString("name: \"list1\""));
    assertThat(result, org.hamcrest.Matchers.containsString("rules: {"));
    assertThat(result, org.hamcrest.Matchers.containsString("name: \"rule1\""));
    assertThat(result, org.hamcrest.Matchers.containsString("name: \"rule2\""));
  }

  @Test
  public void testFirewallRuleList_ruleOrdering() {
    FirewallRule rule1 =
        FirewallRule.builder().setName("rule1").setAction("accept").setIpProtocol("tcp").build();
    FirewallRule rule2 =
        FirewallRule.builder().setName("rule2").setAction("drop").setIpProtocol("udp").build();
    FirewallRule rule3 =
        FirewallRule.builder().setName("rule3").setAction("reject").setIpProtocol("icmp").build();

    FirewallRuleList list =
        FirewallRuleList.builder()
            .setName("list1")
            .addRule(rule1)
            .addRule(rule2)
            .addRule(rule3)
            .build();

    assertThat(list.getRules().size(), equalTo(3));
    assertThat(list.getRules().get(0), equalTo(rule1));
    assertThat(list.getRules().get(1), equalTo(rule2));
    assertThat(list.getRules().get(2), equalTo(rule3));
  }

  @Test
  public void testFirewallRule_multipleProtocols() {
    String[] protocols = {"tcp", "udp", "icmp", "ip", "sctp", "ospf", "bgp"};

    for (String protocol : protocols) {
      FirewallRule rule =
          FirewallRule.builder()
              .setName("rule_" + protocol)
              .setAction("accept")
              .setIpProtocol(protocol)
              .build();

      assertThat(rule.getIpProtocol(), equalTo(protocol));
    }
  }
}
