package org.batfish.z3.expr.visitors;

import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BitVecExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.ExtractExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IdExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.ListExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.PrefixMatchExpr;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;

public interface GenericExprVisitor<R> {
  R visitAndExpr(AndExpr andExpr);

  R visitStateExpr(StateExpr stateExpr);

  R visitBitVecExpr(BitVecExpr bitVecExpr);

  R visitEqExpr(EqExpr eqExpr);

  R visitExtractExpr(ExtractExpr extractExpr);

  R visitFalseExpr(FalseExpr falseExpr);

  R visitHeaderSpaceMatchExpr(HeaderSpaceMatchExpr headerSpaceMatchExpr);

  R visitIdExpr(IdExpr idExpr);

  R visitIfExpr(IfExpr ifExpr);

  R visitListExpr(ListExpr listExpr);

  R visitLitIntExpr(LitIntExpr litIntExpr);

  R visitNotExpr(NotExpr notExpr);

  R visitOrExpr(OrExpr orExpr);

  R visitPrefixMatchExpr(PrefixMatchExpr prefixMatchExpr);

  R visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr);

  R visitTrueExpr(TrueExpr trueExpr);

  R visitVarIntExpr(VarIntExpr varIntExpr);
}
