package org.batfish.representation.juniper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.jupiter.api.Test;

/** Test for {@link FwFromSourceAddressExcept} */
class FwFromSourceAddressExceptTest {

  @Test
  void testToAclLineMatchExpr() {
    IpWildcard ipWildcard = IpWildcard.parse("1.1.1.0/24");
    FwFromSourceAddressExcept from =
        new FwFromSourceAddressExcept(ipWildcard, ipWildcard.toString());
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder().setNotSrcIps(ipWildcard.toIpSpace()).build(),
            TraceElement.of("Matched source-address 1.1.1.0/24 except")));
  }
}
