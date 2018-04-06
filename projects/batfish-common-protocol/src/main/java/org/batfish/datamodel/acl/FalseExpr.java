package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Map;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class FalseExpr extends AclLineMatchExpr {

  public FalseExpr() {}

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitFalseExpr(this);
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Map<String, IpAccessList> availableAcls) {
    return false;
  }

  @Override
  public int hashCode() {
    return ((Boolean) false).hashCode();
  }

  @Override
  public boolean exprEquals(Object o) {
    return true;
  }

  @Override
  public String toString() {
    ToStringHelper helper = MoreObjects.toStringHelper(getClass());
    return helper.toString();
  }
}
