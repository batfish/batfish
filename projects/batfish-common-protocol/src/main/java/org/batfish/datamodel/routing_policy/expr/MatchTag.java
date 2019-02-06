package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

/**
 * Boolean expression that evaluates whether an {@link Environment}'s route's BGP tag matches a
 * given {@link IntExpr} using a given {@link IntComparator}.
 */
@ParametersAreNonnullByDefault
public final class MatchTag extends BooleanExpr {
  private static final String PROP_CMP = "cmp";
  private static final String PROP_TAG = "tag";

  private static final long serialVersionUID = 1L;

  @Nonnull private final IntComparator _cmp;
  @Nonnull private final IntExpr _tag;

  @JsonCreator
  private static MatchTag jsonCreator(
      @Nullable @JsonProperty(PROP_CMP) IntComparator cmp,
      @Nullable @JsonProperty(PROP_TAG) IntExpr tag) {
    checkArgument(cmp != null, "%s must be provided", PROP_CMP);
    checkArgument(tag != null, "%s must be provided", PROP_TAG);
    return new MatchTag(cmp, tag);
  }

  public MatchTag(IntComparator cmp, IntExpr tag) {
    _cmp = cmp;
    _tag = tag;
  }

  @Override
  public Result evaluate(Environment environment) {
    int lhs;
    if (environment.getReadFromIntermediateBgpAttributes()) {
      lhs = environment.getIntermediateBgpAttributes().getTag();
    } else if (environment.getUseOutputAttributes()) {
      lhs = environment.getOutputRoute().getTag();
    } else {
      lhs = environment.getOriginalRoute().getTag();
    }
    int rhs = _tag.evaluate(environment);
    return _cmp.apply(lhs, rhs);
  }

  @JsonProperty(PROP_CMP)
  @Nonnull
  public IntComparator getCmp() {
    return _cmp;
  }

  @JsonProperty(PROP_TAG)
  @Nonnull
  public IntExpr getTag() {
    return _tag;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchTag)) {
      return false;
    }
    MatchTag other = (MatchTag) obj;
    return _cmp == other._cmp && Objects.equals(_tag, other._tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_cmp.ordinal(), _tag);
  }
}
