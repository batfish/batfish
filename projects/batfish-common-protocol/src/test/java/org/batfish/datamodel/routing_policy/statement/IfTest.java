package org.batfish.datamodel.routing_policy.statement;

import static org.batfish.datamodel.matchers.TraceTreeMatchers.isTraceTree;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.datamodel.trace.Tracer;
import org.junit.Test;

/** Test of {@link If} */
@ParametersAreNonnullByDefault
public final class IfTest {

  @Test
  public void testJavaSerialization() {
    assertThat(SerializationUtils.clone(IF), equalTo(IF));
  }

  @Test
  public void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(IF, If.class), equalTo(IF));
  }

  @Test
  public void testSimplifyBranches() {
    Statement complexBranchStatement =
        new If(
            BooleanExprs.TRUE,
            ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
            ImmutableList.of(Statements.ReturnFalse.toStaticStatement()));
    Statement simplifiedBranchStatement = Statements.ReturnTrue.toStaticStatement();
    If before =
        new If(
            BooleanExprs.CALL_EXPR_CONTEXT,
            ImmutableList.of(complexBranchStatement),
            ImmutableList.of(complexBranchStatement));
    assertThat(
        before.simplify(),
        contains(
            new If(
                BooleanExprs.CALL_EXPR_CONTEXT,
                ImmutableList.of(simplifiedBranchStatement),
                ImmutableList.of(simplifiedBranchStatement))));
  }

  @Test
  public void testSimplifyGuard() {
    assertThat(
        new If(
                new Disjunction(BooleanExprs.CALL_EXPR_CONTEXT),
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))
            .simplify(),
        contains(
            new If(
                BooleanExprs.CALL_EXPR_CONTEXT,
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))));
  }

  @Test
  public void testSimplifyToTrueBranch() {
    assertThat(
        new If(
                BooleanExprs.TRUE,
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))
            .simplify(),
        contains(Statements.ReturnTrue.toStaticStatement()));
  }

  @Test
  public void testSimplifyToFalseBranch() {
    assertThat(
        new If(
                BooleanExprs.FALSE,
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))
            .simplify(),
        contains(Statements.ReturnFalse.toStaticStatement()));
  }

  @Test
  public void testNoSimplifyImpureGuard() {
    If ifWithImpureGuard = new If(new CallExpr("foo"), ImmutableList.of(), ImmutableList.of());
    assertThat(ifWithImpureGuard.simplify(), contains(ifWithImpureGuard));
  }

  /* Test that tracing is not done if there is no tracer or no comment */
  @Test
  public void testExecute_noTracer() {
    Configuration c = Configuration.builder().setHostname("host").build();
    Tracer tracer = new Tracer();
    tracer.newSubTrace();

    Environment environment = Environment.builder(c).setTraceTree(tracer).build();
    IF.execute(environment);

    tracer.endSubTrace();

    assertThat(tracer.getTrace(), equalTo(ImmutableList.of()));
  }

  @Test
  public void testExecute() {
    // comment is set, guard is true -- should be traced
    assertThat(
        executeHelper(
            new If(
                "comment",
                BooleanExprs.TRUE,
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))),
        contains(isTraceTree("Matched 'comment'")));

    // no comment -- don't trace
    assertThat(executeHelper(IF), equalTo(ImmutableList.of()));

    // guard is false -- don't trace
    assertThat(
        executeHelper(
            new If(
                "comment",
                BooleanExprs.FALSE,
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))),
        equalTo(ImmutableList.of()));

    // no statements -- make sure that trace is ended (otherwise getTraceTree in helper will barf)
    assertThat(
        executeHelper(new If("comment", BooleanExprs.TRUE, ImmutableList.of(), ImmutableList.of())),
        contains(isTraceTree("Matched 'comment'")));
  }

  private static List<TraceTree> executeHelper(If ifStatement) {
    Configuration c = Configuration.builder().setHostname("host").build();
    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    Environment environment = Environment.builder(c).setTraceTree(tracer).build();
    ifStatement.execute(environment);
    tracer.endSubTrace();
    return tracer.getTrace();
  }

  private static final If IF =
      new If(
          BooleanExprs.TRUE,
          ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
          ImmutableList.of(Statements.ReturnFalse.toStaticStatement()));
}
