package org.batfish.z3.expr;

import org.batfish.z3.state.State;
import org.batfish.z3.state.State.StateExpr;
import org.batfish.z3.state.StateParameterization;

public interface BooleanExprVisitor {

  void visitAndExpr(AndExpr andExpr);

  void visitEqExpr(EqExpr eqExpr);

  void visitFalseExpr(FalseExpr falseExpr);

  void visitHeaderSpaceMatchExpr(HeaderSpaceMatchExpr headerSpaceMatchExpr);

  void visitIfExpr(IfExpr ifExpr);

  void visitNotExpr(NotExpr notExpr);

  void visitOrExpr(OrExpr orExpr);

  void visitPrefixMatchExpr(PrefixMatchExpr prefixMatchExpr);

  void visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr);

  void visitSaneExpr(SaneExpr saneExpr);

  <T extends State<T, ?>, P extends StateParameterization<T>> void visitStateExpr(
      StateExpr<T, P> stateExpr);

  void visitTrueExpr(TrueExpr trueExpr);
}
