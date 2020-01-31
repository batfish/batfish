package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.TraceElement;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "class")
public abstract class AclLineMatchExpr implements Serializable {
  protected static final String PROP_TRACE_ELEMENT = "traceElement";

  protected final @Nullable TraceElement _traceElement;

  @JsonCreator
  public AclLineMatchExpr(@JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement) {
    _traceElement = traceElement;
  }

  public abstract <R> R accept(GenericAclLineMatchExprVisitor<R> visitor);

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (!(getClass() == o.getClass())) {
      return false;
    }
    return Objects.equals(_traceElement, ((AclLineMatchExpr) o)._traceElement) && exprEquals(o);
  }

  protected abstract boolean exprEquals(Object o);

  @JsonProperty(PROP_TRACE_ELEMENT)
  public @Nullable TraceElement getTraceElement() {
    return _traceElement;
  }

  @Override
  public abstract int hashCode();

  @Override
  public abstract String toString();
}
