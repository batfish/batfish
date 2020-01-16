package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class AndMatchExpr extends AclLineMatchExpr {
  private static final String PROP_CONJUNCTS = "conjuncts";

  private final List<AclLineMatchExpr> _conjuncts;

  public AndMatchExpr(Iterable<AclLineMatchExpr> conjuncts) {
    this(conjuncts, null);
  }

  @JsonCreator
  public AndMatchExpr(
      @JsonProperty(PROP_CONJUNCTS) Iterable<AclLineMatchExpr> conjuncts,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description) {
    super(description);
    _conjuncts = conjuncts != null ? ImmutableList.copyOf(conjuncts) : ImmutableList.of();
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
    return Objects.hash(_conjuncts, _description);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_DESCRIPTION, _description)
        .add(PROP_CONJUNCTS, _conjuncts)
        .toString();
  }
}
