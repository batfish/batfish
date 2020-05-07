package org.batfish.representation.aws;

import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class VpcEndpointGatewayTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new VpcEndpointGateway("id", "vpc"), new VpcEndpointGateway("id", "vpc"))
        .addEqualityGroup(new VpcEndpointGateway("other", "vpc"))
        .addEqualityGroup(new VpcEndpointGateway("id", "other"))
        .testEquals();
  }
}
