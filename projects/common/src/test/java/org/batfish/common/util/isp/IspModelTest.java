package org.batfish.common.util.isp;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.isp_configuration.traffic_filtering.IspTrafficFiltering;
import org.junit.Test;

public class IspModelTest {

  @Test
  public void testEquals() {
    IspModel.Builder builder = IspModel.builder().setAsn(1L).setName("name");
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setAsn(2).build())
        .addEqualityGroup(builder.setName("other").build())
        .addEqualityGroup(
            builder
                .setSnapshotConnections(
                    new SnapshotConnection(
                        ImmutableList.of(),
                        IspBgpActivePeer.create(
                            BgpActivePeerConfig.builder()
                                .setLocalIp(Ip.ZERO)
                                .setPeerAddress(Ip.ZERO)
                                .setLocalAs(1L)
                                .setRemoteAs(2L)
                                .build())))
                .build())
        .addEqualityGroup(
            builder.setAdditionalPrefixesToInternet(Prefix.parse("1.1.1.1/32")).build())
        .addEqualityGroup(
            builder.setTrafficFiltering(IspTrafficFiltering.blockReservedAddressesAtInternet()))
        .testEquals();
  }
}
