package org.batfish.z3.expr;

public interface IntExprVisitor {

  void visitExtractExpr(ExtractExpr extractExpr);

  void visitLitIntExpr(LitIntExpr litIntExpr);

  void visitVarIntExpr(VarIntExpr varIntExpr);
}
