package org.batfish.datamodel.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.Flow;
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
    MockAclLineExpr eTrue = new MockAclLineExpr(true);
    MockAclLineExpr eFalse = new MockAclLineExpr(false);

    // Setup simple expression with a single true boolean expr
    Set<AclLineExpr> setTrue = new HashSet<>();
    setTrue.add(eTrue);
    OrExpr exprTrue = new OrExpr(setTrue);

    // Setup simple expression with a single false boolean expr
    Set<AclLineExpr> setFalse = new HashSet<>();
    setFalse.add(eFalse);
    OrExpr exprFalse = new OrExpr(setFalse);

    // Confirm true boolean expr matches
    assertThat(exprTrue.match(createFlow(), "", new HashSet<>()), equalTo(true));
    // Confirm false boolean expr does not match
    assertThat(exprFalse.match(createFlow(), "", new HashSet<>()), equalTo(false));
  }

  @Test
  public void testMultipleExprs() {
    MockAclLineExpr eTrue = new MockAclLineExpr(true);
    MockAclLineExpr eFalse = new MockAclLineExpr(false);

    // Setup
    Set<AclLineExpr> setTrueTrue = new HashSet<>();
    setTrueTrue.add(eTrue);
    setTrueTrue.add(eTrue);
    OrExpr exprTrueTrue = new OrExpr(setTrueTrue);

    Set<AclLineExpr> setTrueFalse = new HashSet<>();
    setTrueFalse.add(eTrue);
    setTrueFalse.add(eFalse);
    OrExpr exprTrueFalse = new OrExpr(setTrueFalse);

    Set<AclLineExpr> setFalseFalse = new HashSet<>();
    setFalseFalse.add(eFalse);
    setFalseFalse.add(eFalse);
    OrExpr exprFalseFalse = new OrExpr(setFalseFalse);

    // Confirm boolean expr true OR true = true
    assertThat(exprTrueTrue.match(createFlow(), "", new HashSet<>()), equalTo(true));
    // Confirm boolean expr true OR false = true
    assertThat(exprTrueFalse.match(createFlow(), "", new HashSet<>()), equalTo(true));
    // Confirm boolean expr false OR false = false
    assertThat(exprFalseFalse.match(createFlow(), "", new HashSet<>()), equalTo(false));
  }
}
