package org.batfish.z3.expr.visitors;

import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.CurrentIsOriginalExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.PrefixMatchExpr;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.SaneExpr;
import org.batfish.z3.expr.TrueExpr;

public interface GenericBooleanExprVisitor<R> {

  R castToGenericBooleanExprVisitorReturnType(Object o);

  R visitAndExpr(AndExpr andExpr);

  R visitCurrentIsOriginalExpr(CurrentIsOriginalExpr currentIsOriginalExpr);

  R visitEqExpr(EqExpr eqExpr);

  R visitFalseExpr(FalseExpr falseExpr);

  R visitHeaderSpaceMatchExpr(HeaderSpaceMatchExpr headerSpaceMatchExpr);

  R visitIfExpr(IfExpr ifExpr);

  R visitNotExpr(NotExpr notExpr);

  R visitOrExpr(OrExpr orExpr);

  R visitPrefixMatchExpr(PrefixMatchExpr prefixMatchExpr);

  R visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr);

  R visitSaneExpr(SaneExpr saneExpr);

  R visitTrueExpr(TrueExpr trueExpr);
}
