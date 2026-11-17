package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchIpProtocol;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.TraceElement;
import org.junit.jupiter.api.Test;

/** Test for {@link FwFromProtocol} */
class FwFromProtocolTest {

  @Test
  void testToAclLineMatchExpr() {
    FwFromProtocol from = new FwFromProtocol(IpProtocol.TCP);
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        matchIpProtocol(IpProtocol.TCP, TraceElement.of("Matched protocol tcp")));
  }
}
