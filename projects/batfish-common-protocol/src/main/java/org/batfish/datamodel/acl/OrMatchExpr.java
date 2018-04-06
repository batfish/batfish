package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class OrMatchExpr extends AclLineMatchExpr {
  private final Set<AclLineMatchExpr> _disjuncts;

  public OrMatchExpr(Set<AclLineMatchExpr> disjuncts) {
    _disjuncts = ImmutableSet.copyOf(disjuncts);
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitOrMatchExpr(this);
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Map<String, IpAccessList> availableAcls) {
    return _disjuncts.stream().anyMatch(d -> d.match(flow, srcInterface, availableAcls));
  }

  @Override
  public boolean exprEquals(Object o) {
    return Objects.equals(_disjuncts, ((OrMatchExpr) o).getDisjuncts());
  }

  @Override
  public int hashCode() {
    return Objects.hash(_disjuncts);
  }

  @Override
  public String toString() {
    ToStringHelper helper = MoreObjects.toStringHelper(getClass());
    helper.add("disjuncts", _disjuncts);
    return helper.toString();
  }

  public Set<AclLineMatchExpr> getDisjuncts() {
    return _disjuncts;
  }
}
