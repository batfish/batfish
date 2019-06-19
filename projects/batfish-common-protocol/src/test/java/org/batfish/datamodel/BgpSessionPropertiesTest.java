package org.batfish.datamodel;

import static org.batfish.datamodel.BgpSessionProperties.getAddressFamilyIntersection;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpSessionProperties.Builder;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.bgp.AddressFamily.Type;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.junit.Test;

/** Tests of {@link BgpSessionProperties} */
public class BgpSessionPropertiesTest {
  @Test
  public void testSessionCreation() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    BgpActivePeerConfig p1 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip1)
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .setPeerAddress(ip2)
            .setAdvertiseInactive(true)
            .build();
    BgpActivePeerConfig p2 =
        BgpActivePeerConfig.builder()
            .setLocalIp(ip2)
            .setLocalAs(1L)
            .setRemoteAs(2L)
            .setPeerAddress(ip1)
            .setAdvertiseInactive(false)
            .build();
    BgpSessionProperties session = BgpSessionProperties.from(p1, p2, false);
    assertTrue(session.getAdvertiseInactive());
    assertFalse(session.getAdvertiseExternal());
  }

  @Test
  public void testEquals() {
    Builder builder = BgpSessionProperties.builder();
    Ip headIp = Ip.parse("1.1.1.1");
    Ip tailIp = Ip.parse("2.2.2.2");
    BgpSessionProperties bsp = builder.setTailIp(tailIp).setHeadIp(headIp).build();
    new EqualsTester()
        .addEqualityGroup(bsp, bsp, builder.build())
        .addEqualityGroup(builder.setAdditionalPaths(true).build())
        .addEqualityGroup(builder.setAddressFamilies(ImmutableSet.of(Type.IPV4_UNICAST)).build())
        .addEqualityGroup(builder.setAdvertiseExternal(true).build())
        .addEqualityGroup(builder.setAdvertiseInactive(true).build())
        // note the head/tail swap
        .addEqualityGroup(builder.setHeadIp(tailIp).build())
        .addEqualityGroup(builder.setTailIp(headIp).build())
        .addEqualityGroup(builder.setSessionType(SessionType.EBGP_SINGLEHOP))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    Ip headIp = Ip.parse("1.1.1.1");
    Ip tailIp = Ip.parse("2.2.2.2");
    BgpSessionProperties bsp =
        BgpSessionProperties.builder().setTailIp(tailIp).setHeadIp(headIp).build();
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
            .setAdvertiseInactive(true)
            .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.instance());
    BgpActivePeerConfig peer1 = peerBuilder.build();

    // intersection with itself only contains ipv4
    assertThat(getAddressFamilyIntersection(peer1, peer1), contains(Type.IPV4_UNICAST));
    assertThat(
        getAddressFamilyIntersection(
            peer1,
            peerBuilder
                .setEvpnAddressFamily(new EvpnAddressFamily(ImmutableSet.of(), ImmutableSet.of()))
                .build()),
        contains(Type.IPV4_UNICAST));
    // Clear ipv4, should get empty intersection back
    assertThat(
        getAddressFamilyIntersection(peer1, peerBuilder.setIpv4UnicastAddressFamily(null).build()),
        empty());
  }
}
