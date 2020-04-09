package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromPacketLength} */
public class FwFromPacketLengthTest {

  @Test
  public void testToAclLineMatchExpr() {
    List<SubRange> ranges = ImmutableList.of(SubRange.singleton(1), new SubRange(2, 3));
    FwFromPacketLength from = new FwFromPacketLength(ranges, false);

    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder().setPacketLengths(ranges).build(),
            TraceElement.of("Matched packet-length 1 2-3")));
  }

  @Test
  public void testToAclLineMatchExpr_except() {
    List<SubRange> ranges = ImmutableList.of(SubRange.singleton(1), new SubRange(2, 3));
    FwFromPacketLength from = new FwFromPacketLength(ranges, true);

    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder().setNotPacketLengths(ranges).build(),
            TraceElement.of("Matched packet-length 1 2-3 except")));
  }
}
