package org.batfish.datamodel.acl;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.TraceElement;

public class FalseExpr extends AclLineMatchExpr {

  public static final FalseExpr INSTANCE = new FalseExpr();

  @Nullable private final TraceElement _traceElement;

  private FalseExpr() {
    super(null);
    _traceElement = null;
  }

  public FalseExpr(@Nullable TraceElement traceElement) {
    super(traceElement);
    _traceElement = traceElement;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitFalseExpr(this);
  }

  @Override
  public int hashCode() {
    return Objects.hash((Boolean) false, _traceElement);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FalseExpr)) {
      return false;
    }
    FalseExpr rhs = (FalseExpr) obj;
    return Objects.equals(_traceElement, rhs._traceElement);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return true;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass()).toString();
  }
}
