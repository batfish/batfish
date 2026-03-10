package org.batfish.specifier.parboiled;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.specifier.MockSpecifierContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ParboiledRoutingPolicySpecifierTest {

  private static final String _nodeName = "node0";
  private static final MockSpecifierContext _ctxt;

  private static final RoutingPolicy _routingPolicy1 =
      RoutingPolicy.builder().setName("routingPolicy1").build();
  private static final RoutingPolicy _routingPolicy2 =
      RoutingPolicy.builder().setName("routingPolicy2").build();

  static {
    NetworkFactory nf = new NetworkFactory();
    Configuration.Builder cb = nf.configurationBuilder().setHostname(_nodeName);
    cb.setConfigurationFormat(ConfigurationFormat.CISCO_IOS);

    Configuration n1 = cb.build();

    n1.getRoutingPolicies()
        .putAll(
            ImmutableMap.of(
                _routingPolicy1.getName(),
                _routingPolicy1,
                _routingPolicy2.getName(),
                _routingPolicy2));

    _ctxt = MockSpecifierContext.builder().setConfigs(ImmutableMap.of(_nodeName, n1)).build();
  }

  @Test
  public void testResolveDifference() {
    assertThat(
        new ParboiledRoutingPolicySpecifier(
                new DifferenceRoutingPolicyAstNode(
                    new NameRegexRoutingPolicyAstNode("routingPolicy.*"),
                    new NameRoutingPolicyAstNode("routingPolicy1")))
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of(_routingPolicy2)));
    assertThat(
        new ParboiledRoutingPolicySpecifier(
                new DifferenceRoutingPolicyAstNode(
                    new NameRoutingPolicyAstNode("routingPolicy1"),
                    new NameRegexRoutingPolicyAstNode("routingPolicy2.*")))
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of(_routingPolicy1)));
  }

  @Test
  public void testResolveIntersection() {
    assertThat(
        new ParboiledRoutingPolicySpecifier(
                new IntersectionRoutingPolicyAstNode(
                    new NameRegexRoutingPolicyAstNode("routingPolicy.*"),
                    new NameRoutingPolicyAstNode("routingPolicy1")))
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of(_routingPolicy1)));
    assertThat(
        new ParboiledRoutingPolicySpecifier(
                new IntersectionRoutingPolicyAstNode(
                    new NameRoutingPolicyAstNode("routingPolicy1"),
                    new NameRegexRoutingPolicyAstNode("routingPolicy2")))
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of()));
  }

  @Test
  public void testResolveName() {
    assertThat(
        new ParboiledRoutingPolicySpecifier(new NameRoutingPolicyAstNode("routingPolicy1"))
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of(_routingPolicy1)));
  }

  @Test
  public void testResolveNameRegex() {
    assertThat(
        new ParboiledRoutingPolicySpecifier(new NameRegexRoutingPolicyAstNode("routingPolicy.*2"))
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of(_routingPolicy2)));
  }

  @Test
  public void testResolveUnion() {
    assertThat(
        new ParboiledRoutingPolicySpecifier(
                new UnionRoutingPolicyAstNode(
                    new NameRoutingPolicyAstNode("routingPolicy1"),
                    new NameRoutingPolicyAstNode("routingPolicy2")))
            .resolve(_nodeName, _ctxt),
        equalTo(ImmutableSet.of(_routingPolicy1, _routingPolicy2)));
  }

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testParseBadInput() {
    _thrown.expect(IllegalArgumentException.class);
    _thrown.expectMessage("Error parsing");
    ParboiledRoutingPolicySpecifier.parse("@connected");
  }

  @Test
  public void testParseGoodInput() {
    assertThat(
        ParboiledRoutingPolicySpecifier.parse("router0"),
        equalTo(new ParboiledRoutingPolicySpecifier(new NameRoutingPolicyAstNode("router0"))));
  }
}
