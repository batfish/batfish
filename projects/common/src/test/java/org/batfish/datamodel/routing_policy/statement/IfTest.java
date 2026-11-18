package org.batfish.datamodel.routing_policy.statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.junit.jupiter.api.Test;

/** Test of {@link If} */
@ParametersAreNonnullByDefault
final class IfTest {

  @Test
  void testJavaSerialization() {
    assertThat(SerializationUtils.clone(IF), equalTo(IF));
  }

  @Test
  void testJacksonSerialization() {
    assertThat(BatfishObjectMapper.clone(IF, If.class), equalTo(IF));
  }

  @Test
  void testSimplifyBranches() {
    Statement complexBranchStatement =
        new If(
            BooleanExprs.TRUE,
            ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
            ImmutableList.of(Statements.ReturnFalse.toStaticStatement()));
    Statement simplifiedBranchStatement = Statements.ReturnTrue.toStaticStatement();
    If before =
        new If(
            BooleanExprs.CALL_EXPR_CONTEXT,
            ImmutableList.of(complexBranchStatement),
            ImmutableList.of(complexBranchStatement));
    assertThat(
        before.simplify(),
        contains(
            new If(
                BooleanExprs.CALL_EXPR_CONTEXT,
                ImmutableList.of(simplifiedBranchStatement),
                ImmutableList.of(simplifiedBranchStatement))));
  }

  @Test
  void testSimplifyGuard() {
    assertThat(
        new If(
                new Disjunction(BooleanExprs.CALL_EXPR_CONTEXT),
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))
            .simplify(),
        contains(
            new If(
                BooleanExprs.CALL_EXPR_CONTEXT,
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))));
  }

  @Test
  void testSimplifyToTrueBranch() {
    assertThat(
        new If(
                BooleanExprs.TRUE,
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))
            .simplify(),
        contains(Statements.ReturnTrue.toStaticStatement()));
  }

  @Test
  void testSimplifyToFalseBranch() {
    assertThat(
        new If(
                BooleanExprs.FALSE,
                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))
            .simplify(),
        contains(Statements.ReturnFalse.toStaticStatement()));
  }

  @Test
  void testNoSimplifyImpureGuard() {
    If ifWithImpureGuard = new If(new CallExpr("foo"), ImmutableList.of(), ImmutableList.of());
    assertThat(ifWithImpureGuard.simplify(), contains(ifWithImpureGuard));
  }

  private static final If IF =
      new If(
          BooleanExprs.TRUE,
          ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
          ImmutableList.of(Statements.ReturnFalse.toStaticStatement()));
}
