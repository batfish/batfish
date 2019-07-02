package org.batfish.datamodel.questions;

import static org.batfish.datamodel.questions.BgpPeerPropertySpecifier.getIsPassive;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.junit.Test;

public class BgpPeerPropertySpecifierTest {

  @Test
  public void getIsPassiveTest() {
    assertFalse(
        getIsPassive(
            BgpActivePeerConfig.builder()
                .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
                .build()));
    assertFalse(
        getIsPassive(
            BgpUnnumberedPeerConfig.builder()
                .setPeerInterface("i")
                .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
                .build()));
    assertTrue(
        getIsPassive(
            BgpPassivePeerConfig.builder()
                .setIpv4UnicastAddressFamily(Ipv4UnicastAddressFamily.builder().build())
                .build()));
  }
}
