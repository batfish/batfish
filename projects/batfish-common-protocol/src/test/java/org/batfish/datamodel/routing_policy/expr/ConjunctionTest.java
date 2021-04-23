package org.batfish.datamodel.routing_policy.expr;

import static org.batfish.datamodel.routing_policy.expr.BooleanExprs.CALL_EXPR_CONTEXT;
import static org.batfish.datamodel.routing_policy.expr.BooleanExprs.CALL_STATEMENT_CONTEXT;
import static org.batfish.datamodel.routing_policy.expr.BooleanExprs.FALSE;
import static org.batfish.datamodel.routing_policy.expr.BooleanExprs.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link Conjunction} */
@RunWith(JUnit4.class)
public class ConjunctionTest {

  @Test
  public void simplifyEmpty() {
    Conjunction empty = new Conjunction();
    Assert.assertThat(empty.simplify(), is(TRUE));
  }

  @Test
  public void simplifyFalse() {
    Conjunction falseS = new Conjunction(ImmutableList.of(FALSE));
    Assert.assertThat(falseS.simplify(), is(FALSE));
  }

  @Test
  public void simplifyTrue() {
    Conjunction trueS = new Conjunction(ImmutableList.of(TRUE));
    Assert.assertThat(trueS.simplify(), is(TRUE));
  }

  @Test
  public void simplifySingleStatement() {
    Conjunction single = new Conjunction(ImmutableList.of(CALL_EXPR_CONTEXT));
    Assert.assertThat(single.simplify(), is(CALL_EXPR_CONTEXT));
  }

  @Test
  public void simplifySkipTrue() {
    Conjunction manyTrue = new Conjunction(ImmutableList.of(TRUE, TRUE, CALL_EXPR_CONTEXT, TRUE));
    Assert.assertThat(manyTrue.simplify(), is(CALL_EXPR_CONTEXT));
  }

  @Test
  public void simplifyTwoStatements() {
    Conjunction two = new Conjunction(ImmutableList.of(CALL_EXPR_CONTEXT, CALL_STATEMENT_CONTEXT));
    BooleanExpr simplified = two.simplify();
    Assert.assertThat(simplified, equalTo(two));
    Assert.assertThat(simplified.simplify(), is(simplified));
  }

  @Test
  public void simplifyShortCircuit() {
    Conjunction shortCircuit =
        new Conjunction(ImmutableList.of(CALL_EXPR_CONTEXT, FALSE, CALL_STATEMENT_CONTEXT));
    BooleanExpr simplified = shortCircuit.simplify();
    Assert.assertThat(simplified, instanceOf(Conjunction.class));
    List<BooleanExpr> conjuncts = ((Conjunction) simplified).getConjuncts();
    Assert.assertThat(conjuncts, equalTo(ImmutableList.of(CALL_EXPR_CONTEXT, FALSE)));
  }

  @Test
  public void simplifyInnerStatements() {
    Conjunction innerSimplifiable = new Conjunction(ImmutableList.of(new Conjunction()));
    assertThat(innerSimplifiable.simplify(), equalTo(BooleanExprs.TRUE));
  }
}
