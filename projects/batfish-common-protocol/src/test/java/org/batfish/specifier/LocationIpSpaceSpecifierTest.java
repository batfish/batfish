package org.batfish.specifier;

import static org.batfish.specifier.Location.interfaceLocation;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.NetworkFactory;
import org.hamcrest.Matchers;
import org.junit.Test;

public class LocationIpSpaceSpecifierTest {
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
    Interface.Builder ib = nf.interfaceBuilder().setAdminUp(false);

    _c1 = cb.build();
    ib.setOwner(_c1);

    /*
     * The /30 is not considered to be a host network (for historical reasons; see the comment on
     * NodeNameRegexConnectedHostsIpSpaceSpecifier.HOST_SUBNET_MAX_PREFIX_LENGTH).
     */
    _i1 =
        ib.setAddresses(
                ConcreteInterfaceAddress.parse("1.0.0.1/24"),
                ConcreteInterfaceAddress.parse("2.0.0.1/30"))
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
                        true, _i1.getConcreteAddress().getIp().toIpSpace(), EmptyIpSpace.INSTANCE),
                    interfaceLocation(_i2),
                    new LocationInfo(
                        true, _i2.getConcreteAddress().getIp().toIpSpace(), EmptyIpSpace.INSTANCE),
                    interfaceLocation(_i3),
                    new LocationInfo(true, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE)))
            .build();
  }

  @Test
  public void testResolve() {
    IpSpace ipSpace =
        new LocationIpSpaceSpecifier(AllInterfacesLocationSpecifier.INSTANCE).resolve(_context);
    assertThat(
        AclIpSpace.union(ipSpace),
        Matchers.equalTo(
            AclIpSpace.union(
                Ip.parse("1.0.0.1").toIpSpace(),
                AclIpSpace.union(Ip.parse("1.0.0.2").toIpSpace()))));
  }

  @Test
  public void testResolve_noLocations() {
    MockSpecifierContext ctxt = MockSpecifierContext.builder().build();
    IpSpaceSpecifier specifier =
        new LocationIpSpaceSpecifier(new MockLocationSpecifier(ImmutableSet.of()));
    assertThat(specifier.resolve(ctxt), equalTo(EmptyIpSpace.INSTANCE));
  }
}
