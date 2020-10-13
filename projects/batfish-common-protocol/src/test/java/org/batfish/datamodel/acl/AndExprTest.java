package org.batfish.datamodel.acl;

import static org.batfish.datamodel.matchers.AclLineMatchExprMatchers.matches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.Flow;
import org.junit.Test;

public class AndExprTest {
  private static Flow createFlow() {
    Flow.Builder b = Flow.builder();
    b.setIngressNode("ingressNode");
    return b.build();
  }

  @Test
  public void testSingleExpr() {
    // Test that if and only if the only ACL line is a match, the AndMatchExpr indicates a match

    // Setup simple expression with a single true boolean expr
    List<AclLineMatchExpr> setTrue = ImmutableList.of(TrueExpr.INSTANCE);
    AndMatchExpr exprTrue = new AndMatchExpr(setTrue);

    // Setup simple expression with a single false boolean expr
    List<AclLineMatchExpr> setFalse = ImmutableList.of(FalseExpr.INSTANCE);
    AndMatchExpr exprFalse = new AndMatchExpr(setFalse);

    // Confirm true boolean expr matches
    assertThat(exprTrue, matches(createFlow(), ""));
    // Confirm false boolean expr does not match
    assertThat(exprFalse, not(matches(createFlow(), "")));
  }

  @Test
  public void testMultipleExprs() {
    // Test that if and only if all ACL lines are a match, the AndMatchExpr returns a match

    List<AclLineMatchExpr> setTrueTrue = ImmutableList.of(TrueExpr.INSTANCE, TrueExpr.INSTANCE);
    AndMatchExpr exprTrueTrue = new AndMatchExpr(setTrueTrue);

    List<AclLineMatchExpr> setTrueFalse = ImmutableList.of(TrueExpr.INSTANCE, FalseExpr.INSTANCE);
    AndMatchExpr exprTrueFalse = new AndMatchExpr(setTrueFalse);

    List<AclLineMatchExpr> setFalseFalse = ImmutableList.of(FalseExpr.INSTANCE, FalseExpr.INSTANCE);
    AndMatchExpr exprFalseFalse = new AndMatchExpr(setFalseFalse);

    // Confirm boolean expr true AND true = true
    assertThat(exprTrueTrue, matches(createFlow(), ""));
    // Confirm boolean expr true AND false = false
    assertThat(exprTrueFalse, not(matches(createFlow(), "")));
    // Confirm boolean expr false AND false = false
    assertThat(exprFalseFalse, not(matches(createFlow(), "")));
  }
}
