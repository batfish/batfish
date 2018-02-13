package org.batfish.z3.expr.visitors;

import static com.google.common.collect.ImmutableList.of;
import static org.batfish.z3.expr.visitors.Simplifier.simplifyBooleanExpr;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.state.NumberedQuery;
import org.junit.Test;

public class SimplifierTest {

  private int _atomCounter;

  private BooleanExpr newAtom() {
    return new NumberedQuery(_atomCounter++);
  }

  @Test
  public void testSimplifyAndExprFalseConjunct() {
    BooleanExpr p1 = newAtom();
    AndExpr leftFalse = new AndExpr(of(FalseExpr.INSTANCE, p1));
    AndExpr rightFalse = new AndExpr(of(p1, FalseExpr.INSTANCE));

    assertThat(simplifyBooleanExpr(leftFalse), equalTo(FalseExpr.INSTANCE));
    assertThat(simplifyBooleanExpr(rightFalse), equalTo(FalseExpr.INSTANCE));
  }

  @Test
  public void testSimplifyAndExprNestedConjuncts() {
    BooleanExpr p1 = newAtom();
    BooleanExpr p2 = newAtom();
    BooleanExpr p3 = newAtom();
    AndExpr leftNested = new AndExpr(of(new AndExpr(of(p1, p2)), p3));
    AndExpr rightNested = new AndExpr(of(p1, new AndExpr(of(p2, p3))));

    assertThat(leftNested, not(equalTo(rightNested)));
    assertThat(simplifyBooleanExpr(leftNested), equalTo(new AndExpr(of(p1, p2, p3))));
    assertThat(simplifyBooleanExpr(rightNested), equalTo(new AndExpr(of(p1, p2, p3))));
  }

  @Test
  public void testSimplifyAndExprPreserveUnalteredInstance() {
    BooleanExpr p1 = newAtom();
    BooleanExpr p2 = newAtom();
    AndExpr and = new AndExpr(of(p1, p2));

    assertThat(simplifyBooleanExpr(and), sameInstance(and));
  }

  @Test
  public void testSimplifyAndExprTrueConjuncts() {
    BooleanExpr p1 = newAtom();
    AndExpr and = new AndExpr(of(TrueExpr.INSTANCE, p1, TrueExpr.INSTANCE));

    assertThat(simplifyBooleanExpr(and), equalTo(p1));
  }
}
