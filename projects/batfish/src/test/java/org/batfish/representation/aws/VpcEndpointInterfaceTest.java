package org.batfish.representation.aws;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.parboiled.common.ImmutableList;

public class VpcEndpointInterfaceTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new VpcEndpointInterface(
                "id", "vpc", ImmutableList.of(), ImmutableList.of(), ImmutableMap.of()),
            new VpcEndpointInterface(
                "id", "vpc", ImmutableList.of(), ImmutableList.of(), ImmutableMap.of()))
        .addEqualityGroup(
            new VpcEndpointInterface(
                "other", "vpc", ImmutableList.of(), ImmutableList.of(), ImmutableMap.of()))
        .addEqualityGroup(
            new VpcEndpointInterface(
                "id", "other", ImmutableList.of(), ImmutableList.of(), ImmutableMap.of()))
        .addEqualityGroup(
            new VpcEndpointInterface(
                "id", "vpc", ImmutableList.of("other"), ImmutableList.of(), ImmutableMap.of()))
        .addEqualityGroup(
            new VpcEndpointInterface(
                "id", "vpc", ImmutableList.of(), ImmutableList.of("other"), ImmutableMap.of()))
        .addEqualityGroup(
            new VpcEndpointInterface(
                "id", "vpc", ImmutableList.of(), ImmutableList.of(), ImmutableMap.of("tag", "tag")))
        .testEquals();
  }
}
