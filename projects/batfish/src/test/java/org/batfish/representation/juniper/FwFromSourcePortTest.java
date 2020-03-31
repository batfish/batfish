package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromSourcePort} */
public class FwFromSourcePortTest {

  @Test
  public void testToAclLineMatchExpr() {
    FwFromSourcePort from = new FwFromSourcePort(new SubRange(1, 2));
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP)
                .setSrcPorts(new SubRange(1, 2))
                .build(),
            TraceElement.of("Matched source-port 1-2")));
  }

  @Test
  public void testToAclLineMatchExpr_single() {
    FwFromSourcePort from = new FwFromSourcePort(SubRange.singleton(1));
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP)
                .setSrcPorts(SubRange.singleton(1))
                .build(),
            TraceElement.of("Matched source-port 1")));
  }
}
