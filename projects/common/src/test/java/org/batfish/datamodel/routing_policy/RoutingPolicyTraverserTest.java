package org.batfish.datamodel.routing_policy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableList;
import java.util.stream.Stream;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchBgpSessionType;
import org.batfish.datamodel.routing_policy.expr.MatchColor;
import org.batfish.datamodel.routing_policy.expr.MatchIpv4;
import org.batfish.datamodel.routing_policy.expr.MatchPeerAddress;
import org.batfish.datamodel.routing_policy.expr.MatchProcessAsn;
import org.batfish.datamodel.routing_policy.expr.MatchSourceProtocol;
import org.batfish.datamodel.routing_policy.expr.RouteIsClassful;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.Comment;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.RemoveTunnelEncapsulationAttribute;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.datamodel.routing_policy.statement.SetDefaultTag;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetVarMetricType;
import org.junit.Test;

public final class RoutingPolicyTraverserTest {

  private static final class PeerAddressDetector
      extends RoutingPolicyTraverser<Boolean, Configuration> {

    @Override
    protected Boolean defaultResult() {
      return false;
    }

    @Override
    protected Boolean combineResults(Stream<Boolean> results) {
      return results.anyMatch(Boolean::booleanValue);
    }

    @Override
    protected Boolean visitCalledPolicy(String policyName, Configuration configuration) {
      RoutingPolicy policy = configuration.getRoutingPolicies().get(policyName);
      return policy != null && visitStatements(policy.getStatements(), configuration);
    }

    @Override
    protected Boolean visitDefaultPolicy(String policyName, Configuration configuration) {
      return visitCalledPolicy(policyName, configuration);
    }

    @Override
    public Boolean visitMatchPeerAddress(
        MatchPeerAddress matchPeerAddress, Configuration configuration) {
      return true;
    }
  }

  @Test
  public void testCalledPolicy() {
    Configuration configuration = configuration();
    RoutingPolicy called =
        RoutingPolicy.builder()
            .setOwner(configuration)
            .setName("called")
            .addStatement(new If(new MatchPeerAddress(Ip.ZERO), ImmutableList.of()))
            .build();
    RoutingPolicy root =
        RoutingPolicy.builder()
            .setOwner(configuration)
            .setName("root")
            .addStatement(new CallStatement(called.getName()))
            .build();

    assertThat(
        new PeerAddressDetector().visitStatements(root.getStatements(), configuration), is(true));
  }

  @Test
  public void testDefaultPolicy() {
    Configuration configuration = configuration();
    RoutingPolicy defaultPolicy =
        RoutingPolicy.builder()
            .setOwner(configuration)
            .setName("default")
            .addStatement(new If(new MatchPeerAddress(Ip.ZERO), ImmutableList.of()))
            .build();
    RoutingPolicy root =
        RoutingPolicy.builder()
            .setOwner(configuration)
            .setName("root")
            .addStatement(new SetDefaultPolicy(defaultPolicy.getName()))
            .build();

    assertThat(
        new PeerAddressDetector().visitStatements(root.getStatements(), configuration), is(true));
  }

  @Test
  public void testDefaultResult() {
    Configuration configuration = configuration();
    RoutingPolicy policy =
        RoutingPolicy.builder()
            .setOwner(configuration)
            .setName("policy")
            .addStatement(new Comment("comment"))
            .addStatement(RemoveTunnelEncapsulationAttribute.instance())
            .addStatement(new SetDefaultTag(new LiteralLong(1L)))
            .addStatement(new SetMetric(new LiteralLong(1L)))
            .addStatement(new SetVarMetricType("metricType"))
            .addStatement(
                new If(
                    new MatchColor(1L),
                    ImmutableList.of(
                        new If(MatchIpv4.instance(), ImmutableList.of()),
                        new If(RouteIsClassful.instance(), ImmutableList.of()),
                        new If(
                            new MatchBgpSessionType(MatchBgpSessionType.Type.EBGP),
                            ImmutableList.of()),
                        new If(new MatchProcessAsn(1L), ImmutableList.of()),
                        new If(new MatchSourceProtocol(RoutingProtocol.BGP), ImmutableList.of()))))
            .build();

    assertThat(
        new PeerAddressDetector().visitStatements(policy.getStatements(), configuration),
        is(false));
  }

  private static Configuration configuration() {
    return Configuration.builder()
        .setHostname("hostname")
        .setConfigurationFormat(ConfigurationFormat.HOST)
        .build();
  }
}
