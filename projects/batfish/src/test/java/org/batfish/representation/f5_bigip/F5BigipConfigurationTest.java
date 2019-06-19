package org.batfish.representation.f5_bigip;

import static org.batfish.representation.f5_bigip.F5BigipConfiguration.REFBOOK_SOURCE_POOLS;
import static org.batfish.representation.f5_bigip.F5BigipConfiguration.REFBOOK_SOURCE_VIRTUAL_ADDRESSES;
import static org.batfish.representation.f5_bigip.F5BigipConfiguration.toAddressGroup;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Names;
import org.batfish.datamodel.vendor_family.f5_bigip.Pool;
import org.batfish.datamodel.vendor_family.f5_bigip.PoolMember;
import org.batfish.datamodel.vendor_family.f5_bigip.VirtualAddress;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.ReferenceBook;
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

    String refbookName = Names.generatedReferenceBook("node", REFBOOK_SOURCE_POOLS);
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
    PoolMember m1 = new PoolMember("m1", null, 0);
    m1.setAddress(Ip.parse("1.1.1.1"));

    // null address
    PoolMember m2 = new PoolMember("m2", null, 0);

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
    PoolMember m1 = new PoolMember("m1", null, 0);
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

    String refbookName = Names.generatedReferenceBook("node", REFBOOK_SOURCE_VIRTUAL_ADDRESSES);
    assertThat(
        configuration.getGeneratedReferenceBooks().get(refbookName),
        equalTo(
            ReferenceBook.builder(refbookName)
                .setAddressGroups(ImmutableList.of(toAddressGroup(v1)))
                .build()));
  }
}
