package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromProtocol} */
public class FwFromProtocolTest {

  @Test
  public void testToAclLineMatchExpr() {
    FwFromProtocol from = new FwFromProtocol(IpProtocol.TCP);
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder().setIpProtocols(IpProtocol.TCP).build(),
            TraceElement.of("Matched protocol tcp")));
  }
}
