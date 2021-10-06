package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.NetworkFactory;
import org.junit.Test;

public class AllActiveSourcesTest {

  private final NetworkFactory _nf = new NetworkFactory();

  private Interface createInterface(String hostname, String interfaceName, boolean active) {
    Configuration n1 =
        _nf.configurationBuilder()
            .setHostname(hostname)
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    return _nf.interfaceBuilder().setName(interfaceName).setOwner(n1).setActive(active).build();
  }

  @Test
  public void testResolve() {
    Interface activeSource = createInterface("n1", "i", true);
    Interface activeNonSource = createInterface("n2", "i", true);
    Interface inactiveSource = createInterface("n3", "i", false);
    Interface activeLinkSource = createInterface("n4", "i", true);

    Location activeSourceLoc =
        new InterfaceLocation(activeSource.getOwner().getHostname(), activeSource.getName());
    Location activeNonSourceLoc =
        new InterfaceLocation(activeNonSource.getOwner().getHostname(), activeNonSource.getName());
    Location inactiveSourceLoc =
        new InterfaceLocation(inactiveSource.getOwner().getHostname(), inactiveSource.getName());
    Location activeLinkSourceLoc =
        new InterfaceLinkLocation(
            activeLinkSource.getOwner().getHostname(), activeLinkSource.getName());

    SpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setLocationInfo(
                ImmutableMap.of(
                    activeSourceLoc,
                    new LocationInfo(true, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE),
                    activeNonSourceLoc,
                    new LocationInfo(false, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE),
                    inactiveSourceLoc,
                    new LocationInfo(true, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE),
                    activeLinkSourceLoc,
                    new LocationInfo(true, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE)))
            .setConfigs(
                ImmutableMap.of(
                    activeSource.getOwner().getHostname(),
                    activeSource.getOwner(),
                    activeNonSource.getOwner().getHostname(),
                    activeNonSource.getOwner(),
                    inactiveSource.getOwner().getHostname(),
                    inactiveSource.getOwner(),
                    activeLinkSource.getOwner().getHostname(),
                    activeLinkSource.getOwner()))
            .build();
    assertThat(
        AllActiveSources.ALL_ACTIVE_SOURCES.resolve(ctxt),
        contains(activeSourceLoc, activeLinkSourceLoc));
  }
}
