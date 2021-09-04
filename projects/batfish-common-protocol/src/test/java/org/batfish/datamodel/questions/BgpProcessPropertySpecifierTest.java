package org.batfish.datamodel.questions;

import static org.batfish.datamodel.questions.BgpProcessPropertySpecifier.isIpv4UnicastRouteReflector;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSortedMap;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpActivePeerConfig.Builder;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link BgpProcessPropertySpecifier}. */
@RunWith(JUnit4.class)
public class BgpProcessPropertySpecifierTest {
  @Test
  public void testIsRouteReflector() {
    BgpProcess emptyProcess = BgpProcess.testBgpProcess(Ip.ZERO);
    assertFalse("no rr clients", isIpv4UnicastRouteReflector(emptyProcess));

    Ipv4UnicastAddressFamily.Builder v4afBuilder = Ipv4UnicastAddressFamily.builder();
    Builder builder = BgpActivePeerConfig.builder();
    BgpActivePeerConfig activePeerWithRRC =
        builder
            .setIpv4UnicastAddressFamily(v4afBuilder.setRouteReflectorClient(true).build())
            .build();
    BgpActivePeerConfig activePeerWithoutRRC =
        builder
            .setIpv4UnicastAddressFamily(v4afBuilder.setRouteReflectorClient(false).build())
            .build();
    BgpPassivePeerConfig passivePeerWithRRC =
        BgpPassivePeerConfig.builder()
            .setIpv4UnicastAddressFamily(v4afBuilder.setRouteReflectorClient(true).build())
            .build();
    BgpPassivePeerConfig passivePeerWithoutRRC =
        BgpPassivePeerConfig.builder()
            .setIpv4UnicastAddressFamily(v4afBuilder.setRouteReflectorClient(false).build())
            .build();
    String peerInterface = "iface";
    BgpUnnumberedPeerConfig unnumberedPeerWithRRC =
        BgpUnnumberedPeerConfig.builder()
            .setPeerInterface(peerInterface)
            .setIpv4UnicastAddressFamily(v4afBuilder.setRouteReflectorClient(true).build())
            .build();
    Ip ipA = Ip.parse("1.2.3.4");
    Ip ipB = Ip.parse("1.2.3.5");
    Prefix p30a = Prefix.create(ipA, 30);
    Prefix p30b = Prefix.parse("1.2.3.8/30");

    // One active peer RRC
    BgpProcess hasActiveNeighbor = BgpProcess.testBgpProcess(Ip.ZERO);
    hasActiveNeighbor.setNeighbors(ImmutableSortedMap.of(ipA, activePeerWithRRC));
    assertTrue("has active rr client", isIpv4UnicastRouteReflector(hasActiveNeighbor));

    // One passive peer RRC
    BgpProcess hasPassiveNeighbor = BgpProcess.testBgpProcess(Ip.ZERO);
    hasPassiveNeighbor.setPassiveNeighbors(ImmutableSortedMap.of(p30a, passivePeerWithRRC));
    assertTrue("has passive rr client", isIpv4UnicastRouteReflector(hasPassiveNeighbor));

    // One unnumbered peer RRC
    BgpProcess hasUnnumberedNeighbor = BgpProcess.testBgpProcess(Ip.ZERO);
    hasUnnumberedNeighbor.setInterfaceNeighbors(
        ImmutableSortedMap.of(peerInterface, unnumberedPeerWithRRC));
    assertTrue("has unnumbered rr client", isIpv4UnicastRouteReflector(hasUnnumberedNeighbor));

    // Mix
    BgpProcess hasNeighborMix = BgpProcess.testBgpProcess(Ip.ZERO);
    hasNeighborMix.setNeighbors(
        ImmutableSortedMap.of(ipA, activePeerWithoutRRC, ipB, activePeerWithRRC));
    hasNeighborMix.setPassiveNeighbors(
        ImmutableSortedMap.of(p30a, passivePeerWithoutRRC, p30b, passivePeerWithRRC));
    assertTrue(
        "has mix of active and inactive rr client", isIpv4UnicastRouteReflector(hasNeighborMix));

    // Both inactive
    BgpProcess hasAllInactive = BgpProcess.testBgpProcess(Ip.ZERO);
    hasAllInactive.setNeighbors(ImmutableSortedMap.of(ipA, activePeerWithoutRRC));
    hasAllInactive.setPassiveNeighbors(ImmutableSortedMap.of(p30a, passivePeerWithoutRRC));
    assertFalse("has multiple inactive rr clients", isIpv4UnicastRouteReflector(hasAllInactive));
  }
}
