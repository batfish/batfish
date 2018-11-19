package org.batfish.specifier;

import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.batfish.specifier.IpSpaceAssignmentMatchers.hasEntry;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
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
        ib.setAddresses(new InterfaceAddress("1.0.0.1/24"), new InterfaceAddress("2.0.0.0/30"))
            .build();

    // another interface on _i1's subnet
    _i2 = ib.setAddresses(new InterfaceAddress("1.0.0.2/24")).build();

    // another interface with no addresses
    _i3 = nf.interfaceBuilder().setOwner(_c1).build();

    _configs = ImmutableMap.of(_c1.getHostname(), _c1);
    _context =
        MockSpecifierContext.builder()
            .setConfigs(_configs)
            .setInterfaceOwnedIps(
                ImmutableMap.of(
                    _c1.getHostname(),
                    ImmutableMap.of(
                        _i1.getName(),
                        _i1.getAddress().getIp().toIpSpace(),
                        _i2.getName(),
                        _i2.getAddress().getIp().toIpSpace(),
                        _i3.getName(),
                        EmptyIpSpace.INSTANCE)))
            .setSnapshotOwnedIps(
                AclIpSpace.union(
                    _i1.getAddress().getIp().toIpSpace(), _i2.getAddress().getIp().toIpSpace()))
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
            containsIp(new Ip("1.0.0.1")),
            contains(new InterfaceLocation(_i1.getOwner().getHostname(), _i1.getName()))));

    assertThat(
        assignment,
        hasEntry(
            allOf(
                // contains a host IP
                containsIp(new Ip("1.0.0.3")),
                // does not contain i1's IP
                not(containsIp(new Ip("1.0.0.1"))),
                // does not contain i2's IP
                not(containsIp(new Ip("1.0.0.2"))),
                // does not include any IPs from 2.0.0.0/30 because it's not a host subnet.
                not(
                    anyOf(
                        containsIp(new Ip("2.0.0.0")),
                        containsIp(new Ip("2.0.0.1")),
                        containsIp(new Ip("2.0.0.2")),
                        containsIp(new Ip("2.0.0.3"))))),
            contains(new InterfaceLinkLocation(_i1.getOwner().getHostname(), _i1.getName()))));

    // Locations that don't own any ipspace are assigned EmptyIpSpace.
    assertThat(
        assignment,
        hasEntry(
            equalTo(EmptyIpSpace.INSTANCE),
            contains(new InterfaceLocation(_i3.getOwner().getHostname(), _i3.getName()))));
    assertThat(
        assignment,
        hasEntry(
            equalTo(EmptyIpSpace.INSTANCE),
            contains(new InterfaceLinkLocation(_i3.getOwner().getHostname(), _i3.getName()))));
  }

  @Test
  public void testLocationIpSpaceSpecifier() {
    IpSpaceAssignment assignment =
        new LocationIpSpaceSpecifier(AllInterfacesLocationSpecifier.INSTANCE)
            .resolve(ImmutableSet.of(), _context);
    assertThat(assignment, hasEntry(containsIp(new Ip("1.0.0.1")), equalTo(ImmutableSet.of())));
  }
}
