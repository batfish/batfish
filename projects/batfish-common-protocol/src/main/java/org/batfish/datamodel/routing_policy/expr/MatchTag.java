package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@ParametersAreNonnullByDefault
public final class MatchTag extends BooleanExpr {
  private static final String PROP_CMP = "cmp";
  private static final String PROP_TAG = "tag";

  /** */
  private static final long serialVersionUID = 1L;

  @Nonnull private IntComparator _cmp;

  @Nonnull private IntExpr _tag;

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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof MatchTag)) {
      return false;
    }
    MatchTag other = (MatchTag) obj;
    return _cmp == other._cmp && _tag.equals(other._tag);
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _cmp.ordinal();
    result = prime * result + _tag.hashCode();
    return result;
  }

  public void setCmp(IntComparator cmp) {
    _cmp = cmp;
  }

  public void setTag(IntExpr tag) {
    _tag = tag;
  }
}
