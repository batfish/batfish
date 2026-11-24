package org.batfish.representation.juniper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.jupiter.api.Test;

/** Test for {@link FwFromIcmpTypeExcept} */
class FwFromIcmpTypeExceptTest {
  @Test
  void testToAclLineMatchExpr() {
    FwFromIcmpTypeExcept from = new FwFromIcmpTypeExcept(new SubRange(1, 2));
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.ICMP)
                .setNotIcmpTypes(ImmutableList.of(new SubRange(1, 2)))
                .build(),
            TraceElement.of("Matched icmp-type-except 1-2")));
  }

  @Test
  void testToAclLineMatchExpr_single() {
    FwFromIcmpTypeExcept from = new FwFromIcmpTypeExcept(SubRange.singleton(1));
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.ICMP)
                .setNotIcmpTypes(ImmutableList.of(SubRange.singleton(1)))
                .build(),
            TraceElement.of("Matched icmp-type-except 1")));
  }
}
