package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromPort} */
public class FwFromPortTest {
  @Test
  public void testToAclLineMatchExpr() {
    FwFromPort from = new FwFromPort(new SubRange(1, 2));
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP)
                .setSrcOrDstPorts(ImmutableList.of(new SubRange(1, 2)))
                .build(),
            TraceElement.of("Matched port 1-2")));
  }

  @Test
  public void testToAclLineMatchExpr_single() {
    FwFromPort from = new FwFromPort(SubRange.singleton(1));
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder()
                .setIpProtocols(IpProtocol.TCP, IpProtocol.UDP)
                .setSrcOrDstPorts(ImmutableList.of(SubRange.singleton(1)))
                .build(),
            TraceElement.of("Matched port 1")));
  }
}
