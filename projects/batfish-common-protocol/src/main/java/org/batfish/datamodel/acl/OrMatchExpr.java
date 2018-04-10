package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import org.batfish.common.util.CommonUtil;

public class OrMatchExpr extends AclLineMatchExpr {
  private static final String PROP_DISJUNCTS = "disjuncts";
  private static final long serialVersionUID = 1L;

  private final Set<AclLineMatchExpr> _disjuncts;

  @JsonCreator
  public OrMatchExpr(@JsonProperty(PROP_DISJUNCTS) Iterable<AclLineMatchExpr> disjuncts) {
    _disjuncts = ImmutableSet.copyOf(disjuncts);
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitOrMatchExpr(this);
  }

  @Override
  protected int compareSameClass(AclLineMatchExpr o) {
    return CommonUtil.compareIterable(_disjuncts, ((OrMatchExpr) o)._disjuncts);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_disjuncts, ((OrMatchExpr) o)._disjuncts);
  }

  @JsonProperty(PROP_DISJUNCTS)
  public Set<AclLineMatchExpr> getDisjuncts() {
    return _disjuncts;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_disjuncts);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_DISJUNCTS, _disjuncts).toString();
  }
}
