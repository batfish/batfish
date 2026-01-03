package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.TraceElement;

public class NotMatchExpr extends AclLineMatchExpr {
  private static final String PROP_OPERAND = "operand";

  private final AclLineMatchExpr _operand;

  public NotMatchExpr(AclLineMatchExpr operand) {
    this(operand, null);
  }

  @JsonCreator
  public NotMatchExpr(
      @JsonProperty(PROP_OPERAND) AclLineMatchExpr operand,
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement) {
    super(traceElement);
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

  @JsonProperty(PROP_OPERAND)
  public AclLineMatchExpr getOperand() {
    return _operand;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_operand);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).add(PROP_OPERAND, _operand).toString();
  }
}
