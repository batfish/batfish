package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class AndMatchExpr extends AclLineMatchExpr {
  private final Set<AclLineMatchExpr> _conjuncts;

  public AndMatchExpr(Set<AclLineMatchExpr> conjuncts) {
    _conjuncts = ImmutableSet.copyOf(conjuncts);
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitAndMatchExpr(this);
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Map<String, IpAccessList> availableAcls) {
    return _conjuncts.stream().allMatch(c -> c.match(flow, srcInterface, availableAcls));
  }

  @Override
  public int hashCode() {
    return Objects.hash(_conjuncts);
  }

  @Override
  public boolean exprEquals(Object o) {
    return Objects.equals(_conjuncts, ((AndMatchExpr) o).getConjuncts());
  }

  @Override
  public String toString() {
    ToStringHelper helper = MoreObjects.toStringHelper(getClass());
    helper.add("conjuncts", _conjuncts);
    return helper.toString();
  }

  public Set<AclLineMatchExpr> getConjuncts() {
    return _conjuncts;
  }
}
