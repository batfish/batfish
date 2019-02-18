package org.batfish.datamodel.acl;

import static org.batfish.datamodel.matchers.AclLineMatchExprMatchers.matches;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.batfish.datamodel.Flow;
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

    NotMatchExpr exprNotTrue = new NotMatchExpr(TrueExpr.INSTANCE);
    NotMatchExpr exprNotFalse = new NotMatchExpr(FalseExpr.INSTANCE);

    // Confirm boolean expr NOT true = false
    assertThat(exprNotTrue, not(matches(createFlow(), "")));

    // Confirm boolean expr NOT false = true
    assertThat(exprNotFalse, matches(createFlow(), ""));
  }
}
