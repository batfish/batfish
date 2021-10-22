package org.batfish.datamodel;

import static org.batfish.datamodel.BgpSessionProperties.getAddressFamilyIntersection;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.util.EnumSet;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpSessionProperties.Builder;
import org.batfish.datamodel.BgpSessionProperties.RouteExchange;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.bgp.AddressFamily.Type;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.junit.Test;

/** Tests of {@link BgpSessionProperties} */
public class BgpSessionPropertiesTest {
  @Test
  public void testSessionCreationEbgp() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    long as1 = 1;
    long as2 = 2;
    Ipv4UnicastAddressFamily addressFamily1 =
        Ipv4UnicastAddressFamily.builder()
            .setAddressFamilyCapabilities(
                AddressFamilyCapabilities.builder().setAdvertiseInactive(true).build())
            .build();
    Ipv4UnicastAddressFamily addressFamily2 =
        Ipv4UnicastAddressFamily.builder()
            .setAddressFamilyCapabilities(
                AddressFamilyCapabilities.builder().setAdvertiseInactive(false).build())
            .build();
    BgpActivePeerConfig p1 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip1)
            .setLocalAs(as1)
            .setRemoteAs(as2)
            .setPeerAddress(ip2)
            .setIpv4UnicastAddressFamily(addressFamily1)
            .build();
    BgpActivePeerConfig p2 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip2)
            .setLocalAs(as2)
            .setRemoteAs(as1)
            .setPeerAddress(ip1)
            .setIpv4UnicastAddressFamily(addressFamily2)
            .build();
    {
      BgpSessionProperties forwardSession =
          BgpSessionProperties.builder()
              .setAddressFamilies(ImmutableList.of(addressFamily1.getType()))
              .setLocalAs(as1)
              .setRemoteAs(as2)
              .setLocalIp(ip1)
              .setRemoteIp(ip2)
              .setSessionType(SessionType.EBGP_SINGLEHOP)
              .setRouteExchangeSettings(
                  // Add paths false, advertise external false, advertise inactive true
                  ImmutableMap.of(addressFamily1.getType(), new RouteExchange(false, false, true)))
              .build();
      assertThat(BgpSessionProperties.from(p1, p2, false), equalTo(forwardSession));
    }
    {
      BgpSessionProperties reverseSession =
          BgpSessionProperties.builder()
              .setAddressFamilies(ImmutableList.of(addressFamily1.getType()))
              .setLocalAs(as2)
              .setRemoteAs(as1)
              .setLocalIp(ip2)
              .setRemoteIp(ip1)
              .setSessionType(SessionType.EBGP_SINGLEHOP)
              .setRouteExchangeSettings(
                  // Add paths false, advertise external false, advertise inactive true
                  ImmutableMap.of(addressFamily1.getType(), new RouteExchange(false, false, false)))
              .build();
      assertThat(BgpSessionProperties.from(p1, p2, true), equalTo(reverseSession));
    }
  }

  @Test
  public void testSessionCreationIbgp() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    long as = 1;
    Ipv4UnicastAddressFamily addressFamily1 =
        Ipv4UnicastAddressFamily.builder()
            .setAddressFamilyCapabilities(
                AddressFamilyCapabilities.builder()
                    .setAdditionalPathsSelectAll(true)
                    .setAdditionalPathsSend(true)
                    .setAdvertiseExternal(true)
                    .build())
            .build();
    Ipv4UnicastAddressFamily addressFamily2 =
        Ipv4UnicastAddressFamily.builder()
            .setAddressFamilyCapabilities(
                AddressFamilyCapabilities.builder().setAdditionalPathsReceive(true).build())
            .build();
    BgpActivePeerConfig p1 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip1)
            .setLocalAs(as)
            .setRemoteAs(as)
            .setPeerAddress(ip2)
            .setIpv4UnicastAddressFamily(addressFamily1)
            .build();
    BgpActivePeerConfig p2 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip2)
            .setLocalAs(as)
            .setRemoteAs(as)
            .setPeerAddress(ip1)
            .setIpv4UnicastAddressFamily(addressFamily2)
            .build();
    {
      BgpSessionProperties forwardSession =
          BgpSessionProperties.builder()
              .setAddressFamilies(ImmutableList.of(addressFamily1.getType()))
              .setLocalAs(as)
              .setRemoteAs(as)
              .setLocalIp(ip1)
              .setRemoteIp(ip2)
              .setSessionType(SessionType.IBGP)
              .setRouteExchangeSettings(
                  // Add paths false, advertise external false, advertise inactive true
                  ImmutableMap.of(addressFamily1.getType(), new RouteExchange(true, true, false)))
              .build();
      assertThat(BgpSessionProperties.from(p1, p2, false), equalTo(forwardSession));
    }
    {
      BgpSessionProperties reverseSession =
          BgpSessionProperties.builder()
              .setAddressFamilies(ImmutableList.of(addressFamily1.getType()))
              .setLocalAs(as)
              .setRemoteAs(as)
              .setLocalIp(ip2)
              .setRemoteIp(ip1)
              .setSessionType(SessionType.IBGP)
              .setRouteExchangeSettings(
                  // Add paths false, advertise external false, advertise inactive true
                  ImmutableMap.of(addressFamily1.getType(), new RouteExchange(false, false, false)))
              .build();
      assertThat(BgpSessionProperties.from(p1, p2, true), equalTo(reverseSession));
    }
  }

  @Test
  public void testEquals() {
    Builder builder = BgpSessionProperties.builder();
    long headAs = 1;
    long tailAs = 3;
    Ip headIp = Ip.parse("1.1.1.1");
    Ip tailIp = Ip.parse("2.2.2.2");
    BgpSessionProperties bsp =
        builder
            .setLocalAs(tailAs)
            .setRemoteAs(headAs)
            .setLocalIp(tailIp)
            .setRemoteIp(headIp)
            .build();
    new EqualsTester()
        .addEqualityGroup(bsp, bsp, builder.build())
        .addEqualityGroup(
            builder
                .setRouteExchangeSettings(
                    ImmutableMap.of(Type.IPV4_UNICAST, new RouteExchange(true, false, true)))
                .build())
        .addEqualityGroup(builder.setAddressFamilies(ImmutableSet.of(Type.IPV4_UNICAST)).build())
        .addEqualityGroup(builder.setRemoteAs(headAs + 1).build())
        .addEqualityGroup(builder.setLocalAs(tailAs + 1).build())
        // note the head/tail swap
        .addEqualityGroup(builder.setRemoteIp(tailIp).build())
        .addEqualityGroup(builder.setLocalIp(headIp).build())
        .addEqualityGroup(builder.setSessionType(SessionType.EBGP_SINGLEHOP))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    Ip headIp = Ip.parse("1.1.1.1");
    Ip tailIp = Ip.parse("2.2.2.2");
    BgpSessionProperties bsp =
        BgpSessionProperties.builder()
            .setAddressFamilies(EnumSet.allOf(Type.class))
            .setRouteExchangeSettings(
                ImmutableMap.of(Type.IPV4_UNICAST, new RouteExchange(true, false, true)))
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .setLocalIp(tailIp)
            .setRemoteIp(headIp)
            .setSessionType(SessionType.EBGP_MULTIHOP)
            .build();
    assertThat(BatfishObjectMapper.clone(bsp, BgpSessionProperties.class), equalTo(bsp));
  }

  @Test
  public void testGetAddressFamilyIntersection() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    BgpActivePeerConfig.Builder peerBuilder =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip1)
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .setPeerAddress(ip2)
            .setIpv4UnicastAddressFamily(
                Ipv4UnicastAddressFamily.builder()
                    .setAddressFamilyCapabilities(
                        AddressFamilyCapabilities.builder().setAdvertiseInactive(true).build())
                    .build());
    BgpActivePeerConfig peer1 = peerBuilder.build();

    // intersection with itself only contains ipv4
    assertThat(getAddressFamilyIntersection(peer1, peer1), contains(Type.IPV4_UNICAST));
    assertThat(
        getAddressFamilyIntersection(
            peer1,
            peerBuilder
                .setEvpnAddressFamily(
                    EvpnAddressFamily.builder()
                        .setL2Vnis(ImmutableSet.of())
                        .setL3Vnis(ImmutableSet.of())
                        .setPropagateUnmatched(true)
                        .build())
                .build()),
        contains(Type.IPV4_UNICAST));
    // Clear ipv4, should get empty intersection back
    assertThat(
        getAddressFamilyIntersection(peer1, peerBuilder.setIpv4UnicastAddressFamily(null).build()),
        empty());
  }

  /** Test that session head/tail IPs are not {@link Ip#AUTO} for dynamic session */
  @Test
  public void testPassiveSessionIpComputation() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    long as1 = 1;
    long as2 = 2;
    Ipv4UnicastAddressFamily addressFamily = Ipv4UnicastAddressFamily.builder().build();
    BgpActivePeerConfig p1 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip1)
            .setLocalAs(as1)
            .setRemoteAs(as2)
            .setPeerAddress(ip2)
            .setIpv4UnicastAddressFamily(addressFamily)
            .build();
    BgpPassivePeerConfig p2 =
        BgpPassivePeerConfig.builder()
            .setPeerPrefix(Prefix.create(ip1, 24))
            .setLocalIp(Ip.AUTO)
            .setLocalAs(as2)
            .setRemoteAs(as1)
            .setIpv4UnicastAddressFamily(addressFamily)
            .build();
    BgpSessionProperties session = BgpSessionProperties.from(p1, p2, false);
    assertThat(session.getLocalIp(), equalTo(ip1));
    assertThat(session.getRemoteIp(), equalTo(ip2));
  }
}
