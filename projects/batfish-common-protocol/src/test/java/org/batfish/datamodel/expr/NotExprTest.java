package org.batfish.datamodel.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashSet;
import org.batfish.datamodel.Flow;
import org.junit.Test;

public class NotExprTest {
  private Flow createFlow() {
    Flow.Builder b = new Flow.Builder();
    b.setIngressNode("ingressNode");
    b.setTag("empty");
    return b.build();
  }

  @Test
  public void testSingleExpr() {
    MockAclLineExpr ePass = new MockAclLineExpr(true);
    NotExpr exprTrue = new NotExpr(ePass);

    MockAclLineExpr eFail = new MockAclLineExpr(false);
    NotExpr exprFalse = new NotExpr(eFail);

    // Confirm boolean expr not true does not match
    assertThat(exprTrue.match(createFlow(), "", new HashSet<>()), equalTo(false));
    // Confirm boolean expr not false matches
    assertThat(exprFalse.match(createFlow(), "", new HashSet<>()), equalTo(true));
  }
}
