package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class OrMatchExpr extends AclLineMatchExpr {
  private static final String PROP_DISJUNCTS = "disjuncts";

  private final List<AclLineMatchExpr> _disjuncts;

  public OrMatchExpr(Iterable<AclLineMatchExpr> disjuncts) {
    this(disjuncts, null);
  }

  @JsonCreator
  public OrMatchExpr(
      @JsonProperty(PROP_DISJUNCTS) Iterable<AclLineMatchExpr> disjuncts,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description) {
    super(description);
    _disjuncts = disjuncts != null ? ImmutableList.copyOf(disjuncts) : ImmutableList.of();
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitOrMatchExpr(this);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_disjuncts, ((OrMatchExpr) o)._disjuncts);
  }

  @JsonProperty(PROP_DISJUNCTS)
  public List<AclLineMatchExpr> getDisjuncts() {
    return _disjuncts;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_disjuncts);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add(PROP_DESCRIPTION, _description)
        .add(PROP_DISJUNCTS, _disjuncts)
        .toString();
  }
}
