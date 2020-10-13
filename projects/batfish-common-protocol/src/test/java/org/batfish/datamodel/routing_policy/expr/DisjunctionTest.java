package org.batfish.datamodel.routing_policy.expr;

import static org.batfish.datamodel.routing_policy.expr.BooleanExprs.CALL_EXPR_CONTEXT;
import static org.batfish.datamodel.routing_policy.expr.BooleanExprs.CALL_STATEMENT_CONTEXT;
import static org.batfish.datamodel.routing_policy.expr.BooleanExprs.FALSE;
import static org.batfish.datamodel.routing_policy.expr.BooleanExprs.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link Disjunction}. */
@RunWith(JUnit4.class)
public class DisjunctionTest {

  @Test
  public void simplifyEmpty() {
    Disjunction empty = new Disjunction();
    assertThat(empty.simplify(), is(FALSE));
  }

  @Test
  public void simplifyFalse() {
    Disjunction falseS = new Disjunction(ImmutableList.of(FALSE));
    assertThat(falseS.simplify(), is(FALSE));
  }

  @Test
  public void simplifyTrue() {
    Disjunction trueS = new Disjunction(ImmutableList.of(TRUE));
    assertThat(trueS.simplify(), is(TRUE));
  }

  @Test
  public void simplifySingleStatement() {
    Disjunction single = new Disjunction(ImmutableList.of(CALL_EXPR_CONTEXT));
    assertThat(single.simplify(), is(CALL_EXPR_CONTEXT));
  }

  @Test
  public void simplifySkipFalse() {
    Disjunction manyFalse =
        new Disjunction(ImmutableList.of(FALSE, FALSE, CALL_EXPR_CONTEXT, FALSE));
    assertThat(manyFalse.simplify(), is(CALL_EXPR_CONTEXT));
  }

  @Test
  public void simplifyTwoStatements() {
    Disjunction two = new Disjunction(ImmutableList.of(CALL_EXPR_CONTEXT, CALL_STATEMENT_CONTEXT));
    BooleanExpr simplified = two.simplify();
    assertThat(simplified, allOf(instanceOf(Disjunction.class), equalTo(two)));
    assertThat(simplified.simplify(), is(simplified));
  }

  @Test
  public void simplifyShortCircuit() {
    Disjunction shortCircuit =
        new Disjunction(ImmutableList.of(CALL_EXPR_CONTEXT, TRUE, CALL_STATEMENT_CONTEXT));
    BooleanExpr simplified = shortCircuit.simplify();
    assertThat(simplified, instanceOf(Disjunction.class));
    List<BooleanExpr> disjuncts = ((Disjunction) simplified).getDisjuncts();
    assertThat(disjuncts, equalTo(ImmutableList.of(CALL_EXPR_CONTEXT, TRUE)));
  }
}
