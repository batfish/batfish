package org.batfish.question.aclreachability2;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.batfish.datamodel.acl.AclLineMatchExpr;
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
 * Collects all {@link AclLineMatchExpr}s of the given type built into the visited expression.
 * Multiple expressions of the given type may be built into the visited expression if the visited
 * expression is of type {@link AndMatchExpr}, {@link OrMatchExpr}, or {@link NotMatchExpr}. This
 * visitor is therefore useful for finding all ACLs referenced or all header spaces contained within
 * a single ACL line.
 *
 * @param <T> Type of {@link AclLineMatchExpr} to collect from the visited expression.
 */
@SuppressWarnings("unchecked")
public class TypeMatchExprsCollector<T extends AclLineMatchExpr>
    implements GenericAclLineMatchExprVisitor<List<T>> {

  private final Class<T> _type;

  public TypeMatchExprsCollector(Class<T> type) {
    _type = type;
  }

  @Override
  public List<T> visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    List<T> matchingExprs = new ArrayList<>();
    if (_type.isAssignableFrom(AndMatchExpr.class)) {
      matchingExprs.add((T) andMatchExpr);
    }
    for (AclLineMatchExpr conjunct : andMatchExpr.getConjuncts()) {
      matchingExprs.addAll(conjunct.accept(this));
    }
    return matchingExprs;
  }

  @Override
  public List<T> visitFalseExpr(FalseExpr falseExpr) {
    return _type.isAssignableFrom(FalseExpr.class)
        ? ImmutableList.of((T) falseExpr)
        : ImmutableList.of();
  }

  @Override
  public List<T> visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    return _type.isAssignableFrom(MatchHeaderSpace.class)
        ? ImmutableList.of((T) matchHeaderSpace)
        : ImmutableList.of();
  }

  @Override
  public List<T> visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    return _type.isAssignableFrom(MatchSrcInterface.class)
        ? ImmutableList.of((T) matchSrcInterface)
        : ImmutableList.of();
  }

  @Override
  public List<T> visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    List<T> matchingExprs = new ArrayList<>();
    if (_type.isAssignableFrom(NotMatchExpr.class)) {
      matchingExprs.add((T) notMatchExpr);
    }
    matchingExprs.addAll(notMatchExpr.getOperand().accept(this));
    return matchingExprs;
  }

  @Override
  public List<T> visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    return _type.isAssignableFrom(OriginatingFromDevice.class)
        ? ImmutableList.of((T) originatingFromDevice)
        : ImmutableList.of();
  }

  @Override
  public List<T> visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    List<T> matchingExprs = new ArrayList<>();
    if (_type.isAssignableFrom(OrMatchExpr.class)) {
      matchingExprs.add((T) orMatchExpr);
    }
    for (AclLineMatchExpr disjunct : orMatchExpr.getDisjuncts()) {
      matchingExprs.addAll(disjunct.accept(this));
    }
    return matchingExprs;
  }

  @Override
  public List<T> visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    return _type.isAssignableFrom(PermittedByAcl.class)
        ? ImmutableList.of((T) permittedByAcl)
        : ImmutableList.of();
  }

  @Override
  public List<T> visitTrueExpr(TrueExpr trueExpr) {
    return _type.isAssignableFrom(TrueExpr.class)
        ? ImmutableList.of((T) trueExpr)
        : ImmutableList.of();
  }
}
