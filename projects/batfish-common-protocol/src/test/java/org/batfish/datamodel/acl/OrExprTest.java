package org.batfish.datamodel.acl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.matchers.AclLineMatchExprMatchers;
import org.junit.Test;

public class OrExprTest {
  private Flow createFlow() {
    Flow.Builder b = new Flow.Builder();
    b.setIngressNode("ingressNode");
    b.setTag("empty");
    return b.build();
  }

  @Test
  public void testSingleExpr() {
    // Test that if and only if the only ACL line is a match, the OrMatchExpr returns a match

    // Setup simple expression with a single true boolean expr
    List<AclLineMatchExpr> setTrue = ImmutableList.of(TrueExpr.TRUE_EXPR);
    OrMatchExpr exprTrue = new OrMatchExpr(setTrue);

    // Setup simple expression with a single false boolean expr
    List<AclLineMatchExpr> setFalse = ImmutableList.of(FalseExpr.FALSE_EXPR);
    OrMatchExpr exprFalse = new OrMatchExpr(setFalse);

    // Confirm true boolean expr matches
    assertThat(exprTrue, AclLineMatchExprMatchers.matches(createFlow(), "", ImmutableMap.of()));
    // Confirm false boolean expr does not match
    assertThat(
        exprFalse, not(AclLineMatchExprMatchers.matches(createFlow(), "", ImmutableMap.of())));
  }

  @Test
  public void testMultipleExprs() {
    // Test that if either of two ACL lines is a match, the OrMatchExpr returns a match

    List<AclLineMatchExpr> setTrueTrue = ImmutableList.of(TrueExpr.TRUE_EXPR, TrueExpr.TRUE_EXPR);
    OrMatchExpr exprTrueTrue = new OrMatchExpr(setTrueTrue);

    List<AclLineMatchExpr> setTrueFalse =
        ImmutableList.of(TrueExpr.TRUE_EXPR, FalseExpr.FALSE_EXPR);
    OrMatchExpr exprTrueFalse = new OrMatchExpr(setTrueFalse);

    List<AclLineMatchExpr> setFalseFalse =
        ImmutableList.of(FalseExpr.FALSE_EXPR, FalseExpr.FALSE_EXPR);
    OrMatchExpr exprFalseFalse = new OrMatchExpr(setFalseFalse);

    // Confirm boolean expr true OR true = true
    assertThat(exprTrueTrue, AclLineMatchExprMatchers.matches(createFlow(), "", ImmutableMap.of()));
    // Confirm boolean expr true OR false = true
    assertThat(
        exprTrueFalse, AclLineMatchExprMatchers.matches(createFlow(), "", ImmutableMap.of()));
    // Confirm boolean expr false OR false = false
    assertThat(
        exprFalseFalse, not(AclLineMatchExprMatchers.matches(createFlow(), "", ImmutableMap.of())));
  }
}
