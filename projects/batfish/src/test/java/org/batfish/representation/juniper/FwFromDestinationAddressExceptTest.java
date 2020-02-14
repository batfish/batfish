package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.ip.IpWildcard;
import org.batfish.datamodel.HeaderSpace;
import org.junit.Test;

/** Test for {@link FwFromDestinationAddressExcept} */
public class FwFromDestinationAddressExceptTest {

  @Test
  public void testToHeaderspace() {
    IpWildcard ips = IpWildcard.parse("1.1.1.0/24");
    FwFromDestinationAddressExcept from = new FwFromDestinationAddressExcept(ips);
    assertThat(
        from.toHeaderspace(), equalTo(HeaderSpace.builder().setNotDstIps(ips.toIpSpace()).build()));
  }
}
