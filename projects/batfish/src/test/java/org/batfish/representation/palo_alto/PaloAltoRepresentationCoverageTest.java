package org.batfish.representation.palo_alto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix6;
import org.batfish.representation.palo_alto.InterfaceAddress.Type;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/** Focused tests for simple Palo Alto representation objects with low patch coverage. */
public final class PaloAltoRepresentationCoverageTest {
  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testBgpConnectionOptionsGettersSetters() {
    BgpConnectionOptions options = new BgpConnectionOptions();
    assertThat(options.getIncomingAllow(), nullValue());
    assertThat(options.getOutgoingAllow(), nullValue());
    assertThat(options.getLocalPort(), nullValue());
    assertThat(options.getRemotePort(), nullValue());
    assertThat(options.getHoldTime(), nullValue());
    assertThat(options.getIdleHoldTime(), nullValue());
    assertThat(options.getKeepAliveInterval(), nullValue());
    assertThat(options.getMinRouteAdvInterval(), nullValue());
    assertThat(options.getOpenDelayTime(), nullValue());

    options.setIncomingAllow(true);
    options.setOutgoingAllow(false);
    options.setLocalPort(123);
    options.setRemotePort(456);
    options.setHoldTime(30);
    options.setIdleHoldTime(40);
    options.setKeepAliveInterval(50);
    options.setMinRouteAdvInterval(60);
    options.setOpenDelayTime(70);

    assertThat(options.getIncomingAllow(), equalTo(true));
    assertThat(options.getOutgoingAllow(), equalTo(false));
    assertThat(options.getLocalPort(), equalTo(123));
    assertThat(options.getRemotePort(), equalTo(456));
    assertThat(options.getHoldTime(), equalTo(30));
    assertThat(options.getIdleHoldTime(), equalTo(40));
    assertThat(options.getKeepAliveInterval(), equalTo(50));
    assertThat(options.getMinRouteAdvInterval(), equalTo(60));
    assertThat(options.getOpenDelayTime(), equalTo(70));
  }

  @Test
  public void testBgpPeerGettersSettersAndDefaults() {
    BgpPeer peer = new BgpPeer("peer1");
    assertThat(peer.getName(), equalTo("peer1"));
    assertThat(peer.getEnable(), equalTo(false));
    assertThat(peer.getConnectionOptions(), notNullValue());
    assertThat(peer.getEnableSenderSideLoopDetection(), nullValue());
    assertThat(peer.getLocalAddress(), nullValue());
    assertThat(peer.getLocalInterface(), nullValue());
    assertThat(peer.getMultihop(), nullValue());
    assertThat(peer.getPeerAddress(), nullValue());
    assertThat(peer.getPeerAs(), nullValue());
    assertThat(peer.getReflectorClient(), nullValue());
    assertThat(peer.getBfdProfile(), nullValue());

    peer.setEnable(true);
    peer.setEnableSenderSideLoopDetection(true);
    peer.setLocalAddress(Ip.parse("1.1.1.1"));
    peer.setLocalInterface("ethernet1/1");
    peer.setMultihop(3);
    peer.setPeerAddress(Ip.parse("2.2.2.2"));
    peer.setPeerAs(65001L);
    peer.setReflectorClient(BgpPeer.ReflectorClient.CLIENT);
    peer.setBfdProfile("bfd-prof");

    assertThat(peer.getEnable(), equalTo(true));
    assertThat(peer.getEnableSenderSideLoopDetection(), equalTo(true));
    assertThat(peer.getLocalAddress(), equalTo(Ip.parse("1.1.1.1")));
    assertThat(peer.getLocalInterface(), equalTo("ethernet1/1"));
    assertThat(peer.getMultihop(), equalTo(3));
    assertThat(peer.getPeerAddress(), equalTo(Ip.parse("2.2.2.2")));
    assertThat(peer.getPeerAs(), equalTo(65001L));
    assertThat(peer.getReflectorClient(), equalTo(BgpPeer.ReflectorClient.CLIENT));
    assertThat(peer.getBfdProfile(), equalTo("bfd-prof"));
  }

  @Test
  public void testHighAvailabilityGettersSetters() {
    HighAvailability highAvailability = new HighAvailability();
    assertThat(highAvailability.getDeviceId(), nullValue());
    assertThat(highAvailability.getGroupId(), nullValue());
    assertThat(highAvailability.getEnabled(), nullValue());

    highAvailability.setDeviceId(1);
    highAvailability.setGroupId(200);
    highAvailability.setEnabled(true);

    assertThat(highAvailability.getDeviceId(), equalTo(1));
    assertThat(highAvailability.getGroupId(), equalTo(200));
    assertThat(highAvailability.getEnabled(), equalTo(true));
  }

  @Test
  public void testInterfaceGettersSettersAndAddresses() {
    Interface iface = new Interface("ethernet1/1", Interface.Type.PHYSICAL);
    assertThat(iface.getName(), equalTo("ethernet1/1"));
    assertThat(iface.getType(), equalTo(Interface.Type.PHYSICAL));
    assertThat(iface.getActive(), equalTo(true));
    assertThat(iface.getMtu(), equalTo(Interface.DEFAULT_INTERFACE_MTU));
    assertThat(iface.getAddress(), nullValue());
    assertThat(iface.getAllAddresses(), empty());
    assertThat(iface.getUnits().isEmpty(), equalTo(true));
    assertThat(iface.getComment(), nullValue());
    assertThat(iface.getAggregateGroup(), nullValue());
    assertThat(iface.getParent(), nullValue());
    assertThat(iface.getTag(), nullValue());
    assertThat(iface.getHa(), nullValue());
    assertThat(iface.getLldpEnabled(), nullValue());
    assertThat(iface.getNdpProxy(), nullValue());
    assertThat(iface.getRouterAdvertisement(), nullValue());
    assertThat(iface.getZone(), nullValue());

    InterfaceAddress addr1 = new InterfaceAddress(Type.IP_PREFIX, "10.0.0.1/24");
    InterfaceAddress addr2 = new InterfaceAddress(Type.REFERENCE, "foo");
    iface.addAddress(addr1);
    iface.addAddress(addr2);

    Interface parent = new Interface("ae1", Interface.Type.AGGREGATED_ETHERNET);
    Zone zone = new Zone("zone1", new Vsys("vsys1"));
    iface.setActive(false);
    iface.setComment("comment");
    iface.setAggregateGroup("ae1");
    iface.setMtu(9000);
    iface.setParent(parent);
    iface.setTag(100);
    iface.setHa(true);
    iface.setLldpEnabled(false);
    iface.setNdpProxy(true);
    iface.setRouterAdvertisement(false);
    iface.setZone(zone);
    iface.getUnits().put("ethernet1/1.1", new Interface("ethernet1/1.1", Interface.Type.LAYER3));

    assertThat(iface.getActive(), equalTo(false));
    assertThat(iface.getAddress(), equalTo(addr1));
    assertThat(iface.getAllAddresses(), contains(addr1, addr2));
    assertThat(iface.getComment(), equalTo("comment"));
    assertThat(iface.getAggregateGroup(), equalTo("ae1"));
    assertThat(iface.getMtu(), equalTo(9000));
    assertThat(iface.getParent(), equalTo(parent));
    assertThat(iface.getTag(), equalTo(100));
    assertThat(iface.getHa(), equalTo(true));
    assertThat(iface.getLldpEnabled(), equalTo(false));
    assertThat(iface.getNdpProxy(), equalTo(true));
    assertThat(iface.getRouterAdvertisement(), equalTo(false));
    assertThat(iface.getZone(), equalTo(zone));
    assertThat(iface.getUnits().keySet(), contains("ethernet1/1.1"));
  }

  @Test
  public void testIp6PrefixParseAndPreserveIp() {
    Ip6Prefix prefix = Ip6Prefix.parse("1:2:3:4::1/64");
    assertThat(prefix.getIp(), equalTo(Ip6.parse("1:2:3:4::1")));
    assertThat(prefix.getPrefix(), equalTo(Prefix6.parse("1:2:3:4::/64")));
  }

  @Test
  public void testIp6PrefixInvalidFormat() {
    _thrown.expect(IllegalArgumentException.class);
    Ip6Prefix.parse("1:2:3:4::1");
  }

  @Test
  public void testSnmpAccessSetting() {
    SnmpAccessSetting setting = new SnmpAccessSetting("v2c");
    assertThat(setting.getVersion(), equalTo("v2c"));
    assertThat(setting.getCommunityStrings(), empty());

    setting.addCommunityString("public");
    setting.addCommunityString("private");
    assertThat(setting.getCommunityStrings(), contains("public", "private"));
  }
}
