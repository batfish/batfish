package org.batfish.z3.expr.visitors;

import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.PrefixMatchExpr;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.SaneExpr;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.TrueExpr;

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

  void visitStateExpr(StateExpr stateExpr);

  void visitTrueExpr(TrueExpr trueExpr);
}
