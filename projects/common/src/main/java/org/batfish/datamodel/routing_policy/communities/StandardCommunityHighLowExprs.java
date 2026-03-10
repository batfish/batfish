package org.batfish.datamodel.routing_policy.communities;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.routing_policy.expr.IntExpr;

/**
 * An expression representing a standard community via an expression for its high 16 bits and an
 * expression for its low 16 bits.
 */
public class StandardCommunityHighLowExprs extends CommunityExpr {

  public StandardCommunityHighLowExprs(IntExpr highExpr, IntExpr lowExpr) {
    _highExpr = highExpr;
    _lowExpr = lowExpr;
  }

  @Override
  public <T, U> T accept(CommunityExprVisitor<T, U> visitor, U arg) {
    return visitor.visitStandardCommunityHighLowExprs(this, arg);
  }

  @JsonProperty(PROP_HIGH_EXPR)
  public @Nonnull IntExpr getHighExpr() {
    return _highExpr;
  }

  @JsonProperty(PROP_LOW_EXPR)
  public @Nonnull IntExpr getLowExpr() {
    return _lowExpr;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof StandardCommunityHighLowExprs)) {
      return false;
    }
    StandardCommunityHighLowExprs rhs = (StandardCommunityHighLowExprs) obj;
    return _highExpr.equals(rhs._highExpr) && _lowExpr.equals(rhs._lowExpr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_highExpr, _lowExpr);
  }

  private static final String PROP_HIGH_EXPR = "highExpr";
  private static final String PROP_LOW_EXPR = "lowExpr";

  @JsonCreator
  private static @Nonnull StandardCommunityHighLowExprs create(
      @JsonProperty(PROP_HIGH_EXPR) @Nullable IntExpr highExpr,
      @JsonProperty(PROP_LOW_EXPR) @Nullable IntExpr lowExpr) {
    checkArgument(highExpr != null, "Missing %s", PROP_HIGH_EXPR);
    checkArgument(lowExpr != null, "Missing %s", PROP_LOW_EXPR);
    return new StandardCommunityHighLowExprs(highExpr, lowExpr);
  }

  private final @Nonnull IntExpr _highExpr;
  private final @Nonnull IntExpr _lowExpr;
}
