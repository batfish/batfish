package org.batfish.datamodel.acl;

import static org.batfish.datamodel.matchers.AclLineMatchExprMatchers.matches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.Flow;
import org.junit.Test;

public class OrExprTest {
  private Flow createFlow() {
    Flow.Builder b = Flow.builder();
    b.setIngressNode("ingressNode");
    return b.build();
  }

  @Test
  public void testSingleExpr() {
    // Test that if and only if the only ACL line is a match, the OrMatchExpr returns a match

    // Setup simple expression with a single true boolean expr
    List<AclLineMatchExpr> setTrue = ImmutableList.of(TrueExpr.INSTANCE);
    OrMatchExpr exprTrue = new OrMatchExpr(setTrue);

    // Setup simple expression with a single false boolean expr
    List<AclLineMatchExpr> setFalse = ImmutableList.of(FalseExpr.INSTANCE);
    OrMatchExpr exprFalse = new OrMatchExpr(setFalse);

    // Confirm true boolean expr matches
    assertThat(exprTrue, matches(createFlow(), ""));
    // Confirm false boolean expr does not match
    assertThat(exprFalse, not(matches(createFlow(), "")));
  }

  @Test
  public void testMultipleExprs() {
    // Test that if either of two ACL lines is a match, the OrMatchExpr returns a match

    List<AclLineMatchExpr> setTrueTrue = ImmutableList.of(TrueExpr.INSTANCE, TrueExpr.INSTANCE);
    OrMatchExpr exprTrueTrue = new OrMatchExpr(setTrueTrue);

    List<AclLineMatchExpr> setTrueFalse = ImmutableList.of(TrueExpr.INSTANCE, FalseExpr.INSTANCE);
    OrMatchExpr exprTrueFalse = new OrMatchExpr(setTrueFalse);

    List<AclLineMatchExpr> setFalseFalse = ImmutableList.of(FalseExpr.INSTANCE, FalseExpr.INSTANCE);
    OrMatchExpr exprFalseFalse = new OrMatchExpr(setFalseFalse);

    // Confirm boolean expr true OR true = true
    assertThat(exprTrueTrue, matches(createFlow(), ""));
    // Confirm boolean expr true OR false = true
    assertThat(exprTrueFalse, matches(createFlow(), ""));
    // Confirm boolean expr false OR false = false
    assertThat(exprFalseFalse, not(matches(createFlow(), "")));
  }
}
