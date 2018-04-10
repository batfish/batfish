package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;

public class OrMatchExpr extends AclLineMatchExpr {
  private static final long serialVersionUID = 1L;
  private final Set<AclLineMatchExpr> _disjuncts;

  public OrMatchExpr(Iterable<AclLineMatchExpr> disjuncts) {
    _disjuncts = ImmutableSet.copyOf(disjuncts);
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitOrMatchExpr(this);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_disjuncts, ((OrMatchExpr) o)._disjuncts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_disjuncts);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add("disjuncts", _disjuncts).toString();
  }

  public Set<AclLineMatchExpr> getDisjuncts() {
    return _disjuncts;
  }
}
