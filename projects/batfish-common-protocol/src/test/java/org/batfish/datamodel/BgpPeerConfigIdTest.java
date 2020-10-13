package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link BgpPeerConfigId} */
public class BgpPeerConfigIdTest {

  @Test
  public void testEquals() {
    BgpPeerConfigId id = new BgpPeerConfigId("c1", "vrf1", Prefix.ZERO, false);
    new EqualsTester()
        .addEqualityGroup(id, id, new BgpPeerConfigId("c1", "vrf1", Prefix.ZERO, false))
        .addEqualityGroup(new BgpPeerConfigId("c2", "vrf1", Prefix.ZERO, false))
        .addEqualityGroup(new BgpPeerConfigId("c1", "vrf2", Prefix.ZERO, false))
        .addEqualityGroup(new BgpPeerConfigId("c1", "vrf1", Prefix.parse("1.1.1.1/24"), false))
        .addEqualityGroup(new BgpPeerConfigId("c1", "vrf1", Prefix.ZERO, true))
        .addEqualityGroup(new BgpPeerConfigId("c1", "vrf1", "peerIface"))
        .addEqualityGroup(new BgpPeerConfigId("c1", "vrf1", "peerIface2"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    BgpPeerConfigId id = new BgpPeerConfigId("c1", "vrf1", Prefix.ZERO, false);
    assertThat(BatfishObjectMapper.clone(id, BgpPeerConfigId.class), equalTo(id));
  }

  @Test
  public void testCompareTo() {
    List<BgpPeerConfigId> ordered =
        ImmutableList.of(
            new BgpPeerConfigId("c0", "vrf0", Prefix.ZERO, false),
            new BgpPeerConfigId("c1", "vrf0", Prefix.ZERO, false),
            new BgpPeerConfigId("c1", "vrf1", Prefix.ZERO, false),
            new BgpPeerConfigId("c1", "vrf1", Prefix.parse("1.1.1.1/24"), false),
            new BgpPeerConfigId("c1", "vrf1", Prefix.parse("1.1.1.1/24"), true),
            new BgpPeerConfigId("c2", "vrf1", Prefix.parse("1.1.1.1/24"), true),
            new BgpPeerConfigId("c2", "vrf2", Prefix.parse("1.1.1.1/24"), true),
            new BgpPeerConfigId("c2", "vrf2", Prefix.parse("2.2.2.2/24"), true),
            new BgpPeerConfigId("c2", "vrf2", "iface"),
            new BgpPeerConfigId("c3", "vrf2", "iface"),
            new BgpPeerConfigId("c3", "vrf3", "iface"),
            new BgpPeerConfigId("c3", "vrf3", "iface2"));
    for (int i = 0; i < ordered.size(); i++) {
      for (int j = 0; j < ordered.size(); j++) {
        assertThat(
            Integer.signum(ordered.get(i).compareTo(ordered.get(j))),
            equalTo(Integer.signum(i - j)));
      }
    }
  }
}
