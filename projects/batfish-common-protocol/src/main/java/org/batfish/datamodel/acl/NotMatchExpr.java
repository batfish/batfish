package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Map;
import java.util.Objects;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class NotMatchExpr extends AclLineMatchExpr {
  private final AclLineMatchExpr _operand;

  public NotMatchExpr(AclLineMatchExpr operand) {
    _operand = operand;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitNotMatchExpr(this);
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Map<String, IpAccessList> availableAcls) {
    return !_operand.match(flow, srcInterface, availableAcls);
  }

  @Override
  public boolean exprEquals(Object o) {
    return Objects.equals(_operand, ((NotMatchExpr) o).getOperand());
  }

  @Override
  public int hashCode() {
    return _operand.hashCode();
  }

  @Override
  public String toString() {
    ToStringHelper helper = MoreObjects.toStringHelper(getClass());
    return helper.toString();
  }

  public AclLineMatchExpr getOperand() {
    return _operand;
  }
}
