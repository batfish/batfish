package org.batfish.specifier;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.batfish.specifier.IpSpaceAssignmentMatchers.hasEntry;
import static org.batfish.specifier.Location.interfaceLocation;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.junit.Test;

public class IpSpaceSpecifierTest {
  private static final Set<Location> _allLocations;

  private static final Map<String, Configuration> _configs;

  private static final SpecifierContext _context;

  private static final Configuration _c1;

  private static final Interface _i1;
  private static final Interface _i2;
  private static final Interface _i3;

  static {
    NetworkFactory nf = new NetworkFactory();
    Builder cb = nf.configurationBuilder();
    cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder().setActive(false);

    _c1 = cb.build();
    ib.setOwner(_c1);

    /*
     * The /30 is not considered to be a host network (for historical reasons; see the comment on
     * NodeNameRegexConnectedHostsIpSpaceSpecifier.HOST_SUBNET_MAX_PREFIX_LENGTH).
     */
    _i1 =
        ib.setAddresses(
                ConcreteInterfaceAddress.parse("1.0.0.1/24"),
                ConcreteInterfaceAddress.parse("2.0.0.0/30"))
            .build();

    // another interface on _i1's subnet
    _i2 = ib.setAddresses(ConcreteInterfaceAddress.parse("1.0.0.2/24")).build();

    // another interface with no addresses
    _i3 = nf.interfaceBuilder().setOwner(_c1).build();

    _configs = ImmutableMap.of(_c1.getHostname(), _c1);
    _context =
        MockSpecifierContext.builder()
            .setConfigs(_configs)
            .setLocationInfo(
                ImmutableMap.of(
                    interfaceLocation(_i1),
                        new LocationInfo(
                            true,
                            _i1.getConcreteAddress().getIp().toIpSpace(),
                            EmptyIpSpace.INSTANCE),
                    interfaceLocation(_i2),
                        new LocationInfo(
                            true,
                            _i2.getConcreteAddress().getIp().toIpSpace(),
                            EmptyIpSpace.INSTANCE),
                    interfaceLocation(_i3),
                        new LocationInfo(true, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE)))
            .build();

    _allLocations =
        Sets.union(
            AllInterfacesLocationSpecifier.INSTANCE.resolve(_context),
            AllInterfaceLinksLocationSpecifier.INSTANCE.resolve(_context));
  }

  @Test
  public void testConstantIpSpaceSpecifier() {
    IpSpaceAssignment assignment =
        new ConstantIpSpaceSpecifier(UniverseIpSpace.INSTANCE).resolve(_allLocations, _context);
    assertThat(assignment, hasEntry(equalTo(UniverseIpSpace.INSTANCE), equalTo(_allLocations)));
  }

  @Test
  public void testInferFromLocationIpSpaceSpecifier() {
    Set<Location> interfaceLocations = AllInterfacesLocationSpecifier.INSTANCE.resolve(_context);
    IpSpaceAssignment assignment =
        InferFromLocationIpSpaceSpecifier.INSTANCE.resolve(interfaceLocations, _context);

    // all locations are present
    Set<Location> assignmentLocations =
        assignment.getEntries().stream()
            .map(Entry::getLocations)
            .flatMap(Set::stream)
            .collect(ImmutableSet.toImmutableSet());
    assertThat(assignmentLocations, containsInAnyOrder(interfaceLocations.toArray()));

    assertThat(
        assignment, hasEntry(containsIp(Ip.parse("1.0.0.1")), contains(interfaceLocation(_i1))));

    // Locations that don't own any ipspace are assigned EmptyIpSpace.
    assertThat(
        assignment,
        hasEntry(
            equalTo(EmptyIpSpace.INSTANCE),
            contains(new InterfaceLocation(_i3.getOwner().getHostname(), _i3.getName()))));
  }

  @Test
  public void testLocationIpSpaceSpecifier() {
    IpSpaceAssignment assignment =
        new LocationIpSpaceSpecifier(AllInterfacesLocationSpecifier.INSTANCE)
            .resolve(ImmutableSet.of(), _context);
    assertThat(assignment, hasEntry(containsIp(Ip.parse("1.0.0.1")), equalTo(ImmutableSet.of())));
  }
}
