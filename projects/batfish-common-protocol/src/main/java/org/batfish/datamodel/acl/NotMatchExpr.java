package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import java.util.Objects;

public class NotMatchExpr extends AclLineMatchExpr {
  private static final long serialVersionUID = 1L;
  private final AclLineMatchExpr _operand;

  public NotMatchExpr(AclLineMatchExpr operand) {
    _operand = operand;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitNotMatchExpr(this);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_operand, ((NotMatchExpr) o)._operand);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_operand);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).toString();
  }

  public AclLineMatchExpr getOperand() {
    return _operand;
  }
}
