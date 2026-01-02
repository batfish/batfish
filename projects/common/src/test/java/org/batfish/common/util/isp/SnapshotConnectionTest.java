package org.batfish.common.util.isp;

import static org.batfish.common.util.isp.IspModelingUtils.LINK_LOCAL_ADDRESS;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.common.topology.Layer1Node;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Ip;
import org.junit.Test;

public class SnapshotConnectionTest {

  @Test
  public void testEquals() {
    IspBgpActivePeer ispBgpActivePeer =
        IspBgpActivePeer.create(
            BgpActivePeerConfig.builder()
                .setLocalIp(Ip.ZERO)
                .setPeerAddress(Ip.ZERO)
                .setLocalAs(1L)
                .setRemoteAs(2L)
                .build());
    new EqualsTester()
        .addEqualityGroup(
            new SnapshotConnection(ImmutableList.of(), ispBgpActivePeer),
            new SnapshotConnection(ImmutableList.of(), ispBgpActivePeer))
        .addEqualityGroup(
            new SnapshotConnection(
                ImmutableList.of(
                    new IspInterface("name", LINK_LOCAL_ADDRESS, new Layer1Node("1", "2"), null)),
                ispBgpActivePeer))
        .addEqualityGroup(
            new SnapshotConnection(
                ImmutableList.of(),
                IspBgpActivePeer.create(
                    BgpActivePeerConfig.builder()
                        .setLocalIp(Ip.ZERO)
                        .setPeerAddress(Ip.ZERO)
                        .setLocalAs(2L)
                        .setRemoteAs(1L)
                        .build())))
        .testEquals();
  }
}
