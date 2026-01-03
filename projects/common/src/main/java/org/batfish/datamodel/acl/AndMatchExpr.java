package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.datamodel.TraceElement;

public class AndMatchExpr extends AclLineMatchExpr {
  private static final String PROP_CONJUNCTS = "conjuncts";

  private final List<AclLineMatchExpr> _conjuncts;

  public AndMatchExpr(Iterable<? extends AclLineMatchExpr> conjuncts) {
    this(conjuncts, (TraceElement) null);
  }

  @JsonCreator
  public AndMatchExpr(
      @JsonProperty(PROP_CONJUNCTS) Iterable<? extends AclLineMatchExpr> conjuncts,
      @JsonProperty(PROP_TRACE_ELEMENT) @Nullable TraceElement traceElement) {
    super(traceElement);
    _conjuncts = conjuncts != null ? ImmutableList.copyOf(conjuncts) : ImmutableList.of();
  }

  public AndMatchExpr(
      Iterable<? extends AclLineMatchExpr> conjuncts, @Nullable String traceElement) {
    this(conjuncts, traceElement == null ? null : TraceElement.of(traceElement));
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitAndMatchExpr(this);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_conjuncts, ((AndMatchExpr) o)._conjuncts);
  }

  @JsonProperty(PROP_CONJUNCTS)
  public List<AclLineMatchExpr> getConjuncts() {
    return _conjuncts;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_conjuncts, _traceElement);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_TRACE_ELEMENT, _traceElement)
        .add(PROP_CONJUNCTS, _conjuncts)
        .toString();
  }
}
