package org.batfish.z3.expr.visitors;

import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.expr.TransformationStateExpr;

public interface GenericStateExprVisitor<R> {

  R castToGenericStateExprVisitorReturnType(Object o);

  R visitBasicStateExpr(BasicStateExpr basicStateExpr);

  R visitTransformationStateExpr(TransformationStateExpr transformationStateExpr);
}
