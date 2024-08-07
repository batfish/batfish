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
}
