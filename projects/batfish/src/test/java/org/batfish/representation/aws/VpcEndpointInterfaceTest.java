package org.batfish.representation.aws;

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.parboiled.common.ImmutableList;

public class VpcEndpointInterfaceTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new VpcEndpointInterface("id", "vpc", ImmutableList.of(), ImmutableList.of()),
            new VpcEndpointInterface("id", "vpc", ImmutableList.of(), ImmutableList.of()))
        .addEqualityGroup(
            new VpcEndpointInterface("other", "vpc", ImmutableList.of(), ImmutableList.of()))
        .addEqualityGroup(
            new VpcEndpointInterface("id", "other", ImmutableList.of(), ImmutableList.of()))
        .addEqualityGroup(
            new VpcEndpointInterface("id", "vpc", ImmutableList.of("other"), ImmutableList.of()))
        .addEqualityGroup(
            new VpcEndpointInterface("id", "vpc", ImmutableList.of(), ImmutableList.of("other")))
        .testEquals();
  }
}
