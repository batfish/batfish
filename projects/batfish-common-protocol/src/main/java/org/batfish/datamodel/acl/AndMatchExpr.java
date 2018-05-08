package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.common.util.CommonUtil;

public class AndMatchExpr extends AclLineMatchExpr {
  private static final String PROP_CONJUNCTS = "conjuncts";
  private static final long serialVersionUID = 1L;

  private final SortedSet<AclLineMatchExpr> _conjuncts;

  public AndMatchExpr(Iterable<AclLineMatchExpr> conjuncts) {
    this(conjuncts, null);
  }

  @JsonCreator
  public AndMatchExpr(
      @JsonProperty(PROP_CONJUNCTS) Iterable<AclLineMatchExpr> conjuncts,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description) {
    super(description);
    _conjuncts = conjuncts != null ? ImmutableSortedSet.copyOf(conjuncts) : ImmutableSortedSet.of();
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitAndMatchExpr(this);
  }

  @Override
  protected int compareSameClass(AclLineMatchExpr o) {
    return CommonUtil.compareIterable(_conjuncts, ((AndMatchExpr) o)._conjuncts);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_conjuncts, ((AndMatchExpr) o)._conjuncts);
  }

  @JsonProperty(PROP_CONJUNCTS)
  public SortedSet<AclLineMatchExpr> getConjuncts() {
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
