package org.batfish.specifier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.regex.Pattern;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.junit.Test;

public class NameRegexRoutingPolicySpecifierTest {

  @Test
  public void testResolve() {

    Configuration node1 = new Configuration("node1", ConfigurationFormat.CISCO_IOS);
    Configuration node2 = new Configuration("node2", ConfigurationFormat.CISCO_IOS);

    RoutingPolicy routingPolicy1node1 =
        RoutingPolicy.builder().setName("routingPolicy1").setOwner(node1).build();
    RoutingPolicy routingPolicy2node1 =
        RoutingPolicy.builder().setName("routingPolicy2").setOwner(node1).build();
    RoutingPolicy routingPolicy1node2 =
        RoutingPolicy.builder().setName("routingPolicy1").setOwner(node2).build();

    node1.setRoutingPolicies(
        ImmutableSortedMap.of(
            routingPolicy1node1.getName(),
            routingPolicy1node1,
            routingPolicy2node1.getName(),
            routingPolicy2node1));
    node2.setRoutingPolicies(
        ImmutableSortedMap.of(routingPolicy1node2.getName(), routingPolicy1node2));

    MockSpecifierContext ctxt =
        MockSpecifierContext.builder()
            .setConfigs(ImmutableMap.of(node1.getHostname(), node1, node2.getHostname(), node2))
            .build();

    assertThat(
        new NameRegexRoutingPolicySpecifier(Pattern.compile("routingPolicy1"))
            .resolve("node1", ctxt),
        equalTo(ImmutableSet.of(routingPolicy1node1)));

    assertThat(
        new NameRegexRoutingPolicySpecifier(Pattern.compile("routingPolicy"))
            .resolve("node1", ctxt),
        equalTo(ImmutableSet.of(routingPolicy1node1, routingPolicy2node1)));

    // bad node name
    assertThat(
        new NameRegexRoutingPolicySpecifier(Pattern.compile("routingPolicy1"))
            .resolve("node", ctxt),
        equalTo(ImmutableSet.of()));

    // non-matching pattern
    assertThat(
        new NameRegexRoutingPolicySpecifier(Pattern.compile("routingPolicy3"))
            .resolve("node1", ctxt),
        equalTo(ImmutableSet.of()));
  }
}
