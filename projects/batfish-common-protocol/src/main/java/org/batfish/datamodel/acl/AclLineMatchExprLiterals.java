package org.batfish.datamodel.acl;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import org.batfish.common.BatfishException;

public class AclLineMatchExprLiterals implements GenericAclLineMatchExprVisitor<Void> {

  private Set<AclLineMatchExpr> _literals;

  private AclLineMatchExprLiterals() {
    _literals = Collections.newSetFromMap(new IdentityHashMap<>());
  }

  public static Set<AclLineMatchExpr> getLiterals(AclLineMatchExpr expr) {
    AclLineMatchExprLiterals aclLineMatchExprLiterals = new AclLineMatchExprLiterals();
    aclLineMatchExprLiterals.visit(expr);
    return aclLineMatchExprLiterals._literals;
  }

  @Override
  public Void visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    andMatchExpr.getConjuncts().forEach(this::visit);
    return null;
  }

  @Override
  public Void visitFalseExpr(FalseExpr falseExpr) {
    return null;
  }

  @Override
  public Void visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    _literals.add(matchHeaderSpace);
    return null;
  }

  @Override
  public Void visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    _literals.add(matchSrcInterface);
    return null;
  }

  @Override
  public Void visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    // normalization retains negation on literals but not in general, so we recurse either way
    this.visit(notMatchExpr.getOperand());
    return null;
  }

  @Override
  public Void visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    _literals.add(originatingFromDevice);
    return null;
  }

  @Override
  public Void visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    orMatchExpr.getDisjuncts().forEach(this::visit);
    return null;
  }

  @Override
  public Void visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    throw new BatfishException("PermittedByAcl expressions must be inlined");
  }

  @Override
  public Void visitTrueExpr(TrueExpr trueExpr) {
    return null;
  }
}
