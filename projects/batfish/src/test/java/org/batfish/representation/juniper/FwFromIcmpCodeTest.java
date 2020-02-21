package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromIcmpCode} */
public class FwFromIcmpCodeTest {

  @Test
  public void testToAclLineMatchExpr() {
    FwFromIcmpCode from = new FwFromIcmpCode(new SubRange(1, 2));
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.ICMP)
                .setIcmpCodes(new SubRange(1, 2))
                .build(),
            TraceElement.of("Matched icmp-code 1-2")));
  }
}
