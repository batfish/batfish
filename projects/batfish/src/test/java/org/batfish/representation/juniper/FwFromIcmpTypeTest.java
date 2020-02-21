package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromIcmpType} */
public class FwFromIcmpTypeTest {
  @Test
  public void testToAclLineMatchExpr() {
    FwFromIcmpType from = new FwFromIcmpType(new SubRange(1, 2));
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.ICMP)
                .setIcmpTypes(new SubRange(1, 2))
                .build(),
            TraceElement.of("Matched icmp-type 1-2")));
  }

  @Test
  public void testToAclLineMatchExpr_single() {
    FwFromIcmpType from = new FwFromIcmpType(SubRange.singleton(1));
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.ICMP)
                .setIcmpTypes(SubRange.singleton(1))
                .build(),
            TraceElement.of("Matched icmp-type 1")));
  }
}
