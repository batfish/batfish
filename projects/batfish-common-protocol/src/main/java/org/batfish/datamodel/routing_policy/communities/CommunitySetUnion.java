package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A {@link CommunitySetExpr} representing the union of the sets represented by its constituent
 * expressions.
 */
public final class CommunitySetUnion extends CommunitySetExpr {

  public CommunitySetUnion(CommunitySetExpr expr1, CommunitySetExpr expr2) {
    _expr1 = expr1;
    _expr2 = expr2;
  }

  @Override
  public <T, U> T accept(CommunitySetExprVisitor<T, U> visitor, U arg) {
    return visitor.visitCommunitySetUnion(this, arg);
  }

  @JsonProperty(PROP_EXPR1)
  public @Nonnull CommunitySetExpr getExpr1() {
    return _expr1;
  }

  @JsonProperty(PROP_EXPR2)
  public @Nonnull CommunitySetExpr getExpr2() {
    return _expr2;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CommunitySetUnion)) {
      return false;
    }
    CommunitySetUnion rhs = (CommunitySetUnion) obj;
    return _expr1.equals(rhs._expr1) && _expr2.equals(rhs._expr2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_expr1, _expr2);
  }

  private static final String PROP_EXPR1 = "expr1";
  private static final String PROP_EXPR2 = "expr2";

  @JsonCreator
  private static @Nonnull CommunitySetUnion create(
      @JsonProperty(PROP_EXPR1) @Nullable CommunitySetExpr expr1,
      @JsonProperty(PROP_EXPR2) @Nullable CommunitySetExpr expr2) {
    checkArgument(expr1 != null, "Missing %s", PROP_EXPR1);
    checkArgument(expr2 != null, "Missing %s", PROP_EXPR2);
    return new CommunitySetUnion(expr1, expr2);
  }

  private final @Nonnull CommunitySetExpr _expr1;
  private final @Nonnull CommunitySetExpr _expr2;
}
