package org.batfish.datamodel.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.junit.Test;

public class AndExprTest {
  private static Flow createFlow() {
    Flow.Builder b = new Flow.Builder();
    b.setIngressNode("ingressNode");
    b.setTag("empty");
    return b.build();
  }

  @Test
  public void testSingleExpr() {
    // Test that if and only if the only ACL line is a match, the AndExpr returns a match

    MockAclLineExpr eTrue = new MockAclLineExpr(true);
    MockAclLineExpr eFalse = new MockAclLineExpr(false);

    // Setup simple expression with a single true boolean expr
    Set<AclLineExpr> setTrue = new HashSet<>();
    setTrue.add(eTrue);
    AndExpr exprTrue = new AndExpr(setTrue);

    // Setup simple expression with a single false boolean expr
    Set<AclLineExpr> setFalse = new HashSet<>();
    setFalse.add(eFalse);
    AndExpr exprFalse = new AndExpr(setFalse);

    // Confirm true boolean expr matches
    assertThat(exprTrue.match(createFlow(), "", new HashSet<>()), equalTo(true));
    // Confirm false boolean expr does not match
    assertThat(exprFalse.match(createFlow(), "", new HashSet<>()), equalTo(false));
  }

  @Test
  public void testMultipleExprs() {
    // Test that if and only if all ACL lines are a match, the AndExpr returns a match

    MockAclLineExpr eTrue = new MockAclLineExpr(true);
    MockAclLineExpr eFalse = new MockAclLineExpr(false);

    // Setup
    Set<AclLineExpr> setTrueTrue = new HashSet<>();
    setTrueTrue.add(eTrue);
    setTrueTrue.add(eTrue);
    AndExpr exprTrueTrue = new AndExpr(setTrueTrue);

    Set<AclLineExpr> setTrueFalse = new HashSet<>();
    setTrueFalse.add(eTrue);
    setTrueFalse.add(eFalse);
    AndExpr exprTrueFalse = new AndExpr(setTrueFalse);

    Set<AclLineExpr> setFalseFalse = new HashSet<>();
    setFalseFalse.add(eFalse);
    setFalseFalse.add(eFalse);
    AndExpr exprFalseFalse = new AndExpr(setFalseFalse);

    // Confirm boolean expr true AND true = true
    assertThat(exprTrueTrue.match(createFlow(), "", new HashSet<>()), equalTo(true));
    // Confirm boolean expr true AND false = false
    assertThat(exprTrueFalse.match(createFlow(), "", new HashSet<>()), equalTo(false));
    // Confirm boolean expr false AND false = false
    assertThat(exprFalseFalse.match(createFlow(), "", new HashSet<>()), equalTo(false));
  }
}
