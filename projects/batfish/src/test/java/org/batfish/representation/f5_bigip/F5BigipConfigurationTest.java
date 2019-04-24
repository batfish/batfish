package org.batfish.representation.f5_bigip;

import static org.batfish.representation.f5_bigip.F5BigipConfiguration.REFBOOK_SOURCE_POOLS;
import static org.batfish.representation.f5_bigip.F5BigipConfiguration.REFBOOK_SOURCE_VIRTUAL_ADDRESSES;
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

public class F5BigipConfigurationTest {

  /** Check that virtual addresses are mapped to address groups in a reference book */
  @Test
  public void testGeneratedReferenceBooksVirtualAddresses() {
    F5BigipConfiguration f5Config = new F5BigipConfiguration();
    f5Config.setHostname("node");

    VirtualAddress v1 = new VirtualAddress("v1");
    v1.setAddress(Ip.parse("1.1.1.1"));

    // null address
    VirtualAddress v2 = new VirtualAddress("v2");

    f5Config.getVirtualAddresses().put(v1.getName(), v1);
    f5Config.getVirtualAddresses().put(v2.getName(), v2);

    Configuration configuration =
        Iterables.getOnlyElement(f5Config.toVendorIndependentConfigurations());

    String refbookName = Names.generatedReferenceBook("node", REFBOOK_SOURCE_VIRTUAL_ADDRESSES);
    assertThat(
        configuration.getGeneratedReferenceBooks().get(refbookName),
        equalTo(
            ReferenceBook.builder(refbookName)
                .setAddressGroups(
                    ImmutableList.of(
                        new AddressGroup(ImmutableSortedSet.of("1.1.1.1"), v1.getName())))
                .build()));
  }

  @Test
  public void testGeneratedReferenceBooksVirtualAddressesEmpty() {
    F5BigipConfiguration f5Config = new F5BigipConfiguration();
    f5Config.setHostname("node");
    Configuration configuration =
        Iterables.getOnlyElement(f5Config.toVendorIndependentConfigurations());

    String refbookName = Names.generatedReferenceBook("node", REFBOOK_SOURCE_VIRTUAL_ADDRESSES);
    assertThat(
        configuration.getGeneratedReferenceBooks().get(refbookName),
        equalTo(ReferenceBook.builder(refbookName).build()));
  }

  /** Check that pools are mapped to address groups in a reference book */
  @Test
  public void testGeneratedReferenceBooksPools() {
    F5BigipConfiguration f5Config = new F5BigipConfiguration();
    f5Config.setHostname("node");

    PoolMember m1 = new PoolMember("m1", null, 0);
    m1.setAddress(Ip.parse("1.1.1.1"));

    // null address
    PoolMember m2 = new PoolMember("m2", null, 0);

    Pool p1 = new Pool("p1");
    p1.getMembers().put(m1.getName(), m1);
    p1.getMembers().put(m2.getName(), m2);

    PoolMember m3 = new PoolMember("m3", null, 0);
    m3.setAddress(Ip.parse("2.2.2.2"));

    Pool p2 = new Pool("p2");
    p2.getMembers().put(m3.getName(), m3);

    f5Config.getPools().put(p1.getName(), p1);
    f5Config.getPools().put(p2.getName(), p2);

    Configuration configuration =
        Iterables.getOnlyElement(f5Config.toVendorIndependentConfigurations());

    String refbookName = Names.generatedReferenceBook("node", REFBOOK_SOURCE_POOLS);
    assertThat(
        configuration.getGeneratedReferenceBooks().get(refbookName),
        equalTo(
            ReferenceBook.builder(refbookName)
                .setAddressGroups(
                    ImmutableList.of(
                        new AddressGroup(ImmutableSortedSet.of("1.1.1.1"), p1.getName()),
                        new AddressGroup(ImmutableSortedSet.of("2.2.2.2"), p2.getName())))
                .build()));
  }

  @Test
  public void testGeneratedReferenceBooksPoolsEmpty() {
    F5BigipConfiguration f5Config = new F5BigipConfiguration();
    f5Config.setHostname("node");
    Configuration configuration =
        Iterables.getOnlyElement(f5Config.toVendorIndependentConfigurations());

    String refbookName = Names.generatedReferenceBook("node", REFBOOK_SOURCE_POOLS);
    assertThat(
        configuration.getGeneratedReferenceBooks().get(refbookName),
        equalTo(ReferenceBook.builder(refbookName).build()));
  }
}
