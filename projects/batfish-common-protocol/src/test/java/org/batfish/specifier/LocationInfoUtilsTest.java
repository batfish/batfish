package org.batfish.specifier;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.batfish.datamodel.matchers.IpSpaceMatchers.containsIp;
import static org.batfish.specifier.Location.interfaceLinkLocation;
import static org.batfish.specifier.Location.interfaceLocation;
import static org.batfish.specifier.LocationInfoUtils.computeLocationInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import java.util.Map;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.Vrf;
import org.junit.Test;

public class LocationInfoUtilsTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new LocationInfo(true, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE),
            new LocationInfo(true, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE))
        .addEqualityGroup(new LocationInfo(false, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE))
        .addEqualityGroup(new LocationInfo(true, UniverseIpSpace.INSTANCE, EmptyIpSpace.INSTANCE))
        .addEqualityGroup(new LocationInfo(true, EmptyIpSpace.INSTANCE, UniverseIpSpace.INSTANCE))
        .testEquals();
  }

  @Test
  public void testComputeLocationInfo_default() {
    NetworkFactory nf = new NetworkFactory();

    Configuration config =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("a")
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Interface.Builder ib = nf.interfaceBuilder().setOwner(config).setAdminUp(true).setVrf(vrf);
    Interface i = ib.setName("i").setAddress(ConcreteInterfaceAddress.parse("1.1.1.1/29")).build();

    // i has no LocationInfo -- will use default VI logic
    InterfaceLocation iloc = interfaceLocation(i);
    InterfaceLinkLocation linkLoc = interfaceLinkLocation(i);

    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("1.1.1.2");
    Ip ip3 = Ip.parse("1.1.1.3");

    Map<String, Map<String, IpSpace>> interfaceOwnedIps =
        ImmutableMap.of("a", ImmutableMap.of("i", ip1.toIpSpace()));
    Map<Location, LocationInfo> locationInfo =
        computeLocationInfo(
            ip1.toIpSpace(), // ip1 is owned, so subtracted from link locations' source IPs
            interfaceOwnedIps,
            ImmutableMap.of(config.getHostname(), config));

    // iloc
    {
      LocationInfo info = locationInfo.get(iloc);
      assertFalse(info.isSource());
      // source Ips taken from interfaceOwnedIps.
      assertThat(info.getSourceIps(), containsIp(ip1));
      assertEquals(EmptyIpSpace.INSTANCE, info.getArpIps());
    }

    // linkLoc
    {
      LocationInfo info = locationInfo.get(linkLoc);
      assertTrue(info.isSource());
      // source IPs computed from i1's concrete address, excluding the snapshot-owned IP ip1
      assertThat(info.getSourceIps(), allOf(not(containsIp(ip1)), containsIp(ip2)));
      assertThat(info.getArpIps(), containsIp(ip3));
    }
  }

  @Test
  public void testComputeLocationInfo_vendorSupplied() {
    NetworkFactory nf = new NetworkFactory();

    Configuration config =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("a")
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Interface.Builder ib = nf.interfaceBuilder().setOwner(config).setAdminUp(true).setVrf(vrf);
    Interface i = ib.setName("i").build();

    // i has LocationInfo -- will subtract snapshotOwnedIps
    InterfaceLocation iLoc = interfaceLocation(i);
    InterfaceLinkLocation linkLoc = interfaceLinkLocation(i);

    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("1.1.1.2");
    IpSpace ips12 = checkNotNull(AclIpSpace.union(ip1.toIpSpace(), ip2.toIpSpace()));

    // mock vendor-supplied LocationInfo
    {
      LocationInfo info = new LocationInfo(true, ips12, ips12);
      config.setLocationInfo(ImmutableMap.of(iLoc, info, linkLoc, info));
    }

    Map<Location, LocationInfo> locationInfo =
        computeLocationInfo(
            ip1.toIpSpace(), // ip1 is owned, so subtracted from link locations' source IPs
            ImmutableMap.of(),
            ImmutableMap.of(config.getHostname(), config));

    // iLoc
    {
      LocationInfo info = locationInfo.get(iLoc);
      assertTrue(info.isSource());
      assertThat(info.getSourceIps(), allOf(containsIp(ip1), containsIp(ip2)));
      assertThat(info.getArpIps(), allOf(containsIp(ip1), containsIp(ip2)));
    }
    // linkLoc
    {
      LocationInfo info = locationInfo.get(linkLoc);
      assertTrue(info.isSource());
      // source IPs taken from vendor-supplied location info
      assertThat(info.getSourceIps(), allOf(not(containsIp(ip1)), containsIp(ip2)));
      assertThat(info.getArpIps(), allOf(containsIp(ip1), containsIp(ip2)));
    }
  }

  @Test
  public void testComputeLocationInfo_inactive() {
    NetworkFactory nf = new NetworkFactory();

    Configuration config =
        nf.configurationBuilder()
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .setHostname("a")
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(config).build();
    Interface.Builder ib = nf.interfaceBuilder().setOwner(config).setAdminUp(true).setVrf(vrf);
    Interface i3 = ib.setName("i3").setAdminUp(false).build();

    // i3 has LocationInfo but is inactive
    InterfaceLocation iface3 = interfaceLocation(i3);
    InterfaceLinkLocation link3 = interfaceLinkLocation(i3);

    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("1.1.1.2");
    IpSpace ips12 = checkNotNull(AclIpSpace.union(ip1.toIpSpace(), ip2.toIpSpace()));

    // mock vendor-supplied LocationInfo
    {
      LocationInfo info = new LocationInfo(true, ips12, ips12);
      config.setLocationInfo(ImmutableMap.of(iface3, info, link3, info));
    }

    Map<Location, LocationInfo> locationInfo =
        computeLocationInfo(
            ip1.toIpSpace(), // ip1 is owned, so subtracted from link locations' source IPs
            ImmutableMap.of(),
            ImmutableMap.of(config.getHostname(), config));

    // iface3
    {
      LocationInfo info = locationInfo.get(iface3);
      // since inactive, has {@link LocationInfo.NOTHING}, which is eventually filtered out.
      assertNull(info);
    }
    // link3
    {
      LocationInfo info = locationInfo.get(link3);
      // since inactive, has {@link LocationInfo.NOTHING}, which is eventually filtered out.
      assertNull(info);
    }
  }
}
