package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.testing.EqualsTester;
import java.util.List;
import java.util.NavigableMap;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Configuration.Builder;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.datamodel.trace.Tracer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link ConjunctionChain} */
@RunWith(JUnit4.class)
public class ConjunctionChainTest {

  private static Prefix NETWORK_1 = Prefix.parse("1.1.1.0/24");
  private static Prefix NETWORK_2 = Prefix.parse("2.2.2.0/24");
  private static Prefix NETWORK_3 = Prefix.parse("3.3.3.0/24");

  private static RoutingPolicy P1 =
      RoutingPolicy.builder()
          .setName("p1")
          .setStatements(
              ImmutableList.of(
                  new If(
                      new MatchTag(IntComparator.EQ, new LiteralLong(1)),
                      ImmutableList.of(Statements.ReturnTrue.toStaticStatement())),
                  new If(
                      new MatchTag(IntComparator.EQ, new LiteralLong(2)),
                      ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))))
          .build();
  private static RoutingPolicy P2 =
      RoutingPolicy.builder()
          .setName("p2")
          .setStatements(
              ImmutableList.of(
                  new If(
                      new MatchPrefixSet(
                          DestinationNetwork.instance(),
                          new ExplicitPrefixSet(
                              new PrefixSpace(PrefixRange.fromPrefix(NETWORK_1)))),
                      ImmutableList.of(Statements.ReturnTrue.toStaticStatement())),
                  new If(
                      new MatchPrefixSet(
                          DestinationNetwork.instance(),
                          new ExplicitPrefixSet(
                              new PrefixSpace(PrefixRange.fromPrefix(NETWORK_2)))),
                      ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))))
          .build();
  private static RoutingPolicy DEFAULT_POLICY =
      RoutingPolicy.builder()
          .setName("default-policy")
          .setStatements(
              ImmutableList.of(
                  new If(
                      BooleanExprs.TRUE, ImmutableList.of(new SetMetric(new LiteralLong(500L))))))
          .build();

  private static Environment buildEnvironment(
      NavigableMap<String, RoutingPolicy> policies, String defaultPolicy, AbstractRoute route) {
    NetworkFactory nf = new NetworkFactory();
    Builder cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.JUNIPER);
    Configuration c = cb.build();
    c.setVrfs(
        ImmutableMap.of(Configuration.DEFAULT_VRF_NAME, new Vrf(Configuration.DEFAULT_VRF_NAME)));
    c.setRoutingPolicies(policies);
    return Environment.builder(c)
        .setDefaultPolicy(defaultPolicy)
        .setOriginalRoute(route)
        .setOutputRoute(route.toBuilder())
        .build();
  }

  private static Environment buildEnvironmentWithTracer(
      NavigableMap<String, RoutingPolicy> policies,
      String defaultPolicy,
      AbstractRoute route,
      Tracer tracer) {
    NetworkFactory nf = new NetworkFactory();
    Builder cb = nf.configurationBuilder().setConfigurationFormat(ConfigurationFormat.JUNIPER);
    Configuration c = cb.build();
    c.setVrfs(
        ImmutableMap.of(Configuration.DEFAULT_VRF_NAME, new Vrf(Configuration.DEFAULT_VRF_NAME)));
    c.setRoutingPolicies(policies);
    return Environment.builder(c)
        .setDefaultPolicy(defaultPolicy)
        .setOriginalRoute(route)
        .setOutputRoute(route.toBuilder())
        .setTracer(tracer)
        .build();
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Object())
        .addEqualityGroup(
            new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE)),
            new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE)))
        .addEqualityGroup(ImmutableList.of())
        .addEqualityGroup(ImmutableList.of(BooleanExprs.FALSE))
        .testEquals();
  }

  @Test
  public void testJavaSerialization() {
    ConjunctionChain cc = new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE));
    assertThat(SerializationUtils.clone(cc), equalTo(cc));
  }

  @Test
  public void testJsonSerialization() {
    ConjunctionChain cc = new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE));
    assertThat(BatfishObjectMapper.clone(cc, ConjunctionChain.class), equalTo(cc));
  }

  @Test
  public void testEvaluate() {
    // Test that AND is evaluated correctly
    ConjunctionChain cc =
        new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE, BooleanExprs.FALSE));
    Result result =
        cc.evaluate(
            Environment.builder(new Configuration("host", ConfigurationFormat.JUNIPER)).build());
    assertThat(result, equalTo(new Result(false, false, false, false)));

    // result._return should not be true here because if there were more policies in the chain, it
    // should evaluate them too.
    cc = new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE, BooleanExprs.TRUE));
    result =
        cc.evaluate(
            Environment.builder(new Configuration("host", ConfigurationFormat.JUNIPER)).build());
    assertThat(result, equalTo(new Result(true, false, false, false)));
  }

  @Test
  public void testWithCallExprs() {
    /*
    Two policies in chain, plus a default policy:
    - P1 accepts routes with tag 1 and rejects routes with tag 2
    - P2 accepts routes with network NETWORK_1 and rejects routes with network NETWORK_2
    - Default policy sets route's metric to 500 (so we can ascertain whether a route has hit it)
     */
    ConjunctionChain conjunctionChain =
        new ConjunctionChain(
            ImmutableList.of(new CallExpr(P1.getName()), new CallExpr(P2.getName())));
    NavigableMap<String, RoutingPolicy> policiesMap =
        ImmutableSortedMap.of(
            P1.getName(), P1, P2.getName(), P2, DEFAULT_POLICY.getName(), DEFAULT_POLICY);
    StaticRoute.Builder srb = StaticRoute.testBuilder().setAdmin(5).setMetric(10L);

    // Route with tag 1 and NETWORK_1 should be accepted by both policies, no metric change
    Environment environment =
        buildEnvironment(
            policiesMap, DEFAULT_POLICY.getName(), srb.setTag(1L).setNetwork(NETWORK_1).build());
    Result result = conjunctionChain.evaluate(environment);
    assertThat(environment.getOutputRoute().getMetric(), equalTo(10L));
    assertThat(result, equalTo(new Result(true, false, false, false)));

    // Route with tag 2 and NETWORK_1 should be rejected by first policy, no metric change
    environment =
        buildEnvironment(
            policiesMap, DEFAULT_POLICY.getName(), srb.setTag(2L).setNetwork(NETWORK_1).build());
    result = conjunctionChain.evaluate(environment);
    assertThat(environment.getOutputRoute().getMetric(), equalTo(10L));
    assertThat(result, equalTo(new Result(false, false, false, false)));

    // Route with tag 1 and NETWORK_2 should be rejected by second policy, no metric change
    environment =
        buildEnvironment(
            policiesMap, DEFAULT_POLICY.getName(), srb.setTag(1L).setNetwork(NETWORK_2).build());
    result = conjunctionChain.evaluate(environment);
    assertThat(environment.getOutputRoute().getMetric(), equalTo(10L));
    assertThat(result, equalTo(new Result(false, false, false, false)));

    // Route with tag 3 and NETWORK_3 should fall through both policies, hit default, update metric
    environment =
        buildEnvironment(
            policiesMap, DEFAULT_POLICY.getName(), srb.setTag(3L).setNetwork(NETWORK_3).build());
    result = conjunctionChain.evaluate(environment);
    assertThat(environment.getOutputRoute().getMetric(), equalTo(500L));
    assertThat(result, equalTo(new Result(false, false, true, false)));
  }

  @Test
  public void testToString() {
    ConjunctionChain cc = new ConjunctionChain(ImmutableList.of(BooleanExprs.TRUE));
    assertThat(cc.toString(), equalTo("ConjunctionChain{subroutines=[True]}"));
  }

  /** Helper to create a BooleanExpr that sets a trace element and returns a given result */
  private static BooleanExpr tracedExpr(String traceName, Result result) {
    return new BooleanExpr() {
      @Override
      public Result evaluate(Environment environment) {
        Tracer tracer = environment.getTracer();
        if (tracer != null) {
          tracer.setTraceElement(TraceElement.of(traceName));
        }
        return result;
      }

      @Override
      public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
        throw new UnsupportedOperationException();
      }

      @Override
      public int hashCode() {
        return traceName.hashCode() * 31 + result.hashCode();
      }

      @Override
      public boolean equals(Object obj) {
        return obj == this;
      }
    };
  }

  @Test
  public void testTraceManagement_FalseResultDiscardsTrace() {
    // When a subroutine returns false without fall-through, its trace should be discarded
    BooleanExpr trueExpr = tracedExpr("should-keep", new Result(true));
    BooleanExpr falseExpr = tracedExpr("should-discard", new Result(false, false, false, false));
    ConjunctionChain chain = new ConjunctionChain(ImmutableList.of(trueExpr, falseExpr));

    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    Environment environment =
        buildEnvironmentWithTracer(
            ImmutableSortedMap.of(),
            "default",
            StaticRoute.testBuilder().setNetwork(NETWORK_1).setAdmin(1).build(),
            tracer);

    Result result = chain.evaluate(environment);
    assertThat(result.getBooleanValue(), is(false));

    tracer.endSubTrace();
    List<TraceTree> trace = tracer.getTrace();

    // Should only have one trace element (the true one), false one should be discarded
    assertThat(trace, hasSize(1));
    assertThat(trace.get(0).getTraceElement().getText(), equalTo("should-keep"));
  }

  @Test
  public void testTraceManagement_TrueResultKeepsTrace() {
    // When subroutines return true with fall-through, traces should be kept
    // Use fall-through=true so they proceed to the next subroutine
    BooleanExpr fallThroughExpr1 = tracedExpr("first", new Result(true, false, true, false));
    BooleanExpr exitExpr = tracedExpr("second-exit", new Result(false, true, false, false));
    ConjunctionChain chain = new ConjunctionChain(ImmutableList.of(fallThroughExpr1, exitExpr));

    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    Environment environment =
        buildEnvironmentWithTracer(
            ImmutableSortedMap.of(),
            "default",
            StaticRoute.testBuilder().setNetwork(NETWORK_1).setAdmin(1).build(),
            tracer);

    Result result = chain.evaluate(environment);
    assertThat(result.getExit(), is(true));

    tracer.endSubTrace();
    List<TraceTree> trace = tracer.getTrace();

    // Both should be kept - first had fall-through, second had exit
    assertThat(trace, hasSize(2));
    assertThat(trace.get(0).getTraceElement().getText(), equalTo("first"));
    assertThat(trace.get(1).getTraceElement().getText(), equalTo("second-exit"));
  }

  @Test
  public void testTraceManagement_ExitResultKeepsTrace() {
    // When a subroutine exits, its trace should be kept
    BooleanExpr fallThroughExpr = tracedExpr("before-exit", new Result(true, false, true, false));
    BooleanExpr exitExpr = tracedExpr("exit-statement", new Result(false, true, false, false));
    ConjunctionChain chain = new ConjunctionChain(ImmutableList.of(fallThroughExpr, exitExpr));

    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    Environment environment =
        buildEnvironmentWithTracer(
            ImmutableSortedMap.of(),
            "default",
            StaticRoute.testBuilder().setNetwork(NETWORK_1).setAdmin(1).build(),
            tracer);

    Result result = chain.evaluate(environment);
    assertThat(result.getExit(), is(true));

    tracer.endSubTrace();
    List<TraceTree> trace = tracer.getTrace();

    // Should have both trace elements (exit results are kept)
    assertThat(trace, hasSize(2));
    assertThat(trace.get(0).getTraceElement().getText(), equalTo("before-exit"));
    assertThat(trace.get(1).getTraceElement().getText(), equalTo("exit-statement"));
  }

  @Test
  public void testTraceManagement_NoTracerDoesNotCrash() {
    // When tracer is null, evaluation should still work
    BooleanExpr trueExpr = tracedExpr("true", new Result(true));
    BooleanExpr falseExpr = tracedExpr("false", new Result(false, false, false, false));
    ConjunctionChain chain = new ConjunctionChain(ImmutableList.of(trueExpr, falseExpr));

    Environment environment =
        buildEnvironment(
            ImmutableSortedMap.of(),
            "default",
            StaticRoute.testBuilder().setNetwork(NETWORK_1).setAdmin(1).build());

    Result result = chain.evaluate(environment);
    assertThat(result.getBooleanValue(), is(false));
  }

  @Test
  public void testTraceManagement_FirstFalseDiscardsOnlyItsTrace() {
    // When the first subroutine returns false, only its trace should be discarded
    BooleanExpr falseExpr = tracedExpr("should-discard", new Result(false, false, false, false));
    BooleanExpr trueExpr = tracedExpr("should-not-evaluate", new Result(true));
    ConjunctionChain chain = new ConjunctionChain(ImmutableList.of(falseExpr, trueExpr));

    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    Environment environment =
        buildEnvironmentWithTracer(
            ImmutableSortedMap.of(),
            "default",
            StaticRoute.testBuilder().setNetwork(NETWORK_1).setAdmin(1).build(),
            tracer);

    Result result = chain.evaluate(environment);
    assertThat(result.getBooleanValue(), is(false));

    tracer.endSubTrace();
    List<TraceTree> trace = tracer.getTrace();

    // Should have no traces (first one failed and was discarded, second never evaluated)
    assertThat(trace, empty());
  }

  @Test
  public void testTraceManagement_FallThroughKeepsTrace() {
    // When subroutines return with fall-through, traces should be kept
    BooleanExpr fallThroughExpr1 =
        tracedExpr("first-fallthrough", new Result(true, false, true, false));
    BooleanExpr fallThroughExpr2 =
        tracedExpr("second-fallthrough", new Result(true, false, true, false));
    ConjunctionChain chain =
        new ConjunctionChain(ImmutableList.of(fallThroughExpr1, fallThroughExpr2));

    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    Environment environment =
        buildEnvironmentWithTracer(
            ImmutableSortedMap.of("default", DEFAULT_POLICY),
            "default",
            StaticRoute.testBuilder().setNetwork(NETWORK_1).setAdmin(1).build(),
            tracer);

    // Will fall through to default policy
    chain.evaluate(environment);

    tracer.endSubTrace();
    List<TraceTree> trace = tracer.getTrace();

    // Should have traces from both fall-through subroutines (they had fall-through=true)
    assertThat(trace, hasSize(2));
    assertThat(trace.get(0).getTraceElement().getText(), equalTo("first-fallthrough"));
    assertThat(trace.get(1).getTraceElement().getText(), equalTo("second-fallthrough"));
  }
}
