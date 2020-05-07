package org.batfish.representation.aws;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class VpcEndpointGatewayTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new VpcEndpointGateway("id", "vpc", ImmutableMap.of()),
            new VpcEndpointGateway("id", "vpc", ImmutableMap.of()))
        .addEqualityGroup(new VpcEndpointGateway("other", "vpc", ImmutableMap.of()))
        .addEqualityGroup(new VpcEndpointGateway("id", "other", ImmutableMap.of()))
        .addEqualityGroup(new VpcEndpointGateway("id", "vpc", ImmutableMap.of("tag", "tag")))
        .testEquals();
  }
}
