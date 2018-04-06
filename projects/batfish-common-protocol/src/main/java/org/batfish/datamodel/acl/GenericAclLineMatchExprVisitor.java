package org.batfish.datamodel.acl;

public interface GenericAclLineMatchExprVisitor<R> {

  R visitAndMatchExpr(AndMatchExpr andMatchExpr);

  R visitFalseExpr(FalseExpr falseExpr);

  R visitMatchSrcInterface(MatchSrcInterface matchSrcInterface);

  R visitMatchHeaderspace(MatchHeaderspace matchHeaderspace);

  R visitNotMatchExpr(NotMatchExpr notMatchExpr);

  R visitOrMatchExpr(OrMatchExpr orMatchExpr);

  R visitPermittedByAcl(PermittedByAcl permittedByAcl);

  R visitTrueExpr(TrueExpr trueExpr);
}
