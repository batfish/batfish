package org.batfish.z3.expr.visitors;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;

public class AclLineMatchBooleanExprTransformer
    implements GenericAclLineMatchExprVisitor<BooleanExpr> {

  private static final AclLineMatchBooleanExprTransformer INSTANCE =
      new AclLineMatchBooleanExprTransformer();

  public static BooleanExpr transform(AclLineMatchExpr aclLineMatchExpr) {
    return aclLineMatchExpr.accept(INSTANCE);
  }

  private AclLineMatchBooleanExprTransformer() {}

  @Override
  public BooleanExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    return new AndExpr(
        andMatchExpr
            .getConjuncts()
            .stream()
            .map(conjunct -> conjunct.accept(this))
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public BooleanExpr visitFalseExpr(FalseExpr falseExpr) {
    return org.batfish.z3.expr.FalseExpr.INSTANCE;
  }

  @Override
  public BooleanExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    return new HeaderSpaceMatchExpr(matchHeaderSpace.getHeaderspace());
  }

  @Override
  public BooleanExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public BooleanExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    return new NotExpr(notMatchExpr.accept(this));
  }

  @Override
  public BooleanExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return new OrExpr(
        orMatchExpr
            .getDisjuncts()
            .stream()
            .map(disjunct -> disjunct.accept(this))
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public BooleanExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public BooleanExpr visitTrueExpr(TrueExpr trueExpr) {
    return org.batfish.z3.expr.TrueExpr.INSTANCE;
  }
}
