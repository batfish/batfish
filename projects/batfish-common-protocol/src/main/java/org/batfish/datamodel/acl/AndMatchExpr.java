package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;

public class AndMatchExpr extends AclLineMatchExpr {
  private final Set<AclLineMatchExpr> _conjuncts;

  public AndMatchExpr(Iterable<AclLineMatchExpr> conjuncts) {
    _conjuncts = ImmutableSet.copyOf(conjuncts);
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitAndMatchExpr(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_conjuncts);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_conjuncts, ((AndMatchExpr) o)._conjuncts);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("conjuncts", _conjuncts).toString();
  }

  public Set<AclLineMatchExpr> getConjuncts() {
    return _conjuncts;
  }
}
