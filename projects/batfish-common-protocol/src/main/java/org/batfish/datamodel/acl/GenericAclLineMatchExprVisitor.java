package org.batfish.datamodel.acl;

public interface GenericAclLineMatchExprVisitor<R> {

  default R visit(AclLineMatchExpr expr) {
    return expr.accept(this);
  }

  R visitAndMatchExpr(AndMatchExpr andMatchExpr);

  R visitFalseExpr(FalseExpr falseExpr);

  R visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace);

  R visitMatchSrcInterface(MatchSrcInterface matchSrcInterface);

  R visitNotMatchExpr(NotMatchExpr notMatchExpr);

  R visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice);

  R visitOrMatchExpr(OrMatchExpr orMatchExpr);

  R visitPermittedByAcl(PermittedByAcl permittedByAcl);

  R visitTrueExpr(TrueExpr trueExpr);
}
