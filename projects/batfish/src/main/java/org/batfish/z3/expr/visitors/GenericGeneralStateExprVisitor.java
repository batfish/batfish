package org.batfish.z3.expr.visitors;

import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.expr.TransformationStateExpr;

public interface GenericGeneralStateExprVisitor<R> {

  R castToGenericGeneralStateExprVisitorReturnType(Object o);

  R visitBasicStateExpr(BasicStateExpr basicStateExpr);

  R visitTransformationStateExpr(TransformationStateExpr transformationStateExpr);
}
