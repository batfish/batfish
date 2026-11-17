package org.batfish.representation.juniper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.jupiter.api.Test;

/** Test for {@link FwFromPacketLength} */
class FwFromPacketLengthTest {

  @Test
  void testToAclLineMatchExpr() {
    SubRange range = new SubRange(2, 3);
    FwFromPacketLength from = new FwFromPacketLength(range, false);

    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder().setPacketLengths(ImmutableList.of(range)).build(),
            TraceElement.of("Matched packet-length 2-3")));
  }

  @Test
  void testToAclLineMatchExpr_except() {
    SubRange range = SubRange.singleton(1);
    FwFromPacketLength from = new FwFromPacketLength(range, true);

    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder().setNotPacketLengths(ImmutableList.of(range)).build(),
            TraceElement.of("Matched packet-length 1 except")));
  }
}
