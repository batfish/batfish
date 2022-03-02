package org.batfish.specifier;

import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.specifier.SpecifierUtils.isActiveL3Interface;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.junit.Test;

/** Test for {@link SpecifierUtils}. */
public final class SpecifierUtilsTest {

  @Test
  public void testLocationIsActiveL3Interface() {
    NetworkFactory nf = new NetworkFactory();
    Configuration node = nf.configurationBuilder().setConfigurationFormat(CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(node).build();
    Interface activeL3Interface =
        nf.interfaceBuilder()
            .setOwner(node)
            .setVrf(vrf)
            .setSwitchport(false)
            .setAddress(ConcreteInterfaceAddress.parse("1.2.3.4/24"))
            .build();
    Interface activeNonL3Interface =
        nf.interfaceBuilder().setOwner(node).setVrf(vrf).setSwitchport(false).build();
    Interface inactiveInterface =
        nf.interfaceBuilder()
            .setAdminUp(false)
            .setOwner(node)
            .setVrf(vrf)
            .setSwitchport(false)
            .setAddress(ConcreteInterfaceAddress.parse("2.3.4.5/24"))
            .build();
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(node.getHostname(), node);

    String hostname = node.getHostname();
    // Interfaces
    assertTrue(
        isActiveL3Interface(new InterfaceLocation(hostname, activeL3Interface.getName()), configs));
    assertFalse(
        isActiveL3Interface(
            new InterfaceLocation(hostname, activeNonL3Interface.getName()), configs));
    assertFalse(
        isActiveL3Interface(new InterfaceLocation(hostname, inactiveInterface.getName()), configs));
    // Links
    assertTrue(
        isActiveL3Interface(
            new InterfaceLinkLocation(hostname, activeL3Interface.getName()), configs));
    assertFalse(
        isActiveL3Interface(
            new InterfaceLinkLocation(hostname, activeNonL3Interface.getName()), configs));
    assertFalse(
        isActiveL3Interface(
            new InterfaceLinkLocation(hostname, inactiveInterface.getName()), configs));
  }
}
