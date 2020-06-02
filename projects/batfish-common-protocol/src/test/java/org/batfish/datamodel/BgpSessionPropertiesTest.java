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
  public void testSessionCreation() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    long as1 = 1;
    long as2 = 2;
    Ipv4UnicastAddressFamily addressFamily =
        Ipv4UnicastAddressFamily.builder()
            .setAddressFamilyCapabilities(
                AddressFamilyCapabilities.builder().setAdvertiseInactive(true).build())
            .build();
    BgpActivePeerConfig p1 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip1)
            .setLocalAs(as1)
            .setRemoteAs(as2)
            .setPeerAddress(ip2)
            .setIpv4UnicastAddressFamily(addressFamily)
            .build();
    BgpActivePeerConfig p2 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip2)
            .setLocalAs(as2)
            .setRemoteAs(as1)
            .setPeerAddress(ip1)
            .setIpv4UnicastAddressFamily(addressFamily)
            .build();
    BgpSessionProperties session = BgpSessionProperties.from(p1, p2, false);

    BgpSessionProperties expectedSession =
        BgpSessionProperties.builder()
            .setAddressFamilies(ImmutableList.of(addressFamily.getType()))
            .setTailAs(as1)
            .setHeadAs(as2)
            .setTailIp(ip1)
            .setHeadIp(ip2)
            .setSessionType(SessionType.EBGP_SINGLEHOP)
            .setRouteExchangeSettings(
                // Add paths false, advertise external false, advertise inactive true
                ImmutableMap.of(addressFamily.getType(), new RouteExchange(false, false, true)))
            .build();
    assertThat(session, equalTo(expectedSession));
  }

  @Test
  public void testEquals() {
    Builder builder = BgpSessionProperties.builder();
    long headAs = 1;
    long tailAs = 3;
    Ip headIp = Ip.parse("1.1.1.1");
    Ip tailIp = Ip.parse("2.2.2.2");
    BgpSessionProperties bsp =
        builder.setTailAs(tailAs).setHeadAs(headAs).setTailIp(tailIp).setHeadIp(headIp).build();
    new EqualsTester()
        .addEqualityGroup(bsp, bsp, builder.build())
        .addEqualityGroup(
            builder
                .setRouteExchangeSettings(
                    ImmutableMap.of(Type.IPV4_UNICAST, new RouteExchange(true, false, true)))
                .build())
        .addEqualityGroup(builder.setAddressFamilies(ImmutableSet.of(Type.IPV4_UNICAST)).build())
        .addEqualityGroup(builder.setHeadAs(headAs + 1).build())
        .addEqualityGroup(builder.setTailAs(tailAs + 1).build())
        // note the head/tail swap
        .addEqualityGroup(builder.setHeadIp(tailIp).build())
        .addEqualityGroup(builder.setTailIp(headIp).build())
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
            .setTailAs(1L)
            .setHeadAs(2L)
            .setTailIp(tailIp)
            .setHeadIp(headIp)
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
    assertThat(session.getTailIp(), equalTo(ip1));
    assertThat(session.getHeadIp(), equalTo(ip2));
  }
}
