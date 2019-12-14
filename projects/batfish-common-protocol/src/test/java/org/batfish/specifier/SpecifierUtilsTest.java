package org.batfish.specifier;

import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.specifier.SpecifierUtils.isActive;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Vrf;
import org.junit.Test;

/** Test for {@link SpecifierUtils}. */
public final class SpecifierUtilsTest {

  @Test
  public void testLocationIsActive() {
    NetworkFactory nf = new NetworkFactory();
    Configuration node = nf.configurationBuilder().setConfigurationFormat(CISCO_IOS).build();
    Vrf vrf = nf.vrfBuilder().setOwner(node).build();
    Interface activeInterface = nf.interfaceBuilder().setOwner(node).setVrf(vrf).build();
    Interface inactiveInterface =
        nf.interfaceBuilder().setActive(false).setOwner(node).setVrf(vrf).build();
    SortedMap<String, Configuration> configs = ImmutableSortedMap.of(node.getHostname(), node);

    String hostname = node.getHostname();
    assertTrue(isActive(new InterfaceLinkLocation(hostname, activeInterface.getName()), configs));
    assertFalse(
        isActive(new InterfaceLinkLocation(hostname, inactiveInterface.getName()), configs));
    assertTrue(isActive(new InterfaceLinkLocation(hostname, activeInterface.getName()), configs));
    assertFalse(
        isActive(new InterfaceLinkLocation(hostname, inactiveInterface.getName()), configs));
  }
}
