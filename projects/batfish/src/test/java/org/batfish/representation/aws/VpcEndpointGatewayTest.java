package org.batfish.representation.aws;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.junit.Test;

public class VpcEndpointGatewayTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new VpcEndpointGateway("id", "service", "vpc", ImmutableMap.of()),
            new VpcEndpointGateway("id", "service", "vpc", ImmutableMap.of()))
        .addEqualityGroup(new VpcEndpointGateway("other", "service", "vpc", ImmutableMap.of()))
        .addEqualityGroup(new VpcEndpointGateway("id", "other", "vpc", ImmutableMap.of()))
        .addEqualityGroup(new VpcEndpointGateway("id", "service", "other", ImmutableMap.of()))
        .addEqualityGroup(
            new VpcEndpointGateway("id", "service", "vpc", ImmutableMap.of("tag", "tag")))
        .testEquals();
  }
}
