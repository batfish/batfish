package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that evaluates whether an {@link Environment}'s route's second tag ({@link
 * BgpRoute#getTag2()}) matches a given {@link LongExpr} using a given {@link IntComparator}. A
 * route that is not a BGP route has second tag 0.
 */
@ParametersAreNonnullByDefault
public final class MatchTag2 extends BooleanExpr {
  private static final String PROP_CMP = "cmp";
  private static final String PROP_TAG2 = "tag2";

  private final @Nonnull IntComparator _cmp;
  private final @Nonnull LongExpr _tag2;

  @JsonCreator
  private static MatchTag2 jsonCreator(
      @JsonProperty(PROP_CMP) @Nullable IntComparator cmp,
      @JsonProperty(PROP_TAG2) @Nullable LongExpr tag2) {
    checkArgument(cmp != null, "%s must be provided", PROP_CMP);
    checkArgument(tag2 != null, "%s must be provided", PROP_TAG2);
    return new MatchTag2(cmp, tag2);
  }

  public MatchTag2(IntComparator cmp, LongExpr tag2) {
    _cmp = cmp;
    _tag2 = tag2;
  }

  @Override
  public <T, U> T accept(BooleanExprVisitor<T, U> visitor, U arg) {
    return visitor.visitMatchTag2(this, arg);
  }

  @Override
  public Result evaluate(Environment environment) {
    long lhs;
    if (environment.getUseOutputAttributes()) {
      lhs = tag2(environment.getOutputRoute());
    } else if (environment.getReadFromIntermediateBgpAttributes()) {
      lhs = environment.getIntermediateBgpAttributes().getTag2();
    } else {
      lhs = tag2(environment.getOriginalRoute());
    }
    long rhs = _tag2.evaluate(environment);
    return _cmp.apply(lhs, rhs);
  }

  private static long tag2(AbstractRouteBuilder<?, ?> route) {
    return route instanceof BgpRoute.Builder<?, ?> b ? b.getTag2() : 0L;
  }

  private static long tag2(AbstractRoute route) {
    return route instanceof BgpRoute<?, ?> b ? b.getTag2() : 0L;
  }

  @JsonProperty(PROP_CMP)
  public @Nonnull IntComparator getCmp() {
    return _cmp;
  }

  @JsonProperty(PROP_TAG2)
  public @Nonnull LongExpr getTag2() {
    return _tag2;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchTag2)) {
      return false;
    }
    MatchTag2 other = (MatchTag2) obj;
    return _cmp == other._cmp && Objects.equals(_tag2, other._tag2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_cmp.ordinal(), _tag2);
  }
}
