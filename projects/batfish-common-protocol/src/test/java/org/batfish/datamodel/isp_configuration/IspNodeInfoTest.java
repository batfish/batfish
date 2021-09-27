package org.batfish.datamodel.isp_configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.isp_configuration.traffic_filtering.IspTrafficFiltering;
import org.junit.Test;

/** Tests for {@link IspNodeInfo} */
public class IspNodeInfoTest {
  @Test
  public void testEquals() {
    List<IspAnnouncement> prefixList =
        ImmutableList.of(new IspAnnouncement(Prefix.parse("1.1.1.1/32")));
    new EqualsTester()
        .addEqualityGroup(
            new IspNodeInfo(42, "n1", prefixList, null),
            new IspNodeInfo(42L, "n1", prefixList, null))
        .addEqualityGroup(new IspNodeInfo(24, "n1", prefixList, null))
        .addEqualityGroup(new IspNodeInfo(42, "other", prefixList, null))
        .addEqualityGroup(
            new IspNodeInfo(
                42, "n1", ImmutableList.of(new IspAnnouncement(Prefix.parse("2.2.2.2/32"))), null))
        .addEqualityGroup(new IspNodeInfo(42, "n1", prefixList, IspTrafficFiltering.none()))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    IspNodeInfo ispNodeInfo =
        new IspNodeInfo(
            42,
            "n1",
            ImmutableList.of(new IspAnnouncement(Prefix.parse("1.1.1.1/32"))),
            IspTrafficFiltering.none());

    assertThat(BatfishObjectMapper.clone(ispNodeInfo, IspNodeInfo.class), equalTo(ispNodeInfo));
  }
}
