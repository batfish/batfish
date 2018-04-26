package org.batfish.z3.expr.visitors;

import org.batfish.z3.expr.ExtractExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.TransformedVarIntExpr;
import org.batfish.z3.expr.VarIntExpr;

public interface IntExprVisitor {
  void visitExtractExpr(ExtractExpr extractExpr);

  void visitLitIntExpr(LitIntExpr litIntExpr);

  void visitTransformedVarIntExpr(TransformedVarIntExpr transformedVarIntExpr);

  void visitVarIntExpr(VarIntExpr varIntExpr);
}
