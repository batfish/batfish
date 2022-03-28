package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromIcmpCodeExcept} */
public class FwFromIcmpCodeExceptTest {

  @Test
  public void testToAclLineMatchExpr() {
    FwFromIcmpCodeExcept from = new FwFromIcmpCodeExcept(new SubRange(1, 2));
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.ICMP)
                .setNotIcmpCodes(ImmutableList.of(new SubRange(1, 2)))
                .build(),
            TraceElement.of("Matched icmp-code-except 1-2")));
  }
}
