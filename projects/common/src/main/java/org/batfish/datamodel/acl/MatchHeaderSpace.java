package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.TraceElement;

public class MatchHeaderSpace extends AclLineMatchExpr {
  private static final String PROP_HEADER_SPACE = "headerSpace";

  private final HeaderSpace _headerSpace;

  public MatchHeaderSpace(HeaderSpace headerSpace) {
    this(headerSpace, (TraceElement) null);
  }

  @JsonCreator
  public MatchHeaderSpace(
      @JsonProperty(PROP_HEADER_SPACE) HeaderSpace headerSpace,
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement) {
    super(traceElement);
    _headerSpace = headerSpace;
  }

  public MatchHeaderSpace(HeaderSpace headerSpace, @Nullable String traceElement) {
    this(headerSpace, traceElement == null ? null : TraceElement.of(traceElement));
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitMatchHeaderSpace(this);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_headerSpace, ((MatchHeaderSpace) o)._headerSpace);
  }

  @JsonProperty(PROP_HEADER_SPACE)
  public HeaderSpace getHeaderspace() {
    return _headerSpace;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_headerSpace);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_HEADER_SPACE, _headerSpace)
        .add(PROP_TRACE_ELEMENT, getTraceElement())
        .toString();
  }
}
