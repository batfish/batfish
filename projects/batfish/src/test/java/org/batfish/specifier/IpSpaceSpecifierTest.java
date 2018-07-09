package org.batfish.specifier;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.batfish.specifier.IpSpaceAssignmentMatchers.hasEntry;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.specifier.IpSpaceAssignment.Entry;
import org.junit.Test;

public class IpSpaceSpecifierTest {
  private static final Set<Location> _allLocations;

  private static final Map<String, Configuration> _configs;

  private static final SpecifierContext _context;

  private static final Interface _i1;
  private static final Interface _i2;

  static {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb = nf.configurationBuilder();
    cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS);
    Interface.Builder ib = nf.interfaceBuilder();

    Configuration n1 = cb.build();
    ib.setOwner(n1);

    _i1 = ib.setAddress(new InterfaceAddress("1.0.0.0/24")).build();
    _i2 = nf.interfaceBuilder().setOwner(n1).build();

    _configs = ImmutableMap.of(n1.getHostname(), n1);
    _context =
        MockSpecifierContext.builder()
            .setConfigs(_configs)
            .setInterfaceOwnedIps(
                ImmutableMap.of(
                    n1.getName(),
                    ImmutableMap.of(
                        _i1.getName(),
                        _i1.getAddress().getIp().toIpSpace(),
                        _i2.getName(),
                        EmptyIpSpace.INSTANCE)))
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
    IpSpaceAssignment assignment =
        InferFromLocationIpSpaceSpecifier.INSTANCE.resolve(_allLocations, _context);

    // all locations are present
    Set<Location> assignmentLocations =
        assignment
            .getEntries()
            .stream()
            .map(Entry::getLocations)
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    assertThat(assignmentLocations, containsInAnyOrder(_allLocations.toArray()));

    assertThat(
        assignment,
        hasEntry(
            containsIp(new Ip("1.0.0.0")),
            contains(new InterfaceLocation(_i1.getOwner().getName(), _i1.getName()))));

    assertThat(
        assignment,
        hasEntry(
            allOf(containsIp(new Ip("1.0.0.1")), not(containsIp(new Ip("1.0.0.0")))),
            contains(new InterfaceLinkLocation(_i1.getOwner().getName(), _i1.getName()))));

    // Locations that don't own any ipspace are assigned EmptyIpSpace.
    assertThat(
        assignment,
        hasEntry(
            equalTo(EmptyIpSpace.INSTANCE),
            contains(new InterfaceLocation(_i2.getOwner().getName(), _i2.getName()))));
    assertThat(
        assignment,
        hasEntry(
            equalTo(EmptyIpSpace.INSTANCE),
            contains(new InterfaceLinkLocation(_i2.getOwner().getName(), _i2.getName()))));
  }
}
