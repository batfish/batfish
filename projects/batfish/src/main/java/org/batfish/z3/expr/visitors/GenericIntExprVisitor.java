package org.batfish.z3.expr.visitors;

import org.batfish.z3.expr.ExtractExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.TransformedVarIntExpr;
import org.batfish.z3.expr.VarIntExpr;

public interface GenericIntExprVisitor<R> {

  R castToGenericIntExprVisitorReturnType(Object o);

  R visitExtractExpr(ExtractExpr extractExpr);

  R visitLitIntExpr(LitIntExpr litIntExpr);

  R visitTransformedVarIntExpr(TransformedVarIntExpr transformedVarIntExpr);

  R visitVarIntExpr(VarIntExpr varIntExpr);
}
