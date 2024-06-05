package org.batfish.datamodel.questions;

import static org.batfish.datamodel.questions.BgpPeerPropertySpecifier.SEND_COMMUNITY;
import static org.batfish.datamodel.questions.BgpPeerPropertySpecifier.getIsPassive;
import static org.batfish.datamodel.questions.BgpPeerPropertySpecifier.getPropertyDescriptor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpUnnumberedPeerConfig;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.EvpnAddressFamily;
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

  @Test
  public void testReportEVPNPropertiesIfIPv4NotPresent() {
    // Neither v4 nor EVPN defaults
    assertThat(
        getPropertyDescriptor(SEND_COMMUNITY)
            .getGetter()
            .apply(BgpActivePeerConfig.builder().build()),
        equalTo(false));

    // Neither EVPN present, overrides that default
    EvpnAddressFamily evpnAf =
        EvpnAddressFamily.builder()
            .setAddressFamilyCapabilities(
                AddressFamilyCapabilities.builder().setSendCommunity(true).build())
            .setPropagateUnmatched(true)
            .build();
    BgpActivePeerConfig c = BgpActivePeerConfig.builder().setEvpnAddressFamily(evpnAf).build();
    assertThat(getPropertyDescriptor(SEND_COMMUNITY).getGetter().apply(c), equalTo(true));
  }
}
