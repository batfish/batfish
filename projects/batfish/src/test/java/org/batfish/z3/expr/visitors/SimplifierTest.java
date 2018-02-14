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
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.state.NumberedQuery;
import org.junit.Test;

public class SimplifierTest {

  private int _atomCounter;

  private BooleanExpr newAtom() {
    return new NumberedQuery(_atomCounter++);
  }

  /** Test that we keep simplifying until no more simplifications are possible. */
  @Test
  public void testChainedSimplifications() {
    OrExpr or = new OrExpr(of(new OrExpr(of(new OrExpr(of())))));
    assertThat(simplifyBooleanExpr(or), equalTo(FalseExpr.INSTANCE));
  }

  /**
   * Test that an AND node with a single child (other than TRUE or FALSE) simplifies to that child.
   */
  @Test
  public void testSimplifyAnd1() {
    BooleanExpr p1 = newAtom();
    AndExpr and = new AndExpr(of(p1));
    assertThat(simplifyBooleanExpr(and), equalTo(p1));
  }

  /** Test simplifications: false AND E --> false E AND false --> false */
  @Test
  public void testSimplifyAndExprFalseConjunct() {
    BooleanExpr p1 = newAtom();
    AndExpr leftFalse = new AndExpr(of(FalseExpr.INSTANCE, p1));
    AndExpr rightFalse = new AndExpr(of(p1, FalseExpr.INSTANCE));

    assertThat(simplifyBooleanExpr(leftFalse), equalTo(FalseExpr.INSTANCE));
    assertThat(simplifyBooleanExpr(rightFalse), equalTo(FalseExpr.INSTANCE));
  }

  /** Test that nested ANDs get flattened into one AND. */
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

  /** Test that an unsimplifiable expression is unchanged by simplification. */
  @Test
  public void testSimplifyAndExprPreserveUnalteredInstance() {
    BooleanExpr p1 = newAtom();
    BooleanExpr p2 = newAtom();
    AndExpr and = new AndExpr(of(p1, p2));

    assertThat(simplifyBooleanExpr(and), sameInstance(and));
  }

  /** Test that any TRUE children of an AND node are removed. */
  @Test
  public void testSimplifyAndTrue() {
    BooleanExpr p1 = newAtom();
    BooleanExpr p2 = newAtom();
    AndExpr and = new AndExpr(of(TrueExpr.INSTANCE, p1, TrueExpr.INSTANCE, p2));

    assertThat(simplifyBooleanExpr(and), equalTo(new AndExpr(of(p1, p2))));
  }

  /** Test that an empty AND node is simplified to true (the identity under AND). */
  @Test
  public void testSimplifyEmptyAnd() {
    AndExpr and = new AndExpr(of());
    assertThat(simplifyBooleanExpr(and), equalTo(TrueExpr.INSTANCE));
  }

  /** Test that an empty OR node is simplified to false (the identity under OR). */
  @Test
  public void testSimplifyEmptyOr() {
    OrExpr or = new OrExpr(of());
    assertThat(simplifyBooleanExpr(or), equalTo(FalseExpr.INSTANCE));
  }

  /**
   * Test that an OR node with a single child (other than TRUE or FALSE) simplifies to that child.
   */
  @Test
  public void testSimplifyOr1() {
    BooleanExpr p1 = newAtom();
    OrExpr or = new OrExpr(of(p1));
    assertThat(simplifyBooleanExpr(or), equalTo(p1));
  }

  /** Test that any FALSE children of an OR node are removed. */
  @Test
  public void testSimplifyOrFalse() {
    BooleanExpr p1 = newAtom();
    BooleanExpr p2 = newAtom();
    OrExpr and = new OrExpr(of(FalseExpr.INSTANCE, p1, FalseExpr.INSTANCE, p2));

    assertThat(simplifyBooleanExpr(and), equalTo(new OrExpr(of(p1, p2))));
  }

  /** Test that an OR node with a TRUE child simplifies to TRUE. */
  @Test
  public void testSimplifyOrTrue() {
    BooleanExpr p1 = newAtom();
    OrExpr or = new OrExpr(of(p1, TrueExpr.INSTANCE));
    assertThat(simplifyBooleanExpr(or), equalTo(TrueExpr.INSTANCE));
  }
}
