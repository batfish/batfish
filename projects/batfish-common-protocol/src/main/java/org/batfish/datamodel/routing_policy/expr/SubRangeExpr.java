package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.Environment;

@ParametersAreNonnullByDefault
public final class SubRangeExpr implements Serializable {
  private static final String PROP_FIRST = "first";
  private static final String PROP_LAST = "last";

  private @Nonnull IntExpr _first;

  private @Nonnull IntExpr _last;

  @JsonCreator
  private static SubRangeExpr jsonCreator(
      @JsonProperty(PROP_FIRST) @Nullable IntExpr first,
      @JsonProperty(PROP_LAST) @Nullable IntExpr last) {
    checkArgument(first != null, "%s must be provided", PROP_FIRST);
    checkArgument(last != null, "%s must be provided", PROP_LAST);
    return new SubRangeExpr(first, last);
  }

  public SubRangeExpr(IntExpr first, IntExpr last) {
    _first = first;
    _last = last;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof SubRangeExpr)) {
      return false;
    }
    SubRangeExpr other = (SubRangeExpr) obj;
    return _first.equals(other._first) && _last.equals(other._last);
  }

  public SubRange evaluate(Environment env) {
    int first = _first.evaluate(env);
    int last = _last.evaluate(env);
    return new SubRange(first, last);
  }

  @JsonProperty(PROP_FIRST)
  public @Nonnull IntExpr getFirst() {
    return _first;
  }

  @JsonProperty(PROP_LAST)
  public @Nonnull IntExpr getLast() {
    return _last;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _first.hashCode();
    result = prime * result + _last.hashCode();
    return result;
  }

  public void setFirst(IntExpr first) {
    _first = first;
  }

  public void setLast(IntExpr last) {
    _last = last;
  }
}
