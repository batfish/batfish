package org.batfish.z3.expr.visitors;

import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.expr.TransformationStateExpr;

public interface VoidStateExprVisitor {

  void visitBasicStateExpr(BasicStateExpr basicStateExpr);

  void visitTransformationStateExpr(TransformationStateExpr transformationStateExpr);
}
