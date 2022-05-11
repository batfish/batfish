package org.batfish.datamodel.isp_configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Ip;
import org.junit.Test;

public class BgpPeerInfoTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new BgpPeerInfo("host", null, Ip.ZERO, null, null),
            new BgpPeerInfo("host", null, Ip.ZERO, null, null),
            new BgpPeerInfo("HoSt", null, Ip.ZERO, null, null)) // hostname is canonicalized
        .addEqualityGroup(new BgpPeerInfo("other", null, Ip.ZERO, null, null))
        .addEqualityGroup(new BgpPeerInfo("other", Ip.MAX, Ip.ZERO, null, null))
        .addEqualityGroup(new BgpPeerInfo("host", null, Ip.MAX, null, null))
        .addEqualityGroup(new BgpPeerInfo("host", null, Ip.ZERO, "vrf", null))
        .addEqualityGroup(
            new BgpPeerInfo("host", null, Ip.ZERO, null, new IspAttachment(null, "iface", null)))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    BgpPeerInfo bgpPeerInfo = new BgpPeerInfo("host", null, Ip.ZERO, null, null);
    assertThat(BatfishObjectMapper.clone(bgpPeerInfo, BgpPeerInfo.class), equalTo(bgpPeerInfo));
  }
}
