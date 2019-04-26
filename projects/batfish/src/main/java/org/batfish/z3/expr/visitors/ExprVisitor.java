package org.batfish.z3.expr.visitors;

import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.ExtractExpr;
import org.batfish.z3.expr.IdExpr;
import org.batfish.z3.expr.IfThenElse;
import org.batfish.z3.expr.IpSpaceMatchExpr;
import org.batfish.z3.expr.ListExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.VarIntExpr;

public interface ExprVisitor {
  void visitAndExpr(AndExpr andExpr);

  void visitEqExpr(EqExpr eqExpr);

  void visitExtractExpr(ExtractExpr extractExpr);

  void visitFalseExpr();

  void visitIdExpr(IdExpr idExpr);

  void visitIfThenElse(IfThenElse ifThenElse);

  void visitListExpr(ListExpr listExpr);

  void visitLitIntExpr(LitIntExpr litIntExpr);

  void visitIpSpaceMatchExpr(IpSpaceMatchExpr matchIpSpaceExpr);

  void visitNotExpr(NotExpr notExpr);

  void visitOrExpr(OrExpr orExpr);

  void visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr);

  void visitStateExpr(StateExpr stateExpr);

  void visitTrueExpr();

  void visitVarIntExpr(VarIntExpr varIntExpr);
}
