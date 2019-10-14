package org.batfish.representation.cumulus;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.batfish.representation.cumulus.CumulusConversions.computeBgpGenerationPolicyName;
import static org.batfish.representation.cumulus.CumulusConversions.generateExportAggregateConditions;
import static org.batfish.representation.cumulus.CumulusConversions.generateGeneratedRoutes;
import static org.batfish.representation.cumulus.CumulusConversions.generateGenerationPolicy;
import static org.batfish.representation.cumulus.CumulusConversions.suppressSummarizedPrefixes;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.stream.Stream;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

/** Test for {@link CumulusConversions}. */
public final class CumulusConversionsTest {
  private NetworkFactory _nf;
  private Configuration _c;
  private Vrf _v;

  @Before
  public void setup() {
    _nf = new NetworkFactory();
    _c = _nf.configurationBuilder().build();
    _v = _nf.vrfBuilder().setOwner(_c).build();
  }

  private Environment finalEnvironment(Statement statement, String network) {
    RoutingPolicy policy =
        RoutingPolicy.builder(_nf).setOwner(_c).setStatements(ImmutableList.of(statement)).build();
    Environment env =
        Environment.builder(_c)
            .setOriginalRoute(
                Bgpv4Route.builder()
                    .setNetwork(Prefix.parse(network))
                    // Only network matters for these tests, but Bgp4Route requires these have
                    // values.
                    .setOriginatorIp(Ip.parse("1.1.1.1"))
                    .setOriginType(OriginType.IGP)
                    .setProtocol(RoutingProtocol.BGP)
                    .build())
            .build();
    policy.call(env);
    return env;
  }

  private boolean value(BooleanExpr expr, Environment env) {
    RoutingPolicy policy =
        RoutingPolicy.builder(_nf)
            .setOwner(_c)
            .setStatements(
                ImmutableList.of(
                    new If(
                        expr,
                        ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                        ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))))
            .build();
    return policy.call(env).getBooleanValue();
  }

  private boolean value(RoutingPolicy policy, String network) {
    return value(policy, Prefix.parse(network));
  }

  /**
   * Evaluate the policy in an environment with the original route being a static route with the
   * input network.
   */
  private boolean value(RoutingPolicy policy, Prefix network) {
    return policy
        .call(
            Environment.builder(_c)
                .setOriginalRoute(
                    StaticRoute.builder().setAdministrativeCost(0).setNetwork(network).build())
                .build())
        .getBooleanValue();
  }

  @Test
  public void testGenerateExportAggregateConditions() {
    BooleanExpr booleanExpr =
        generateExportAggregateConditions(
            ImmutableMap.of(
                Prefix.parse("1.2.3.0/24"),
                new BgpVrfAddressFamilyAggregateNetworkConfiguration()));

    // longer not exported
    {
      Environment env =
          Environment.builder(_c)
              .setOriginalRoute(
                  GeneratedRoute.builder().setNetwork(Prefix.parse("1.2.3.4/32")).build())
              .build();
      assertFalse(value(booleanExpr, env));
    }

    // shorter not exported
    {
      Environment env =
          Environment.builder(_c)
              .setOriginalRoute(
                  GeneratedRoute.builder().setNetwork(Prefix.parse("1.2.0.0/16")).build())
              .build();
      assertFalse(value(booleanExpr, env));
    }

    // exact match exported
    {
      Environment env =
          Environment.builder(_c)
              .setOriginalRoute(
                  GeneratedRoute.builder().setNetwork(Prefix.parse("1.2.3.0/24")).build())
              .build();
      assertTrue(value(booleanExpr, env));
    }
  }

  @Test
  public void testGenerateGeneratedRoutes() {
    Prefix prefix = Prefix.parse("1.2.3.0/24");
    generateGeneratedRoutes(
        _c, _v, ImmutableMap.of(prefix, new BgpVrfAddressFamilyAggregateNetworkConfiguration()));
    String policyName = computeBgpGenerationPolicyName(true, _v.getName(), prefix.toString());

    // configuration has the generation policy
    assertThat(_c.getRoutingPolicies(), Matchers.hasKey(policyName));

    // vrf has generated route
    ImmutableList<GeneratedRoute> grs =
        _v.getGeneratedRoutes().stream()
            .filter(gr -> gr.getNetwork().equals(prefix))
            .collect(ImmutableList.toImmutableList());
    assertThat(grs, hasSize(1));

    GeneratedRoute gr = grs.get(0);
    assertTrue(gr.getDiscard());
    assertThat(gr.getGenerationPolicy(), equalTo(policyName));
  }

  @Test
  public void testGenerateGenerationPolicy() {
    Prefix prefix = Prefix.parse("1.2.3.0/24");
    generateGenerationPolicy(_c, _v.getName(), prefix);

    RoutingPolicy policy =
        _c.getRoutingPolicies()
            .get(computeBgpGenerationPolicyName(true, _v.getName(), prefix.toString()));

    assertTrue(value(policy, "1.2.3.4/32"));
    assertFalse(value(policy, "1.2.3.0/24"));
    assertFalse(value(policy, "1.2.0.0/16"));
  }

  @Test
  public void testSuppressSummarizedPrefixes() {
    Prefix suppressedPrefix = Prefix.parse("1.2.3.0/24");
    If stmt = suppressSummarizedPrefixes(_c, _v.getName(), Stream.of(suppressedPrefix));
    assertNotNull(stmt);
    assertTrue(firstNonNull(finalEnvironment(stmt, "1.2.3.4/32").getSuppressed(), false));
    assertFalse(firstNonNull(finalEnvironment(stmt, "1.2.3.0/24").getSuppressed(), false));
    assertFalse(firstNonNull(finalEnvironment(stmt, "1.2.0.0/16").getSuppressed(), false));
  }
}
