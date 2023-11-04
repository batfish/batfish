package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.routing_policy.Environment;

@ParametersAreNonnullByDefault
public final class ExplicitAs extends AsExpr {
  private static final String PROP_AS = "as";

  private long _as;

  @JsonCreator
  private static ExplicitAs jsonCreator(@JsonProperty(PROP_AS) @Nullable Long as) {
    checkArgument(as != null, "%s must be provided", PROP_AS);
    return new ExplicitAs(as);
  }

  public ExplicitAs(long as) {
    _as = as;
  }

  /** Use {@link #ExplicitAs(long)}. */
  @Deprecated
  public ExplicitAs(int as) {
    this((long) as);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof ExplicitAs)) {
      return false;
    }
    ExplicitAs other = (ExplicitAs) obj;
    return _as == other._as;
  }

  @Override
  public long evaluate(Environment environment) {
    return _as;
  }

  @JsonProperty(PROP_AS)
  public long getAs() {
    return _as;
  }

  @Override
  public int hashCode() {
    return Long.hashCode(_as);
  }

  public void setAs(int as) {
    _as = as;
  }
}
