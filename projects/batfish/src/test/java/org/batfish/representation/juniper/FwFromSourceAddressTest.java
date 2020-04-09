package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromSourceAddress} */
public class FwFromSourceAddressTest {

  @Test
  public void testToAclLineMatchExpr() {
    IpWildcard ipWildcard = IpWildcard.parse("1.1.1.0/24");
    FwFromSourceAddress from = new FwFromSourceAddress(ipWildcard, "1.1.1.0/24-desc");
    assertEquals(
        from.toAclLineMatchExpr(null, null, null),
        new MatchHeaderSpace(
            HeaderSpace.builder().setSrcIps(ipWildcard.toIpSpace()).build(),
            TraceElement.of("Matched source-address 1.1.1.0/24-desc")));
  }
}
