package org.batfish.datamodel.acl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.lessThan;

import org.junit.Test;

public class AclLineMatchExprTest {

  private static final AclLineMatchExpr[] EXPRS = {
    new AndMatchExpr(null, null),
    new AndMatchExpr(null, "a"),
    new AndMatchExpr(null, "b"),
    FalseExpr.INSTANCE,
  };

  @Test
  public void testCompareToForDifferentVals() {
    for (int i = 0; i + 1 < EXPRS.length; i++) {
      for (int j = i + 1; j < EXPRS.length; j++) {
        assertThat(EXPRS[i].compareTo(EXPRS[j]), lessThan(0));
      }
    }
  }

  @Test
  public void testCompareToForEqualVals() {
    // Test equivalent expressions with both non-null and null descriptions
    assertThat(new AndMatchExpr(null, "x").compareTo(new AndMatchExpr(null, "x")), equalTo(0));
    assertThat(new AndMatchExpr(null, null).compareTo(new AndMatchExpr(null, null)), equalTo(0));
  }
}
