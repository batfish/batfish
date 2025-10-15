package org.batfish.datamodel.routing_policy.expr;

import static org.batfish.datamodel.routing_policy.expr.BooleanExprs.CALL_EXPR_CONTEXT;
import static org.batfish.datamodel.routing_policy.expr.BooleanExprs.CALL_STATEMENT_CONTEXT;
import static org.batfish.datamodel.routing_policy.expr.BooleanExprs.FALSE;
import static org.batfish.datamodel.routing_policy.expr.BooleanExprs.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.datamodel.trace.Tracer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link Conjunction} */
@RunWith(JUnit4.class)
public class ConjunctionTest {

  @Test
  public void simplifyEmpty() {
    Conjunction empty = new Conjunction();
    assertThat(empty.simplify(), is(TRUE));
  }

  @Test
  public void simplifyFalse() {
    Conjunction falseS = new Conjunction(ImmutableList.of(FALSE));
    assertThat(falseS.simplify(), is(FALSE));
  }

  @Test
  public void simplifyTrue() {
    Conjunction trueS = new Conjunction(ImmutableList.of(TRUE));
    assertThat(trueS.simplify(), is(TRUE));
  }

  @Test
  public void simplifySingleStatement() {
    Conjunction single = new Conjunction(ImmutableList.of(CALL_EXPR_CONTEXT));
    assertThat(single.simplify(), is(CALL_EXPR_CONTEXT));
  }

  @Test
  public void simplifySkipTrue() {
    Conjunction manyTrue = new Conjunction(ImmutableList.of(TRUE, TRUE, CALL_EXPR_CONTEXT, TRUE));
    assertThat(manyTrue.simplify(), is(CALL_EXPR_CONTEXT));
  }

  @Test
  public void simplifyTwoStatements() {
    Conjunction two = new Conjunction(ImmutableList.of(CALL_EXPR_CONTEXT, CALL_STATEMENT_CONTEXT));
    BooleanExpr simplified = two.simplify();
    assertThat(simplified, equalTo(two));
    assertThat(simplified.simplify(), is(simplified));
  }

  @Test
  public void simplifyShortCircuit() {
    Conjunction shortCircuit =
        new Conjunction(ImmutableList.of(CALL_EXPR_CONTEXT, FALSE, CALL_STATEMENT_CONTEXT));
    BooleanExpr simplified = shortCircuit.simplify();
    assertThat(simplified, instanceOf(Conjunction.class));
    List<BooleanExpr> conjuncts = ((Conjunction) simplified).getConjuncts();
    assertThat(conjuncts, equalTo(ImmutableList.of(CALL_EXPR_CONTEXT, FALSE)));
  }

  @Test
  public void simplifyInnerStatements() {
    Conjunction innerSimplifiable = new Conjunction(ImmutableList.of(new Conjunction()));
    assertThat(innerSimplifiable.simplify(), equalTo(BooleanExprs.TRUE));
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
    // When a conjunct returns false, its trace should be discarded
    BooleanExpr trueExpr = tracedExpr("should-keep", new Result(true));
    BooleanExpr falseExpr = tracedExpr("should-discard", new Result(false));
    Conjunction conjunction = new Conjunction(ImmutableList.of(trueExpr, falseExpr));

    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    Configuration config = new Configuration("test", ConfigurationFormat.JUNIPER);
    Environment environment = Environment.builder(config).setTracer(tracer).build();

    Result result = conjunction.evaluate(environment);
    assertThat(result.getBooleanValue(), is(false));

    tracer.endSubTrace();
    List<TraceTree> trace = tracer.getTrace();

    // Should only have one trace element (the true one), false one should be discarded
    assertThat(trace, hasSize(1));
    assertThat(trace.get(0).getTraceElement().getText(), equalTo("should-keep"));
  }

  @Test
  public void testTraceManagement_TrueResultKeepsTrace() {
    // When all conjuncts return true, all traces should be kept
    BooleanExpr trueExpr1 = tracedExpr("first", new Result(true));
    BooleanExpr trueExpr2 = tracedExpr("second", new Result(true));
    Conjunction conjunction = new Conjunction(ImmutableList.of(trueExpr1, trueExpr2));

    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    Configuration config = new Configuration("test", ConfigurationFormat.JUNIPER);
    Environment environment = Environment.builder(config).setTracer(tracer).build();

    Result result = conjunction.evaluate(environment);
    assertThat(result.getBooleanValue(), is(true));

    tracer.endSubTrace();
    List<TraceTree> trace = tracer.getTrace();

    // Should have both trace elements
    assertThat(trace, hasSize(2));
    assertThat(trace.get(0).getTraceElement().getText(), equalTo("first"));
    assertThat(trace.get(1).getTraceElement().getText(), equalTo("second"));
  }

  @Test
  public void testTraceManagement_ExitResultKeepsTrace() {
    // When a conjunct exits, its trace should be kept
    BooleanExpr trueExpr = tracedExpr("before-exit", new Result(true));
    BooleanExpr exitExpr = tracedExpr("exit-statement", new Result(false, true, false, false));
    Conjunction conjunction = new Conjunction(ImmutableList.of(trueExpr, exitExpr));

    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    Configuration config = new Configuration("test", ConfigurationFormat.JUNIPER);
    Environment environment = Environment.builder(config).setTracer(tracer).build();

    Result result = conjunction.evaluate(environment);
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
    BooleanExpr falseExpr = tracedExpr("false", new Result(false));
    Conjunction conjunction = new Conjunction(ImmutableList.of(trueExpr, falseExpr));

    Configuration config = new Configuration("test", ConfigurationFormat.JUNIPER);
    Environment environment = Environment.builder(config).build(); // No tracer

    Result result = conjunction.evaluate(environment);
    assertThat(result.getBooleanValue(), is(false));
  }

  @Test
  public void testTraceManagement_FirstFalseDiscardsOnlyItsTrace() {
    // When the first conjunct returns false, only its trace should be discarded
    BooleanExpr falseExpr = tracedExpr("should-discard", new Result(false));
    BooleanExpr trueExpr = tracedExpr("should-not-evaluate", new Result(true));
    Conjunction conjunction = new Conjunction(ImmutableList.of(falseExpr, trueExpr));

    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    Configuration config = new Configuration("test", ConfigurationFormat.JUNIPER);
    Environment environment = Environment.builder(config).setTracer(tracer).build();

    Result result = conjunction.evaluate(environment);
    assertThat(result.getBooleanValue(), is(false));

    tracer.endSubTrace();
    List<TraceTree> trace = tracer.getTrace();

    // Should have no traces (first one failed and was discarded, second never evaluated)
    assertThat(trace, empty());
  }
}
