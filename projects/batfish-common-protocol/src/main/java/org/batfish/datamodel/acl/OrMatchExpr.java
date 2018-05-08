package org.batfish.datamodel.acl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Objects;
import java.util.SortedSet;
import javax.annotation.Nullable;
import org.batfish.common.util.CommonUtil;

public class OrMatchExpr extends AclLineMatchExpr {
  private static final String PROP_DISJUNCTS = "disjuncts";
  private static final long serialVersionUID = 1L;

  private final SortedSet<AclLineMatchExpr> _disjuncts;

  public OrMatchExpr(Iterable<AclLineMatchExpr> disjuncts) {
    this(disjuncts, null);
  }

  @JsonCreator
  public OrMatchExpr(
      @JsonProperty(PROP_DISJUNCTS) Iterable<AclLineMatchExpr> disjuncts,
      @JsonProperty(PROP_DESCRIPTION) @Nullable String description) {
    super(description);
    _disjuncts = disjuncts != null ? ImmutableSortedSet.copyOf(disjuncts) : ImmutableSortedSet.of();
  }

  @Override
  public <R> R accept(GenericAclLineMatchExprVisitor<R> visitor) {
    return visitor.visitOrMatchExpr(this);
  }

  @Override
  protected int compareSameClass(AclLineMatchExpr o) {
    return CommonUtil.compareIterable(_disjuncts, ((OrMatchExpr) o)._disjuncts);
  }

  @Override
  protected boolean exprEquals(Object o) {
    return Objects.equals(_disjuncts, ((OrMatchExpr) o)._disjuncts);
  }

  @JsonProperty(PROP_DISJUNCTS)
  public SortedSet<AclLineMatchExpr> getDisjuncts() {
    return _disjuncts;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_disjuncts);
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
