package org.batfish.datamodel;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.ospf.Ospfv3Area;
import org.batfish.datamodel.ospf.Ospfv3AreaRange;
import org.junit.Test;

/** Tests for OSPFv3 area aggregation ranges. */
public final class Ospfv3AreaRangeTest {

  @Test
  public void testRangeSerialization() {
    Ospfv3AreaRange range =
        new Ospfv3AreaRange(
            Prefix6.parse(
                "2001:db8:100::/48"),
            Ospfv3AreaRange.Type.INTER_AREA,
            false);

    Ospfv3AreaRange clone =
        BatfishObjectMapper.clone(
            range,
            Ospfv3AreaRange.class);

    assertThat(
        clone,
        equalTo(range));
  }

  @Test
  public void testAreaRangeSerialization() {
    Ospfv3AreaRange advertised =
        new Ospfv3AreaRange(
            Prefix6.parse(
                "2001:db8:100::/48"),
            Ospfv3AreaRange.Type.INTER_AREA,
            true);

    Ospfv3AreaRange suppressed =
        new Ospfv3AreaRange(
            Prefix6.parse(
                "2001:db8:200::/48"),
            Ospfv3AreaRange.Type.INTER_AREA,
            false);

    Ospfv3Area area =
        Ospfv3Area.builder()
            .setNumber(1L)
            .addInterface("Ethernet1")
            .addRange(advertised)
            .addRange(suppressed)
            .build();

    Ospfv3Area clone =
        BatfishObjectMapper.clone(
            area,
            Ospfv3Area.class);

    assertThat(
        clone.getRanges(),
        equalTo(
            List.of(
                advertised,
                suppressed)));
  }
}
