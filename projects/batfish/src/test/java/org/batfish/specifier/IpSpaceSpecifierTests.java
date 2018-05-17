package org.batfish.specifier;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.UniverseIpSpace;
import org.junit.Test;

public class IpSpaceSpecifierTests {
  private static final Set<Location> _allLocations;

  private static final Map<String, Configuration> _configs;

  private static final SpecifierContext _context;

  private static final Interface _i1;

  static {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb = nf.configurationBuilder();
    cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder();

    Configuration n1 = cb.build();
    ib.setOwner(n1);

    _i1 = ib.setAddress(new InterfaceAddress("1.0.0.0/24")).build();

    _configs = ImmutableMap.of(n1.getHostname(), n1);
    _context = MockSpecifierContext.builder().setConfigs(_configs).build();
    _allLocations =
        Sets.union(
            AllInterfacesLocationSpecifier.INSTANCE.resolve(_context),
            AllInterfaceLinksLocationSpecifier.INSTANCE.resolve(_context));
  }

  @Test
  public void testConstantIpSpaceSpecifier() {
    assertThat(
        new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE).resolve(_allLocations, _context),
        equalTo(ImmutableMap.of(_allLocations, UniverseIpSpace.INSTANCE)));
  }

  @Test
  public void testInferFromLocationIpSpaceSpecifier() {
    Map<Set<Location>, IpSpace> ipSpaces =
        InferFromLocationIpSpaceSpecifier.INSTANCE.resolve(_allLocations, _context);

    // each key is a singleton set of a location, all locations are present
    assertThat(
        ipSpaces.keySet(),
        equalTo(
            _allLocations.stream().map(ImmutableSet::of).collect(ImmutableSet.toImmutableSet())));

    assertThat(
        ipSpaces,
        hasEntry(
            equalTo(
                ImmutableSet.of(new InterfaceLocation(_i1.getOwner().getName(), _i1.getName()))),
            equalTo(new Ip("1.0.0.0").toIpSpace())));

    assertThat(
        ipSpaces,
        hasEntry(
            equalTo(
                ImmutableSet.of(
                    new InterfaceLinkLocation(_i1.getOwner().getName(), _i1.getName()))),
            allOf(containsIp(new Ip("1.0.0.1")), not(containsIp(new Ip("1.0.0.0"))))));
  }
}
