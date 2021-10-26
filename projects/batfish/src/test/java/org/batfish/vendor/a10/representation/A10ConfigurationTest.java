package org.batfish.vendor.a10.representation;

import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceEnabledEffective;
import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceHumanName;
import static org.batfish.vendor.a10.representation.A10Configuration.getInterfaceMtuEffective;
import static org.batfish.vendor.a10.representation.Interface.DEFAULT_MTU;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.referencelibrary.GeneratedRefBookUtils;
import org.batfish.referencelibrary.GeneratedRefBookUtils.BookType;
import org.batfish.referencelibrary.ReferenceBook;
import org.junit.Test;

/** Tests of {@link A10Configuration}. */
public class A10ConfigurationTest {
  @Test
  public void testGetInterfaceEnabledEffective() {
    Interface ethNullEnabled = new Interface(Interface.Type.ETHERNET, 1);
    Interface loopNullEnabled = new Interface(Interface.Type.LOOPBACK, 1);

    // Defaults
    // Ethernet is disabled by default
    assertFalse(getInterfaceEnabledEffective(ethNullEnabled));
    // Loopback is enabled by default
    assertTrue(getInterfaceEnabledEffective(loopNullEnabled));

    // Explicit enabled value set
    Interface eth = new Interface(Interface.Type.ETHERNET, 1);
    eth.setEnabled(true);
    assertTrue(getInterfaceEnabledEffective(eth));
    eth.setEnabled(false);
    assertFalse(getInterfaceEnabledEffective(eth));
  }

  @Test
  public void testGetInterfaceMtuEffective() {
    Interface eth = new Interface(Interface.Type.ETHERNET, 1);

    assertThat(getInterfaceMtuEffective(eth), equalTo(DEFAULT_MTU));
    eth.setMtu(1234);
    assertThat(getInterfaceMtuEffective(eth), equalTo(1234));
  }

  @Test
  public void testGetInterfaceHumanName() {
    assertThat(
        getInterfaceHumanName(new Interface(Interface.Type.ETHERNET, 9)), equalTo("Ethernet 9"));
    assertThat(getInterfaceHumanName(new Interface(Interface.Type.TRUNK, 9)), equalTo("Trunk 9"));
    assertThat(
        getInterfaceHumanName(new Interface(Interface.Type.LOOPBACK, 9)), equalTo("Loopback 9"));
    assertThat(
        getInterfaceHumanName(new Interface(Interface.Type.VE, 9)), equalTo("VirtualEthernet 9"));
  }

  /** Test that reference book is generated as part of conversion */
  @Test
  public void testToVendorConfiguration_generateReferenceBook() {
    A10Configuration a10Configuration = new A10Configuration();
    a10Configuration.setHostname("c");
    VirtualServerTarget target1 = new VirtualServerTargetAddress(Ip.parse("1.1.1.1"));
    VirtualServerTarget target2 = new VirtualServerTargetAddress(Ip.parse("2.2.2.2"));
    a10Configuration.getOrCreateVirtualServer("vs1", target1);
    a10Configuration.getOrCreateVirtualServer("vs2", target2);
    Configuration c =
        Iterables.getOnlyElement(a10Configuration.toVendorIndependentConfigurations());

    String bookName = GeneratedRefBookUtils.getName("c", BookType.VirtualAddresses);

    assertThat(
        c.getGeneratedReferenceBooks(),
        equalTo(
            ImmutableMap.of(
                bookName,
                ReferenceBook.builder(bookName)
                    .setAddressGroups(
                        ImmutableList.of(
                            new VirtualServerTargetToAddressGroup("vs1").visit(target1),
                            new VirtualServerTargetToAddressGroup("vs2").visit(target2)))
                    .build())));
  }
}
