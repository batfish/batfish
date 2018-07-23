package org.batfish.z3.expr.visitors;

import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BitVecExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.ExtractExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IdExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.IfThenElse;
import org.batfish.z3.expr.IpSpaceMatchExpr;
import org.batfish.z3.expr.ListExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.PrefixMatchExpr;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.TransformedVarIntExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;

public interface ExprVisitor {
  void visitAndExpr(AndExpr andExpr);

  void visitBitVecExpr(BitVecExpr bitVecExpr);

  void visitEqExpr(EqExpr eqExpr);

  void visitExtractExpr(ExtractExpr extractExpr);

  void visitFalseExpr(FalseExpr falseExpr);

  void visitHeaderSpaceMatchExpr(HeaderSpaceMatchExpr headerSpaceMatchExpr);

  void visitIdExpr(IdExpr idExpr);

  void visitIfExpr(IfExpr ifExpr);

  void visitIfThenElse(IfThenElse ifThenElse);

  void visitListExpr(ListExpr listExpr);

  void visitLitIntExpr(LitIntExpr litIntExpr);

  void visitIpSpaceMatchExpr(IpSpaceMatchExpr matchIpSpaceExpr);

  void visitNotExpr(NotExpr notExpr);

  void visitOrExpr(OrExpr orExpr);

  void visitPrefixMatchExpr(PrefixMatchExpr prefixMatchExpr);

  void visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr);

  void visitStateExpr(StateExpr stateExpr);

  void visitTrueExpr(TrueExpr trueExpr);

  void visitVarIntExpr(VarIntExpr varIntExpr);

  void visitTransformedVarIntExpr(TransformedVarIntExpr transformedVarIntExpr);
}
