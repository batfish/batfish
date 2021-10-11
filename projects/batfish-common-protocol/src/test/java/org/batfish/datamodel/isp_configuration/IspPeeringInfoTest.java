package org.batfish.datamodel.isp_configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.isp_configuration.IspPeeringInfo.Peer;
import org.junit.Test;

public class IspPeeringInfoTest {

  @Test
  public void testEqualsIspPeeringInfo() {
    new EqualsTester()
        .addEqualityGroup(
            new IspPeeringInfo(new Peer(1), new Peer(2)),
            new IspPeeringInfo(new Peer(1), new Peer(2)))
        .addEqualityGroup(new IspPeeringInfo(new Peer(10), new Peer(2)))
        .addEqualityGroup(new IspPeeringInfo(new Peer(1), new Peer(20)))
        .testEquals();
  }

  @Test
  public void testEqualsPeer() {
    new EqualsTester()
        .addEqualityGroup(new Peer(1), new Peer(1))
        .addEqualityGroup(new Peer(2))
        .testEquals();
  }

  @Test
  public void testJsonSerializationIspPeeringInfo() {
    IspPeeringInfo ispPeeringInfo = new IspPeeringInfo(new Peer(1L), new Peer(2L));

    assertThat(
        BatfishObjectMapper.clone(ispPeeringInfo, IspPeeringInfo.class), equalTo(ispPeeringInfo));
  }

  @Test
  public void testJsonSerializationPeer() {
    Peer peer = new Peer(1L);
    assertThat(BatfishObjectMapper.clone(peer, Peer.class), equalTo(peer));
  }
}
