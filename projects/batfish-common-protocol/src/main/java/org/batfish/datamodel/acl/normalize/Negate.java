package org.batfish.datamodel.acl.normalize;

import com.google.common.collect.ImmutableSortedSet;
import java.util.Comparator;
import java.util.SortedSet;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

/**
 * Negate an {@link AclLineMatchExpr}. Eliminate double-negation and apply deMorgan's law where
 * possible. Negate shallowly -- rather than recursively negating when applying deMorgan, use {@link
 * AclLineMatchExprs#not}.
 */
public final class Negate implements GenericAclLineMatchExprVisitor<AclLineMatchExpr> {
  private static final Negate INSTANCE = new Negate();

  private Negate() {}

  public static AclLineMatchExpr negate(AclLineMatchExpr expr) {
    return expr.accept(INSTANCE);
  }

  private static SortedSet<AclLineMatchExpr> negate(SortedSet<AclLineMatchExpr> exprs) {
    return exprs
        .stream()
        .map(AclLineMatchExprs::not)
        .collect(ImmutableSortedSet.toImmutableSortedSet(Comparator.naturalOrder()));
  }

  @Override
  public AclLineMatchExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    return new OrMatchExpr(negate(andMatchExpr.getConjuncts()));
  }

  @Override
  public AclLineMatchExpr visitFalseExpr(FalseExpr falseExpr) {
    return TrueExpr.INSTANCE;
  }

  @Override
  public AclLineMatchExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    return new NotMatchExpr(matchHeaderSpace);
  }

  @Override
  public AclLineMatchExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    return new NotMatchExpr(matchSrcInterface);
  }

  @Override
  public AclLineMatchExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    // eliminate double-negation
    return notMatchExpr.getOperand();
  }

  @Override
  public AclLineMatchExpr visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    return new NotMatchExpr(originatingFromDevice);
  }

  @Override
  public AclLineMatchExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return new AndMatchExpr(negate(orMatchExpr.getDisjuncts()));
  }

  @Override
  public AclLineMatchExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    return new NotMatchExpr(permittedByAcl);
  }

  @Override
  public AclLineMatchExpr visitTrueExpr(TrueExpr trueExpr) {
    return FalseExpr.INSTANCE;
  }
}
