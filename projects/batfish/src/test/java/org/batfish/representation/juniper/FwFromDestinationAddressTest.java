package org.batfish.representation.juniper;

import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.IpWildcard;
import org.junit.jupiter.api.Test;

/** Test for {@link FwFromDestinationAddress} */
class FwFromDestinationAddressTest {

  @Test
  void testToAclLineMatchExpr() {
    FwFromDestinationAddress from =
        new FwFromDestinationAddress(IpWildcard.parse("1.1.1.0/24"), "1.1.1.0/24");
    assertThat(
        from.toAclLineMatchExpr(null, null, null),
        equalTo(
            matchDst(
                IpWildcard.parse("1.1.1.0/24").toIpSpace(),
                "Matched destination-address 1.1.1.0/24")));
  }
}
