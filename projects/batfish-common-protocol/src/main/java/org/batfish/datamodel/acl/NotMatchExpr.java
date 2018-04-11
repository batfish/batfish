package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;

public class NotMatchExpr extends AclLineMatchExpr {
  private static final String PROP_OPERAND = "operand";
  private static final long serialVersionUID = 1L;

  private final AclLineMatchExpr _operand;

  @JsonCreator
  public NotMatchExpr(@JsonProperty(PROP_OPERAND) AclLineMatchExpr operand) {
    _operand = operand;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitNotMatchExpr(this);
  }

  @Override
  protected int compareSameClass(AclLineMatchExpr o) {
    return _operand.compareTo(((NotMatchExpr) o)._operand);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_operand, ((NotMatchExpr) o)._operand);
  }

  @JsonProperty(PROP_OPERAND)
  public AclLineMatchExpr getOperand() {
    return _operand;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_operand);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_OPERAND, _operand).toString();
  }
}
