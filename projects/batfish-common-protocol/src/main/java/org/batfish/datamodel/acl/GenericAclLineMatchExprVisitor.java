package org.batfish.datamodel.acl;

public interface GenericAclLineMatchExprVisitor<R> {

  default R visit(AclLineMatchExpr expr) {
    return expr.accept(this);
  }

  R visitAndMatchExpr(AndMatchExpr andMatchExpr);

  R visitDeniedByAcl(DeniedByAcl deniedByAcl);

  R visitFalseExpr(FalseExpr falseExpr);

  R visitMatchDestinationIp(MatchDestinationIp matchDestinationIp);

  R visitMatchDestinationPort(MatchDestinationPort matchDestinationPort);

  R visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace);

  R visitMatchSourceIp(MatchSourceIp matchSourceIp);

  R visitMatchSourcePort(MatchSourcePort matchSourcePort);

  R visitMatchSrcInterface(MatchSrcInterface matchSrcInterface);

  R visitNotMatchExpr(NotMatchExpr notMatchExpr);

  R visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice);

  R visitOrMatchExpr(OrMatchExpr orMatchExpr);

  R visitPermittedByAcl(PermittedByAcl permittedByAcl);

  R visitTrueExpr(TrueExpr trueExpr);
}
