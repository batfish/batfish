package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Map;
import java.util.Objects;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class TrueExpr extends AclLineMatchExpr {

  public TrueExpr() {}

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitTrueExpr(this);
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Map<String, IpAccessList> availableAcls) {
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash((Boolean) true);
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
