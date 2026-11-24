package org.batfish.representation.juniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.junit.jupiter.api.Test;

/** Test for {@link FwFromDestinationAddressExcept} */
class FwFromDestinationAddressExceptTest {

  @Test
  void testToHeaderspace() {
    IpWildcard ips = IpWildcard.parse("1.1.1.0/24");
    FwFromDestinationAddressExcept from = new FwFromDestinationAddressExcept(ips, ips.toString());
    assertThat(
        from.toHeaderspace(), equalTo(HeaderSpace.builder().setNotDstIps(ips.toIpSpace()).build()));
  }
}
