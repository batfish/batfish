package org.batfish.representation.juniper;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpWildcard;
import org.junit.Test;

/** Test for {@link FwFromDestinationAddress} */
public class FwFromDestinationAddressTest {

  @Test
  public void testToHeaderspace() {
    FwFromDestinationAddress from =
        new FwFromDestinationAddress(IpWildcard.parse("1.1.1.0/24"), "1.1.1.0/24");
    assertThat(
        from.toHeaderspace(),
        equalTo(
            HeaderSpace.builder().setDstIps(IpWildcard.parse("1.1.1.0/24").toIpSpace()).build()));
  }
}
