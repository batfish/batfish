package org.batfish.datamodel.acl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import java.util.HashMap;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.matchers.AclLineMatchExprMatchers;
import org.junit.Test;

public class NotExprTest {
  private static Flow createFlow() {
    Flow.Builder b = new Flow.Builder();
    b.setIngressNode("ingressNode");
    b.setTag("empty");
    return b.build();
  }

  @Test
  public void testSingleExpr() {
    // Test that the NotMatchExpr returns the opposite of the underlying ACL line

    NotMatchExpr exprNotTrue = new NotMatchExpr(TrueExpr.trueExpr);
    NotMatchExpr exprNotFalse = new NotMatchExpr(FalseExpr.falseExpr);

    // Confirm boolean expr NOT true = false
    assertThat(
        exprNotTrue, not(AclLineMatchExprMatchers.matches(createFlow(), "", new HashMap<>())));

    // Confirm boolean expr NOT false = true
    assertThat(exprNotFalse, AclLineMatchExprMatchers.matches(createFlow(), "", new HashMap<>()));
  }
}
