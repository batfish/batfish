package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.TraceElement;

public class TrueExpr extends AclLineMatchExpr {
  public static final TrueExpr INSTANCE = new TrueExpr();

  private final @Nullable TraceElement _traceElement;

  private TrueExpr() {
    super(null);
    _traceElement = null;
  }

  public TrueExpr(@Nullable TraceElement traceElement) {
    super(traceElement);
    _traceElement = traceElement;
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitTrueExpr(this);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return true;
  }

  @Override
  public int hashCode() {
    return Objects.hash((Boolean) true, _traceElement);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof TrueExpr)) {
      return false;
    }
    TrueExpr rhs = (TrueExpr) obj;
    return Objects.equals(_traceElement, rhs._traceElement);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_TRACE_ELEMENT, _traceElement)
        .toString();
  }

  @JsonCreator
  private static @Nonnull TrueExpr jsonCreator(
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement) {
    return new TrueExpr(traceElement);
  }
}
